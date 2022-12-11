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
import com.health.clayful.model.UserModel
import com.health.clayful.network.ApiConstants
import com.health.clayful.network.responseAndErrorHandle.ErrorMessage
import com.health.clayful.ui.forgotPassword.ForgotPasswordActivity
import com.health.clayful.ui.login.LoginActivity
import com.health.clayful.utils.ConnectivityReceiver
import com.health.clayful.utils.Constants
import com.health.clayful.utils.startIntentActivity
import com.smartner.australia.network.responseAndErrorHandle.ApiResponse
import io.customer.sdk.CustomerIO
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel(val loginActivity: LoginActivity<ViewBinding>) : ViewModel(),
    Callback<JsonElement>, ApiResponse {

    private var getCallLogin : Call<JsonElement> ?= null

    var userEmail : ObservableField<String> = ObservableField("")
    var userPassword : ObservableField<String> = ObservableField("")

    lateinit var apiResponse : ApiResponse

    val isProgressShowing : MutableLiveData<Boolean> = MutableLiveData()
    private var errorMessageModel : MutableLiveData<ErrorMessage> = MutableLiveData()
    val userModel : MutableLiveData<UserModel> = MutableLiveData()

    var fieldError : MutableLiveData<String> = MutableLiveData()


    fun callLogin() {
        if(isValidCredential()) {
            callUserLogin()
        }
    }

    private fun isValidCredential(): Boolean {
        return if(userEmail.get().toString().trim().isEmpty() && userPassword.get().toString().trim().isEmpty()) {
            fieldError.value = Constants.EMAIL_OR_PASSWORD_EMPTY
            false
        }
        else if(userEmail.get().toString().trim().isEmpty()) {
            fieldError.value = Constants.EMAIL_EMPTY_FIELD_ERROR
            false
        }
        else if(userPassword.get()!!.isEmpty()) {
            fieldError.value = Constants.PASSWORD_EMPTY_FIELD_ERROR
            false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail.get().toString().trim()).matches()) {
            fieldError.value = Constants.INVALID_EMAIL_ERROR
            false
        }
        else true
    }

    fun openForgotPassword() {
        loginActivity.startIntentActivity<ForgotPasswordActivity<Any?>>(false)
    }

    fun identifyPerson(userId: String?) {
        if(loginActivity.builder != null) {
            CustomerIO.instance().identify(userId.toString())
        }
        else {
            loginActivity.initializeCustomerIO()
            CustomerIO.instance().identify(userId.toString())
        }
    }

    private fun callUserLogin() {

        val jsonObject = JSONObject()
        jsonObject.put("email", userEmail.get().toString().trim())
        jsonObject.put("password", userPassword.get().toString().trim())
        //jsonObject.put("site", loginActivity.resources.getString(R.string.site_key))
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        getCallLogin = ApiConstants.getApiServices().apiLogin(requestBody)
        hitApi(getCallLogin, true, loginActivity, this)

    }

    private fun hitApi(call: Call<JsonElement>?, showProgress: Boolean, context: Context, listener: ApiResponse) {

        if (ConnectivityReceiver().isConnectedOrConnecting(context)) {
            if(showProgress) {
                isProgressShowing.value = showProgress
            }
            call?.enqueue(this)
            apiResponse = listener
        } else {
            loginActivity.showAlertDialog(loginActivity.resources.getString(R.string.no_internet_available))
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
        loginActivity.showAlertDialog(t.message.toString())
    }

    override fun onSuccess(call: Call<JsonElement>, responseCode: Int, response: String) {
        if(call == getCallLogin) {
            userModel.value = Gson().fromJson(response, UserModel::class.java)
        }
    }

    override fun onError(call: Call<JsonElement>, errorCode: Int, errorMsg: String) {
        isProgressShowing.value = false
        loginActivity.showAlertDialog(errorMsg)
    }
}