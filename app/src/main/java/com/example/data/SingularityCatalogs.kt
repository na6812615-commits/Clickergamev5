package com.example.data

import kotlin.math.pow
import kotlin.math.roundToLong

enum class CyberDroneId(
    val id: String,
    val title: String,
    val roleLabel: String,
    val benefitSummary: String,
    val maxLevel: Int,
    val baseDollarCost: Double,
    val baseChipCost: Long,
    val baseCoreCost: Long
) {
    PULSE_DRONE(
        id = "pulse_drone",
        title = "Drone Xung Nhịp α",
        roleLabel = "Tấn Công",
        benefitSummary = "+30% Sức Mạnh Tap, +1.5% Crit & tự phát xung Điểm mỗi giây / cấp",
        maxLevel = 20,
        baseDollarCost = 22.0,
        baseChipCost = 2L,
        baseCoreCost = 0L
    ),
    MINER_DRONE(
        id = "miner_drone",
        title = "Drone Khoan Dữ Liệu",
        roleLabel = "Kinh Tế",
        benefitSummary = "+15% Tỷ Giá $, tạo $ tự động & khai thác Chip ⚙ định kỳ / cấp",
        maxLevel = 20,
        baseDollarCost = 28.0,
        baseChipCost = 2L,
        baseCoreCost = 0L
    ),
    STRIKER_DRONE(
        id = "striker_drone",
        title = "Drone Oanh Tạc Mk.X",
        roleLabel = "Đột Kích",
        benefitSummary = "+25% Sát Thương Boss/Raid, tự bắn Boss & nạp Overdrive +15% / cấp",
        maxLevel = 15,
        baseDollarCost = 40.0,
        baseChipCost = 3L,
        baseCoreCost = 1L
    ),
    CHRONO_DRONE(
        id = "chrono_drone",
        title = "Drone Thời Không Ω",
        roleLabel = "Hỗ Trợ",
        benefitSummary = "-4% Hồi Chiêu Kỹ Năng (tối đa -40%) & +20% Sản Lượng Đế Chế / cấp",
        maxLevel = 12,
        baseDollarCost = 55.0,
        baseChipCost = 4L,
        baseCoreCost = 2L
    );

    fun levelIn(game: GameStateEntity): Int = when (this) {
        PULSE_DRONE -> game.dronePulseLv
        MINER_DRONE -> game.droneMinerLv
        STRIKER_DRONE -> game.droneStrikerLv
        CHRONO_DRONE -> game.droneChronoLv
    }

    fun dollarCostAt(level: Int): Double =
        (baseDollarCost * 1.48.pow(level.coerceAtLeast(0))).roundToLong().toDouble()

    fun chipCostAt(level: Int): Long = baseChipCost + (level / 2L)

    fun coreCostAt(level: Int): Long = if (baseCoreCost == 0L) 0L else baseCoreCost + (level / 3L)
}

enum class CyberRuneId(
    val id: String,
    val title: String,
    val tag: String,
    val effectSummary: String,
    val maxRank: Int,
    val baseShardCost: Long,
    val baseCoreCost: Long,
    val baseVoidCost: Long
) {
    BERSERK(
        id = "berserk",
        title = "Rune Cuồng Nộ Đỏ",
        tag = "SÁT THƯƠNG",
        effectSummary = "+45% Tap, +0.6x Crit DMG, +3% Crit & -10% Cooldown Tap / bậc",
        maxRank = 5,
        baseShardCost = 2L,
        baseCoreCost = 1L,
        baseVoidCost = 1L
    ),
    MIDAS(
        id = "midas",
        title = "Rune Hoàng Kim Midas",
        tag = "KINH TẾ",
        effectSummary = "+45% Tỷ Giá $ & +35% Thu Nhập Đô La Toàn Hệ Thống / bậc",
        maxRank = 5,
        baseShardCost = 2L,
        baseCoreCost = 1L,
        baseVoidCost = 1L
    ),
    VAMPIRE(
        id = "vampire",
        title = "Rune Huyết Tinh Boss",
        tag = "ĐỘT KÍCH",
        effectSummary = "+50% Sát Thương Boss/Arena/Raid & +25% Nạp Overdrive / bậc",
        maxRank = 5,
        baseShardCost = 3L,
        baseCoreCost = 1L,
        baseVoidCost = 1L
    ),
    CHRONO(
        id = "chrono",
        title = "Rune Dòng Chảy Thời Gian",
        tag = "TỐC ĐỘ",
        effectSummary = "+40% Sức Mạnh Auto/Thụ Động & -6% Hồi Chiêu Kỹ Năng / bậc",
        maxRank = 5,
        baseShardCost = 3L,
        baseCoreCost = 2L,
        baseVoidCost = 1L
    ),
    HARVEST(
        id = "harvest",
        title = "Rune Thu Hoạch Hạm Đội",
        tag = "TÀI NGUYÊN",
        effectSummary = "+35% Thưởng Thám Hiểm, +25% Kho Bạc & +15% XP Mùa / bậc",
        maxRank = 5,
        baseShardCost = 3L,
        baseCoreCost = 2L,
        baseVoidCost = 1L
    ),
    SINGULARITY(
        id = "singularity",
        title = "Rune Lõi Kỳ Điểm",
        tag = "TỐI THƯỢNG",
        effectSummary = "+40% Tất Cả Sản Lượng nhân theo Cấp Kỳ Điểm của bạn / bậc",
        maxRank = 5,
        baseShardCost = 5L,
        baseCoreCost = 3L,
        baseVoidCost = 2L
    );

    fun rankIn(game: GameStateEntity): Int = when (this) {
        BERSERK -> game.runeBerserkLv
        MIDAS -> game.runeMidasLv
        VAMPIRE -> game.runeVampireLv
        CHRONO -> game.runeChronoLv
        HARVEST -> game.runeHarvestLv
        SINGULARITY -> game.runeSingularityLv
    }

    fun shardCostAt(rank: Int): Long = baseShardCost * (rank + 1L)
    fun coreCostAt(rank: Int): Long = baseCoreCost + rank.toLong()
    fun voidCostAt(rank: Int): Long = if (rank == 0) 0L else baseVoidCost * rank.toLong()
}

