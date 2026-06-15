package com.levonty.unitalk.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Country(val code: String, val nameRu: String, val emoji: String)

val ALL_COUNTRIES = listOf(
    Country("AF", "Афганистан", "🇦🇫"), Country("AL", "Албания", "🇦🇱"),
    Country("DZ", "Алжир", "🇩🇿"), Country("AD", "Андорра", "🇦🇩"),
    Country("AO", "Ангола", "🇦🇴"), Country("AR", "Аргентина", "🇦🇷"),
    Country("AM", "Армения", "🇦🇲"), Country("AU", "Австралия", "🇦🇺"),
    Country("AT", "Австрия", "🇦🇹"), Country("AZ", "Азербайджан", "🇦🇿"),
    Country("BH", "Бахрейн", "🇧🇭"), Country("BD", "Бангладеш", "🇧🇩"),
    Country("BY", "Беларусь", "🇧🇾"), Country("BE", "Бельгия", "🇧🇪"),
    Country("BZ", "Белиз", "🇧🇿"), Country("BJ", "Бенин", "🇧🇯"),
    Country("BT", "Бутан", "🇧🇹"), Country("BO", "Боливия", "🇧🇴"),
    Country("BA", "Босния и Герцеговина", "🇧🇦"), Country("BW", "Ботсвана", "🇧🇼"),
    Country("BR", "Бразилия", "🇧🇷"), Country("BN", "Бруней", "🇧🇳"),
    Country("BG", "Болгария", "🇧🇬"), Country("BF", "Буркина-Фасо", "🇧🇫"),
    Country("BI", "Бурунди", "🇧🇮"), Country("KH", "Камбоджа", "🇰🇭"),
    Country("CM", "Камерун", "🇨🇲"), Country("CA", "Канада", "🇨🇦"),
    Country("CV", "Кабо-Верде", "🇨🇻"), Country("CF", "ЦАР", "🇨🇫"),
    Country("TD", "Чад", "🇹🇩"), Country("CL", "Чили", "🇨🇱"),
    Country("CN", "Китай", "🇨🇳"), Country("CO", "Колумбия", "🇨🇴"),
    Country("KM", "Коморы", "🇰🇲"), Country("CD", "ДР Конго", "🇨🇩"),
    Country("CG", "Конго", "🇨🇬"), Country("CR", "Коста-Рика", "🇨🇷"),
    Country("HR", "Хорватия", "🇭🇷"), Country("CU", "Куба", "🇨🇺"),
    Country("CY", "Кипр", "🇨🇾"), Country("CZ", "Чехия", "🇨🇿"),
    Country("DK", "Дания", "🇩🇰"), Country("DJ", "Джибути", "🇩🇯"),
    Country("DO", "Доминиканская Республика", "🇩🇴"), Country("EC", "Эквадор", "🇪🇨"),
    Country("EG", "Египет", "🇪🇬"), Country("SV", "Сальвадор", "🇸🇻"),
    Country("GQ", "Экваториальная Гвинея", "🇬🇶"), Country("ER", "Эритрея", "🇪🇷"),
    Country("EE", "Эстония", "🇪🇪"), Country("ET", "Эфиопия", "🇪🇹"),
    Country("FJ", "Фиджи", "🇫🇯"), Country("FI", "Финляндия", "🇫🇮"),
    Country("FR", "Франция", "🇫🇷"), Country("GA", "Габон", "🇬🇦"),
    Country("GM", "Гамбия", "🇬🇲"), Country("GE", "Грузия", "🇬🇪"),
    Country("DE", "Германия", "🇩🇪"), Country("GH", "Гана", "🇬🇭"),
    Country("GR", "Греция", "🇬🇷"), Country("GT", "Гватемала", "🇬🇹"),
    Country("GN", "Гвинея", "🇬🇳"), Country("GW", "Гвинея-Бисау", "🇬🇼"),
    Country("GY", "Гайана", "🇬🇾"), Country("HT", "Гаити", "🇭🇹"),
    Country("HN", "Гондурас", "🇭🇳"), Country("HU", "Венгрия", "🇭🇺"),
    Country("IS", "Исландия", "🇮🇸"), Country("IN", "Индия", "🇮🇳"),
    Country("ID", "Индонезия", "🇮🇩"), Country("IR", "Иран", "🇮🇷"),
    Country("IQ", "Ирак", "🇮🇶"), Country("IE", "Ирландия", "🇮🇪"),
    Country("IL", "Израиль", "🇮🇱"), Country("IT", "Италия", "🇮🇹"),
    Country("JM", "Ямайка", "🇯🇲"), Country("JP", "Япония", "🇯🇵"),
    Country("JO", "Иордания", "🇯🇴"), Country("KZ", "Казахстан", "🇰🇿"),
    Country("KE", "Кения", "🇰🇪"), Country("KW", "Кувейт", "🇰🇼"),
    Country("KG", "Кыргызстан", "🇰🇬"), Country("LA", "Лаос", "🇱🇦"),
    Country("LV", "Латвия", "🇱🇻"), Country("LB", "Ливан", "🇱🇧"),
    Country("LS", "Лесото", "🇱🇸"), Country("LR", "Либерия", "🇱🇷"),
    Country("LY", "Ливия", "🇱🇾"), Country("LI", "Лихтенштейн", "🇱🇮"),
    Country("LT", "Литва", "🇱🇹"), Country("LU", "Люксембург", "🇱🇺"),
    Country("MG", "Мадагаскар", "🇲🇬"), Country("MW", "Малави", "🇲🇼"),
    Country("MY", "Малайзия", "🇲🇾"), Country("MV", "Мальдивы", "🇲🇻"),
    Country("ML", "Мали", "🇲🇱"), Country("MT", "Мальта", "🇲🇹"),
    Country("MR", "Мавритания", "🇲🇷"), Country("MX", "Мексика", "🇲🇽"),
    Country("MD", "Молдова", "🇲🇩"), Country("MC", "Монако", "🇲🇨"),
    Country("MN", "Монголия", "🇲🇳"), Country("ME", "Черногория", "🇲🇪"),
    Country("MA", "Марокко", "🇲🇦"), Country("MZ", "Мозамбик", "🇲🇿"),
    Country("MM", "Мьянма", "🇲🇲"), Country("NA", "Намибия", "🇳🇦"),
    Country("NP", "Непал", "🇳🇵"), Country("NL", "Нидерланды", "🇳🇱"),
    Country("NZ", "Новая Зеландия", "🇳🇿"), Country("NI", "Никарагуа", "🇳🇮"),
    Country("NE", "Нигер", "🇳🇪"), Country("NG", "Нигерия", "🇳🇬"),
    Country("MK", "Северная Македония", "🇲🇰"), Country("NO", "Норвегия", "🇳🇴"),
    Country("OM", "Оман", "🇴🇲"), Country("PK", "Пакистан", "🇵🇰"),
    Country("PA", "Панама", "🇵🇦"), Country("PG", "Папуа — Новая Гвинея", "🇵🇬"),
    Country("PY", "Парагвай", "🇵🇾"), Country("PE", "Перу", "🇵🇪"),
    Country("PH", "Филиппины", "🇵🇭"), Country("PL", "Польша", "🇵🇱"),
    Country("PT", "Португалия", "🇵🇹"), Country("QA", "Катар", "🇶🇦"),
    Country("RO", "Румыния", "🇷🇴"), Country("RU", "Россия", "🇷🇺"),
    Country("RW", "Руанда", "🇷🇼"), Country("SA", "Саудовская Аравия", "🇸🇦"),
    Country("SN", "Сенегал", "🇸🇳"), Country("RS", "Сербия", "🇷🇸"),
    Country("SL", "Сьерра-Леоне", "🇸🇱"), Country("SG", "Сингапур", "🇸🇬"),
    Country("SK", "Словакия", "🇸🇰"), Country("SI", "Словения", "🇸🇮"),
    Country("SO", "Сомали", "🇸🇴"), Country("ZA", "ЮАР", "🇿🇦"),
    Country("SS", "Южный Судан", "🇸🇸"), Country("ES", "Испания", "🇪🇸"),
    Country("LK", "Шри-Ланка", "🇱🇰"), Country("SD", "Судан", "🇸🇩"),
    Country("SR", "Суринам", "🇸🇷"), Country("SE", "Швеция", "🇸🇪"),
    Country("CH", "Швейцария", "🇨🇭"), Country("SY", "Сирия", "🇸🇾"),
    Country("TW", "Тайвань", "🇹🇼"), Country("TJ", "Таджикистан", "🇹🇯"),
    Country("TZ", "Танзания", "🇹🇿"), Country("TH", "Таиланд", "🇹🇭"),
    Country("TL", "Тимор-Лесте", "🇹🇱"), Country("TG", "Того", "🇹🇬"),
    Country("TT", "Тринидад и Тобаго", "🇹🇹"), Country("TN", "Тунис", "🇹🇳"),
    Country("TR", "Турция", "🇹🇷"), Country("TM", "Туркменистан", "🇹🇲"),
    Country("UG", "Уганда", "🇺🇬"), Country("UA", "Украина", "🇺🇦"),
    Country("AE", "ОАЭ", "🇦🇪"), Country("GB", "Великобритания", "🇬🇧"),
    Country("US", "США", "🇺🇸"), Country("UY", "Уругвай", "🇺🇾"),
    Country("UZ", "Узбекистан", "🇺🇿"), Country("VE", "Венесуэла", "🇻🇪"),
    Country("VN", "Вьетнам", "🇻🇳"), Country("YE", "Йемен", "🇾🇪"),
    Country("ZM", "Замбия", "🇿🇲"), Country("ZW", "Зимбабве", "🇿🇼")
).sortedBy { it.nameRu }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerScreen(
    title: String = "Выбрать страну",
    selectedCodes: List<String> = emptyList(),
    multiSelect: Boolean = false,
    onBack: () -> Unit,
    onSelect: (Country) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) ALL_COUNTRIES
        else ALL_COUNTRIES.filter { it.nameRu.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Поиск страны...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            LazyColumn {
                items(filtered, key = { it.code }) { country ->
                    val isSelected = country.code in selectedCodes
                    ListItem(
                        headlineContent = { Text("${country.emoji}  ${country.nameRu}") },
                        trailingContent = {
                            if (isSelected) Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable { onSelect(country) },
                        colors = ListItemDefaults.colors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surface
                        )
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}