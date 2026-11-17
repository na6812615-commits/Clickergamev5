package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface GoogleSignInOutcome {
    data class Success(val user: FirebaseUser) : GoogleSignInOutcome
    data object Cancelled : GoogleSignInOutcome
    data object NoCredentialAvailable : GoogleSignInOutcome
    data class Failure(val message: String, val suggestWebFallback: Boolean = false) : GoogleSignInOutcome
}

object AccountPrefs {
    private const val PREFS_NAME = "tapcore_account_prefs"
    private const val KEY_NICKNAME_PREFIX = "custom_nickname_"
    private const val KEY_EXPLICIT_SIGN_OUT = "explicit_sign_out"
    private const val KEY_HAS_SIGNED_IN_BEFORE = "has_signed_in_before"

    fun getNickname(context: Context, user: FirebaseUser?): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (user == null) return "Khách"
        val saved = prefs.getString("${KEY_NICKNAME_PREFIX}${user.uid}", "")?.trim().orEmpty()
        if (saved.isNotBlank()) return saved.take(40)
        return user.displayName?.trim()?.take(40)?.ifBlank { null }
            ?: user.email?.substringBefore('@')?.take(40)?.ifBlank { null }
            ?: "TapCore Player"
    }

    fun saveNickname(context: Context, uid: String, nickname: String): String {
        val clean = nickname.trim().replace(Regex("\\s+"), " ").take(40).ifBlank { "TapCore Player" }
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("${KEY_NICKNAME_PREFIX}$uid", clean)
            .apply()
        return clean
    }

    fun isExplicitlySignedOut(context: Context): Boolean {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_EXPLICIT_SIGN_OUT, false)
    }

    fun setExplicitlySignedOut(context: Context, signedOut: Boolean) {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_EXPLICIT_SIGN_OUT, signedOut)
            .apply()
    }

    fun hasSignedInBefore(context: Context): Boolean {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAS_SIGNED_IN_BEFORE, false)
    }

    fun setHasSignedInBefore(context: Context, signedIn: Boolean) {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_SIGNED_IN_BEFORE, signedIn)
            .apply()
    }
}

object GoogleAuthHelper {

    private const val TAG = "GoogleAuthHelper"
    private val autoSignInRunning = java.util.concurrent.atomic.AtomicBoolean(false)
    private val autoSignInAttemptedThisSession = java.util.concurrent.atomic.AtomicBoolean(false)

    fun ensureFirebaseAuth(context: Context): FirebaseAuth? {
        return runCatching {
            if (FirebaseApp.getApps(context.applicationContext).isEmpty()) {
                FirebaseApp.initializeApp(context.applicationContext)
            }
            Firebase.auth
        }.onFailure { err ->
            Log.e(TAG, "FirebaseAuth initialization failed", err)
        }.getOrNull()
    }

