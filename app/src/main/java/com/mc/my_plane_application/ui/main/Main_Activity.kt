package com.mc.my_plane_application.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import com.mc.my_plane_application.ui.historic.Historic_Activity
import com.mc.my_plane_application.ui.localisation.Localisation_Activity
import com.mc.my_plane_application.R
import com.mc.my_plane_application.ui.command.Command_activity
import com.mc.my_plane_application.ui.scan.Scan_activity
import com.mc.my_plane_application.ui.settings.Recycler_activity

class Main_Activity : AppCompatActivity() {
    // Code permettant de retourner les informations (intent) nécessaires
    // conpanion object = méthode static
    companion object {
        private const val IDENTIFIANT_ID = "IDENTIFIANT_ID"

        fun getStartIntent(context: Context, identifiant: String?): Intent {
            return Intent(context, Main_Activity::class.java).apply {
                putExtra(IDENTIFIANT_ID, identifiant)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // code BOUTON_commander
        findViewById<Button>(R.id.button_command_by_web).setOnClickListener {

            //permet de renvoyer un paramètre
            Handler(Looper.getMainLooper()).post {
                startActivity(
                    Command_activity.getStartIntent(
                        this,
                        "CECI-EST-UN-IDENTIFIANT"
                    )
                )
            }
        }

        // code BOUTON_commander
        findViewById<Button>(R.id.button_scan_devices).setOnClickListener {

            //permet de renvoyer un paramètre
            Handler(Looper.getMainLooper()).post {
                startActivity(
                    Scan_activity.getStartIntent(
                        this,
                        "CECI-EST-UN-IDENTIFIANT"
                    )
                )
            }
        }

        // code BOUTON_localisation
        findViewById<Button>(R.id.button_localisation).setOnClickListener {

            //permet de renvoyer un paramètre
            Handler(Looper.getMainLooper()).post {
                startActivity(
                    Localisation_Activity.getStartIntent(
                        this,
                        "CECI-EST-UN-IDENTIFIANT"
                    )
                )
            }
        }

        /*
        // code BOUTON2 HISTORIQUE
        findViewById<Button>(R.id.button_historique).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.title))
                .setMessage(resources.getString(R.string.supporting_text))
                .setIcon(R.drawable.ic_laucher_airplane_foreground)
                .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                    // Respond to neutral button press
                }
                .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                    // Respond to negative button press
                }
                .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                    // Respond to positive button press
                }
                .show()
            }
         */
        findViewById<Button>(R.id.button_historique).setOnClickListener {
            startActivity(
                Historic_Activity.getStartIntent(
                    this,
                    "CECI-EST-UN-IDENTIFIANT"
                )
            );
        }

        // code BOUTON_SETTINGS
        findViewById<Button>(R.id.button_settings).setOnClickListener {
            startActivity(
                Recycler_activity.getStartIntent(
                    this,
                    "CECI-EST-UN-IDENTIFIANT"
                )
            );
        }

        // code LIEN_ESEO
        findViewById<TextView>(R.id.text_lien_ESEO).setOnClickListener {
            // code CARTE_ESEO
            findViewById<TextView>(R.id.text_carte_eseo).setOnClickListener {
                Handler(Looper.getMainLooper()).post {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("geo:47.472822,-0.5621756")
                        )
                    );
                }
            }
        }
    }

    // Retourne l'identifiant passé en paramètre à l'activité
    private fun getIdentifiant(): String? {
        return intent.extras?.getString(IDENTIFIANT_ID, null)
    }

    val targetIntent = Intent().apply {
        action = android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
    }
    val targetIntentParam = Intent().apply {
        action = android.provider.Settings.ACTION_SETTINGS
    }

}