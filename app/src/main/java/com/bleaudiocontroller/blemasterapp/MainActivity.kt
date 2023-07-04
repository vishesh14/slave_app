package com.bleaudiocontroller.blemasterapp

import android.Manifest
import android.bluetooth.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothGattServer: BluetoothGattServer
    private lateinit var characteristic: BluetoothGattCharacteristic

    private lateinit var textViewMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkSelfPermission()
        textViewMessage = findViewById(R.id.textViewMessage)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothGattServer = bluetoothManager.openGattServer(this, gattServerCallback)

        val service = BluetoothGattService(
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"),
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        characteristic = BluetoothGattCharacteristic(
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"),
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        service.addCharacteristic(characteristic)
        bluetoothGattServer.addService(service)

        textViewMessage.text = "Waiting for commands..."
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(
            device: BluetoothDevice,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(device, status, newState)

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Device connected: ${device.address}")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Device disconnected: ${device.address}")
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            // Not used in this example
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )

            val command = value.toString(Charsets.UTF_8)

            runOnUiThread {
                textViewMessage.text = "Received command: $command"
            }

            // TODO: Handle the received command
        }
    }
    private fun checkSelfPermission() {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        checkSelfPermission()
        bluetoothGattServer.close()
    }

    companion object {
        private const val uuid = "31990d6c-893d-4876-875e-adb9cfa59e48"
        private const val TAG = "MainActivity"
    }


}
