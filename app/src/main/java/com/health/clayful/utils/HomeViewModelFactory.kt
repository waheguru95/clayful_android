package com.health.clayful.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.health.clayful.ui.home.HomeActivity
import com.health.clayful.viewModel.HomeViewModel

class HomeViewModelFactory(private val homeActivity: HomeActivity<ViewBinding>) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(homeActivity) as T
        }
        else {
            throw IllegalArgumentException("Unknown class name")
        }
    }
}