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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.net.Uri
import kotlinx.coroutines.launch
import com.example.FirebaseManager
import com.example.*
import com.example.ui.components.*

@Composable
fun AdoptionScreen(viewModel: MainViewModel) {
    val pipeline by viewModel.adoptionPipeline.collectAsState()
    val animals by viewModel.animalsList.collectAsState()

    // Search and Filters
    var searchKeyword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var selectedBreed by remember { mutableStateOf("الكل") }
    var selectedPriceFilter by remember { mutableStateOf("الكل") } // "الكل", "مجاني", "للبيع"

    // Dialog States
    var showSurveyDialog by remember { mutableStateOf(false) }
    var showPrepModal by remember { mutableStateOf(false) }
    var showHealthPassport by remember { mutableStateOf(false) }
    var showAddAnimalDialog by remember { mutableStateOf(false) }
    var showAnimalSelectorDialog by remember { mutableStateOf(false) }
    var showBreedSelectorDialog by remember { mutableStateOf(false) }
    var showPriceSelectorDialog by remember { mutableStateOf(false) }

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
                            text = "دليل تجهيز المنزل لاستقبال الأليف",
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(22.dp))
                        Text(
                            text = "السجل الصحي الرقمي لـ بندق",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مكتمل وموثق", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                        Text(
                            text = "استبيان ملاءمة المسكن",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }

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

    // 4. Animal Selector Dialog (اختيار نوع أو فئة الحيوان)
    if (showAnimalSelectorDialog) {
        Dialog(onDismissRequest = { showAnimalSelectorDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF14B8A6), modifier = Modifier.size(20.dp))
                            Text(
                                text = "اختر فئة الحيوان",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        IconButton(
                            onClick = { showAnimalSelectorDialog = false },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF94A3B8))
                        }
                    }
                    Text(
                        text = "حدد نوع الحيوان لتصفية الأليفات والسلالات التابعة له",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    data class AnimalCategoryItem(val key: String, val title: String, val subtitle: String, val icon: ImageVector)

                    val animalOptions = listOf(
                        AnimalCategoryItem("الكل", "جميع الحيوانات", "عرض كل الأليفات والطيور المتاحة", Icons.Default.Pets),
                        AnimalCategoryItem("قطة", "قطط", "سيامي، شيرازي، بريطاني، هيمالايا، بلدي...", Icons.Default.Pets),
                        AnimalCategoryItem("كلب", "كلاب", "جولدن، هاسكي، جيرمن، لابرادور، جراء...", Icons.Default.Pets),
                        AnimalCategoryItem("طيور", "طيور وببغاوات", "كاسكو، كوكاتيل، كناري، طيور الحب...", Icons.Default.CrueltyFree),
                        AnimalCategoryItem("أرنب", "أرانب", "هولندي، ليون هيد، فلندر عملاق...", Icons.Default.CrueltyFree),
                        AnimalCategoryItem("خيل", "خيول ومواشي", "عربي أصيل، ثوروبريد، بوني...", Icons.Default.Pets),
                        AnimalCategoryItem("هامستر", "هامستر وقوارض", "سوري، كابياء، شنشيلة، قنفذ...", Icons.Default.CrueltyFree),
                        AnimalCategoryItem("سلحفاة", "سلاحف وزواحف", "برية، مائية، حرباء، جيكو...", Icons.Default.Pets),
                        AnimalCategoryItem("أسماك", "أسماك زينة", "فايتر، جولد فيش، أحواض بحرية...", Icons.Default.WaterDrop),
                        AnimalCategoryItem("دواجن", "دواجن منزلية", "بط، دجاج، سمان...", Icons.Default.Eco),
                        AnimalCategoryItem("أخرى", "حيوانات أخرى", "فصائل مميزة أخرى", Icons.Default.AutoAwesome)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(animalOptions) { (key, title, subtitle, icon) ->
                            val isSelected = selectedCategory == key
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) Color(0xFF0D9488).copy(alpha = 0.25f) else Color(0xFF0F172A),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF14B8A6) else Color.White.copy(alpha = 0.06f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCategory = key
                                        selectedBreed = "الكل"
                                        showAnimalSelectorDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF14B8A6).copy(alpha = 0.25f) else Color(0xFF1E293B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFF2DD4BF) else Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF2DD4BF) else Color.White
                                        )
                                        Text(
                                            text = subtitle,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "محدد",
                                            tint = Color(0xFF14B8A6),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Breed / Subtype Selector Dialog (اختيار نوع أو سلالة الحيوان مثل سيامي، شيرازي، جولدن...)
    if (showBreedSelectorDialog) {
        Dialog(onDismissRequest = { showBreedSelectorDialog = false }) {
            var breedSearchQuery by remember { mutableStateOf("") }

            val breedTitle = when (selectedCategory) {
                "قطة" -> "اختر سلالة القطط"
                "كلب" -> "اختر سلالة الكلاب"
                "طيور" -> "اختر نوع أو فصيلة الطائر"
                "أرنب" -> "اختر سلالة الأرانب"
                "خيل" -> "اختر فصيلة الخيل"
                "هامستر" -> "اختر فصيلة القوارض"
                "سلحفاة" -> "اختر نوع الزواحف"
                "أسماك" -> "اختر نوع الأسماك"
                else -> "اختر نوع أو سلالة الحيوان"
            }

            val categoryBreeds: List<Pair<String, String>> = remember(selectedCategory, animals) {
                val baseList: List<Pair<String, String>> = when (selectedCategory) {
                    "قطة" -> listOf(
                        Pair("الكل", "جميع سلالات القطط"),
                        Pair("سيامي", "سيامي (Siamese)"),
                        Pair("شيرازي", "شيرازي / فارسي (Persian)"),
                        Pair("هيمالايا", "هيمالايا (Himalayan)"),
                        Pair("بريطاني", "بريطاني قصير الشعر (British Shorthair)"),
                        Pair("سكوتش", "سكوتش فولد (Scottish Fold)"),
                        Pair("راغدول", "راغدول (Ragdoll)"),
                        Pair("ماين كون", "ماين كون (Maine Coon)"),
                        Pair("بنغالي", "بنغالي (Bengal)"),
                        Pair("سفنكس", "سفنكس فرعوني (Sphynx)"),
                        Pair("بيرمان", "بيرمان المقدس (Birman)"),
                        Pair("تركي", "تركي أنجورا أو فان (Turkish)"),
                        Pair("بلدي", "بلدي / محلي (Domestic / Local)"),
                        Pair("هجين", "هجين منوع (Mixed)"),
                        Pair("أخرى", "سلالة أخرى")
                    )
                    "كلب" -> listOf(
                        Pair("الكل", "جميع سلالات الكلاب"),
                        Pair("جولدن", "جولدن ريتريفر (Golden Retriever)"),
                        Pair("جيرمن", "جيرمن شيبرد (German Shepherd)"),
                        Pair("هاسكي", "هاسكي سيبيري (Siberian Husky)"),
                        Pair("لابرادور", "لابرادور ريتريفر (Labrador)"),
                        Pair("بيتبول", "بيتبول أمريكي (Pitbull)"),
                        Pair("روتوايلر", "روتوايلر (Rottweiler)"),
                        Pair("دوبرمان", "دوبرمان (Doberman)"),
                        Pair("بودل", "بودل (Poodle)"),
                        Pair("شيواوا", "شيواوا (Chihuahua)"),
                        Pair("مالينوا", "مالينوا بلجيكي (Belgian Malinois)"),
                        Pair("تشاو تشاو", "تشاو تشاو (Chow Chow)"),
                        Pair("سامويد", "سامويد (Samoyed)"),
                        Pair("بولدوج", "بولدوج (Bulldog)"),
                        Pair("بلدي", "بلدي / كنعاني (Baladi)"),
                        Pair("هجين", "هجين منوع (Mixed)"),
                        Pair("أخرى", "سلالة أخرى")
                    )
                    "طيور" -> listOf(
                        Pair("الكل", "جميع أنواع الطيور"),
                        Pair("كاسكو", "ببغاء كاسكو أفريقي رمادي"),
                        Pair("كوكاتيل", "كوكاتيل / كروان مغرد"),
                        Pair("بادجي", "بادجي / درة أسترالي"),
                        Pair("طيور الحب", "طيور الحب / فيشر (Lovebirds)"),
                        Pair("كناري", "كناري هولندي وسوري"),
                        Pair("حسون", "طائر الحسون المغرد"),
                        Pair("مكاو", "ببغاء مكاو ملون"),
                        Pair("كونيور", "ببغاء كونيور شمس"),
                        Pair("درة", "ببغاء درة هندي مطوق"),
                        Pair("زيبرا", "عصافير زيبرا فينش"),
                        Pair("حمام", "حمام زينة وزاجل"),
                        Pair("أخرى", "نوع آخر")
                    )
                    "أرنب" -> listOf(
                        Pair("الكل", "جميع سلالات الأرانب"),
                        Pair("هولندي", "هولندي (Dutch)"),
                        Pair("ليون هيد", "ليون هيد / رأس الأسد (Lionhead)"),
                        Pair("فلندر", "فلندر عملاق (Flemish Giant)"),
                        Pair("أنجورا", "أنجورا صوفي (Angora)"),
                        Pair("ريكس", "ريكس مخملي (Rex)"),
                        Pair("قزم", "قزم هولندي (Netherland Dwarf)"),
                        Pair("لوب", "هولاند لوب متدلي الأذنين"),
                        Pair("بلدي", "بلدي محلي"),
                        Pair("أخرى", "سلالة أخرى")
                    )
                    "خيل" -> listOf(
                        Pair("الكل", "جميع فصائل الخيول"),
                        Pair("عربي", "خيل عربي أصيل"),
                        Pair("ثوروبريد", "ثوروبريد أصيل"),
                        Pair("بوني", "بوني صغير"),
                        Pair("هجين", "هجين / خليط"),
                        Pair("أخرى", "فصيلة أخرى")
                    )
                    "هامستر" -> listOf(
                        Pair("الكل", "جميع القوارض"),
                        Pair("سوري", "هامستر سوري ذهبي"),
                        Pair("روسي", "هامستر روسي قزم"),
                        Pair("غينيا", "خنزير غينيا / كابياء"),
                        Pair("شنشيلة", "شنشيلة ناعمة"),
                        Pair("قنفذ", "قنفذ أفريقي قزم"),
                        Pair("أخرى", "فصيلة أخرى")
                    )
                    "سلحفاة" -> listOf(
                        Pair("الكل", "جميع الزواحف"),
                        Pair("برية", "سلحفاة برية"),
                        Pair("مائية", "سلحفاة مائية (Red-eared)"),
                        Pair("حرباء", "حرباء ملونة"),
                        Pair("إغوانا", "إغوانا خضراء"),
                        Pair("جيكو", "جيكو / برص النمر"),
                        Pair("أخرى", "نوع آخر")
                    )
                    "أسماك" -> listOf(
                        Pair("الكل", "جميع الأسماك"),
                        Pair("فايتر", "سمكة فايتر (Betta)"),
                        Pair("ذهبية", "سمكة ذهبية (Goldfish)"),
                        Pair("جوبي", "سمك الجوبي (Guppy)"),
                        Pair("أنجل", "أنجل فيش ملائكي"),
                        Pair("ديسكس", "ديسكس ملون"),
                        Pair("أخرى", "نوع آخر")
                    )
                    else -> listOf(
                        Pair("الكل", "جميع السلالات والأنواع"),
                        Pair("سيامي", "قط سيامي"),
                        Pair("شيرازي", "قط شيرازي"),
                        Pair("هيمالايا", "قط هيمالايا"),
                        Pair("بريطاني", "قط بريطاني"),
                        Pair("جولدن", "كلب جولدن ريتريفر"),
                        Pair("جيرمن", "كلب جيرمن شيبرد"),
                        Pair("هاسكي", "كلب هاسكي سيبيري"),
                        Pair("كاسكو", "ببغاء كاسكو"),
                        Pair("كوكاتيل", "كوكاتيل / كروان"),
                        Pair("هولندي", "أرنب هولندي"),
                        Pair("عربي", "خيل عربي أصيل"),
                        Pair("بلدي", "بلدي / محلي"),
                        Pair("هجين", "هجين منوع"),
                        Pair("أخرى", "سلالة أخرى")
                    )
                }

                val dynamicBreeds = animals
                    .map { it.breed.trim() }
                    .filter { b ->
                        b.isNotBlank() && b != "فصيل منوع" && b != "غير معروف" &&
                        baseList.none { it.first.equals(b, ignoreCase = true) || it.second.contains(b, ignoreCase = true) }
                    }
                    .distinct()
                    .map { Pair(it, it) }

                baseList + dynamicBreeds
            }

            val filteredBreeds = remember(categoryBreeds, breedSearchQuery) {
                if (breedSearchQuery.isBlank()) categoryBreeds
                else categoryBreeds.filter {
                    it.first.contains(breedSearchQuery, ignoreCase = true) ||
                    it.second.contains(breedSearchQuery, ignoreCase = true)
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = breedTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = { showBreedSelectorDialog = false },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF94A3B8))
                        }
                    }

                    Text(
                        text = "اختر السلالة الدقيقة (مثل سيامي، شيرازي، جولدن، هاسكي...)",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Search field inside breed dialog
                    BasicTextField(
                        value = breedSearchQuery,
                        onValueChange = { breedSearchQuery = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        cursorBrush = SolidColor(Color(0xFF38BDF8)),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    if (breedSearchQuery.isEmpty()) {
                                        Text("ابحث عن سلالة محددة...", color = Color(0xFF64748B), fontSize = 12.sp)
                                    }
                                    innerTextField()
                                }
                                if (breedSearchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { breedSearchQuery = "" },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredBreeds) { (key, label) ->
                            val isSelected = selectedBreed == key || (key == "الكل" && selectedBreed == "الكل")
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF0F172A),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedBreed = key
                                        showBreedSelectorDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF38BDF8) else Color.White
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "محدد",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 6. Price / Offer Selector Dialog (مجاني، جميع العروض، للبيع)
    if (showPriceSelectorDialog) {
        Dialog(onDismissRequest = { showPriceSelectorDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Sell, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(20.dp))
                            Text(
                                text = "نوع العرض والسعر",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        IconButton(
                            onClick = { showPriceSelectorDialog = false },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF94A3B8))
                        }
                    }
                    Text(
                        text = "اختر عرض التبني المجاني أو المعروض للبيع",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    data class PriceOptionItem(val key: String, val title: String, val subtitle: String, val icon: ImageVector, val tint: Color)

                    val priceOptions = listOf(
                        PriceOptionItem("الكل", "جميع العروض", "عرض كل الأليفات والطيور (تبني مجاني وللبيع)", Icons.Default.Sell, Color(0xFFA78BFA)),
                        PriceOptionItem("مجاني", "تبني مجاني 100%", "أليفات معروضة للتبني الإنساني دون أي مقابل مالي", Icons.Default.VolunteerActivism, Color(0xFF10B981)),
                        PriceOptionItem("للبيع", "للبيع بأسعار مناسبة", "أليفات وسلالات معروضة للبيع المباشر", Icons.Default.LocalOffer, Color(0xFFF59E0B))
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        priceOptions.forEach { item ->
                            val isSelected = selectedPriceFilter == item.key
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) item.tint.copy(alpha = 0.22f) else Color(0xFF0F172A),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) item.tint else Color.White.copy(alpha = 0.06f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPriceFilter = item.key
                                        showPriceSelectorDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(item.tint.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = item.tint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) item.tint else Color.White
                                        )
                                        Text(
                                            text = item.subtitle,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "محدد",
                                            tint = item.tint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = Color(0xFF14B8A6),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "بوابة التبني",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
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

        // Fixed Single Row Filters (All 4 in 1 row, fixed without horizontal scrolling)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. خانة "الكل"
            val isAllSelected = selectedCategory == "الكل" && selectedBreed == "الكل" && selectedPriceFilter == "الكل"
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isAllSelected) Color(0xFF0D9488) else Color(0xFF1E293B),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isAllSelected) Color(0xFF14B8A6) else Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .weight(0.72f)
                    .clickable {
                        selectedCategory = "الكل"
                        selectedBreed = "الكل"
                        selectedPriceFilter = "الكل"
                    }
                    .testTag("filter_all_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = if (isAllSelected) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "الكل",
                        fontSize = 12.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isAllSelected) Color.White else Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 2. خانة "اختيار الحيوان"
            val isCategoryActive = selectedCategory != "الكل"
            val animalButtonLabel = when (selectedCategory) {
                "الكل" -> "الحيوان"
                "قطة" -> "قطط"
                "كلب" -> "كلاب"
                "طيور" -> "طيور"
                "أرنب" -> "أرانب"
                "خيل" -> "خيول"
                "هامستر" -> "قوارض"
                "سلحفاة" -> "زواحف"
                "أسماك" -> "أسماك"
                "دواجن" -> "دواجن"
                "أخرى" -> "أخرى"
                else -> selectedCategory
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isCategoryActive) Color(0xFF0D9488) else Color(0xFF1E293B),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isCategoryActive) Color(0xFF14B8A6) else Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .weight(1.05f)
                    .clickable { showAnimalSelectorDialog = true }
                    .testTag("filter_animal_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = animalButtonLabel,
                        fontSize = 11.5.sp,
                        fontWeight = if (isCategoryActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCategoryActive) Color.White else Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "قائمة الحيوانات",
                        tint = if (isCategoryActive) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // 3. خانة "نوع / سلالة الحيوان"
            val isBreedActive = selectedBreed != "الكل" && selectedBreed.isNotBlank()
            val breedButtonLabel = if (isBreedActive) {
                selectedBreed
            } else {
                "النوع"
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isBreedActive) Color(0xFF0284C7) else Color(0xFF1E293B),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isBreedActive) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .weight(0.95f)
                    .clickable { showBreedSelectorDialog = true }
                    .testTag("filter_breed_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = breedButtonLabel,
                        fontSize = 11.5.sp,
                        fontWeight = if (isBreedActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isBreedActive) Color.White else Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "قائمة السلالات",
                        tint = if (isBreedActive) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // 4. خانة "نوع العرض / السعر" (جميع العروض / تبني مجاني / للبيع)
            val isPriceActive = selectedPriceFilter != "الكل"
            val priceButtonLabel = when (selectedPriceFilter) {
                "مجاني" -> "مجاني"
                "للبيع" -> "للبيع"
                else -> "العرض"
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isPriceActive) Color(0xFF7C3AED) else Color(0xFF1E293B),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isPriceActive) Color(0xFFA78BFA) else Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .weight(0.95f)
                    .clickable { showPriceSelectorDialog = true }
                    .testTag("filter_price_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = priceButtonLabel,
                        fontSize = 11.5.sp,
                        fontWeight = if (isPriceActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isPriceActive) Color.White else Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "قائمة العروض",
                        tint = if (isPriceActive) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(15.dp)
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
                "خيل" -> animal.species.contains("خيل", true) || animal.species.contains("حصان", true) || animal.species.contains("مهر", true)
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
            val matchBreed = when {
                selectedBreed == "الكل" || selectedBreed.isBlank() -> true
                selectedBreed == "أخرى" -> true
                else -> animal.breed.contains(selectedBreed, ignoreCase = true) ||
                        animal.description.contains(selectedBreed, ignoreCase = true) ||
                        animal.name.contains(selectedBreed, ignoreCase = true) ||
                        animal.species.contains(selectedBreed, ignoreCase = true)
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

            matchCategory && matchBreed && matchPrice && matchQuery
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
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(48.dp)
                    )
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
                            selectedBreed = "الكل"
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

        Spacer(modifier = Modifier.height(110.dp))
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

            val coroutineScope = rememberCoroutineScope()
            var isUploadingPetImage by remember { mutableStateOf(false) }

            val petPhotoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                if (uri != null) {
                    isUploadingPetImage = true
                    coroutineScope.launch {
                        val result = FirebaseManager.uploadMedia(uri, folder = "pets")
                        isUploadingPetImage = false
                        if (result.isSuccess) {
                            newImageUrl = result.getOrNull() ?: uri.toString()
                        } else {
                            newImageUrl = uri.toString()
                        }
                    }
                }
            }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "المعلومات الأساسية",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }

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
                                        val selectedPetCategory = DOMESTIC_PET_LIST.find { it.name == newSpecies }?.category ?: ""
                                        val petIconVector = when {
                                            selectedPetCategory.contains("قط") -> Icons.Default.Pets
                                            selectedPetCategory.contains("كلب") -> Icons.Default.Pets
                                            selectedPetCategory.contains("طيور") -> Icons.Default.CrueltyFree
                                            selectedPetCategory.contains("قوارض") || selectedPetCategory.contains("أرانب") -> Icons.Default.CrueltyFree
                                            selectedPetCategory.contains("زواحف") -> Icons.Default.Pets
                                            selectedPetCategory.contains("أسماك") -> Icons.Default.WaterDrop
                                            selectedPetCategory.contains("دواجن") -> Icons.Default.Eco
                                            else -> Icons.Default.Pets
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0D9488).copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = petIconVector,
                                                contentDescription = null,
                                                tint = Color(0xFF2DD4BF),
                                                modifier = Modifier.size(20.dp)
                                            )
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
                                    listOf("ذكر" to "ذكر", "أنثى" to "أنثى").forEach { (gKey, gLabel) ->
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
                                    listOf("مجاني" to "مجاني", "للبيع" to "للبيع").forEach { (pKey, pLabel) ->
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "التفاصيل والوصف",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }

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

                        // Pet Image Picker (Gallery / Direct Photo)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = Color(0xFFCBD5E1),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("صورة الأليف", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFCBD5E1))
                            }

                            Button(
                                 onClick = {
                                     petPhotoPickerLauncher.launch(
                                         PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                     )
                                 },
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .height(48.dp),
                                 shape = RoundedCornerShape(14.dp),
                                 colors = ButtonDefaults.buttonColors(
                                     containerColor = Color(0xFF0D9488)
                                 ),
                                 enabled = !isUploadingPetImage
                             ) {
                                 if (isUploadingPetImage) {
                                     CircularProgressIndicator(
                                         color = Color.White,
                                         modifier = Modifier.size(20.dp),
                                         strokeWidth = 2.dp
                                     )
                                     Spacer(modifier = Modifier.width(8.dp))
                                     Text("جاري رفع الصورة إلى السحابة...", color = Color.White, fontSize = 13.sp)
                                 } else {
                                     Icon(
                                         imageVector = Icons.Default.AddPhotoAlternate,
                                         contentDescription = null,
                                         tint = Color.White,
                                         modifier = Modifier.size(22.dp)
                                     )
                                     Spacer(modifier = Modifier.width(8.dp))
                                     Text(
                                         text = if (newImageUrl.isBlank()) "اختر صورة الأليف من الهاتف" else "تغيير صورة الأليف",
                                         fontWeight = FontWeight.Bold,
                                         fontSize = 13.5.sp,
                                         color = Color.White
                                     )
                                 }
                             }

                            if (newImageUrl.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(1.dp, Color(0xFF0D9488), RoundedCornerShape(14.dp))
                                ) {
                                    AsyncImage(
                                        model = newImageUrl,
                                        contentDescription = "معاينة صورة الأليف",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = { newImageUrl = "" },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(28.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "حذف الصورة", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

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
                                    text = "نشر الأليف",
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
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (isForSale) Icons.Default.Sell else Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (isForSale) "للبيع" else "مجاني",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (animal.gender == "ذكر") Icons.Default.Male else Icons.Default.Female,
                            contentDescription = null,
                            tint = if (animal.gender == "ذكر") Color(0xFF38BDF8) else Color(0xFFF472B6),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = animal.gender,
                            color = if (animal.gender == "ذكر") Color(0xFF38BDF8) else Color(0xFFF472B6),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Age Overlay (Bottom-Start)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = animal.age,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
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
    val category: String,
    val description: String,
    val defaultImageUrl: String
)

val DOMESTIC_PET_LIST = listOf(
    // قطط
    DomesticPetItem("قطة (أليف منزلي)", "قطط", "القط المنزلي الأكثر شعبية، هادئ ومحبوب ونظيف", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600"),
    DomesticPetItem("قط شيرازي / فارسي", "قطط", "قط طويل الشعر، هادئ ولطيف جداً ومناسب للبيوت", "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=600"),
    DomesticPetItem("قط سيامي", "قطط", "قط ذكي، فضولي، اجتماعي وصوته مميز", "https://images.unsplash.com/photo-1513360309081-38f0762daed1?w=600"),
    DomesticPetItem("قط سكوتش فولد", "قطط", "قط ذو أذنين مطويتين وشخصية ودودة هادئة", "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=600"),
    DomesticPetItem("قط بريطاني قصير الشعر", "قطط", "قط ممتلئ وهادئ ومناسب للشقق والعائلات", "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=600"),

    // كلاب
    DomesticPetItem("كلب (أليف منزلي)", "كلاب", "وفي ومخلص، رفيق العائلة الأول والمنزل", "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600"),
    DomesticPetItem("كلب هاسكي سيبيري", "كلاب", "كلب نشيط وفرو كثيف وجميل ومحب للعب", "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=600"),
    DomesticPetItem("كلب جولدن ريتريفر / لابرادور", "كلاب", "ودود جداً ومحب للأطفال والعائلات وسهل التدريب", "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600"),
    DomesticPetItem("كلب جيرمن شيبارد (راعي ألماني)", "كلاب", "شديد الذكاء ومخلص وحارس ممتاز", "https://images.unsplash.com/photo-1589941013453-ec89f33b5e95?w=600"),
    DomesticPetItem("كلب بيتبول / بولدوج", "كلاب", "كلب قوي ومخلص للعائلة عند الرعاية الحسنة", "https://images.unsplash.com/photo-1537151608828-ea2b11777ee8?w=600"),
    DomesticPetItem("كلب صغير (بودل / تشيهواهوا / بوميرانيان)", "كلاب", "كلب صغير الحجم مثالي للشقق والمنازل الصغيرة", "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=600"),

    // طيور وببغاوات
    DomesticPetItem("ببغاء (ببغاء متكلم)", "طيور", "ذكي وممتع وقادر على تقليد الأصوات والكلمات", "https://images.unsplash.com/photo-1522850949506-585e5e2298f7?w=600"),
    DomesticPetItem("طائر الكناري", "طيور", "تغريد عذب وجميل وألوان صفراء زاهية تملأ البيت", "https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=600"),
    DomesticPetItem("طائر الحسون", "طيور", "صوت عذب وألوان زاهية وريش جذاب", "https://images.unsplash.com/photo-1555169062-013468b47731?w=600"),
    DomesticPetItem("طائر البادجي (طائر الحب)", "طيور", "طائر صغير اجتماعي ملون وسهل التربية", "https://images.unsplash.com/photo-1544943910-4c1dc44a0d9b?w=600"),
    DomesticPetItem("طائر الكوكاتيل", "طيور", "ببغاء قزم بتاج أصفر وخدود برتقالية وودود", "https://images.unsplash.com/photo-1598133894008-61f7fdb8cc3a?w=600"),
    DomesticPetItem("طائر المينا", "طيور", "طائر ذكي ونبيه يحاكي أصوات المنزل والكلام", "https://images.unsplash.com/photo-1516610101564-e10be23fe36f?w=600"),
    DomesticPetItem("حمام زينة / يمام", "طيور", "طائر هادئ وأنيق وودود للتربية المنزلية", "https://images.unsplash.com/photo-1546853020-ca4909aef454?w=600"),

    // قوارض وأرانب
    DomesticPetItem("أرنب منزلي (بلدي / هولندي / قزم)", "قوارض وأرانب", "أليف وديع وناعم يحب الخضار واللعب الهادئ", "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=600"),
    DomesticPetItem("هامستر (سوري / قزم روسي)", "قوارض وأرانب", "صغير ولطيف جداً ومناسب للأطفال في قفص مجهز", "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=600"),
    DomesticPetItem("خنزير غينيا (كابياء)", "قوارض وأرانب", "حيوان اجتماعي لطيف يصدر أصواتاً محببة عند الفرح", "https://images.unsplash.com/photo-1535241749838-299277b6305f?w=600"),
    DomesticPetItem("شنشيلة (Chinchilla)", "قوارض وأرانب", "فرو ناعم للغاية مثل الحرير ويحب القفز", "https://images.unsplash.com/photo-1583337130417-3346a1be7dee?w=600"),
    DomesticPetItem("قنفذ إفريقي قزم", "قوارض وأرانب", "حيوان فريد هادئ وغير مؤذي يتحول لكرة شوكية", "https://images.unsplash.com/photo-1508921912186-1d1a45ebb3c1?w=600"),
    DomesticPetItem("فيريت (ابن عرس الأليف)", "قوارض وأرانب", "لعوب وفضولي للغاية ومرح جداً داخل المنزل", "https://images.unsplash.com/photo-1618255651586-1eb8674d825c?w=600"),
    DomesticPetItem("سنجاب طائر (شوجر جلايدر)", "قوارض وأرانب", "حيوان جرابي صغير يطير بلطف ويتعلق بصاحبه", "https://images.unsplash.com/photo-1507666405895-422efe7d517f?w=600"),
    DomesticPetItem("فأر أليف / جربوع", "قوارض وأرانب", "قارض صغير ونظيف وسهل الرعاية في القفص", "https://images.unsplash.com/photo-1583337130417-3346a1be7dee?w=600"),

    // زواحف وبرمائيات
    DomesticPetItem("سلحفاة (برية أو مائية)", "زواحف", "أليف هادئ ومسالم يعيش لسنوات طويلة في حوضه", "https://images.unsplash.com/photo-1437622368342-7a3d73a34c8f?w=600"),
    DomesticPetItem("أبو بريص الفهد (جيكو Leopard Gecko)", "زواحف", "زاحف هادئ أليف وسهل العناية بألوان جلد جميلة", "https://images.unsplash.com/photo-1500463955536-f8b11747ef98?w=600"),
    DomesticPetItem("إغوانا خضراء", "زواحف", "سحلية كبيرة وعشبية لمحبي الزواحف والتربية المنزلية", "https://images.unsplash.com/photo-1504450758481-7338eba7524a?w=600"),
    DomesticPetItem("حرباء منزلية", "زواحف", "تغير ألوانها وحركتها مميزة وعيونها المستقلة", "https://images.unsplash.com/photo-1563281577-a7be47e20db9?w=600"),
    DomesticPetItem("أكسولوتل (عفريت الماء / سمندر)", "زواحف", "كائن برمائي مبتسم يعيش في الأحواض المائية الباردة", "https://images.unsplash.com/photo-1500463955536-f8b11747ef98?w=600"),
    DomesticPetItem("ضفدع شجري أليف", "زواحف", "ألوان زاهية وجمال طبيعي هادئ في الأحواض", "https://images.unsplash.com/photo-1500463955536-f8b11747ef98?w=600"),

    // أسماك وكائنات مائية
    DomesticPetItem("سمكة الفايتر (المقاتل السيامي / Betta)", "أسماك", "ألوان ساحرة وزعانف ملكية تعيش في أحواض فردية", "https://images.unsplash.com/photo-1522069169874-c58ec4b76be5?w=600"),
    DomesticPetItem("سمكة ذهبية (جولد فيش)", "أسماك", "سمكة الأحواض الأشهر والأكثر ألفة وجمالاً", "https://images.unsplash.com/photo-1524704654690-b56c05c78a00?w=600"),
    DomesticPetItem("أسماك زينة استوائية (نيون / جوفي / مولي)", "أسماك", "أسماك ملونة وسريعة الحركة تعطي حيوية للحوض", "https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=600"),
    DomesticPetItem("جمبري زينة مائي (شريمب نيوكاريدينا)", "أسماك", "ينظف الحوض وله ألوان حمراء وزرقاء مميزة", "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=600"),

    // دواجن وطيور منزلية
    DomesticPetItem("بط صغير / بط منزلي", "دواجن", "بط أليف مرح ومحب للماء والحدائق المنزلية", "https://images.unsplash.com/photo-1465153690352-10c1b29577f8?w=600"),
    DomesticPetItem("دجاج سيلكي (دجاج حريري / زينة)", "دواجن", "دجاج ذو ريش قطني ناعم وأليف جداً ومسالم", "https://images.unsplash.com/photo-1548550023-2bdb3c5beed7?w=600"),
    DomesticPetItem("طائر السمان المنزلي", "دواجن", "طائر صغير وسريع الإنتاج وسهل التربية", "https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=600"),

    // أخرى
    DomesticPetItem("حيوان أليف آخر", "أخرى", "أي حيوان أليف منزلي آخر تود عرضه ورعايته", "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600")
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
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = Color(0xFF14B8A6),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "اختر نوع الأليف المنزلي",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "جميع الحيوانات المنزلية مع إمكانية التمرير والبحث",
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
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF2DD4BF),
                                modifier = Modifier.size(18.dp)
                            )
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
                                    // Icon Avatar
                                    val petIcon = when {
                                        item.category.contains("قط") -> Icons.Default.Pets
                                        item.category.contains("كلب") -> Icons.Default.Pets
                                        item.category.contains("طيور") -> Icons.Default.CrueltyFree
                                        item.category.contains("قوارض") || item.category.contains("أرانب") -> Icons.Default.CrueltyFree
                                        item.category.contains("زواحف") -> Icons.Default.Pets
                                        item.category.contains("أسماك") -> Icons.Default.WaterDrop
                                        item.category.contains("دواجن") -> Icons.Default.Eco
                                        else -> Icons.Default.AutoAwesome
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF0D9488) else Color(0xFF1E293B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = petIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else Color(0xFF2DD4BF),
                                            modifier = Modifier.size(20.dp)
                                        )
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
