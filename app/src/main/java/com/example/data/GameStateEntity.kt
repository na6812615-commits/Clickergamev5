package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

@Entity(tableName = "game_state")
data class GameStateEntity(
    @PrimaryKey val id: Int = 1,
    val diem: Double = 0.0,
    val dollars: Double = 0.0,
    val clicks: Long = 0L,
    val multiplier: Double = 1.0,
    val baseMultiplier: Double = 1.0,
    val pointsPerClick: Double = 1.0,
    val clickCooldownSec: Double = 0.25,
    val cooldownStep: Double = 0.125,
    val discountFactor: Double = 1.0,
    val discountStep: Double = 0.1,
    val pointPowerLv: Int = 0,
    val pointPowerCost: Double = 25.0,
    val multiplierLv: Int = 0,
    val multiplierCost: Double = 50.0,
    val cooldownLv: Int = 0,
    val cooldownCost: Double = 75.0,
    val discountLv: Int = 0,
    val discountCost: Double = 5.0,
    val menuSpeedLv: Int = 0,
    val menuSpeedCost: Double = 3.0,
    val menuTransitionSec: Double = 1.0,
    val toastSpeedLv: Int = 0,
    val toastSpeedCost: Double = 3.0,
    val toastDurationSec: Double = 1.0,
    val autoUnlocked: Boolean = false,
    val autoEnabled: Boolean = false,
    val autoPoints: Double = 1.0,
    val autoCooldownSec: Double = 1.0,
    val autoPowerLv: Int = 0,
    val autoPowerCost: Double = 15.0,
    val autoSpeedLv: Int = 0,
    val autoSpeedCost: Double = 20.0,
    val autoSpeedStep: Double = 0.25,
    val restartLv: Int = 0,
    val restartCost: Double = 150.0,
    val bestCombo: Int = 0,
    val lifetimePoints: Double = 0.0,
    val totalCrits: Long = 0L,
    val critChanceLv: Int = 0,
    val critChanceCost: Double = 120.0,
    val critMultLv: Int = 0,
    val critMultCost: Double = 200.0,
    val comboWindowLv: Int = 0,
    val comboWindowCost: Double = 90.0,
    val passiveLv: Int = 0,
    val passiveCost: Double = 8.0,
    val overdriveCharge: Double = 0.0,
    val overdriveLv: Int = 0,
    val overdriveCost: Double = 12.0,
    val shards: Long = 0L,
    val permStartPointsLv: Int = 0,
    val permGlobalMultLv: Int = 0,
    val permCritLv: Int = 0,
    val permDiscountLv: Int = 0,
    val claimedAchievements: String = "",
    val diamonds: Long = 0L,
    val hasVipPass: Boolean = false,
    val premTapGodLv: Int = 0,
    val premMegaCritLv: Int = 0,
    val premAutoGodLv: Int = 0,
    val premZeroCdLv: Int = 0,
    val premWealthMagnetLv: Int = 0,
    val premGoldenFeverEndAt: Long = 0L,
    val dailyStreak: Int = 0,
    val lastDailyClaimDay: Long = -1L,
    val totalDailyClaims: Int = 0,
    val seasonXp: Long = 0L,
    val seasonPassPremium: Boolean = false,
    val claimedSeasonFree: String = "",
    val claimedSeasonPremium: String = "",
    val gambleSpins: Long = 0L,
    val gambleWins: Long = 0L,
    val gambleBiggestWin: Double = 0.0,
    val gambleJackpotPool: Double = 2500.0,
    val bossLevel: Int = 1,
    val bossCurrentHp: Double = 250.0,
    val bossesDefeated: Long = 0L,
    val relicTapLv: Int = 0,
    val relicCritLv: Int = 0,
    val relicLuckLv: Int = 0,
    val relicBossLv: Int = 0,
    val relicPassiveLv: Int = 0,
    val boosterTapSurgeEndAt: Long = 0L,
    val boosterCritStormEndAt: Long = 0L,
    val boosterAutoFrenzyEndAt: Long = 0L,
    val dailyQuestDay: Long = -1L,
    val dailyQuestTaps: Int = 0,
    val dailyQuestCrits: Int = 0,
    val dailyQuestGambles: Int = 0,
    val dailyQuestBossKills: Int = 0,
    val claimedDailyQuests: String = "",
    val techChips: Long = 0L,
    val fleetScoutLv: Int = 0,
    val fleetHarvesterLv: Int = 0,
    val fleetCruiserLv: Int = 0,
    val fleetMothershipLv: Int = 0,
    val activeExpeditionId: String = "",
    val expeditionEndAt: Long = 0L,
    val totalExpeditionsCompleted: Long = 0L,
    val stockCoreShares: Long = 0L,
    val stockCoreAvgCost: Double = 0.0,
    val stockNeonShares: Long = 0L,
    val stockNeonAvgCost: Double = 0.0,
    val stockQbitShares: Long = 0L,
    val stockQbitAvgCost: Double = 0.0,
    val stockVoidShares: Long = 0L,
    val stockVoidAvgCost: Double = 0.0,
    val stockTotalProfit: Double = 0.0,
    val stockTradesCount: Long = 0L,
    val techOverclockLv: Int = 0,
    val techAutoProtocolLv: Int = 0,
    val techConverterLv: Int = 0,
    val techReactorCoreLv: Int = 0,
    val techMarketIntelLv: Int = 0,
    val techSuperCapLv: Int = 0,
    val skillEmpReadyAt: Long = 0L,
    val skillHyperReadyAt: Long = 0L,
    val skillHyperEndAt: Long = 0L,
    val skillRainReadyAt: Long = 0L,
    val totalSkillsCast: Long = 0L,
    val quantumCores: Long = 0L,
    val empireSolarLv: Int = 0,
    val empireFoundryLv: Int = 0,
    val empireExtractorLv: Int = 0,
    val empireLabLv: Int = 0,
    val empireCitadelLv: Int = 0,
    val empirePendingPoints: Double = 0.0,
    val empirePendingDollars: Double = 0.0,
    val empirePendingShards: Long = 0L,
    val empirePendingCores: Long = 0L,
    val gearBladeLv: Int = 0,
    val gearAegisLv: Int = 0,
    val gearNeuralLv: Int = 0,
    val gearNovaLv: Int = 0,
    val arenaWave: Int = 1,
    val arenaEnemyHp: Double = 450.0,
    val arenaFury: Double = 0.0,
    val arenaShieldCharges: Int = 0,
    val arenaGlory: Long = 0L,
    val towerHighestFloorClaimed: Int = 0,
    // Mega Update 4.0: Singularity Ascension, AI Companion Drones, Quantum Core Runes & Raid Dungeons
    val voidEssence: Long = 0L,
    val singularityLv: Int = 0,
    val singularityTapPerkLv: Int = 0,
    val singularityCashPerkLv: Int = 0,
    val singularityDronePerkLv: Int = 0,
    val singularityBossPerkLv: Int = 0,
    val dronePulseLv: Int = 0,
    val droneMinerLv: Int = 0,
    val droneStrikerLv: Int = 0,
    val droneChronoLv: Int = 0,
    val droneOverclockEndAt: Long = 0L,
    val runeBerserkLv: Int = 0,
    val runeMidasLv: Int = 0,
    val runeVampireLv: Int = 0,
    val runeChronoLv: Int = 0,
    val runeHarvestLv: Int = 0,
    val runeSingularityLv: Int = 0,
    val equippedRunes: String = "",
    val raidEnergy: Int = 5,
    val raidTapCounter: Int = 0,
    val activeDungeonId: String = "",
    val dungeonStage: Int = 1,
    val dungeonEnemyHp: Double = 0.0,
    val totalDungeonsCleared: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun discountedPointCost(baseCost: Double): Long =
        (baseCost / discountFactor).roundToLong().coerceAtLeast(1L)

    val globalPermanentMultiplier: Double
        get() = 1.0 + permGlobalMultLv * 0.25

    val seasonLevel: Int
        get() = SeasonPassCatalog.levelForXp(seasonXp)

    val seasonMultiplier: Double
        get() = 1.0 + seasonLevel * 0.05

    val relicTapMultiplier: Double
        get() = 1.0 + relicTapLv * 0.35

    val relicPassiveMultiplier: Double
        get() = 1.0 + relicPassiveLv * 0.35

    val bossDamageMultiplier: Double
        get() = (1.0 + relicBossLv * 0.50) *
            (1.0 + techReactorCoreLv * 0.30) *
            (1.0 + gearBladeLv * 0.25) *
            (1.0 + droneStrikerLv * 0.25) *
            (1.0 + singularityBossPerkLv * 0.45) *
            (1.0 + activeRuneRank(CyberRuneId.VAMPIRE) * 0.50)

    val techTapMultiplier: Double
        get() = 1.0 + techOverclockLv * 0.25

    val techAutoMultiplier: Double
        get() = 1.0 + techAutoProtocolLv * 0.30

    val techConverterMultiplier: Double
        get() = 1.0 + techConverterLv * 0.20

    val techExpeditionBonusMultiplier: Double
        get() = (1.0 + techMarketIntelLv * 0.15 + fleetMothershipLv * 0.10) *
            (1.0 + empireExtractorLv * 0.12) *
            (1.0 + activeRuneRank(CyberRuneId.HARVEST) * 0.35)

    val skillCooldownFactor: Double
        get() = (1.0 - techSuperCapLv * 0.10 - droneChronoLv * 0.04 - activeRuneRank(CyberRuneId.CHRONO) * 0.06)
            .coerceAtLeast(0.30)

    val skillPowerMultiplier: Double
        get() = 1.0 + techSuperCapLv * 0.15

    val empireCitadelSynergy: Double
        get() = 1.0 + empireCitadelLv * 0.20 + droneChronoLv * 0.12

    val empireGlobalMultiplier: Double
        get() = 1.0 + empireSolarLv * 0.08 + droneChronoLv * 0.08

    val empireLabAutoMultiplier: Double
        get() = 1.0 + empireLabLv * 0.18

    val empireSolarPtsPerSec: Double
        get() = if (empireSolarLv <= 0) 0.0 else empireSolarLv * 28.0 * empireCitadelSynergy * multiplier * globalPermanentMultiplier * seasonMultiplier * singularityGlobalMultiplier

    val empireFoundryDollarsPer10s: Double
        get() = empireFoundryLv * 0.90 * empireCitadelSynergy * (1.0 + activeRuneRank(CyberRuneId.HARVEST) * 0.25)

    val totalEmpireLevel: Int
        get() = empireSolarLv + empireFoundryLv + empireExtractorLv + empireLabLv + empireCitadelLv

    val gearTapMultiplier: Double
        get() = 1.0 + gearBladeLv * 0.35

    val gearPassiveMultiplier: Double
        get() = 1.0 + gearAegisLv * 0.35

    val gearCritChanceBonus: Double
        get() = gearAegisLv * 2.0

    val gearComboWindowBonusMs: Long
        get() = gearNeuralLv * 300L

    val gearOverdriveChargeMult: Double
        get() = 1.0 + gearNeuralLv * 0.12

    val gearFleetBonusMultiplier: Double
        get() = 1.0 + gearNovaLv * 0.25

    val gearDollarBonusMultiplier: Double
        get() = 1.0 + gearNovaLv * 0.15

    val totalGearLevel: Int
        get() = gearBladeLv + gearAegisLv + gearNeuralLv + gearNovaLv

    val arenaTapMultiplier: Double
        get() = 1.0 + (arenaWave - 1).coerceAtLeast(0) * 0.04

    val towerGlobalMultiplier: Double
        get() = 1.0 + towerHighestFloorClaimed * 0.15

    // Mega Update 4.0 Multipliers
    val singularityGlobalMultiplier: Double
        get() = (1.0 + singularityLv * 0.65) *
            (1.0 + singularityTapPerkLv * 0.50) *
            (1.0 + activeRuneRank(CyberRuneId.SINGULARITY) * (0.40 * (singularityLv + 1)))

    val totalDroneLevel: Int
        get() = dronePulseLv + droneMinerLv + droneStrikerLv + droneChronoLv

    val droneSwarmMultiplier: Double
        get() = 1.0 + singularityDronePerkLv * 0.35

    val droneTapMultiplier: Double
        get() = (1.0 + dronePulseLv * 0.30) * droneSwarmMultiplier

    val dronePointsPerSec: Double
        get() = if (dronePulseLv <= 0) 0.0 else maxOf(10.0, pointsPerClick * 3.5) * dronePulseLv * multiplier * globalPermanentMultiplier * droneSwarmMultiplier * singularityGlobalMultiplier

    val droneDollarsPer10s: Double
        get() = droneMinerLv * 0.65 * droneSwarmMultiplier

    val droneBossDps: Double
        get() = if (droneStrikerLv <= 0) 0.0 else maxOf(12.0, effectivePointsPerClick * 0.35) * droneStrikerLv * droneSwarmMultiplier

    val totalRuneRank: Int
        get() = runeBerserkLv + runeMidasLv + runeVampireLv + runeChronoLv + runeHarvestLv + runeSingularityLv

    val equippedRuneIds: List<String>
        get() = if (equippedRunes.isBlank()) emptyList() else equippedRunes.split(",").filter { it.isNotBlank() }.distinct().take(3)

    val equippedRuneSynergyMultiplier: Double
        get() = when (equippedRuneIds.size) {
            3 -> 1.45
            2 -> 1.20
            else -> 1.0
        }

    fun isRuneEquipped(rune: CyberRuneId): Boolean = equippedRuneIds.contains(rune.id)

    fun activeRuneRank(rune: CyberRuneId): Int =
        if (isRuneEquipped(rune)) rune.rankIn(this) else 0

    val singularityRequirementPoints: Double
        get() = 100_000.0 * 4.5.pow(singularityLv.coerceIn(0, 20))

    val singularityRequirementRestartLv: Int
        get() = 3 + singularityLv * 2

    val canAscendSingularity: Boolean
        get() = restartLv >= singularityRequirementRestartLv || lifetimePoints >= singularityRequirementPoints

    val pendingVoidEssenceOnSingularity: Long
        get() = if (!canAscendSingularity) 0L else {
            val restartContrib = (restartLv / 2L).coerceAtLeast(1L)
            val lifeContrib = kotlin.math.log10(lifetimePoints.coerceAtLeast(1000.0)).toLong()
            (2L + restartContrib + (lifeContrib - 3L).coerceAtLeast(0L) + droneChronoLv / 4L).coerceAtLeast(2L)
        }

    val fleetMothershipSynergy: Double
        get() = 1.0 + fleetMothershipLv * 0.25

    val fleetTotalPower: Int
        get() = fleetScoutLv + fleetHarvesterLv * 2 + fleetCruiserLv * 4 + fleetMothershipLv * 8 + empireCitadelLv * 3 + droneStrikerLv * 2

    val fleetPointsPerSec: Double
        get() {
            if (fleetScoutLv + fleetHarvesterLv + fleetCruiserLv + fleetMothershipLv <= 0) return 0.0
            val basePts = (fleetScoutLv * 4.0 + fleetHarvesterLv * 10.0 + fleetCruiserLv * 22.0 + fleetMothershipLv * 45.0)
            return basePts * fleetMothershipSynergy * multiplier * globalPermanentMultiplier * seasonMultiplier * empireGlobalMultiplier * gearFleetBonusMultiplier * towerGlobalMultiplier * singularityGlobalMultiplier
        }

    val fleetDollarsPer10Sec: Double
        get() = fleetHarvesterLv * 0.35 * fleetMothershipSynergy

    val fleetBossDps: Double
        get() = if (fleetCruiserLv <= 0) 0.0 else maxOf(6.0, effectivePointsPerClick * 0.25) * fleetCruiserLv * fleetMothershipSynergy

    val totalTechLevel: Int
        get() = techOverclockLv + techAutoProtocolLv + techConverterLv + techReactorCoreLv + techMarketIntelLv + techSuperCapLv

    val vipMultiplier: Double
        get() = if (hasVipPass) 3.0 else 1.0

    val premTapMultiplier: Double
        get() = 2.0.pow(premTapGodLv.coerceIn(0, 20))

    val premAutoMultiplier: Double
        get() = 2.5.pow(premAutoGodLv.coerceIn(0, 15))

    val premPassiveMultiplier: Double
        get() = 1.0 + premAutoGodLv * 0.5

    val effectivePointsPerClick: Double
        get() = pointsPerClick * multiplier * globalPermanentMultiplier * vipMultiplier * premTapMultiplier * relicTapMultiplier * seasonMultiplier * techTapMultiplier * empireGlobalMultiplier * gearTapMultiplier * arenaTapMultiplier * towerGlobalMultiplier * singularityGlobalMultiplier * droneTapMultiplier * (1.0 + activeRuneRank(CyberRuneId.BERSERK) * 0.45) * equippedRuneSynergyMultiplier

    val effectiveAutoPointsPerTick: Double
        get() = autoPoints * multiplier * globalPermanentMultiplier * vipMultiplier * premAutoMultiplier * relicPassiveMultiplier * seasonMultiplier * techAutoMultiplier * empireGlobalMultiplier * empireLabAutoMultiplier * gearPassiveMultiplier * towerGlobalMultiplier * singularityGlobalMultiplier * (1.0 + activeRuneRank(CyberRuneId.CHRONO) * 0.40) * equippedRuneSynergyMultiplier

    val effectiveClickCooldownSec: Double
        get() {
            val berserkFactor = if (activeRuneRank(CyberRuneId.BERSERK) > 0) 0.90 else 1.0
            return (clickCooldownSec * 0.8.pow(premZeroCdLv.coerceIn(0, 5)) * berserkFactor).coerceAtLeast(0.005)
        }

    val critChancePercent: Double
        get() = (critChanceLv * 4.0 + permCritLv * 3.0 + premMegaCritLv * 8.0 + relicCritLv * 2.5 + fleetScoutLv * 1.5 + gearCritChanceBonus + dronePulseLv * 1.5 + activeRuneRank(CyberRuneId.BERSERK) * 3.0 + if (hasVipPass) 10.0 else 0.0)
            .coerceAtMost(95.0)

    val critMultiplier: Double
        get() = 2.5 + critMultLv * 0.5 + premMegaCritLv * 1.5 + relicCritLv * 0.4 + empireCitadelLv * 0.25 + activeRuneRank(CyberRuneId.BERSERK) * 0.6

    val comboWindowMs: Long
        get() = 2000L + comboWindowLv * 350L + gearComboWindowBonusMs

    val passivePointsPerSec: Double
        get() = if (passiveLv <= 0) 0.0 else effectivePointsPerClick * 0.4 * passiveLv * premPassiveMultiplier * relicPassiveMultiplier * empireLabAutoMultiplier * gearPassiveMultiplier * (1.0 + activeRuneRank(CyberRuneId.CHRONO) * 0.40)

    val overdriveDurationSec: Double
        get() = 10.0 + overdriveLv * 2.0 + premZeroCdLv * 3.0

    val overdriveChargePerTap: Double
        get() = (5.0 + overdriveLv * 1.0) * (1.0 + techReactorCoreLv * 0.20) * gearOverdriveChargeMult * (1.0 + droneStrikerLv * 0.15) * (1.0 + activeRuneRank(CyberRuneId.VAMPIRE) * 0.25) * (if (hasVipPass) 2.0 else 1.0)

    val dollarExchangeMultiplier: Double
        get() = 1.5.pow(premWealthMagnetLv.coerceIn(0, 15)) * techConverterMultiplier * (1.0 + empireFoundryLv * 0.10) * gearDollarBonusMultiplier * (1.0 + droneMinerLv * 0.15) * (1.0 + singularityCashPerkLv * 0.40) * (1.0 + activeRuneRank(CyberRuneId.MIDAS) * 0.45)

    val restartRequirement: Long
        get() = discountedPointCost(restartCost)

    val nextBaseMultiplier: Double
        get() = baseMultiplier + 0.5

    val pendingShardsOnRestart: Long
        get() = if (diem < restartRequirement) {
            0L
        } else {
            val baseShards = 1L + sqrt(diem / restartRequirement.toDouble()).toLong() + (restartLv / 2L)
            (baseShards * (1.0 + premWealthMagnetLv * 0.25 + droneChronoLv * 0.08)).roundToLong().coerceAtLeast(1L)
        }

    val permStartPointsAmount: Double
        get() = permStartPointsLv * 500.0

    val permStartDiscountBonus: Double
        get() = permDiscountLv * 0.15

    val reactorTier: ReactorTier
        get() = when {
            restartLv >= 10 || lifetimePoints >= 2_500_000.0 -> ReactorTier.V_INFINITY
            restartLv >= 6 || lifetimePoints >= 250_000.0 -> ReactorTier.IV_SINGULARITY
            restartLv >= 3 || lifetimePoints >= 25_000.0 -> ReactorTier.III_QUANTUM
            restartLv >= 1 || lifetimePoints >= 2_500.0 -> ReactorTier.II_PLASMA
            else -> ReactorTier.I_NUCLEAR
        }

    fun isAchievementClaimed(achievementId: String): Boolean {
        if (claimedAchievements.isBlank()) return false
        return claimedAchievements.split(",").contains(achievementId)
    }

    fun isSeasonFreeClaimed(level: Int): Boolean {
        if (claimedSeasonFree.isBlank()) return false
        return claimedSeasonFree.split(",").contains(level.toString())
    }

    fun isSeasonEliteClaimed(level: Int): Boolean {
        if (claimedSeasonPremium.isBlank()) return false
        return claimedSeasonPremium.split(",").contains(level.toString())
    }

    fun isDailyQuestClaimed(questId: String): Boolean {
        if (claimedDailyQuests.isBlank()) return false
        return claimedDailyQuests.split(",").contains(questId)
    }

    fun relicLevel(relic: RelicId): Int = when (relic) {
        RelicId.TITAN -> relicTapLv
        RelicId.CRIT_EYE -> relicCritLv
        RelicId.LUCKY_CLOVER -> relicLuckLv
        RelicId.BOSS_SLAYER -> relicBossLv
        RelicId.ETERNAL_CELL -> relicPassiveLv
    }
}

