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
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.model.JORDAN_GOVERNORATES
import com.example.data.model.JobTender
import com.example.data.model.PartsInclusionMode
import com.example.data.model.PortfolioMediaItem
import com.example.data.model.PriceAgreementMode
import com.example.data.model.ProviderProfile
import com.example.data.model.ServiceBooking
import com.example.data.model.SkillCategory
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

private fun savePickedPortfolioImageToStorage(context: Context, sourceUri: Uri): String {
    return try {
        val destFile = File(context.filesDir, "portfolio_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        if (destFile.exists() && destFile.length() > 0L) {
            Uri.fromFile(destFile).toString()
        } else {
            sourceUri.toString()
        }
    } catch (_: Exception) {
        sourceUri.toString()
    }
}

@Composable
private fun rememberPortfolioBitmap(context: Context, uriString: String): ImageBitmap? {
    return remember(uriString) {
        if (uriString.isBlank()) return@remember null
        try {
            val parsed = Uri.parse(uriString)
            context.contentResolver.openInputStream(parsed)?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProviderDetailBottomSheet(
    provider: ProviderProfile,
    isFavorite: Boolean = false,
    onToggleFavorite: (ProviderProfile) -> Unit = {},
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
                IconButton(
                    onClick = { onToggleFavorite(provider) },
                    modifier = Modifier.testTag("sheet_favorite_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "محفوظ في صنايعية بيتنا" else "إضافة إلى صنايعية بيتنا",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Jordanian Family-Safe Non-Conviction, Emergency 24h & Female Provider Badges
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (provider.hasNonConvictionCertificate) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "🛡️ موثق الهوية وعدم محكومية (آمن للعائلات)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                if (provider.isEmergency24hResponder) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "🚨 فزعة طوارئ 24 ساعة (وصول خلال 15 دقيقة)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                if (provider.isFemaleProvider) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "👩‍💼 مقدمة خدمة للسيدات والعائلات",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            // In-App Video Showcase Button (Opens inside Forasni without leaving to TikTok/Facebook)
            Button(
                onClick = {
                    val firstVid = allPortfolio.firstOrNull { it.isVideo } ?: PortfolioMediaItem(
                        index = 1,
                        title = "فيديو ميداني لتوثيق شغل ${provider.name} (${provider.skillTitle})",
                        isVideo = true,
                        isBeforeAfter = false,
                        durationLabel = "فيديو موثق 00:35",
                        mediaUri = ""
                    )
                    selectedMediaPreview = firstVid
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sheet_watch_in_app_video_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🎥 شاهد فيديو شغل المعلم (يفتح داخل التطبيق مباشرة)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
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
                val context = LocalContext.current
                filteredPortfolio.forEach { item ->
                    val realBitmap = rememberPortfolioBitmap(context, item.mediaUri)
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
                                    .height(104.dp)
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
                            ) {
                                if (realBitmap != null) {
                                    Image(
                                        bitmap = realBitmap,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth().height(104.dp)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_forasni_hero_banner),
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.42f,
                                        modifier = Modifier.fillMaxWidth().height(104.dp)
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = if (realBitmap != null) "📷 صورة حقيقية #${item.index}" else "#${item.index}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                if (realBitmap == null) {
                                    Icon(
                                        imageVector = if (item.isVideo) Icons.Default.PlayCircleFilled else Icons.Default.PhotoLibrary,
                                        contentDescription = item.title,
                                        tint = if (item.isVideo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = item.durationLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
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
        if (media.isVideo) {
            InAppVideoPlayerDialog(
                provider = provider,
                media = media,
                onDismiss = { selectedMediaPreview = null },
                onBookNow = {
                    selectedMediaPreview = null
                    onOpenBookingEscrow(provider)
                }
            )
        } else {
            val context = LocalContext.current
            val previewBitmap = rememberPortfolioBitmap(context, media.mediaUri)
            AlertDialog(
                onDismissRequest = { selectedMediaPreview = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "معاينة صورة عمل حقيقية موثقة",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                text = {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
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
                            if (previewBitmap != null) {
                                Image(
                                    bitmap = previewBitmap,
                                    contentDescription = media.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(190.dp)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_forasni_hero_banner),
                                    contentDescription = media.title,
                                    contentScale = ContentScale.Crop,
                                    alpha = 0.45f,
                                    modifier = Modifier.fillMaxWidth().height(190.dp)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
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
                    Button(
                        onClick = {
                            selectedMediaPreview = null
                            onOpenBookingEscrow(provider)
                        }
                    ) {
                        Text("✅ احجز مع المعلم الآن")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedMediaPreview = null }) {
                        Text("إغلاق")
                    }
                }
            )
        }
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
    var copiedAlias by remember { mutableStateOf(false) }
    var copiedIban by remember { mutableStateOf(false) }

    val ibanNumber = remember(cliqAlias) {
        when (cliqAlias) {
            "FORASNIJO" -> "JO94CBJO0010000000000131000302"
            else -> "JO27ARAB1180000000108524900101"
        }
    }

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
                    text = "التحويل المباشر (اسم كليك CliQ / IBAN)",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "انسخ اسم كليك (CliQ) أو رقم الآيبان (IBAN) وحوّل مباشرة من تطبيق بنكك في الأردن:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. CliQ Alias Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "اسم المستفيد: $recipientName",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "اسم كليك (CliQ Alias):",
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
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("CliQ Alias", cliqAlias))
                                    copiedAlias = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (copiedAlias) "تم نسخ الاسم ✓" else "نسخ اسم كليك")
                            }
                        }
                    }
                }

                // 2. Jordanian Bank IBAN Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "رقم الآيبان البنكي (IBAN):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ibanNumber,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("IBAN", ibanNumber))
                                copiedIban = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (copiedIban) "تم نسخ الـ IBAN بنجاح ✓" else "نسخ رقم الـ IBAN للتحويل البنكي")
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("المبلغ المتفق عليه (دينار أردني د.أ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toLongOrNull() ?: defaultAmountJod
                    onConfirmCliQ(
                        cliqAlias,
                        amt,
                        "$purposeTitle (CliQ: $cliqAlias / IBAN: $ibanNumber)"
                    )
                },
                modifier = Modifier.testTag("confirm_cliq_transfer_button")
            ) {
                Text("تم نسخ الحساب والتحويل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
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
    var partsMode by remember { mutableStateOf(PartsInclusionMode.LABOR_ONLY) }
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

                // Labor Only vs. Parts Included Selector (أجرة يد فقط أم شامل القطع؟)
                Text(
                    text = "اتفاق قطع الغيار والمواد (لمنع أي خلاف بعد الشغل):",
                    style = MaterialTheme.typography.labelLarge
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PartsInclusionMode.entries.forEach { pMode ->
                        FilterChip(
                            selected = partsMode == pMode,
                            onClick = { partsMode = pMode },
                            label = { Text(pMode.labelAr) },
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
                            add(partsMode.tag.removeSurrounding("[", "]"))
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
    var partsMode by remember { mutableStateOf(PartsInclusionMode.LABOR_ONLY) }
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

                // Labor Only vs. Parts Included Selector
                Text(
                    text = "اتفاق قطع الغيار والمواد:",
                    style = MaterialTheme.typography.labelLarge
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PartsInclusionMode.entries.forEach { pMode ->
                        FilterChip(
                            selected = partsMode == pMode,
                            onClick = { partsMode = pMode },
                            label = { Text(pMode.labelAr) },
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
                        append("${partsMode.tag} ")
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

    val context = LocalContext.current
    val portfolioItems = remember {
        val initial = existingProvider?.parsedPortfolioItems()
        mutableStateListOf<Pair<String, String>>().apply {
            if (!initial.isNullOrEmpty()) {
                initial.forEach { item ->
                    val storedTitle = if (item.mediaUri.isNotBlank()) "${item.title}::${item.mediaUri}" else item.title
                    add(storedTitle to if (item.isVideo) "VIDEO" else "PHOTO")
                }
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
    var newWorkPickedUri by remember { mutableStateOf("") }

    val portfolioPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedUriStr = savePickedPortfolioImageToStorage(context, uri)
            newWorkPickedUri = savedUriStr
            if (newWorkTitle.isBlank()) {
                newWorkTitle = "صورة عمل حقيقية موثقة (${portfolioItems.size + 1})"
            }
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

            // Family Safety Non-Conviction, Emergency 24h & Female Section Toggles
            var hasNonConvictionCert by remember {
                mutableStateOf(existingProvider?.hasNonConvictionCertificate ?: true)
            }
            var isEmergency24hResponder by remember {
                mutableStateOf(existingProvider?.isEmergency24hResponder ?: true)
            }
            var isFemaleProvider by remember {
                mutableStateOf(existingProvider?.isFemaleProvider ?: false)
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "شارات الأمان العائلي والطوارئ (تزيد ثقة الزبائن 3 أضعاف):",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛡️ لدي شهادة عدم محكومية وهوية موثقة (آمن للعائلات)",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = hasNonConvictionCert,
                            onCheckedChange = {
                                hasNonConvictionCert = it
                                if (it && !licenseTitle.contains("عدم محكومية")) {
                                    licenseTitle = "$licenseTitle + شهادة عدم محكومية موثقة"
                                }
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚨 متاح لطلبات فزعة الطوارئ 24 ساعة",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isEmergency24hResponder,
                            onCheckedChange = {
                                isEmergency24hResponder = it
                                if (it && !licenseTitle.contains("طوارئ")) {
                                    licenseTitle = "$licenseTitle • طوارئ 24 ساعة"
                                }
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👩‍💼 مقدمة خدمة (قسم السيدات والعائلات)",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isFemaleProvider,
                            onCheckedChange = {
                                isFemaleProvider = it
                                if (it && !licenseTitle.contains("قسم السيدات")) {
                                    licenseTitle = "$licenseTitle • قسم السيدات والعائلات"
                                }
                            }
                        )
                    }
                }
            }

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

            // Portfolio 10-20 Items Manager with Real Photo Upload
            Text(
                text = "معرض الأعمال الموثق بالصور الحقيقية (${portfolioItems.size} / 20 صورة وفيديو)",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick = {
                    portfolioPhotoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pick_real_portfolio_photo_button")
            ) {
                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (newWorkPickedUri.isNotBlank()) {
                        "✅ تم اختيار صورة حقيقية من جهازك (اضغط + لإضافتها للمعرض)"
                    } else {
                        "📷 اختيار صورة حقيقية أو فيديو من استوديو الهاتف"
                    }
                )
            }

            var newWorkVideoUrl by remember { mutableStateOf("") }

            OutlinedTextField(
                value = newWorkVideoUrl,
                onValueChange = {
                    newWorkVideoUrl = it
                    if (it.isNotBlank()) {
                        newWorkIsVideo = true
                        if (newWorkTitle.isBlank()) {
                            newWorkTitle = "فيديو توثيق شغل ميداني (${portfolioItems.size + 1})"
                        }
                    }
                },
                label = {
                    Text("🎥 رابط فيديو لشغلك (TikTok / YouTube Shorts / Reels) - يفتح داخل التطبيق مجاناً")
                },
                placeholder = {
                    Text("https://www.youtube.com/shorts/... أو رابط تيك توك/ريلز")
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("craftsman_video_url_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newWorkTitle,
                    onValueChange = { newWorkTitle = it },
                    label = { Text("عنوان العمل (مثال: تركيب مكيف قبل وبعد)") },
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
                            val effectiveUri = newWorkVideoUrl.trim().ifBlank { newWorkPickedUri }
                            val combinedEntry = if (effectiveUri.isNotBlank()) {
                                "${newWorkTitle.trim()}::$effectiveUri"
                            } else {
                                newWorkTitle.trim()
                            }
                            portfolioItems.add(
                                0,
                                combinedEntry to if (newWorkIsVideo || newWorkVideoUrl.isNotBlank()) "VIDEO" else "PHOTO"
                            )
                            newWorkTitle = ""
                            newWorkPickedUri = ""
                            newWorkVideoUrl = ""
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة عمل")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                portfolioItems.forEachIndexed { idx, (rawTitle, itemType) ->
                    val cleanItemTitle = rawTitle.substringBefore("::")
                    val itemUriStr = rawTitle.substringAfter("::", "")
                    val thumbBitmap = rememberPortfolioBitmap(context, itemUriStr)
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
                        if (thumbBitmap != null) {
                            Image(
                                bitmap = thumbBitmap,
                                contentDescription = cleanItemTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Icon(
                                imageVector = if (itemType == "VIDEO") Icons.Default.Videocam else Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${idx + 1}. $cleanItemTitle ${if (itemUriStr.isNotBlank()) "(📷 صورة حقيقية مرفقة)" else ""}",
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

@Composable
fun WarrantyCertificateDialog(
    booking: ServiceBooking,
    provider: ProviderProfile?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var copiedWarranty by remember { mutableStateOf(false) }
    val certNumber = remember(booking.id) { booking.warrantyCertificateNumber() }
    val partsScope = remember(booking.serviceTitle) { booking.partsInclusionLabel() }
    val vehicleLabel = remember(provider) {
        if (provider != null) {
            "${provider.vehicleModelDisplay()} (${provider.vehiclePlateDisplay()})"
        } else {
            "سيارة خدمة معتمدة في فرصني"
        }
    }

    val warrantyFullText = remember(booking, certNumber) {
        buildString {
            appendLine("🧾 سند كفالة وضمان رقمي لمدة 7 أيام — منصة فرصني الأردن")
            appendLine("رقم السند الموثق: $certNumber")
            appendLine("الخدمة المنفذة: ${booking.serviceTitle}")
            appendLine("اسم الحرفي المنفذ: ${booking.providerName} (${booking.providerPhone})")
            appendLine("التوثيق: شهادة عدم محكومية وهوية أحوال مدنية موثقة")
            appendLine("نطاق القطع والمصنعية: $partsScope")
            appendLine("المبلغ المتفق عليه: ${booking.escrowAmountJod} د.أ")
            appendLine("الموقع: ${booking.governorate} - ${booking.neighborhood}")
            appendLine("الضمان: يلتزم الحرفي بإصلاح أي ملاحظة مجاناً خلال 7 أيام من تاريخ ${booking.scheduledDate}.")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "🧾 سند كفالة رقمي لمدة 7 أيام",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "رقم السند: $certNumber • بختم منصة فرصني الأردن",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛡️ كفالة مصنعية رسمية (7 أيام)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "ساري المفعول ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        HorizontalDivider()

                        Text(
                            text = "الخدمة المشمولة بالكفالة: ${booking.serviceTitle}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "اسم الحرفي المنفذ: ${booking.providerName} (${booking.providerSkill})",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "رقم هاتف الحرفي الموثق: ${booking.providerPhone}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "بيانات المركبة المسجلة: $vehicleLabel",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "اتفاق القطع والمصنعية: $partsScope",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "قيمة العمل المتفق عليها: ${booking.escrowAmountJod} دينار أردني",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تاريخ السند: ${booking.scheduledDate} (${booking.scheduledTime}) — ${booking.governorate} / ${booking.neighborhood}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✅ شروط الكفالة: في حال ظهور نفس العطل خلال 7 أيام من تاريخ الإنجاز، يلتزم الحرفي بالعودة وإصلاحه مجاناً بدون أي أجور إضافية.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Forasni Warranty", warrantyFullText))
                            copiedWarranty = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (copiedWarranty) "تم النسخ ✓" else "نسخ السند")
                    }

                    Button(
                        onClick = {
                            try {
                                val cleanDigits = booking.providerPhone.filter { it.isDigit() }
                                val encoded = Uri.encode(warrantyFullText)
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://wa.me/$cleanDigits?text=$encoded")
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatsapp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إرسال عبر واتساب")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق السند")
            }
        }
    )
}

@Composable
fun DoorstepSafetyIdDialog(
    booking: ServiceBooking,
    provider: ProviderProfile?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var copiedPin by remember { mutableStateOf(false) }
    val securityPin = remember(booking.id) { booking.doorstepSecurityPin() }
    val badgeId = remember(booking.providerId) { booking.craftsmanDigitalBadgeId() }
    val vehicleLabel = remember(provider) {
        if (provider != null) {
            "${provider.vehicleModelDisplay()} • ${provider.vehiclePlateDisplay()}"
        } else {
            "سيارة خدمة معتمدة في فرصني"
        }
    }

    val familySafetyShareText = remember(booking, securityPin, badgeId) {
        "🪪 بطاقة تحقق وأمان للعائلة (تطبيق فرصني الأردن):\n" +
            "اسم الحرفي القادم: ${booking.providerName}\n" +
            "كود الأمان عند طرق الباب: [$securityPin]\n" +
            "رقم الهوية المهنية: $badgeId\n" +
            "السيارة: $vehicleLabel\n" +
            "تنبيه: لا تفتح الباب إلا بعد أن يذكر الحرفي كود الأمان ($securityPin)."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "🪪 كود أمان الباب وبطاقة هوية الحرفي",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "للتحقق من هوية الحرفي عند رن الجرس (أمان 100% للعائلات)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Big 4-digit PIN Callout
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "كود التحقق السري عند دق الباب (اطلبه من الحرفي):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "🔐  $securityPin",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "لا تفتح الباب إلا بعد أن يقرأ لك الحرفي هذا الرقم من تطبيقه",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Digital Craftsman ID Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "بطاقة هوية حرفي معتمد — فرصني الأردن",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = badgeId,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        HorizontalDivider()

                        Text(
                            text = "الاسم الكامل: ${booking.providerName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "المهنة والاختصاص: ${booking.providerSkill}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "حالة السجل العدلي: 🛡️ شهادة عدم محكومية وهوية أحوال مدنية موثقة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "مركبة الحرفي واللوحة: $vehicleLabel",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "رقم الهاتف المسجل: ${booking.providerPhone}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Doorstep PIN", familySafetyShareText))
                            copiedPin = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (copiedPin) "تم النسخ ✓" else "نسخ الكود للعائلة")
                    }

                    Button(
                        onClick = {
                            try {
                                val encoded = Uri.encode(familySafetyShareText)
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://wa.me/?text=$encoded")
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatsapp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إرسال الكود لأهل البيت")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
fun PartsInvoiceBreakdownDialog(
    booking: ServiceBooking,
    onDismiss: () -> Unit,
    onSaveInvoiceBreakdown: (laborJod: Long, partsCostJod: Long, storeName: String, hasNeighborDiscount: Boolean) -> Unit
) {
    val context = LocalContext.current
    var laborStr by remember { mutableStateOf(booking.escrowAmountJod.coerceAtLeast(12L).toString()) }
    var partsStr by remember { mutableStateOf("8") }
    var storeName by remember { mutableStateOf("محلات البشيتي لمواد البناء والعدد - عمّان") }
    var applyNeighborDiscount by remember { mutableStateOf(false) }
    var invoicePhotoUri by remember { mutableStateOf("") }
    var copiedGroupInvite by remember { mutableStateOf(false) }

    val receiptPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            invoicePhotoUri = savePickedPortfolioImageToStorage(context, uri)
        }
    }

    val receiptBitmap = rememberPortfolioBitmap(context, invoicePhotoUri)
    val baseLabor = laborStr.toLongOrNull() ?: 12L
    val discountedLabor = if (applyNeighborDiscount) (baseLabor * 70L / 100L).coerceAtLeast(5L) else baseLabor
    val partsCost = partsStr.toLongOrNull() ?: 0L
    val netTotalJod = discountedLabor + partsCost

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "🧾 توثيق فاتورة قطع الغيار + خصم الجيران (-30%)",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "تفصيل شفاف بالقرش: أجرة يد الحرفي + فاتورة محل القطع الأصلية",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
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
                // Quick Jordanian Hardware Store Presets
                Text(
                    text = "اختر محل مواد البناء أو قطع الغيار في الأردن:",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "محلات البشيتي - عمّان",
                        "مؤسسة قدورة للكهرباء - خلدا",
                        "مركز الصويص للسباكة - البيادر",
                        "مؤسسة التاج للتكييف - إربد"
                    ).forEach { store ->
                        FilterChip(
                            selected = storeName.startsWith(store.take(10)),
                            onClick = { storeName = store },
                            label = { Text(store) }
                        )
                    }
                }

                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("اسم محل قطع الغيار على الفاتورة") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = laborStr,
                        onValueChange = { laborStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("أجرة يد الحرفي (د.أ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = partsStr,
                        onValueChange = { partsStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("قيمة فاتورة القطع (د.أ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Upload Real Receipt Photo Button
                OutlinedButton(
                    onClick = {
                        receiptPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (invoicePhotoUri.isNotBlank()) {
                            "✅ تم إرفاق صورة فاتورة المحل الحقيقية"
                        } else {
                            "📷 إرفاق صورة فاتورة المحل المختومة (للتوثيق)"
                        }
                    )
                }

                if (receiptBitmap != null) {
                    Image(
                        bitmap = receiptBitmap,
                        contentDescription = "صورة فاتورة قطع الغيار",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                // Group Neighbor Discount -30% Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🏘️ اطلب مع جارك بنفس العمارة ووفّر 30%!",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "إذا انضم جارك في نفس العمارة لنفس زيارة الحرفي، تحصلان على خصم 30% على أجرة اليد",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = applyNeighborDiscount,
                                onCheckedChange = { applyNeighborDiscount = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = {
                                val inviteMsg = "🏘️ يا جيران العمارة الكرام، الحرفي (${booking.providerName} - ${booking.providerSkill}) جاي اليوم عالعمارة في ${booking.neighborhood}. اللي حابب ينضم معنا بنفس الزيارة يحصل على خصم 30% على أجرة اليد عبر تطبيق فرصني!"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Neighbor Discount", inviteMsg))
                                copiedGroupInvite = true
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
                                if (copiedGroupInvite) {
                                    "تم نسخ دعوة الجيران لقروب العمارة ✓"
                                } else {
                                    "نسخ دعوة خصم 30% لقروب واتساب العمارة"
                                }
                            )
                        }
                    }
                }

                // Itemized Summary Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ملخص الفاتورة النهائية الشفافة:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "• أجرة يد الحرفي: $discountedLabor د.أ ${if (applyNeighborDiscount) "(بعد خصم الجيران 30% بدلاً من $baseLabor د.أ)" else ""}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• قطع الغيار بفاتورة ($storeName): $partsCost د.أ",
                            style = MaterialTheme.typography.bodySmall
                        )
                        HorizontalDivider()
                        Text(
                            text = "المجموع الصافي للدفع (كليك / نقداً): $netTotalJod دينار أردني",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveInvoiceBreakdown(
                        baseLabor,
                        partsCost,
                        storeName.ifBlank { "محل مواد بناء معتمد" },
                        applyNeighborDiscount
                    )
                },
                modifier = Modifier.testTag("save_parts_invoice_button")
            ) {
                Text("اعتماد الفاتورة ($netTotalJod د.أ)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
fun HomeInspectionRequestDialog(
    onDismiss: () -> Unit,
    onConfirmInspection: (
        purposeLabel: String,
        governorate: String,
        neighborhood: String,
        scheduledDate: String,
        priceJod: Long
    ) -> Unit
) {
    var selectedPurpose by remember { mutableStateOf("فحص شقة قبل الشراء (قبل دفع العربون)") }
    var selectedGov by remember { mutableStateOf("عمّان") }
    var neighborhood by remember { mutableStateOf("خلدا") }
    var dateStr by remember { mutableStateOf("اليوم - 04:00 مساءً") }
    var includeThermalLeakCheck by remember { mutableStateOf(true) }

    val totalPriceJod = if (includeThermalLeakCheck) 25L else 18L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "🏠 فحص شقة شامل قبل الشراء أو الإيجار",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "لا تتفاجأ بالرطوبة أو مشاكل السباكة والكهرباء بعد السكن!",
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
                Text(
                    text = "نوع الفحص المطلوب:",
                    style = MaterialTheme.typography.labelLarge
                )
                listOf(
                    "فحص شقة قبل الشراء (قبل دفع العربون)",
                    "فحص شقة قبل الإيجار واستلام المفاتيح",
                    "فحص فيلا / بيت مستقل قبل الترميم"
                ).forEach { purpose ->
                    FilterChip(
                        selected = selectedPurpose == purpose,
                        onClick = { selectedPurpose = purpose },
                        label = { Text(purpose) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "📋 قائمة الفحص الهندسي (40 نقطة فحص):",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text("✓ فحص ضغط مواسير المياه، الخزانات، المضخة، وتصريف الحمامات", style = MaterialTheme.typography.bodySmall)
                        Text("✓ فحص لوحة القواطع (3 فاز/1 فاز)، الأحمال، والأباريز", style = MaterialTheme.typography.bodySmall)
                        Text("✓ فحص رطوبة الجدران، عزل السطح، والأباجورات والشبابيك", style = MaterialTheme.typography.bodySmall)
                        Text("✓ فحص بويلر التدفئة المركزية، الراديترات، والمكيفات", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "إضافة فحص كاميرا التسريب الحراري والرطوبة المخفية",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "يكشف أي تسريب مياه خلف البلاط بدون تكسير (+7 د.أ)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = includeThermalLeakCheck,
                        onCheckedChange = { includeThermalLeakCheck = it }
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = neighborhood,
                        onValueChange = { neighborhood = it },
                        label = { Text("الحي السكني") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("موعد الفحص") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "تكلفة الفحص الشامل مع التقرير المكتوب: $totalPriceJod دينار أردني",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmInspection(
                        selectedPurpose,
                        selectedGov,
                        neighborhood.ifBlank { "خلدا" },
                        dateStr.ifBlank { "اليوم" },
                        totalPriceJod
                    )
                },
                modifier = Modifier.testTag("confirm_home_inspection_button")
            ) {
                Text("حجز مهندس الفحص ($totalPriceJod د.أ)")
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
fun ApartmentMovingCalculatorDialog(
    onDismiss: () -> Unit,
    onConfirmMovingBooking: (
        roomsLabel: String,
        floorLabel: String,
        hasElevator: Boolean,
        extrasLabel: String,
        fromArea: String,
        toArea: String,
        totalPriceJod: Long
    ) -> Unit
) {
    var selectedRoomsIdx by remember { mutableIntStateOf(1) } // 0: Studio, 1: 2BR, 2: 3BR, 3: Villa
    var selectedFloorIdx by remember { mutableIntStateOf(1) } // 0: Ground/1st, 1: 2nd-3rd, 2: 4th+
    var hasElevator by remember { mutableStateOf(true) }
    var includeCarpentryDismantle by remember { mutableStateOf(true) }
    var includeAcMoving by remember { mutableStateOf(false) }
    var includeBubbleWrapPacking by remember { mutableStateOf(true) }
    var fromArea by remember { mutableStateOf("عمّان - طبربور") }
    var toArea by remember { mutableStateOf("عمّان - خلدا") }

    val roomsOptions = listOf(
        "استوديو / غرفة وصالة" to 25L,
        "شقة غرفتين نوم" to 40L,
        "شقة 3 غرف نوم" to 55L,
        "فيلا / منزل كبير" to 85L
    )
    val floorOptions = listOf(
        "طابق أرضي / أول" to 0L,
        "طابق ثاني أو ثالث" to 10L,
        "طابق رابع فما فوق" to 20L
    )

    val baseRoomCost = roomsOptions[selectedRoomsIdx].second
    val floorSurcharge = if (hasElevator) floorOptions[selectedFloorIdx].second / 2 else floorOptions[selectedFloorIdx].second
    val carpentryCost = if (includeCarpentryDismantle) 15L else 0L
    val acCost = if (includeAcMoving) 15L else 0L
    val packingCost = if (includeBubbleWrapPacking) 10L else 0L
    val totalMovingJod = baseRoomCost + floorSurcharge + carpentryCost + acCost + packingCost

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "🚚 حاسبة نقل عفش وترحيل الشقق بالأردن",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "احسب التكلفة العادلة فوراً مع باص نقل مغلق ونجار لفك وتركيب غرف النوم",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
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
                Text(text = "1. حجم الشقة أو العفش:", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    roomsOptions.forEachIndexed { idx, (label, cost) ->
                        FilterChip(
                            selected = selectedRoomsIdx == idx,
                            onClick = { selectedRoomsIdx = idx },
                            label = { Text("$label (أساسي: $cost د.أ)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Text(text = "2. الطابق وتوفر المصعد (الأصنصير):", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    floorOptions.forEachIndexed { idx, (label, _) ->
                        FilterChip(
                            selected = selectedFloorIdx == idx,
                            onClick = { selectedFloorIdx = idx },
                            label = { Text(label) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "هل يتوفر مصعد متاح لتحميل الأثاث؟",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = hasElevator,
                        onCheckedChange = { hasElevator = it }
                    )
                }

                Text(text = "3. خدمات الفك والتركيب والتغليف:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔧 نجار لفك وتركيب غرف النوم والبرادي (+15 د.أ)", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = includeCarpentryDismantle, onCheckedChange = { includeCarpentryDismantle = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("❄️ فني لفك ونقل وتركيب المكيفات مع حفظ الغاز (+15 د.أ)", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = includeAcMoving, onCheckedChange = { includeAcMoving = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📦 تغليف الشاشات والزجاج ببلاستيك الفقاعات (+10 د.أ)", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = includeBubbleWrapPacking, onCheckedChange = { includeBubbleWrapPacking = it })
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fromArea,
                        onValueChange = { fromArea = it },
                        label = { Text("النقل من منطقة") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = toArea,
                        onValueChange = { toArea = it },
                        label = { Text("إلى منطقة") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "السعر العادل الشامل للنقل والتركيب: $totalMovingJod دينار أردني",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "يشمل باص نقل مغلق هونداي H100 + العمال + كفالة عدم الخدش أو الكسر.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val extras = buildList {
                        if (includeCarpentryDismantle) add("فك وتركيب نجارة")
                        if (includeAcMoving) add("فك وتركيب مكيف")
                        if (includeBubbleWrapPacking) add("تغليف آمن")
                    }.joinToString(" + ").ifEmpty { "نقل فقط" }
                    onConfirmMovingBooking(
                        roomsOptions[selectedRoomsIdx].first,
                        floorOptions[selectedFloorIdx].first,
                        hasElevator,
                        extras,
                        fromArea.ifBlank { "عمّان" },
                        toArea.ifBlank { "خلدا" },
                        totalMovingJod
                    )
                },
                modifier = Modifier.testTag("confirm_moving_calculator_button")
            ) {
                Text("حجز الباص والفريق ($totalMovingJod د.أ)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * تحويل روابط تيك توك أو يوتيوب شورتس أو ريلز إلى رابط مشغّل مدمج (Embed URL) يفتح داخل التطبيق فقط
 */
private fun toEmbeddedVideoUrl(rawUrl: String): String {
    val trimmed = rawUrl.trim()
    if (trimmed.isEmpty()) return ""
    return when {
        trimmed.contains("youtube.com/shorts/") -> {
            val id = trimmed.substringAfter("youtube.com/shorts/").substringBefore("?").substringBefore("/")
            "https://www.youtube.com/embed/$id?autoplay=1&playsinline=1&rel=0"
        }
        trimmed.contains("youtu.be/") -> {
            val id = trimmed.substringAfter("youtu.be/").substringBefore("?").substringBefore("/")
            "https://www.youtube.com/embed/$id?autoplay=1&playsinline=1&rel=0"
        }
        trimmed.contains("youtube.com/watch?v=") -> {
            val id = trimmed.substringAfter("v=").substringBefore("&")
            "https://www.youtube.com/embed/$id?autoplay=1&playsinline=1&rel=0"
        }
        trimmed.contains("tiktok.com/") && trimmed.contains("/video/") -> {
            val id = trimmed.substringAfter("/video/").substringBefore("?").substringBefore("/")
            "https://www.tiktok.com/embed/v2/$id"
        }
        else -> trimmed
    }
}

/**
 * مشغّل فيديو توثيق شغل الحرفي المدمج داخل تطبيق «فرصني» (In-App Video Player Dialog)
 * يشتغل داخل التطبيق نفسه بدون نقل الزبون إلى تيك توك أو فيسبوك، مع زر حجز فوري بالأسفل.
 */
@Suppress("SetJavaScriptEnabled")
@Composable
fun InAppVideoPlayerDialog(
    provider: ProviderProfile,
    media: PortfolioMediaItem,
    onDismiss: () -> Unit,
    onBookNow: () -> Unit
) {
    val hasExternalEmbedUrl = remember(media.mediaUri) {
        media.mediaUri.startsWith("http://") || media.mediaUri.startsWith("https://")
    }
    var useWebEmbedMode by remember(hasExternalEmbedUrl) { mutableStateOf(hasExternalEmbedUrl) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentSecond by remember { mutableIntStateOf(0) }
    val totalSeconds = 35

    val stages = remember(provider) {
        listOf(
            "00:00 - 00:10 • فحص العطل والموقع في ${provider.neighborhood} وتجهيز المعدات",
            "00:11 - 00:24 • تنفيذ شغل (${provider.skillTitle}) بدقة ونظافة عالية",
            "00:25 - 00:35 • الفحص النهائي قبل وبعد الشغل وتسليم الكفالة للزبون ✅"
        )
    }

    LaunchedEffect(isPlaying, useWebEmbedMode) {
        while (isPlaying && !useWebEmbedMode) {
            delay(1000L)
            currentSecond = (currentSecond + 1) % (totalSeconds + 1)
        }
    }

    val currentStageCaption = when {
        currentSecond <= 10 -> stages[0]
        currentSecond <= 24 -> stages[1]
        else -> stages[2]
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "مشغّل فيديو «فرصني» الداخلي 🎥",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "توثيق شغل المعلم ${provider.name} (داخل التطبيق بدون خروج)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasExternalEmbedUrl) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = useWebEmbedMode,
                            onClick = { useWebEmbedMode = true },
                            label = { Text("مشغّل الفيديو المدمج") }
                        )
                        FilterChip(
                            selected = !useWebEmbedMode,
                            onClick = { useWebEmbedMode = false },
                            label = { Text("معاينة مراحل الشغل") }
                        )
                    }
                }

                if (useWebEmbedMode && hasExternalEmbedUrl) {
                    val embedUrl = remember(media.mediaUri) { toEmbeddedVideoUrl(media.mediaUri) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.mediaPlaybackRequiresUserGesture = false
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            val url = request?.url?.toString() ?: return true
                                            // منع الخروج إلى تطبيق تيك توك أو فيسبوك الخارجي وإبقاء الزبون داخل فرصني
                                            return !(url.startsWith("http://") || url.startsWith("https://"))
                                        }
                                    }
                                    loadUrl(embedUrl)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(260.dp)
                        )
                    }
                } else {
                    // مشغّل الفيديو التفاعلي المدمج داخل التطبيق
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                            .clickable { isPlaying = !isPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_forasni_hero_banner),
                            contentDescription = media.title,
                            contentScale = ContentScale.Crop,
                            alpha = 0.38f,
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                        )

                        // شارة "يعمل داخل التطبيق" في الأعلى
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (isPlaying) "🔴 يعمل الآن داخل فرصني • بدون إعلانات" else "⏸ متوقف مؤقتاً",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.StopCircle else Icons.Default.PlayCircleFilled,
                                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل الفيديو",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = currentStageCaption,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // شريط التقدم الزمني للفيديو بالأسفل
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { currentSecond.toFloat() / totalSeconds.toFloat() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = String.format(Locale.US, "00:%02d / 00:%02d", currentSecond, totalSeconds),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "اضغط على الشاشة للإيقاف/التشغيل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "💡 ميزة المشغّل الداخلي: يفتح الفيديو داخل تطبيق «فرصني» مباشرة بدون نقل الزبون لتيك توك أو فيسبوك، وبدون أي تكلفة سيرفرات تخزين سحابي عليك!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onBookNow,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.testTag("video_player_book_now_button")
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("✅ عجبك الشغل؟ احجز مع المعلم الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق الفيديو")
            }
        }
    )
}



