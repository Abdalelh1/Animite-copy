package com.imashnake.animite.social.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imashnake.animite.social.data.AuthRepository
import com.imashnake.animite.social.model.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Loading)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isUsernameAvailable = MutableStateFlow<Boolean?>(null)
    val isUsernameAvailable: StateFlow<Boolean?> = _isUsernameAvailable.asStateFlow()

    private var checkUsernameJob: Job? = null

    fun checkUsername(username: String) {
        checkUsernameJob?.cancel()
        val clean = username.trim().lowercase(Locale.ROOT)
        if (clean.length < 3) {
            _isUsernameAvailable.value = null
            return
        }
        checkUsernameJob = viewModelScope.launch(Dispatchers.IO) {
            delay(350)
            val available = runCatching {
                authRepository.isUsernameAvailable(clean)
            }.getOrDefault(true)
            _isUsernameAvailable.value = available
        }
    }

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("يرجى إدخال البريد الإلكتروني وكلمة المرور")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = AuthUiState.Loading
            runCatching {
                authRepository.signInWithEmail(email, pass)
            }.onSuccess { result ->
                result.onSuccess { _uiState.value = AuthUiState.Success }
                    .onFailure { _uiState.value = AuthUiState.Error(formatAuthError(it)) }
            }.onFailure {
                _uiState.value = AuthUiState.Error(formatAuthError(it))
            }
        }
    }

    fun signUp(email: String, pass: String, username: String, displayName: String) {
        if (email.isBlank() || pass.isBlank() || username.isBlank()) {
            _uiState.value = AuthUiState.Error("يرجى ملء جميع الحقول المطلوبة")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = AuthUiState.Loading
            runCatching {
                authRepository.signUpWithEmail(email, pass, username, displayName)
            }.onSuccess { result ->
                result.onSuccess { _uiState.value = AuthUiState.Success }
                    .onFailure { _uiState.value = AuthUiState.Error(formatAuthError(it)) }
            }.onFailure {
                _uiState.value = AuthUiState.Error(formatAuthError(it))
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = AuthUiState.Loading
            runCatching {
                authRepository.signInWithGoogle(idToken)
            }.onSuccess { result ->
                result.onSuccess { _uiState.value = AuthUiState.Success }
                    .onFailure { _uiState.value = AuthUiState.Error(formatAuthError(it)) }
            }.onFailure {
                _uiState.value = AuthUiState.Error(formatAuthError(it))
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("يرجى إدخال بريدك الإلكتروني لإعادة تعيين كلمة المرور")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = AuthUiState.Loading
            runCatching {
                authRepository.sendPasswordResetEmail(email)
            }.onSuccess { result ->
                result.onSuccess { _uiState.value = AuthUiState.Message("تم إرسال رابط إعادة تعيين كلمة المرور إلى بريدك الإلكتروني.") }
                    .onFailure { _uiState.value = AuthUiState.Error(formatAuthError(it)) }
            }.onFailure {
                _uiState.value = AuthUiState.Error(formatAuthError(it))
            }
        }
    }

    fun setErrorMessage(error: String) {
        _uiState.value = AuthUiState.Error(error)
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }

    fun signOut() {
        authRepository.signOut()
    }

    private fun formatAuthError(throwable: Throwable): String {
        val message = throwable.localizedMessage ?: throwable.message.orEmpty()
        return when {
            message.contains("user-not-found", ignoreCase = true) ||
                message.contains("no user record", ignoreCase = true) ->
                "لم يتم العثور على حساب مسجل بهذا البريد الإلكتروني."
            message.contains("wrong-password", ignoreCase = true) ||
                message.contains("invalid-credential", ignoreCase = true) ->
                "البريد الإلكتروني أو كلمة المرور غير صحيحة."
            message.contains("email-already-in-use", ignoreCase = true) ->
                "البريد الإلكتروني مستخدم بالفعل بحساب آخر."
            message.contains("invalid-email", ignoreCase = true) ->
                "صيغة البريد الإلكتروني غير صالحة."
            message.contains("weak-password", ignoreCase = true) ->
                "كلمة المرور ضعيفة جداً (يجب أن تتكون من 8 أحرف على الأقل)."
            message.contains("network-request-failed", ignoreCase = true) ||
                message.contains("offline", ignoreCase = true) ||
                message.contains("unable to resolve host", ignoreCase = true) ->
                "تعذر الاتصال بالخادم. يرجى التحقق من اتصالك بالإنترنت."
            message.contains("too-many-requests", ignoreCase = true) ->
                "تم حظر المحاولات مؤقتاً بسبب كثرة الطلبات. يرجى المحاولة لاحقاً."
            message.isNotBlank() -> message
            else -> "حدث خطأ أثناء تنفيذ العملية."
        }
    }
}

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Message(val message: String) : AuthUiState
    data class Error(val error: String) : AuthUiState
}
