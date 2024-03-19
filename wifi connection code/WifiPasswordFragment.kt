package com.example.tokidosapplication.view.playcubes

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.Navigation
import com.example.tokidosapplication.R
import com.example.tokidosapplication.databinding.FragmentWifiPasswordBinding
import com.example.tokidosapplication.view.data.Wifi


class WifiPasswordFragment(private val isOther: Boolean, private val wifi: Wifi?, private val listener: WifiPasswordDialogFragmentListener? = null) : DialogFragment() {
    private lateinit var binding: FragmentWifiPasswordBinding
    var ssid: Editable? = null
    var password: Editable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE);
        binding = FragmentWifiPasswordBinding.inflate(layoutInflater, container, false)
        binding.enterWifiLayout.visibility = View.VISIBLE
        //binding.connectingWifiLayout.visibility = View.GONE
        if(isOther){
            binding.textView17.text = "Enter Other Network"

            binding.textView18.visibility = View.GONE
            binding.editTextNetworkName.visibility = View.VISIBLE

        }else{
            binding.textView17.text = "Enter the password for:"
            binding.textView18.text = getWifiName()
            binding.textView18.visibility = View.VISIBLE
            binding.editTextNetworkName.visibility = View.GONE
        }

        binding.imageViewCross.setOnClickListener {
            dismiss()
        }
        binding.editTextPassword.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                println("p0:${p0.toString()} ${p0.toString().length}}")
                if(p0?.toString()?.length != 0) {
                    binding.buttonConnectToWifi.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.orange_button_background_shape
                    )
                    binding.buttonConnectToWifi.isEnabled = true
                }
                else {
                    binding.buttonConnectToWifi.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.orange_light_button_background_shape
                    )
                    binding.buttonConnectToWifi.isEnabled = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
                password = p0
            }
        })
        binding.editTextNetworkName.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                ssid = p0
            }
        })

        binding.passwordSection.setEndIconOnClickListener {
            if (binding.editTextPassword.transformationMethod == null){
                binding.editTextPassword.transformationMethod = PasswordTransformationMethod()
            }else{
                binding.editTextPassword.transformationMethod = null
            }
        }
        binding.buttonConnectToWifi.setOnClickListener{
            println("ppp $wifi")
            val wifi = if(isOther) wifi?.copy(ssid = ssid.toString()) else wifi

            listener?.connectWifi(wifi, password.toString())
            dismiss()
            //binding.enterWifiLayout.visibility = View.GONE
            //binding.connectingWifiLayout.visibility = View.VISIBLE

        }
//        val animatorSet1 = createAnimatorSet(binding.imageViewThreeDot1)
//        val animatorSet2 = createAnimatorSet(binding.imageViewThreeDot2)
//        val animatorSet3 = createAnimatorSet(binding.imageViewThreeDot3)
//        // Start the animator sets
//        animatorSet1.start()
//        animatorSet2.start()
//        animatorSet3.start()
//        AnimationUtils.loadAnimation(activity?.applicationContext, R.anim.bottom_top).also { moveAnimation ->
//
//            binding.imageViewThreeDot1.startAnimation(moveAnimation)
//        }
//        AnimationUtils.loadAnimation(activity?.applicationContext, R.anim.dot_move).also { moveAnimation ->
//
//            binding.imageViewThreeDot2.startAnimation(moveAnimation)
//        }
//        AnimationUtils.loadAnimation(activity?.applicationContext, R.anim.top_bottom).also { moveAnimation ->
//
//            binding.imageViewThreeDot3.startAnimation(moveAnimation)
//        }
        //binding.imageViewThreeDot.animation = AnimationUtils.loadAnimation(activity?.applicationContext, R.anim.dot_move)
//        val mAnimation = TranslateAnimation(
//            TranslateAnimation.ABSOLUTE, 0f,
//            TranslateAnimation.ABSOLUTE, 0f,
//            TranslateAnimation.RELATIVE_TO_PARENT, 0f,
//            TranslateAnimation.RELATIVE_TO_PARENT, 0.5f,
//
//        )
//        mAnimation.setDuration(2000)
//        mAnimation.setRepeatCount(-1)
//        mAnimation.setRepeatMode(Animation.REVERSE)
//        mAnimation.setInterpolator(LinearInterpolator())
//        binding.imageViewThreeDot1.animation = mAnimation
        return binding.root
    }
//    private fun createAnimatorSet(view: ImageView): AnimatorSet {
//        val animatorSet = AnimatorSet()
//
//        val animator1 = ValueAnimator.ofFloat(0f, -100f)
//        animator1.duration = 1000
//        animator1.interpolator = AccelerateDecelerateInterpolator()
//        animator1.addUpdateListener {
//            view.translationY = it.animatedValue as Float
//        }
//
//        val animator2 = ValueAnimator.ofFloat(-100f, 0f)
//        animator2.duration = 1000
//        animator2.interpolator = AccelerateDecelerateInterpolator()
//        animator2.addUpdateListener {
//            view.translationY = it.animatedValue as Float
//        }
//
//        animatorSet.playSequentially(animator1, animator2)
//        animatorSet.repeatMode = ValueAnimator.RESTART
//        animatorSet.repeatCount = ValueAnimator.INFINITE
//        return animatorSet
//    }
//    private fun animation(){
//        val mValueAnimator = ValueAnimator()
//
//        mValueAnimator.setIntValues(0, 360);
//        mValueAnimator.setDuration(1000)
//        mValueAnimator.interpolator = DecelerateInterpolator()
//        mValueAnimator.addUpdateListener { animation ->
//            val angle = animation.animatedValue.toString().toInt()
//            rotateDegree = (rotateDegree + 45) % 360
//            invalidate()
//        }
//        if (mValueAnimator.isRunning) {
//            mValueAnimator.end();
//        }
//        mValueAnimator.start();
//    }
    fun getWifiName(): String?{
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
//            return wifi?.SSID
//        }else{
//           return wifi?.wifiSsid.toString()
//        }
        return wifi?.ssid
    }
    interface WifiPasswordDialogFragmentListener{
        fun connectWifi(wifi: Wifi?, password:String)
    }
}