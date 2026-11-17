package com.example.data

import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.roundToLong

object DayClock {
    fun localEpochDay(nowMs: Long = System.currentTimeMillis()): Long {
        val offsetMs = TimeZone.getDefault().getOffset(nowMs).toLong()
        return TimeUnit.MILLISECONDS.toDays(nowMs + offsetMs)
    }
}

data class DailyRewardDef(
    val dayNumber: Int, // 1..7
    val title: String,
    val subtitle: String,
    val basePoints: Double,
    val dollars: Double,
    val shards: Long,
    val seasonXp: Long
)

object DailyRewardCatalog {
    val weekRewards: List<DailyRewardDef> = listOf(
        DailyRewardDef(
            dayNumber = 1,
            title = "Khởi Động Năng Lượng",
            subtitle = "Quà chào ngày mới",
            basePoints = 2_500.0,
            dollars = 25.0,
            shards = 1L,
            seasonXp = 70L
        ),
        DailyRewardDef(
            dayNumber = 2,
            title = "Tiếp Tế Tinh Thể",
            subtitle = "Tích lũy Mảnh Lõi & Tiền mặt",
            basePoints = 5_000.0,
            dollars = 45.0,
            shards = 2L,
            seasonXp = 80L
        ),
        DailyRewardDef(
            dayNumber = 3,
            title = "Lõi Sạc Tốc Độ",
            subtitle = "Bơm mạnh Điểm & Season XP",
            basePoints = 10_000.0,
            dollars = 75.0,
            shards = 3L,
            seasonXp = 100L
        ),
        DailyRewardDef(
            dayNumber = 4,
            title = "Kho Đô La Ngầm",
            subtitle = "Bơm tiền mặt cho \$Shop\$",
            basePoints = 18_000.0,
            dollars = 160.0,
            shards = 4L,
            seasonXp = 115L
        ),
        DailyRewardDef(
            dayNumber = 5,
            title = "Tinh Thể Lượng Tử",
            subtitle = "Nâng sức mạnh Cổ Vật",
            basePoints = 32_000.0,
            dollars = 240.0,
            shards = 8L,
            seasonXp = 130L
        ),
        DailyRewardDef(
            dayNumber = 6,
            title = "Siêu Sạc Vô Cực",
            subtitle = "Bùng nổ tài nguyên",
            basePoints = 65_000.0,
            dollars = 380.0,
            shards = 12L,
            seasonXp = 150L
        ),
        DailyRewardDef(
            dayNumber = 7,
            title = "RƯƠNG MEGA CYBER",
            subtitle = "Đỉnh cao chuỗi 7 ngày!",
            basePoints = 180_000.0,
            dollars = 750.0,
            shards = 22L,
            seasonXp = 220L
        )
    )

    fun canClaimToday(game: GameStateEntity, todayEpochDay: Long = DayClock.localEpochDay()): Boolean {
        return game.lastDailyClaimDay < todayEpochDay
    }

    fun nextClaimDayIndex(game: GameStateEntity, todayEpochDay: Long = DayClock.localEpochDay()): Int {
        val nextStreak = if (game.lastDailyClaimDay == todayEpochDay - 1L) {
            game.dailyStreak + 1
        } else if (game.lastDailyClaimDay == todayEpochDay) {
            game.dailyStreak.coerceAtLeast(1)
        } else {
            1
        }
        return ((nextStreak - 1) % 7) + 1
    }

    fun scaledPointsFor(def: DailyRewardDef, game: GameStateEntity): Double {
        return maxOf(def.basePoints, game.effectivePointsPerClick * (def.dayNumber * 120.0))
    }
}

enum class SeasonRewardType {
    POINTS,
    DOLLARS,
    SHARDS
}

data class SeasonRewardItem(
    val type: SeasonRewardType,
    val amount: Double,
    val label: String
)

data class SeasonTierDef(
    val level: Int,
    val requiredCumulativeXp: Long,
    val freeReward: SeasonRewardItem,
    val eliteReward: SeasonRewardItem
)

object SeasonPassCatalog {
    const val SEASON_NAME = "MÙA 1: KỶ NGUYÊN LƯỢNG TỬ"
    const val ELITE_PASS_COST_DOLLARS = 450.0
    const val ELITE_PASS_COST_SHARDS = 10L
    const val ELITE_PASS_VIP_DISCOUNT_DOLLARS = 250.0
    const val ELITE_PASS_VIP_DISCOUNT_SHARDS = 6L
    const val MAX_LEVEL = 20

