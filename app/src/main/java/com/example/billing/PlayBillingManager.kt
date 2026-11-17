package com.example.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DiamondProductDef(
    val productId: String,
    val title: String,
    val subtitle: String,
    val diamondsGranted: Long,
    val bonusLabel: String?,
    val fallbackPriceVnd: String,
    val isVipPass: Boolean = false
)

object DiamondCatalog {
    val all: List<DiamondProductDef> = listOf(
        DiamondProductDef(
            productId = "tapcore_diamond_100",
            title = "Túi 100 Kim Cương",
            subtitle = "Khởi đầu rực rỡ • Cộng ngay +100💎",
            diamondsGranted = 100L,
            bonusLabel = null,
            fallbackPriceVnd = "29.000₫"
        ),
        DiamondProductDef(
            productId = "tapcore_diamond_550",
            title = "Rương 550 Kim Cương",
            subtitle = "Bao gồm +50💎 tặng thêm",
            diamondsGranted = 550L,
            bonusLabel = "PHỔ BIẾN • +10%",
            fallbackPriceVnd = "129.000₫"
        ),
        DiamondProductDef(
            productId = "tapcore_diamond_1400",
            title = "Kho Báu 1.400 Kim Cương",
            subtitle = "Gói cao cấp • Tặng thêm +350💎",
            diamondsGranted = 1_400L,
            bonusLabel = "+25% BONUS",
            fallbackPriceVnd = "299.000₫"
        ),
        DiamondProductDef(
            productId = "tapcore_diamond_4000",
            title = "Ngân Hà 4.000 Kim Cương",
            subtitle = "Tối thượng • Dư sức mua mọi nâng cấp VIP",
            diamondsGranted = 4_000L,
            bonusLabel = "SIÊU HỜI • +50%",
            fallbackPriceVnd = "699.000₫"
        ),
        DiamondProductDef(
            productId = "tapcore_vip_omega",
            title = "ĐẶC QUYỀN VIP OMEGA",
            subtitle = "Vĩnh viễn x3 Điểm & Auto, +10% Crit, x2 Nạp Overdrive & Tặng ngay +300💎",
            diamondsGranted = 300L,
            bonusLabel = "VĨNH VIỄN • VIP",
            fallbackPriceVnd = "199.000₫",
            isVipPass = true
        )
    )

    fun findById(productId: String): DiamondProductDef? = all.firstOrNull { it.productId == productId }
}

data class BillingUiState(
    val isPlayStoreConnected: Boolean = false,
    val isReadyToPurchase: Boolean = false,
    val statusMessage: String = "Đang kết nối Google Play Billing...",
    val formattedPrices: Map<String, String> = emptyMap(),
    val availableProductIds: Set<String> = emptySet(),
    val isDebugBuild: Boolean = BuildConfig.DEBUG
)

interface BillingController {
    val billingState: StateFlow<BillingUiState>
    fun startConnection()
    fun refreshPurchasesAndProducts()
    fun launchDiamondPurchase(activity: Activity, productId: String): Boolean
    fun release()
}

class NoOpBillingController : BillingController {
    private val _state = MutableStateFlow(
        BillingUiState(
            isPlayStoreConnected = false,
            isReadyToPurchase = false,
            statusMessage = "Chế độ kiểm thử không kết nối Google Play"
        )
    )
    override val billingState: StateFlow<BillingUiState> = _state.asStateFlow()
    override fun startConnection() {}
    override fun refreshPurchasesAndProducts() {}
    override fun launchDiamondPurchase(activity: Activity, productId: String): Boolean = false
    override fun release() {}
}

