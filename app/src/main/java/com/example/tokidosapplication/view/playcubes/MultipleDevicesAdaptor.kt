package com.example.tokidosapplication.view.playcubes

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.net.wifi.ScanResult
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tokidosapplication.R

class MultipleDevicesAdaptor(private val dataSet: ArrayList<BluetoothDevice>, lis: MyItemClickListener): RecyclerView.Adapter<MultipleDevicesAdaptor.ViewHolder>()  {

    private  var listener: MyItemClickListener = lis

    class ViewHolder(view: View, dataSet: ArrayList<BluetoothDevice>, clickListener: MyItemClickListener) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.textViewPlayCubeName)
            view.setOnClickListener {
                clickListener.onItemClicked(dataSet, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cubes_row_item, parent, false)


        return ViewHolder(view, dataSet, listener)
    }



    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = dataSet[position].name
    }
    override fun getItemCount(): Int {
        return dataSet.size

    }
    interface MyItemClickListener{
        fun onItemClicked(data: ArrayList<BluetoothDevice>, position: Int)
    }
}