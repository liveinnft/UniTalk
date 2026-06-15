package com.levonty.unitalk.ui.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.levonty.unitalk.data.model.*
import com.levonty.unitalk.ui.common.ALL_COUNTRIES
import com.levonty.unitalk.ui.common.CountryPickerScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegistered: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri }

    LaunchedEffect(state.success) {
        if (state.success) onRegistered()
    }

    if (showCountryPicker) {
        CountryPickerScreen(
            title = "Выбрать страну",
            selectedCodes = if (state.country.isNotBlank()) listOf(state.country) else emptyList(),
            onBack = { showCountryPicker = false },
            onSelect = { country ->
                viewModel.setCountry(country.code)
                showCountryPicker = false
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Создать аккаунт", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Photo
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { photoLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Фото",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp))
                            Text("Фото", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Nickname
            SectionLabel("Никнейм *")
            OutlinedTextField(
                value = state.nickname,
                onValueChange = { viewModel.setNickname(it) },
                placeholder = { Text("Как тебя называть?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )

            // Birth date
            SectionLabel("Дата рождения *")
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, null)
                Spacer(Modifier.width(8.dp))
                val dateLabel = if (state.birthDay.isNotBlank() && state.birthMonth.isNotBlank() && state.birthYear.isNotBlank())
                    "${state.birthDay} ${monthName(state.birthMonth.toIntOrNull() ?: 0)} ${state.birthYear}"
                else "Выбрать дату"
                Text(dateLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
            }

            // Gender
            SectionLabel("Пол (необязательно)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Gender.MALE to "Мужской", Gender.FEMALE to "Женский", Gender.OTHER to "Другой")
                    .forEach { (g, label) ->
                        FilterChip(
                            selected = state.gender == g,
                            onClick = { viewModel.setGender(if (state.gender == g) null else g) },
                            label = { Text(label, fontSize = 13.sp) }
                        )
                    }
            }

            // Country + City
            SectionLabel("Страна и город")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showCountryPicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    val country = ALL_COUNTRIES.find { it.code == state.country }
                    if (country != null) {
                        Text("${country.emoji} ${country.nameRu}", maxLines = 1)
                    } else {
                        Icon(Icons.Default.Public, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Страна")
                    }
                }
                OutlinedTextField(
                    value = state.city,
                    onValueChange = { viewModel.setCity(it) },
                    placeholder = { Text("Город") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Languages
            SectionLabel("Языки")
            LanguagePicker(
                selected = state.languages,
                onAdd = { viewModel.addLanguage(it) },
                onRemove = { viewModel.removeLanguage(it) }
            )

            // Interests
            SectionLabel("Интересы")
            InterestPicker(
                selected = state.interests,
                onAdd = { viewModel.addInterest(it) },
                onRemove = { viewModel.removeInterest(it) }
            )

            // Bio
            SectionLabel("О себе")
            OutlinedTextField(
                value = state.bio,
                onValueChange = { viewModel.setBio(it) },
                placeholder = { Text("Расскажи о себе...") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                supportingText = { Text("${state.bio.length}/3000") }
            )

            // Error
            state.error?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(err, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
                }
            }

            // Submit
            Button(
                onClick = { viewModel.register() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading)
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else
                    Text("Зарегистрироваться", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        WheelDatePickerDialog(
            initialDay = state.birthDay.toIntOrNull() ?: 1,
            initialMonth = state.birthMonth.toIntOrNull() ?: 1,
            initialYear = state.birthYear.toIntOrNull() ?: 2000,
            onDismiss = { showDatePicker = false },
            onConfirm = { d, m, y ->
                viewModel.setBirthDate(d.toString(), m.toString(), y.toString())
                showDatePicker = false
            }
        )
    }
}

// ─── Wheel Date Picker ────────────────────────────────────────────────────────

@Composable
fun WheelDatePickerDialog(
    initialDay: Int, initialMonth: Int, initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (day: Int, month: Int, year: Int) -> Unit
) {
    var selectedDay by remember { mutableStateOf(initialDay) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedYear by remember { mutableStateOf(initialYear) }

    val days = (1..31).toList()
    val months = (1..12).toList()
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = ((currentYear - 100)..currentYear).toList().reversed()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Дата рождения", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("День", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        WheelPicker(items = days, selectedItem = selectedDay, onItemSelected = { selectedDay = it }) { it.toString() }
                    }
                    Column(modifier = Modifier.weight(1.6f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Месяц", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        WheelPicker(items = months, selectedItem = selectedMonth, onItemSelected = { selectedMonth = it }) { monthName(it) }
                    }
                    Column(modifier = Modifier.weight(1.3f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Год", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        WheelPicker(items = years, selectedItem = selectedYear, onItemSelected = { selectedYear = it }) { it.toString() }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Отмена") }
                    Button(onClick = { onConfirm(selectedDay, selectedMonth, selectedYear) }, modifier = Modifier.weight(1f)) { Text("Готово") }
                }
            }
        }
    }
}

@Composable
fun <T> WheelPicker(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    label: (T) -> String
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = maxOf(0, items.indexOf(selectedItem) - 1)
    )
    val snapBehavior = rememberSnapFlingBehavior(listState)
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        )
        LazyColumn(
            state = listState,
            flingBehavior = snapBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            item { Spacer(Modifier.height(48.dp)) }
            items(items) { item ->
                val isSelected = item == selectedItem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable {
                            onItemSelected(item)
                            val idx = items.indexOf(item)
                            scope.launch { listState.animateScrollToItem(maxOf(0, idx - 1)) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label(item),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = if (isSelected) 16.sp else 14.sp
                    )
                }
            }
            item { Spacer(Modifier.height(48.dp)) }
        }
        LaunchedEffect(listState.firstVisibleItemIndex) {
            val idx = listState.firstVisibleItemIndex + 1
            if (idx in items.indices) onItemSelected(items[idx])
        }
    }
}

fun monthName(month: Int) = when (month) {
    1 -> "Январь"; 2 -> "Февраль"; 3 -> "Март"; 4 -> "Апрель"
    5 -> "Май"; 6 -> "Июнь"; 7 -> "Июль"; 8 -> "Август"
    9 -> "Сентябрь"; 10 -> "Октябрь"; 11 -> "Ноябрь"; 12 -> "Декабрь"
    else -> ""
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

// ─── Language Picker ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePicker(selected: List<Language>, onAdd: (Language) -> Unit, onRemove: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        selected.forEach { lang ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(lang.nameRu, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (lang.isNative) "Родной" else "${lang.level}/5",
                            color = MaterialTheme.colorScheme.primary, fontSize = 13.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { onRemove(lang.code) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(6.dp))
            Text("Добавить язык")
        }
    }
    if (showDialog) {
        AddLanguageDialog(onDismiss = { showDialog = false }, onAdd = { onAdd(it); showDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLanguageDialog(onDismiss: () -> Unit, onAdd: (Language) -> Unit) {
    val commonLanguages = remember {
        listOf(
            Language("ru", "Русский", "Russian", 5), Language("en", "Английский", "English", 5),
            Language("pl", "Польский", "Polish", 5), Language("de", "Немецкий", "German", 5),
            Language("fr", "Французский", "French", 5), Language("es", "Испанский", "Spanish", 5),
            Language("uk", "Украинский", "Ukrainian", 5), Language("tr", "Турецкий", "Turkish", 5),
            Language("zh", "Китайский", "Chinese", 5), Language("ja", "Японский", "Japanese", 5),
            Language("ko", "Корейский", "Korean", 5), Language("ar", "Арабский", "Arabic", 5)
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedLang.nameRu, onValueChange = {}, readOnly = true,
                        label = { Text("Язык") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isNative, onCheckedChange = { isNative = it })
                    Text("Родной язык")
                }
                if (!isNative) {
                    val levelLabels = listOf("Начинающий", "Базовый", "Средний", "Продвинутый", "Свободно")
                    Text("Уровень: ${levelLabels.getOrElse(level - 1) { "" }}", fontWeight = FontWeight.Medium)
                    Slider(value = level.toFloat(), onValueChange = { level = it.toInt() }, valueRange = 1f..5f, steps = 3)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(selectedLang.copy(level = if (isNative) 5 else level, isNative = isNative)) }) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

// ─── Interest Picker ──────────────────────────────────────────────────────────

@Composable
fun InterestPicker(selected: List<Interest>, onAdd: (Interest) -> Unit, onRemove: (Long) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    if (selected.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            selected.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { interest ->
                        InputChip(
                            selected = true,
                            onClick = { onRemove(interest.id) },
                            label = { Text(interest.nameRu, fontSize = 12.sp) },
                            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                        )
                    }
                }
            }
        }
    }
    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Add, null)
        Spacer(Modifier.width(6.dp))
        Text("Добавить интерес")
    }
    if (showDialog) {
        AddInterestDialog(onDismiss = { showDialog = false }, onAdd = { onAdd(it); showDialog = false })
    }
}

@Composable
fun AddInterestDialog(onDismiss: () -> Unit, onAdd: (Interest) -> Unit) {
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
            Interest(15, "Природа", "Nature", category = "Образ жизни"),
            Interest(16, "Танцы", "Dancing", category = "Искусство"),
            Interest(17, "Йога", "Yoga", category = "Спорт"),
            Interest(18, "Настольные игры", "Board games", category = "Развлечения"),
            Interest(19, "Психология", "Psychology", category = "Образование"),
            Interest(20, "Астрономия", "Astronomy", category = "Наука")
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
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("Поиск...") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    items(filtered) { interest ->
                        ListItem(
                            headlineContent = { Text(interest.nameRu) },
                            supportingContent = { Text(interest.category, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp) },
                            modifier = Modifier.clickable { onAdd(interest) }
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}