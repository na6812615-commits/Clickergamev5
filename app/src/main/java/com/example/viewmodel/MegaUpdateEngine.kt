package com.example.viewmodel

import com.example.data.BetCurrency
import com.example.data.BossCatalog
import com.example.data.CasinoCatalog
import com.example.data.DailyQuestCatalog
import com.example.data.DailyRewardCatalog
import com.example.data.DayClock
import com.example.data.GameStateEntity
import com.example.data.RelicId
import com.example.data.SeasonPassCatalog
import com.example.data.SeasonRewardItem
import com.example.data.SeasonRewardType
import com.example.data.WheelSliceDef
import com.example.ui.Formatters
import kotlin.math.roundToLong
import kotlin.random.Random

data class SupplyDrop(
    val id: Long,
    val label: String,
    val rewardType: SupplyDropReward,
    val amount: Double
)

enum class SupplyDropReward {
    POINTS,
    SHARDS,
    DOLLARS,
    SEASON_XP
}

data class MinefieldState(
    val currency: BetCurrency,
    val betAmount: Double,
    val bombIndices: Set<Int>,
    val revealedSafe: Set<Int> = emptySet(),
    val hitBombIndex: Int? = null,
    val isActive: Boolean = true
) {
    val currentMultiplier: Double
        get() = CasinoCatalog.mineMultipliers[revealedSafe.size.coerceIn(0, CasinoCatalog.mineMultipliers.lastIndex)]

    val currentPayout: Double
        get() = betAmount * currentMultiplier
}

object MegaUpdateEngine {

    fun syncDailyQuestDay(game: GameStateEntity, today: Long = DayClock.localEpochDay()): GameStateEntity {
        return if (game.dailyQuestDay == today) {
            game
        } else {
            game.copy(
                dailyQuestDay = today,
                dailyQuestTaps = 0,
                dailyQuestCrits = 0,
                dailyQuestGambles = 0,
                dailyQuestBossKills = 0,
                claimedDailyQuests = ""
            )
        }
    }

    fun addSeasonXp(game: GameStateEntity, baseXp: Long): GameStateEntity {
        val mult = if (game.seasonPassPremium) 1.5 else 1.0
        val gained = (baseXp * mult).roundToLong().coerceAtLeast(1L)
        return game.copy(seasonXp = game.seasonXp + gained)
    }

    fun countUnclaimedSeasonRewards(game: GameStateEntity): Int {
        val currentLv = game.seasonLevel
        var count = 0
        for (tier in SeasonPassCatalog.tiers) {
            if (tier.level <= currentLv) {
                if (!game.isSeasonFreeClaimed(tier.level)) count++
                if (game.seasonPassPremium && !game.isSeasonEliteClaimed(tier.level)) count++
            }
        }
        return count
    }

    fun countUnclaimedDailyQuests(game: GameStateEntity, today: Long = DayClock.localEpochDay()): Int {
        val synced = syncDailyQuestDay(game, today)
        return DailyQuestCatalog.all.count { q ->
            !synced.isDailyQuestClaimed(q.id) && q.progressSelector(synced) >= q.target
        }
    }

    data class DailyClaimOutcome(
        val updatedGame: GameStateEntity?,
        val message: String,
        val isError: Boolean
    )

    fun claimDailyReward(
        game: GameStateEntity,
        today: Long = DayClock.localEpochDay()
    ): DailyClaimOutcome {
        if (!DailyRewardCatalog.canClaimToday(game, today)) {
            return DailyClaimOutcome(null, "Hôm nay bạn đã điểm danh rồi! Quay lại vào ngày mai nhé.", true)
        }
        val newStreak = if (game.lastDailyClaimDay == today - 1L) game.dailyStreak + 1 else 1
        val dayIndex = ((newStreak - 1) % 7) + 1
        val def = DailyRewardCatalog.weekRewards[dayIndex - 1]

        val vipMult = if (game.hasVipPass) 2.0 else 1.0
        val streakMult = if (newStreak >= 7) 1.25 else 1.0
        val totalMult = vipMult * streakMult

        val pts = DailyRewardCatalog.scaledPointsFor(def, game) * totalMult
        val dollars = (def.dollars * totalMult).roundToLong().toDouble()
        val shards = (def.shards * totalMult).roundToLong().coerceAtLeast(1L)
        val xp = (def.seasonXp * totalMult).roundToLong().coerceAtLeast(def.seasonXp)

        val updated = addSeasonXp(
            game.copy(
                diem = game.diem + pts,
                lifetimePoints = game.lifetimePoints + pts,
                dollars = game.dollars + dollars,
                shards = game.shards + shards,
                dailyStreak = newStreak,
                lastDailyClaimDay = today,
                totalDailyClaims = game.totalDailyClaims + 1
            ),
            baseXp = xp
        )
        return DailyClaimOutcome(
            updatedGame = updated,
            message = "Điểm danh Ngày $dayIndex (Chuỗi $newStreak)! +$${dollars.toLong()}, +${shards}✦, +${xp} XP & +${Formatters.formatPoints(pts)}đ!",
            isError = false
        )
    }

