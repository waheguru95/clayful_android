package com.health.clayful.ui.newPassword

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewbinding.ViewBinding
import com.health.clayful.R
import com.health.clayful.base.BaseActivity
import com.health.clayful.databinding.ActivityNewPasswordBinding
import com.health.clayful.ui.passwordResetSuccess.ResetPasswordSuccessActivity
import com.health.clayful.utils.Constants
import com.health.clayful.utils.NewPasswordViewModelFactory
import com.health.clayful.utils.startIntentActivity
import com.health.clayful.viewModel.NewPasswordViewModel

class NewPasswordActivity<T> : BaseActivity<ActivityNewPasswordBinding>(), TextWatcher, View.OnClickListener {

    private var viewModel : NewPasswordViewModel ?= null
    private var isPasswordHintVisible = false

    override fun createBinding(): ActivityNewPasswordBinding {
        return ActivityNewPasswordBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDialogView()
        setViewListeners()
        initiateViewModel()
        viewModelObserver()
        initiateShakeAnimation()
        changeTopBarColor(resources.getColor(R.color.white))
        changeStatusBarIconColorToBlack(binding.root)
    }

    private fun initiateViewModel() {
        viewModel = ViewModelProviders.of(this, NewPasswordViewModelFactory(this as NewPasswordActivity<ViewBinding>)).get(NewPasswordViewModel::class.java)
        binding.model = viewModel
    }

    private fun viewModelObserver() {

        viewModel?.isProgressShowing?.observe(this, Observer {
            if(it) {
                showLoader()
            }
            else {
                hideLoader()
            }
        })

        viewModel?.resetPasswordResponse?.observe(this, Observer {
            if(it != null) {
                startIntentActivity<ResetPasswordSuccessActivity<Any?>>(false)
                viewModel?.resetPasswordResponse?.value = null
            }
        })

        viewModel?.fieldError?.observe(this, Observer { errorType ->
            if(errorType != null) {
                when (errorType) {
                    Constants.OTP_FIELD_EMPTY -> {
                        binding.oneTimeCodeEmpty.showView()
                        binding.oneTimeCodeEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.oneTimeCodeEt.startAnimation(shake)
                    }
                    Constants.NEW_PASSWORD_EMPTY -> {
                        binding.newPasswordEmpty.showView()
                        binding.newPasswordEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.newPasswordEt.startAnimation(shake)
                    }

                    Constants.CONFIRM_PASSWORD_EMPTY -> {
                        binding.confirmPasswordEmpty.showView()
                        binding.confirmPasswordEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.confirmPasswordEt.startAnimation(shake)
                    }

                    Constants.PASSWORD_LENGTH_ERROR -> {
                        binding.newPasswordEmpty.text = resources.getString(R.string.password_length_error)
                        binding.newPasswordEmpty.showView()
                        binding.newPasswordEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.newPasswordEt.startAnimation(shake)
                    }

                    Constants.CONFIRM_PASSWORD_MISMATCH -> {
                        binding.confirmPasswordEmpty.text = resources.getString(R.string.password_missmatch)
                        binding.confirmPasswordEmpty.showView()
                        binding.confirmPasswordEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.confirmPasswordEt.startAnimation(shake)
                    }

                    Constants.ALL_FIELDS_ARE_EMPTY -> {
                        binding.oneTimeCodeEmpty.showView()
                        binding.newPasswordEmpty.showView()
                        binding.confirmPasswordEmpty.showView()

                        binding.oneTimeCodeEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.newPasswordEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.confirmPasswordEt.background = resources.getDrawable(R.drawable.field_error_bg)

                        binding.oneTimeCodeEt.startAnimation(shake)
                        binding.newPasswordEt.startAnimation(shake)
                        binding.confirmPasswordEt.startAnimation(shake)
                    }
                }
                viewModel?.fieldError?.value = null
            }
        })
    }

