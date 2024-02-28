package com.example.tokidosapplication.view.playcubes

import android.bluetooth.BluetoothGattCharacteristic
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayCubesConnectBaseViewModel:ViewModel() {

    private val _characteristicsMap = MutableLiveData<HashMap<String, BluetoothGattCharacteristic>>()
    val characteristicsMap: LiveData<HashMap<String, BluetoothGattCharacteristic>> = _characteristicsMap
    init {
        _characteristicsMap.value = HashMap()
    }
    fun addItem(key: String, value: BluetoothGattCharacteristic) {
        _characteristicsMap.value?.put(key, value)
        _characteristicsMap.postValue(_characteristicsMap.value)
    }

    fun removeItem(key: String) {
        _characteristicsMap.value?.remove(key)
        _characteristicsMap.postValue(_characteristicsMap.value)
    }
}