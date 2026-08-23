package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.*
import com.example.ui.components.*

@Composable
fun AdoptionScreen(viewModel: MainViewModel) {
    val pipeline by viewModel.adoptionPipeline.collectAsState()
    val animals by viewModel.animalsList.collectAsState()

    // Search and Filters
    var searchKeyword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var selectedPriceFilter by remember { mutableStateOf("الكل") } // "الكل", "مجاني", "للبيع"

    // Dialog States
    var showSurveyDialog by remember { mutableStateOf(false) }
    var showPrepModal by remember { mutableStateOf(false) }
    var showHealthPassport by remember { mutableStateOf(false) }
    var showAddAnimalDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // 1. Preparation Guide Dialog
    if (showPrepModal) {
        Dialog(onDismissRequest = { showPrepModal = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00B4D8).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = "دليل تجهيز المنزل لاستقبال الأليف 📖",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Text("خطوات أساسية لتوفير بيئة آمنة ومريحة لصديقك الجديد:", fontSize = 13.sp, color = Color(0xFF94A3B8))

                    AdoptionBulletPoint("تأمين النوافذ والشرفات بشباك أمان محكمة.")
                    AdoptionBulletPoint("تغطية وتنظيم الأسلاك والأجهزة الكهربائية.")
                    AdoptionBulletPoint("توفير أوعية طعام وماء صحية من الستانلس ستيل.")
                    AdoptionBulletPoint("تخصيص ركن هادئ ومريح بسرير أو بطانية ناعمة.")

                    Button(
                        onClick = { showPrepModal = false },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تم وفهمت", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 2. Health Passport Dialog
    if (showHealthPassport) {
        Dialog(onDismissRequest = { showHealthPassport = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "السجل الصحي الرقمي لـ بندق 🩺",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )

                    // QR Code visual
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(10.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
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

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("سجل التحصينات والتطعيمات:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("• اللقاح الأساسي:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("مكتمل وموثق ✓", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("• تطعيم السعار السنوي:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("الموعد القادم: ٢٨ نوفمبر", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = { showHealthPassport = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إغلاق السجل", color = Color.White)
                    }
                }
            }
        }
    }

    // 3. Adoption Survey Dialog
    if (showSurveyDialog) {
        Dialog(onDismissRequest = { showSurveyDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
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

                    Text(
                        text = "استبيان ملاءمة المسكن 🏠",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = style,
                        onValueChange = { style = it },
                        label = { Text("نمط السكن (شقة / فيلا)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = security,
                        onValueChange = { security = it },
                        label = { Text("تأمين النوافذ والشرفات") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        label = { Text("ساعات غيابك اليومية") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = kids,
                        onValueChange = { kids = it },
                        label = { Text("وجود أطفال في المنزل") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = activity,
                        onValueChange = { activity = it },
                        label = { Text("شكل رعاية الأليف اليومية") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showSurveyDialog = false }) { Text("إلغاء", color = Color(0xFF94A3B8)) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.updateAdoptionPipelineAnswers(style, security, hours, kids, activity)
                                showSurveyDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                        ) {
                            Text("حفظ التغييرات")
                        }
                    }
                }
            }
        }
    }

    // Main Adoption Screen Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "بوابة التبني 🐾",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "تصفح أليفك القادم وامنحه حياة جديدة وآمنة",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Add Pet Button
            Button(
                onClick = { showAddAnimalDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier.testTag("add_animal_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة أليف",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "إضافة أليف",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Modern Search Bar
        BasicTextField(
            value = searchKeyword,
            onValueChange = { searchKeyword = it },
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            cursorBrush = SolidColor(Color(0xFF00B4D8)),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("adoption_search_input"),
            decorationBox = @Composable { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchKeyword.isEmpty()) {
                            Text(
                                text = "ابحث بالاسم، السلالة، أو الوصف...",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                    if (searchKeyword.isNotEmpty()) {
                        IconButton(
                            onClick = { searchKeyword = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "مسح",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Category Filter Chips
        val categories = listOf(
            Pair("الكل", "🐾 الكل"),
            Pair("قطة", "🐱 قطط"),
            Pair("كلب", "🐶 كلاب"),
            Pair("طيور", "🦜 طيور وببغاوات"),
            Pair("أرنب", "🐰 أرانب"),
            Pair("هامستر", "🐹 هامستر وقوارض"),
            Pair("سلحفاة", "🐢 سلاحف وزواحف"),
            Pair("أسماك", "🐠 أسماك زينة"),
            Pair("دواجن", "🦆 دواجن منزلية"),
            Pair("أخرى", "✨ أخرى")
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { (key, label) ->
                val isSelected = selectedCategory == key
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) Color(0xFF0D9488) else Color(0xFF1E293B),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) Color(0xFF14B8A6) else Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.clickable { selectedCategory = key }
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Price Filter Row (All, Free Adoption, For Sale)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Pair("الكل", "جميع العروض"),
                Pair("مجاني", "تبني مجاني 🎁"),
                Pair("للبيع", "للبيع 💰")
            ).forEach { (filterKey, filterLabel) ->
                val isActive = selectedPriceFilter == filterKey
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isActive) Color(0xFF334155) else Color(0xFF1E293B).copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (isActive) Color(0xFF38BDF8).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.05f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedPriceFilter = filterKey }
                ) {
                    Text(
                        text = filterLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Logic
        val filteredAnimals = animals.filter { animal ->
            val matchCategory = when (selectedCategory) {
                "الكل" -> true
                "قطة" -> animal.species.contains("قط", true)
                "كلب" -> animal.species.contains("كلب", true)
                "طيور" -> animal.species.contains("طير", true) || animal.species.contains("طيور", true) ||
                        animal.species.contains("ببغاء", true) || animal.species.contains("كناري", true) ||
                        animal.species.contains("حسون", true) || animal.species.contains("كوكاتيل", true) ||
                        animal.species.contains("بادجي", true) || animal.species.contains("مينا", true) ||
                        animal.species.contains("حمام", true)
                "أرنب" -> animal.species.contains("أرنب", true) || animal.species.contains("ارنب", true)
                "هامستر" -> animal.species.contains("هامستر", true) || animal.species.contains("غينيا", true) ||
                        animal.species.contains("كابياء", true) || animal.species.contains("شنشيلة", true) ||
                        animal.species.contains("قنفذ", true) || animal.species.contains("فيريت", true) ||
                        animal.species.contains("عرس", true) || animal.species.contains("سنجاب", true) ||
                        animal.species.contains("فأر", true)
                "سلحفاة" -> animal.species.contains("سلحفاة", true) || animal.species.contains("جيكو", true) ||
                        animal.species.contains("سحلية", true) || animal.species.contains("إغوانا", true) ||
                        animal.species.contains("حرباء", true) || animal.species.contains("أكسولوتل", true) ||
                        animal.species.contains("سمندر", true) || animal.species.contains("ضفدع", true)
                "أسماك" -> animal.species.contains("سمك", true) || animal.species.contains("فايتر", true) ||
                        animal.species.contains("ذهبية", true) || animal.species.contains("جمبري", true) ||
                        animal.species.contains("مائي", true) || animal.species.contains("أحواض", true)
                "دواجن" -> animal.species.contains("بط", true) || animal.species.contains("دجاج", true) ||
                        animal.species.contains("سمان", true)
                "أخرى" -> true
                else -> animal.species.contains(selectedCategory, true)
            }
            val matchPrice = when (selectedPriceFilter) {
                "مجاني" -> animal.priceStatus != "للبيع"
                "للبيع" -> animal.priceStatus == "للبيع"
                else -> true
            }
            val matchQuery = searchKeyword.isBlank() ||
                    animal.name.contains(searchKeyword, true) ||
                    animal.breed.contains(searchKeyword, true) ||
                    animal.description.contains(searchKeyword, true)

            matchCategory && matchPrice && matchQuery
        }

        if (filteredAnimals.isEmpty()) {
            // Empty State
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🔍", fontSize = 42.sp)
                    Text(
                        text = "لم يتم العثور على أليف مطابق",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "جرب تغيير كلمات البحث أو إزالة الفلاتر المحددة",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = {
                            searchKeyword = ""
                            selectedCategory = "الكل"
                            selectedPriceFilter = "الكل"
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("إعادة ضبط الفلاتر", fontSize = 12.sp)
                    }
                }
            }
        } else {
            // 2-Column Simple & Beautiful Animal Cards
            filteredAnimals.chunked(2).forEach { rowAnimals ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowAnimals.forEach { animal ->
                        AnimalCard(
                            animal = animal,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectAnimal(animal) }
                        )
                    }
                    // Keep balance if odd count
                    if (rowAnimals.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    // 4. Add Animal Full-Screen View
    if (showAddAnimalDialog) {
        Dialog(
            onDismissRequest = { showAddAnimalDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            BackHandler {
                showAddAnimalDialog = false
            }

            var newName by remember { mutableStateOf("") }
            var newSpecies by remember { mutableStateOf("قطة (أليف منزلي)") }
            var newBreed by remember { mutableStateOf("") }
            var newAge by remember { mutableStateOf("") }
            var newGender by remember { mutableStateOf("ذكر") }
            var newPriceStatus by remember { mutableStateOf("مجاني") }
            var newDescription by remember { mutableStateOf("") }
            var newBackstory by remember { mutableStateOf("") }
            var newImageUrl by remember { mutableStateOf("") }
            var showSpeciesPickerModal by remember { mutableStateOf(false) }

            val formScrollState = rememberScrollState()

            // Species Picker Searchable Modal
            if (showSpeciesPickerModal) {
                SpeciesPickerModal(
                    currentSelection = newSpecies,
                    onSelect = { selectedPet ->
                        newSpecies = selectedPet.name
                        if (newImageUrl.isBlank()) {
                            newImageUrl = selectedPet.defaultImageUrl
                        }
                        showSpeciesPickerModal = false
                    },
                    onCustomSelect = { customName ->
                        newSpecies = customName
                        showSpeciesPickerModal = false
                    },
                    onDismiss = { showSpeciesPickerModal = false }
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding(),
                color = Color(0xFF0F172A)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top App Bar
                    Surface(
                        color = Color(0xFF1E293B),
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0D9488).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pets,
                                        contentDescription = null,
                                        tint = Color(0xFF2DD4BF),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "إضافة أليف جديد",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "اعرض أليفك للتبني أو البيع الآمن",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showAddAnimalDialog = false },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "إغلاق",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Form Body (Scrollable)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(formScrollState)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Section: Basic Info
                        Text(
                            text = "📌 المعلومات الأساسية",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF38BDF8)
                        )

                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("اسم الأليف *") },
                            placeholder = { Text("مثال: لوسي، ماكس، سكر...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pet_name_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B),
                                focusedBorderColor = Color(0xFF0D9488),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            )
                        )

                        // Species Selection (Dropdown Box with Down Arrow)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("نوع الأليف *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFCBD5E1))

                            // Interactive Dropdown Box with Down Arrow
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF0D9488)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showSpeciesPickerModal = true }
                                    .testTag("species_dropdown_box")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        val matchEmoji = DOMESTIC_PET_LIST.find { it.name == newSpecies }?.emoji ?: "🐾"
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0D9488).copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(matchEmoji, fontSize = 20.sp)
                                        }
                                        Column {
                                            Text(
                                                text = newSpecies.ifBlank { "اضغط لاختيار نوع الأليف" },
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "اضغط لفتح القائمة والاختيار",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }

                                    // Down Arrow Icon
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "قائمة منسدلة",
                                        tint = Color(0xFF2DD4BF),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        // Breed & Age Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = newBreed,
                                onValueChange = { newBreed = it },
                                label = { Text("السلالة") },
                                placeholder = { Text("مثال: شيرازي، سيامي") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF1E293B),
                                    unfocusedContainerColor = Color(0xFF1E293B),
                                    focusedBorderColor = Color(0xFF0D9488),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                )
                            )

                            OutlinedTextField(
                                value = newAge,
                                onValueChange = { newAge = it },
                                label = { Text("العمر") },
                                placeholder = { Text("مثال: 4 أشهر، سنة") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF1E293B),
                                    unfocusedContainerColor = Color(0xFF1E293B),
                                    focusedBorderColor = Color(0xFF0D9488),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }

                        // Gender and Price Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Gender
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("الجنس *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFCBD5E1))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("ذكر" to "ذكر ♂", "أنثى" to "أنثى ♀").forEach { (gKey, gLabel) ->
                                        val active = newGender == gKey
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { newGender = gKey },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (active) Color(0xFF00B4D8) else Color(0xFF1E293B),
                                            border = BorderStroke(1.dp, if (active) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.08f))
                                        ) {
                                            Text(
                                                text = gLabel,
                                                color = if (active) Color.White else Color(0xFF94A3B8),
                                                fontSize = 12.sp,
                                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Price/Offer Status
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("نوع العرض *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFCBD5E1))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("مجاني" to "مجاني 🎁", "للبيع" to "للبيع 💰").forEach { (pKey, pLabel) ->
                                        val active = newPriceStatus == pKey
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { newPriceStatus = pKey },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (active) {
                                                if (pKey == "للبيع") Color(0xFFE11D48) else Color(0xFF10B981)
                                            } else Color(0xFF1E293B),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                                        ) {
                                            Text(
                                                text = pLabel,
                                                color = if (active) Color.White else Color(0xFF94A3B8),
                                                fontSize = 12.sp,
                                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section: Details & Notes
                        Text(
                            text = "📝 التفاصيل والوصف",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF38BDF8)
                        )

                        OutlinedTextField(
                            value = newDescription,
                            onValueChange = { newDescription = it },
                            label = { Text("طباع الأليف وحالته الصحية") },
                            placeholder = { Text("مثال: لعوب، معتاد على الليتر بوكس، هادئ ومحب للأطفال...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            minLines = 3,
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B),
                                focusedBorderColor = Color(0xFF0D9488),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            )
                        )

                        OutlinedTextField(
                            value = newImageUrl,
                            onValueChange = { newImageUrl = it },
                            label = { Text("رابط صورة الأليف (اختياري)") },
                            placeholder = { Text("اتركه فارغاً لاستخدام صورة نموذجية تلقائية") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B),
                                focusedBorderColor = Color(0xFF0D9488),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Bottom Action Bar
                    Surface(
                        color = Color(0xFF1E293B),
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { showAddAnimalDialog = false },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Text("إلغاء", color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    if (newName.isNotBlank()) {
                                        val finalImg = if (newImageUrl.isBlank()) {
                                            val matchedPet = DOMESTIC_PET_LIST.find { it.name == newSpecies }
                                            if (matchedPet != null) {
                                                matchedPet.defaultImageUrl
                                            } else {
                                                when {
                                                    newSpecies.contains("قط") -> "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600"
                                                    newSpecies.contains("كلب") -> "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600"
                                                    newSpecies.contains("ببغاء") || newSpecies.contains("طير") || newSpecies.contains("كناري") || newSpecies.contains("حسون") -> "https://images.unsplash.com/photo-1522850949506-585e5e2298f7?w=600"
                                                    newSpecies.contains("أرنب") || newSpecies.contains("ارنب") -> "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=600"
                                                    newSpecies.contains("هامستر") || newSpecies.contains("غينيا") || newSpecies.contains("كابياء") -> "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=600"
                                                    newSpecies.contains("سلحفاة") || newSpecies.contains("جيكو") || newSpecies.contains("زواحف") -> "https://images.unsplash.com/photo-1437622368342-7a3d73a34c8f?w=600"
                                                    newSpecies.contains("سمك") || newSpecies.contains("فايتر") || newSpecies.contains("ذهبية") -> "https://images.unsplash.com/photo-1522069169874-c58ec4b76be5?w=600"
                                                    newSpecies.contains("قنفذ") -> "https://images.unsplash.com/photo-1508921912186-1d1a45ebb3c1?w=600"
                                                    newSpecies.contains("فيريت") || newSpecies.contains("عرس") -> "https://images.unsplash.com/photo-1618255651586-1eb8674d825c?w=600"
                                                    newSpecies.contains("بط") || newSpecies.contains("دجاج") -> "https://images.unsplash.com/photo-1465153690352-10c1b29577f8?w=600"
                                                    else -> "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600"
                                                }
                                            }
                                        } else {
                                            newImageUrl
                                        }
                                        viewModel.addAnimal(
                                            name = newName.trim(),
                                            species = newSpecies,
                                            breed = newBreed.ifBlank { "بلدي" }.trim(),
                                            age = newAge.ifBlank { "غير معروف" }.trim(),
                                            gender = newGender,
                                            description = newDescription.ifBlank { "أليف لطيف يبحث عن عائلة محبة ورعاية آمنة." }.trim(),
                                            backstory = newBackstory.ifBlank { "تم إنقاذه وتقديمه للرعاية والتبني." }.trim(),
                                            imageUrl = finalImg,
                                            priceStatus = newPriceStatus
                                        )
                                        showAddAnimalDialog = false
                                    }
                                },
                                enabled = newName.isNotBlank(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0D9488),
                                    disabledContainerColor = Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .weight(2f)
                                    .height(48.dp)
                                    .testTag("submit_add_animal_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (newName.isNotBlank()) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "نشر الأليف 🐾",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (newName.isNotBlank()) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simple & Beautiful Animal Card Component
@Composable
fun AnimalCard(
    animal: AnimalItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isForSale = animal.priceStatus == "للبيع"

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("animal_card_${animal.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image Header Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .clickable { onClick() }
            ) {
                AsyncImage(
                    model = animal.imageUrl,
                    contentDescription = animal.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onClick() }
                )

                // Top Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )

                // Bottom Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                // Status Badge (Top-Start)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isForSale) Color(0xFFE11D48).copy(alpha = 0.9f) else Color(0xFF10B981).copy(alpha = 0.9f),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = if (isForSale) "للبيع 💰" else "مجاني 🎁",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Gender Badge (Top-End)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = if (animal.gender == "ذكر") "ذكر ♂" else "أنثى ♀",
                        color = if (animal.gender == "ذكر") Color(0xFF38BDF8) else Color(0xFFF472B6),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Age Overlay (Bottom-Start)
                Text(
                    text = "⏳ ${animal.age}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Info Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Name
                Text(
                    text = animal.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Breed & Species
                Text(
                    text = "${animal.breed} • ${animal.species}",
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
fun AdoptionBulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", fontWeight = FontWeight.Bold, color = Color(0xFF00B4D8), fontSize = 14.sp)
        Text(text, fontSize = 12.sp, lineHeight = 18.sp, color = Color(0xFFE2E8F0))
    }
}

// ----------------------------------------------------
// Domestic Pets Comprehensive Database & Search Modal
// ----------------------------------------------------

data class DomesticPetItem(
    val name: String,
    val emoji: String,
    val category: String,
    val description: String,
    val defaultImageUrl: String
)

val DOMESTIC_PET_LIST = listOf(
    // 🐱 قطط
    DomesticPetItem("قطة (أليف منزلي)", "🐱", "قطط", "القط المنزلي الأكثر شعبية، هادئ ومحبوب ونظيف", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600"),
    DomesticPetItem("قط شيرازي / فارسي", "🐱", "قطط", "قط طويل الشعر، هادئ ولطيف جداً ومناسب للبيوت", "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=600"),
    DomesticPetItem("قط سيامي", "🐱", "قطط", "قط ذكي، فضولي، اجتماعي وصوته مميز", "https://images.unsplash.com/photo-1513360309081-38f0762daed1?w=600"),
    DomesticPetItem("قط سكوتش فولد", "🐱", "قطط", "قط ذو أذنين مطويتين وشخصية ودودة هادئة", "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=600"),
    DomesticPetItem("قط بريطاني قصير الشعر", "🐱", "قطط", "قط ممتلئ وهادئ ومناسب للشقق والعائلات", "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=600"),

    // 🐶 كلاب
    DomesticPetItem("كلب (أليف منزلي)", "🐶", "كلاب", "وفي ومخلص، رفيق العائلة الأول والمنزل", "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600"),
    DomesticPetItem("كلب هاسكي سيبيري", "🐕", "كلاب", "كلب نشيط وفرو كثيف وجميل ومحب للعب", "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=600"),
    DomesticPetItem("كلب جولدن ريتريفر / لابرادور", "🦮", "كلاب", "ودود جداً ومحب للأطفال والعائلات وسهل التدريب", "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600"),
    DomesticPetItem("كلب جيرمن شيبارد (راعي ألماني)", "🐕", "كلاب", "شديد الذكاء ومخلص وحارس ممتاز", "https://images.unsplash.com/photo-1589941013453-ec89f33b5e95?w=600"),
    DomesticPetItem("كلب بيتبول / بولدوج", "🐶", "كلاب", "كلب قوي ومخلص للعائلة عند الرعاية الحسنة", "https://images.unsplash.com/photo-1537151608828-ea2b11777ee8?w=600"),
    DomesticPetItem("كلب صغير (بودل / تشيهواهوا / بوميرانيان)", "🐩", "كلاب", "كلب صغير الحجم مثالي للشقق والمنازل الصغيرة", "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=600"),

    // 🦜 طيور وببغاوات
    DomesticPetItem("ببغاء (ببغاء متكلم)", "🦜", "طيور", "ذكي وممتع وقادر على تقليد الأصوات والكلمات", "https://images.unsplash.com/photo-1522850949506-585e5e2298f7?w=600"),
    DomesticPetItem("طائر الكناري", "🐦", "طيور", "تغريد عذب وجميل وألوان صفراء زاهية تملأ البيت", "https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=600"),
    DomesticPetItem("طائر الحسون", "🐦", "طيور", "صوت عذب وألوان زاهية وريش جذاب", "https://images.unsplash.com/photo-1555169062-013468b47731?w=600"),
    DomesticPetItem("طائر البادجي (طائر الحب)", "🦜", "طيور", "طائر صغير اجتماعي ملون وسهل التربية", "https://images.unsplash.com/photo-1544943910-4c1dc44a0d9b?w=600"),
    DomesticPetItem("طائر الكوكاتيل", "🦜", "طيور", "ببغاء قزم بتاج أصفر وخدود برتقالية وودود", "https://images.unsplash.com/photo-1598133894008-61f7fdb8cc3a?w=600"),
    DomesticPetItem("طائر المينا", "🐦", "طيور", "طائر ذكي ونبيه يحاكي أصوات المنزل والكلام", "https://images.unsplash.com/photo-1516610101564-e10be23fe36f?w=600"),
    DomesticPetItem("حمام زينة / يمام", "🕊️", "طيور", "طائر هادئ وأنيق وودود للتربية المنزلية", "https://images.unsplash.com/photo-1546853020-ca4909aef454?w=600"),

    // 🐰 قوارض وأرانب
    DomesticPetItem("أرنب منزلي (بلدي / هولندي / قزم)", "🐰", "قوارض وأرانب", "أليف وديع وناعم يحب الخضار واللعب الهادئ", "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=600"),
    DomesticPetItem("هامستر (سوري / قزم روسي)", "🐹", "قوارض وأرانب", "صغير ولطيف جداً ومناسب للأطفال في قفص مجهز", "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=600"),
    DomesticPetItem("خنزير غينيا (كابياء)", "🐹", "قوارض وأرانب", "حيوان اجتماعي لطيف يصدر أصواتاً محببة عند الفرح", "https://images.unsplash.com/photo-1535241749838-299277b6305f?w=600"),
    DomesticPetItem("شنشيلة (Chinchilla)", "🐭", "قوارض وأرانب", "فرو ناعم للغاية مثل الحرير ويحب القفز", "https://images.unsplash.com/photo-1583337130417-3346a1be7dee?w=600"),
    DomesticPetItem("قنفذ إفريقي قزم", "🦔", "قوارض وأرانب", "حيوان فريد هادئ وغير مؤذي يتحول لكرة شوكية", "https://images.unsplash.com/photo-1508921912186-1d1a45ebb3c1?w=600"),
    DomesticPetItem("فيريت (ابن عرس الأليف)", "🐾", "قوارض وأرانب", "لعوب وفضولي للغاية ومرح جداً داخل المنزل", "https://images.unsplash.com/photo-1618255651586-1eb8674d825c?w=600"),
    DomesticPetItem("سنجاب طائر (شوجر جلايدر)", "🐿️", "قوارض وأرانب", "حيوان جرابي صغير يطير بلطف ويتعلق بصاحبه", "https://images.unsplash.com/photo-1507666405895-422efe7d517f?w=600"),
    DomesticPetItem("فأر أليف / جربوع", "🐭", "قوارض وأرانب", "قارض صغير ونظيف وسهل الرعاية في القفص", "https://images.unsplash.com/photo-1583337130417-3346a1be7dee?w=600"),

    // 🐢 زواحف وبرمائيات
    DomesticPetItem("سلحفاة (برية أو مائية)", "🐢", "زواحف", "أليف هادئ ومسالم يعيش لسنوات طويلة في حوضه", "https://images.unsplash.com/photo-1437622368342-7a3d73a34c8f?w=600"),
    DomesticPetItem("أبو بريص الفهد (جيكو Leopard Gecko)", "🦎", "زواحف", "زاحف هادئ أليف وسهل العناية بألوان جلد جميلة", "https://images.unsplash.com/photo-1500463955536-f8b11747ef98?w=600"),
    DomesticPetItem("إغوانا خضراء", "🦎", "زواحف", "سحلية كبيرة وعشبية لمحبي الزواحف والتربية المنزلية", "https://images.unsplash.com/photo-1504450758481-7338eba7524a?w=600"),
    DomesticPetItem("حرباء منزلية", "🦎", "زواحف", "تغير ألوانها وحركتها مميزة وعيونها المستقلة", "https://images.unsplash.com/photo-1563281577-a7be47e20db9?w=600"),
    DomesticPetItem("أكسولوتل (عفريت الماء / سمندر)", "🦎", "زواحف", "كائن برمائي مبتسم يعيش في الأحواض المائية الباردة", "https://images.unsplash.com/photo-1500463955536-f8b11747ef98?w=600"),
    DomesticPetItem("ضفدع شجري أليف", "🐸", "زواحف", "ألوان زاهية وجمال طبيعي هادئ في الأحواض", "https://images.unsplash.com/photo-1500463955536-f8b11747ef98?w=600"),

    // 🐠 أسماك وكائنات مائية
    DomesticPetItem("سمكة الفايتر (المقاتل السيامي / Betta)", "🐠", "أسماك", "ألوان ساحرة وزعانف ملكية تعيش في أحواض فردية", "https://images.unsplash.com/photo-1522069169874-c58ec4b76be5?w=600"),
    DomesticPetItem("سمكة ذهبية (جولد فيش)", "🐟", "أسماك", "سمكة الأحواض الأشهر والأكثر ألفة وجمالاً", "https://images.unsplash.com/photo-1524704654690-b56c05c78a00?w=600"),
    DomesticPetItem("أسماك زينة استوائية (نيون / جوفي / مولي)", "🐠", "أسماك", "أسماك ملونة وسريعة الحركة تعطي حيوية للحوض", "https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=600"),
    DomesticPetItem("جمبري زينة مائي (شريمب نيوكاريدينا)", "🦐", "أسماك", "ينظف الحوض وله ألوان حمراء وزرقاء مميزة", "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=600"),

    // 🦆 دواجن وطيور منزلية
    DomesticPetItem("بط صغير / بط منزلي", "🦆", "دواجن", "بط أليف مرح ومحب للماء والحدائق المنزلية", "https://images.unsplash.com/photo-1465153690352-10c1b29577f8?w=600"),
    DomesticPetItem("دجاج سيلكي (دجاج حريري / زينة)", "🐔", "دواجن", "دجاج ذو ريش قطني ناعم وأليف جداً ومسالم", "https://images.unsplash.com/photo-1548550023-2bdb3c5beed7?w=600"),
    DomesticPetItem("طائر السمان المنزلي", "🐦", "دواجن", "طائر صغير وسريع الإنتاج وسهل التربية", "https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=600"),

    // ✨ أخرى
    DomesticPetItem("حيوان أليف آخر", "✨", "أخرى", "أي حيوان أليف منزلي آخر تود عرضه ورعايته", "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600")
)

@Composable
fun SpeciesPickerModal(
    currentSelection: String,
    onSelect: (DomesticPetItem) -> Unit,
    onCustomSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }

    val categories = listOf("الكل", "قطط", "كلاب", "طيور", "قوارض وأرانب", "زواحف", "أسماك", "دواجن", "أخرى")

    val filteredList = remember(searchQuery, selectedCategoryFilter) {
        DOMESTIC_PET_LIST.filter { pet ->
            val matchCategory = selectedCategoryFilter == "الكل" || pet.category == selectedCategoryFilter
            val matchSearch = searchQuery.isBlank() ||
                    pet.name.contains(searchQuery, ignoreCase = true) ||
                    pet.description.contains(searchQuery, ignoreCase = true) ||
                    pet.category.contains(searchQuery, ignoreCase = true)
            matchCategory && matchSearch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D9488).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🐾", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = "اختر نوع الأليف المنزلي",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "جميع الحيوانات المنزلية مع إمكانية التمرير والبحث ⬇️⬆️",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar with clear button
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(Color(0xFF00B4D8)),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("species_picker_search_input"),
                    decorationBox = @Composable { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0F172A), RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0xFF0D9488).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "بحث", tint = Color(0xFF2DD4BF), modifier = Modifier.size(18.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "ابحث بالاسم: قط، هامستر، سلحفاة، ببغاء، سمك...",
                                        color = Color(0xFF64748B),
                                        fontSize = 12.sp
                                    )
                                }
                                innerTextField()
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips Row (Horizontal Scroll)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF0D9488) else Color(0xFF0F172A),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF14B8A6) else Color.White.copy(alpha = 0.06f)),
                            modifier = Modifier.clickable { selectedCategoryFilter = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom search fallback banner
                if (searchQuery.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0D9488).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF0D9488)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCustomSelect(searchQuery.trim())
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("✨", fontSize = 18.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "استخدام: \"$searchQuery\"",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF2DD4BF)
                                )
                                Text(
                                    text = "تعيين كاسم مخصص لنوع الأليف",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Vertical Scrollable List (Up and Down)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredList.isEmpty() && searchQuery.isBlank()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("لا توجد نتائج مطابقة", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(filteredList) { item ->
                            val isSelected = currentSelection.contains(item.name) || currentSelection == item.name
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) Color(0xFF0D9488).copy(alpha = 0.25f) else Color(0xFF0F172A),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF2DD4BF) else Color.White.copy(alpha = 0.06f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(item) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Emoji Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF0D9488) else Color(0xFF1E293B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.emoji, fontSize = 20.sp)
                                    }

                                    // Details
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF334155).copy(alpha = 0.6f)
                                            ) {
                                                Text(
                                                    text = item.category,
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF38BDF8),
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = item.description,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "تم الاختيار",
                                            tint = Color(0xFF2DD4BF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق القائمة", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}
