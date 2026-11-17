package com.example.viewmodel

import com.example.data.AlchemyRecipeId
import com.example.data.ArenaBossCatalog
import com.example.data.DayClock
import com.example.data.EmpireDistrictId
import com.example.data.GameStateEntity
import com.example.data.InfinityTowerCatalog
import com.example.data.QuantumGearId
import com.example.data.TowerFloorDef
import com.example.ui.Formatters
import kotlin.math.roundToLong

data class CosmicActionOutcome(
    val updatedGame: GameStateEntity?,
    val message: String,
    val isError: Boolean = false
)

data class ArenaCombatOutcome(
    val updatedGame: GameStateEntity?,
    val damageDealt: Double,
    val waveCleared: Boolean,
    val wasCrit: Boolean,
    val summary: String,
    val isError: Boolean = false
)

object CosmicEmpireEngine {

    fun upgradeEmpireDistrict(
        game: GameStateEntity,
        district: EmpireDistrictId
    ): CosmicActionOutcome {
        val currentLv = district.levelIn(game)
        if (currentLv >= district.maxLevel) {
            return CosmicActionOutcome(null, "${district.title} đã đạt cấp tối đa (${district.maxLevel})!", isError = true)
        }
        val dCost = district.dollarCostAt(currentLv)
        val cCost = district.chipCostAt(currentLv)
        val sCost = district.shardCostAt(currentLv)

        if (game.dollars < dCost) {
            return CosmicActionOutcome(null, "Cần ${Formatters.formatDollars(dCost)} để nâng cấp ${district.title}!", isError = true)
        }
        if (game.techChips < cCost) {
            return CosmicActionOutcome(null, "Cần ${cCost}⚙ Chip Công Nghệ để xây dựng ${district.title}!", isError = true)
        }
        if (game.shards < sCost) {
            return CosmicActionOutcome(null, "Cần ${sCost}✦ Mảnh Lõi để mở rộng ${district.title}!", isError = true)
        }

        val deducted = game.copy(
            dollars = game.dollars - dCost,
            techChips = game.techChips - cCost,
            shards = game.shards - sCost
        )
        val upgraded = when (district) {
            EmpireDistrictId.SOLAR_MATRIX -> deducted.copy(empireSolarLv = currentLv + 1)
            EmpireDistrictId.DOLLAR_FOUNDRY -> deducted.copy(empireFoundryLv = currentLv + 1)
            EmpireDistrictId.SHARD_EXTRACTOR -> deducted.copy(empireExtractorLv = currentLv + 1)
            EmpireDistrictId.QUANTUM_LAB -> deducted.copy(empireLabLv = currentLv + 1)
            EmpireDistrictId.NEXUS_CITADEL -> deducted.copy(empireCitadelLv = currentLv + 1)
        }
        val withXp = MegaUpdateEngine.addSeasonXp(upgraded, baseXp = 30L)
        return CosmicActionOutcome(
            updatedGame = withXp,
            message = "Nâng cấp ${district.title} lên LV.${currentLv + 1} (+30 XP)!"
        )
    }

    fun hasPendingTreasury(game: GameStateEntity): Boolean =
        game.empirePendingPoints >= 1.0 ||
            game.empirePendingDollars >= 0.1 ||
            game.empirePendingShards > 0L ||
            game.empirePendingCores > 0L

    fun collectEmpireTreasury(game: GameStateEntity): CosmicActionOutcome {
        if (!hasPendingTreasury(game)) {
            return CosmicActionOutcome(null, "Kho Bạc Đế Chế hiện đang trống, hãy nâng cấp Công Trình để sản xuất!", isError = true)
        }
        val pts = game.empirePendingPoints
        val dollars = game.empirePendingDollars
        val shards = game.empirePendingShards
        val cores = game.empirePendingCores

        val updated = MegaUpdateEngine.addSeasonXp(
            game.copy(
                diem = game.diem + pts,
                lifetimePoints = game.lifetimePoints + pts,
                dollars = game.dollars + dollars,
                shards = game.shards + shards,
                quantumCores = game.quantumCores + cores,
                empirePendingPoints = 0.0,
                empirePendingDollars = 0.0,
                empirePendingShards = 0L,
                empirePendingCores = 0L
            ),
            baseXp = 25L
        )
        val parts = buildList {
            if (pts >= 1.0) add("+${Formatters.formatPoints(pts)}đ")
            if (dollars >= 0.05) add("+${Formatters.formatDollars(dollars)}")
            if (shards > 0L) add("+${shards}✦")
            if (cores > 0L) add("+${cores}⚛")
        }.joinToString(", ")
        return CosmicActionOutcome(updated, "Thu hoạch Kho Bạc Đế Chế: $parts (+25 XP)!")
    }

