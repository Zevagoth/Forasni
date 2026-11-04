package com.example.util

import com.example.data.model.ProviderProfile
import com.example.data.model.SkillCategory
import kotlin.math.min

/**
 * محرك البحث الذكي باللهجة الأردنية واللغة العربية لتطبيق «فرصني» (Forasni Smart Search Engine).
 * يجمع بين:
 * 1) تطبيع الحروف العربية وتجريد الكلمات (Arabic Normalization & Light Stemming)
 * 2) شبكة المرادفات الدلالية والمصطلحات الأردنية ووصف الأعطال (Semantic Problem-to-Skill Graph)
 * 3) التسامح مع الأخطاء الإملائية (Fuzzy Matching via Levenshtein Edit Distance)
 * 4) ترتيب النتائج حسب الأهمية وتعدد الكلمات (Multi-Token Weighted Relevance Ranking)
 */
object ArabicSmartSearchEngine {

    data class SmartSearchResult(
        val providers: List<ProviderProfile>,
        val matchedIntentLabel: String? = null
    )

    // قاموس دلالي يربط كلام الزبون باللهجة الأردنية أو وصف المشكلة بالمهنة المناسبة
    private data class SemanticCluster(
        val category: SkillCategory,
        val labelAr: String,
        val keywords: List<String>
    )

