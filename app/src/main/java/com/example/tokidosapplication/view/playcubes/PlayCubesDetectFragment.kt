package com.example.tokidosapplication.view.playcubes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tokidosapplication.R
import com.example.tokidosapplication.databinding.FragmentPlayCubesDetectBinding
import com.example.tokidosapplication.databinding.FragmentPlaycubesCheckBinding

class PlayCubesDetectFragment : Fragment() {
    private lateinit var binding: FragmentPlayCubesDetectBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlayCubesDetectBinding.inflate(layoutInflater, container, false)

        return binding.root
    }


}