package com.example.data

import kotlin.math.pow
import kotlin.math.roundToLong

enum class EmpireDistrictId(
    val id: String,
    val title: String,
    val subtitle: String,
    val benefitSummary: String,
    val maxLevel: Int,
    val baseDollarCost: Double,
    val baseChipCost: Long,
    val baseShardCost: Long
) {
    SOLAR_MATRIX(
        id = "solar_matrix",
        title = "Ma Trận Mặt Trời",
        subtitle = "Thu thập năng lượng quang tử liên tục cho Lõi TapCore",
        benefitSummary = "+28đ/s cơ bản & +8% Tổng Sản Lượng / cấp",
        maxLevel = 20,
        baseDollarCost = 18.0,
        baseChipCost = 1L,
        baseShardCost = 0L
    ),
    DOLLAR_FOUNDRY(
        id = "dollar_foundry",
        title = "Xưởng Đúc Đô La",
        subtitle = "Tinh luyện ngoại tệ $ và tăng tỷ giá quy đổi toàn hệ thống",
        benefitSummary = "+$0.90 / 10s vào Kho Bạc & +10% Tỷ Giá $ / cấp",
        maxLevel = 20,
        baseDollarCost = 24.0,
        baseChipCost = 1L,
        baseShardCost = 0L
    ),
    SHARD_EXTRACTOR(
        id = "shard_extractor",
        title = "Mỏ Tinh Thể Lõi",
        subtitle = "Khai thác Mảnh Lõi ✦ tự động và cường hóa thưởng Thám Hiểm",
        benefitSummary = "+1✦ mỗi 30s & +12% Thưởng Thám Hiểm / cấp",
        maxLevel = 15,
        baseDollarCost = 35.0,
        baseChipCost = 2L,
        baseShardCost = 1L
    ),
    QUANTUM_LAB(
        id = "quantum_lab",
        title = "Viện Lượng Tử",
        subtitle = "Tổng hợp ⚛ Lõi Lượng Tử định kỳ và khuếch đại Auto Click",
        benefitSummary = "+1⚛ mỗi 40s & +18% Sức Mạnh Auto/Thụ Động / cấp",
        maxLevel = 15,
        baseDollarCost = 45.0,
        baseChipCost = 3L,
        baseShardCost = 1L
    ),
    NEXUS_CITADEL(
        id = "nexus_citadel",
        title = "Thành Trì Nexus",
        subtitle = "Pháo đài chỉ huy tối thượng khuếch đại toàn bộ Đế Chế & Hạm Đội",
        benefitSummary = "+20% Hiệu Lực Đế Chế, +0.25x Crit DMG & +3 Lực Chiến / cấp",
        maxLevel = 12,
        baseDollarCost = 70.0,
        baseChipCost = 4L,
        baseShardCost = 2L
    );

    fun levelIn(game: GameStateEntity): Int = when (this) {
        SOLAR_MATRIX -> game.empireSolarLv
        DOLLAR_FOUNDRY -> game.empireFoundryLv
        SHARD_EXTRACTOR -> game.empireExtractorLv
        QUANTUM_LAB -> game.empireLabLv
        NEXUS_CITADEL -> game.empireCitadelLv
    }

    fun dollarCostAt(level: Int): Double =
        (baseDollarCost * 1.45.pow(level.coerceAtLeast(0))).roundToLong().toDouble()

    fun chipCostAt(level: Int): Long =
        baseChipCost + (level / 2L)

    fun shardCostAt(level: Int): Long =
        if (baseShardCost == 0L) 0L else baseShardCost + (level / 3L)
}

enum class AlchemyRecipeId(
    val id: String,
    val title: String,
    val inputSummary: String,
    val outputSummary: String
) {
    POINTS_TO_CHIPS(
        id = "points_to_chips",
        title = "Ép Điểm Thành Chip ⚙",
        inputSummary = "Điểm Năng Lượng",
        outputSummary = "+2⚙ Chip Công Nghệ & +20 XP"
    ),
    SYNTH_QUANTUM_CORE(
        id = "synth_quantum_core",
        title = "Ngưng Tụ Lõi Lượng Tử",
        inputSummary = "$35 + 3⚙ Chip",
        outputSummary = "+1⚛ Lõi Lượng Tử & +35 XP"
    ),
    SHARDS_TO_QUANTUM_CORE(
        id = "shards_to_quantum_core",
        title = "Tinh Luyện Mảnh Lõi",
        inputSummary = "3✦ Mảnh Lõi + $20",
        outputSummary = "+2⚛ Lõi Lượng Tử & +45 XP"
    ),
    QUANTUM_ROYAL_CRATE(
        id = "quantum_royal_crate",
        title = "Rương Lượng Tử Hoàng Gia",
        inputSummary = "3⚛ Lõi Lượng Tử",
        outputSummary = "Điểm Lớn + $75 + 3✦ + 85 XP"
    );

    fun pointCostFor(game: GameStateEntity): Double =
        maxOf(1_500.0, (game.effectivePointsPerClick * 65.0).roundToLong().toDouble())
}

