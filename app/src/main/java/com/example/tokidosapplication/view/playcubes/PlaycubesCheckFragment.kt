package com.example.tokidosapplication.view.playcubes

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.tokidosapplication.BleScanUtils
import com.example.tokidosapplication.R
import com.example.tokidosapplication.databinding.FragmentPlaycubesCheckBinding

class PlaycubesCheckFragment : Fragment() {
    private var isCheck1 = false
    private var isCheck2 = false
    private var isCheck3 = false
    private var isCheck4 = false
    private var isCheck5 = false
    private lateinit var binding:FragmentPlaycubesCheckBinding
    @SuppressLint("MissingPermission")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlaycubesCheckBinding.inflate(layoutInflater, container, false)
        binding.button.setOnClickListener {
            view?.let { it1 ->
                if(isCheck1 && isCheck2 && isCheck3 && isCheck4 && isCheck5)
                    Navigation.findNavController(it1).navigate(R.id.action_playcubesCheckFragment2_to_detectingFragment)
            }

        }
        binding.checkBox.setOnCheckedChangeListener{buttonView, isChecked ->
            isCheck1 = isChecked
            allChecked()
        }
        binding.checkBox2.setOnCheckedChangeListener{buttonView, isChecked ->
            isCheck2 = isChecked
            allChecked()
        }
        binding.checkBox3.setOnCheckedChangeListener{buttonView, isChecked ->
            isCheck3 = isChecked
            allChecked()
        }
        binding.checkBox4.setOnCheckedChangeListener{buttonView, isChecked ->
            isCheck4 = isChecked
            allChecked()
        }
        binding.checkBox5.setOnCheckedChangeListener{buttonView, isChecked ->
            isCheck5 = isChecked
            allChecked()
        }
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return binding.root
    }
    private fun allChecked(){
        println("$isCheck1 , $isCheck2 , $isCheck3 , $isCheck4 , $isCheck5")
        if (isCheck1 && isCheck2 && isCheck3 && isCheck4 && isCheck5){
            binding.button.background =  ContextCompat.getDrawable(requireContext(), R.drawable.orange_button_background_shape)
        }else{
            binding.button.background =  ContextCompat.getDrawable(requireContext(), R.drawable.orange_light_button_background_shape)

        }
    }
}