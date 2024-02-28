//package com.example.tokidosapplication.view.playcubes
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.NetworkCapabilities
//import android.net.NetworkRequest
//import android.net.wifi.WifiConfiguration
//import android.net.wifi.WifiManager
//import android.net.wifi.WifiNetworkSpecifier
//import android.os.Build
//import android.view.View
//import android.widget.Toast
//import com.example.tokidosapplication.view.BLEhelper
//import com.example.tokidosapplication.view.data.Wifi
//
//class WifiHelper(context: Context?) {
//    private lateinit var wifiManager: WifiManager
//
//
//    val instance = BLEhelper.getInstance()
//
//    private fun startWifiScan(){
//        checkPermissions()
//        val success = wifiManager.startScan()
//        binding.wifiLaodingView.visibility = View.VISIBLE
//        if (!success) {
//            scanFailure()
//        }
//    }
//
//    private fun addQuotationMarks(name:String?):String{
//        return "\"${name}\""
//    }
////    @SuppressLint("MissingPermission")
////    private fun connectParticularWifi(wifi: Wifi?, password: String){
////        binding.connectingWifiLoadingLayout.visibility= View.VISIBLE
////        //val ssid = if (Build.VERSION.SDK_INT >= 33) "\"${wifi!!.wifiSsid.toString()}\"" else "\"${wifi!!.SSID}\""
////        val ssid = addQuotationMarks(wifi?.ssid)
////        selectedWifi= wifi
////
////        instance.getBluetoothLeService()?.setConnectingWifi(wifi)
////
////        println("sdk: ${Build.VERSION.SDK_INT}")
////        //val wifiPassword =  "\"" + "Ga5l0U2g6@14" + "\""
////        val wifiPassword1 =  "\"" + "WeDesign4U" + "\""
////        val wifiPassword =  "\"" + password + "\""
////
////        inputWifiSSID = wifi?.ssid
////        inputWifiPassword = password
////
////        //println("LLLL $wifiPassword $wifiPassword1 ${wifiPassword==wifiPassword1}")
////        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
////            println("there $inputWifiSSID $wifiPassword $ssid ${wifiManager.isWifiEnabled} $wifiPassword1")
////            //https://stackoverflow.com/questions/10350119/unable-to-connect-with-wpa2-android
////            try {
////                val configurationList = wifiManager.configuredNetworks //已经保存密码的WiFi
//////                val item = configurationList.find { it.SSID == ssid }
//////                if (item != null) {
//////                    println("saved wifi $item")
//////                    //for saved password wifi, connect it directly
//////                    wifiManager.disconnect()
//////
//////                    wifiManager.enableNetwork(item.networkId, true)
//////                    wifiManager.reconnect()
//////
//////                }
//////                else {
////                val wifiConfiguration = WifiConfiguration()
////                //清除一些默认wifi的配置
////                wifiConfiguration.allowedAuthAlgorithms.clear()
////                wifiConfiguration.allowedGroupCiphers.clear()
////                wifiConfiguration.allowedKeyManagement.clear()
////                wifiConfiguration.allowedPairwiseCiphers.clear()
////                wifiConfiguration.allowedProtocols.clear()
////                wifiConfiguration.SSID = ssid
////
////                // if it's WAP-PSK or WAP2-PSK means WPA
////                if (wifi?.securityType?.contains("WPA-PSK") == true || wifi?.securityType?.contains("WPA2-PSK") == true) {
////                    wifiConfiguration.preSharedKey = wifiPassword
////                    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
////                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
////                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
////                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
////                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
////                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
////                    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
////                    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
////                    wifiConfiguration.status = WifiConfiguration.Status.ENABLED
////
////                }  else if (wifi?.securityType?.contains("WEP") == true) {
////                    wifiConfiguration.wepKeys[0] = addQuotationMarks(password)
////                    wifiConfiguration.wepTxKeyIndex = 0;
////                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
////                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
////                } else if (wifi?.securityType?.contains("ESS") == true) {
////                    // ESS means no password
////                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
////                } else if (wifi?.securityType?.contains("WPA2-EAP") == true) {
////                    Toast.makeText(requireContext(), "Enterpise Wifi Doesn't support", Toast.LENGTH_SHORT).show()
////                }
////                val netId = wifiManager.addNetwork(wifiConfiguration)
////                println("netID: $netId")
////                wifiManager.disconnect()
////                wifiManager.enableNetwork(netId, true)
////                wifiManager.reconnect()
////                //}
////
////                handler.postDelayed({
////                    //if(wifiInfo.bssid != selectedWifi?.bssid){
////                    println("wifiInfo.ssid != selectedWifi?.ssid: ${wifiInfo.ssid} ${selectedWifi?.ssid} ${"\"selectedWifi?.ssid\""}")
////                    if(wifiInfo.ssid != "\"${selectedWifi?.ssid}\""){ //wifiInfo.ssid has double quotes but selectedWifi?.ssid has no quotes
////                        updateUIFail()
////                        //不要忘记handler.removeCallbacksAndMessages(null)，当success的时候和关闭等待连接窗口
////
////                    }
////                }, CONNECT_PERIOD)
////            } catch (e: Exception) {
////                e.printStackTrace()
////            }
////        }else {
////            println("here")
////            val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
////                .setSsid( ssid )
////                .setWpa2Passphrase(wifiPassword)
////                .build()
////            val networkRequest = NetworkRequest.Builder()
////                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
////                .setNetworkSpecifier(wifiNetworkSpecifier)
////                .build()
////            connectivityManager = activity?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
////
////            connectivityManager?.requestNetwork(networkRequest,networkCallback)
////        }
////    }
//
//}