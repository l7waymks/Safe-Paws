package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ChatMessage
import com.example.MainViewModel
import com.example.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotLayout(viewModel: MainViewModel, onClose: () -> Unit) {
    // Intercept system Back button so the app doesn't exit when pressing back
    BackHandler(enabled = true) {
        onClose()
    }

    val messages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    var inputMsgText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size, isChatLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Deep polished slate
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Header Bar
        Surface(
            color = Color(0xFF1E293B),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Monkey Doctor Avatar Badge
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF00B4D8), CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.monkey_doctor_avatar_1786969237937),
                            contentDescription = "المستشار البيطري",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "المستشار البيطري 🐾",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            // Online indicator
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                        }
                        Text(
                            text = "استشارات بيطرية ورعاية فورية",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "إغلاق الدردشة",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Messages List Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val isUser = message.isUser
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isUser) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp, top = 2.dp)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color(0xFF00B4D8), CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.monkey_doctor_avatar_1786969237937),
                                    contentDescription = "المستشار البيطري",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = if (isUser) 18.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 18.dp
                                    )
                                )
                                .background(
                                    if (isUser) {
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF0D9488), Color(0xFF0F766E))
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF1E293B), Color(0xFF334155))
                                        )
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isUser) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = if (isUser) 18.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 18.dp
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = message.text,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                if (isChatLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color(0xFF00B4D8), CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.monkey_doctor_avatar_1786969237937),
                                    contentDescription = "المستشار البيطري",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF00B4D8)
                                    )
                                    Text(
                                        text = "المستشار يكتب الرد...",
                                        fontSize = 13.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Medical Suggestion Chips
        val quickChips = listOf(
            "🌡️ ارتفاع الحرارة" to "أليفي يعاني من ارتفاع في درجة الحرارة وسخونة، ما الإسعافات الأولية والتصرف الصحيح؟",
            "🩺 القيء أو الاستفراغ" to "أليفي يستفرغ منذ الصباح، ما هي الأسباب والعلاج المنزلي المناسب؟",
            "🥣 فقدان الشهية والخمول" to "قطتي/كلبي يرفض الأكل ولديه خمول شديد، كيف أتعامل معه؟",
            "💉 جدول التطعيمات" to "ما هو جدول التطعيمات والتحصينات الوقائية الأساسية؟",
            "🚫 أطعمة سامة ممنوعة" to "ما هي الأطعمة الممنوعة والسامة التي يجب تجنب تقديمها للحيوانات؟",
            "🐾 الفطريات وتساقط الشعر" to "أليفي يعاني من حكة شديدة وتساقط شعر وبقع دائرية، ما العلاج؟",
            "🍼 رعاية القطط والجراء الرضيعة" to "كيف أعتني بجرو أو قطة رضيعة بدون أم من حيث الحليب والتدفئة؟",
            "🩹 إسعافات الجروح والكسور" to "ما هي خطوات الإسعاف الأولي السريعة عند حدوث جرح نازف أو كسر للأليف؟"
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickChips) { (label, query) ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF00B4D8).copy(alpha = 0.4f)),
                    modifier = Modifier.clickable {
                        viewModel.sendChatMessage(query)
                    }
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Bottom Input Field Bar
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMsgText,
                    onValueChange = { inputMsgText = it },
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputMsgText.isNotBlank()) {
                                viewModel.sendChatMessage(inputMsgText.trim())
                                inputMsgText = ""
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    placeholder = {
                        Text(
                            text = "اكتب استفسارك هنا (مثال: أسباب خمول القطة)...",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text_field"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    textStyle = TextStyle(fontSize = 14.sp, color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF00B4D8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                IconButton(
                    onClick = {
                        if (inputMsgText.isNotBlank()) {
                            viewModel.sendChatMessage(inputMsgText.trim())
                            inputMsgText = ""
                            focusManager.clearFocus()
                        }
                    },
                    enabled = inputMsgText.isNotBlank(),
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputMsgText.isNotBlank()) Color(0xFF0D9488) else Color(0xFF334155)
                        )
                        .testTag("send_chat_message_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال",
                        tint = if (inputMsgText.isNotBlank()) Color.White else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