data class RaidDungeonDef(
    val id: String,
    val title: String,
    val subtitle: String,
    val energyCost: Int,
    val baseStageHp: Double,
    val stageNames: List<String>,
    val rewardDollars: Double,
    val rewardChips: Long,
    val rewardShards: Long,
    val rewardCores: Long,
    val rewardVoidEssence: Long,
    val rewardXp: Long
)

object RaidDungeonCatalog {
    const val MAX_RAID_ENERGY = 5
    const val TAPS_PER_ENERGY = 30

    val dungeons: List<RaidDungeonDef> = listOf(
        RaidDungeonDef(
            id = "red_datacenter",
            title = "Trạm Dữ Liệu Đỏ",
            subtitle = "Đột kích 3 lớp tường lửa bảo mật thu thập Chip & Đô La",
            energyCost = 1,
            baseStageHp = 400.0,
            stageNames = listOf("Tường Lửa Sentinel", "Máy Chủ Mã Độc", "Lõi Dữ Liệu Đỏ"),
            rewardDollars = 35.0,
            rewardChips = 5L,
            rewardShards = 2L,
            rewardCores = 1L,
            rewardVoidEssence = 1L,
            rewardXp = 55L
        ),
        RaidDungeonDef(
            id = "shadow_reactor",
            title = "Lò Phản Ứng Bóng Tối",
            subtitle = "Phá hủy 3 trạm biến áp bóng tối để đoạt Lõi Lượng Tử & Tinh Chất",
            energyCost = 2,
            baseStageHp = 2_400.0,
            stageNames = listOf("Vệ Binh Bóng Tối", "Tuabin Phản Vật Chất", "Chúa Tể Lò Hạt Nhân"),
            rewardDollars = 95.0,
            rewardChips = 10L,
            rewardShards = 4L,
            rewardCores = 3L,
            rewardVoidEssence = 3L,
            rewardXp = 110L
        ),
        RaidDungeonDef(
            id = "omega_singularity",
            title = "Hố Đen Kỳ Điểm Omega",
            subtitle = "Hầm ngục tối thượng — rèn luyện hỏa lực săn 🌌 Tinh Chất Hư Không",
            energyCost = 3,
            baseStageHp = 15_000.0,
            stageNames = listOf("Kỵ Sĩ Chân Trời Sự Kiện", "Hư Vô Nuốt Chửng", "Hoàng Đế Kỳ Điểm Omega"),
            rewardDollars = 240.0,
            rewardChips = 20L,
            rewardShards = 8L,
            rewardCores = 6L,
            rewardVoidEssence = 7L,
            rewardXp = 220L
        )
    )

    fun findById(id: String): RaidDungeonDef? = dungeons.firstOrNull { it.id == id }
}

enum class SingularityPerkId(
    val id: String,
    val title: String,
    val benefitSummary: String,
    val maxLevel: Int,
    val baseVoidCost: Long
) {
    TAP_AMPLIFIER(
        id = "tap_amp",
        title = "Siêu Khuếch Đại Lõi",
        benefitSummary = "+50% Sức Mạnh Tap & Sản Lượng Toàn Hệ Thống vĩnh viễn / cấp",
        maxLevel = 15,
        baseVoidCost = 1L
    ),
    CASH_REACTOR(
        id = "cash_reactor",
        title = "Lò Đúc Hư Không",
        benefitSummary = "+40% Tỷ Giá $ & Khởi đầu với +$15/cấp sau mỗi lần Kỳ Điểm / Prestige",
        maxLevel = 12,
        baseVoidCost = 1L
    ),
    DRONE_SWARM(
        id = "drone_swarm",
        title = "Trí Tuệ Bầy Đàn AI",
        benefitSummary = "+35% Toàn Bộ Sản Lượng & Sát Thương Drone AI / cấp",
        maxLevel = 15,
        baseVoidCost = 1L
    ),
    BOSS_DESTROYER(
        id = "boss_destroyer",
        title = "Phá Hủy Thiên Hà",
        benefitSummary = "+45% Sát Thương Boss, Đấu Trường & Ngục Tối Raid / cấp",
        maxLevel = 12,
        baseVoidCost = 2L
    );

    fun levelIn(game: GameStateEntity): Int = when (this) {
        TAP_AMPLIFIER -> game.singularityTapPerkLv
        CASH_REACTOR -> game.singularityCashPerkLv
        DRONE_SWARM -> game.singularityDronePerkLv
        BOSS_DESTROYER -> game.singularityBossPerkLv
    }

    fun voidCostAt(level: Int): Long = baseVoidCost + level.toLong()
}
