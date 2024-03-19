package com.example.tokidosapplication.view.playcubes

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.MacAddress
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.net.wifi.hotspot2.PasspointConfiguration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PatternMatcher
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.tokidosapplication.ACTION_WIFI_CONNECTED
import com.example.tokidosapplication.ACTION_WIFI_UNAVAILABLE
import com.example.tokidosapplication.BluetoothLeService
import com.example.tokidosapplication.MyTask
import com.example.tokidosapplication.R
import com.example.tokidosapplication.SECURITY_EAP
import com.example.tokidosapplication.SECURITY_NONE
import com.example.tokidosapplication.SECURITY_WPA
import com.example.tokidosapplication.SECURITY_WEP
import com.example.tokidosapplication.authentication.MyAmplifyAuthClient
import com.example.tokidosapplication.databinding.FragmentWifiPairingBinding
import com.example.tokidosapplication.executeQueue
import com.example.tokidosapplication.registrationToken
import com.example.tokidosapplication.registration_token_char_uuid
import com.example.tokidosapplication.view.BLEhelper
import com.example.tokidosapplication.view.data.Wifi
import com.example.tokidosapplication.wifiConnection
import com.example.tokidosapplication.wifiPassword
import com.example.tokidosapplication.wifiSSID
import com.example.tokidosapplication.wifiSecurityType
import com.example.tokidosapplication.wifi_connection_uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WifiPairingFragment : Fragment() {

    private lateinit var binding: FragmentWifiPairingBinding
    private var selectedWifi: Wifi? = null

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiArrayAdapter: WifiAdaptor

    private var connectivityManager: ConnectivityManager? = null
    private lateinit var instance: BLEhelper

    private var wifiArrayList: ArrayList<ScanResult> = ArrayList()
    private var wifiList: ArrayList<Wifi> = ArrayList()
    private var inputWifiSSID: String? = null
    private var inputWifiPassword: String? = null
    private var wifiInfo: WifiInfo? = null
        private val handler = Handler(Looper.getMainLooper())
    // Stops check connect after 10 seconds.
    private val CONNECT_PERIOD: Long = 10000
    private  var newNetworkCallBack: ConnectivityManager.NetworkCallback? = null
    private var jobSendWifiCredentials:Job? = null
    private var dequeuedTask:MyTask? =null

    var isFirstTime = true
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    // Show all the supported services and characteristics on the user interface.
                    instance.getBluetoothLeService()?.displayGattServices(instance.getBluetoothLeService()?.getSupportedGattServices())
                }
                BluetoothLeService.ACTION_WRITE_SUCCESS ->{
                    //TODO: go next dequeue
                    val callbackData = intent.getStringExtra(BluetoothLeService.ACTION_WRITE_SUCCESS)
                    println("gattUpdateReceiver $callbackData ${dequeuedTask!=null} ${callbackData ==dequeuedTask!!.characteristicUUID?.uuid.toString()} ${executeQueue.getInstance().getQueueSize()}")
                    if(dequeuedTask != null){
                        if(callbackData == dequeuedTask!!.characteristicUUID?.uuid.toString()){
                            dequeuedTask = executeQueue.getInstance().dequeue()
                        }
                    }else{//free memory once dequeued completed
                        executeQueue.getInstance().destroy()
                    }
                }
                BluetoothLeService.ACTION_DATA_AVAILABLE->{
                    //get data from intent
                    val callbackData1 = intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                    val callbackData = intent.getStringExtra(BluetoothLeService.EXTRA_DATA_CHARACTERISTIC)
                    println("ACTION_DATA_AVAILABLE: $callbackData $callbackData1 ${dequeuedTask?.characteristicUUID}")

                    if(dequeuedTask != null){
                        if(callbackData == dequeuedTask!!.characteristicUUID?.uuid.toString()){
                            //check wifi first because we enqueue read wifi connection first, which means it gets dequeued first than read token
                            if(callbackData1?.contains("Not Connected") == true || callbackData1?.contains("No Token") == true || callbackData1?.contains("Token in progress") == true || callbackData1?.contains("Token error") == true){

                                updateUIFail()
                                return
                            }
                            else if(callbackData1?.contains("Token success") == true && callbackData == registration_token_char_uuid){
                                //if goes here means read wifi connection returns "Connected"
                                updateUIHidePairingLoading()
                                Navigation.findNavController(binding.root).navigate(R.id.action_wifiPairingFragment_to_allDoneFragment)
                                return
                            }
                            dequeuedTask = executeQueue.getInstance().dequeue()
                        }
                    }else{//free memory once dequeued completed
                        executeQueue.getInstance().destroy()
                    }
//                    if(callbackData.contains("Not Connected") || callbackData.contains("No Token") || callbackData.contains("Token in progress") || callbackData.contains("Token error")){
//                        updateUIFail()
//                        return
//                    }else if (callbackData?.contains("Connected") == true && isFirstTime){
//                        updateUIHidePairingLoading()
//                        Navigation.findNavController(binding.root).navigate(R.id.action_wifiPairingFragment_to_allDoneFragment)
//                        isFirstTime = false
//                    }

                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWifiPairingBinding.inflate(layoutInflater, container, false)

        wifiManager = activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager

        lifecycle.addObserver(binding.wifiLaodingView)

        instance = BLEhelper.getInstance()

        println("GATT connected Wifi ${BLEhelper.getInstance().getBluetoothLeService()} " +
                "${BLEhelper.getInstance().getBluetoothLeService()?.getBluetoothGatt()} ${BLEhelper.getInstance().getBluetoothLeService()?.getCharacteristicsMap()}")

        //scan wifi
        startWifiScan()

        wifiArrayAdapter = WifiAdaptor(wifiList, object : WifiAdaptor.MyItemClickListener{
            override fun onItemClicked(data: ArrayList<Wifi>, position: Int) {
                if (Build.VERSION.SDK_INT < 30){println("connectWifi ${data[position]}")}
                if (data[position].securityType == SECURITY_NONE){
                    //TODO: send ssid and mac address directly to Cubes through BLE
                    sendCredentials(data[position], "anything")
                    Toast.makeText(requireContext(), "TODO: send ssid and mac address directly to Cubes through BLE", Toast.LENGTH_SHORT).show()
                }
                else if (data[position].securityType == SECURITY_EAP){
                    Toast.makeText(requireContext(), "Maybe TODO: you are connecting EAP security type wifi, may need more info(like NeuronicWorks-KW wifi is EAP)", Toast.LENGTH_SHORT).show()
                }
                else if (data[position].ssid == "Other..."){
                    activity?.let { WifiPasswordFragment(true, data[position], object : WifiPasswordFragment.WifiPasswordDialogFragmentListener{
                        override fun connectWifi(wifi: Wifi?, password: String) {
                            sendCredentials(wifi, password)
                        }
                    }).show(it.supportFragmentManager, "WifiPasswordDialogFragment") }
                }else{
                    activity?.let { WifiPasswordFragment(false, data[position], object : WifiPasswordFragment.WifiPasswordDialogFragmentListener{
                        override fun connectWifi(wifi: Wifi?, password: String) {
                            println("password: $password")
                            sendCredentials(wifi, password)
                        }
                    }).show(it.supportFragmentManager, "WifiPasswordDialogFragment") }
                }
            }
        })
        wifiList.clear()
        binding.wifiListRecyclerView.adapter = wifiArrayAdapter
        binding.wifiListRecyclerView.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )
        binding.refreshListWifi.setOnClickListener{
            val mBluetoothManager = activity?.applicationContext?.getSystemService(BluetoothManager::class.java)
            var devices:MutableList<BluetoothDevice>? = null
//            if (ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                devices = mBluetoothManager?.getConnectedDevices(BluetoothProfile.GATT);
//            }
//            val a = BluetoothLeService().getSupportedGattServices()
//            val d = bluetoothService?.getBluetoothGatt()
//            println("pqpqpqp555: $d")
//            d?.discoverServices()

//            if (devices != null) {
//                for (device in devices){
//                    println(device)
//                }
//
//            }
            updateUIRefresh()
            wifiList.clear()
            binding.wifiListRecyclerView.adapter = wifiArrayAdapter
            startWifiScan()
        }
        binding.imageViewCross1.setOnClickListener{
            updateUIHidePairingLoading()

        }
        binding.wifiFailScreen.tryAgainWifi.setOnClickListener{
            updateUIRefresh()
            wifiList.clear()
            binding.wifiListRecyclerView.adapter = wifiArrayAdapter
            startWifiScan()
        }

        // Define the Y translations for the dots
        val translateY = 20
        // Dot 1 Animation
        val dot1Animator = ObjectAnimator.ofFloat(binding.imageViewThreeDot1, "translationY", 0f, translateY.toFloat()).apply {
            duration = 1000 // Adjust duration as needed
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Dot 2 Animation
        val dot2Animator = ObjectAnimator.ofFloat(binding.imageViewThreeDot2, "translationY", 0f, translateY.toFloat()).apply {
            startDelay = 250 // Delay start by 250ms
            duration = 1000 // Adjust duration as needed
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Dot 3 Animation
        val dot3Animator = ObjectAnimator.ofFloat(binding.imageViewThreeDot3, "translationY", 0f, translateY.toFloat()).apply {
            startDelay = 500 // Delay start by 500ms
            duration = 1000 // Adjust duration as needed
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Start animations
        dot1Animator.start()
        dot2Animator.start()
        dot3Animator.start()
        return binding.root
    }

    fun getWifiSecurity(result: ScanResult): Int {
        if (result?.capabilities?.contains("WPA-PSK") == true || result?.capabilities?.contains("WPA2-PSK") == true) {
            return SECURITY_WPA
        }else if (result?.capabilities?.contains("WEP") == true) {
            return SECURITY_WEP
        }  else if (result?.capabilities?.contains("WPA2-EAP") == true) {
            return SECURITY_EAP
        }else if (result?.capabilities?.contains("ESS") == true) {
            return SECURITY_NONE
        }else{
            return SECURITY_NONE
        }

    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        updateUIHidePairingLoading()

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        activity?.registerReceiver(wifiScanReceiver, intentFilter, Context.RECEIVER_EXPORTED)

        val intentFilter1 = IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);
        context?.registerReceiver(wifiNetworkSuggestionReceiver, intentFilter1)

        IntentFilter().apply {
            this.addAction(ACTION_WIFI_CONNECTED)
            this.addAction(ACTION_WIFI_UNAVAILABLE)
            activity?.registerReceiver(wifiConnectionReceiver, this, Context.RECEIVER_EXPORTED)
        }

        activity?.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter(),
            Context.RECEIVER_EXPORTED)

//        val request = NetworkRequest.Builder()
//            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//            .build()
        connectivityManager = activity?.applicationContext?.getSystemService(ConnectivityManager::class.java)
        //connectivityManager?.registerNetworkCallback(request, networkCallback)// For listen

        println("characteristicsMap WifiPairPage ${instance.getBluetoothLeService()?.getCharacteristicsMap()}")
    }
    fun makeGattUpdateIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
            addAction(BluetoothLeService.ACTION_WRITE_SUCCESS)
            addAction(BluetoothLeService.EXTRA_DATA_CHARACTERISTIC)
        }
    }
    override fun onPause() {
        super.onPause()
        selectedWifi=null
        handler.removeCallbacksAndMessages(null)
        jobSendWifiCredentials?.cancel()
        println("unregister onPause gattUpdateReceiver and networkCallback")
        activity?.unregisterReceiver(wifiScanReceiver)
        activity?.unregisterReceiver(wifiConnectionReceiver)
        //connectivityManager?.unregisterNetworkCallback(networkCallback)

        //newNetworkCallBack?.let { connectivityManager?.unregisterNetworkCallback(it) }

        activity?.unregisterReceiver(gattUpdateReceiver)
        context?.unregisterReceiver(wifiNetworkSuggestionReceiver)

    }

    override fun onDestroy() {
        super.onDestroy()
//        selectedWifi=null
//        handler.removeCallbacksAndMessages(null)
//        println("unregister onDestroy gattUpdateReceiver and networkCallback")
//        activity?.unregisterReceiver(wifiScanReceiver)
//        requireContext().unregisterReceiver(wifiConnectionReceiver)
//        connectivityManager?.unregisterNetworkCallback(networkCallback)
//
//        //newNetworkCallBack?.let { connectivityManager?.unregisterNetworkCallback(it) }
//
//        activity?.unregisterReceiver(gattUpdateReceiver)
    }
    private fun startWifiScan(){
        println("Start wifi scan")
        checkPermissions()
        val success = wifiManager.startScan()
        binding.wifiLaodingView.visibility = View.VISIBLE
        if (!success) {
            scanFailure()
        }
    }
    private fun addQuotationMarks(name:String?):String{
        return "\"${name}\""
    }
    private fun sendCredentials(wifi: Wifi?, password: String){
        //reset somethings
        isFirstTime=true
        binding.connectingWifiLoadingLayout.visibility=View.VISIBLE

        selectedWifi= wifi
        instance.getBluetoothLeService()?.setConnectingWifi(wifi)
        println("sdk: ${Build.VERSION.SDK_INT}")
        //val wifiPassword =  "\"" + "Ga5l0U2g6@14" + "\""
        //val wifiPassword =  "\"" + password + "\""
        //inputWifiSSID = if (Build.VERSION.SDK_INT >= 33) wifi.wifiSsid.toString() else wifi.SSID
        inputWifiSSID = wifi?.ssid
        inputWifiPassword = password

        jobSendWifiCredentials = CoroutineScope(Dispatchers.IO).launch {
            val characteristicsMap = instance.getBluetoothLeService()?.getCharacteristicsMap()
            val token = MyAmplifyAuthClient.getInstance(requireContext()).getAccessToken()

            println("sda: $characteristicsMap $token")
            if (characteristicsMap != null) {
                // add task to queue
                val queue = executeQueue.getInstance()
                queue.cleanQueue()
                //add wifi credentials
                characteristicsMap[wifiSSID]?.let { queue.enqueue(MyTask(it,inputWifiSSID!!)) }
                characteristicsMap[wifiPassword]?.let { queue.enqueue(MyTask(it,inputWifiPassword!!)) }
                characteristicsMap[wifiSecurityType]?.let { queue.enqueue(MyTask(it,"anything for now")) }
                //read wifi connection on connected BLE device
                characteristicsMap[wifiConnection]?.let { queue.enqueue(MyTask(it)) }
                if(token.isSuccess){
                    //write token to connected BLE device
                    val tokenString = token.getOrNull()!!
                    val numPacket = tokenString.length.div(500)
                    for (i:Int in 0..numPacket){
                        val totalPacket = "${numPacket.div(10)}${numPacket%10}"
                        val positionPacket = "${i.div(10)}${i%10}"
                        var subData:String
                        if(i==numPacket){
                            subData = tokenString.substring(500*i)
                        }else{
                            subData = tokenString.substring(500*i, 500*(i+1)-1)
                        }
                        val packetData = "$totalPacket$positionPacket$subData"
                        println("packetData $packetData")
                        characteristicsMap[registrationToken]?.let { queue.enqueue(MyTask(it,packetData)) }
                    }
                    //read wifi registrationToken is valid/invalid on connected BLE device
                    characteristicsMap[registrationToken]?.let { queue.enqueue(MyTask(it)) }
                }
                dequeuedTask = queue.dequeue()
            }else{
                binding.connectingWifiLoadingLayout.visibility=View.INVISIBLE
                withContext(Dispatchers.Main){
                    Toast.makeText(context, "No characteristics being read", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun scanSuccess() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            val rawResults = wifiManager.scanResults
            val filterReult: MutableList<ScanResult>
            rawResults.forEach { aa -> println("LL: adding raw ${aa}")}
            //remove duplicates in raw result
            val uniqueResults: Set<ScanResult> = HashSet<ScanResult>(rawResults)
            println("LL: adding ${uniqueResults}")
            //filter out result with blank wifi name
            if (Build.VERSION.SDK_INT >= 33){
                filterReult = rawResults.filter{
                    it?.wifiSsid.toString().isNotBlank() && it?.wifiSsid.toString().isNotEmpty()
                }.toMutableList()
            }else{
                filterReult = rawResults.filter{
                    it.SSID.toString().isNotBlank() && it?.SSID.toString().isNotEmpty()
                }.toMutableList()
            }
            wifiList.clear()

            //Conver to data class Wifi and merge two different ScanResult with same ssid due to dual-band
            convertWifiAndMergeScanResultsWithSsid(filterReult)

            //wifiList.addAll(uniqueOrderedResults)
            wifiArrayAdapter.notifyDataSetChanged()

            binding.wifiLaodingView.visibility = View.INVISIBLE
        }
    }
    private fun convertWifiAndMergeScanResultsWithSsid(scanResults:MutableList<ScanResult>){
        val map = HashMap<String, ScanResult>()
        //merge two different ScanResult with same ssid due to dual-band
        scanResults.forEach { scanResult ->
            val scanResultSsid = if (Build.VERSION.SDK_INT >= 33) scanResult.wifiSsid.toString() else scanResult.SSID
            if (!map.containsKey(scanResultSsid)) map[scanResultSsid]=scanResult  else map[scanResultSsid]=scanResult//no need to care 2.4G or 5G frequency on same name, because connection only need ssid,password and security type. Also if both appear 2.4&5, it will always connect to 5 if 5 rssi is not too weak
        }
        //conver to Wifi data class
        map.forEach{
            //Build.VERSION.SDK_INT >= 33 ssid has double quotes, need to remove
            val mapSsid = if (Build.VERSION.SDK_INT >= 33) it.value.wifiSsid.toString().substring(1, it.value.wifiSsid.toString().length -1) else it.value.SSID
            val rssi = it.value.level

            wifiList.add(Wifi(mapSsid,rssi,"",getWifiSecurity(it.value),it.value.BSSID))
            //wifiArrayAdapter.notifyItemInserted()
        }
        wifiList.sortedByDescending { result -> result.ssid }
        wifiList.add(Wifi("Other...",0,"",SECURITY_WPA))
    }
    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("LL lala")
            return
        }else{
            val results =  wifiManager.scanResults
            println("scanFailure $results")
            updateUIFail()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            val success = wifiManager.startScan()
            if (!success) {
                // scan failure handling
                scanFailure()
            }
        }
    }
    private fun updateUIHidePairingLoading(){
        binding.connectingWifiLoadingLayout.visibility = View.INVISIBLE
        handler.removeCallbacksAndMessages(null)
        jobSendWifiCredentials?.cancel()

    }
    private fun updateUIFail(){
        binding.wifiLaodingView.visibility = View.INVISIBLE
        binding.wifiScanScreen.visibility = View.INVISIBLE
        binding.wifiFailScreen.root.visibility = View.VISIBLE
        binding.connectingWifiLoadingLayout.visibility=View.INVISIBLE
    }
    private fun updateUIRefresh(){
        binding.wifiLaodingView.visibility = View.VISIBLE
        binding.wifiScanScreen.visibility = View.VISIBLE
        binding.wifiFailScreen.root.visibility = View.GONE
    }
    private fun checkPermissions(){
        val locationManager = activity?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        val ch1=ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_WIFI_STATE
        ) != PackageManager.PERMISSION_GRANTED
        val ch2=ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CHANGE_WIFI_STATE
        ) != PackageManager.PERMISSION_GRANTED
        val ch3=ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        val ch4 = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        val ch5= ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CHANGE_NETWORK_STATE
        ) != PackageManager.PERMISSION_GRANTED
        val ch6 = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_SETTINGS
        ) != PackageManager.PERMISSION_GRANTED
        println("LL lala1: ACCESS_WIFI_STATE:$ch1 $ch2 $ch3 $ch4 $ch5 $ch6 ${wifiManager.isWifiEnabled} ${Build.VERSION.SDK_INT}")
        if(!wifiManager.isWifiEnabled) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startActivity( Intent(Settings.Panel.ACTION_WIFI));
            } else {
                // Use a full page activity - if Wifi is critcal for your app
                startActivity( Intent(Settings.ACTION_WIFI_SETTINGS));
                // Or use the deprecated method
                //wifiManager.isWifiEnabled = true
                //binding.wifiSwitch.isChecked = wifiManager.isWifiEnabled
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !locationManager.isLocationEnabled) {
                println("lalsll214 ${locationManager.isLocationEnabled}")
                buildAlertMessageNoGps();

            }else{
                println("lalsll555 ${locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)}, ${locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER)}")

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(
                        LocationManager.NETWORK_PROVIDER)) {
                    buildAlertMessageNoGps();
                }
            }
        }
    }
    private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes",
                DialogInterface.OnClickListener { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
            .setNegativeButton("No",
                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert: AlertDialog = builder.create()
        alert.show()
    }


    /***
     *  Wifi Connection: this part is not using in the app, but it's helpful for potential future request
     */
    val wifiNetworkSuggestionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!intent.action.equals(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                return;
            }
            // do post connect processing here
            println("wifiNetworkSuggestionReceiver")
        }
    };

    private val wifiConnectionReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            println("我不信")
            when(intent.action){
                ACTION_WIFI_UNAVAILABLE->{
                    connectivityManager?.unregisterNetworkCallback(networkCallback)
                }
                ACTION_WIFI_CONNECTED ->{
                    //send wifi ssid&password to BLE connected device
//                    CoroutineScope(Dispatchers.IO).launch {
//                        val characteristicsMap = instance.getBluetoothLeService()?.getCharacteristicsMap()
//                        if (characteristicsMap != null) {
//                            //write wifi credentials(ssid & password) to connected BLE device
//                            characteristicsMap[wifiSSID]?.let { instance.getBluetoothLeService()?.writeCharacteristic(it,inputWifiSSID!!) }
//                            delay(2000)
//                            characteristicsMap[wifiPassword]?.let { instance.getBluetoothLeService()?.writeCharacteristic(it,inputWifiPassword!!) }
//                            delay(2000)
//                            characteristicsMap[wifiSecurityType]?.let { instance.getBluetoothLeService()?.writeCharacteristic(it,"anything for now") }
//                            delay(2000)
//                            //read wifi connection on connected BLE device
//                            //characteristicsMap[wifiConnection]?.let { instance.getBluetoothLeService()?.readCharacteristic(it) }
//                            withContext(Dispatchers.Main){
//                                updateUIHidePairingLoading()
//                                connectivityManager?.unregisterNetworkCallback(networkCallback)
//
//                                //CoroutineScope(Dispatchers.Main).launch {
//                                Navigation.findNavController(binding.root).navigate(R.id.action_wifiPairingFragment_to_allDoneFragment)
//
//                                //}
//                            }
//                        }else{
//                            binding.connectingWifiLoadingLayout.visibility=View.INVISIBLE
//                            withContext(Dispatchers.Main){
//                                Toast.makeText(context, "No characteristics being read", Toast.LENGTH_SHORT).show()
//                            }
//
//                        }
//                    }
                    //After successfully connect to wifi, send wifi info to cube and Cube connected wifi, remove handler runnable job, and navigate to next page
//                    updateUIHidePairingLoading()
//                    connectivityManager?.unregisterNetworkCallback(networkCallback)
//
//                    Navigation.findNavController(binding.root).navigate(R.id.action_wifiPairingFragment_to_allDoneFragment)

                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun connectParticularWifi(wifi: Wifi?, password: String){
        //reset somethings
        isFirstTime=true
        binding.connectingWifiLoadingLayout.visibility=View.VISIBLE

        //val ssid = if (Build.VERSION.SDK_INT >= 33) "\"${wifi!!.wifiSsid.toString()}\"" else "\"${wifi!!.SSID}\""
        val ssid = addQuotationMarks(wifi?.ssid)
        selectedWifi= wifi
        instance.getBluetoothLeService()?.setConnectingWifi(wifi)
        println("sdk: ${Build.VERSION.SDK_INT}")
        //val wifiPassword =  "\"" + "Ga5l0U2g6@14" + "\""
        val wifiPassword1 =  "\"" + "WeDesign4U" + "\""
        val wifiPassword =  "\"" + password + "\""
        //inputWifiSSID = if (Build.VERSION.SDK_INT >= 33) wifi.wifiSsid.toString() else wifi.SSID
        inputWifiSSID = wifi?.ssid
        inputWifiPassword = password
        //println("LLLL $wifiPassword $wifiPassword1 ${wifiPassword==wifiPassword1}")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            println("there $inputWifiSSID wifiPassword:$wifiPassword ssid:$ssid ${wifiManager.isWifiEnabled} $wifiPassword1")
            //https://stackoverflow.com/questions/10350119/unable-to-connect-with-wpa2-android
            try {
                val request = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build()
                connectivityManager = activity?.applicationContext?.getSystemService(ConnectivityManager::class.java)
                connectivityManager?.registerNetworkCallback(request, networkCallback)// For listen
                val configurationList = wifiManager.configuredNetworks //已经保存密码的WiFi
//                val item = configurationList.find { it.SSID == ssid }
//                if (item != null) {
//                    println("saved wifi $item")
//                    //for saved password wifi, connect it directly
//                    wifiManager.disconnect()
//
//                    wifiManager.enableNetwork(item.networkId, true)
//                    wifiManager.reconnect()
//
//                }
//                else {
                val wifiConfiguration = WifiConfiguration()
                //清除一些默认wifi的配置
                wifiConfiguration.allowedAuthAlgorithms.clear()
                wifiConfiguration.allowedGroupCiphers.clear()
                wifiConfiguration.allowedKeyManagement.clear()
                wifiConfiguration.allowedPairwiseCiphers.clear()
                wifiConfiguration.allowedProtocols.clear()
                wifiConfiguration.SSID = ssid

                // if it's WAP-PSK or WAP2-PSK means WPA
                if (wifi?.securityType == SECURITY_WPA) {
                    wifiConfiguration.preSharedKey = wifiPassword
                    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                    wifiConfiguration.status = WifiConfiguration.Status.ENABLED

                }  else if (wifi?.securityType == SECURITY_WEP) {
                    wifiConfiguration.wepKeys[0] = wifiPassword
                    wifiConfiguration.wepTxKeyIndex = 0;
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                }  else if (wifi?.securityType == SECURITY_EAP) {
                    Toast.makeText(requireContext(), "Enterpise Wifi Doesn't support", Toast.LENGTH_SHORT).show()
                }  else if (wifi?.securityType == SECURITY_NONE) {
                    // ESS means no password
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                }
                val netId = wifiManager.addNetwork(wifiConfiguration)
                println("netID: $netId")
                wifiManager.disconnect()
                wifiManager.enableNetwork(netId, true)
                wifiManager.reconnect()
                //}

                handler.postDelayed({
                    //if(wifiInfo.bssid != selectedWifi?.bssid){
                    if (wifiInfo != null){
                        println("wifiInfo.ssid != selectedWifi?.ssid: ${wifiInfo!!.ssid} ${selectedWifi?.ssid} ${"\"selectedWifi?.ssid\""}")
                        if(wifiInfo!!.ssid != "\"${selectedWifi?.ssid}\""){ //wifiInfo.ssid has double quotes but selectedWifi?.ssid has no quotes
                            updateUIFail()
                            //不要忘记handler.removeCallbacksAndMessages(null)，当success的时候和关闭等待连接窗口

                        }
                    }else{
                        updateUIFail()
                    }

                }, CONNECT_PERIOD)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else {
            println("here: $ssid $wifiPassword $selectedWifi ${wifi!!.bssid}")
//            val suggestion1 = WifiNetworkSuggestion.Builder()
//                .setSsid(ssid.substring(1, ssid.length -1))
//                .setIsAppInteractionRequired(true) // Optional (Needs location permission)
//                .build();
            val suggestion2 = WifiNetworkSuggestion.Builder()
                .setSsid(ssid.substring(1, ssid.length -1))
                .setWpa2Passphrase(wifiPassword.substring(1, wifiPassword.length -1))
                .setIsAppInteractionRequired(true)// Optional (Needs location permission)
                .build()
//            val suggestion3 = WifiNetworkSuggestion.Builder()
//                .setSsid(ssid.substring(1, ssid.length -1))
//                .setWpa3Passphrase(wifiPassword.substring(1, wifiPassword.length -1))
//                .setIsAppInteractionRequired(true) // Optional (Needs location permission)
//                .build();
//            var  suggestionsList:List<WifiNetworkSuggestion>
//            var suggestion4:WifiNetworkSuggestion
//            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R){
//                val passpointConfig = PasspointConfiguration(); // configure passpointConfig to include a valid Passpoint configuration
//                suggestion4 = WifiNetworkSuggestion.Builder()
//                    .setPasspointConfig(passpointConfig)
//                    .setIsAppInteractionRequired(true) // Optional (Needs location permission)
//                    .build();
//                suggestionsList = listOf(suggestion1, suggestion2, suggestion3, suggestion4);
//            }else{
//                suggestionsList = listOf(suggestion1, suggestion2, suggestion3);
//
//            }
            val suggestionsList = listOf(suggestion2)
            val status = wifiManager.addNetworkSuggestions(suggestionsList);
            if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                // do error handling here
            }

            println("status: $status ")


            val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
                //.setSsid(ssid.substring(1, ssid.length -1))
                .setSsidPattern(PatternMatcher(ssid.substring(1, ssid.length -1), PatternMatcher.PATTERN_PREFIX))
                .setBssid(MacAddress.fromString(wifi.bssid))
                .setWpa2Passphrase(wifiPassword.substring(1, wifiPassword.length -1))
                .build()
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)//.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build()
            //connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            connectivityManager?.requestNetwork(networkRequest,networkCallback)
        }
    }
    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        context?.sendBroadcast(intent)
    }

    private val networkCallback =   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO ) {

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)

                val info: WifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    networkCapabilities.transportInfo as WifiInfo
                } else {
                    wifiManager.connectionInfo
                }
                wifiInfo=info
                val connectedWifiName = info.ssid.replace("\"", "")

                //val selectedWifiSSID = if (Build.VERSION.SDK_INT >= 33) "${selectedWifi?.wifiSsid.toString()}" else "${selectedWifi?.SSID}"
                val selectedWifiSSID = selectedWifi?.ssid.toString()

                println("network onCapabilitiesChanged:$connectedWifiName info.bssid:${info.bssid}  selectedWifi?.BSSID:${selectedWifi?.bssid} info.ssid:${info.ssid.toString()} selectedWifi.ssid:${selectedWifi?.ssid} connectedWifiName：$connectedWifiName selectedWifiSSID:$selectedWifiSSID ${connectedWifiName == selectedWifiSSID} ${info.bssid == selectedWifi?.bssid} info.macAddress:${info.macAddress} ${info.ssid.length} ${networkCapabilities.transportInfo} $info ")
                //connectedWifiName == selectedWifiSSID is because for Dual-band concurrent (5GHz/2.4GHz), so connect to 2.4G mac address will jump to 5G mac address, so can't use mac address to verify
                //info.bssid == selectedWifi?.BSSID verification works for none Dual-band concurrent or Dual-band concurrent but 5G signal strength is weak, it will not automatically jump to 5G if user initally connect to 2.4G
