package com.example.viewmodel

import com.example.data.CyberSkillId
import com.example.data.CyberStockTicker
import com.example.data.ExpeditionCatalog
import com.example.data.ExpeditionZoneDef
import com.example.data.FleetShipType
import com.example.data.GameStateEntity
import com.example.data.TechNodeId
import com.example.ui.Formatters
import kotlin.math.roundToLong
import kotlin.random.Random

data class StockMarketSnapshot(
    val prices: Map<CyberStockTicker, Double> = CyberStockTicker.entries.associateWith { it.initialPrice },
    val history: Map<CyberStockTicker, List<Double>> = CyberStockTicker.entries.associateWith {
        List(16) { idx ->
            val drift = ((idx % 5) - 2) * 0.015
            (it.initialPrice * (1.0 + drift)).coerceIn(it.minPrice, it.maxPrice)
        } + it.initialPrice
    },
    val marketRegimeLabel: String = "ỔN ĐỊNH",
    val tickCount: Long = 0L
)

object GalaxyEngine {

    const val CHIP_SYNTHESIS_DOLLAR_COST = 80.0
    const val CHIP_SYNTHESIS_AMOUNT = 3L

    data class ActionOutcome(
        val updatedGame: GameStateEntity?,
        val message: String,
        val isError: Boolean
    )

    fun upgradeFleetShip(game: GameStateEntity, ship: FleetShipType): ActionOutcome {
        val currentLv = ship.levelIn(game)
        if (currentLv >= ship.maxLevel) {
            return ActionOutcome(null, "${ship.title} đã đạt cấp tối đa (${ship.maxLevel})!", true)
        }
        val dollarCost = ship.dollarCostAt(currentLv)
        val shardCost = ship.shardCostAt(currentLv)
        if (game.dollars < dollarCost || game.shards < shardCost) {
            val needText = buildString {
                append("$${dollarCost.toLong()}")
                if (shardCost > 0L) append(" & ${shardCost}✦")
            }
            return ActionOutcome(null, "Cần $needText để nâng ${ship.title}!", true)
        }
        val next = when (ship) {
            FleetShipType.SCOUT -> game.copy(
                dollars = game.dollars - dollarCost,
                shards = game.shards - shardCost,
                fleetScoutLv = currentLv + 1
            )
            FleetShipType.HARVESTER -> game.copy(
                dollars = game.dollars - dollarCost,
                shards = game.shards - shardCost,
                fleetHarvesterLv = currentLv + 1
            )
            FleetShipType.CRUISER -> game.copy(
                dollars = game.dollars - dollarCost,
                shards = game.shards - shardCost,
                fleetCruiserLv = currentLv + 1
            )
            FleetShipType.MOTHERSHIP -> game.copy(
                dollars = game.dollars - dollarCost,
                shards = game.shards - shardCost,
                fleetMothershipLv = currentLv + 1
            )
        }
        return ActionOutcome(
            updatedGame = next,
            message = "Nâng cấp ${ship.title} lên LV.${currentLv + 1}! (Lực chiến: ${next.fleetTotalPower})",
            isError = false
        )
    }

    fun startExpedition(
        game: GameStateEntity,
        zone: ExpeditionZoneDef,
        nowMs: Long = System.currentTimeMillis()
    ): ActionOutcome {
        if (game.activeExpeditionId.isNotBlank()) {
            return ActionOutcome(null, "Hạm đội đang thực hiện chuyến thám hiểm khác!", true)
        }
        if (game.fleetTotalPower < zone.minFleetPower) {
            return ActionOutcome(
                null,
                "Cần Tổng Lực Chiến Hạm đội ≥ ${zone.minFleetPower} (hiện tại: ${game.fleetTotalPower})!",
                true
            )
        }
        val next = game.copy(
            activeExpeditionId = zone.id,
            expeditionEndAt = nowMs + zone.durationSec * 1000L
        )
        return ActionOutcome(
            updatedGame = next,
            message = "Đã phóng hạm đội tới ${zone.title} (${zone.durationSec}s)!",
            isError = false
        )
    }

