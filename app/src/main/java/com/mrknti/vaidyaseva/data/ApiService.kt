package com.mrknti.vaidyaseva.data

import com.mrknti.vaidyaseva.data.authentication.AuthData
import com.mrknti.vaidyaseva.data.chat.ChatMessage
import com.mrknti.vaidyaseva.data.chat.ChatThread
import com.mrknti.vaidyaseva.data.userService.Service
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.Date

interface ApiService {

    @POST("login")
    @FormUrlEncoded
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): Flow<AuthData>

    @POST("signup/{role}")
    @FormUrlEncoded
    fun signup(
        @Field("username") username: String,
        @Field("password") password: String,
        @Path("role") role: String
    ): Flow<AuthData>

    @GET("service-request/open")
    fun getOpenServices(@Query("pageNumber") pagenum: Int): Flow<List<Service>>

    @GET("service-request/closed")
    fun getClosedServices(@Query("pageNumber") pagenum: Int): Flow<List<Service>>

    @POST("raise-request")
    @FormUrlEncoded
    fun bookService(
        @Field("requestType") serviceType: String,
        @Field("serviceTime") serviceTime: String,
        @Field("comment") comment: String?,
    ): Flow<ServiceBooking>

    @POST("ack")
    @FormUrlEncoded
    fun acknowledgeService(
        @Field("serviceId") serviceId: Int
    ): Flow<Unit>

    @POST("complete")
    @FormUrlEncoded
    fun completeService(
        @Field("serviceId") serviceId: Int
    ): Flow<Unit>

    @GET("chat-threads")
    fun getChats(): Flow<List<ChatThread>>

    @GET("chat")
    fun getChatDetail(
        @Query("threadId") threadId: Int,
        @Query("pageNumber") pagenum: Int
    ): Flow<ChatThread>

    @POST("chat")
    @FormUrlEncoded
    fun addChatMessage(
        @Field("threadId") threadId: Int,
        @Field("chat") message: String
    ): Flow<ChatMessage>

    @POST("register-fcm")
    @FormUrlEncoded
    fun registerFCMToken(
        @Field("registrationToken") token: String
    ): Flow<Unit>
}