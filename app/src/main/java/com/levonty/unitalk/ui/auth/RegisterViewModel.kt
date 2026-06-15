package com.levonty.unitalk.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levonty.unitalk.data.model.*
import com.levonty.unitalk.data.repository.SessionRepository
import com.levonty.unitalk.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class RegisterState(
    val nickname: String = "",
    val birthDay: String = "",
    val birthMonth: String = "",
    val birthYear: String = "",
    val gender: Gender? = null,
    val bio: String = "",
    val country: String = "",
    val city: String = "",
    val languages: List<Language> = emptyList(),
    val interests: List<Interest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun setNickname(v: String) { _state.value = _state.value.copy(nickname = v) }
    fun setBirthDate(day: String, month: String, year: String) {
        _state.value = _state.value.copy(birthDay = day, birthMonth = month, birthYear = year)
    }
    fun setGender(g: Gender?) { _state.value = _state.value.copy(gender = g) }
    fun setCountry(code: String) { _state.value = _state.value.copy(country = code) }
    fun setCity(v: String) { _state.value = _state.value.copy(city = v) }
    fun setBio(v: String) { if (v.length <= 3000) _state.value = _state.value.copy(bio = v) }

    fun addLanguage(lang: Language) {
        _state.value = _state.value.copy(languages = _state.value.languages + lang)
    }
    fun removeLanguage(code: String) {
        _state.value = _state.value.copy(languages = _state.value.languages.filter { it.code != code })
    }
    fun addInterest(interest: Interest) {
        if (_state.value.interests.none { it.id == interest.id })
            _state.value = _state.value.copy(interests = _state.value.interests + interest)
    }
    fun removeInterest(id: Long) {
        _state.value = _state.value.copy(interests = _state.value.interests.filter { it.id != id })
    }

    fun register() {
        val s = _state.value
        if (s.nickname.isBlank()) { _state.value = s.copy(error = "Введите никнейм"); return }
        val day = s.birthDay.toIntOrNull()
        val month = s.birthMonth.toIntOrNull()
        val year = s.birthYear.toIntOrNull()
        if (day == null || month == null || year == null) {
            _state.value = s.copy(error = "Выберите дату рождения"); return
        }
        val cal = Calendar.getInstance().apply { set(year, month - 1, day) }
        val ageYears = ((System.currentTimeMillis() - cal.timeInMillis) / (365.25 * 24 * 3600 * 1000)).toInt()
        if (ageYears < 14) { _state.value = s.copy(error = "Минимальный возраст — 14 лет"); return }

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            val user = User(
                id = UUID.randomUUID().toString(),
                nickname = s.nickname.trim(),
                birthDate = cal.timeInMillis,
                gender = s.gender,
                bio = s.bio,
                country = s.country,
                city = s.city,
                languages = s.languages,
                interests = s.interests
            )
            userRepo.saveUser(user)
            sessionRepo.login(user.id)
            _state.value = _state.value.copy(isLoading = false, success = true)
        }
    }
}