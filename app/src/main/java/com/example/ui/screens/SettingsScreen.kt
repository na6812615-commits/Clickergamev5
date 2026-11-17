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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.audio.LocalSoundManager
import com.example.data.GameSettings
import com.example.data.GameStateEntity
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
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
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    settings: GameSettings,
    game: GameStateEntity,
    isSignedIn: Boolean = false,
    accountName: String = "Chưa đăng nhập",
    onOpenAccount: () -> Unit = {},
    onUpdateSettings: (GameSettings) -> Unit,
    onTestSfx: () -> Unit,
    onHardReset: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val soundManager = LocalSoundManager.current
    var showResetConfirm by remember { mutableStateOf(false) }

    BackHandler {
        if (showResetConfirm) {
            showResetConfirm = false
        } else {
            onClose()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.96f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("settings_screen"),
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
                        .testTag("close_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Quay lại"
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "CÀI ĐẶT HỆ THỐNG",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberCyan
                    )
                    Text(
                        text = "Tùy chỉnh âm thanh, đồ họa và dữ liệu TapCore VN",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 0: Account & Sync
                item {
                    SectionLabel(title = "TÀI KHOẢN & ĐỒNG BỘ", color = AmberGold)
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = CarbonSurface.copy(alpha = 0.92f),
                        border = BorderStroke(
                            1.dp,
                            if (isSignedIn) NeonEmerald.copy(alpha = 0.55f) else AmberGold.copy(alpha = 0.55f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = null,
                                    tint = if (isSignedIn) NeonEmerald else AmberGold,
                                    modifier = Modifier.size(34.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isSignedIn) accountName else "Chưa đăng nhập Google",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isSignedIn) "Đã kết nối hồ sơ & BXH Toàn Cầu" else "Đăng nhập để lưu tên & đua top toàn cầu",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = {
                                    soundManager.playButtonClick()
                                    onClose()
                                    onOpenAccount()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSignedIn) CyberCyan else AmberGold,
                                    contentColor = ObsidianBgDeep
                                ),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("settings_account_button")
                            ) {
                                Text(if (isSignedIn) "Quản lý" else "Đăng nhập", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Section 1: Audio & Music
                item {
                    SectionLabel(title = "ÂM THANH & NHẠC NỀN", color = CyberCyan)
                }

                item {
                    AudioVolumeCard(
                        iconRes = R.drawable.ic_music,
                        title = "Nhạc nền Synthwave",
                        description = "Âm thanh điện tử thư giãn chạy nền khi chơi",
                        enabled = settings.bgmEnabled,
                        volume = settings.bgmVolume,
                        accent = CyberCyan,
                        switchTag = "switch_bgm",
                        sliderTag = "slider_bgm_volume",
                        onToggle = { enabled ->
                            soundManager.playButtonClick()
                            onUpdateSettings(settings.copy(bgmEnabled = enabled))
                        },
                        onVolumeChange = { vol ->
                            onUpdateSettings(settings.copy(bgmVolume = vol))
                        }
                    )
                }

                item {
                    AudioVolumeCard(
                        iconRes = R.drawable.ic_volume,
                        title = "Hiệu ứng Âm thanh (SFX)",
                        description = "Tiếng chạm tăng dần theo Combo, Crit, nâng cấp & lỗi",
                        enabled = settings.sfxEnabled,
                        volume = settings.sfxVolume,
                        accent = NeonEmerald,
                        switchTag = "switch_sfx",
                        sliderTag = "slider_sfx_volume",
                        onToggle = { enabled ->
                            soundManager.playButtonClick()
                            onUpdateSettings(settings.copy(sfxEnabled = enabled))
                        },
                        onVolumeChange = { vol ->
                            onUpdateSettings(settings.copy(sfxVolume = vol))
                        },
                        extraContent = {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onTestSfx,
                                enabled = settings.sfxEnabled,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (settings.sfxEnabled) NeonEmerald.copy(alpha = 0.6f) else CardBorder
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = NeonEmerald,
                                    disabledContentColor = TextMuted
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("test_sfx_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Nghe thử hiệu ứng SFX",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    )
                }

                // Section 2: Graphics & Feel
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    SectionLabel(title = "ĐỒ HỌA & CẢM GIÁC BẤM", color = ElectricViolet)
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = CarbonSurface.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ToggleSettingRow(
                                iconRes = R.drawable.ic_vibration,
                                title = "Phản hồi Rung (Haptics)",
                                description = "Rung nhẹ phản hồi mỗi khi chạm lõi năng lượng",
                                checked = settings.hapticsEnabled,
                                accent = ElectricViolet,
                                switchTag = "switch_haptics",
                                onCheckedChange = { checked ->
                                    soundManager.playButtonClick()
                                    onUpdateSettings(settings.copy(hapticsEnabled = checked))
                                }
                            )

                            ToggleSettingRow(
                                iconRes = R.drawable.ic_sparkles,
                                title = "Đồ họa nền động 60fps",
                                description = "Lưới tọa độ & hạt neon chuyển động (tắt để tiết kiệm pin)",
                                checked = settings.highGraphicsEnabled,
                                accent = CyberCyan,
                                switchTag = "switch_high_graphics",
                                onCheckedChange = { checked ->
                                    soundManager.playButtonClick()
                                    onUpdateSettings(settings.copy(highGraphicsEnabled = checked))
                                }
                            )

                            ToggleSettingRow(
                                iconRes = R.drawable.ic_bolt,
                                title = "Hiện chữ bay +Điểm",
                                description = "Hiển thị số điểm & CRIT nổi lên khi chạm nhanh",
                                checked = settings.floatingTextEnabled,
                                accent = AmberGold,
                                switchTag = "switch_floating_text",
                                onCheckedChange = { checked ->
                                    soundManager.playButtonClick()
                                    onUpdateSettings(settings.copy(floatingTextEnabled = checked))
                                }
                            )
                        }
                    }
                }

                // Section 3: Profile & Data
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    SectionLabel(title = "THỐNG KÊ & DỮ LIỆU", color = AmberGold)
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = CarbonSurface.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatSummaryRow("Lõi hiện tại", "${game.reactorTier.code} • ${game.reactorTier.displayName}", CyberCyan)
                            StatSummaryRow("Tổng điểm trọn đời", Formatters.formatPoints(game.lifetimePoints), TextPrimary)
                            StatSummaryRow("Tổng lượt chạm", Formatters.formatLong(game.clicks), TextPrimary)
                            StatSummaryRow("Kỷ lục Combo", "${game.bestCombo}x", NeonEmerald)
                            StatSummaryRow("Tổng đòn Chí mạng", Formatters.formatLong(game.totalCrits), AmberGold)
                            StatSummaryRow("Cấp Restarts & Mảnh Lõi", "R-${game.restartLv} • ${game.shards}✦", ElectricViolet)
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = CoralCrimsonDim.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, CoralCrimson.copy(alpha = 0.55f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "VÙNG NGUY HIỂM",
                                style = MaterialTheme.typography.labelMedium,
                                color = CoralCrimson,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Xóa toàn bộ tiến độ, nâng cấp, thành tựu, Mảnh Lõi và bắt đầu lại từ đầu. Cài đặt âm thanh vẫn được giữ lại.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    soundManager.playButtonClick()
                                    showResetConfirm = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoralCrimson,
                                    contentColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("hard_reset_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteForever,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đặt lại toàn bộ dữ liệu (Hard Reset)",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = CarbonSurface,
            titleContentColor = CoralCrimson,
            textContentColor = TextSecondary,
            title = {
                Text(
                    text = "Xóa toàn bộ dữ liệu game?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Hành động này không thể hoàn tác! Tất cả Điểm, Tiền $, Auto Click, Thành tựu, Mảnh Lõi ✦ và Cấp Restarts sẽ về 0.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onHardReset()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralCrimson,
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier.testTag("confirm_hard_reset_button")
                ) {
                    Text("Xóa vĩnh viễn")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirm = false },
                    modifier = Modifier.testTag("cancel_hard_reset_button")
                ) {
                    Text(
                        text = "Hủy",
                        color = TextSecondary
                    )
                }
            }
        )
    }
}

@Composable
private fun SectionLabel(
    title: String,
    color: Color
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun AudioVolumeCard(
    iconRes: Int,
    title: String,
    description: String,
    enabled: Boolean,
    volume: Float,
    accent: Color,
    switchTag: String,
    sliderTag: String,
    onToggle: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    extraContent: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CarbonSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, if (enabled) accent.copy(alpha = 0.45f) else CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = if (enabled) accent.copy(alpha = 0.16f) else ElevatedCard,
                        border = BorderStroke(1.dp, if (enabled) accent.copy(alpha = 0.5f) else CardBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                tint = if (enabled) accent else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBgDeep,
                        checkedTrackColor = accent,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = ElevatedCard
                    ),
                    modifier = Modifier.testTag(switchTag)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Âm lượng",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = "${(volume * 100f).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (enabled) accent else TextMuted
                )
            }

            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                enabled = enabled,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    inactiveTrackColor = ElevatedCard,
                    disabledThumbColor = TextMuted,
                    disabledActiveTrackColor = TextMuted.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(sliderTag)
            )

            extraContent?.invoke()
        }
    }
}

@Composable
private fun ToggleSettingRow(
    iconRes: Int,
    title: String,
    description: String,
    checked: Boolean,
    accent: Color,
    switchTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = if (checked) accent.copy(alpha = 0.16f) else ElevatedCard,
                border = BorderStroke(1.dp, if (checked) accent.copy(alpha = 0.45f) else CardBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = if (checked) accent else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBgDeep,
                checkedTrackColor = accent,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = ElevatedCard
            ),
            modifier = Modifier.testTag(switchTag)
        )
    }
}

@Composable
private fun StatSummaryRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = valueColor
        )
    }
}
