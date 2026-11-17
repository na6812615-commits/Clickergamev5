package com.example.data

import kotlin.math.pow
import kotlin.math.roundToLong

enum class FleetShipType(
    val id: String,
    val title: String,
    val roleLabel: String,
    val description: String,
    val maxLevel: Int,
    val baseDollars: Double,
    val baseShards: Long
) {
    SCOUT(
        id = "scout",
        title = "Drone Trinh Sát Mk.X",
        roleLabel = "Tốc độ & Chí mạng",
        description = "+4 Điểm/s cơ bản & +1.5% tỉ lệ Crit mỗi cấp",
        maxLevel = 20,
        baseDollars = 22.0,
        baseShards = 0L
    ),
    HARVESTER(
        id = "harvester",
        title = "Máy Gặt Lượng Tử",
        roleLabel = "Khai thác $ thụ động",
        description = "+10 Điểm/s & tự động thu +$0.35 mỗi 10 giây/cấp",
        maxLevel = 20,
        baseDollars = 45.0,
        baseShards = 1L
    ),
    CRUISER(
        id = "cruiser",
        title = "Tuần Dương Hạm Plasma",
        roleLabel = "Bắn phá Boss tự động",
        description = "+22 Điểm/s & tự động bắn Boss Lõi mỗi giây",
        maxLevel = 15,
        baseDollars = 95.0,
        baseShards = 2L
    ),
    MOTHERSHIP(
        id = "mothership",
        title = "Mẫu Hạm Hố Đen",
        roleLabel = "Cộng hưởng toàn Hạm đội",
        description = "+45 Điểm/s, +25% sản lượng toàn Hạm đội & +10% quà Thám Hiểm/cấp",
        maxLevel = 10,
        baseDollars = 240.0,
        baseShards = 4L
    );

    fun levelIn(game: GameStateEntity): Int = when (this) {
        SCOUT -> game.fleetScoutLv
        HARVESTER -> game.fleetHarvesterLv
        CRUISER -> game.fleetCruiserLv
        MOTHERSHIP -> game.fleetMothershipLv
    }

    fun dollarCostAt(level: Int): Double =
        (baseDollars * 1.48.pow(level)).roundToLong().toDouble()

    fun shardCostAt(level: Int): Long =
        if (baseShards == 0L) 0L else baseShards + (level / 2L)
}

data class ExpeditionZoneDef(
    val id: String,
    val title: String,
    val subtitle: String,
    val durationSec: Long,
    val minFleetPower: Int,
    val basePoints: Double,
    val baseDollars: Double,
    val baseShards: Long,
    val baseTechChips: Long,
    val seasonXp: Long
)

object ExpeditionCatalog {
    val zones: List<ExpeditionZoneDef> = listOf(
        ExpeditionZoneDef(
            id = "exp_belt",
            title = "Vành Đai Thiên Thạch Đỏ",
            subtitle = "Chuyến ngắn • Thu thập mảnh kim loại & Chip cơ bản",
            durationSec = 45L,
            minFleetPower = 1,
            basePoints = 3_500.0,
            baseDollars = 28.0,
            baseShards = 1L,
            baseTechChips = 2L,
            seasonXp = 45L
        ),
        ExpeditionZoneDef(
            id = "exp_nebula",
            title = "Tinh Vân Neon Tím",
            subtitle = "Chuyến trung bình • Bơm tiền $ & Chip Công Nghệ",
            durationSec = 150L,
            minFleetPower = 4,
            basePoints = 15_000.0,
            baseDollars = 95.0,
            baseShards = 3L,
            baseTechChips = 5L,
            seasonXp = 95L
        ),
        ExpeditionZoneDef(
            id = "exp_pulsar",
            title = "Lõi Sao Neutron",
            subtitle = "Chuyến dài • Nguồn Mảnh Lõi ✦ dồi dào",
            durationSec = 360L,
            minFleetPower = 10,
            basePoints = 65_000.0,
            baseDollars = 240.0,
            baseShards = 6L,
            baseTechChips = 10L,
            seasonXp = 175L
        ),
        ExpeditionZoneDef(
            id = "exp_void",
            title = "Chân Trời Hố Đen Vô Cực",
            subtitle = "Viễn chinh tối thượng • Kho báu Thiên Hà khổng lồ",
            durationSec = 720L,
            minFleetPower = 20,
            basePoints = 220_000.0,
            baseDollars = 620.0,
            baseShards = 14L,
            baseTechChips = 22L,
            seasonXp = 320L
        )
    )

    fun findById(id: String): ExpeditionZoneDef? = zones.firstOrNull { it.id == id }
}

