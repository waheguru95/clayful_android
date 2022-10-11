package com.health.clayful.ui.forgotPassword

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewbinding.ViewBinding
import com.health.clayful.R
import com.health.clayful.base.BaseActivity
import com.health.clayful.databinding.ActivityForgotPasswordBinding
import com.health.clayful.ui.login.LoginActivity
import com.health.clayful.ui.newPassword.NewPasswordActivity
import com.health.clayful.utils.Constants
import com.health.clayful.utils.ForgotPasswordViewModelFactory
import com.health.clayful.utils.startIntentActivity
import com.health.clayful.viewModel.ForgotPasswordViewModel

class ForgotPasswordActivity<T>  : BaseActivity<ActivityForgotPasswordBinding>(), TextWatcher {

    private var viewModel : ForgotPasswordViewModel ?= null

    override fun createBinding(): ActivityForgotPasswordBinding {
        return ActivityForgotPasswordBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDialogView()
        initiateViewModel()
        viewModelObserver()
        initiateShakeAnimation()
        changeTopBarColor(resources.getColor(R.color.white))
        changeStatusBarIconColorToBlack(binding.root)

        binding.emailEt.addTextChangedListener(this)
        binding.emailEt.onFocusChangeListener = MyFocusChangeListener(binding.emailEt)

    }

    private fun initiateViewModel() {
        viewModel = ViewModelProviders.of(this, ForgotPasswordViewModelFactory(this as ForgotPasswordActivity<ViewBinding>)).get(ForgotPasswordViewModel::class.java)
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

        viewModel?.forgotPasswordResponse?.observe(this, Observer {
            if(it!= null) {
                startIntentActivity<NewPasswordActivity<Any?>>(false)
                viewModel?.forgotPasswordResponse?.value = null
            }
        })

        viewModel?.fieldError?.observe(this, Observer { errorType ->
            if(errorType != null) {
                when (errorType) {
                    Constants.EMAIL_EMPTY_FIELD_ERROR -> {
                        binding.emailFieldCantBeEmpty.showView()
                        binding.emailEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.emailEt.startAnimation(shake)
                    }
                    Constants.INVALID_EMAIL_ERROR -> {
                        binding.emailFieldCantBeEmpty.text = getString(R.string.invalid_email)
                        binding.emailFieldCantBeEmpty.showView()
                        binding.emailEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.emailEt.startAnimation(shake)
                    }
                }
                viewModel?.fieldError?.value = null
            }
        })
    }

    inner class MyFocusChangeListener(private val editText: AppCompatEditText): View.OnFocusChangeListener {
        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (editText.id == binding.emailEt.id && hasFocus) {
                Handler().postDelayed({
                    binding.scrollView.smoothScrollTo(0,500)
                },400)
                if(!binding.emailFieldCantBeEmpty.isVisible) {
                    binding.emailEt.background = resources.getDrawable(R.drawable.edit_text_on_focus_bg)
                }
            }
        }
    }

    override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) { }

    override fun afterTextChanged(p0: Editable) {
        if(binding.emailEt.hasFocus()) {
            binding.emailEt.background = resources.getDrawable(R.drawable.edit_text_on_focus_bg)
            binding.emailFieldCantBeEmpty.hideView()
        }
    }
}