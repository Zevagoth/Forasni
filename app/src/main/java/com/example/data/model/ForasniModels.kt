package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ThemeMode(val labelAr: String) {
    LIGHT("الوضع الساطع (نهاري)"),
    DARK("الوضع الداكن (ليلي)"),
    SYSTEM("تلقائي حسب الجهاز")
}

enum class PriceAgreementMode(val id: String, val labelAr: String) {
    FIXED("FIXED", "سعر محدد مسبقاً"),
    NEGOTIABLE("NEGOTIABLE", "قابل للمفاصلة والتفاوض"),
    ON_INSPECTION("ON_INSPECTION", "لم يُحدد بعد (بالاتفاق مع الحرفي)")
}

enum class SkillCategory(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val isHomeMaintenance: Boolean,
    val marketPriceRangeAr: String
) {
    ALL("ALL", "الكل", "جميع المهارات والمهن", true, "متوسط أسعار الخدمات اليومية في الأردن: 10 - 25 د.أ"),
    CLEANING("CLEANING", "شطف وتنظيف", "شطف عمارات، درج ومنازل", true, "متوسط سعر شطف العمارة/الدرج في الأردن: 12 - 18 د.أ"),
    GARDENING("GARDENING", "جز عشب وحدائق", "تنسيق حدائق وتقليم أشجار", true, "متوسط سعر جز عشب الحديقة وتقليم الشجر: 15 - 25 د.أ"),
    AC_TECH("AC_TECH", "إصلاح تكييف وأجهزة", "تصليح مكيفات، غسالات وثلاجات", true, "متوسط سعر إصلاح/صيانة المكيف أو الجهاز في الأردن: 12 - 20 د.أ"),
    ELECTRICIAN("ELECTRICIAN", "كهربائي", "تمديدات وصيانة أعطال", true, "متوسط سعر كشف وإصلاح أعطال الكهرباء: 10 - 20 د.أ"),
    PLUMBER("PLUMBER", "سباك", "مواسير وتأسيس صحي", true, "متوسط سعر صيانة السباكة والمضخات: 10 - 18 د.أ"),
    CARPENTER("CARPENTER", "نجار", "أثاث، أبواب ومطابخ", true, "متوسط سعر صيانة الأبواب والمفصلات والنجارة: 12 - 25 د.أ"),
    BLACKSMITH("BLACKSMITH", "حداد", "حمايات، مظلات وأبواب", true, "متوسط سعر أعمال الحدادة والصيانة: 15 - 30 د.أ"),
    TUTOR("TUTOR", "مدرس خصوصي", "توجيهي، جامعات ولغات", false, "متوسط سعر الحصة الخصوصية في الأردن: 12 - 20 د.أ"),
    DESIGNER("DESIGNER", "مصمم جرافيك", "هويات بصرية وسوشيال ميديا", false, "متوسط سعر التصميم الجرافيكي والريلز: 10 - 25 د.أ"),
    TRANSLATOR("TRANSLATOR", "مترجم", "ترجمة فورية ومعتمدة", false, "متوسط سعر الترجمة للصفحة/الساعة: 8 - 15 د.أ"),
    DRIVER("DRIVER", "سائق وتوصيل", "نقل، توصيل وشحن خفيف", false, "متوسط سعر نقلة الباص/البيك أب داخل المحافظة: 10 - 20 د.أ");

    companion object {
        fun fromId(id: String): SkillCategory =
            entries.find { it.id == id } ?: ELECTRICIAN
    }
}

data class JordanGovernorate(
    val nameAr: String,
    val neighborhoods: List<String>
)

