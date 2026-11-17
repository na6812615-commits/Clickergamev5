package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CyberStockTicker
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CoralCrimsonDim
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldDim
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GalaxyEngine
import com.example.viewmodel.GameUiState

@Composable
fun CyberExchangeScreen(
    uiState: GameUiState,
    onSelectTicker: (CyberStockTicker) -> Unit,
    onBuyStock: (CyberStockTicker, Long) -> Unit,
    onSellStock: (CyberStockTicker, Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val game = uiState.game
    val market = uiState.marketSnapshot
    val ticker = uiState.selectedStockTicker
    val price = market.prices[ticker] ?: ticker.initialPrice
    val history = market.history[ticker] ?: listOf(ticker.initialPrice)
    val prevPrice = if (history.size >= 2) history[history.size - 2] else price
    val pctDelta = if (prevPrice > 0.0) ((price - prevPrice) / prevPrice) * 100.0 else 0.0
    val isUp = pctDelta >= 0.0

    val ownedShares = ticker.sharesIn(game)
    val avgCost = ticker.avgCostIn(game)
    val positionValue = ownedShares * price
    val unrealizedPnl = if (ownedShares > 0L && avgCost > 0.0) positionValue - (ownedShares * avgCost) else 0.0
    val portfolioTotal = GalaxyEngine.portfolioMarketValue(game, market.prices)
    val feeRatePct = GalaxyEngine.marketFeeRate(game) * 100.0
    val maxBuyable = if (price > 0.0) {
        val unitWithFee = price * (1.0 + GalaxyEngine.marketFeeRate(game))
        (game.dollars / unitWithFee).toLong().coerceAtLeast(0L)
    } else 0L

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            contentColor = NeonEmerald
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_cyber_exchange_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Đóng Sàn Chứng Khoán")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SÀN CHỨNG KHOÁN CYBER",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tiền mặt: ${Formatters.formatDollars(game.dollars)} • Giao dịch: ${game.stockTradesCount}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = TextSecondary
                        )
                    }
                }
            }

            // Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(98.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.img_cyber_exchange_banner),
                    contentDescription = "Sàn Chứng Khoán và Crypto TapCore",
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
                        text = "XU HƯỚNG THỊ TRƯỜNG: ${market.marketRegimeLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Giá trị Danh mục: ${Formatters.formatDollars(portfolioTotal)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tổng lãi đã chốt: +${Formatters.formatDollars(game.stockTotalProfit)} • Phí sàn: ${Formatters.formatDecimal(feeRatePct)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberGold
                    )
                }
            }

            // Ticker Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CyberStockTicker.entries.forEach { item ->
                    val itemPrice = market.prices[item] ?: item.initialPrice
                    val selected = item == ticker
                    val itemShares = item.sharesIn(game)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 58.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectTicker(item) }
                            .testTag("select_ticker_${item.symbol.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) ElevatedCard else CarbonSurface,
                        border = BorderStroke(1.5.dp, if (selected) NeonEmerald else CardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.symbol,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) NeonEmerald else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = Formatters.formatDollars(itemPrice),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = TextSecondary,
                                maxLines = 1
                            )
                            if (itemShares > 0L) {
                                Text(
                                    text = "x$itemShares",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberGold
                                )
                            }
                        }
                    }
                }
            }

            // Active Ticker Sparkline & Details Card
            val trendColor = if (isUp) NeonEmerald else CoralCrimson
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stock_chart_card"),
                shape = RoundedCornerShape(16.dp),
                color = CarbonSurface,
                border = BorderStroke(1.dp, trendColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${ticker.symbol} — ${ticker.title}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ticker.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isUp) NeonEmeraldDim else CoralCrimsonDim
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isUp) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                                    contentDescription = null,
                                    tint = trendColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (isUp) "+" else ""}${Formatters.formatDecimal(pctDelta)}%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = trendColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = Formatters.formatDollars(price),
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Biên độ: ${Formatters.formatDollars(ticker.minPrice)} – ${Formatters.formatDollars(ticker.maxPrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    // Live Sparkline Canvas
                    StockSparklineChart(
                        points = history,
                        lineColor = trendColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            }

            // Position & Unrealized P&L Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = ElevatedCard,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Đang nắm giữ", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = "$ownedShares ${ticker.symbol}",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Giá vốn TB", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = if (ownedShares > 0L) Formatters.formatDollars(avgCost) else "-",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = TextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Lời/Lỗ tạm tính", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            val pnlColor = when {
                                ownedShares == 0L -> TextMuted
                                unrealizedPnl >= 0.0 -> NeonEmerald
                                else -> CoralCrimson
                            }
                            val pnlText = when {
                                ownedShares == 0L -> "$0"
                                unrealizedPnl >= 0.0 -> "+${Formatters.formatDollars(unrealizedPnl)}"
                                else -> "-${Formatters.formatDollars(-unrealizedPnl)}"
                            }
                            Text(
                                text = pnlText,
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = pnlColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    // BUY Row
                    Text("MUA VÀO (${ticker.symbol})", style = MaterialTheme.typography.labelSmall, color = NeonEmerald, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TradeActionButton(
                            label = "Mua 1",
                            enabled = maxBuyable >= 1L,
                            accent = NeonEmerald,
                            onClick = { onBuyStock(ticker, 1L) },
                            tag = "buy_stock_1",
                            modifier = Modifier.weight(1f)
                        )
                        TradeActionButton(
                            label = "Mua 5",
                            enabled = maxBuyable >= 5L,
                            accent = NeonEmerald,
                            onClick = { onBuyStock(ticker, 5L) },
                            tag = "buy_stock_5",
                            modifier = Modifier.weight(1f)
                        )
                        TradeActionButton(
                            label = if (maxBuyable > 0L) "Max ($maxBuyable)" else "Mua Max",
                            enabled = maxBuyable > 0L,
                            accent = NeonEmerald,
                            onClick = { if (maxBuyable > 0L) onBuyStock(ticker, maxBuyable) },
                            tag = "buy_stock_max",
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    // SELL Row
                    Text("BÁN CHỐT LỜI (${ticker.symbol})", style = MaterialTheme.typography.labelSmall, color = CoralCrimson, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TradeActionButton(
                            label = "Bán 1",
                            enabled = ownedShares >= 1L,
                            accent = CoralCrimson,
                            onClick = { onSellStock(ticker, 1L) },
                            tag = "sell_stock_1",
                            modifier = Modifier.weight(1f)
                        )
                        val halfShares = (ownedShares / 2L).coerceAtLeast(1L)
                        TradeActionButton(
                            label = "Bán 50%",
                            enabled = ownedShares >= 1L,
                            accent = CoralCrimson,
                            onClick = { onSellStock(ticker, halfShares) },
                            tag = "sell_stock_half",
                            modifier = Modifier.weight(1f)
                        )
                        TradeActionButton(
                            label = "Bán Hết",
                            enabled = ownedShares >= 1L,
                            accent = CoralCrimson,
                            onClick = { onSellStock(ticker, ownedShares) },
                            tag = "sell_stock_all",
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(Icons.Rounded.ShowChart, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Giá cổ phiếu & tiền mã hóa biến động mỗi 4 giây. Dùng $ để giao dịch — Không đụng Kim Cương.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun TradeActionButton(
    label: String,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) accent else CarbonSurface,
            contentColor = if (enabled) ObsidianBgDeep else TextMuted
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun StockSparklineChart(
    points: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ObsidianBgDeep,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            if (points.size < 2) return@Canvas
            val minVal = points.minOrNull() ?: 0.0
            val maxVal = points.maxOrNull() ?: 1.0
            val span = (maxVal - minVal).coerceAtLeast(0.01)

            // Horizontal reference lines
            for (step in 1..3) {
                val y = size.height * (step / 4f)
                drawLine(
                    color = CardBorder.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val coords = points.mapIndexed { idx, value ->
                val x = (idx.toFloat() / (points.size - 1).toFloat()) * size.width
                val normY = ((value - minVal) / span).toFloat().coerceIn(0f, 1f)
                val y = size.height - (normY * size.height)
                Offset(x, y)
            }

            val linePath = Path().apply {
                coords.forEachIndexed { idx, pt ->
                    if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                }
            }
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(coords.last().x, size.height)
                lineTo(coords.first().x, size.height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent)
                )
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
            )

            // Latest price dot
            val last = coords.last()
            drawCircle(
                color = lineColor,
                radius = 5.dp.toPx(),
                center = last
            )
        }
    }
}