enum class CyberStockTicker(
    val symbol: String,
    val title: String,
    val description: String,
    val initialPrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val volatilityPercent: Double
) {
    CORE(
        symbol = "CORE",
        title = "TapCore Energy",
        description = "Cổ phiếu năng lượng lõi ổn định, biến động nhẹ",
        initialPrice = 24.0,
        minPrice = 10.0,
        maxPrice = 68.0,
        volatilityPercent = 0.08
    ),
    NEON(
        symbol = "NEON",
        title = "Neon Synth Grid",
        description = "Mạng lưới điện tử tăng trưởng cân bằng",
        initialPrice = 52.0,
        minPrice = 18.0,
        maxPrice = 160.0,
        volatilityPercent = 0.14
    ),
    QBIT(
        symbol = "QBIT",
        title = "Quantum Bit Token",
        description = "Tiền mã hóa lượng tử biến động mạnh",
        initialPrice = 110.0,
        minPrice = 30.0,
        maxPrice = 420.0,
        volatilityPercent = 0.22
    ),
    VOID(
        symbol = "VOID",
        title = "Void Singularity",
        description = "Tài sản hố đen siêu biến động — lướt sóng cực gắt!",
        initialPrice = 240.0,
        minPrice = 55.0,
        maxPrice = 1150.0,
        volatilityPercent = 0.32
    );

    fun sharesIn(game: GameStateEntity): Long = when (this) {
        CORE -> game.stockCoreShares
        NEON -> game.stockNeonShares
        QBIT -> game.stockQbitShares
        VOID -> game.stockVoidShares
    }

    fun avgCostIn(game: GameStateEntity): Double = when (this) {
        CORE -> game.stockCoreAvgCost
        NEON -> game.stockNeonAvgCost
        QBIT -> game.stockQbitAvgCost
        VOID -> game.stockVoidAvgCost
    }
}

enum class TechNodeId(
    val id: String,
    val tier: Int,
    val title: String,
    val effectPerLevel: String,
    val maxLevel: Int,
    val baseChipCost: Long
) {
    OVERCLOCK(
        id = "tech_overclock",
        tier = 1,
        title = "Ép Xung Lõi Xung Kích",
        effectPerLevel = "+25% Điểm/Tap mỗi cấp",
        maxLevel = 12,
        baseChipCost = 2L
    ),
    AUTO_PROTOCOL(
        id = "tech_auto_protocol",
        tier = 1,
        title = "Giao Thức Tự Động AI",
        effectPerLevel = "+30% sản lượng Auto Click mỗi cấp",
        maxLevel = 12,
        baseChipCost = 2L
    ),
    CONVERTER(
        id = "tech_converter",
        tier = 2,
        title = "Lưới Chuyển Hóa Tiền Đô",
        effectPerLevel = "+20% Tiền $ nhận được khi đổi Điểm",
        maxLevel = 10,
        baseChipCost = 3L
    ),
    REACTOR_CORE(
        id = "tech_reactor_core",
        tier = 2,
        title = "Khoang Phản Ứng Phá Giáp",
        effectPerLevel = "+30% DMG lên Boss & +20% sạc Overdrive",
        maxLevel = 10,
        baseChipCost = 3L
    ),
    MARKET_INTEL(
        id = "tech_market_intel",
        tier = 3,
        title = "Phân Tích Thiên Hà & Sàn $",
        effectPerLevel = "+15% Thưởng Thám Hiểm & giảm 15% phí sàn chứng khoán",
        maxLevel = 8,
        baseChipCost = 4L
    ),
    SUPER_CAPACITOR(
        id = "tech_super_cap",
        tier = 3,
        title = "Siêu Tụ Điện Kỹ Năng",
        effectPerLevel = "-10% hồi chiêu & +15% uy lực Kỹ Năng Chủ Động",
        maxLevel = 6,
        baseChipCost = 5L
    );

    fun levelIn(game: GameStateEntity): Int = when (this) {
        OVERCLOCK -> game.techOverclockLv
        AUTO_PROTOCOL -> game.techAutoProtocolLv
        CONVERTER -> game.techConverterLv
        REACTOR_CORE -> game.techReactorCoreLv
        MARKET_INTEL -> game.techMarketIntelLv
        SUPER_CAPACITOR -> game.techSuperCapLv
    }

    fun chipCostAt(level: Int): Long = baseChipCost * (level + 1L)
}

enum class CyberSkillId(
    val id: String,
    val title: String,
    val shortTitle: String,
    val description: String,
    val baseCooldownSec: Long,
    val durationSec: Long
) {
    EMP_BURST(
        id = "skill_emp",
        title = "Xung Điện Từ (EMP Burst)",
        shortTitle = "EMP",
        description = "Bùng nổ 25x Điểm/Tap tức thì & giáng đòn mạnh lên Boss Lõi!",
        baseCooldownSec = 20L,
        durationSec = 0L
    ),
    HYPER_FOCUS(
        id = "skill_hyper",
        title = "Siêu Hội Tụ (Hyper Focus)",
        shortTitle = "Hội Tụ",
        description = "100% Chí Mạng + x2.5 Điểm/Tap trong 10 giây!",
        baseCooldownSec = 35L,
        durationSec = 10L
    ),
    QUANTUM_RAIN(
        id = "skill_rain",
        title = "Mưa Tài Nguyên (Quantum Rain)",
        shortTitle = "Mưa $",
        description = "Thu hoạch ngay 30s sản lượng Hạm đội + Auto + Thụ động & thưởng Tiền $!",
        baseCooldownSec = 45L,
        durationSec = 0L
    )
}
