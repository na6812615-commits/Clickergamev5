package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import androidx.compose.material.icons.rounded.OpenInBrowser
import com.example.auth.AccountPrefs
import com.example.auth.GoogleAuthHelper
import com.example.auth.GoogleSignInOutcome
import com.example.data.GameStateEntity
import com.example.data.LeaderboardEntry
import com.example.data.LeaderboardRepository
import com.example.data.LeaderboardSortBy
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDim
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CoralCrimsonDim
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldDim
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.LeaderboardViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun LeaderboardScreen(
    game: GameStateEntity,
    currentUser: FirebaseUser?,
    firebaseAuth: FirebaseAuth?,
    playerNickname: String,
    onNicknameUpdated: (String) -> Unit,
    onClose: () -> Unit,
    onShowToast: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    BackHandler {
        onClose()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isSigningIn by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(firebaseAuth, currentUser) {
        if (firebaseAuth != null && currentUser == null) {
            GoogleAuthHelper.checkPendingWebSignIn(context, firebaseAuth)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianBg,
                        ObsidianBgDeep
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("leaderboard_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = ElevatedCard,
                        contentColor = AmberGold
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("close_leaderboard_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Quay lại"
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BẢNG XẾP HẠNG TOÀN CẦU",
                        style = MaterialTheme.typography.titleLarge,
                        color = AmberGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Top người chơi TapCore VN theo thời gian thực",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val signedInUser = currentUser
            if (signedInUser == null || firebaseAuth == null) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        LocalPlayerScoreCard(game = game)
                    }
                    item {
                        GoogleSignInGateCard(
                            isSigningIn = isSigningIn,
                            errorMessage = authErrorMessage,
                            firebaseReady = firebaseAuth != null,
                            onSignInClick = {
                                if (firebaseAuth == null || isSigningIn) return@GoogleSignInGateCard
                                coroutineScope.launch {
                                    isSigningIn = true
                                    authErrorMessage = null
                                    when (val outcome = GoogleAuthHelper.onGoogleSignInClicked(context, firebaseAuth)) {
                                        is GoogleSignInOutcome.Success -> {
                                            authErrorMessage = null
                                            onShowToast("Đăng nhập Google thành công!", false)
                                        }
                                        GoogleSignInOutcome.Cancelled -> {
                                            authErrorMessage = "Hộp thoại Google Sign-In đã đóng. Nếu thiết bị chưa hỗ trợ Credential Manager, hãy thử nút \"Đăng nhập qua Trình duyệt\" bên dưới."
                                        }
                                        GoogleSignInOutcome.NoCredentialAvailable -> {
                                            authErrorMessage = "Không tìm thấy tài khoản Google sẵn sàng trên thiết bị. Hãy dùng nút \"Đăng nhập qua Trình duyệt\" bên dưới."
                                        }
                                        is GoogleSignInOutcome.Failure -> {
                                            authErrorMessage = outcome.message
                                        }
                                    }
                                    isSigningIn = false
                                }
                            },
                            onWebOAuthClick = {
                                if (firebaseAuth == null || isSigningIn) return@GoogleSignInGateCard
                                coroutineScope.launch {
                                    isSigningIn = true
                                    authErrorMessage = null
                                    when (val outcome = GoogleAuthHelper.signInWithGoogleWebOAuth(context, firebaseAuth)) {
                                        is GoogleSignInOutcome.Success -> {
                                            authErrorMessage = null
                                            onShowToast("Đăng nhập Google thành công!", false)
                                        }
                                        GoogleSignInOutcome.Cancelled -> {
                                            authErrorMessage = "Bạn đã hủy đăng nhập qua trình duyệt."
                                        }
                                        GoogleSignInOutcome.NoCredentialAvailable -> {
                                            authErrorMessage = "Không tìm thấy phiên đăng nhập Google."
                                        }
                                        is GoogleSignInOutcome.Failure -> {
                                            authErrorMessage = outcome.message
                                        }
                                    }
                                    isSigningIn = false
                                }
                            }
                        )
                    }
                }
            } else {
                AuthenticatedGlobalLeaderboard(
                    user = signedInUser,
                    firebaseAuth = firebaseAuth,
                    game = game,
                    playerNickname = playerNickname,
                    onNicknameUpdated = onNicknameUpdated,
                    onShowToast = onShowToast,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedGlobalLeaderboard(
    user: FirebaseUser,
    firebaseAuth: FirebaseAuth,
    game: GameStateEntity,
    playerNickname: String,
    onNicknameUpdated: (String) -> Unit,
    onShowToast: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showNameDialog by remember { mutableStateOf(false) }

    val repository = remember(user.uid) {
        val dbId = context.getString(R.string.firestore_database_id)
        LeaderboardRepository(
            firestore = FirebaseFirestore.getInstance(dbId),
            auth = firebaseAuth
        )
    }
    val leaderboardVm: LeaderboardViewModel = viewModel(
        key = user.uid,
        factory = LeaderboardViewModel.factory(
            repository = repository,
            currentUserId = user.uid,
            defaultDisplayName = playerNickname
        )
    )
    val uiState by leaderboardVm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(user.uid) {
        leaderboardVm.syncScore(game, playerNickname)
    }

    LaunchedEffect(uiState.syncStatusMessage) {
        uiState.syncStatusMessage?.let { msg ->
            onShowToast(msg, false)
            leaderboardVm.clearMessages()
        }
    }

    val myRank = remember(uiState.entries, user.uid) {
        val idx = uiState.entries.indexOfFirst { it.userId == user.uid }
        if (idx >= 0) idx + 1 else null
    }

    Column(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("global_leaderboard_list"),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                LocalPlayerScoreCard(
                    game = game,
                    rank = myRank,
                    playerName = playerNickname
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CarbonSurface.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tài khoản thi đấu",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = playerNickname,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { showNameDialog = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("edit_leaderboard_name_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Đổi tên hiển thị BXH",
                                    tint = AmberGold
                                )
                            }

                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        GoogleAuthHelper.signOut(context, firebaseAuth)
                                        onShowToast("Đã đăng xuất khỏi Bảng Xếp Hạng", false)
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("sign_out_leaderboard_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                                    contentDescription = "Đăng xuất Google",
                                    tint = CoralCrimson
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { leaderboardVm.syncScore(game, playerNickname) },
                            enabled = !uiState.isSyncing,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = ObsidianBgDeep
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("sync_leaderboard_button")
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = ObsidianBgDeep
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đang đồng bộ...", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.CloudSync,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đồng bộ điểm hiện tại lên BXH", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SortModeButton(
                        label = "Top Lượt Click",
                        selected = uiState.sortBy == LeaderboardSortBy.CLICKS,
                        onClick = { leaderboardVm.setSortBy(LeaderboardSortBy.CLICKS) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sort_clicks_button")
                    )
                    SortModeButton(
                        label = "Top Tổng Điểm",
                        selected = uiState.sortBy == LeaderboardSortBy.LIFETIME_POINTS,
                        onClick = { leaderboardVm.setSortBy(LeaderboardSortBy.LIFETIME_POINTS) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sort_points_button")
                    )
                }
            }

            if (uiState.errorMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = CoralCrimsonDim,
                        border = BorderStroke(1.dp, CoralCrimson.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = CoralCrimson,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.errorMessage ?: "Đã xảy ra lỗi kết nối Firestore",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { leaderboardVm.syncScore(game, playerNickname) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Thử lại",
                                    tint = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading && uiState.entries.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CarbonSurface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = CyberCyan)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Đang tải Bảng Xếp Hạng Toàn Cầu...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else if (uiState.entries.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CarbonSurface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_trophy),
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Chưa có dữ liệu trên Bảng Xếp Hạng",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Nhấn nút Đồng bộ điểm để ghi danh kỷ lục đầu tiên!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = uiState.entries,
                    key = { _, item -> item.userId }
                ) { index, entry ->
                    GlobalRankRowCard(
                        rank = index + 1,
                        entry = entry,
                        isCurrentUser = entry.userId == user.uid,
                        sortBy = uiState.sortBy
                    )
                }
            }
        }
    }

    if (showNameDialog) {
        EditDisplayNameDialog(
            initialName = playerNickname,
            onDismiss = { showNameDialog = false },
            onConfirm = { newName ->
                val sanitized = AccountPrefs.saveNickname(context, user.uid, newName)
                onNicknameUpdated(sanitized)
                showNameDialog = false
                leaderboardVm.syncScore(game, sanitized)
            }
        )
    }
}

@Composable
private fun LocalPlayerScoreCard(
    game: GameStateEntity,
    rank: Int? = null,
    playerName: String? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("local_player_rank_card"),
        shape = RoundedCornerShape(18.dp),
        color = CarbonSurface.copy(alpha = 0.94f),
        border = BorderStroke(1.5.dp, CyberCyan.copy(alpha = 0.65f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = AmberGoldDim,
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.7f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Leaderboard,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = playerName ?: "Hồ sơ của bạn",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (game.hasVipPass) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AmberGoldDim
                                ) {
                                    Text(
                                        text = "VIP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RoyalGold,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${game.reactorTier.code} • ${game.reactorTier.displayName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan
                        )
                    }
                }

                if (rank != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AmberGoldDim,
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = "Hạng #$rank",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = AmberGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ElevatedCard, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LocalScoreStat(
                    label = "Tổng lượt Click",
                    value = Formatters.formatLong(game.clicks),
                    color = CyberCyan,
                    modifier = Modifier.weight(1f)
                )
                LocalScoreStat(
                    label = "Tổng điểm đời",
                    value = Formatters.formatPoints(game.lifetimePoints),
                    color = AmberGold,
                    modifier = Modifier.weight(1f)
                )
                LocalScoreStat(
                    label = "Combo • Restarts",
                    value = "${game.bestCombo}x • R${game.restartLv}",
                    color = NeonEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GoogleSignInGateCard(
    isSigningIn: Boolean,
    errorMessage: String?,
    firebaseReady: Boolean,
    onSignInClick: () -> Unit,
    onWebOAuthClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_signin_card"),
        shape = RoundedCornerShape(18.dp),
        color = CarbonSurface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = CyberCyanDim,
                border = BorderStroke(1.5.dp, CyberCyan.copy(alpha = 0.6f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CloudSync,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Đấu Trường Bảng Xếp Hạng Toàn Cầu",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Đăng nhập bằng tài khoản Google để đồng bộ lượt Click và điểm tích lũy của bạn lên Firebase Firestore và tranh hạng cùng người chơi toàn cầu.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CoralCrimsonDim,
                    border = BorderStroke(1.dp, CoralCrimson.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralCrimson,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSignInClick,
                enabled = firebaseReady && !isSigningIn,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberGold,
                    contentColor = ObsidianBgDeep
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .testTag("google_signin_button")
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = ObsidianBgDeep
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang kết nối Google...", style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Login,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng nhập Google để xem BXH", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onWebOAuthClick,
                enabled = firebaseReady && !isSigningIn,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("google_web_signin_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng nhập Google qua Trình duyệt (Dự phòng)", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SortModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberCyanDim,
                contentColor = CyberCyan
            ),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.8f)),
            modifier = modifier.heightIn(min = 48.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CardBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            modifier = modifier.heightIn(min = 48.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

@Composable
private fun GlobalRankRowCard(
    rank: Int,
    entry: LeaderboardEntry,
    isCurrentUser: Boolean,
    sortBy: LeaderboardSortBy
) {
    val rankColor = when (rank) {
        1 -> RoyalGold
        2 -> Color(0xFFE2E8F0)
        3 -> Color(0xFFFB923C)
        else -> TextSecondary
    }
    val borderColor = when {
        isCurrentUser -> CyberCyan.copy(alpha = 0.85f)
        rank == 1 -> RoyalGold.copy(alpha = 0.65f)
        rank == 2 -> Color(0xFFE2E8F0).copy(alpha = 0.45f)
        rank == 3 -> Color(0xFFFB923C).copy(alpha = 0.45f)
        else -> CardBorder
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_row_$rank"),
        shape = RoundedCornerShape(14.dp),
        color = if (isCurrentUser) ElevatedCard else CarbonSurface.copy(alpha = 0.92f),
        border = BorderStroke(if (isCurrentUser || rank <= 3) 1.25.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = rankColor.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, rankColor.copy(alpha = 0.55f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#$rank",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = rankColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isCurrentUser) CyberCyan else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = NeonEmeraldDim
                        ) {
                            Text(
                                text = "BẠN",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonEmerald,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (entry.hasVipPass) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = AmberGoldDim
                        ) {
                            Text(
                                text = "VIP",
                                style = MaterialTheme.typography.labelSmall,
                                color = RoyalGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${entry.reactorCode} • Combo ${entry.bestCombo}x • R${entry.restartLv}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                val primaryText = if (sortBy == LeaderboardSortBy.CLICKS) {
                    "${Formatters.formatLong(entry.clicks)} clicks"
                } else {
                    "${Formatters.formatPoints(entry.lifetimePoints.toDouble())} điểm"
                }
                val secondaryText = if (sortBy == LeaderboardSortBy.CLICKS) {
                    "${Formatters.formatPoints(entry.lifetimePoints.toDouble())} đ"
                } else {
                    "${Formatters.formatLong(entry.clicks)} clicks"
                }
                Text(
                    text = primaryText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (sortBy == LeaderboardSortBy.CLICKS) CyberCyan else AmberGold
                )
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun EditDisplayNameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var draftName by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonSurface,
        titleContentColor = AmberGold,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = "Đổi tên hiển thị BXH",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    text = "Nhập biệt danh hiển thị công khai trên Bảng Xếp Hạng (tối đa 40 ký tự).",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { if (it.length <= 40) draftName = it },
                    singleLine = true,
                    label = { Text("Biệt danh") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("leaderboard_nickname_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(draftName) },
                enabled = draftName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = ObsidianBgDeep
                ),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Lưu & Đồng bộ")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Hủy", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun LocalScoreStat(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
