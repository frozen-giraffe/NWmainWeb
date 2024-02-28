package com.example.tokidosapplication.view

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.example.tokidosapplication.BluetoothLeService

class BLEhelper private constructor(){
    private var bluetoothService : BluetoothLeService? = null

    val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            println("Service Connected")
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            println("Service Connected $bluetoothService")

        }

        override fun onBindingDied(name: ComponentName?) {
            super.onBindingDied(name)
            println("Service onBindingDied")

        }

        override fun onNullBinding(name: ComponentName?) {
            super.onNullBinding(name)
            println("Service onNullBinding")

        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
            println("Service Disconnected")

        }
    }
    companion object {
        @Volatile
        private var instance: BLEhelper? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: BLEhelper().also { instance = it }
            }
    }
    fun getBluetoothLeService():BluetoothLeService?{
        return bluetoothService
    }
}