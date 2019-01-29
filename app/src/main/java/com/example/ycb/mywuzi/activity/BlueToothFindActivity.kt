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

    private var mScanning = true

    //作为客户端的Socket
    private var clientSocket: BluetoothSocket? = null
    //蓝牙ServerSocket
    internal var bluetoothServerSocket: BluetoothServerSocket? = null
    //客户端连接后服务端的Socket
    internal var serverSocket: BluetoothSocket? = null

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

    private fun initServer() {
        //开启子线程等待连接
        Thread(Runnable {
            try {
                //开启服务端
                //等待客户端接入
                while (true) {
                    bluetoothServerSocket =
                            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(bluetoothAdapter.getName(), Config.UUID)
                    serverSocket = bluetoothServerSocket?.accept()
                    if (serverSocket?.isConnected()!!) {


                        runOnUiThread {
                            Toast.makeText(this@BlueToothFindActivity, "接收挑战请求，建立连接成功！", Toast.LENGTH_SHORT)
                            //执行socket方法

//                            val blueToothGameAty = BlueToothGameAty()
//                            blueToothGameAty.blueToothGameAty.manageConnectedSocket(fuwuSocket, false)
                            //       blueToothGameAty.blueToothGameAty.chushihua(blueToothGameAty);
                        }


                        //跳转到蓝牙游戏activity
//                        val i = Intent(this@BlueToothFindActivity, BlueToothGameAty::class.java)
//                        startActivity(i)
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
     * 打开蓝牙设备，设置可见性。
     */
    private fun openBlueTooth() {
        //创建一个Intent对象,并且将其action的值设置为BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE也就是蓝牙设备设置为可见状态
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        //将一个键值对存放到Intent对象当中,主要用于指定可见状态的持续时间,大于300秒,就认为是300秒
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        //执行请求
        startActivityForResult(intent,Const.REQUEST_BLUETOOTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_BLUETOOTH) {
            scanLeDevice(true);
        }
    }

    fun scanLeDevice(enable : Boolean){
        if (enable) {
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
//                var bluetoothLeScanner:BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
//                bluetoothLeScanner .startScan(object : ScanCallback() {
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

            //设置广播获得未配对可检测的蓝牙设备
            //创建一个IntentFilter对象,将其action指定为BluetoothDevice.ACTION_FOUND
            //IntentFilter它是一个过滤器,只有符合过滤器的Intent才会被我们的BluetoothReceiver所接收
            val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            //创建一个BluetoothReceiver对象
            var bluetoothReceiver = BluetoothReceiver()
            //设置广播的优先级为最大
            intentFilter.priority = Integer.MAX_VALUE
            //注册广播接收器 注册完后每次发送广播后，BluetoothReceiver就可以接收到这个广播了
            registerReceiver(bluetoothReceiver, intentFilter)
            //开始扫描周围的可见的蓝牙设备
            bluetoothAdapter.startDiscovery()
        } else {

        }
    }

    //接收广播
    /**
     * 接受广播，并显示尚未配对的可用的周围所有蓝牙设备
     */
    private inner class BluetoothReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            //如果是正在扫描状态
            if (BluetoothDevice.ACTION_FOUND == action) {
                //只要BluetoothReceiver接收到来自于系统的广播,这个广播是什么呢,是我找到了一个远程蓝牙设备
                //Intent代表刚刚发现远程蓝牙设备适配器的对象,可以从收到的Intent对象取出一些信息
                val bluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // 如果该设备已经被配对，则跳过
                //  if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                if (!devices.contains(bluetoothDevice)) {
                    //设备数组获得新的设备信息并更新adapter
                    addressDevices.add(
                        BluetoothDeviceModel(
                            bluetoothDevice.name,
                            bluetoothDevice.address,
                            bluetoothDevice.bondState
                        )
                    )
                    //添加新的设备到设备Arraylist
                    devices.add(bluetoothDevice)
                    adapter.notifyDataSetChanged()
                    LogUtil.LogMsg(this@BlueToothFindActivity.javaClass,"BluetoothReceiver =" +bluetoothDevice.address)
                }

            }
        }
    }

    private fun buildConnect(position: Int, isClient: Boolean) {
        //自己主动去连接
        val device = bluetoothAdapter.getRemoteDevice(addressDevices.get(position).deviceAddress)
        var result: Boolean? = false
        try {
            //先进行配对
            //如果没有配对
            LogUtil.LogMsg(this@BlueToothFindActivity.javaClass, "开始配对")
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                var createBondMethod: Method? = null
                createBondMethod = BluetoothDevice::class.java
                    .getMethod("createBond")

                LogUtil.LogMsg(this@BlueToothFindActivity.javaClass, "开始配对")
                result = createBondMethod!!.invoke(device) as Boolean
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
                                        Toast.makeText(this@BlueToothFindActivity, "连接成功！！", Toast.LENGTH_SHORT).show()
                                        //执行socket方法
                                        val blueToothGameAty = CheckerActivity()

//                                        blueToothGameAty.blueToothGameAty.manageConnectedSocket(clientSocket, true)
                                        //   blueToothGameAty.blueToothGameAty.chushihua(blueToothGameAty);
                                    }
                                    //跳转到蓝牙游戏activity
//                                    val i = Intent(this@BlueToothFindOthersAty, BlueToothGameAty::class.java)
//                                    startActivity(i)
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