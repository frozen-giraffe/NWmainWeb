package com.example.tokidosapplication.view.playcubes

import android.net.wifi.ScanResult
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tokidosapplication.R
import com.example.tokidosapplication.SECURITY_NONE
import com.example.tokidosapplication.SIGNAL_STRENGTH_LOW
import com.example.tokidosapplication.SIGNAL_STRENGTH_MEDIUM
import com.example.tokidosapplication.SIGNAL_STRENGTH_STRONG
import com.example.tokidosapplication.view.data.Wifi

class WifiAdaptor(private val dataSet: ArrayList<Wifi>, lis: MyItemClickListener): RecyclerView.Adapter<WifiAdaptor.ViewHolder>() {

    private  var listener: MyItemClickListener = lis

    class ViewHolder(view: View, dataSet: ArrayList<Wifi>, clickListener: MyItemClickListener) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val imageView: ImageView
        val passwordRequiredImage: ImageView

        init {
            imageView = view.findViewById(R.id.wifiSignalImageView)
            passwordRequiredImage = view.findViewById(R.id.passwordRequiredImage)
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.textViewWifiName)
            view.setOnClickListener {
                clickListener.onItemClicked(dataSet, adapterPosition)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder  {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.wifi_row_item, parent, false)


        return ViewHolder(view, dataSet, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        println("onBindViewHolder:  ${dataSet[position]}")
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
//            holder.textView.text = dataSet[position].SSID
//        }else{
//            holder.textView.text = dataSet[position].wifiSsid.toString()
//        }
        holder.imageView.visibility = if(dataSet[position].ssid == "Other...") View.INVISIBLE else View.VISIBLE
        holder.passwordRequiredImage.visibility = if(dataSet[position].securityType == SECURITY_NONE || dataSet[position].ssid == "Other...") View.INVISIBLE else View.VISIBLE

        holder.textView.text = dataSet[position].ssid
        when(calculateSignalLevel(dataSet[position].rssi)){
            SIGNAL_STRENGTH_STRONG -> holder.imageView.setImageResource(R.drawable.signal_wifi_strong)
            SIGNAL_STRENGTH_MEDIUM -> holder.imageView.setImageResource(R.drawable.signal_wifi_low_medium)
            SIGNAL_STRENGTH_LOW -> holder.imageView.setImageResource(R.drawable.signal_wifi_low)

        }
    }
    private fun calculateSignalLevel(rssi: Int): Int{
        if (rssi > -50){
            return SIGNAL_STRENGTH_STRONG
        }else if(rssi >=-70){
            return SIGNAL_STRENGTH_MEDIUM
        }else{
            return SIGNAL_STRENGTH_LOW
        }
    }
    override fun getItemCount(): Int {
        return dataSet.size
    }
    interface MyItemClickListener{
        fun onItemClicked(data: ArrayList<Wifi>, position: Int)
    }
}