package com.example.ycb.mywuzi.adapter

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.ycb.mywuzi.R
import com.example.ycb.mywuzi.model.BluetoothDeviceModel

/**
 * Created by biao on 2019/1/29.
 */
class BluetoothCellAdapter(var context: Context,var list: MutableList<BluetoothDeviceModel>?): RecyclerView.Adapter<BluetoothCellAdapter.BluetoothViewHolder>() {
    private var itemclickListener: OnItemClickListener? = null
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): BluetoothViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.bluetooth_adapter,p0,false)
        var bluetoothViewHolder = BluetoothViewHolder(view)
        return bluetoothViewHolder
    }

    override fun getItemCount(): Int {
        return list?.size?:0
    }

    override fun onBindViewHolder(p0: BluetoothViewHolder, p1: Int) {
        p0.run {
            list?.get(p1)?.run {
                name.text = deviceName
                address.text = deviceAddress
                num.text = p1.plus(1).toString()
                itemView.setOnClickListener {
                    if (itemclickListener != null) {
                        itemclickListener?.onItemClick(p1, this)}
                }
                if(deviceState == BluetoothDevice.BOND_NONE)
                    state.text = "未配对"
                else if (deviceState == BluetoothDevice.BOND_BONDED)
                    state.text = "已配对"
                else if(deviceState == BluetoothDevice.BOND_BONDING)
                    state.text = "正在配对"

            }
        }
    }


    class BluetoothViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var view = itemView
        var name = itemView.findViewById<TextView>(R.id.tv_bluetooth_name)
        var address = itemView.findViewById<TextView>(R.id.tv_bluetooth_address)
        var num = itemView.findViewById<TextView>(R.id.tv_bluetooth_num)
        var state = itemView.findViewById<TextView>(R.id.tv_bluetooth_state)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, data: BluetoothDeviceModel)
    }

    fun setItemclickListener(itemclickListener: OnItemClickListener) {
        this.itemclickListener = itemclickListener
    }
}