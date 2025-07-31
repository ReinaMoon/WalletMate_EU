package com.yourdomain.walletmateeu.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore 인스턴스를 앱 컨텍스트에 위임하여 싱글톤으로 관리
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 저장된 통화 값을 읽어오는 Flow
    val currency: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENCY_KEY] ?: "EUR" // 기본값은 EUR
        }

    // 통화 값을 저장하는 함수
    suspend fun saveCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_KEY] = currency
        }
    }

    // 키를 관리하는 private object
    private object PreferencesKeys {
        val CURRENCY_KEY = stringPreferencesKey("currency_key")
    }
}