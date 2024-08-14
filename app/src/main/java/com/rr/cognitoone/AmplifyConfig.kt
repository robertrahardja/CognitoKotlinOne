package com.rr.cognitoone

import android.content.Context
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify

object AmplifyConfig {
    fun initialize(context: Context) {
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(context)
            println("Initialized Amplify")
        } catch (error: Exception) {
            println("Could not initialize Amplify: " + error.message)
        }
    }
}