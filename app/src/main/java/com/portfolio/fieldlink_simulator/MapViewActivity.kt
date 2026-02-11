package com.portfolio.fieldlink_simulator

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.portfolio.fieldlink_simulator.ui.theme.FieldLink_SimulatorTheme
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView

class MapViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FieldLink_SimulatorTheme {
                MapNavigatorScreen()
            }
        }

    }
}
@Composable
@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape,cutout=none,navigation=gesture"
)
fun MapViewPreview(){
    MapNavigatorScreen()
}

data class LoggedInUser(
    val id: Int,
    val name: String,
    val avatar: String
)

@Composable
fun MapNavigatorScreen(
    user: LoggedInUser = LoggedInUser(1, "사용자", "👨")
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9)) // slate-100 느낌
    ) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                user = user,
                query = query,
                onQueryChange = { query = it }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp)
            ) {
/*                // 지도 배경(가짜)
                FakeMapBackground()*/

                // osmdroid 지도
                OsmMap()

                // 왼쪽 하단 컨트롤 버튼
                MapControls(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 24.dp)
                )

                // 메뉴 토글 버튼(우측 가운데)
                MenuFab(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 24.dp),
                    isOpen = isMenuOpen,
                    onToggle = { isMenuOpen = !isMenuOpen }
                )

                // 슬라이드 메뉴(오른쪽)
                SlideMenu(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    isOpen = isMenuOpen,
                    onClose = { isMenuOpen = false }
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    user: LoggedInUser,
    query: String,
    onQueryChange: (String) -> Unit
) {
    var isExit by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 타이틀 영역
                /*Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color(0xFF2563EB), // blue-600 느낌
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Map Navigator",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B) // slate-800
                    )
                }

                Spacer(Modifier.width(22.dp))*/

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // 👤 사용자 이름
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "user",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "사용자",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(Modifier.width(20.dp))

                        // 📍 현재 위치
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "location",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "currentLocation",
                                fontSize = 13.sp,
                                color = Color(0xFF334155)
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        // 🔋 배터리
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "battery",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "100%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155)
                            )
                        }

                        Spacer(Modifier.width(16.dp))
                    }
                }

            }

            // 유저 칩
            UserChip(user = user)
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .widthIn(min = 260.dp, max = 420.dp)
            .height(40.dp)
            .clip(shape)
            .background(Color(0xFFF1F5F9))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color(0xFF94A3B8), // slate-400
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("위치 검색...", color = Color(0xFF64748B), fontSize = 14.sp) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF2563EB)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun UserChip(user: LoggedInUser) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(Color(0xFFF1F5F9))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = user.avatar, fontSize = 20.sp)
        Text(
            text = user.name,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155) // slate-700
        )
    }
}

@Composable
private fun FakeMapBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFD1FAE5), // green-100
                        Color(0xFFEFF6FF), // blue-50
                        Color(0xFFF1F5F9)  // slate-100
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 그리드
            val step = 40f
            val gridColor = Color(0xFF94A3B8).copy(alpha = 0.12f)
            for (x in 0..(size.width / step).toInt()) {
                drawLine(
                    color = gridColor,
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(size.height / step).toInt()) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y * step),
                    end = Offset(size.width, y * step),
                    strokeWidth = 1f
                )
            }

            // 가상 도로(굵은 라인)
            val roadColor = Color(0xFF94A3B8).copy(alpha = 0.35f)
            fun roadHorizontal(y: Float) {
                drawRect(
                    color = roadColor,
                    topLeft = Offset(0f, y),
                    size = Size(size.width, 8f)
                )
            }
            fun roadVertical(x: Float) {
                drawRect(
                    color = roadColor,
                    topLeft = Offset(x, 0f),
                    size = Size(8f, size.height)
                )
            }

            roadHorizontal(size.height * 0.33f)
            roadHorizontal(size.height * 0.66f)
            roadVertical(size.width * 0.25f)
            roadVertical(size.width * 0.66f)

            // 마커 3개
            drawPin(
                center = Offset(size.width * 0.33f, size.height * 0.25f),
                color = Color(0xFFEF4444),
                scale = 1.2f
            )
            drawPin(
                center = Offset(size.width * 0.75f, size.height * 0.66f),
                color = Color(0xFF3B82F6),
                scale = 0.95f
            )
            drawPin(
                center = Offset(size.width * 0.5f, size.height * 0.5f),
                color = Color(0xFF22C55E),
                scale = 0.95f
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPin(
    center: Offset,
    color: Color,
    scale: Float
) {
    // 간단한 핀 모양(원 + 삼각형)
    val r = 10f * scale
    drawCircle(color = color, radius = r, center = center)

    // 아래 꼬리
    val tailHeight = 18f * scale
    drawLine(
        color = color,
        start = Offset(center.x, center.y + r),
        end = Offset(center.x, center.y + r + tailHeight),
        strokeWidth = 6f * scale,
        cap = StrokeCap.Round
    )

    // 그림자 느낌(살짝)
    drawCircle(
        color = Color.Black.copy(alpha = 0.08f),
        radius = (r + 6f) * scale,
        center = Offset(center.x + 2f, center.y + 3f)
    )
}

@Composable
private fun MapControls(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ControlButton(icon = Icons.Default.LocationOn) { /* TODO */ }
        ControlButton(icon = Icons.Default.Place) { /* TODO */ }
        ZoomButton(text = "+") { /* TODO */ }
        ZoomButton(text = "−") { /* TODO */ }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(52.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFF334155))
        }
    }
}

@Composable
private fun ZoomButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(52.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
        }
    }
}

@Composable
private fun MenuFab(
    modifier: Modifier = Modifier,
    isOpen: Boolean,
    onToggle: () -> Unit
) {
    FloatingActionButton(
        onClick = onToggle,
        containerColor = Color(0xFF2563EB),
        contentColor = Color.White,
        modifier = modifier.size(64.dp),
        shape = CircleShape
    ) {
        Icon(
            imageVector = if (isOpen) Icons.Default.Close else Icons.Default.Menu,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun SlideMenu(
    modifier: Modifier = Modifier,
    isOpen: Boolean,
    onClose: () -> Unit
) {
    val targetWidth = 320.dp
    val offsetX by animateDpAsState(
        targetValue = if (isOpen) 0.dp else targetWidth,
        animationSpec = tween(durationMillis = 300),
        label = "menuOffset"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(targetWidth)
            .offset(x = offsetX)
            .shadow(24.dp)
            .background(Color.White)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Menu", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color(0xFF0F172A))
                Text(
                    text = "닫기",
                    color = Color(0xFF2563EB),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onClose() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Divider(color = Color(0xFFE2E8F0))

            Text("• 내 위치로 이동", color = Color(0xFF334155))
            Text("• 레이어 설정", color = Color(0xFF334155))
            Text("• 사용자 정보", color = Color(0xFF334155))
            Text("• 설정", color = Color(0xFF334155))
            Text("• 로그아웃", color = Color(0xFF334155))

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onClose() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("메뉴 닫기", color = Color.White)
            }
        }
    }
}

@Composable
private fun OsmMap(
    modifier: Modifier = Modifier
){
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setBuiltInZoomControls(true)
                controller.setZoom(15.0)
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { mapView ->
            // 업데이트 후 동작
        }
    )
}