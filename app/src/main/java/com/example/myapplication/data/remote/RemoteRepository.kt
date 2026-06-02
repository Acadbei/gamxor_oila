package com.example.myapplication.data.remote

import android.content.Context
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.local.LocalUserStore
import com.example.myapplication.data.model.CaregiverProfile
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RemoteRepository private constructor(
    context: Context
) {

    private val appContext = context.applicationContext
    private val userStore = LocalUserStore(appContext)
    private val okHttpClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(userStore))
            .build()
    }

    @Volatile
    private var cachedBaseUrl: String? = null

    @Volatile
    private var cachedApi: FamilyCareApiService? = null

    private fun api(): FamilyCareApiService {
        val resolvedBaseUrl = currentBaseUrl()
        return cachedApi?.takeIf { cachedBaseUrl == resolvedBaseUrl } ?: synchronized(this) {
            cachedApi?.takeIf { cachedBaseUrl == resolvedBaseUrl } ?: buildApi(resolvedBaseUrl).also {
                cachedBaseUrl = resolvedBaseUrl
                cachedApi = it
            }
        }
    }

    private fun buildApi(baseUrl: String): FamilyCareApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(FamilyCareApiService::class.java)
    }

    fun currentBaseUrl(): String {
        return normalizeBaseUrl(
            userStore.loadBackendBaseUrl(BuildConfig.BACKEND_BASE_URL)
        )
    }

    fun updateBaseUrl(baseUrl: String): String {
        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)
        userStore.saveBackendBaseUrl(normalizedBaseUrl)
        synchronized(this) {
            cachedBaseUrl = null
            cachedApi = null
        }
        return normalizedBaseUrl
    }

    suspend fun healthcheck() = api().healthcheck()

    suspend fun requestCode(phone: String) = api().requestCode(RequestCodeRequest(phone))

    suspend fun verifyCode(phone: String, challengeId: String, code: String) =
        api().verifyCode(VerifyCodeRequest(phone = phone, challengeId = challengeId, code = code))

    suspend fun register(phone: String, fullName: String, challengeId: String) =
        api().register(RegisterRequest(phone = phone, fullName = fullName, challengeId = challengeId))

    suspend fun login(phone: String, challengeId: String) =
        api().login(LoginRequest(phone = phone, challengeId = challengeId))

    suspend fun logout() = api().logout(authorizationHeader())

    suspend fun dashboard() = api().dashboard(authorizationHeader())

    suspend fun saveProfile(profile: CaregiverProfile) =
        api().updateProfile(profile, authorizationHeader())

    suspend fun sendInvitation(name: String, relation: String, phone: String) =
        api().sendInvitation(
            InvitationRequest(name = name, relation = relation, phone = phone),
            authorizationHeader()
        )

    suspend fun acceptInvitation(invitationId: Int) =
        api().acceptInvitation(invitationId, authorization = authorizationHeader())

    suspend fun dismissInvitation(invitationId: Int) =
        api().deleteInvitation(invitationId, authorizationHeader())

    suspend fun markNotificationRead(notificationId: Int) =
        api().markNotificationRead(notificationId, authorization = authorizationHeader())

    suspend fun markAllNotificationsRead() =
        api().markAllNotificationsRead(authorization = authorizationHeader())

    suspend fun dismissNotification(notificationId: Int) =
        api().deleteNotification(notificationId, authorizationHeader())

    suspend fun updateMemberLocation(memberId: Int, payload: UpdateLocationRequest) =
        api().updateMemberLocation(memberId, payload, authorizationHeader())

    suspend fun getMemberLocationHistory(memberId: Int) =
        api().getMemberLocationHistory(memberId, authorizationHeader())

    suspend fun getSosRoutes() = api().getSosRoutes(authorizationHeader())

    suspend fun triggerSos() = api().triggerSos(authorization = authorizationHeader())

    suspend fun resolveSos(alertId: Int) =
        api().resolveSos(alertId, authorization = authorizationHeader())

    fun saveAuthToken(token: String) = userStore.saveAuthToken(token)
    fun clearAuthToken() = userStore.clearAuthToken()
    fun loadAuthToken(): String? = userStore.loadAuthToken()

    fun saveRegistrationState(isRegistered: Boolean) = userStore.saveRegistrationState(isRegistered)
    fun loadRegistrationState(defaultValue: Boolean = false) = userStore.loadRegistrationState(defaultValue)

    fun loadProfile(default: CaregiverProfile) = userStore.loadProfile(default)
    fun saveCachedProfile(profile: CaregiverProfile) = userStore.saveProfile(profile)

    private fun authorizationHeader(): String? {
        val token = userStore.loadAuthToken()?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return if (token.contains(' ')) token else "Token $token"
    }

    companion object {
        @Volatile
        private var instance: RemoteRepository? = null
        private const val DEFAULT_TIMEOUT_SECONDS = 20L

        fun normalizeBaseUrl(rawValue: String): String {
            return normalizeBaseUrlOrNull(rawValue)
                ?: normalizeBaseUrlOrNull(BuildConfig.BACKEND_BASE_URL)
                ?: "http://10.0.2.2:8000/api/v1/"
        }

        private fun normalizeBaseUrlOrNull(rawValue: String): String? {
            val trimmedValue = rawValue.trim().ifBlank { return null }
            val withScheme = if (
                trimmedValue.startsWith("http://", ignoreCase = true) ||
                trimmedValue.startsWith("https://", ignoreCase = true)
            ) {
                trimmedValue
            } else {
                "http://$trimmedValue"
            }

            val parsedUrl = withScheme.toHttpUrlOrNull() ?: return null
            val pathSegments = parsedUrl.pathSegments.filter { it.isNotBlank() }
            val normalizedPathSegments = if (pathSegments.takeLast(2) == listOf("api", "v1")) {
                pathSegments
            } else {
                pathSegments + listOf("api", "v1")
            }

            return parsedUrl.newBuilder()
                .encodedPath("/${normalizedPathSegments.joinToString("/")}/")
                .build()
                .toString()
        }

        fun getInstance(context: Context): RemoteRepository {
            return instance ?: synchronized(this) {
                instance ?: RemoteRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