class PlayBillingManager(
    context: Context,
    private val onPurchaseGranted: (productId: String, quantity: Int) -> Unit,
    private val onBillingMessage: (message: String, isError: Boolean) -> Unit
) : BillingController, PurchasesUpdatedListener {

    private val appContext = context.applicationContext
    private val tokenPrefs = appContext.getSharedPreferences("tapcore_billing_tokens", Context.MODE_PRIVATE)

    private val _billingState = MutableStateFlow(BillingUiState())
    override val billingState: StateFlow<BillingUiState> = _billingState.asStateFlow()

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    override fun startConnection() {
        if (billingClient.isReady) {
            queryProductsAndExistingPurchases()
            return
        }
        try {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _billingState.update {
                            it.copy(
                                isPlayStoreConnected = true,
                                statusMessage = "Google Play đã kết nối • Đang kiểm tra gói Kim Cương..."
                            )
                        }
                        queryProductsAndExistingPurchases()
                    } else {
                        _billingState.update {
                            it.copy(
                                isPlayStoreConnected = false,
                                isReadyToPurchase = false,
                                statusMessage = "Google Play Billing chưa khả dụng (${billingResult.debugMessage.ifBlank { "Mã ${billingResult.responseCode}" }})"
                            )
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _billingState.update {
                        it.copy(
                            isPlayStoreConnected = false,
                            isReadyToPurchase = false,
                            statusMessage = "Mất kết nối dịch vụ Google Play • Đang tự động kết nối lại..."
                        )
                    }
                }
            })
        } catch (t: Throwable) {
            _billingState.update {
                it.copy(
                    isPlayStoreConnected = false,
                    isReadyToPurchase = false,
                    statusMessage = "Không thể khởi tạo Google Play Billing: ${t.message ?: "Lỗi hệ thống"}"
                )
            }
        }
    }

    override fun refreshPurchasesAndProducts() {
        if (!billingClient.isReady) {
            startConnection()
        } else {
            queryProductsAndExistingPurchases()
        }
    }

    private fun queryProductsAndExistingPurchases() {
        val productList = DiamondCatalog.all.map { def ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(def.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val list = queryProductDetailsResult.productDetailsList
                val priceMap = mutableMapOf<String, String>()
                val availableIds = mutableSetOf<String>()
                synchronized(productDetailsMap) {
                    productDetailsMap.clear()
                    list.forEach { details ->
                        productDetailsMap[details.productId] = details
                        availableIds.add(details.productId)
                        details.oneTimePurchaseOfferDetails?.formattedPrice?.let { price ->
                            priceMap[details.productId] = price
                        }
                    }
                }
                val ready = availableIds.isNotEmpty()
                _billingState.update {
                    it.copy(
                        isPlayStoreConnected = true,
                        isReadyToPurchase = ready,
                        formattedPrices = priceMap,
                        availableProductIds = availableIds,
                        statusMessage = if (ready) {
                            "Google Play Billing sẵn sàng (${availableIds.size} gói)"
                        } else {
                            "Đã kết nối Google Play • Gói nạp chưa xuất bản trên Play Console"
                        }
                    )
                }
            } else {
                _billingState.update {
                    it.copy(
                        isReadyToPurchase = false,
                        statusMessage = "Không tải được danh sách gói Play Store (Mã ${billingResult.responseCode})"
                    )
                }
            }
        }

        val purchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(purchasesParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { handlePurchase(it) }
            }
        }
    }

    override fun launchDiamondPurchase(activity: Activity, productId: String): Boolean {
        val details = synchronized(productDetailsMap) { productDetailsMap[productId] } ?: return false
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            onBillingMessage(
                "Không thể mở thanh toán Google Play (${result.debugMessage.ifBlank { "Mã ${result.responseCode}" }})",
                true
            )
        }
        return true
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                onBillingMessage("Bạn đã hủy giao dịch Google Play", true)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                queryProductsAndExistingPurchases()
                onBillingMessage("Bạn đã sở hữu gói này trên tài khoản Google Play", false)
            }
            else -> {
                onBillingMessage(
                    "Giao dịch không thành công (Mã ${billingResult.responseCode})",
                    true
                )
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            onBillingMessage("Giao dịch đang chờ thanh toán hoàn tất trên Google Play...", false)
            return
        }
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        val tokenKey = "token_${purchase.purchaseToken}"
        val alreadyGranted = tokenPrefs.getBoolean(tokenKey, false)

        if (!alreadyGranted) {
            tokenPrefs.edit().putBoolean(tokenKey, true).apply()
            val qty = purchase.quantity.coerceAtLeast(1)
            purchase.products.forEach { productId ->
                onPurchaseGranted(productId, qty)
            }
        }

        val hasNonConsumableVip = purchase.products.any { DiamondCatalog.findById(it)?.isVipPass == true }
        if (hasNonConsumableVip) {
            if (!purchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(ackParams) { }
            }
        } else {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.consumeAsync(consumeParams) { _, _ -> }
        }
    }

    override fun release() {
        try {
            billingClient.endConnection()
        } catch (_: Throwable) {
        }
    }
}
