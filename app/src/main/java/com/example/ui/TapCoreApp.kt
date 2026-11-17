package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.audio.GameAudioController
import com.example.audio.LocalSoundManager
import com.example.audio.NoOpAudioController
import com.example.auth.AccountPrefs
import com.example.auth.GoogleAuthHelper
import com.example.billing.BillingController
import com.example.billing.NoOpBillingController
import com.example.ui.components.CyberBackground
import com.example.ui.components.FeedbackBanner
import com.example.ui.components.StatsHud
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AchievementBadgeOverlay
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.BossAndRelicsScreen
import com.example.ui.screens.CashShopScreen
import com.example.ui.screens.CasinoScreen
import com.example.ui.screens.CosmicArenaScreen
import com.example.ui.screens.CyberEmpireScreen
import com.example.ui.screens.CyberExchangeScreen
import com.example.ui.screens.DailyRewardsScreen
import com.example.ui.screens.DroneAndRuneScreen
import com.example.ui.screens.FeatureHubScreen
import com.example.ui.screens.GalaxyFleetScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.PointShopScreen
import com.example.ui.screens.PremiumShopScreen
import com.example.ui.screens.QuantumAlchemyScreen
import com.example.ui.screens.RestartScreen
import com.example.ui.screens.SeasonPassScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SingularityRaidScreen
import com.example.ui.screens.TapCoreScreen
import com.example.ui.screens.TechTreeScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameTab
import com.example.viewmodel.GameViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class NavDestination(
    val tab: GameTab,
    val label: String,
    val iconRes: Int,
    val tag: String
)

private val Destinations = listOf(
    NavDestination(
        tab = GameTab.TAP,
        label = "Chơi",
        iconRes = R.drawable.ic_bolt,
        tag = "nav_tab_play"
    ),
    NavDestination(
        tab = GameTab.POINT_SHOP,
        label = "Shop",
        iconRes = R.drawable.ic_shop,
        tag = "nav_tab_point_shop"
    ),
    NavDestination(
        tab = GameTab.CASH_SHOP,
        label = "\$Shop\$",
        iconRes = R.drawable.ic_dollar,
        tag = "nav_tab_cash_shop"
    ),
    NavDestination(
        tab = GameTab.ACHIEVEMENTS,
        label = "Nhiệm vụ",
        iconRes = R.drawable.ic_trophy,
        tag = "nav_tab_achievements"
    ),
    NavDestination(
        tab = GameTab.RESTARTS,
        label = "Restarts",
        iconRes = R.drawable.ic_restart,
        tag = "nav_tab_restarts"
    )
)

