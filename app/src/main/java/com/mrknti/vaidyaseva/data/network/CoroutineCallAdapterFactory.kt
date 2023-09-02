package com.mrknti.vaidyaseva.data.network

import kotlinx.coroutines.flow.Flow
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class CoroutineCallAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Flow::class.java) {
            return null
        }
        val typeOfFlow = getInnerType(returnType, Flow::class.java) ?: return null
        return CoroutineCallAdapter<Any>(typeOfFlow)
    }

    private fun getInnerType(type: Type, klazz: Class<*>): Type? {
        if (type !is ParameterizedType) {
            return null
        }
        if (getRawType(type) != klazz) {
            return null
        }
        return getParameterUpperBound(0, type)
    }

}