val JORDAN_GOVERNORATES = listOf(
    JordanGovernorate(
        nameAr = "عمّان",
        neighborhoods = listOf("جميع الأحياء", "خلدا", "طبربور", "عبدون", "تلاع العلي", "جبل الحسين", "مرج الحمام", "الجبيهة", "الشميساني", "صويلح")
    ),
    JordanGovernorate(
        nameAr = "إربد",
        neighborhoods = listOf("جميع الأحياء", "شارع الجامعة", "الحي الشرقي", "الهاشمي", "حنينا", "البارحة", "ضاحية الحسين")
    ),
    JordanGovernorate(
        nameAr = "الزرقاء",
        neighborhoods = listOf("جميع الأحياء", "الزرقاء الجديدة", "مدينة الشرق", "الرصيفة", "حي المعصوم", "جبل طارق")
    ),
    JordanGovernorate(
        nameAr = "العقبة",
        neighborhoods = listOf("جميع الأحياء", "المنطقة السكنية الخامسة", "المنطقة الثامنة", "الرمال", "المحدودة")
    ),
    JordanGovernorate(
        nameAr = "البلقاء (السلط)",
        neighborhoods = listOf("جميع الأحياء", "السلط المركز", "الفحيص", "ماحص", "عين الباشا")
    ),
    JordanGovernorate(
        nameAr = "مادبا",
        neighborhoods = listOf("جميع الأحياء", "وسط مادبا", "الفيصلية", "حنينا")
    )
)

data class PortfolioMediaItem(
    val index: Int,
    val title: String,
    val isVideo: Boolean,
    val isBeforeAfter: Boolean = index % 2 == 1,
    val durationLabel: String = if (isVideo) "00:45" else "صورة قبل/بعد موثقة"
)

data class CustomerReviewItem(
    val reviewerName: String,
    val stars: Int,
    val comment: String,
    val dateLabel: String
)

data class PromotedServiceAd(
    val id: String,
    val providerId: String,
    val providerName: String,
    val skillCategory: String,
    val adTitle: String,
    val adDescription: String,
    val governorate: String,
    val neighborhood: String,
    val priceLabel: String,
    val phone: String,
    val whatsapp: String,
    val cliqAlias: String,
    val promotionTierLabel: String = "إعلان ممول VIP (5 دنانير - مثبت بالصدارة)",
    val hasBeforeAfterPhotos: Boolean = true,
    val viewsCount: Int = 420
)

