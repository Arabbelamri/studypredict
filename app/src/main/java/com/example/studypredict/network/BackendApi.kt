package com.example.studypredict.network

import android.os.Build
import com.example.studypredict.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
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

data class RemotePrediction(
    val id: Int,
    val userId: Int,
    val predictedScore: Double,
    val hoursStudied: Double,
    val attendance: Double,
    val grade: String?,
    val createdAt: String
)

data class RemoteTip(
    val id: Int,
    val title: String,
    val description: String,
    val category: String
)

data class RemoteNote(
    val id: Int,
    val noteType: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val audioUrl: String?
)

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(val message: String, val unauthorized: Boolean = false) : ApiResult<Nothing>()
}

private const val CUSTOM_BACKEND_URL = BuildConfig.CUSTOM_BACKEND_URL

object BackendApi {
    private val BASE_URL: String
        get() {
            val configured = CUSTOM_BACKEND_URL.trim().trimEnd('/')
            if (configured.isNotBlank()) return configured
            return if (isProbablyEmulator()) {
                // Android Emulator -> host machine loopback
                "http://10.0.2.2:8080"
            } else {
                // Physical device default (can still use adb reverse)
                "http://127.0.0.1:8080"
            }
        }

    private fun isProbablyEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
            Build.MODEL.contains("Emulator", ignoreCase = true) ||
            Build.MODEL.contains("Android SDK built for", ignoreCase = true) ||
            Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
            Build.PRODUCT.contains("sdk", ignoreCase = true) ||
            Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
            Build.HARDWARE.contains("ranchu", ignoreCase = true)
    }
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
        attendance: Double,
        previousScores: Double,
        tutoringSessions: Int,
        physicalActivity: Double,
        extracurricularActivities: Boolean
    ): ApiResult<Int> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("period_days", periodDays)
                put("hours_worked", hoursWorked)
                put("exercises_done", exercisesDone)
                put("sleep_hours_avg", sleepHoursAvg)
                put("attendance", attendance)
                put("previous_scores", previousScores)
                put("tutoring_sessions", tutoringSessions)
                put("physical_activity", physicalActivity)
                put("extracurricular_activities", extracurricularActivities)
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

                val array = JSONArray(body)
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

    suspend fun getPredictions(token: String): ApiResult<List<RemotePrediction>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/v1/predictions")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext ApiResult.Failure(
                        message = extractError(body, "Chargement de l'historique impossible."),
                        unauthorized = response.code == 401
                    )
                }

                val array = JSONArray(body)
                val predictions = buildList {
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        add(
                            RemotePrediction(
                                id = item.getInt("id"),
                                userId = item.getInt("user_id"),
                                predictedScore = item.getDouble("predicted_score"),
                                hoursStudied = item.getDouble("hours_studied"),
                                attendance = item.getDouble("attendance"),
                                grade = item.optString("grade").takeIf { it.isNotBlank() },
                                createdAt = item.getString("created_at")
                            )
                        )
                    }
                }
                ApiResult.Success(predictions)
            }
        } catch (_: Exception) {
            ApiResult.Failure("Chargement de l'historique impossible. Verifiez la connexion au backend.")
        }
    }

    suspend fun getLatestTips(token: String): ApiResult<List<RemoteTip>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/v1/tips/me/latest")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext ApiResult.Failure(
                        message = extractError(body, "Chargement des conseils impossible."),
                        unauthorized = response.code == 401
                    )
                }

                val array = JSONArray(body)
                val tips = buildList {
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        add(
                            RemoteTip(
                                id = item.getInt("id"),
                                title = item.getString("title"),
                                description = item.getString("description"),
                                category = item.getString("category")
                            )
                        )
                    }
                }
                ApiResult.Success(tips)
            }
        } catch (_: Exception) {
            ApiResult.Failure("Chargement des conseils impossible. Verifiez la connexion au backend.")
        }
    }

    suspend fun getMyNotes(token: String): ApiResult<List<RemoteNote>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/v1/notes/me")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext ApiResult.Failure(
                        message = extractError(body, "Chargement des notes impossible."),
                        unauthorized = response.code == 401
                    )
                }

                val array = JSONArray(body)
                val notes = buildList {
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        add(
                            RemoteNote(
                                id = item.getInt("id"),
                                noteType = item.getString("note_type"),
                                title = item.getString("title"),
                                content = item.getString("content"),
                                createdAt = item.getString("created_at"),
                                updatedAt = item.getString("updated_at"),
                                audioUrl = item.optString("audio_url").takeIf { it.isNotBlank() }
                            )
                        )
                    }
                }
                ApiResult.Success(notes)
            }
        } catch (_: Exception) {
            ApiResult.Failure("Chargement des notes impossible. Verifiez la connexion au backend.")
        }
    }

    suspend fun createNote(
        token: String,
        title: String,
        content: String,
        noteType: String = "text"
    ): ApiResult<RemoteNote> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("note_type", noteType)
                put("title", title)
                put("content", content)
            }

            val request = Request.Builder()
                .url("$BASE_URL/v1/notes")
                .addHeader("Authorization", "Bearer $token")
                .post(payload.toString().toRequestBody(jsonMedia))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext ApiResult.Failure(
                        message = extractError(body, "Creation de la note impossible."),
                        unauthorized = response.code == 401
                    )
                }

                val item = JSONObject(body)
                ApiResult.Success(
                    RemoteNote(
                        id = item.getInt("id"),
                        noteType = item.getString("note_type"),
                        title = item.getString("title"),
                        content = item.getString("content"),
                        createdAt = item.getString("created_at"),
                        updatedAt = item.getString("updated_at"),
                        audioUrl = item.optString("audio_url").takeIf { it.isNotBlank() }
                    )
                )
            }
        } catch (_: Exception) {
            ApiResult.Failure("Creation de la note impossible. Verifiez la connexion au backend.")
        }
    }

    suspend fun createVoiceNote(
        token: String,
        title: String,
        content: String?,
        audioFilePath: String
    ): ApiResult<RemoteNote> = withContext(Dispatchers.IO) {
        try {
            val file = File(audioFilePath)
            if (!file.exists()) {
                return@withContext ApiResult.Failure("Enregistrement audio introuvable.")
            }

            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("note_type", "voice")
                .addFormDataPart("title", title)
            if (!content.isNullOrBlank()) {
                builder.addFormDataPart("content", content)
            }
            builder.addFormDataPart(
                "audio_file",
                file.name,
                file.asRequestBody("audio/m4a".toMediaTypeOrNull())
            )

            val request = Request.Builder()
                .url("$BASE_URL/v1/notes/voice")
                .addHeader("Authorization", "Bearer $token")
                .post(builder.build())
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext ApiResult.Failure(
                        message = extractError(body, "Creation du mémo vocal impossible."),
                        unauthorized = response.code == 401
                    )
                }

                val item = JSONObject(body)
                ApiResult.Success(
                    RemoteNote(
                        id = item.getInt("id"),
                        noteType = item.getString("note_type"),
                        title = item.getString("title"),
                        content = item.getString("content"),
                        createdAt = item.getString("created_at"),
                        updatedAt = item.getString("updated_at"),
                        audioUrl = item.optString("audio_url").takeIf { it.isNotBlank() }
                    )
                )
            }
        } catch (_: Exception) {
            ApiResult.Failure("Creation du mémo vocal impossible. Verifiez la connexion au backend.")
        }
    }

    suspend fun deleteNote(token: String, noteId: Int): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/v1/notes/$noteId")
                .addHeader("Authorization", "Bearer $token")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    ApiResult.Success(Unit)
                } else {
                    val body = response.body?.string().orEmpty()
                    ApiResult.Failure(
                        message = extractError(body, "Suppression de la note impossible."),
                        unauthorized = response.code == 401
                    )
                }
            }
        } catch (_: Exception) {
            ApiResult.Failure("Suppression de la note impossible. Verifiez la connexion au backend.")
        }
    }

    private fun extractError(body: String, fallback: String): String {
        return try {
            val root = JSONObject(body)
            val detailObject = root.optJSONObject("detail")
            if (detailObject != null) {
                return detailObject.optString("message").takeIf { it.isNotBlank() } ?: fallback
            }

            val detailArray = root.optJSONArray("detail")
            if (detailArray != null && detailArray.length() > 0) {
                val first = detailArray.optJSONObject(0)
                val msg = first?.optString("msg").orEmpty()
                if (msg.isNotBlank()) return msg
            }
            fallback
        } catch (_: Exception) {
            fallback
        }
    }
}