    fun transmuteRecipe(
        game: GameStateEntity,
        recipe: AlchemyRecipeId
    ): CosmicActionOutcome {
        return when (recipe) {
            AlchemyRecipeId.POINTS_TO_CHIPS -> {
                val costPts = recipe.pointCostFor(game)
                if (game.diem < costPts) {
                    return CosmicActionOutcome(
                        null,
                        "Cần ${Formatters.formatPoints(costPts)} điểm để ép Chip!",
                        isError = true
                    )
                }
                val next = MegaUpdateEngine.addSeasonXp(
                    game.copy(
                        diem = game.diem - costPts,
                        techChips = game.techChips + 2L
                    ),
                    baseXp = 20L
                )
                CosmicActionOutcome(next, "Chuyển hóa thành công: +2⚙ Chip Công Nghệ & +20 XP!")
            }

            AlchemyRecipeId.SYNTH_QUANTUM_CORE -> {
                val dollarCost = 35.0
                val chipCost = 3L
                if (game.dollars < dollarCost || game.techChips < chipCost) {
                    return CosmicActionOutcome(
                        null,
                        "Cần $35 và 3⚙ Chip để ngưng tụ Lõi Lượng Tử!",
                        isError = true
                    )
                }
                val next = MegaUpdateEngine.addSeasonXp(
                    game.copy(
                        dollars = game.dollars - dollarCost,
                        techChips = game.techChips - chipCost,
                        quantumCores = game.quantumCores + 1L
                    ),
                    baseXp = 35L
                )
                CosmicActionOutcome(next, "Ngưng tụ thành công: +1⚛ Lõi Lượng Tử & +35 XP!")
            }

            AlchemyRecipeId.SHARDS_TO_QUANTUM_CORE -> {
                val shardCost = 3L
                val dollarCost = 20.0
                if (game.shards < shardCost || game.dollars < dollarCost) {
                    return CosmicActionOutcome(
                        null,
                        "Cần 3✦ Mảnh Lõi và $20 để tinh luyện Lõi Lượng Tử!",
                        isError = true
                    )
                }
                val next = MegaUpdateEngine.addSeasonXp(
                    game.copy(
                        shards = game.shards - shardCost,
                        dollars = game.dollars - dollarCost,
                        quantumCores = game.quantumCores + 2L
                    ),
                    baseXp = 45L
                )
                CosmicActionOutcome(next, "Tinh luyện thành công: +2⚛ Lõi Lượng Tử & +45 XP!")
            }

            AlchemyRecipeId.QUANTUM_ROYAL_CRATE -> {
                val coreCost = 3L
                if (game.quantumCores < coreCost) {
                    return CosmicActionOutcome(
                        null,
                        "Cần 3⚛ Lõi Lượng Tử để mở Rương Hoàng Gia!",
                        isError = true
                    )
                }
                val ptsReward = maxOf(25_000.0, (game.effectivePointsPerClick * 300.0).roundToLong().toDouble())
                val dollarReward = 75.0
                val shardReward = 3L
                val next = MegaUpdateEngine.addSeasonXp(
                    game.copy(
                        quantumCores = game.quantumCores - coreCost,
                        diem = game.diem + ptsReward,
                        lifetimePoints = game.lifetimePoints + ptsReward,
                        dollars = game.dollars + dollarReward,
                        shards = game.shards + shardReward
                    ),
                    baseXp = 85L
                )
                CosmicActionOutcome(
                    next,
                    "Mở Rương Lượng Tử: +${Formatters.formatPoints(ptsReward)}đ, +$75, +3✦ & +85 XP!"
                )
            }
        }
    }

