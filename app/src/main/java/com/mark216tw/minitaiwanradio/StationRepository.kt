package com.mark216tw.minitaiwanradio

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class StationRepository(private val context: Context) {
    fun loadFavoriteIds(): List<String> = context
        .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        .getString(FAVORITE_IDS_KEY, null)
        ?.let { encoded ->
            runCatching {
                val array = JSONArray(encoded)
                List(array.length()) { index -> array.getString(index) }
            }.getOrDefault(emptyList())
        }
        .orEmpty()

    fun saveFavoriteIds(ids: List<String>) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(FAVORITE_IDS_KEY, JSONArray(ids).toString())
            .apply()
    }

    suspend fun load(): List<Station> = withContext(Dispatchers.IO) {
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        preferences.getString(CACHE_KEY, null)
            ?.let { cached -> runCatching { parse(cached) }.getOrNull() }
            ?.takeIf { it.isNotEmpty() }
            ?.let { return@withContext it }

        runCatching { parseAssets() }.getOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?.let { return@withContext it }

        val content = download()
        val stations = parse(content)
        preferences.edit().putString(CACHE_KEY, content).apply()
        stations
    }

    suspend fun refreshRemote(): Result<List<Station>> = withContext(Dispatchers.IO) {
        runCatching {
            val content = download()
            val stations = parse(content)
            context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putString(CACHE_KEY, content)
                .apply()
            stations
        }
    }

    private fun download(): String {
        val connection = URL(DATA_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = 8_000
        connection.readTimeout = 8_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "mini-taiwan-radio/0.1")
        return connection.inputStream.bufferedReader().use { it.readText() }
            .also { connection.disconnect() }
    }

    private fun parseAssets(): List<Station> = context.assets.open("stations.json")
        .bufferedReader()
        .use { parse(it.readText()) }

    private fun parse(content: String): List<Station> {
        val stations = JSONObject(content).getJSONArray("stations")
        return buildList {
            for (index in 0 until stations.length()) {
                val item = stations.getJSONObject(index)
                if (item.optBoolean("noshow", false)) continue
                add(
                    Station(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        network = item.optString("network"),
                        frequency = item.optString("frequency"),
                        region = item.optString("region"),
                        streamUrl = item.optString("streamUrl"),
                        websiteUrl = item.optString("websiteUrl"),
                        logoUrl = item.optString("logoUrl"),
                        enabled = item.optBoolean("enabled", true),
                    ),
                )
            }
        }
    }

    private companion object {
        const val DATA_URL = "https://mark216tw.github.io/mini-taiwan-radio/data/stations.v1.json"
        const val PREFERENCES = "station_cache"
        const val CACHE_KEY = "stations_json"
        const val FAVORITE_IDS_KEY = "favorite_station_ids"
    }
}
