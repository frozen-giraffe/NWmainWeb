package com.example.tokidosapplication


const val service_uuid="b2bbc642-46da-11ed-b878-0242ac120002"
const val wifiSSID="wifiSSID"
const val ssid_char_uuid="3a308486-0609-415f-8b2e-8464d24a8fde"// can't readCharacteristic
const val wifiPassword="wifiPassword"
const val wifi_password_char_uuid="f299a682-8544-4af7-b564-72c92e65263a"// can't readCharacteristic
const val wifiSecurityType="wifiSecurityType"
const val security_type_uuid="386bfa8f-7bfc-40f7-890e-749fc9ee18f3"// can't readCharacteristic
const val wifiConnection="wifiConnection"
const val wifi_connection_uuid="b1e2fb25-693d-4658-90c0-b095ae2360b9"// can readCharacteristic(says if wifi connected or not)
const val wifiList="wifiList"
const val wifi_list_uuid="772b4b7a-0494-40a6-bc17-416245249ee7"//can readCharacteristic(comma separated wifi list)
const val ACTION_WIFI_CONNECTED =
        "com.example.bluetooth.le.ACTION_WIFI_CONNECTED"
const val ACTION_WIFI_UNAVAILABLE = "com.example.bluetooth.le.ACTION_WIFI_UNAVAILABLE"

const val SECURITY_WEP = 1
const val SECURITY_WPA = 2 //use ScanResult capabilities see all WAP and WAP2 are PSK(NWI IoT-KW: [WPA2-PSK-CCMP][RSN-PSK-CCMP][ESS])
const val SECURITY_EAP = 3 //use ScanResult capabilities see wifi need name and password are EAP(NeuronicWorks-KW: [WPA2-EAP-CCMP][RSN-EAP-CCMP][ESS])
const val SECURITY_NONE = 0

const val SIGNAL_STRENGTH_POOR = 0
const val SIGNAL_STRENGTH_LOW = 1
const val SIGNAL_STRENGTH_MEDIUM= 2
const val SIGNAL_STRENGTH_STRONG = 3
