সমিতি হিসাব অ্যাপ - মোবাইল থেকে APK তৈরির নোট

এই ফোল্ডারটি Android project। APK বানাতে GitHub Actions ব্যবহার করা যায়।
প্রজেক্টের মূল ফাইলগুলো repository-র root-এ (SomitiHisab ফোল্ডারের ভিতরের ফাইলগুলো) আপলোড করতে হবে।
.github/workflows/android.yml ফাইলটি রাখতে হবে।
GitHub-এ main branch-এ ফাইলগুলো commit করলে Actions build শুরু করবে।
Actions > Build SomitiHisab APK > সর্বশেষ সফল run > Artifacts থেকে SomitiHisab-debug-apk ডাউনলোড করুন।
ZIP artifact খুলে app-debug.apk ফোনে ইনস্টল করুন। Android নিরাপত্তা সতর্কতা দেখালে শুধুমাত্র নিজের তৈরি ও যাচাই করা APK-র জন্য অনুমতি দিন।
এই debug APK পরীক্ষার জন্য; Google Play প্রকাশের জন্য নয়।
