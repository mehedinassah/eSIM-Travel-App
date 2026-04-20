# Firebase Setup Guide for eSIM Travel App

## Complete Step-by-Step Instructions

---

## ✅ Step 1: Create Firebase Project

### 1.1 Go to Firebase Console
- Visit: https://console.firebase.google.com
- Sign in with your Google account

### 1.2 Create New Project
1. Click **"Create a project"** button
2. Enter project name: `eSIM Travel App`
3. Click **Continue**
4. Choose analytics settings (optional but recommended)
5. Select default Analytics location: **United States**
6. Click **Create project**
7. Wait 2-3 minutes for project to be created

### 1.3 Verify Project Created
- You should see a dashboard with "Get started by adding Firebase to your app"

---

## ✅ Step 2: Register Android App in Firebase

### 2.1 Add Android App
1. On Firebase dashboard, click **Android** icon (looks like Android logo)
2. You'll see "Register app" form

### 2.2 Fill App Registration Form
```
Package name: com.esim.travelapp
App nickname: eSIM Travel App (optional)
Debug signing certificate SHA-1: (see Step 2.3)
```

### 2.3 Get Your App's SHA-1 Certificate

**Option A: Using Android Studio (Easiest)**
1. Open your project in Android Studio
2. Go to **Tools** → **Google Play Console** → **App Signing**
3. Or go to **Build** → **Analyze APK**
4. Or right-click project → **Open Module Settings**
5. Go to **Build** tab
6. Scroll to find SHA-1 hash

**Option B: Using Terminal (Windows)**
```bash
# Navigate to your Android SDK directory or use:
cd C:\Users\nassa\.android

# List certificates:
keytool -list -v -keystore debug.keystore

# When prompted, password is: android
# Copy the SHA1 value
```

**Option C: Using Gradle**
```bash
# In your project directory, run:
./gradlew signingReport

# Look for debugAndroidTest's SHA1
```

### 2.4 Enter SHA-1 and Register
1. Copy your SHA-1 hash
2. Paste into Firebase registration form
3. Click **Register app**

---

## ✅ Step 3: Download google-services.json

### 3.1 Download Configuration File
1. After registering app, Firebase shows: **"Download google-services.json"**
2. Click **Download google-services.json**
3. A JSON file will download to your computer

### 3.2 Place File in Project
1. In Android Studio, go to **Project** view (not Android)
2. Navigate to: `app/` folder (NOT `app/src/`)
3. Paste `google-services.json` here:
   ```
   eSIM Travel App/
   ├── app/
   │   ├── google-services.json  ← Place here
   │   ├── src/
   │   ├── build.gradle
   │   └── ...
   ```

### 3.3 Verify Placement
- File should be at: `c:\Users\nassa\AndroidStudioProjects\eSIM Travel App\app\google-services.json`
- NOT in: `app/src/main/` or any other subfolder

---

## ✅ Step 4: Configure build.gradle Files

### 4.1 Project-Level build.gradle
Location: `eSIM Travel App/build.gradle` (at root level)

Add this in the `plugins` section:
```gradle
plugins {
    id 'com.google.gms.google-services' version '4.3.15' apply false
}
```

Full example:
```gradle
plugins {
    id 'com.android.application' version '7.4.0' apply false
    id 'com.android.library' version '7.4.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.22' apply false
    id 'com.google.gms.google-services' version '4.3.15' apply false  // ADD THIS LINE
}
```

### 4.2 App-Level build.gradle
Location: `app/build.gradle`

**Step 1:** Add plugin at the TOP of plugins section:
```gradle
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-kapt'
    id 'com.google.gms.google-services'  // ADD THIS LINE
}
```

**Step 2:** Add Firebase BOM in dependencies:
```gradle
dependencies {
    // Firebase BOM (Billing of Materials) - manages all Firebase versions
    implementation 'com.google.firebase:firebase-bom:32.7.0'
    
    // Firebase services (no need to specify versions, BOM manages them)
    implementation 'com.google.firebase:firebase-messaging-ktx'
    implementation 'com.google.firebase:firebase-analytics-ktx'
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-database-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'
    implementation 'com.google.firebase:firebase-storage-ktx'
}
```