//            if (info.bssid == selectedWifi?.bssid || connectedWifiName == selectedWifiSSID){
//                broadcastUpdate(ACTION_WIFI_CONNECTED)
//            }
                if (connectedWifiName == selectedWifiSSID  && isFirstTime ){
                    broadcastUpdate(ACTION_WIFI_CONNECTED)
                    isFirstTime = false

                }

            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                println("network onAvailable")
                // To make sure that requests don't go over mobile data
                connectivityManager?.bindProcessToNetwork(network)
            }
            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                println("network onLosing")
                // This is to stop the looping request for OnePlus & Xiaomi model
                connectivityManager?.bindProcessToNetwork(null)
            }
            override fun onLost(network: Network) {
                super.onLost(network)
                println("network onLost")
            }
            override fun onUnavailable() {
                super.onUnavailable()
                println("network onUnavailable")
                broadcastUpdate(ACTION_WIFI_UNAVAILABLE)

            }
            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
                val a = linkProperties.domains
                println("network onLinkPropertiesChanged $a")

            }
        }
    }else{
        object : ConnectivityManager.NetworkCallback() {

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)

                val info: WifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    networkCapabilities.transportInfo as WifiInfo
                } else {
                    wifiManager.connectionInfo
                }
                wifiInfo=info
                val connectedWifiName = info.ssid.replace("\"", "")

                //val selectedWifiSSID = if (Build.VERSION.SDK_INT >= 33) "${selectedWifi?.wifiSsid.toString()}" else "${selectedWifi?.SSID}"
                val selectedWifiSSID = selectedWifi?.ssid.toString()

                println("network onCapabilitiesChanged:$connectedWifiName info.bssid:${info.bssid}  selectedWifi?.BSSID:${selectedWifi?.bssid} info.ssid:${info.ssid.toString()} selectedWifi.ssid:${selectedWifi?.ssid} connectedWifiName：$connectedWifiName selectedWifiSSID:$selectedWifiSSID ${connectedWifiName == selectedWifiSSID} ${info.bssid == selectedWifi?.bssid} info.macAddress:${info.macAddress} $info ")
                //connectedWifiName == selectedWifiSSID is because for Dual-band concurrent (5GHz/2.4GHz), so connect to 2.4G mac address will jump to 5G mac address, so can't use mac address to verify
                //info.bssid == selectedWifi?.BSSID verification works for none Dual-band concurrent or Dual-band concurrent but 5G signal strength is weak, it will not automatically jump to 5G if user initally connect to 2.4G
//            if (info.bssid == selectedWifi?.bssid || connectedWifiName == selectedWifiSSID){
//                broadcastUpdate(ACTION_WIFI_CONNECTED)
//            }

                if (connectedWifiName == selectedWifiSSID && isFirstTime){
                    broadcastUpdate(ACTION_WIFI_CONNECTED)
                    isFirstTime = false
                }

            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                println("network onAvailable")
                // To make sure that requests don't go over mobile data
                connectivityManager?.bindProcessToNetwork(network)
            }
            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                println("network onLosing")
                // This is to stop the looping request for OnePlus & Xiaomi model
                connectivityManager?.bindProcessToNetwork(null)
            }
            override fun onLost(network: Network) {
                super.onLost(network)
                println("network onLost")
            }
            override fun onUnavailable() {
                super.onUnavailable()
                println("network onUnavailable")
                broadcastUpdate(ACTION_WIFI_UNAVAILABLE)
            }
            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
                val a = linkProperties.domains
                println("network onLinkPropertiesChanged $a")
            }
        }
    }

}