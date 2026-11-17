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
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Whatshot
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.audio.LocalSoundManager
import com.example.data.BoosterType
import com.example.data.BossCatalog
import com.example.data.DailyQuestCatalog
import com.example.data.DailyQuestDef
import com.example.data.RelicId
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDim
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CoralCrimsonDim
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
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
import com.example.viewmodel.GameUiState

@Composable
fun BossAndRelicsScreen(
    uiState: GameUiState,
    onAttackBoss: () -> Unit,
    onUpgradeRelic: (RelicId) -> Unit,
    onActivateBooster: (BoosterType) -> Unit,
    onClaimDailyQuest: (DailyQuestDef) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val game = uiState.game
    val soundManager = LocalSoundManager.current
    var subTab by remember { mutableIntStateOf(0) } // 0 = Boss & Boosters, 1 = Cổ Vật, 2 = NV Hàng Ngày

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
                            contentColor = CoralCrimson
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_boss_relics_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ĐỘT KÍCH BOSS & CỔ VẬT",
                            style = MaterialTheme.typography.titleLarge,
                            color = CoralCrimson,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Đã hạ: ${game.bossesDefeated} Boss • Mảnh Lõi: ${game.shards}✦",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf(
                    "Boss & Buff",
                    "Rèn Cổ Vật",
                    if (uiState.unclaimedDailyQuestCount > 0) "NV Ngày (${uiState.unclaimedDailyQuestCount})" else "NV Ngày"
                )
                tabs.forEachIndexed { idx, label ->
                    Button(
                        onClick = {
                            soundManager.playButtonClick()
                            subTab = idx
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (subTab == idx) CoralCrimson else CarbonSurface,
                            contentColor = if (subTab == idx) TextPrimary else TextSecondary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("boss_hub_tab_$idx")
                    ) {
                        Text(label, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (subTab) {
                    0 -> {
                        item {
                            val maxHp = BossCatalog.maxHpForLevel(game.bossLevel).coerceAtLeast(1.0)
                            val hpFraction = (game.bossCurrentHp / maxHp).toFloat().coerceIn(0f, 1f)
                            val bossName = BossCatalog.titleForLevel(game.bossLevel)

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = CoralCrimsonDim.copy(alpha = 0.72f),
                                border = BorderStroke(1.5.dp, CoralCrimson)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(148.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.img_boss_raid_banner),
                                            contentDescription = "Cyber Boss Raid",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color.Transparent, ObsidianBgDeep.copy(alpha = 0.88f))
                                                    )
                                                )
                                                .padding(12.dp),
                                            contentAlignment = Alignment.BottomStart
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "BOSS LV.${game.bossLevel}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = AmberGold,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = bossName,
                                                        style = MaterialTheme.typography.titleLarge,
                                                        color = TextPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                if (!uiState.lastBossDamageText.isNullOrBlank()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = ObsidianBgDeep.copy(alpha = 0.9f),
                                                        border = BorderStroke(1.dp, AmberGold)
                                                    ) {
                                                        Text(
                                                            text = uiState.lastBossDamageText,
                                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                                            color = AmberGold,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "HP: ${Formatters.formatPoints(game.bossCurrentHp)} / ${Formatters.formatPoints(maxHp)}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Thưởng: +$${BossCatalog.rewardDollars(game.bossLevel, game.relicLuckLv).toLong()} • +${BossCatalog.rewardShards(game.bossLevel, game.relicLuckLv)}✦",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RoyalGold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { hpFraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(99.dp)),
                                        color = CoralCrimson,
                                        trackColor = ObsidianBgDeep
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = onAttackBoss,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CoralCrimson,
                                            contentColor = TextPrimary
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 54.dp)
                                            .testTag("attack_boss_button")
                                    ) {
                                        Icon(Icons.Rounded.Whatshot, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "TẤN CÔNG BOSS (-${Formatters.formatPoints(maxOf(12.0, game.effectivePointsPerClick) * game.bossDamageMultiplier)} HP)",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "SIÊU KÍCH HOẠT (MEGA BOOSTERS)",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        items(BoosterType.entries, key = { it.id }) { booster ->
                            val remSec = when (booster) {
                                BoosterType.TAP_SURGE -> uiState.tapSurgeRemainingSec
                                BoosterType.CRIT_STORM -> uiState.critStormRemainingSec
                                BoosterType.AUTO_FRENZY -> uiState.autoFrenzyRemainingSec
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = if (remSec > 0L) CyberCyanDim.copy(alpha = 0.7f) else CarbonSurface.copy(alpha = 0.94f),
                                border = BorderStroke(1.dp, if (remSec > 0L) CyberCyan else CardBorder)
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
                                            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (remSec > 0L) "${booster.title} (${remSec}s)" else booster.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = booster.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Button(
                                        onClick = { onActivateBooster(booster) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricViolet,
                                            contentColor = TextPrimary
                                        ),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("activate_booster_${booster.id}")
                                    ) {
                                        Text("${booster.shardCost}✦", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        items(RelicId.entries, key = { it.id }) { relic ->
                            val lv = game.relicLevel(relic)
                            val isMax = lv >= relic.maxLevel
                            val cost = relic.costAt(lv)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("relic_card_${relic.id}"),
                                shape = RoundedCornerShape(16.dp),
                                color = CarbonSurface.copy(alpha = 0.94f),
                                border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.55f))
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
                                            Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${relic.title} • LV.$lv/${relic.maxLevel}",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${relic.effectPerLevel} (Giữ vĩnh viễn khi Restarts)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Button(
                                        onClick = { onUpgradeRelic(relic) },
                                        enabled = !isMax,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricViolet,
                                            contentColor = TextPrimary,
                                            disabledContainerColor = ElevatedCard,
                                            disabledContentColor = TextMuted
                                        ),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("upgrade_relic_${relic.id}")
                                    ) {
                                        Text(if (isMax) "MAX" else "${cost}✦", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        items(DailyQuestCatalog.all, key = { it.id }) { quest ->
                            val progress = quest.progressSelector(game).coerceAtMost(quest.target)
                            val claimed = game.isDailyQuestClaimed(quest.id)
                            val done = progress >= quest.target
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("daily_quest_${quest.id}"),
                                shape = RoundedCornerShape(16.dp),
                                color = if (claimed) NeonEmeraldDim.copy(alpha = 0.4f) else CarbonSurface.copy(alpha = 0.94f),
                                border = BorderStroke(1.dp, if (done && !claimed) AmberGold else CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = quest.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Thưởng: +${quest.rewardShards}✦ • +$${quest.rewardDollars.toLong()} • +${quest.rewardXp} XP",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = RoyalGold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        when {
                                            claimed -> Icon(Icons.Rounded.CheckCircle, contentDescription = "Đã nhận", tint = NeonEmerald)
                                            done -> Button(
                                                onClick = { onClaimDailyQuest(quest) },
                                                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = ObsidianBgDeep),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("claim_daily_quest_${quest.id}")
                                            ) {
                                                Text("Nhận", fontWeight = FontWeight.Bold)
                                            }
                                            else -> Text(
                                                text = "$progress/${quest.target}",
                                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                                color = CyberCyan
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { (progress.toFloat() / quest.target.toFloat()).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(99.dp)),
                                        color = if (done) NeonEmerald else CyberCyan,
                                        trackColor = ElevatedCard
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
