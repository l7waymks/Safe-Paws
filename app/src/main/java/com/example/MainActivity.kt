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

@Composable
fun MapScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val incidents by viewModel.strayIncidents.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }
    var filterCategory by remember { mutableStateOf("الكل") }
    
    val prefs = remember { context.getSharedPreferences("safepaws_settings", android.content.Context.MODE_PRIVATE) }
    val savedLat = remember { prefs.getFloat("user_lat", 0f).toDouble() }
    val savedLng = remember { prefs.getFloat("user_lng", 0f).toDouble() }

    // Live GPS Location coordinates (load saved location or fallback to null)
    var userLatitude by remember {
        mutableStateOf<Double?>(
            if (savedLat != 0.0) savedLat else null
        )
    }
    var userLongitude by remember {
        mutableStateOf<Double?>(
            if (savedLng != 0.0) savedLng else null
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    var isGpsEnabled by remember { mutableStateOf(false) }
    var gpsAccuracy by remember { mutableStateOf<Float?>(null) }
    var lastLoggedLocation by remember { mutableStateOf<Location?>(null) }
    var deviceBearing by remember { mutableStateOf(0f) }

    // Dynamic checks of phone's real GPS status
    LaunchedEffect(Unit) {
        while (true) {
            val mgr = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            isGpsEnabled = mgr?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
            kotlinx.coroutines.delay(2000)
        }
    }

    // High fidelity compass / device heading sensor registration with dual-method fallback
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? android.hardware.SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        val magSensor = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD)

        val listener = object : android.hardware.SensorEventListener {
            var gravity: FloatArray? = null
            var geomagnetic: FloatArray? = null

            override fun onSensorChanged(event: android.hardware.SensorEvent) {
                if (event.sensor.type == android.hardware.Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    android.hardware.SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientationValues = FloatArray(3)
                    android.hardware.SensorManager.getOrientation(rotationMatrix, orientationValues)
                    val azimuthInDegrees = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                    deviceBearing = (azimuthInDegrees + 360f) % 360f
                } else {
                    if (event.sensor.type == android.hardware.Sensor.TYPE_ACCELEROMETER) {
                        gravity = event.values.clone()
                    }
                    if (event.sensor.type == android.hardware.Sensor.TYPE_MAGNETIC_FIELD) {
                        geomagnetic = event.values.clone()
                    }
                    if (gravity != null && geomagnetic != null) {
                        val r = FloatArray(9)
                        val i = FloatArray(9)
                        if (android.hardware.SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                            val orientation = FloatArray(3)
                            android.hardware.SensorManager.getOrientation(r, orientation)
                            val azimuthInDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            deviceBearing = (azimuthInDegrees + 360f) % 360f
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: android.hardware.Sensor, accuracy: Int) {}
        }

        sensorManager?.let { sm ->
            rotationSensor?.let { sm.registerListener(listener, it, android.hardware.SensorManager.SENSOR_DELAY_UI) }
            accelSensor?.let { sm.registerListener(listener, it, android.hardware.SensorManager.SENSOR_DELAY_UI) }
            magSensor?.let { sm.registerListener(listener, it, android.hardware.SensorManager.SENSOR_DELAY_UI) }
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Free OpenStreetMap layout setup
    var lastSearchedLat by remember { mutableStateOf(if (savedLat != 0.0) savedLat else 24.7136) }
    var lastSearchedLng by remember { mutableStateOf(if (savedLng != 0.0) savedLng else 46.6753) }
    var mapCenterLat by remember { mutableStateOf(if (savedLat != 0.0) savedLat else 24.7136) }
    var mapCenterLng by remember { mutableStateOf(if (savedLng != 0.0) savedLng else 46.6753) }
    var fetchedPlacesList by remember { mutableStateOf<List<PetPlace>>(emptyList()) }
    var selectedPlace by remember { mutableStateOf<PetPlace?>(null) }
    var selectedMapStyle by remember { mutableStateOf("voyager") }

    // Center map automatically when location loads initially
    var hasCenteredMapInitially by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(userLatitude, userLongitude) {
        val lat = userLatitude
        val lng = userLongitude
        if (lat != null && lng != null && !hasCenteredMapInitially) {
            lastSearchedLat = lat
            lastSearchedLng = lng
            mapCenterLat = lat
            mapCenterLng = lng
            hasCenteredMapInitially = true

            // Live Fetch around current GPS Center coordinates via Overpass API
            fetchNearbyPlacesFromOverpass(lat, lng) { results ->
                fetchedPlacesList = results
            }
        }
    }

    // Load last known GPS location as center until first precision lock arrives
    LaunchedEffect(Unit) {
        val savedLatVal = prefs.getFloat("user_lat", 0f).toDouble()
        val savedLngVal = prefs.getFloat("user_lng", 0f).toDouble()
        if (savedLatVal != 0.0 && savedLngVal != 0.0) {
            userLatitude = savedLatVal
            userLongitude = savedLngVal
            mapCenterLat = savedLatVal
            mapCenterLng = savedLngVal
            android.util.Log.d("MapScreen", "Loaded last verified GPS location on startup: $savedLatVal, $savedLngVal")
            fetchNearbyPlacesFromOverpass(savedLatVal, savedLngVal) { results ->
                fetchedPlacesList = results
            }
        }
    }

    // Retrieve active location via GPS with proper unregistration on lifecycle changes
    DisposableEffect(hasLocationPermission, lifecycleOwner) {
        var areUpdatesRegistered = false
        var listener: LocationListener? = null
        var manager: LocationManager? = null

        fun startLocationUpdates() {
            val isFineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!isFineGranted) {
                android.util.Log.d("MapScreen", "Cannot start location updates: ACCESS_FINE_LOCATION not granted yet.")
                return
            }
            try {
                val appContext = context.applicationContext
                if (manager == null) {
                    manager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                }
                isGpsEnabled = manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                
                // Set up our high-precision location listner with physical filters
                if (listener == null) {
                    listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            val acc = location.accuracy
                            gpsAccuracy = acc
                            
                            // 9. If accuracy is greater than 50m, discard
                            if (acc > 50f) {
                                android.util.Log.w("MapScreen", "Discarded poor location accuracy: ${acc} meters")
                                return
                            }
                            
                            // 8. Distance and physical speed check
                            val prev = lastLoggedLocation
                            val nowTime = System.currentTimeMillis()
                            if (prev != null) {
                                val distance = prev.distanceTo(location)
                                val timeDeltaSeconds = (nowTime - prev.time) / 1000f
                                if (timeDeltaSeconds > 0.5f) {
                                    val speed = distance / timeDeltaSeconds
                                    if (speed > 150f) { // Stats jump anomalous speed (> 540 km/h)
                                        android.util.Log.e("MapScreen", "Anomalous speed jump detected: ${speed}m/s. Discarding location update.")
                                        return
                                    }
                                }
                            }
                            
                            // Active verified parameters
                            userLatitude = location.latitude
                            userLongitude = location.longitude
                            lastLoggedLocation = location
                            
                            // Persistence
                            prefs.edit()
                                .putFloat("user_lat", location.latitude.toFloat())
                                .putFloat("user_lng", location.longitude.toFloat())
                                .apply()
                        }
                    }
                }
                
                if (!areUpdatesRegistered && manager != null && listener != null) {
                    manager!!.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L,
                        1f,
                        listener!!
                    )
                    manager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000L,
                        2f,
                        listener!!
                    )
                    areUpdatesRegistered = true
                    android.util.Log.d("MapScreen", "Successfully registered GPS/Network updates")
                }
            } catch (e: Exception) {
                android.util.Log.e("MapScreen", "Error starting location updates: ${e.message}")
            }
        }

        fun stopLocationUpdates() {
            if (manager != null && listener != null && areUpdatesRegistered) {
                try {
                    manager!!.removeUpdates(listener!!)
                    android.util.Log.d("MapScreen", "Successfully removed location updates listener")
                } catch (e: Exception) {
                    android.util.Log.e("MapScreen", "Error removing location updates: ${e.message}")
                }
            }
        }

        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME && hasLocationPermission) {
                startLocationUpdates()
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                stopLocationUpdates()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        if (hasLocationPermission) {
            startLocationUpdates()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stopLocationUpdates()
        }
    }

    val combinedPlaces = remember(fetchedPlacesList, incidents) {
        val predefinedSpots = listOf(
            PetPlace("clinic_1", "مستشفى العاصمة البيطري 🏥", "عيادات بيطرية 🏥", "clinic", 24.7012, 46.6853, "مستشفى متكامل لتقديم خدمات الطوارئ الطبية والجراحة والرعاية السريرية الفائقة بمميزات عالمية.", "4.8", "210", "+966 500001234", "مفتوح ٢٤ ساعة طوارئ", "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=400&auto=format&fit=crop&q=60"),
            PetPlace("clinic_2", "عيادة د. فهد للحيوانات الأليفة 🏥", "عيادات بيطرية 🏥", "clinic", 24.6852, 46.7214, "تشخيص متقدم، تطعيمات، سونار، ورعاية متميزة لكل أنواع القطط والكلاب رعاية تامة.", "4.6", "115", "+966 507894561", "٠٩:٠٠ ص - ١٠:٠٠ م", "https://images.unsplash.com/photo-1516600118604-51a31610c14b?w=400&auto=format&fit=crop&q=60"),
            PetPlace("shelter_1", "جمعية رفق لملاقئ الحيوانات 🐶", "الملاجئ والتبني 🐶", "shelter", 24.7584, 46.6432, "ملجأ خيري إنساني لإيواء الكلاب والقطط الضالة وتهيئتها للتبني في بيوت حنونة وآمنة.", "4.9", "154", "+966 501234567", "٠٨:٠٠ ص - ٠٩:٠٠ م", "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=400&auto=format&fit=crop&q=60"),
            PetPlace("shelter_2", "بيت الأمان لإنقاذ الحيوان 🐶", "الملاجئ والتبني 🐶", "shelter", 24.7153, 46.6268, "موقع إنقاذ وملاجئ القطط لتقديم الاستضافة الطبية المؤقتة والتبني الفوري المجاني لمستحقيها.", "4.7", "89", "+966 551122334", "٠٩:٠٠ ص - ٠٦:٠٠ م", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400&auto=format&fit=crop&q=60"),
            PetPlace("shop_1", "محل واحة أليف لمستلزمات القطط 🛒", "متاجر ومستلزمات 🛒", "shop", 24.7612, 46.6198, "أغذية جافة، رطبة، ألعاب، أقفاص، وإكسسوارات مستوردة فاخرة لحيوانك الأليف المميز.", "4.8", "95", "+966 504443332", "١٠:٠٠ ص - ١١:٠٠ م", "https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=400&auto=format&fit=crop&q=60"),
            PetPlace("grooming_1", "صالون الكلب المدلل للجمال ✂️", "العناية والفنادق ✂️", "grooming", 24.7198, 46.6692, "استحمام، قص شعر، تنظيف مخالب وأذن، سبا هادئ للكلاب والقطط الضالة بأيدي خبراء.", "4.9", "135", "+966 502221115", "١٢:٠٠ م - ١٠:٠٠ م", "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=400&auto=format&fit=crop&q=60"),
            PetPlace("hotel_1", "فندق السعادة لاستضافة القطط 🏨", "العناية والفنادق ✂️", "hotel", 24.7511, 46.6622, "أجنحة خاصة مكيفة وآمنة بالكامل لإقامة حيوانك الأليف أثناء سفرك، مع رعاية غذائية دقيقة.", "5.0", "78", "+966 505556667", "٠٨:٠٠ ص - ١٠:٠٠ م", "https://images.unsplash.com/photo-1590419690008-905885483012?w=400&auto=format&fit=crop&q=60"),
            PetPlace("park_1", "حديقة أليفي المفتوحة للعب والتنزه 🎾", "مراكز تدريب وحدائق 🎓", "park", 24.7301, 46.6499, "متنزه رائع مغلق وآمن للتمشية واللعب مع الحيوانات الأليفة في الهواء ومناطق الجري الحرة.", "4.7", "112", "-", "مفتوح ٢٤ ساعة طوال أيام الأسبوع", "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400&auto=format&fit=crop&q=60"),
            PetPlace("training_1", "مدرسة أبطال الرياض لتدريب الكلاب 🎓", "مراكز تدريب وحدائق 🎓", "training", 24.7431, 46.6575, "تدريب على الطاعة، تعديل السلوك، تدريب الحماية والتمشية الهادئة مع أفضل مدربين بالمملكة.", "4.9", "64", "+966 503211234", "٠٤:٠٠ م - ١٠:٠٠ م", "https://images.unsplash.com/photo-1535268647977-a403b69fc756?w=400&auto=format&fit=crop&q=60")
        )

        val customRescueSpots = incidents.map { incident ->
            val hash = incident.id.hashCode().toDouble()
            val latOffset = ((Math.abs(hash) % 100) / 1000.0) - 0.05
            val lngOffset = (((Math.abs(hash) / 100).toInt() % 100) / 1000.0) - 0.05
            val latVal = 24.7136 + latOffset
            val lngVal = 46.6753 + lngOffset
            PetPlace(
                id = incident.id,
                name = incident.title,
                category = "بلاغات عاجلة 🚨",
                type = "emergency",
                lat = latVal,
                lng = lngVal,
                desc = "التبليغ عن حالة في: ${incident.location}. تم الرفع بواسطة المتطوع: ${incident.reporter}.",
                rating = "عاجل",
                reviews = "شغال",
                phone = "+966 500000000",
                hours = "٢٤ ساعة إنقاذ طوارئ",
                imageUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400&auto=format&fit=crop&q=60"
            )
        }

        val geoJsonString = """
        {
          "type": "FeatureCollection",
          "features": [
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "مدينة القدس الشريف 🇵🇸",
                "Name (EN)": "Jerusalem",
                "النوع": "مدينة تاريخية وعاصمة دينية",
                "category": "religion",
                "الوصف": "من أقدم وأقدس مدن العالم ولها وجدان تاريخي فريد ومأوى للتكافل الإنساني."
              },
              "geometry": { "type": "Point", "coordinates": [ 35.2137, 31.7683 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "عيادة الرياض البيطرية الحديثة (طبيب حيوانات)",
                "Name (EN)": "Modern Riyadh Vet Clinic (Doctor)",
                "النوع": "طبيب وعيادة بيطرية للحيوانات الأليفة",
                "الدولة": "المملكة العربية السعودية",
                "المدينة": "الرياض",
                "category": "vet",
                "الوصف": "تطعيمات، جراحة عامة، رعاية عاجلة بكادر طبي كامل.",
                "الهاتف": "+966-50-1234567"
              },
              "geometry": { "type": "Point", "coordinates": [ 46.6753, 24.7116 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "متجر أليفي لغذاء ومستلزمات القطط والكلاب",
                "Name (EN)": "Aleefi Pet Store & Supplies",
                "النوع": "متجر ومحل حيوانات أليفة وأغذية",
                "الدولة": "المملكة العربية السعودية",
                "المدينة": "الرياض",
                "category": "petstore",
                "الوصف": "أغذية مستوردة، ألعاب قطط، بيوت حيوانات، أقفاص طيور. ساعات العمل: 9:00 ص - 11:30 م"
              },
              "geometry": { "type": "Point", "coordinates": [ 46.7234, 24.7301 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "عيادة هليوبوليس للحيوانات الأليفة (دكتور بيطري)",
                "Name (EN)": "Heliopolis Pet & Vet Clinic (Doctor)",
                "النوع": "طبيب وعيادة بيطرية للحيوانات الأليفة",
                "الدولة": "مصر",
                "المدينة": "القاهرة",
                "category": "vet",
                "الوصف": "طب أطفال الحيوان، فحص سونار وتحاليل دم، قص أظافر وتجميل. دعم طبي طوارئ على مدار 24 ساعة.",
                "الهاتف": "+20120000000"
              },
              "geometry": { "type": "Point", "coordinates": [ 31.3301, 30.1002 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "متجر كنز الحيوان والأغذية والألعاب",
                "Name (EN)": "Animal Treasure Pet Shop",
                "النوع": "متجر ومحل حيوانات أليفة وأغذية",
                "الدولة": "مصر",
                "المدينة": "القاهرة",
                "category": "petstore",
                "الوصف": "رمل قطط صحي، دراي فود ممتاز، فيتامينات ومكملات غذائية للكلاب."
              },
              "geometry": { "type": "Point", "coordinates": [ 31.245, 30.041 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "عيادة الشفاء للطب البيطري (طبيب حيوانات)",
                "Name (EN)": "Al Shifa Veterinary Cabinets (Doctor)",
                "النوع": "طبيب وعيادة بيطرية للحيوانات الأليفة",
                "الدولة": "المغرب",
                "المدينة": "الدار البيضاء",
                "category": "vet",
                "الوصف": "تطعيم الكلاب والقطط، عمليات تجميلية وجراحة الفم والأسنان."
              },
              "geometry": { "type": "Point", "coordinates": [ -7.59, 33.573 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "محل مستلزمات كلاب وقطط الوفاء والأليفة",
                "Name (EN)": "Al Wafaa Dog & Cat Pet Market (Shop)",
                "النوع": "متجر ومحل حيوانات أليفة وأغذية",
                "الدولة": "المغرب",
                "المدينة": "الدار البيضاء",
                "category": "petstore",
                "الوصف": "أجهزة إطعام أوتوماتيكية، أحواض سمك مجهزة، استشارات تربية مجانية."
              },
              "geometry": { "type": "Point", "coordinates": [ -7.611, 33.585 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "عيادة عمان لرعاية الحيوان والطيور (طبيب بيطري)",
                "Name (EN)": "Amman Animal & Bird Clinic (Doctor)",
                "النوع": "طبيب وعيادة بيطرية للحيوانات الأليفة",
                "الدولة": "الأردن",
                "المدينة": "عمان",
                "category": "vet",
                "الوصف": "كشف عام، تصوير إشعاعي، وقاية ومكافحة الطفيليات والحشرات للأرانب والهامستر للحيوانات."
              },
              "geometry": { "type": "Point", "coordinates": [ 35.91, 31.953 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "متجر طيور الحب وببغاء الزينة وأغذيتها",
                "Name (EN)": "Love Birds & Parrot Ornament Store & Supplies",
                "النوع": "متجر ومحل حيوانات أليفة وأغذية",
                "الدولة": "الأردن",
                "المدينة": "عمان",
                "category": "petstore",
                "الوصف": "بذور طيور مدعمة بالفيتامينات، أقفاص حديدية كبرى، ألعاب تفاعلية للببغاء."
              },
              "geometry": { "type": "Point", "coordinates": [ 35.885, 31.972 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "مستشفى الجميرا البيطري المتقدم للحيوانات الأليفة",
                "Name (EN)": "Jumeirah Veterinary Hospital (Doctor)",
                "النوع": "طبيب وعيادة بيطرية للحيوانات الأليفة",
                "الدولة": "الإمارات العربية المتحدة",
                "المدينة": "دبي",
                "category": "vet",
                "الوصف": "أحدث تقنيات التشخيص، رعاية وإقامة فندقية للحيوانات، رعاية يومية صحية كاملة."
              },
              "geometry": { "type": "Point", "coordinates": [ 55.28, 25.21 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "العاصمة الرياض",
                "Name (EN)": "Riyadh",
                "الدولة": "المملكة العربية السعودية",
                "Country": "Saudi Arabia",
                "category": "shelter",
                "الوصف": "العاصمة التاريخية والسياسية للمملكة ومركز تنموي واقتصادي رائد لخدمات ودعم الرفق بالحيوان."
              },
              "geometry": { "type": "Point", "coordinates": [ 46.7196, 24.7136 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "العاصمة القاهرة",
                "Name (EN)": "Cairo",
                "الدولة": "مصر",
                "Country": "Egypt",
                "category": "shelter",
                "الوصف": "تأسست عام 969 ميلادي على يد الفاطميين ومشهورة بمدينة الألف مئذنة، وبها العديد من ملاجئ الحيوان."
              },
              "geometry": { "type": "Point", "coordinates": [ 31.2357, 30.0444 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "العاصمة بغداد",
                "Name (EN)": "Baghdad",
                "الدولة": "العراق",
                "Country": "Iraq",
                "category": "shelter",
                "الوصف": "عاصمة الخلافة العباسية التاريخية ودار الحكمة ومركز العلوم والفلسفة القديم والنشاط الطوعي لإنقاذ الحيوان."
              },
              "geometry": { "type": "Point", "coordinates": [ 44.3615, 33.3128 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "العاصمة طوكيو",
                "Name (EN)": "Tokyo",
                "الدولة": "اليابان",
                "Country": "Japan",
                "category": "shelter",
                "الوصف": "أكبر حاضرة سكانية في العالم ومزيج فريد من التكنولوجيا الفائقة وتوفر عيادات ذكية ومراكز تبني."
              },
              "geometry": { "type": "Point", "coordinates": [ 139.6503, 35.6762 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "العاصمة باريس",
                "Name (EN)": "Paris",
                "الدولة": "فرنسا",
                "Country": "France",
                "category": "shelter",
                "الوصف": "مدينة الأنوار التاريخية، مركز عالمي للفن ومجتمعات رعاية وحماية الفصيلة الأليفة."
              },
              "geometry": { "type": "Point", "coordinates": [ 2.3522, 48.8566 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الاسم (عربي)": "العاصمة لندن",
                "Name (EN)": "London",
                "الدولة": "المملكة المتحدة",
                "Country": "United Kingdom",
                "category": "shelter",
                "الوصف": "عاصمة تاريخية على نهر التايمز، تجمع بين متاحف عريقة وقوة مالية متطورة وقوانين رعاية الحيوان الصارمة."
              },
              "geometry": { "type": "Point", "coordinates": [ -0.1278, 51.5074 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "المعلم (عربي)": "أهرامات الجيزة",
                "Name (EN)": "Giza Pyramids",
                "category": "park",
                "الوصف": "مقابر ملوكية للفراعنة خوفو وخفرع ومنقرع بنيت قبل أكثر من 4500 عام وتحتفظ بجاذبيتها المعمارية والشهامة في إنقاذ حيواناتها."
              },
              "geometry": { "type": "Point", "coordinates": [ 31.1342, 29.9792 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "المعلم (عربي)": "البتراء (المدينة الوردية)",
                "Name (EN)": "Petra",
                "category": "park",
                "الوصف": "مدينة كاملة منحوتة في الصخور الوردية أسستها حضارة الأنباط العربية وتتميز بنشاط حماية الخيول والجمال للأفواج السياحية."
              },
              "geometry": { "type": "Point", "coordinates": [ 35.4444, 30.3285 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "المعلم (عربي)": "المدرج الروماني (الكولوسيوم)",
                "Name (EN)": "The Colosseum",
                "category": "park",
                "الوصف": "أكبر مدرج روماني تاريخي في قلب روما، كان يتسع لحوالي 50 ألف متفرج لمتابعة قتالات المصارعين قديماً."
              },
              "geometry": { "type": "Point", "coordinates": [ 12.4922, 41.8902 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "المعلم (عربي)": "تاج محل",
                "Name (EN)": "Taj Mahal",
                "category": "park",
                "الوصف": "ضريح رائع من الرخام الأبيض أقامه الإمبراطور شاه جهان لتخليد ذكرى زوجته الراحلة ممتاز محل ويمثل قمة الفن المغولي."
              },
              "geometry": { "type": "Point", "coordinates": [ 78.0421, 27.1751 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "النهر": "نهر النيل العظيم",
                "Name (EN)": "The Nile River",
                "category": "rescue",
                "الوصف": "أطول أنهار العالم، مهد الحضارات التاريخية ومأوى التنوع الحيوي المميز."
              },
              "geometry": { "type": "Point", "coordinates": [ 31.2357, 30.0444 ] }
            },
            {
              "type": "Feature",
              "properties": {
                "الإقليم": "مثلث برمودا الغامض",
                "Name (EN)": "Bermuda Triangle",
                "category": "rescue",
                "الوصف": "اشتهر تاريخياً بسبب التقارير الغامضة والإشاعات حول اختفاء بعض السفن والطائرات."
              },
              "geometry": { "type": "Point", "coordinates": [ -71.0, 25.0 ] }
            }
          ]
        }
        """.trimIndent()

        val geoJsonPlaces = try {
            val obj = org.json.JSONObject(geoJsonString)
            val features = obj.getJSONArray("features")
            val list = mutableListOf<PetPlace>()
            for (i in 0 until features.length()) {
                val f = features.getJSONObject(i)
                val props = f.getJSONObject("properties")
                val geom = f.getJSONObject("geometry")
                val coords = geom.getJSONArray("coordinates")
                val lngObj = coords.getDouble(0)
                val latObj = coords.getDouble(1)
                
                val nameStr = when {
                    props.has("الاسم (عربي)") -> props.getString("الاسم (عربي)")
                    props.has("المعلم (عربي)") -> props.getString("المعلم (عربي)")
                    props.has("Name (EN)") -> props.getString("Name (EN)")
                    props.has("name") -> props.getString("name")
                    props.has("الاسم") -> props.getString("الاسم")
                    props.has("النهر") -> props.getString("النهر")
                    props.has("الإقليم") -> props.getString("الإقليم")
                    else -> "موقع جيو مضاف 📍"
                }
                
                val descStr = when {
                    props.has("الوصف") -> props.getString("الوصف")
                    props.has("description") -> props.getString("description")
                    props.has("التاريخ") -> props.getString("التاريخ")
                    props.has("المستلزمات المتوفرة") -> props.getString("المستلزمات المتوفرة")
                    props.has("المنتجات") -> props.getString("المنتجات")
                    props.has("الخدمات البيطرية") -> props.getString("الخدمات البيطرية")
                    props.has("الخدمات") -> props.getString("الخدمات")
                    else -> "تبين من الخريطة التفاعلية والبيانات الجيوجيسون الممتازة."
                }
                
                val searchStr = (nameStr + " " + descStr + " " + props.optString("النوع") + " " + props.optString("category") + " " + props.optString("التصنيف")).lowercase()
                
                val mappedCategoryAndType = when {
                    searchStr.contains("طبيب") || searchStr.contains("بيطر") || searchStr.contains("عيادة") || searchStr.contains("دكتور") || searchStr.contains("vet") || searchStr.contains("clinic") || searchStr.contains("doctor") || searchStr.contains("veterinary") -> {
                        Pair("عيادات بيطرية 🏥", "clinic")
                    }
                    searchStr.contains("متجر") || searchStr.contains("محل") || searchStr.contains("مستلزمات") || searchStr.contains("shop") || searchStr.contains("store") || searchStr.contains("market") || searchStr.contains("pet store") -> {
                        Pair("متاجر ومستلزمات 🛒", "shop")
                    }
                    searchStr.contains("ملجأ") || searchStr.contains("تبني") || searchStr.contains("إيواء") || searchStr.contains("shelter") || searchStr.contains("sanctuary") || searchStr.contains("الأمان") || searchStr.contains("عاصمة") || searchStr.contains("riyadh") || searchStr.contains("الرياض") || searchStr.contains("القاهرة") || searchStr.contains("بغداد") || searchStr.contains("طوكيو") || searchStr.contains("باريس") || searchStr.contains("لندن") || searchStr.contains("القدس") || searchStr.contains("jerusalem") -> {
                        Pair("الملاجئ والتبني 🐶", "shelter")
                    }
                    searchStr.contains("حديقة") || searchStr.contains("منتزه") || searchStr.contains("تدريب") || searchStr.contains("مدرسة") || searchStr.contains("park") || searchStr.contains("training") || searchStr.contains("zoo") || searchStr.contains("wonder") || searchStr.contains("أهرامات") || searchStr.contains("البتراء") || searchStr.contains("كولوسيوم") || searchStr.contains("تاج محل") -> {
                        Pair("مراكز تدريب وحدائق 🎓", "park")
                    }
                    else -> {
                        Pair("الملاجئ والتبني 🐶", "shelter")
                    }
                }
                
                val ratingVal = if (mappedCategoryAndType.second == "clinic" || mappedCategoryAndType.second == "shop") "4.8" else "مميز"
                val reviewsVal = if (mappedCategoryAndType.second == "clinic" || mappedCategoryAndType.second == "shop") "142" else "إرشاد"
                val phoneVal = props.optString("الهاتف", "-")
                
                list.add(
                    PetPlace(
                        id = "geojson_$i",
                        name = nameStr,
                        category = mappedCategoryAndType.first,
                        type = mappedCategoryAndType.second,
                        lat = latObj,
                        lng = lngObj,
                        desc = descStr,
                        rating = ratingVal,
                        reviews = reviewsVal,
                        phone = phoneVal,
                        hours = "٠٩:٠٠ ص - ٠٩:٠٠ م",
                        imageUrl = if (mappedCategoryAndType.second == "clinic") {
                            "https://images.unsplash.com/photo-1516600118604-51a31610c14b?w=400&auto=format&fit=crop&q=60"
                        } else if (mappedCategoryAndType.second == "shop") {
                            "https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=400&auto=format&fit=crop&q=60"
                        } else {
                            "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400&auto=format&fit=crop&q=60"
                        }
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList<PetPlace>()
        }

        predefinedSpots + customRescueSpots + fetchedPlacesList + geoJsonPlaces
    }

    // Filter locations dynamically using the top filters
    val filteredPlaces = remember(combinedPlaces, filterCategory) {
        combinedPlaces.filter { place ->
            when (filterCategory) {
                "الكل" -> true
                "الملاجئ والتبني 🐶" -> place.category == "الملاجئ والتبني 🐶" || place.type == "shelter"
                "عيادات بيطرية 🏥" -> place.category == "عيادات بيطرية 🏥" || place.type == "clinic"
                "متاجر ومستلزمات 🛒" -> place.category == "متاجر ومستلزمات 🛒" || place.type == "shop"
                "العناية والفنادق ✂️" -> place.category == "العناية والفنادق ✂️" || place.type == "grooming" || place.type == "hotel"
                "مراكز تدريب وحدائق 🎓" -> place.category == "مراكز تدريب وحدائق 🎓" || place.type == "training" || place.type == "park"
                "بلاغات عاجلة 🚨" -> place.category == "بلاغات عاجلة 🚨" || place.type == "emergency"
                else -> true
            }
        }
    }

    // Detect if camera is far from previous search coords to show the "Search this area" button
    val showSearchThisAreaButton = remember(mapCenterLat, mapCenterLng, lastSearchedLat, lastSearchedLng) {
        val latDelta = Math.abs(mapCenterLat - lastSearchedLat)
        val lngDelta = Math.abs(mapCenterLng - lastSearchedLng)
        latDelta > 0.015 || lngDelta > 0.015
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Map Heading Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("خريطة التضامن والإنقاذ 📍", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                        Text("ابحث عن الملاجئ وعيادات الحيوانات الشريكة وصناديق التغذية.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = { showReportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إرسال بلاغ 🚨", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Interactive Filters of Pins
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("الكل", "الملاجئ والتبني 🐶", "عيادات بيطرية 🏥", "متاجر ومستلزمات 🛒", "العناية والفنادق ✂️", "مراكز تدريب وحدائق 🎓", "بلاغات عاجلة 🚨").forEach { category ->
                        FilterChip(
                            selected = filterCategory == category,
                            onClick = { filterCategory = category },
                            label = { Text(category, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // New sleek manual city override geocoding search bar with dynamic OSM integration
        var locationQuery by remember { mutableStateOf("") }
        var isLocatingAddress by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = locationQuery,
                onValueChange = { locationQuery = it },
                placeholder = { Text("أدخل مدينتك لتثبيت موقعك (مثال: الدار البيضاء، الرياض)", fontSize = 11.sp, color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "موقع",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (locationQuery.isNotEmpty()) {
                        IconButton(onClick = { locationQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = {
                        if (locationQuery.isNotBlank() && !isLocatingAddress) {
                            isLocatingAddress = true
                            geocodeAddress(locationQuery) { lat, lng ->
                                isLocatingAddress = false
                                if (lat != null && lng != null) {
                                    userLatitude = lat
                                    userLongitude = lng
                                    mapCenterLat = lat
                                    mapCenterLng = lng
                                    hasCenteredMapInitially = false // allow center trigger
                                    
                                    prefs.edit()
                                        .putFloat("user_lat", lat.toFloat())
                                        .putFloat("user_lng", lng.toFloat())
                                        .apply()
                                    
                                    android.widget.Toast.makeText(context, "تم تحديد مكان الهاتف بنجاح: $locationQuery 🗺️", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "تعذر تحديد الموقع، جرب اسماً آخر للمدينة.", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )
            )
            Button(
                onClick = {
                    if (locationQuery.isNotBlank() && !isLocatingAddress) {
                        isLocatingAddress = true
                        geocodeAddress(locationQuery) { lat, lng ->
                            isLocatingAddress = false
                            if (lat != null && lng != null) {
                                userLatitude = lat
                                userLongitude = lng
                                mapCenterLat = lat
                                mapCenterLng = lng
                                hasCenteredMapInitially = false // allow center trigger
                                
                                prefs.edit()
                                    .putFloat("user_lat", lat.toFloat())
                                    .putFloat("user_lng", lng.toFloat())
                                    .apply()
                                
                                android.widget.Toast.makeText(context, "تم تحديد مكان الهاتف بنجاح: $locationQuery 🗺️", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "تعذر تحديد الموقع، جرب اسماً آخر للمدينة.", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                enabled = locationQuery.isNotBlank() && !isLocatingAddress,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLocatingAddress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("انتقال 🗺️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Interactive free OpenStreetMap Native Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
        ) {
            var mapViewRef by remember { mutableStateOf<MapView?>(null) }

            // Define gorgeous premium raster styles matching vector aesthetics
            val cartoVoyager = remember {
                org.osmdroid.tileprovider.tilesource.XYTileSource(
                    "CartoDB_Voyager",
                    1, 19, 256, ".png",
                    arrayOf(
                        "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                        "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                        "https://c.basemaps.cartocdn.com/rastertiles/voyager/",
                        "https://d.basemaps.cartocdn.com/rastertiles/voyager/"
                    )
                )
            }

            val cartoPositron = remember {
                org.osmdroid.tileprovider.tilesource.XYTileSource(
                    "CartoDB_Positron",
                    1, 19, 256, ".png",
                    arrayOf(
                        "https://a.basemaps.cartocdn.com/light_all/",
                        "https://b.basemaps.cartocdn.com/light_all/",
                        "https://c.basemaps.cartocdn.com/light_all/",
                        "https://d.basemaps.cartocdn.com/light_all/"
                    )
                )
            }

            val cartoDarkMatter = remember {
                org.osmdroid.tileprovider.tilesource.XYTileSource(
                    "CartoDB_DarkMatter",
                    1, 19, 256, ".png",
                    arrayOf(
                        "https://a.basemaps.cartocdn.com/dark_all/",
                        "https://b.basemaps.cartocdn.com/dark_all/",
                        "https://c.basemaps.cartocdn.com/dark_all/",
                        "https://d.basemaps.cartocdn.com/dark_all/"
                    )
                )
            }

            // Arabic cardinal directions converter helper
            fun getBearingDirectionArabic(bearing: Float): String {
                val b = (bearing + 360f) % 360f
                return when {
                    b >= 337.5 || b < 22.5 -> "شمال ⬆️"
                    b >= 22.5 && b < 67.5 -> "شمال شرق ↗️"
                    b >= 67.5 && b < 112.5 -> "شرق ➡️"
                    b >= 112.5 && b < 157.5 -> "جنوب شرق ↘️"
                    b >= 157.5 && b < 202.5 -> "جنوب ⬇️"
                    b >= 202.5 && b < 247.5 -> "جنوب غرب ↙️"
                    b >= 247.5 && b < 292.5 -> "غرب ⬅️"
                    b >= 292.5 && b < 337.5 -> "شمال غرب ↖️"
                    else -> "شمال ⬆️"
                }
            }

            // Custom Dynamic Markers Factory (drawing beautiful emojis in glossy badges / navigation chevron for user)
            fun createCustomMarkerIcon(context: android.content.Context, type: String): android.graphics.drawable.Drawable {
                val density = context.resources.displayMetrics.density
                val size = if (type == "user") (48 * density).toInt() else (46 * density).toInt()
                val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

                if (type == "user") {
                    val scale = density
                    val centerX = size / 2f
                    val centerY = size / 2f

                    // 1. Draw a realistic smooth drop-shadow for depth on the map (proportional offset)
                    val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = 0x88000000.toInt() // High-clarity shadow paint
                        style = android.graphics.Paint.Style.FILL
                    }
                    val shadowPath = android.graphics.Path().apply {
                        moveTo(centerX + 1.2f * scale, centerY - 16f * scale)
                        lineTo(centerX + 15f * scale, centerY + 18f * scale)
                        lineTo(centerX + 1.2f * scale, centerY + 11f * scale)
                        lineTo(centerX - 13f * scale, centerY + 18f * scale)
                        close()
                    }
                    canvas.drawPath(shadowPath, shadowPaint)

                    // 2. Clear white outline mask wrapping the arrow to pop on both dark & light map backgrounds
                    val outlinePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.WHITE
                        style = android.graphics.Paint.Style.FILL
                    }
                    val outlinePath = android.graphics.Path().apply {
                        moveTo(centerX, centerY - 20f * scale)
                        lineTo(centerX + 16f * scale, centerY + 18f * scale)
                        lineTo(centerX, centerY + 10f * scale)
                        lineTo(centerX - 16f * scale, centerY + 18f * scale)
                        close()
                    }
                    canvas.drawPath(outlinePath, outlinePaint)

                    // 3. Draw Left wing in a gorgeous light cyber-blue (reflective highlight side)
                    val leftPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = 0xFF29B6F6.toInt() // Glowing GPS cyan-blue
                        style = android.graphics.Paint.Style.FILL
                    }
                    val leftPath = android.graphics.Path().apply {
                        moveTo(centerX, centerY - 17f * scale)
                        lineTo(centerX - 13f * scale, centerY + 15f * scale)
                        lineTo(centerX, centerY + 8f * scale)
                        close()
                    }
                    canvas.drawPath(leftPath, leftPaint)

                    // 4. Draw Right wing in a darker midnight-blue (3D realism shadow side)
                    val rightPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = 0xFF0288D1.toInt() // Metallic dark-blue
                        style = android.graphics.Paint.Style.FILL
                    }
                    val rightPath = android.graphics.Path().apply {
                        moveTo(centerX, centerY - 17f * scale)
                        lineTo(centerX + 13f * scale, centerY + 15f * scale)
                        lineTo(centerX, centerY + 8f * scale)
                        close()
                    }
                    canvas.drawPath(rightPath, rightPaint)

                    // 5. Draw a small, minimalist high-tech red beacon dot in the center crease for that aviation-navigator feel
                    val beaconPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = 0xFFFF5252.toInt() // Vivid highlight red
                        style = android.graphics.Paint.Style.FILL
                    }
                    canvas.drawCircle(centerX, centerY + 2f * scale, 2.5f * scale, beaconPaint)

                    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
                }

                val colorInt = when (type) {
                    "clinic" -> 0xFF23B5D3.toInt()     // Vibrant Teal
                    "shop" -> 0xFF9966CC.toInt()       // Indigo/Pet Purple
                    "shelter" -> 0xFFF16B22.toInt()    // Creative Orange
                    "emergency" -> 0xFFD32F2F.toInt()  // Red alert
                    "grooming", "hotel" -> 0xFF009688.toInt() // Ocean Teal
                    "training", "park" -> 0xFF4CAF50.toInt() // Green nature
                    else -> 0xFF757575.toInt()         // Grey fallback
                }

                // 1. Glow ring
                paint.color = colorInt
                paint.alpha = 45
                canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
                
                // 2. Base solid circle
                paint.alpha = 255
                paint.color = colorInt
                canvas.drawCircle(size / 2f, size / 2f, size / 2.6f, paint)

                // 3. Crisp white center
                paint.color = android.graphics.Color.WHITE
                canvas.drawCircle(size / 2f, size / 2f, size / 3.4f, paint)
                
                // 4. White accent outline
                paint.apply {
                    color = android.graphics.Color.WHITE
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 2f * density
                }
                canvas.drawCircle(size / 2f, size / 2f, size / 2.6f, paint)

                // 5. Draw the emojis cleanly in the center of the badge
                val symbol = when (type) {
                    "clinic" -> "🏥"
                    "shop" -> "🛒"
                    "shelter" -> "🏠"
                    "emergency" -> "🚨"
                    "grooming", "hotel" -> "🧼"
                    "training", "park" -> "🐾"
                    else -> "📍"
                }
                
                paint.apply {
                    style = android.graphics.Paint.Style.FILL
                    textSize = 13f * density
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val fontMetrics = paint.fontMetrics
                val textY = (size / 2f) - ((fontMetrics.ascent + fontMetrics.descent) / 2f)
                canvas.drawText(symbol, size / 2f, textY, paint)

                return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
            }



            // Sync Map view initialization & center behavior
            LaunchedEffect(hasCenteredMapInitially) {
                if (hasCenteredMapInitially) {
                    mapViewRef?.controller?.animateTo(org.osmdroid.util.GeoPoint(mapCenterLat, mapCenterLng))
                    mapViewRef?.controller?.setZoom(14.5)
                }
            }

            AndroidView(
                factory = { ctx ->
                    org.osmdroid.config.Configuration.getInstance().userAgentValue = ctx.packageName
                    
                    MapView(ctx).apply {
                        setMultiTouchControls(true)
                        setBuiltInZoomControls(false)
                        
                        // Set standard initial zoom and center
                        controller.setZoom(13.5)
                        controller.setCenter(org.osmdroid.util.GeoPoint(mapCenterLat, mapCenterLng))
                        
                        // Track dragging & scrolling to update search coordinates dynamically
                        addMapListener(object : org.osmdroid.events.MapListener {
                            override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                                val center = mapCenter
                                if (Math.abs(mapCenterLat - center.latitude) > 0.0001 || Math.abs(mapCenterLng - center.longitude) > 0.0001) {
                                    mapCenterLat = center.latitude
                                    mapCenterLng = center.longitude
                                }
                                return true
                            }
                            override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                                val center = mapCenter
                                if (Math.abs(mapCenterLat - center.latitude) > 0.0001 || Math.abs(mapCenterLng - center.longitude) > 0.0001) {
                                    mapCenterLat = center.latitude
                                    mapCenterLng = center.longitude
                                }
                                return true
                            }
                        })
                        
                        mapViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    mapView.overlays.clear()
                    
                    // Apply Selected Map Premium Theme
                    val currentTileSource = when (selectedMapStyle) {
                        "positron" -> cartoPositron
                        "dark" -> cartoDarkMatter
                        else -> cartoVoyager
                    }
                    if (mapView.tileProvider.tileSource.name() != currentTileSource.name()) {
                        mapView.setTileSource(currentTileSource)
                    }

                    // 1. Enable Live phone's compass overlay nicely rotated facing direction
                    try {
                        val compassOverlay = org.osmdroid.views.overlay.compass.CompassOverlay(
                            mapView.context,
                            org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider(mapView.context),
                            mapView
                        ).apply {
                            enableCompass()
                            // Move compass down to prevent overlap with Map Styles
                            setCompassCenter(32f * mapView.context.resources.displayMetrics.density, 75f * mapView.context.resources.displayMetrics.density)
                        }
                        mapView.overlays.add(compassOverlay)
                    } catch (e: Exception) {
                        android.util.Log.e("MapScreen", "Error loading compass: ${e.message}")
                    }

                    // 2. Clear & Draw colored Polyline route line path to selected animal care
                    val userLat = userLatitude
                    val userLng = userLongitude
                    val selected = selectedPlace
                    if (selected != null && userLat != null && userLng != null) {
                        val routeLine = org.osmdroid.views.overlay.Polyline(mapView).apply {
                            val points = listOf(
                                org.osmdroid.util.GeoPoint(userLat, userLng),
                                org.osmdroid.util.GeoPoint(selected.lat, selected.lng)
                            )
                            setPoints(points)
                            
                            outlinePaint.apply {
                                color = 0xFF1E88E5.toInt() // Blue neon modern path
                                strokeWidth = 10f
                                strokeCap = android.graphics.Paint.Cap.ROUND
                                isAntiAlias = true
                            }
                        }
                        mapView.overlays.add(routeLine)
                    }
                    
                    // ٣. إضافة علامات دبابيس الحيوانات الأليفة (Pet Places Markers)
                    // تتضمن هذه القائمة المواقع الساكنة التي تم إدخال إحداثياتها يدوياً (Hardcoded coordinates) مثل العيادات والملاجئ
                    filteredPlaces.forEach { place ->
                        val marker = org.osmdroid.views.overlay.Marker(mapView).apply {
                            position = org.osmdroid.util.GeoPoint(place.lat, place.lng)
                            title = place.name
                            snippet = place.category
                            
                            // شرح استخدام أيقونة مخصصة (Custom Drawable icon like a dog or cat icon):
                            // لاستخدام ملف drawable مخصص من مجلد التطبيق (مثال: ic_dog أو ic_cat):
                            // val customIcon = androidx.core.content.ContextCompat.getDrawable(mapView.context, R.drawable.ic_custom_dog)
                            // icon = customIcon
                            
                            // نستخدم هنا المولد التفاعلي المصمم لإنتاج أيقونات مميزة بألوان متناسقة لكل نوع (عيادة، متجر، ملجأ) لتفادي أي كراش
                            icon = createCustomMarkerIcon(mapView.context, place.type)
                            
                            setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                            setOnMarkerClickListener { m, _ ->
                                selectedPlace = place
                                mapView.controller.animateTo(m.position)
                                m.showInfoWindow()
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                    
                    // ٤. علامة الموقع الحالي التفاعلية والنباضة مع سهم التوجيه الدوار للأقمار الصناعية (Live Navigational Pulsing User Marker with Heading)
                    if (userLat != null && userLng != null) {
                        val userMarker = org.osmdroid.views.overlay.Marker(mapView).apply {
                            position = org.osmdroid.util.GeoPoint(userLat, userLng)
                            title = "موقعك الحالي - الاتجاه: ${getBearingDirectionArabic(deviceBearing)} 🧭"
                            icon = createCustomMarkerIcon(mapView.context, "user")
                            setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                            rotation = -deviceBearing // Rotate live with precision compass azimuth values
                        }
                        mapView.overlays.add(userMarker)
                    }
                    
                    mapView.invalidate()
                }
            )

            // FLOATING Style selector for Voyager, Light, and Dark styles
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "voyager" to "🌈 ملون",
                    "positron" to "☀️ فاتح",
                    "dark" to "🌙 داكن"
                ).forEach { (styleKey, label) ->
                    val isSelected = selectedMapStyle == styleKey
                    Surface(
                        onClick = { selectedMapStyle = styleKey },
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // FLOATING "Search this Area" button
            if (showSearchThisAreaButton) {
                Button(
                    onClick = {
                        lastSearchedLat = mapCenterLat
                        lastSearchedLng = mapCenterLng
                        fetchNearbyPlacesFromOverpass(mapCenterLat, mapCenterLng) { results ->
                            fetchedPlacesList = results
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("البحث في هذه المنطقة 🔍", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // FLOATING location center GPS Recenter Button
            FloatingActionButton(
                onClick = {
                    val lat = userLatitude
                    val lng = userLongitude
                    if (lat != null && lng != null && mapViewRef != null) {
                        mapViewRef?.controller?.animateTo(org.osmdroid.util.GeoPoint(lat, lng))
                        mapViewRef?.controller?.setZoom(16.0)
                        mapCenterLat = lat
                        mapCenterLng = lng
                        android.widget.Toast.makeText(context, "تم الانتقال إلى إحداثيات موقعك الحقيقي الدقيق 🎯", android.widget.Toast.LENGTH_SHORT).show()
                        
                        // Dynamic Fetch around current GPS coordinates
                        fetchNearbyPlacesFromOverpass(lat, lng) { results ->
                            fetchedPlacesList = results
                        }
                    } else {
                        val mgr = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? LocationManager
                        val isGeonEnabled = mgr?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
                        if (!isGeonEnabled) {
                            android.widget.Toast.makeText(context, "⚠️ الـ GPS مغلق بالجهاز! يرجى تشغيله من إعدادات الهاتف والمحاولة مجدداً.", android.widget.Toast.LENGTH_LONG).show()
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        } else {
                            android.widget.Toast.makeText(context, "⚠️ جاري البحث عن إشارة GPS الدقيقة والاتصال بالأقمار الصناعية... يرجى الانتظار والوقوف في مكان مكشوف.", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (selectedPlace != null) 220.dp else 16.dp, end = 16.dp)
                    .size(48.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "تحديد موقعي")
            }

            // BEAUTIFUL GPS DEBUGGER & STATUS PANEL
            var isExpanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = if (selectedPlace != null) 235.dp else 16.dp)
                    .widthIn(max = 260.dp)
                    .shadow(6.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("بيانات الـ GPS الحقيقية لموقعك 🛰️", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(if (isExpanded) "▲" else "▼", fontSize = 10.sp, color = Color.Gray)
                    }
                    if (isExpanded) {
                        androidx.compose.material3.HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("خط العرض:", fontSize = 10.sp, color = Color.Gray)
                            Text(userLatitude?.let { String.format("%.6f", it) } ?: "جاري رصد الأقمار...", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("خط الطول:", fontSize = 10.sp, color = Color.Gray)
                            Text(userLongitude?.let { String.format("%.6f", it) } ?: "جاري رصد الأقمار...", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("دقة الموقع:", fontSize = 10.sp, color = Color.Gray)
                            Text(gpsAccuracy?.let { "${String.format("%.1f", it)} متر" } ?: "لم يتم التحديد بعد", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("حالة الـ GPS بالجهاز:", fontSize = 10.sp, color = Color.Gray)
                            Text(if (isGpsEnabled) "مفعّل ✅" else "معطل ❌", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isGpsEnabled) Color(0xFF2E7D32) else Color.Red)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إذن تحديد الموقع:", fontSize = 10.sp, color = Color.Gray)
                            Text(if (hasLocationPermission) "مسموح ✅" else "مرفوض ❌", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (hasLocationPermission) Color(0xFF2E7D32) else Color.Red)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("اتجاه البوصلة الحالي:", fontSize = 10.sp, color = Color.Gray)
                            Text("${Math.round(deviceBearing)}° ${getBearingDirectionArabic(deviceBearing)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // FLOATING PENDING SATELLITE WARNING BANNER
            if (userLatitude == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 75.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "🛰️ بانتظار إشارة GPS حقيقية عالية الدقة...",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "تم تعطيل المحاكاة وخوادم تحديد المواقع التقريبية تماماً حفاظاً على سلامتك. يرجى الوقوف في مكان مكشوف أو تفعيل خدمة الـ GPS الهاتفية والانتظار لقفل القمر الصناعي.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 14.sp
                        )
                        if (!isGpsEnabled) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "الرجاء فتح إعدادات مستوى الموقع يدوياً.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("تفعيل مستوى الموقع (GPS) ⚙️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // SLIDE-UP Details bottom card when a pin is clicked
            if (selectedPlace != null) {
                val place = selectedPlace!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .shadow(16.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = place.category,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "شريك معتمد 🤝",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Rating, Hours, Phone and Photo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = place.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = place.rating,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "(${place.reviews} مراجعة)",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text("⏱️ دوام العمل: ${place.hours}", fontSize = 11.sp)
                                Text("📞 للتواصل: ${place.phone}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Location Description Details
                        Text(
                            text = place.desc,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    android.widget.Toast.makeText(context, "جاري إطلاق محاكاة المسار ونقاط الاتجاهات...", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(2f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🚗 رسم المسار", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    android.widget.Toast.makeText(context, "جاري الاتصال بـ ${place.name}...", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("📞 اتصل", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { selectedPlace = null },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("إغلاق", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
