///*
// * Copyright 2020 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
package com.mrknti.vaidyaseva

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.mrknti.vaidyaseva.data.ApiService
import com.mrknti.vaidyaseva.data.authentication.AuthRepository
import com.mrknti.vaidyaseva.data.authentication.AuthRepositoryImpl
import com.mrknti.vaidyaseva.data.DataStoreManager
import com.mrknti.vaidyaseva.data.chat.ChatRepository
import com.mrknti.vaidyaseva.data.chat.ChatRepositoryImpl
import com.mrknti.vaidyaseva.data.dataModel.EnvelopeConverterFactory
import com.mrknti.vaidyaseva.data.network.CoroutineCallAdapterFactory
import com.mrknti.vaidyaseva.data.room.VaidyasevaDatabase
import com.mrknti.vaidyaseva.data.userService.ServiceRepositoryImpl
import com.mrknti.vaidyaseva.data.userService.ServicesRepository
import com.mrknti.vaidyaseva.notifications.NotificationsManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.LoggingEventListener
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.Date


/**
 * A very simple global singleton dependency graph.
 *
 * For a real app, you would use something like Hilt/Dagger instead.
 */
object Graph {
    private lateinit var okHttpClient: OkHttpClient

    lateinit var database: VaidyasevaDatabase
        private set

    lateinit var dataStoreManager: DataStoreManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var servicesRepository: ServicesRepository
        private set

    lateinit var chatRepository: ChatRepository
        private set

    lateinit var moshi: Moshi
        private set

    @SuppressLint("StaticFieldLeak")
    lateinit var notificationsManager: NotificationsManager
        private set

    private lateinit var retrofit: Retrofit

    private lateinit var apiService: ApiService

    private val mainDispatcher: CoroutineDispatcher
        get() = Dispatchers.Main

    private val ioDispatcher: CoroutineDispatcher
        get() = Dispatchers.IO

    private fun createCache(context: Context): Cache = Cache(
        directory = File(context.cacheDir, CLIENT_CACHE_DIRECTORY),
        maxSize = CLIENT_CACHE_SIZE
    )

    fun provide(context: Context) {
        dataStoreManager = DataStoreManager(context)

        okHttpClient = OkHttpClient.Builder()
            .cache(createCache(context))
            .apply {
                if (BuildConfig.DEBUG) {
                    eventListenerFactory(LoggingEventListener.Factory())
                }
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                addInterceptor(VsInterceptor())
                addInterceptor(
                    ChuckerInterceptor.Builder(context)
                        .alwaysReadResponseBody(true)
                        .build()
                )
            }
            .build()

        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl("http://35.207.46.206:8080/")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(EnvelopeConverterFactory())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()

        apiService = retrofit.create(ApiService::class.java)

        authRepository = AuthRepositoryImpl(apiService)
        servicesRepository = ServiceRepositoryImpl(apiService)
        chatRepository = ChatRepositoryImpl(apiService)
        notificationsManager = NotificationsManager(context)

//        database = Room.databaseBuilder(context, VaidyasevaDatabase::class.java, "data.db")
//            // This is not recommended for normal apps, but the goal of this sample isn't to
//            // showcase all of Room.
//            .fallbackToDestructiveMigration()
//            .build()
    }
}

class VsInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authToken = runBlocking { Graph.dataStoreManager.authToken.first() }
        var request = chain.request()
        val requestBuilder = request.newBuilder()
            .addHeader("Accept", "*/*")
            .addHeader("Content-Type", "application/json;charset=utf-8")
        if (authToken != null) {
            requestBuilder.addHeader("token", "$authToken")
        }
        request = requestBuilder.build()
        return chain.proceed(request = request)
    }
}

private const val CLIENT_CACHE_SIZE = 10 * 1024 * 1024L
private const val CLIENT_CACHE_DIRECTORY = "http"