package com.example.viewmodel

import com.example.data.CyberDroneId
import com.example.data.CyberRuneId
import com.example.data.DayClock
import com.example.data.GameStateEntity
import com.example.data.RaidDungeonCatalog
import com.example.data.RaidDungeonDef
import com.example.data.SingularityPerkId
import com.example.ui.Formatters
import kotlin.math.pow
import kotlin.math.roundToLong

data class SingularityActionOutcome(
    val updatedGame: GameStateEntity?,
    val message: String,
    val isError: Boolean = false
)

data class DungeonStrikeOutcome(
    val updatedGame: GameStateEntity?,
    val damageDealt: Double,
    val stageCleared: Boolean,
    val dungeonCleared: Boolean,
    val wasCrit: Boolean,
    val summary: String,
    val isError: Boolean = false
)

object SingularityEngine {

    fun upgradeDrone(game: GameStateEntity, drone: CyberDroneId): SingularityActionOutcome {
        val currentLv = drone.levelIn(game)
        if (currentLv >= drone.maxLevel) {
            return SingularityActionOutcome(null, "${drone.title} đã đạt cấp tối đa (${drone.maxLevel})!", isError = true)
        }
        val dCost = drone.dollarCostAt(currentLv)
        val cCost = drone.chipCostAt(currentLv)
        val qCost = drone.coreCostAt(currentLv)

        if (game.dollars < dCost) {
            return SingularityActionOutcome(null, "Cần ${Formatters.formatDollars(dCost)} để nâng cấp ${drone.title}!", isError = true)
        }
        if (game.techChips < cCost) {
            return SingularityActionOutcome(null, "Cần ${cCost}⚙ Chip để lắp đặt ${drone.title}!", isError = true)
        }
        if (game.quantumCores < qCost) {
            return SingularityActionOutcome(null, "Cần ${qCost}⚛ Lõi Lượng Tử để nâng cấp ${drone.title}!", isError = true)
        }

        val deducted = game.copy(
            dollars = game.dollars - dCost,
            techChips = game.techChips - cCost,
            quantumCores = game.quantumCores - qCost
        )
        val upgraded = when (drone) {
            CyberDroneId.PULSE_DRONE -> deducted.copy(dronePulseLv = currentLv + 1)
            CyberDroneId.MINER_DRONE -> deducted.copy(droneMinerLv = currentLv + 1)
            CyberDroneId.STRIKER_DRONE -> deducted.copy(droneStrikerLv = currentLv + 1)
            CyberDroneId.CHRONO_DRONE -> deducted.copy(droneChronoLv = currentLv + 1)
        }
        val withXp = MegaUpdateEngine.addSeasonXp(upgraded, baseXp = 35L)
        return SingularityActionOutcome(
            updatedGame = withXp,
            message = "Nâng cấp ${drone.title} lên LV.${currentLv + 1} (+35 XP)!"
        )
    }

    fun activateDroneOverclock(
        game: GameStateEntity,
        nowMillis: Long = System.currentTimeMillis()
    ): SingularityActionOutcome {
        if (game.totalDroneLevel <= 0) {
            return SingularityActionOutcome(null, "Hãy chế tạo ít nhất 1 Drone AI trước khi Ép Xung!", isError = true)
        }
        if (game.droneOverclockEndAt > nowMillis) {
            val rem = ((game.droneOverclockEndAt - nowMillis) / 1000L).coerceAtLeast(1L)
            return SingularityActionOutcome(null, "Bầy Đàn Drone đang Ép Xung (còn ${rem}s)!", isError = true)
        }
        val dCost = 25.0
        val cCost = 2L
        if (game.dollars < dCost || game.techChips < cCost) {
            return SingularityActionOutcome(null, "Cần $25 & 2⚙ Chip để kích hoạt Ép Xung Bầy Đàn (x3 trong 30s)!", isError = true)
        }
        val next = MegaUpdateEngine.addSeasonXp(
            game.copy(
                dollars = game.dollars - dCost,
                techChips = game.techChips - cCost,
                droneOverclockEndAt = nowMillis + 30_000L
            ),
            baseXp = 25L
        )
        return SingularityActionOutcome(next, "ÉP XUNG BẦY ĐÀN: Toàn bộ Drone AI x3 sức mạnh trong 30s!")
    }

