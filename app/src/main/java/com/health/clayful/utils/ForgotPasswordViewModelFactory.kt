package com.health.clayful.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.health.clayful.ui.forgotPassword.ForgotPasswordActivity
import com.health.clayful.viewModel.ForgotPasswordViewModel

class ForgotPasswordViewModelFactory(private val myActivity: ForgotPasswordActivity<ViewBinding>) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            return ForgotPasswordViewModel(myActivity) as T
        }
        else {
            throw IllegalArgumentException("Unknown class name")
        }
    }
}