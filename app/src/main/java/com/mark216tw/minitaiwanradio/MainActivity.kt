package com.mark216tw.minitaiwanradio

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class PlaybackUiState(
    val stationId: String? = null,
    val stationName: String? = null,
    val playing: Boolean = false,
    val buffering: Boolean = false,
    val connecting: Boolean = false,
    val error: String? = null,
)

private val ThemeColors = listOf(
    Color(0xFFFF765F),
    Color(0xFF4DA9E8),
    Color(0xFF43C6A5),
    Color(0xFF9B7EDE),
    Color(0xFFF2C94C),
    Color(0xFFFFA34D),
)

class MainActivity : ComponentActivity() {
    private var playbackState by mutableStateOf(PlaybackUiState())
    private var mediaController: MediaController? = null

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            playbackState = playbackState.copy(
                stationId = mediaItem?.mediaId,
                stationName = mediaItem?.mediaMetadata?.title?.toString(),
                error = null,
            )
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            playbackState = if (isPlaying) {
                playbackState.copy(playing = true, buffering = false, connecting = false)
            } else {
                playbackState.copy(playing = false)
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_BUFFERING) {
                playbackState = playbackState.copy(buffering = true, connecting = true)
            } else if (state == Player.STATE_READY) {
                playbackState = playbackState.copy(buffering = false)
            } else if (state == Player.STATE_IDLE && playbackState.stationId != null) {
                playbackState = PlaybackUiState()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            playbackState = playbackState.copy(
                playing = false,
                buffering = false,
                connecting = false,
                error = "串流暫時無法播放",
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        connectMediaController()

        val stationRepository = StationRepository(applicationContext)
        val uiPreferences = UiPreferences(applicationContext)
        setContent {
            var displayMode by remember { mutableStateOf(uiPreferences.loadDisplayMode()) }
            var themeColor by remember { mutableStateOf(Color(uiPreferences.loadThemeColor())) }
            val sleepTimerEndElapsedRealtime by PlaybackService.sleepTimerEndElapsedRealtime.collectAsStateWithLifecycle()
            MiniTaiwanRadioTheme(displayMode, themeColor) {
                AppContent(
                    stationRepository = stationRepository,
                    uiPreferences = uiPreferences,
                    playbackState = playbackState,
                    displayMode = displayMode,
                    themeColor = themeColor,
                    onDisplayModeChange = {
                        displayMode = it
                        uiPreferences.saveDisplayMode(it)
                    },
                    onThemeColorChange = {
                        themeColor = it
                        uiPreferences.saveThemeColor(it.toArgb())
                    },
                    onPlay = ::play,
                    onStop = ::stopPlayback,
                    sleepTimerEndElapsedRealtime = sleepTimerEndElapsedRealtime,
                    onSetSleepTimer = ::setSleepTimer,
                    onCancelSleepTimer = ::cancelSleepTimer,
                )
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1001)
        }
    }

    private fun connectMediaController() {
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        future.addListener({ runCatching {
            mediaController = future.get()
            mediaController?.addListener(playerListener)
        } }, mainExecutor)
    }

    private fun play(station: Station) {
        playbackState = playbackState.copy(
            stationId = station.id,
            stationName = station.name,
            playing = false,
            buffering = true,
            connecting = true,
            error = null,
        )
        val intent = Intent(this, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_PLAY
            putExtra(PlaybackService.EXTRA_STREAM_URL, station.streamUrl)
            putExtra(PlaybackService.EXTRA_STATION_NAME, station.name)
            putExtra(PlaybackService.EXTRA_STATION_ID, station.id)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopPlayback() {
        mediaController?.stop()
        startService(Intent(this, PlaybackService::class.java).setAction(PlaybackService.ACTION_STOP))
        playbackState = PlaybackUiState()
    }

    private fun setSleepTimer(minutes: Int) {
        startService(
            Intent(this, PlaybackService::class.java)
                .setAction(PlaybackService.ACTION_SET_SLEEP_TIMER)
                .putExtra(PlaybackService.EXTRA_SLEEP_TIMER_DURATION_MILLIS, minutes * 60_000L),
        )
    }

    private fun cancelSleepTimer() {
        startService(Intent(this, PlaybackService::class.java).setAction(PlaybackService.ACTION_CANCEL_SLEEP_TIMER))
    }

    override fun onDestroy() {
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        super.onDestroy()
    }
}

@Composable
private fun AppContent(
    stationRepository: StationRepository,
    uiPreferences: UiPreferences,
    playbackState: PlaybackUiState,
    displayMode: String,
    themeColor: Color,
    onDisplayModeChange: (String) -> Unit,
    onThemeColorChange: (Color) -> Unit,
    onPlay: (Station) -> Unit,
    onStop: () -> Unit,
    sleepTimerEndElapsedRealtime: Long?,
    onSetSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var showSettings by remember { mutableStateOf(false) }
    var stations by remember { mutableStateOf<List<Station>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var updateLoading by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<String?>(null) }

    fun loadInitial() {
        scope.launch {
            loading = true
            loadError = null
            runCatching { stationRepository.load() }
                .onSuccess { stations = it }
                .onFailure { loadError = "無法更新清單，請稍後再試" }
            loading = false
        }
    }

    fun refreshStations() {
        scope.launch {
            updateLoading = true
            updateResult = null
            stationRepository.refreshRemote()
                .onSuccess {
                    stations = it
                    updateResult = "更新成功，共 ${it.size} 個電台"
                }
                .onFailure {
                    updateResult = "更新失敗，已保留目前的電台資料"
                }
            updateLoading = false
        }
    }

    LaunchedEffect(Unit) { loadInitial() }
    BackHandler(enabled = showSettings) { showSettings = false }
    if (showSettings) {
        SettingsScreen(
            displayMode = displayMode,
            themeColor = themeColor,
            updateLoading = updateLoading,
            updateResult = updateResult,
            onBack = { showSettings = false },
            onDisplayModeChange = onDisplayModeChange,
            onThemeColorChange = onThemeColorChange,
            onRefreshStations = ::refreshStations,
        )
    } else {
        RadioHome(
            repository = stationRepository,
            stations = stations,
            loading = loading,
            error = loadError,
            playbackState = playbackState,
            onPlay = onPlay,
            onStop = onStop,
            sleepTimerEndElapsedRealtime = sleepTimerEndElapsedRealtime,
            onSetSleepTimer = onSetSleepTimer,
            onCancelSleepTimer = onCancelSleepTimer,
            onSettings = { showSettings = true },
        )
    }
}

@Composable
private fun RadioHome(
    repository: StationRepository,
    stations: List<Station>,
    loading: Boolean,
    error: String?,
    playbackState: PlaybackUiState,
    onPlay: (Station) -> Unit,
    onStop: () -> Unit,
    sleepTimerEndElapsedRealtime: Long?,
    onSetSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onSettings: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val itemHeights = remember { mutableStateMapOf<String, Int>() }
    var favoriteIds by remember { mutableStateOf(repository.loadFavoriteIds()) }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(stations, loading, error) {
        if (loading || (stations.isEmpty() && error != null)) return@LaunchedEffect
        val validIds = favoriteIds.filter { id -> stations.any { station -> station.id == id } }
        if (validIds != favoriteIds) {
            favoriteIds = validIds
            repository.saveFavoriteIds(validIds)
        }
    }

    fun toggleFavorite(station: Station) {
        val added = station.id !in favoriteIds
        favoriteIds = if (added) favoriteIds + station.id else favoriteIds.filterNot { it == station.id }
        repository.saveFavoriteIds(favoriteIds)
        if (added) scope.launch { snackbarHostState.showSnackbar("${station.name}已加入最愛") }
    }

    fun moveFavorite(id: String, direction: Int): Boolean {
        val oldIndex = favoriteIds.indexOf(id)
        val newIndex = oldIndex + direction
        if (oldIndex < 0 || newIndex !in favoriteIds.indices) return false
        val anchorIndex = listState.firstVisibleItemIndex
        val anchorOffset = listState.firstVisibleItemScrollOffset
        val reordered = favoriteIds.toMutableList().apply { add(newIndex, removeAt(oldIndex)) }
        favoriteIds = reordered
        listState.requestScrollToItem(anchorIndex, anchorOffset)
        repository.saveFavoriteIds(reordered)
        return true
    }

    val itemSpacing = with(LocalDensity.current) { 6.dp.toPx() }

    fun moveDistance(id: String, direction: Int): Float? {
        val oldIndex = favoriteIds.indexOf(id)
        val neighborIndex = oldIndex + direction
        if (oldIndex < 0 || neighborIndex !in favoriteIds.indices) return null
        return itemHeights[favoriteIds[neighborIndex]]?.plus(itemSpacing)
    }

    fun onDragDelta(delta: Float) {
        dragOffset += delta
        val id = draggingId ?: return
        while (true) {
            val direction = if (dragOffset > 0f) 1 else -1
            val distance = moveDistance(id, direction) ?: break
            if (kotlin.math.abs(dragOffset) <= distance / 2f) break
            if (!moveFavorite(id, direction)) break
            dragOffset -= direction * distance
        }
        val favoriteIndex = favoriteIds.indexOf(id)
        if (favoriteIndex == 0) dragOffset = dragOffset.coerceAtLeast(0f)
        if (favoriteIndex == favoriteIds.lastIndex) dragOffset = dragOffset.coerceAtMost(0f)
    }

    val favoriteSet = favoriteIds.toSet()
    val sortedStations = favoriteIds.mapNotNull { id -> stations.firstOrNull { it.id == id } } +
        stations.filter { it.id !in favoriteSet }
    fun scrollToPlaying() {
        val index = sortedStations.indexOfFirst { it.id == playbackState.stationId }
        if (index >= 0) scope.launch { listState.animateScrollToItem(index) }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (playbackState.stationName != null) {
                    MiniPlayer(
                        state = playbackState,
                        sleepTimerEndElapsedRealtime = sleepTimerEndElapsedRealtime,
                        onSetSleepTimer = onSetSleepTimer,
                        onCancelSleepTimer = onCancelSleepTimer,
                        onStop = onStop,
                        onClick = ::scrollToPlaying,
                    )
                }
            },
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp)) {
                Header(onSettings = onSettings)
                when {
                    loading && stations.isEmpty() -> LoadingView()
                    stations.isEmpty() -> EmptyView(message = error ?: "目前沒有電台資料")
                    else -> {
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = draggingId == null,
                            contentPadding = PaddingValues(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(sortedStations, key = { it.id }) { station ->
                                StationCard(
                                    itemModifier = if (station.id == draggingId) Modifier else Modifier.animateItem(),
                                    station = station,
                                    isPlaying = playbackState.stationId == station.id && playbackState.playing,
                                    isFavorite = station.id in favoriteSet,
                                    isDragging = station.id == draggingId,
                                    dragOffset = if (station.id == draggingId) dragOffset else 0f,
                                    onPlay = { onPlay(station) },
                                    onStop = onStop,
                                    onToggleFavorite = { toggleFavorite(station) },
                                    onHeightChanged = { itemHeights[station.id] = it },
                                    onDragStart = {
                                        if (station.id in favoriteSet) {
                                            draggingId = station.id
                                            dragOffset = 0f
                                        }
                                    },
                                    onDrag = ::onDragDelta,
                                    onDragEnd = {
                                        draggingId = null
                                        dragOffset = 0f
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(onSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(54.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
            Text(")))", color = MaterialTheme.colorScheme.onPrimary, fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("mini台灣電台", fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text("把台灣電台帶著走", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = "設定") }
    }
}

@Composable
private fun MiniPlayer(
    state: PlaybackUiState,
    sleepTimerEndElapsedRealtime: Long?,
    onSetSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onStop: () -> Unit,
    onClick: () -> Unit,
) {
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    val remainingSeconds = rememberRemainingSeconds(sleepTimerEndElapsedRealtime)
    if (showSleepTimerSheet) {
        SleepTimerSheet(
            isActive = remainingSeconds != null,
            onSet = onSetSleepTimer,
            onCancel = onCancelSleepTimer,
            onDismiss = { showSleepTimerSheet = false },
        )
    }
    Surface(
        modifier = Modifier.navigationBarsPadding().clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        tonalElevation = 8.dp,
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("現正播放", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        when {
                            state.error != null -> state.error
                            state.connecting -> "正在連線..."
                            state.playing -> "播放中"
                            else -> ""
                        },
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(4.dp))
                    IconButton(modifier = Modifier.size(36.dp), onClick = { showSleepTimerSheet = true }) {
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = "設定睡眠定時器",
                            modifier = Modifier.size(17.dp),
                            tint = if (remainingSeconds != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    remainingSeconds?.let {
                        Text(
                            formatSleepTimer(it),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }
                }
                Text(state.stationName.orEmpty(), fontWeight = FontWeight.Bold, maxLines = 1)
            }
            IconButton(modifier = Modifier.size(38.dp).background(MaterialTheme.colorScheme.primary, CircleShape), onClick = onStop) {
                Icon(Icons.Filled.Stop, contentDescription = "停止播放", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun rememberRemainingSeconds(endElapsedRealtime: Long?): Long? {
    var now by remember(endElapsedRealtime) { mutableStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(endElapsedRealtime) {
        while (endElapsedRealtime != null && now < endElapsedRealtime) {
            now = SystemClock.elapsedRealtime()
            delay(1_000)
        }
    }
    return endElapsedRealtime
        ?.let { ((it - now).coerceAtLeast(0L) + 999L) / 1_000L }
        ?.takeIf { it > 0L }
}

private fun formatSleepTimer(totalSeconds: Long): String {
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerSheet(
    isActive: Boolean,
    onSet: (Int) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text("睡眠定時器", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("播放將在指定時間後自動停止", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
            listOf(
                listOf(5 to "5 分鐘", 10 to "10 分鐘"),
                listOf(15 to "15 分鐘", 30 to "30 分鐘"),
                listOf(45 to "45 分鐘", 60 to "1 小時"),
            ).forEach { options ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    options.forEach { (minutes, label) ->
                        OutlinedButton(
                            onClick = { onSet(minutes); onDismiss() },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(label)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (isActive) {
                TextButton(onClick = { onCancel(); onDismiss() }, modifier = Modifier.align(Alignment.End)) {
                    Text("取消定時器")
                }
            }
        }
    }
}

@Composable
private fun StationCard(
    itemModifier: Modifier,
    station: Station,
    isPlaying: Boolean,
    isFavorite: Boolean,
    isDragging: Boolean,
    dragOffset: Float,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onToggleFavorite: () -> Unit,
    onHeightChanged: (Int) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    Card(
        modifier = itemModifier.fillMaxWidth().onSizeChanged { onHeightChanged(it.height) }.graphicsLayer { scaleX = if (isDragging) 1.03f else 1f; scaleY = if (isDragging) 1.03f else 1f }.zIndex(if (isDragging) 1f else 0f).offset { IntOffset(0, if (isDragging) dragOffset.roundToInt() else 0) }.then(
            if (isFavorite) Modifier.pointerInput(station.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd,
                    onDrag = { change, amount -> change.consume(); onDrag(amount.y) },
                )
            } else Modifier,
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 10.dp else 1.dp),
        border = if (isPlaying) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text("FM", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(station.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(listOf(station.network, station.frequency, station.region).filter { it.isNotBlank() }.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(modifier = Modifier.size(36.dp), onClick = onToggleFavorite) {
                    Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = if (isFavorite) "取消最愛" else "加入最愛", tint = if (isFavorite) Color(0xFFFF8A8A) else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (station.streamUrl.isBlank() || !station.enabled) {
                    Text("準備中", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                } else {
                    IconButton(modifier = Modifier.size(36.dp), onClick = if (isPlaying) onStop else onPlay) {
                        Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = if (isPlaying) "停止播放" else "播放", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    displayMode: String,
    themeColor: Color,
    updateLoading: Boolean,
    updateResult: String?,
    onBack: () -> Unit,
    onDisplayModeChange: (String) -> Unit,
    onThemeColorChange: (Color) -> Unit,
    onRefreshStations: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回") }
                Text("設定", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
            Text("顯示模式", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("system" to "系統", "light" to "淺色", "dark" to "深色").forEach { (value, label) ->
                    FilterChip(selected = displayMode == value, onClick = { onDisplayModeChange(value) }, label = { Text(label) })
                }
            }
            Spacer(Modifier.height(28.dp))
            Text("主題色彩", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ThemeColors.forEach { color ->
                    Box(
                        modifier = Modifier.size(42.dp).background(color, CircleShape).clickable { onThemeColorChange(color) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (color == themeColor) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "已選擇",
                                tint = if (color == Color(0xFFF2C94C)) Color(0xFF12304A) else Color.White,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(34.dp).background(themeColor, CircleShape))
                Spacer(Modifier.width(12.dp))
                Text("自訂色彩", fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(12.dp))
                HueSlider(modifier = Modifier.weight(1f), themeColor = themeColor, onChange = onThemeColorChange)
            }
            Spacer(Modifier.height(28.dp))
            Text("電台資料", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onRefreshStations, enabled = !updateLoading) {
                Text(if (updateLoading) "更新中..." else "更新電台清單")
            }
            updateResult?.let {
                Text(it, color = if (it.startsWith("更新成功")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "版本 ${BuildConfig.VERSION_NAME}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Build ${BuildConfig.BUILD_TIMESTAMP_UTC}.${BuildConfig.VERSION_CODE}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun HueSlider(modifier: Modifier = Modifier, themeColor: Color, onChange: (Color) -> Unit) {
    var hue by remember(themeColor) { mutableStateOf(themeColor.toHsv()[0]) }
    Box(modifier = modifier.height(36.dp).drawBehind {
            drawRoundRect(Brush.horizontalGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)), cornerRadius = CornerRadius(14.dp.toPx()))
        }) {
        Slider(value = hue, onValueChange = { hue = it; onChange(Color.hsv(it, 0.72f, 0.95f)) }, valueRange = 0f..360f, modifier = Modifier.fillMaxWidth(), colors = SliderDefaults.colors(thumbColor = Color.Transparent, activeTrackColor = Color.Transparent, inactiveTrackColor = Color.Transparent))
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width * (hue / 360f)
            drawCircle(Color.White, radius = 10.dp.toPx(), center = Offset(x, size.height / 2), style = Stroke(width = 3.dp.toPx()))
            drawCircle(themeColor, radius = 6.dp.toPx(), center = Offset(x, size.height / 2))
        }
    }
}

private fun Color.toHsv(): FloatArray {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val delta = max - min
    val hue = when {
        delta == 0f -> 0f
        max == red -> (60f * ((green - blue) / delta) + 360f) % 360f
        max == green -> 60f * ((blue - red) / delta + 2f)
        else -> 60f * ((red - green) / delta + 4f)
    }
    return floatArrayOf(hue, if (max == 0f) 0f else delta / max, max)
}

@Composable
private fun MiniTaiwanRadioTheme(displayMode: String, themeColor: Color, content: @Composable () -> Unit) {
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val view = androidx.compose.ui.platform.LocalView.current
    val dark = displayMode == "dark" || (displayMode == "system" && systemDark)
    val colors = if (dark) darkColorScheme(primary = themeColor, secondary = themeColor, tertiary = themeColor) else lightColorScheme(primary = themeColor, secondary = themeColor, tertiary = themeColor)
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            WindowCompat.getInsetsController(it, it.decorView).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(colorScheme = colors, content = content)
}

@Composable
private fun LoadingView() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } }

@Composable
private fun EmptyView(message: String) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
