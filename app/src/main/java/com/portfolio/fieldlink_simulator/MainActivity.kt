package com.portfolio.fieldlink_simulator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.fieldlink_simulator.ui.theme.FieldLink_SimulatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FieldLink_SimulatorTheme {
                PatternLoginView()
            }
        }
    }
}

@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape,cutout=none,navigation=gesture"
)
@Composable
fun PatterLoginPreview(){
    PatternLoginView()
}

data class User(
    val id: Int,
    val name: String,
    val avatar: String,
    val pattern: List<Int>
)

private val mockUsers = listOf(
    User(1, "김민수", "👨", listOf(0, 1, 2, 5, 8)),
    User(2, "이지은", "👩", listOf(0, 4, 8, 7, 6)),
    User(3, "박서준", "👨‍💼", listOf(2, 4, 6, 3, 0)),
    User(4, "최유나", "👩‍💻", listOf(1, 4, 7, 8, 5)),
)

@Composable
fun PatternLoginView() {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var loginSuccess by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    suspend fun showSnack(msg: String) {
        snackbarHostState.showSnackbar(message = msg, withDismissAction = true)
    }

    val bgBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(padding)
                .padding(24.dp)
        ) {
            ResponsiveTwoPane(
                left = {
                    FrostedCard(title = "사용자 선택") {
                        UserSelector(
                            users = mockUsers,
                            selectedUser = selectedUser,
                            onSelectUser = {
                                selectedUser = it
                                loginSuccess = false
                            }
                        )
                    }
                },
                right = {
                    FrostedCard {
                        if (selectedUser == null) {
                            EmptyRightPanel()
                        } else {
                            PatternLoginPanel(
                                user = selectedUser!!,
                                loginSuccess = loginSuccess,
                                onPatternComplete = { pattern ->
                                    val ok = pattern == selectedUser!!.pattern
                                    if (ok) {
                                        loginSuccess = true
                                    } else {
                                        loginSuccess = false
                                        // 패턴 초기화는 PatternGrid 내부에서 처리됨
                                        // 메시지만 보여주기
                                        // (snackbar가 더 자연스러움)
                                        // coroutine scope 필요
                                    }
                                },
                                onLoginSuccessShown = {
                                    // 성공 애니메이션 후 초기화
                                    /*selectedUser = null
                                    loginSuccess = false*/
                                },
                                onTooShort = {
                                    // 최소 4점 안내
                                },
                                onWrong = {
                                    // 틀림 안내
                                },
                                snackbarHostState = snackbarHostState
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ResponsiveTwoPane(
    left: @Composable () -> Unit,
    right: @Composable () -> Unit
) {
    /*BoxWithConstraints(Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 900.dp
        if (isWide) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(Modifier.weight(1f)) { left() }
                Box(Modifier.weight(1f)) { right() }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                left()
                right()
            }
        }
    }*/
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(Modifier.weight(1f)) { left() }
        Box(Modifier.weight(1f)) { right() }
    }
}

@Composable
private fun FrostedCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.10f),
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, Color.White.copy(alpha = 0.20f), shape)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            content()
        }
    }
}

