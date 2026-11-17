package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val wireValue: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write")
}

object FirestoreErrorHandler {

    private const val TAG = "FirestoreError"

    fun handleFirestoreError(
        error: Throwable,
        operationType: OperationType,
        path: String?,
        auth: FirebaseAuth? = runCatching { Firebase.auth }.getOrNull()
    ): String {
        val currentUser = auth?.currentUser
        val providerArray = JSONArray().apply {
            currentUser?.providerData?.forEach { info ->
                put(
                    JSONObject().apply {
                        put("providerId", info.providerId)
                        put("displayName", info.displayName ?: "")
                        put("email", info.email ?: "")
                        put("photoUrl", info.photoUrl?.toString() ?: "")
                    }
                )
            }
        }

        val authInfo = JSONObject().apply {
            put("userId", currentUser?.uid ?: "")
            put("email", currentUser?.email ?: "")
            put("emailVerified", currentUser?.isEmailVerified ?: false)
            put("isAnonymous", currentUser?.isAnonymous ?: false)
            put("providerInfo", providerArray)
        }

        val payload = JSONObject().apply {
            put("error", error.message ?: error.javaClass.simpleName)
            put("operationType", operationType.wireValue)
            put("path", path ?: "")
            put("authInfo", authInfo)
        }.toString()

        Log.e(TAG, "Firestore Error: $payload", error)
        return payload
    }
}