    fun upgradeRune(game: GameStateEntity, rune: CyberRuneId): SingularityActionOutcome {
        val currentRank = rune.rankIn(game)
        if (currentRank >= rune.maxRank) {
            return SingularityActionOutcome(null, "${rune.title} đã đạt Bậc Tối Đa (${rune.maxRank})!", isError = true)
        }
        val sCost = rune.shardCostAt(currentRank)
        val qCost = rune.coreCostAt(currentRank)
        val vCost = rune.voidCostAt(currentRank)

        if (game.shards < sCost) {
            return SingularityActionOutcome(null, "Cần ${sCost}✦ Mảnh Lõi để rèn ${rune.title}!", isError = true)
        }
        if (game.quantumCores < qCost) {
            return SingularityActionOutcome(null, "Cần ${qCost}⚛ Lõi Lượng Tử để rèn ${rune.title}!", isError = true)
        }
        if (game.voidEssence < vCost) {
            return SingularityActionOutcome(null, "Cần ${vCost}🌌 Tinh Chất Hư Không để thăng bậc ${rune.title}!", isError = true)
        }

        val deducted = game.copy(
            shards = game.shards - sCost,
            quantumCores = game.quantumCores - qCost,
            voidEssence = game.voidEssence - vCost
        )
        var upgraded = when (rune) {
            CyberRuneId.BERSERK -> deducted.copy(runeBerserkLv = currentRank + 1)
            CyberRuneId.MIDAS -> deducted.copy(runeMidasLv = currentRank + 1)
            CyberRuneId.VAMPIRE -> deducted.copy(runeVampireLv = currentRank + 1)
            CyberRuneId.CHRONO -> deducted.copy(runeChronoLv = currentRank + 1)
            CyberRuneId.HARVEST -> deducted.copy(runeHarvestLv = currentRank + 1)
            CyberRuneId.SINGULARITY -> deducted.copy(runeSingularityLv = currentRank + 1)
        }
        // Auto-equip newly forged Rune if a slot (<3) is available
        val equipped = upgraded.equippedRuneIds.toMutableList()
        if (!equipped.contains(rune.id) && equipped.size < 3) {
            equipped.add(rune.id)
            upgraded = upgraded.copy(equippedRunes = equipped.joinToString(","))
        }
        val withXp = MegaUpdateEngine.addSeasonXp(upgraded, baseXp = 45L)
        val actionWord = if (currentRank == 0) "Chế tạo & Trang bị" else "Thăng Bậc ${currentRank + 1}"
        return SingularityActionOutcome(withXp, "$actionWord ${rune.title} thành công (+45 XP)!")
    }

    fun toggleEquipRune(game: GameStateEntity, rune: CyberRuneId): SingularityActionOutcome {
        val rank = rune.rankIn(game)
        if (rank <= 0) {
            return SingularityActionOutcome(null, "Hãy chế tạo ${rune.title} trước khi trang bị!", isError = true)
        }
        val current = game.equippedRuneIds.toMutableList()
        return if (current.contains(rune.id)) {
            current.remove(rune.id)
            SingularityActionOutcome(
                game.copy(equippedRunes = current.joinToString(",")),
                "Đã tháo ${rune.title} khỏi Ma Trận Rune."
            )
        } else {
            if (current.size >= 3) {
                return SingularityActionOutcome(null, "Ma Trận Rune tối đa 3 ô! Hãy tháo 1 Rune trước.", isError = true)
            }
            current.add(rune.id)
            val bonusTag = when (current.size) {
                2 -> " (Cộng Hưởng 2-Rune: +20%!)"
                3 -> " (Cộng Hưởng 3-Rune: +45%!)"
                else -> ""
            }
            SingularityActionOutcome(
                game.copy(equippedRunes = current.joinToString(",")),
                "Đã lắp ${rune.title}$bonusTag"
            )
        }
    }

    fun rechargeRaidEnergy(game: GameStateEntity): SingularityActionOutcome {
        if (game.raidEnergy >= RaidDungeonCatalog.MAX_RAID_ENERGY) {
            return SingularityActionOutcome(null, "Chìa Khóa Raid đã đầy (${RaidDungeonCatalog.MAX_RAID_ENERGY}/${RaidDungeonCatalog.MAX_RAID_ENERGY})!", isError = true)
        }
        val dCost = 20.0
        if (game.dollars < dCost) {
            return SingularityActionOutcome(null, "Cần $20 để nạp +2 Chìa Khóa Raid (hoặc Tap 30 lần = +1 Chìa)!", isError = true)
        }
        val nextEnergy = (game.raidEnergy + 2).coerceAtMost(RaidDungeonCatalog.MAX_RAID_ENERGY)
        return SingularityActionOutcome(
            game.copy(dollars = game.dollars - dCost, raidEnergy = nextEnergy),
            "Đã nạp Chìa Khóa Raid ($nextEnergy/${RaidDungeonCatalog.MAX_RAID_ENERGY})!"
        )
    }

