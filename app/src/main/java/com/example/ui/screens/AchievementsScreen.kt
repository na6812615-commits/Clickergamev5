package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AchievementCatalog
import com.example.data.AchievementDef
import com.example.data.AchievementEntity
import com.example.data.GameStateEntity
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDim
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldDim
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AchievementsScreen(
    game: GameStateEntity,
    records: Map<String, AchievementEntity> = emptyMap(),
    onClaimAchievement: (AchievementDef) -> Unit,
    onOpenBadgeOverlay: () -> Unit = {},
    onOpenLeaderboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unlockedCount = AchievementCatalog.all.count { def ->
        (records[def.id]?.isUnlocked == true) || def.progressSelector(game) >= def.targetValue
    }
    val claimedCount = AchievementCatalog.all.count { game.isAchievementClaimed(it.id) || records[it.id]?.isClaimed == true }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("achievements_screen"),
        contentPadding = PaddingValues(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NHIỆM VỤ & THÀNH TỰU",
                        style = MaterialTheme.typography.titleLarge,
                        color = AmberGold
                    )
                    Text(
                        text = "Đã mở $unlockedCount/${AchievementCatalog.all.size} huy hiệu • Đã nhận $claimedCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onOpenBadgeOverlay,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberGold,
                            contentColor = ObsidianBgDeep
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("open_badge_overlay_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MilitaryTech,
                            contentDescription = "Mở tủ huy hiệu",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Huy hiệu", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = onOpenLeaderboard,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("open_leaderboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Leaderboard,
                            contentDescription = "Mở bảng xếp hạng",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("BXH", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Lifetime Records Summary
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CarbonSurface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RecordStat(
                        label = "Tổng điểm đời",
                        value = Formatters.formatPoints(game.lifetimePoints),
                        color = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    RecordStat(
                        label = "Số lần Crit",
                        value = Formatters.formatLong(game.totalCrits),
                        color = AmberGold,
                        modifier = Modifier.weight(1f)
                    )
                    RecordStat(
                        label = "Kỷ lục Combo",
                        value = "${game.bestCombo}x",
                        color = NeonEmerald,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        items(AchievementCatalog.all, key = { it.id }) { def ->
            val rec = records[def.id]
            val currentVal = maxOf(def.progressSelector(game), rec?.progressValue ?: 0.0)
            val isCompleted = (rec?.isUnlocked == true) || (currentVal >= def.targetValue)
            val isClaimed = game.isAchievementClaimed(def.id) || (rec?.isClaimed == true)
            val progress = if (isCompleted) 1f else (currentVal / def.targetValue).toFloat().coerceIn(0f, 1f)

            AchievementCard(
                def = def,
                currentValue = if (isCompleted) def.targetValue else currentVal,
                progress = progress,
                isCompleted = isCompleted,
                isClaimed = isClaimed,
                onClaim = { onClaimAchievement(def) }
            )
        }
    }
}

@Composable
private fun RecordStat(
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
private fun AchievementCard(
    def: AchievementDef,
    currentValue: Double,
    progress: Float,
    isCompleted: Boolean,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    val accent = when {
        isClaimed -> NeonEmerald
        isCompleted -> AmberGold
        else -> CyberCyan
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CarbonSurface.copy(alpha = 0.92f),
        border = BorderStroke(
            1.dp,
            when {
                isCompleted && !isClaimed -> AmberGold.copy(alpha = 0.7f)
                isClaimed -> NeonEmerald.copy(alpha = 0.35f)
                else -> CardBorder
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        painter = painterResource(R.drawable.ic_trophy),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = def.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = def.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${Formatters.formatPoints(currentValue.coerceAtMost(def.targetValue))} / ${Formatters.formatPoints(def.targetValue)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (def.rewardDollars > 0.0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonEmeraldDim
                        ) {
                            Text(
                                text = "+${Formatters.formatDollars(def.rewardDollars)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (def.rewardShards > 0L) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF281C4A)
                        ) {
                            Text(
                                text = "+${def.rewardShards}✦",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricViolet,
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
                color = accent,
                trackColor = ElevatedCard
            )

            if (isCompleted || isClaimed) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onClaim,
                    enabled = !isClaimed,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = ObsidianBgDeep,
                        disabledContainerColor = ElevatedCard,
                        disabledContentColor = NeonEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("claim_achievement_${def.id}")
                ) {
                    Text(
                        text = if (isClaimed) "ĐÃ NHẬN THƯỞNG" else "NHẬN THƯỞNG NGAY",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
