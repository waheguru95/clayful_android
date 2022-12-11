package com.health.clayful.ui.feedback

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.health.clayful.R
import com.health.clayful.base.BaseActivity
import com.health.clayful.databinding.ActivityFeedbackBinding
import com.health.clayful.utils.Constants


class FeedbackActivity<T> : BaseActivity<ActivityFeedbackBinding>() {

    override fun createBinding(): ActivityFeedbackBinding {
        return ActivityFeedbackBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        changeTopBarColor(resources.getColor(R.color.app_bg_color))
        changeStatusBarIconColorToWhite(binding.root)

        val url = intent.extras?.getString(Constants.FEEDBACK_URL)

        binding.feedbackWebView.settings.javaScriptEnabled = true
        binding.feedbackWebView.settings.loadWithOverviewMode = true
        binding.feedbackWebView.settings.useWideViewPort = true

        binding.feedbackWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                Log.e("myuriis", url)

                if(url.equals("https://www.clayfulhealth.com/mindfulness") || url.equals("https://app.clayfulhealth.com/mindfulness")) {
                    finish()
                }
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {

            }
        }

        binding.feedbackWebView.loadUrl(url.toString())
    }

}