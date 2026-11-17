package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameStateEntity
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDim
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CoralCrimsonDim
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldDim
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashShopScreen(
    game: GameStateEntity,
    showAutoUpgradeSheet: Boolean,
    onExchange1000: () -> Unit,
    onExchangeAll: () -> Unit,
    onBuyDiscount: () -> Unit,
    onBuyMenuSpeed: () -> Unit,
    onBuyToastSpeed: () -> Unit,
    onBuyAutoClick: () -> Unit,
    onBuyPassiveCore: () -> Unit,
    onBuyOverdriveUpgrade: () -> Unit,
    onOpenAutoUpgrade: () -> Unit,
    onDismissAutoUpgrade: () -> Unit,
    onBuyAutoPower: () -> Unit,
    onBuyAutoSpeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cash_shop_screen"),
        contentPadding = PaddingValues(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.cash_shop_header),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }

        // Currency Exchange Section
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CarbonSurface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f)),
                tonalElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_exchange),
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.exchange_section_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = stringResource(R.string.exchange_rate_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onExchange1000,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (game.diem >= 1000.0) NeonEmerald else ElevatedCard,
                                contentColor = if (game.diem >= 1000.0) ObsidianBgDeep else TextMuted
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("exchange_1000_button")
                        ) {
                            Text(
                                text = "1.000 điểm → $1",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = onExchangeAll,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (game.diem > 0.0) CyberCyan else CardBorder
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (game.diem > 0.0) CyberCyan else TextMuted
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("exchange_all_button")
                        ) {
                            val preview = Formatters.formatDollars(game.diem / 1000.0)
                            Text(
                                text = "Đổi hết ($preview)",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.cash_shop_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = NeonEmerald
            )
        }

        // 1: Giảm giá
        item {
            CashShopCard(
                iconRes = R.drawable.ic_tag,
                accent = NeonEmerald,
                accentDim = NeonEmeraldDim,
                title = "Giảm giá cửa hàng Điểm & Restart",
                description = "Tăng hệ số chia giá (${Formatters.formatDecimal(game.discountFactor)} → ${Formatters.formatDecimal(game.discountFactor + game.discountStep)})",
                levelLabel = "LV ${game.discountLv}",
                priceLabel = Formatters.formatDollars(game.discountCost),
                affordable = game.dollars >= game.discountCost,
                purchased = false,
                actionLabel = stringResource(R.string.buy_button),
                buttonTag = "buy_discount_button",
                onClick = onBuyDiscount
            )
        }

        // 2: Lõi Thụ động & Offline
        item {
            CashShopCard(
                iconRes = R.drawable.ic_timer,
                accent = CyberCyan,
                accentDim = CyberCyanDim,
                title = "Lõi Hấp Thụ Thụ Động (Offline)",
                description = "Tự sinh điểm mỗi giây (${Formatters.formatDecimal(game.passivePointsPerSec)}/s) và nhận thưởng khi thoát app tới 4 giờ!",
                levelLabel = "LV ${game.passiveLv}",
                priceLabel = Formatters.formatDollars(game.passiveCost),
                affordable = game.dollars >= game.passiveCost,
                purchased = false,
                actionLabel = stringResource(R.string.buy_button),
                buttonTag = "buy_passive_core_button",
                onClick = onBuyPassiveCore
            )
        }

        // 3: Nâng cấp Overdrive
        item {
            CashShopCard(
                iconRes = R.drawable.ic_fire,
                accent = CoralCrimson,
                accentDim = CoralCrimsonDim,
                title = "Nâng cấp Lõi Overdrive",
                description = "Kéo dài thời gian x2 Overdrive (${Formatters.formatSeconds(game.overdriveDurationSec)} → ${Formatters.formatSeconds(game.overdriveDurationSec + 2.0)}) & sạc nhanh hơn",
                levelLabel = "LV ${game.overdriveLv}",
                priceLabel = Formatters.formatDollars(game.overdriveCost),
                affordable = game.dollars >= game.overdriveCost,
                purchased = false,
                actionLabel = stringResource(R.string.buy_button),
                buttonTag = "buy_overdrive_upgrade_button",
                onClick = onBuyOverdriveUpgrade
            )
        }

        // 4: Auto Click ($25)
        item {
            CashShopCard(
                iconRes = R.drawable.ic_auto,
                accent = NeonEmerald,
                accentDim = NeonEmeraldDim,
                title = "Auto Click tự động",
                description = "Tự động cộng điểm mỗi ${Formatters.formatSeconds(game.autoCooldownSec)} ngay cả khi không chạm tay",
                levelLabel = if (game.autoUnlocked) "ĐÃ MỞ" else "1 LẦN/VÒNG",
                priceLabel = if (game.autoUnlocked) stringResource(R.string.purchased_badge) else "$25",
                affordable = !game.autoUnlocked && game.dollars >= 25.0,
                purchased = game.autoUnlocked,
                actionLabel = if (game.autoUnlocked) stringResource(R.string.purchased_badge) else stringResource(R.string.buy_button),
                buttonTag = "buy_auto_click_button",
                onClick = onBuyAutoClick
            )
        }

        // 5: Nâng cấp Auto Click
        item {
            CashShopCard(
                iconRes = R.drawable.ic_tune,
                accent = ElectricViolet,
                accentDim = Color(0xFF281C4A),
                title = stringResource(R.string.auto_upgrade_sheet_title),
                description = "Tăng sức mạnh điểm Auto (x2) và rút ngắn chu kỳ Auto (${Formatters.formatDecimal(game.autoPoints)} điểm / ${Formatters.formatSeconds(game.autoCooldownSec)})",
                levelLabel = "LV ${game.autoPowerLv + game.autoSpeedLv}",
                priceLabel = if (game.autoUnlocked) "Khả dụng" else "Cần Auto Click",
                affordable = game.autoUnlocked,
                purchased = false,
                actionLabel = stringResource(R.string.open_auto_upgrade_btn),
                buttonTag = "open_auto_upgrade_button",
                onClick = onOpenAutoUpgrade
            )
        }

        // 6: Giảm thời gian chuyển cảnh (cd3)
        item {
            CashShopCard(
                iconRes = R.drawable.ic_bolt,
                accent = CyberCyan,
                accentDim = CyberCyanDim,
                title = "Giảm thời gian chuyển cảnh",
                description = "Tăng tốc độ chuyển tab và hộp thoại (${Formatters.formatSeconds(game.menuTransitionSec)} → ${Formatters.formatSeconds((game.menuTransitionSec / 2.0).coerceAtLeast(0.05))})",
                levelLabel = "LV ${game.menuSpeedLv}",
                priceLabel = Formatters.formatDollars(game.menuSpeedCost),
                affordable = game.dollars >= game.menuSpeedCost,
                purchased = false,
                actionLabel = stringResource(R.string.buy_button),
                buttonTag = "buy_menu_speed_button",
                onClick = onBuyMenuSpeed
            )
        }

        // 7: Giảm thời gian thông báo (cd4)
        item {
            CashShopCard(
                iconRes = R.drawable.ic_bell,
                accent = AmberGold,
                accentDim = AmberGoldDim,
                title = "Giảm thời gian thông báo",
                description = "Thu gọn nhanh hơn thông báo trạng thái (${Formatters.formatSeconds(game.toastDurationSec)} → ${Formatters.formatSeconds((game.toastDurationSec / 2.0).coerceAtLeast(0.15))})",
                levelLabel = "LV ${game.toastSpeedLv}",
                priceLabel = Formatters.formatDollars(game.toastSpeedCost),
                affordable = game.dollars >= game.toastSpeedCost,
                purchased = false,
                actionLabel = stringResource(R.string.buy_button),
                buttonTag = "buy_toast_speed_button",
                onClick = onBuyToastSpeed
            )
        }
    }

    if (showAutoUpgradeSheet) {
        BackHandler {
            onDismissAutoUpgrade()
        }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissAutoUpgrade,
            sheetState = sheetState,
            containerColor = CarbonSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.auto_upgrade_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )

                if (!game.autoUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ElevatedCard,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = stringResource(R.string.auto_upgrade_locked_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberGold,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                CashShopCard(
                    iconRes = R.drawable.ic_bolt,
                    accent = NeonEmerald,
                    accentDim = NeonEmeraldDim,
                    title = "Tăng điểm Auto Click (x2)",
                    description = "Điểm gốc mỗi nhịp Auto: ${Formatters.formatDecimal(game.autoPoints)} → ${Formatters.formatDecimal(game.autoPoints * 2.0)}",
                    levelLabel = "LV ${game.autoPowerLv}",
                    priceLabel = Formatters.formatDollars(game.autoPowerCost),
                    affordable = game.autoUnlocked && game.dollars >= game.autoPowerCost,
                    purchased = false,
                    actionLabel = stringResource(R.string.buy_button),
                    buttonTag = "buy_auto_power_button",
                    onClick = onBuyAutoPower
                )

                val nextAutoCd = (game.autoCooldownSec - game.autoSpeedStep).coerceAtLeast(0.05)
                CashShopCard(
                    iconRes = R.drawable.ic_speed,
                    accent = CyberCyan,
                    accentDim = CyberCyanDim,
                    title = "Tăng tốc chu kỳ Auto Click",
                    description = "Chu kỳ kích hoạt: ${Formatters.formatSeconds(game.autoCooldownSec)} → ${Formatters.formatSeconds(nextAutoCd)}",
                    levelLabel = "LV ${game.autoSpeedLv}",
                    priceLabel = Formatters.formatDollars(game.autoSpeedCost),
                    affordable = game.autoUnlocked && game.dollars >= game.autoSpeedCost,
                    purchased = false,
                    actionLabel = stringResource(R.string.buy_button),
                    buttonTag = "buy_auto_speed_button",
                    onClick = onBuyAutoSpeed
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CashShopCard(
    iconRes: Int,
    accent: Color,
    accentDim: Color,
    title: String,
    description: String,
    levelLabel: String,
    priceLabel: String,
    affordable: Boolean,
    purchased: Boolean,
    actionLabel: String,
    buttonTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CarbonSurface.copy(alpha = 0.92f),
        border = BorderStroke(
            1.dp,
            when {
                purchased -> NeonEmerald.copy(alpha = 0.5f)
                affordable -> accent.copy(alpha = 0.45f)
                else -> CardBorder
            }
        ),
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = accentDim,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElevatedCard
                        ) {
                            Text(
                                text = levelLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priceLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (purchased || affordable) accent else TextMuted
                )

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (affordable) accent else ElevatedCard,
                        contentColor = if (affordable) ObsidianBgDeep else TextMuted
                    ),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag(buttonTag)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
