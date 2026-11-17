package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameStateEntity
import com.example.data.SeasonPassCatalog
import com.example.data.SeasonTierDef
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDim
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.DiamondBlue
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SeasonPassScreen(
    game: GameStateEntity,
    unclaimedCount: Int,
    onUnlockElite: () -> Unit,
    onClaimTier: (level: Int, isElite: Boolean) -> Unit,
    onClaimAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val currentLv = game.seasonLevel
    val nextTier = SeasonPassCatalog.nextTierForXp(game.seasonXp)
    val prevReq = SeasonPassCatalog.tiers.firstOrNull { it.level == currentLv }?.requiredCumulativeXp ?: 0L
    val progressFraction = if (nextTier == null) {
        1f
    } else {
        val span = (nextTier.requiredCumulativeXp - prevReq).coerceAtLeast(1L)
        ((game.seasonXp - prevReq).toFloat() / span.toFloat()).coerceIn(0f, 1f)
    }
    val (eliteDollars, eliteShards) = SeasonPassCatalog.eliteUnlockCost(game.hasVipPass)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.98f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
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
                            contentColor = CyberCyan
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_season_pass_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = SeasonPassCatalog.SEASON_NAME,
                            style = MaterialTheme.typography.titleMedium,
                            color = ElectricViolet,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cấp $currentLv/${SeasonPassCatalog.MAX_LEVEL} • Buff Mùa: x${Formatters.formatDecimal(game.seasonMultiplier)} Điểm",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                if (unclaimedCount > 0) {
                    Button(
                        onClick = onClaimAll,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonEmerald,
                            contentColor = ObsidianBgDeep
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("claim_all_season_button")
                    ) {
                        Text("Nhận Tất Cả ($unclaimedCount)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(148.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img_season_pass_banner),
                            contentDescription = "Banner Mùa Giải Lượng Tử",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            ObsidianBgDeep.copy(alpha = 0.92f)
                                        )
                                    )
                                )
                                .padding(14.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CẤP MÙA $currentLv",
                                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (nextTier == null) "MAX XP: ${game.seasonXp}" else "${game.seasonXp} / ${nextTier.requiredCumulativeXp} XP",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(99.dp)),
                                    color = ElectricViolet,
                                    trackColor = ElevatedCard
                                )
                            }
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (game.seasonPassPremium) AmberGoldDim.copy(alpha = 0.6f) else Color(0xFF221640),
                        border = BorderStroke(1.dp, if (game.seasonPassPremium) RoyalGold else ElectricViolet)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = RoyalGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (game.seasonPassPremium) "CYBER ELITE PASS ĐÃ MỞ KHÓA" else "MỞ KHÓA CYBER ELITE PASS",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = RoyalGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "+50% tốc độ cày Season XP & mở toàn bộ nhánh quà Cao Cấp (Điểm, Tiền $ & Mảnh Lõi ✦ giá trị)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            if (!game.seasonPassPremium) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = onUnlockElite,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RoyalGold,
                                        contentColor = ObsidianBgDeep
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("unlock_elite_pass_button")
                                ) {
                                    Text("$${eliteDollars.toLong()} + ${eliteShards}✦", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                items(SeasonPassCatalog.tiers, key = { it.level }) { tier ->
                    SeasonTierRow(
                        tier = tier,
                        currentLevel = currentLv,
                        hasElite = game.seasonPassPremium,
                        freeClaimed = game.isSeasonFreeClaimed(tier.level),
                        eliteClaimed = game.isSeasonEliteClaimed(tier.level),
                        onClaimFree = { onClaimTier(tier.level, false) },
                        onClaimElite = { onClaimTier(tier.level, true) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SeasonTierRow(
    tier: SeasonTierDef,
    currentLevel: Int,
    hasElite: Boolean,
    freeClaimed: Boolean,
    eliteClaimed: Boolean,
    onClaimFree: () -> Unit,
    onClaimElite: () -> Unit
) {
    val unlocked = currentLevel >= tier.level
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("season_tier_${tier.level}"),
        shape = RoundedCornerShape(16.dp),
        color = CarbonSurface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, if (unlocked) CyberCyan.copy(alpha = 0.55f) else CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (unlocked) CyberCyanDim else ElevatedCard
                ) {
                    Text(
                        text = "CẤP ${tier.level}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (unlocked) CyberCyan else TextMuted,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "${tier.requiredCumulativeXp} XP",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SeasonTrackTile(
                    trackLabel = "MIỄN PHÍ",
                    rewardLabel = tier.freeReward.label,
                    unlocked = unlocked,
                    claimed = freeClaimed,
                    isLockedByPaywall = false,
                    accent = CyberCyan,
                    onClick = onClaimFree,
                    modifier = Modifier.weight(1f)
                )
                SeasonTrackTile(
                    trackLabel = "ELITE VIP",
                    rewardLabel = tier.eliteReward.label,
                    unlocked = unlocked,
                    claimed = eliteClaimed,
                    isLockedByPaywall = !hasElite,
                    accent = RoyalGold,
                    onClick = onClaimElite,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SeasonTrackTile(
    trackLabel: String,
    rewardLabel: String,
    unlocked: Boolean,
    claimed: Boolean,
    isLockedByPaywall: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ElevatedCard.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, if (unlocked && !claimed && !isLockedByPaywall) accent else CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = trackLabel,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = rewardLabel,
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            when {
                claimed -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Đã nhận",
                            tint = NeonEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đã nhận", style = MaterialTheme.typography.labelMedium, color = NeonEmerald)
                    }
                }
                isLockedByPaywall -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Cần Elite Pass",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cần Elite", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    }
                }
                unlocked -> {
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = ObsidianBgDeep
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text("Nhận Quà", fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Text("Chưa đủ XP", style = MaterialTheme.typography.labelMedium, color = DiamondBlue.copy(alpha = 0.6f))
                }
            }
        }
    }
}
