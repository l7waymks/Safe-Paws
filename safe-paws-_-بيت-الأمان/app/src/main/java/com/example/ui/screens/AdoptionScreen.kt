package com.example.ui.ui


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

import com.example.*
import com.example.ui.components.*
import com.example.ui.screens.*

fun AdoptionScreen(viewModel: MainViewModel) {
    val pipeline by viewModel.adoptionPipeline.collectAsState()
    var showSurveyDialog by remember { mutableStateOf(false) }

    // Search and Category filter states
    var searchKeyword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    val animals by viewModel.animalsList.collectAsState()

    // Helpful Resources dialogues triggers
    var showPrepModal by remember { mutableStateOf(false) }
    var showExpertChat by remember { mutableStateOf(false) }
    var showHealthPassport by remember { mutableStateOf(false) }
    var showAddAnimalDialog by remember { mutableStateOf(false) }

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
            .background(Color.Black)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Main Headline Row with '+' button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "بوابة التبني 🐾",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            IconButton(
                onClick = { showAddAnimalDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة أليف",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "تصفح الحيوانات الأليفة المتاحة للتبني أو للبيع.",
            fontSize = 13.sp,
            color = Color.LightGray.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        BasicTextField(
            value = searchKeyword,
            onValueChange = { searchKeyword = it },
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("adoption_search_input"),
            decorationBox = @Composable { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color(0xFF1E1E1E),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchKeyword.isEmpty()) {
                            Text(
                                text = "ابحث عن كلب، قطة، سلالة معينة...",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                    if (searchKeyword.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "مسح",
                            tint = Color.LightGray,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { searchKeyword = "" }
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Categories Row
        val categories = listOf("الكل", "كلب", "قطة", "طيور", "أرنب")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary 
                            else Color(0xFF1E1E1E)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter animals
        val filteredAnimals = animals.filter {
            (selectedCategory == "الكل" || it.species == selectedCategory) &&
            (it.name.contains(searchKeyword, true) || it.breed.contains(searchKeyword, true) || it.description.contains(searchKeyword, true))
        }

        if (filteredAnimals.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🔍", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("لم نجد حيوانات مطابقة لبحثك.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            filteredAnimals.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { animal ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF121212))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .clickable { viewModel.selectAnimal(animal) }
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                ) {
                                    AsyncImage(
                                        model = animal.imageUrl,
                                        contentDescription = animal.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Price Status Badge
                                    val isForSale = animal.priceStatus == "للبيع"
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .background(
                                                if (isForSale) Color(0xFFD32F2F) else Color(0xFF2E7D32), 
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                            .align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = if (isForSale) "للبيع 💰" else "تبني مجاني 🎁",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = animal.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${animal.breed} • ${animal.age}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showAddAnimalDialog) {
        Dialog(onDismissRequest = { showAddAnimalDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    var newName by remember { mutableStateOf("") }
                    var newSpecies by remember { mutableStateOf("قطة") }
                    var newBreed by remember { mutableStateOf("") }
                    var newAge by remember { mutableStateOf("") }
                    var newGender by remember { mutableStateOf("ذكر") }
                    var newPriceStatus by remember { mutableStateOf("مجاني") } // "مجاني" or "للبيع"
                    var newDescription by remember { mutableStateOf("") }
                    var newBackstory by remember { mutableStateOf("") }
                    var newImageUrl by remember { mutableStateOf("") }

                    Text(
                        text = "إضافة أليف جديد 🐾",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("اسم الأليف") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Species Row
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("النوع:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("قطة", "كلب", "طيور", "أرنب").forEach { species ->
                                val active = newSpecies == species
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { newSpecies = species }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(species, color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newBreed,
                        onValueChange = { newBreed = it },
                        label = { Text("السلالة (مثال: شيرازي، هجين)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newAge,
                        onValueChange = { newAge = it },
                        label = { Text("العمر (مثال: ٣ أشهر، سنتين)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Gender Row
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("الجنس:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("ذكر", "أنثى").forEach { gender ->
                                val active = newGender == gender
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { newGender = gender }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(gender, color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Price Status Row
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("نوع العرض:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("مجاني", "للبيع").forEach { status ->
                                val active = newPriceStatus == status
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (active) {
                                                if (status == "للبيع") Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                            } else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { newPriceStatus = status }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (status == "للبيع") "للبيع 💰" else "تبني مجاني 🎁",
                                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("السيرة الذاتية (الوصف)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = newBackstory,
                        onValueChange = { newBackstory = it },
                        label = { Text("قصته وظروف إنقاذه") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = newImageUrl,
                        onValueChange = { newImageUrl = it },
                        label = { Text("رابط الصورة (اختياري)") },
                        placeholder = { Text("اتركه فارغاً لاستخدام صورة افتراضية") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddAnimalDialog = false }) { Text("إلغاء") }
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    val finalImg = if (newImageUrl.isBlank()) {
                                        if (newSpecies == "قطة") {
                                            "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600"
                                        } else if (newSpecies == "كلب") {
                                            "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600"
                                        } else if (newSpecies == "طيور") {
                                            "https://images.unsplash.com/photo-1522850949506-585e5e2298f7?w=600"
                                        } else {
                                            "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=600" // rabbit
                                        }
                                    } else {
                                        newImageUrl
                                    }
                                    viewModel.addAnimal(
                                        name = newName,
                                        species = newSpecies,
                                        breed = newBreed.ifBlank { "بلدي" },
                                        age = newAge.ifBlank { "غير معروف" },
                                        gender = newGender,
                                        description = newDescription.ifBlank { "أليف لطيف يبحث عن عائلة." },
                                        backstory = newBackstory.ifBlank { "تم إنقاذه وتقديمه للرعاية." },
                                        imageUrl = finalImg,
                                        priceStatus = newPriceStatus
                                    )
                                    showAddAnimalDialog = false
                                }
                            }
                        ) {
                            Text("إضافة")
                        }
                    }
                }
            }
        }
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


