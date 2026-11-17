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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameStateEntity
import com.example.data.ShardShopCosts
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDim
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RestartScreen(
    game: GameStateEntity,
    showConfirmDialog: Boolean,
    onRequestRestart: () -> Unit,
    onConfirmRestart: () -> Unit,
    onDismissDialog: () -> Unit,
    onBuyPermStartPoints: () -> Unit,
    onBuyPermGlobalMult: () -> Unit,
    onBuyPermCrit: () -> Unit,
    onBuyPermDiscount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val requirement = game.restartRequirement
    val progress = (game.diem / requirement.toDouble()).toFloat().coerceIn(0f, 1f)
    val canRestart = game.diem >= requirement
    val pendingShards = game.pendingShardsOnRestart

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("restart_screen"),
        contentPadding = PaddingValues(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.restart_header),
                    style = MaterialTheme.typography.titleLarge,
                    color = AmberGold
                )
                Text(
                    text = stringResource(R.string.restart_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Prestige Hero Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = CarbonSurface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = if (canRestart) 0.7f else 0.35f)),
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_restart),
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cấp Restarts: ${game.restartLv}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberGoldDim
                        ) {
                            Text(
                                text = "LV ${game.restartLv} → ${game.restartLv + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = ElevatedCard
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Hệ số nhân khởi điểm",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = Formatters.formatMultiplier(game.baseMultiplier),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = Formatters.formatMultiplier(game.nextBaseMultiplier),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = AmberGold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF281C4A),
                                border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = if (canRestart) "+${pendingShards}✦ Lõi" else "+0✦",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ElectricViolet,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tiến độ yêu cầu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "${Formatters.formatPoints(game.diem)} / ${Formatters.formatLong(requirement)} điểm",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (canRestart) NeonEmerald else CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(RoundedCornerShape(99.dp)),
                        color = if (canRestart) NeonEmerald else AmberGold,
                        trackColor = ElevatedCard
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onRequestRestart,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canRestart) AmberGold else ElevatedCard,
                            contentColor = if (canRestart) ObsidianBgDeep else TextMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                            .testTag("restart_action_button")
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_restart),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (canRestart) {
                                "Thực hiện Restarts (+${pendingShards}✦)"
                            } else {
                                "Cần thêm ${Formatters.formatPoints((requirement - game.diem).coerceAtLeast(0.0))} điểm"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Permanent Core Shard Upgrades
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KHO NÂNG CẤP VĨNH VIỄN",
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricViolet
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF281C4A),
                    border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Bạn có: ${game.shards}✦",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricViolet,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        item {
            val cost = ShardShopCosts.costFor(game.permStartPointsLv, 2L)
            PermanentShardCard(
                title = "Khởi đầu Thần Tốc",
                description = "Mỗi lần Restarts xong sẽ có sẵn +${Formatters.formatPoints((game.permStartPointsLv + 1) * 500.0)} điểm khởi đầu",
                levelLabel = "LV ${game.permStartPointsLv}",
                costShards = cost,
                affordable = game.shards >= cost,
                isMaxed = false,
                buttonTag = "buy_perm_start_points",
                onBuy = onBuyPermStartPoints
            )
        }

        item {
            val cost = ShardShopCosts.costFor(game.permGlobalMultLv, 3L)
            PermanentShardCard(
                title = "Bậc thầy Hệ số (+25% Vĩnh viễn)",
                description = "Nhân vĩnh viễn toàn bộ điểm kiếm được (${Formatters.formatMultiplier(game.globalPermanentMultiplier)} → ${Formatters.formatMultiplier(game.globalPermanentMultiplier + 0.25)})",
                levelLabel = "LV ${game.permGlobalMultLv}",
                costShards = cost,
                affordable = game.shards >= cost,
                isMaxed = false,
                buttonTag = "buy_perm_global_mult",
                onBuy = onBuyPermGlobalMult
            )
        }

        item {
            val isMax = game.permCritLv >= 8
            val cost = ShardShopCosts.costFor(game.permCritLv, 2L)
            PermanentShardCard(
                title = "May mắn Chí mạng (+3% Crit)",
                description = "Tỉ lệ Crit nền không bị mất khi Restarts (hiện tại +${game.permCritLv * 3}%)",
                levelLabel = "LV ${game.permCritLv}/8",
                costShards = cost,
                affordable = !isMax && game.shards >= cost,
                isMaxed = isMax,
                buttonTag = "buy_perm_crit",
                onBuy = onBuyPermCrit
            )
        }

        item {
            val cost = ShardShopCosts.costFor(game.permDiscountLv, 3L)
            PermanentShardCard(
                title = "Thương nhân Vĩnh viễn (+15% Giảm giá)",
                description = "Tăng vĩnh viễn hệ số giảm giá khởi điểm sau mỗi lần Restarts (+${Formatters.formatDecimal(game.permStartDiscountBonus)})",
                levelLabel = "LV ${game.permDiscountLv}",
                costShards = cost,
                affordable = game.shards >= cost,
                isMaxed = false,
                buttonTag = "buy_perm_discount",
                onBuy = onBuyPermDiscount
            )
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            containerColor = CarbonSurface,
            titleContentColor = AmberGold,
            textContentColor = TextSecondary,
            title = {
                Text(
                    text = stringResource(R.string.restart_confirm_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Bạn sẽ nhận được +${pendingShards}✦ Mảnh Lõi và tăng vĩnh viễn hệ số nhân khởi điểm từ ${Formatters.formatDecimal(game.baseMultiplier)}x lên ${Formatters.formatDecimal(game.nextBaseMultiplier)}x. Tiền $, cấp độ Shop và Auto Click trong vòng này sẽ đặt lại.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = ObsidianBgDeep
                    ),
                    modifier = Modifier.testTag("confirm_restart_button")
                ) {
                    Text(stringResource(R.string.confirm_action))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissDialog,
                    modifier = Modifier.testTag("cancel_restart_button")
                ) {
                    Text(
                        text = stringResource(R.string.cancel_action),
                        color = TextSecondary
                    )
                }
            }
        )
    }
}

@Composable
private fun PermanentShardCard(
    title: String,
    description: String,
    levelLabel: String,
    costShards: Long,
    affordable: Boolean,
    isMaxed: Boolean,
    buttonTag: String,
    onBuy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CarbonSurface.copy(alpha = 0.92f),
        border = BorderStroke(
            1.dp,
            if (affordable || isMaxed) ElectricViolet.copy(alpha = 0.55f) else CardBorder
        )
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
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ElevatedCard
                    ) {
                        Text(
                            text = levelLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricViolet,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onBuy,
                enabled = !isMaxed,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (affordable) ElectricViolet else ElevatedCard,
                    contentColor = if (affordable) ObsidianBgDeep else TextMuted,
                    disabledContainerColor = ElevatedCard,
                    disabledContentColor = ElectricViolet
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag(buttonTag)
            ) {
                Text(
                    text = if (isMaxed) "MAX" else "${costShards}✦",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
