package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.JobTender
import com.example.data.model.ProviderProfile
import com.example.data.model.ThemeMode
import com.example.ui.viewmodel.ForasniViewModel
import com.example.ui.viewmodel.UiState

enum class ForasniTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    DIRECTORY("directory", R.string.nav_directory, Icons.Default.HomeRepairService),
    TENDERS("tenders", R.string.nav_tenders, Icons.Default.Gavel),
    PROMOTED_ADS("promoted_ads", R.string.nav_promoted_ads, Icons.Default.Campaign),
    BOOKINGS("bookings", R.string.nav_bookings, Icons.Default.CalendarMonth),
    WALLET("wallet", R.string.nav_wallet, Icons.Default.AccountBalanceWallet)
}

private data class CliQDialogRequest(
    val recipientName: String,
    val cliqAlias: String,
    val initialAmountJod: Int,
    val purposeLabel: String,
    val isWalletTopUp: Boolean,
    val topUpPoints: Long = 50L
)

private fun launchDirectPhoneCall(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phoneNumber.trim()}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

private fun launchDirectWhatsApp(context: Context, whatsappNumber: String, providerName: String, skillTitle: String) {
    try {
        val cleanDigits = whatsappNumber.filter { it.isDigit() }
        val encodedMsg = Uri.encode("مرحباً $providerName، تواصلت معك عبر تطبيق فرصني بخصوص خدمة: $skillTitle")
        val uri = Uri.parse("https://wa.me/$cleanDigits?text=$encodedMsg")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForasniMainScreen(
    viewModel: ForasniViewModel,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val filteredProvidersState by viewModel.filteredProvidersState.collectAsStateWithLifecycle()
    val allProvidersState by viewModel.providersState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val tendersState by viewModel.tendersState.collectAsStateWithLifecycle()
    val bookingsState by viewModel.bookingsState.collectAsStateWithLifecycle()
    val unlockedTenderIds by viewModel.unlockedTenderIds.collectAsStateWithLifecycle()
    val acceptedTenderIds by viewModel.acceptedTenderIds.collectAsStateWithLifecycle()
    val promotedAdsState by viewModel.promotedAdsState.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val autoReviewPrompt by viewModel.autoReviewPrompt.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(ForasniTab.DIRECTORY) }
    var selectedProviderForDetail by remember { mutableStateOf<ProviderProfile?>(null) }
    var selectedProviderForBooking by remember { mutableStateOf<ProviderProfile?>(null) }
    var selectedProviderForReview by remember { mutableStateOf<Triple<ProviderProfile, String?, String?>?>(null) }
    var showCreateTenderDialog by remember { mutableStateOf(false) }
    var showPromoteAd5JodDialog by remember { mutableStateOf(false) }
    var selectedTenderForUnlock by remember { mutableStateOf<JobTender?>(null) }
    var showMyCraftsmanSheet by remember { mutableStateOf(false) }
    var activeCliQRequest by remember { mutableStateOf<CliQDialogRequest?>(null) }

    BackHandler(enabled = currentTab != ForasniTab.DIRECTORY) {
        currentTab = ForasniTab.DIRECTORY
    }

    val userProfile = (userProfileState as? UiState.Success)?.data
    val myExistingProvider = remember(allProvidersState, viewModel.currentUserId) {
        (allProvidersState as? UiState.Success)?.data?.find {
            it.id == "prov_user_${viewModel.currentUserId}"
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "فرصني",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "الأردن",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.toggleLightDarkTheme(isCurrentlyDark = themeMode == ThemeMode.DARK)
                            },
                            modifier = Modifier.testTag("topbar_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (themeMode == ThemeMode.DARK) {
                                    Icons.Default.LightMode
                                } else {
                                    Icons.Default.DarkMode
                                },
                                contentDescription = "تبديل الوضع الساطع والداكن",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { currentTab = ForasniTab.WALLET }
                                .testTag("topbar_points_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "رصيد النقاط",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${userProfile?.pointsBalance ?: 25L} نقطة",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { showMyCraftsmanSheet = true },
                            modifier = Modifier.testTag("topbar_craftsman_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = stringResource(id = R.string.create_provider_profile),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar {
                        ForasniTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = stringResource(id = tab.labelRes)
                                    )
                                },
                                label = {
                                    Text(text = stringResource(id = tab.labelRes))
                                },
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        ForasniTab.entries.forEach { tab ->
                            NavigationRailItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = stringResource(id = tab.labelRes)
                                    )
                                },
                                label = {
                                    Text(text = stringResource(id = tab.labelRes))
                                },
                                modifier = Modifier.testTag("rail_tab_${tab.route}")
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    AnimatedVisibility(visible = statusBanner != null) {
                        statusBanner?.let { message ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearBanner() },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "إغلاق",
                                            tint = MaterialTheme.colorScheme.onSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentTab) {
                            ForasniTab.DIRECTORY -> {
                                DirectoryTabContent(
                                    providersState = filteredProvidersState,
                                    filterState = filterState,
                                    availableNeighborhoods = viewModel.availableNeighborhoodsForSelectedGov(),
                                    onSearchQueryChange = viewModel::updateSearchQuery,
                                    onSelectSkill = viewModel::selectSkillCategory,
                                    onSelectGovernorate = viewModel::selectGovernorate,
                                    onSelectNeighborhood = viewModel::selectNeighborhood,
                                    onToggleOnlyAvailable = viewModel::toggleOnlyAvailableNow,
                                    onToggleLiveMap = viewModel::toggleLiveMapSection,
                                    onResetFilters = viewModel::resetFilters,
                                    onSelectProvider = { selectedProviderForDetail = it },
                                    onDirectCall = { provider ->
                                        viewModel.recordDirectLeadAndNotify(provider, "الاتصال الهاتفي")
                                        launchDirectPhoneCall(context, provider.businessPhone)
                                    },
                                    onDirectWhatsApp = { provider ->
                                        viewModel.recordDirectLeadAndNotify(provider, "محادثة واتساب")
                                        launchDirectWhatsApp(
                                            context,
                                            provider.whatsappNumber,
                                            provider.name,
                                            provider.skillTitle
                                        )
                                    },
                                    onBookProvider = { selectedProviderForBooking = it },
                                    onOpenCliQForProvider = { provider ->
                                        activeCliQRequest = CliQDialogRequest(
                                            recipientName = provider.name,
                                            cliqAlias = provider.cliqAlias.ifBlank { "FORASNIJO" },
                                            initialAmountJod = provider.hourlyRateJod.toInt().coerceAtLeast(10),
                                            purposeLabel = "تحويل أجر خدمة: ${provider.skillTitle}",
                                            isWalletTopUp = false
                                        )
                                    },
                                    onOpenMyCraftsmanProfile = { showMyCraftsmanSheet = true },
                                    onOpenQuickServiceTender = { showCreateTenderDialog = true }
                                )
                            }

                            ForasniTab.TENDERS -> {
                                TendersTabContent(
                                    tendersState = tendersState,
                                    unlockedTenderIds = unlockedTenderIds,
                                    acceptedTenderIds = acceptedTenderIds,
                                    currentUserId = viewModel.currentUserId,
                                    onOpenCreateTender = { showCreateTenderDialog = true },
                                    onSelectTenderToUnlock = { selectedTenderForUnlock = it },
                                    onAcceptJobDirectly = { tender ->
                                        viewModel.acceptJobOpportunityDirectly(tender)
                                    },
                                    onDirectCallClient = { phone ->
                                        launchDirectPhoneCall(context, phone)
                                    }
                                )
                            }

                            ForasniTab.PROMOTED_ADS -> {
                                PromotedAdsAndPackagesTabContent(
                                    promotedAds = promotedAdsState,
                                    monthlyPackages = viewModel.monthlyBuildingPackages,
                                    onOpenPromoteAd5JodDialog = { showPromoteAd5JodDialog = true },
                                    onDirectCallAd = { ad ->
                                        launchDirectPhoneCall(context, ad.phone)
                                    },
                                    onDirectWhatsAppAd = { ad ->
                                        launchDirectWhatsApp(
                                            context,
                                            ad.whatsapp,
                                            ad.providerName,
                                            ad.adTitle
                                        )
                                    },
                                    onSubscribeMonthlyPackage = { pkg ->
                                        viewModel.subscribeToMonthlyPackage(pkg)
                                        currentTab = ForasniTab.BOOKINGS
                                    }
                                )
                            }

                            ForasniTab.BOOKINGS -> {
                                BookingsAndEscrowTabContent(
                                    bookingsState = bookingsState,
                                    providersState = allProvidersState,
                                    onAdvanceBookingStage = viewModel::advanceBookingProgress,
                                    onOpenReviewForBooking = { prov, bookingId, jobTitle ->
                                        selectedProviderForReview = Triple(prov, bookingId, jobTitle)
                                    },
                                    onOpenCliQForBooking = { prov, amountJod, jobTitle ->
                                        activeCliQRequest = CliQDialogRequest(
                                            recipientName = prov.name,
                                            cliqAlias = prov.cliqAlias.ifBlank { "FORASNIJO" },
                                            initialAmountJod = amountJod,
                                            purposeLabel = "تحويل كليك مقابل: $jobTitle",
                                            isWalletTopUp = false
                                        )
                                    },
                                    onNavigateToDirectory = { currentTab = ForasniTab.DIRECTORY }
                                )
                            }

                            ForasniTab.WALLET -> {
                                WalletAndAccountTabContent(
                                    userProfileState = userProfileState,
                                    b2bOffers = viewModel.b2bPartnerOffers,
                                    themeMode = themeMode,
                                    onSelectThemeMode = viewModel::setThemeMode,
                                    onSwitchAccountType = viewModel::switchUserAccountType,
                                    onUpdateAccountDetails = viewModel::updateAccountDetails,
                                    onTopUpPoints = viewModel::topUpPoints,
                                    onOpenCliQTopUp = {
                                        activeCliQRequest = CliQDialogRequest(
                                            recipientName = "محفظة منصة فرصني الأردن",
                                            cliqAlias = "FORASNIJO",
                                            initialAmountJod = 10,
                                            purposeLabel = "شحن فوري لرصيد النقاط (+50 نقطة) عبر كليك",
                                            isWalletTopUp = true,
                                            topUpPoints = 50L
                                        )
                                    },
                                    onOpenCraftsmanSheet = { showMyCraftsmanSheet = true },
                                    onCouponCopied = { _ ->
                                        viewModel.updateSearchQuery(filterState.searchQuery)
                                    },
                                    onSignOut = onSignOut
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modals & Bottom Sheets
    selectedProviderForDetail?.let { provider ->
        ProviderDetailBottomSheet(
            provider = provider,
            onDismiss = { selectedProviderForDetail = null },
            onDirectCall = { prov ->
                viewModel.recordDirectLeadAndNotify(prov, "الاتصال الهاتفي")
                launchDirectPhoneCall(context, prov.businessPhone)
            },
            onDirectWhatsApp = { prov ->
                viewModel.recordDirectLeadAndNotify(prov, "محادثة واتساب")
                launchDirectWhatsApp(context, prov.whatsappNumber, prov.name, prov.skillTitle)
            },
            onOpenBookingEscrow = { prov ->
                selectedProviderForDetail = null
                selectedProviderForBooking = prov
            },
            onOpenCliQTransfer = { prov ->
                selectedProviderForDetail = null
                activeCliQRequest = CliQDialogRequest(
                    recipientName = prov.name,
                    cliqAlias = prov.cliqAlias.ifBlank { "FORASNIJO" },
                    initialAmountJod = prov.hourlyRateJod.toInt().coerceAtLeast(10),
                    purposeLabel = "تحويل أجر خدمة: ${prov.skillTitle}",
                    isWalletTopUp = false
                )
            },
            onOpenReviewDialog = { prov ->
                selectedProviderForDetail = null
                selectedProviderForReview = Triple(prov, null, null)
            }
        )
    }

    selectedProviderForBooking?.let { provider ->
        BookingEscrowDialog(
            provider = provider,
            onDismiss = { selectedProviderForBooking = null },
            onConfirmBooking = { serviceTitle, dateStr, timeStr, amountJod, useEscrow ->
                viewModel.createAppointmentBooking(
                    provider = provider,
                    serviceTitle = serviceTitle,
                    scheduledDate = dateStr,
                    scheduledTime = timeStr,
                    escrowAmountJod = amountJod,
                    useEscrow = useEscrow
                )
                selectedProviderForBooking = null
                currentTab = ForasniTab.BOOKINGS
            }
        )
    }

    // Manual review dialog OR automatic review dialog triggered when craftsman presses "Job Completed"
    val effectiveReviewTarget: Triple<ProviderProfile, String?, String?>? =
        selectedProviderForReview ?: autoReviewPrompt?.let { (prov, bkg) ->
            Triple(prov, bkg.id, bkg.serviceTitle)
        }
    effectiveReviewTarget?.let { (provider, bookingId, completedJobTitle) ->
        ReviewProviderDialog(
            provider = provider,
            completedJobTitle = completedJobTitle,
            onDismiss = {
                selectedProviderForReview = null
                viewModel.dismissAutoReviewPrompt()
            },
            onSubmitReview = { stars, comment ->
                viewModel.submitReview(provider, stars, comment, bookingId)
                selectedProviderForReview = null
                viewModel.dismissAutoReviewPrompt()
            }
        )
    }

    activeCliQRequest?.let { req ->
        CliQTransferDialog(
            recipientName = req.recipientName,
            cliqAlias = req.cliqAlias,
            defaultAmountJod = req.initialAmountJod.toLong(),
            purposeTitle = req.purposeLabel,
            onDismiss = { activeCliQRequest = null },
            onConfirmCliQ = { recipientAlias, amountJod, purpose ->
                if (req.isWalletTopUp) {
                    viewModel.topUpPoints(req.topUpPoints, amountJod.toInt())
                } else {
                    viewModel.confirmCliQTransfer(
                        recipientAlias = recipientAlias,
                        amountJod = amountJod,
                        purposeLabel = purpose
                    )
                }
                activeCliQRequest = null
            }
        )
    }

    if (showCreateTenderDialog) {
        CreateTenderDialog(
            onDismiss = { showCreateTenderDialog = false },
            onSubmitTender = { title, skillCategory, gov, hood, desc, budget, urgency, phone ->
                viewModel.postNewJobTender(
                    title = title,
                    skillCategory = skillCategory,
                    governorate = gov,
                    neighborhood = hood,
                    description = desc,
                    budgetJod = budget,
                    urgency = urgency,
                    clientPhone = phone
                )
                showCreateTenderDialog = false
                currentTab = ForasniTab.TENDERS
            }
        )
    }

    selectedTenderForUnlock?.let { tender ->
        UnlockTenderBidDialog(
            tender = tender,
            userPointsBalance = userProfile?.pointsBalance ?: 25L,
            onDismiss = { selectedTenderForUnlock = null },
            onConfirmUnlockAndBid = { bidPriceJod, bidNote ->
                viewModel.unlockTenderAndBid(tender, bidPriceJod, bidNote)
                selectedTenderForUnlock = null
            }
        )
    }

    if (showPromoteAd5JodDialog) {
        CreatePromotedAd5JodDialog(
            initialProviderName = myExistingProvider?.name ?: userProfile?.displayName ?: "حرفي فرصني",
            onDismiss = { showPromoteAd5JodDialog = false },
            onPublishPromotedAd = { provName, skillCat, adTitle, adDesc, gov, hood, priceLabel, phone, cliqAlias ->
                viewModel.publishPromotedAd5Jod(
                    providerName = provName,
                    skillCategory = skillCat,
                    adTitle = adTitle,
                    adDescription = adDesc,
                    governorate = gov,
                    neighborhood = hood,
                    priceLabel = priceLabel,
                    phone = phone,
                    cliqAlias = cliqAlias
                )
                showPromoteAd5JodDialog = false
                currentTab = ForasniTab.PROMOTED_ADS
            }
        )
    }

    if (showMyCraftsmanSheet) {
        CreateOrEditProviderSheet(
            initialName = userProfile?.displayName ?: "حرفي فرصني",
            existingProvider = myExistingProvider,
            onDismiss = { showMyCraftsmanSheet = false },
            onSaveProfile = { name, skillCat, skillTitle, gov, hood, bio, phone, whatsapp, rate, fee, avail, license, vehicleModel, vehiclePlate, titles, types ->
                viewModel.saveMyCraftsmanProfile(
                    name = name,
                    skillCategory = skillCat,
                    skillTitle = skillTitle,
                    governorate = gov,
                    neighborhood = hood,
                    bio = bio,
                    businessPhone = phone,
                    whatsappNumber = whatsapp,
                    hourlyRateJod = rate,
                    inspectionFeeJod = fee,
                    isAvailableNow = avail,
                    licenseTitle = license,
                    vehicleModel = vehicleModel,
                    vehiclePlate = vehiclePlate,
                    portfolioTitles = titles,
                    portfolioTypes = types
                )
                showMyCraftsmanSheet = false
            }
        )
    }
}
