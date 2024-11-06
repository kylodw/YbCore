package com.ybzl.network

import android.app.Application
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetWorkRequest {
    private val loggingInterceptor by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        interceptor
    }
    private const val TIME_OUT = 30L
    private val okHttpClient by lazy {
        OkHttpClient.Builder().apply {
            writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            readTimeout(TIME_OUT, TimeUnit.SECONDS)
            callTimeout(TIME_OUT, TimeUnit.SECONDS)
            connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            addInterceptor(loggingInterceptor)
        }.build()
    }
    private var retrofit: Retrofit? = null
    private var application: Application? = null


    fun <T : Any> createApiService(clazz: Class<T>): T {
        if (retrofit == null) {
            throw IllegalArgumentException("retrofit is null")
        }
        return retrofit!!.create(clazz)
    }


    fun updateRetrofit(newBaseUrl: String, newOkHttpClient: OkHttpClient? = null) {
        retrofit = Retrofit.Builder()
            .baseUrl(newBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(newOkHttpClient ?: okHttpClient)
            .build()
    }

    fun init(application: Application, baseUrl: String) {
        this.application = application
        updateRetrofit(baseUrl)
    }

    fun getApplicationContext(): Context {
        if (application == null) {
            throw IllegalArgumentException("application is null")
        }
        return application!!.applicationContext
    }
}