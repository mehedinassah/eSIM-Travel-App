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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = PreferenceManager.getUserId(requireContext())

        val userNameText: TextView = view.findViewById(R.id.userNameText)
        val userEmailText: TextView = view.findViewById(R.id.userEmailText)
        val editProfileButton: Button = view.findViewById(R.id.editProfileButton)
        val purchaseHistoryButton: Button = view.findViewById(R.id.purchaseHistoryButton)
        val settingsButton: Button = view.findViewById(R.id.settingsButton)
        val logoutButton: Button = view.findViewById(R.id.logoutButton)

        userNameText.text = PreferenceManager.getUserName(requireContext())
        userEmailText.text = PreferenceManager.getUserEmail(requireContext())

        editProfileButton.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        purchaseHistoryButton.setOnClickListener {
            // Navigate to purchase history
        }

        settingsButton.setOnClickListener {
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
        // Refresh user data when returning from edit profile
        val userNameText: TextView? = view?.findViewById(R.id.userNameText)
        val userEmailText: TextView? = view?.findViewById(R.id.userEmailText)
        userNameText?.text = PreferenceManager.getUserName(requireContext())
        userEmailText?.text = PreferenceManager.getUserEmail(requireContext())
    }
}
