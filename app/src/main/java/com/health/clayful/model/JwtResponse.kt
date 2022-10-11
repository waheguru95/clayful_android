package com.health.clayful.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class JwtResponse {
    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("token")
    @Expose
    var token: String? = null
}