data class MonthlyBuildingPackage(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val monthlyPriceJod: Int,
    val regularPriceJod: Int,
    val includesList: List<String>,
    val recommendedApartments: Int = 6
)

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val accountType: String = "CLIENT", // CLIENT or PROVIDER
    val governorate: String = "عمّان",
    val neighborhood: String = "خلدا",
    val phoneNumber: String = "+962790000000",
    val pointsBalance: Long = 25L,
    val linkedProviderId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ProviderProfile(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val isPublished: Boolean = true,
    val name: String = "",
    val skillCategory: String = "ELECTRICIAN",
    val skillTitle: String = "",
    val governorate: String = "عمّان",
    val neighborhood: String = "خلدا",
    val bio: String = "",
    val businessPhone: String = "",
    val whatsappNumber: String = "",
    val hourlyRateJod: Long = 10L,
    val inspectionFeeJod: Long = 5L,
    val isAvailableNow: Boolean = true,
    val isVerified: Boolean = true,
    val licenseTitle: String = "",
    val portfolioCount: Long = 12L,
    val portfolioTitles: String = "",
    val portfolioTypes: String = "",
    val pointsBalance: Long = 30L,
    val leadsCount: Long = 0L,
    val ratingAvg: Double = 4.8,
    val reviewCount: Long = 12L,
    val latestReviewText: String = "",
    val distanceKm: Double = 1.8,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    @get:Exclude
    val cliqAlias: String
        get() = when (id) {
            "prov_amman_clean_10" -> "ABUALEZZCLEAN"
            "prov_amman_garden_11" -> "ABURAADGARDEN"
            "prov_amman_elec_1" -> "TAREQNSOUR962"
            "prov_amman_plumb_2" -> "ABUSANADPLUMB"
            "prov_irbid_carp_3" -> "MAHMOUDWOOD"
            "prov_zarqa_black_4" -> "HAMZAIRONJO"
            "prov_amman_tutor_5" -> "RAWANMATHJO"
            "prov_amman_design_6" -> "YAZANDESIGN"
            "prov_aqaba_ac_7" -> "FIRASCOOLAC"
            else -> whatsappNumber.takeLast(9).ifEmpty { "FORASNIJO" }
        }

    @get:Exclude
    val isSameNeighborhoodBadge: Boolean
        get() = distanceKm <= 2.0

    fun vehicleModelDisplay(): String {
        if (licenseTitle.contains("المركبة:")) {
            val afterCar = licenseTitle.substringAfter("المركبة:").trim()
            return afterCar.substringBefore("(").trim().ifEmpty { "سيارة خدمة مجهزة" }
        }
        return when (id) {
            "prov_amman_clean_10" -> "فان هونداي H1 أبيض (معدات شطف وتنظيف)"
            "prov_amman_garden_11" -> "بيك أب كيا بونغو (معدات جز عشب وحدائق)"
            "prov_amman_elec_1" -> "كيا نيرو هجين فضي (ورشة كهرباء متنقلة)"
            "prov_amman_plumb_2" -> "ميتسوبيشي L200 (معدات سباكة وضغط مياه)"
            "prov_aqaba_ac_7" -> "فان تويوتا هايس (ورشة تكييف وفريون)"
            "prov_amman_driver_9" -> "باص نقل هونداي H100 مغلق"
            else -> "سيارة خدمة حرفي مجهزة"
        }
    }

    fun vehiclePlateDisplay(): String {
        if (licenseTitle.contains("لوحة")) {
            val platePart = licenseTitle.substringAfter("لوحة").replace(")", "").replace(":", "").trim()
            if (platePart.isNotEmpty()) return "لوحة أردنية $platePart"
        }
        return when (id) {
            "prov_amman_clean_10" -> "لوحة أردنية 19-84520"
            "prov_amman_garden_11" -> "لوحة أردنية 42-31908"
            "prov_amman_elec_1" -> "لوحة أردنية 21-60412"
            "prov_amman_plumb_2" -> "لوحة أردنية 17-99214"
            "prov_aqaba_ac_7" -> "لوحة أردنية 38-44109"
            "prov_amman_driver_9" -> "لوحة أردنية 60-11820"
            else -> "لوحة أردنية 23-51890"
        }
    }

    fun cleanLicenseDisplay(): String =
        licenseTitle.substringBefore("• المركبة:").trim()

    fun parsedPortfolioItems(): List<PortfolioMediaItem> {
        val titles = portfolioTitles.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        val types = portfolioTypes.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        if (titles.isEmpty()) return emptyList()
        return titles.mapIndexed { idx, title ->
            val typeStr = types.getOrElse(idx) { if (idx % 3 == 0) "VIDEO" else "PHOTO" }
            val isVid = typeStr.equals("VIDEO", ignoreCase = true)
            val isBeforeAfter = !isVid && (idx % 2 == 0 || title.contains("قبل وبعد"))
            PortfolioMediaItem(
                index = idx + 1,
                title = title,
                isVideo = isVid,
                isBeforeAfter = isBeforeAfter,
                durationLabel = when {
                    isVid -> "فيديو عمل (${25 + (idx * 7) % 45} ثانية)"
                    isBeforeAfter -> "صورة (قبل / بعد الشغل) #${idx + 1}"
                    else -> "صورة عمل موثقة #${idx + 1}"
                }
            )
        }
    }

    fun parsedReviewsList(): List<CustomerReviewItem> {
        val entries = latestReviewText.split("||").map { it.trim() }.filter { it.isNotEmpty() }
        val parsed = entries.mapIndexed { idx, raw ->
            val parts = raw.split("★")
            if (parts.size >= 3) {
                CustomerReviewItem(
                    reviewerName = parts[0].trim(),
                    stars = parts[1].trim().toIntOrNull() ?: 5,
                    comment = parts[2].trim(),
                    dateLabel = if (idx == 0) "بعد إتمام عمل موثق • مؤخراً" else "زبون موثق في $governorate"
                )
            } else {
                CustomerReviewItem(
                    reviewerName = "زبون موثق في $neighborhood",
                    stars = 5,
                    comment = raw,
                    dateLabel = "بعد إتمام العمل"
                )
            }
        }
        val defaultCommunityReviews = listOf(
            CustomerReviewItem(
                reviewerName = "سامي العبداللات",
                stars = 5,
                comment = "التزم بالموعد على الدقيقة وشغله مرتب ونظيف جداً، أنصح الجميع بالتعامل معه.",
                dateLabel = "عمّان • قبل يومين"
            ),
            CustomerReviewItem(
                reviewerName = "أم عمر الحديد",
                stars = 5,
                comment = "تعامل راقٍ وأمانة في العمل والسعر نفس اللي اتفقنا عليه بالضبط.",
                dateLabel = "$governorate • قبل أسبوع"
            )
        )
        return parsed + defaultCommunityReviews
    }
}

