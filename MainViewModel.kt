package com.example

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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

data class CommunityComment(
    val id: String,
    val author: String,
    val handle: String,
    val avatarUrl: String? = null,
    val text: String,
    val timestamp: String,
    val likesCount: Int = 0,
    val isVerified: Boolean = false,
    val videoTimestamp: String? = null,
    val tag: String? = null,
    val isLikedByMe: Boolean = false,
    val badge: String? = null
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
    val sharesCount: Int = 0,
    val handle: String = "",
    val isVerified: Boolean = false,
    val isShelter: Boolean = false,
    val viewsCount: String = "1.5K",
    val isLikedByMe: Boolean = false,
    val isRepostedByMe: Boolean = false,
    val isBookmarkedByMe: Boolean = false,
    val comments: List<CommunityComment> = emptyList()
)

data class ProfilePostItem(
    val id: String,
    val title: String,
    val category: String,
    val type: String, // "post", "rescue", "video", "saved"
    val dateOrLikes: String,
    val details: String,
    val imageUrl: String,
    val location: String = "الرياض، المملكة العربية السعودية",
    val likesCount: Int = 124,
    val commentsCount: Int = 18,
    val reporter: String = "أحمد محمد"
)

data class UserProfileData(
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val bio: String = "🐾 منقذ وناشط في مجال الرفق بالحيوان وإنقاذ الحالات الحرجة 🌿\nعضو معتمد في شبكة أمان الحيوانات الأليفة 💚",
    val category: String = "متطوع ومنقذ معتمد | حماية وتأهيل 🐾",
    val location: String = "الرياض، المملكة العربية السعودية",
    val trustScore: Int = 94,
    val rescuesCount: Int = 18,
    val followersCount: Int = 1240,
    val followingCount: Int = 195,
    val isVerified: Boolean = true,
    val isShelter: Boolean = false,
    val isFollowing: Boolean = false,
    val posts: List<ProfilePostItem> = emptyList(),
    val directMessages: List<ChatMessage> = emptyList()
)

