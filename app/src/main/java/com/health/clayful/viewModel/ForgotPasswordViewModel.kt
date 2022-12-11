package com.health.clayful.viewModel

import android.content.Context
import android.util.Patterns
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.health.clayful.R
import com.health.clayful.network.ApiConstants
import com.health.clayful.network.responseAndErrorHandle.ErrorMessage
import com.health.clayful.ui.forgotPassword.ForgotPasswordActivity
import com.health.clayful.ui.newPassword.NewPasswordActivity
import com.health.clayful.utils.ConnectivityReceiver
import com.health.clayful.utils.Constants
import com.health.clayful.utils.startIntentActivity
import com.smartner.australia.network.responseAndErrorHandle.ApiResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordViewModel(val myActivity: ForgotPasswordActivity<ViewBinding>) : ViewModel(), Callback<JsonElement>, ApiResponse {

    private lateinit var context: Context
    var resetPasswordEmail : ObservableField<String> = ObservableField("")

    private var getCallForgotPassword : Call<JsonElement>?= null

    lateinit var apiResponse : ApiResponse

    val isProgressShowing : MutableLiveData<Boolean> = MutableLiveData()
    private var errorMessageModel : MutableLiveData<ErrorMessage> = MutableLiveData()
    val forgotPasswordResponse : MutableLiveData<Boolean> = MutableLiveData()
    var fieldError : MutableLiveData<String> = MutableLiveData()

    private fun hitApi(call: Call<JsonElement>?, showProgress: Boolean, context: Context, listener: ApiResponse) {
        if (ConnectivityReceiver().isConnectedOrConnecting(context)) {
            if(showProgress){
                isProgressShowing.value = true
            }
            call?.enqueue(this)
            apiResponse = listener
        } else {
            myActivity.showAlertDialog(myActivity.resources.getString(R.string.no_internet_available))
        }
    }

    fun sendResetPasswordMail() {
        if(resetPasswordEmail.get().toString().trim().isEmpty()) {
            fieldError.value = Constants.EMAIL_EMPTY_FIELD_ERROR
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(resetPasswordEmail.get().toString().trim()).matches()) {
            fieldError.value = Constants.INVALID_EMAIL_ERROR
        }
        else {
            val jsonObject = JSONObject()
            jsonObject.put("email", resetPasswordEmail.get().toString().trim())
            //jsonObject.put("site", myActivity.resources.getString(R.string.site_key))

            val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

            getCallForgotPassword = ApiConstants.getApiServices().apiForgotPassword(requestBody)
            hitApi(getCallForgotPassword, true, myActivity, this)
        }
    }

    fun openResetPasswordFragmnt() {
        myActivity.startIntentActivity<NewPasswordActivity<Any?>>(false)
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
        myActivity.showAlertDialog(t.message.toString())
    }

    override fun onSuccess(call: Call<JsonElement>, responseCode: Int, response: String) {
        isProgressShowing.value = false

        when(call) {
            getCallForgotPassword -> {
                forgotPasswordResponse.value = true
            }
        }
    }

    override fun onError(call: Call<JsonElement>, errorCode: Int, errorMsg: String) {
        isProgressShowing.value = false
        myActivity.showAlertDialog(errorMsg)
    }


}