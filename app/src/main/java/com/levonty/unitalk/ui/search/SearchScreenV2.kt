package com.levonty.unitalk.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.levonty.unitalk.data.model.User
import com.levonty.unitalk.data.repository.SessionRepository
import com.levonty.unitalk.data.repository.UserRepository
import com.levonty.unitalk.ui.common.ALL_COUNTRIES
import com.levonty.unitalk.ui.common.CountryPickerScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── ViewModel ────────────────────────────────────────────────────────────────

data class SearchFilters(
    val ageFrom: Int = 14,
    val ageTo: Int = 99,
    val includeCountries: List<String> = emptyList(),
    val excludeCountries: List<String> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    var query by mutableStateOf("")
    var filters by mutableStateOf(SearchFilters())
    var currentUser by mutableStateOf<User?>(null)

    val users: StateFlow<List<User>> = userRepo.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            sessionRepo.currentUserId.firstOrNull()?.let { uid ->
                currentUser = userRepo.getUserById(uid)
            }
        }
    }

    val filteredUsers: List<User>
        @androidx.compose.runtime.Composable
        get() {
            val all by users.collectAsState()
            val me = currentUser ?: return emptyList()
            return all.filter { u ->
                if (u.id == me.id) return@filter false
                if (u.age < filters.ageFrom || u.age > filters.ageTo) return@filter false
                if (filters.includeCountries.isNotEmpty() && u.country !in filters.includeCountries) return@filter false
                if (u.country in filters.excludeCountries) return@filter false
                if (query.isNotBlank()) {
                    val q = query.lowercase()
                    u.nickname.lowercase().contains(q) || u.bio.lowercase().contains(q) ||
                            u.interests.any { it.nameRu.lowercase().contains(q) }
                } else true
            }
        }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onUserClick: (String) -> Unit,
    onChatsClick: () -> Unit,
    onProfileClick: (String) -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }
    var showIncludeCountryPicker by remember { mutableStateOf(false) }
    var showExcludeCountryPicker by remember { mutableStateOf(false) }

    val filteredUsers = viewModel.filteredUsers

    // Country picker overlays
    if (showIncludeCountryPicker) {
        CountryPickerScreen(
            title = "Показывать из...",
            selectedCodes = viewModel.filters.includeCountries,
            multiSelect = true,
            onBack = { showIncludeCountryPicker = false },
            onSelect = { country ->
                val current = viewModel.filters.includeCountries.toMutableList()
                if (country.code in current) current.remove(country.code) else current.add(country.code)
                viewModel.filters = viewModel.filters.copy(includeCountries = current)
            }
        )
        return
    }

    if (showExcludeCountryPicker) {
        CountryPickerScreen(
            title = "Не показывать из...",
            selectedCodes = viewModel.filters.excludeCountries,
            multiSelect = true,
            onBack = { showExcludeCountryPicker = false },
            onSelect = { country ->
                val current = viewModel.filters.excludeCountries.toMutableList()
                if (country.code in current) current.remove(country.code) else current.add(country.code)
                viewModel.filters = viewModel.filters.copy(excludeCountries = current)
            }
        )
        return
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // ── Search bar + filter button ─────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.query,
                        onValueChange = { viewModel.query = it },
                        placeholder = { Text("Поиск по имени, интересам...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (viewModel.query.isNotBlank()) {
                                IconButton(onClick = { viewModel.query = "" }) {
                                    Icon(Icons.Default.Close, null)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Filter button
                    val hasActiveFilters = viewModel.filters != SearchFilters()
                    FilledTonalIconButton(
                        onClick = { showFilters = true },
                        modifier = Modifier.size(56.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (hasActiveFilters) Badge()
                            }
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Фильтры")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🌍", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Пользователи не найдены", style = MaterialTheme.typography.titleMedium)
                    Text("Попробуй изменить фильтры", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Найдено: ${filteredUsers.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                items(filteredUsers, key = { it.id }) { user ->
                    UserCard(user = user, onClick = { onUserClick(user.id) })
                }
            }
        }
    }

    // ── Filter Bottom Sheet ────────────────────────────────────────────────────
    if (showFilters) {
        FilterBottomSheet(
            filters = viewModel.filters,
            onFiltersChange = { viewModel.filters = it },
            onDismiss = { showFilters = false },
            onIncludeCountries = { showIncludeCountryPicker = true },
            onExcludeCountries = { showExcludeCountryPicker = true }
        )
    }
}

// ─── User Card ────────────────────────────────────────────────────────────────

@Composable
fun UserCard(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (user.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                user.nickname.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(user.nickname, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${user.age}", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
                }

                // Country + city
                val country = ALL_COUNTRIES.find { it.code == user.country }
                if (country != null || user.city.isNotBlank()) {
                    Text(
                        buildString {
                            country?.let { append("${it.emoji} ${it.nameRu}") }
                            if (user.cityVisible && user.city.isNotBlank()) {
                                if (country != null) append(", ")
                                append(user.city)
                            }
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Languages
                if (user.languages.isNotEmpty()) {
                    Text(
                        user.languages.joinToString(" · ") { it.nameRu },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Interests preview
                if (user.interests.isNotEmpty()) {
                    Text(
                        user.interests.take(3).joinToString(", ") { it.nameRu },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }
            }

            // Online indicator
            if (user.isOnline) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .align(Alignment.Top)
                ) {
                    Surface(color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.fillMaxSize()) {}
                }
            }
        }
    }
}

// ─── Filter Bottom Sheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filters: SearchFilters,
    onFiltersChange: (SearchFilters) -> Unit,
    onDismiss: () -> Unit,
    onIncludeCountries: () -> Unit,
    onExcludeCountries: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Фильтры", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = { onFiltersChange(SearchFilters()) }) {
                    Text("Сбросить")
                }
            }

            // Age range
            Text("Возраст: ${filters.ageFrom} – ${filters.ageTo}", fontWeight = FontWeight.Medium)
            RangeSlider(
                value = filters.ageFrom.toFloat()..filters.ageTo.toFloat(),
                onValueChange = { range ->
                    onFiltersChange(filters.copy(
                        ageFrom = range.start.toInt(),
                        ageTo = range.endInclusive.toInt()
                    ))
                },
                valueRange = 14f..99f,
                steps = 84
            )

            HorizontalDivider()

            // Include countries
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Показывать из стран", fontWeight = FontWeight.Medium)
                if (filters.includeCountries.isNotEmpty()) {
                    Text(
                        filters.includeCountries.mapNotNull { code ->
                            ALL_COUNTRIES.find { it.code == code }
                        }.joinToString(", ") { "${it.emoji} ${it.nameRu}" },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedButton(
                    onClick = onIncludeCountries,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (filters.includeCountries.isEmpty()) "Выбрать страны" else "Изменить (${filters.includeCountries.size})")
                }
            }

            // Exclude countries
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Не показывать из стран", fontWeight = FontWeight.Medium)
                if (filters.excludeCountries.isNotEmpty()) {
                    Text(
                        filters.excludeCountries.mapNotNull { code ->
                            ALL_COUNTRIES.find { it.code == code }
                        }.joinToString(", ") { "${it.emoji} ${it.nameRu}" },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                OutlinedButton(
                    onClick = onExcludeCountries,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Block, null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (filters.excludeCountries.isEmpty()) "Заблокировать страны" else "Изменить (${filters.excludeCountries.size})")
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Применить", fontSize = 16.sp)
            }
        }
    }
}