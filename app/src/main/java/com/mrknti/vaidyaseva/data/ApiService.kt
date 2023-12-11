package com.mrknti.vaidyaseva.data

import com.mrknti.vaidyaseva.data.authentication.AuthData
import com.mrknti.vaidyaseva.data.authentication.RegisterDevice
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.building.RoomOccupancy
import com.mrknti.vaidyaseva.data.chat.ChatMessage
import com.mrknti.vaidyaseva.data.chat.ChatThread
import com.mrknti.vaidyaseva.data.user.InboxItem
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.data.user.UserDocument
import com.mrknti.vaidyaseva.data.user.UserInfo
import com.mrknti.vaidyaseva.data.userService.Service
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @POST("login")
    @FormUrlEncoded
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): Flow<AuthData>

    @POST("register")
    @FormUrlEncoded
    fun registerUser(
        @Field("firstName") firstName: String,
        @Field("lastName") lastName: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("role") role: String
    ): Flow<User>

    @GET("service-request/open")
    fun getOpenServices(@Query("lastServiceId") lastServiceId: Int?): Flow<List<Service>>

    @GET("service-request/closed")
    fun getClosedServices(@Query("lastServiceId") lastServiceId: Int?): Flow<List<Service>>

    @POST("raise-request")
    @FormUrlEncoded
    fun bookService(
        @Field("requestType") serviceType: String,
        @Field("serviceTime") serviceTime: String,
        @Field("comment") comment: String?,
        @Field("bookForUserId") requesterId: Int?,
        @Field("source") source: Int?,
        @Field("destination") destination: Int?,
    ): Flow<Service>

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
        @Query("lastMessageId") lastMessageId: Int?
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
        @Field("registrationToken") token: String,
        @Field("deviceId") deviceId: Int?
    ): Flow<RegisterDevice>

    @Multipart
    @POST("upload")
    fun uploadDocument(
        @Part("userId") clientId: Int,
        @Part("documentType") documentType: Int,
        @Part("expiryTime") expiryTime: RequestBody,
        @Part data: List<MultipartBody.Part>
    ): Flow<Unit>

    @GET("search/user")
    fun searchUser(
        @Query("searchQuery") query: String
    ): Flow<List<User>>

    @GET("building-grid")
    fun getBuildings(): Flow<List<BuildingData>>

    @GET("building-data")
    fun getBuildingDetail(
        @Query("buildingId") buildingId: Int
    ): Flow<BuildingData>

    @POST("book/room")
    @FormUrlEncoded
    fun bookRoom(
        @Field("roomId") roomId: Int,
        @Field("occupantId") occupantId: Int,
        @Field("checkInTime") checkIn: String,
        @Field("checkOutTime") checkOut: String
    ): Flow<RoomOccupancy>

    @POST("book/checkout")
    @FormUrlEncoded
    fun checkOutOccupancy(
        @Field("occupancyId") occupancyId: Int
    ): Flow<RoomOccupancy>

    @GET("get-all")
    fun getAllDocuments(
        @Query("userId") userId: Int
    ): Flow<List<UserDocument>>

    @GET("/inbox")
    fun getInbox(): Flow<List<InboxItem>>

    @GET("/user-info")
    fun getUserInfo(@Query("userId") userId: Int?): Flow<UserInfo>

    @POST("/logout")
    @FormUrlEncoded
    fun logout(@Field("deviceId") deviceId: Int?): Flow<Unit>

    @HTTP(method = "DELETE", path = "/delete-document", hasBody = false)
    fun deleteDocument(@Query("documentId") id: Int): Flow<Unit>

}