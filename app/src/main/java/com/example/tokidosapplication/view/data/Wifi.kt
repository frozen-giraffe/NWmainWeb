package com.example.tokidosapplication.view.data

import com.example.tokidosapplication.SECURITY_WPA

data class Wifi(val ssid: String = "", val rssi: Int, val password: String="", val securityType: Int = SECURITY_WPA, val bssid: String = ""){

}