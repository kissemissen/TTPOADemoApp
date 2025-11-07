package com.havrebollsolutions.ttpoademoapp.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.havrebollsolutions.ttpoademoapp.data.models.AdyenConfig
import com.havrebollsolutions.ttpoademoapp.data.models.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    private val datastore: DataStore<Preferences>
) {
    // Logotype logic
    /**
     * Reads the stored logotype image URI as a String Flow.
     * The URI will be stored as a String (Uri.toString()).
     */
    val logotypeUriPath: Flow<String?> = datastore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading logotype preference", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            // Return the stored URI string, or null if not set
            preferences[LOGOTYPE_URI_PATH]
        }


    // Return a Flow of the Currency enum
    val selectedCurrency: Flow<Currency> = datastore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            // 1. Get the stored string (e.g., "EUR")
            val code = preferences[SELECTED_CURRENCY] ?: Currency.SEK.isoCode

            // 2. Convert the string to the Currency enum object.
            //    Default to SEK if the stored code is invalid/missing.
            Currency.fromIsoCode(code) ?: Currency.SEK
        }

    // Adyen configuration logic
    // Use a library like Gson or kotlinx.serialization to handle the conversion
    // (Assuming you have a serializer defined, e.g., using Gson)
    private val gson = Gson()
    // Save Adyen configuration to DataStore
    val adyenConfig: Flow<AdyenConfig?> = datastore.data
        .map { preferences ->
            val jsonString = preferences[ADYEN_CONFIG_KEY]
            if (jsonString.isNullOrBlank()) {
                null
            } else {
                gson.fromJson(jsonString, AdyenConfig::class.java)
            }
        }

    /**
     * Saves the logotype URI path (as a String) to DataStore.
     * @param uriPath The Uri.toString() of the selected logotype image, or null to clear.
     */
    suspend fun saveLogotypeUriPath(uriPath: String?) {
        datastore.edit { preferences ->
            if (uriPath.isNullOrBlank()) {
                // Clear the preference if null is passed
                preferences.remove(LOGOTYPE_URI_PATH)
            } else {
                // Save the string representation of the URI
                preferences[LOGOTYPE_URI_PATH] = uriPath
            }
        }
    }

    /**
     * Saves the selected currency to DataStore.
     * @param currency The selected Currency object.
     */
    suspend fun saveSelectedCurrency(currency: Currency) {
        datastore.edit { preferences ->
            preferences[SELECTED_CURRENCY] = currency.isoCode
        }
    }

    /**
     * Saves the Adyen configuration to DataStore.
     * @param config The AdyenConfig object to be saved.
     */
    suspend fun saveAdyenConfig(config: AdyenConfig) {
        datastore.edit { preferences ->
            val jsonString = gson.toJson(config)
            preferences[ADYEN_CONFIG_KEY] = jsonString
        }
    }


    private companion object {
        val LOGOTYPE_URI_PATH = stringPreferencesKey("logotype_uri_path")

        val SELECTED_CURRENCY = stringPreferencesKey("selected_currency")

        val ADYEN_CONFIG_KEY = stringPreferencesKey("adyen_config")

        const val TAG = "UserPreferencesRepo"
    }
}