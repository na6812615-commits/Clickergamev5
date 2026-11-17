package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.VerifiedUser
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.auth.AccountPrefs
import com.example.auth.GoogleAuthHelper
import com.example.auth.GoogleSignInOutcome
import com.example.data.GameStateEntity
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    game: GameStateEntity,
    currentUser: FirebaseUser?,
    firebaseAuth: FirebaseAuth?,
    playerNickname: String,
    onNicknameUpdated: (String) -> Unit,
    onOpenLeaderboard: () -> Unit,
    onShowToast: (String, Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isBusy by remember { mutableStateOf(false) }
    var authMessage by remember { mutableStateOf<String?>(null) }
    var showEditNicknameDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (showEditNicknameDialog) {
            showEditNicknameDialog = false
        } else {
            onClose()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ObsidianBg, ObsidianBgDeep)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("account_screen"),
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
                        contentColor = CyberCyan
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("close_account_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Quay lại"
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TÀI KHOẢN & ĐĂNG NHẬP",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (currentUser != null) {
                            "Đã kết nối tài khoản Google • Sẵn sàng BXH Toàn Cầu"
                        } else {
                            "Đăng nhập để đồng bộ hồ sơ & thi đấu Bảng Xếp Hạng"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentUser == null || firebaseAuth == null) {
                    item {
                        UnauthenticatedAccountCard(
                            isBusy = isBusy,
                            firebaseReady = firebaseAuth != null,
                            errorMessage = authMessage,
                            onGoogleCredentialSignIn = {
                                if (firebaseAuth == null || isBusy) return@UnauthenticatedAccountCard
                                scope.launch {
                                    isBusy = true
                                    authMessage = null
                                    when (val res = GoogleAuthHelper.onGoogleSignInClicked(context, firebaseAuth)) {
                                        is GoogleSignInOutcome.Success -> {
                                            authMessage = null
                                            onShowToast("Đăng nhập Google thành công!", false)
                                        }
                                        GoogleSignInOutcome.Cancelled -> {
                                            authMessage = "Bạn đã đóng hộp thoại Google Sign-In. Nếu bạn vừa thêm tài khoản Google trên máy, hãy thử bấm lại hoặc dùng \"Đăng nhập Google qua Trình duyệt\" bên dưới."
                                        }
                                        GoogleSignInOutcome.NoCredentialAvailable -> {
                                            authMessage = "Chưa tìm thấy thông tin Google trong Credential Manager. Hãy thử nút \"Đăng nhập Google qua Trình duyệt\" bên dưới!"
                                        }
                                        is GoogleSignInOutcome.Failure -> {
                                            authMessage = res.message
                                        }
                                    }
                                    isBusy = false
                                }
                            },
                            onGoogleWebOAuthSignIn = {
                                if (firebaseAuth == null || isBusy) return@UnauthenticatedAccountCard
                                scope.launch {
                                    isBusy = true
                                    authMessage = null
                                    when (val res = GoogleAuthHelper.signInWithGoogleWebOAuth(context, firebaseAuth)) {
                                        is GoogleSignInOutcome.Success -> {
                                            authMessage = null
                                            onShowToast("Đăng nhập Google thành công!", false)
                                        }
                                        GoogleSignInOutcome.Cancelled -> {
                                            authMessage = "Bạn đã hủy đăng nhập qua trình duyệt."
                                        }
                                        GoogleSignInOutcome.NoCredentialAvailable -> {
                                            authMessage = "Không tìm thấy phiên đăng nhập Google."
                                        }
                                        is GoogleSignInOutcome.Failure -> {
                                            authMessage = res.message
                                        }
                                    }
                                    isBusy = false
                                }
                            },
                            onRetryAutoCheck = {
                                if (firebaseAuth == null || isBusy) return@UnauthenticatedAccountCard
                                scope.launch {
                                    isBusy = true
                                    authMessage = null
                                    AccountPrefs.setExplicitlySignedOut(context, false)
                                    when (val res = GoogleAuthHelper.attemptAutoSignIn(context, firebaseAuth, forceCheck = true)) {
                                        is GoogleSignInOutcome.Success -> {
                                            onShowToast("Đã tự động khôi phục đăng nhập Google!", false)
                                        }
                                        else -> {
                                            authMessage = "Chưa phát hiện phiên Google đã ủy quyền. Vui lòng chọn một trong hai nút đăng nhập phía trên."
                                        }
                                    }
                                    isBusy = false
                                }
                            }
                        )
                    }
                } else {
                    item {
                        SignedInProfileCard(
                            user = currentUser,
                            nickname = playerNickname,
                            game = game,
                            isBusy = isBusy,
                            onEditNickname = { showEditNicknameDialog = true },
                            onOpenLeaderboard = {
                                onClose()
                                onOpenLeaderboard()
                            },
                            onSignOut = {
                                if (isBusy) return@SignedInProfileCard
                                scope.launch {
                                    isBusy = true
                                    GoogleAuthHelper.signOut(context, firebaseAuth)
                                    isBusy = false
                                    onShowToast("Đã đăng xuất khỏi tài khoản Google.", false)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEditNicknameDialog && currentUser != null) {
        var draft by remember(playerNickname) { mutableStateOf(playerNickname) }
        AlertDialog(
            onDismissRequest = { showEditNicknameDialog = false },
            containerColor = CarbonSurface,
            titleContentColor = CyberCyan,
            textContentColor = TextSecondary,
            title = { Text("Đổi tên người chơi") },
            text = {
                Column {
                    Text(
                        text = "Biệt danh này hiển thị trên hồ sơ và Bảng Xếp Hạng Toàn Cầu (tối đa 40 ký tự).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { if (it.length <= 40) draft = it },
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
                            .testTag("account_nickname_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val saved = AccountPrefs.saveNickname(context, currentUser.uid, draft)
                        onNicknameUpdated(saved)
                        showEditNicknameDialog = false
                        onShowToast("Đã cập nhật tên: $saved", false)
                    },
                    enabled = draft.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = ObsidianBgDeep
                    ),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Lưu tên")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNicknameDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun UnauthenticatedAccountCard(
    isBusy: Boolean,
    firebaseReady: Boolean,
    errorMessage: String?,
    onGoogleCredentialSignIn: () -> Unit,
    onGoogleWebOAuthSignIn: () -> Unit,
    onRetryAutoCheck: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("account_signin_card"),
        shape = RoundedCornerShape(20.dp),
        color = CarbonSurface.copy(alpha = 0.95f),
        border = BorderStroke(1.2.dp, ElectricViolet.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(68.dp),
                shape = CircleShape,
                color = AmberGoldDim,
                border = BorderStroke(1.5.dp, AmberGold.copy(alpha = 0.7f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Bạn chưa đăng nhập tài khoản",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Đăng nhập Google để xác thực danh tính người chơi, lưu biệt danh cá nhân và đua Top trên Bảng Xếp Hạng Toàn Cầu.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CoralCrimsonDim,
                    border = BorderStroke(1.dp, CoralCrimson.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onGoogleCredentialSignIn,
                enabled = firebaseReady && !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberGold,
                    contentColor = ObsidianBgDeep
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("account_google_signin_button")
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = ObsidianBgDeep
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang xác thực...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Login,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng nhập bằng Google", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onGoogleWebOAuthSignIn,
                enabled = firebaseReady && !isBusy,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("account_google_web_signin_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng nhập Google qua Trình duyệt (Dự phòng)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onRetryAutoCheck,
                enabled = firebaseReady && !isBusy,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("account_retry_auto_signin_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kiểm tra lại tài khoản trên máy", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun SignedInProfileCard(
    user: FirebaseUser,
    nickname: String,
    game: GameStateEntity,
    isBusy: Boolean,
    onEditNickname: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onSignOut: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("account_profile_card"),
        shape = RoundedCornerShape(20.dp),
        color = CarbonSurface.copy(alpha = 0.95f),
        border = BorderStroke(1.2.dp, NeonEmerald.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = NeonEmeraldDim,
                        border = BorderStroke(1.5.dp, NeonEmerald)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = nickname.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                color = NeonEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = nickname,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.VerifiedUser,
                                contentDescription = "Đã xác thực",
                                tint = NeonEmerald,
                                modifier = Modifier.size(16.dp)
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
                            text = user.email?.takeIf { it.isNotBlank() } ?: "Google Account",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "UID: ${user.uid.take(10)}...",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = TextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onEditNickname,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("account_edit_nickname_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Đổi tên hiển thị",
                        tint = AmberGold
                    )
                }
            }

            // Player Career Summary
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ElevatedCard,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Lượt Click", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = Formatters.formatLong(game.clicks),
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Tổng Điểm", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = Formatters.formatPoints(game.lifetimePoints),
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = AmberGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Restarts", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "R-${game.restartLv}",
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = NeonEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Button(
                onClick = onOpenLeaderboard,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = ObsidianBgDeep
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("account_open_leaderboard_button")
            ) {
                Icon(Icons.Rounded.Leaderboard, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Xem & Đồng bộ Bảng Xếp Hạng", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onSignOut,
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CoralCrimson.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("account_signout_button")
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất tài khoản Google", fontWeight = FontWeight.Bold)
            }
        }
    }
}