    fun enterDungeon(game: GameStateEntity, dungeon: RaidDungeonDef): SingularityActionOutcome {
        if (game.activeDungeonId.isNotBlank()) {
            return SingularityActionOutcome(null, "Bạn đang chiến đấu trong một Ngục Tối! Hãy hoàn tất trước.", isError = true)
        }
        if (game.raidEnergy < dungeon.energyCost) {
            return SingularityActionOutcome(null, "Cần ${dungeon.energyCost} Chìa Khóa Raid để vào ${dungeon.title}!", isError = true)
        }
        val startHp = stageHp(dungeon, stage = 1)
        val next = game.copy(
            raidEnergy = game.raidEnergy - dungeon.energyCost,
            activeDungeonId = dungeon.id,
            dungeonStage = 1,
            dungeonEnemyHp = startHp
        )
        return SingularityActionOutcome(next, "Tiến vào ${dungeon.title} — Ải 1/3: ${dungeon.stageNames[0]}!")
    }

    fun stageHp(dungeon: RaidDungeonDef, stage: Int): Double {
        val factor = 1.65.pow((stage - 1).coerceAtLeast(0))
        return (dungeon.baseStageHp * factor).roundToLong().toDouble()
    }

    fun strikeDungeonEnemy(
        game: GameStateEntity,
        rollIsCrit: Boolean,
        isOverdrive: Boolean,
        today: Long = DayClock.localEpochDay()
    ): DungeonStrikeOutcome {
        val dungeon = RaidDungeonCatalog.findById(game.activeDungeonId)
            ?: return DungeonStrikeOutcome(null, 0.0, false, false, false, "Hãy chọn Ngục Tối Raid để bắt đầu!", isError = true)

        val synced = MegaUpdateEngine.syncDailyQuestDay(game, today)
        val critMult = if (rollIsCrit) synced.critMultiplier else 1.0
        val odMult = if (isOverdrive) 2.0 else 1.0
        val droneCombatBoost = 1.0 + synced.droneStrikerLv * 0.25 * synced.droneSwarmMultiplier
        val damage = maxOf(35.0, synced.effectivePointsPerClick) *
            synced.bossDamageMultiplier *
            droneCombatBoost *
            critMult *
            odMult

        val remHp = synced.dungeonEnemyHp - damage
        val baseTapped = synced.copy(
            clicks = synced.clicks + 1L,
            totalCrits = synced.totalCrits + if (rollIsCrit) 1L else 0L,
            dailyQuestTaps = synced.dailyQuestTaps + 1,
            dailyQuestCrits = synced.dailyQuestCrits + if (rollIsCrit) 1 else 0
        )

        return if (remHp <= 0.0) {
            if (synced.dungeonStage < 3) {
                val nextStage = synced.dungeonStage + 1
                val nextHp = stageHp(dungeon, nextStage)
                val next = MegaUpdateEngine.addSeasonXp(
                    baseTapped.copy(
                        dungeonStage = nextStage,
                        dungeonEnemyHp = nextHp
                    ),
                    baseXp = 15L
                )
                DungeonStrikeOutcome(
                    updatedGame = next,
                    damageDealt = damage,
                    stageCleared = true,
                    dungeonCleared = false,
                    wasCrit = rollIsCrit,
                    summary = "Hạ Ải ${synced.dungeonStage}/3! Tiếp theo: ${dungeon.stageNames[nextStage - 1]}"
                )
            } else {
                val essenceBonus = (synced.droneChronoLv / 4L)
                val totalVoid = dungeon.rewardVoidEssence + essenceBonus
                val next = MegaUpdateEngine.addSeasonXp(
                    baseTapped.copy(
                        activeDungeonId = "",
                        dungeonStage = 1,
                        dungeonEnemyHp = 0.0,
                        totalDungeonsCleared = synced.totalDungeonsCleared + 1L,
                        dollars = synced.dollars + dungeon.rewardDollars,
                        techChips = synced.techChips + dungeon.rewardChips,
                        shards = synced.shards + dungeon.rewardShards,
                        quantumCores = synced.quantumCores + dungeon.rewardCores,
                        voidEssence = synced.voidEssence + totalVoid
                    ),
                    baseXp = dungeon.rewardXp
                )
                DungeonStrikeOutcome(
                    updatedGame = next,
                    damageDealt = damage,
                    stageCleared = true,
                    dungeonCleared = true,
                    wasCrit = rollIsCrit,
                    summary = "CHINH PHỤC ${dungeon.title}! +$${dungeon.rewardDollars.toLong()}, +${dungeon.rewardChips}⚙, +${dungeon.rewardShards}✦, +${dungeon.rewardCores}⚛, +${totalVoid}🌌!"
                )
            }
        } else {
            DungeonStrikeOutcome(
                updatedGame = baseTapped.copy(dungeonEnemyHp = remHp),
                damageDealt = damage,
                stageCleared = false,
                dungeonCleared = false,
                wasCrit = rollIsCrit,
                summary = (if (rollIsCrit) "CRIT -" else "-") + Formatters.formatPoints(damage) + " HP"
            )
        }
    }

