package cz.mares.foldtest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5EEAD4),
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Color(0xFF115E59),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFF93C5FD),
    background = Color(0xFF0B1120),
    surface = Color(0xFF131C31),
    surfaceVariant = Color(0xFF1E293B),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF134E4A),
    secondary = Color(0xFF1D4ED8),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE2E8F0),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF334155)
)

@Composable
fun App() {
    val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var draftNote by rememberSaveable {
        mutableStateOf("Rozepsaná poznámka přežije rozevření i zavření skládačky.")
    }
    var stepCounter by rememberSaveable { mutableStateOf(1) }
    var gridMask by rememberSaveable { mutableStateOf("100010001") }
    var lastWindowSig by rememberSaveable { mutableStateOf("") }
    var resizeEvents by rememberSaveable { mutableStateOf(0) }

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            color = MaterialTheme.colorScheme.background
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val widthDp = maxWidth.value.roundToInt()
                val heightDp = maxHeight.value.roundToInt()
                val isExpandedFoldable = maxWidth >= 600.dp
                val currentSig = "${widthDp}x${heightDp}"

                LaunchedEffect(currentSig) {
                    if (lastWindowSig.isNotEmpty() && lastWindowSig != currentSig) {
                        resizeEvents += 1
                    }
                    lastWindowSig = currentSig
                }

                val ratioHundredths = if (heightDp > 0) {
                    ((widthDp.toFloat() / heightDp.toFloat()) * 100f).roundToInt()
                } else {
                    100
                }
                val ratioText = "${ratioHundredths / 100}.${(ratioHundredths % 100).toString().padStart(2, '0')} : 1"

                val boardMaxSide = minOf(
                    if (isExpandedFoldable) (maxWidth / 2) - 64.dp else maxWidth - 56.dp,
                    maxHeight * 0.36f
                ).coerceIn(132.dp, 260.dp)

                if (isExpandedFoldable) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))
                            NavigationRailItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Text("📖", fontSize = 20.sp) },
                                label = { Text("Dva panely") }
                            )
                            NavigationRailItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Text("🎮", fontSize = 20.sp) },
                                label = { Text("Plocha 1:1") }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                TelemetryCard(
                                    widthDp = widthDp,
                                    heightDp = heightDp,
                                    ratioText = ratioText,
                                    isExpanded = true,
                                    resizeEvents = resizeEvents
                                )
                                ChecklistCard()
                                FooterLabel()
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ContinuityCard(
                                    draftNote = draftNote,
                                    onNoteChange = { draftNote = it },
                                    stepCounter = stepCounter,
                                    onIncrement = { stepCounter += 1 },
                                    onReset = {
                                        stepCounter = 0
                                        gridMask = "000000000"
                                    }
                                )
                                FoldableGameBoardCard(
                                    boardSide = boardMaxSide,
                                    gridMask = gridMask,
                                    onToggleCell = { idx ->
                                        gridMask = toggleMaskIndex(gridMask, idx)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()) {
                                if (selectedTab == 0) {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        TelemetryCard(
                                            widthDp = widthDp,
                                            heightDp = heightDp,
                                            ratioText = ratioText,
                                            isExpanded = false,
                                            resizeEvents = resizeEvents
                                        )
                                        ContinuityCard(
                                            draftNote = draftNote,
                                            onNoteChange = { draftNote = it },
                                            stepCounter = stepCounter,
                                            onIncrement = { stepCounter += 1 },
                                            onReset = {
                                                stepCounter = 0
                                                gridMask = "000000000"
                                            }
                                        )
                                        ChecklistCard()
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        TelemetryCard(
                                            widthDp = widthDp,
                                            heightDp = heightDp,
                                            ratioText = ratioText,
                                            isExpanded = false,
                                            resizeEvents = resizeEvents
                                        )
                                        FoldableGameBoardCard(
                                            boardSide = boardMaxSide,
                                            gridMask = gridMask,
                                            onToggleCell = { idx ->
                                                gridMask = toggleMaskIndex(gridMask, idx)
                                            }
                                        )
                                    }
                                }
                            }
                            FooterLabel()
                        }

                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Text("📱", fontSize = 18.sp) },
                                label = { Text("Diagnostika a stav") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Text("🎮", fontSize = 18.sp) },
                                label = { Text("Herní plocha") }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun toggleMaskIndex(mask: String, index: Int): String {
    val chars = mask.padEnd(9, '0').toCharArray()
    if (index in 0..8) {
        chars[index] = if (chars[index] == '1') '0' else '1'
    }
    return chars.concatToString()
}

@Composable
private fun TelemetryCard(
    widthDp: Int,
    heightDp: Int,
    ratioText: String,
    isExpanded: Boolean,
    resizeEvents: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FoldTest • Android 17 (API 37)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Skládačka / Široký" else "Kompaktní",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            MetricRow("Aktuální okno", "$widthDp × $heightDp dp")
            MetricRow("Poměr stran", ratioText)
            MetricRow(
                "Aktivní rozvržení",
                if (isExpanded) "2 panely + 20 dp mezera pantu" else "1 sloupec + spodní lišta"
            )
            MetricRow("Zachycené změny okna", "$resizeEvents× (bez ztráty stavu)")
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ContinuityCard(
    draftNote: String,
    onNoteChange: (String) -> Unit,
    stepCounter: Int,
    onIncrement: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Test udržení rozdělané práce (rememberSaveable)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = draftNote,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rozepsaný text při přechodu displejů") },
                shape = RoundedCornerShape(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onIncrement,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Krok +1 (stav: $stepCounter)")
                }
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Vynulovat")
                }
            }
        }
    }
}

@Composable
private fun FoldableGameBoardCard(
    boardSide: Dp,
    gridMask: String,
    onToggleCell: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Čtvercová herní deska z minOf(šířka, výška)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Klepni na políčka a změň velikost okna — deska se nikdy neořízne ani na poměru 1:1.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier.size(boardSide),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0..2) {
                            val idx = row * 3 + col
                            val active = gridMask.getOrNull(idx) == '1'
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (active) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onToggleCell(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (active) "★" else "${idx + 1}",
                                    color = if (active) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
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
private fun ChecklistCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Aktivní parametry tohoto buildu",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text("✅ compileSdk = 37, targetSdk = 37 (Android 17)", fontSize = 13.sp)
            Text("✅ AGP 8.5.2 + suppressUnsupportedCompileSdk=37", fontSize = 13.sp)
            Text("✅ resizeableActivity=true, bez zámku orientace", fontSize = 13.sp)
            Text("✅ Plynulý přechod configChanges bez zničení Activity", fontSize = 13.sp)
        }
    }
}

@Composable
private fun FooterLabel() {
    Text(
        text = "© 2026 Created by Pavel Mareš",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    )
}