    fun upgradeQuantumGear(
        game: GameStateEntity,
        gear: QuantumGearId
    ): CosmicActionOutcome {
        val currentLv = gear.levelIn(game)
        if (currentLv >= gear.maxLevel) {
            return CosmicActionOutcome(null, "${gear.title} đã đạt cấp tối đa (${gear.maxLevel})!", isError = true)
        }
        val coreCost = gear.coreCostAt(currentLv)
        val chipCost = gear.chipCostAt(currentLv)
        if (game.quantumCores < coreCost) {
            return CosmicActionOutcome(null, "Cần ${coreCost}⚛ Lõi Lượng Tử để rèn ${gear.title}!", isError = true)
        }
        if (game.techChips < chipCost) {
            return CosmicActionOutcome(null, "Cần ${chipCost}⚙ Chip Công Nghệ để rèn ${gear.title}!", isError = true)
        }

        val deducted = game.copy(
            quantumCores = game.quantumCores - coreCost,
            techChips = game.techChips - chipCost
        )
        val upgraded = when (gear) {
            QuantumGearId.CYBER_BLADE -> deducted.copy(gearBladeLv = currentLv + 1)
            QuantumGearId.AEGIS_PLATING -> deducted.copy(gearAegisLv = currentLv + 1)
            QuantumGearId.NEURAL_SYNC -> deducted.copy(gearNeuralLv = currentLv + 1)
            QuantumGearId.NOVA_THRUSTER -> deducted.copy(gearNovaLv = currentLv + 1)
        }
        val withXp = MegaUpdateEngine.addSeasonXp(upgraded, baseXp = 45L)
        return CosmicActionOutcome(
            updatedGame = withXp,
            message = "Rèn thành công ${gear.title} LV.${currentLv + 1} (+45 XP)!"
        )
    }

    fun attackArena(
        game: GameStateEntity,
        rollIsCrit: Boolean,
        isTapSurge: Boolean,
        isOverdrive: Boolean,
        today: Long = DayClock.localEpochDay()
    ): ArenaCombatOutcome {
        val synced = MegaUpdateEngine.syncDailyQuestDay(game, today)
        val hasShield = synced.arenaShieldCharges > 0
        val effectiveCrit = rollIsCrit || hasShield
        val shieldBoost = if (hasShield) 1.75 else 1.0
        val tapSurgeMult = if (isTapSurge) 4.0 else 1.0
        val overdriveMult = if (isOverdrive) 2.0 else 1.0
        val critMult = if (effectiveCrit) synced.critMultiplier else 1.0
        val gearArenaBoost = 1.0 + synced.totalGearLevel * 0.15

        val damage = maxOf(18.0, synced.effectivePointsPerClick) *
            synced.bossDamageMultiplier *
            gearArenaBoost *
            shieldBoost *
            tapSurgeMult *
            overdriveMult *
            critMult

        val nextShield = if (hasShield) synced.arenaShieldCharges - 1 else 0
        val nextFury = (synced.arenaFury + if (effectiveCrit) 22.0 else 16.0).coerceAtMost(100.0)
        val baseWorked = synced.copy(
            clicks = synced.clicks + 1L,
            totalCrits = synced.totalCrits + if (effectiveCrit) 1L else 0L,
            arenaShieldCharges = nextShield,
            arenaFury = nextFury,
            dailyQuestTaps = synced.dailyQuestTaps + 1,
            dailyQuestCrits = synced.dailyQuestCrits + if (effectiveCrit) 1 else 0
        )

        return resolveArenaDamage(baseWorked, damage, effectiveCrit)
    }

    fun castArenaOmega(
        game: GameStateEntity,
        today: Long = DayClock.localEpochDay()
    ): ArenaCombatOutcome {
        if (game.arenaFury < 100.0) {
            return ArenaCombatOutcome(
                updatedGame = null,
                damageDealt = 0.0,
                waveCleared = false,
                wasCrit = false,
                summary = "Cần 100% Nộ Khí để tung Tuyệt Kỹ Omega!",
                isError = true
            )
        }
        val synced = MegaUpdateEngine.syncDailyQuestDay(game, today)
        val gearArenaBoost = 1.0 + synced.totalGearLevel * 0.15
        val megaDmg = maxOf(40.0, synced.effectivePointsPerClick) *
            synced.bossDamageMultiplier *
            gearArenaBoost *
            synced.critMultiplier *
            4.5

        val bonusDollars = 15.0
        val bonusCores = 1L
        val clearedFuryGame = synced.copy(
            arenaFury = 0.0,
            dollars = synced.dollars + bonusDollars,
            quantumCores = synced.quantumCores + bonusCores,
            totalCrits = synced.totalCrits + 1L,
            totalSkillsCast = synced.totalSkillsCast + 1L
        )
        val res = resolveArenaDamage(clearedFuryGame, megaDmg, true)
        val finalMsg = if (res.waveCleared) {
            "OMEGA BÙNG NỔ! ${res.summary}"
        } else {
            "TUYỆT KỸ OMEGA: -${Formatters.formatPoints(megaDmg)} HP! (+$15 & +1⚛)"
        }
        return res.copy(summary = finalMsg)
    }