@Composable
private fun UserSelector(
    users: List<User>,
    selectedUser: User?,
    onSelectUser: (User) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
    ) {
        items(users) { user ->
            val selected = selectedUser?.id == user.id
            val bg = if (selected) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.06f)
            val border = if (selected) Color(0xFF3B82F6) else Color.Transparent

            Surface(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSelectUser(user) },
                color = bg,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .border(1.dp, border, RoundedCornerShape(18.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = user.avatar, fontSize = 5.sp)
                    }

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "패턴으로 로그인",
                            color = Color.White.copy(alpha = 0.65f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRightPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 360.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .border(
                    width = 4.dp,
                    color = Color.White.copy(alpha = 0.20f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 34.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "좌측에서 사용자를 선택해주세요",
            color = Color.White.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun PatternLoginPanel(
    user: User,
    loginSuccess: Boolean,
    onPatternComplete: (List<Int>) -> Unit,
    onLoginSuccessShown: () -> Unit,
    onTooShort: () -> Unit,
    onWrong: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 520.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(user.avatar, fontSize = 10.sp)
            Spacer(Modifier.width(4.dp))
            Text(user.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(4.dp))
            Text("패턴을 그려주세요", color = Color.White.copy(alpha = 0.65f), fontSize = 13.sp)
        }

        Spacer(Modifier.height(10.dp))

        if (loginSuccess) {
            val context = LocalContext.current
            val activity = context as? Activity

            LoginSuccessView(
                onDone = {
                    onLoginSuccessShown()
                }
            )

            // 로그인 성공시 MapView 화면으로 이동
            LaunchedEffect(loginSuccess) {
                Log.e("Login","로그인 성공!")
                delay(2000L) // 🎯 여기서 2초 대기
                val intent = Intent(context, MapViewActivity::class.java)
                context.startActivity(intent)
                activity?.finish()
            }


        } else {
            PatternGrid(
                modifier = Modifier,
                onPatternComplete = { pattern ->
                    if (pattern.size < 4) {
                        scope.launch {
                            snackbarHostState.showSnackbar("최소 4개의 점을 연결해주세요.")
                        }
                        onTooShort()
                        return@PatternGrid
                    }

                    if (pattern == user.pattern) {
                        onPatternComplete(pattern)
                        // 성공 애니메이션으로 전환은 상위 state(loginSuccess)에서 처리
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("패턴이 일치하지 않습니다. 입력패턴 : " + pattern)
                        }
                        onWrong()
                        // 틀렸을 때도 PatternGrid 내부에서 초기화됨
                    }
                }
            )
        }
    }
}

@Composable
    private fun LoginSuccessView(onDone: () -> Unit) {
    val bounce = rememberInfiniteTransition(label = "bounce")
    val scale by bounce.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(1000)
        onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(CircleShape)
                .background(Color(0xFF22C55E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "success",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Text("로그인 성공!", color = Color.White, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun LoginSuccessHandler(
    onNavigate: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1000L) // 1초 딜레이
        onNavigate()
    }
}

@Composable
private fun PatternGrid(
    modifier: Modifier = Modifier,
    onPatternComplete: (List<Int>) -> Unit
) {
    val gridSize = 3
    val dotRadius = 10.dp
    val selectedDotRadius = 20.dp

    var pattern by remember { mutableStateOf(listOf<Int>()) }
    var isDrawing by remember { mutableStateOf(false) }
    var currentPos by remember { mutableStateOf<Offset?>(null) }
//    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val density = LocalDensity.current
    val dotRadiusPx = with(density) { dotRadius.toPx() }
    val selectedDotRadiusPx = with(density) { selectedDotRadius.toPx() }

    fun dotPosition(index: Int, canvasSize: Size): Offset {
        val cellSize = canvasSize.width / gridSize
        val row = index / gridSize
        val col = index % gridSize
        return Offset(
            x = col * cellSize + cellSize / 2f,
            y = row * cellSize + cellSize / 2f
        )
    }

    fun nearestDot(x: Float, y: Float, canvasSize: Size): Int {
        val cellSize = canvasSize.width / gridSize
        var nearest = -1
        var minDist = Float.POSITIVE_INFINITY

        for (i in 0 until gridSize * gridSize) {
            val p = dotPosition(i, canvasSize)
            val dist = hypot(x - p.x, y - p.y)
            if (dist < cellSize / 3f && dist < minDist) {
                minDist = dist
                nearest = i
            }
        }
        return nearest
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val canvasShape = RoundedCornerShape(18.dp)
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(canvasShape)
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.20f), canvasShape)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val s = this.size
                                val canvas = Size(s.width.toFloat(), s.height.toFloat())
                                val idx = nearestDot(offset.x, offset.y, canvas)
                                if (idx != -1) {
                                    pattern = listOf(idx)
                                    isDrawing = true
                                    currentPos = offset
                                }
                                Log.e("Drag","드래그 시작")
                            },
                            onDrag = { change, _ ->
                                if (!isDrawing) return@detectDragGestures
                                val s = this.size
                                val canvas = Size(s.width.toFloat(),s.height.toFloat())
                                val pos = change.position
                                currentPos = pos

                                val idx = nearestDot(pos.x, pos.y, canvas)
                                if (idx != -1 && !pattern.contains(idx)) {
                                    pattern = pattern + idx
                                }
                                Log.e("Drag","드래그 중")
                            },
                            onDragEnd = {
                                Log.e("Drag","드래그 끝")
                                val result = pattern
                                // reset
                                pattern = emptyList()
                                isDrawing = false
                                currentPos = null

                                if (result.isNotEmpty()) {
                                    onPatternComplete(result)
                                }
                            },
                            onDragCancel = {
                                Log.e("Drag","드래그 취소")
                                pattern = emptyList()
                                isDrawing = false
                                currentPos = null
                            }
                        )
                    }
            ) {
                val size = this.size
                val blue = Color(0xFF3B82F6)
                val blueLight = Color(0xFF60A5FA)
                val white30 = Color.White.copy(alpha = 0.30f)
                val white50 = Color.White.copy(alpha = 0.50f)

                // Lines
                if (pattern.isNotEmpty()) {
                    val path = Path()
                    val first = dotPosition(pattern.first(), size)
                    path.moveTo(first.x, first.y)
                    for (i in 1 until pattern.size) {
                        val p = dotPosition(pattern[i], size)
                        path.lineTo(p.x, p.y)
                    }
                    if (isDrawing && currentPos != null) {
                        path.lineTo(currentPos!!.x, currentPos!!.y)
                    }
                    drawPath(
                        path = path,
                        color = blue,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // Dots
                for (i in 0 until gridSize * gridSize) {
                    val p = dotPosition(i, size)
                    val selected = pattern.contains(i)

                    drawCircle(
                        color = if (selected) blue else white30,
                        radius = if (selected) selectedDotRadiusPx else dotRadiusPx,
                        center = p
                    )
                    drawCircle(
                        color = if (selected) blueLight else white50,
                        radius = if (selected) selectedDotRadiusPx else dotRadiusPx,
                        center = p,
                        style = Stroke(width = if (selected) 3.dp.toPx() else 2.dp.toPx())
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = if (pattern.isNotEmpty()) "${pattern.size}개 점 선택됨" else "점을 터치하고 드래그하세요",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 12.sp
        )
    }
}