/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

//https://cursos.innovadomotics.com/courses/android-studio-y-arduino-comunicacion-bluetooth-kotlin/leccion/desarrollo-codigo/

package com.example.edge_health.presentation

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.edge_health.R
import com.example.edge_health.presentation.theme.Edge_healthTheme
import java.util.UUID

const val REQUEST_ENABLE_BT = 1

class MainActivity : ComponentActivity() {
    lateinit var mBtAdapter: BluetoothAdapter
    var mAddressDevices: ArrayAdapter<String>? = null
    var mNameDevices: ArrayAdapter<String>? = null
    var activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == REQUEST_ENABLE_BT) {
            Log.i("MainActivity", "ACTIVIDAD REGISTRADA")
        }
    }
    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private var m_bluetoothSocket: BluetoothSocket? = null

        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }
    private val TAG = "jose-Legion-5"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)
        Log.d(TAG, "onCreate")

        mAddressDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        val bluetoothBtn = findViewById<Button>(R.id.btButton)

        // Configurar el adaptador Bluetooth
        mBtAdapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
        // Aquí se tendrá también que buscar dispositivos tras el checkBT
        bluetoothBtn.setOnClickListener {
            //Encender Bluetooth
            if (!mBtAdapter.isEnabled) {
                Log.d(TAG, "Bluetooth apagado")
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i("MainActivity", "ActivityCompat#requestPermissions")
                }
                activityLauncher.launch(enableBtIntent)
            } else {
                Log.d(TAG, "Bluetooth encendido")
            }

            fun checkPairedDevices(): Boolean {
                val pairedDevices = mBtAdapter.bondedDevices
                if (pairedDevices.isEmpty()) {
                    Log.d(TAG, "No hay dispositivos emparejados")
                    return false
                } else {
                    for (device in pairedDevices) {
                        Log.d(TAG, "Dispositivo: ${device.name}")
                        if (device.name == "jose-Legion-5") {
                            m_address = device.address
                            Log.d(TAG, "Dispositivo encontrado")

                            return true
                        }
                    }
                    return false
                }
            }

            if (checkPairedDevices()) {
                Log.d(TAG, "Dispositivo encontrado con dirección: $m_address")
                val device: BluetoothDevice = mBtAdapter.getRemoteDevice(m_address)
                try {
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    m_bluetoothSocket!!.connect()
                    m_isConnected = true
                    Log.d(TAG, "Conexión establecida")
                } catch (e: Exception) {
                    m_isConnected = false
                    Log.d(TAG, "Error al conectar")
                }

            }
        }

    }

}
