package com.mrknti.vaidyaseva

import android.app.Application

class VaidyasevaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}