    val tiers: List<SeasonTierDef> = (1..MAX_LEVEL).map { lv ->
        val xpReq = (lv * 90L) + (lv * (lv - 1) * 20L)
        val free = when (lv % 3) {
            1 -> SeasonRewardItem(SeasonRewardType.POINTS, 3_500.0 * lv, "+${(3.5 * lv).roundToLong()}K Điểm")
            2 -> SeasonRewardItem(SeasonRewardType.DOLLARS, (30 + lv * 14).toDouble(), "+$${30 + lv * 14}")
            else -> SeasonRewardItem(SeasonRewardType.SHARDS, (2 + lv / 3).toDouble(), "+${2 + lv / 3}✦")
        }
        val elite = when {
            lv == MAX_LEVEL -> SeasonRewardItem(SeasonRewardType.SHARDS, 30.0, "+30✦ SIÊU PHẨM")
            lv % 5 == 0 -> SeasonRewardItem(SeasonRewardType.DOLLARS, (250 + lv * 40).toDouble(), "+$${250 + lv * 40}")
            lv % 3 == 0 -> SeasonRewardItem(SeasonRewardType.SHARDS, (5 + lv / 2).toDouble(), "+${5 + lv / 2}✦")
            lv % 2 == 0 -> SeasonRewardItem(SeasonRewardType.DOLLARS, (100 + lv * 30).toDouble(), "+$${100 + lv * 30}")
            else -> SeasonRewardItem(SeasonRewardType.POINTS, 12_000.0 * lv, "+${12 * lv}K Điểm")
        }
        SeasonTierDef(
            level = lv,
            requiredCumulativeXp = xpReq,
            freeReward = free,
            eliteReward = elite
        )
    }

    fun levelForXp(xp: Long): Int {
        var reached = 0
        for (tier in tiers) {
            if (xp >= tier.requiredCumulativeXp) {
                reached = tier.level
            } else {
                break
            }
        }
        return reached
    }

    fun nextTierForXp(xp: Long): SeasonTierDef? = tiers.firstOrNull { xp < it.requiredCumulativeXp }

    fun eliteUnlockCost(hasVip: Boolean): Pair<Double, Long> =
        if (hasVip) {
            ELITE_PASS_VIP_DISCOUNT_DOLLARS to ELITE_PASS_VIP_DISCOUNT_SHARDS
        } else {
            ELITE_PASS_COST_DOLLARS to ELITE_PASS_COST_SHARDS
        }
}

enum class BetCurrency(val label: String, val unitSymbol: String) {
    POINTS("Điểm", "Đ"),
    DOLLARS("Tiền $", "$")
}

data class WheelSliceDef(
    val id: Int,
    val label: String,
    val multiplier: Double,
    val bonusShards: Long = 0L,
    val isJackpot: Boolean = false,
    val weight: Int
)

object CasinoCatalog {
    val wheelSlices: List<WheelSliceDef> = listOf(
        WheelSliceDef(0, "x0.5", 0.5, weight = 18),
        WheelSliceDef(1, "x2.0", 2.0, weight = 18),
        WheelSliceDef(2, "HỤT!", 0.0, weight = 16),
        WheelSliceDef(3, "x3.0", 3.0, weight = 12),
        WheelSliceDef(4, "+3✦", 1.2, bonusShards = 3L, weight = 12),
        WheelSliceDef(5, "x5.0", 5.0, weight = 8),
        WheelSliceDef(6, "+6✦", 1.5, bonusShards = 6L, weight = 10),
        WheelSliceDef(7, "NỔ HŨ!", 10.0, bonusShards = 10L, isJackpot = true, weight = 6)
    )

    val slotSymbols = listOf("⚡", "✦", "💵", "🔥", "7️⃣")

    fun betAmountsFor(currency: BetCurrency, game: GameStateEntity): List<Double> = when (currency) {
        BetCurrency.POINTS -> {
            val base = maxOf(100.0, (game.effectivePointsPerClick * 15.0).roundToLong().toDouble())
            listOf(base, base * 5.0, base * 25.0)
        }
        BetCurrency.DOLLARS -> listOf(10.0, 50.0, 250.0)
    }

    val mineMultipliers: List<Double> = listOf(1.0, 1.35, 1.85, 2.6, 3.8, 5.5, 8.0, 12.0)
}

