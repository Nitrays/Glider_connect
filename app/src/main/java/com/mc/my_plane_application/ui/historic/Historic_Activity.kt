package com.mc.my_plane_application.ui.historic

import com.mc.my_plane_application.ui.settings.adapter.Adapter_items
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mc.my_plane_application.R
import com.mc.my_plane_application.ui.settings.data.SettingsItem

class Historic_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historic)

        // ici on déclare nos variables de notre liste du recycle view.
        // Exemple de déclaration dans la datasource (à déclarer dans votre Activity)
        val myItems = arrayOf(
            SettingsItem("loc1", R.drawable.bluetooth_icon) {
                // Action au cliques
            },
            SettingsItem("loc2", R.drawable.info_icon) {
                // Action aux cliques
            }
        )

        //val items = mutableListOf("Item 1", "Item 2")
        val rvDevices = this.findViewById<RecyclerView>(R.id.rvDevices)

        rvDevices.layoutManager = LinearLayoutManager(this)
        rvDevices.adapter = Adapter_items(myItems)
    }

    // Code permettant de retourner les informations (intent) nécessaires
    // conpanion object = méthode static
    companion object {
        private const val IDENTIFIANT_ID = "IDENTIFIANT_ID"

        fun getStartIntent(context: Context, identifiant: String?): Intent {
            return Intent(context, Historic_Activity::class.java).apply {
                putExtra(IDENTIFIANT_ID, identifiant)
            }
        }
        private const val PERMISSION_REQUEST_LOCATION = 9999
    }
}