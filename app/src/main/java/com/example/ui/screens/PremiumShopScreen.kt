package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.billing.BillingUiState
import com.example.billing.DiamondCatalog
import com.example.billing.DiamondProductDef
import com.example.data.GameStateEntity
import com.example.data.PremiumShopCosts
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDim
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DiamondBlue
import com.example.ui.theme.DiamondDim
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
import kotlin.math.roundToLong

@Composable
fun PremiumShopScreen(
    game: GameStateEntity,
    selectedTab: Int,
    goldenFeverRemainingSec: Long,
    lastMysteryCrateReward: String?,
    billingState: BillingUiState,
    onSelectTab: (Int) -> Unit,
    onLaunchRealPurchase: (DiamondProductDef) -> Boolean,
    onRefreshBilling: () -> Unit,
    onBuyPointsPack: (Int) -> Unit,
    onBuyDollarsPack: (Int) -> Unit,
    onBuyShards: () -> Unit,
    onBuyInstantOverdrive: () -> Unit,
    onBuyGoldenFever: () -> Unit,
    onOpenMysteryCrate: () -> Unit,
    onBuyPremTapGod: () -> Unit,
    onBuyPremMegaCrit: () -> Unit,
    onBuyPremAutoGod: () -> Unit,
    onBuyPremZeroCd: () -> Unit,
    onBuyPremWealthMagnet: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var unavailableProductDialog by remember { mutableStateOf<DiamondProductDef?>(null) }

    BackHandler {
        if (unavailableProductDialog != null) {
            unavailableProductDialog = null
        } else {
            onClose()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1224),
                        ObsidianBg,
                        ObsidianBgDeep
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("premium_shop_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onClose,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = ElevatedCard,
                            contentColor = DiamondBlue
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_premium_shop_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PREMIUM SHOP",
                                style = MaterialTheme.typography.titleLarge,
                                color = RoyalGold,
                                fontWeight = FontWeight.Bold
                            )
                            if (game.hasVipPass) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AmberGoldDim,
                                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = "VIP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RoyalGold,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Kho báu Kim Cương & Nâng cấp Thần Thánh",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DiamondDim,
                    border = BorderStroke(1.dp, DiamondBlue.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTab(1) }
                        .testTag("premium_diamond_balance")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_diamond),
                            contentDescription = "Kim Cương",
                            tint = DiamondBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = Formatters.formatLong(game.diamonds),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = DiamondBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Segmented Sub-Tab Switcher
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CarbonSurface,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PremiumTabPill(
                        label = "TIÊU KIM CƯƠNG",
                        selected = selectedTab == 0,
                        accent = RoyalGold,
                        tag = "premium_tab_spend",
                        onClick = { onSelectTab(0) },
                        modifier = Modifier.weight(1f)
                    )
                    PremiumTabPill(
                        label = "NẠP KIM CƯƠNG",
                        selected = selectedTab == 1,
                        accent = DiamondBlue,
                        tag = "premium_tab_topup",
                        onClick = { onSelectTab(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                SpendDiamondsContent(
                    game = game,
                    goldenFeverRemainingSec = goldenFeverRemainingSec,
                    lastMysteryCrateReward = lastMysteryCrateReward,
                    onGoToTopup = { onSelectTab(1) },
                    onBuyPointsPack = onBuyPointsPack,
                    onBuyDollarsPack = onBuyDollarsPack,
                    onBuyShards = onBuyShards,
                    onBuyInstantOverdrive = onBuyInstantOverdrive,
                    onBuyGoldenFever = onBuyGoldenFever,
                    onOpenMysteryCrate = onOpenMysteryCrate,
                    onBuyPremTapGod = onBuyPremTapGod,
                    onBuyPremMegaCrit = onBuyPremMegaCrit,
                    onBuyPremAutoGod = onBuyPremAutoGod,
                    onBuyPremZeroCd = onBuyPremZeroCd,
                    onBuyPremWealthMagnet = onBuyPremWealthMagnet,
                    modifier = Modifier.weight(1f)
                )
            } else {
                TopupDiamondsContent(
                    game = game,
                    billingState = billingState,
                    onRefreshBilling = onRefreshBilling,
                    onBuyRealMoneyProduct = { product ->
                        val launched = onLaunchRealPurchase(product)
                        if (!launched) {
                            unavailableProductDialog = product
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    unavailableProductDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { unavailableProductDialog = null },
            containerColor = CarbonSurface,
            titleContentColor = DiamondBlue,
            textContentColor = TextSecondary,
            title = {
                Text(
                    text = "Cổng Google Play Billing",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Kim Cương chỉ được mua duy nhất qua Google Play Billing chính thức (${product.fallbackPriceVnd}) cho gói \"${product.title}\" (Product ID: ${product.productId}).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Hiện Google Play Store chưa trả về sản phẩm in-app này trên thiết bị/tài khoản hiện tại nên cổng thanh toán chưa thể mở giao dịch.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AmberGold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        unavailableProductDialog = null
                        onRefreshBilling()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiamondBlue,
                        contentColor = ObsidianBgDeep
                    ),
                    modifier = Modifier.testTag("retry_billing_dialog_button")
                ) {
                    Text("Kiểm tra lại Store")
                }
            },
            dismissButton = {
                TextButton(onClick = { unavailableProductDialog = null }) {
                    Text("Đóng", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun PremiumTabPill(
    label: String,
    selected: Boolean,
    accent: Color,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accent.copy(alpha = 0.2f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, accent.copy(alpha = 0.7f)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) accent else TextMuted,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SpendDiamondsContent(
    game: GameStateEntity,
    goldenFeverRemainingSec: Long,
    lastMysteryCrateReward: String?,
    onGoToTopup: () -> Unit,
    onBuyPointsPack: (Int) -> Unit,
    onBuyDollarsPack: (Int) -> Unit,
    onBuyShards: () -> Unit,
    onBuyInstantOverdrive: () -> Unit,
    onBuyGoldenFever: () -> Unit,
    onOpenMysteryCrate: () -> Unit,
    onBuyPremTapGod: () -> Unit,
    onBuyPremMegaCrit: () -> Unit,
    onBuyPremAutoGod: () -> Unit,
    onBuyPremZeroCd: () -> Unit,
    onBuyPremWealthMagnet: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // VIP Omega Hero Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CarbonSurface,
                border = BorderStroke(1.5.dp, if (game.hasVipPass) RoyalGold else ElectricViolet.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF281C4A),
                                    Color(0xFF332409)
                                )
                            )
                        )
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = AmberGoldDim,
                            border = BorderStroke(1.dp, RoyalGold)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_crown),
                                    contentDescription = null,
                                    tint = RoyalGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (game.hasVipPass) "ĐẶC QUYỀN VIP OMEGA: ĐÃ BẬT" else "THẺ VIP OMEGA VĨNH VIỄN",
                                style = MaterialTheme.typography.titleMedium,
                                color = RoyalGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "x3 Điểm & Auto • +10% Crit • x2 Sạc Overdrive (Không mất khi Restarts)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    if (!game.hasVipPass) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = onGoToTopup,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalGold,
                                contentColor = ObsidianBgDeep
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("vip_go_topup_button")
                        ) {
                            Text("Nạp VIP", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Buffs & Mystery Crate
        item {
            PremiumSectionTitle("VẬT PHẨM & BÙA BUFF TỨC THÌ", AmberGold)
        }

        item {
            val feverActive = goldenFeverRemainingSec > 0L
            val mins = goldenFeverRemainingSec / 60L
            val secs = goldenFeverRemainingSec % 60L
            val timeText = if (feverActive) String.format("%02d:%02d", mins, secs) else "Chưa bật"
            PremiumSpendCard(
                title = if (feverActive) "Cơn Bão Hoàng Kim (x3 Điểm • $timeText)" else "Cơn Bão Hoàng Kim (x3 Điểm • 5 Phút)",
                description = "Nhân 3 toàn bộ điểm Tap, Auto & Thụ động trong 5 phút (có thể cộng dồn)",
                accent = RoyalGold,
                costDiamonds = 15L,
                affordable = game.diamonds >= 15L,
                buttonTag = "buy_golden_fever",
                onBuy = onBuyGoldenFever
            )
        }

        item {
            PremiumSpendCard(
                title = "Bình Xung Quá Tải (Instant Overdrive)",
                description = "Sạc đầy 100% và kích hoạt ngay lập tức chế độ Overdrive x2!",
                accent = CyberCyan,
                costDiamonds = 8L,
                affordable = game.diamonds >= 8L,
                buttonTag = "buy_instant_overdrive",
                onBuy = onBuyInstantOverdrive
            )
        }

        item {
            val desc = if (lastMysteryCrateReward != null) {
                "Mở thưởng ngẫu nhiên: Bão Điểm, Mưa $, +8✦, Kho Tài Nguyên ($260 + 12✦) hoặc Siêu Nổ Hũ! (Vừa mở: $lastMysteryCrateReward)"
            } else {
                "Mở thưởng ngẫu nhiên: Bão Điểm, Mưa $, +8✦, Kho Tài Nguyên ($260 + 12✦) hoặc Siêu Nổ Hũ!"
            }
            PremiumSpendCard(
                title = "Hộp Bí Ẩn Vũ Trụ (Mystery Crate)",
                description = desc,
                accent = ElectricViolet,
                costDiamonds = 30L,
                affordable = game.diamonds >= 30L,
                buttonTag = "open_mystery_crate",
                onBuy = onOpenMysteryCrate
            )
        }

        item {
            PremiumSpendCard(
                title = "Tinh Thể Lõi (+5✦ Mảnh Lõi)",
                description = "Nhận ngay 5 Mảnh Lõi ✦ để nâng cấp Kho Vĩnh Viễn trong Restarts",
                accent = ElectricViolet,
                costDiamonds = 25L,
                affordable = game.diamonds >= 25L,
                buttonTag = "buy_prem_shards",
                onBuy = onBuyShards
            )
        }

        // Mythic Permanent Upgrades
        item {
            Spacer(modifier = Modifier.height(4.dp))
            PremiumSectionTitle("NÂNG CẤP SIÊU XỊN (VĨNH VIỄN)", DiamondBlue)
        }

        item {
            val isMax = game.premTapGodLv >= 10
            val cost = PremiumShopCosts.upgradeCost(game.premTapGodLv, 20L)
            PremiumUpgradeCard(
                title = "Thánh Chỉ Bấm Tay (x2 Tap/cấp)",
                description = "Nhân đôi sức mạnh chạm vĩnh viễn (hiện tại: x${Formatters.formatLong(game.premTapMultiplier.roundToLong())})",
                levelLabel = "LV ${game.premTapGodLv}/10",
                costDiamonds = cost,
                affordable = !isMax && game.diamonds >= cost,
                isMaxed = isMax,
                accent = DiamondBlue,
                buttonTag = "buy_prem_tap_god",
                onBuy = onBuyPremTapGod
            )
        }

        item {
            val isMax = game.premMegaCritLv >= 5
            val cost = PremiumShopCosts.upgradeCost(game.premMegaCritLv, 30L)
            PremiumUpgradeCard(
                title = "Lõi Hủy Diệt (+8% Crit & +1.5x Sát thương)",
                description = "Tăng vĩnh viễn tỉ lệ và sát thương Chí mạng (hiện tại: ${Formatters.formatDecimal(game.critChancePercent)}% • ${Formatters.formatMultiplier(game.critMultiplier)})",
                levelLabel = "LV ${game.premMegaCritLv}/5",
                costDiamonds = cost,
                affordable = !isMax && game.diamonds >= cost,
                isMaxed = isMax,
                accent = CoralGlow,
                buttonTag = "buy_prem_mega_crit",
                onBuy = onBuyPremMegaCrit
            )
        }

        item {
            val isMax = game.premAutoGodLv >= 10
            val cost = PremiumShopCosts.upgradeCost(game.premAutoGodLv, 35L)
            PremiumUpgradeCard(
                title = "Siêu Trí Tuệ Auto (x2.5 Auto/cấp)",
                description = "Nhân 2.5x điểm Auto Click & +50% Lõi Thụ động mỗi cấp",
                levelLabel = "LV ${game.premAutoGodLv}/10",
                costDiamonds = cost,
                affordable = !isMax && game.diamonds >= cost,
                isMaxed = isMax,
                accent = NeonEmerald,
                buttonTag = "buy_prem_auto_god",
                onBuy = onBuyPremAutoGod
            )
        }

        item {
            val isMax = game.premZeroCdLv >= 5
            val cost = PremiumShopCosts.upgradeCost(game.premZeroCdLv, 40L)
            PremiumUpgradeCard(
                title = "Phá Vỡ Giới Hạn (-20% Hồi chiêu & +3s Overdrive)",
                description = "Giảm hồi chiêu chạm thực tế xuống cực nhanh (${Formatters.formatSeconds(game.effectiveClickCooldownSec)})",
                levelLabel = "LV ${game.premZeroCdLv}/5",
                costDiamonds = cost,
                affordable = !isMax && game.diamonds >= cost,
                isMaxed = isMax,
                accent = CyberCyan,
                buttonTag = "buy_prem_zero_cd",
                onBuy = onBuyPremZeroCd
            )
        }

        item {
            val isMax = game.premWealthMagnetLv >= 8
            val cost = PremiumShopCosts.upgradeCost(game.premWealthMagnetLv, 45L)
            PremiumUpgradeCard(
                title = "Nam Châm Tỷ Phú (x1.5 Đổi $ & +25% ✦)",
                description = "Nhân tiền $ khi quy đổi điểm (x${Formatters.formatDecimal(game.dollarExchangeMultiplier)}) & tăng Mảnh Lõi khi Restarts",
                levelLabel = "LV ${game.premWealthMagnetLv}/8",
                costDiamonds = cost,
                affordable = !isMax && game.diamonds >= cost,
                isMaxed = isMax,
                accent = RoyalGold,
                buttonTag = "buy_prem_wealth_magnet",
                onBuy = onBuyPremWealthMagnet
            )
        }

        // Buy Points with Diamonds
        item {
            Spacer(modifier = Modifier.height(4.dp))
            PremiumSectionTitle("MUA ĐIỂM BẰNG KIM CƯƠNG", CyberCyan)
        }

        item {
            val pts = PremiumShopCosts.smallPointsPack(game)
            PremiumSpendCard(
                title = "Túi Điểm Khởi Nghiệp",
                description = "Nhận ngay +${Formatters.formatPoints(pts)} điểm (tăng theo sức mạnh hiện tại)",
                accent = CyberCyan,
                costDiamonds = 5L,
                affordable = game.diamonds >= 5L,
                buttonTag = "buy_prem_points_0",
                onBuy = { onBuyPointsPack(0) }
            )
        }

        item {
            val pts = PremiumShopCosts.mediumPointsPack(game)
            PremiumSpendCard(
                title = "Rương Năng Lượng Lớn",
                description = "Nhận ngay +${Formatters.formatPoints(pts)} điểm tức thì",
                accent = CyberCyan,
                costDiamonds = 20L,
                affordable = game.diamonds >= 20L,
                buttonTag = "buy_prem_points_1",
                onBuy = { onBuyPointsPack(1) }
            )
        }

        item {
            val pts = PremiumShopCosts.megaPointsPack(game)
            PremiumSpendCard(
                title = "Kho Báu Lõi Vô Cực",
                description = "Bơm ngay +${Formatters.formatPoints(pts)} điểm siêu khổng lồ!",
                accent = CyberCyan,
                costDiamonds = 60L,
                affordable = game.diamonds >= 60L,
                buttonTag = "buy_prem_points_2",
                onBuy = { onBuyPointsPack(2) }
            )
        }

        // Buy Dollars with Diamonds
        item {
            Spacer(modifier = Modifier.height(4.dp))
            PremiumSectionTitle("MUA TIỀN ĐÔ ($) BẰNG KIM CƯƠNG", NeonEmerald)
        }

        item {
            PremiumSpendCard(
                title = "Vali Tiền Mặt (+$25)",
                description = "Đủ tiền mở khóa ngay Auto Click trong \$Shop\$!",
                accent = NeonEmerald,
                costDiamonds = 10L,
                affordable = game.diamonds >= 10L,
                buttonTag = "buy_prem_dollars_0",
                onBuy = { onBuyDollarsPack(0) }
            )
        }

        item {
            PremiumSpendCard(
                title = "Két Sắt Tỷ Phú (+$150)",
                description = "Bổ sung ngân quỹ mua hàng loạt nâng cấp \$Shop\$",
                accent = NeonEmerald,
                costDiamonds = 35L,
                affordable = game.diamonds >= 35L,
                buttonTag = "buy_prem_dollars_1",
                onBuy = { onBuyDollarsPack(1) }
            )
        }

        item {
            PremiumSpendCard(
                title = "Ngân Hàng Trung Ương (+$600)",
                description = "Nguồn tiền Đô khổng lồ nâng cấp toàn diện hệ thống",
                accent = NeonEmerald,
                costDiamonds = 90L,
                affordable = game.diamonds >= 90L,
                buttonTag = "buy_prem_dollars_2",
                onBuy = { onBuyDollarsPack(2) }
            )
        }
    }
}

@Composable
private fun TopupDiamondsContent(
    game: GameStateEntity,
    billingState: BillingUiState,
    onRefreshBilling: () -> Unit,
    onBuyRealMoneyProduct: (DiamondProductDef) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Play Billing Live Status Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CarbonSurface.copy(alpha = 0.94f),
                border = BorderStroke(
                    1.dp,
                    if (billingState.isReadyToPurchase) NeonEmerald.copy(alpha = 0.6f) else AmberGold.copy(alpha = 0.55f)
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
                            text = if (billingState.isReadyToPurchase) {
                                "GOOGLE PLAY BILLING: SẴN SÀNG"
                            } else {
                                "GOOGLE PLAY BILLING: CHẾ ĐỘ CHỜ"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (billingState.isReadyToPurchase) NeonEmerald else AmberGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = billingState.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onRefreshBilling,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("refresh_billing_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Làm mới Google Play Billing",
                            tint = DiamondBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        items(DiamondCatalog.all, key = { it.productId }) { product ->
            val ownedVip = product.isVipPass && game.hasVipPass
            val priceText = billingState.formattedPrices[product.productId] ?: product.fallbackPriceVnd
            val accent = if (product.isVipPass) RoyalGold else DiamondBlue

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CarbonSurface.copy(alpha = 0.94f),
                border = BorderStroke(
                    width = if (product.isVipPass || product.bonusLabel != null) 1.5.dp else 1.dp,
                    color = accent.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape,
                            color = if (product.isVipPass) AmberGoldDim else DiamondDim,
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.7f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(
                                        if (product.isVipPass) R.drawable.ic_crown else R.drawable.ic_diamond
                                    ),
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            product.bonusLabel?.let { badge ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = accent.copy(alpha = 0.18f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        text = badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = product.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = product.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { onBuyRealMoneyProduct(product) },
                        enabled = !ownedVip,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = ObsidianBgDeep,
                            disabledContainerColor = NeonEmeraldDim,
                            disabledContentColor = NeonEmerald
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("buy_iap_${product.productId}")
                    ) {
                        Text(
                            text = if (ownedVip) "ĐÃ CÓ" else priceText,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

private val CoralGlow = Color(0xFFFF4D6D)

@Composable
private fun PremiumSectionTitle(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun PremiumSpendCard(
    title: String,
    description: String,
    accent: Color,
    costDiamonds: Long,
    affordable: Boolean,
    buttonTag: String,
    onBuy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CarbonSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, if (affordable) accent.copy(alpha = 0.5f) else CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onBuy,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (affordable) DiamondBlue else ElevatedCard,
                    contentColor = if (affordable) ObsidianBgDeep else TextMuted
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag(buttonTag)
            ) {
                Text(
                    text = "${costDiamonds}💎",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun PremiumUpgradeCard(
    title: String,
    description: String,
    levelLabel: String,
    costDiamonds: Long,
    affordable: Boolean,
    isMaxed: Boolean,
    accent: Color,
    buttonTag: String,
    onBuy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CarbonSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, if (affordable || isMaxed) accent.copy(alpha = 0.55f) else CardBorder)
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
                            color = accent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onBuy,
                enabled = !isMaxed,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (affordable) accent else ElevatedCard,
                    contentColor = if (affordable) ObsidianBgDeep else TextMuted,
                    disabledContainerColor = ElevatedCard,
                    disabledContentColor = accent
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag(buttonTag)
            ) {
                Text(
                    text = if (isMaxed) "MAX" else "${costDiamonds}💎",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
