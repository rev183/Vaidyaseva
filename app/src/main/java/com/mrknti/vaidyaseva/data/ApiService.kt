package com.mrknti.vaidyaseva.data

import com.mrknti.vaidyaseva.data.dataModel.AuthData
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

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

}