    fun claimExpedition(
        game: GameStateEntity,
        nowMs: Long = System.currentTimeMillis()
    ): ActionOutcome {
        val zone = ExpeditionCatalog.findById(game.activeExpeditionId)
            ?: return ActionOutcome(null, "Không có chuyến thám hiểm nào đang chạy!", true)
        if (nowMs < game.expeditionEndAt) {
            val remSec = ((game.expeditionEndAt - nowMs) / 1000L).coerceAtLeast(1L)
            return ActionOutcome(null, "Hạm đội chưa trở về! Còn ${remSec}s nữa.", true)
        }

        val powerMult = 1.0 + (game.fleetTotalPower * 0.04)
        val bonusMult = powerMult * game.techExpeditionBonusMultiplier * (if (game.hasVipPass) 1.5 else 1.0)
        val ptsWon = maxOf(zone.basePoints * bonusMult, game.effectivePointsPerClick * zone.durationSec * 0.8)
        val dollarsWon = (zone.baseDollars * bonusMult).roundToLong().toDouble()
        val shardsWon = (zone.baseShards * bonusMult).roundToLong().coerceAtLeast(zone.baseShards)
        val chipsWon = (zone.baseTechChips * bonusMult).roundToLong().coerceAtLeast(zone.baseTechChips)

        val next = MegaUpdateEngine.addSeasonXp(
            game.copy(
                diem = game.diem + ptsWon,
                lifetimePoints = game.lifetimePoints + ptsWon,
                dollars = game.dollars + dollarsWon,
                shards = game.shards + shardsWon,
                techChips = game.techChips + chipsWon,
                activeExpeditionId = "",
                expeditionEndAt = 0L,
                totalExpeditionsCompleted = game.totalExpeditionsCompleted + 1L
            ),
            baseXp = zone.seasonXp
        )
        return ActionOutcome(
            updatedGame = next,
            message = "Thu hoạch ${zone.title}: +${Formatters.formatPoints(ptsWon)}đ, +$${dollarsWon.toLong()}, +${shardsWon}✦ & +${chipsWon}⚙!",
            isError = false
        )
    }

    fun tickMarket(
        snapshot: StockMarketSnapshot,
        random: Random = Random
    ): StockMarketSnapshot {
        val newTick = snapshot.tickCount + 1L
        val regimeRoll = ((newTick / 6L) % 3L).toInt()
        val (regimeBias, regimeLabel) = when (regimeRoll) {
            1 -> 0.015 to "TĂNG TRƯỞNG (BULL)"
            2 -> -0.012 to "ĐIỀU CHỈNH (BEAR)"
            else -> 0.004 to "DAO ĐỘNG CÂN BẰNG"
        }

        val nextPrices = mutableMapOf<CyberStockTicker, Double>()
        val nextHistory = mutableMapOf<CyberStockTicker, List<Double>>()

        for (ticker in CyberStockTicker.entries) {
            val prev = snapshot.prices[ticker] ?: ticker.initialPrice
            val swing = (random.nextDouble(-1.0, 1.0) * ticker.volatilityPercent) + regimeBias
            // Mean reversion pull when extreme
            val meanPull = ((ticker.initialPrice - prev) / ticker.initialPrice) * 0.04
            val updated = ((prev * (1.0 + swing + meanPull)) * 100.0).roundToLong() / 100.0
            val clamped = updated.coerceIn(ticker.minPrice, ticker.maxPrice)
            nextPrices[ticker] = clamped
            val oldHist = snapshot.history[ticker] ?: listOf(ticker.initialPrice)
            nextHistory[ticker] = (oldHist + clamped).takeLast(20)
        }

        return StockMarketSnapshot(
            prices = nextPrices,
            history = nextHistory,
            marketRegimeLabel = regimeLabel,
            tickCount = newTick
        )
    }

