package com.mrknti.vaidyaseva.data.dataModel

import com.squareup.moshi.Types
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type


data class Envelope<T>(val status: Int?, val message: String?, val data: T?)

internal class EnvelopeConverter<T>(private val delegate: Converter<ResponseBody, Envelope<T?>>) :
    Converter<ResponseBody, T?> {

    @Throws(IOException::class)
    override fun convert(responseBody: ResponseBody): T? {
        val envelope = delegate.convert(responseBody)
        return envelope?.data
    }
}

class EnvelopeConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type, annotations: Array<out Annotation>, retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val envelopeType = Types.newParameterizedType(Envelope::class.java, type)
        val delegate = retrofit.nextResponseBodyConverter<Envelope<Any?>?>(this, envelopeType, annotations)
        return EnvelopeConverter(delegate)
    }
}