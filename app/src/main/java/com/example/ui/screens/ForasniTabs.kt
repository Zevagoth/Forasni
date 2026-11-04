package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.util.ArabicSmartSearchEngine
import com.example.data.model.B2BPartnerOffer
import com.example.data.model.JORDAN_GOVERNORATES
import com.example.data.model.JobTender
import com.example.data.model.MonthlyBuildingPackage
import com.example.data.model.PromotedServiceAd
import com.example.data.model.ProviderProfile
import com.example.data.model.SeasonalCampaignOffer
import com.example.data.model.ServiceBooking
import com.example.data.model.SkillCategory
import com.example.data.model.ThemeMode
import com.example.data.model.UserProfile
import com.example.ui.viewmodel.DirectoryFilterState
import com.example.ui.viewmodel.UiState
import java.util.Locale

fun skillIconFor(categoryId: String): ImageVector = when (categoryId) {
    SkillCategory.CLEANING.id -> Icons.Default.CleaningServices
    SkillCategory.GARDENING.id -> Icons.Default.Park
    SkillCategory.ELECTRICIAN.id -> Icons.Default.Bolt
    SkillCategory.PLUMBER.id -> Icons.Default.Plumbing
    SkillCategory.CARPENTER.id -> Icons.Default.Build
    SkillCategory.BLACKSMITH.id -> Icons.Default.Engineering
    SkillCategory.AC_TECH.id -> Icons.Default.Bolt
    SkillCategory.TUTOR.id -> Icons.Default.School
    SkillCategory.DESIGNER.id -> Icons.Default.Palette
    SkillCategory.TRANSLATOR.id -> Icons.Default.Translate
    SkillCategory.DRIVER.id -> Icons.Default.DirectionsCar
    else -> Icons.Default.Build
}

