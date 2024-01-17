package com.mc.my_plane_application.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mc.my_plane_application.R
import com.mc.my_plane_application.ui.settings.adapter.Adapter_items
import com.mc.my_plane_application.ui.settings.data.SettingsItem

class Recycler_activity : AppCompatActivity() {
    companion object {
        private const val IDENTIFIANT_ID = "IDENTIFIANT_ID"
        fun getStartIntent(context: Context, identifiant: String?): Intent {
            return Intent(context, Recycler_activity::class.java).apply {
                putExtra(IDENTIFIANT_ID, identifiant)
            }
        }
    }

    // Retourne l'identifiant passé en paramètre à l'activité
    private fun getIdentifiant(): String? {
        return intent.extras?.getString(IDENTIFIANT_ID, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)

        // ici on déclare nos variables de notre liste du recycle view.
        // Exemple de déclaration dans la datasource (à déclarer dans votre Activity)
        val myItems = arrayOf(
            SettingsItem("Paramètres", R.drawable.bluetooth_icon) {
                // Action au cliques
                Handler(Looper.getMainLooper()).post {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }

            },
            SettingsItem("Paramétrage Localisation", R.drawable.info_icon) {
                // Action aux cliques
                Handler(Looper.getMainLooper()).post {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            },
            SettingsItem("Application carte", R.drawable.settings_icon) {
                // Action aux cliques


            },
            SettingsItem("Site de l'ESEO", R.drawable.logo_eseo_e) {
                // Action aux cliques
                Handler(Looper.getMainLooper()).post {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://eseo.fr/")));
                }
            },
            SettingsItem("Email", R.drawable.logo_eseo_e) {
            // Action aux cliques
            },
            SettingsItem("Information", R.drawable.logo_eseo_e) {
            // Action aux cliques
        }
        )


    //val items = mutableListOf("Item 1", "Item 2")
    val rvDevices = this.findViewById<RecyclerView>(R.id.rvDevices)

    rvDevices.layoutManager = LinearLayoutManager(this)
    rvDevices.adapter = Adapter_items(myItems)
    //{ item ->
    //    Toast.makeText(this@Recycler_activity,"Connexion à $item", Toast.LENGTH_SHORT).show()
    //    }
    }


}