@Composable
fun TapCoreApp(
    viewModel: GameViewModel,
    billingController: BillingController = NoOpBillingController(),
    audioController: GameAudioController = NoOpAudioController()
) {
    CompositionLocalProvider(LocalSoundManager provides audioController) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val billingState by billingController.billingState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val appScope = rememberCoroutineScope()

        val firebaseAuth = remember(context) { GoogleAuthHelper.ensureFirebaseAuth(context) }
        val currentUser by if (firebaseAuth != null) {
            remember(firebaseAuth) { GoogleAuthHelper.observeAuthUser(firebaseAuth) }
                .collectAsStateWithLifecycle(initialValue = firebaseAuth.currentUser)
        } else {
            remember { mutableStateOf<FirebaseUser?>(null) }
        }
        var playerNickname by remember(currentUser?.uid) {
            mutableStateOf(AccountPrefs.getNickname(context, currentUser))
        }

        LaunchedEffect(firebaseAuth) {
            if (firebaseAuth != null) {
                if (firebaseAuth.currentUser != null) {
                    AccountPrefs.setHasSignedInBefore(context, true)
                } else {
                    GoogleAuthHelper.attemptAutoSignIn(context, firebaseAuth, forceCheck = false)
                }
            }
        }

        DisposableEffect(lifecycleOwner, firebaseAuth) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && firebaseAuth != null && firebaseAuth.currentUser == null) {
                    appScope.launch {
                        GoogleAuthHelper.checkPendingWebSignIn(context, firebaseAuth)
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        if (uiState.selectedTab != GameTab.TAP &&
            !uiState.showAutoUpgradeSheet &&
            !uiState.showRestartDialog &&
            !uiState.showSettings &&
            !uiState.showAccount &&
            !uiState.showPremiumShop &&
            !uiState.showLeaderboard &&
            !uiState.showDailyRewards &&
            !uiState.showSeasonPass &&
            !uiState.showCasino &&
            !uiState.showBossAndRelics &&
            !uiState.showGalaxyFleet &&
            !uiState.showCyberExchange &&
            !uiState.showTechTree &&
            !uiState.showCyberEmpire &&
            !uiState.showQuantumAlchemy &&
            !uiState.showCosmicArena &&
            !uiState.showBadgeOverlay &&
            !uiState.showDroneAndRune &&
            !uiState.showSingularityRaid &&
            !uiState.showFeatureHub
        ) {
            BackHandler {
                viewModel.selectTab(GameTab.TAP)
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 840.dp

        CyberBackground(
            isOverdriveActive = uiState.isOverdriveActive,
            comboCount = uiState.comboCount,
            highGraphicsEnabled = uiState.settings.highGraphicsEnabled
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = CarbonSurface.copy(alpha = 0.96f),
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        Destinations.forEach { dest ->
                            val selected = uiState.selectedTab == dest.tab
                            val showBadge = dest.tab == GameTab.ACHIEVEMENTS && uiState.unclaimedAchievementCount > 0
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(dest.tab) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (showBadge) {
                                                Badge(containerColor = AmberGold, contentColor = ObsidianBgDeep) {
                                                    Text("${uiState.unclaimedAchievementCount}")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(dest.iconRes),
                                            contentDescription = dest.label,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    indicatorColor = CyberCyanDim,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag(dest.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyberCyan)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(
                            containerColor = CarbonSurface.copy(alpha = 0.96f),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Destinations.forEach { dest ->
                                val selected = uiState.selectedTab == dest.tab
                                val showBadge = dest.tab == GameTab.ACHIEVEMENTS && uiState.unclaimedAchievementCount > 0
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(dest.tab) },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (showBadge) {
                                                    Badge(containerColor = AmberGold, contentColor = ObsidianBgDeep) {
                                                        Text("${uiState.unclaimedAchievementCount}")
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(dest.iconRes),
                                                contentDescription = dest.label,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    },
                                    label = { Text(dest.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = CyberCyan,
                                        selectedTextColor = CyberCyan,
                                        indicatorColor = CyberCyanDim,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag(dest.tag)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(max = 620.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            val hubAlertCount = (if (uiState.canClaimDailyToday) 1 else 0) +
                                uiState.unclaimedSeasonRewardsCount +
                                uiState.unclaimedDailyQuestCount +
                                uiState.readyTowerFloorCount +
                                (if (uiState.isExpeditionReady) 1 else 0) +
                                (if (uiState.hasPendingEmpireTreasury) 1 else 0) +
                                (if (uiState.game.canAscendSingularity) 1 else 0)

                            StatsHud(
                                game = uiState.game,
                                isOverdriveActive = uiState.isOverdriveActive,
                                isSignedIn = currentUser != null,
                                accountLabel = if (currentUser != null) playerNickname else "Đăng nhập",
                                featureHubAlertCount = hubAlertCount,
                                onOpenAccount = viewModel::openAccount,
                                onOpenFeatureHub = viewModel::openFeatureHub,
                                onOpenPremiumShop = { viewModel.openPremiumShop(0) },
                                onOpenSettings = viewModel::openSettings
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                val transitionMs = (uiState.game.menuTransitionSec * 280.0)
                                    .roundToInt()
                                    .coerceIn(50, 320)

                                AnimatedContent(
                                    targetState = uiState.selectedTab,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(transitionMs)) togetherWith
                                            fadeOut(animationSpec = tween(transitionMs))
                                    },
                                    label = "tab_content_transition"
                                ) { tab ->
                                    when (tab) {
                                        GameTab.TAP -> TapCoreScreen(
                                            uiState = uiState,
                                            onTapCore = viewModel::onTapCore,
                                            onActivateOverdrive = viewModel::activateOverdrive,
                                            onToggleAuto = viewModel::toggleAutoEnabled,
                                            onOpenCashShop = { viewModel.selectTab(GameTab.CASH_SHOP) },
                                            onCastSkill = viewModel::castSkill,
                                            onCollectSupplyDrop = viewModel::collectSupplyDrop
                                        )
                                        GameTab.POINT_SHOP -> PointShopScreen(
                                            game = uiState.game,
                                            onBuyPointPower = viewModel::buyPointPower,
                                            onBuyMultiplier = viewModel::buyMultiplier,
                                            onBuyCooldown = viewModel::buyCooldownReduction,
                                            onBuyCritChance = viewModel::buyCritChance,
                                            onBuyCritMultiplier = viewModel::buyCritMultiplier,
                                            onBuyComboWindow = viewModel::buyComboWindow
                                        )
                                        GameTab.CASH_SHOP -> CashShopScreen(
                                            game = uiState.game,
                                            showAutoUpgradeSheet = uiState.showAutoUpgradeSheet,
                                            onExchange1000 = viewModel::exchange1000Points,
                                            onExchangeAll = viewModel::exchangeAllPoints,
                                            onBuyDiscount = viewModel::buyDiscount,
                                            onBuyMenuSpeed = viewModel::buyMenuSpeed,
                                            onBuyToastSpeed = viewModel::buyToastSpeed,
                                            onBuyAutoClick = viewModel::buyAutoClick,
                                            onBuyPassiveCore = viewModel::buyPassiveCore,
                                            onBuyOverdriveUpgrade = viewModel::buyOverdriveUpgrade,
                                            onOpenAutoUpgrade = viewModel::openAutoUpgradeSheet,
                                            onDismissAutoUpgrade = viewModel::dismissAutoUpgradeSheet,
                                            onBuyAutoPower = viewModel::buyAutoPower,
                                            onBuyAutoSpeed = viewModel::buyAutoSpeed
                                        )
                                        GameTab.ACHIEVEMENTS -> AchievementsScreen(
                                            game = uiState.game,
                                            records = uiState.achievementRecords,
                                            onClaimAchievement = viewModel::claimAchievement,
                                            onOpenBadgeOverlay = viewModel::openBadgeOverlay,
                                            onOpenLeaderboard = viewModel::openLeaderboard
                                        )
                                        GameTab.RESTARTS -> RestartScreen(
                                            game = uiState.game,
                                            showConfirmDialog = uiState.showRestartDialog,
                                            onRequestRestart = viewModel::requestRestartDialog,
                                            onConfirmRestart = viewModel::confirmRestart,
                                            onDismissDialog = viewModel::dismissRestartDialog,
                                            onBuyPermStartPoints = viewModel::buyPermStartPoints,
                                            onBuyPermGlobalMult = viewModel::buyPermGlobalMult,
                                            onBuyPermCrit = viewModel::buyPermCrit,
                                            onBuyPermDiscount = viewModel::buyPermDiscount
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.offlineEarnedPoints >= 1.0) {
            AlertDialog(
                onDismissRequest = viewModel::dismissOfflineBanner,
                containerColor = CarbonSurface,
                titleContentColor = CyberCyan,
                textContentColor = TextSecondary,
                title = {
                    Text(
                        text = "Thu hoạch ngoại tuyến!",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = "Trong lúc bạn rời khỏi game, Lõi Thụ động & Auto Click đã tạo ra thêm +${Formatters.formatPoints(uiState.offlineEarnedPoints)} điểm!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::dismissOfflineBanner,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = ObsidianBgDeep
                        )
                    ) {
                        Text(stringResource(R.string.confirm_action))
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = uiState.showPremiumShop,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            PremiumShopScreen(
                game = uiState.game,
                selectedTab = uiState.premiumShopTab,
                goldenFeverRemainingSec = uiState.goldenFeverRemainingSec,
                lastMysteryCrateReward = uiState.lastMysteryCrateReward,
                billingState = billingState,
                onSelectTab = viewModel::selectPremiumShopTab,
                onLaunchRealPurchase = { product ->
                    val activity = context.findActivity()
                    if (activity != null) {
                        billingController.launchDiamondPurchase(activity, product.productId)
                    } else {
                        false
                    }
                },
                onRefreshBilling = billingController::refreshPurchasesAndProducts,
                onBuyPointsPack = viewModel::buyPointsWithDiamonds,
                onBuyDollarsPack = viewModel::buyDollarsWithDiamonds,
                onBuyShards = viewModel::buyShardsWithDiamonds,
                onBuyInstantOverdrive = viewModel::buyInstantOverdriveWithDiamonds,
                onBuyGoldenFever = viewModel::buyGoldenFeverWithDiamonds,
                onOpenMysteryCrate = viewModel::openMysteryCrateWithDiamonds,
                onBuyPremTapGod = viewModel::buyPremTapGod,
                onBuyPremMegaCrit = viewModel::buyPremMegaCrit,
                onBuyPremAutoGod = viewModel::buyPremAutoGod,
                onBuyPremZeroCd = viewModel::buyPremZeroCd,
                onBuyPremWealthMagnet = viewModel::buyPremWealthMagnet,
                onClose = viewModel::closePremiumShop
            )
        }

        AnimatedVisibility(
            visible = uiState.showLeaderboard,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            LeaderboardScreen(
                game = uiState.game,
                currentUser = currentUser,
                firebaseAuth = firebaseAuth,
                playerNickname = playerNickname,
                onNicknameUpdated = { playerNickname = it },
                onClose = viewModel::closeLeaderboard,
                onShowToast = viewModel::showBillingMessage
            )
        }

        AnimatedVisibility(
            visible = uiState.showSettings,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            SettingsScreen(
                settings = uiState.settings,
                game = uiState.game,
                isSignedIn = currentUser != null,
                accountName = playerNickname,
                onOpenAccount = viewModel::openAccount,
                onUpdateSettings = viewModel::updateSettings,
                onTestSfx = viewModel::testSfx,
                onHardReset = viewModel::hardResetGame,
                onClose = viewModel::closeSettings
            )
        }

        AnimatedVisibility(
            visible = uiState.showAccount,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            AccountScreen(
                game = uiState.game,
                currentUser = currentUser,
                firebaseAuth = firebaseAuth,
                playerNickname = playerNickname,
                onNicknameUpdated = { playerNickname = it },
                onOpenLeaderboard = viewModel::openLeaderboard,
                onShowToast = viewModel::showBillingMessage,
                onClose = viewModel::closeAccount
            )
        }

        AnimatedVisibility(
            visible = uiState.showDailyRewards,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            DailyRewardsScreen(
                game = uiState.game,
                canClaimToday = uiState.canClaimDailyToday,
                onClaimDaily = viewModel::claimDailyReward,
                onClose = viewModel::closeDailyRewards
            )
        }

        AnimatedVisibility(
            visible = uiState.showSeasonPass,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            SeasonPassScreen(
                game = uiState.game,
                unclaimedCount = uiState.unclaimedSeasonRewardsCount,
                onUnlockElite = viewModel::unlockEliteSeasonPass,
                onClaimTier = viewModel::claimSeasonTier,
                onClaimAll = viewModel::claimAllSeasonRewards,
                onClose = viewModel::closeSeasonPass
            )
        }

        AnimatedVisibility(
            visible = uiState.showCasino,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            CasinoScreen(
                uiState = uiState,
                onSelectGame = viewModel::selectCasinoGame,
                onSelectCurrency = viewModel::selectCasinoCurrency,
                onSelectBetTier = viewModel::selectCasinoBetTier,
                onSpinWheel = viewModel::spinLuckyWheel,
                onPlaySlot777 = viewModel::playSlot777,
                onPlayDice = viewModel::playQuantumDice,
                onStartMinefield = viewModel::startMinefieldGame,
                onRevealMineTile = viewModel::revealMinefieldTile,
                onCashOutMinefield = viewModel::cashOutMinefield,
                onClose = viewModel::closeCasino
            )
        }

        AnimatedVisibility(
            visible = uiState.showBossAndRelics,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            BossAndRelicsScreen(
                uiState = uiState,
                onAttackBoss = viewModel::attackBoss,
                onUpgradeRelic = viewModel::upgradeRelic,
                onActivateBooster = viewModel::activateBooster,
                onClaimDailyQuest = viewModel::claimDailyQuest,
                onClose = viewModel::closeBossAndRelics
            )
        }

        AnimatedVisibility(
            visible = uiState.showGalaxyFleet,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            GalaxyFleetScreen(
                uiState = uiState,
                onUpgradeShip = viewModel::upgradeFleetShip,
                onStartExpedition = viewModel::startExpedition,
                onClaimExpedition = viewModel::claimExpedition,
                onClose = viewModel::closeGalaxyFleet
            )
        }

        AnimatedVisibility(
            visible = uiState.showCyberExchange,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            CyberExchangeScreen(
                uiState = uiState,
                onSelectTicker = viewModel::selectStockTicker,
                onBuyStock = viewModel::buyStock,
                onSellStock = viewModel::sellStock,
                onClose = viewModel::closeCyberExchange
            )
        }

        AnimatedVisibility(
            visible = uiState.showTechTree,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            TechTreeScreen(
                uiState = uiState,
                onUpgradeTechNode = viewModel::upgradeTechNode,
                onSynthesizeChips = viewModel::synthesizeTechChips,
                onClose = viewModel::closeTechTree
            )
        }

        AnimatedVisibility(
            visible = uiState.showCyberEmpire,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            CyberEmpireScreen(
                uiState = uiState,
                onUpgradeDistrict = viewModel::upgradeEmpireDistrict,
                onCollectTreasury = viewModel::collectEmpireTreasury,
                onClose = viewModel::closeCyberEmpire
            )
        }

        AnimatedVisibility(
            visible = uiState.showQuantumAlchemy,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            QuantumAlchemyScreen(
                uiState = uiState,
                onTransmute = viewModel::transmuteRecipe,
                onUpgradeGear = viewModel::upgradeQuantumGear,
                onClose = viewModel::closeQuantumAlchemy
            )
        }

        AnimatedVisibility(
            visible = uiState.showCosmicArena,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            CosmicArenaScreen(
                uiState = uiState,
                onAttackArena = viewModel::attackArena,
                onCastArenaOmega = viewModel::castArenaOmega,
                onActivateArenaShield = viewModel::activateArenaShield,
                onClaimTowerFloor = viewModel::claimTowerFloor,
                onClose = viewModel::closeCosmicArena
            )
        }

        AnimatedVisibility(
            visible = uiState.showBadgeOverlay,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            AchievementBadgeOverlay(
                game = uiState.game,
                records = uiState.achievementRecords,
                onClaimAchievement = viewModel::claimAchievement,
                onClose = viewModel::closeBadgeOverlay
            )
        }

        AnimatedVisibility(
            visible = uiState.showDroneAndRune,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            DroneAndRuneScreen(
                uiState = uiState,
                onUpgradeDrone = viewModel::upgradeDrone,
                onActivateDroneOverclock = viewModel::activateDroneOverclock,
                onUpgradeRune = viewModel::upgradeRune,
                onToggleEquipRune = viewModel::toggleEquipRune,
                onClose = viewModel::closeDroneAndRune
            )
        }

        AnimatedVisibility(
            visible = uiState.showFeatureHub,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            FeatureHubScreen(
                uiState = uiState,
                onOpenDailyRewards = viewModel::openDailyRewards,
                onOpenSeasonPass = viewModel::openSeasonPass,
                onOpenBadgeOverlay = viewModel::openBadgeOverlay,
                onOpenBossAndRelics = viewModel::openBossAndRelics,
                onOpenCosmicArena = viewModel::openCosmicArena,
                onOpenSingularityRaid = viewModel::openSingularityRaid,
                onOpenCyberEmpire = viewModel::openCyberEmpire,
                onOpenGalaxyFleet = viewModel::openGalaxyFleet,
                onOpenTechTree = viewModel::openTechTree,
                onOpenQuantumAlchemy = viewModel::openQuantumAlchemy,
                onOpenDroneAndRune = viewModel::openDroneAndRune,
                onOpenCyberExchange = viewModel::openCyberExchange,
                onOpenCasino = viewModel::openCasino,
                onClose = viewModel::closeFeatureHub
            )
        }

        AnimatedVisibility(
            visible = uiState.showSingularityRaid,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            SingularityRaidScreen(
                uiState = uiState,
                onRechargeRaidEnergy = viewModel::rechargeRaidEnergy,
                onEnterDungeon = viewModel::enterDungeon,
                onStrikeDungeonEnemy = viewModel::strikeDungeonEnemy,
                onPerformSingularityAscension = viewModel::performSingularityAscension,
                onUpgradeSingularityPerk = viewModel::upgradeSingularityPerk,
                onClose = viewModel::closeSingularityRaid
            )
        }

        FeedbackBanner(
            banner = uiState.toastBanner,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
