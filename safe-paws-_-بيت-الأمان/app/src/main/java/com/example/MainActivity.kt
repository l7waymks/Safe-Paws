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

import com.example.ui.components.*
import com.example.ui.screens.*

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

@Composable


fun SafePawsApp(viewModel: MainViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    var showClinicsDialog by remember { mutableStateOf(false) }
    var showSheltersDialog by remember { mutableStateOf(false) }
    val showGlobalExpertChat by viewModel.showExpertChat.collectAsState()

    if (showClinicsDialog) {
        VetClinicsDialog(onDismiss = { showClinicsDialog = false })
    }
    if (showSheltersDialog) {
        PartnerSheltersDialog(onDismiss = { showSheltersDialog = false })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(106.dp)
                ) {
                    val barWidth = maxWidth
                    val leftPeakX = barWidth * 0.18f
                    val rightPeakX = barWidth * 0.82f
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = PrimaryTeal,
                                shape = CustomBottomBarShape()
                            )
                    ) {
                        // Left Peak Button (Clinics / Vet Expert Chat)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = leftPeakX - 24.dp, top = 2.dp)
                                .size(48.dp)
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null,
                                    onClick = { viewModel.showExpertChat.value = true }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            VetPeakIcon(color = Color.White)
                        }

                    // Right Peak Button (Shelters)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = rightPeakX - 24.dp, top = 2.dp)
                            .size(48.dp)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = { showSheltersDialog = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        ShelterPeakIcon(color = Color.White)
                    }

                    // Force LTR inside the Bottom Bar so that tabs align exactly Left to Right:
                    // Profile -> Map -> Adoption -> Community -> Home
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomTabItem(
                                selected = currentTab == AppTab.Profile,
                                onClick = { viewModel.selectTab(AppTab.Profile) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "الملف",
                                        tint = if (currentTab == AppTab.Profile) Color.White else Color.White.copy(alpha = 0.7f)
                                    )
                                },
                                label = "الملف",
                                isProfile = true
                            )
                            CustomTabItem(
                                selected = currentTab == AppTab.Map,
                                onClick = { viewModel.selectTab(AppTab.Map) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "الخريطة",
                                        tint = if (currentTab == AppTab.Map) Color.White else Color.White.copy(alpha = 0.7f)
                                    )
                                },
                                label = "الخريطة"
                            )
                            CustomTabItem(
                                selected = currentTab == AppTab.Adoption,
                                onClick = { viewModel.selectTab(AppTab.Adoption) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.VolunteerActivism,
                                        contentDescription = "التبني",
                                        tint = if (currentTab == AppTab.Adoption) Color.White else Color.White.copy(alpha = 0.7f)
                                    )
                                },
                                label = "التبني"
                            )
                            CustomTabItem(
                                selected = currentTab == AppTab.Community,
                                onClick = { viewModel.selectTab(AppTab.Community) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Comment,
                                        contentDescription = "المجتمع",
                                        tint = if (currentTab == AppTab.Community) Color.White else Color.White.copy(alpha = 0.7f)
                                    )
                                },
                                label = "المجتمع"
                            )
                            CustomTabItem(
                                selected = currentTab == AppTab.Home,
                                onClick = { viewModel.selectTab(AppTab.Home) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "الرئيسية",
                                        tint = if (currentTab == AppTab.Home) Color.White else Color.White.copy(alpha = 0.7f)
                                    )
                                },
                                label = "الرئيسية"
                            )
                        }
                    }
                }
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

    AnimatedVisibility(
        visible = showGlobalExpertChat,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            ChatBotLayout(viewModel = viewModel, onClose = { viewModel.showExpertChat.value = false })
        }
    }
    }
}