enum class QuantumGearId(
    val id: String,
    val title: String,
    val slotLabel: String,
    val bonusSummary: String,
    val maxLevel: Int,
    val baseCores: Long,
    val baseChips: Long
) {
    CYBER_BLADE(
        id = "cyber_blade",
        title = "Lưỡi Kiếm Cyber",
        slotLabel = "Vũ Khí",
        bonusSummary = "+35% Sức Mạnh Tap & +25% Sát Thương Đấu Trường / cấp",
        maxLevel = 15,
        baseCores = 1L,
        baseChips = 2L
    ),
    AEGIS_PLATING(
        id = "aegis_plating",
        title = "Giáp Aegis Lượng Tử",
        slotLabel = "Giáp Trụ",
        bonusSummary = "+35% Thụ Động/Auto & +2% Tỉ Lệ Chí Mạng / cấp",
        maxLevel = 15,
        baseCores = 1L,
        baseChips = 2L
    ),
    NEURAL_SYNC(
        id = "neural_sync",
        title = "Mũ Đồng Bộ Thần Kinh",
        slotLabel = "Cảm Biến",
        bonusSummary = "+0.3s Combo & +12% Tốc Độ Nạp Overdrive / cấp",
        maxLevel = 12,
        baseCores = 2L,
        baseChips = 3L
    ),
    NOVA_THRUSTER(
        id = "nova_thruster",
        title = "Động Cơ Siêu Tân Tinh",
        slotLabel = "Phản Lực",
        bonusSummary = "+25% Sản Lượng Hạm Đội & +15% Tỷ Giá $ / cấp",
        maxLevel = 12,
        baseCores = 2L,
        baseChips = 3L
    );

    fun levelIn(game: GameStateEntity): Int = when (this) {
        CYBER_BLADE -> game.gearBladeLv
        AEGIS_PLATING -> game.gearAegisLv
        NEURAL_SYNC -> game.gearNeuralLv
        NOVA_THRUSTER -> game.gearNovaLv
    }

    fun coreCostAt(level: Int): Long = baseCores + level.toLong()

    fun chipCostAt(level: Int): Long = baseChips + (level * 2L)
}

object ArenaBossCatalog {
    private val guardianNames = listOf(
        "Vệ Binh Ánh Sáng Alpha",
        "Kỵ Sĩ Hư Không Neon",
        "Cự Thần Lượng Tử",
        "Phượng Hoàng Bão Từ",
        "Đao Phủ Thiên Hà",
        "Long Thần Mạch Điện",
        "Thực Thể Hố Đen Mk.IX",
        "Thiên Sứ Phản Vật Chất",
        "Hoàng Đế Bầu Trời Cyber",
        "Titan Vô Cực Omega"
    )

    fun titleForWave(wave: Int): String {
        val idx = ((wave - 1).coerceAtLeast(0)) % guardianNames.size
        return "${guardianNames[idx]} (Đợt $wave)"
    }

    fun maxHpForWave(wave: Int): Double {
        val clamped = (wave - 1).coerceIn(0, 50)
        return (450.0 * 1.48.pow(clamped)).roundToLong().toDouble()
    }

    fun rewardDollars(wave: Int): Double = (18.0 + wave * 9.0)

    fun rewardShards(wave: Int): Long = 1L + (wave / 2L)

    fun rewardChips(wave: Int): Long = 2L + (wave / 2L)

    fun rewardQuantumCores(wave: Int): Long = if (wave % 2 == 0) 2L else 1L

    fun rewardGlory(wave: Int): Long = 25L + wave * 12L

    fun rewardXp(wave: Int): Long = 45L + wave * 14L
}

data class TowerFloorDef(
    val floor: Int,
    val title: String,
    val requirementText: String,
    val rewardDollars: Double,
    val rewardShards: Long,
    val rewardChips: Long,
    val rewardCores: Long,
    val rewardXp: Long,
    val isCompleted: (GameStateEntity) -> Boolean
)

