package com.mc.my_plane_application.ui.command

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mc.my_plane_application.R

class Command_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_command)
    }

    // Code permettant de retourner les informations (intent) nécessaires
    // conpanion object = méthode static
    companion object {
        private const val IDENTIFIANT_ID = "IDENTIFIANT_ID"

        fun getStartIntent(context: Context, identifiant: String?): Intent {
            return Intent(context, Command_activity::class.java).apply {
                putExtra(IDENTIFIANT_ID, identifiant)
            }
        }
        private const val PERMISSION_REQUEST_LOCATION = 9999
    }
}