# Stripe Payment Integration & Testing Guide

## 🎯 Quick Setup (5 minutes)

### 1. Get Your Stripe Publishable Key
- Go to **[Stripe Dashboard](https://dashboard.stripe.com)**
- Click **Developers → API Keys** (top right)
- Copy the **Publishable key** (starts with `pk_test_`)
- Example: `pk_test_51A234567890BcD12E3FGhIJKlMnOpQr`

### 2. Add Key to Your Project

**Option A: BuildConfig (Recommended - Secure)**
Open `app/build.gradle` and update:
```gradle
defaultConfig {
    // ... other config ...
    buildConfigField "String", "STRIPE_PUBLISHABLE_KEY", "\"pk_test_YOUR_ACTUAL_KEY_HERE\""
}
```

**Then rebuild:**
```bash
.\gradlew.bat clean build -x test
```

**Option B: Direct Edit (Testing Only)**
Edit [PaymentManager.kt](../../app/src/main/java/com/esim/travelapp/utils/PaymentManager.kt) directly and replace:
```kotlin
private val stripe: Stripe = Stripe(
    context,
    "pk_test_YOUR_PUBLISHABLE_KEY_HERE"  // ← Replace this
)
```

---

## 🧪 Testing Payment Flows

### Step 1: Launch Payment Test Activity
```bash
# After building, run on emulator/device:
adb shell am start -n com.esim.travelapp/.PaymentTestActivity
```

Or add a shortcut to MainActivity to launch it:
```kotlin
// In MainActivity
val testPaymentButton = findViewById<Button>(R.id.testPaymentButton)
testPaymentButton.setOnClickListener {
    startActivity(Intent(this, PaymentTestActivity::class.java))
}
```

### Step 2: Use Stripe Test Cards

**All test cards expire: 12/25, CVC: 123 (any 3-4 digits)**

| Card Type | Number | Expected Result |
|-----------|--------|-----------------|
| ✅ Success | 4242 4242 4242 4242 | Payment succeeds |
| 💳 Visa | 4000 0000 0000 0002 | Payment declines |
| 💳 Visa | 4000 0000 0000 9995 | Card is declined (insufficient funds) |
| 💳 Mastercard | 5555 5555 5555 4444 | Payment succeeds |
| 💳 American Express | 3782 822463 10005 | Payment succeeds |

### Step 3: Test All Payment Methods

1. **Credit Card Payment**
   - Card: `4242 4242 4242 4242`
   - Month: `12` | Year: `25` | CVC: `123`
   - Amount: Any value (e.g., `29.99`)
   - Click **Test Card Payment**
   - ✅ Expected: Green success message with Transaction ID

2. **Declined Card**
   - Click **Test Declined Card (4000 0000 0000 0002)**
   - ❌ Expected: Red error message

3. **Digital Wallets**
   - Click **Test Google Pay** (currently shows "coming soon")
   - Click **Test Bank Transfer** (currently shows "coming soon")

---

## 📱 Integration with Real Payment Activity

The payment testing is already integrated into your [PaymentActivity.kt](../../app/src/main/java/com/esim/travelapp/ui/purchase/PaymentActivity.kt):

```kotlin
// When user clicks "Pay Now" button:
paymentViewModel.processPayment(
    userId = currentUserId,
    purchaseId = purchaseId,
    amount = amount,
    method = "card",  // or "google_pay", "bank_transfer"
    transactionRef = transactionRef
)

// Observe results:
paymentViewModel.paymentState.observe(this) { state ->
    when (state) {
        is PaymentState.Success -> {
            // Payment succeeded - navigate to confirmation
        }
        is PaymentState.Failed -> {
            // Payment failed - show error
        }
    }
}
```

---

## 🔒 Security Best Practices

✅ **DO:**
- Store publishable key in `BuildConfig` (automatically generated)
- Keep secret key on backend only (never in app)
- Use HTTPS for all payment communication
- Validate amounts on backend
- Never store full card numbers

❌ **DON'T:**
- Hardcode keys in source code (use BuildConfig or BuildFlavors)
- Log sensitive payment data
- Send card data to your backend (use Stripe tokens instead)
- Test with real payment methods

---

## 🚀 Production Checklist

Before launching to Play Store:

- [ ] Replace `pk_test_*` with production key `pk_live_*`
- [ ] Set up Stripe webhook handlers for payment confirmation
- [ ] Implement payment receipt generation
- [ ] Add payment history to user profile
- [ ] Test with real Stripe test mode
- [ ] Implement error handling for network failures
- [ ] Add payment timeout handling (30 seconds max)
- [ ] Remove `PaymentTestActivity` from AndroidManifest.xml
- [ ] Delete `PaymentTestActivity.kt` and `activity_payment_test.xml`
- [ ] Obfuscate code with ProGuard/R8:
  ```gradle
  buildTypes {
      release {
          minifyEnabled true
          shrinkResources true
      }
  }
  ```

---

## 📊 Payment Flow Diagram

```
User Selects Plan
        ↓
Clicks "Buy Plan" Button
        ↓
PaymentActivity Opens
        ↓
Displays Payment Methods:
├── 💳 Credit/Debit Card
├── 🔵 Google Pay
└── 🏦 Bank Transfer
        ↓
User Selects Method & Amount
        ↓
PaymentViewModel.processPayment()
        ↓
PaymentManager.processCardPayment()
        ↓
Stripe API Call (via backend)
        ↓
Success? → PaymentConfirmationActivity
Failure? → PaymentFailureActivity
```

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| **BuildConfig not generated** | Run `./gradlew clean build` |
| **Stripe key not found** | Check BuildConfig in `app/build/generated/source/buildConfig/` |
| **Payment always fails** | Verify test mode key (starts with `pk_test_`) |
| **Cannot find PaymentTestActivity** | Ensure AndroidManifest.xml has the activity entry |
| **Card validation error** | Use Stripe test card: 4242 4242 4242 4242 |
| **CVC error** | Test cards accept any 3-4 digit CVC |

---

## 📚 Useful Resources

- **Stripe Documentation**: https://stripe.com/docs/testing
- **Stripe Test Mode**: https://stripe.com/docs/keys#test-live-modes
- **Android Stripe SDK**: https://github.com/stripe/stripe-android
- **Payment Security**: https://stripe.com/docs/security

---

## ✅ Verification Steps

1. **Verify Stripe Key Added:**
   ```bash
   grep -r "STRIPE_PUBLISHABLE_KEY" app/build.gradle
   ```

2. **Verify BuildConfig Generated:**
   ```bash
   cat app/build/generated/source/buildConfig/debug/com/esim/travelapp/BuildConfig.java | grep STRIPE
   ```

3. **Test Payment Flow:**
   - Build & run app
   - Launch PaymentTestActivity
   - Enter test card: `4242 4242 4242 4242`
   - Verify green success message

---

**Last Updated:** April 2026  
**Status:** ✅ Ready for testing
