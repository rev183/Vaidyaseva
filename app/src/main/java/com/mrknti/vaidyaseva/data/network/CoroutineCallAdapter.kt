package com.mrknti.vaidyaseva.data.network

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import okio.use
import org.json.JSONObject
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Invocation
import retrofit2.Response
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CoroutineCallAdapter<T>(
    private val responseType: Type,
) : CallAdapter<T, Flow<*>> {

    override fun adapt(call: Call<T>): Flow<T> {
        return flow {
            emit(suspendCancellableCoroutine { continuation ->
                call.enqueue(object : Callback<T> {
                    override fun onFailure(call: Call<T>, t: Throwable) {
                        continuation.resumeWithException(t)
                    }

                    override fun onResponse(call: Call<T>, response: Response<T>) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body == null) {
                                val invocation = call.request().tag(Invocation::class.java)!!
                                val method = invocation.method()
                                val e = KotlinNullPointerException("Response from " +
                                        method.declaringClass.name +
                                        '.' +
                                        method.name +
                                        " was null but response body type was declared as non-null")
                                continuation.resumeWithException(e)
                            } else {
                                continuation.resume(body)
                            }
                        } else {
                            try {
                                val errorBodyJson = response.errorBody()?.byteStream().use { stream ->
                                    val text = stream?.bufferedReader()?.readText()
                                    Log.e("CoroutineCallAdapter", "Error body: $text")
                                    text
                                }
                                val errorJsonBody = JSONObject(errorBodyJson!!)
                                val errorBody =
                                    VsErrorBody(response.code(), errorJsonBody.getString("message"))
                                continuation.resumeWithException(VsNetworkException(errorBody))
                            } catch (e: Exception) {
                                Log.e("CoroutineCallAdapter", "Error parsing error body", e)
                                continuation.resumeWithException(HttpException(response))
                            }
                        }
                    }
                })
                continuation.invokeOnCancellation { call.cancel() }
            })
        }
    }

    override fun responseType(): Type = responseType

}