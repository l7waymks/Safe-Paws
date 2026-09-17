package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.AnimalItem
import com.example.MainViewModel
import com.example.StrayIncident
import kotlinx.coroutines.delay

enum class SearchCategory(val label: String, val icon: String) {
    ALL("الكل", "🌐"),
    PETS("حيوانات التبني", "🐾"),
    RESCUES("بلاغات الإنقاذ", "🚨"),
    PEOPLE("منقذين وملاجئ", "🛡️"),
    GUIDES("نصائح وتوعية", "📖")
}

data class SearchUserItem(
    val name: String,
    val handle: String,
    val avatarUrl: String?,
    val isVerified: Boolean = false,
    val isShelter: Boolean = false,
    val location: String = "المملكة العربية السعودية",
    val bio: String = "ناشط ومحب للحيوانات الأليفة",
    val trustScore: Int = 92
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val initialQuery by viewModel.searchInitialQuery.collectAsState()
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var selectedCategory by rememberSaveable { mutableStateOf(SearchCategory.ALL) }
    var selectedSpeciesFilter by rememberSaveable { mutableStateOf("الكل") }

    val recentSearches by viewModel.recentSearches.collectAsState()
    val animals by viewModel.animalsList.collectAsState()
    val incidents by viewModel.strayIncidents.collectAsState()

    // Extract unique users and shelters from incidents + defaults
    val communityUsers = remember(incidents) {
        val list = mutableListOf<SearchUserItem>()
        // Defaults
        list.add(
            SearchUserItem(
                name = "ملجأ الرياض للرعاية",
                handle = "riyadh_shelter",
                avatarUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=100",
                isVerified = true,
                isShelter = true,
                location = "حي الملقا، الرياض",
                bio = "ملجأ وجمعية غير ربحية لحماية ورعاية وتأهيل الحيوانات المشردة 🌿",
                trustScore = 98
            )
        )
        list.add(
            SearchUserItem(
                name = "د. خالد البيطري",
                handle = "dr_khaled_vet",
                avatarUrl = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=100",
                isVerified = true,
                isShelter = false,
                location = "حي السليمانية، الرياض",
                bio = "طبيب بيطري وجراح حيوانات أليفة • مستشار رعاية وتغذية 🩺",
                trustScore = 96
            )
        )
        list.add(
            SearchUserItem(
                name = "سارة المنصور",
                handle = "sara_mansoor",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100",
                isVerified = true,
                isShelter = false,
                location = "الرياض",
                bio = "متطوعة إنقاذ ورعاية قطط الشوارع 🐱 مأوى مؤقت",
                trustScore = 91
            )
        )
        incidents.forEach { inc ->
            if (list.none { it.handle == inc.handle || it.name == inc.reporter }) {
                list.add(
                    SearchUserItem(
                        name = inc.reporter,
                        handle = inc.handle.ifBlank { "user_${inc.id}" },
                        avatarUrl = inc.reporterAvatarUrl,
                        isVerified = inc.isVerified,
                        isShelter = inc.isShelter,
                        location = inc.location,
                        bio = "عضو مجتمع بيت الأمان، ساهم في إنقاذ حيوانات أليفة 🐾",
                        trustScore = 88
                    )
                )
            }
        }
        list
    }

    // Auto-focus the search bar when the screen is first opened
    LaunchedEffect(Unit) {
        if (query.isBlank()) {
            delay(150)
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                // Ignore if focus failed
            }
        }
    }

    // Filter Animals
    val filteredAnimals = remember(query, animals, selectedSpeciesFilter) {
        if (query.isBlank() && selectedSpeciesFilter == "الكل") emptyList()
        else {
            animals.filter { pet ->
                val matchesQuery = query.isBlank() ||
                        pet.name.contains(query, ignoreCase = true) ||
                        pet.species.contains(query, ignoreCase = true) ||
                        pet.breed.contains(query, ignoreCase = true) ||
                        pet.description.contains(query, ignoreCase = true) ||
                        pet.compatibility.contains(query, ignoreCase = true) ||
                        pet.backstory.contains(query, ignoreCase = true)

                val matchesSpecies = when (selectedSpeciesFilter) {
                    "الكل" -> true
                    "قطط 🐱" -> pet.species.contains("قط", ignoreCase = true)
                    "كلاب 🐶" -> pet.species.contains("كلب", ignoreCase = true)
                    "أخرى 🐰" -> !pet.species.contains("قط") && !pet.species.contains("كلب")
                    else -> true
                }

                matchesQuery && matchesSpecies
            }
        }
    }

    // Filter Incidents / Posts
    val filteredIncidents = remember(query, incidents) {
        if (query.isBlank()) emptyList()
        else {
            incidents.filter { inc ->
                inc.title.contains(query, ignoreCase = true) ||
                        inc.description.contains(query, ignoreCase = true) ||
                        inc.location.contains(query, ignoreCase = true) ||
                        inc.reporter.contains(query, ignoreCase = true) ||
                        inc.handle.contains(query, ignoreCase = true)
            }
        }
    }

    // Filter Rescuers & Shelters
    val filteredPeople = remember(query, communityUsers) {
        if (query.isBlank()) emptyList()
        else {
            communityUsers.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                        user.handle.contains(query, ignoreCase = true) ||
                        user.location.contains(query, ignoreCase = true) ||
                        user.bio.contains(query, ignoreCase = true)
            }
        }
    }

    // Filter Guides & Care Articles
    val filteredGuides = remember(query, incidents) {
        if (query.isBlank()) emptyList()
        else {
            incidents.filter { inc ->
                inc.title.contains("نصيح") || inc.title.contains("دليل") ||
                        inc.title.contains("توعية") || inc.title.contains("مقال") ||
                        inc.title.contains("قصة") || inc.description.contains("نصيح") ||
                        inc.description.contains("طعام") || inc.description.contains("عناية")
            }.filter { inc ->
                inc.title.contains(query, ignoreCase = true) ||
                        inc.description.contains(query, ignoreCase = true)
            }
        }
    }

    val totalResultsCount = filteredAnimals.size + filteredIncidents.size + filteredPeople.size

    val trendingKeywords = listOf(
        "🐱 قطط شيرازي",
        "🐶 جراء صغيرة للتبني",
        "🚨 حالات عاجلة بالرياض",
        "🏥 عيادات بيطرية ٢٤ ساعة",
        "🥣 مبادرة طعام الشتاء",
        "🐕 كلاب جولدن ريتريفر",
        "📍 قطط جدة",
        "✨ قصص نجاح الإنقاذ"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_dedicated_screen"),
        color = Color(0xFF0B1120) // Premium dark slate background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Top Search Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Back Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Search Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "أيقونة البحث",
                            tint = Color(0xFF2DD4BF),
                            modifier = Modifier.size(20.dp)
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "ابحث عن أليف، سلالة، بلاغ إنقاذ، أو منقذ...",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .testTag("search_main_input"),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color(0xFF2DD4BF)),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        focusManager.clearFocus()
                                        if (query.isNotBlank()) {
                                            viewModel.addRecentSearch(query)
                                        }
                                    }
                                )
                            )
                        }

                        // Clear Button
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = { query = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "مسح النص",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Filter Category Pills (الكل، حيوانات التبني، بلاغات الإنقاذ، المنقذين والملاجئ، نصائح)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SearchCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0D9488) else Color(0xFF1E293B),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF2DD4BF) else Color(0xFF334155).copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.clickable {
                            selectedCategory = category
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = category.icon, fontSize = 13.sp)
                            Text(
                                text = category.label,
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Sub-filter for Pets Category (Species filter)
            if (selectedCategory == SearchCategory.PETS || selectedCategory == SearchCategory.ALL) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val speciesList = listOf("الكل", "قطط 🐱", "كلاب 🐶", "أخرى 🐰")
                    items(speciesList) { sp ->
                        val isSel = selectedSpeciesFilter == sp
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) Color(0xFF2DD4BF).copy(alpha = 0.2f) else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isSel) Color(0xFF2DD4BF) else Color(0xFF334155).copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.clickable { selectedSpeciesFilter = sp }
                        ) {
                            Text(
                                text = sp,
                                color = if (isSel) Color(0xFF2DD4BF) else Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

            // 3. Screen Body: Discover State vs Live Results State
            if (query.isBlank()) {
                // ==================== DISCOVER / INITIAL STATE ====================
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Recent Searches
                    if (recentSearches.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = Color(0xFF2DD4BF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "عمليات البحث الأخيرة",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "مسح السجل",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.clickable { viewModel.clearRecentSearches() }
                                    )
                                }

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    recentSearches.forEach { item ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = Color(0xFF1E293B),
                                            border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .clickable {
                                                        query = item
                                                        viewModel.addRecentSearch(item)
                                                    }
                                                    .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = item,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFE2E8F0)
                                                )
                                                IconButton(
                                                    onClick = { viewModel.removeRecentSearch(item) },
                                                    modifier = Modifier.size(18.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "حذف",
                                                        tint = Color(0xFF94A3B8),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Trending Topics & Searches
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🔥", fontSize = 16.sp)
                                Text(
                                    text = "الأكثر بحثاً وتداولاً",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                trendingKeywords.forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFF1E293B).copy(alpha = 0.8f),
                                        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f)),
                                        modifier = Modifier.clickable {
                                            val clean = tag.replace("🐱", "").replace("🐶", "").replace("🚨", "")
                                                .replace("🏥", "").replace("🥣", "").replace("🐕", "")
                                                .replace("📍", "").replace("✨", "").trim()
                                            query = clean
                                            viewModel.addRecentSearch(clean)
                                        }
                                    ) {
                                        Text(
                                            text = tag,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFCBD5E1),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Quick Explore Category Grids
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "استكشاف سريع حسب الفئة 🧭",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                QuickExploreCard(
                                    title = "قطط للتبني",
                                    subtitle = "أليفة ومطّعمة",
                                    icon = "🐱",
                                    gradient = listOf(Color(0xFF0F766E), Color(0xFF115E59)),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        query = "قطة"
                                        selectedCategory = SearchCategory.PETS
                                    }
                                )
                                QuickExploreCard(
                                    title = "كلاب وجراء",
                                    subtitle = "وفية وتنتظر بيتاً",
                                    icon = "🐶",
                                    gradient = listOf(Color(0xFF0369A1), Color(0xFF075985)),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        query = "كلب"
                                        selectedCategory = SearchCategory.PETS
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                QuickExploreCard(
                                    title = "بلاغات عاجلة",
                                    subtitle = "تحتاج تدخلاً سريعاً",
                                    icon = "🚨",
                                    gradient = listOf(Color(0xFF991B1B), Color(0xFF7F1D1D)),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        query = "عاجل"
                                        selectedCategory = SearchCategory.RESCUES
                                    }
                                )
                                QuickExploreCard(
                                    title = "استشارة بيطرية",
                                    subtitle = "طبيبك الذكي 24/7",
                                    icon = "🩺",
                                    gradient = listOf(Color(0xFF4338CA), Color(0xFF3730A3)),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        onClose()
                                        viewModel.showExpertChat.value = true
                                    }
                                )
                            }
                        }
                    }

                    // Spotlight Pets Carousel
                    if (animals.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "حيوانات تبحث عن عائلة محبة 🏡",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "عرض الكل",
                                        color = Color(0xFF2DD4BF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable {
                                            selectedCategory = SearchCategory.PETS
                                            query = " "
                                        }
                                    )
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    items(animals.take(6)) { pet ->
                                        SpotlightPetMiniCard(
                                            pet = pet,
                                            onClick = {
                                                viewModel.addRecentSearch(pet.name)
                                                viewModel.selectAnimal(pet)
                                                onClose()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ==================== LIVE RESULTS STATE ====================
                if (totalResultsCount == 0 && (selectedCategory == SearchCategory.ALL || (selectedCategory == SearchCategory.PETS && filteredAnimals.isEmpty()) || (selectedCategory == SearchCategory.RESCUES && filteredIncidents.isEmpty()) || (selectedCategory == SearchCategory.PEOPLE && filteredPeople.isEmpty()))) {
                    // Empty Search Results View
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🔍", fontSize = 34.sp)
                                }
                            }

                            Text(
                                text = "لم نجد نتائج مطابقة لـ \"$query\"",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "تأكد من صحة الكلمات المكتوبة أو جرب البحث بكلمات أبسط مثل 'قطة'، 'كلب'، 'الرياض'، أو 'إنقاذ'.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )

                            Button(
                                onClick = { query = "" },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                            ) {
                                Text("مسح البحث والبدء من جديد", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Results Found List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "نتائج البحث لـ \"$query\" (${totalResultsCount} نتيجة)",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Category: ALL
                        if (selectedCategory == SearchCategory.ALL) {
                            // Animals Section
                            if (filteredAnimals.isNotEmpty()) {
                                item {
                                    SearchResultsSectionHeader(
                                        title = "حيوانات للتبني 🐾",
                                        count = filteredAnimals.size,
                                        onViewAll = { selectedCategory = SearchCategory.PETS }
                                    )
                                }
                                items(filteredAnimals.take(3)) { pet ->
                                    SearchResultPetCard(
                                        pet = pet,
                                        onClick = {
                                            viewModel.addRecentSearch(query)
                                            viewModel.selectAnimal(pet)
                                            onClose()
                                        }
                                    )
                                }
                            }

                            // Incidents & Rescues Section
                            if (filteredIncidents.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    SearchResultsSectionHeader(
                                        title = "بلاغات وحالات إنقاذ 🚨",
                                        count = filteredIncidents.size,
                                        onViewAll = { selectedCategory = SearchCategory.RESCUES }
                                    )
                                }
                                items(filteredIncidents.take(3)) { incident ->
                                    SearchResultIncidentCard(
                                        incident = incident,
                                        onUserClick = {
                                            viewModel.addRecentSearch(query)
                                            viewModel.openUserProfileByInfo(
                                                name = incident.reporter,
                                                handle = incident.handle,
                                                avatarUrl = incident.reporterAvatarUrl,
                                                isVerified = incident.isVerified,
                                                isShelter = incident.isShelter,
                                                location = incident.location
                                            )
                                            onClose()
                                        }
                                    )
                                }
                            }

                            // People & Shelters Section
                            if (filteredPeople.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    SearchResultsSectionHeader(
                                        title = "المنقذين والملاجئ 🛡️",
                                        count = filteredPeople.size,
                                        onViewAll = { selectedCategory = SearchCategory.PEOPLE }
                                    )
                                }
                                items(filteredPeople.take(3)) { user ->
                                    SearchResultUserCard(
                                        user = user,
                                        onClick = {
                                            viewModel.addRecentSearch(query)
                                            viewModel.openUserProfileByInfo(
                                                name = user.name,
                                                handle = user.handle,
                                                avatarUrl = user.avatarUrl,
                                                isVerified = user.isVerified,
                                                isShelter = user.isShelter,
                                                location = user.location
                                            )
                                            onClose()
                                        }
                                    )
                                }
                            }
                        }

                        // Category: PETS
                        if (selectedCategory == SearchCategory.PETS) {
                            items(filteredAnimals) { pet ->
                                SearchResultPetCard(
                                    pet = pet,
                                    onClick = {
                                        viewModel.addRecentSearch(query)
                                        viewModel.selectAnimal(pet)
                                        onClose()
                                    }
                                )
                            }
                        }

                        // Category: RESCUES
                        if (selectedCategory == SearchCategory.RESCUES) {
                            items(filteredIncidents) { incident ->
                                SearchResultIncidentCard(
                                    incident = incident,
                                    onUserClick = {
                                        viewModel.addRecentSearch(query)
                                        viewModel.openUserProfileByInfo(
                                            name = incident.reporter,
                                            handle = incident.handle,
                                            avatarUrl = incident.reporterAvatarUrl,
                                            isVerified = incident.isVerified,
                                            isShelter = incident.isShelter,
                                            location = incident.location
                                        )
                                        onClose()
                                    }
                                )
                            }
                        }

                        // Category: PEOPLE
                        if (selectedCategory == SearchCategory.PEOPLE) {
                            items(filteredPeople) { user ->
                                SearchResultUserCard(
                                    user = user,
                                    onClick = {
                                        viewModel.addRecentSearch(query)
                                        viewModel.openUserProfileByInfo(
                                            name = user.name,
                                            handle = user.handle,
                                            avatarUrl = user.avatarUrl,
                                            isVerified = user.isVerified,
                                            isShelter = user.isShelter,
                                            location = user.location
                                        )
                                        onClose()
                                    }
                                )
                            }
                        }

                        // Category: GUIDES
                        if (selectedCategory == SearchCategory.GUIDES) {
                            items(filteredGuides) { guide ->
                                SearchResultIncidentCard(
                                    incident = guide,
                                    onUserClick = {
                                        viewModel.addRecentSearch(query)
                                        viewModel.openUserProfileByInfo(
                                            name = guide.reporter,
                                            handle = guide.handle,
                                            avatarUrl = guide.reporterAvatarUrl,
                                            isVerified = guide.isVerified,
                                            isShelter = guide.isShelter,
                                            location = guide.location
                                        )
                                        onClose()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- COMPONENT WIDGETS -----------------

@Composable
private fun QuickExploreCard(
    title: String,
    subtitle: String,
    icon: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(86.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradient))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subtitle, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                }
                Text(text = icon, fontSize = 28.sp)
            }
        }
    }
}

@Composable
private fun SpotlightPetMiniCard(
    pet: AnimalItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                AsyncImage(
                    model = pet.imageUrl,
                    contentDescription = pet.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = pet.species,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = pet.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = pet.breed,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SearchResultsSectionHeader(
    title: String,
    count: Int,
    onViewAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Color.White)
            Surface(
                shape = CircleShape,
                color = Color(0xFF334155)
            ) {
                Text(
                    text = count.toString(),
                    color = Color(0xFF2DD4BF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }
        Text(
            text = "المزيد >",
            color = Color(0xFF2DD4BF),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onViewAll() }
        )
    }
}

@Composable
private fun SearchResultPetCard(
    pet: AnimalItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = pet.imageUrl,
                contentDescription = pet.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = pet.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0D9488).copy(alpha = 0.2f),
                        border = BorderStroke(0.8.dp, Color(0xFF2DD4BF))
                    ) {
                        Text(
                            text = pet.species,
                            fontSize = 10.sp,
                            color = Color(0xFF2DD4BF),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${pet.breed} • ${pet.age} • ${pet.gender}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = pet.description,
                    fontSize = 11.5.sp,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0D9488)
            ) {
                Text(
                    text = "تبنى 🐾",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchResultIncidentCard(
    incident: StrayIncident,
    onUserClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = incident.reporterAvatarUrl,
                        contentDescription = incident.reporter,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = incident.reporter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            if (incident.isVerified) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color(0xFF2DD4BF),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = "📍 ${incident.location} • ${incident.timestamp}",
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                if (incident.isEmergency) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Text(
                            text = "حالة عاجلة 🚨",
                            color = Color(0xFFEF4444),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = incident.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = Color.White
            )

            Text(
                text = incident.description,
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            if (!incident.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = incident.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

@Composable
private fun SearchResultUserCard(
    user: SearchUserItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = user.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    if (user.isVerified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF2DD4BF),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    if (user.isShelter) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF38BDF8).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ملجأ معتمد",
                                fontSize = 9.5.sp,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "@${user.handle} • 📍 ${user.location}",
                    fontSize = 11.5.sp,
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = user.bio,
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF334155)
            ) {
                Text(
                    text = "عرض الملف",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
