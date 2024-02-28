package com.example.tokidosapplication

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import com.example.tokidosapplication.view.data.Wifi
import com.example.tokidosapplication.view.playcubes.DetectingFragment
import java.util.UUID

private const val TAG = "BluetoothLeService"


class BluetoothLeService: Service() {

    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var connectionState = STATE_DISCONNECTED

    private val characteristicsMap: HashMap<String, BluetoothGattCharacteristic> = hashMapOf()

    private var connectingWifi: Wifi? = null

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }
    fun close() {
        bluetoothGatt?.let { gatt ->
            println("gatt close")
            if(checkPermission()){
                gatt.disconnect()
                gatt.close()
            }
            bluetoothGatt = null

        }
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService {
            println("lalala getService")
            return this@BluetoothLeService
        }
    }
    fun getCharacteristicsMap():HashMap<String,BluetoothGattCharacteristic>{
        return characteristicsMap
    }

    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return bluetoothGatt?.services
    }

    fun getBluetoothGatt(): BluetoothGatt?{
        return bluetoothGatt
    }

    fun setConnectingWifi(wifi: Wifi?){
        connectingWifi = wifi
    }

    fun initialize(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }
    private fun checkPermission():Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "No permission for BLUETOOTH_CONNECT", Toast.LENGTH_SHORT).show()
            return false

        }
        return true
    }
    fun connect(device: BluetoothDevice?): Boolean {
        println("connect function in Service: $device")
        if (device == null){
            return false
        }
        //connectGatt need BLUETOOTH_CONNECT permission,also BLUETOOTH_CONNECT need api>=31
        if (!checkPermission()) return false

        try {
            bluetoothGatt = device.connectGatt(this, false, gattCallback)
        }catch (exception: IllegalArgumentException) {
            Log.w(TAG, "Device not found with provided address.  Unable to connect.")
            return false
        }
        return true

    }
    @SuppressLint("MissingPermission")
    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic? = null) {
        val intent = Intent(action)
        if (characteristic!=null){
            println("broadcastUpdate characteristic: $characteristic, uuid of char: ${characteristic.uuid} ${characteristic.value}")
            println("getValue from BluetoothGattCharacteristic is deprecated from api lvl 33, please use gatt.readCharacteristic")
            val data = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    characteristic.value?.toString(Charsets.UTF_8)
            }else{ bluetoothGatt?.let { gatt ->
                    //gatt.readCharacteristic(characteristic)
                    " "
                }
            } ?: ""
            when (characteristic.uuid.toString()){
                wifi_connection_uuid ->{
                    val connectionStatus = characteristic.value.toString(Charsets.UTF_8)

                    println("connectionStatus: ${connectionStatus}")
                    intent.putExtra(EXTRA_DATA, connectionStatus)

                }
                ssid_char_uuid ->{
                    println("write callback ssid: $data")
                    //if(data == connectingWifi?.ssid)
                    //intent.putExtra(EXTRA_DATA, data)
                }
                wifi_password_char_uuid ->{
                    println("write callback password: $data")
                    //intent.putExtra(EXTRA_DATA, data)
                }
                security_type_uuid ->{
                    println("write callback security: $data")
                    //intent.putExtra(EXTRA_DATA, data)
                }
                else ->{
                    // For all other profiles, writes the data formatted in HEX.
                    val data: ByteArray? = characteristic.value
                    if (data?.isNotEmpty() == true) {
                        val hexString: String = data.joinToString(separator = " ") {
                            String.format("%02X", it)
                        }
                        println("heartRate1: \"$data\\n$hexString\"")
                        //intent.putExtra(EXTRA_DATA, "$data\n$hexString")
                    }
                }
            }
        }

        sendBroadcast(intent)
    }
    fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
    @SuppressLint("MissingPermission")
    fun displayGattServices(gattServices: List<BluetoothGattService?>?) {
        if (gattServices == null) return
        var serviceUuid: String?
        // Loops through available GATT Services.
        gattServices.forEach { gattService ->
            serviceUuid = gattService?.uuid.toString()
            println("serviceUuid: $serviceUuid")

            gattService?.characteristics?.forEach { gattCharacteristic ->
                val uuid = gattCharacteristic.uuid.toString()
                println("stringL: ${gattCharacteristic.toString()} $uuid ${wifi_password_char_uuid}   ")
                //add gattCharacteristic to map
                if (serviceUuid == service_uuid){
                    if(uuid == ssid_char_uuid){
                        //bluetoothGatt?.setCharacteristicNotification(gattCharacteristic, true) //src: https://blog.csdn.net/uu13567/article/details/78017421 开启后onCharacteristicChanged就会有东西
                        characteristicsMap[wifiSSID]=gattCharacteristic
                    }else if(uuid == wifi_password_char_uuid){
                        characteristicsMap[wifiPassword]=gattCharacteristic
                    }else if(uuid == security_type_uuid){
                        characteristicsMap[wifiSecurityType]=gattCharacteristic
                    }else if(uuid == wifi_connection_uuid){
                        characteristicsMap[wifiConnection]=gattCharacteristic

                    }
                }
                println("gattCharacteristics: $uuid properties:${gattCharacteristic.properties}")
            }
        }
        println("characteristicsMap:$characteristicsMap")
    }
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic):Boolean {
        println("readCharacteristic: ${characteristic.uuid}")
        bluetoothGatt?.let { gatt ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                println("readCharacteristic: no permission")
                return false
            }
            val result = gatt.readCharacteristic(characteristic)
            println("readCharacteristic result:$result")
            return result
        } ?: run {
            Log.w(TAG, "BluetoothGatt not initialized")
            return false
        }
    }
    @SuppressLint("MissingPermission")
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, data:String):Int{
        val convertData = data.toByteArray(Charsets.UTF_8)
        var status:Int
        bluetoothGatt?.let { gatt ->
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                status = gatt.writeCharacteristic(characteristic, convertData, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                println("writeCharacteristic $status")
                return status
            }else{
                characteristic.value = convertData
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                val st = gatt.writeCharacteristic(characteristic)
                println("writeCharacteristic $st")
                if(st) return 0 else return -1

            }

        } ?: run {
            Log.w(TAG, "BluetoothGatt not initialized")
            return -1
        }
    }

    //GATT callback
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val deviceAddress = gatt?.device?.address
            println("onConnectionStateChange")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    println("successfully connected to the GATT Server")
                    // successfully connected to the GATT Server
                    connectionState = STATE_CONNECTED
                    broadcastUpdate(ACTION_GATT_CONNECTED)
                    // Attempts to discover services after successful connection.
                    bluetoothGatt?.discoverServices() //gatt.writeCharacteristic()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected from the GATT Server
                    connectionState = STATE_DISCONNECTED
                    close()
                    broadcastUpdate(ACTION_GATT_DISCONNECTED)

                }
            }else{
                Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                close()

                connectionState = STATE_DISCONNECTED

                broadcastUpdate(ACTION_GATT_DISCONNECTED)

            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                //println("test: ${bluetoothGatt?.services}")
//                bluetoothGatt?.services?.forEach { gattService ->
//                    val characteristicsTable = gattService.characteristics.joinToString(
//                        separator = "\n|--",
//                        prefix = "|--"
//                    ) { it.uuid.toString() }
//
//                    val currentServiceData = HashMap<String, String>()
//                    val uuid = gattService?.uuid.toString()
//                    println("gattService?.uuid: $uuid $characteristicsTable")
//                }
            } else {
                Log.w("BluetoothService", "onServicesDiscovered received: $status")
            }
        }
        //This method was deprecated in API level 33
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }
        //Use this for API level 33
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            println("value: ${value.toString(Charsets.UTF_8)}")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    characteristic?.value?.toString(Charsets.UTF_8)
                }else{ bluetoothGatt?.let { gatt ->
                    //gatt.readCharacteristic(characteristic)
                    " "}}
                println("write callback1: $data" )
                if(data == connectingWifi?.ssid)
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                else
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)

            }
        }
    }

    companion object {
        const val ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED="com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE="com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA="com.example.bluetooth.le.ACTION_EXTRA_DATA"

        const val EXTRA_DATA_SSID="com.example.bluetooth.le.ACTION_EXTRA_DATA_SSID"
        const val EXTRA_DATA_PASSWARD="com.example.bluetooth.le.ACTION_EXTRA_DATA_PASSWARD"
        const val EXTRA_DATA_SECURITY_TYPE="com.example.bluetooth.le.ACTION_EXTRA_DATA_SECURITY_TYPE"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2

    }
}