**Step 3:** Sync Gradle
- Android Studio will show: "Sync Now" button
- Click it to download Firebase dependencies

---

## ✅ Step 5: Enable Firebase Services

### 5.1 In Firebase Console
1. Go back to Firebase Console: https://console.firebase.google.com
2. Select your project
3. Go to left sidebar

### 5.2 Enable Cloud Messaging
1. Click **Cloud Messaging** (under Build section)
2. Click **Enable Cloud Messaging**
3. You'll see:
   - Server API Key
   - Sender ID (copy this!)

**Save these values:**
- Sender ID: `YOUR_SENDER_ID` (needed for testing)

### 5.3 Enable Analytics
1. Click **Analytics** in sidebar
2. Click **Enable Analytics**
3. Choose location and reporting preference
4. Click **Enable**

### 5.4 Enable Realtime Database (Optional)
1. Click **Realtime Database** in sidebar
2. Click **Create Database**
3. Choose location and security rules
4. Use `Test mode` for development (NOT production)

### 5.5 Enable Firestore (Optional)
1. Click **Firestore Database** in sidebar
2. Click **Create Database**
3. Choose location and security rules
4. Use `Test mode` for development

---

## ✅ Step 6: Configure AndroidManifest.xml

### 6.1 Add Internet Permission
In `AndroidManifest.xml`, add these permissions:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Firebase needs internet -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- Notification permission (Android 13+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application>
        <!-- Your existing config -->
    </application>

</manifest>
```

---

## ✅ Step 7: Initialize Firebase in Your App

### 7.1 In MainActivity.kt (or Application class)
Add initialization in `onCreate()`:

```kotlin
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase (usually done automatically)
        FirebaseApp.initializeApp(this)

        // Get FCM token (for testing notifications)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "getInstanceId failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "FCM Token: $token")
            // You can save this token to your backend
        }
    }
}
```

---

## ✅ Step 8: Implement Firebase Cloud Messaging

### 8.1 You Already Have This!
Your `ESIMMessagingService.kt` is already set up. It's in:
```
app/src/main/java/com/esim/travelapp/service/ESIMMessagingService.kt
```

### 8.2 Verify Service Implementation
Check that you have these methods:

```kotlin
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ESIMMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        // Handle notification
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            sendNotification(it.title!!, it.body!!)
        }

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        // Send token to your server for future notifications
    }

    private fun sendNotification(title: String, message: String) {
        // Create notification channel for Android 8.0+
        val channelId = "default"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0+ requires notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val type = data["type"]
        when (type) {
            "plan_activation" -> {
                Log.d("FCM", "Plan activated: ${data["plan_name"]}")
            }
            "low_balance" -> {
                Log.d("FCM", "Low balance: ${data["remaining_data"]}")
            }
            "plan_expiring" -> {
                Log.d("FCM", "Plan expiring in: ${data["days"]} days")
            }
            "promotional" -> {
                Log.d("FCM", "Promo: ${data["offer"]}")
            }
        }
    }
}
```

### 8.3 Register Service in Manifest
Already done! Check that you have in `AndroidManifest.xml`:

```xml
<service android:name=".service.ESIMMessagingService">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

---

## ✅ Step 9: Test Firebase Cloud Messaging

### 9.1 Get Your FCM Token
1. Run app on device/emulator
2. Open **Logcat** in Android Studio
3. Filter by "FCM"
4. You'll see: `FCM Token: eZXr2Y...abc123...` (long string)
5. **Copy this token** - you'll need it for testing

### 9.2 Send Test Notification
1. Go to Firebase Console
2. Select your project
3. Go to **Cloud Messaging** (left sidebar under Engage)
4. Click **Send your first message**
5. Fill in the form:

**Message Details:**
- Notification title: `Test Notification`
- Notification text: `This is a test message from Firebase`
- Target: Select "By token"
- Token: Paste your FCM token from step 9.1

**Scheduling:**
- Send now

6. Click **Send**
7. If app is running, you'll see notification appear!

---

## ✅ Step 10: Use Firebase Analytics

### 10.1 Analytics Auto-Enabled
Firebase Analytics is automatically enabled. It tracks:
- App launches
- Screen views
- Crashes
- Custom events

