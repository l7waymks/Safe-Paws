package com.example

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
    val timestamp: String
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
            StrayIncident("s1", "أربعة جراء ضائعة بحاجة لرعاية عاجلة", "تم العثور عليها وحالتها مستقرة بانتظار تبنيها.", "حي الياسمين، الرياض", "أحمد محمد", 41, 240, true, "منذ يومين"),
            StrayIncident("s2", "كلب جولدن - حالة طارئة", "يحتاج لعملية جراحية عاجلة. شارك للمساعدة!", "حي الملز، الرياض", "أحمد محمد", 18, 1200, true, "منذ ٥ ساعات"),
            StrayIncident("s3", "جمال العيون في الطبيعة", "لقطة فنية لإحدى حالات الإنقاذ السابقة.", "حي السليمانية، الرياض", "سارة أحمد", 124, 182, false, "منذ ٣ أيام")
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
        val newIncident = StrayIncident(
            id = "s_${(100..999).random()}",
            title = title,
            description = desc,
            location = location,
            reporter = "أحمد محمد",
            commentsCount = 0,
            likesCount = 0,
            isEmergency = true,
            timestamp = "الآن"
        )
        _strayIncidents.value = listOf(newIncident) + _strayIncidents.value
        _rescuesCount.value = _rescuesCount.value + 1
        _trustScore.value = _trustScore.value + 15
    }
}
