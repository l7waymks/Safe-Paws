package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.*
import com.example.ui.components.*
import com.example.ui.theme.PrimaryTeal

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    var showEducation by remember { mutableStateOf(false) }
    var showNationalAnimals by remember { mutableStateOf(false) }
    var showNationalAnimalsMapDirectly by remember { mutableStateOf(false) }

    if (showEducation) {
        EducationScreen(onBack = { showEducation = false })
        return
    }

    if (showNationalAnimalsMapDirectly) {
        NationalAnimalsMapScreen(onBack = { showNationalAnimalsMapDirectly = false })
        return
    }

    if (showNationalAnimals) {
        NationalAnimalsScreen(onBack = { showNationalAnimals = false })
        return
    }

    val animals by viewModel.animalsList.collectAsState()
    val incidents by viewModel.strayIncidents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchKeyword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var activeTab by remember { mutableStateOf("posts") } // "posts" (selected by default) or "adoption"

    LaunchedEffect(Unit) {
        viewModel.fetchAnimals()
    }

    val listState = rememberLazyListState()
    val isHeaderVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = isHeaderVisible,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            Column {
                // Welcome Banner and Search Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "اعثر على صديقك المخلص 🐾",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        BasicTextField(
                            value = searchKeyword,
                            onValueChange = { searchKeyword = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("search_pets_input"),
                            decorationBox = @Composable { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "بحث",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (searchKeyword.isEmpty()) {
                                            Text(
                                                text = "بحث عن منشور، سلالة أو صفة...",
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 12.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                    if (searchKeyword.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "مسح",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { searchKeyword = "" }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                // AI Vet Consultant Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                        .clickable { viewModel.showExpertChat.value = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFF00B4D8), CircleShape)
                        ) {
                            Image(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.monkey_doctor_avatar_1786969237937),
                                contentDescription = "المستشار البيطري",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "المستشار البيطري 🐾",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "استشارات بيطرية ورعاية فورية لأليفك على مدار الساعة",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                    }
                }

                // National Animals Card (Countries & Famous Animals)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "🌍", fontSize = 22.sp)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "خريطة حيوانات العالم المصورة 🦁🐓🐂",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "الحيوان مطبوع في قلب كل دولة وممتد حتى حدودها (المغرب 🇲🇦، فرنسا 🇫🇷، إسبانيا 🇪🇸، السعودية 🇸🇦...)",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Illustrated Map Visual Banner Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showNationalAnimalsMapDirectly = true }
                        ) {
                            Image(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.world_map_animals_art_1787510619646),
                                contentDescription = "خريطة حيوانات العالم",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                        )
                                    )
                                    .padding(vertical = 6.dp, horizontal = 10.dp),
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🔍 اضغط لتكبير الخريطة واستكشاف حيوان كل بلد",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        color = Color(0xFF0F766E),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = "عرض الخريطة 🗺️",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showNationalAnimalsMapDirectly = true },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("🗺️ تصفح الخريطة المصورة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = { showNationalAnimals = true },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("📖 الموسوعة والمسابقات", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Countries Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val quickList = listOf(
                                Triple("🇲🇦 المغرب", "🦁 أسد الأطلس", Color(0xFFC1272D).copy(alpha = 0.15f)),
                                Triple("🇫🇷 فرنسا", "🐓 الديك الغالي", Color(0xFF002395).copy(alpha = 0.15f)),
                                Triple("🇪🇸 إسبانيا", "🐂 الثور الإسباني", Color(0xFFAA151B).copy(alpha = 0.15f)),
                                Triple("🇸🇦 السعودية", "🦅 الصقر والجمل", Color(0xFF006C35).copy(alpha = 0.15f))
                            )
                            quickList.forEach { (country, animal, bgCol) ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showNationalAnimalsMapDirectly = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = bgCol
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = country, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(text = animal, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }

                // Education / Articles Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                        .clickable { showEducation = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(
                            text = "المقالات التثقيفية: نصائح للرعاية والإسعافات 📚",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Dynamic Server-Driven UI Block (SDUI)
                var sduiRoot by remember { mutableStateOf<com.example.ui.sdui.SduiComponent?>(null) }
                LaunchedEffect(Unit) {
                    val sduiJson = com.example.SupabaseManager.fetchDynamicUI()
                    sduiRoot = com.example.ui.sdui.SduiParser.parse(sduiJson)
                }

                sduiRoot?.let { rootComponent ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        com.example.ui.sdui.RenderSduiComponent(rootComponent)
                    }
                }

                // Beautiful Sub-tab selector inside HomeScreen: المنشورات vs التبني
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val subTabs = listOf(
                        "posts" to "المنشورات اليومية 📱",
                        "adoption" to "طلبات التبني 🐾"
                    )
                    subTabs.forEach { (key, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (activeTab == key) MaterialTheme.colorScheme.primary 
                                    else Color(0xFF1E1E1E)
                                )
                                .clickable { activeTab = key }
                                .border(
                                    width = 1.dp,
                                    color = if (activeTab == key) Color.Transparent else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Category Filter Row (Only shown for adoption sub-tab)
                if (activeTab == "adoption") {
                    val categories = listOf("الكل", "كلب", "قطة")
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("category_chip_$category")
                            )
                        }
                    }
                }
            }
        }

        // Main Listings
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (activeTab == "posts") {
                // Filter community posts based on search
                val filteredIncidents = incidents.filter {
                    it.title.contains(searchKeyword, true) || it.description.contains(searchKeyword, true) || it.reporter.contains(searchKeyword, true)
                }

                if (filteredIncidents.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "لم نجد منشورات مطابقة لبحثك حالياً.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredIncidents) { incident ->
                            CommunityPostCard(
                                incident = incident,
                                onImageClick = {
                                    val matched = animals.find { 
                                        it.imageUrl == incident.imageUrl || 
                                        (it.name.isNotBlank() && incident.title.contains(it.name)) ||
                                        (it.name.isNotBlank() && incident.description.contains(it.name))
                                    } ?: AnimalItem(
                                        id = incident.id,
                                        name = incident.title.replace("🚨", "").replace("عاجل:", "").trim().take(16).ifBlank { "أليف الإنقاذ" },
                                        species = if (incident.description.contains("كلب") || incident.title.contains("كلب")) "كلب" else "قطة",
                                        breed = "بلدي / هجين أليف",
                                        age = "سنة واحدة",
                                        gender = "ذكر",
                                        size = "متوسط",
                                        description = incident.description,
                                        imageUrl = incident.imageUrl ?: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600",
                                        likesCount = incident.likesCount,
                                        specialNeeds = incident.isEmergency,
                                        vaccinated = true,
                                        neutered = false,
                                        compatibility = "أليف ولطيف واجتماعي مع البشر",
                                        backstory = "تم إنقاذه ونشر تفاصيله في المجتمع بـ ${incident.location} بواسطة ${incident.reporter}."
                                    )
                                    viewModel.selectAnimal(matched)
                                }
                            )
                        }
                    }
                }
            } else {
                val filteredAnimals = animals.filter {
                    (selectedCategory == "الكل" || it.species == selectedCategory) &&
                    (it.name.contains(searchKeyword, true) || it.breed.contains(searchKeyword, true) || it.description.contains(searchKeyword, true))
                }

                if (filteredAnimals.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "لم نجد حيوانات مطابقة لبحثك حالياً.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "تواصل مع رعاية المتطوعين لمزيد من المساعدة.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredAnimals) { animal ->
                            AnimalCard(animal = animal, onClick = { viewModel.selectAnimal(animal) }, onLike = { viewModel.incrementLikes(animal) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimalCard(animal: AnimalItem, onClick: () -> Unit, onLike: () -> Unit) {
    var likes by remember { mutableStateOf(animal.likesCount) }
    var userLiked by remember { mutableStateOf(false) }

    val mockCommentsCount = remember(animal.id) { (animal.id.hashCode() % 25 + 10).coerceAtLeast(5) }
    val mockRepostsCount = remember(animal.id) { (animal.id.hashCode() % 5 + 1).coerceAtLeast(1) }
    val mockSharesCount = remember(animal.id) { (animal.id.hashCode() % 12 + 3).coerceAtLeast(2) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("animal_card_${animal.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row (Avatar, Name, Time, More Option)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Golden Yellow circle avatar with text "RS" (matching the screenshot exactly!)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD54F)) // Golden Yellow
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RS",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 15.sp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "rengosport18", // Matching the username from the screenshot!
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            // Small verified badge
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E7D32)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "موثق",
                                    tint = Color.White,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                        Text(
                            text = "مبادرة تبني الحيوانات الأليفة",
                            fontSize = 11.sp,
                            color = Color.LightGray.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "منذ ٢٢ ساعة", // Matching screenshot's "22h" style
                        fontSize = 12.sp,
                        color = Color.LightGray.copy(alpha = 0.6f)
                    )
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "خيارات",
                        tint = Color.LightGray.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text Post Content (Title, attributes and description)
            Text(
                text = "🚨 حالة تبني جديدة للبطل ${animal.name} (${animal.breed})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${animal.name} هو ${animal.species} أليف ورائع، يبلغ من العمر ${animal.age} وهو ${animal.gender}. ${animal.description}",
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = Color.LightGray.copy(alpha = 0.85f)
            )

            // Location & attributes tag
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "الموقع",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "الرياض، المملكة العربية السعودية",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                if (animal.vaccinated) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("مطعّم ✅", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE65100).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("PetMatch ${animal.matchPercentage}% 🎯", color = Color(0xFFFFB74D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Animal Image Section
            Spacer(modifier = Modifier.height(12.dp))
            AsyncImage(
                model = animal.imageUrl,
                contentDescription = animal.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onClick() }
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Actions Row (Exactly matching the layout in screenshot)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Likes Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            likes = if (userLiked) likes - 1 else likes + 1
                            userLiked = !userLiked
                            onLike()
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (userLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "أعجبني",
                        tint = if (userLiked) Color(0xFFE91E63) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = likes.toString(),
                        color = if (userLiked) Color(0xFFE91E63) else Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Comments Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "التعليقات",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mockCommentsCount.toString(),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Reposts Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "إعادة نشر",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mockRepostsCount.toString(),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Share Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "مشاركة",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mockSharesCount.toString(),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
