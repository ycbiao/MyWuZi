package com.example.ycb.mywuzi.activity

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.*
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.example.ycb.mywuzi.R
import com.example.ycb.mywuzi.adapter.BluetoothCellAdapter
import com.example.ycb.mywuzi.base.BaseActivity
import com.example.ycb.mywuzi.model.BluetoothDeviceModel
import com.example.ycb.mywuzi.util.Config
import com.example.ycb.mywuzi.util.Const
import com.example.ycb.mywuzi.util.Const.Companion.REQUEST_BLUETOOTH
import com.example.ycb.mywuzi.widget.LogUtil
import kotlinx.android.synthetic.main.bluetooth_activity.*
import java.io.IOException
import java.lang.reflect.Method

/**
 * Created by biao on 2019/1/28.
 */
class BlueToothFindActivity : BaseActivity()  {

    private var addressDevices : MutableList<BluetoothDeviceModel> = arrayListOf()

    private var devices : MutableList<BluetoothDevice> = arrayListOf()

    private lateinit var adapter: BluetoothCellAdapter

    //蓝牙组件
    private  lateinit var bluetoothAdapter: BluetoothAdapter

    private var bluetoothReceiver = BluetoothReceiver()

    //作为客户端的Socket
    private var clientSocket: BluetoothSocket? = null
    //蓝牙ServerSocket
    internal var bluetoothServerSocket: BluetoothServerSocket? = null
    //客户端连接后服务端的Socket
    internal var serverSocketAfter: BluetoothSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bluetooth_activity)
        adapter = BluetoothCellAdapter(this,addressDevices)
        rv_blueTooth.layoutManager = LinearLayoutManager(this)
        rv_blueTooth.adapter = adapter
        //得到本机蓝牙设备的adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled()) {
            //设置手机蓝牙可见性
            openBlueTooth()
        }else{
            scanLeDevice(true)
        }
        adapter.setItemclickListener(object : BluetoothCellAdapter.OnItemClickListener{
            override fun onItemClick(position: Int, data: BluetoothDeviceModel) {
                buildConnect(position,true)
            }
        })

        initServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
    }

    private fun initServer() {
        //开启子线程等待连接
        Thread(Runnable {
            try {
                //开启服务端
                //等待客户端接入
                while (true) {
                    bluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(bluetoothAdapter.getName(), Config.UUID)
                    serverSocketAfter = bluetoothServerSocket?.accept()
                    if (serverSocketAfter?.isConnected()!!) {

                        runOnUiThread {
                            showToast("接收挑战请求，建立连接成功！")

                            CheckerActivity.manageConnectedSocket(serverSocketAfter!!, false)
                        }

                        val i = Intent(this@BlueToothFindActivity, CheckerActivity::class.java)
                        i.putExtra(Const.MODEL_TYPE,Const.MODEL_TYPE_BLUE)
                        startActivity(i)

                        //初始化线程来传输数据
                        // manageConnectedSocket(fuwuSocket);
                        //得到连接之后关闭ServerSocket
                        // bluetoothServerSocket.close();
                        //打断线程
                        //   Thread.interrupted();
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("whalea", "没读到的原因！：" + e.message)
            }
        }).start()
    }


    /**
     * 打开蓝牙设备。
     */
    private fun openBlueTooth() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        //指定可见状态的持续时间,大于300秒,就认为是300秒
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 500)
        startActivityForResult(intent,Const.REQUEST_BLUETOOTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //resultCode返回设置的BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,这里是500,不允许则返回0
        if (resultCode == 500 && requestCode == REQUEST_BLUETOOTH) {
            scanLeDevice(true);
        }
    }

    fun scanLeDevice(enable : Boolean){
        if (enable) {
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
//                var bluetoothLeScanner:BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
//                bluetoothLeScanner .startScan(object : ScanCallback() {//扫描只能手环之类
//                    override fun onScanFailed(errorCode: Int) {
//                        super.onScanFailed(errorCode)
//                        LogUtil.LogMsg(this@BlueToothFindActivity.javaClass,"onScanFailed =" + errorCode.toString())
//                    }
//
//                    override fun onScanResult(callbackType: Int, result: ScanResult?) {
//                        super.onScanResult(callbackType, result)
//                        LogUtil.LogMsg(this@BlueToothFindActivity.javaClass,"onScanResult =" + result?.scanRecord.toString())
//                        var  bluetoothDeviceModel = BluetoothDeviceModel().also {
//                            it.deviceAddress = result?.device.toString()
//                            it.deviceName = result?.scanRecord?.deviceName ?: "null"
//                        }
//                        devices.add(bluetoothDeviceModel)
//                        adapter.list = devices
//                        adapter.notifyDataSetChanged()
//                    }
//
//                    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
//                        super.onBatchScanResults(results)
//                        LogUtil.LogMsg(this@BlueToothFindActivity.javaClass,"onBatchScanResults =" +results.toString())
//                    }
//                })
//            }

            val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            intentFilter.priority = Integer.MAX_VALUE
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //监听搜索完毕
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);               //监听是否配对
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);     //监听配对的状态
            registerReceiver(bluetoothReceiver, intentFilter)
            //开始扫描周围的可见的蓝牙设备
            bluetoothAdapter.startDiscovery()
            tv_bluetooth_hint.text = "正在扫描蓝牙设备..."
        }
    }

    //接收广播
    /**
     * 接受广播，并显示尚未配对的可用的周围所有蓝牙设备
     */
    private inner class BluetoothReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val bluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            when (action) {
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED ->{
                    tv_bluetooth_hint.text = "停止扫描蓝牙设备"
                }
                BluetoothDevice.ACTION_FOUND ->{
                    if (!devices.contains(bluetoothDevice)) {
                        //设备数组获得新的设备信息并更新adapter
                        if(bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED || (bluetoothDevice.name != null && !bluetoothDevice.name.isEmpty())){
                            addressDevices.add(0,
                                BluetoothDeviceModel(
                                    bluetoothDevice.name,
                                    bluetoothDevice.address,
                                    bluetoothDevice.bondState
                                )
                            )
                        }else{
                            addressDevices.add(
                                BluetoothDeviceModel(
                                    bluetoothDevice.name,
                                    bluetoothDevice.address,
                                    bluetoothDevice.bondState
                                )
                            )
                        }
                        //添加新的设备到设备Arraylist
                        devices.add(bluetoothDevice)
                        LogUtil.LogMsg(this@BlueToothFindActivity.javaClass,"BluetoothReceiver =" +bluetoothDevice.address)
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED->{
                    if(devices.contains(bluetoothDevice)){
                        addressDevices.forEach {
                            if(it.deviceAddress.equals(bluetoothDevice.address)){
                                it.deviceState = bluetoothDevice.bondState
                            }
                        }
                        LogUtil.LogMsg(this@BlueToothFindActivity.javaClass,"bondState =" +bluetoothDevice.bondState)
                    }
                }
            }
            adapter.notifyDataSetChanged()
        }

    }

    private fun buildConnect(position: Int, isClient: Boolean) {
        //自己主动去连接
        val device = bluetoothAdapter.getRemoteDevice(addressDevices.get(position).deviceAddress)
        var result: Boolean? = false
        try {
            //先进行配对
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                LogUtil.LogMsg(this@BlueToothFindActivity.javaClass, "开始配对")
//                device.setPin(byteArrayOf("0000".toByte()));
                LogUtil.LogMsg(this@BlueToothFindActivity.javaClass, "配对 = ${device.createBond()}")

            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                //获得客户端Socket
                clientSocket = device.createRfcommSocketToServiceRecord(Config.UUID)
                val aDialog = AlertDialog.Builder(this@BlueToothFindActivity).setTitle("发起对战")
                    .setMessage("确认挑战玩家：" + addressDevices.get(position).deviceName + "吗？")
                    .setNegativeButton("确定") { dialog, which ->
                        Thread(Runnable {
                            //先停止扫描，以防止之后的连接被阻塞
                            bluetoothAdapter.cancelDiscovery()
                            try {
                                //开始连接，发送连接请求
                                clientSocket?.connect()
                                if (!bluetoothAdapter.isEnabled) {
                                    bluetoothAdapter.enable()
                                }
                                if (clientSocket?.isConnected()!!) {
                                    runOnUiThread {
                                        showToast("连接成功！！")
                                        //执行socket方法
                                        val checkerActivity = CheckerActivity()

                                        CheckerActivity.manageConnectedSocket(clientSocket!!, true)
                                        //   blueToothGameAty.blueToothGameAty.chushihua(blueToothGameAty);
                                    }
                                    //跳转到蓝牙游戏activity
                                    val i = Intent(this@BlueToothFindActivity, CheckerActivity::class.java)
                                    i.putExtra(Const.MODEL_TYPE,Const.MODEL_TYPE_BLUE)
                                    startActivity(i)
                                }
                            } catch (e: IOException) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@BlueToothFindActivity,
                                        "连接失败！！" + e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                /*   try {
                                               clientSocket.close();
                                           } catch (IOException e1) {
                                           }
                                           return;*/
                            }

                            // manageConnectedSocket(clientSocket);
                            //之后关闭socket，清除内部资源
                            /*      try {
                                        clientSocket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }*/
                        }).start()
                    }
                    .setPositiveButton("取消", null).show()
            }//如果已经配对好了
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}