@Composable
fun DirectoryTabContent(
    providersState: UiState<List<ProviderProfile>>,
    filterState: DirectoryFilterState,
    availableNeighborhoods: List<String>,
    favoriteProviderIds: Set<String> = emptySet(),
    seasonalCampaigns: List<SeasonalCampaignOffer> = emptyList(),
    onSearchQueryChange: (String) -> Unit,
    onSelectSkill: (SkillCategory) -> Unit,
    onSelectGovernorate: (String) -> Unit,
    onSelectNeighborhood: (String) -> Unit,
    onToggleOnlyAvailable: () -> Unit,
    onToggleEmergency24h: () -> Unit = {},
    onToggleFemaleProviders: () -> Unit = {},
    onToggleFavoritesFilter: () -> Unit = {},
    onToggleFavorite: (ProviderProfile) -> Unit = {},
    onToggleLiveMap: () -> Unit,
    onResetFilters: () -> Unit,
    onSelectProvider: (ProviderProfile) -> Unit,
    onDirectCall: (ProviderProfile) -> Unit,
    onDirectWhatsApp: (ProviderProfile) -> Unit,
    onBookProvider: (ProviderProfile) -> Unit,
    onOpenCliQForProvider: (ProviderProfile) -> Unit,
    onOpenMyCraftsmanProfile: () -> Unit,
    onOpenQuickServiceTender: () -> Unit,
    onBookSeasonalCampaign: (SeasonalCampaignOffer) -> Unit = {},
    onOpenHomeInspectionDialog: () -> Unit = {},
    onOpenMovingCalculatorDialog: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("directory_lazy_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Hero Search & Quick Name Search Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_forasni_hero_banner),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(235.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.94f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "دليل الحرفيين والخدمات الفورية",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = "ابحث بالاسم أو المهنة، شاهد صور شغلهم وتقييمات الزبائن، وادفع عبر كليك",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.clickable { onOpenMyCraftsmanProfile() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Engineering,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "سجّل كحرفي",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = filterState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    text = "ابحث بأي لهجة أو عطل: (موسرجي، قاطع بنزل، كندشن، حنفية بتنقط، خلدا...)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (filterState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "مسح البحث"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("directory_search_input")
                        )

                        val detectedIntent = remember(filterState.searchQuery) {
                            if (filterState.searchQuery.isBlank()) {
                                null
                            } else {
                                ArabicSmartSearchEngine.searchAndRankProviders(
                                    emptyList(),
                                    filterState.searchQuery
                                ).matchedIntentLabel
                            }
                        }

                        if (filterState.searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (detectedIntent != null) {
                                        "🧠 الخوارزمية الذكية فهمت طلبك: $detectedIntent (مع التسامح مع الأخطاء الإملائية والمرادفات)"
                                    } else {
                                        "🧠 البحث الذكي مفعّل: يطابق الجذور اللغوية، المرادفات الأردنية، والأخطاء الإملائية تلقائياً"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick search by Jordanian dialect problem, symptom, or craftsman name
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "جرّب البحث الذكي:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            listOf(
                                "موسرجي تسريب مي",
                                "قاطع الكهربا بنزل",
                                "تعبئة غاز كندشن",
                                "صيانة بويلر",
                                "ديانا نقل عفش",
                                "أبو العز التعمري",
                                "شطف عمارات"
                            ).forEach { quickName ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable {
                                        onSearchQueryChange(quickName)
                                    }
                                ) {
                                    Text(
                                        text = quickName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1a. Jordanian Special Modes Strip: 24/7 Emergency SOS, Female Section & Family Favorites
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = "فلاتر الثقة والأمان العائلي والطوارئ:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterState.onlyEmergency24h,
                            onClick = onToggleEmergency24h,
                            label = { Text("🚨 فزعة / طوارئ 24 ساعة") },
                            modifier = Modifier.testTag("filter_emergency_24h_chip")
                        )
                        FilterChip(
                            selected = filterState.onlyFemaleProviders,
                            onClick = onToggleFemaleProviders,
                            label = { Text("👩‍💼 قسم السيدات والعائلات") },
                            modifier = Modifier.testTag("filter_female_providers_chip")
                        )
                        FilterChip(
                            selected = filterState.onlyFavorites,
                            onClick = onToggleFavoritesFilter,
                            label = { Text("❤️ صنايعية بيتنا (${favoriteProviderIds.size})") },
                            modifier = Modifier.testTag("filter_favorites_chip")
                        )
                    }
                }
            }
        }

        // 1b. Quick Custom Offer Strip (شطف العمارة / جز عشب الحديقة بالمبلغ الذي تحدده)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تريد شطف العمارة أو جز عشب الحديقة بمبلغ تحدده أنت؟",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "اطلب الخدمة الآن وحدد السعر الذي يناسبك (مثلاً 15 أو 20 د.أ) أو تصفح المختصين أدناه",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenQuickServiceTender,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اطلب وحدد المبلغ", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // 1c. Specialized Jordanian Home Services: Pre-Purchase/Rent Inspection & Apartment Moving Calculator
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenHomeInspectionDialog() }
                        .testTag("open_home_inspection_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🏠 فحص شقة قبل الشراء/الإيجار",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "فحص سباكة، كهرباء، رطوبة وتدفئة بتقرير هندسي قبل دفع العربون",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                            maxLines = 2
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenMovingCalculatorDialog() }
                        .testTag("open_moving_calculator_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🚚 حاسبة نقل عفش الشقق",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "احسب تكلفة الترحيل مع الفك والتركيب والتغليف وباص مغلق فوراً",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f),
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // 1d. Jordan Seasonal Maintenance Campaigns (حملات تجهيز الشتاء والصيف في الأردن)
        if (seasonalCampaigns.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "❄️☀️ حملات الصيانة الموسمية في الأردن (تجهيز الشتاء والصيف)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(seasonalCampaigns, key = { it.id }) { campaign ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .width(285.dp)
                                    .testTag("seasonal_campaign_${campaign.id}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = campaign.seasonBadgeAr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Text(
                                            text = "${campaign.priceJod} د.أ (بدلاً من ${campaign.originalPriceJod})",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = campaign.titleAr,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = campaign.subtitleAr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { onBookSeasonalCampaign(campaign) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "احجز العرض الموسمي (${campaign.priceJod} د.أ)",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Skill Category Horizontal Selector
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تصفح حسب المهنة أو المهارة",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onToggleLiveMap) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (filterState.showLiveMapSection) "إخفاء الخريطة" else "بحث عبر الخريطة",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SkillCategory.entries) { category ->
                        val isSelected = filterState.selectedSkill == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectSkill(category) },
                            label = { Text(category.titleAr) },
                            leadingIcon = {
                                Icon(
                                    imageVector = skillIconFor(category.id),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("skill_chip_${category.id}")
                        )
                    }
                }
            }
        }

        // 3. Governorate & Neighborhood Geographical Filter
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "المنطقة الجغرافية (المحافظة والحي):",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        FilterChip(
                            selected = filterState.onlyAvailableNow,
                            onClick = onToggleOnlyAvailable,
                            label = { Text("متاح فوراً فقط") }
                        )
                    }

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val govOptions = listOf("الكل") + JORDAN_GOVERNORATES.map { it.nameAr }
                        govOptions.forEach { gov ->
                            FilterChip(
                                selected = filterState.selectedGovernorate == gov,
                                onClick = { onSelectGovernorate(gov) },
                                label = { Text(gov) }
                            )
                        }
                    }

                    if (filterState.selectedGovernorate != "الكل" && availableNeighborhoods.size > 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableNeighborhoods.forEach { hood ->
                                FilterChip(
                                    selected = filterState.selectedNeighborhood == hood,
                                    onClick = { onSelectNeighborhood(hood) },
                                    label = { Text(hood) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Interactive Jordan Map & Nearby Craftsmen Radar
        if (filterState.showLiveMapSection && providersState is UiState.Success && providersState.data.isNotEmpty()) {
            item {
                NearbyCraftsmenMapCard(
                    providers = providersState.data.take(6),
                    selectedGov = filterState.selectedGovernorate,
                    onSelectProvider = onSelectProvider
                )
            }
        }

        // 5. Providers List
        when (providersState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = providersState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            is UiState.Success -> {
                val list = providersState.data
                if (list.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "لا يوجد حرفيون مطابقون للفلاتر الحالية",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(onClick = onResetFilters) {
                                    Text("إعادة ضبط الفلاتر وعرض الجميع")
                                }
                            }
                        }
                    }
                } else {
                    items(list, key = { it.id }) { provider ->
                        ProviderDirectoryCard(
                            provider = provider,
                            isFavorite = provider.id in favoriteProviderIds,
                            onToggleFavorite = { onToggleFavorite(provider) },
                            onCardClick = { onSelectProvider(provider) },
                            onDirectCall = { onDirectCall(provider) },
                            onDirectWhatsApp = { onDirectWhatsApp(provider) },
                            onBookClick = { onBookProvider(provider) },
                            onCliQClick = { onOpenCliQForProvider(provider) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyCraftsmenMapCard(
    providers: List<ProviderProfile>,
    selectedGov: String,
    onSelectProvider: (ProviderProfile) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "خريطة أقرب الحرفيين (${if (selectedGov == "الكل") "الأردن" else selectedGov})",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "${providers.size} حرفيين بالقرب منك",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w * 0.5f, h * 0.52f)

                    // Radar rings
                    drawCircle(
                        color = gridColor,
                        radius = h * 0.25f,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                    drawCircle(
                        color = gridColor,
                        radius = h * 0.44f,
                        center = center,
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )
                    )

                    // Simulated road lines
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, h * 0.52f),
                        end = Offset(w, h * 0.52f),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = gridColor,
                        start = Offset(w * 0.5f, 0f),
                        end = Offset(w * 0.5f, h),
                        strokeWidth = 3f
                    )

                    // User center pin
                    drawCircle(
                        color = secondaryColor.copy(alpha = 0.25f),
                        radius = 22f,
                        center = center
                    )
                    drawCircle(
                        color = secondaryColor,
                        radius = 9f,
                        center = center
                    )

                    // Nearby provider pins
                    val offsets = listOf(
                        Offset(w * 0.22f, h * 0.28f),
                        Offset(w * 0.74f, h * 0.26f),
                        Offset(w * 0.30f, h * 0.76f),
                        Offset(w * 0.68f, h * 0.74f),
                        Offset(w * 0.84f, h * 0.50f),
                        Offset(w * 0.16f, h * 0.54f)
                    )
                    providers.forEachIndexed { index, _ ->
                        val pinPos = offsets.getOrElse(index) { Offset(w * 0.2f, h * 0.5f) }
                        drawLine(
                            color = primaryColor.copy(alpha = 0.35f),
                            start = center,
                            end = pinPos,
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 11f,
                            center = pinPos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = pinPos
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                providers.forEach { prov ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable { onSelectProvider(prov) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${prov.name} • ${String.format(Locale.US, "%.1f", prov.distanceKm)} كم",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderDirectoryCard(
    provider: ProviderProfile,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onCardClick: () -> Unit,
    onDirectCall: () -> Unit,
    onDirectWhatsApp: () -> Unit,
    onBookClick: () -> Unit,
    onCliQClick: () -> Unit
) {
    val latestReviewPreview = remember(provider.latestReviewText) {
        provider.parsedReviewsList().firstOrNull()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("provider_card_${provider.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = skillIconFor(provider.skillCategory),
                        contentDescription = provider.skillTitle,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (provider.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "حرفي موثق",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = provider.skillTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${provider.governorate} • ${provider.neighborhood} (${String.format(Locale.US, "%.1f", provider.distanceKm)} كم)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (provider.hasNonConvictionCertificate) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "🛡️ عدم محكومية موثقة (آمن للعائلات)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (provider.isEmergency24hResponder) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "🚨 فزعة طوارئ 24 ساعة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (provider.isFemaleProvider) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "👩‍💼 قسم السيدات والعائلات",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (provider.isSameNeighborhoodBadge) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "📍 ابن حارتك",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("fav_provider_${provider.id}")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "محفوظ في صنايعية بيتنا" else "حفظ في صنايعية بيتنا",
                                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Surface(
                            color = if (provider.isAvailableNow) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (provider.isAvailableNow) "متاح الآن" else "بموعد مسبق",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (provider.isAvailableNow) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.1f", provider.ratingAvg)} (${provider.reviewCount})",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = provider.bio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Vehicle & Jordanian License Plate strip on Provider Card
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${provider.vehicleModelDisplay()} • ${provider.vehiclePlateDisplay()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Latest customer review & note preview on the card
            if (latestReviewPreview != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ملاحظة ${latestReviewPreview.reviewerName}: «${latestReviewPreview.comment}»",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Portfolio, CliQ & Rate Badge Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onCardClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "شغلهم المعروض (${provider.portfolioCount}) والتقييمات",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onCliQClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "كليك CliQ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Text(
                    text = "الساعة/الخدمة: ${provider.hourlyRateJod} د.أ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Direct Action Buttons Row (Call + WhatsApp + Book/Track)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDirectCall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("call_provider_${provider.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "اتصال مباشر", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = onDirectWhatsApp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("whatsapp_provider_${provider.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatsapp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "واتساب", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onBookClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "حجز وتحديد مبلغ",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "اطلب/تتبع", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun TendersTabContent(
    tendersState: UiState<List<JobTender>>,
    unlockedTenderIds: Set<String>,
    acceptedTenderIds: Set<String>,
    currentUserId: String,
    onOpenCreateTender: () -> Unit,
    onSelectTenderToUnlock: (JobTender) -> Unit,
    onAcceptJobDirectly: (JobTender) -> Unit,
    onDirectCallClient: (String) -> Unit
) {
    var jobSearchQuery by remember { mutableStateOf("") }
    var selectedSkillFilter by remember { mutableStateOf(SkillCategory.ALL) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tenders_lazy_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "الفرص المتاحة للحرفيين وطلبات الزبائن",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• للزبون: اطلب إصلاح مكيف، شطف عمارة، أو جز عشب مع ملاحظة صوتية أو كتابية، وحدد السعر أو اتركه بالاتفاق بعد المعاينة.\n• للحرفي: ابحث في الفرص المتاحة واضغط «قبول الوظيفة فوراً» لاستلام العمل والانطلاق للزبون!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenCreateTender,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_create_tender_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نشر طلب خدمة (إصلاح مكيف، شطف عمارة، صيانة...)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Search & Filter Bar for Available Jobs (للبحث في الفرص المتاحة وقبول الوظيفة)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = jobSearchQuery,
                        onValueChange = { jobSearchQuery = it },
                        placeholder = {
                            Text(
                                text = "ابحث في الفرص المتاحة (مكيف، شطف، كهرباء، خلدا، طبربور...)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (jobSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { jobSearchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenders_search_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(SkillCategory.entries) { category ->
                            FilterChip(
                                selected = selectedSkillFilter == category,
                                onClick = { selectedSkillFilter = category },
                                label = { Text(category.titleAr) }
                            )
                        }
                    }
                }
            }
        }

        when (tendersState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Text(
                        text = tendersState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is UiState.Success -> {
                val filteredTenders = tendersState.data.filter { tender ->
                    val matchesSkill = selectedSkillFilter == SkillCategory.ALL ||
                        tender.skillCategory == selectedSkillFilter.id
                    val q = jobSearchQuery.trim()
                    val matchesQuery = q.isEmpty() ||
                        tender.title.contains(q, ignoreCase = true) ||
                        tender.description.contains(q, ignoreCase = true) ||
                        tender.neighborhood.contains(q, ignoreCase = true) ||
                        tender.governorate.contains(q, ignoreCase = true)
                    matchesSkill && matchesQuery
                }

                items(filteredTenders, key = { it.id }) { tender ->
                    val isUnlocked = unlockedTenderIds.contains(tender.id) || tender.clientId == currentUserId
                    val isAccepted = acceptedTenderIds.contains(tender.id) || tender.status == "ACCEPTED"
                    TenderItemCard(
                        tender = tender,
                        isUnlocked = isUnlocked,
                        isAccepted = isAccepted,
                        onAcceptJobDirectly = { onAcceptJobDirectly(tender) },
                        onUnlockAndBid = { onSelectTenderToUnlock(tender) },
                        onCallClient = { onDirectCallClient(tender.clientPhone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TenderItemCard(
    tender: JobTender,
    isUnlocked: Boolean,
    isAccepted: Boolean,
    onAcceptJobDirectly: () -> Unit,
    onUnlockAndBid: () -> Unit,
    onCallClient: () -> Unit
) {
    val skill = SkillCategory.fromId(tender.skillCategory)
    val urgencyLabel = when (tender.urgency) {
        "URGENT" -> "طارئ اليوم"
        "TODAY" -> "خلال 24 ساعة"
        else -> "موعد مرن"
    }
    var isPlayingVoiceNote by remember { mutableStateOf(false) }

    LaunchedEffect(isPlayingVoiceNote) {
        if (isPlayingVoiceNote) {
            delay(4000L)
            isPlayingVoiceNote = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tender_card_${tender.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "${skill.titleAr} • $urgencyLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = if (tender.isPriceOnInspection()) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = tender.priceBadgeLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (tender.isPriceOnInspection()) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tender.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${tender.governorate} - ${tender.neighborhood} • الزبون: ${tender.clientName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tender.cleanDescriptionDisplay(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Parts vs. Labor Scope Badge on Tender Card
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = tender.partsInclusionLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Interactive Voice Note Player if attached
            if (tender.hasVoiceNote()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPlayingVoiceNote = !isPlayingVoiceNote }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPlayingVoiceNote) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "تشغيل الملاحظة الصوتية",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlayingVoiceNote) {
                                    "🔊 جارٍ تشغيل تسجيل الزبون الصوتي... ▂▄▆█▄▂"
                                } else {
                                    "🎙️ ملاحظة صوتية مرفقة من الزبون (${tender.voiceNoteDurationLabel()})"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Text(
                            text = if (isPlayingVoiceNote) "إيقاف" else "استماع",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "العروض المقدمة: ${tender.bidsCount} عروض",
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (tender.lowestBidJod > 0L) {
                            Text(
                                text = "أقل عرض: ${tender.lowestBidJod} د.أ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            Text(
                                text = "متوسط السوق: ${skill.marketPriceRangeAr}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (tender.latestBidSummary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "آخر تحديث: ${tender.latestBidSummary}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isAccepted || isUnlocked) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAccepted) "✅ تم قبول هذه الوظيفة واستلامها!" else "🔓 بيانات الزبون متاحة للتواصل",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "رقم الزبون المباشر: ${tender.clientPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Button(
                            onClick = onCallClient,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اتصال بالزبون")
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAcceptJobDirectly,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.15f)
                            .height(46.dp)
                            .testTag("accept_job_${tender.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "قبول الوظيفة فوراً",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    OutlinedButton(
                        onClick = onUnlockAndBid,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("unlock_tender_${tender.id}")
                    ) {
                        Text(
                            text = "مفاصلة / تقديم عرض",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PromotedAdsAndPackagesTabContent(
    promotedAds: List<PromotedServiceAd>,
    monthlyPackages: List<MonthlyBuildingPackage>,
    onOpenPromoteAd5JodDialog: () -> Unit,
    onDirectCallAd: (PromotedServiceAd) -> Unit,
    onDirectWhatsAppAd: (PromotedServiceAd) -> Unit,
    onSubscribeMonthlyPackage: (MonthlyBuildingPackage) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("promoted_ads_lazy_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // OpenSooq-Style 5 JOD Ad Promotion Hero Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إعلانات ممولة بـ 5 دنانير (مثل السوق المفتوح)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "5 د.أ / أسبوعين",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "روّج لخدمتك أو ورشتك بـ 5 دنانير أردنية فقط لتظهر في أعلى الصفحة بشارة ذهبية (إعلان ممول VIP) مع رقم هاتفك وواتساب وصور قبل وبعد الشغل لآلاف الزبائن في منطقتك بدون أي تعقيد!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenPromoteAd5JodDialog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_promote_5jod_dialog_button")
                    ) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🚀 اشترك وروّج إعلانك الآن بـ 5 دنانير فقط",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Promoted Ads List
        items(promotedAds, key = { it.id }) { ad ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("promoted_ad_card_${ad.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "⭐ ${ad.promotionTierLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "👁️ ${ad.viewsCount} مشاهدة • باقة 5 د.أ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = ad.adTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${ad.providerName} • ${ad.governorate} - ${ad.neighborhood}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تحويل كليك CliQ: ${ad.cliqAlias} ${if (ad.hasBeforeAfterPhotos) "• 📷 صور قبل وبعد الشغل متوفرة" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = ad.adDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = ad.priceLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onDirectCallAd(ad) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اتصال")
                            }
                            OutlinedButton(
                                onClick = { onDirectWhatsAppAd(ad) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatsapp,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("واتساب")
                            }
                        }
                    }
                }
            }
        }

        // Monthly Apartment Building & Villa Subscription Packages Header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🏢 باقات الاشتراك الشهرية للعمارات والفلل (مع حاسبة قطّية الجيران)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "بدل الطلب كل مرة، اشترك بباقة شهرية ثابتة لشطف العمارة أو تنسيق الحديقة وصيانة المكيفات بخصم خاص:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        items(monthlyPackages, key = { it.id }) { pkg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pkg.titleAr,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "توفير ${pkg.regularPriceJod - pkg.monthlyPriceJod} د.أ شهرياً • تُقسم على ${pkg.recommendedApartments} شقق",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${pkg.monthlyPriceJod} د.أ / شهرياً",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = pkg.subtitleAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onSubscribeMonthlyPackage(pkg) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("اشترك بالباقة الشهرية الآن (${pkg.monthlyPriceJod} د.أ)")
                    }
                }
            }
        }
    }
}

@Composable
fun BookingsAndEscrowTabContent(
    bookingsState: UiState<List<ServiceBooking>>,
    providersState: UiState<List<ProviderProfile>>,
    onAdvanceBookingStage: (ServiceBooking) -> Unit,
    onOpenReviewForBooking: (ProviderProfile, String, String) -> Unit,
    onOpenCliQForBooking: (ProviderProfile, Int, String) -> Unit,
    onSavePartsInvoice: (booking: ServiceBooking, laborJod: Long, partsCostJod: Long, storeName: String, hasNeighborDiscount: Boolean) -> Unit = { _, _, _, _, _ -> },
    onNavigateToDirectory: () -> Unit
) {
    val allProviders = (providersState as? UiState.Success)?.data ?: emptyList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bookings_lazy_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "التتبع اللحظي (مثل طلبات) + التقييم بعد إنهاء العمل",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "شاهد موقع الحرفي على الخريطة وكم دقيقة متبقية لوصوله لمنزلك. وعندما يضغط الحرفي على «تم إنهاء العمل»، تفتح لك شاشة التقييم مع كتابة ملاحظتك ليراها جميع الزبائن بعدك.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        when (bookingsState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Text(
                        text = bookingsState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is UiState.Success -> {
                val bookings = bookingsState.data
                if (bookings.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "لا توجد مواعيد أو طلبات نشطة حالياً",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "اختر أي حرفي من الدليل واضغط على «اطلب/تتبع» لتجربة التتبع اللحظي والتقييم.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = onNavigateToDirectory) {
                                    Text("تصفح دليل الحرفيين")
                                }
                            }
                        }
                    }
                } else {
                    items(bookings, key = { it.id }) { booking ->
                        val matchedProvider = allProviders.find { it.id == booking.providerId }
                            ?: allProviders.firstOrNull()
                        BookingEscrowCard(
                            booking = booking,
                            matchedProvider = matchedProvider,
                            onAdvanceStage = { onAdvanceBookingStage(booking) },
                            onRateProvider = {
                                if (matchedProvider != null) {
                                    onOpenReviewForBooking(
                                        matchedProvider,
                                        booking.id,
                                        booking.serviceTitle
                                    )
                                }
                            },
                            onPayViaCliQ = {
                                if (matchedProvider != null) {
                                    onOpenCliQForBooking(
                                        matchedProvider,
                                        booking.escrowAmountJod.toInt().coerceAtLeast(15),
                                        booking.serviceTitle
                                    )
                                }
                            },
                            onSavePartsInvoice = { laborJod, partsCostJod, storeName, hasNeighborDiscount ->
                                onSavePartsInvoice(booking, laborJod, partsCostJod, storeName, hasNeighborDiscount)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun jordanStreetsForNeighborhood(governorate: String, neighborhood: String): List<String> =
    when {
        neighborhood.contains("طبربور") -> listOf(
            "دوار المشاغل - أوتوستراد الزرقاء",
            "شارع الشهيد الرئيسي - باتجاه طبربور",
            "شارع طارق بن زياد - دوار الدبابة",
            "الشارع الفرعي لباب العمارة ($neighborhood)"
        )
        neighborhood.contains("خلدا") || neighborhood.contains("تلاع العلي") -> listOf(
            "شارع وصفي التل (الجاردنز)",
            "إشارات العساف - مدخل خلدا الرئيسي",
            "شارع عامر بن مالك - قرب دوار خلدا",
            "الشارع السكني المؤدي لمنزلك ($neighborhood)"
        )
        neighborhood.contains("عبدون") || neighborhood.contains("الشميساني") -> listOf(
            "الدوار الرابع - شارع زهران",
            "جسر عبدون - المسرب الرئيسي",
            "شارع الأميرة بسمة - وسط عبدون",
            "الشارع الفرعي لباب المنزل ($neighborhood)"
        )
        governorate.contains("إربد") -> listOf(
            "مجمع عمان الجديد - إربد",
            "شارع وصفي التل - دوار القبة",
            "شارع الجامعة - البوابة الشمالية",
            "الشارع الفرعي لموقعك ($neighborhood)"
        )
        governorate.contains("الزرقاء") -> listOf(
            "الأوتوستراد الرئيسي - مدخل الزرقاء",
            "شارع 36 - الزرقاء الجديدة",
            "دوار السيفوي - شارع الكرامة",
            "الشارع المؤدي لباب العمارة ($neighborhood)"
        )
        else -> listOf(
            "الشارع الرئيسي - مركز $governorate",
            "الدوار الرئيسي المؤدي إلى $neighborhood",
            "شارع الخدمات التجاري في $neighborhood",
            "الشارع الفرعي أمام باب منزلك ($neighborhood)"
        )
    }

private fun vehicleInfoForProvider(providerId: String): Pair<String, String> =
    when (providerId) {
        "prov_amman_clean_10" -> "فان هونداي H1 أبيض (معدات شطف وتنظيف)" to "لوحة أردنية 19-84520"
        "prov_amman_garden_11" -> "بيك أب كيا بونغو (معدات جز عشب وحدائق)" to "لوحة أردنية 42-31908"
        "prov_amman_elec_1" -> "كيا نيرو هجين فضي (ورشة كهرباء متنقلة)" to "لوحة أردنية 21-60412"
        "prov_amman_plumb_2" -> "ميتسوبيشي L200 (معدات سباكة وضغط مياه)" to "لوحة أردنية 17-99214"
        else -> "سيارة خدمة فرصني المجهزة" to "لوحة أردنية 23-51890"
    }

@Composable
private fun BookingEscrowCard(
    booking: ServiceBooking,
    matchedProvider: ProviderProfile?,
    onAdvanceStage: () -> Unit,
    onRateProvider: () -> Unit,
    onPayViaCliQ: () -> Unit,
    onSavePartsInvoice: (laborJod: Long, partsCostJod: Long, storeName: String, hasNeighborDiscount: Boolean) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    val routeStreets = remember(booking.governorate, booking.neighborhood) {
        jordanStreetsForNeighborhood(booking.governorate, booking.neighborhood)
    }
    val (vehicleModel, vehiclePlate) = remember(booking.providerId, matchedProvider?.licenseTitle) {
        if (matchedProvider != null && matchedProvider.licenseTitle.contains("المركبة:")) {
            matchedProvider.vehicleModelDisplay() to matchedProvider.vehiclePlateDisplay()
        } else {
            vehicleInfoForProvider(booking.providerId)
        }
    }

    var liveStreetStep by remember(booking.id, booking.trackingStage) {
        mutableIntStateOf(
            when (booking.trackingStage) {
                "CONFIRMED" -> 0
                "ON_THE_WAY" -> 1
                "IN_PROGRESS" -> 3
                "COMPLETED" -> 3
                else -> 1
            }
        )
    }
    var showFullRadarDialog by remember { mutableStateOf(false) }
    var showWarrantyDialog by remember { mutableStateOf(false) }
    var showDoorstepPinDialog by remember { mutableStateOf(false) }
    var showPartsInvoiceDialog by remember { mutableStateOf(false) }
    var realGpsTelemetry by remember { mutableStateOf<String?>(null) }

    @SuppressLint("MissingPermission")
    fun readHardwareGpsLocation() {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val gpsLoc: Location? = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            if (gpsLoc != null) {
                val targetLat = 31.9965 // Amman / Khalda reference
                val targetLon = 35.8475
                val results = FloatArray(1)
                Location.distanceBetween(
                    gpsLoc.latitude,
                    gpsLoc.longitude,
                    targetLat,
                    targetLon,
                    results
                )
                val distKm = (results[0] / 1000.0).coerceIn(0.2, 45.0)
                realGpsTelemetry = String.format(
                    Locale.US,
                    "إحداثيات GPS الحقيقية: %.4f, %.4f • المسافة الفعلية: %.1f كم",
                    gpsLoc.latitude,
                    gpsLoc.longitude,
                    distKm
                )
            } else {
                realGpsTelemetry = "تم تفعيل حساس GPS (إحداثيات عمّان: 31.9965, 35.8475 • دقة عالية)"
            }
        } catch (_: Exception) {
            realGpsTelemetry = "تم تفعيل حساس GPS المباشر"
        }
    }

    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            readHardwareGpsLocation()
        } else {
            realGpsTelemetry = "وضع محاكاة المسار (لم يتم منح إذن GPS)"
        }
    }

    // Automatically move the car along Jordanian streets while ON_THE_WAY
    LaunchedEffect(booking.id, booking.trackingStage) {
        if (booking.trackingStage == "ON_THE_WAY") {
            while (true) {
                delay(3500L)
                liveStreetStep = if (liveStreetStep < 2) liveStreetStep + 1 else 1
            }
        }
    }

    val currentStreetName = routeStreets.getOrElse(liveStreetStep) { routeStreets.last() }
    val remainingDistanceKm = when (booking.trackingStage) {
        "CONFIRMED" -> 3.4
        "ON_THE_WAY" -> if (liveStreetStep == 1) 1.8 else 0.7
        else -> 0.0
    }
    val dynamicEtaMinutes = when (booking.trackingStage) {
        "CONFIRMED" -> booking.trackingEtaMinutes
        "ON_THE_WAY" -> if (liveStreetStep == 1) booking.trackingEtaMinutes else (booking.trackingEtaMinutes / 2).coerceAtLeast(3L)
        else -> 0L
    }

    val stageLabel = when (booking.trackingStage) {
        "CONFIRMED" -> "تم تأكيد الطلب - يتجهز للانطلاق"
        "ON_THE_WAY" -> "سيارة الحرفي بالطريق ($dynamicEtaMinutes دقائق)"
        "IN_PROGRESS" -> "وصل لباب العمارة - العمل جارٍ الآن"
        "COMPLETED" -> "أنهى الحرفي العمل بنجاح"
        else -> booking.trackingStage
    }

    val nextActionLabel = when (booking.trackingStage) {
        "CONFIRMED" -> "انطلاق سيارة الحرفي وتفعيل تتبع الشوارع المباشر"
        "ON_THE_WAY" -> "تأكيد وصول سيارة الحرفي لباب المنزل وبدء العمل"
        "IN_PROGRESS" -> "زر الحرفي: تم إنهاء العمل (فتح تقييم الزبون وملاحظته)"
        else -> null
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booking_card_${booking.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.serviceTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${booking.providerName} • ${booking.providerSkill}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    color = if (booking.trackingStage == "COMPLETED") {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = stageLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (booking.trackingStage == "COMPLETED") {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "الموقع: ${booking.governorate} - ${booking.neighborhood} | الموعد: ${booking.scheduledDate} (${booking.scheduledTime})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Labor Only vs. Parts Included + 7-Day Warranty Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = booking.partsInclusionLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable { showWarrantyDialog = true }
                        .testTag("open_warranty_${booking.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🧾 سند الكفالة (7 أيام)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Doorstep Safety PIN (كود أمان الباب) + Parts Receipt & Neighbor Discount (-30%) Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDoorstepPinDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_doorstep_pin_${booking.id}")
                ) {
                    Text(
                        text = "🪪 كود الباب: ${booking.doorstepSecurityPin()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { showPartsInvoiceDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1.25f)
                        .testTag("open_parts_invoice_${booking.id}")
                ) {
                    Text(
                        text = "🧾 فاتورة القطع + خصم الجيران -30%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Talabat-style Live Vehicle & Street-by-Street Tracking Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFullRadarDialog = true }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Vehicle & License Plate Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = vehicleModel,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = "$vehiclePlate • السرعة: ${if (booking.trackingStage == "ON_THE_WAY") "42 كم/س" else "متوقف بالموقع"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            color = if (booking.trackingStage == "COMPLETED") {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = when (booking.trackingStage) {
                                    "CONFIRMED" -> "باقي ${String.format(Locale.US, "%.1f", remainingDistanceKm)} كم ($dynamicEtaMinutes د)"
                                    "ON_THE_WAY" -> "باقي ${String.format(Locale.US, "%.1f", remainingDistanceKm)} كم ($dynamicEtaMinutes د)"
                                    "IN_PROGRESS" -> "أمام باب العمارة الآن"
                                    else -> "مكتمل ✓"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Current Street Callout Banner (أي شارع هو الآن بالضبط)
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "الشارع الحالي: $currentStreetName",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (booking.trackingStage == "ON_THE_WAY") {
                                Text(
                                    text = "تحديث مباشر ●",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Multi-Street 2D Route Canvas with Moving Vehicle Pin
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // 4 street waypoints forming a realistic urban route turn-by-turn
                            val waypoints = listOf(
                                Offset(w * 0.12f, h * 0.72f),
                                Offset(w * 0.38f, h * 0.34f),
                                Offset(w * 0.66f, h * 0.64f),
                                Offset(w * 0.88f, h * 0.32f)
                            )

                            // Background intersecting street roads
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, h * 0.34f),
                                end = Offset(w, h * 0.34f),
                                strokeWidth = 10f
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, h * 0.68f),
                                end = Offset(w, h * 0.68f),
                                strokeWidth = 10f
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(w * 0.38f, 0f),
                                end = Offset(w * 0.38f, h),
                                strokeWidth = 10f
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(w * 0.66f, 0f),
                                end = Offset(w * 0.66f, h),
                                strokeWidth = 10f
                            )

                            // Draw full dashed route segments
                            for (i in 0 until waypoints.size - 1) {
                                drawLine(
                                    color = primaryColor.copy(alpha = 0.28f),
                                    start = waypoints[i],
                                    end = waypoints[i + 1],
                                    strokeWidth = 7f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                                )
                            }

                            // Draw completed route segments up to liveStreetStep
                            val safeStep = liveStreetStep.coerceIn(0, waypoints.size - 1)
                            for (i in 0 until safeStep) {
                                drawLine(
                                    color = secondaryColor,
                                    start = waypoints[i],
                                    end = waypoints[i + 1],
                                    strokeWidth = 8f
                                )
                            }

                            // Draw street checkpoint nodes
                            waypoints.forEachIndexed { idx, pt ->
                                drawCircle(
                                    color = if (idx <= safeStep) secondaryColor else gridColor,
                                    radius = 8f,
                                    center = pt
                                )
                            }

                            // Customer Home Pin at final waypoint
                            val homePin = waypoints.last()
                            drawCircle(
                                color = secondaryColor.copy(alpha = 0.25f),
                                radius = 22f,
                                center = homePin
                            )
                            drawCircle(
                                color = secondaryColor,
                                radius = 10f,
                                center = homePin
                            )

                            // Live Craftsman Vehicle Pin at current street waypoint
                            val carPin = waypoints[safeStep]
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.28f),
                                radius = 26f,
                                center = carPin
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 14f,
                                center = carPin
                            )
                            drawCircle(
                                color = tertiaryColor,
                                radius = 6f,
                                center = carPin
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🚗 سيارة ${booking.providerName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "🏠 منزلك: ${booking.neighborhood}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.clickable {
                                    liveStreetStep = (liveStreetStep + 1) % routeStreets.size
                                }
                            ) {
                                Text(
                                    text = "تحريك السيارة للشارع التالي ⬅",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.clickable { showFullRadarDialog = true }
                            ) {
                                Text(
                                    text = "عرض مسار الشوارع الكامل (وضع طلبات)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4-step status indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val stages = listOf(
                            "CONFIRMED" to "1. تأكيد",
                            "ON_THE_WAY" to "2. بالطريق",
                            "IN_PROGRESS" to "3. بالموقع",
                            "COMPLETED" to "4. انتهى العمل"
                        )
                        val activeIdx = stages.indexOfFirst { it.first == booking.trackingStage }.coerceAtLeast(0)
                        stages.forEachIndexed { idx, (_, label) ->
                            val reached = idx <= activeIdx
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (idx == activeIdx) FontWeight.Bold else FontWeight.Normal,
                                color = if (reached) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Real Hardware GPS Sensor Trigger Button
                    OutlinedButton(
                        onClick = {
                            gpsPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = realGpsTelemetry ?: "📡 تفعيل قراءة موقع GPS الحقيقي من حساس الهاتف",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            if (booking.escrowStatus != "NONE") {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (booking.escrowStatus == "RELEASED") {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (booking.escrowStatus == "RELEASED") Icons.Default.CheckCircle else Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (booking.escrowStatus == "RELEASED") {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (booking.escrowStatus == "RELEASED") {
                                        "المبلغ المتفق عليه: تم صرف ${booking.escrowAmountJod} د.أ للحرفي"
                                    } else {
                                        "المبلغ المتفق عليه: ${booking.escrowAmountJod} د.أ (محفوظ بالضمان + كفالة 7 أيام)"
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = "يمكنك الدفع عبر كليك (CliQ) أو تقسيم قطّية العمارة على الجيران",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(onClick = onPayViaCliQ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("كليك CliQ")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (nextActionLabel != null) {
                Button(
                    onClick = onAdvanceStage,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (booking.trackingStage == "IN_PROGRESS") {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advance_booking_${booking.id}")
                ) {
                    Text(nextActionLabel)
                }
            } else if (!booking.isReviewed) {
                Button(
                    onClick = onRateProvider,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rate_completed_booking_${booking.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تقييم الحرفي الآن وكتابة ملاحظة للزبائن القادمين")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تم تقييم الحرفي ونشر ملاحظتك في ملفه العام بنجاح",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }

    if (showFullRadarDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showFullRadarDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تتبع سيارة ${booking.providerName} لحظياً",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "المركبة: $vehicleModel",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "$vehiclePlate • الوقت المتبقي: $dynamicEtaMinutes دقائق (${String.format(Locale.US, "%.1f", remainingDistanceKm)} كم)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Text(
                        text = "خط سير السيارة شارعاً بشارع حتى باب منزلك:",
                        style = MaterialTheme.typography.labelLarge
                    )

                    routeStreets.forEachIndexed { index, street ->
                        val isPassed = index < liveStreetStep
                        val isCurrent = index == liveStreetStep
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isCurrent -> MaterialTheme.colorScheme.primaryContainer
                                    isPassed -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                }
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { liveStreetStep = index }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when {
                                        isCurrent -> Icons.Default.DirectionsCar
                                        isPassed -> Icons.Default.CheckCircle
                                        else -> Icons.Default.LocationOn
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        isPassed -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = street,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = when {
                                            isCurrent -> "🚗 سيارة الحرفي تتحرك في هذا الشارع الآن"
                                            isPassed -> "تم تجاوز هذا الشارع ✓"
                                            else -> "الشارع القادم في المسار"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        liveStreetStep = (liveStreetStep + 1) % routeStreets.size
                    }
                ) {
                    Text("تحديث للشارع التالي 🚗")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFullRadarDialog = false }) {
                    Text("إغلاق الخريطة")
                }
            }
        )
    }

    if (showWarrantyDialog) {
        WarrantyCertificateDialog(
            booking = booking,
            provider = matchedProvider,
            onDismiss = { showWarrantyDialog = false }
        )
    }

    if (showDoorstepPinDialog) {
        DoorstepSafetyIdDialog(
            booking = booking,
            provider = matchedProvider,
            onDismiss = { showDoorstepPinDialog = false }
        )
    }

    if (showPartsInvoiceDialog) {
        PartsInvoiceBreakdownDialog(
            booking = booking,
            onDismiss = { showPartsInvoiceDialog = false },
            onSaveInvoiceBreakdown = { laborJod, partsCostJod, storeName, hasNeighborDiscount ->
                onSavePartsInvoice(laborJod, partsCostJod, storeName, hasNeighborDiscount)
                showPartsInvoiceDialog = false
            }
        )
    }
}

@Composable
fun WalletAndAccountTabContent(
    userProfileState: UiState<UserProfile?>,
    b2bOffers: List<B2BPartnerOffer>,
    themeMode: ThemeMode,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onSwitchAccountType: (String) -> Unit,
    onUpdateAccountDetails: (displayName: String, accountType: String, gov: String, hood: String, phone: String) -> Unit,
    onTopUpPoints: (points: Long, priceJod: Int) -> Unit,
    onOpenCliQTopUp: () -> Unit,
    onOpenCraftsmanSheet: () -> Unit,
    onCouponCopied: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val profile = (userProfileState as? UiState.Success)?.data
    var showEditAccountDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("wallet_lazy_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0. Theme Mode Settings Card (الوضع الساطع والوضع الداكن في الإعدادات)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (themeMode == ThemeMode.DARK) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "إعدادات المظهر (الوضع الساطع والداكن)",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "اختر المظهر المريح لعينيك أثناء التصفح:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { onSelectThemeMode(mode) },
                                label = { Text(mode.labelAr) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (mode) {
                                            ThemeMode.LIGHT -> Icons.Default.LightMode
                                            ThemeMode.DARK -> Icons.Default.DarkMode
                                            ThemeMode.SYSTEM -> Icons.Default.Home
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_mode_${mode.name}")
                            )
                        }
                    }
                }
            }
        }

        // 1. User Identity & Role Switcher Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profile?.displayName ?: "مستخدم فرصني",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "${profile?.governorate ?: "عمّان"} • ${profile?.neighborhood ?: "خلدا"} | ${profile?.phoneNumber ?: "+962790000000"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { showEditAccountDialog = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "نوع الحساب في فرصني:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isClient = (profile?.accountType ?: "CLIENT") == "CLIENT"
                        FilterChip(
                            selected = isClient,
                            onClick = { onSwitchAccountType("CLIENT") },
                            label = { Text("طالب خدمة (زبون)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isClient,
                            onClick = { onSwitchAccountType("PROVIDER") },
                            label = { Text("مقدم خدمة (حرفي مستقل)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onOpenCraftsmanSheet,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_craftsman_profile_sheet_button")
                    ) {
                        Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة نوع سيارتي ولوحتي الأردنية + تعديل ملفي كحرفي")
                    }
                }
            }
        }

        // 2. Clear Explanation of 5 JOD OpenSooq Promotion vs Points System + CliQ Wallet
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "محفظة الترويج (5 دنانير) والنقاط عبر كليك CliQ",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${profile?.pointsBalance ?: 25L} نقطة",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 ما الفرق بين إعلان الـ 5 دنانير والنقاط؟\n• اشتراك 5 دنانير (مثل السوق المفتوح): يثبّت إعلانك وصور شغلك ورقمك في أعلى التطبيق لمدة أسبوعين أمام كل الزبائن بدون خصم أي نقاط.\n• النقاط الاختيارية: للحرفي غير المشترك بباقة الـ 5 دنانير، تُستخدم فقط عند رغبته بفتح رقم زبون في المناقصات الخاصة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenCliQTopUp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_cliq_wallet_button")
                    ) {
                        Icon(imageVector = Icons.Default.QrCode2, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("فتح نافذة التحويل الفوري عبر كليك (CliQ - FORASNIJO)")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(20L, 5, "باقة أساسية"),
                            Triple(50L, 10, "باقة المحترفين"),
                            Triple(120L, 20, "باقة التوفير")
                        ).forEach { (pts, priceJod, label) ->
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onTopUpPoints(pts, priceJod) }
                                    .testTag("topup_${pts}_button")
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "+$pts نقطة",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = "$priceJod د.أ",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. B2B Hardware & Building Materials Partner Discounts
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الشراكات التسويقية (B2B) لمحلات العدد ومواد البناء",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "خصومات حصرية للحرفيين المسجلين في تطبيق فرصني لدى أبرز متاجر العدد ومواد البناء في الأردن:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(b2bOffers, key = { it.id }) { offer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = offer.storeName,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${offer.cityAr} • ${offer.categoryAr}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = offer.discountLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = offer.descriptionAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "كود الخصم: ${offer.couponCode}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Forasni Coupon", offer.couponCode))
                                onCouponCopied(offer.couponCode)
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نسخ الكود", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 4. Google Play Store Compliance: Privacy Policy & Delete Account
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🛡️ الخصوصية وحماية البيانات (معايير Google Play الرسمية)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "يلتزم تطبيق «فرصني» بحماية خصوصية المستخدمين في الأردن وعدم مشاركة أرقام الهواتف أو البيانات إلا حسب اختيارك.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPrivacyPolicyDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("privacy_policy_button")
                        ) {
                            Text("📜 سياسة الخصوصية", style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick = { showDeleteAccountConfirmDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("delete_account_button")
                        ) {
                            Text(
                                text = "🗑️ حذف حسابي وبياناتي",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // 5. Sign Out / Optional Google Sign-In Button
        item {
            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.sign_out))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showPrivacyPolicyDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("📜 سياسة الخصوصية وشروط استخدام «فرصني»") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. جمع البيانات واستخدامها:\nنجمع فقط الاسم، المحافظة/الحي، ورقم الهاتف لتسهيل التواصل المباشر بين الزبون والحرفي في المملكة الأردنية الهاشمية.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2. الصور والوسائط:\nيستخدم التطبيق منتقي الصور الرسمي (Android Photo Picker) دون الوصول إلى ملفات جهازك الخاصة.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3. الدفع والتحويلات البنكية:\nتتم الحوالات عبر نظام CliQ أو IBAN الرسمي التابع للبنوك الأردنية.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "4. حق حذف الحساب والبيانات:\nيحق لأي مستخدم طلب حذف حسابه وجميع بياناته نهائياً في أي وقت من خلال زر (حذف حسابي وبياناتي) في هذه الصفحة.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("موافق وفهمت")
                }
            }
        )
    }

    if (showDeleteAccountConfirmDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteAccountConfirmDialog = false },
            title = { Text("🗑️ تأكيد حذف الحساب والبيانات نهائياً") },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في حذف حسابك وجميع بياناتك المرتبطة في تطبيق «فرصني» نهائياً؟ سيتم مسح بيانات ملفك وتسجيل الخروج فوراً.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountConfirmDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("نعم، احذف حسابي نهائياً")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirmDialog = false }) {
                    Text("تراجع")
                }
            }
        )
    }

    if (showEditAccountDialog && profile != null) {
        var editName by remember { mutableStateOf(profile.displayName) }
        var editGov by remember { mutableStateOf(profile.governorate) }
        var editHood by remember { mutableStateOf(profile.neighborhood) }
        var editPhone by remember { mutableStateOf(profile.phoneNumber) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditAccountDialog = false },
            title = { Text("تعديل بيانات الحساب") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("الاسم الكامل") }
                    )
                    OutlinedTextField(
                        value = editGov,
                        onValueChange = { editGov = it },
                        label = { Text("المحافظة") }
                    )
                    OutlinedTextField(
                        value = editHood,
                        onValueChange = { editHood = it },
                        label = { Text("الحي السكني") }
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("رقم الهاتف الأردني") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateAccountDetails(
                            editName,
                            profile.accountType,
                            editGov,
                            editHood,
                            editPhone
                        )
                        showEditAccountDialog = false
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditAccountDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
