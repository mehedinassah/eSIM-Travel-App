package com.esim.travelapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.esim.travelapp.R
import com.esim.travelapp.ui.auth.LoginActivity
import com.esim.travelapp.ui.profile.EditProfileActivity
import com.esim.travelapp.ui.profile.SettingsActivity
import com.esim.travelapp.utils.PreferenceManager

class ProfileFragment : Fragment() {

    private var currentUserId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_professional, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = PreferenceManager.getUserId(requireContext())

        // Profile header info
        val profileUserName: TextView = view.findViewById(R.id.profileUserName)
        val profileUserEmail: TextView = view.findViewById(R.id.profileUserEmail)
        val profileActivePlans: TextView = view.findViewById(R.id.profileActivePlans)
        val profileTotalSpent: TextView = view.findViewById(R.id.profileTotalSpent)
        val profileCountriesVisited: TextView = view.findViewById(R.id.profileCountriesVisited)

        profileUserName.text = PreferenceManager.getUserName(requireContext())
        profileUserEmail.text = PreferenceManager.getUserEmail(requireContext())
        // TODO: Load actual stats from database
        profileActivePlans.text = "0"
        profileTotalSpent.text = "$0"
        profileCountriesVisited.text = "0"

        // Menu options
        val wishlistMenu: LinearLayout = view.findViewById(R.id.wishlistMenuOption)
        val referralMenu: LinearLayout = view.findViewById(R.id.referralMenuOption)
        val autoRenewalMenu: LinearLayout = view.findViewById(R.id.autoRenewalMenuOption)
        val supportMenu: LinearLayout = view.findViewById(R.id.supportMenuOption)
        val settingsMenu: LinearLayout = view.findViewById(R.id.settingsMenuOption)
        val logoutButton: Button = view.findViewById(R.id.logoutButton)

        wishlistMenu.setOnClickListener {
            // TODO: Navigate to wishlist
        }

        referralMenu.setOnClickListener {
            startActivity(Intent(requireContext(), com.esim.travelapp.ui.profile.ReferralActivity::class.java))
        }

        autoRenewalMenu.setOnClickListener {
            startActivity(Intent(requireContext(), com.esim.travelapp.ui.profile.AutoRenewalActivity::class.java))
        }

        supportMenu.setOnClickListener {
            startActivity(Intent(requireContext(), com.esim.travelapp.ui.support.SupportActivity::class.java))
        }

        settingsMenu.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        logoutButton.setOnClickListener {
            PreferenceManager.clearUser(requireContext())
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning
        val profileUserName: TextView? = view?.findViewById(R.id.profileUserName)
        val profileUserEmail: TextView? = view?.findViewById(R.id.profileUserEmail)
        profileUserName?.text = PreferenceManager.getUserName(requireContext())
        profileUserEmail?.text = PreferenceManager.getUserEmail(requireContext())
    }
}