    fun performSingularityAscension(game: GameStateEntity): SingularityActionOutcome {
        if (!game.canAscendSingularity) {
            return SingularityActionOutcome(
                null,
                "Cần đạt Restart Lv.${game.singularityRequirementRestartLv} hoặc ${Formatters.formatPoints(game.singularityRequirementPoints)} điểm trọn đời để Vượt Cấp Kỳ Điểm!",
                isError = true
            )
        }
        val earnedVoid = game.pendingVoidEssenceOnSingularity
        val nextSingularityLv = game.singularityLv + 1
        val startDollars = game.singularityCashPerkLv * 15.0
        val startPoints = game.permStartPointsAmount
        val startDiscount = 1.0 + game.permStartDiscountBonus

        val ascended = game.copy(
            diem = startPoints,
            dollars = startDollars,
            clicks = 0L,
            multiplier = 1.0 + nextSingularityLv * 0.5,
            baseMultiplier = 1.0 + nextSingularityLv * 0.5,
            pointsPerClick = 1.0,
            clickCooldownSec = 0.25,
            cooldownStep = 0.125,
            discountFactor = startDiscount,
            discountStep = 0.1,
            pointPowerLv = 0,
            pointPowerCost = 25.0,
            multiplierLv = 0,
            multiplierCost = 50.0,
            cooldownLv = 0,
            cooldownCost = 75.0,
            discountLv = 0,
            discountCost = 5.0,
            menuSpeedLv = 0,
            menuSpeedCost = 3.0,
            menuTransitionSec = 1.0,
            toastSpeedLv = 0,
            toastSpeedCost = 3.0,
            toastDurationSec = 1.0,
            autoUnlocked = false,
            autoEnabled = false,
            autoPoints = 1.0,
            autoCooldownSec = 1.0,
            autoPowerLv = 0,
            autoPowerCost = 15.0,
            autoSpeedLv = 0,
            autoSpeedCost = 20.0,
            autoSpeedStep = 0.25,
            restartLv = 0,
            restartCost = 150.0,
            critChanceLv = 0,
            critChanceCost = 120.0,
            critMultLv = 0,
            critMultCost = 200.0,
            comboWindowLv = 0,
            comboWindowCost = 90.0,
            passiveLv = 0,
            passiveCost = 8.0,
            overdriveCharge = 0.0,
            overdriveLv = 0,
            overdriveCost = 12.0,
            lifetimePoints = game.lifetimePoints + startPoints,
            singularityLv = nextSingularityLv,
            voidEssence = game.voidEssence + earnedVoid
        )
        val withXp = MegaUpdateEngine.addSeasonXp(ascended, baseXp = 150L)
        return SingularityActionOutcome(
            updatedGame = withXp,
            message = "VƯỢT CẤP KỲ ĐIỂM LV.$nextSingularityLv! Nhận +${earnedVoid}🌌 Tinh Chất Hư Không & +65% Toàn Bộ Sản Lượng!"
        )
    }

    fun upgradeSingularityPerk(game: GameStateEntity, perk: SingularityPerkId): SingularityActionOutcome {
        val currentLv = perk.levelIn(game)
        if (currentLv >= perk.maxLevel) {
            return SingularityActionOutcome(null, "${perk.title} đã đạt cấp tối đa (${perk.maxLevel})!", isError = true)
        }
        val cost = perk.voidCostAt(currentLv)
        if (game.voidEssence < cost) {
            return SingularityActionOutcome(null, "Cần ${cost}🌌 Tinh Chất Hư Không để nâng ${perk.title}!", isError = true)
        }
        val deducted = game.copy(voidEssence = game.voidEssence - cost)
        val upgraded = when (perk) {
            SingularityPerkId.TAP_AMPLIFIER -> deducted.copy(singularityTapPerkLv = currentLv + 1)
            SingularityPerkId.CASH_REACTOR -> deducted.copy(singularityCashPerkLv = currentLv + 1)
            SingularityPerkId.DRONE_SWARM -> deducted.copy(singularityDronePerkLv = currentLv + 1)
            SingularityPerkId.BOSS_DESTROYER -> deducted.copy(singularityBossPerkLv = currentLv + 1)
        }
        return SingularityActionOutcome(upgraded, "Thăng Hoa ${perk.title} LV.${currentLv + 1} thành công!")
    }
}
