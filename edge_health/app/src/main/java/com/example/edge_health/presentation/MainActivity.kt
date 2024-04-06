/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

//https://cursos.innovadomotics.com/courses/android-studio-y-arduino-comunicacion-bluetooth-kotlin/leccion/desarrollo-codigo/

package com.example.edge_health.presentation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.VolleyError
import com.example.edge_health.R
import org.json.JSONObject


const val REQUEST_ENABLE_BT = 1

class MainActivity : ComponentActivity() {

    var activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == REQUEST_ENABLE_BT) {
            Log.i("MainActivity", "ACTIVIDAD REGISTRADA")
        }
    }
    var reqq = false
    private val SSID = "jose-Legion-5"
    private val TAG = "MainActivity"
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)
        Log.d(TAG, "onCreate")

        // Scan WiFi
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        else {
            Log.d(TAG, "PERMISSION GRANTED")
        }

        val myButton = findViewById<Button>(R.id.btButton)

        myButton.setOnClickListener {
            // TODO: Check whether the watch is connected to the node or not first
            // Maybe just disconnect from the current network and connect to the node
            // TODO: Encapsulate the connection to the node in either a method or a class
            val info = wifiManager.connectionInfo
            val ssid = info.ssid

            if (ssid == SSID) {
                Log.d(TAG, "Already connected to the node")
                // Check whether the node is trusted or not and if not, disconnect and check for other node
                // maybe add a counter to skip the node if it's not trusted, if counter is 0 then do not skip
                // any node and try the first one if it is not trusted then start increasing the counter
                return@setOnClickListener
            }



            val wifiScanList: List<ScanResult> = wifiManager.scanResults
            var foundNode = false
            for (result in wifiScanList) {// local counter inside the if statement to check if we have to skip the node, this for later
                Log.d(TAG, result.SSID)
                if (result.SSID == SSID) {
                    Log.d(TAG, "FOUND NODE")
                    foundNode = true
                    break
                }
            }
            if (!foundNode){
                Log.d(TAG, "No node found")
            }
            else {
                val networkPass = "12345678"
                val conf = WifiConfiguration()
                conf.SSID = "\"" + SSID + "\""
                conf.preSharedKey = "\""+ networkPass +"\""
                val netId = wifiManager.addNetwork(conf)
                wifiManager.disconnect()
                wifiManager.enableNetwork(netId, true)
                wifiManager.reconnect()

            }



        }


        val APIActioner = findViewById<Button>(R.id.API)
        var resp = JSONObject()
        val callback = object : VolleyCallback {
            override fun onSuccess(response: JSONObject) {
                // Handle the JSON response here
                Log.d("Respooooonse", response.toString())
                resp = response;
                print(resp)
                val textView = findViewById<TextView>(R.id.textView)

                textView.setText(resp.getJSONArray("users").getJSONObject(0).getString("name"))
            }

            override fun onError(error: VolleyError) {
                // Handle errors here
                throw RuntimeException("Error: $error")
            }
        }
        APIActioner.setOnClickListener{

            var req = VolleyRequest(this)
            resp = req.sendRequest(callback, "/api/users/all", Request.Method.GET, null)



        }
    }



}
