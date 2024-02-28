package com.example.tokidosapplication.view.playcubes

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tokidosapplication.BleScanUtils
import com.example.tokidosapplication.BluetoothLeService
import com.example.tokidosapplication.R

import com.example.tokidosapplication.databinding.FragmentDetectingBinding
import com.example.tokidosapplication.databinding.FragmentPlaycubesCheckBinding
import com.example.tokidosapplication.view.BLEhelper
import com.example.tokidosapplication.view.LoadingView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetectingFragment : Fragment() {
    private lateinit var binding: FragmentDetectingBinding
    private lateinit var viewModel: PlayCubesConnectBaseViewModel

    private  var mBluetoothManager: BluetoothManager? = null
    private lateinit var mBluetoothScanner: BluetoothLeScanner
    private lateinit var leDeviceListAdapter: MultipleDevicesAdaptor
    private var mBluetoothAdapter: BluetoothAdapter? = null

    private var scanning = false
    private var connected = false
    private var isMultipleCubesPage = false // use for display/not display fail & success page for multiple page
    private val handler = Handler(Looper.getMainLooper())

    private var mBTDevices: ArrayList<BluetoothDevice> = ArrayList()

    var instance = BLEhelper.getInstance()
    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000


    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            println("gattUpdateReceiver: ${intent.action}")
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    connected = true
                    println("GATT connected ${BLEhelper.getInstance().getBluetoothLeService()} ${BLEhelper.getInstance().getBluetoothLeService()?.getBluetoothGatt()}")

                    if(isMultipleCubesPage){
                        Navigation.findNavController(binding.root).navigate(R.id.action_detectingFragment_to_wifiPairingFragment)
                    }else{
                        UIupdateSuccessScaned()
                    }
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    println("GATT disconnected")
                    UIupadteDetectionFail(BluetoothLeService.ACTION_GATT_DISCONNECTED)
                }
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    // Show all the supported services and characteristics on the user interface.
                    println("GATT DISCOVERED")
                    instance.getBluetoothLeService()?.displayGattServices(instance.getBluetoothLeService()?.getSupportedGattServices())
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate Detecting")

        mBluetoothManager = activity?.getSystemService(BluetoothManager::class.java)
        mBluetoothAdapter = mBluetoothManager?.adapter

        leDeviceListAdapter = MultipleDevicesAdaptor(mBTDevices, object : MultipleDevicesAdaptor.MyItemClickListener{
            override fun onItemClicked(data: ArrayList<BluetoothDevice>, position: Int) {
                println("hello")
                //BleScanUtils.getInstance.setPickedDevice(data[position])
                connectGATTService(data[position]) // if connected, BleScanUtils.getInstance.setPickedDevice(data[position]) in gattUpdateReceiver
            }
        })
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, "This device doesn't support bluetooth", Toast.LENGTH_LONG).show()
        }else{
            mBluetoothScanner = mBluetoothAdapter!!.bluetoothLeScanner
        }

        //setup bound BLE GATT Service
        val gattServiceIntent = Intent(context, BluetoothLeService::class.java)
        activity?.bindService(gattServiceIntent, instance.serviceConnection, Context.BIND_AUTO_CREATE)


    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("onCreateView Detecting")
        binding = FragmentDetectingBinding.inflate(layoutInflater, container, false)
        //bind lifecycle of custom loading view to page lifecycle
        lifecycle.addObserver(binding.view)

        viewModel = ViewModelProvider(this).get(PlayCubesConnectBaseViewModel::class.java)

        binding.detectMultipleCubesScreen.recyclerView.adapter = leDeviceListAdapter
        binding.detectMultipleCubesScreen.recyclerView.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )

        println("on DectecingFragment ${BLEhelper.getInstance().getBluetoothLeService()} ${BLEhelper.getInstance().getBluetoothLeService()?.getBluetoothGatt()}")
        //always close any GATT connection when enter this page and before scanning
        BLEhelper.getInstance().getBluetoothLeService()?.close()

        checkAndRequestPermissions().let {
            isMultipleCubesPage=false
            if(it) BleScanUtils.getInstance.scanLeDevice(activity?.applicationContext)
        }

        BleScanUtils.getInstance.setListener {
            for (d in it){
                println("BleScanUtils: ${d} ${d.name}")
            }
            if (it.isEmpty()){
                isMultipleCubesPage = false

                UIupadteDetectionFail()
            }else if (it.size > 1){
                isMultipleCubesPage = true
                UIupdateMultipleDevices()
                mBTDevices.clear()
                for (device in it){
                    println("Utils BLE device: ${device.name} ${it.size}")
                    mBTDevices.add(device)
                    leDeviceListAdapter.notifyDataSetChanged()
                }
            }else{
                // potentially solution for BluetoothGattCallback: status code 133 Error(Error {133} encountered for {10:97:BD:EC:98:B6}! Disconnecting...)
                // Because it may caused by connect GATT right after scan stop, only happens here
                isMultipleCubesPage = false

                handler.postDelayed({
                    connectGATTService(it.first())
                },500)
            }
        }
        binding.detectMultipleCubesScreen.refreshMultiplePage.setOnClickListener{
            UIupdateScanning()

            mBTDevices.clear()
            binding.detectMultipleCubesScreen.recyclerView.adapter = leDeviceListAdapter
            checkAndRequestPermissions().let {
                if(it) BleScanUtils.getInstance.scanLeDevice(activity?.applicationContext)
            }
        }
        binding.detectFailScreen.tryAgainConnectPlayCube.setOnClickListener{
            UIupdateScanning()
            isMultipleCubesPage=false
            mBTDevices.clear()
            binding.detectMultipleCubesScreen.recyclerView.adapter = leDeviceListAdapter

            checkAndRequestPermissions().let {
                if(it) BleScanUtils.getInstance.scanLeDevice(activity?.applicationContext)
            }
        }
        binding.multipleCubesConnectFailScreen.tryAgainMultiplePage.setOnClickListener{
            UIupdateScanning()
            isMultipleCubesPage=false
            mBTDevices.clear()
            binding.detectMultipleCubesScreen.recyclerView.adapter = leDeviceListAdapter

            checkAndRequestPermissions().let {
                if(it) BleScanUtils.getInstance.scanLeDevice(activity?.applicationContext)
            }
        }
        return binding.root
    }
    private fun connectGATTService(device: BluetoothDevice){
        println("bluetoothService: ${instance.getBluetoothLeService()}")
        instance.getBluetoothLeService()?.let {bluetoothLEService ->
            // call functions on service to check connection and connect to devices
            if (!bluetoothLEService.initialize()) {
                Log.e("BLE Connect", "Unable to initialize Bluetooth")
            }else{
                val result = bluetoothLEService.connect(device)
                println("BLE Connect=$result")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        println("register gattUpdateReceiver")
        activity?.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter(),
            Context.RECEIVER_EXPORTED)

    }
    override fun onPause() {
        super.onPause()
        println("unregister gattUpdateReceiver")
        activity?.unregisterReceiver(gattUpdateReceiver)
    }
    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy DetectingFragment")
        activity?.unbindService(instance.serviceConnection)
    }
    private fun makeGattUpdateIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        }
    }
    private fun UIupdateSuccessScaned(){
        binding.detectMultipleCubesScreen.root.visibility = View.INVISIBLE
        binding.detectScreen.visibility = View.INVISIBLE
        binding.detectSuccessScreen.root.visibility = View.VISIBLE
        binding.detectSuccessScreen.button.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_detectingFragment_to_wifiPairingFragment)
        }
    }

    private fun UIupdateScanning(){
        binding.detectScreen.visibility = View.VISIBLE
        binding.detectMultipleCubesScreen.root.visibility = View.INVISIBLE
        binding.multipleCubesConnectFailScreen.root.visibility = View.INVISIBLE

        binding.detectFailScreen.root.visibility = View.INVISIBLE
        binding.detectSuccessScreen.root.visibility = View.INVISIBLE
        //UIupdateSuccessScaned()
        //binding.button.isClickable = true
    }
    private fun UIupadteDetectionFail(from: String = ""){
        binding.detectScreen.visibility = View.INVISIBLE

        if(isMultipleCubesPage || from == BluetoothLeService.ACTION_GATT_DISCONNECTED){//show pairing fail page
            binding.multipleCubesConnectFailScreen.root.visibility = View.VISIBLE
        }else{//show connect fail page
            binding.detectFailScreen.root.visibility = View.VISIBLE
        }

        binding.detectSuccessScreen.root.visibility = View.INVISIBLE
        binding.detectMultipleCubesScreen.root.visibility = View.INVISIBLE
    }
    private fun UIupdateMultipleDevices(){
        binding.detectScreen.visibility = View.INVISIBLE
        binding.detectMultipleCubesScreen.root.visibility = View.VISIBLE
        //binding.detectMultipleCubesScreen.
    }
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    private fun startScanningPlayCubes(){
//        checkAndRequestPermissions().let {
//            if (it) scanLeDevice()
//        }
//    }
//    @SuppressLint("MissingPermission")
//    private fun scanLeDevice() {
//
//        if (!scanning) { // Stops scanning after a pre-defined scan period.package:mine
//            handler.postDelayed({
//                scanning = false
//                mBluetoothScanner.stopScan(leScanCallback)
//                if(mBTDevices.isEmpty()){
//                    UIupadteDetectionFail()
//                }else{
//                    UIupdateSuccessScaned()
//
//                    bluetoothService?.let {
//                        // call functions on service to check connection and connect to devices
//                        if (!it.initialize()) {
//                            Log.e("BLE Connect", "Unable to initialize Bluetooth")
//                        }else{
//                            println(mBTDevices)
//                            val result = it.connect(mBTDevices[0])
//                            println("BLE Connect=$result")
//                        }
//                    }
//                }
//            }, SCAN_PERIOD)
//            scanning = true
//            mBluetoothScanner.startScan(leScanCallback)
//        } else {
//            scanning = false
//            mBluetoothScanner.stopScan(leScanCallback)
//        }
//    }
    // Device scan callback.
//    private val leScanCallback: ScanCallback = object : ScanCallback() {
//        @SuppressLint("MissingPermission")
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            super.onScanResult(callbackType, result)
//            //leDeviceListAdapter.addDevice(result.device)
//            //println("All BLE result: ${result.device.name} ${result}")
//            if (result.device != null && result.device.name != null && result.device.name.contains("Tokidos", ignoreCase = true)){
//                println("BLE device: ${result.device.name}")
//                mBTDevices.add(result.device)
//                leDeviceListAdapter.add(result.device)
//                leDeviceListAdapter.notifyDataSetChanged()
//
//            }
//
//        }
//    }

    val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var isPermissionsGranted=true
        permissions.entries.forEach {
            Log.d("DEBUG", "${it.key} = ${it.value}")
            if(it.value == false){
                isPermissionsGranted = false
            }
        }
        if (isPermissionsGranted){
            isMultipleCubesPage=false
            BleScanUtils.getInstance.scanLeDevice(activity?.applicationContext)
        }
    }
    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("gra")
        } else {
            // PERMISSION NOT GRANTED
            println("deni")
        }
    }
    private fun checkAndRequestPermissions():Boolean{
        var blueToothConnectPermission:Int = PackageManager.PERMISSION_DENIED
        var blueToothScanPermission: Int = PackageManager.PERMISSION_DENIED
        var fineLocationPermission: Int = PackageManager.PERMISSION_DENIED
        var coarseLocationPermission: Int = PackageManager.PERMISSION_DENIED
        var blueToothAdminPermission: Int = PackageManager.PERMISSION_DENIED
        context?.apply {
            blueToothConnectPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            blueToothScanPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            fineLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            coarseLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            blueToothAdminPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
        }

        //expected false false false false false true
        println("checkAndRequestPermissions " +
                "blueToothConnect:${blueToothConnectPermission != PackageManager.PERMISSION_GRANTED} " +
                "blueToothScan:${blueToothScanPermission != PackageManager.PERMISSION_GRANTED} " +
                "fineLocation:${fineLocationPermission != PackageManager.PERMISSION_GRANTED} " +
                "coarseLocation:${coarseLocationPermission != PackageManager.PERMISSION_GRANTED} " +
                "blueToothAdmin:${blueToothAdminPermission != PackageManager.PERMISSION_GRANTED} " +
                "isEnabled:${mBluetoothAdapter?.isEnabled}")
        // above or equal to Android 12（API lvl 31）need two more permission:BLUETOOTH_CONNECT, BLUETOOTH_SCAN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //1. check if all permissions are granted
            if(blueToothConnectPermission != PackageManager.PERMISSION_GRANTED
                || blueToothScanPermission != PackageManager.PERMISSION_GRANTED
                || fineLocationPermission != PackageManager.PERMISSION_GRANTED
                || coarseLocationPermission != PackageManager.PERMISSION_GRANTED
                || blueToothAdminPermission != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(
//                    arrayOf(
//                        Manifest.permission.BLUETOOTH_CONNECT,
//                        Manifest.permission.BLUETOOTH_SCAN,
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION,
//                        Manifest.permission.BLUETOOTH_ADMIN
//                    ), 5
//                )
//                val requestMultiplePermissions = registerForActivityResult(
//                    ActivityResultContracts.RequestMultiplePermissions()
//                ) { permissions ->
//                    permissions.entries.forEach {
//                        Log.d("DEBUG", "${it.key} = ${it.value}")
//                    }
//                }
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADMIN
                    )
                )
                return false
            }else{
                println("has all permission")


                return true
            }
        } else {
            println("heh")
            if(fineLocationPermission != PackageManager.PERMISSION_GRANTED
                || coarseLocationPermission != PackageManager.PERMISSION_GRANTED
                || blueToothAdminPermission != PackageManager.PERMISSION_GRANTED) {
                val a = requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADMIN,
                    ), 6
                )
//                requestMultiplePermissions.launch(
//                    arrayOf(
//                        Manifest.permission.BLUETOOTH_CONNECT,
//                        Manifest.permission.BLUETOOTH_SCAN,
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION,
//                        Manifest.permission.BLUETOOTH_ADMIN
//                    )
//                )
                return false
            }else{
                println("has all permission")

                return true
            }
        }
    }

}