package com.example.tokidosapplication.view.playcubes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.tokidosapplication.R
import com.example.tokidosapplication.databinding.FragmentSettingBinding


class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(layoutInflater, container, false)

        // remove current fragment action bar
        super.onCreate(savedInstanceState)
        //(requireActivity() as AppCompatActivity).supportActionBar?.hide()
        //(activity as AppCompatActivity?)!!.supportActionBar!!.hide()
//        val callback: OnBackPressedCallback =
//            object : OnBackPressedCallback(true /* enabled by default */) {
//                override fun handleOnBackPressed() {
//                    Toast.makeText(context,"sd", Toast.LENGTH_LONG).show()
//                }
//            }
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        return binding.root
    }



}