    private fun setViewListeners() {
        binding.oneTimeCodeEt.onFocusChangeListener = MyFocusChangeListener(binding.oneTimeCodeEt)
        binding.newPasswordEt.onFocusChangeListener = MyFocusChangeListener(binding.newPasswordEt)
        binding.confirmPasswordEt.onFocusChangeListener = MyFocusChangeListener(binding.confirmPasswordEt)

        binding.oneTimeCodeEt.addTextChangedListener(this)
        binding.newPasswordEt.addTextChangedListener(this)
        binding.confirmPasswordEt.addTextChangedListener(this)

        binding.PasswordHint.setOnClickListener(this)

    }

    inner class MyFocusChangeListener(private val editText: AppCompatEditText): View.OnFocusChangeListener {
        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (editText.id == binding.oneTimeCodeEt.id && hasFocus) {
                if(!binding.oneTimeCodeEmpty.isVisible) {
                    binding.oneTimeCodeEt.background = resources.getDrawable(R.drawable.edit_text_on_focus_bg)
                }
                if( !binding.newPasswordEmpty.isVisible) {
                    binding.newPasswordEt.background = resources.getDrawable(R.drawable.edit_text_bg)
                }
                if( !binding.confirmPasswordEmpty.isVisible) {
                    binding.confirmPasswordEt.background = resources.getDrawable(R.drawable.edit_text_bg)
                }
            } else if (editText.id == binding.newPasswordEt.id && hasFocus) {

                if(!binding.newPasswordEmpty.isVisible) {
                    binding.newPasswordEt.background = resources.getDrawable(R.drawable.edit_text_on_focus_bg)
                }
                if( !binding.oneTimeCodeEmpty.isVisible) {
                    binding.oneTimeCodeEt.background = resources.getDrawable(R.drawable.edit_text_bg)
                }
                if( !binding.confirmPasswordEmpty.isVisible) {
                    binding.confirmPasswordEt.background = resources.getDrawable(R.drawable.edit_text_bg)
                }

            } else if (editText.id == binding.confirmPasswordEt.id && hasFocus) {

                Handler().postDelayed({
                    binding.scrollView.smoothScrollTo(0,2000)
                },400)

                if(!binding.confirmPasswordEmpty.isVisible) {
                    binding.confirmPasswordEt.background = resources.getDrawable(R.drawable.edit_text_on_focus_bg)
                }
                if( !binding.newPasswordEmpty.isVisible) {
                    binding.newPasswordEt.background = resources.getDrawable(R.drawable.edit_text_bg)
                }
                if( !binding.oneTimeCodeEmpty.isVisible) {
                    binding.oneTimeCodeEt.background = resources.getDrawable(R.drawable.edit_text_bg)
                }
            }
        }
    }

    fun fadeInAnimation(viewToFadeIn: View) {
        val fadeIn = ObjectAnimator.ofFloat(viewToFadeIn, "alpha", 0f, 1f)
        fadeIn.duration = 600
        fadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)

                viewToFadeIn.visibility = View.VISIBLE
                isPasswordHintVisible = true
            }
        })
        fadeIn.start()
    }

    fun fadeOutAnimation(viewToFadeOut: View) {
        val fadeOut = ObjectAnimator.ofFloat(viewToFadeOut, "alpha", 1f, 0f)
        fadeOut.duration = 600
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                viewToFadeOut.visibility = View.GONE
                isPasswordHintVisible = false
            }
        })
        fadeOut.start()
    }


    override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(p0: Editable) {
        if(binding.oneTimeCodeEt.hasFocus()) {
            binding.oneTimeCodeEt.background = resources.getDrawable(R.drawable.edit_text_on_focus_bg)
            binding.oneTimeCodeEmpty.hideView()
        }
        else if(binding.newPasswordEt.hasFocus()) {
            binding.newPasswordEt.background = resources.getDrawable(R.drawable.edit_text_on_focus_bg)
            binding.newPasswordEmpty.hideView()
        }
        else if(binding.confirmPasswordEt.hasFocus()) {
            binding.confirmPasswordEt.background = resources.getDrawable(R.drawable.edit_text_on_focus_bg)
            binding.confirmPasswordEmpty.hideView()
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            binding.PasswordHint.id -> {
                if (isPasswordHintVisible) {
                    fadeOutAnimation(binding.passwordSuggestionView)
                }
                else {
                    fadeInAnimation(binding.passwordSuggestionView)
                }
            }
        }
    }

}