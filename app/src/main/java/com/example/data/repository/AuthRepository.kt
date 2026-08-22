package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.authDataStore by preferencesDataStore(name = "nova_auth_prefs")

data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String,
    val isGuest: Boolean = false,
    val avatarUrl: String? = null,
    val planType: String = "Pro Enterprise"
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    data class Guest(val user: AuthUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthRepository(private val context: Context) {

    private val KEY_IS_GUEST = booleanPreferencesKey("is_guest")
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    private val KEY_USER_NAME = stringPreferencesKey("user_name")
    private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        // Initialize default user state
        loadInitialUser()
    }

    private fun loadInitialUser() {
        // Default to a rich authenticated Cosmic Explorer session or Guest
        val defaultUser = AuthUser(
            id = "user_cosmic_01",
            email = "explorer@nova.ai",
            displayName = "Cosmic Explorer",
            isGuest = false,
            planType = "Pro Studio"
        )
        _currentUser.value = defaultUser
        _authState.value = AuthState.Authenticated(defaultUser)
    }

    suspend fun login(email: String, pass: String): Result<AuthUser> {
        _authState.value = AuthState.Loading
        if (email.isBlank() || !email.contains("@")) {
            val err = "Please provide a valid email address"
            _authState.value = AuthState.Error(err)
            return Result.failure(IllegalArgumentException(err))
        }
        if (pass.length < 6) {
            val err = "Password must be at least 6 characters"
            _authState.value = AuthState.Error(err)
            return Result.failure(IllegalArgumentException(err))
        }

        val name = email.substringBefore("@").replace(".", " ").capitalizeWords()
        val user = AuthUser(
            id = UUID.randomUUID().toString(),
            email = email,
            displayName = name,
            isGuest = false,
            planType = "Pro Studio"
        )

        context.authDataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_IS_GUEST] = false
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USER_EMAIL] = user.email
            prefs[KEY_USER_NAME] = user.displayName
        }

        _currentUser.value = user
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    suspend fun register(name: String, email: String, pass: String): Result<AuthUser> {
        _authState.value = AuthState.Loading
        if (name.isBlank()) {
            val err = "Please enter your full name"
            _authState.value = AuthState.Error(err)
            return Result.failure(IllegalArgumentException(err))
        }
        if (email.isBlank() || !email.contains("@")) {
            val err = "Please enter a valid email address"
            _authState.value = AuthState.Error(err)
            return Result.failure(IllegalArgumentException(err))
        }
        if (pass.length < 6) {
            val err = "Password must be at least 6 characters"
            _authState.value = AuthState.Error(err)
            return Result.failure(IllegalArgumentException(err))
        }

        val user = AuthUser(
            id = UUID.randomUUID().toString(),
            email = email,
            displayName = name,
            isGuest = false,
            planType = "Pro Studio"
        )

        context.authDataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_IS_GUEST] = false
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USER_EMAIL] = user.email
            prefs[KEY_USER_NAME] = user.displayName
        }

        _currentUser.value = user
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    suspend fun continueAsGuest(): AuthUser {
        val guestUser = AuthUser(
            id = "guest_" + UUID.randomUUID().toString().take(8),
            email = "guest@nova.ai",
            displayName = "Guest Creator",
            isGuest = true,
            planType = "Guest Trial"
        )

        context.authDataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs[KEY_IS_GUEST] = true
            prefs[KEY_USER_ID] = guestUser.id
            prefs[KEY_USER_EMAIL] = guestUser.email
            prefs[KEY_USER_NAME] = guestUser.displayName
        }

        _currentUser.value = guestUser
        _authState.value = AuthState.Guest(guestUser)
        return guestUser
    }

    suspend fun resetPassword(email: String): Result<String> {
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        return Result.success("Password reset instructions sent to $email. Please check your inbox.")
    }

    suspend fun logout() {
        val guestUser = AuthUser(
            id = "guest_" + UUID.randomUUID().toString().take(8),
            email = "guest@nova.ai",
            displayName = "Guest Creator",
            isGuest = true,
            planType = "Guest Trial"
        )

        context.authDataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs[KEY_IS_GUEST] = true
            prefs[KEY_USER_ID] = guestUser.id
            prefs[KEY_USER_EMAIL] = guestUser.email
            prefs[KEY_USER_NAME] = guestUser.displayName
        }

        _currentUser.value = guestUser
        _authState.value = AuthState.Guest(guestUser)
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
