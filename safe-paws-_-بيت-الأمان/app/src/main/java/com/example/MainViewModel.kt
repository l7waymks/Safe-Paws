package com.example

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab {
    Home,
    Community,
    Adoption,
    Map,
    Profile
}

data class AdoptionPipeline(
    val petId: String = "1",
    val petName: String = "بندق",
    val petBreed: String = "جولدن ريتريفر",
    val petAge: String = "سنتان",
    val petGender: String = "ذكر",
    val petImageUrl: String = "https://lh3.googleusercontent.com/aida-public/AB6AXuAoDSKCeLAzLLM17fELuvP0tR8ureUg8OBaPcngBQw9v5hdsPZr2NYfASeVyJP1CYV3j0vgtGF8PApwnv_67ijmt3tq1cJDaiNSKU-rvY7WvivV5pNJjcFwm74bj49oL5foWS-yPG4Uo4KMvVY0fyM7OhcgJ9pv7eh3GyNJh4aqDixuJuZkbN4APoqE_nbLJVSkSzemNc2A_w8fWVL949cDMpGFNYv3k6GWJE09CJkSZfMbh2US1BPzExZIpEutGp7szWLmXQLZVEk",
    val currentStep: Int = 2, // 1 to 5
    val stepStatus: String = "قيد المراجعة",
    val submissionDate: String = "٢٤ أكتوبر ٢٠٢٦",
    // Survey answers
    val homeStyle: String = "شقة سكنية",
    val securityEnabled: String = "نعم، النوافذ والشرفات مؤمنة",
    val dailyHoursAlone: String = "أقل من ٤ ساعات",
    val kidsPresence: String = "نعم، يتوفر أطفال بالمنزل",
    val activityIntent: String = "المشي والجري اليومي في الحديقة"
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String = "الآن"
)

data class StrayIncident(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val reporter: String,
    val commentsCount: Int,
    val likesCount: Int,
    val isEmergency: Boolean = false,
    val timestamp: String,
    val lat: Double = 24.7136,
    val lng: Double = 46.6753,
    val imageUrl: String? = null,
    val reporterAvatarUrl: String? = null,
    val repostsCount: Int = 0,
    val sharesCount: Int = 0
)

