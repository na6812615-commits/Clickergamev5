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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Shield
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
import com.example.data.ArenaBossCatalog
import com.example.data.InfinityTowerCatalog
import com.example.data.TowerFloorDef
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDim
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CoralCrimsonDim
import com.example.ui.theme.CyberCyan
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
import com.example.viewmodel.GameUiState

@Composable
fun CosmicArenaScreen(
    uiState: GameUiState,
    onAttackArena: () -> Unit,
    onCastArenaOmega: () -> Unit,
    onActivateArenaShield: () -> Unit,
    onClaimTowerFloor: (TowerFloorDef) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val game = uiState.game
    val soundManager = LocalSoundManager.current
    var subTab by remember { mutableIntStateOf(0) } // 0 = Đấu Trường Thiên Hà, 1 = Tháp Vô Cực

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
                            .testTag("close_cosmic_arena_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Đóng Đấu Trường Vũ Trụ")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ĐẤU TRƯỜNG & THÁP VÔ CỰC",
                            style = MaterialTheme.typography.titleLarge,
                            color = CoralCrimson,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Đợt ${game.arenaWave} • ${game.arenaGlory} Vinh Quang • Tháp ${game.towerHighestFloorClaimed}/12",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.img_cosmic_arena_banner),
                    contentDescription = "Đấu Trường Vũ Trụ TapCore",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(ObsidianBgDeep.copy(alpha = 0.9f), Color.Transparent)
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = "HÀO QUANG CHIẾN BINH",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sức Mạnh Tap Đấu Trường: x${Formatters.formatDecimal(game.arenaTapMultiplier)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Buff Tháp Vô Cực: x${Formatters.formatDecimal(game.towerGlobalMultiplier)} Toàn Sản Lượng",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val towerTitle = if (uiState.readyTowerFloorCount > 0) "Tháp Vô Cực (!)" else "Tháp Vô Cực"
                listOf("Đấu Trường", towerTitle).forEachIndexed { idx, label ->
                    Button(
                        onClick = {
                            soundManager.playButtonClick()
                            subTab = idx
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (subTab == idx) CoralCrimson else CarbonSurface,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("arena_sub_tab_$idx")
                    ) {
                        Icon(
                            imageVector = if (idx == 0) Icons.Rounded.Whatshot else Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(label, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (subTab == 0) {
                    item {
                        val maxHp = ArenaBossCatalog.maxHpForWave(game.arenaWave).coerceAtLeast(1.0)
                        val hpFrac = (game.arenaEnemyHp / maxHp).toFloat().coerceIn(0f, 1f)
                        val guardianTitle = ArenaBossCatalog.titleForWave(game.arenaWave)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("arena_boss_card"),
                            shape = RoundedCornerShape(18.dp),
                            color = CarbonSurface,
                            border = BorderStroke(1.dp, CoralCrimson.copy(alpha = 0.7f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = guardianTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CoralCrimsonDim
                                    ) {
                                        Text(
                                            text = "ĐỢT ${game.arenaWave}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                            color = CoralCrimson,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "HP: ${Formatters.formatPoints(game.arenaEnemyHp)} / ${Formatters.formatPoints(maxHp)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = CoralCrimson
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { hpFrac },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(99.dp)),
                                    color = CoralCrimson,
                                    trackColor = ElevatedCard
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Nộ Khí Omega: ${game.arenaFury.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = AmberGold
                                    )
                                    Text(
                                        text = "Khiên Xung Kích: ${game.arenaShieldCharges} đòn",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = CyberCyan
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (game.arenaFury / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(99.dp)),
                                    color = AmberGold,
                                    trackColor = ElevatedCard
                                )

                                uiState.lastArenaDamageText?.let { dmgText ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Đòn vừa đánh: $dmgText",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = CyberCyan
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Thưởng vượt đợt: +$${ArenaBossCatalog.rewardDollars(game.arenaWave).toLong()}, +${ArenaBossCatalog.rewardShards(game.arenaWave)}✦, +${ArenaBossCatalog.rewardChips(game.arenaWave)}⚙, +${ArenaBossCatalog.rewardQuantumCores(game.arenaWave)}⚛",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonEmerald
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = onAttackArena,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CoralCrimson,
                                        contentColor = TextPrimary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("arena_attack_button")
                                ) {
                                    Icon(Icons.Rounded.Whatshot, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("TẤN CÔNG VỆ BINH", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val omegaReady = game.arenaFury >= 100.0
                                    Button(
                                        onClick = onCastArenaOmega,
                                        enabled = omegaReady,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AmberGold,
                                            contentColor = ObsidianBgDeep
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("arena_omega_button")
                                    ) {
                                        Icon(Icons.Rounded.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("OMEGA (100%)", fontWeight = FontWeight.Bold, maxLines = 1)
                                    }

                                    Button(
                                        onClick = onActivateArenaShield,
                                        enabled = game.dollars >= 30.0,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyberCyan,
                                            contentColor = ObsidianBgDeep
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("arena_shield_button")
                                    ) {
                                        Icon(Icons.Rounded.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Khiên $30", fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = AmberGoldDim,
                            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "CHINH PHỤC THÁP VÔ CỰC (12 TẦNG)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = RoyalGold,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Mỗi tầng mở khóa mang lại +15% Toàn Bộ Sản Lượng Vĩnh Viễn cùng ⚛ Lõi Lượng Tử & Kho Báu!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    items(InfinityTowerCatalog.floors) { floor ->
                        val claimed = floor.floor <= game.towerHighestFloorClaimed
                        val isNext = floor.floor == game.towerHighestFloorClaimed + 1
                        val met = floor.isCompleted(game)
                        val canClaim = isNext && met

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = CarbonSurface,
                            border = BorderStroke(
                                1.dp,
                                when {
                                    claimed -> NeonEmerald.copy(alpha = 0.6f)
                                    canClaim -> RoyalGold
                                    else -> CardBorder
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = floor.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (claimed) NeonEmerald else TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Điều kiện: ${floor.requirementText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (met) CyberCyan else TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+$${floor.rewardDollars.toLong()}, +${floor.rewardShards}✦, +${floor.rewardChips}⚙, +${floor.rewardCores}⚛",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = AmberGold
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                if (claimed) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Đã chinh phục",
                                        tint = NeonEmerald,
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Button(
                                        onClick = { onClaimTowerFloor(floor) },
                                        enabled = canClaim,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = RoyalGold,
                                            contentColor = ObsidianBgDeep
                                        ),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("claim_tower_floor_${floor.floor}")
                                    ) {
                                        Text(if (isNext) "NHẬN" else "KHÓA", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
