package com.example

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    init {
        try {
            // Check if user is signed in, if not we can sign in anonymously or listen to auth
            if (auth.currentUser == null) {
                auth.signInAnonymously()
                    .addOnSuccessListener { Log.d(TAG, "Firebase Anonymous Auth success: ${it.user?.uid}") }
                    .addOnFailureListener { Log.w(TAG, "Firebase Auth sign-in: ${it.message}") }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase init error: ${e.message}")
        }
    }

    // --- Google Authentication & User Auth ---

    suspend fun signInWithGoogleIdToken(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("تعذر الحصول على بيانات المستخدم من Google")
            Log.d(TAG, "Google Sign-In success: ${user.displayName} (${user.email})")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in with Google ID token: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth.signOut()
            auth.signInAnonymously()
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out: ${e.message}")
        }
    }

    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isGoogleUser(): Boolean {
        val user = auth.currentUser
        return user != null && !user.isAnonymous && user.email?.isNotBlank() == true
    }

    // --- 1. Cloud Storage (Uploading Profile Pictures, Videos, Stories) ---

    suspend fun uploadMedia(uri: Uri, folder: String = "uploads"): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}"
            val ref = storage.reference.child("$folder/$fileName")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading media from URI: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun uploadMediaBytes(bytes: ByteArray, folder: String = "uploads", extension: String = "jpg"): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.$extension"
            val ref = storage.reference.child("$folder/$fileName")
            ref.putBytes(bytes).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading media bytes: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- 2. User Profile Management ---

    suspend fun saveUserProfile(user: UserProfileData): Result<Boolean> {
        return try {
            val userMap = hashMapOf(
                "id" to user.id,
                "name" to user.name,
                "handle" to user.handle,
                "avatarUrl" to user.avatarUrl,
                "bio" to user.bio,
                "category" to user.category,
                "location" to user.location,
                "trustScore" to user.trustScore,
                "rescuesCount" to user.rescuesCount,
                "followersCount" to user.followersCount,
                "followingCount" to user.followingCount,
                "isVerified" to user.isVerified,
                "isShelter" to user.isShelter,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(user.id)
                .set(userMap, SetOptions.merge())
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<Map<String, Any>?> {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            Result.success(doc.data)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- 3. Stories (القصص والستوريات) ---

    suspend fun publishStory(
        authorName: String,
        authorAvatar: String,
        mediaUrl: String,
        isVideo: Boolean = false,
        caption: String = ""
    ): Result<String> {
        return try {
            val storyId = UUID.randomUUID().toString()
            val storyData = hashMapOf(
                "id" to storyId,
                "authorName" to authorName,
                "authorAvatar" to authorAvatar,
                "mediaUrl" to mediaUrl,
                "isVideo" to isVideo,
                "caption" to caption,
                "createdAt" to System.currentTimeMillis(),
                "expiresAt" to (System.currentTimeMillis() + 24 * 60 * 60 * 1000) // 24 hours
            )
            firestore.collection("stories").document(storyId).set(storyData).await()
            Result.success(storyId)
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing story: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchActiveStories(): Result<List<Map<String, Any>>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val querySnapshot = firestore.collection("stories")
                .whereGreaterThan("expiresAt", currentTime)
                .orderBy("expiresAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val stories = querySnapshot.documents.mapNotNull { it.data }
            Result.success(stories)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching stories: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- 4. Community Posts (المنشورات والفيديوهات) ---

    suspend fun publishPost(
        title: String,
        description: String,
        location: String,
        reporter: String,
        imageUrl: String?,
        reporterAvatarUrl: String?,
        isEmergency: Boolean = false,
        handle: String = ""
    ): Result<String> {
        return try {
            val postId = UUID.randomUUID().toString()
            val postData = hashMapOf(
                "id" to postId,
                "title" to title,
                "description" to description,
                "location" to location,
                "reporter" to reporter,
                "reporterAvatarUrl" to (reporterAvatarUrl ?: ""),
                "imageUrl" to (imageUrl ?: ""),
                "likesCount" to 0,
                "commentsCount" to 0,
                "repostsCount" to 0,
                "sharesCount" to 0,
                "handle" to handle,
                "isEmergency" to isEmergency,
                "isVerified" to true,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("community_posts").document(postId).set(postData).await()
            Result.success(postId)
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing community post: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchPosts(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("community_posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            val posts = snapshot.documents.mapNotNull { it.data }
            Result.success(posts)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching community posts: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- 5. Animals for Adoption / Rescue (الحيوانات المعروضة للتبني) ---

    suspend fun publishAnimal(animal: AnimalItem): Result<String> {
        return try {
            val animalData = hashMapOf(
                "id" to animal.id,
                "name" to animal.name,
                "species" to animal.species,
                "breed" to animal.breed,
                "age" to animal.age,
                "gender" to animal.gender,
                "size" to animal.size,
                "description" to animal.description,
                "imageUrl" to animal.imageUrl,
                "likesCount" to animal.likesCount,
                "specialNeeds" to animal.specialNeeds,
                "vaccinated" to animal.vaccinated,
                "neutered" to animal.neutered,
                "compatibility" to animal.compatibility,
                "backstory" to animal.backstory,
                "priceStatus" to animal.priceStatus,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("animals").document(animal.id).set(animalData, SetOptions.merge()).await()
            Result.success(animal.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing animal: ${e.message}", e)
            Result.failure(e)
        }
    }
}
