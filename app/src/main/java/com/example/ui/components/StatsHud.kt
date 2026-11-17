package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameStateEntity
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
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StatsHud(
    game: GameStateEntity,
    isOverdriveActive: Boolean,
    isSignedIn: Boolean = false,
    accountLabel: String = "Đăng nhập",
    featureHubAlertCount: Int = 0,
    onOpenAccount: () -> Unit = {},
    onOpenFeatureHub: () -> Unit = {},
    onOpenPremiumShop: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stats_hud"),
        shape = RoundedCornerShape(20.dp),
        color = CarbonSurface.copy(alpha = 0.92f),
        border = BorderStroke(
            1.dp,
            if (isOverdriveActive) AmberGold.copy(alpha = 0.7f) else CardBorder
        ),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ElevatedCard.copy(alpha = 0.78f),
                            CarbonSurface
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Always-visible Account / Login pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSignedIn) NeonEmeraldDim.copy(alpha = 0.85f) else AmberGoldDim.copy(alpha = 0.85f),
                    border = BorderStroke(
                        1.dp,
                        if (isSignedIn) NeonEmerald.copy(alpha = 0.7f) else AmberGold.copy(alpha = 0.75f)
                    ),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenAccount)
                        .testTag("hud_account_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSignedIn) Icons.Rounded.VerifiedUser else Icons.Rounded.AccountCircle,
                            contentDescription = "Tài khoản & Đăng nhập",
                            tint = if (isSignedIn) NeonEmerald else AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = accountLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSignedIn) NeonEmerald else AmberGold,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (game.shards > 0L || game.restartLv > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF281C4A),
                            border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_shard),
                                    contentDescription = null,
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${game.shards}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricViolet
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DiamondDim,
                        border = BorderStroke(1.dp, DiamondBlue.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenPremiumShop)
                            .testTag("open_premium_shop_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_diamond),
                                contentDescription = "Mở Premium Shop",
                                tint = DiamondBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${Formatters.formatLong(game.diamonds)} • VIP",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DiamondBlue
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = ElevatedCard,
                            contentColor = CyberCyan
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Mở cài đặt",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isOverdriveActive) AmberGold else CyberCyan)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${game.reactorTier.code} • ${game.reactorTier.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (game.hasVipPass) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AmberGoldDim,
                            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = "VIP OMEGA",
                                style = MaterialTheme.typography.labelSmall,
                                color = RoyalGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElevatedCard,
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenFeatureHub)
                        .testTag("open_feature_hub_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgedBox(
                            badge = {
                                if (featureHubAlertCount > 0) {
                                    Badge(containerColor = AmberGold, contentColor = Color(0xFF060913)) {
                                        Text("$featureHubAlertCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Widgets,
                                contentDescription = "Mở Trung Tâm Tính Năng",
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trung Tâm",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.stats_points_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = Formatters.formatPoints(game.diem),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isOverdriveActive) AmberGold else CyberCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("stats_points_value")
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    StatBadge(
                        iconRes = R.drawable.ic_dollar,
                        text = Formatters.formatDollars(game.dollars),
                        tint = NeonEmerald,
                        container = NeonEmeraldDim,
                        tag = "stats_dollars_value"
                    )
                    StatBadge(
                        iconRes = R.drawable.ic_bolt,
                        text = Formatters.formatMultiplier(game.multiplier * game.globalPermanentMultiplier),
                        tint = AmberGold,
                        container = AmberGoldDim,
                        tag = "stats_multiplier_value"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElevatedCard.copy(alpha = 0.8f))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniStatCell(
                    iconRes = R.drawable.ic_bolt,
                    label = "Điểm/Tap",
                    value = "+${Formatters.formatDecimal(game.effectivePointsPerClick)}",
                    tint = CyberCyan,
                    modifier = Modifier.weight(1f)
                )
                MiniStatCell(
                    iconRes = R.drawable.ic_speed,
                    label = "Hồi chiêu",
                    value = Formatters.formatSeconds(game.effectiveClickCooldownSec),
                    tint = NeonEmerald,
                    modifier = Modifier.weight(1f)
                )
                MiniStatCell(
                    iconRes = R.drawable.ic_crit,
                    label = "Crit",
                    value = "${Formatters.formatDecimal(game.critChancePercent)}%",
                    tint = AmberGold,
                    modifier = Modifier.weight(1f)
                )
                MiniStatCell(
                    iconRes = R.drawable.ic_timer,
                    label = if (game.passiveLv > 0) "Thụ động" else "Clicks",
                    value = if (game.passiveLv > 0) {
                        "+${Formatters.formatDecimal(game.passivePointsPerSec)}/s"
                    } else {
                        Formatters.formatLong(game.clicks)
                    },
                    tint = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatBadge(
    iconRes: Int,
    text: String,
    tint: Color,
    container: Color,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = container,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = tint,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MiniStatCell(
    iconRes: Int,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = tint,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
