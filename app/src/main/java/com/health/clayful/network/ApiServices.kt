package com.health.clayful.network

import com.google.gson.JsonElement
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiServices {

    @POST("auth/login/")
    fun apiLogin(@Body requestBody: RequestBody): Call<JsonElement>

    @POST("auth/send-reset-password-email/")
    fun apiForgotPassword(@Body requestBody: RequestBody): Call<JsonElement>

    @POST("auth/reset-password/")
    fun apiResetPassword(@Body requestBody: RequestBody): Call<JsonElement>

    @POST("api/get_token/")
    fun getJwtToken(@Body requestBody: RequestBody) : Call<JsonElement>

    /*@POST("customers/{identifier}")
    fun apiIdentifyPeople(@Path("identifier") identifier: String, @Header("Authorization") authorization: String, @Body requestBody: RequestBody) : Call<JsonElement>*/
}
