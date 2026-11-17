package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.LocalSoundManager
import com.example.data.AlchemyRecipeId
import com.example.data.QuantumGearId
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
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
fun QuantumAlchemyScreen(
    uiState: GameUiState,
    onTransmute: (AlchemyRecipeId) -> Unit,
    onUpgradeGear: (QuantumGearId) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val game = uiState.game
    val soundManager = LocalSoundManager.current
    var subTab by remember { mutableIntStateOf(0) } // 0 = Lò Chuyển Hóa, 1 = Rèn Trang Bị

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
                            contentColor = ElectricViolet
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_quantum_alchemy_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Đóng Lò Giả Kim")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GIẢ KIM & TRANG BỊ ⚛",
                            style = MaterialTheme.typography.titleLarge,
                            color = ElectricViolet,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${game.quantumCores}⚛ Lõi • ${game.techChips}⚙ Chip • ${game.shards}✦ • ${Formatters.formatDollars(game.dollars)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberCyanDim,
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "GEAR LV.${game.totalGearLevel}",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Lò Chuyển Hóa", "Rèn Trang Bị ⚛").forEachIndexed { idx, label ->
                    Button(
                        onClick = {
                            soundManager.playButtonClick()
                            subTab = idx
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (subTab == idx) ElectricViolet else CarbonSurface,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("alchemy_tab_$idx")
                    ) {
                        Icon(
                            imageVector = if (idx == 0) Icons.Rounded.Science else Icons.Rounded.Build,
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
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = ElevatedCard,
                            border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "CHUYỂN HÓA VẬT CHẤT LƯỢNG TỬ",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Chế tạo Chip Công Nghệ ⚙ và Lõi Lượng Tử ⚛ để rèn Trang Bị tối thượng hoặc mở Rương Hoàng Gia.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    items(AlchemyRecipeId.entries) { recipe ->
                        val (canCraft, costDisplay) = when (recipe) {
                            AlchemyRecipeId.POINTS_TO_CHIPS -> {
                                val ptsCost = recipe.pointCostFor(game)
                                (game.diem >= ptsCost) to "${Formatters.formatPoints(ptsCost)} Điểm"
                            }
                            AlchemyRecipeId.SYNTH_QUANTUM_CORE ->
                                (game.dollars >= 35.0 && game.techChips >= 3L) to "$35 + 3⚙ Chip"
                            AlchemyRecipeId.SHARDS_TO_QUANTUM_CORE ->
                                (game.shards >= 3L && game.dollars >= 20.0) to "3✦ Mảnh Lõi + $20"
                            AlchemyRecipeId.QUANTUM_ROYAL_CRATE ->
                                (game.quantumCores >= 3L) to "3⚛ Lõi Lượng Tử"
                        }
                        val accent = when (recipe) {
                            AlchemyRecipeId.POINTS_TO_CHIPS -> CyberCyan
                            AlchemyRecipeId.SYNTH_QUANTUM_CORE -> ElectricViolet
                            AlchemyRecipeId.SHARDS_TO_QUANTUM_CORE -> NeonEmerald
                            AlchemyRecipeId.QUANTUM_ROYAL_CRATE -> RoyalGold
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = CarbonSurface,
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = recipe.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Nguyên liệu: $costDisplay",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = if (canCraft) AmberGold else TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Nhận: ${recipe.outputSummary}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accent
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = { onTransmute(recipe) },
                                    enabled = canCraft,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accent,
                                        contentColor = ObsidianBgDeep
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("transmute_${recipe.id}")
                                ) {
                                    Text("CHẾ TẠO", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(QuantumGearId.entries) { gear ->
                        val lv = gear.levelIn(game)
                        val isMax = lv >= gear.maxLevel
                        val coreCost = gear.coreCostAt(lv)
                        val chipCost = gear.chipCostAt(lv)
                        val canUpgrade = !isMax && game.quantumCores >= coreCost && game.techChips >= chipCost
                        val accent = when (gear) {
                            QuantumGearId.CYBER_BLADE -> AmberGold
                            QuantumGearId.AEGIS_PLATING -> CyberCyan
                            QuantumGearId.NEURAL_SYNC -> ElectricViolet
                            QuantumGearId.NOVA_THRUSTER -> NeonEmerald
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = CarbonSurface,
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = accent.copy(alpha = 0.16f),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Rounded.Shield, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "${gear.title} (${gear.slotLabel})",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Cấp $lv / ${gear.maxLevel}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                                color = accent
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onUpgradeGear(gear) },
                                        enabled = canUpgrade,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = accent,
                                            contentColor = ObsidianBgDeep
                                        ),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("upgrade_gear_${gear.id}")
                                    ) {
                                        Text(if (isMax) "MAX" else "Rèn Cấp", fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(gear.bonusSummary, style = MaterialTheme.typography.labelMedium, color = accent)
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { (lv.toFloat() / gear.maxLevel.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(99.dp)),
                                    color = accent,
                                    trackColor = ElevatedCard
                                )
                                if (!isMax) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Cần: ${coreCost}⚛ Lõi Lượng Tử • ${chipCost}⚙ Chip",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = if (canUpgrade) TextPrimary else TextMuted
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
