package com.example.tokidosapplication.view.playcubes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tokidosapplication.R

class PlayCubesAdaptor(private val dataSet: Array<String>, lis: MyItemClickListener):RecyclerView.Adapter<PlayCubesAdaptor.ViewHolder>() {

    private  var listener: MyItemClickListener = lis

    class ViewHolder(view: View, dataSet: Array<String>, clickListener: MyItemClickListener) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.textView14)
            view.setOnClickListener {
                clickListener.onItemClicked(dataSet, adapterPosition)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder  {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.play_cubes_row_item, parent, false)
        println("asasa 1 $view")

        return ViewHolder(view, dataSet, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println("asasa $position, ${dataSet[position]}")
        holder.textView.text = dataSet[position]

    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
    interface MyItemClickListener{
        fun onItemClicked(data: Array<String>, position: Int)
    }
}