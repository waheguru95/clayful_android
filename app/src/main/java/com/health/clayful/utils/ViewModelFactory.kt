package com.health.clayful.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.health.clayful.ui.login.LoginActivity
import com.health.clayful.viewModel.AuthViewModel

class ViewModelFactory(private val authActivity: LoginActivity<ViewBinding>) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authActivity) as T
        }
        else {
            throw IllegalArgumentException("Unknown class name")
        }
    }
}