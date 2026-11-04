package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.B2BPartnerOffer
import com.example.data.model.JORDAN_GOVERNORATES
import com.example.data.model.JobTender
import com.example.data.model.JordanSeedData
import com.example.data.model.MonthlyBuildingPackage
import com.example.data.model.PromotedServiceAd
import com.example.data.model.ProviderProfile
import com.example.data.model.ServiceBooking
import com.example.data.model.SkillCategory
import com.example.data.model.ThemeMode
import com.example.data.model.UserProfile
import com.example.data.repository.ForasniRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DirectoryFilterState(
    val selectedSkill: SkillCategory = SkillCategory.ALL,
    val selectedGovernorate: String = "الكل",
    val selectedNeighborhood: String = "جميع الأحياء",
    val searchQuery: String = "",
    val onlyAvailableNow: Boolean = false,
    val showLiveMapSection: Boolean = true
)

class ForasniViewModel(
    private val repository: ForasniRepository,
    val currentUserId: String,
    private val initialDisplayName: String,
    val isAuthenticated: Boolean = true
) : ViewModel() {

    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _filterState = MutableStateFlow(DirectoryFilterState())
    val filterState: StateFlow<DirectoryFilterState> = _filterState.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _unlockedTenderIds = MutableStateFlow<Set<String>>(emptySet())
    val unlockedTenderIds: StateFlow<Set<String>> = _unlockedTenderIds.asStateFlow()

    private val _acceptedTenderIds = MutableStateFlow<Set<String>>(emptySet())
    val acceptedTenderIds: StateFlow<Set<String>> = _acceptedTenderIds.asStateFlow()

    private val _promotedAdsState = MutableStateFlow<List<PromotedServiceAd>>(JordanSeedData.initialPromotedAds)
    val promotedAdsState: StateFlow<List<PromotedServiceAd>> = _promotedAdsState.asStateFlow()

    // Automatically triggered when a craftsman marks a booking as COMPLETED so the customer rates & leaves a note
    private val _autoReviewPrompt = MutableStateFlow<Pair<ProviderProfile, ServiceBooking>?>(null)
    val autoReviewPrompt: StateFlow<Pair<ProviderProfile, ServiceBooking>?> = _autoReviewPrompt.asStateFlow()

    val b2bPartnerOffers: List<B2BPartnerOffer> = JordanSeedData.b2bHardwarePartnerOffers
    val monthlyBuildingPackages: List<MonthlyBuildingPackage> = JordanSeedData.monthlyBuildingPackages

    private val localUserProfileFlow = MutableStateFlow<UserProfile?>(
        UserProfile(
            id = currentUserId,
            userId = currentUserId,
            displayName = initialDisplayName,
            accountType = "CLIENT",
            governorate = "عمّان",
            neighborhood = "خلدا",
            phoneNumber = "+962795001122",
            pointsBalance = 35L,
            linkedProviderId = "prov_user_$currentUserId"
        )
    )

    private val localProvidersFlow = MutableStateFlow<List<ProviderProfile>>(
        JordanSeedData.initialProviders(currentUserId).map { it.second }
    )

    private val localTendersFlow = MutableStateFlow<List<JobTender>>(
        JordanSeedData.initialTenders(currentUserId).map { it.second }
    )

    private val localBookingsFlow = MutableStateFlow<List<ServiceBooking>>(
        listOf(
            ServiceBooking(
                id = "booking_demo_1",
                userId = currentUserId,
                providerId = "prov_amman_clean_10",
                providerName = "أبو العز التعمري",
                providerSkill = "شطف وتنظيف عمارات، درج ومداخل",
                providerPhone = "+962796012345",
                serviceTitle = "شطف وتنظيف درج ومدخل العمارة (4 طوابق)",
                governorate = "عمّان",
                neighborhood = "طبربور",
                scheduledDate = "اليوم - 2026-11-04",
                scheduledTime = "04:30 مساءً",
                trackingStage = "ON_THE_WAY",
                trackingEtaMinutes = 9L,
                escrowAmountJod = 15L,
                escrowCommissionJod = 1L,
                escrowStatus = "HELD_IN_ESCROW",
                isReviewed = false
            ),
            ServiceBooking(
                id = "booking_demo_2",
                userId = currentUserId,
                providerId = "prov_amman_garden_11",
                providerName = "أبو رعد السلطي",
                providerSkill = "جز عشب وتنسيق حدائق منزلية",
                providerPhone = "+962775129900",
                serviceTitle = "جز عشب الحديقة وتقليم الأشجار",
                governorate = "عمّان",
                neighborhood = "خلدا",
                scheduledDate = "اليوم - 2026-11-04",
                scheduledTime = "05:30 مساءً",
                trackingStage = "IN_PROGRESS",
                trackingEtaMinutes = 0L,
                escrowAmountJod = 20L,
                escrowCommissionJod = 1L,
                escrowStatus = "HELD_IN_ESCROW",
                isReviewed = false
            )
        )
    )

    private val activeUserProfileSource: Flow<UserProfile?> =
        if (isAuthenticated) repository.observeUserProfile(currentUserId) else localUserProfileFlow

    private val activeProvidersSource: Flow<List<ProviderProfile>> =
        if (isAuthenticated) repository.observePublishedProviders() else localProvidersFlow

    private val activeTendersSource: Flow<List<JobTender>> =
        if (isAuthenticated) repository.observePublishedTenders() else localTendersFlow

    private val activeBookingsSource: Flow<List<ServiceBooking>> =
        if (isAuthenticated) repository.observeUserBookings(currentUserId) else localBookingsFlow

    val userProfileState: StateFlow<UiState<UserProfile?>> = activeUserProfileSource
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing userProfile", error)
            emit(UiState.Error(error.localizedMessage ?: "تعذر تحميل الملف الشخصي"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Success(localUserProfileFlow.value)
        )

    val providersState: StateFlow<UiState<List<ProviderProfile>>> = activeProvidersSource
        .map<List<ProviderProfile>, UiState<List<ProviderProfile>>> { list ->
            UiState.Success(list.sortedWith(compareByDescending<ProviderProfile> { it.isAvailableNow }.thenByDescending { it.ratingAvg }))
        }
        .catch { error ->
            Log.w(TAG, "Error observing providers", error)
            emit(UiState.Error(error.localizedMessage ?: "تعذر تحميل قائمة الحرفيين"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Success(localProvidersFlow.value)
        )

    val filteredProvidersState: StateFlow<UiState<List<ProviderProfile>>> = combine(
        providersState,
        _filterState
    ) { provState, filter ->
        when (provState) {
            is UiState.Loading -> UiState.Loading
            is UiState.Error -> provState
            is UiState.Success -> {
                val filtered = provState.data.filter { provider ->
                    val matchesSkill = filter.selectedSkill == SkillCategory.ALL ||
                        provider.skillCategory == filter.selectedSkill.id
                    val matchesGov = filter.selectedGovernorate == "الكل" ||
                        provider.governorate == filter.selectedGovernorate
                    val matchesNeighborhood = filter.selectedNeighborhood == "جميع الأحياء" ||
                        provider.neighborhood == filter.selectedNeighborhood
                    val matchesAvailability = !filter.onlyAvailableNow || provider.isAvailableNow
                    val q = filter.searchQuery.trim()
                    val matchesQuery = q.isEmpty() ||
                        provider.name.contains(q, ignoreCase = true) ||
                        provider.skillTitle.contains(q, ignoreCase = true) ||
                        provider.governorate.contains(q, ignoreCase = true) ||
                        provider.neighborhood.contains(q, ignoreCase = true) ||
                        provider.bio.contains(q, ignoreCase = true)

                    matchesSkill && matchesGov && matchesNeighborhood && matchesAvailability && matchesQuery
                }
                UiState.Success(filtered)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Success(localProvidersFlow.value)
    )

    val tendersState: StateFlow<UiState<List<JobTender>>> = activeTendersSource
        .map<List<JobTender>, UiState<List<JobTender>>> { list ->
            UiState.Success(list.sortedByDescending { it.budgetJod })
        }
        .catch { error ->
            Log.w(TAG, "Error observing tenders", error)
            emit(UiState.Error(error.localizedMessage ?: "تعذر تحميل المناقصات وطلبات العمل"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Success(localTendersFlow.value)
        )

    val bookingsState: StateFlow<UiState<List<ServiceBooking>>> = activeBookingsSource
        .map<List<ServiceBooking>, UiState<List<ServiceBooking>>> { list ->
            UiState.Success(list)
        }
        .catch { error ->
            Log.w(TAG, "Error observing bookings", error)
            emit(UiState.Error(error.localizedMessage ?: "تعذر تحميل المواعيد وعقود الضمان"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Success(localBookingsFlow.value)
        )

    init {
        if (isAuthenticated) {
            bootstrapSessionData()
        }
    }

    private fun bootstrapSessionData() {
        viewModelScope.launch {
            repository.ensureUserProfile(displayName = initialDisplayName)
            repository.seedInitialMarketplaceIfEmpty()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        _statusBanner.value = "تم تغيير مظهر التطبيق إلى: ${mode.labelAr}"
    }

    fun toggleLightDarkTheme(isCurrentlyDark: Boolean) {
        val next = if (isCurrentlyDark) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(next)
    }

    fun dismissAutoReviewPrompt() {
        _autoReviewPrompt.value = null
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun selectSkillCategory(category: SkillCategory) {
        _filterState.value = _filterState.value.copy(selectedSkill = category)
    }

    fun selectGovernorate(governorate: String) {
        _filterState.value = _filterState.value.copy(
            selectedGovernorate = governorate,
            selectedNeighborhood = "جميع الأحياء"
        )
    }

    fun selectNeighborhood(neighborhood: String) {
        _filterState.value = _filterState.value.copy(selectedNeighborhood = neighborhood)
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun toggleOnlyAvailableNow() {
        _filterState.value = _filterState.value.copy(
            onlyAvailableNow = !_filterState.value.onlyAvailableNow
        )
    }

    fun toggleLiveMapSection() {
        _filterState.value = _filterState.value.copy(
            showLiveMapSection = !_filterState.value.showLiveMapSection
        )
    }

    fun resetFilters() {
        _filterState.value = DirectoryFilterState()
    }

    fun availableNeighborhoodsForSelectedGov(): List<String> {
        val gov = _filterState.value.selectedGovernorate
        if (gov == "الكل") return listOf("جميع الأحياء")
        return JORDAN_GOVERNORATES.find { it.nameAr == gov }?.neighborhoods
            ?: listOf("جميع الأحياء")
    }

    fun confirmCliQTransfer(recipientAlias: String, amountJod: Long, purposeLabel: String) {
        _statusBanner.value = "تم تأكيد تحويل كليك (CliQ) بقيمة $amountJod د.أ إلى اللقب ($recipientAlias) مقابل: $purposeLabel"
    }

    fun recordDirectLeadAndNotify(provider: ProviderProfile, channelLabel: String) {
        if (!isAuthenticated) {
            localProvidersFlow.value = localProvidersFlow.value.map {
                if (it.id == provider.id) {
                    it.copy(
                        pointsBalance = (it.pointsBalance - 1L).coerceAtLeast(0L),
                        leadsCount = it.leadsCount + 1L
                    )
                } else {
                    it
                }
            }
            _statusBanner.value = "تم فتح $channelLabel المباشر مع ${provider.name} (تم احتساب نقطة نتيجة واحدة من رصيد الحرفي)"
            return
        }
        viewModelScope.launch {
            val result = repository.recordDirectLeadContact(provider)
            if (result.isSuccess) {
                _statusBanner.value = "تم فتح $channelLabel المباشر مع ${provider.name} (تم احتساب نقطة نتيجة واحدة من رصيد الحرفي)"
            }
        }
    }

    fun switchUserAccountType(newType: String) {
        val currentProfile = (userProfileState.value as? UiState.Success)?.data ?: return
        val roleLabel = if (newType == "PROVIDER") "مقدم خدمة / حرفي مستقل" else "طالب خدمة / زبون"
        if (!isAuthenticated) {
            localUserProfileFlow.value = currentProfile.copy(accountType = newType)
            _statusBanner.value = "تم تحديث نوع حسابك إلى: $roleLabel"
            return
        }
        viewModelScope.launch {
            val result = repository.updateUserProfile(
                displayName = currentProfile.displayName,
                accountType = newType,
                governorate = currentProfile.governorate,
                neighborhood = currentProfile.neighborhood,
                phoneNumber = currentProfile.phoneNumber,
                pointsBalance = currentProfile.pointsBalance
            )
            if (result.isSuccess) {
                _statusBanner.value = "تم تحديث نوع حسابك إلى: $roleLabel"
            }
        }
    }

    fun updateAccountDetails(
        displayName: String,
        accountType: String,
        governorate: String,
        neighborhood: String,
        phoneNumber: String
    ) {
        val currentProfile = (userProfileState.value as? UiState.Success)?.data
        val currentPoints = currentProfile?.pointsBalance ?: 35L
        if (!isAuthenticated) {
            localUserProfileFlow.value = (currentProfile ?: UserProfile()).copy(
                displayName = displayName,
                accountType = accountType,
                governorate = governorate,
                neighborhood = neighborhood,
                phoneNumber = phoneNumber,
                pointsBalance = currentPoints
            )
            _statusBanner.value = "تم حفظ بيانات حسابك بنجاح"
            return
        }
        viewModelScope.launch {
            val result = repository.updateUserProfile(
                displayName = displayName,
                accountType = accountType,
                governorate = governorate,
                neighborhood = neighborhood,
                phoneNumber = phoneNumber,
                pointsBalance = currentPoints
            )
            if (result.isSuccess) {
                _statusBanner.value = "تم حفظ بيانات حسابك بنجاح"
            } else {
                _statusBanner.value = "تعذر حفظ البيانات، يرجى المحاولة مرة أخرى"
            }
        }
    }

    fun topUpPoints(pointsToAdd: Long, priceJod: Int) {
        val currentProfile = (userProfileState.value as? UiState.Success)?.data ?: return
        val newBalance = currentProfile.pointsBalance + pointsToAdd
        if (!isAuthenticated) {
            localUserProfileFlow.value = currentProfile.copy(pointsBalance = newBalance)
            _statusBanner.value = "تم شحن محفظتك عبر كليك (CliQ) بـ +$pointsToAdd نقطة بنجاح ($priceJod د.أ)"
            return
        }
        viewModelScope.launch {
            val result = repository.updateUserProfile(
                displayName = currentProfile.displayName,
                accountType = currentProfile.accountType,
                governorate = currentProfile.governorate,
                neighborhood = currentProfile.neighborhood,
                phoneNumber = currentProfile.phoneNumber,
                pointsBalance = newBalance
            )
            if (result.isSuccess) {
                _statusBanner.value = "تم شحن محفظتك عبر كليك (CliQ) بـ +$pointsToAdd نقطة بنجاح ($priceJod د.أ)"
            }
        }
    }

    fun saveMyCraftsmanProfile(
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
        licenseTitle: String,
        vehicleModel: String = "فان هونداي H1 أبيض",
        vehiclePlate: String = "19-84520",
        portfolioTitles: List<String>,
        portfolioTypes: List<String>
    ) {
        val currentProfile = (userProfileState.value as? UiState.Success)?.data
        val points = currentProfile?.pointsBalance ?: 35L
        val myDocId = "prov_user_$currentUserId"
        val cleanBaseLicense = licenseTitle.substringBefore("• المركبة:").trim().ifBlank { "حرفي مستقل مسجل في فرصني" }
        val combinedLicenseAndVehicle = if (vehicleModel.isNotBlank()) {
            "$cleanBaseLicense • المركبة: ${vehicleModel.trim()} (لوحة ${vehiclePlate.trim().ifEmpty { "23-51890" }})"
        } else {
            cleanBaseLicense
        }.take(195)

        if (!isAuthenticated) {
            val newProvider = ProviderProfile(
                id = myDocId,
                ownerId = currentUserId,
                isPublished = true,
                name = name,
                skillCategory = skillCategory,
                skillTitle = skillTitle,
                governorate = governorate,
                neighborhood = neighborhood,
                bio = bio,
                businessPhone = businessPhone,
                whatsappNumber = whatsappNumber,
                hourlyRateJod = hourlyRateJod,
                inspectionFeeJod = inspectionFeeJod,
                isAvailableNow = isAvailableNow,
                isVerified = true,
                licenseTitle = combinedLicenseAndVehicle,
                portfolioCount = portfolioTitles.size.toLong(),
                portfolioTitles = portfolioTitles.joinToString("|"),
                portfolioTypes = portfolioTypes.joinToString("|"),
                pointsBalance = points,
                leadsCount = 1L,
                ratingAvg = 5.0,
                reviewCount = 1L,
                latestReviewText = "إدارة فرصني★5★حرفي مسجل وموثق في منصة فرصني",
                distanceKm = 1.2
            )
            val existingList = localProvidersFlow.value.filterNot { it.id == myDocId }
            localProvidersFlow.value = listOf(newProvider) + existingList
            if (currentProfile != null) {
                localUserProfileFlow.value = currentProfile.copy(
                    displayName = name,
                    accountType = "PROVIDER",
                    governorate = governorate,
                    neighborhood = neighborhood,
                    phoneNumber = businessPhone
                )
            }
            _statusBanner.value = "تم حفظ ملفك المهني وبيانات مركبتك ($vehicleModel - لوحة $vehiclePlate) ومعرض أعمالك (${portfolioTitles.size} عمل)!"
            return
        }

        viewModelScope.launch {
            val result = repository.saveProviderProfile(
                providerId = myDocId,
                name = name,
                skillCategory = skillCategory,
                skillTitle = skillTitle,
                governorate = governorate,
                neighborhood = neighborhood,
                bio = bio,
                businessPhone = businessPhone,
                whatsappNumber = whatsappNumber,
                hourlyRateJod = hourlyRateJod,
                inspectionFeeJod = inspectionFeeJod,
                isAvailableNow = isAvailableNow,
                isVerified = true,
                licenseTitle = combinedLicenseAndVehicle,
                portfolioTitlesList = portfolioTitles,
                portfolioTypesList = portfolioTypes,
                pointsBalance = points
            )
            if (result.isSuccess) {
                if (currentProfile != null && currentProfile.accountType != "PROVIDER") {
                    repository.updateUserProfile(
                        displayName = name,
                        accountType = "PROVIDER",
                        governorate = governorate,
                        neighborhood = neighborhood,
                        phoneNumber = businessPhone,
                        pointsBalance = points
                    )
                }
                _statusBanner.value = "تم حفظ ملفك المهني وبيانات مركبتك ($vehicleModel - لوحة $vehiclePlate) ومعرض أعمالك (${portfolioTitles.size} عمل)!"
            } else {
                _statusBanner.value = "تعذر حفظ ملف الحرفي: ${result.exceptionOrNull()?.localizedMessage ?: ""}"
            }
        }
    }

    fun acceptJobOpportunityDirectly(tender: JobTender) {
        val currentProfile = (userProfileState.value as? UiState.Success)?.data
        val craftsmanName = currentProfile?.displayName ?: "حرفي فرصني"
        _acceptedTenderIds.value = _acceptedTenderIds.value + tender.id
        _unlockedTenderIds.value = _unlockedTenderIds.value + tender.id

        val summary = "تم قبول الوظيفة من قبل: $craftsmanName (جاهز للتنفيذ بـ ${tender.budgetJod} د.أ)"
        localTendersFlow.value = localTendersFlow.value.map {
            if (it.id == tender.id) {
                it.copy(
                    bidsCount = it.bidsCount + 1L,
                    latestBidSummary = summary
                )
            } else {
                it
            }
        }

        val newBooking = ServiceBooking(
            id = "booking_job_${tender.id}",
            userId = currentUserId,
            providerId = "prov_user_$currentUserId",
            providerName = craftsmanName,
            providerSkill = SkillCategory.fromId(tender.skillCategory).titleAr,
            providerPhone = currentProfile?.phoneNumber ?: "+962795001122",
            serviceTitle = "${tender.title} (للزبون: ${tender.clientName})",
            governorate = tender.governorate,
            neighborhood = tender.neighborhood,
            scheduledDate = "اليوم - فوري",
            scheduledTime = "الآن",
            trackingStage = "ON_THE_WAY",
            trackingEtaMinutes = 11L,
            escrowAmountJod = tender.budgetJod,
            escrowCommissionJod = 1L,
            escrowStatus = "HELD_IN_ESCROW",
            isReviewed = false
        )
        localBookingsFlow.value = listOf(newBooking) + localBookingsFlow.value.filterNot { it.id == newBooking.id }
        _statusBanner.value = "تم قبول الوظيفة (${tender.title}) بنجاح! ظهرت الآن في جدول مواعيدك وفتحت بيانات تواصل الزبون (${tender.clientPhone})."
    }

    fun publishPromotedAd5Jod(
        providerName: String,
        skillCategory: String,
        adTitle: String,
        adDescription: String,
        governorate: String,
        neighborhood: String,
        priceLabel: String,
        phone: String,
        cliqAlias: String
    ) {
        val newAd = PromotedServiceAd(
            id = "ad_vip_${UUID.randomUUID().toString().take(6)}",
            providerId = "prov_user_$currentUserId",
            providerName = providerName,
            skillCategory = skillCategory,
            adTitle = adTitle,
            adDescription = adDescription,
            governorate = governorate,
            neighborhood = neighborhood,
            priceLabel = priceLabel,
            phone = phone,
            whatsapp = phone.filter { it.isDigit() },
            cliqAlias = cliqAlias.ifBlank { "FORASNIJO" },
            promotionTierLabel = "إعلان ممول VIP ★ (مدفوع 5 دنانير - مثبت بالصدارة)",
            hasBeforeAfterPhotos = true,
            viewsCount = 120
        )
        _promotedAdsState.value = listOf(newAd) + _promotedAdsState.value
        _statusBanner.value = "تم تفعيل إعلانك الممول (بـ 5 دنانير على طريقة السوق المفتوح) وتثبيته في صدارة الإعلانات!"
    }

    fun subscribeToMonthlyPackage(pkg: MonthlyBuildingPackage) {
        val newBooking = ServiceBooking(
            id = "booking_pkg_${pkg.id}",
            userId = currentUserId,
            providerId = "prov_amman_clean_10",
            providerName = "فريق باقات فرصني الشهرية",
            providerSkill = pkg.titleAr,
            providerPhone = "+962796012345",
            serviceTitle = pkg.titleAr,
            governorate = "عمّان",
            neighborhood = "خلدا / طبربور",
            scheduledDate = "اشتراك شهري (كل جمعة)",
            scheduledTime = "10:00 صباحاً",
            trackingStage = "CONFIRMED",
            trackingEtaMinutes = 15L,
            escrowAmountJod = pkg.monthlyPriceJod.toLong(),
            escrowCommissionJod = 2L,
            escrowStatus = "HELD_IN_ESCROW",
            isReviewed = false
        )
        localBookingsFlow.value = listOf(newBooking) + localBookingsFlow.value.filterNot { it.id == newBooking.id }
        _statusBanner.value = "تم الاشتراك في (${pkg.titleAr}) بـ ${pkg.monthlyPriceJod} د.أ شهرياً وإضافتها لجدول المواعيد!"
    }

    fun postNewJobTender(
        title: String,
        skillCategory: String,
        governorate: String,
        neighborhood: String,
        description: String,
        budgetJod: Long,
        urgency: String,
        clientPhone: String
    ) {
        val currentProfile = (userProfileState.value as? UiState.Success)?.data
        val clientName = currentProfile?.displayName ?: initialDisplayName
        if (!isAuthenticated) {
            val newTender = JobTender(
                id = "tender_${UUID.randomUUID().toString().take(8)}",
                clientId = currentUserId,
                clientName = clientName,
                clientPhone = clientPhone,
                isPublished = true,
                title = title,
                skillCategory = skillCategory,
                governorate = governorate,
                neighborhood = neighborhood,
                description = description,
                budgetJod = budgetJod,
                urgency = urgency,
                unlockCostPoints = 1L,
                status = "OPEN",
                bidsCount = 0L,
                lowestBidJod = 0L,
                latestBidSummary = "بانتظار أول عرض سعر من الحرفيين",
                unlockedByCount = 0L
            )
            localTendersFlow.value = listOf(newTender) + localTendersFlow.value
            _statusBanner.value = "تم نشر طلب العمل ($title - بميزانية $budgetJod د.أ) في المناقصات!"
            return
        }
        viewModelScope.launch {
            val result = repository.createJobTender(
                clientName = clientName,
                clientPhone = clientPhone,
                title = title,
                skillCategory = skillCategory,
                governorate = governorate,
                neighborhood = neighborhood,
                description = description,
                budgetJod = budgetJod,
                urgency = urgency
            )
            if (result.isSuccess) {
                _statusBanner.value = "تم نشر طلب العمل ($title - بميزانية $budgetJod د.أ) في المناقصات!"
            } else {
                _statusBanner.value = "تعذر نشر الطلب، يرجى التحقق من الحقول"
            }
        }
    }

    fun unlockTenderAndBid(
        tender: JobTender,
        bidPriceJod: Long,
        bidNote: String
    ) {
        val currentProfile = (userProfileState.value as? UiState.Success)?.data
        val currentPoints = currentProfile?.pointsBalance ?: 35L
        if (currentPoints < tender.unlockCostPoints) {
            _statusBanner.value = "رصيد النقاط غير كافٍ لفتح المناقصة (${tender.unlockCostPoints} نقاط). يرجى شحن محفظتك."
            return
        }
        val craftsmanName = currentProfile?.displayName ?: initialDisplayName
        if (!isAuthenticated) {
            val safeBid = bidPriceJod.coerceIn(1L, 50000L)
            val newLowest = if (tender.lowestBidJod <= 0L || safeBid < tender.lowestBidJod) safeBid else tender.lowestBidJod
            val summary = "${craftsmanName.take(40)}: ${bidNote.take(220)} ($safeBid د.أ)"
            localTendersFlow.value = localTendersFlow.value.map {
                if (it.id == tender.id) {
                    it.copy(
                        bidsCount = it.bidsCount + 1L,
                        lowestBidJod = newLowest,
                        latestBidSummary = summary,
                        unlockedByCount = it.unlockedByCount + 1L
                    )
                } else {
                    it
                }
            }
            if (currentProfile != null) {
                localUserProfileFlow.value = currentProfile.copy(
                    pointsBalance = (currentPoints - tender.unlockCostPoints).coerceAtLeast(0L)
                )
            }
            _unlockedTenderIds.value = _unlockedTenderIds.value + tender.id
            _statusBanner.value = "تم فتح بيانات تواصل الزبون وإرسال عرض السعر ($bidPriceJod د.أ) بنجاح!"
            return
        }
        viewModelScope.launch {
            val bidResult = repository.unlockTenderAndSubmitBid(
                tender = tender,
                craftsmanName = craftsmanName,
                bidPriceJod = bidPriceJod,
                bidNote = bidNote
            )
            if (bidResult.isSuccess) {
                if (currentProfile != null) {
                    repository.updateUserProfile(
                        displayName = currentProfile.displayName,
                        accountType = currentProfile.accountType,
                        governorate = currentProfile.governorate,
                        neighborhood = currentProfile.neighborhood,
                        phoneNumber = currentProfile.phoneNumber,
                        pointsBalance = (currentPoints - tender.unlockCostPoints).coerceAtLeast(0L)
                    )
                }
                _unlockedTenderIds.value = _unlockedTenderIds.value + tender.id
                _statusBanner.value = "تم فتح بيانات تواصل الزبون وإرسال عرض السعر ($bidPriceJod د.أ) بنجاح!"
            }
        }
    }

    fun createAppointmentBooking(
        provider: ProviderProfile,
        serviceTitle: String,
        scheduledDate: String,
        scheduledTime: String,
        escrowAmountJod: Long,
        useEscrow: Boolean
    ) {
        val escrowMsg = if (useEscrow) "مع تحويل وحجز $escrowAmountJod د.أ عبر كليك (CliQ Escrow)" else "بنظام الدفع عند الإنجاز"
        if (!isAuthenticated) {
            val safeEscrow = if (useEscrow) escrowAmountJod.coerceIn(5L, 50000L) else 0L
            val commission = if (useEscrow) (safeEscrow * 5L / 100L).coerceAtLeast(1L) else 0L
            val newBooking = ServiceBooking(
                id = "booking_${UUID.randomUUID().toString().take(8)}",
                userId = currentUserId,
                providerId = provider.id,
                providerName = provider.name,
                providerSkill = provider.skillTitle,
                providerPhone = provider.businessPhone,
                serviceTitle = serviceTitle,
                governorate = provider.governorate,
                neighborhood = provider.neighborhood,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime,
                trackingStage = "CONFIRMED",
                trackingEtaMinutes = 16L,
                escrowAmountJod = safeEscrow,
                escrowCommissionJod = commission,
                escrowStatus = if (useEscrow && safeEscrow > 0L) "HELD_IN_ESCROW" else "NONE",
                isReviewed = false
            )
            localBookingsFlow.value = listOf(newBooking) + localBookingsFlow.value
            _statusBanner.value = "تم حجز الموعد مع ${provider.name} $escrowMsg!"
            return
        }
        viewModelScope.launch {
            val result = repository.createBooking(
                providerId = provider.id,
                providerName = provider.name,
                providerSkill = provider.skillTitle,
                providerPhone = provider.businessPhone,
                serviceTitle = serviceTitle,
                governorate = provider.governorate,
                neighborhood = provider.neighborhood,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime,
                escrowAmountJod = escrowAmountJod,
                useEscrow = useEscrow
            )
            if (result.isSuccess) {
                _statusBanner.value = "تم حجز الموعد مع ${provider.name} $escrowMsg!"
            }
        }
    }

    fun advanceBookingProgress(booking: ServiceBooking) {
        val (nextStage, nextEta, nextEscrow) = when (booking.trackingStage) {
            "CONFIRMED" -> Triple("ON_THE_WAY", 9L, booking.escrowStatus)
            "ON_THE_WAY" -> Triple("IN_PROGRESS", 0L, booking.escrowStatus)
            "IN_PROGRESS" -> Triple(
                "COMPLETED",
                0L,
                if (booking.escrowStatus == "HELD_IN_ESCROW") "RELEASED" else booking.escrowStatus
            )
            else -> return
        }
        val msg = when (nextStage) {
            "ON_THE_WAY" -> "انطلق الحرفي ${booking.providerName}! يمكنك تتبع موقعه على الخريطة (باقي 9 دقائق)"
            "IN_PROGRESS" -> "وصل الحرفي ${booking.providerName} لموقعك وبدأ العمل الآن"
            "COMPLETED" -> if (nextEscrow == "RELEASED") {
                "ضغط الحرفي «إنهاء العمل» وتم تحرير الضمان (${booking.escrowAmountJod} د.أ عبر كليك). قيّم تجربتك الآن!"
            } else {
                "أنهى الحرفي ${booking.providerName} العمل! نافذة التقييم والملاحظة جاهزة الآن."
            }
            else -> "تم تحديث حالة الموعد"
        }

        val allProvs = (providersState.value as? UiState.Success)?.data ?: localProvidersFlow.value
        val matchedProvider = allProvs.find { it.id == booking.providerId } ?: allProvs.firstOrNull()

        if (!isAuthenticated) {
            val updatedBooking = booking.copy(
                trackingStage = nextStage,
                trackingEtaMinutes = nextEta,
                escrowStatus = nextEscrow
            )
            localBookingsFlow.value = localBookingsFlow.value.map {
                if (it.id == booking.id) updatedBooking else it
            }
            _statusBanner.value = msg
            if (nextStage == "COMPLETED" && matchedProvider != null) {
                _autoReviewPrompt.value = matchedProvider to updatedBooking
            }
            return
        }

        viewModelScope.launch {
            val result = repository.updateBookingStageAndEscrow(
                bookingId = booking.id,
                newStage = nextStage,
                newEtaMinutes = nextEta,
                newEscrowStatus = nextEscrow
            )
            if (result.isSuccess) {
                _statusBanner.value = msg
                if (nextStage == "COMPLETED" && matchedProvider != null) {
                    _autoReviewPrompt.value = matchedProvider to booking.copy(
                        trackingStage = nextStage,
                        escrowStatus = nextEscrow
                    )
                }
            }
        }
    }

    fun submitReview(
        provider: ProviderProfile,
        stars: Int,
        comment: String,
        bookingId: String? = null
    ) {
        val currentProfile = (userProfileState.value as? UiState.Success)?.data
        val reviewerName = currentProfile?.displayName?.take(25) ?: "زبون موثق"
        val formattedReviewEntry = "$reviewerName★${stars.coerceIn(1, 5)}★${comment.trim()}"
        val combinedReviews = if (provider.latestReviewText.isNotBlank()) {
            "$formattedReviewEntry||${provider.latestReviewText}".take(460)
        } else {
            formattedReviewEntry.take(460)
        }

        _autoReviewPrompt.value = null

        if (!isAuthenticated) {
            val oldCount = provider.reviewCount.coerceAtLeast(0L)
            val newCount = oldCount + 1L
            val rawAvg = ((provider.ratingAvg * oldCount) + stars.coerceIn(1, 5)) / newCount.toDouble()
            val roundedAvg = ((rawAvg * 10.0).roundToInt() / 10.0).coerceIn(1.0, 5.0)
            localProvidersFlow.value = localProvidersFlow.value.map {
                if (it.id == provider.id) {
                    it.copy(
                        ratingAvg = roundedAvg,
                        reviewCount = newCount,
                        latestReviewText = combinedReviews
                    )
                } else {
                    it
                }
            }
            if (!bookingId.isNullOrBlank()) {
                localBookingsFlow.value = localBookingsFlow.value.map {
                    if (it.id == bookingId) it.copy(isReviewed = true) else it
                }
            }
            _statusBanner.value = "تم نشر تقييمك ($stars نجوم) وملاحظتك في ملف ${provider.name} ليراها جميع الزبائن!"
            return
        }
        viewModelScope.launch {
            val result = repository.submitProviderReview(
                provider = provider,
                stars = stars,
                reviewComment = combinedReviews,
                bookingId = bookingId
            )
            if (result.isSuccess) {
                _statusBanner.value = "تم نشر تقييمك ($stars نجوم) وملاحظتك في ملف ${provider.name} ليراها جميع الزبائن!"
            }
        }
    }

    private companion object {
        const val TAG = "ForasniVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
