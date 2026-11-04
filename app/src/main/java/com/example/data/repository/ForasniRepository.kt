package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.JobTender
import com.example.data.model.JordanSeedData
import com.example.data.model.ProviderProfile
import com.example.data.model.ServiceBooking
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

class ForasniRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // 1. Observe Authenticated User Profile
    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, "users/$userId")
                }
                throw error
            }

    // 2. Ensure User Profile Exists on Sign-In
    suspend fun ensureUserProfile(
        displayName: String,
        defaultGovernorate: String = "عمّان",
        defaultNeighborhood: String = "خلدا"
    ): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val existingSnap = docRef.get().await()
            if (existingSnap.exists()) {
                existingSnap.toObject(
                    UserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: UserProfile(id = uid, userId = uid, displayName = displayName)
            } else {
                val safeName = displayName.trim().ifEmpty { "مستخدم فرصني" }.take(80)
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "accountType" to "CLIENT",
                    "governorate" to defaultGovernorate,
                    "neighborhood" to defaultNeighborhood,
                    "phoneNumber" to "+962790000000",
                    "pointsBalance" to 25L,
                    "linkedProviderId" to "prov_user_$uid",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                UserProfile(
                    id = uid,
                    userId = uid,
                    displayName = safeName,
                    accountType = "CLIENT",
                    governorate = defaultGovernorate,
                    neighborhood = defaultNeighborhood,
                    phoneNumber = "+962790000000",
                    pointsBalance = 25L,
                    linkedProviderId = "prov_user_$uid"
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // 3. Update User Role, Location, or Points Wallet
    suspend fun updateUserProfile(
        displayName: String,
        accountType: String,
        governorate: String,
        neighborhood: String,
        phoneNumber: String,
        pointsBalance: Long
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "displayName" to displayName.trim().take(80),
                    "accountType" to accountType,
                    "governorate" to governorate,
                    "neighborhood" to neighborhood,
                    "phoneNumber" to phoneNumber.trim().take(25),
                    "pointsBalance" to pointsBalance.coerceIn(0L, 100000L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // 4. Observe Published Service Providers (Aligned with Query Enforcer: isPublished == true)
    fun observePublishedProviders(): Flow<List<ProviderProfile>> =
        db.collection("providers")
            .whereEqualTo("isPublished", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    ProviderProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, "providers")
                }
                throw error
            }

    // 5. Seed Initial Jordanian Providers and Tenders if Empty
    suspend fun seedInitialMarketplaceIfEmpty(): Result<Unit> = runCatching {
        val uid = requireUserId()
        try {
            val existingProviders = db.collection("providers")
                .whereEqualTo("isPublished", true)
                .limit(2)
                .get()
                .await()

            if (existingProviders.isEmpty) {
                for ((docId, provider) in JordanSeedData.initialProviders(uid)) {
                    val docRef = db.collection("providers").document(docId)
                    docRef.set(providerToCreateMap(provider, uid)).await()
                }
            }

            val existingTenders = db.collection("tenders")
                .whereEqualTo("isPublished", true)
                .limit(2)
                .get()
                .await()

            if (existingTenders.isEmpty) {
                for ((docId, tender) in JordanSeedData.initialTenders(uid)) {
                    val docRef = db.collection("tenders").document(docId)
                    docRef.set(tenderToCreateMap(tender, uid)).await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "providers/tenders_seed")
            throw e
        }
    }

    // 6. Create or Update Craftsman Provider Profile (Including 10-20 Portfolio Items)
    suspend fun saveProviderProfile(
        providerId: String,
        name: String,
        skillCategory: String,
        skillTitle: String,
        governorate: String,
        neighborhood: String,
        bio: String,
        businessPhone: String,
        whatsappNumber: String,
        hourlyRateJod: Long,
        inspectionFeeJod: Long,
        isAvailableNow: Boolean,
        isVerified: Boolean,
        licenseTitle: String,
        portfolioTitlesList: List<String>,
        portfolioTypesList: List<String>,
        pointsBalance: Long
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val safeDocId = providerId.ifBlank { "prov_user_$uid" }
        val docRef = db.collection("providers").document(safeDocId)

        val titlesJoined = portfolioTitlesList.joinToString("|").take(3400)
        val typesJoined = portfolioTypesList.joinToString("|").take(950)
        val count = portfolioTitlesList.size.toLong().coerceIn(1L, 30L)

        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "name" to name.trim().take(90),
                        "skillCategory" to skillCategory,
                        "skillTitle" to skillTitle.trim().take(75),
                        "governorate" to governorate,
                        "neighborhood" to neighborhood,
                        "bio" to bio.trim().take(550),
                        "businessPhone" to businessPhone.trim().take(24),
                        "whatsappNumber" to whatsappNumber.trim().take(24),
                        "hourlyRateJod" to hourlyRateJod.coerceIn(1L, 1000L),
                        "inspectionFeeJod" to inspectionFeeJod.coerceIn(0L, 500L),
                        "isAvailableNow" to isAvailableNow,
                        "isVerified" to isVerified,
                        "licenseTitle" to licenseTitle.trim().take(140),
                        "portfolioCount" to count,
                        "portfolioTitles" to titlesJoined,
                        "portfolioTypes" to typesJoined,
                        "pointsBalance" to pointsBalance.coerceIn(0L, 100000L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val createPayload = mapOf(
                    "ownerId" to uid,
                    "isPublished" to true,
                    "name" to name.trim().take(90),
                    "skillCategory" to skillCategory,
                    "skillTitle" to skillTitle.trim().take(75),
                    "governorate" to governorate,
                    "neighborhood" to neighborhood,
                    "bio" to bio.trim().take(550),
                    "businessPhone" to businessPhone.trim().take(24),
                    "whatsappNumber" to whatsappNumber.trim().take(24),
                    "hourlyRateJod" to hourlyRateJod.coerceIn(1L, 1000L),
                    "inspectionFeeJod" to inspectionFeeJod.coerceIn(0L, 500L),
                    "isAvailableNow" to isAvailableNow,
                    "isVerified" to isVerified,
                    "licenseTitle" to licenseTitle.trim().take(140),
                    "portfolioCount" to count,
                    "portfolioTitles" to titlesJoined,
                    "portfolioTypes" to typesJoined,
                    "pointsBalance" to pointsBalance.coerceIn(0L, 100000L),
                    "leadsCount" to 1L,
                    "ratingAvg" to 5.0,
                    "reviewCount" to 1L,
                    "latestReviewText" to "حرفي مسجل وموثق في منصة فرصني",
                    "distanceKm" to 1.5,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            }
            safeDocId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // 7. Record Direct Call / WhatsApp Click (Free Direct Contact — No Points Deducted on Button Click)
    suspend fun recordDirectLeadContact(provider: ProviderProfile): Result<Unit> = runCatching {
        requireUserId()
        require(provider.id.isNotBlank()) { "Provider ID must not be blank" }
        val docRef = db.collection("providers").document(provider.id)
        val newLeads = provider.leadsCount + 1L
        try {
            docRef.update(
                mapOf(
                    "leadsCount" to newLeads,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // 8. Submit Customer Rating & Verified Review for Provider
    suspend fun submitProviderReview(
        provider: ProviderProfile,
        stars: Int,
        reviewComment: String,
        bookingId: String? = null
    ): Result<Unit> = runCatching {
        requireUserId()
        require(provider.id.isNotBlank()) { "Provider ID must not be blank" }
        val providerRef = db.collection("providers").document(provider.id)
        val oldCount = provider.reviewCount.coerceAtLeast(0L)
        val newCount = oldCount + 1L
        val rawAvg = ((provider.ratingAvg * oldCount) + stars.coerceIn(1, 5)) / newCount.toDouble()
        val roundedAvg = ((rawAvg * 10.0).roundToInt() / 10.0).coerceIn(1.0, 5.0)

        try {
            providerRef.update(
                mapOf(
                    "ratingAvg" to roundedAvg,
                    "reviewCount" to newCount,
                    "latestReviewText" to reviewComment.trim().take(450),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (!bookingId.isNullOrBlank()) {
                val bookingRef = db.collection("bookings").document(bookingId)
                bookingRef.update(
                    mapOf(
                        "isReviewed" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, providerRef.path)
            throw e
        }
    }

    // 9. Observe Published Job Tenders (Aligned with Query Enforcer: isPublished == true)
    fun observePublishedTenders(): Flow<List<JobTender>> =
        db.collection("tenders")
            .whereEqualTo("isPublished", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    JobTender::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, "tenders")
                }
                throw error
            }

    // 10. Create New Customer Job Request / Tender
    suspend fun createJobTender(
        clientName: String,
        clientPhone: String,
        title: String,
        skillCategory: String,
        governorate: String,
        neighborhood: String,
        description: String,
        budgetJod: Long,
        urgency: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val collectionRef = db.collection("tenders")
        val docRef = collectionRef.document()
        val payload = mapOf(
            "clientId" to uid,
            "clientName" to clientName.trim().ifEmpty { "زبون فرصني" }.take(90),
            "clientPhone" to clientPhone.trim().ifEmpty { "+962790000000" }.take(24),
            "isPublished" to true,
            "title" to title.trim().take(110),
            "skillCategory" to skillCategory,
            "governorate" to governorate,
            "neighborhood" to neighborhood,
            "description" to description.trim().take(750),
            "budgetJod" to budgetJod.coerceIn(5L, 50000L),
            "urgency" to urgency,
            "unlockCostPoints" to 2L,
            "status" to "OPEN",
            "bidsCount" to 0L,
            "lowestBidJod" to 0L,
            "latestBidSummary" to "بانتظار أول عرض سعر من الحرفيين",
            "unlockedByCount" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // 11. Unlock Tender Contact Info & Submit Craftsman Price Bid
    suspend fun unlockTenderAndSubmitBid(
        tender: JobTender,
        craftsmanName: String,
        bidPriceJod: Long,
        bidNote: String
    ): Result<Unit> = runCatching {
        requireUserId()
        require(tender.id.isNotBlank()) { "Tender ID must not be blank" }
        val tenderRef = db.collection("tenders").document(tender.id)
        val newBidsCount = tender.bidsCount + 1L
        val newUnlockedCount = tender.unlockedByCount + 1L
        val safeBid = bidPriceJod.coerceIn(1L, 50000L)
        val newLowest = if (tender.lowestBidJod <= 0L || safeBid < tender.lowestBidJod) {
            safeBid
        } else {
            tender.lowestBidJod
        }
        val summary = "${craftsmanName.take(40)}: ${bidNote.take(220)} ($safeBid د.أ)"

        try {
            tenderRef.update(
                mapOf(
                    "bidsCount" to newBidsCount,
                    "lowestBidJod" to newLowest,
                    "latestBidSummary" to summary.take(380),
                    "unlockedByCount" to newUnlockedCount,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, tenderRef.path)
            throw e
        }
    }

    // 12. Observe User Bookings & Escrow Contracts (Aligned with userId == uid)
    fun observeUserBookings(userId: String): Flow<List<ServiceBooking>> =
        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    ServiceBooking::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, "bookings")
                }
                throw error
            }

    // 13. Get User Bookings Once (For verification and rule tests)
    suspend fun getUserBookings(): Result<List<ServiceBooking>> = runCatching {
        val uid = requireUserId()
        try {
            val snap = db.collection("bookings")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            snap.toObjects(
                ServiceBooking::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "bookings")
            throw e
        }
    }

    // 14. Fetch Single Booking by ID
    suspend fun getBookingById(bookingId: String): Result<ServiceBooking?> = runCatching {
        requireUserId()
        val docRef = db.collection("bookings").document(bookingId)
        try {
            val snap = docRef.get().await()
            snap.toObject(
                ServiceBooking::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    // 15. Create Appointment Booking with Optional Escrow Protection
    suspend fun createBooking(
        providerId: String,
        providerName: String,
        providerSkill: String,
        providerPhone: String,
        serviceTitle: String,
        governorate: String,
        neighborhood: String,
        scheduledDate: String,
        scheduledTime: String,
        escrowAmountJod: Long,
        useEscrow: Boolean
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("bookings").document()
        val safeEscrow = if (useEscrow) escrowAmountJod.coerceIn(5L, 50000L) else 0L
        val commission = if (useEscrow) (safeEscrow * 5L / 100L).coerceAtLeast(1L) else 0L
        val escrowStatus = if (useEscrow && safeEscrow > 0L) "HELD_IN_ESCROW" else "NONE"

        val payload = mapOf(
            "userId" to uid,
            "providerId" to providerId,
            "providerName" to providerName.trim().take(90),
            "providerSkill" to providerSkill.trim().take(75),
            "providerPhone" to providerPhone.trim().take(24),
            "serviceTitle" to serviceTitle.trim().take(190),
            "governorate" to governorate,
            "neighborhood" to neighborhood,
            "scheduledDate" to scheduledDate.trim().take(35),
            "scheduledTime" to scheduledTime.trim().take(35),
            "trackingStage" to "CONFIRMED",
            "trackingEtaMinutes" to 18L,
            "escrowAmountJod" to safeEscrow,
            "escrowCommissionJod" to commission,
            "escrowStatus" to escrowStatus,
            "isReviewed" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // 16. Advance Live Route Tracking & Release Escrow Funds on Completion
    suspend fun updateBookingStageAndEscrow(
        bookingId: String,
        newStage: String,
        newEtaMinutes: Long,
        newEscrowStatus: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("bookings").document(bookingId)
        try {
            docRef.update(
                mapOf(
                    "trackingStage" to newStage,
                    "trackingEtaMinutes" to newEtaMinutes.coerceIn(0L, 240L),
                    "escrowStatus" to newEscrowStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    private fun providerToCreateMap(provider: ProviderProfile, ownerUid: String): Map<String, Any> =
        mapOf(
            "ownerId" to ownerUid,
            "isPublished" to true,
            "name" to provider.name,
            "skillCategory" to provider.skillCategory,
            "skillTitle" to provider.skillTitle,
            "governorate" to provider.governorate,
            "neighborhood" to provider.neighborhood,
            "bio" to provider.bio,
            "businessPhone" to provider.businessPhone,
            "whatsappNumber" to provider.whatsappNumber,
            "hourlyRateJod" to provider.hourlyRateJod,
            "inspectionFeeJod" to provider.inspectionFeeJod,
            "isAvailableNow" to provider.isAvailableNow,
            "isVerified" to provider.isVerified,
            "licenseTitle" to provider.licenseTitle,
            "portfolioCount" to provider.portfolioCount,
            "portfolioTitles" to provider.portfolioTitles,
            "portfolioTypes" to provider.portfolioTypes,
            "pointsBalance" to provider.pointsBalance,
            "leadsCount" to provider.leadsCount,
            "ratingAvg" to provider.ratingAvg,
            "reviewCount" to provider.reviewCount,
            "latestReviewText" to provider.latestReviewText,
            "distanceKm" to provider.distanceKm,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

    private fun tenderToCreateMap(tender: JobTender, clientUid: String): Map<String, Any> =
        mapOf(
            "clientId" to clientUid,
            "clientName" to tender.clientName,
            "clientPhone" to tender.clientPhone,
            "isPublished" to true,
            "title" to tender.title,
            "skillCategory" to tender.skillCategory,
            "governorate" to tender.governorate,
            "neighborhood" to tender.neighborhood,
            "description" to tender.description,
            "budgetJod" to tender.budgetJod,
            "urgency" to tender.urgency,
            "unlockCostPoints" to tender.unlockCostPoints,
            "status" to tender.status,
            "bidsCount" to tender.bidsCount,
            "lowestBidJod" to tender.lowestBidJod,
            "latestBidSummary" to tender.latestBidSummary,
            "unlockedByCount" to tender.unlockedByCount,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
}
