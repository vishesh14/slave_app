package com.bleaudiocontroller.blemasterapp.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BluetoothService(private val context: Context) {

    private val TAG = "BluetoothService"
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null

    fun connectToDevice(device: BluetoothDevice) {
        if (isLocationPermissionGranted()) {
            checkSelfPermission()
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        } else {
            Log.e(TAG, "Location permission not granted")
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            permissionCheck == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun disconnect(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.")
                checkSelfPermission()
                gatt?.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.")
                // Handle disconnection here
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Get the desired service and characteristic
                val service: BluetoothGattService? =
                    gatt?.getService(java.util.UUID.fromString(uuid))
                val characteristic: BluetoothGattCharacteristic? =
                    service?.getCharacteristic(java.util.UUID.fromString(uuid))
                // Perform desired operations on the characteristic
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }
    }

    private fun checkSelfPermission() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    companion object {

        private const val uuid = "31990d6c-893d-4876-875e-adb9cfa59e48"

    }
}