    fun observeAuthUser(auth: FirebaseAuth): Flow<FirebaseUser?> = callbackFlow {
        trySend(auth.currentUser)
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun attemptAutoSignIn(
        context: Context,
        auth: FirebaseAuth,
        forceCheck: Boolean = false
    ): GoogleSignInOutcome {
        auth.currentUser?.let {
            AccountPrefs.setHasSignedInBefore(context, true)
            return GoogleSignInOutcome.Success(it)
        }
        if (AccountPrefs.isExplicitlySignedOut(context)) {
            return GoogleSignInOutcome.NoCredentialAvailable
        }
        if (!forceCheck) {
            if (!AccountPrefs.hasSignedInBefore(context)) {
                return GoogleSignInOutcome.NoCredentialAvailable
            }
            if (!autoSignInAttemptedThisSession.compareAndSet(false, true)) {
                return GoogleSignInOutcome.NoCredentialAvailable
            }
        }
        if (!autoSignInRunning.compareAndSet(false, true)) {
            return GoogleSignInOutcome.NoCredentialAvailable
        }
        return try {
            val clientId = runCatching { context.getString(R.string.default_web_client_id) }.getOrNull()
            if (clientId.isNullOrBlank()) {
                return GoogleSignInOutcome.Failure("Thiếu cấu hình OAuth Web Client ID")
            }

            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val hostContext = context.findActivity() ?: context
            val result = credentialManager.getCredential(context = hostContext, request = request)
            handleCredentialResult(context, result.credential, auth)
        } catch (e: CancellationException) {
            throw e
        } catch (_: NoCredentialException) {
            GoogleSignInOutcome.NoCredentialAvailable
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Silent Google sign-in cancelled: ${e.message}", e)
            GoogleSignInOutcome.Cancelled
        } catch (e: GetCredentialException) {
            Log.d(TAG, "Silent auto sign-in skipped (${e.type}): ${e.message}")
            GoogleSignInOutcome.NoCredentialAvailable
        } catch (e: Exception) {
            Log.w(TAG, "Silent auto sign-in exception: ${e.message}", e)
            GoogleSignInOutcome.NoCredentialAvailable
        } finally {
            autoSignInRunning.set(false)
        }
    }

    suspend fun checkPendingWebSignIn(
        context: Context,
        auth: FirebaseAuth
    ): GoogleSignInOutcome? {
        auth.currentUser?.let {
            AccountPrefs.setHasSignedInBefore(context, true)
            return GoogleSignInOutcome.Success(it)
        }
        val pending = auth.pendingAuthResult ?: return null
        return try {
            val result = pending.await()
            val user = result.user ?: return null
            AccountPrefs.setExplicitlySignedOut(context, false)
            AccountPrefs.setHasSignedInBefore(context, true)
            GoogleSignInOutcome.Success(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Pending web sign-in check failed: ${e.message}")
            null
        }
    }

    suspend fun onGoogleSignInClicked(
        context: Context,
        auth: FirebaseAuth
    ): GoogleSignInOutcome {
        AccountPrefs.setExplicitlySignedOut(context, false)
        val hostContext = context.findActivity() ?: context
        val clientId = runCatching { hostContext.getString(R.string.default_web_client_id) }.getOrNull()
        if (clientId.isNullOrBlank()) {
            return GoogleSignInOutcome.Failure(
                "Thiếu cấu hình default_web_client_id từ google-services.json.",
                suggestWebFallback = true
            )
        }

        val credentialManager = CredentialManager.create(hostContext)
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(clientId).build()
        val primaryRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                context = hostContext,
                request = primaryRequest
            )
            handleCredentialResult(hostContext, result.credential, auth)
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google sign-in flow cancelled or dismissed: ${e.message}", e)
            // On some emulators, adding/picking an account closes the sheet before issuing token;
            // attempt silent authorized-account check once in case Google account just finished syncing.
            val silentCheck = attemptAuthorizedGoogleId(hostContext, credentialManager, clientId, auth, filterByAuthorized = true)
            if (silentCheck is GoogleSignInOutcome.Success) {
                silentCheck
            } else {
                GoogleSignInOutcome.Cancelled
            }
        } catch (e: NoCredentialException) {
            Log.w(TAG, "GetSignInWithGoogleOption NoCredentialException, trying GetGoogleIdOption fallback", e)
            tryAllGoogleAccountsFallback(hostContext, credentialManager, clientId, auth)
        } catch (e: GetCredentialProviderConfigurationException) {
            Log.e(TAG, "Credential Manager provider config issue", e)
            tryAllGoogleAccountsFallback(hostContext, credentialManager, clientId, auth)
        } catch (e: GetCredentialUnsupportedException) {
            Log.e(TAG, "Credential Manager unsupported on device", e)
            GoogleSignInOutcome.Failure(
                "Thiết bị không hỗ trợ Credential Manager. Vui lòng dùng nút \"Đăng nhập qua Trình duyệt\" bên dưới.",
                suggestWebFallback = true
            )
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Google sign-in failed (${e.type}): ${e.message}", e)
            GoogleSignInOutcome.Failure(
                "Không thể đăng nhập bằng Credential Manager (${e.localizedMessage ?: e.type}). Bạn có thể thử nút \"Đăng nhập qua Trình duyệt\" bên dưới.",
                suggestWebFallback = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Google auth failed", e)
            GoogleSignInOutcome.Failure(
                "Lỗi xác thực Firebase: ${e.localizedMessage ?: "Không xác định"}",
                suggestWebFallback = true
            )
        }
    }

    private suspend fun tryAllGoogleAccountsFallback(
        hostContext: Context,
        credentialManager: CredentialManager,
        clientId: String,
        auth: FirebaseAuth
    ): GoogleSignInOutcome {
        return try {
            attemptAuthorizedGoogleId(
                context = hostContext,
                credentialManager = credentialManager,
                clientId = clientId,
                auth = auth,
                filterByAuthorized = false
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Fallback GoogleIdOption cancelled: ${e.message}", e)
            GoogleSignInOutcome.Cancelled
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google credential available on device", e)
            GoogleSignInOutcome.Failure(
                "Thiết bị chưa có tài khoản Google sẵn sàng cho Credential Manager. Hãy bấm \"Đăng nhập Google qua Trình duyệt\" bên dưới để đăng nhập trực tiếp!",
                suggestWebFallback = true
            )
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Fallback GoogleIdOption failed (${e.type})", e)
            GoogleSignInOutcome.Failure(
                "Google Play Services trên thiết bị từ chối Credential Manager (${e.localizedMessage ?: e.type}). Hãy dùng \"Đăng nhập Google qua Trình duyệt\" bên dưới!",
                suggestWebFallback = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Fallback GoogleIdOption error", e)
            GoogleSignInOutcome.Failure(
                "Lỗi đăng nhập Google: ${e.localizedMessage ?: "Không xác định"}",
                suggestWebFallback = true
            )
        }
    }

    private suspend fun attemptAuthorizedGoogleId(
        context: Context,
        credentialManager: CredentialManager,
        clientId: String,
        auth: FirebaseAuth,
        filterByAuthorized: Boolean
    ): GoogleSignInOutcome {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorized)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val result = credentialManager.getCredential(context = context, request = request)
        return handleCredentialResult(context, result.credential, auth)
    }

    suspend fun signInWithGoogleWebOAuth(
        context: Context,
        auth: FirebaseAuth
    ): GoogleSignInOutcome {
        val activity = context.findActivity()
            ?: return GoogleSignInOutcome.Failure("Không tìm thấy cửa sổ Activity để mở đăng nhập Google.")

        AccountPrefs.setExplicitlySignedOut(context, false)
        val provider = OAuthProvider.newBuilder("google.com")
            .addCustomParameter("prompt", "select_account")
            .build()

        return try {
            val pending = auth.pendingAuthResult
            val result = if (pending != null) {
                pending.await()
            } else {
                auth.startActivityForSignInWithProvider(activity, provider).await()
            }
            val user = result.user
            if (user != null) {
                AccountPrefs.setExplicitlySignedOut(context, false)
                AccountPrefs.setHasSignedInBefore(context, true)
                GoogleSignInOutcome.Success(user)
            } else {
                GoogleSignInOutcome.Failure("Không thể lấy hồ sơ tài khoản Google từ Firebase.")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val msg = e.localizedMessage.orEmpty()
            Log.e(TAG, "Google Web OAuth failed: $msg", e)
            if (msg.contains("canceled", ignoreCase = true) || msg.contains("cancelled", ignoreCase = true)) {
                GoogleSignInOutcome.Cancelled
            } else {
                GoogleSignInOutcome.Failure("Đăng nhập Google qua trình duyệt thất bại: ${msg.ifBlank { "Lỗi OAuth" }}")
            }
        }
    }

    suspend fun signOut(
        context: Context,
        auth: FirebaseAuth
    ) {
        withContext(NonCancellable) {
            AccountPrefs.setExplicitlySignedOut(context, true)
            try {
                CredentialManager.create(context)
                    .clearCredentialState(ClearCredentialStateRequest())
            } catch (e: ClearCredentialException) {
                Log.w(TAG, "Failed to clear credential state", e)
            }
            auth.signOut()
        }
    }

    private suspend fun handleCredentialResult(
        context: Context,
        credential: Credential,
        auth: FirebaseAuth
    ): GoogleSignInOutcome {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user
            return if (user != null) {
                AccountPrefs.setExplicitlySignedOut(context, false)
                AccountPrefs.setHasSignedInBefore(context, true)
                GoogleSignInOutcome.Success(user)
            } else {
                GoogleSignInOutcome.Failure("Không thể lấy thông tin người dùng Firebase.", suggestWebFallback = true)
            }
        }
        return GoogleSignInOutcome.Failure(
            "Loại chứng thực không được hỗ trợ (${credential.type}).",
            suggestWebFallback = true
        )
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