    fun marketFeeRate(game: GameStateEntity): Double {
        return (0.02 * (1.0 - game.techMarketIntelLv * 0.12)).coerceAtLeast(0.002)
    }

    fun buyStock(
        game: GameStateEntity,
        ticker: CyberStockTicker,
        unitPrice: Double,
        quantity: Long
    ): ActionOutcome {
        if (quantity <= 0L) {
            return ActionOutcome(null, "Số lượng mua phải lớn hơn 0!", true)
        }
        val fee = unitPrice * quantity * marketFeeRate(game)
        val totalCost = unitPrice * quantity + fee
        if (game.dollars < totalCost) {
            return ActionOutcome(
                null,
                "Cần ${Formatters.formatDollars(totalCost)} để mua $quantity ${ticker.symbol}!",
                true
            )
        }
        val oldShares = ticker.sharesIn(game)
        val oldAvg = ticker.avgCostIn(game)
        val newShares = oldShares + quantity
        val effectiveUnitPaid = totalCost / quantity.toDouble()
        val newAvg = ((oldShares * oldAvg) + (quantity * effectiveUnitPaid)) / newShares.toDouble()

        val updated = updateTickerPosition(
            game.copy(
                dollars = game.dollars - totalCost,
                stockTradesCount = game.stockTradesCount + 1L
            ),
            ticker = ticker,
            shares = newShares,
            avgCost = newAvg
        )
        return ActionOutcome(
            updatedGame = updated,
            message = "Đã mua $quantity ${ticker.symbol} giá ${Formatters.formatDollars(unitPrice)}!",
            isError = false
        )
    }

    fun sellStock(
        game: GameStateEntity,
        ticker: CyberStockTicker,
        unitPrice: Double,
        quantity: Long
    ): ActionOutcome {
        val oldShares = ticker.sharesIn(game)
        if (oldShares <= 0L || quantity <= 0L) {
            return ActionOutcome(null, "Bạn chưa nắm giữ cổ phần ${ticker.symbol} nào!", true)
        }
        val sellQty = quantity.coerceAtMost(oldShares)
        val gross = unitPrice * sellQty
        val fee = gross * marketFeeRate(game)
        val netProceeds = (gross - fee).coerceAtLeast(0.0)
        val oldAvg = ticker.avgCostIn(game)
        val realizedProfit = netProceeds - (oldAvg * sellQty)
        val remaining = oldShares - sellQty
        val nextAvg = if (remaining == 0L) 0.0 else oldAvg

        val updated = MegaUpdateEngine.addSeasonXp(
            updateTickerPosition(
                game.copy(
                    dollars = game.dollars + netProceeds,
                    stockTotalProfit = game.stockTotalProfit + maxOf(0.0, realizedProfit),
                    stockTradesCount = game.stockTradesCount + 1L
                ),
                ticker = ticker,
                shares = remaining,
                avgCost = nextAvg
            ),
            baseXp = if (realizedProfit > 0.0) 18L else 6L
        )
        val pnlLabel = if (realizedProfit >= 0.0) {
            "+${Formatters.formatDollars(realizedProfit)}"
        } else {
            "-${Formatters.formatDollars(-realizedProfit)}"
        }
        return ActionOutcome(
            updatedGame = updated,
            message = "Đã bán $sellQty ${ticker.symbol} thu ${Formatters.formatDollars(netProceeds)} (Lời/Lỗ: $pnlLabel)!",
            isError = false
        )
    }

    private fun updateTickerPosition(
        game: GameStateEntity,
        ticker: CyberStockTicker,
        shares: Long,
        avgCost: Double
    ): GameStateEntity = when (ticker) {
        CyberStockTicker.CORE -> game.copy(stockCoreShares = shares, stockCoreAvgCost = avgCost)
        CyberStockTicker.NEON -> game.copy(stockNeonShares = shares, stockNeonAvgCost = avgCost)
        CyberStockTicker.QBIT -> game.copy(stockQbitShares = shares, stockQbitAvgCost = avgCost)
        CyberStockTicker.VOID -> game.copy(stockVoidShares = shares, stockVoidAvgCost = avgCost)
    }

