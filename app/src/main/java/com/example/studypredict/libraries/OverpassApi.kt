package com.example.studypredict.libraries

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class LibraryPlace(
    val name: String,
    val lat: Double,
    val lon: Double
)

private val client = OkHttpClient()

suspend fun fetchLibrariesOverpass(
    lat: Double,
    lon: Double,
    radiusMeters: Int
): List<LibraryPlace> = withContext(Dispatchers.IO) {
    val query = """
        [out:json];
        (
          node["amenity"="library"](around:$radiusMeters,$lat,$lon);
          way["amenity"="library"](around:$radiusMeters,$lat,$lon);
          relation["amenity"="library"](around:$radiusMeters,$lat,$lon);
        );
        out center;
    """.trimIndent()

    val body = "data=$query".toRequestBody("application/x-www-form-urlencoded".toMediaType())

    val req = Request.Builder()
        .url("https://overpass-api.de/api/interpreter")
        .post(body)
        .build()

    val resp = client.newCall(req).execute()
    if (!resp.isSuccessful) return@withContext emptyList()

    val json = JSONObject(resp.body?.string().orEmpty())
    val elements = json.getJSONArray("elements")

    val out = mutableListOf<LibraryPlace>()
    for (i in 0 until elements.length()) {
        val el = elements.getJSONObject(i)
        val tags = el.optJSONObject("tags")
        val name = tags?.optString("name")?.takeIf { it.isNotBlank() } ?: "Bibliothèque"

        val (plat, plon) = when {
            el.has("lat") && el.has("lon") -> el.getDouble("lat") to el.getDouble("lon")
            el.has("center") -> {
                val c = el.getJSONObject("center")
                c.getDouble("lat") to c.getDouble("lon")
            }
            else -> continue
        }

        out += LibraryPlace(name = name, lat = plat, lon = plon)
    }

    out.sortedBy { it.name }
}