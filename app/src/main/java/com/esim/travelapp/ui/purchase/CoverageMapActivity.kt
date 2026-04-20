package com.esim.travelapp.ui.purchase

import android.os.Bundle
import android.widget.TextView
import com.esim.travelapp.R
import com.esim.travelapp.ui.BaseActivity

class CoverageMapActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coverage_map)

        val planId = intent.getIntExtra("plan_id", 0)
        val country = intent.getStringExtra("country") ?: ""

        setupUI(country)
    }

    private fun setupUI(country: String) {
        val titleTextView: TextView = findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = findViewById(R.id.descriptionTextView)
        val coverageTextView: TextView = findViewById(R.id.coverageTextView)

        titleTextView.text = "$country Coverage Map"
        
        // Simulate coverage information
        when (country) {
            "USA" -> {
                descriptionTextView.text = "eSIM coverage available in all 50 states"
                coverageTextView.text = "Coverage Details:\n" +
                        "• 4G LTE: 99.5% coverage\n" +
                        "• 5G: Available in major cities\n" +
                        "• Data speeds: up to 150 Mbps\n" +
                        "• Supported carriers: Verizon, AT&T, T-Mobile"
            }
            "UK" -> {
                descriptionTextView.text = "eSIM coverage available across the UK"
                coverageTextView.text = "Coverage Details:\n" +
                        "• 4G LTE: 98% coverage\n" +
                        "• 5G: Available in London, Manchester, Birmingham\n" +
                        "• Data speeds: up to 100 Mbps\n" +
                        "• Supported carriers: EE, Vodafone, O2"
            }
            "Japan" -> {
                descriptionTextView.text = "eSIM coverage available throughout Japan"
                coverageTextView.text = "Coverage Details:\n" +
                        "• 4G LTE: 99% coverage\n" +
                        "• 5G: Available in major cities\n" +
                        "• Data speeds: up to 200 Mbps\n" +
                        "• Supported carriers: NTT DoCoMo, Softbank, au"
            }
            else -> {
                descriptionTextView.text = "eSIM coverage information"
                coverageTextView.text = "Select a country to view coverage details"
            }
        }
    }
}
