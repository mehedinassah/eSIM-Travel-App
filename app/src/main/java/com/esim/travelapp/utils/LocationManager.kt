package com.esim.travelapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import android.location.Geocoder
import java.io.IOException
import android.annotation.SuppressLint

/**
 * Manages location services and geolocation features
 */
class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context)

    // Map of coordinates to countries (simplified for demonstration)
    private val countryCoordinates = mapOf(
        "USA" to Pair(37.0902, -95.7129),
        "UK" to Pair(55.3781, -3.4360),
        "Canada" to Pair(56.1304, -106.3468),
        "France" to Pair(46.2276, 2.2137),
        "Germany" to Pair(51.1657, 10.4515),
        "Japan" to Pair(36.2048, 138.2529),
        "Australia" to Pair(-25.2744, 133.7751),
        "India" to Pair(20.5937, 78.9629)
    )

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get the last known location
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): Pair<Double, Double>? =
        suspendCancellableCoroutine { continuation ->
            try {
                if (!hasLocationPermission()) {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                }.addOnFailureListener {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }

    /**
     * Get country name from coordinates
     */
    suspend fun getCountryFromCoordinates(
        latitude: Double,
        longitude: Double
    ): String? = suspendCancellableCoroutine { continuation ->
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                continuation.resume(addresses[0].countryName)
            } else {
                continuation.resume(null)
            }
        } catch (e: IOException) {
            continuation.resume(null)
        }
    }

    /**
     * Find the closest country to user's location
     */
    fun findClosestCountry(userLat: Double, userLon: Double): String? {
        var closestCountry: String? = null
        var minDistance = Double.MAX_VALUE

        for ((country, coords) in countryCoordinates) {
            val distance = calculateDistance(
                userLat, userLon,
                coords.first, coords.second
            )
            if (distance < minDistance) {
                minDistance = distance
                closestCountry = country
            }
        }

        return closestCountry
    }

    /**
     * Calculate distance between two coordinates (Haversine formula)
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusKm * c
    }

    /**
     * Get all available countries for plans
     */
    fun getAvailableCountries(): List<String> {
        return listOf("USA", "UK", "Canada", "France", "Germany", "Japan", "Australia", "India")
    }

    /**
     * Check if a country is available for plans
     */
    fun isCountryAvailable(country: String): Boolean {
        return countryCoordinates.containsKey(country)
    }
}
