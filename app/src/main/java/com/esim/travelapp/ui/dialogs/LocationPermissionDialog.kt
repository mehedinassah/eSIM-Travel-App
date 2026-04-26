package com.esim.travelapp.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.esim.travelapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LocationPermissionDialog(
    private val context: Context,
    private val onPermissionSelected: (String) -> Unit
) {

    fun show(): Dialog {
        // Create RadioGroup with options
        val radioGroup = RadioGroup(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(20, 20, 20, 20)
        }

        // Option 1
        val option1 = RadioButton(context).apply {
            text = "Allow all the time"
            id = 1
            isChecked = false
            textSize = 16f
            setPadding(10, 20, 10, 20)
        }
        radioGroup.addView(option1)

        // Option 2
        val option2 = RadioButton(context).apply {
            text = "Allow only while using the app"
            id = 2
            isChecked = false
            textSize = 16f
            setPadding(10, 20, 10, 20)
        }
        radioGroup.addView(option2)

        // Option 3
        val option3 = RadioButton(context).apply {
            text = "Not now"
            id = 3
            isChecked = true
            textSize = 16f
            setPadding(10, 20, 10, 20)
        }
        radioGroup.addView(option3)

        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Your Location")
        builder.setMessage("The app uses location to show plans available in your region.\n\nChoose the location access level:")
        builder.setView(radioGroup)

        builder.setPositiveButton("Continue") { dialog, _ ->
            when (radioGroup.checkedRadioButtonId) {
                1 -> onPermissionSelected("PERMISSION_ALWAYS")
                2 -> onPermissionSelected("PERMISSION_WHILE_USING")
                3 -> onPermissionSelected("SKIP")
            }
            dialog.dismiss()
        }

        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        return dialog
    }
}