    fun portfolioMarketValue(game: GameStateEntity, prices: Map<CyberStockTicker, Double>): Double {
        return CyberStockTicker.entries.sumOf { ticker ->
            ticker.sharesIn(game) * (prices[ticker] ?: ticker.initialPrice)
        }
    }

    fun upgradeTechNode(game: GameStateEntity, node: TechNodeId): ActionOutcome {
        val currentLv = node.levelIn(game)
        if (currentLv >= node.maxLevel) {
            return ActionOutcome(null, "${node.title} đã đạt cấp tối đa!", true)
        }
        val cost = node.chipCostAt(currentLv)
        if (game.techChips < cost) {
            return ActionOutcome(null, "Cần ${cost}⚙ Chip Công Nghệ để nâng ${node.title}!", true)
        }
        val next = when (node) {
            TechNodeId.OVERCLOCK -> game.copy(techChips = game.techChips - cost, techOverclockLv = currentLv + 1)
            TechNodeId.AUTO_PROTOCOL -> game.copy(techChips = game.techChips - cost, techAutoProtocolLv = currentLv + 1)
            TechNodeId.CONVERTER -> game.copy(techChips = game.techChips - cost, techConverterLv = currentLv + 1)
            TechNodeId.REACTOR_CORE -> game.copy(techChips = game.techChips - cost, techReactorCoreLv = currentLv + 1)
            TechNodeId.MARKET_INTEL -> game.copy(techChips = game.techChips - cost, techMarketIntelLv = currentLv + 1)
            TechNodeId.SUPER_CAPACITOR -> game.copy(techChips = game.techChips - cost, techSuperCapLv = currentLv + 1)
        }
        return ActionOutcome(
            updatedGame = next,
            message = "Nghiên cứu ${node.title} LV.${currentLv + 1} thành công!",
            isError = false
        )
    }

    fun synthesizeTechChips(game: GameStateEntity, packs: Int = 1): ActionOutcome {
        val safePacks = packs.coerceAtLeast(1)
        val totalCost = CHIP_SYNTHESIS_DOLLAR_COST * safePacks
        if (game.dollars < totalCost) {
            return ActionOutcome(null, "Cần ${Formatters.formatDollars(totalCost)} để chế tạo Chip Công Nghệ!", true)
        }
        val gainedChips = CHIP_SYNTHESIS_AMOUNT * safePacks
        val next = game.copy(
            dollars = game.dollars - totalCost,
            techChips = game.techChips + gainedChips
        )
        return ActionOutcome(
            updatedGame = next,
            message = "Chế tạo thành công +${gainedChips}⚙ Chip Công Nghệ!",
            isError = false
        )
    }

