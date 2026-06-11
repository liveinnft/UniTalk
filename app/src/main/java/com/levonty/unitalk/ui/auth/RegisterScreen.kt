package com.levonty.unitalk.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

// ─── ViewModel ───────────────────────────────────────────────────────────────

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

    fun update(block: RegisterState.() -> RegisterState) {
        _state.value = _state.value.block()
    }

    fun addLanguage(lang: Language) {
        _state.value = _state.value.copy(languages = _state.value.languages + lang)
    }

    fun removeLanguage(code: String) {
        _state.value = _state.value.copy(languages = _state.value.languages.filter { it.code != code })
    }

    fun addInterest(interest: Interest) {
        if (_state.value.interests.none { it.id == interest.id }) {
            _state.value = _state.value.copy(interests = _state.value.interests + interest)
        }
    }

    fun removeInterest(id: Long) {
        _state.value = _state.value.copy(interests = _state.value.interests.filter { it.id != id })
    }

    fun register() {
        val s = _state.value
        if (s.nickname.isBlank()) {
            _state.value = s.copy(error = "Введите никнейм")
            return
        }
        val day = s.birthDay.toIntOrNull()
        val month = s.birthMonth.toIntOrNull()
        val year = s.birthYear.toIntOrNull()
        if (day == null || month == null || year == null) {
            _state.value = s.copy(error = "Введите корректную дату рождения")
            return
        }
        val cal = Calendar.getInstance().apply { set(year, month - 1, day) }
        val ageMs = System.currentTimeMillis() - cal.timeInMillis
        val ageYears = (ageMs / (365.25 * 24 * 3600 * 1000)).toInt()
        if (ageYears < 14) {
            _state.value = s.copy(error = "Минимальный возраст — 14 лет")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            val user = User(
                id = UUID.randomUUID().toString(),
                nickname = s.nickname.trim(),
                birthDate = cal.timeInMillis,
                gender = s.gender,
                bio = s.bio.take(3000),
                country = s.country.trim().uppercase(),
                city = s.city.trim(),
                languages = s.languages,
                interests = s.interests
            )
            userRepo.saveUser(user)
            sessionRepo.login(user.id)
            _state.value = _state.value.copy(isLoading = false, success = true)
        }
    }
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegistered: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) onRegistered()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("UniTalk — Регистрация") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Nickname
            OutlinedTextField(
                value = state.nickname,
                onValueChange = { viewModel.update { copy(nickname = it) } },
                label = { Text("Никнейм *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Birth date row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.birthDay,
                    onValueChange = { if (it.length <= 2) viewModel.update { copy(birthDay = it) } },
                    label = { Text("День") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.birthMonth,
                    onValueChange = { if (it.length <= 2) viewModel.update { copy(birthMonth = it) } },
                    label = { Text("Месяц") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.birthYear,
                    onValueChange = { if (it.length <= 4) viewModel.update { copy(birthYear = it) } },
                    label = { Text("Год") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.5f)
                )
            }

            // Gender
            GenderSelector(
                selected = state.gender,
                onSelect = { viewModel.update { copy(gender = it) } }
            )

            // Country / City
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.country,
                    onValueChange = { viewModel.update { copy(country = it) } },
                    label = { Text("Страна (RU/PL/DE...)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.city,
                    onValueChange = { viewModel.update { copy(city = it) } },
                    label = { Text("Город") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Bio
            OutlinedTextField(
                value = state.bio,
                onValueChange = { if (it.length <= 3000) viewModel.update { copy(bio = it) } },
                label = { Text("О себе (до 3000 символов)") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("${state.bio.length}/3000") }
            )

            // Languages
            LanguagePicker(
                selected = state.languages,
                onAdd = { viewModel.addLanguage(it) },
                onRemove = { viewModel.removeLanguage(it) }
            )

            // Interests
            InterestPicker(
                selected = state.interests,
                onAdd = { viewModel.addInterest(it) },
                onRemove = { viewModel.removeInterest(it) }
            )

            // Error
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Submit
            Button(
                onClick = { viewModel.register() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Зарегистрироваться")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Sub-composables ─────────────────────────────────────────────────────────

@Composable
private fun GenderSelector(selected: Gender?, onSelect: (Gender?) -> Unit) {
    Text("Пол (необязательно)", style = MaterialTheme.typography.labelMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(Gender.MALE to "Муж", Gender.FEMALE to "Жен", Gender.OTHER to "Другой").forEach { (g, label) ->
            FilterChip(
                selected = selected == g,
                onClick = { onSelect(if (selected == g) null else g) },
                label = { Text(label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePicker(
    selected: List<Language>,
    onAdd: (Language) -> Unit,
    onRemove: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Text("Языки", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

    selected.forEach { lang ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val levelLabel = if (lang.isNative) "Родной" else "${lang.level}/5"
            Text("${lang.nameRu} ($levelLabel)")
            IconButton(onClick = { onRemove(lang.code) }) {
                Icon(Icons.Default.Close, contentDescription = "Удалить")
            }
        }
    }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text("Добавить язык")
    }

    if (showDialog) {
        AddLanguageDialog(
            onDismiss = { showDialog = false },
            onAdd = { onAdd(it); showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLanguageDialog(onDismiss: () -> Unit, onAdd: (Language) -> Unit) {
    val commonLanguages = remember {
        listOf(
            Language("ru", "Русский", "Russian", 5, false),
            Language("en", "Английский", "English", 5, false),
            Language("pl", "Польский", "Polish", 5, false),
            Language("de", "Немецкий", "German", 5, false),
            Language("fr", "Французский", "French", 5, false),
            Language("es", "Испанский", "Spanish", 5, false),
            Language("uk", "Украинский", "Ukrainian", 5, false),
            Language("tr", "Турецкий", "Turkish", 5, false)
        )
    }
    var selectedLang by remember { mutableStateOf(commonLanguages[0]) }
    var level by remember { mutableStateOf(3) }
    var isNative by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить язык") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedLang.nameRu,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Язык") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        commonLanguages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.nameRu) },
                                onClick = { selectedLang = lang; expanded = false }
                            )
                        }
                    }
                }

                if (!isNative) {
                    Text("Уровень: $level/5")
                    Slider(
                        value = level.toFloat(),
                        onValueChange = { level = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isNative, onCheckedChange = { isNative = it })
                    Text("Родной язык")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAdd(selectedLang.copy(level = if (isNative) 5 else level, isNative = isNative))
            }) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun InterestPicker(
    selected: List<Interest>,
    onAdd: (Interest) -> Unit,
    onRemove: (Long) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Text("Интересы", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

    // Chips
    com.google.accompanist.flowlayout.FlowRow is not available, use a simple wrapping layout:
    selected.chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.forEach { interest ->
                InputChip(
                    selected = true,
                    onClick = { onRemove(interest.id) },
                    label = { Text(interest.nameRu) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, Modifier.size(16.dp)) }
                )
            }
        }
    }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Add, null)
        Spacer(Modifier.width(4.dp))
        Text("Добавить интерес")
    }

    if (showDialog) {
        AddInterestDialog(
            onDismiss = { showDialog = false },
            onAdd = { onAdd(it); showDialog = false }
        )
    }
}

@Composable
private fun AddInterestDialog(onDismiss: () -> Unit, onAdd: (Interest) -> Unit) {
    val catalogue = remember {
        listOf(
            Interest(1, "Программирование", "Programming", category = "Технологии"),
            Interest(2, "Музыка", "Music", category = "Искусство"),
            Interest(3, "Кино", "Movies", category = "Развлечения"),
            Interest(4, "Путешествия", "Traveling", category = "Образ жизни"),
            Interest(5, "Игры", "Gaming", category = "Развлечения"),
            Interest(6, "Чтение", "Reading", category = "Образование"),
            Interest(7, "Спорт", "Sports", category = "Спорт"),
            Interest(8, "Кулинария", "Cooking", category = "Образ жизни"),
            Interest(9, "Аниме", "Anime", category = "Развлечения"),
            Interest(10, "Языки", "Languages", category = "Образование"),
            Interest(11, "Фотография", "Photography", category = "Искусство"),
            Interest(12, "Рисование", "Drawing", category = "Искусство"),
            Interest(13, "Наука", "Science", category = "Образование"),
            Interest(14, "История", "History", category = "Образование"),
            Interest(15, "Природа", "Nature", category = "Образ жизни")
        )
    }
    var query by remember { mutableStateOf("") }
    val filtered = catalogue.filter { it.nameRu.contains(query, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выбрать интерес") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Поиск...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                filtered.take(20).forEach { interest ->
                    ListItem(
                        headlineContent = { Text(interest.nameRu) },
                        supportingContent = { Text(interest.category) },
                        modifier = Modifier.clickable { onAdd(interest) }
                    )
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}