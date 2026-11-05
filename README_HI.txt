ABVIVA LOAN CALCULATOR - ANDROID PROJECT

इस प्रोजेक्ट में आपके दिए हुए ABVIVA Ventures लोगो को Android ऐप के Launcher Icon में लगाया गया है।
ऐप WebView के अंदर मौजूदा Loan Calculator HTML चलाता है। EMI, Dealer Disbursement, Scheme Comparison और बाकी कैलकुलेशन HTML ऐप के समान हैं।

Android Studio से APK बनाने का तरीका:
1. ZIP को Extract करें।
2. Android Studio खोलें और Open चुनकर इस फोल्डर को खोलें।
3. Gradle Sync पूरा होने दें। Android SDK 35 और JDK 17 उपलब्ध होना चाहिए।
4. Build > Build Bundle(s) / APK(s) > Build APK(s) चुनें।
5. APK इस पथ पर बनेगा: app/build/outputs/apk/debug/app-debug.apk

GitHub Actions से APK:
इस प्रोजेक्ट को GitHub repository में push करें। Actions टैब में “Build ABVIVA Android APK” workflow चलाएँ।
पूरा होने पर workflow के Artifacts में ABVIVA-Loan-Calculator-APK मिलेगा।

Package ID: com.abviva.loancalculator
Minimum Android: Android 6.0 (API 23)
Version: 1.0 (Debug build)

नोट: यह Android Studio में build करने योग्य source project है। इस environment में Android SDK/Gradle build tools उपलब्ध नहीं थे, इसलिए यहाँ APK binary compile नहीं की गई है।