enum class RelicId(
    val id: String,
    val title: String,
    val effectPerLevel: String,
    val maxLevel: Int,
    val baseShardCost: Long
) {
    TITAN("titan", "Cổ Vật Titan", "+35% Điểm/Tap mỗi cấp", 15, 3L),
    CRIT_EYE("crit_eye", "Mắt Thần Lượng Tử", "+2.5% Tỉ lệ Crit & +0.4x DMG Crit", 12, 4L),
    LUCKY_CLOVER("lucky_clover", "Cỏ 4 Lá Neon", "+5% May mắn Casino & +15% Thưởng Boss", 12, 4L),
    BOSS_SLAYER("boss_slayer", "Pháo Phá Giáp", "+50% Sát thương lên Boss Lõi", 15, 3L),
    ETERNAL_CELL("eternal_cell", "Pin Vĩnh Cửu", "+35% Lõi Thụ Động & Auto Click", 15, 4L);

    fun costAt(level: Int): Long = baseShardCost * (level + 1L)
}

object BossCatalog {
    private val bossNames = listOf(
        "Sentinel MK-01",
        "Hộ Vệ Plasma",
        "Rồng Lõi Lượng Tử",
        "Ác Quỷ Bóng Tối",
        "Thực Thể Hố Đen",
        "Hoàng Đế Cyber Vô Cực"
    )

    fun titleForLevel(level: Int): String {
        val baseName = bossNames[(level - 1).coerceAtLeast(0) % bossNames.size]
        val cycle = ((level - 1) / bossNames.size) + 1
        return if (cycle > 1) "$baseName Mk.$cycle" else baseName
    }

    fun maxHpForLevel(level: Int): Double =
        (250.0 * 2.5.pow((level - 1).coerceAtLeast(0))).roundToLong().toDouble()

    fun rewardDollars(level: Int, luckLv: Int): Double =
        ((35.0 + level * 25.0) * (1.0 + luckLv * 0.15)).roundToLong().toDouble()

    fun rewardShards(level: Int, luckLv: Int): Long =
        ((3L + level / 2L) * (1.0 + luckLv * 0.12)).roundToLong().coerceAtLeast(3L)

    fun rewardSeasonXp(level: Int): Long = 55L + level * 18L
}

enum class BoosterType(
    val id: String,
    val title: String,
    val description: String,
    val durationSec: Long,
    val shardCost: Long
) {
    TAP_SURGE(
        id = "tap_surge",
        title = "Bão Sát Thương x4",
        description = "Nhân 4 lần Điểm/Tap & Sát thương Boss trong 30 giây",
        durationSec = 30L,
        shardCost = 4L
    ),
    CRIT_STORM(
        id = "crit_storm",
        title = "Bão Chí Mạng 100%",
        description = "Đảm bảo 100% Chí Mạng mọi cú chạm trong 20 giây",
        durationSec = 20L,
        shardCost = 5L
    ),
    AUTO_FRENZY(
        id = "auto_frenzy",
        title = "Siêu Ép Xung Auto",
        description = "Tăng tốc Auto Click x2.5 & sản lượng x2 trong 45 giây",
        durationSec = 45L,
        shardCost = 3L
    )
}

data class DailyQuestDef(
    val id: String,
    val title: String,
    val target: Int,
    val rewardXp: Long,
    val rewardDollars: Double,
    val rewardShards: Long,
    val progressSelector: (GameStateEntity) -> Int
)

object DailyQuestCatalog {
    val all: List<DailyQuestDef> = listOf(
        DailyQuestDef(
            id = "dq_taps_100",
            title = "Khởi động tay: Chạm Lõi 100 lần hôm nay",
            target = 100,
            rewardXp = 70L,
            rewardDollars = 45.0,
            rewardShards = 2L,
            progressSelector = { it.dailyQuestTaps }
        ),
        DailyQuestDef(
            id = "dq_crits_15",
            title = "Xạ thủ lượng tử: Kích hoạt 15 Chí mạng",
            target = 15,
            rewardXp = 95L,
            rewardDollars = 75.0,
            rewardShards = 3L,
            progressSelector = { it.dailyQuestCrits }
        ),
        DailyQuestDef(
            id = "dq_gamble_3",
            title = "Tay chơi Cyber: Chơi Casino 3 lượt",
            target = 3,
            rewardXp = 110L,
            rewardDollars = 90.0,
            rewardShards = 3L,
            progressSelector = { it.dailyQuestGambles }
        ),
        DailyQuestDef(
            id = "dq_boss_1",
            title = "Thợ săn Boss: Hạ gục 1 Boss Lõi",
            target = 1,
            rewardXp = 135L,
            rewardDollars = 140.0,
            rewardShards = 5L,
            progressSelector = { it.dailyQuestBossKills }
        ),
        DailyQuestDef(
            id = "dq_taps_350",
            title = "Siêu nhân Clicker: Chạm Lõi 350 lần",
            target = 350,
            rewardXp = 180L,
            rewardDollars = 220.0,
            rewardShards = 7L,
            progressSelector = { it.dailyQuestTaps }
        )
    )
}