class MainViewModel : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.Home)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedUserProfile = MutableStateFlow<UserProfileData?>(null)
    val selectedUserProfile: StateFlow<UserProfileData?> = _selectedUserProfile.asStateFlow()

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

    // Video Playback State for Home Screen Reels
    private val _isVideoPlaying = MutableStateFlow(true)
    val isVideoPlaying: StateFlow<Boolean> = _isVideoPlaying.asStateFlow()

    private val _videoProgress = MutableStateFlow(0f)
    val videoProgress: StateFlow<Float> = _videoProgress.asStateFlow()

    private val _videoDurationSeconds = MutableStateFlow(30)
    val videoDurationSeconds: StateFlow<Int> = _videoDurationSeconds.asStateFlow()

    private val _videoCurrentSeconds = MutableStateFlow(0f)
    val videoCurrentSeconds: StateFlow<Float> = _videoCurrentSeconds.asStateFlow()

    private val _isScrubbingVideo = MutableStateFlow(false)
    val isScrubbingVideo: StateFlow<Boolean> = _isScrubbingVideo.asStateFlow()

    init {
        // Continuous, smooth video playback clock
        viewModelScope.launch {
            val stepMs = 50L
            while (isActive) {
                delay(stepMs)
                if (_isVideoPlaying.value && !_isScrubbingVideo.value) {
                    val totalSec = _videoDurationSeconds.value.coerceAtLeast(1)
                    val totalMs = totalSec * 1000f
                    var curMs = _videoProgress.value * totalMs + stepMs
                    if (curMs >= totalMs) {
                        curMs = 0f
                    }
                    val newProgress = (curMs / totalMs).coerceIn(0f, 1f)
                    val newCurrentSec = curMs / 1000f
                    _videoProgress.value = newProgress
                    _videoCurrentSeconds.value = newCurrentSec
                }
            }
        }
    }

    fun updateVideoProgress(progress: Float, currentSec: Float, totalSec: Int) {
        if (!_isScrubbingVideo.value) {
            _videoProgress.value = progress.coerceIn(0f, 1f)
            _videoCurrentSeconds.value = currentSec
            _videoDurationSeconds.value = totalSec
        }
    }

    fun setScrubbing(isScrubbing: Boolean) {
        _isScrubbingVideo.value = isScrubbing
    }

    fun seekVideoProgress(progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        _videoProgress.value = clamped
        _videoCurrentSeconds.value = clamped * _videoDurationSeconds.value
    }

    fun toggleVideoPlayback() {
        _isVideoPlaying.value = !_isVideoPlaying.value
    }

    fun setVideoPlaying(playing: Boolean) {
        _isVideoPlaying.value = playing
    }

    // Active User Context
    private val _userName = MutableStateFlow("أحمد محمد 🐾")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userHandle = MutableStateFlow("ahmed_rescues")
    val userHandle: StateFlow<String> = _userHandle.asStateFlow()

    private val _userCategory = MutableStateFlow("منقذ ومتطوع معتمد | رعاية وإنقاذ 🌿")
    val userCategory: StateFlow<String> = _userCategory.asStateFlow()

    private val _userBio = MutableStateFlow("🐾 شغوف بإنقاذ ورعاية الحيوانات الأليفة وتوفير بيئة آمنة لها بالرياض 🌧️\nمؤسس مبادرة 'طعام الشتاء' 🥣 | معاً لبيئة أكثر رحمة وأماناً 💚")
    val userBio: StateFlow<String> = _userBio.asStateFlow()

    private val _userLink = MutableStateFlow("safepaws.app/ahmed")
    val userLink: StateFlow<String> = _userLink.asStateFlow()

    private val _userLocation = MutableStateFlow("الرياض، المملكة العربية السعودية")
    val userLocation: StateFlow<String> = _userLocation.asStateFlow()

    private val _userPhone = MutableStateFlow("+966 50 123 4567")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userFollowersCount = MutableStateFlow(1420)
    val userFollowersCount: StateFlow<Int> = _userFollowersCount.asStateFlow()

    private val _userFollowingCount = MutableStateFlow(286)
    val userFollowingCount: StateFlow<Int> = _userFollowingCount.asStateFlow()

    private val _trustScore = MutableStateFlow(85)
    val trustScore: StateFlow<Int> = _trustScore.asStateFlow()

    private val _rescuesCount = MutableStateFlow(42)
    val rescuesCount: StateFlow<Int> = _rescuesCount.asStateFlow()

    private val _profileImageUrl = MutableStateFlow("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400")
    val profileImageUrl: StateFlow<String> = _profileImageUrl.asStateFlow()

    // App Authentication State
    private val _isUserLoggedIn = MutableStateFlow(true)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow("ahmed@safepaws.app")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    fun login(identifier: String, pass: String): Boolean {
        if (identifier.isNotBlank()) {
            if (identifier.contains("@")) {
                _userEmail.value = identifier
                _userHandle.value = identifier.substringBefore("@")
                _userName.value = identifier.substringBefore("@")
            } else {
                _userName.value = identifier
                _userHandle.value = identifier.removePrefix("@").replace(" ", "_")
                _userEmail.value = "${_userHandle.value}@safepaws.app"
            }
        }
        _isUserLoggedIn.value = true
        syncProfileToFirebase()
        return true
    }

    fun register(
        name: String,
        handle: String,
        email: String,
        role: String
    ): Boolean {
        if (name.isNotBlank()) _userName.value = name
        if (handle.isNotBlank()) _userHandle.value = handle.removePrefix("@").replace(" ", "_")
        if (email.isNotBlank()) _userEmail.value = email
        if (role.isNotBlank()) _userCategory.value = role
        _isUserLoggedIn.value = true
        syncProfileToFirebase()
        return true
    }

    fun loginAsGuest() {
        _userName.value = "زائر بيت الأمان 🐾"
        _userHandle.value = "guest_user"
        _userCategory.value = "محب للحيوانات الأليفة 🌿"
        _userEmail.value = "guest@safepaws.app"
        _isUserLoggedIn.value = true
    }

    fun logout() {
        _isUserLoggedIn.value = false
    }

    fun updateProfileImageUrl(url: String) {
        _profileImageUrl.value = url
        syncProfileToFirebase()
    }

    fun updateProfile(
        name: String,
        handle: String,
        bio: String,
        link: String,
        location: String,
        phone: String
    ) {
        _userName.value = name
        _userHandle.value = handle.removePrefix("@")
        _userBio.value = bio
        _userLink.value = link
        _userLocation.value = location
        _userPhone.value = phone
        syncProfileToFirebase()
    }

    private fun syncProfileToFirebase() {
        viewModelScope.launch {
            try {
                val profileData = UserProfileData(
                    id = _userHandle.value.ifBlank { "my_user_id" },
                    name = _userName.value,
                    handle = _userHandle.value,
                    avatarUrl = _profileImageUrl.value,
                    bio = _userBio.value,
                    location = _userLocation.value,
                    trustScore = _trustScore.value,
                    rescuesCount = _rescuesCount.value,
                    followersCount = _userFollowersCount.value,
                    followingCount = _userFollowingCount.value,
                    isVerified = true
                )
                FirebaseManager.saveUserProfile(profileData)
            } catch (e: Exception) {
                Log.w("MainViewModel", "Firebase profile sync notice: ${e.message}")
            }
        }
    }

    fun isCurrentLoggedInUser(nameOrHandle: String): Boolean {
        val clean = nameOrHandle.removePrefix("@").trim()
        return clean.equals(_userHandle.value, ignoreCase = true) ||
               clean.equals(_userName.value, ignoreCase = true) ||
               clean.contains("أحمد محمد")
    }

    // Adoption tracker workflow
    private val _adoptionPipeline = MutableStateFlow(AdoptionPipeline())
    val adoptionPipeline: StateFlow<AdoptionPipeline> = _adoptionPipeline.asStateFlow()

    // Chat Expert Bot
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("1", "أهلاً بك في البث الاستشاري لـ بيت الأمان 🐾 أنا المستشار البيطري. كيف يمكنني مساعدتك في رعاية وتغذية وصحة أليفك أو الإجابة على استفساراتك الطبية؟", false)
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // UI state for showing the expert chat screen globally
    val showExpertChat = MutableStateFlow(false)

    // UI state for showing dedicated search screen
    val showSearchScreen = MutableStateFlow(false)
    val searchInitialQuery = MutableStateFlow("")

    private val _recentSearches = MutableStateFlow<List<String>>(
        listOf("قطط للتبني", "جرو صغير", "عيادات الرياض", "طعام الشتاء", "إنقاذ حيوانات")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    fun openSearch(initialQuery: String = "") {
        searchInitialQuery.value = initialQuery
        showSearchScreen.value = true
    }

    fun closeSearch() {
        showSearchScreen.value = false
        searchInitialQuery.value = ""
    }

    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            val current = _recentSearches.value.toMutableList()
            current.remove(trimmed)
            current.add(0, trimmed)
            _recentSearches.value = current.take(10)
        }
    }

    fun removeRecentSearch(query: String) {
        val current = _recentSearches.value.toMutableList()
        current.remove(query)
        _recentSearches.value = current
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    // PetMatch Onboarding State
    private val _onboardingQuizCompleted = MutableStateFlow(false)
    val onboardingQuizCompleted: StateFlow<Boolean> = _onboardingQuizCompleted.asStateFlow()

    private val _quizPercentage = MutableStateFlow<Int?>(null)
    val quizPercentage: StateFlow<Int?> = _quizPercentage.asStateFlow()

    private val _quizAnalysisReport = MutableStateFlow("")
    val quizAnalysisReport: StateFlow<String> = _quizAnalysisReport.asStateFlow()

    private val _isQuizAnalyzing = MutableStateFlow(false)
    val isQuizAnalyzing: StateFlow<Boolean> = _isQuizAnalyzing.asStateFlow()

    // Stray Map Pins & Community Tweets State
    private val _strayIncidents = MutableStateFlow(
        listOf(
            StrayIncident(
                id = "s0", 
                title = "🚨 عاجل: إنقاذ قطة محاصرة في نفق الملقا", 
                description = "بفضل الله ثم استجابة متطوعي #بيت_الأمان السريعة تم إنقاذ هذه القطة الصغيرة بعد أن كانت عالقة لأكثر من ١٢ ساعة. تم فحصها بيطرياً وهي الآن بأمان كامل وبصحة جيدة! 💚🐾 #إنقاذ_حيوانات #الرياض", 
                location = "حي الملقا، الرياض", 
                reporter = "ملجأ الرياض للرعاية", 
                handle = "riyadh_shelter",
                isVerified = true,
                isShelter = true,
                commentsCount = 18, 
                likesCount = 342, 
                isEmergency = false, 
                timestamp = "منذ ٢ س", 
                lat = 24.8036, 
                lng = 46.6253,
                imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600",
                reporterAvatarUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=100",
                repostsCount = 45, 
                sharesCount = 28,
                viewsCount = "4.2K",
                comments = listOf(
                    CommunityComment("c1", "سارة المنصور", "sara_mansoor", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100", "جزاكم الله كل خير على سرعة التدخل! 💚", "منذ ساعة", 12, true, videoTimestamp = "00:08", tag = "دعم ومحبة ❤️", badge = "🏆 متطوع متميز"),
                    CommunityComment("c2", "د. خالد البيطري", "dr_khaled_vet", "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=100", "مستعدون لتقديم التطعيمات والفحص الدوري مجاناً للقطة في عيادتنا.", "منذ ٤٥ د", 24, true, videoTimestamp = "00:15", tag = "رأي واستشارة 🩺", badge = "🩺 طبيب معتمد")
                )
            ),
            StrayIncident(
                id = "s1", 
                title = "🐾 أربعة جراء لطيفة بحاجة لرعاية واستضافة", 
                description = "تم العثور عليها في الشارع بجانب الحديقة العامة وحالتها مستقرة وصحتها ممتازة، بانتظار عائلة محبة لتبنيها أو متطوع للاعتناء بها 🐕🌧️ #تبنى_لا_تشتري #جراء", 
                location = "حي الياسمين، الرياض", 
                reporter = "أحمد محمد", 
                handle = "ahmed_rescues",
                isVerified = true,
                commentsCount = 24, 
                likesCount = 158, 
                isEmergency = false, 
                timestamp = "منذ ٥ س", 
                lat = 24.8136, 
                lng = 46.6853,
                imageUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600",
                reporterAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100",
                repostsCount = 14, 
                sharesCount = 19,
                viewsCount = "2.8K",
                comments = listOf(
                    CommunityComment("c3", "نورة العتيبي", "noura_al", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100", "أستطيع استضافة جروين مؤقتاً في فناء منزلي، تواصلوا معي!", "منذ ٣ س", 18, false, videoTimestamp = "00:04", tag = "عرض استضافة 🏡", badge = "🐾 كافل أليف")
                )
            ),
            StrayIncident(
                id = "s2", 
                title = "🐕 كلب جولدن ودود يبحث عن صاحبه", 
                description = "تم رصد هذا الكلب اللطيف يتجول بمفرده ويبدو أليفاً ومدرباً. نساعد في البحث عن صاحبه أو توفير مأوى آمن له 🐾 #كلاب_أليفة #الرياض_الملز", 
                location = "حي الملز، الرياض", 
                reporter = "د. فهد الشمري", 
                handle = "dr_fahad_vet",
                isVerified = true,
                commentsCount = 15, 
                likesCount = 184, 
                isEmergency = false, 
                timestamp = "منذ ٧ س", 
                lat = 24.6636, 
                lng = 46.7253,
                imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600",
                reporterAvatarUrl = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=100",
                repostsCount = 38, 
                sharesCount = 54,
                viewsCount = "5.1K"
            ),
            StrayIncident(
                id = "s3", 
                title = "✨ قصة نجاح: تعافي القط سكر بعد شهرين من العناية", 
                description = "تتذكرون القط 'سكر' الذي أنقذناه من تحت أحد الجسور؟ اليوم أتم تطعيماته بالكامل واكتمل وزنه المثالي وأصبح مرحاً للغاية! شكراً لكل من ساهم ودعم 🌟😻 #قصص_نجاح #بيت_الأمان", 
                location = "حي السليمانية، الرياض", 
                reporter = "سارة أحمد", 
                handle = "sara_ahmed",
                isVerified = true,
                commentsCount = 42, 
                likesCount = 520, 
                isEmergency = false, 
                timestamp = "منذ يومين", 
                lat = 24.7036, 
                lng = 46.6953,
                imageUrl = "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=600",
                reporterAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100",
                repostsCount = 62, 
                sharesCount = 112,
                viewsCount = "8.9K"
            ),
            StrayIncident(
                id = "s4", 
                title = "💡 نصيحة شتوية لأصحاب الحيوانات الأليفة",
                description = "مع انخفاض درجات الحرارة ليلاً، يرجى فحص محرك السيارة قبل التشغيل (طرق خفيف على الكبوت) فقد تلجأ القطط للمحرك طلباً للدفء 🚗🐈 #توعية #رعاية_الحيوان",
                location = "المملكة العربية السعودية",
                reporter = "فريق بيت الأمان الرسمي",
                handle = "SafePawsApp",
                isVerified = true,
                isShelter = true,
                commentsCount = 56,
                likesCount = 890,
                isEmergency = false,
                timestamp = "منذ ٣ أيام",
                lat = 24.7136,
                lng = 46.6753,
                imageUrl = "https://images.unsplash.com/photo-1574158622682-e40e69881006?w=600",
                reporterAvatarUrl = "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=100",
                repostsCount = 180,
                sharesCount = 245,
                viewsCount = "14.5K"
            ),
            StrayIncident(
                id = "s5", 
                title = "📖 مقال: الدليل الشامل للإسعافات الأولية للحيوانات الأليفة",
                description = "في حالات الإنقاذ والرعاية، التصرف السليم يضمن سلامة الأليف! إليك أهم 5 قواعد: 1. الحفاظ على الهدوء وعدم إفزاع الحيوان 2. تنظيف الجروح بمحلول ملحي معقم 3. تجنب إعطاء أدوية بشرية نهائياً 4. تدفئة الحيوان ببطانية خفيفة 5. زيارة أقرب عيادة بيطرية مسجلة في التطبيق 🏥🐾 #دليل_الإنقاذ #صحة_الحيوان #مقالات",
                location = "عيادة الأمان البيطرية",
                reporter = "د. نادية العلي",
                handle = "dr_nadia_vet",
                isVerified = true,
                isShelter = true,
                commentsCount = 38,
                likesCount = 612,
                isEmergency = false,
                timestamp = "منذ ٤ أيام",
                lat = 24.7236,
                lng = 46.6853,
                imageUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=600",
                reporterAvatarUrl = "https://images.unsplash.com/photo-1594824813620-30f14652c6f1?w=100",
                repostsCount = 95,
                sharesCount = 160,
                viewsCount = "9.8K",
                comments = listOf(
                    CommunityComment("c4", "فهد التميمي", "fahad_t", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100", "معلومات قيمة جداً ومفيدة لكل مربي حيوان أليف، شكراً دكتورة!", "منذ يومين", 19, true, videoTimestamp = "00:20", tag = "استشارة طبية 🩺", badge = "⭐ مساهم نشط")
                )
            ),
            StrayIncident(
                id = "s6",
                title = "🥗 مقال: التغذية السليمة للقطط والكلاب بعد التبني",
                description = "عند تبني أليف جديد، يحتاج جهازه الهضمي إلى فترة تكيف تدريجية لمدة 7-10 أيام. ينصح بخلط الطعام القديم بالجديد بنسب تصاعدية، مع توفير مياه نقية متجددة وتجنب الأطعمة الممنوعة كالشوكولاتة والبصل والعنب 🥦🥣 #تغذية_الحيوان #تبنى_بمسؤولية #مقالات_تثقيفية",
                location = "المركز الوطني لرعاية الحيوان",
                reporter = "أخصائي التغذية ريان",
                handle = "rayan_nutrition",
                isVerified = true,
                isShelter = false,
                commentsCount = 29,
                likesCount = 478,
                isEmergency = false,
                timestamp = "منذ ٥ أيام",
                lat = 24.7336,
                lng = 46.6953,
                imageUrl = "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=600",
                reporterAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100",
                repostsCount = 72,
                sharesCount = 104,
                viewsCount = "7.3K"
            )
        )
    )
    val strayIncidents: StateFlow<List<StrayIncident>> = _strayIncidents.asStateFlow()

    init {
        fetchAnimals()
    }

    fun selectTab(tab: AppTab) {
        _selectedUserProfile.value = null
        _selectedAnimal.value = null
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
                    // Also sync with Firebase
                    FirebaseManager.publishAnimal(newAnimal)
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

    fun clearSelectedAnimal() {
        selectAnimal(null)
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

    fun toggleLikeIncident(incidentId: String) {
        _strayIncidents.value = _strayIncidents.value.map { incident ->
            if (incident.id == incidentId) {
                val newLiked = !incident.isLikedByMe
                val newCount = if (newLiked) incident.likesCount + 1 else (incident.likesCount - 1).coerceAtLeast(0)
                incident.copy(
                    isLikedByMe = newLiked,
                    likesCount = newCount
                )
            } else incident
        }
    }

    fun toggleRepostIncident(incidentId: String) {
        _strayIncidents.value = _strayIncidents.value.map { incident ->
            if (incident.id == incidentId) {
                val newReposted = !incident.isRepostedByMe
                val newCount = if (newReposted) incident.repostsCount + 1 else (incident.repostsCount - 1).coerceAtLeast(0)
                incident.copy(
                    isRepostedByMe = newReposted,
                    repostsCount = newCount
                )
            } else incident
        }
    }

    fun toggleBookmarkIncident(incidentId: String) {
        _strayIncidents.value = _strayIncidents.value.map { incident ->
            if (incident.id == incidentId) {
                incident.copy(isBookmarkedByMe = !incident.isBookmarkedByMe)
            } else incident
        }
    }

    fun addIncidentReply(
        incidentId: String, 
        replyText: String, 
        tag: String? = null, 
        videoTimestamp: String? = null, 
        badge: String? = null
    ) {
        if (replyText.isBlank()) return
        val newReply = CommunityComment(
            id = "c_${(1000..9999).random()}",
            author = _userName.value.ifBlank { "أحمد محمد" },
            handle = _userHandle.value.ifBlank { "ahmed_rescues" },
            avatarUrl = _profileImageUrl.value.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100" },
            text = replyText,
            timestamp = "الآن",
            likesCount = 0,
            isVerified = true,
            videoTimestamp = videoTimestamp,
            tag = tag,
            badge = badge ?: "🐾 عضو مجتمعي"
        )
        _strayIncidents.value = _strayIncidents.value.map { incident ->
            if (incident.id == incidentId) {
                incident.copy(
                    comments = incident.comments + newReply,
                    commentsCount = incident.commentsCount + 1
                )
            } else incident
        }
        _trustScore.value = _trustScore.value + 5
    }

    fun toggleLikeComment(incidentId: String, commentId: String) {
        _strayIncidents.value = _strayIncidents.value.map { incident ->
            if (incident.id == incidentId) {
                val updatedComments = incident.comments.map { comment ->
                    if (comment.id == commentId) {
                        val newLiked = !comment.isLikedByMe
                        val newCount = if (newLiked) comment.likesCount + 1 else (comment.likesCount - 1).coerceAtLeast(0)
                        comment.copy(isLikedByMe = newLiked, likesCount = newCount)
                    } else comment
                }
                incident.copy(comments = updatedComments)
            } else incident
        }
    }

    fun submitNewRescue(
        title: String, 
        desc: String, 
        location: String,
        isEmergency: Boolean = false,
        customImageUrl: String? = null
    ) {
        if (desc.isBlank()) return
        val effectiveTitle = if (title.isBlank()) "🐾 مشاركة مجتمعية جديدة" else title

        val randomLat = 24.7136 + (Math.random() - 0.5) * 0.15
        val randomLng = 46.6753 + (Math.random() - 0.5) * 0.15
        
        val petImages = listOf(
            "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600",
            "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600",
            "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600",
            "https://images.unsplash.com/photo-1415369629372-26f2fe60c467?w=600",
            "https://images.unsplash.com/photo-1537151608828-ea2b117b6281?w=600"
        )
        val selectedImg = customImageUrl ?: if (Math.random() > 0.3) petImages.random() else null

        val newIncident = StrayIncident(
            id = "s_${(100..999).random()}",
            title = effectiveTitle,
            description = desc,
            location = if (location.isBlank()) "الرياض، المملكة العربية السعودية" else location,
            reporter = "أحمد محمد",
            handle = "ahmed_rescues",
            isVerified = true,
            commentsCount = 0,
            likesCount = 0,
            isEmergency = false,
            timestamp = "الآن",
            lat = randomLat,
            lng = randomLng,
            imageUrl = selectedImg,
            reporterAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100",
            repostsCount = 0,
            sharesCount = 0,
            viewsCount = "1",
            comments = emptyList()
        )
        _strayIncidents.value = listOf(newIncident) + _strayIncidents.value
        _rescuesCount.value = _rescuesCount.value + 1
        _trustScore.value = _trustScore.value + 15

        // Sync to Firebase
        viewModelScope.launch {
            try {
                FirebaseManager.publishPost(
                    title = newIncident.title,
                    description = newIncident.description,
                    location = newIncident.location,
                    reporter = newIncident.reporter,
                    imageUrl = newIncident.imageUrl,
                    reporterAvatarUrl = newIncident.reporterAvatarUrl,
                    isEmergency = newIncident.isEmergency,
                    handle = newIncident.handle
                )
            } catch (e: Exception) {
                Log.w("MainViewModel", "Firebase post sync: ${e.message}")
            }
        }
    }

    fun openUserProfile(user: UserProfileData) {
        _selectedUserProfile.value = user
    }

    fun openUserProfileByInfo(
        name: String,
        handle: String = "",
        avatarUrl: String? = null,
        isVerified: Boolean = false,
        isShelter: Boolean = false,
        location: String = "الرياض، المملكة العربية السعودية"
    ) {
        val cleanHandle = if (handle.isNotBlank()) handle.removePrefix("@") else name.replace(" ", "_").lowercase()
        
        // If clicking on own profile, navigate directly to Profile tab
        if (!isShelter && isCurrentLoggedInUser(cleanHandle) || isCurrentLoggedInUser(name)) {
            _selectedUserProfile.value = null
            _currentTab.value = AppTab.Profile
            return
        }

        // Find existing posts from stray incidents
        val userPosts = _strayIncidents.value.filter { 
            it.reporter.equals(name, ignoreCase = true) || it.handle.equals(cleanHandle, ignoreCase = true) 
        }.map { inc ->
            ProfilePostItem(
                id = inc.id,
                title = inc.title,
                category = "إنقاذ وتأهيل 🐾",
                type = "rescue",
                dateOrLikes = inc.timestamp,
                details = inc.description,
                imageUrl = inc.imageUrl ?: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600",
                location = inc.location,
                likesCount = inc.likesCount,
                commentsCount = inc.commentsCount,
                reporter = inc.reporter
            )
        }.ifEmpty {
            listOf(
                ProfilePostItem(
                    id = "p_${cleanHandle}_1",
                    title = "متابعة ورعاية مستمرة للأليف 🐾",
                    category = "إنقاذ وتأهيل 🐾",
                    type = "rescue",
                    dateOrLikes = "منذ يومين",
                    details = "تم إجراء الفحص الطبي الشامل وتقديم التطعيمات اللازمة، وصار الأليف بصحة وسعادة بالغة ومستعد للمنزل الجديد.",
                    imageUrl = "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=600",
                    location = location,
                    likesCount = 184,
                    commentsCount = 23,
                    reporter = name
                ),
                ProfilePostItem(
                    id = "p_${cleanHandle}_2",
                    title = "لحظات اللعب والمرح في الحديقة 🐱",
                    category = "حياة يومية 🌿",
                    type = "post",
                    dateOrLikes = "منذ ٥ أيام",
                    details = "الأليف يحتاج إلى الحب والاهتمام اليومي واللعب، شكراً لكل من ساهم في دعمه ورعايته.",
                    imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600",
                    location = location,
                    likesCount = 295,
                    commentsCount = 41,
                    reporter = name
                ),
                ProfilePostItem(
                    id = "p_${cleanHandle}_3",
                    title = "توزيع الوجبات ومتابعة الحالة الصحية 🥣",
                    category = "مبادرة مجتمعية 💚",
                    type = "video",
                    dateOrLikes = "منذ أسبوع",
                    details = "فيديو توثيقي لعملية الإنقاذ والإسعاف الأولي وتوفير المأوى الآمن.",
                    imageUrl = "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=600",
                    location = location,
                    likesCount = 412,
                    commentsCount = 52,
                    reporter = name
                )
            )
        }

        val effectiveAvatar = avatarUrl?.takeIf { it.isNotBlank() } ?: when {
            isShelter -> "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=400"
            name.contains("سارة") || name.contains("نورة") || name.contains("منى") -> "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400"
            name.contains("خالد") || name.contains("طبيب") || name.contains("دكتور") -> "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400"
            else -> "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400"
        }

        _selectedUserProfile.value = UserProfileData(
            id = cleanHandle,
            name = name,
            handle = cleanHandle,
            avatarUrl = effectiveAvatar,
            bio = if (isShelter) "🏥 ملجأ ومركز رعاية وإيواء رسمي للحيوانات الأليفة.\nنستقبل الحالات ونوفر الفحص والرعاية الصحية وخدمات التبني المجانية 🐾💚"
                  else "🐾 متطوع شغوف ومحب للحيوانات الأليفة بالرياض 🌧️\nعضو نشط في مبادرة الإنقاذ المجتمعي | معاً لبيئة أكثر رحمة وأماناً 🌿",
            category = if (isShelter) "ملجأ ومركز رعاية معتمد 🏥" else if (name.contains("طبيب") || name.contains("دكتور")) "طبيب بيطري واستشاري رعاية 🩺" else "منقذ ومتطوع نشط 🐾",
            location = location,
            trustScore = if (isShelter) 99 else if (isVerified) 96 else 91,
            rescuesCount = if (isShelter) 84 else 19,
            followersCount = if (isShelter) 3420 else 890,
            followingCount = if (isShelter) 45 else 230,
            isVerified = isVerified || isShelter,
            isShelter = isShelter,
            isFollowing = false,
            posts = userPosts,
            directMessages = listOf(
                ChatMessage(
                    id = "m1",
                    text = "مرحباً بك! أنا $name. يسعدني التواصل معك وتقديم المساعدة في رعاية وإنقاذ الحيوانات الأليفة 🐾",
                    isUser = false,
                    timestamp = "الآن"
                )
            )
        )
    }

    fun closeUserProfile() {
        _selectedUserProfile.value = null
    }

    fun toggleFollowSelectedUser() {
        val current = _selectedUserProfile.value ?: return
        val newFollowState = !current.isFollowing
        val newFollowersCount = if (newFollowState) current.followersCount + 1 else (current.followersCount - 1).coerceAtLeast(0)
        _selectedUserProfile.value = current.copy(
            isFollowing = newFollowState,
            followersCount = newFollowersCount
        )
    }

    fun sendDirectMessageToSelectedUser(messageText: String) {
        val current = _selectedUserProfile.value ?: return
        if (messageText.isBlank()) return
        val userMsg = ChatMessage(
            id = "m_${System.currentTimeMillis()}",
            text = messageText,
            isUser = true,
            timestamp = "الآن"
        )
        val replyMsg = ChatMessage(
            id = "m_rep_${System.currentTimeMillis()}",
            text = "شكراً لرسالتك ولتواصلك معي 💚! سأقوم بالرد عليك في أقرب فرصة بخصوص: '$messageText'. بارك الله فيك على اهتمامك بالأليف 🐾",
            isUser = false,
            timestamp = "الآن"
        )
        _selectedUserProfile.value = current.copy(
            directMessages = current.directMessages + userMsg + replyMsg
        )
    }
}
