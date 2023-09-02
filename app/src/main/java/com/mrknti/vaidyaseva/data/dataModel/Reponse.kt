package com.mrknti.vaidyaseva.data.dataModel

data class Reponse<T>(val status: Int, val message: String?, val data: T?)