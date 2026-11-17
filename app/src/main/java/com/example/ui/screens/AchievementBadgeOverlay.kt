package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.LocalSoundManager
import com.example.data.AchievementCatalog
import com.example.data.AchievementCategory
import com.example.data.AchievementDef
import com.example.data.AchievementEntity
import com.example.data.BadgeTier
import com.example.data.GameStateEntity
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDim
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldDim
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class BadgeFilter(val label: String) {
    ALL("Tất cả"),
    UNLOCKED("Đã mở khóa"),
    CLAIMABLE("Chờ nhận"),
    CLICKING("Click"),
    PRESTIGE("Prestige")
}

@Composable
fun AchievementBadgeOverlay(
    game: GameStateEntity,
    records: Map<String, AchievementEntity>,
    onClaimAchievement: (AchievementDef) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val soundManager = LocalSoundManager.current
    var selectedFilter by remember { mutableIntStateOf(0) }

    val badgeEntries = remember(game, records) {
        AchievementCatalog.all.map { def ->
            val rec = records[def.id]
            val liveProgress = def.progressSelector(game).coerceAtLeast(0.0)
            val savedProgress = rec?.progressValue ?: 0.0
            val effectiveProgress = maxOf(liveProgress, savedProgress)
            val isUnlocked = (rec?.isUnlocked == true) || (liveProgress >= def.targetValue)
            val isClaimed = game.isAchievementClaimed(def.id) || (rec?.isClaimed == true)
            BadgeDisplayItem(
                def = def,
                record = rec,
                effectiveProgress = if (isUnlocked) def.targetValue else effectiveProgress,
                isUnlocked = isUnlocked,
                isClaimed = isClaimed
            )
        }
    }

    val unlockedCount = badgeEntries.count { it.isUnlocked }
    val claimableCount = badgeEntries.count { it.isUnlocked && !it.isClaimed }

    val filtered = remember(badgeEntries, selectedFilter) {
        when (BadgeFilter.entries[selectedFilter]) {
            BadgeFilter.ALL -> badgeEntries.sortedByDescending { it.isUnlocked }
            BadgeFilter.UNLOCKED -> badgeEntries.filter { it.isUnlocked }
            BadgeFilter.CLAIMABLE -> badgeEntries.filter { it.isUnlocked && !it.isClaimed }
            BadgeFilter.CLICKING -> badgeEntries.filter { it.def.category == AchievementCategory.CLICKING }
            BadgeFilter.PRESTIGE -> badgeEntries.filter { it.def.category == AchievementCategory.PRESTIGE }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.98f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("achievement_badge_overlay"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Top Overlay Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(
                        onClick = onClose,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = ElevatedCard,
                            contentColor = AmberGold
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_badge_overlay_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Đóng Tủ Huy Hiệu")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TỦ HUY HIỆU THÀNH TỰU",
                            style = MaterialTheme.typography.titleLarge,
                            color = AmberGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Đã mở khóa $unlockedCount / ${AchievementCatalog.all.size} huy hiệu • Room DB",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AmberGoldDim,
                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = "$unlockedCount/${AchievementCatalog.all.size}",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = AmberGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trophy Summary Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CarbonSurface,
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryBadgeStat(
                        label = "Huy Hiệu Mở",
                        value = "$unlockedCount",
                        color = AmberGold,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryBadgeStat(
                        label = "Chờ Nhận",
                        value = "$claimableCount",
                        color = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryBadgeStat(
                        label = "Tổng Click",
                        value = Formatters.formatLong(game.clicks),
                        color = NeonEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryBadgeStat(
                        label = "Prestige",
                        value = "LV.${game.restartLv}",
                        color = ElectricViolet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BadgeFilter.entries.indices.toList()) { idx ->
                    val filter = BadgeFilter.entries[idx]
                    val label = when (filter) {
                        BadgeFilter.UNLOCKED -> "${filter.label} ($unlockedCount)"
                        BadgeFilter.CLAIMABLE -> "${filter.label} ($claimableCount)"
                        else -> filter.label
                    }
                    FilterChip(
                        selected = selectedFilter == idx,
                        onClick = {
                            soundManager.playButtonClick()
                            selectedFilter = idx
                        },
                        label = { Text(label, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberGold,
                            selectedLabelColor = ObsidianBgDeep,
                            containerColor = CarbonSurface,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("badge_filter_$idx")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = CarbonSurface,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Chưa có huy hiệu nào trong mục này",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(filtered, key = { it.def.id }) { item ->
                        BadgeShowcaseCard(
                            item = item,
                            onClaim = { onClaimAchievement(item.def) }
                        )
                    }
                }
            }
        }
    }
}

private data class BadgeDisplayItem(
    val def: AchievementDef,
    val record: AchievementEntity?,
    val effectiveProgress: Double,
    val isUnlocked: Boolean,
    val isClaimed: Boolean
)

@Composable
private fun SummaryBadgeStat(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BadgeShowcaseCard(
    item: BadgeDisplayItem,
    onClaim: () -> Unit
) {
    val def = item.def
    val tierColor = when (def.badgeTier) {
        BadgeTier.BRONZE -> Color(0xFFD98A4E)
        BadgeTier.SILVER -> CyberCyan
        BadgeTier.GOLD -> AmberGold
        BadgeTier.QUANTUM -> ElectricViolet
    }
    val activeAccent = if (item.isUnlocked) tierColor else TextMuted
    val progress = (item.effectiveProgress / def.targetValue).toFloat().coerceIn(0f, 1f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("badge_card_${def.id}"),
        shape = RoundedCornerShape(16.dp),
        color = if (item.isUnlocked) CarbonSurface else CarbonSurface.copy(alpha = 0.65f),
        border = BorderStroke(
            1.dp,
            when {
                item.isUnlocked && !item.isClaimed -> RoyalGold
                item.isUnlocked -> tierColor.copy(alpha = 0.55f)
                else -> CardBorder
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HexBadgeEmblem(
                    isUnlocked = item.isUnlocked,
                    tierColor = tierColor,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = def.badgeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (item.isUnlocked) activeAccent else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = activeAccent.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = when (def.badgeTier) {
                                    BadgeTier.BRONZE -> "ĐỒNG"
                                    BadgeTier.SILVER -> "BẠC"
                                    BadgeTier.GOLD -> "VÀNG"
                                    BadgeTier.QUANTUM -> "LƯỢNG TỬ"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = activeAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${def.title} — ${def.description}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.isUnlocked) TextPrimary else TextMuted
                    )

                    val unlockedAt = item.record?.unlockedAt ?: 0L
                    if (item.isUnlocked && unlockedAt > 0L) {
                        val fmt = remember(unlockedAt) {
                            SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(Date(unlockedAt))
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Đã mở khóa lúc $fmt",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${Formatters.formatPoints(item.effectiveProgress.coerceAtMost(def.targetValue))} / ${Formatters.formatPoints(def.targetValue)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = if (item.isUnlocked) activeAccent else TextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (def.rewardDollars > 0.0) {
                        Surface(shape = RoundedCornerShape(6.dp), color = NeonEmeraldDim) {
                            Text(
                                text = "+${Formatters.formatDollars(def.rewardDollars)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (def.rewardShards > 0L) {
                        Surface(shape = RoundedCornerShape(6.dp), color = CyberCyanDim) {
                            Text(
                                text = "+${def.rewardShards}✦",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = activeAccent,
                trackColor = ElevatedCard
            )

            if (item.isUnlocked && !item.isClaimed) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onClaim,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = ObsidianBgDeep
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("overlay_claim_badge_${def.id}")
                ) {
                    Text("NHẬN THƯỞNG HUY HIỆU", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HexBadgeEmblem(
    isUnlocked: Boolean,
    tierColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f - 2.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)
            val path = Path()
            for (i in 0 until 6) {
                val angle = (i * 60.0 - 90.0) * PI / 180.0
                val pt = Offset(
                    x = center.x + radius * cos(angle).toFloat(),
                    y = center.y + radius * sin(angle).toFloat()
                )
                if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
            path.close()

            if (isUnlocked) {
                drawPath(
                    path = path,
                    brush = Brush.radialGradient(
                        colors = listOf(tierColor.copy(alpha = 0.45f), ObsidianBgDeep)
                    )
                )
                drawPath(
                    path = path,
                    color = tierColor,
                    style = Stroke(width = 2.2.dp.toPx())
                )
            } else {
                drawPath(
                    path = path,
                    color = ElevatedCard
                )
                drawPath(
                    path = path,
                    color = CardBorder,
                    style = Stroke(width = 1.4.dp.toPx())
                )
            }
        }

        Icon(
            imageVector = if (isUnlocked) Icons.Rounded.MilitaryTech else Icons.Rounded.Lock,
            contentDescription = if (isUnlocked) "Huy hiệu đã mở khóa" else "Huy hiệu đang khóa",
            tint = if (isUnlocked) tierColor else TextMuted,
            modifier = Modifier.size(26.dp)
        )
    }
}