enum class ReactorTier(val code: String, val displayName: String, val sides: Int) {
    I_NUCLEAR("Mk.I", "Lõi Hạt Nhân", 4),
    II_PLASMA("Mk.II", "Lõi Plasma", 6),
    III_QUANTUM("Mk.III", "Lõi Lượng Tử", 8),
    IV_SINGULARITY("Mk.IV", "Lõi Hố Đen", 10),
    V_INFINITY("Mk.V", "Lõi Vô Cực", 12)
}

object ComboSystem {
    const val COMBO_WINDOW_MS = 2000L

    fun multiplierFor(combo: Int): Double = when {
        combo >= 50 -> 3.0
        combo >= 35 -> 2.5
        combo >= 20 -> 2.0
        combo >= 10 -> 1.5
        combo >= 5 -> 1.2
        else -> 1.0
    }

    fun tierLabelFor(combo: Int): String = when {
        combo >= 50 -> "QUÁ TẢI NEON"
        combo >= 35 -> "SIÊU TỐC"
        combo >= 20 -> "BÙNG NỔ"
        combo >= 10 -> "NHỊP ĐỘ"
        combo >= 5 -> "KHỞI ĐỘNG"
        else -> "CHUỖI COMBO"
    }
}

object ShardShopCosts {
    fun costFor(level: Int, baseCost: Long): Long = baseCost * (level + 1L)
}

object PremiumShopCosts {
    fun upgradeCost(level: Int, baseDiamonds: Long): Long = baseDiamonds * (level + 1L)

    fun smallPointsPack(game: GameStateEntity): Double =
        maxOf(5_000.0, game.effectivePointsPerClick * 250.0)

    fun mediumPointsPack(game: GameStateEntity): Double =
        maxOf(50_000.0, game.effectivePointsPerClick * 1_500.0)

    fun megaPointsPack(game: GameStateEntity): Double =
        maxOf(500_000.0, game.effectivePointsPerClick * 8_000.0)
}
