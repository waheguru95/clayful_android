package com.health.clayful.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.health.clayful.ui.newPassword.NewPasswordActivity
import com.health.clayful.viewModel.ForgotPasswordViewModel
import com.health.clayful.viewModel.NewPasswordViewModel

class NewPasswordViewModelFactory(private val myActivity: NewPasswordActivity<ViewBinding>) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(NewPasswordViewModel::class.java)) {
            return NewPasswordViewModel(myActivity) as T
        }
        else {
            throw IllegalArgumentException("Unknown class name")
        }
    }
}