### 10.2 Log Custom Events
In your code (e.g., when user purchases):

```kotlin
import com.google.firebase.analytics.FirebaseAnalytics
import android.os.Bundle

class PurchaseActivity : AppCompatActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    fun onPlanPurchased(planId: String, price: Double) {
        val params = Bundle().apply {
            putString("plan_id", planId)
            putDouble("price", price)
            putString("currency", "USD")
        }
        firebaseAnalytics.logEvent("plan_purchased", params)
    }

    fun onPlanViewed(planName: String) {
        val params = Bundle().apply {
            putString("plan_name", planName)
        }
        firebaseAnalytics.logEvent("plan_viewed", params)
    }
}
```

### 10.3 View Analytics Data
1. Go to Firebase Console
2. Click **Analytics** in sidebar
3. Click **Dashboard**
4. View real-time users, events, etc.
5. Wait 24 hours for full data processing

---

## ✅ Step 11: Use Firebase Authentication

### 11.1 Enable Authentication
1. Go to Firebase Console
2. Click **Authentication** in sidebar
3. Click **Get started**
4. Enable **Email/Password** provider
5. Click **Save**

### 11.2 Create User
In your LoginActivity or RegisterActivity:

```kotlin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
    }

    fun registerUser(email: String, password: String, displayName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "User created successfully")
                    
                    // Set display name
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d("Auth", "User profile updated")
                            }
                        }
                    
                    // Navigate to main screen
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Log.w("Auth", "Authentication failed", task.exception)
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
```

### 11.3 Login User
```kotlin
fun loginUser(email: String, password: String) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d("Auth", "User logged in successfully")
                val user = auth.currentUser
                Log.d("Auth", "User: ${user?.email}")
                
                // Navigate to main screen
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                Log.w("Auth", "Login failed", task.exception)
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
}
```

---

## ✅ Step 12: Use Firebase Realtime Database

### 12.1 Enable Realtime Database
1. Go to Firebase Console
2. Click **Realtime Database** in sidebar
3. Click **Create Database**
4. Location: Select your region
5. Security rules: Choose **Start in test mode** (for development)
6. Click **Enable**

### 12.2 Write Data
```kotlin
import com.google.firebase.database.FirebaseDatabase

fun saveUserProfile(userId: String, name: String, email: String) {
    val database = FirebaseDatabase.getInstance()
    val userRef = database.getReference("users/$userId")
    
    val userMap = mapOf(
        "name" to name,
        "email" to email,
        "created_at" to System.currentTimeMillis()
    )
    
    userRef.setValue(userMap)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Database", "User saved successfully")
            } else {
                Log.w("Database", "Failed to save user", task.exception)
            }
        }
}
```

### 12.3 Read Data
```kotlin
fun getUserProfile(userId: String) {
    val database = FirebaseDatabase.getInstance()
    val userRef = database.getReference("users/$userId")
    
    userRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val name = snapshot.child("name").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                Log.d("Database", "User: $name, $email")
            }
        }
        
        override fun onCancelled(error: DatabaseError) {
            Log.w("Database", "Failed to read value", error.toException())
        }
    })
}
```

---

## ✅ Step 13: Use Cloud Firestore

### 13.1 Enable Firestore
1. Go to Firebase Console
2. Click **Firestore Database** in sidebar
3. Click **Create Database**
4. Location: Select your region
5. Security rules: Choose **Start in test mode**
6. Click **Enable**

### 13.2 Write Document
```kotlin
import com.google.firebase.firestore.FirebaseFirestore

fun savePlanPurchase(userId: String, planId: String, price: Double) {
    val db = FirebaseFirestore.getInstance()
    
    val purchase = mapOf(
        "plan_id" to planId,
        "price" to price,
        "purchased_at" to com.google.firebase.Timestamp.now(),
        "status" to "active"
    )
    
    db.collection("users").document(userId)
        .collection("purchases").document()
        .set(purchase)
        .addOnSuccessListener {
            Log.d("Firestore", "Purchase saved")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Failed to save", e)
        }
}
```

