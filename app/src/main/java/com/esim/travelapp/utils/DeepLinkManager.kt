package com.esim.travelapp.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.esim.travelapp.ui.main.MainActivity

/**
 * Manages deep linking for the eSIM Travel App
 */
class DeepLinkManager(private val activity: Activity) {

    /**
     * Handle deep link based on URI
     */
    fun handleDeepLink(uri: Uri) {
        when (uri.scheme) {
            "https", "http" -> handleWebLink(uri)
            "esimtravel" -> handleAppLink(uri)
        }
    }

    private fun handleWebLink(uri: Uri) {
        val host = uri.host ?: return
        val path = uri.path ?: return

        when {
            host.contains("esimtravel.app") && path.contains("/plan") -> {
                // Handle plan link: esimtravel.app/plan/123
                val planId = path.split("/").lastOrNull()?.toIntOrNull()
                if (planId != null) {
                    navigateToPlanDetails(planId)
                }
            }
            host.contains("esimtravel.app") && path.contains("/referral") -> {
                // Handle referral link
                val code = uri.getQueryParameter("ref")
                if (!code.isNullOrEmpty()) {
                    navigateToReferral(code)
                }
            }
            host.contains("esimtravel.app") && path.contains("/support") -> {
                // Handle support link
                navigateToSupport()
            }
        }
    }

    private fun handleAppLink(uri: Uri) {
        val host = uri.host ?: return
        when (host) {
            "plan" -> {
                val planId = uri.lastPathSegment?.toIntOrNull()
                if (planId != null) {
                    navigateToPlanDetails(planId)
                }
            }
            "referral" -> {
                val code = uri.getQueryParameter("code")
                if (!code.isNullOrEmpty()) {
                    navigateToReferral(code)
                }
            }
            "support" -> navigateToSupport()
            "notification" -> {
                val type = uri.getQueryParameter("type")
                handleNotificationDeepLink(type)
            }
        }
    }

    private fun navigateToPlanDetails(planId: Int) {
        // Navigate to plan details activity
        val intent = Intent(activity, com.esim.travelapp.ui.purchase.PlanDetailsActivity::class.java)
        intent.putExtra("plan_id", planId)
        activity.startActivity(intent)
    }

    private fun navigateToReferral(code: String) {
        // Navigate to referral activity with code
        val intent = Intent(activity, com.esim.travelapp.ui.profile.ReferralActivity::class.java)
        intent.putExtra("referral_code", code)
        activity.startActivity(intent)
    }

    private fun navigateToSupport() {
        val intent = Intent(activity, com.esim.travelapp.ui.support.SupportActivity::class.java)
        activity.startActivity(intent)
    }

    private fun handleNotificationDeepLink(type: String?) {
        when (type) {
            "plan_activation" -> {
                val intent = Intent(activity, MainActivity::class.java)
                intent.putExtra("tab", "dashboard")
                activity.startActivity(intent)
            }
            "low_balance" -> {
                val intent = Intent(activity, MainActivity::class.java)
                intent.putExtra("tab", "storefront")
                activity.startActivity(intent)
            }
            "support" -> navigateToSupport()
        }
    }

    /**
     * Generate shareable link for a plan
     */
    fun generatePlanShareLink(planId: Int): String {
        return "https://esimtravel.app/plan/$planId"
    }

    /**
     * Generate shareable referral link
     */
    fun generateReferralShareLink(code: String): String {
        return "https://esimtravel.app/join?ref=$code"
    }

    companion object {
        /**
         * Create an Intent from a deep link
         */
        fun createDeepLinkIntent(activity: Activity, uri: Uri): Intent? {
            return when (uri.scheme) {
                "https", "http" -> {
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    if (intent.resolveActivity(activity.packageManager) != null) intent else null
                }
                "esimtravel" -> Intent(activity, MainActivity::class.java).apply {
                    data = uri
                }
                else -> null
            }
        }
    }
}