    private val semanticClusters: List<SemanticCluster> = listOf(
        SemanticCluster(
            category = SkillCategory.PLUMBER,
            labelAr = "سباكة، موسرجي وتمديدات صحية",
            keywords = listOf(
                "سباك", "سباكة", "موسرجي", "مواسرجي", "مواسير", "ماسورة", "حنفية", "حنفيه",
                "بطارية", "خلاط", "مي", "ميه", "مياه", "تسريب", "تهريب", "تنقيط", "بتنقط",
                "مجاري", "بالوعة", "مصرف", "تسليك", "مسكرة", "انسداد", "خزان", "عوامة",
                "ماتور مي", "مضخة", "جيزر", "كيزر", "سخان", "سيفونات", "تواليت", "حمام",
                "مغسلة", "نيبل", "تمديدات صحية", "تاسيس سباكة", "plumber", "plumbing", "water leak"
            )
        ),
        SemanticCluster(
            category = SkillCategory.ELECTRICIAN,
            labelAr = "كهرباء منازل، قواطع وإنارة",
            keywords = listOf(
                "كهربائي", "كهرباء", "كهربا", "كهربجي", "قاطع", "قواطع", "فيوز", "طبلون",
                "شورت", "تماس", "لمبة", "انارة", "اضاءة", "ثريا", "سبوت", "لد", "ابريز",
                "فيش", "كبسة", "سويتش", "اسلاك", "تمديد كهرباء", "فاز", "ثري فاز", "عداد",
                "انفرتر", "انتركوم", "جرس", "كاميرات مراقبة", "ستلايت", "رسيفر", "صحن",
                "electrician", "electricity", "wiring"
            )
        ),
        SemanticCluster(
            category = SkillCategory.AC_TECH,
            labelAr = "تكييف وتبريد، بويلرات وصيانة أجهزة منزلية",
            keywords = listOf(
                "مكيف", "مكيفات", "تكييف", "كندشن", "كوندشن", "سبليت", "انفرتر", "غاز مكيف",
                "فريون", "تعبئة غاز", "تبريد", "تدفئة", "بويلر", "بويلرات", "حارقة", "رديتر",
                "رديترات", "ديزل", "صوبة", "صوبات", "تنظيف مكيف", "غسيل مكيف", "فك وتركيب مكيف",
                "كمبروسر", "ضاغط", "غسالة", "غسالات", "ثلاجة", "ثلاجات", "فريزر", "فرن",
                "غاز", "جلاية", "نشافة", "مايكروويف", "كولر", "فلتر مي",
                "ac", "air condition", "hvac", "boiler", "heating", "fridge", "washer"
            )
        ),
        SemanticCluster(
            category = SkillCategory.CARPENTER,
            labelAr = "نجارة، فك وتركيب غرف نوم ومطابخ وديكور",
            keywords = listOf(
                "نجار", "نجارة", "خشب", "غرفة نوم", "غرف نوم", "خزانة", "خزائن", "تخت",
                "سرير", "مطبخ", "مطابخ", "المنيوم", "اباجور", "شتر", "شباك", "شبابيك",
                "باب خشب", "مفصلات", "سحاب", "رفوف", "فك وتركيب", "برادي", "ستائر",
                "طاولة", "فرش", "دهين", "دهان", "طراشة", "معجون", "جبسم بورد", "جبس",
                "ديكور", "ورق جدران", "رطوبة", "عزل", "carpenter", "carpentry", "furniture", "kitchen"
            )
        ),
        SemanticCluster(
            category = SkillCategory.BLACKSMITH,
            labelAr = "حدادة، حمايات شبابيك، مظلات وأقفال",
            keywords = listOf(
                "حداد", "حدادة", "حمايات", "حماية شباك", "درابزين", "مظلة", "مظلات",
                "قرميد", "زينكو", "لحام", "باب حديد", "قفل", "اقفال", "مفتاح", "غالون",
                "كالون", "زرفيل", "باب مسكر", "سياج", "هنجر", "blacksmith", "welding", "locksmith"
            )
        ),
        SemanticCluster(
            category = SkillCategory.DRIVER,
            labelAr = "نقل عفش، ديانا مغلقة وتوصيل",
            keywords = listOf(
                "نقل", "عفش", "نقل عفش", "ترحيل", "رحيل", "نقل اثاث", "ديانا", "ونش",
                "تغليف", "كراتين", "عتالة", "شاحنة", "بكب", "فان", "نقل شقة", "سائق",
                "توصيل", "شحن", "moving", "mover", "truck", "driver"
            )
        ),
        SemanticCluster(
            category = SkillCategory.CLEANING,
            labelAr = "شطف عمارات، تنظيف منازل وتعقيم",
            keywords = listOf(
                "تنظيف", "شطف", "عمارة", "درج", "مدخل", "تعقيم", "تلميع", "بلاط",
                "جلي", "جلي بلاط", "رخام", "غسيل كنب", "غسيل سجاد", "موكيت", "شغالة",
                "عاملة", "سيدات", "تنظيف بعد البناء", "تنظيف شقق", "cleaning", "cleaner"
            )
        ),
        SemanticCluster(
            category = SkillCategory.GARDENING,
            labelAr = "تنسيق حدائق، تقليم أشجار وجز عشب",
            keywords = listOf(
                "حديقة", "حدائق", "مزارع", "زراعة", "شجر", "اشجار", "تقليم", "جز",
                "عشب", "نجيل", "ثيل", "عشب صناعي", "شبكة ري", "تنقيط", "رش مبيدات",
                "حشرات", "حجر", "زريعة", "garden", "gardening", "landscaping"
            )
        ),
        SemanticCluster(
            category = SkillCategory.TUTOR,
            labelAr = "تدريس خصوصي، توجيهي وجامعات",
            keywords = listOf(
                "مدرس", "مدرسة", "معلم", "معلمة", "خصوصي", "توجيهي", "رياضيات", "فيزياء",
                "انجليزي", "تأسيس", "دراسة", "حصة", "حصص", "جامعة", "tutor", "teacher"
            )
        ),
        SemanticCluster(
            category = SkillCategory.DESIGNER,
            labelAr = "تصميم جرافيك، مونتاج وريلز",
            keywords = listOf(
                "مصمم", "تصميم", "جرافيك", "شعار", "لوجو", "بوسترات", "مونتاج", "ريلز",
                "سوشيال ميديا", "اعلان", "designer", "graphic", "logo"
            )
        ),
        SemanticCluster(
            category = SkillCategory.TRANSLATOR,
            labelAr = "ترجمة معتمدة وتدقيق لغوي",
            keywords = listOf(
                "مترجم", "مترجمة", "ترجمة", "انجليزي", "فرنسي", "الماني", "ابحاث",
                "توثيق", "translator", "translation"
            )
        )
    )

    // مرادفات المناطق والأحياء الأردنية (عربي / إنجليزي / تهجئات مختلفة)
    private val locationSynonyms: Map<String, List<String>> = mapOf(
        "عمان" to listOf("عمان", "عمّان", "amman"),
        "خلدا" to listOf("خلدا", "خلدة", "khalda"),
        "طبربور" to listOf("طبربور", "tabarbour", "tabarbor"),
        "عبدون" to listOf("عبدون", "abdoun", "abdoon"),
        "الجبيهة" to listOf("جبيهة", "الجبيهة", "الأردنية", "jubeiha"),
        "تلاع العلي" to listOf("تلاع", "تلاع العلي", "شارع وصفي التل", "الجاردنز", "gardens", "tlaa"),
        "مرج الحمام" to listOf("مرج الحمام", "مرج", "البنيات", "ناعور", "marj"),
        "الصويفية" to listOf("صويفية", "الصويفية", "سابع", "الدوار السابع", "sweifieh"),
        "الشميساني" to listOf("شميساني", "الشميساني", "shmeisani"),
        "جبل الحسين" to listOf("جبل الحسين", "حسين", "اللويبدة", "العبدلي"),
        "صويلح" to listOf("صويلح", "صافوط", "sweileh"),
        "اربد" to listOf("اربد", "إربد", "irbid", "الحصن", "الرمثا", "شارع الجامعة"),
        "الزرقاء" to listOf("زرقاء", "الزرقاء", "زرقا", "الرصيفة", "zarqa"),
        "البلقاء" to listOf("سلط", "السلط", "البقعة", "عين الباشا", "الفحيص", "ماحص", "salt"),
        "العقبة" to listOf("عقبة", "العقبة", "aqaba"),
        "مادبا" to listOf("مادبا", "مأدبا", "madaba")
    )

