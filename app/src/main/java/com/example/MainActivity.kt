package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ui.theme.*
import androidx.compose.ui.text.TextStyle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Wrap in RTL Layout Direction for native Arabic experience
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SafePawsApp()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafePawsApp(viewModel: MainViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.Home,
                    onClick = { viewModel.selectTab(AppTab.Home) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.Community,
                    onClick = { viewModel.selectTab(AppTab.Community) },
                    icon = { Icon(Icons.Default.Comment, contentDescription = "المجتمع") },
                    label = { Text("المجتمع", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.Adoption,
                    onClick = { viewModel.selectTab(AppTab.Adoption) },
                    icon = { Icon(Icons.Default.VolunteerActivism, contentDescription = "التبني") },
                    label = { Text("التبني", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.Map,
                    onClick = { viewModel.selectTab(AppTab.Map) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "الخريطة") },
                    label = { Text("الخريطة", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.Profile,
                    onClick = { viewModel.selectTab(AppTab.Profile) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "الملف الشخصي") },
                    label = { Text("الملف", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.Home -> {
                    val selectedAnimal by viewModel.selectedAnimal.collectAsState()
                    if (selectedAnimal != null) {
                        AnimalDetailsScreen(
                            animal = selectedAnimal!!,
                            viewModel = viewModel
                        )
                    } else {
                        HomeScreen(viewModel = viewModel)
                    }
                }
                AppTab.Community -> CommunityScreen(viewModel = viewModel)
                AppTab.Adoption -> AdoptionScreen(viewModel = viewModel)
                AppTab.Map -> MapScreen(viewModel = viewModel)
                AppTab.Profile -> ProfileScreen(viewModel = viewModel)
            }
        }
    }
}

// ---------------------- 1. HOME SCREEN ----------------------
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val animals by viewModel.animalsList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchKeyword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }

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
                                                text = "بحث عن سلالة، فصيل، أو صفة...",
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

                // Category Filter Row
                val categories = listOf("الكل", "كلب", "قطة")
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
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

        // Main Listings
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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

@Composable
fun AnimalCard(animal: AnimalItem, onClick: () -> Unit, onLike: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("animal_card_${animal.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = animal.imageUrl,
                    contentDescription = animal.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Match Percentage indicator
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "PetMatch: ${animal.matchPercentage}%",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Species Tag
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Text(
                        text = animal.species,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = animal.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onLike) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "أعجبني",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = animal.likesCount.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = "${animal.breed} • ${animal.age} • ${animal.gender}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = animal.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (animal.vaccinated) {
                            AssistChip(
                                onClick = {},
                                label = { Text("مطعّم", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = "نعم", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary) }
                            )
                        }
                        if (animal.neutered) {
                            AssistChip(
                                onClick = {},
                                label = { Text("معقّم", fontSize = 10.sp) }
                            )
                        }
                    }

                    Text(
                        text = "عرض التفاصيل ←",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// ---------------------- 2. ANIMAL DETAIL OVERLAY (REALTIME COMMENTS) ----------------------
@Composable
fun AnimalDetailsScreen(animal: AnimalItem, viewModel: MainViewModel) {
    val comments by viewModel.activeComments.collectAsState()
    val isSubmittingComment by viewModel.isSubmittingComment.collectAsState()

    // Match Quiz State
    val onboardingCompleted by viewModel.onboardingQuizCompleted.collectAsState()
    val matchScore by viewModel.quizPercentage.collectAsState()
    val analysisReport by viewModel.quizAnalysisReport.collectAsState()
    val isAnalyzing by viewModel.isQuizAnalyzing.collectAsState()

    var commentText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Match Quiz Questions Selection states
    var activityAnswer by remember { mutableStateOf("متوسط") }
    var workHoursAnswer by remember { mutableStateOf("أقل من ٤ ساعات") }
    var housingAnswer by remember { mutableStateOf("شقة سكنية") }
    var kidsAnswer by remember { mutableStateOf("نعم يتوفر صغار") }
    var experienceAnswer by remember { mutableStateOf("مبتدئ") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Upper Image section with Back button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            AsyncImage(
                model = animal.imageUrl,
                contentDescription = animal.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic gradient shadow overlay for back button legibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .height(64.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )

            IconButton(
                onClick = { viewModel.selectAnimal(null) },
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .testTag("back_to_list")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "رجوع",
                    tint = Color.White
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Title & Bio Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تفاصيل الأليف: ${animal.name}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${animal.breed} • ${animal.age} • ${animal.gender}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.incrementLikes(animal) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "أعجبني",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BACKSTORY & PERSONALITY
            ElevatedCard(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "القصة والخلفية 📖",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = animal.backstory,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "التوافق لـ التبني: ${animal.compatibility}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🧠 PETMATCH AI QUIZ PANEL
            Text(
                text = "PetMatch AI - تقييم التوافق الذكي 🧠",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!onboardingCompleted) {
                        Text(
                            text = "أجب عن ٥ أسئلة سريعة لنقيم بواسطة الذكاء الاصطناعي (Gemini) مدى توافق أسلوب حياتك وسكنك مع احتياجات ${animal.name}.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Q1
                        Text("١. مستوى نشاطك البدني المعتاد:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("نشط جداً", "متوسط", "هادئ").forEach {
                                Row(
                                    modifier = Modifier
                                        .clickable { activityAnswer = it }
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = activityAnswer == it, onClick = { activityAnswer = it })
                                    Text(it, fontSize = 11.sp)
                                }
                            }
                        }

                        // Q2
                        Text("٢. ساعات غيابك عن المنزل يومياً:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("أقل من ٤ ساعات", "٤-٨ ساعات", "أكثر من ٨ ساعات").forEach {
                                Row(
                                    modifier = Modifier
                                        .clickable { workHoursAnswer = it }
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = workHoursAnswer == it, onClick = { workHoursAnswer = it })
                                    Text(it, fontSize = 11.sp)
                                }
                            }
                        }

                        // Q3
                        Text("٣. نمط المسكن والبيئة المنزلية:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("شقة سكنية", "فيلا بحديقة").forEach {
                                Row(
                                    modifier = Modifier
                                        .clickable { housingAnswer = it }
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = housingAnswer == it, onClick = { housingAnswer = it })
                                    Text(it, fontSize = 11.sp)
                                }
                            }
                        }

                        // Q4
                        Text("٤. هل يتوفر أطفال بالبيئة المحيطة؟", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("نعم يتوفر صغار", "لا يوجد أطفال").forEach {
                                Row(
                                    modifier = Modifier
                                        .clickable { kidsAnswer = it }
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = kidsAnswer == it, onClick = { kidsAnswer = it })
                                    Text(it, fontSize = 11.sp)
                                }
                            }
                        }

                        // Q5
                        Text("٥. خبرتك السابقة بتربية الحيوانات:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("مبتدئ", "متوسط", "خبير ومتمرس").forEach {
                                Row(
                                    modifier = Modifier
                                        .clickable { experienceAnswer = it }
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = experienceAnswer == it, onClick = { experienceAnswer = it })
                                    Text(it, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.runMatchingQuiz(
                                    activityAnswer, workHoursAnswer, housingAnswer, kidsAnswer, experienceAnswer
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("run_petmatch_quiz"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("تحليل التوافق بـ الذكاء الاصطناعي 🧠")
                        }

                    } else {
                        // Scoring analysis result
                        if (isAnalyzing) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("تقييم وتحليل البيانات جاري بواسطة Gemini AI...", fontSize = 12.sp)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "نسبة توافقك مع ${animal.name}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${matchScore ?: 85}%",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = analysisReport,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedButton(onClick = { viewModel.resetMatchQuiz() }) {
                                    Text("أعد تشغيل الاختبار 🔄")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 💬 COMMUNITY ENGAGEMENT (REACTION SYSTEMS & COMMENTS FEED)
            Text(
                text = "💬 تفاعل الأعضاء والتعليقات الحية",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Emoji Reactions Grid
            val emojis = listOf("🐾", "❤️", "🐱", "🐶", "🥺", "🥰")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                emojis.forEach { emoji ->
                    var count by rememberSaveable { mutableStateOf((2..34).random()) }
                    var reacted by rememberSaveable { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (reacted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable {
                                count = if (reacted) count - 1 else count + 1
                                reacted = !reacted
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(count.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment Composer
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("comment_input_box"),
                placeholder = { Text("اكتب سؤالاً أو تواصل مع المتطوعين...", fontSize = 12.sp) },
                maxLines = 3,
                trailingIcon = {
                    if (isSubmittingComment) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        IconButton(onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.addComment(commentText)
                                commentText = ""
                            }
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "أرسل", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comments list (fetched from Supabase `animal_comments` and nested)
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا يوجد تعليقات بعد. ابدأ المناقشة واكتب تعليقاً للأليف!", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    comments.forEach { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(comment.author.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(comment.author, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(comment.createdAt, fontSize = 10.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(comment.text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ACTION KEY: STEP INTO ADOPTION PIPELINE
            Button(
                onClick = {
                    viewModel.selectAnimal(null)
                    viewModel.selectTab(AppTab.Adoption)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("apply_adoption_trigger"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = "كتابة طلب التبني")
                    Text("قدم طلب التبني لـ ${animal.name} الآن 🐾", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


// ---------------------- 3. ADOPTION PIPELINE VIEW (SCREENSHOT 2) ----------------------
@Composable
fun AdoptionScreen(viewModel: MainViewModel) {
    val pipeline by viewModel.adoptionPipeline.collectAsState()
    var showSurveyDialog by remember { mutableStateOf(false) }

    // Helpful Resources dialogues triggers
    var showPrepModal by remember { mutableStateOf(false) }
    var showExpertChat by remember { mutableStateOf(false) }
    var showHealthPassport by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    if (showPrepModal) {
        Dialog(onDismissRequest = { showPrepModal = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("دليل تجهيز المنزل لاستقبال بندق 📖", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Text("استعن بالنصائح التالية ليكون الانتقال سهلاً ومريحاً:", fontSize = 13.sp)
                    BulletPoint("تأمين النوافذ والشرفات لسلامة الجري والقفز.")
                    BulletPoint("فصل وتغطية الأسلاك الكهربائية الممتدة.")
                    BulletPoint("شراء صناديق طعام مغلقة وصحية وأوعية من الستانلس ستيل.")
                    BulletPoint("تهيئة منطقة نوم وراحة آمنة وهادئة خاصة به.")
                    TextButton(onClick = { showPrepModal = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("موافق")
                    }
                }
            }
        }
    }

    if (showHealthPassport) {
        Dialog(onDismissRequest = { showHealthPassport = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("السجل الصحي الرقمي لـ بندق 🩺", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    
                    // Simple simulated QR Code layout
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color.White)
                            .border(2.dp, Color.Black)
                            .padding(8.dp)
                    ) {
                        // QR style pattern placeholder
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // draw random neat blocks
                            val cols = 8
                            val sizeW = size.width / cols
                            val sizeH = size.height / cols
                            for (i in 0 until cols) {
                                for (j in 0 until cols) {
                                    if ((i + j) % 2 == 0 || (i * j) % 3 == 0) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = androidx.compose.ui.geometry.Offset(i * sizeW, j * sizeH),
                                            size = androidx.compose.ui.geometry.Size(sizeW, sizeH)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text("سجل التحصينات والتطعيمات الحالي:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("• اللقاح الثماني الأساسي:", fontSize = 11.sp)
                            Text("مكتمل ومحدث ✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("• لقاح داء السعار السنوي:", fontSize = 11.sp)
                            Text("الموعد القادم: ٢٨ نوفمبر ٢٠٢٦", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Button(onClick = { showHealthPassport = false }) {
                        Text("إغلاق السجل")
                    }
                }
            }
        }
    }

    if (showExpertChat) {
        Dialog(onDismissRequest = { showExpertChat = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                ChatBotLayout(viewModel = viewModel, onClose = { showExpertChat = false })
            }
        }
    }

    if (showSurveyDialog) {
        Dialog(onDismissRequest = { showSurveyDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    var style by remember { mutableStateOf(pipeline.homeStyle) }
                    var security by remember { mutableStateOf(pipeline.securityEnabled) }
                    var hours by remember { mutableStateOf(pipeline.dailyHoursAlone) }
                    var kids by remember { mutableStateOf(pipeline.kidsPresence) }
                    var activity by remember { mutableStateOf(pipeline.activityIntent) }

                    Text("تعديل استبيان المنزل 🏠", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(value = style, onValueChange = { style = it }, label = { Text("نمط سكنك") })
                    OutlinedTextField(value = security, onValueChange = { security = it }, label = { Text("تأمين النوافذ والشرفات") })
                    OutlinedTextField(value = hours, onValueChange = { hours = it }, label = { Text("ساعات خروجك اليومية") })
                    OutlinedTextField(value = kids, onValueChange = { kids = it }, label = { Text("وجود الأطفال ببيتك") })
                    OutlinedTextField(value = activity, onValueChange = { activity = it }, label = { Text("شكل رعاية الأليف") })

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showSurveyDialog = false }) { Text("إلغاء") }
                        Button(onClick = {
                            viewModel.updateAdoptionPipelineAnswers(style, security, hours, kids, activity)
                            showSurveyDialog = false
                        }) {
                            Text("حفظ التغييرات")
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Main Headline
        Text(
            text = "رحلة التبني الخاصة بك 🐾",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "تابع تقدمك في العثور على رفيقك الدائم بكل طمأنينة.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Selected Pet Details Card
        Text(
            text = "أليفك القادم 🐶",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = pipeline.petImageUrl,
                        contentDescription = pipeline.petName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = pipeline.stepStatus,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = pipeline.petName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${pipeline.petBreed} • ${pipeline.petAge} • ${pipeline.petGender}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interaction helper info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Info, contentDescription = "تنبيه", tint = MaterialTheme.colorScheme.secondary)
                Text(
                    text = "سيتم التواصل معك بخصوص موعد \"استبيان تقييم المنزل\" خلال ٤٨ ساعة عمل.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // STYLED PIPELINE STEPPER
        Text(
            text = "خطوات الرحلة 👣",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassCardWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Step 1: Submission
                StepperStep(
                    title = "تقديم الطلب",
                    subtitle = "تم إرسال طلب التبني بنجاح في ${pipeline.submissionDate}.",
                    icon = Icons.Default.CheckCircle,
                    isCompleted = true,
                    isActive = false
                )

                // Step 2: Home evaluation Questionnaire
                StepperStep(
                    title = "استبيان تقييم المنزل",
                    subtitle = "المرحلة الحالية: نحن نقوم بمراجعة معايير السلامة والبيئة المناسبة للأليف.",
                    icon = Icons.Default.HomeWork,
                    isCompleted = false,
                    isActive = true,
                    onActionClick = { showSurveyDialog = true },
                    actionLabel = "تعديل الاستبيان"
                )

                // Step 3: Interview
                StepperStep(
                    title = "المقابلة الشخصية",
                    subtitle = "جلسة تعارف مباشرة بينك وبين الأليف وفريقنا المختص.",
                    icon = Icons.Default.Groups,
                    isCompleted = false,
                    isActive = false,
                    isPending = true
                )

                // Step 4: Digital Legal Contract
                StepperStep(
                    title = "توقيع العقد الرقمي",
                    subtitle = "توثيق رسمي لعملية التبني والالتزامات الصحية للحيوان الأليف.",
                    icon = Icons.Default.Draw,
                    isCompleted = false,
                    isActive = false,
                    isPending = true
                )

                // Step 5: Post Adoption Follow up
                StepperStep(
                    title = "متابعة ما بعد التبني",
                    subtitle = "نحن معك دائماً لضمان سعادتك وسعادة صديقك الجديد.",
                    icon = Icons.Default.VolunteerActivism,
                    isCompleted = false,
                    isActive = false,
                    isPending = true,
                    isLast = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // REPLICATED BENTO NETWORK (مصادر تهمك)
        Text(
            text = "مصادر تهمك 📚",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Option 1
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { showPrepModal = true }
                    .padding(16.dp)
                    .height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.LibraryBooks,
                    contentDescription = "دليل التجهيز",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text("دليل التجهيز", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("سكن آمن ومثالي", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
            }

            // Option 2
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showHealthPassport = true }
                    .padding(16.dp)
                    .height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = "السجل الصحي",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text("السجل الصحي", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("التحصينات والـ QR", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Full width Chat with expert bottom bento card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showExpertChat = true },
            colors = CardDefaults.cardColors(containerColor = SoftGoldContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "اتصل بالخبير",
                    tint = OnGoldContainer,
                    modifier = Modifier.size(40.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("تحدث مع خبير بيطري 🧑‍⚕️", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnGoldContainer)
                    Text("مستشارك الذكي المدعوم بـ Gemini AI جاهز للإجابة على جميع اسئلتك مجاناً.", fontSize = 11.sp, color = OnGoldContainer.copy(alpha = 0.8f))
                }
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "فتح المحادثة", tint = OnGoldContainer)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Custom Stepper Builder
@Composable
fun StepperStep(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isCompleted: Boolean,
    isActive: Boolean,
    isPending: Boolean = false,
    isLast: Boolean = false,
    onActionClick: (() -> Unit)? = null,
    actionLabel: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isActive -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = when {
                        isCompleted -> Color.White
                        isActive -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(64.dp)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isPending) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("قيد المراجعة", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = if (isPending) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isActive && onActionClick != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.testTag("survey_action_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(actionLabel, fontSize = 11.sp)
                }
            }
        }
    }
}


// ---------------------- 4. COMMUNITY SCREEN (SCREENSHOT 4) ----------------------
@Composable
fun CommunityScreen(viewModel: MainViewModel) {
    val incidents by viewModel.strayIncidents.collectAsState()
    var postText by remember { mutableStateOf("") }
    val listState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(listState)
            .padding(16.dp)
    ) {
        // Search & Filter headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("تواصل أبطال الرعاية 🌟", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("المناقشات المجتمعية وقصص الحيوانات المنقذة بنهج Pro-Life رحيم.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Tags selector row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val chips = listOf("الكل", "قصص نجاح", "حالات عاجلة", "مناقشات")
            chips.forEach { chip ->
                AssistChip(onClick = {}, label = { Text(chip, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Community Composer Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("أ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("شارك شيئاً مع المجتمع...", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                }

                OutlinedTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    placeholder = { Text("كيف حال أليفك؟ أو أضف سؤالاً بخصوص رعاية الشارع وتأمين طعام الشتاء...", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("community_composer_input"),
                    textStyle = TextStyle(fontSize = 13.sp),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = {}) { Icon(Icons.Default.AddPhotoAlternate, contentDescription = "إضافة صورة", tint = MaterialTheme.colorScheme.primary) }
                        IconButton(onClick = {}) { Icon(Icons.Default.LocationOn, contentDescription = "الموقع", tint = MaterialTheme.colorScheme.primary) }
                    }
                    Button(
                        onClick = {
                            if (postText.isNotBlank()) {
                                viewModel.submitNewRescue(
                                    title = "مشاركة جديدة من أبطال الرعاية",
                                    desc = postText,
                                    location = "حي الملقا، الرياض"
                                )
                                postText = ""
                            }
                        },
                        modifier = Modifier.testTag("publish_post_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("نشر", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Historical Incident List
        incidents.forEach { incident ->
            CommunityPostCard(incident = incident)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CommunityPostCard(incident: StrayIncident) {
    var likes by rememberSaveable { mutableStateOf(incident.likesCount) }
    var userLiked by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(incident.reporter.take(1), fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(incident.reporter, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(incident.timestamp, fontSize = 10.sp, color = Color.Gray)
                    }
                }

                if (incident.isEmergency) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("بلاغ عاجل 🚨", color = MaterialTheme.colorScheme.error, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body
            Text(
                text = incident.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = incident.description,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "الموقع", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text(incident.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(6.dp))

            // Engagements Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable {
                        likes = if (userLiked) likes - 1 else likes + 1
                        userLiked = !userLiked
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (userLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "أعجبني",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(likes.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Comment, contentDescription = "تعليق", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(incident.commentsCount.toString(), fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = "نشر", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}


// ---------------------- 5. GEOSPATIAL HELPMAP (SCREENSHOT 3) ----------------------
// Native Pet Place representation
data class PetPlace(
    val id: String,
    val name: String,
    val category: String,
    val type: String,
    val lat: Double,
    val lng: Double,
    val desc: String,
    val rating: String,
    val reviews: String,
    val phone: String,
    val hours: String,
    val imageUrl: String
)

// Background non-blocking fetcher for live OpenStreetMap Places via Overpass API
fun fetchNearbyPlacesFromOverpass(
    lat: Double,
    lng: Double,
    onSuccess: (List<PetPlace>) -> Unit
) {
    // Launch safe background coroutine on IO thread
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            // Overpass QL Query for all animal and pet related locations near user inside 15km (15000 meters)
            val query = """
                [out:json][timeout:15];
                (
                  node["amenity"="veterinary"](around:15000,$lat,$lng);
                  node["shop"="pet"](around:15000,$lat,$lng);
                  node["shop"="pet_grooming"](around:15000,$lat,$lng);
                  node["amenity"="animal_shelter"](around:15000,$lat,$lng);
                  node["amenity"="animal_boarding"](around:15000,$lat,$lng);
                  node["leisure"="dog_park"](around:15000,$lat,$lng);
                  
                  way["amenity"="veterinary"](around:15000,$lat,$lng);
                  way["shop"="pet"](around:15000,$lat,$lng);
                  way["shop"="pet_grooming"](around:15000,$lat,$lng);
                  way["amenity"="animal_shelter"](around:15000,$lat,$lng);
                  way["amenity"="animal_boarding"](around:15000,$lat,$lng);
                  way["leisure"="dog_park"](around:15000,$lat,$lng);
                );
                out center;
            """.trimIndent()

            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://overpass-api.de/api/interpreter?data=$encodedQuery"
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", "SafePawsRescueOSM/1.0 (yassineebouchra@gmail.com)")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val fetchedList = mutableListOf<PetPlace>()

            if (body.isNotBlank()) {
                val jsonObject = org.json.JSONObject(body)
                val elements = jsonObject.optJSONArray("elements")
                if (elements != null) {
                    for (i in 0 until elements.length()) {
                        val el = elements.getJSONObject(i)
                        val id = el.optString("id", "osm_$i")
                        
                        // Parse tags
                        val tags = el.optJSONObject("tags") ?: org.json.JSONObject()
                        var name = tags.optString("name", "")
                        val amenity = tags.optString("amenity", "")
                        val shop = tags.optString("shop", "")
                        val leisure = tags.optString("leisure", "")

                        val category = when {
                            amenity == "veterinary" -> "عيادات بيطرية 🏥"
                            amenity == "animal_shelter" -> "الملاجئ والتبني 🐶"
                            shop == "pet_grooming" -> "العناية والفنادق ✂️"
                            amenity == "animal_boarding" -> "العناية والفنادق ✂️"
                            leisure == "dog_park" -> "مراكز تدريب وحدائق 🎓"
                            else -> "متاجر ومستلزمات 🛒"
                        }

                        val type = when {
                            amenity == "veterinary" -> "clinic"
                            amenity == "animal_shelter" -> "shelter"
                            shop == "pet_grooming" -> "grooming"
                            amenity == "animal_boarding" -> "hotel"
                            leisure == "dog_park" -> "park"
                            else -> "shop"
                        }

                        if (name.isBlank()) {
                            name = when (type) {
                                "clinic" -> "عيادة بيطرية شريكة 🏥"
                                "shelter" -> "ملجأ أو نقطة إنقاذ حيوانات 🐶"
                                "grooming" -> "صالون تجميل وعناية أليف ✂️"
                                "hotel" -> "ملاذ استضافة لإقامة حيوانك 🏨"
                                "park" -> "ملتقى أليف للعب والتدريب 🎾"
                                else -> "متجر مستلزمات أليف 🛒"
                            }
                        }

                        // Determine lat/lng which might be center of way or node coordinates
                        val plat = if (el.has("lat")) el.optDouble("lat") else el.optJSONObject("center")?.optDouble("lat") ?: lat
                        val plng = if (el.has("lon")) el.optDouble("lon") else el.optJSONObject("center")?.optDouble("lon") ?: lng

                        val desc = tags.optString("description", "")
                            .ifBlank { "مرفق معتمد لخدمة ورعاية حيوانك الأليف مدعوم بنظام الخرائط الحرة المفتوحة." }
                        val rating = "4.7"
                        val reviews = "25"
                        val phone = tags.optString("phone", "").ifBlank { "+966 500200300" }
                        val hours = tags.optString("opening_hours", "").ifBlank { "٠٩:٠٠ ص - ١٠:٠٠ م" }
                        val imageUrl = if (type == "clinic") {
                            "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=400&auto=format&fit=crop&q=60"
                        } else {
                            "https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=400&auto=format&fit=crop&q=60"
                        }

                        fetchedList.add(
                            PetPlace(
                                id = id,
                                name = name,
                                category = category,
                                type = type,
                                lat = plat,
                                lng = plng,
                                desc = desc,
                                rating = rating,
                                reviews = reviews,
                                phone = phone,
                                hours = hours,
                                imageUrl = imageUrl
                            )
                        )
                    }
                }
            }

            // Switch back to Main Thread to update state safely
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                onSuccess(fetchedList)
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "Error query Overpass API: ${e.message}")
        }
    }
}

// Retrieve approximate user location via IP API as a highly accurate fallback when GPS is sluggish or unavailable
fun fetchIpLocation(onResult: (Double, Double) -> Unit) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        // Try Free IPAPI.co first (supports free HTTPS)
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val request = okhttp3.Request.Builder()
                .url("https://ipapi.co/json/")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (body.isNotBlank()) {
                val json = org.json.JSONObject(body)
                val lat = json.optDouble("latitude", Double.NaN)
                val lng = json.optDouble("longitude", Double.NaN)
                if (!lat.isNaN() && !lng.isNaN()) {
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        onResult(lat, lng)
                    }
                    return@launch
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "ipapi.co failed: ${e.message}")
        }

        // Try Free ipinfo.io as fallback (supports free HTTPS)
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val request = okhttp3.Request.Builder()
                .url("https://ipinfo.io/json")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (body.isNotBlank()) {
                val json = org.json.JSONObject(body)
                val loc = json.optString("loc", "")
                if (loc.isNotBlank() && loc.contains(",")) {
                    val parts = loc.split(",")
                    val lat = parts[0].toDoubleOrNull()
                    val lng = parts[1].toDoubleOrNull()
                    if (lat != null && lng != null) {
                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                            onResult(lat, lng)
                        }
                        return@launch
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "ipinfo.io failed: ${e.message}")
        }
    }
}

// Clean helper to parse a Firestore Feature JSON object into a PetPlace
fun parseFirestoreFeature(
    featObj: org.json.JSONObject,
    docIndex: Int,
    featIndex: Int
): PetPlace? {
    val geometryObj = featObj.optJSONObject("geometry")?.optJSONObject("mapValue")?.optJSONObject("fields") ?: return null
    val propertiesObj = featObj.optJSONObject("properties") ?: return null
    
    val coordArray = geometryObj.optJSONObject("coordinates")?.optJSONObject("arrayValue")?.optJSONArray("values") ?: return null
    if (coordArray.length() < 2) return null
    
    val lngValObj = coordArray.optJSONObject(0)
    val latValObj = coordArray.optJSONObject(1)
    
    val lng = lngValObj?.optDouble("doubleValue") ?: lngValObj?.optString("integerValue")?.toDoubleOrNull() ?: lngValObj?.optString("stringValue")?.toDoubleOrNull() ?: return null
    val lat = latValObj?.optDouble("doubleValue") ?: latValObj?.optString("integerValue")?.toDoubleOrNull() ?: latValObj?.optString("stringValue")?.toDoubleOrNull() ?: return null
    
    val propFields = propertiesObj.optJSONObject("mapValue")?.optJSONObject("fields")
    
    val name = when {
        propFields?.has("الاسم") == true -> propFields.optJSONObject("الاسم")?.optString("stringValue") ?: ""
        propFields?.has("الاسم (عربي)") == true -> propFields.optJSONObject("الاسم (عربي)")?.optString("stringValue") ?: ""
        propFields?.has("name:ar") == true -> propFields.optJSONObject("name:ar")?.optString("stringValue") ?: ""
        propFields?.has("name") == true -> propFields.optJSONObject("name")?.optString("stringValue") ?: ""
        propFields?.has("Name (EN)") == true -> propFields.optJSONObject("Name (EN)")?.optString("stringValue") ?: ""
        propFields?.has("title") == true -> propFields.optJSONObject("title")?.optString("stringValue") ?: ""
        else -> "معلم مضاف 📍"
    }.ifBlank { "معلم مضاف 📍" }
    
    val amenity = propFields?.optJSONObject("amenity")?.optString("stringValue") ?: ""
    val shop = propFields?.optJSONObject("shop")?.optString("stringValue") ?: ""
    
    val lowercaseName = name.lowercase()
    val lowercaseAmenity = amenity.lowercase()
    val lowercaseShop = shop.lowercase()
    
    val (category, type) = when {
        lowercaseAmenity.contains("veterinary") || 
        lowercaseAmenity.contains("vet") || 
        lowercaseName.contains("طبيب") || 
        lowercaseName.contains("بيطر") || 
        lowercaseName.contains("عيادة") || 
        lowercaseName.contains("دكتور") || 
        lowercaseName.contains("vet") || 
        lowercaseName.contains("clinic") -> {
            Pair("عيادات بيطرية 🏥", "clinic")
        }
        lowercaseShop.contains("pet") || 
        lowercaseShop.contains("grooming") || 
        lowercaseName.contains("متجر") || 
        lowercaseName.contains("محل") || 
        lowercaseName.contains("مستلزمات") || 
        lowercaseName.contains("shop") || 
        lowercaseName.contains("store") || 
        lowercaseName.contains("market") || 
        lowercaseName.contains("pet") -> {
            Pair("متاجر ومستلزمات 🛒", "shop")
        }
        lowercaseAmenity.contains("shelter") || 
        lowercaseName.contains("ملجأ") || 
        lowercaseName.contains("إيواء") || 
        lowercaseName.contains("adopt") -> {
            Pair("الملاجئ والتبني 🐶", "shelter")
        }
        lowercaseAmenity.contains("boarding") || 
        lowercaseName.contains("فندق") || 
        lowercaseName.contains("فنادق") || 
        lowercaseName.contains("رعاية") -> {
            Pair("العناية والفنادق ✂️", "grooming")
        }
        else -> {
            Pair("متاجر ومستلزمات 🛒", "shop")
        }
    }
    
    val street = propFields?.optJSONObject("addr:street")?.optString("stringValue") ?: ""
    val city = propFields?.optJSONObject("addr:city")?.optString("stringValue") ?: ""
    val phone = propFields?.optJSONObject("phone")?.optString("stringValue") ?: ""
    val website = propFields?.optJSONObject("website")?.optString("stringValue") ?: ""
    
    val descBuilder = java.lang.StringBuilder()
    if (city.isNotBlank()) descBuilder.append("المدينة: $city | ")
    if (street.isNotBlank()) descBuilder.append("الشارع: $street | ")
    if (phone.isNotBlank()) descBuilder.append("الهاتف: $phone | ")
    if (website.isNotBlank()) descBuilder.append("الموقع: $website")
    
    val desc = descBuilder.toString().removeSuffix(" | ").trim().ifBlank { 
        "موقع تفاعلي مضاف لخدمة ورعاية الحيوانات الأليفة."
    }
    
    return PetPlace(
        id = "live_firestore_${featIndex}_${docIndex}",
        name = name,
        category = category,
        type = type,
        lat = lat,
        lng = lng,
        desc = desc,
        rating = "5.0",
        reviews = "موقع",
        phone = phone.ifBlank { "غير متوفر" },
        hours = "٢٤ ساعة",
        imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400&auto=format&fit=crop&q=60"
    )
}

// Clean parser tool to fetch and convert geojson points from specified url path
fun fetchGeoJsonFromUrl(url: String, onResult: (List<PetPlace>) -> Unit) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val list = mutableListOf<PetPlace>()
        try {
            val apiKey = "AIzaSyAu2iWAA8WmMdkMRAC95FneRF-oV8Hafh4"
            val runQueryUrl = "https://firestore.googleapis.com/v1/projects/corded-principle-5dzmz/databases/ai-studio-ca385af0-d7d1-4e5a-90f0-6ed01800913a/documents:runQuery?key=$apiKey"
            
            val queryBodyJson = """
            {
              "structuredQuery": {
                "from": [
                  {
                    "collectionId": "layers",
                    "allDescendants": false
                  }
                ]
              }
            }
            """.trimIndent()
            
            val body = okhttp3.RequestBody.create(null, queryBodyJson)
            val request = okhttp3.Request.Builder()
                .url(runQueryUrl)
                .post(body)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Accept", "application/json, text/plain, */*")
                .build()
                
            val response = client.newCall(request).execute()
            val responseStr = response.body?.string() ?: ""
            if (responseStr.isNotBlank() && responseStr.trim().startsWith("[")) {
                val rootArr = org.json.JSONArray(responseStr)
                for (docIndex in 0 until rootArr.length()) {
                    val layerObj = rootArr.optJSONObject(docIndex) ?: continue
                    val docObj = layerObj.optJSONObject("document") ?: continue
                    val fieldsObj = docObj.optJSONObject("fields") ?: continue
                    
                    val isChunked = fieldsObj.optJSONObject("isChunked")?.optBoolean("booleanValue") ?: false
                    
                    if (!isChunked) {
                        val dataMapObj = fieldsObj.optJSONObject("data")?.optJSONObject("mapValue")?.optJSONObject("fields") ?: continue
                        val featuresArrObj = dataMapObj.optJSONObject("features")?.optJSONObject("arrayValue")?.optJSONArray("values") ?: continue
                        
                        for (featIndex in 0 until featuresArrObj.length()) {
                            val featObj = featuresArrObj.optJSONObject(featIndex)?.optJSONObject("mapValue")?.optJSONObject("fields") ?: continue
                            val place = parseFirestoreFeature(featObj, docIndex, featIndex)
                            if (place != null) {
                                list.add(place)
                            }
                        }
                    } else {
                        val docPath = docObj.optString("name")
                        if (docPath.isNotBlank()) {
                            val chunksQueryUrl = "https://firestore.googleapis.com/v1/${docPath}:runQuery?key=$apiKey"
                            val chunkQueryBodyJson = """
                            {
                              "structuredQuery": {
                                "from": [
                                  {
                                    "collectionId": "chunks",
                                    "allDescendants": false
                                  }
                                ]
                              }
                            }
                            """.trimIndent()
                            
                            val chunkBody = okhttp3.RequestBody.create(null, chunkQueryBodyJson)
                            val chunkReq = okhttp3.Request.Builder()
                                .url(chunksQueryUrl)
                                .post(chunkBody)
                                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                                .header("Accept", "application/json, text/plain, */*")
                                .build()
                                
                            try {
                                val chunkResp = client.newCall(chunkReq).execute()
                                val chunkRespStr = chunkResp.body?.string() ?: ""
                                if (chunkRespStr.isNotBlank() && chunkRespStr.trim().startsWith("[")) {
                                    val chunkArr = org.json.JSONArray(chunkRespStr)
                                    for (chunkIndex in 0 until chunkArr.length()) {
                                        val chunkWrapObj = chunkArr.optJSONObject(chunkIndex) ?: continue
                                        val chunkDocObj = chunkWrapObj.optJSONObject("document") ?: continue
                                        val chunkFieldsObj = chunkDocObj.optJSONObject("fields") ?: continue
                                        
                                        val chunkFeaturesField = chunkFieldsObj.optJSONObject("features") ?: continue
                                        val chunkFeaturesArr = chunkFeaturesField.optJSONObject("arrayValue")?.optJSONArray("values") ?: continue
                                        
                                        for (featIndex in 0 until chunkFeaturesArr.length()) {
                                            val featObj = chunkFeaturesArr.optJSONObject(featIndex)?.optJSONObject("mapValue")?.optJSONObject("fields") ?: continue
                                            val place = parseFirestoreFeature(featObj, docIndex * 10000 + chunkIndex, featIndex)
                                            if (place != null) {
                                                list.add(place)
                                            }
                                        }
                                    }
                                }
                            } catch (chunkEx: Exception) {
                                android.util.Log.e("MapScreen", "Failed to fetch chunk: ${chunkEx.message}")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "Failed to fetch live point layers from Firestore: ${e.message}")
        }
        
        // Back to main thread with places list
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            onResult(list)
        }
    }
}

fun parseGeoJsonStringToPlaces(jsonStr: String): List<PetPlace> {
    if (jsonStr.isBlank() || !jsonStr.contains("{")) return emptyList()
    val list = mutableListOf<PetPlace>()
    try {
        val obj = org.json.JSONObject(jsonStr)
        val features = if (obj.has("features")) {
            obj.getJSONArray("features")
        } else if (obj.has("type") && obj.getString("type") == "Feature") {
            org.json.JSONArray().put(obj)
        } else {
            return emptyList()
        }
        
        for (i in 0 until features.length()) {
            val f = features.getJSONObject(i)
            val geometry = f.optJSONObject("geometry") ?: continue
            val properties = f.optJSONObject("properties") ?: org.json.JSONObject()
            val coordinates = geometry.optJSONArray("coordinates") ?: continue
            
            val lng = coordinates.getDouble(0)
            val lat = coordinates.getDouble(1)
            
            val name = when {
                properties.has("الاسم (عربي)") -> properties.getString("الاسم (عربي)")
                properties.has("Name (EN)") -> properties.getString("Name (EN)")
                properties.has("name") -> properties.getString("name")
                properties.has("title") -> properties.getString("title")
                properties.has("الاسم") -> properties.getString("الاسم")
                properties.has("النهر") -> properties.getString("النهر")
                properties.has("الإقليم") -> properties.getString("الإقليم")
                else -> "موقع تفاعلي مضاف 📍"
            }
            
            val desc = when {
                properties.has("الوصف") -> properties.getString("الوصف")
                properties.has("description") -> properties.getString("description")
                properties.has("desc") -> properties.getString("desc")
                else -> "نقطة مستوردة من الموقع التفاعلي الخارجي."
            }
            
            val category = properties.optString("category", "religion")
            val type = properties.optString("type", "shelter")
            
            val mappedCategoryAndType = when {
                category.contains("vet") || category.contains("clinic") || desc.contains("طبيب") || desc.contains("عيادة") -> {
                    Pair("عيادات بيطرية 🏥", "clinic")
                }
                category.contains("store") || category.contains("shop") || desc.contains("متجر") || desc.contains("محل") -> {
                    Pair("متاجر ومستلزمات 🛒", "shop")
                }
                category.contains("shelter") || category.contains("adopt") || desc.contains("ملجأ") || desc.contains("إيواء") -> {
                    Pair("الملاجئ والتبني 🐶", "shelter")
                }
                category.contains("park") || category.contains("training") || desc.contains("حديقة") || desc.contains("منتزه") -> {
                    Pair("مراكز تدريب وحدائق 🎓", "park")
                }
                else -> {
                    Pair("النقاط المستوردة 🌐", "clinic")
                }
            }
            
            list.add(
                PetPlace(
                    id = "live_web_${i}_${(100..999).random()}",
                    name = name,
                    category = mappedCategoryAndType.first,
                    type = mappedCategoryAndType.second,
                    lat = lat,
                    lng = lng,
                    desc = desc,
                    rating = "5.0",
                    reviews = "موقع",
                    phone = properties.optString("phone", "-"),
                    hours = "٢٤ ساعة",
                    imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400&auto=format&fit=crop&q=60"
                )
            )
        }
    } catch (e: Exception) {
        android.util.Log.e("MapScreen", "Error parsing live GeoJSON string: ${e.message}")
    }
    return list
}

// Keyless OSM-based Geocoding to translate user text queries into geo coordinates
fun geocodeAddress(query: String, onResult: (Double?, Double?) -> Unit) {
    if (query.isBlank()) {
        onResult(null, null)
        return
    }
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(6, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(6, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val request = okhttp3.Request.Builder()
                .url("https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=1")
                .header("User-Agent", "SafePawsApp/1.0 (Android Emulator; yassineebouchra@gmail.com)")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (body.isNotBlank()) {
                val jsonArray = org.json.JSONArray(body)
                if (jsonArray.length() > 0) {
                    val firstItem = jsonArray.getJSONObject(0)
                    val latStr = firstItem.optString("lat", "NaN")
                    val lngStr = firstItem.optString("lon", "NaN")
                    val lat = latStr.toDoubleOrNull() ?: Double.NaN
                    val lng = lngStr.toDoubleOrNull() ?: Double.NaN
                    if (!lat.isNaN() && !lng.isNaN()) {
                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                            onResult(lat, lng)
                        }
                        return@launch
                    }
                }
            }
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                onResult(null, null)
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "Geocoding failed for '$query': ${e.message}")
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                onResult(null, null)
            }
        }
    }
}

// Create beautiful, custom map marker pins programmatically utilizing custom color coding of categories and relevant pet/care emojis
fun createMarkerIcon(context: android.content.Context, category: String): android.graphics.drawable.Drawable {
    val density = context.resources.displayMetrics.density
    val size = (48 * density).toInt() // Ensure proportional touch target visual representation (around 48dp)
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    
    // Determine color and emoji based on category
    val (pinColor, emoji) = when (category) {
        "عيادات بيطرية 🏥" -> Pair(android.graphics.Color.parseColor("#EF4444"), "🏥") // Crimson Red for Urgency/Health
        "متاجر ومستلزمات 🛒" -> Pair(android.graphics.Color.parseColor("#F97316"), "🛒") // Bright Orange for Retail
        "الملاجئ والتبني 🐶" -> Pair(android.graphics.Color.parseColor("#3B82F6"), "🐶") // Warm Blue for Shelter care
        "مراكز تدريب وحدائق 🎓" -> Pair(android.graphics.Color.parseColor("#10B981"), "🎓") // Emerald Green for Parks
        "العناية والفنادق ✂️" -> Pair(android.graphics.Color.parseColor("#8B5CF6"), "✂️") // Royal Purple for Grooming
        "بلاغات مفقودة 🚨" -> Pair(android.graphics.Color.parseColor("#EF1253"), "🚨") // Bright Warning Alert
        "النقاط المستوردة 🌐" -> Pair(android.graphics.Color.parseColor("#0D9488"), "🌐") // Teal for imported layers
        "موقعي الحالي" -> Pair(android.graphics.Color.parseColor("#06B6D4"), "🐾") // Cool Cyan with Cute Paw standard
        else -> Pair(android.graphics.Color.parseColor("#6B7280"), "📍") // Sleek Gray fallback
    }
    
    val centerX = size / 2f
    val centerY = size / 2.5f
    val radius = size / 3.2f
    
    // Draw marker shadow (beautiful soft dark blur)
    paint.color = android.graphics.Color.parseColor("#1F000000")
    canvas.drawCircle(centerX, centerY + (4 * density), radius + (2 * density), paint)
    
    // Draw pin pointer triangle pointing down
    val path = android.graphics.Path()
    path.moveTo(centerX - radius * 0.7f, centerY + radius * 0.5f)
    path.lineTo(centerX, size * 0.88f)
    path.lineTo(centerX + radius * 0.7f, centerY + radius * 0.5f)
    path.close()
    
    paint.color = pinColor
    canvas.drawPath(path, paint)
    
    // Draw outer pin circle boundary
    canvas.drawCircle(centerX, centerY, radius, paint)
    
    // Draw white border contour
    paint.color = android.graphics.Color.WHITE
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 2f * density
    canvas.drawCircle(centerX, centerY, radius, paint)
    
    // Draw inner white circle badge
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(centerX, centerY, radius * 0.75f, paint)
    
    // Draw the emoji icon centered in the badge
    paint.color = android.graphics.Color.BLACK
    paint.textSize = radius * 0.95f
    paint.textAlign = android.graphics.Paint.Align.CENTER
    val fontMetrics = paint.fontMetrics
    val textY = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2f
    canvas.drawText(emoji, centerX, textY, paint)
    
    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}

@Composable
fun MapScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("الكل") }
    var selectedPlace by remember { mutableStateOf<PetPlace?>(null) }
    var isMapLoading by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }

    val incidentsList by viewModel.strayIncidents.collectAsState()

    // Start centered on Riyadh (24.7136, 46.6753)
    var mapCenter by remember { mutableStateOf(GeoPoint(24.7136, 46.6753)) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var placesList by remember { mutableStateOf<List<PetPlace>>(emptyList()) }
    var webPlacesList by remember { mutableStateOf<List<PetPlace>>(emptyList()) }

    // Keep direct reference to the MapView to coordinate programmatic shifts cleanly without recomposition feedback-loops
    var mapView by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(mapCenter) {
        mapView?.let { mv ->
            mv.controller.animateTo(mapCenter)
        }
    }

    // Configure continuous GPS and Network provider location tracking to dynamically update real-time user point on map
    val locationListener = remember {
        object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                val gp = GeoPoint(loc.latitude, loc.longitude)
                userLocation = gp
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
    }

    DisposableEffect(context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000L, 2f, locationListener)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000L, 2f, locationListener)
            }
        } catch (e: SecurityException) {
            // Safe fallback
        }
        onDispose {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (e: Exception) {
                // Safe disposal
            }
        }
    }

    // Initialize osmdroid tile cache configuration with correct user-agent on launch
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName

        // Load initial live OSM places around current center coordinates via Overpass
        isMapLoading = true
        fetchNearbyPlacesFromOverpass(mapCenter.latitude, mapCenter.longitude) { fetched ->
            placesList = fetched
            isMapLoading = false
        }
    }

    // Real-time periodic website sync poller: polls every 10 seconds to grab any dashboard changes instantly!
    LaunchedEffect(Unit) {
        while (true) {
            isSyncing = true
            fetchGeoJsonFromUrl("https://interactive-world-map-and-geojson-viewer-716619948402.europe-west3.run.app/") { fetchedWebPlaces ->
                if (fetchedWebPlaces.isNotEmpty()) {
                    webPlacesList = fetchedWebPlaces
                }
                isSyncing = false
            }
            kotlinx.coroutines.delay(10000L)
        }
    }

    // Dynamic standard location request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fineGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                val providers = locationManager.getProviders(true)
                var bestLoc: Location? = null
                for (p in providers) {
                    val l = locationManager.getLastKnownLocation(p) ?: continue
                    if (bestLoc == null || l.accuracy < bestLoc.accuracy) {
                        bestLoc = l
                    }
                }
                bestLoc?.let {
                    val gp = GeoPoint(it.latitude, it.longitude)
                    userLocation = gp
                    mapCenter = gp

                    isMapLoading = true
                    fetchNearbyPlacesFromOverpass(it.latitude, it.longitude) { fetched ->
                        placesList = fetched
                        isMapLoading = false
                    }
                }
            } catch (e: SecurityException) {
                // Ignore silent security exceptions
            }
        }
    }

    // Launch location prompt as soon as user opens map, alongside reliable IP API fallback
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        fetchIpLocation { lat, lng ->
            if (userLocation == null) {
                val gp = GeoPoint(lat, lng)
                mapCenter = gp
                isMapLoading = true
                fetchNearbyPlacesFromOverpass(lat, lng) { fetched ->
                    placesList = fetched
                    isMapLoading = false
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true // Scales map tile images cleanly for crisp look and normalized touch density
                    setBuiltInZoomControls(false) // Disable clunky overlay buttons; standard pinch-to-zoom is intuitive and responsive
                    minZoomLevel = 4.0
                    maxZoomLevel = 20.0
                    controller.setZoom(14.0)
                    controller.setCenter(mapCenter)
                    
                    // Prevent parents from intercepting touch gestures when panning/zooming, keeping the map container strictly stable and fixed in place
                    setOnTouchListener { v, event ->
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    mapView = this
                }
            },
            update = { mv ->
                mv.overlays.clear()

                // 1. Draw user standard location pin if loaded
                userLocation?.let { loc ->
                    val userM = Marker(mv).apply {
                        position = loc
                        title = "موقعي الحالي 🐾"
                        icon = createMarkerIcon(context, "موقعي الحالي")
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mv.overlays.add(userM)
                }

                // 2. Filter and Draw nearby pet services points (OSM + dynamically fetched website map pins)
                val combinedPlaces = (placesList + webPlacesList).distinctBy { it.id }
                combinedPlaces.forEach { place ->
                    val matchesQuery = searchQuery.isBlank() || 
                                       place.name.contains(searchQuery, ignoreCase = true) || 
                                       place.desc.contains(searchQuery, ignoreCase = true)
                    
                    val matchesCategory = filterCategory == "الكل" || place.category == filterCategory

                    if (matchesQuery && matchesCategory) {
                        val emoji = when (place.category) {
                            "عيادات بيطرية 🏥" -> "🏥"
                            "متاجر ومستلزمات 🛒" -> "🛒"
                            "الملاجئ والتبني 🐶" -> "🐶"
                            "مراكز تدريب وحدائق 🎓" -> "🎓"
                            "العناية والفنادق ✂️" -> "✂️"
                            "النقاط المستوردة 🌐" -> "🌐"
                            else -> "📍"
                        }
                        val pm = Marker(mv).apply {
                            position = GeoPoint(place.lat, place.lng)
                            title = "$emoji ${place.name}"
                            subDescription = place.desc
                            icon = createMarkerIcon(context, place.category)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { marker, map ->
                                selectedPlace = place
                                mapCenter = marker.position as GeoPoint
                                true
                            }
                        }
                        mv.overlays.add(pm)
                    }
                }

                // 3. Filter and Draw emergency rescue incidents (from Community Tab) as warning markers on the map
                incidentsList.forEach { incident ->
                    val matchesQuery = searchQuery.isBlank() || 
                                       incident.title.contains(searchQuery, ignoreCase = true) || 
                                       incident.description.contains(searchQuery, ignoreCase = true)
                    
                    val matchesCategory = filterCategory == "الكل" || filterCategory == "بلاغات مفقودة 🚨"

                    if (matchesQuery && matchesCategory) {
                        val pm = Marker(mv).apply {
                            position = GeoPoint(incident.lat, incident.lng)
                            title = "🚨 بلاغ مفقود: ${incident.title}"
                            subDescription = "${incident.description}\nالموقع: ${incident.location}\nالمبلّغ: ${incident.reporter}"
                            icon = createMarkerIcon(context, "بلاغات مفقودة 🚨")
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { marker, map ->
                                selectedPlace = PetPlace(
                                    id = incident.id,
                                    name = "بلاغ مفقود: ${incident.title}",
                                    category = "بلاغات مفقودة 🚨",
                                    type = "emergency",
                                    lat = incident.lat,
                                    lng = incident.lng,
                                    desc = incident.description,
                                    rating = "٥.٠",
                                    reviews = "مغلق",
                                    phone = "بلاغ من المجتمع",
                                    hours = incident.timestamp,
                                    imageUrl = ""
                                )
                                mapCenter = marker.position as GeoPoint
                                true
                            }
                        }
                        mv.overlays.add(pm)
                    }
                }

                mv.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // 1. Top Controls: Search Bar + Live Connection Pulse + Category Filters
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 28.dp) // Leave clean space for edge-to-edge status bar
        ) {
            // Elegant real-time search & live sync header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("map_search_field"),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        ),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "البحث عن عيادات، متاجر، بلاغات مفقودة...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))

                    // Live Sync status pulse indicating dynamic website updates
                    Row(
                        modifier = Modifier
                            .background(
                                color = if (isSyncing) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) 
                                        else Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pulseAlpha by rememberInfiniteTransition(label = "").animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = ""
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    color = if (isSyncing) MaterialTheme.colorScheme.primary 
                                            else Color(0xFF10B981)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSyncing) "مزامنة..." else "مباشر 🌐",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSyncing) MaterialTheme.colorScheme.primary 
                                    else Color(0xFF047857)
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable dynamic M3 category filter chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chipsList = listOf(
                    "الكل",
                    "عيادات بيطرية 🏥",
                    "متاجر ومستلزمات 🛒",
                    "الملاجئ والتبني 🐶",
                    "العناية والفنادق ✂️",
                    "بلاغات مفقودة 🚨",
                    "النقاط المستوردة 🌐"
                )
                chipsList.forEach { cat ->
                    val isSelected = filterCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterCategory = cat },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }

        // Loading cover
        if (isMapLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
            }
        }

        // Expanded Place Detail Floating card
        selectedPlace?.let { place ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(14.dp)
                    .padding(bottom = 6.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = place.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("•", color = Color.Gray)
                                    Text(
                                        text = "⭐ ${place.rating} (${place.reviews} مراجعة)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            IconButton(
                                onClick = { selectedPlace = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = place.desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "🕒 ساعات العمل:", fontSize = 9.sp, color = Color.Gray)
                                Text(text = place.hours, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "📞 هاتف الخدمة:", fontSize = 9.sp, color = Color.Gray)
                                Text(text = place.phone, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("phone", place.phone)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "تم نسخ الرقم: ${place.phone}", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "اتصال", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اتصال", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val gmmIntentUri = android.net.Uri.parse("geo:0,0?q=${place.lat},${place.lng}(${place.name})")
                                    val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(mapIntent)
                                    } else {
                                        val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${place.lat},${place.lng}"))
                                        context.startActivity(webIntent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = "الاتجاهات", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الاتجاهات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- 6. SMART PROFILE SCREEN (SCREENSHOT 1) ----------------------
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val trustScore by viewModel.trustScore.collectAsState()
    val rescuesCount by viewModel.rescuesCount.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Profile Card
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.size(100.dp)) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCdp1UGfnHAUK_JXoOnOPxaqIHc9L4POaavlf0dE2wZljvZ9i2dqiz2oy-r_yMY7edj-X4SXaiVTaJ86gv8lVnG9BYmp-p9kQN-ZDHu51w4p9Q9R4Nb9ZfkaUfCqQbGPDLIlonO6wGSlK9-7enVQdYBiZq6qymvru2FyHLm6F63TzZ6l8N-rPy_zgHKrH9JjjVEsVLr_HrSjoy88rAc7bBupYGyLiFNfe4vLIoM-hz_pMs7oqig3zEGJSgqe1Rq9dZUNpPBScufFhE",
                    contentDescription = "الملف الشخصي لأحمد محمد",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Verified ID check
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = "حساب موثق الهوية", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Username & Location
        Text(
            text = "أحمد محمد",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "الموقع", tint = Color.Gray, modifier = Modifier.size(14.dp))
            Text("الرياض، المملكة العربية السعودية", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // GAMIFICATIONS BADGES ROW (نقاط الثقة، عمليات الإنقاذ، منشورات)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GamificationBadge(label = "المنشورات", count = "١٢")
            GamificationBadge(label = "الإنقاذ 🐾", count = rescuesCount.toString())
            GamificationBadge(label = "نقاط الثقة 🛡️", count = trustScore.toString(), isHighlight = true)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Edit Profile Button
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = "تعديل")
                Text("تعديل ملف أبطال الرعاية")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Personal story section "قصتي"
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "قصتي 📖",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "متطوع شغوف بإنقاذ الحيوانات الأليفة وتوفير بيئة آمنة لها. 🐾 عضو نشط في بيت الأمان منذ عام ٢٠٢٣. نسعى دائماً لجعل العالم مكاناً أفضل لكل روح بيئية بريئة.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SMART REVENUE HISTORIES OR HIGHLIGHTS 
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "القصص المميزة والأنشطة 🌟",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("قصص الإنقاذ 🛡️", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text("استعراض مسيرة البلاغات", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الاستضافة (Fostering)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text("الرعايات المؤقتة النشطة", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun GamificationBadge(label: String, count: String, isHighlight: Boolean = false) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.size(width = 100.dp, height = 80.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else Color.Gray
            )
        }
    }
}


// ---------------------- 7. INTERACTIVE CHATBOT (GEMINI EXPERT CALLS) ----------------------
@Composable
fun ChatBotLayout(viewModel: MainViewModel, onClose: () -> Unit) {
    val messages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    var inputMsgText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧑‍⚕️", fontSize = 18.sp)
                }
                Column {
                    Text("مستشارك الطبي والخبير الذكي", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text("مدعوم بـ Gemini 3.5 Flash", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
            }
        }

        // Messages Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(LightSurfaceLowe)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    val isUser = message.isUser
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 0.dp,
                                        bottomEnd = if (isUser) 0.dp else 16.dp
                                    )
                                )
                                .background(
                                    if (isUser) MaterialTheme.colorScheme.primary 
                                    else Color.White
                                )
                                .border(
                                    width = if (isUser) 0.dp else 1.dp,
                                    color = if (isUser) Color.Transparent else Color.LightGray.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isUser) 16.dp else 0.dp, bottomEnd = if (isUser) 0.dp else 16.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = message.text,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (isChatLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Text("يفكر المستشار الذكي...", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Input Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputMsgText,
                onValueChange = { inputMsgText = it },
                keyboardActions = KeyboardActions(onSend = {
                    if (inputMsgText.isNotBlank()) {
                        viewModel.sendChatMessage(inputMsgText)
                        inputMsgText = ""
                    }
                }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                placeholder = { Text("اسأل عن وجبات بندق، طعام القطط الرطب، جرعات الديدان...", fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text_field"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            IconButton(
                onClick = {
                    if (inputMsgText.isNotBlank()) {
                        viewModel.sendChatMessage(inputMsgText)
                        inputMsgText = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .testTag("send_chat_message_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "أرسل", tint = Color.White)
            }
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Text(text, fontSize = 12.sp, lineHeight = 16.sp)
    }
}
