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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Memory
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CyberSkillId
import com.example.data.TechNodeId
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GalaxyEngine
import com.example.viewmodel.GameUiState

@Composable
fun TechTreeScreen(
    uiState: GameUiState,
    onUpgradeTechNode: (TechNodeId) -> Unit,
    onSynthesizeChips: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val game = uiState.game

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
            // Top bar
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
                            .testTag("close_tech_tree_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Đóng Cây Công Nghệ")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CÂY CÔNG NGHỆ LÕI",
                            style = MaterialTheme.typography.titleLarge,
                            color = ElectricViolet,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tổng cấp: ${game.totalTechLevel} • Tồn tại vĩnh viễn qua Restart",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElevatedCard,
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.65f))
                ) {
                    Text(
                        text = "${game.techChips}⚙ Chip",
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chip Synthesizer Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tech_chip_synthesizer_card"),
                shape = RoundedCornerShape(16.dp),
                color = ElevatedCard,
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Memory, contentDescription = null, tint = CyberCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Lò Tổng Hợp Chip Công Nghệ",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Rớt từ Boss, Thám Hiểm hoặc chế tạo bằng $ (${Formatters.formatDollars(game.dollars)})",
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
                        val can1 = game.dollars >= GalaxyEngine.CHIP_SYNTHESIS_DOLLAR_COST
                        val can5 = game.dollars >= GalaxyEngine.CHIP_SYNTHESIS_DOLLAR_COST * 5
                        Button(
                            onClick = { onSynthesizeChips(1) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (can1) CyberCyan else CarbonSurface,
                                contentColor = if (can1) ObsidianBgDeep else TextMuted
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("synth_chips_1")
                        ) {
                            Text(
                                text = "+${GalaxyEngine.CHIP_SYNTHESIS_AMOUNT}⚙ ($${GalaxyEngine.CHIP_SYNTHESIS_DOLLAR_COST.toLong()})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { onSynthesizeChips(5) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (can5) AmberGold else CarbonSurface,
                                contentColor = if (can5) ObsidianBgDeep else TextMuted
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("synth_chips_5")
                        ) {
                            Text(
                                text = "+${GalaxyEngine.CHIP_SYNTHESIS_AMOUNT * 5}⚙ ($${(GalaxyEngine.CHIP_SYNTHESIS_DOLLAR_COST * 5).toLong()})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 18.dp)
            ) {
                items(TechNodeId.entries, key = { it.id }) { node ->
                    TechNodeCard(
                        node = node,
                        currentLv = node.levelIn(game),
                        techChips = game.techChips,
                        onUpgrade = { onUpgradeTechNode(node) }
                    )
                }

                item(key = "skills_overview_header") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CarbonSurface,
                        border = BorderStroke(1.dp, CoralCrimson.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = AmberGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "KỸ NĂNG CHỦ ĐỘNG TRÊN MÀN HÌNH CHƠI",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = AmberGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            CyberSkillId.entries.forEach { skill ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(skill.title, style = MaterialTheme.typography.labelLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                                        Text(skill.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    val cdSec = (skill.baseCooldownSec * game.skillCooldownFactor).toLong()
                                    Text(
                                        text = "CD ${cdSec}s",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = CyberCyan
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

@Composable
private fun TechNodeCard(
    node: TechNodeId,
    currentLv: Int,
    techChips: Long,
    onUpgrade: () -> Unit
) {
    val isMax = currentLv >= node.maxLevel
    val cost = node.chipCostAt(currentLv)
    val canAfford = !isMax && techChips >= cost
    val accent = when (node.tier) {
        1 -> CyberCyan
        2 -> NeonEmerald
        else -> ElectricViolet
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tech_node_${node.id}"),
        shape = RoundedCornerShape(16.dp),
        color = CarbonSurface,
        border = BorderStroke(1.dp, if (currentLv > 0) accent.copy(alpha = 0.6f) else CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "NHÁNH ${node.tier} • ${node.title}",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = node.effectPerLevel,
                            style = MaterialTheme.typography.bodySmall,
                            color = accent
                        )
                    }
                }
                Text(
                    text = "LV.$currentLv/${node.maxLevel}",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (currentLv.toFloat() / node.maxLevel.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = accent,
                trackColor = ElevatedCard
            )

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onUpgrade,
                enabled = !isMax,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) accent else ElevatedCard,
                    contentColor = if (canAfford) ObsidianBgDeep else TextSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("upgrade_tech_${node.id}")
            ) {
                Text(
                    text = if (isMax) "CÔNG NGHỆ ĐÃ HOÀN THIỆN" else "Nâng cấp LV.${currentLv + 1} ($cost⚙ Chip)",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
