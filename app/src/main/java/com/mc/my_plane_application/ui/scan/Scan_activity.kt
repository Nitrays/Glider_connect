package com.mc.my_plane_application.ui.scan

import com.mc.my_plane_application.ui.scan.adapter.DeviceAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mc.my_plane_application.R
import com.mc.my_plane_application.ui.scan.data.local.LocalPreferences
import com.mc.my_plane_application.ui.scan.data.manager.BluetoothLEManager


class Scan_activity : AppCompatActivity() {
    private var rvDevices: RecyclerView? = null
    private var startScan: Button? = null
    private var currentConnexion: TextView? = null
    private var disconnect: Button? = null
    private var toggleLed: Button? = null
    private var ledStatus: ImageView? = null
    //private var ledStatus: ImageViewCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        setupRecycler()

        // Définition des varables pour le rv
        rvDevices = findViewById<RecyclerView>(R.id.rvDevices)
        startScan = findViewById<Button>(R.id.button_start_scan)
        currentConnexion = findViewById<TextView>(R.id.connecte_a)
        disconnect = findViewById<Button>(R.id.button_deconnexion)
        toggleLed = findViewById<Button>(R.id.button_toggle_led)
        ledStatus = findViewById<ImageView>(R.id.status_led)

        // On lance le scan de device à l'appuie btn
        findViewById<Button>(R.id.button_start_scan).setOnClickListener {
            askForPermission()
        }
        // On déconecte le device à l'appuie btn
        findViewById<Button>(R.id.button_deconnexion).setOnClickListener {
            disconnectFromCurrentDevice()
        }
        // On change l'état de la led à l'appuie btn
        findViewById<Button>(R.id.button_toggle_led).setOnClickListener {
            toggleLed()
        }
    }


    /**
    * Code permettant de retourner les informations (intent) nécessaires
    * conpanion object = méthode static
    */
    companion object {
        private const val IDENTIFIANT_ID = "IDENTIFIANT_ID"

        fun getStartIntent(context: Context, identifiant: String?): Intent {
            return Intent(context, Scan_activity::class.java).apply {
                putExtra(IDENTIFIANT_ID, identifiant)
            }
        }
        private const val PERMISSION_REQUEST_LOCATION = 9999
    }

    // Gestion du Bluetooth
    // L'Adapter permettant de se connecter
    private var bluetoothAdapter: BluetoothAdapter? = null

    // La connexion actuellement établie
    private var currentBluetoothGatt: BluetoothGatt? = null

    // « Interface système nous permettant de scanner »
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    // Parametrage du scan BLE
    private val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    // On ne retourne que les « Devices » proposant le bon UUID
    private var scanFilters: List<ScanFilter> = arrayListOf(
    //ScanFilter.Builder().setServiceUuid(ParcelUuid(BluetoothLEManager.DEVICE_UUID)).build()
    )

    // Variable de fonctionnement
    private var mScanning = false
    private val handler = Handler(Looper.getMainLooper())

    // DataSource de notre adapter.
    private val bleDevicesFoundList = arrayListOf<Device>()

    /**
     * Gère l'action après la demande de permission.
     * 2 cas possibles :
     * - Réussite 🎉.
     * - Échec (refus utilisateur).
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && locationServiceEnabled()) {
                // Permission OK & service de localisation actif => Nous pouvons lancer l'initialisation du BLE.
                // En appelant la méthode setupBLE(), La méthode setupBLE() va initialiser le BluetoothAdapter et lancera le scan.
                setupBLE()

            } else if (!locationServiceEnabled()) {
                // Inviter à activer la localisation
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else {
                // Permission KO => Gérer le cas.
                // Vous devez ici modifier le code pour gérer le cas d'erreur (permission refusé)
                // Avec par exemple une Dialog
            }
        }
    }

    /**
     * Permet de vérifier si l'application possede la permission « Localisation ». OBLIGATOIRE pour scanner en BLE
     * Sur Android 11, il faut la permission « BLUETOOTH_CONNECT » et « BLUETOOTH_SCAN »
     * Sur Android 10 et inférieur, il faut la permission « ACCESS_FINE_LOCATION » qui permet de scanner en BLE
     */
    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Demande de la permission (ou des permissions) à l'utilisateur.
     * Sur Android 11, il faut la permission « BLUETOOTH_CONNECT » et « BLUETOOTH_SCAN »
     * Sur Android 10 et inférieur, il faut la permission « ACCESS_FINE_LOCATION » qui permet de scanner en BLE
     */
    private fun askForPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), PERMISSION_REQUEST_LOCATION)
        }
    }

    private fun locationServiceEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            // This is Deprecated in API 28
            val mode = Settings.Secure.getInt(this.contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF)
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    // LE CODE DU SCAN
    // Dans mon cas je ne l'utilise pas car j'ai choisi la solution des dialogues
    /**
     * La méthode « registerForActivityResult » permet de gérer le résultat d'une activité.
     * Ce code est appelé à chaque fois que l'utilisateur répond à la demande d'activation du Bluetooth (visible ou non)
     * Si l'utilisateur accepte et donc que le BLE devient disponible, on lance le scan.
     * Si l'utilisateur refuse, on affiche un message d'erreur (Toast).
     */
    val registerForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            // Le Bluetooth est activé, on lance le scan
            scanLeDevice()
        } else {
            // Bluetooth non activé, vous DEVEZ gérer ce cas autrement qu'avec un Toast.
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.attention))
                .setMessage(resources.getString(R.string.veuillez_activer_bluetooth))
                .setIcon(R.drawable.disabled_bluetooth)
                .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                    // Respond to negative button press
                    Toast.makeText(this, "Localisation nécessaire, retour au menu principal", Toast.LENGTH_LONG).show()
                    finish()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                    // Respond to positive button press
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }


            //Toast.makeText(this, "Bluetooth non activé", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Récupération de l'adapter Bluetooth & vérification si celui-ci est actif.
     * Si il n'est pas actif, on demande à l'utilisateur de l'activer. Dans ce cas, au résultat le code présent dans « registerForResult » sera appelé.
     * Si il est déjà actif, on lance le scan.
     */
    private fun setupBLE() {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager?)?.let { bluetoothManager ->
            bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter != null && !bluetoothManager.adapter.isEnabled) {
                registerForResult.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else {
                scanLeDevice()
            }
        }
    }
    @SuppressLint("MissingPermission")//rajouté
    // Le scan va durer 10 secondes seulement, sauf si vous passez une autre valeur comme paramètre.
    private fun scanLeDevice(scanPeriod: Long = 10000) {
        if (!mScanning) {
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

            // On vide la liste qui contient les devices actuellement trouvés
            bleDevicesFoundList.clear()

            // Évite de scanner en double
            mScanning = true

            // On lance une tache qui durera « scanPeriod » à savoir donc de base
            // 10 secondes
            handler.postDelayed({
                mScanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
                Toast.makeText(this, getString(R.string.scan_ended), Toast.LENGTH_SHORT).show()
            }, scanPeriod)

            // On lance le scan
            Toast.makeText(this, getString(R.string.scan_started), Toast.LENGTH_SHORT).show()
            bluetoothLeScanner?.startScan(scanFilters, scanSettings, leScanCallback)
        }
    }
    @SuppressLint("MissingPermission")//rajouté
    // Callback appelé à chaque périphérique trouvé.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            // C'est ici que nous allons créer notre « Device » et l'ajouter dans la dataSource de notre RecyclerView

             val device = Device(result.device.name, result.device.address, result.device)
             if (!device.name.isNullOrBlank() && !bleDevicesFoundList.contains(device)) {
                   bleDevicesFoundList.add(device)
            //     Indique à l'adapter que nous avons ajouté un élément, il va donc se mettre à jour
                 findViewById<RecyclerView>(R.id.rvDevices).adapter?.notifyItemInserted(bleDevicesFoundList.size - 1)
             }
        }
    }

    /**
    * Méthode qui initialise le recycler view.
    */
    private fun setupRecycler() {
        val rvDevice = findViewById<RecyclerView>(R.id.rvDevices) // Récupération du RecyclerView présent dans le layout
        rvDevice.layoutManager = LinearLayoutManager(this) // Définition du LayoutManager, Comment vont être affichés les éléments, ici en liste
        rvDevice.adapter = DeviceAdapter(bleDevicesFoundList) { device ->
            // Le code écrit ici sera appelé lorsque l'utilisateur cliquera sur un élément de la liste.
            // C'est un « callback », c'est-à-dire une méthode qui sera appelée à un moment précis.
            // Évidemment, vous pouvez faire ce que vous voulez. Nous nous connecterons plus tard à notre périphérique

            // Pour la démo, nous allons afficher un Toast avec le nom du périphérique choisi par l'utilisateur.
            Toast.makeText(this@Scan_activity, "Clique sur $device", Toast.LENGTH_SHORT).show()


            Toast.makeText(this@Scan_activity, "trying to connect to ${device.name}", Toast.LENGTH_SHORT).show()
            BluetoothLEManager.currentDevice = device.device
            //LocalPreferences.getInstance(this).saveCurrent
            connectToCurrentDevice()

        }

    }

    override fun onResume() {
        super.onResume()
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // Test si le téléphone est compatible BLE, si c'est pas le cas, on finish() l'activity
            Toast.makeText(this, getString(R.string.not_compatible), Toast.LENGTH_SHORT).show()
            finish()
        } else if (hasPermission() && locationServiceEnabled()) {
            // Lancer suite => Activation BLE + Lancer Scan
            //setupBLE()

        } else if(!hasPermission()) {
            // On demande la permission
            askForPermission()
        } else {
            // On demande d'activer la localisation
            // Idéalement on demande avec un activité.
            // À vous de me proposer mieux (Une activité, une dialog, etc) ce sera une dialog
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.attention))
                .setMessage(resources.getString(R.string.veuillez_activer_votre_localisation))
                .setIcon(R.drawable.wrong_location)
                .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                    // Respond to negative button press
                    Toast.makeText(this, "Localisation nécessaire, retour au menu principal", Toast.LENGTH_LONG).show()
                    finish()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                    // Respond to positive button press
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .show()
        }
    }
    @SuppressLint("MissingPermission")//rajouté
    private fun connectToCurrentDevice() {
        BluetoothLEManager.currentDevice?.let { device ->
            Toast.makeText(this, "Connexion en cours … $device", Toast.LENGTH_SHORT).show()


            currentBluetoothGatt = device.connectGatt(
                this,
                false,
                BluetoothLEManager.GattCallback(
                    onConnect = {
                        // On indique à l'utilisateur que nous sommes correctement connecté
                        runOnUiThread {
                            // Nous sommes connecté au device, on active les notifications pour être notifié si la LED change d'état.

                            // À IMPLÉMENTER
                            // Vous devez appeler la méthode qui active les notifications BLE
                            // enableListenBleNotify()

                            // On change la vue « pour être en mode connecté »
                            setUiMode(true)

                            // On sauvegarde dans les « LocalPréférence » de l'application le nom du dernier préphérique
                            // sur lequel nous nous sommes connecté

                            // À IMPLÉMENTER EN FONCTION DE CE QUE NOUS AVONS DIT ENSEMBLE
                        }
                    },
                    onNotify = { runOnUiThread {
                        // VOUS DEVEZ APPELER ICI LA MÉTHODE QUI VA GÉRER LE CHANGEMENT D'ÉTAT DE LA LED DANS L'INTERFACE
                        // Si it (BluetoothGattCharacteristic) est pour l'UUID CHARACTERISTIC_NOTIFY_STATE
                        // Alors vous devez appeler la méthode qui va gérer le changement d'état de la LED
                        /* if(it.getUuid() == com.mc.my_plane_application.ui.scan.data.manager.BluetoothLEManager.CHARACTERISTIC_NOTIFY_STATE) {
                            // À IMPLÉMENTER
                        } else if (it.getUuid() == com.mc.my_plane_application.ui.scan.data.manager.BluetoothLEManager.CHARACTERISTIC_GET_COUNT) {
                            // À IMPLÉMENTER
                        } else if (it.getUuid() == com.mc.my_plane_application.ui.scan.data.manager.BluetoothLEManager.CHARACTERISTIC_GET_WIFI_SCAN) {
                            // À IMPLÉMENTER
                        } */
                    } },
                    onDisconnect = { runOnUiThread { disconnectFromCurrentDevice() } })
            )
        }
    }

    /**
     * On demande la déconnexion du device
     */
    @SuppressLint("MissingPermission")//rajouté
    private fun disconnectFromCurrentDevice() {
        currentBluetoothGatt?.disconnect()
        BluetoothLEManager.currentDevice = null
        setUiMode(false)
    }
    @SuppressLint("MissingPermission", "StringFormatInvalid")//rajotué
    private fun setUiMode(isConnected: Boolean) {
        if (isConnected) {
            // Connecté à un périphérique
            bleDevicesFoundList.clear()
            rvDevices?.visibility = View.GONE
            startScan?.visibility = View.GONE
            currentConnexion?.visibility = View.VISIBLE
            currentConnexion?.text = getString(R.string.connected_to, BluetoothLEManager.currentDevice?.name)
            disconnect?.visibility = View.VISIBLE
            toggleLed?.visibility = View.VISIBLE
        } else {
            // Non connecté, reset de la vue.
            rvDevices?.visibility = View.VISIBLE
            startScan?.visibility = View.VISIBLE
            ledStatus?.visibility = View.GONE
            currentConnexion?.visibility = View.GONE
            disconnect?.visibility = View.GONE
            toggleLed?.visibility = View.GONE
        }
    }

    /**
     * Récupération de « service » BLE (via UUID) qui nous permettra d'envoyer / recevoir des commandes
     */
    private fun getMainDeviceService(): BluetoothGattService? {
        return currentBluetoothGatt?.let { bleGatt ->
            val service = bleGatt.getService(BluetoothLEManager.DEVICE_UUID)
            service?.let {
                return it
            } ?: run {
                Toast.makeText(this, getString(R.string.uuid_not_found), Toast.LENGTH_SHORT).show()
                return null
            }
        } ?: run {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show()
            return null
        }
    }

    /**
     * On change l'état de la LED (via l'UUID de toggle)
     */
    @SuppressLint("MissingPermission")//rajouté
    private fun toggleLed() {
        getMainDeviceService()?.let { service ->
            val toggleLed = service.getCharacteristic(BluetoothLEManager.CHARACTERISTIC_TOGGLE_LED_UUID)
            toggleLed.setValue("1")
            currentBluetoothGatt?.writeCharacteristic(toggleLed)
        }
    }



}