### 13.3 Query Documents
```kotlin
fun getUserPurchases(userId: String) {
    val db = FirebaseFirestore.getInstance()
    
    db.collection("users").document(userId)
        .collection("purchases")
        .orderBy("purchased_at", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed", e)
                return@addSnapshotListener
            }
            
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val planId = doc.getString("plan_id")
                    val price = doc.getDouble("price")
                    Log.d("Firestore", "Purchase: $planId - $price")
                }
            }
        }
}
```

---

## ✅ Step 14: Handle Firebase Errors

### 14.1 Common Errors & Solutions

**Error: "google-services.json not found"**
- Solution: Place file at: `app/google-services.json` (NOT in any subfolder)
- Sync Gradle files

**Error: "FirebaseApp is not initialized"**
- Solution: Make sure you have:
  ```gradle
  implementation 'com.google.firebase:firebase-bom:32.7.0'
  ```
- Add Firebase dependencies in build.gradle

**Error: "Authentication failed with unknown error"**
- Solution: Check in Firebase Console
- Make sure Email/Password provider is enabled
- Check user registration is correct

**Error: "Permission denied" for Database/Firestore**
- Solution: Go to Firebase Console
- Click Security Rules tab
- Change to "Test mode" temporarily for development
- Later, set proper security rules

### 14.2 Debug Mode
Add this to your Application class:

```kotlin
import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable Firebase database logging (development only)
        FirebaseDatabase.getInstance().setLoggingEnabled(BuildConfig.DEBUG)
    }
}
```

---

## ✅ Step 15: Security Best Practices

### 15.1 Hide Sensitive Keys
NEVER commit API keys to Git!

Create `local.properties`:
```properties
STRIPE_PUBLIC_KEY=pk_test_YOUR_KEY
FIREBASE_SERVER_KEY=YOUR_SERVER_KEY
```

Add to `.gitignore`:
```
local.properties
google-services.json
```

### 15.2 Set Security Rules
For Firestore, go to Rules tab:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Only authenticated users can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Public read-only data
    match /plans/{document=**} {
      allow read: if true;
      allow write: if false;
    }
  }
}
```

---

## ✅ Checklist

- [ ] Created Firebase project
- [ ] Registered Android app
- [ ] Downloaded google-services.json
- [ ] Placed file in app/ folder
- [ ] Added Firebase plugin to build.gradle files
- [ ] Added Firebase dependencies
- [ ] Synced Gradle
- [ ] Enabled Cloud Messaging in Firebase Console
- [ ] Enabled Analytics
- [ ] Added internet permission to manifest
- [ ] Registered ESIMMessagingService in manifest
- [ ] Tested notifications (got FCM token, sent test message)
- [ ] Enabled Authentication (optional)
- [ ] Enabled Realtime Database (optional)
- [ ] Enabled Firestore (optional)
- [ ] Tested app runs without errors

---

## 📱 Testing on Real Device vs Emulator

### Real Device (Recommended for Notifications)
1. Connect Android phone via USB
2. Enable Developer Mode: Settings → About → Tap Build Number 7 times
3. Allow USB Debugging when prompted
4. Click "Run" in Android Studio
5. Select your device

### Emulator
1. Create Android Virtual Device in Android Studio
2. Recommended: Android 12 or higher
3. Click "Run" in Android Studio
4. Select emulator

**Note:** Notifications work better on real devices with Google Play Services installed.

---

## 🔗 Useful Links

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Console](https://console.firebase.google.com)
- [Cloud Messaging Docs](https://firebase.google.com/docs/cloud-messaging)
- [Authentication Docs](https://firebase.google.com/docs/auth)
- [Firestore Docs](https://firebase.google.com/docs/firestore)
- [Realtime Database Docs](https://firebase.google.com/docs/database)

---

## ✨ Next Steps

1. **Complete all 15 steps above**
2. **Test notifications** by sending test message
3. **Monitor Analytics** to see real-time data
4. **Implement Authentication** when ready
5. **Add Firestore** for storing user purchases
6. **Set up Security Rules** before production

---

## 💡 Tips

- Always use test keys/databases first before production
- Monitor Firebase costs in Console (free tier available)
- Use emulator for development (cheaper than real device)
- Test on real device before deploying to Play Store
- Enable Firebase Analytics early to track user behavior
- Keep google-services.json in .gitignore

---

**Your app is now ready to use Firebase! 🚀**
