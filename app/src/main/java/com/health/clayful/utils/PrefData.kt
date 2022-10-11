package com.health.clayful.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.*
import com.google.gson.Gson
import com.health.clayful.model.UserModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

class PrefData(context: Context) : AppCompatActivity() {

    lateinit var userIdFlow : Flow<String>
    lateinit var tokenFlow : Flow<String>
    lateinit var loginStatusFlow : Flow<Boolean>
    lateinit var zendeskStatusFlow : Flow<Boolean>
    lateinit var jwtTokenFlow : Flow<String>

    private var authTokenPrefKey : Preferences.Key<String> = preferencesKey("LOGIN_AUTH_TOKEN")
    private var userIdPrefKey : Preferences.Key<String> = preferencesKey("USER_ID")
    private var loginStatusPrefKey : Preferences.Key<Boolean> = preferencesKey("LOGIN_STATUS")
    private var zendeskLoginStatusKey : Preferences.Key<Boolean> = preferencesKey("ZENDESK_LOGIN_STATUS")
    private var jwtTokenKey : Preferences.Key<String> = preferencesKey("JWT_TOKEN")
    private var loginDataPrefKey : Preferences.Key<String> = preferencesKey("USER_LOGIN_STATUS")
    private var firebaseTokenPrefKey : Preferences.Key<String> = preferencesKey("FIREBASE_TOKEN")

    private val dataStore  by lazy {
        context.createDataStore("SmartnerDataStore")
    }

    //saving user auth token
    suspend fun saveToken(token: String) {
        dataStore.edit {
            it[authTokenPrefKey] = token
        }
    }

    suspend fun saveUserId(userId : String) {
        dataStore.edit {
            it[userIdPrefKey] = userId
        }
    }

    // saving user login Status
    suspend fun saveUserLoginStatus(status : Boolean) {
        dataStore.edit {
            it[loginStatusPrefKey] = status
        }
    }

    //zendesk login Status
    suspend fun saveZendeskLoginStatus(status: Boolean) {
        dataStore.edit {
            it[zendeskLoginStatusKey] = status
        }
    }

    // save jwt token
    suspend fun saveJwtToken(jwtToken: String) {
        dataStore.edit {
            it[jwtTokenKey] = jwtToken
        }
    }

    //saving login data to preference
    suspend fun saveUserLoginData(value: UserModel) {

        dataStore.edit {
            val jsonString = Gson().toJson(value)
            it[loginDataPrefKey] = jsonString
        }
    }

    //read user login status
    fun getLoginStatus() {

        loginStatusFlow = dataStore.data
            .catch {
                if(it is Exception) {
                    emit(emptyPreferences()).toString()
                }
            }
            .map { preference ->
                val data = preference[loginStatusPrefKey] ?: false
                data
            }
    }

    fun getZendeskLoginStatus() {
        zendeskStatusFlow = dataStore.data
            .catch {
                if(it is Exception) {
                    emit(emptyPreferences()).toString()
                }
            }
            .map { preference ->
                val data = preference[zendeskLoginStatusKey] ?: false
                data
            }
    }

    // read user login Token
    fun readToken() {
        tokenFlow = dataStore.data
            .catch {
                if(it is Exception) {
                    emit(emptyPreferences()).toString()
                }
            }
            .map { preference ->
                val data = preference[authTokenPrefKey] ?: ""
                data
            }
    }

    // read user id
    fun readUserId() {
        userIdFlow = dataStore.data
            .catch {
                if(it is Exception) {
                    emit(emptyPreferences()).toString()
                }
            }
            .map { preference ->
                val data = preference[userIdPrefKey] ?: ""
                data
            }
    }

    // get jwt token

    fun getJwtToken() {
        jwtTokenFlow = dataStore.data
            .catch {
                if(it is Exception) {
                    emit(emptyPreferences()).toString()
                }
            }
            .map { preference ->
                val data = preference[jwtTokenKey] ?: ""
                data
            }
    }

    suspend fun storeFirebaseToken(token: String) {
        dataStore.edit {
            it[firebaseTokenPrefKey] = token
        }
    }

    //to read login data from pref
    val readLoginData = dataStore.data
        .catch {
            if(it is Exception) {
                emit(emptyPreferences()).toString()
            }
        }
        .map { preference ->
            val data = preference[loginDataPrefKey]
            data
        }
}