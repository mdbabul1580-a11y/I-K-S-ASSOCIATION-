package com.example.somitihisab

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("somiti_data", MODE_PRIVATE) }
    private val members = mutableListOf<Member>()
    private lateinit var root: LinearLayout
    private lateinit var summary: TextView
    data class Member(val id: Int, var name: String, var amount: Long)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadMembers()
        if (members.isEmpty()) {
            for (i in 1..10) members.add(Member(i, "সদস্য $i", 5000L))
            saveMembers()
        }
        buildUi()
    }

    private fun money(n: Long): String = "৳" + NumberFormat.getNumberInstance(Locale("en", "IN")).format(n)
    private fun total(): Long = members.sumOf { it.amount }
    private fun dp(n: Int): Int = (n * resources.displayMetrics.density).toInt()

    private fun buildUi() {
        val scroll = ScrollView(this)
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(24))
            setBackgroundColor(0xFFF6F8F6.toInt())
        }
        scroll.addView(root)
        setContentView(scroll)

        val title = TextView(this).apply {
            text = "সমিতির হিসাব"
            textSize = 27f
            setTextColor(0xFF125C3A.toInt())
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(16))
        }
        root.addView(title)
        summary = TextView(this).apply {
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(dp(16), dp(18), dp(16), dp(18))
            setBackgroundColor(0xFF16794B.toInt())
        }
        root.addView(summary, matchWrap())
        val hint = TextView(this).apply {
            text = "সদস্যের জমা যোগ করুন অথবা নাম পরিবর্তন করুন।"
            textSize = 14f
            setTextColor(0xFF526257.toInt())
            setPadding(0, dp(14), 0, dp(8))
        }
        root.addView(hint)
        val addMember = Button(this).apply {
            text = "＋ নতুন সদস্য যোগ করুন"
            setOnClickListener { addMemberDialog() }
        }
        root.addView(addMember, matchWrap())
        renderMembers()
    }

    private fun renderMembers() {
        // Remove member rows while keeping title, summary, hint and add button.
        while (root.childCount > 4) root.removeViewAt(4)
        summary.text = "মোট জমা\n${money(total())}\nসদস্য: ${members.size} জন"
        members.forEach { member ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(12), dp(14), dp(12))
                setBackgroundColor(0xFFFFFFFF.toInt())
                val params = LinearLayout.LayoutParams(-1, -2)
                params.topMargin = dp(9)
                layoutParams = params
            }
            val name = TextView(this).apply {
                text = member.name
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(0xFF202820.toInt())
            }
            val amount = TextView(this).apply {
                text = "মোট জমা: ${money(member.amount)}"
                textSize = 16f
                setTextColor(0xFF16794B.toInt())
                setPadding(0, dp(4), 0, dp(8))
            }
            val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val deposit = Button(this).apply {
                text = "+ জমা"
                setOnClickListener { depositDialog(member) }
            }
            val edit = Button(this).apply {
                text = "নাম পরিবর্তন"
                setOnClickListener { renameDialog(member) }
            }
            buttons.addView(deposit, LinearLayout.LayoutParams(0, -2, 1f))
            buttons.addView(edit, LinearLayout.LayoutParams(0, -2, 1f))
            card.addView(name); card.addView(amount); card.addView(buttons)
            root.addView(card, matchWrap())
        }
    }

    private fun depositDialog(member: Member) {
        val input = EditText(this).apply {
            hint = "টাকার পরিমাণ লিখুন"
            inputType = 2
        }
        AlertDialog.Builder(this)
            .setTitle("${member.name} — নতুন জমা")
            .setView(input)
            .setPositiveButton("জমা যোগ করুন") { _, _ ->
                val value = input.text.toString().trim().toLongOrNull()
                if (value != null && value > 0) {
                    member.amount += value
                    saveMembers(); renderMembers()
                    Toast.makeText(this, "জমা সংরক্ষণ হয়েছে", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(this, "সঠিক টাকার পরিমাণ লিখুন", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("বাতিল", null).show()
    }

    private fun renameDialog(member: Member) {
        val input = EditText(this).apply { setText(member.name); hint = "সদস্যের নাম" }
        AlertDialog.Builder(this).setTitle("নাম পরিবর্তন") .setView(input)
            .setPositiveButton("সংরক্ষণ") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isNotEmpty()) { member.name = value; saveMembers(); renderMembers() }
            }.setNegativeButton("বাতিল", null).show()
    }

    private fun addMemberDialog() {
        val input = EditText(this).apply { hint = "নতুন সদস্যের নাম" }
        AlertDialog.Builder(this).setTitle("নতুন সদস্য যোগ করুন").setView(input)
            .setPositiveButton("যোগ করুন") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isNotEmpty()) {
                    val id = (members.maxOfOrNull { it.id } ?: 0) + 1
                    members.add(Member(id, value, 0L)); saveMembers(); renderMembers()
                }
            }.setNegativeButton("বাতিল", null).show()
    }

    private fun saveMembers() {
        val array = JSONArray()
        members.forEach { array.put(JSONObject().put("id", it.id).put("name", it.name).put("amount", it.amount)) }
        prefs.edit().putString("members", array.toString()).apply()
    }

    private fun loadMembers() {
        val raw = prefs.getString("members", null) ?: return
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                members.add(Member(o.getInt("id"), o.getString("name"), o.getLong("amount")))
            }
        } catch (_: Exception) { members.clear() }
    }

    private fun matchWrap() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
}
