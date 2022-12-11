package com.health.clayful.viewModel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.health.clayful.R
import com.health.clayful.model.JwtResponse
import com.health.clayful.model.UserModel
import com.health.clayful.network.ApiConstants
import com.health.clayful.network.responseAndErrorHandle.ErrorMessage
import com.health.clayful.ui.home.HomeActivity
import com.health.clayful.ui.login.LoginActivity
import com.health.clayful.utils.ConnectivityReceiver
import com.smartner.australia.network.responseAndErrorHandle.ApiResponse
import io.customer.sdk.CustomerIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import zendesk.android.Zendesk
import zendesk.messaging.android.DefaultMessagingFactory

class HomeViewModel(val homeActivity: HomeActivity<ViewBinding>) : ViewModel(), Callback<JsonElement>, ApiResponse {

    lateinit var apiResponse : ApiResponse
    private var errorMessageModel : MutableLiveData<ErrorMessage> = MutableLiveData()
    val isProgressShowing : MutableLiveData<Boolean> = MutableLiveData()
    val jwtModel : MutableLiveData<JwtResponse> = MutableLiveData()

    private var getCallLogin : Call<JsonElement>?= null
    private var getCallZendeskJwtToken : Call<JsonElement>?= null

    private fun hitApi(call: Call<JsonElement>?, showProgress: Boolean, context: Context, listener: ApiResponse) {

        if (ConnectivityReceiver().isConnectedOrConnecting(context)) {
            if(call != getCallZendeskJwtToken) {
                isProgressShowing.value = showProgress
            }
            call?.enqueue(this)
            apiResponse = listener
        } else {
            goToLoginScreen(homeActivity.resources.getString(R.string.no_internet_available))
        }
    }

    override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
        if (response.isSuccessful) {
            apiResponse.onSuccess(call, response.code(), response.body()!!.toString())
        } else {
            try {
                errorMessageModel.value = Gson().fromJson(response.errorBody()?.string(), ErrorMessage::class.java)
                apiResponse.onError(call, response.code(), errorMessageModel.value?.message.toString())
            }
            catch (ex : Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun onFailure(call: Call<JsonElement>, t: Throwable) {
        isProgressShowing.value = false
        goToLoginScreen(t.message.toString())
    }

    override fun onSuccess(call: Call<JsonElement>, responseCode: Int, response: String) {
        when(call) {
            getCallZendeskJwtToken -> {
                jwtModel.value = Gson().fromJson(response, JwtResponse::class.java)
            }
        }
    }

    override fun onError(call: Call<JsonElement>, errorCode: Int, errorMsg: String) {
        isProgressShowing.value = false
        goToLoginScreen(errorMsg)
    }



    fun getZendeskJwtToken(userId: String) {
        val jsonObject = JSONObject()
        jsonObject.put("external_id", userId)

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        getCallZendeskJwtToken = ApiConstants.getJwtService().getJwtToken(requestBody)
        hitApi(getCallZendeskJwtToken, false, homeActivity, this)
    }

    fun zendeskLogin()
    {
        Zendesk.initialize(
            context = homeActivity,
            channelKey = homeActivity.getString(R.string.zendesk_channel_key),
            {
                Log.e("IntegrationApplication", "Initialization successful")

                Zendesk.instance.loginUser(ApiConstants.jwtToken, {
                    CoroutineScope(Dispatchers.IO).launch {
                        homeActivity.prefData.saveZendeskLoginStatus(true)
                    }
                    Log.e("IntegrationApplication", "User Login successful")
                }, {
                    Log.e("IntegrationApplication", "User Login Failed")
                })
            },
            { error ->
                Log.e("IntegrationApplication", "Initialization failed", error)
            },
            messagingFactory = DefaultMessagingFactory()
        )
    }

    private fun goToLoginScreen(message : String) {
        val mIntent = Intent(homeActivity, LoginActivity::class.java)
        val mBundle = Bundle()
        mBundle.putString("dialog_message",message)
        mBundle.putBoolean("has_error",true)
        mIntent.putExtras(mBundle)
        homeActivity.startActivity(mIntent)
        homeActivity.finish()
    }
}