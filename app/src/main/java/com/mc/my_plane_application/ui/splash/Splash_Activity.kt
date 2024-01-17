package com.mc.my_plane_application.ui.splash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.mc.my_plane_application.R
import com.mc.my_plane_application.ui.main.Main_Activity

class Splash_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // code pour lancer le spash activity après quelques secondes
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Main_Activity.getStartIntent(this, "CECI-EST-UN-IDENTIFIANT")) //mtn avec un paramètre
            finish()
        }, 2000)


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }
}