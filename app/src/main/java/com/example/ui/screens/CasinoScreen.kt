package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BetCurrency
import com.example.data.CasinoCatalog
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
import com.example.viewmodel.MegaUpdateEngine
import com.example.viewmodel.MinefieldState

@Composable
fun CasinoScreen(
    uiState: GameUiState,
    onSelectGame: (Int) -> Unit,
    onSelectCurrency: (BetCurrency) -> Unit,
    onSelectBetTier: (Int) -> Unit,
    onSpinWheel: () -> Unit,
    onPlaySlot777: () -> Unit,
    onPlayDice: (Int) -> Unit,
    onStartMinefield: () -> Unit,
    onRevealMineTile: (Int) -> Unit,
    onCashOutMinefield: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val game = uiState.game
    val bets = CasinoCatalog.betAmountsFor(uiState.casinoCurrency, game)
    val currentBet = bets[uiState.casinoBetTier.coerceIn(0, 2)]

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
                            contentColor = AmberGold
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_casino_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CYBER CASINO 777",
                            style = MaterialTheme.typography.titleLarge,
                            color = AmberGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mini-game giải trí dùng Điểm / Tiền $ trong game",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(136.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img_casino_banner),
                            contentDescription = "Cyber Casino 777 Banner",
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
                                            ObsidianBgDeep.copy(alpha = 0.9f)
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
                                        text = "QUỸ JACKPOT TÍCH LŨY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RoyalGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${Formatters.formatPoints(game.gambleJackpotPool)} ĐIỂM",
                                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = AmberGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Lượt chơi: ${game.gambleSpins}\nThắng: ${game.gambleWins}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }

                // Game mode selector
                item {
                    val gameNames = listOf("Vòng Quay", "Slot 777", "Tài Xỉu", "Dò Kho Báu")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        gameNames.forEachIndexed { idx, name ->
                            FilterChip(
                                selected = uiState.casinoSelectedGame == idx,
                                onClick = { onSelectGame(idx) },
                                label = { Text(name, maxLines = 1) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmberGold,
                                    selectedLabelColor = ObsidianBgDeep,
                                    containerColor = CarbonSurface,
                                    labelColor = TextSecondary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("casino_mode_$idx")
                            )
                        }
                    }
                }

                // Currency & Bet tier selector
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CarbonSurface.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "LOẠI TIỀN CƯỢC & MỨC ĐẶT",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BetCurrency.entries.forEach { currency ->
                                    FilterChip(
                                        selected = uiState.casinoCurrency == currency,
                                        onClick = { onSelectCurrency(currency) },
                                        label = { Text(currency.label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CyberCyan,
                                            selectedLabelColor = ObsidianBgDeep,
                                            containerColor = ElevatedCard,
                                            labelColor = TextSecondary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                bets.forEachIndexed { idx, amt ->
                                    FilterChip(
                                        selected = uiState.casinoBetTier == idx,
                                        onClick = { onSelectBetTier(idx) },
                                        label = {
                                            Text(
                                                text = MegaUpdateEngine.formatBet(uiState.casinoCurrency, amt),
                                                maxLines = 1
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonEmerald,
                                            selectedLabelColor = ObsidianBgDeep,
                                            containerColor = ElevatedCard,
                                            labelColor = TextSecondary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Result banner if any
                if (!uiState.lastCasinoBanner.isNullOrBlank()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (uiState.lastCasinoWon) NeonEmeraldDim else CoralCrimsonDim,
                            border = BorderStroke(
                                1.dp,
                                if (uiState.lastCasinoWon) NeonEmerald else CoralCrimson
                            )
                        ) {
                            Text(
                                text = uiState.lastCasinoBanner,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // Active game card
                item {
                    when (uiState.casinoSelectedGame) {
                        0 -> LuckyWheelCard(
                            spinId = uiState.wheelSpinId,
                            targetSliceIndex = uiState.wheelTargetSliceIndex,
                            betLabel = MegaUpdateEngine.formatBet(uiState.casinoCurrency, currentBet),
                            onSpin = onSpinWheel
                        )
                        1 -> Slot777Card(
                            reels = uiState.slotReels,
                            betLabel = MegaUpdateEngine.formatBet(uiState.casinoCurrency, currentBet),
                            onPlay = onPlaySlot777
                        )
                        2 -> QuantumDiceCard(
                            dicePair = uiState.lastDicePair,
                            betLabel = MegaUpdateEngine.formatBet(uiState.casinoCurrency, currentBet),
                            onPlayChoice = onPlayDice
                        )
                        else -> MinefieldCard(
                            mine = uiState.minefield,
                            betLabel = MegaUpdateEngine.formatBet(uiState.casinoCurrency, currentBet),
                            onStart = onStartMinefield,
                            onRevealTile = onRevealMineTile,
                            onCashOut = onCashOutMinefield
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LuckyWheelCard(
    spinId: Long,
    targetSliceIndex: Int,
    betLabel: String,
    onSpin: () -> Unit
) {
    val rotation = remember { Animatable(0f) }
    val slices = CasinoCatalog.wheelSlices
    val sliceColors = remember {
        listOf(
            Color(0xFF1E293B),
            CyberCyanDim,
            CoralCrimsonDim,
            NeonEmeraldDim,
            Color(0xFF3B1F6E),
            AmberGoldDim,
            Color(0xFF0C4A6E),
            RoyalGold
        )
    }

    LaunchedEffect(spinId) {
        if (spinId > 0L) {
            val sliceSweep = 360f / slices.size
            // Target angle lands slice center under top pointer (-90 degrees)
            val sliceCenterOffset = targetSliceIndex * sliceSweep + sliceSweep / 2f
            val baseRot = ((rotation.value / 360f).toInt() + 4) * 360f
            val finalAngle = baseRot + (360f - sliceCenterOffset)
            rotation.animateTo(
                targetValue = finalAngle,
                animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CarbonSurface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "VÒNG QUAY LƯỢNG TỬ",
                style = MaterialTheme.typography.titleMedium,
                color = AmberGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Cơ hội trúng x5, +6✦, +3✦ hoặc NỔ HŨ 10x + 45% Quỹ Jackpot!",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f - 10.dp.toPx()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val sweep = 360f / slices.size

                    rotate(degrees = rotation.value, pivot = center) {
                        slices.forEachIndexed { idx, _ ->
                            drawArc(
                                color = sliceColors[idx % sliceColors.size],
                                startAngle = -90f + idx * sweep,
                                sweepAngle = sweep,
                                useCenter = true,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2)
                            )
                            drawArc(
                                color = CyberCyan.copy(alpha = 0.45f),
                                startAngle = -90f + idx * sweep,
                                sweepAngle = sweep,
                                useCenter = true,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }

                    // Top pointer triangle
                    val pointerPath = Path().apply {
                        moveTo(center.x, 18.dp.toPx())
                        lineTo(center.x - 10.dp.toPx(), 0f)
                        lineTo(center.x + 10.dp.toPx(), 0f)
                        close()
                    }
                    drawPath(pointerPath, color = AmberGold)
                }

                Surface(
                    shape = CircleShape,
                    color = ObsidianBgDeep,
                    border = BorderStroke(2.dp, AmberGold),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = slices[targetSliceIndex.coerceIn(0, slices.lastIndex)].label,
                            style = MaterialTheme.typography.labelMedium,
                            color = RoyalGold,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                slices.take(4).forEach {
                    Text(it.label, style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                slices.drop(4).forEach {
                    Text(it.label, style = MaterialTheme.typography.labelSmall, color = RoyalGold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSpin,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberGold,
                    contentColor = ObsidianBgDeep
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("casino_spin_wheel_button")
            ) {
                Icon(Icons.Rounded.Casino, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("QUAY NGAY ($betLabel)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun Slot777Card(
    reels: List<String>,
    betLabel: String,
    onPlay: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CarbonSurface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MÁY XÈNG NEON 777",
                style = MaterialTheme.typography.titleMedium,
                color = ElectricViolet,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "3x 7️⃣ = 25x + 40% HŨ • 3x 🔥 = 12x + 8✦ • 3x ✦ = 8x + 5✦ • 2 ô trùng = 1.8x",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                reels.forEach { symbol ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(92.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianBgDeep,
                        border = BorderStroke(2.dp, AmberGold.copy(alpha = 0.7f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = symbol, fontSize = 38.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPlay,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricViolet,
                    contentColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("casino_play_slot_button")
            ) {
                Text("KÉO CẦN 777 ($betLabel)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuantumDiceCard(
    dicePair: Pair<Int, Int>?,
    betLabel: String,
    onPlayChoice: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CarbonSurface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TÀI XỈU LƯỢNG TỬ",
                style = MaterialTheme.typography.titleMedium,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Xỉu (2–6): x2.1 • Hòa 7: x4.5 • Tài (8–12): x2.1",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val d1 = dicePair?.first ?: 6
                val d2 = dicePair?.second ?: 6
                DiceFaceBox(value = d1)
                Text("+", style = MaterialTheme.typography.headlineMedium, color = TextMuted)
                DiceFaceBox(value = d2)
                Text(
                    text = "= ${d1 + d2}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = AmberGold,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onPlayChoice(0) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = ObsidianBgDeep),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp)
                        .testTag("casino_dice_xiu")
                ) {
                    Text("XỈU\n($betLabel)", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = { onPlayChoice(1) },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = ObsidianBgDeep),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp)
                        .testTag("casino_dice_seven")
                ) {
                    Text("HÒA 7\nx4.5", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = { onPlayChoice(2) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = ObsidianBgDeep),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp)
                        .testTag("casino_dice_tai")
                ) {
                    Text("TÀI\n($betLabel)", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DiceFaceBox(value: Int) {
    Surface(
        modifier = Modifier.size(64.dp),
        shape = RoundedCornerShape(14.dp),
        color = ObsidianBgDeep,
        border = BorderStroke(2.dp, CyberCyan)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MinefieldCard(
    mine: MinefieldState?,
    betLabel: String,
    onStart: () -> Unit,
    onRevealTile: (Int) -> Unit,
    onCashOut: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CarbonSurface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DÒ MÌN KHO BÁU 3x3",
                style = MaterialTheme.typography.titleMedium,
                color = NeonEmerald,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "7 ô Ngọc, 2 ô Mìn. Mở càng nhiều hệ số càng tăng (tối đa x12) — Rút bất cứ lúc nào!",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 3x3 Grid
            for (row in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0 until 3) {
                        val idx = row * 3 + col
                        val isRevealedSafe = mine != null && idx in mine.revealedSafe
                        val isHitBomb = mine?.hitBombIndex == idx
                        val showHiddenBomb = mine != null && !mine.isActive && idx in mine.bombIndices
                        val bg = when {
                            isHitBomb -> CoralCrimsonDim
                            isRevealedSafe -> NeonEmeraldDim
                            showHiddenBomb -> CoralCrimsonDim.copy(alpha = 0.55f)
                            else -> ElevatedCard
                        }
                        val label = when {
                            isHitBomb -> "💥"
                            isRevealedSafe -> "💠"
                            showHiddenBomb -> "💣"
                            else -> "?"
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.4f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = mine?.isActive == true && !isRevealedSafe) {
                                    onRevealTile(idx)
                                }
                                .testTag("mine_tile_$idx"),
                            shape = RoundedCornerShape(12.dp),
                            color = bg,
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isHitBomb -> CoralCrimson
                                    isRevealedSafe -> NeonEmerald
                                    else -> CardBorder
                                }
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = label, fontSize = 24.sp, color = TextPrimary)
                            }
                        }
                    }
                }
                if (row < 2) Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (mine == null || !mine.isActive) {
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = ObsidianBgDeep),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("start_minefield_button")
                ) {
                    Text("BẮT ĐẦU VÁN MỚI ($betLabel)", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCashOut,
                        enabled = mine.revealedSafe.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AmberGold),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp)
                            .testTag("cashout_minefield_button")
                    ) {
                        val payoutStr = MegaUpdateEngine.formatBet(mine.currency, mine.currentPayout)
                        Text(
                            text = if (mine.revealedSafe.isEmpty()) "Chọn ít nhất 1 ô" else "RÚT $payoutStr (x${Formatters.formatDecimal(mine.currentMultiplier)})",
                            color = AmberGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
