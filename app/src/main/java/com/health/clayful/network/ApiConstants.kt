package com.health.clayful.network

import androidx.viewbinding.ViewBinding
import com.google.gson.GsonBuilder
import com.health.clayful.R
import com.health.clayful.base.BaseActivity
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConstants {

    private var requestTimeout = 60
    var authToken = ""
    var jwtToken = ""
    var userId = ""
    private var context : BaseActivity<ViewBinding> ?= null

    private var retrofit : Retrofit? = null
    private var okHttpClient: OkHttpClient? = null

    private const val BASE_URL = "https://client.memberstack.com/"
    private const val CUSTOMER_IO_BASE_URL = "https://track.customer.io/api/v1/"
    //private const val JWT_TOKEN_URL = "https://sleepy-shelf-99425.herokuapp.com/"
    private const val JWT_TOKEN_URL = "http://34.27.91.26:8000/"

    // Create Service
    private val service = getClient(BASE_URL, false).create(ApiServices::class.java)
    private val jwtService = getClient(JWT_TOKEN_URL, true).create(ApiServices::class.java)
    private val customerService = getClient(CUSTOMER_IO_BASE_URL, true).create(ApiServices::class.java)

    fun initContext(mContext: BaseActivity<ViewBinding>) {
        this.context = mContext
    }
    fun getApiServices(): ApiServices {
        return service
    }

    fun getJwtService() : ApiServices {
        return jwtService
    }

    fun getCustomerService(): ApiServices? {
        return customerService
    }
    // Create Retrofit Client
    private fun getClient(baseUrl: String, initializeAgain : Boolean): Retrofit {

        if (okHttpClient == null)
            initOkHttp()

        val gson = GsonBuilder().setLenient().create()
        if (retrofit == null || initializeAgain) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient!!)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson)).build()
        }

        return retrofit!!
    }

    private fun initOkHttp() {

        val httpClient = OkHttpClient().newBuilder()
            .connectTimeout(requestTimeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(requestTimeout.toLong(), TimeUnit.SECONDS)
            .writeTimeout(requestTimeout.toLong(), TimeUnit.SECONDS)

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        httpClient.addInterceptor(interceptor)

        httpClient.addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-APP-ID","app_cl6n1dm04001f0znngi290pyo")
                .addHeader("Origin","${context?.resources?.getString(R.string.app_origin)}")

            /*  .addHeader("deviceType", "Android")
              .addHeader("appVersion", BuildConfig.VERSION_NAME)

               Adding Authorization token (API Key)
               Requests will be denied without API key
          if (!TextUtils.isEmpty(PrefUtils().getAuthKey(context))) {
              requestBuilder.addHeader("Authorization","Bearer ${PrefUtils().getAuthKey(context)!!}")
          }
          */
            val request = requestBuilder.build()
            chain.proceed(request)
        }

        okHttpClient = httpClient.build()
    }
}