    /**
     * تطبيع النص العربي لتوحيد الهمزات والتاء المربوطة والألف المقصورة والتشكيل
     */
    fun normalizeArabic(input: String): String {
        if (input.isBlank()) return ""
        val sb = StringBuilder(input.length)
        for (ch in input.lowercase()) {
            when (ch) {
                'أ', 'إ', 'آ', 'ٱ' -> sb.append('ا')
                'ة' -> sb.append('ه')
                'ى', 'ئ' -> sb.append('ي')
                'ؤ' -> sb.append('و')
                'ـ', '\u064B', '\u064C', '\u064D', '\u064E', '\u064F', '\u0650', '\u0651', '\u0652' -> {
                    // تجاهل التشكيل والمد
                }
                else -> sb.append(ch)
            }
        }
        return sb.toString()
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * تجريد الكلمة العربية من أل التعريف واللواحق الشائعة للمطابقة الذكية
     */
    fun lightStemArabicWord(word: String): String {
        var w = normalizeArabic(word)
        if (w.length <= 3) return w

        val prefixes = listOf("وال", "بال", "كال", "فال", "لل", "ال")
        for (prefix in prefixes) {
            if (w.startsWith(prefix) && w.length - prefix.length >= 3) {
                w = w.removePrefix(prefix)
                break
            }
        }

        val suffixes = listOf("ات", "ين", "ون", "يه", "ها", "هم", "ان", "ي", "ه")
        for (suffix in suffixes) {
            if (w.endsWith(suffix) && w.length - suffix.length >= 3) {
                w = w.removeSuffix(suffix)
                break
            }
        }
        return w
    }

    /**
     * حساب المسافة الإملائية (Levenshtein Edit Distance) للتسامح مع الأخطاء المطبعية
     */
    fun levenshteinDistance(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)

        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = min(
                    min(curr[j - 1] + 1, prev[j] + 1),
                    prev[j - 1] + cost
                )
            }
            for (j in 0..b.length) {
                prev[j] = curr[j]
            }
        }
        return prev[b.length]
    }

    /**
     * هل الكلمتان متطابقتان إملائياً أو بجذر الكلمة أو بخطأ مطبعي بسيط؟
     */
    private fun tokensFuzzyMatch(queryToken: String, targetToken: String): Boolean {
        if (queryToken.isEmpty() || targetToken.isEmpty()) return false
        if (targetToken.contains(queryToken) || queryToken.contains(targetToken)) return true

        val stemQ = lightStemArabicWord(queryToken)
        val stemT = lightStemArabicWord(targetToken)
        if (stemQ.length >= 3 && stemT.length >= 3) {
            if (stemT.contains(stemQ) || stemQ.contains(stemT)) return true
        }

        if (queryToken.length >= 4 && targetToken.length >= 4) {
            val maxAllowedDistance = if (queryToken.length >= 7) 2 else 1
            if (levenshteinDistance(queryToken, targetToken) <= maxAllowedDistance) return true
            if (stemQ.length >= 4 && stemT.length >= 4 &&
                levenshteinDistance(stemQ, stemT) <= 1
            ) {
                return true
            }
        }
        return false
    }

    /**
     * تنفيذ البحث الذكي وترتيب الحرفيين حسب درجة التطابق الدلالي واللغوي
     */
    fun searchAndRankProviders(
        providers: List<ProviderProfile>,
        rawQuery: String
    ): SmartSearchResult {
        val normalizedQuery = normalizeArabic(rawQuery)
        if (normalizedQuery.isEmpty()) {
            return SmartSearchResult(providers = providers, matchedIntentLabel = null)
        }

        val queryTokens = normalizedQuery.split(" ").filter { it.isNotBlank() }

        val detectedCategories = mutableSetOf<SkillCategory>()
        val detectedLabels = mutableListOf<String>()

        for (cluster in semanticClusters) {
            val matchedCluster = cluster.keywords.any { kw ->
                val normKw = normalizeArabic(kw)
                normalizedQuery.contains(normKw) ||
                    queryTokens.any { token ->
                        token.length >= 2 && normKw.split(" ").any { kwPart ->
                            tokensFuzzyMatch(token, kwPart)
                        }
                    }
            }
            if (matchedCluster) {
                detectedCategories.add(cluster.category)
                if (cluster.labelAr !in detectedLabels) {
                    detectedLabels.add(cluster.labelAr)
                }
            }
        }

        val expandedLocationTerms = mutableSetOf<String>()
        for ((canonicalLoc, synonyms) in locationSynonyms) {
            val normCanonical = normalizeArabic(canonicalLoc)
            val matchesLoc = synonyms.any { syn ->
                val normSyn = normalizeArabic(syn)
                normalizedQuery.contains(normSyn) ||
                    queryTokens.any { token -> token.length >= 3 && tokensFuzzyMatch(token, normSyn) }
            }
            if (matchesLoc) {
                expandedLocationTerms.add(normCanonical)
                synonyms.forEach { expandedLocationTerms.add(normalizeArabic(it)) }
            }
        }

        val wantsEmergency = queryTokens.any {
            it in listOf("طواري", "طوارئ", "فزعه", "فزعة", "مستعجل", "بالليل", "24")
        }
        val wantsFemale = queryTokens.any {
            it in listOf("سيدات", "ستات", "نسائي", "امراه", "صبيه", "معلمه", "عائلات", "ام")
        }

        val scoredProviders = providers.mapNotNull { provider ->
            var score = 0

            val normName = normalizeArabic(provider.name)
            val normSkill = normalizeArabic(provider.skillTitle)
            val normBio = normalizeArabic(provider.bio)
            val normGov = normalizeArabic(provider.governorate)
            val normNeigh = normalizeArabic(provider.neighborhood)
            val normVehicle = normalizeArabic(provider.vehicleModelDisplay())

            val allProviderText = "$normName $normSkill $normBio $normGov $normNeigh $normVehicle"
            val providerTokens = allProviderText.split(" ").filter { it.isNotBlank() }

            if (normName.contains(normalizedQuery)) score += 120
            if (normSkill.contains(normalizedQuery)) score += 110
            if (normNeigh.contains(normalizedQuery) || normGov.contains(normalizedQuery)) score += 90
            if (normBio.contains(normalizedQuery)) score += 70

            if (detectedCategories.any { it.id == provider.skillCategory }) {
                score += 85
            }

            if (expandedLocationTerms.isNotEmpty()) {
                val locMatch = expandedLocationTerms.any { loc ->
                    normGov.contains(loc) || normNeigh.contains(loc)
                }
                if (locMatch) score += 65
            }

            if (wantsEmergency && provider.isEmergency24hResponder) {
                score += 75
            }
            if (wantsFemale && provider.isFemaleProvider) {
                score += 80
            }

            var matchedTokenCount = 0
            for (qToken in queryTokens) {
                val directOrFuzzy = providerTokens.any { pToken ->
                    tokensFuzzyMatch(qToken, pToken)
                }
                val categoryOrLocMatch =
                    detectedCategories.any { it.id == provider.skillCategory } ||
                        expandedLocationTerms.any { normGov.contains(it) || normNeigh.contains(it) }

                if (directOrFuzzy) {
                    matchedTokenCount++
                    score += 45
                } else if (categoryOrLocMatch) {
                    matchedTokenCount++
                    score += 25
                }
            }

            if (queryTokens.size > 1 && matchedTokenCount == queryTokens.size) {
                score += 60
            }

            if (score > 0) {
                val bonus = (if (provider.isAvailableNow) 8 else 0) +
                    (if (provider.isVerified) 6 else 0) +
                    provider.ratingAvg.toInt()
                Pair(provider, score + bonus)
            } else {
                null
            }
        }

        val rankedList = scoredProviders
            .sortedByDescending { it.second }
            .map { it.first }

        val intentSummary = when {
            detectedLabels.isNotEmpty() -> detectedLabels.joinToString(" • ")
            wantsEmergency -> "🚨 استجابة طوارئ وفزعة 24 ساعة"
            wantsFemale -> "👩‍💼 قسم السيدات والعائلات"
            else -> null
        }

        return SmartSearchResult(
            providers = rankedList,
            matchedIntentLabel = intentSummary
        )
    }
}