class MainViewModel : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.Home)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _animalsList = MutableStateFlow<List<AnimalItem>>(emptyList())
    val animalsList: StateFlow<List<AnimalItem>> = _animalsList.asStateFlow()

    private val _selectedAnimal = MutableStateFlow<AnimalItem?>(null)
    val selectedAnimal: StateFlow<AnimalItem?> = _selectedAnimal.asStateFlow()

    private val _activeComments = MutableStateFlow<List<CommentItem>>(emptyList())
    val activeComments: StateFlow<List<CommentItem>> = _activeComments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmittingComment = MutableStateFlow(false)
    val isSubmittingComment: StateFlow<Boolean> = _isSubmittingComment.asStateFlow()

    // Active User Context
    private val _trustScore = MutableStateFlow(85)
    val trustScore: StateFlow<Int> = _trustScore.asStateFlow()

    private val _rescuesCount = MutableStateFlow(42)
    val rescuesCount: StateFlow<Int> = _rescuesCount.asStateFlow()

    private val _profileImageUrl = MutableStateFlow("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400")
    val profileImageUrl: StateFlow<String> = _profileImageUrl.asStateFlow()

    fun updateProfileImageUrl(url: String) {
        _profileImageUrl.value = url
    }

    // Adoption tracker workflow
    private val _adoptionPipeline = MutableStateFlow(AdoptionPipeline())
    val adoptionPipeline: StateFlow<AdoptionPipeline> = _adoptionPipeline.asStateFlow()

    // Chat Expert Bot
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("1", "أهلاً بك في البث الاستشاري لـ بيت الأمان 🐾 أنا مستشارك الخبير الذكي. كيف يمكنني مساعدتك في تجهيز بيتك لاستقبال أليفك القادم أو الإجابة على استفساراتك العلاجية؟", false)
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // UI state for showing the expert chat screen globally
    val showExpertChat = MutableStateFlow(false)

    // PetMatch Onboarding State
    private val _onboardingQuizCompleted = MutableStateFlow(false)
    val onboardingQuizCompleted: StateFlow<Boolean> = _onboardingQuizCompleted.asStateFlow()

    private val _quizPercentage = MutableStateFlow<Int?>(null)
    val quizPercentage: StateFlow<Int?> = _quizPercentage.asStateFlow()

    private val _quizAnalysisReport = MutableStateFlow("")
    val quizAnalysisReport: StateFlow<String> = _quizAnalysisReport.asStateFlow()

    private val _isQuizAnalyzing = MutableStateFlow(false)
    val isQuizAnalyzing: StateFlow<Boolean> = _isQuizAnalyzing.asStateFlow()

    // Stray Map Pins state
    private val _strayIncidents = MutableStateFlow(
        listOf(
            StrayIncident(
                id = "s0", 
                title = "🚨🚨🚨 الإعلامي علي نوري :", 
                description = "المدرب المغربي وليد الركراكي يدخل في مفاوضات لتدريب أحد الأندية الخليجية الكبرى في الموسم المقبل.", 
                location = "الرياض، المملكة العربية السعودية", 
                reporter = "rengosport18", 
                commentsCount = 30, 
                likesCount = 317, 
                isEmergency = false, 
                timestamp = "22 h", 
                lat = 24.7136, 
                lng = 46.6753,
                imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=600",
                reporterAvatarUrl = null,
                repostsCount = 2,
                sharesCount = 16
            ),
            StrayIncident(
                id = "s1", 
                title = "🚨 أربعة جراء ضائعة بحاجة لرعاية عاجلة", 
                description = "تم العثور عليها في الشارع بجانب الحديقة العامة وحالتها مستقرة حالياً، بانتظار عائلة لتبنيها أو متطوع للاعتناء بها مؤقتاً.", 
                location = "حي الياسمين، الرياض", 
                reporter = "أحمد محمد", 
                commentsCount = 24, 
                likesCount = 158, 
                isEmergency = true, 
                timestamp = "منذ ٢٢ ساعة", 
                lat = 24.8136, 
                lng = 46.6853,
                imageUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600",
                reporterAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100",
                repostsCount = 4,
                sharesCount = 9
            ),
            StrayIncident(
                id = "s2", 
                title = "🚨 كلب جولدن ضائع - حالة طارئة جداً", 
                description = "تم رصد هذا الكلب اللطيف يتجول بمفرده ويبدو عليه التعب والذعر الشديد. يحتاج للمساعدة العاجلة للوصول إلى ملجأ آمن.", 
                location = "حي الملز، الرياض", 
                reporter = "طبيب رعاية", 
                commentsCount = 15, 
                likesCount = 184, 
                isEmergency = true, 
                timestamp = "منذ ٥ ساعات", 
                lat = 24.6636, 
                lng = 46.7253,
                imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600",
                reporterAvatarUrl = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=100",
                repostsCount = 5,
                sharesCount = 24
            ),
            StrayIncident(
                id = "s3", 
                title = "😻 جمال العيون في الطبيعة والإنقاذ", 
                description = "لقطة فنية لإحدى حالات إنقاذ القطط السابقة في الرياض. بفضل تبرعاتكم ورعايتكم المستمرة نوفر حياة أفضل لأصدقائنا الصغار.", 
                location = "حي السليمانية، الرياض", 
                reporter = "سارة أحمد", 
                commentsCount = 42, 
                likesCount = 520, 
                isEmergency = false, 
                timestamp = "منذ ٣ أيام", 
                lat = 24.7036, 
                lng = 46.6953,
                imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600",
                reporterAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100",
                repostsCount = 11,
                sharesCount = 82
            )
        )
    )
    val strayIncidents: StateFlow<List<StrayIncident>> = _strayIncidents.asStateFlow()

    init {
        fetchAnimals()
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun fetchAnimals() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = SupabaseManager.getAnimals()
                _animalsList.value = list
            } catch (e: Exception) {
                _animalsList.value = SupabaseManager.fallbackAnimals
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAnimal(
        name: String,
        species: String,
        breed: String,
        age: String,
        gender: String,
        description: String,
        backstory: String,
        imageUrl: String,
        priceStatus: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newAnimal = SupabaseManager.postAnimal(
                    name = name,
                    species = species,
                    breed = breed,
                    age = age,
                    gender = gender,
                    description = description,
                    backstory = backstory,
                    imageUrl = imageUrl,
                    priceStatus = priceStatus
                )
                if (newAnimal != null) {
                    _animalsList.value = listOf(newAnimal) + _animalsList.value
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error posting animal", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectAnimal(animal: AnimalItem?) {
        _selectedAnimal.value = animal
        if (animal != null) {
            fetchComments(animal.id)
        } else {
            _activeComments.value = emptyList()
        }
    }

    private fun fetchComments(animalId: String) {
        viewModelScope.launch {
            try {
                val list = SupabaseManager.getComments(animalId)
                _activeComments.value = list
            } catch (e: Exception) {
                _activeComments.value = SupabaseManager.fallbackComments.filter { it.animalId == animalId }
            }
        }
    }

    fun incrementLikes(animal: AnimalItem) {
        viewModelScope.launch {
            // Update UI instantly (Optimistic UI update)
            val updatedList = _animalsList.value.map {
                if (it.id == animal.id) it.copy(likesCount = it.likesCount + 1) else it
            }
            _animalsList.value = updatedList
            if (_selectedAnimal.value?.id == animal.id) {
                _selectedAnimal.value = _selectedAnimal.value?.copy(likesCount = animal.likesCount + 1)
            }

            // Patch Supabase
            try {
                SupabaseManager.incrementLike(animal.id, animal.likesCount)
            } catch (e: Exception) {
                // Keep local optimistic state anyway
            }
        }
    }

    fun addComment(text: String) {
        val animal = _selectedAnimal.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            _isSubmittingComment.value = true
            val author = "أحمد محمد" // Logged in user profile
            
            // Add Optimistically
            val tempId = (1000..9999).random().toString()
            val optimisticComment = CommentItem(tempId, animal.id, author, text, "الآن")
            _activeComments.value = listOf(optimisticComment) + _activeComments.value

            try {
                val result = SupabaseManager.postComment(animal.id, author, text)
                if (result != null) {
                    // Update list with the real returned comment from server
                    _activeComments.value = _activeComments.value.map {
                        if (it.id == tempId) result else it
                    }
                }
            } catch (e: Exception) {
                // keep optimistic
            } finally {
                _isSubmittingComment.value = false
            }
        }
    }

    // Onboarding Matcher Quiz API Call
    fun runMatchingQuiz(
        activity: String,
        workHours: String,
        housing: String,
        kids: String,
        experience: String
    ) {
        val curAnimal = _selectedAnimal.value ?: return
        viewModelScope.launch {
            _isQuizAnalyzing.value = true
            _onboardingQuizCompleted.value = true
            
            val answersList = listOf(activity, workHours, housing, kids, experience)
            try {
                val result = GeminiManager.getPetMatchAnalysis(
                    answersList,
                    curAnimal.name,
                    curAnimal.breed,
                    curAnimal.backstory
                )
                
                // Update percentage and text report
                _quizPercentage.value = result.first
                _quizAnalysisReport.value = result.second
                
                // Dynamic trust score reward
                _trustScore.value = _trustScore.value + 5
            } catch (e: Exception) {
                _quizPercentage.value = 85
                _quizAnalysisReport.value = "تحليل محلي: تتوافق بيئة السكن ومستوى نشاطك بشكل ممتاز مع احتياجات التدريب المناسبة لـ ${curAnimal.name}."
            } finally {
                _isQuizAnalyzing.value = false
            }
        }
    }

    fun resetMatchQuiz() {
        _onboardingQuizCompleted.value = false
        _quizPercentage.value = null
        _quizAnalysisReport.value = ""
    }

    // Active Expert Chatbot
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage((1000..9999).random().toString(), text, true)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isChatLoading.value = true
            
            // Collect full dialog clean history for API context window
            val apiHistory = _chatMessages.value.map { Pair(it.text, it.isUser) }
            try {
                val replyText = GeminiManager.getExpertReply(apiHistory)
                val replyMsg = ChatMessage((1000..9999).random().toString(), replyText, false)
                _chatMessages.value = _chatMessages.value + replyMsg
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    (1000..9999).random().toString(), 
                    "عذراً، واجهت عطلاً بسيطاً بالاتصال بالشبكة. يرجى تكرار السؤال أو التحقق مجدداً بعد ثوانٍ.", 
                    false
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun updateAdoptionPipelineAnswers(
        style: String,
        security: String,
        hours: String,
        kids: String,
        activity: String
    ) {
        _adoptionPipeline.value = _adoptionPipeline.value.copy(
            homeStyle = style,
            securityEnabled = security,
            dailyHoursAlone = hours,
            kidsPresence = kids,
            activityIntent = activity,
            currentStep = 2,
            stepStatus = "قيد المراجعة"
        )
        // Gain points for editing questionnaire
        _trustScore.value = _trustScore.value + 10
    }

    fun submitNewRescue(title: String, desc: String, location: String) {
        if (title.isBlank() || desc.isBlank() || location.isBlank()) return
        // Generate random offset within Riyadh region
        val randomLat = 24.7136 + (Math.random() - 0.5) * 0.15
        val randomLng = 46.6753 + (Math.random() - 0.5) * 0.15
        
        val petImages = listOf(
            "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600",
            "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600",
            "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600",
            "https://images.unsplash.com/photo-1415369629372-26f2fe60c467?w=600",
            "https://images.unsplash.com/photo-1537151608828-ea2b117b6281?w=600"
        )
        val selectedImg = petImages.random()

        val newIncident = StrayIncident(
            id = "s_${(100..999).random()}",
            title = title,
            description = desc,
            location = location,
            reporter = "أحمد محمد",
            commentsCount = 0,
            likesCount = 0,
            isEmergency = true,
            timestamp = "الآن",
            lat = randomLat,
            lng = randomLng,
            imageUrl = selectedImg,
            reporterAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100",
            repostsCount = 0,
            sharesCount = 0
        )
        _strayIncidents.value = listOf(newIncident) + _strayIncidents.value
        _rescuesCount.value = _rescuesCount.value + 1
        _trustScore.value = _trustScore.value + 15
    }
}
