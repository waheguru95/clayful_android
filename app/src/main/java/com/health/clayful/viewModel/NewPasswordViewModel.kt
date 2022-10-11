package com.health.clayful.viewModel

import android.content.Context
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
import com.health.clayful.ui.newPassword.NewPasswordActivity
import com.health.clayful.utils.ConnectivityReceiver
import com.health.clayful.utils.Constants
import com.smartner.australia.network.responseAndErrorHandle.ApiResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewPasswordViewModel(val myActivity: NewPasswordActivity<ViewBinding>) : ViewModel(), Callback<JsonElement>, ApiResponse {

    var resetPasswordOtp : ObservableField<String> = ObservableField("")
    var newPassword : ObservableField<String> = ObservableField("")
    var confirmPassword : ObservableField<String> = ObservableField("")

    private var getCallResetPassword : Call<JsonElement>?= null
    lateinit var apiResponse : ApiResponse

    val isProgressShowing : MutableLiveData<Boolean> = MutableLiveData()
    private var errorMessageModel : MutableLiveData<ErrorMessage> = MutableLiveData()
    val resetPasswordResponse : MutableLiveData<Boolean> = MutableLiveData()
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

    fun callResetPassword() {
        if(resetPasswordOtp.get()!!.trim().isEmpty() && newPassword.get()!!.trim().isEmpty() && confirmPassword.get()!!.trim().isEmpty()) {
            fieldError.value = Constants.ALL_FIELDS_ARE_EMPTY
        }
        else if(resetPasswordOtp.get()!!.trim().isEmpty()) {
            fieldError.value = Constants.OTP_FIELD_EMPTY
        }
        else if(newPassword.get()!!.trim().isEmpty()) {
            fieldError.value = Constants.NEW_PASSWORD_EMPTY
        }
        else if(confirmPassword.get()!!.trim().isEmpty()) {
            fieldError.value = Constants.CONFIRM_PASSWORD_EMPTY
        }
        else if(newPassword.get()!!.trim().length < 8) {
            fieldError.value = Constants.PASSWORD_LENGTH_ERROR
        }
        else if(!confirmPassword.get()!!.trim().equals(newPassword.get()!!.trim())) {
            fieldError.value = Constants.CONFIRM_PASSWORD_MISMATCH
        }
        else {
            val jsonObject = JSONObject()
            jsonObject.put("newPassword", newPassword.get().toString().trim())
            jsonObject.put("token", resetPasswordOtp.get().toString().trim())
           // jsonObject.put("site", myActivity.resources.getString(R.string.site_key))

            val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

            getCallResetPassword = ApiConstants.getApiServices().apiResetPassword(requestBody)
            hitApi(getCallResetPassword, true, myActivity, this)
        }
    }

    fun goBack() {
        myActivity.finish()
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
            getCallResetPassword -> {
                resetPasswordResponse.value = true
            }
        }
    }

    override fun onError(call: Call<JsonElement>, errorCode: Int, errorMsg: String) {
        isProgressShowing.value = false
        myActivity.showAlertDialog(errorMsg)
    }
}