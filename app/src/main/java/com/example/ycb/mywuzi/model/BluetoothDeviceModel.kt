package com.example.ycb.mywuzi.model

/**
 * Created by biao on 2019/1/29.
 */
class BluetoothDeviceModel(name: String?, address: String?, bondState: Int) {
    var deviceName = name?:""
    var deviceAddress = address?:""
    var deviceState = bondState

}