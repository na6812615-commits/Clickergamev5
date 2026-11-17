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
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.example.data.DailyRewardCatalog
import com.example.data.DailyRewardDef
import com.example.data.GameStateEntity
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDim
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DiamondBlue
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

@Composable
fun DailyRewardsScreen(
    game: GameStateEntity,
    canClaimToday: Boolean,
    onClaimDaily: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val nextDay = DailyRewardCatalog.nextClaimDayIndex(game)
    val completedDaysInCycle = if (canClaimToday) nextDay - 1 else nextDay

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClose,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = ElevatedCard,
                            contentColor = CyberCyan
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_daily_rewards_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ĐIỂM DANH HÀNG NGÀY",
                            style = MaterialTheme.typography.titleLarge,
                            color = AmberGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Chuỗi liên tiếp: ${game.dailyStreak} ngày • Tổng nhận: ${game.totalDailyClaims}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
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
                            .height(152.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img_daily_rewards_banner),
                            contentDescription = "Kho báu điểm danh hàng ngày",
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
                                            ObsidianBgDeep.copy(alpha = 0.88f)
                                        )
                                    )
                                )
                                .padding(14.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (canClaimToday) "QUÀ NGÀY $nextDay ĐÃ SẴN SÀNG!" else "ĐÃ ĐIỂM DANH HÔM NAY",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (canClaimToday) RoyalGold else NeonEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (game.hasVipPass) "VIP Omega kích hoạt: x2 toàn bộ quà!" else "Duy trì chuỗi 7 ngày để nhận +25% thưởng!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                                Button(
                                    onClick = onClaimDaily,
                                    enabled = canClaimToday,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberGold,
                                        contentColor = ObsidianBgDeep,
                                        disabledContainerColor = ElevatedCard,
                                        disabledContentColor = TextMuted
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("claim_daily_reward_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CardGiftcard,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (canClaimToday) "NHẬN QUÀ" else "ĐÃ NHẬN",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                items(DailyRewardCatalog.weekRewards, key = { it.dayNumber }) { def ->
                    val isClaimed = def.dayNumber <= completedDaysInCycle
                    val isCurrentTarget = canClaimToday && def.dayNumber == nextDay
                    DailyRewardCard(
                        def = def,
                        game = game,
                        isClaimed = isClaimed,
                        isCurrentTarget = isCurrentTarget,
                        onClaim = onClaimDaily
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyRewardCard(
    def: DailyRewardDef,
    game: GameStateEntity,
    isClaimed: Boolean,
    isCurrentTarget: Boolean,
    onClaim: () -> Unit
) {
    val borderColor = when {
        isCurrentTarget -> AmberGold
        isClaimed -> NeonEmerald.copy(alpha = 0.55f)
        else -> CardBorder
    }
    val containerColor = when {
        isCurrentTarget -> AmberGoldDim.copy(alpha = 0.72f)
        isClaimed -> NeonEmeraldDim.copy(alpha = 0.45f)
        else -> CarbonSurface.copy(alpha = 0.94f)
    }

    val scaledPts = DailyRewardCatalog.scaledPointsFor(def, game) * (if (game.hasVipPass) 2.0 else 1.0)
    val mult = if (game.hasVipPass) 2L else 1L

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_reward_day_${def.dayNumber}"),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(if (isCurrentTarget) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (def.dayNumber == 7) RoyalGold else ElevatedCard
                    ) {
                        Text(
                            text = "NGÀY ${def.dayNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (def.dayNumber == 7) ObsidianBgDeep else CyberCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = def.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "+$${(def.dollars * mult).toLong()} • +${def.shards * mult}✦ • +${Formatters.formatPoints(scaledPts)}đ • +${def.seasonXp} XP",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            when {
                isClaimed -> {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Đã nhận",
                        tint = NeonEmerald,
                        modifier = Modifier.size(30.dp)
                    )
                }
                isCurrentTarget -> {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberGold,
                            contentColor = ObsidianBgDeep
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Nhận", fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Text(
                        text = "Sắp tới",
                        style = MaterialTheme.typography.labelMedium,
                        color = DiamondBlue.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