    fun applySeasonReward(
        game: GameStateEntity,
        reward: SeasonRewardItem
    ): GameStateEntity {
        return when (reward.type) {
            SeasonRewardType.POINTS -> {
                val scaled = maxOf(reward.amount, game.effectivePointsPerClick * 120.0)
                game.copy(
                    diem = game.diem + scaled,
                    lifetimePoints = game.lifetimePoints + scaled
                )
            }
            SeasonRewardType.DOLLARS -> game.copy(dollars = game.dollars + reward.amount)
            SeasonRewardType.SHARDS -> game.copy(shards = game.shards + reward.amount.roundToLong())
        }
    }

    fun canAfford(game: GameStateEntity, currency: BetCurrency, amount: Double): Boolean = when (currency) {
        BetCurrency.POINTS -> game.diem >= amount
        BetCurrency.DOLLARS -> game.dollars >= amount
    }

    fun deductCurrency(game: GameStateEntity, currency: BetCurrency, amount: Double): GameStateEntity = when (currency) {
        BetCurrency.POINTS -> game.copy(diem = (game.diem - amount).coerceAtLeast(0.0))
        BetCurrency.DOLLARS -> game.copy(dollars = (game.dollars - amount).coerceAtLeast(0.0))
    }

    fun addCurrency(game: GameStateEntity, currency: BetCurrency, amount: Double): GameStateEntity = when (currency) {
        BetCurrency.POINTS -> game.copy(
            diem = game.diem + amount,
            lifetimePoints = game.lifetimePoints + amount
        )
        BetCurrency.DOLLARS -> game.copy(dollars = game.dollars + amount)
    }

    fun formatBet(currency: BetCurrency, amount: Double): String = when (currency) {
        BetCurrency.POINTS -> "${Formatters.formatPoints(amount)} điểm"
        BetCurrency.DOLLARS -> Formatters.formatDollars(amount)
    }

    fun pickWeightedWheelSlice(luckLv: Int, random: Random = Random): WheelSliceDef {
        // Relic luck slightly boosts high-value slices (id >= 3)
        val adjusted = CasinoCatalog.wheelSlices.map { slice ->
            val boost = if (slice.id >= 3) luckLv * 2 else 0
            slice to (slice.weight + boost)
        }
        val totalWeight = adjusted.sumOf { it.second }
        var roll = random.nextInt(totalWeight)
        for ((slice, weight) in adjusted) {
            if (roll < weight) return slice
            roll -= weight
        }
        return CasinoCatalog.wheelSlices.first()
    }

    data class BossHitOutcome(
        val updatedGame: GameStateEntity,
        val damageDealt: Double,
        val bossDefeated: Boolean,
        val rewardSummary: String?
    )

