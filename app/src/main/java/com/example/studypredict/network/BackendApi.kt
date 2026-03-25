package com.example.studypredict.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

data class RemoteBadge(
    val id: Int,
    val badgeName: String,
    val description: String?,
    val unlocked: Boolean,
    val unlockedAt: String?
)

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(val message: String, val unauthorized: Boolean = false) : ApiResult<Nothing>()
}

object BackendApi {
    private const val BASE_URL = "http://10.0.2.2:8080"
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun register(
        email: String,
        password: String,
        displayName: String?
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("email", email)
                put("password", password)
                if (!displayName.isNullOrBlank()) put("display_name", displayName)
            }

            val request = Request.Builder()
                .url("$BASE_URL/v1/auth/register")
                .post(payload.toString().toRequestBody(jsonMedia))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    ApiResult.Success(Unit)
                } else {
                    val body = response.body?.string().orEmpty()
                    ApiResult.Failure(extractError(body, "Inscription impossible."))
                }
            }
        } catch (_: Exception) {
            ApiResult.Failure("Backend indisponible. Verifiez que l'API tourne sur le port 8080.")
        }
    }

    suspend fun login(email: String, password: String): ApiResult<AuthTokens> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("email", email)
                put("password", password)
            }
            val request = Request.Builder()
                .url("$BASE_URL/v1/auth/login")
                .post(payload.toString().toRequestBody(jsonMedia))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext ApiResult.Failure(
                        message = extractError(body, "Connexion impossible."),
                        unauthorized = response.code == 401
                    )
                }

                val json = JSONObject(body)
                val accessToken = json.getString("access_token")
                val refreshToken = json.getString("refresh_token")
                ApiResult.Success(AuthTokens(accessToken = accessToken, refreshToken = refreshToken))
            }
        } catch (_: Exception) {
            ApiResult.Failure("Backend indisponible. Verifiez que l'API tourne sur le port 8080.")
        }
    }

    suspend fun predictSuccess(
        token: String,
        periodDays: Int,
        hoursWorked: Double,
        exercisesDone: Int,
        sleepHoursAvg: Double,
        attendance: Double
    ): ApiResult<Int> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("period_days", periodDays)
                put("hours_worked", hoursWorked)
                put("exercises_done", exercisesDone)
                put("sleep_hours_avg", sleepHoursAvg)
                put("attendance", attendance)
            }

            val request = Request.Builder()
                .url("$BASE_URL/v1/predict-success")
                .addHeader("Authorization", "Bearer $token")
                .post(payload.toString().toRequestBody(jsonMedia))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext ApiResult.Failure(
                        message = extractError(body, "Prediction impossible."),
                        unauthorized = response.code == 401
                    )
                }

                val json = JSONObject(body)
                ApiResult.Success(json.getInt("success_percent"))
            }
        } catch (_: Exception) {
            ApiResult.Failure("Prediction impossible. Verifiez la connexion au backend.")
        }
    }

    suspend fun logout(refreshToken: String): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply { put("refresh_token", refreshToken) }
            val request = Request.Builder()
                .url("$BASE_URL/v1/auth/logout")
                .post(payload.toString().toRequestBody(jsonMedia))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 401) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.Failure("Deconnexion impossible.")
                }
            }
        } catch (_: Exception) {
            ApiResult.Failure("Deconnexion impossible.")
        }
    }

    suspend fun getMyBadges(token: String): ApiResult<List<RemoteBadge>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/v1/badges/me")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext ApiResult.Failure(
                        message = extractError(body, "Chargement des badges impossible."),
                        unauthorized = response.code == 401
                    )
                }

                val array = org.json.JSONArray(body)
                val badges = buildList {
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        add(
                            RemoteBadge(
                                id = item.getInt("id"),
                                badgeName = item.getString("badge_name"),
                                description = item.optString("description").takeIf { it.isNotBlank() },
                                unlocked = item.getBoolean("unlocked"),
                                unlockedAt = item.optString("unlocked_at").takeIf { it.isNotBlank() }
                            )
                        )
                    }
                }
                ApiResult.Success(badges)
            }
        } catch (_: Exception) {
            ApiResult.Failure("Chargement des badges impossible. Verifiez la connexion au backend.")
        }
    }

    private fun extractError(body: String, fallback: String): String {
        return try {
            val root = JSONObject(body)
            val detail = root.optJSONObject("detail")
            detail?.optString("message")?.takeIf { it.isNotBlank() } ?: fallback
        } catch (_: Exception) {
            fallback
        }
    }
}