    private fun resolveArenaDamage(
        game: GameStateEntity,
        damage: Double,
        wasCrit: Boolean
    ): ArenaCombatOutcome {
        val remainingHp = game.arenaEnemyHp - damage
        return if (remainingHp <= 0.0) {
            val clearedWave = game.arenaWave
            val rDollars = ArenaBossCatalog.rewardDollars(clearedWave)
            val rShards = ArenaBossCatalog.rewardShards(clearedWave)
            val rChips = ArenaBossCatalog.rewardChips(clearedWave)
            val rCores = ArenaBossCatalog.rewardQuantumCores(clearedWave)
            val rGlory = ArenaBossCatalog.rewardGlory(clearedWave)
            val rXp = ArenaBossCatalog.rewardXp(clearedWave)

            val nextWave = clearedWave + 1
            val nextHp = ArenaBossCatalog.maxHpForWave(nextWave)
            val nextGame = MegaUpdateEngine.addSeasonXp(
                game.copy(
                    arenaWave = nextWave,
                    arenaEnemyHp = nextHp,
                    dollars = game.dollars + rDollars,
                    shards = game.shards + rShards,
                    techChips = game.techChips + rChips,
                    quantumCores = game.quantumCores + rCores,
                    arenaGlory = game.arenaGlory + rGlory,
                    totalCrits = game.totalCrits
                ),
                baseXp = rXp
            )
            ArenaCombatOutcome(
                updatedGame = nextGame,
                damageDealt = damage,
                waveCleared = true,
                wasCrit = wasCrit,
                summary = "VƯỢT ĐỢT $clearedWave! +$${rDollars.toLong()}, +${rShards}✦, +${rChips}⚙, +${rCores}⚛ & +${rGlory} Vinh Quang!"
            )
        } else {
            val nextGame = MegaUpdateEngine.addSeasonXp(
                game.copy(
                    arenaEnemyHp = remainingHp,
                    arenaGlory = game.arenaGlory + 3L
                ),
                baseXp = 4L
            )
            ArenaCombatOutcome(
                updatedGame = nextGame,
                damageDealt = damage,
                waveCleared = false,
                wasCrit = wasCrit,
                summary = (if (wasCrit) "CRIT -" else "-") + Formatters.formatPoints(damage) + " HP"
            )
        }
    }

    fun activateArenaShield(game: GameStateEntity): CosmicActionOutcome {
        val dollarCost = 30.0
        if (game.dollars < dollarCost) {
            return CosmicActionOutcome(null, "Cần $30 để nạp 5 Lần Khiên Xung Kích!", isError = true)
        }
        val next = game.copy(
            dollars = game.dollars - dollarCost,
            arenaShieldCharges = (game.arenaShieldCharges + 5).coerceAtMost(25)
        )
        return CosmicActionOutcome(next, "Đã nạp +5 Đòn Khiên Xung Kích (100% Crit & +75% DMG)!")
    }

    fun claimTowerFloor(game: GameStateEntity, floor: TowerFloorDef): CosmicActionOutcome {
        if (floor.floor <= game.towerHighestFloorClaimed) {
            return CosmicActionOutcome(null, "Tầng ${floor.floor} đã được chinh phục!", isError = true)
        }
        if (floor.floor != game.towerHighestFloorClaimed + 1) {
            return CosmicActionOutcome(
                null,
                "Hãy nhận thưởng Tầng ${game.towerHighestFloorClaimed + 1} trước!",
                isError = true
            )
        }
        if (!floor.isCompleted(game)) {
            return CosmicActionOutcome(null, "Chưa đủ điều kiện: ${floor.requirementText}", isError = true)
        }
        val next = MegaUpdateEngine.addSeasonXp(
            game.copy(
                dollars = game.dollars + floor.rewardDollars,
                shards = game.shards + floor.rewardShards,
                techChips = game.techChips + floor.rewardChips,
                quantumCores = game.quantumCores + floor.rewardCores,
                towerHighestFloorClaimed = floor.floor
            ),
            baseXp = floor.rewardXp
        )
        return CosmicActionOutcome(
            updatedGame = next,
            message = "Chinh phục Tầng ${floor.floor}! +$${floor.rewardDollars.toLong()}, +${floor.rewardShards}✦, +${floor.rewardChips}⚙, +${floor.rewardCores}⚛ (+15% Sản Lượng Vĩnh Viễn)!"
        )
    }

    fun countReadyTowerFloors(game: GameStateEntity): Int {
        val nextFloorNum = game.towerHighestFloorClaimed + 1
        val nextDef = InfinityTowerCatalog.floors.firstOrNull { it.floor == nextFloorNum } ?: return 0
        return if (nextDef.isCompleted(game)) 1 else 0
    }
}