    fun hitBoss(
        game: GameStateEntity,
        isCrit: Boolean,
        isTapSurge: Boolean,
        isOverdrive: Boolean,
        today: Long = DayClock.localEpochDay()
    ): BossHitOutcome {
        val synced = syncDailyQuestDay(game, today)
        val tapSurgeMult = if (isTapSurge) 4.0 else 1.0
        val overdriveMult = if (isOverdrive) 2.0 else 1.0
        val critMult = if (isCrit) synced.critMultiplier else 1.0
        val rawDmg = maxOf(12.0, synced.effectivePointsPerClick) *
            synced.bossDamageMultiplier *
            tapSurgeMult *
            overdriveMult *
            critMult

        val remainingHp = synced.bossCurrentHp - rawDmg
        val bonusPoints = synced.effectivePointsPerClick * tapSurgeMult * overdriveMult * 0.5
        var nextGame = synced.copy(
            diem = synced.diem + bonusPoints,
            lifetimePoints = synced.lifetimePoints + bonusPoints,
            clicks = synced.clicks + 1L,
            totalCrits = synced.totalCrits + if (isCrit) 1L else 0L,
            dailyQuestTaps = synced.dailyQuestTaps + 1,
            dailyQuestCrits = synced.dailyQuestCrits + if (isCrit) 1 else 0
        )

        return if (remainingHp <= 0.0) {
            val defeatedLevel = synced.bossLevel
            val rewardDollars = BossCatalog.rewardDollars(defeatedLevel, synced.relicLuckLv)
            val rewardShards = BossCatalog.rewardShards(defeatedLevel, synced.relicLuckLv)
            val rewardChips = (2L + defeatedLevel / 2L).coerceAtLeast(2L)
            val rewardXp = BossCatalog.rewardSeasonXp(defeatedLevel)

            val nextLevel = defeatedLevel + 1
            val nextHp = BossCatalog.maxHpForLevel(nextLevel)
            nextGame = addSeasonXp(
                nextGame.copy(
                    dollars = nextGame.dollars + rewardDollars,
                    shards = nextGame.shards + rewardShards,
                    techChips = nextGame.techChips + rewardChips,
                    bossLevel = nextLevel,
                    bossCurrentHp = nextHp,
                    bossesDefeated = nextGame.bossesDefeated + 1L,
                    dailyQuestBossKills = nextGame.dailyQuestBossKills + 1
                ),
                baseXp = rewardXp
            )
            val summary = "HẠ GỤC BOSS LV.$defeatedLevel! +${rewardShards}✦, +${rewardChips}⚙, +$${rewardDollars.toLong()}, +${rewardXp} XP!"
            BossHitOutcome(nextGame, rawDmg, true, summary)
        } else {
            nextGame = addSeasonXp(nextGame.copy(bossCurrentHp = remainingHp), baseXp = 2L)
            BossHitOutcome(nextGame, rawDmg, false, null)
        }
    }

    fun hitBossWithCustomDamage(
        game: GameStateEntity,
        damage: Double,
        today: Long = DayClock.localEpochDay()
    ): BossHitOutcome {
        val synced = syncDailyQuestDay(game, today)
        val safeDmg = damage.coerceAtLeast(1.0)
        val remainingHp = synced.bossCurrentHp - safeDmg
        return if (remainingHp <= 0.0) {
            val defeatedLevel = synced.bossLevel
            val rewardDollars = BossCatalog.rewardDollars(defeatedLevel, synced.relicLuckLv)
            val rewardShards = BossCatalog.rewardShards(defeatedLevel, synced.relicLuckLv)
            val rewardChips = (2L + defeatedLevel / 2L).coerceAtLeast(2L)
            val rewardXp = BossCatalog.rewardSeasonXp(defeatedLevel)
            val nextLevel = defeatedLevel + 1
            val nextHp = BossCatalog.maxHpForLevel(nextLevel)
            val nextGame = addSeasonXp(
                synced.copy(
                    dollars = synced.dollars + rewardDollars,
                    shards = synced.shards + rewardShards,
                    techChips = synced.techChips + rewardChips,
                    bossLevel = nextLevel,
                    bossCurrentHp = nextHp,
                    bossesDefeated = synced.bossesDefeated + 1L,
                    dailyQuestBossKills = synced.dailyQuestBossKills + 1
                ),
                baseXp = rewardXp
            )
            val summary = "HẠ GỤC BOSS LV.$defeatedLevel! +${rewardShards}✦, +${rewardChips}⚙, +$${rewardDollars.toLong()}, +${rewardXp} XP!"
            BossHitOutcome(nextGame, safeDmg, true, summary)
        } else {
            BossHitOutcome(synced.copy(bossCurrentHp = remainingHp), safeDmg, false, null)
        }
    }

    fun upgradeRelic(game: GameStateEntity, relic: RelicId): Pair<GameStateEntity?, String> {
        val currentLv = game.relicLevel(relic)
        if (currentLv >= relic.maxLevel) {
            return null to "${relic.title} đã đạt cấp tối đa!"
        }
        val cost = relic.costAt(currentLv)
        if (game.shards < cost) {
            return null to "Cần ${cost}✦ Mảnh Lõi để rèn ${relic.title}!"
        }
        val next = when (relic) {
            RelicId.TITAN -> game.copy(shards = game.shards - cost, relicTapLv = currentLv + 1)
            RelicId.CRIT_EYE -> game.copy(shards = game.shards - cost, relicCritLv = currentLv + 1)
            RelicId.LUCKY_CLOVER -> game.copy(shards = game.shards - cost, relicLuckLv = currentLv + 1)
            RelicId.BOSS_SLAYER -> game.copy(shards = game.shards - cost, relicBossLv = currentLv + 1)
            RelicId.ETERNAL_CELL -> game.copy(shards = game.shards - cost, relicPassiveLv = currentLv + 1)
        }
        return next to "Đã rèn ${relic.title} LV.${currentLv + 1}!"
    }
}
