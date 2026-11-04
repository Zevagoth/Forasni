package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.JORDAN_GOVERNORATES
import com.example.data.model.JobTender
import com.example.data.model.PortfolioMediaItem
import com.example.data.model.PriceAgreementMode
import com.example.data.model.ProviderProfile
import com.example.data.model.SkillCategory
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProviderDetailBottomSheet(
    provider: ProviderProfile,
    onDismiss: () -> Unit,
    onDirectCall: (ProviderProfile) -> Unit,
    onDirectWhatsApp: (ProviderProfile) -> Unit,
    onOpenBookingEscrow: (ProviderProfile) -> Unit,
    onOpenReviewDialog: (ProviderProfile) -> Unit,
    onOpenCliQTransfer: (ProviderProfile) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allPortfolio = remember(provider) { provider.parsedPortfolioItems() }
    val allReviews = remember(provider) { provider.parsedReviewsList() }
    var mediaFilter by remember { mutableStateOf("ALL") } // ALL, PHOTO, VIDEO
    var selectedMediaPreview by remember { mutableStateOf<PortfolioMediaItem?>(null) }

    val filteredPortfolio = remember(allPortfolio, mediaFilter) {
        when (mediaFilter) {
            "PHOTO" -> allPortfolio.filter { !it.isVideo }
            "VIDEO" -> allPortfolio.filter { it.isVideo }
            else -> allPortfolio
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = provider.name.take(2),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (provider.isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "موثق",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = provider.skillTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${provider.governorate} • ${provider.neighborhood} (${String.format(Locale.US, "%.1f", provider.distanceKm)} كم)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Verification, Rating & CliQ Badge Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f", provider.ratingAvg)} (${provider.reviewCount} تقييم)",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "السعر التقديري: ${provider.hourlyRateJod} د.أ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // CliQ Instant Payment Badge Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "يقبل التحويل الفوري عبر كليك (CliQ): ${provider.cliqAlias}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(
                            onClick = { onOpenCliQTransfer(provider) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("تحويل كليك", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    if (provider.licenseTitle.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "التوثيق المهني: ${provider.licenseTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "نبذة عن الخبرة والمهارات",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = provider.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Direct Contact Buttons (Call + WhatsApp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onDirectCall(provider) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("sheet_direct_call_button")
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "اتصال مباشر", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = { onDirectWhatsApp(provider) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("sheet_direct_whatsapp_button")
                ) {
                    Icon(imageVector = Icons.Default.Whatsapp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "واتساب فوري", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Action Row: Book with Escrow + Direct CliQ Transfer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onOpenBookingEscrow(provider) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sheet_book_escrow_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "حجز وتتبّع لحظي", style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = { onOpenCliQTransfer(provider) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sheet_cliq_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تحويل كليك CliQ", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(14.dp))

            // Portfolio Gallery Section (10-20 Photos & Videos)
            Text(
                text = "معرض الأعمال الموثقة (${allPortfolio.size} صورة وفيديو)",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "شاهد صور وفيديوهات الشغل السابق المعروضة من الحرفي قبل الاتفاق",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = mediaFilter == "ALL",
                    onClick = { mediaFilter = "ALL" },
                    label = { Text("الكل (${allPortfolio.size})") }
                )
                val photosCount = allPortfolio.count { !it.isVideo }
                val videosCount = allPortfolio.count { it.isVideo }
                FilterChip(
                    selected = mediaFilter == "PHOTO",
                    onClick = { mediaFilter = "PHOTO" },
                    label = { Text("الصور ($photosCount)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                FilterChip(
                    selected = mediaFilter == "VIDEO",
                    onClick = { mediaFilter = "VIDEO" },
                    label = { Text("الفيديوهات ($videosCount)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredPortfolio.forEach { item ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMediaPreview = item },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        )
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(92.dp)
                                    .background(
                                        Brush.linearGradient(
                                            colors = if (item.isVideo) {
                                                listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            } else {
                                                listOf(
                                                    MaterialTheme.colorScheme.secondaryContainer,
                                                    MaterialTheme.colorScheme.tertiaryContainer
                                                )
                                            }
                                        )
                                    )
                                    .padding(10.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = "#${item.index}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Icon(
                                    imageVector = if (item.isVideo) Icons.Default.PlayCircleFilled else Icons.Default.PhotoLibrary,
                                    contentDescription = item.title,
                                    tint = if (item.isVideo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .align(Alignment.Center)
                                )
                                Text(
                                    text = item.durationLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (item.isVideo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                )
                            }
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(14.dp))

            // Customer Reviews & Notes Feed (تقييمات وملاحظات الزبائن بعد انتهاء العمل)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تقييمات وملاحظات الزبائن (${provider.reviewCount})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "تظهر التقييمات والملاحظات بعد إتمام الحرفي للعمل ليستفيد منها الزبائن القادمون",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { onOpenReviewDialog(provider) }) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("أضف تقييمك")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allReviews.forEach { review ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = review.reviewerName,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(review.stars.coerceIn(1, 5)) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = review.dateLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "«${review.comment}»",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (selectedMediaPreview != null) {
        val media = selectedMediaPreview!!
        AlertDialog(
            onDismissRequest = { selectedMediaPreview = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (media.isVideo) Icons.Default.Videocam else Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (media.isVideo) "معاينة فيديو عمل موثق" else "معاينة صورة عمل موثقة",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (media.isVideo) Icons.Default.PlayCircleFilled else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = media.durationLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تم تنفيذ هذا العمل وتوثيقه بواسطة ${provider.name} في ${provider.governorate} (${provider.neighborhood}).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMediaPreview = null }) {
                    Text("إغلاق المعاينة")
                }
            }
        )
    }
}

@Composable
fun CliQTransferDialog(
    recipientName: String,
    cliqAlias: String,
    defaultAmountJod: Long,
    purposeTitle: String,
    onDismiss: () -> Unit,
    onConfirmCliQ: (recipientAlias: String, amountJod: Long, purpose: String) -> Unit
) {
    val context = LocalContext.current
    var amountStr by remember { mutableStateOf(defaultAmountJod.toString()) }
    var selectedBankOrWallet by remember { mutableStateOf("البنك العربي (عربي موبايل)") }
    var copiedToast by remember { mutableStateOf(false) }

    val jordanBanksAndWallets = listOf(
        "البنك العربي (عربي موبايل)",
        "بنك الاتحاد (Etihad)",
        "بنك الإسكان (Iskan Mobile)",
        "زين كاش (Zain Cash)",
        "أورنج موني (Orange Money)",
        "محفظة دينارك (Dinarak)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "نظام التحويل الفوري عبر كليك (CliQ)",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "المستفيد: $recipientName",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "لقب كليك (CliQ Alias):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = cliqAlias,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("CliQ Alias", cliqAlias))
                                    copiedToast = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (copiedToast) "تم النسخ!" else "نسخ اللقب")
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("المبلغ المراد تحويله (دينار أردني د.أ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "اختر تطبيق البنك أو المحفظة الأردنية:",
                    style = MaterialTheme.typography.labelMedium
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    jordanBanksAndWallets.forEach { bank ->
                        FilterChip(
                            selected = selectedBankOrWallet == bank,
                            onClick = { selectedBankOrWallet = bank },
                            label = { Text(bank) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toLongOrNull() ?: defaultAmountJod
                    onConfirmCliQ(cliqAlias, amt, "$purposeTitle عبر $selectedBankOrWallet")
                },
                modifier = Modifier.testTag("confirm_cliq_transfer_button")
            ) {
                Text("تأكيد التحويل عبر كليك")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun VoiceNoteRecorderCard(
    hasRecordedVoice: Boolean,
    recordedSeconds: Int,
    isRecording: Boolean,
    isPlaying: Boolean,
    onToggleRecord: () -> Unit,
    onTogglePlay: () -> Unit,
    onClearVoice: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isRecording) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f)
            } else {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
            }
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
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
                        imageVector = if (isRecording) Icons.Default.StopCircle else Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isRecording) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = when {
                                isRecording -> "جارٍ تسجيل ملاحظتك الصوتية... (00:${String.format(Locale.US, "%02d", recordedSeconds)})"
                                hasRecordedVoice && isPlaying -> "▶️ جارٍ تشغيل رسالتك الصوتية (00:${String.format(Locale.US, "%02d", recordedSeconds)} ث)"
                                hasRecordedVoice -> "تم إرفاق ملاحظة صوتية للحرفي (00:${String.format(Locale.US, "%02d", recordedSeconds)} ثانية) ✓"
                                else -> "ملاحظة صوتية سريعة (اشرح العطل بصوتك)"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "مثال: «يا معلم المكيف بيفصل التبريد بعد 5 دقائق وبيطلع صوت...»",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (!hasRecordedVoice || isRecording) {
                    Button(
                        onClick = onToggleRecord,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isRecording) "إيقاف وحفظ" else "سجّل صوت 🎙️",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            if (hasRecordedVoice && !isRecording) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onTogglePlay,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPlaying) "إيقاف الاستماع" else "استماع للتسجيل الصوتي")
                    }
                    TextButton(onClick = onClearVoice) {
                        Text("إعادة التسجيل")
                    }
                }
            }
        }
    }
}

@Composable
fun BookingEscrowDialog(
    provider: ProviderProfile,
    onDismiss: () -> Unit,
    onConfirmBooking: (serviceTitle: String, dateStr: String, timeStr: String, amountJod: Long, useEscrow: Boolean) -> Unit
) {
    val context = LocalContext.current
    val skillCat = remember(provider.skillCategory) { SkillCategory.fromId(provider.skillCategory) }
    var serviceTitle by remember { mutableStateOf("طلب خدمة: ${provider.skillTitle}") }
    var writtenNote by remember { mutableStateOf("أرجو إحضار العدة اللازمة للفحص والإصلاح") }
    var dateStr by remember { mutableStateOf("اليوم - 2026/11/04") }
    var timeStr by remember { mutableStateOf("04:00 مساءً") }
    var priceMode by remember { mutableStateOf(PriceAgreementMode.FIXED) }
    var amountStr by remember { mutableStateOf((provider.hourlyRateJod).toString()) }
    var useEscrow by remember { mutableStateOf(true) }
    var paymentMethod by remember { mutableStateOf("CLIQ") } // CLIQ or CASH
    var attachedSitePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var splitBetweenNeighbors by remember { mutableStateOf(false) }
    var apartmentsCount by remember { mutableIntStateOf(6) }
    var copiedNeighborsMsg by remember { mutableStateOf(false) }

    // Voice note state
    var isRecordingVoice by remember { mutableStateOf(false) }
    var hasRecordedVoice by remember { mutableStateOf(false) }
    var recordedSeconds by remember { mutableIntStateOf(0) }
    var isPlayingVoice by remember { mutableStateOf(false) }

    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordedSeconds = 1
            while (isRecordingVoice && recordedSeconds < 30) {
                delay(1000L)
                recordedSeconds++
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedSitePhotoUri = uri
        }
    }

    val amountJod = if (priceMode == PriceAgreementMode.ON_INSPECTION) {
        provider.inspectionFeeJod.coerceAtLeast(10L)
    } else {
        amountStr.toLongOrNull() ?: 15L
    }
    val commissionJod = if (useEscrow) (amountJod * 5L / 100L).coerceAtLeast(1L) else 0L
    val sharePerApartment = amountJod.toDouble() / apartmentsCount.coerceAtLeast(1).toDouble()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "طلب خدمة وتتبّع سيارة ${provider.name}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${provider.vehicleModelDisplay()} • ${provider.vehiclePlateDisplay()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Fair Market Price Guide Banner
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 مؤشر الأسعار العادل: ${skillCat.marketPriceRangeAr}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                OutlinedTextField(
                    value = serviceTitle,
                    onValueChange = { serviceTitle = it },
                    label = { Text("الشغلة المطلوبة (مثال: إصلاح مكيف / شطف عمارة / جز عشب)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_service_title_input")
                )

                OutlinedTextField(
                    value = writtenNote,
                    onValueChange = { writtenNote = it },
                    label = { Text("ملاحظة كتابية للحرفي (وصف العطل أو التفاصيل)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Voice Note Recorder
                VoiceNoteRecorderCard(
                    hasRecordedVoice = hasRecordedVoice,
                    recordedSeconds = recordedSeconds.coerceAtLeast(12),
                    isRecording = isRecordingVoice,
                    isPlaying = isPlayingVoice,
                    onToggleRecord = {
                        if (isRecordingVoice) {
                            isRecordingVoice = false
                            hasRecordedVoice = true
                        } else {
                            isRecordingVoice = true
                            isPlayingVoice = false
                        }
                    },
                    onTogglePlay = { isPlayingVoice = !isPlayingVoice },
                    onClearVoice = {
                        hasRecordedVoice = false
                        isRecordingVoice = false
                        isPlayingVoice = false
                        recordedSeconds = 0
                    }
                )

                // Price Negotiation / Agreement Mode Selector
                Text(
                    text = "حالة السعر والمفاصلة:",
                    style = MaterialTheme.typography.labelLarge
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PriceAgreementMode.entries.forEach { mode ->
                        FilterChip(
                            selected = priceMode == mode,
                            onClick = { priceMode = mode },
                            label = { Text(mode.labelAr) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (priceMode != PriceAgreementMode.ON_INSPECTION) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter { ch -> ch.isDigit() } },
                        label = {
                            Text(
                                if (priceMode == PriceAgreementMode.NEGOTIABLE) {
                                    "السعر المبدئي القابل للمفاصلة (دينار أردني د.أ)"
                                } else {
                                    "المبلغ المحدد الذي ستدفعه (دينار أردني د.أ)"
                                }
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🤝 حالة السعر: لم يُحدد بعد — ستتواصل مع ${provider.name} وتتفقان على السعر المناسب بعد معاينة الجهاز أو المكان.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("التاريخ") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeStr,
                        onValueChange = { timeStr = it },
                        label = { Text("الوقت") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Zero-permission Photo Picker to show craftsman the job site before arrival
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (attachedSitePhotoUri != null) {
                            "تم إرفاق صورة المكان/الجهاز بنجاح ✓"
                        } else {
                            "إرفاق صورة الجهاز أو المكان للحرفي (اختياري)"
                        }
                    )
                }

                // Jordanian Apartment Building Cost Splitter (حاسبة قطّية العمارة والجيران)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تقسيم المبلغ على شقق العمارة (قطّية الجيران)",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = "مفيد جداً عند طلب شطف العمارة أو تنسيق الحديقة المشتركة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = splitBetweenNeighbors,
                                onCheckedChange = { splitBetweenNeighbors = it }
                            )
                        }
                        if (splitBetweenNeighbors) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(4, 6, 8, 10).forEach { count ->
                                    FilterChip(
                                        selected = apartmentsCount == count,
                                        onClick = { apartmentsCount = count },
                                        label = { Text("$count شقق") }
                                    )
                                }
                            }
                            Text(
                                text = "حصة كل شقة: ${String.format(Locale.US, "%.2f", sharePerApartment)} د.أ (الإجمالي $amountJod د.أ)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = {
                                    val msg = "يا جيران العمارة الكرام، تم طلب ($serviceTitle) عبر تطبيق فرصني بمبلغ $amountJod د.أ، حصة كل شقة ($apartmentsCount شقق) = ${String.format(Locale.US, "%.2f", sharePerApartment)} د.أ عبر كليك (${provider.cliqAlias})."
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("Building Split", msg))
                                    copiedNeighborsMsg = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatsapp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (copiedNeighborsMsg) {
                                        "تم نسخ رسالة قروب واتساب العمارة ✓"
                                    } else {
                                        "نسخ رسالة القطّية لقروب واتساب العمارة"
                                    }
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "طريقة الدفع المفضلة:",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paymentMethod == "CLIQ",
                        onClick = { paymentMethod = "CLIQ" },
                        label = { Text("تحويل كليك (CliQ)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = paymentMethod == "CASH",
                        onClick = { paymentMethod = "CASH" },
                        label = { Text("نقداً عند الإنجاز") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (useEscrow) {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تفعيل نظام الضمان المالي (CliQ Escrow) + كفالة 7 أيام",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "يُحجز المبلغ عبر كليك (FORASNIJO) ولا يُحوّل للحرفي (${provider.cliqAlias}) إلا بعد أن ينهي العمل وتؤكد استلامك.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = useEscrow,
                                onCheckedChange = { useEscrow = it }
                            )
                        }
                        if (useEscrow) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "المبلغ المحجوز: $amountJod د.أ • عمولة حماية الضمان: $commissionJod د.أ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (serviceTitle.isNotBlank()) {
                        val tags = buildList {
                            if (priceMode == PriceAgreementMode.ON_INSPECTION) add("السعر بالاتفاق")
                            else if (priceMode == PriceAgreementMode.NEGOTIABLE) add("قابل للمفاصلة")
                            if (hasRecordedVoice) add("ملاحظة صوتية 🎙️")
                            if (attachedSitePhotoUri != null) add("صورة مرفقة")
                        }
                        val suffix = if (tags.isNotEmpty()) " (${tags.joinToString(" + ")})" else ""
                        onConfirmBooking(
                            "$serviceTitle$suffix",
                            dateStr.ifBlank { "2026-11-04" },
                            timeStr.ifBlank { "04:00 م" },
                            amountJod,
                            useEscrow
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_booking_button")
            ) {
                Text("تأكيد الطلب وبدء تتبع السيارة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun ReviewProviderDialog(
    provider: ProviderProfile,
    completedJobTitle: String? = null,
    onDismiss: () -> Unit,
    onSubmitReview: (stars: Int, comment: String) -> Unit
) {
    var selectedStars by remember { mutableIntStateOf(5) }
    var comment by remember {
        mutableStateOf(
            if (completedJobTitle != null) {
                "أنجز عمل ($completedJobTitle) بوقت قياسي ونظافة عالية جداً، أنصح الجميع بالتعامل معه."
            } else {
                "التزام تام بالموعد وشغل متقن ونظيف، أنصح بالتعامل معه."
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "اكتمل العمل! قيّم ${provider.name}",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                if (completedJobTitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "أعلن الحرفي انتهاء العمل على: $completedJobTitle",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "اختر عدد النجوم واكتب ملاحظتك لتظهر للزبائن الذين سيبحثون عن هذا الحرفي بعدك:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (star in 1..5) {
                        IconButton(onClick = { selectedStars = star }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star نجوم",
                                tint = if (star <= selectedStars) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                },
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("ملاحظتك وتجربتك (تظهر للناس في ملف الحرفي)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (comment.isNotBlank()) {
                        onSubmitReview(selectedStars, comment)
                    }
                },
                modifier = Modifier.testTag("submit_review_button")
            ) {
                Text("نشر التقييم والملاحظة للناس")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("لاحقاً")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateTenderDialog(
    onDismiss: () -> Unit,
    onSubmitTender: (
        title: String,
        skillCategory: String,
        governorate: String,
        neighborhood: String,
        description: String,
        budgetJod: Long,
        urgency: String,
        clientPhone: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SkillCategory.AC_TECH) }
    var selectedGov by remember { mutableStateOf("عمّان") }
    var neighborhood by remember { mutableStateOf("خلدا") }
    var description by remember { mutableStateOf("") }
    var priceMode by remember { mutableStateOf(PriceAgreementMode.NEGOTIABLE) }
    var budgetStr by remember { mutableStateOf("15") }
    var urgency by remember { mutableStateOf("URGENT") }
    var clientPhone by remember { mutableStateOf("+962795001122") }

    // Voice note state
    var isRecordingVoice by remember { mutableStateOf(false) }
    var hasRecordedVoice by remember { mutableStateOf(false) }
    var recordedSeconds by remember { mutableIntStateOf(0) }
    var isPlayingVoice by remember { mutableStateOf(false) }

    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordedSeconds = 1
            while (isRecordingVoice && recordedSeconds < 30) {
                delay(1000L)
                recordedSeconds++
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "نشر فرصة عمل / طلب صيانة (مع ملاحظة صوتية أو مفاصلة)",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "اختر طلباً سريعاً أو اكتب الشغلة المطلوبة (إصلاح مكيف، شطف عمارة، جز عشب) مع إمكانية إضافة ملاحظة صوتية وتحديد السعر أو تركه للمفاصلة:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Templates requested by user (إصلاح مكيف / شطف عمارة / جز عشب)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = title.contains("مكيف"),
                        onClick = {
                            selectedCategory = SkillCategory.AC_TECH
                            title = "إصلاح وصيانة مكيف لا يبرّد"
                            description = "المكيف يشتغل ويفصل التبريد، أحتاج فني لفحص الغاز والكومبروسر والاتفاق على السعر."
                            priceMode = PriceAgreementMode.ON_INSPECTION
                            budgetStr = "15"
                        },
                        label = { Text("إصلاح مكيف (بالاتفاق)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    FilterChip(
                        selected = title.contains("شطف"),
                        onClick = {
                            selectedCategory = SkillCategory.CLEANING
                            title = "شطف وتنظيف درج ومدخل العمارة"
                            description = "مطلوب شخص لشطف درج العمارة كامل مع المدخل والكراج، الماء ومواد التنظيف متوفرة."
                            priceMode = PriceAgreementMode.FIXED
                            budgetStr = "15"
                        },
                        label = { Text("شطف العمارة (15 د.أ)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    FilterChip(
                        selected = title.contains("جز عشب"),
                        onClick = {
                            selectedCategory = SkillCategory.GARDENING
                            title = "جز عشب وتنسيق حديقة المنزل"
                            description = "مطلوب شخص معه ماكينة لجز عشب الحديقة وتقليم الشجر وتنظيف الأوراق."
                            priceMode = PriceAgreementMode.NEGOTIABLE
                            budgetStr = "20"
                        },
                        label = { Text("جز عشب الحديقة (20 د.أ)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                // Market average indicator
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 ${selectedCategory.marketPriceRangeAr}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("الشغلة المطلوبة (مثال: إصلاح مكيف / شطف العمارة)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tender_title_input")
                )
                Text(text = "نوع المهنة أو المهارة:", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkillCategory.entries.filter { it != SkillCategory.ALL }.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.titleAr) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("ملاحظة كتابية وتفاصيل العطل أو العمل") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tender_desc_input")
                )

                // Voice Note Recorder
                VoiceNoteRecorderCard(
                    hasRecordedVoice = hasRecordedVoice,
                    recordedSeconds = recordedSeconds.coerceAtLeast(14),
                    isRecording = isRecordingVoice,
                    isPlaying = isPlayingVoice,
                    onToggleRecord = {
                        if (isRecordingVoice) {
                            isRecordingVoice = false
                            hasRecordedVoice = true
                        } else {
                            isRecordingVoice = true
                            isPlayingVoice = false
                        }
                    },
                    onTogglePlay = { isPlayingVoice = !isPlayingVoice },
                    onClearVoice = {
                        hasRecordedVoice = false
                        isRecordingVoice = false
                        isPlayingVoice = false
                        recordedSeconds = 0
                    }
                )

                // Price Negotiation / Agreement Mode Selector
                Text(
                    text = "حالة السعر (هل تريد تحديد السعر أم تركه للمفاصلة والاتفاق؟):",
                    style = MaterialTheme.typography.labelLarge
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PriceAgreementMode.entries.forEach { mode ->
                        FilterChip(
                            selected = priceMode == mode,
                            onClick = { priceMode = mode },
                            label = { Text(mode.labelAr) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (priceMode != PriceAgreementMode.ON_INSPECTION) {
                        OutlinedTextField(
                            value = budgetStr,
                            onValueChange = { budgetStr = it.filter { ch -> ch.isDigit() } },
                            label = {
                                Text(
                                    if (priceMode == PriceAgreementMode.NEGOTIABLE) {
                                        "السعر المبدئي (قابل للتفاوض د.أ)"
                                    } else {
                                        "المبلغ المحدد (د.أ)"
                                    }
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("رقم هاتفك للتواصل") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(text = "المحافظة:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    JORDAN_GOVERNORATES.forEach { gov ->
                        FilterChip(
                            selected = selectedGov == gov.nameAr,
                            onClick = {
                                selectedGov = gov.nameAr
                                neighborhood = gov.neighborhoods.getOrElse(1) { "المركز" }
                            },
                            label = { Text(gov.nameAr) }
                        )
                    }
                }
                OutlinedTextField(
                    value = neighborhood,
                    onValueChange = { neighborhood = it },
                    label = { Text("الحي السكني") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "URGENT" to "طارئ الآن",
                        "TODAY" to "اليوم",
                        "FLEXIBLE" to "موعد مرن"
                    ).forEach { (code, label) ->
                        FilterChip(
                            selected = urgency == code,
                            onClick = { urgency = code },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.trim().ifBlank { "طلب ${selectedCategory.titleAr} في $neighborhood" }
                    val baseDesc = description.trim().ifBlank { "مطلوب ${selectedCategory.titleAr} للعمل في $selectedGov - $neighborhood" }
                    val prefixTags = buildString {
                        when (priceMode) {
                            PriceAgreementMode.ON_INSPECTION -> append("[السعر: بالاتفاق بعد المعاينة] ")
                            PriceAgreementMode.NEGOTIABLE -> append("[قابل للمفاصلة] ")
                            PriceAgreementMode.FIXED -> {}
                        }
                        if (hasRecordedVoice) {
                            append("[ملاحظة صوتية: 00:${String.format(Locale.US, "%02d", recordedSeconds.coerceAtLeast(14))} ثانية] ")
                        }
                    }
                    val finalDesc = "$prefixTags$baseDesc".take(450)
                    onSubmitTender(
                        finalTitle,
                        selectedCategory.id,
                        selectedGov,
                        neighborhood.ifBlank { "المركز" },
                        finalDesc,
                        budgetStr.toLongOrNull() ?: 15L,
                        urgency,
                        clientPhone.ifBlank { "+962790000000" }
                    )
                },
                modifier = Modifier.testTag("submit_tender_button")
            ) {
                Text("نشر الفرصة للحرفيين")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun UnlockTenderBidDialog(
    tender: JobTender,
    userPointsBalance: Long,
    onDismiss: () -> Unit,
    onConfirmUnlockAndBid: (bidPriceJod: Long, bidNote: String) -> Unit
) {
    var bidPriceStr by remember { mutableStateOf(tender.budgetJod.toString()) }
    var bidNote by remember {
        mutableStateOf(
            if (tender.isPriceOnInspection()) {
                "جاهز للحضور وفحص الجهاز والاتفاق معك على سعر يرضيك مع كفالة."
            } else {
                "جاهز للتنفيذ الفوري اليوم مع كفالة على الشغل"
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "مفاصلة السعر / تقديم عرض للزبون (${tender.clientName})",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "حالة السعر الحالية: ${tender.priceBadgeLabel()}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "اكتب السعر الذي تقترحه على الزبون أو رسالتك للاتفاق المباشر معه:",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                OutlinedTextField(
                    value = bidPriceStr,
                    onValueChange = { bidPriceStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("السعر المقترح منك للمفاصلة (دينار أردني د.أ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bidNote,
                    onValueChange = { bidNote = it },
                    label = { Text("رسالتك للزبون وموعد وصولك") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = bidPriceStr.toLongOrNull() ?: tender.budgetJod
                    onConfirmUnlockAndBid(price, bidNote.ifBlank { "جاهز للتنفيذ الفوري" })
                },
                modifier = Modifier.testTag("confirm_unlock_bid_button")
            ) {
                Text("إرسال عرض السعر وإظهار رقم الزبون")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateOrEditProviderSheet(
    initialName: String,
    existingProvider: ProviderProfile?,
    onDismiss: () -> Unit,
    onSaveProfile: (
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
        vehicleModel: String,
        vehiclePlate: String,
        portfolioTitles: List<String>,
        portfolioTypes: List<String>
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(existingProvider?.name ?: initialName) }
    var selectedSkill by remember {
        mutableStateOf(
            existingProvider?.let { SkillCategory.fromId(it.skillCategory) } ?: SkillCategory.ELECTRICIAN
        )
    }
    var skillTitle by remember {
        mutableStateOf(existingProvider?.skillTitle ?: "فني كهرباء منازل وصيانة فورية")
    }
    var governorate by remember { mutableStateOf(existingProvider?.governorate ?: "عمّان") }
    var neighborhood by remember { mutableStateOf(existingProvider?.neighborhood ?: "خلدا") }
    var bio by remember {
        mutableStateOf(
            existingProvider?.bio
                ?: "خبرة عملية واسعة في تنفيذ الأعمال بدقة وأمانة عالية، متاح للخدمات الفورية والمشاريع في منطقتي."
        )
    }
    var businessPhone by remember { mutableStateOf(existingProvider?.businessPhone ?: "+962795123456") }
    var whatsappNumber by remember { mutableStateOf(existingProvider?.whatsappNumber ?: "962795123456") }
    var hourlyRateStr by remember { mutableStateOf((existingProvider?.hourlyRateJod ?: 12L).toString()) }
    var inspectionFeeStr by remember { mutableStateOf((existingProvider?.inspectionFeeJod ?: 5L).toString()) }
    var isAvailableNow by remember { mutableStateOf(existingProvider?.isAvailableNow ?: true) }
    var licenseTitle by remember {
        mutableStateOf(existingProvider?.cleanLicenseDisplay()?.ifBlank { null } ?: "شهادة مزاولة مهنة - مؤسسة التدريب المهني")
    }

    // Vehicle Model & Jordanian Plate Number fields requested by user
    var vehicleModel by remember {
        mutableStateOf(existingProvider?.vehicleModelDisplay() ?: "فان هونداي H1 أبيض (مجهز بالعدة)")
    }
    var vehiclePlate by remember {
        mutableStateOf(
            existingProvider?.vehiclePlateDisplay()?.removePrefix("لوحة أردنية ")?.trim() ?: "19-84520"
        )
    }

    val portfolioItems = remember {
        val initial = existingProvider?.parsedPortfolioItems()
        mutableStateListOf<Pair<String, String>>().apply {
            if (!initial.isNullOrEmpty()) {
                initial.forEach { add(it.title to if (it.isVideo) "VIDEO" else "PHOTO") }
            } else {
                add("صورة قبل وبعد تنفيذ المشروع رقم 1" to "PHOTO")
                add("فيديو توثيقي لاختبار العمل بعد التسليم 2" to "VIDEO")
                add("صيانة فورية ومعالجة عطل طارئ 3" to "PHOTO")
                add("تركيب معدات حديثة عالية الجودة 4" to "PHOTO")
                add("فيديو شرح خطوات التنفيذ للزبون 5" to "VIDEO")
                add("تشطيب نهائي وتسليم بالمواصفات 6" to "PHOTO")
                add("مشروع صيانة منزلية متكاملة 7" to "PHOTO")
                add("فيديو قبل وبعد الإصلاح والتجديد 8" to "VIDEO")
                add("تركيب قطع أصلية مع الكفالة 9" to "PHOTO")
                add("مشروع عمل حر موثق 10" to "PHOTO")
                add("فيديو فحص السلامة والأمان 11" to "VIDEO")
                add("صورة تسليم المشروع النهائي 12" to "PHOTO")
            }
        }
    }
    var newWorkTitle by remember { mutableStateOf("") }
    var newWorkIsVideo by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "إنشاء / تحديث ملفي المهني وبيانات سيارتي",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "أضف مهاراتك، نوع مركبتك ورقم اللوحة الأردنية (لتتبع الزبون لك على الخريطة)، ومعرض أعمالك (10 إلى 20 صورة وفيديو).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("الاسم الكامل أو اللقب المهني") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(text = "المهارة الأساسية:", style = MaterialTheme.typography.labelMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SkillCategory.entries.filter { it != SkillCategory.ALL }.forEach { skill ->
                    FilterChip(
                        selected = selectedSkill == skill,
                        onClick = {
                            selectedSkill = skill
                            skillTitle = "${skill.titleAr} - ${skill.subtitleAr}"
                        },
                        label = { Text(skill.titleAr) }
                    )
                }
            }

            OutlinedTextField(
                value = skillTitle,
                onValueChange = { skillTitle = it },
                label = { Text("المسمى المهني التفصيلي") },
                modifier = Modifier.fillMaxWidth()
            )

            // Craftsman Vehicle & Jordanian License Plate Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "بيانات مركبة العمل واللوحة الأردنية (للتتبع المباشر)",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "تظهر للزبون على الخريطة عندما ينطلق الحرفي باتجاه المنزل:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "فان هونداي H1 أبيض" to "19-84520",
                            "بيك أب كيا بونغو" to "42-31908",
                            "كيا نيرو هجين فضي" to "21-60412",
                            "ميتسوبيشي L200" to "17-99214",
                            "بدون مركبة (تنقل حر)" to "مواصلات"
                        ).forEach { (presetCar, presetPlate) ->
                            FilterChip(
                                selected = vehicleModel.startsWith(presetCar),
                                onClick = {
                                    vehicleModel = presetCar
                                    vehiclePlate = presetPlate
                                },
                                label = { Text(presetCar) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = vehicleModel,
                            onValueChange = { vehicleModel = it },
                            label = { Text("نوع السيارة ولونها") },
                            modifier = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value = vehiclePlate,
                            onValueChange = { vehiclePlate = it },
                            label = { Text("رقم اللوحة (19-84520)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Text(text = "المحافظة:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                JORDAN_GOVERNORATES.forEach { gov ->
                    FilterChip(
                        selected = governorate == gov.nameAr,
                        onClick = {
                            governorate = gov.nameAr
                            neighborhood = gov.neighborhoods.getOrElse(1) { "المركز" }
                        },
                        label = { Text(gov.nameAr) }
                    )
                }
            }

            OutlinedTextField(
                value = neighborhood,
                onValueChange = { neighborhood = it },
                label = { Text("الحي السكني / منطقة الخدمة") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = businessPhone,
                    onValueChange = { businessPhone = it },
                    label = { Text("رقم الاتصال (+962)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = whatsappNumber,
                    onValueChange = { whatsappNumber = it },
                    label = { Text("رقم واتساب (962...)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = hourlyRateStr,
                    onValueChange = { hourlyRateStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("سعر الساعة / الخدمة (د.أ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = inspectionFeeStr,
                    onValueChange = { inspectionFeeStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("بدل المعاينة (د.أ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = licenseTitle,
                onValueChange = { licenseTitle = it },
                label = { Text("التراخيص المهنية أو الشهادات المعتمدة") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("نبذة عن خبرتك والخدمات التي تقدمها") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "متاح الآن للطلبات الفورية والطارئة",
                    style = MaterialTheme.typography.titleSmall
                )
                Switch(
                    checked = isAvailableNow,
                    onCheckedChange = { isAvailableNow = it }
                )
            }

            HorizontalDivider()

            // Portfolio 10-20 Items Manager
            Text(
                text = "معرض الأعمال الموثق (${portfolioItems.size} / 20 صورة قبل/بعد وفيديو)",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newWorkTitle,
                    onValueChange = { newWorkTitle = it },
                    label = { Text("عنوان عمل سابق (أو صورة قبل وبعد)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = newWorkIsVideo,
                    onClick = { newWorkIsVideo = !newWorkIsVideo },
                    label = { Text(if (newWorkIsVideo) "فيديو" else "صورة") }
                )
                IconButton(
                    onClick = {
                        if (newWorkTitle.isNotBlank() && portfolioItems.size < 20) {
                            portfolioItems.add(
                                newWorkTitle.trim() to if (newWorkIsVideo) "VIDEO" else "PHOTO"
                            )
                            newWorkTitle = ""
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة عمل")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                portfolioItems.forEachIndexed { idx, (itemTitle, itemType) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (itemType == "VIDEO") Icons.Default.Videocam else Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${idx + 1}. $itemTitle",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        if (portfolioItems.size > 1) {
                            IconButton(
                                onClick = { portfolioItems.removeAt(idx) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "حذف",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onSaveProfile(
                        name.ifBlank { "حرفي فرصني" },
                        selectedSkill.id,
                        skillTitle.ifBlank { selectedSkill.titleAr },
                        governorate,
                        neighborhood.ifBlank { "المركز" },
                        bio.ifBlank { "حرفي مستقل في منصة فرصني" },
                        businessPhone.ifBlank { "+962790000000" },
                        whatsappNumber.ifBlank { "962790000000" },
                        hourlyRateStr.toLongOrNull() ?: 12L,
                        inspectionFeeStr.toLongOrNull() ?: 5L,
                        isAvailableNow,
                        licenseTitle,
                        vehicleModel,
                        vehiclePlate,
                        portfolioItems.map { it.first },
                        portfolioItems.map { it.second }
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_provider_profile_button")
            ) {
                Text("حفظ ونشر ملفي المهني وبيانات مركبتي")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatePromotedAd5JodDialog(
    initialProviderName: String,
    onDismiss: () -> Unit,
    onPublishPromotedAd: (
        providerName: String,
        skillCategory: String,
        adTitle: String,
        adDescription: String,
        governorate: String,
        neighborhood: String,
        priceLabel: String,
        phone: String,
        cliqAlias: String
    ) -> Unit
) {
    var providerName by remember { mutableStateOf(initialProviderName) }
    var selectedCategory by remember { mutableStateOf(SkillCategory.AC_TECH) }
    var adTitle by remember { mutableStateOf("ورشة صيانة وإصلاح مكيفات وأجهزة منزلية بالمنزل") }
    var adDescription by remember {
        mutableStateOf("فحص شامل وإصلاح فوري بالمنزل مع قطع أصلية وكفالة 7 أيام على الشغل. نصلك خلال 20 دقيقة!")
    }
    var governorate by remember { mutableStateOf("عمّان") }
    var neighborhood by remember { mutableStateOf("خلدا وطبربور") }
    var priceLabel by remember { mutableStateOf("السعر بالاتفاق بعد المعاينة (أو ابتداءً من 12 د.أ)") }
    var phone by remember { mutableStateOf("+962795123456") }
    var cliqAlias by remember { mutableStateOf("FORASNIJO") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "روّج إعلانك بـ 5 دنانير (مثل السوق المفتوح)",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "★ باقة الترويج الذهبية (5 دنانير أردنية فقط)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "يظهر إعلانك في صدارة قسم الإعلانات الممولة مع إطار ذهبي، شارة (إعلان ممول VIP)، وأزرار اتصال وواتساب مباشرة لمضاعفة اتصالات الزبائن 5 أضعاف!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = providerName,
                    onValueChange = { providerName = it },
                    label = { Text("اسم المعلن / الورشة") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "قسم الإعلان:", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkillCategory.entries.filter { it != SkillCategory.ALL }.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.titleAr) }
                        )
                    }
                }

                OutlinedTextField(
                    value = adTitle,
                    onValueChange = { adTitle = it },
                    label = { Text("عنوان الإعلان الممول") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = adDescription,
                    onValueChange = { adDescription = it },
                    label = { Text("تفاصيل العرض والخدمة") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceLabel,
                    onValueChange = { priceLabel = it },
                    label = { Text("السعر المعروض (مثال: 15 د.أ أو قابل للتفاوض)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = governorate,
                        onValueChange = { governorate = it },
                        label = { Text("المحافظة") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = neighborhood,
                        onValueChange = { neighborhood = it },
                        label = { Text("الأحياء المخدومة") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف/واتساب") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cliqAlias,
                        onValueChange = { cliqAlias = it },
                        label = { Text("لقب كليك CliQ") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (adTitle.isNotBlank()) {
                        onPublishPromotedAd(
                            providerName.ifBlank { "حرفي فرصني" },
                            selectedCategory.id,
                            adTitle,
                            adDescription,
                            governorate,
                            neighborhood,
                            priceLabel,
                            phone,
                            cliqAlias
                        )
                    }
                },
                modifier = Modifier.testTag("publish_promoted_ad_button")
            ) {
                Text("دفع 5 د.أ (كليك) وتثبيت الإعلان بالصدارة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