@IgnoreExtraProperties
data class JobTender(
    @DocumentId val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val isPublished: Boolean = true,
    val title: String = "",
    val skillCategory: String = "ELECTRICIAN",
    val governorate: String = "عمّان",
    val neighborhood: String = "خلدا",
    val description: String = "",
    val budgetJod: Long = 25L,
    val urgency: String = "URGENT", // URGENT, TODAY, FLEXIBLE
    val unlockCostPoints: Long = 2L,
    val status: String = "OPEN", // OPEN, IN_PROGRESS, CLOSED
    val bidsCount: Long = 0L,
    val lowestBidJod: Long = 0L,
    val latestBidSummary: String = "",
    val unlockedByCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun isPriceOnInspection(): Boolean =
        description.contains("[السعر: بالاتفاق بعد المعاينة]")

    fun isPriceNegotiable(): Boolean =
        description.contains("[قابل للمفاصلة]") || isPriceOnInspection()

    fun hasVoiceNote(): Boolean =
        description.contains("[ملاحظة صوتية")

    fun voiceNoteDurationLabel(): String {
        if (!hasVoiceNote()) return ""
        val after = description.substringAfter("[ملاحظة صوتية:", "").substringBefore("]").trim()
        return after.ifEmpty { "00:14 ثانية" }
    }

    fun cleanDescriptionDisplay(): String =
        description
            .replace("[السعر: بالاتفاق بعد المعاينة]", "")
            .replace("[قابل للمفاصلة]", "")
            .replace(Regex("\\[ملاحظة صوتية:[^]]*]"), "")
            .trim()

    fun priceBadgeLabel(): String = when {
        isPriceOnInspection() -> "السعر: يُحدد بالاتفاق مع الحرفي"
        isPriceNegotiable() -> "$budgetJod د.أ (قابل للمفاصلة)"
        else -> "المبلغ المعروض: $budgetJod د.أ"
    }
}

@IgnoreExtraProperties
data class ServiceBooking(
    @DocumentId val id: String = "",
    val userId: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val providerSkill: String = "",
    val providerPhone: String = "",
    val serviceTitle: String = "",
    val governorate: String = "عمّان",
    val neighborhood: String = "خلدا",
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val trackingStage: String = "CONFIRMED", // CONFIRMED, ON_THE_WAY, IN_PROGRESS, COMPLETED
    val trackingEtaMinutes: Long = 18L,
    val escrowAmountJod: Long = 0L,
    val escrowCommissionJod: Long = 0L,
    val escrowStatus: String = "NONE", // NONE, HELD_IN_ESCROW, RELEASED
    val isReviewed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class B2BPartnerOffer(
    val id: String,
    val storeName: String,
    val cityAr: String,
    val discountLabel: String,
    val descriptionAr: String,
    val couponCode: String,
    val categoryAr: String
)