    fun castSkill(
        game: GameStateEntity,
        skill: CyberSkillId,
        nowMs: Long = System.currentTimeMillis(),
        isOverdrive: Boolean = false,
        isTapSurge: Boolean = false
    ): ActionOutcome {
        val cooldownMs = (skill.baseCooldownSec * 1000L * game.skillCooldownFactor).roundToLong()
        val powerMult = game.skillPowerMultiplier * (if (isOverdrive) 2.0 else 1.0) * (if (isTapSurge) 4.0 else 1.0)

        return when (skill) {
            CyberSkillId.EMP_BURST -> {
                if (nowMs < game.skillEmpReadyAt) {
                    val rem = ((game.skillEmpReadyAt - nowMs) / 1000L).coerceAtLeast(1L)
                    return ActionOutcome(null, "${skill.shortTitle} đang hồi chiêu (${rem}s)!", true)
                }
                val bonusPts = maxOf(500.0, game.effectivePointsPerClick * 25.0 * powerMult)
                val bossDmg = maxOf(40.0, game.effectivePointsPerClick * game.bossDamageMultiplier * 8.0 * game.skillPowerMultiplier)
                val bossOutcome = MegaUpdateEngine.hitBossWithCustomDamage(game, bossDmg)
                val next = MegaUpdateEngine.addSeasonXp(
                    bossOutcome.updatedGame.copy(
                        diem = bossOutcome.updatedGame.diem + bonusPts,
                        lifetimePoints = bossOutcome.updatedGame.lifetimePoints + bonusPts,
                        skillEmpReadyAt = nowMs + cooldownMs,
                        totalSkillsCast = bossOutcome.updatedGame.totalSkillsCast + 1L
                    ),
                    baseXp = 15L
                )
                val bossMsg = if (bossOutcome.bossDefeated) " • HẠ GỤC BOSS!" else " • -${Formatters.formatPoints(bossDmg)} HP Boss"
                ActionOutcome(
                    updatedGame = next,
                    message = "XUNG EMP! +${Formatters.formatPoints(bonusPts)} điểm$bossMsg",
                    isError = false
                )
            }
            CyberSkillId.HYPER_FOCUS -> {
                if (nowMs < game.skillHyperReadyAt) {
                    val rem = ((game.skillHyperReadyAt - nowMs) / 1000L).coerceAtLeast(1L)
                    return ActionOutcome(null, "${skill.shortTitle} đang hồi chiêu (${rem}s)!", true)
                }
                val durMs = (skill.durationSec * 1000L * game.skillPowerMultiplier).roundToLong()
                val next = MegaUpdateEngine.addSeasonXp(
                    game.copy(
                        skillHyperReadyAt = nowMs + cooldownMs,
                        skillHyperEndAt = nowMs + durMs,
                        totalSkillsCast = game.totalSkillsCast + 1L
                    ),
                    baseXp = 15L
                )
                ActionOutcome(
                    updatedGame = next,
                    message = "SIÊU HỘI TỤ x2.5 & 100% CRIT kích hoạt trong ${durMs / 1000L}s!",
                    isError = false
                )
            }
            CyberSkillId.QUANTUM_RAIN -> {
                if (nowMs < game.skillRainReadyAt) {
                    val rem = ((game.skillRainReadyAt - nowMs) / 1000L).coerceAtLeast(1L)
                    return ActionOutcome(null, "${skill.shortTitle} đang hồi chiêu (${rem}s)!", true)
                }
                val fleetSec = game.fleetPointsPerSec * 30.0
                val passiveSec = game.passivePointsPerSec * 30.0
                val autoSec = if (game.autoUnlocked) {
                    (game.effectiveAutoPointsPerTick / game.autoCooldownSec.coerceAtLeast(0.05)) * 15.0
                } else {
                    0.0
                }
                val ptsGain = maxOf(1_200.0, (fleetSec + passiveSec + autoSec) * game.skillPowerMultiplier)
                val dollarGain = ((12.0 + game.fleetHarvesterLv * 4.0 + game.restartLv * 3.0) * game.skillPowerMultiplier).roundToLong().toDouble()
                val next = MegaUpdateEngine.addSeasonXp(
                    game.copy(
                        diem = game.diem + ptsGain,
                        lifetimePoints = game.lifetimePoints + ptsGain,
                        dollars = game.dollars + dollarGain,
                        skillRainReadyAt = nowMs + cooldownMs,
                        totalSkillsCast = game.totalSkillsCast + 1L
                    ),
                    baseXp = 18L
                )
                ActionOutcome(
                    updatedGame = next,
                    message = "MƯA TÀI NGUYÊN! +${Formatters.formatPoints(ptsGain)} điểm & +$${dollarGain.toLong()}!",
                    isError = false
                )
            }
        }
    }
}