object InfinityTowerCatalog {
    val floors: List<TowerFloorDef> = listOf(
        TowerFloorDef(
            floor = 1,
            title = "Tầng 1: Ngọn Lửa Đầu Tiên",
            requirementText = "Tổng 5.000 điểm trọn đời",
            rewardDollars = 25.0,
            rewardShards = 2L,
            rewardChips = 3L,
            rewardCores = 1L,
            rewardXp = 50L,
            isCompleted = { it.lifetimePoints >= 5_000.0 }
        ),
        TowerFloorDef(
            floor = 2,
            title = "Tầng 2: Nhịp Đập Cao Tốc",
            requirementText = "Đạt Combo tốt nhất từ 15x",
            rewardDollars = 35.0,
            rewardShards = 2L,
            rewardChips = 4L,
            rewardCores = 1L,
            rewardXp = 65L,
            isCompleted = { it.bestCombo >= 15 }
        ),
        TowerFloorDef(
            floor = 3,
            title = "Tầng 3: Khởi Công Đế Chế",
            requirementText = "Đạt Tổng cấp Đế Chế Cyber ≥ 3",
            rewardDollars = 50.0,
            rewardShards = 3L,
            rewardChips = 5L,
            rewardCores = 2L,
            rewardXp = 80L,
            isCompleted = { it.totalEmpireLevel >= 3 }
        ),
        TowerFloorDef(
            floor = 4,
            title = "Tầng 4: Hạm Đội Xuất Kích",
            requirementText = "Đạt Tổng Lực Chiến Hạm Đội ≥ 6",
            rewardDollars = 65.0,
            rewardShards = 3L,
            rewardChips = 6L,
            rewardCores = 2L,
            rewardXp = 100L,
            isCompleted = { it.fleetTotalPower >= 6 }
        ),
        TowerFloorDef(
            floor = 5,
            title = "Tầng 5: Săn Boss Vũ Trụ",
            requirementText = "Hạ gục ít nhất 3 Boss Lõi",
            rewardDollars = 85.0,
            rewardShards = 4L,
            rewardChips = 7L,
            rewardCores = 2L,
            rewardXp = 120L,
            isCompleted = { it.bossesDefeated >= 3L }
        ),
        TowerFloorDef(
            floor = 6,
            title = "Tầng 6: Thợ Rèn Lượng Tử",
            requirementText = "Đạt Tổng cấp Trang Bị Lượng Tử ≥ 3",
            rewardDollars = 110.0,
            rewardShards = 5L,
            rewardChips = 8L,
            rewardCores = 3L,
            rewardXp = 140L,
            isCompleted = { it.totalGearLevel >= 3 }
        ),
        TowerFloorDef(
            floor = 7,
            title = "Tầng 7: Đấu Sĩ Thiên Hà",
            requirementText = "Chinh phục Đấu Trường tới Đợt 4+",
            rewardDollars = 140.0,
            rewardShards = 6L,
            rewardChips = 10L,
            rewardCores = 3L,
            rewardXp = 165L,
            isCompleted = { it.arenaWave >= 4 }
        ),
        TowerFloorDef(
            floor = 8,
            title = "Tầng 8: Đỉnh Cao Khoa Học",
            requirementText = "Đạt Tổng cấp Cây Công Nghệ ≥ 6",
            rewardDollars = 180.0,
            rewardShards = 7L,
            rewardChips = 12L,
            rewardCores = 4L,
            rewardXp = 190L,
            isCompleted = { it.totalTechLevel >= 6 }
        ),
        TowerFloorDef(
            floor = 9,
            title = "Tầng 9: Siêu Đô Thị Cyber",
            requirementText = "Đạt Tổng cấp Đế Chế Cyber ≥ 12",
            rewardDollars = 240.0,
            rewardShards = 8L,
            rewardChips = 15L,
            rewardCores = 5L,
            rewardXp = 220L,
            isCompleted = { it.totalEmpireLevel >= 12 }
        ),
        TowerFloorDef(
            floor = 10,
            title = "Tầng 10: Bá Chủ Đấu Trường",
            requirementText = "Chinh phục Đấu Trường tới Đợt 10+",
            rewardDollars = 320.0,
            rewardShards = 10L,
            rewardChips = 18L,
            rewardCores = 6L,
            rewardXp = 260L,
            isCompleted = { it.arenaWave >= 10 }
        ),
        TowerFloorDef(
            floor = 11,
            title = "Tầng 11: Thần Binh Bất Diệt",
            requirementText = "Đạt Tổng cấp Trang Bị Lượng Tử ≥ 12",
            rewardDollars = 450.0,
            rewardShards = 12L,
            rewardChips = 22L,
            rewardCores = 8L,
            rewardXp = 320L,
            isCompleted = { it.totalGearLevel >= 12 }
        ),
        TowerFloorDef(
            floor = 12,
            title = "Tầng 12: Ngai Vàng Vô Cực",
            requirementText = "2.5M điểm trọn đời & Đấu Trường Đợt 15+",
            rewardDollars = 750.0,
            rewardShards = 18L,
            rewardChips = 35L,
            rewardCores = 12L,
            rewardXp = 500L,
            isCompleted = { it.lifetimePoints >= 2_500_000.0 && it.arenaWave >= 15 }
        )
    )
}
