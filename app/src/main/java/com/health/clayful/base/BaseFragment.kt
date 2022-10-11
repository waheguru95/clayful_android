package com.health.clayful.base

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.health.clayful.ui.home.HomeActivity


abstract class BaseFragment<V : ViewBinding> : Fragment() {

    protected var binding: V? = null
    private var mContext : Context ?= null
    lateinit var baseActivity : BaseActivity<ViewBinding>
    lateinit var homeActivity : HomeActivity<ViewBinding>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if(binding?.root == null) {
            binding = onCreateBinding(inflater, container, savedInstanceState)
        }

        return binding?.root
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    abstract fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): V

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context

        try {
            baseActivity = context as BaseActivity<ViewBinding>

            when(mContext) {
                is HomeActivity<*> -> {
                    homeActivity = context as HomeActivity<ViewBinding>
                }
            }
        }
        catch (ex : Exception){}
    }

    @JvmName("getBaseActivity1")
    fun getBaseActivity() : BaseActivity<ViewBinding> {
        return baseActivity
    }

    @JvmName("getHomeActivity1")
    fun getHomeActivity(): HomeActivity<ViewBinding> {
        return homeActivity
    }

    fun View.hideView() {
        this.visibility = View.GONE
    }

    fun View.showView() {
        this.visibility = View.VISIBLE
    }

    fun hideKeyboard() {
        getBaseActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
}