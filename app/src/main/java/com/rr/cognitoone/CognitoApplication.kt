package com.rr.cognitoone

import android.app.Application

class CognitoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AmplifyConfig.initialize(this)
    }
}
