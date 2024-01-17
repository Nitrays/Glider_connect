package com.mc.my_plane_application.ui.localisation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.mc.my_plane_application.R
import java.util.Locale

class Localisation_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_localisation)

        // code BOUTON_go_back
        findViewById<Button>(R.id.button_go_back).setOnClickListener {
            Handler(Looper.getMainLooper()).post {
                finish()
            }
        }

        // code BOUTON_voir_localisation
        findViewById<Button>(R.id.button_localisation_perm).setOnClickListener {
            Handler(Looper.getMainLooper()).post {
                requestPermission()
            }
        }

    }


    // Code permettant de retourner les informations (intent) nécessaires
    // conpanion object = méthode static
    companion object {
        private const val IDENTIFIANT_ID = "IDENTIFIANT_ID"

        fun getStartIntent(context: Context, identifiant: String?): Intent {
            return Intent(context, Localisation_Activity::class.java).apply {
                putExtra(IDENTIFIANT_ID, identifiant)
            }
        }
        private const val PERMISSION_REQUEST_LOCATION = 9999
    }

    //code pour demander la permission de la localisation
    private fun requestPermission() {
        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION)
        } else {
            getLocation()
        }
    }

    //permet de savoir si les paramètres ont été accepté ou non 
    private fun hasPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    //méthode de callback qui va être appelé à chaque fois que l'util prend une décision
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission obtenue, Nous continuons la suite de la logique.
                    getLocation()
                } else {
                    // TODO
                    // Permission non accepté, expliqué ici via une activité ou une dialog pourquoi nous avons besoin de la permission
                }
                return
            }
        }
    }

    @SuppressLint("MissingPermission")
    // méthode 2 via les play services
    private fun getLocation() {
        if (hasPermission()) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { geoCode(it) }
                .addOnFailureListener {
                    // Remplacer par un vrai bon message
                    Toast.makeText(this, "Localisation impossible", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // permet de calculer la distance entre une location et l'ESEO
    private fun getDistanceESEO(location: Location): String {
        //Coordonnées de l'ESEO
        val eseoLat = 47.493719
        val eseoLong = -0.550486

        //calcule de la distance entre 2 points (avec coord eseo)
        val distanceArray = floatArrayOf(0f)
        Location.distanceBetween(
            location.latitude,
            location.longitude,
            eseoLat,
            eseoLong,
            distanceArray
        )
        val distance = distanceArray[0] / 1000
        return String.format("%.1f", distance)
    }

    // méthode 1 permet de convertir une
    private fun geoCode(location: Location){
        val geocoder = Geocoder(this, Locale.getDefault())

        //calcule de l'adresse de la localisation
        val results = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        // colle les messages
        if (results?.isNotEmpty()== true) {
            // text adresse actuelle
            findViewById<TextView>(R.id.my_location).text = results[0].getAddressLine(0)

            // text distance de l'ESEO
            val distanceFromEseo = getDistanceESEO(location)
            findViewById<TextView>(R.id.my_distance_from_ESEO).text = "Distance de l'ESEO : $distanceFromEseo km" //comment on peut mettre en i18n // TODO
        }
    }

    // Retourne l'identifiant passé en paramètre à l'activité
    private fun getIdentifiant(): String? {
        return intent.extras?.getString(IDENTIFIANT_ID, null)
    }



}