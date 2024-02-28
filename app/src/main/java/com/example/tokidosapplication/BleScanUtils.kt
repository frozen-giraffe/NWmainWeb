package com.example.tokidosapplication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat

class BleScanUtils private constructor() {
    /***
     *   This lazy initialization singleton is not thread-safe
     */
    private  var bluetoothManager: BluetoothManager? = null
    private  var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    private var devices: MutableSet<BluetoothDevice>? = null
    private var pickedDevice: BluetoothDevice? = null

    private val handler = Handler(Looper.getMainLooper())
    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    private var scanning = false
    private lateinit var listener: (MutableSet<BluetoothDevice>) -> Unit

    companion object {
        val getInstance:BleScanUtils by lazy {BleScanUtils()}
    }
    fun setListener(bluetoothLeDevice:  (MutableSet<BluetoothDevice>) -> Unit){
        listener = bluetoothLeDevice
    }
    fun setPickedDevice(device: BluetoothDevice){
        pickedDevice= device
    }
    fun getPickedDevice(): BluetoothDevice?{
        return pickedDevice
    }
    @SuppressLint("MissingPermission")
    fun scanLeDevice(context:Context?) {
        if (devices == null){
            devices = HashSet()
        }else{
            devices!!.clear()
        }
        bluetoothManager = context?.getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
                listener.invoke(devices!!)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner?.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)

        }
    }

    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            println("BLE onScanCallBack: ${result.device} ${result.device.name}")
            if (result.device != null && result.device.name != null && result.device.name.contains("Tokidos", ignoreCase = true)){
                //println("Utils BLE device: ${result.device.name}")
                //use set to filter same result
                devices!!.add(result.device)
                //listener.invoke(devices!!)
            }
        }
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            println("scan error: $errorCode")
        }
    }
    //Close if needed when page jumping
    @SuppressLint("MissingPermission")
    fun stopScanBle(){
        if (bluetoothLeScanner!= null && bluetoothAdapter!= null && bluetoothAdapter!!.isEnabled){
            if(scanning){
                scanning = false
                bluetoothLeScanner!!.stopScan(leScanCallback)
            }
        }
    }
}