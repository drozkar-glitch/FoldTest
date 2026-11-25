rootProject.name = "FoldTest"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":composeApp")

===FILE: build.gradle.kts===
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.compose") version "1.7.3" apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
}

===FILE: gradle.properties===
org.gradle.jvmargs=-Xmx3072M -Dfile.encoding=UTF-8
kotlin.code.style=official
android.useAndroidX=true
android.nonTransitiveRClass=true
android.suppressUnsupportedCompileSdk=37
kotlin.mpp.enableCInteropCommonization=true

===FILE: gradle/wrapper/gradle-wrapper.properties===
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists

===FILE: composeApp/build.gradle.kts===
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.9.3")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
}

android {
    namespace = "cz.mares.foldtest"
    compileSdk = 37

    defaultConfig {
        applicationId = "cz.mares.foldtest"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

===FILE: composeApp/src/commonMain/kotlin/cz/mares/foldtest/App.kt===
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

===FILE: composeApp/src/androidMain/kotlin/cz/mares/foldtest/MainActivity.kt===
package cz.mares.foldtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

===FILE: composeApp/src/androidMain/AndroidManifest.xml===
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:roundIcon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:resizeableActivity="true"
            android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize|keyboardHidden|density|uiMode">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>

===FILE: composeApp/src/androidMain/res/values/strings.xml===
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">FoldTest</string>
</resources>

===FILE: composeApp/src/androidMain/res/drawable/ic_launcher_background.xml===
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#0B132B"
        android:pathData="M0,0h108v108h-108z" />
    <path
        android:fillColor="#1C2541"
        android:pathData="M0,0 L108,0 L108,58 Q54,76 0,58 Z" />
    <path
        android:fillColor="#142F43"
        android:pathData="M18,18 C42,8 66,8 90,18 C98,42 98,66 90,90 C66,98 42,98 18,90 C10,66 10,42 18,18 Z" />
</vector>

===FILE: composeApp/src/androidMain/res/drawable/ic_launcher_foreground.xml===
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- Stín pod 3D skládačkou v bezpečné zóně 66x66 (21..87) -->
    <path
        android:fillColor="#44000000"
        android:pathData="M28,77 L54,83 L80,77 L80,81 L54,86 L28,81 Z" />
    <!-- Levé křídlo šasi -->
    <path
        android:fillColor="#334155"
        android:pathData="M26,31 L52,26 L52,76 L26,71 Z" />
    <!-- Pravé křídlo šasi -->
    <path
        android:fillColor="#1E293B"
        android:pathData="M56,26 L82,31 L82,71 L56,76 Z" />
    <!-- Levý svítící displej -->
    <path
        android:fillColor="#2DD4BF"
        android:pathData="M29,34 L51,29.5 L51,72.5 L29,68 Z" />
    <!-- Pravý svítící displej -->
    <path
        android:fillColor="#0EA5E9"
        android:pathData="M57,29.5 L79,34 L79,68 L57,72.5 Z" />
    <!-- Lesklý odlesk levého panelu -->
    <path
        android:fillColor="#55FFFFFF"
        android:pathData="M29,34 L51,29.5 L51,45 L29,49 Z" />
    <!-- Lesklý odlesk pravého panelu -->
    <path
        android:fillColor="#33FFFFFF"
        android:pathData="M57,29.5 L79,34 L79,47 L57,43 Z" />
    <!-- Středový 3D kovový pant -->
    <path
        android:fillColor="#94A3B8"
        android:pathData="M52,25 L56,25 L56,77 L52,77 Z" />
    <path
        android:fillColor="#E2E8F0"
        android:pathData="M53,26 L55,26 L55,40 L53,40 Z" />
</vector>

===FILE: composeApp/src/androidMain/res/drawable/ic_launcher.xml===
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/ic_launcher_background" />
    <item android:drawable="@drawable/ic_launcher_foreground" />
</layer-list>

===FILE: composeApp/src/androidMain/res/drawable-v26/ic_launcher.xml===
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>

===FILE: composeApp/src/androidMain/res/mipmap-anydpi-v26/ic_launcher.xml===
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>

===FILE: composeApp/src/androidMain/res/mipmap-anydpi-v26/ic_launcher_round.xml===
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>

===FILE: composeApp/src/iosMain/kotlin/cz/mares/foldtest/MainViewController.kt===
package cz.mares.foldtest

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App() }

===FILE: iosApp/Configuration/Config.xcconfig===
TEAM_ID=
PRODUCT_NAME=FoldTest
PRODUCT_BUNDLE_IDENTIFIER=cz.mares.foldtest
CURRENT_PROJECT_VERSION=1
MARKETING_VERSION=1.0

===FILE: iosApp/iosApp/iOSApp.swift===
import SwiftUI

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

===FILE: iosApp/iosApp/ContentView.swift===
import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}

===FILE: iosApp/iosApp/Info.plist===
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CADisableMinimumFrameDurationOnPhone</key>
    <true/>
    <key>CFBundleDevelopmentRegion</key>
    <string>$(DEVELOPMENT_LANGUAGE)</string>
    <key>CFBundleExecutable</key>
    <string>$(EXECUTABLE_NAME)</string>
    <key>CFBundleIdentifier</key>
    <string>$(PRODUCT_BUNDLE_IDENTIFIER)</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>$(PRODUCT_NAME)</string>
    <key>CFBundlePackageType</key>
    <string>$(PRODUCT_BUNDLE_PACKAGE_TYPE)</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0</string>
    <key>CFBundleVersion</key>
    <string>1</string>
    <key>LSRequiresIPhoneOS</key>
    <true/>
    <key>UILaunchScreen</key>
    <dict/>
    <key>UISupportedInterfaceOrientations</key>
    <array>
        <string>UIInterfaceOrientationPortrait</string>
        <string>UIInterfaceOrientationLandscapeLeft</string>
        <string>UIInterfaceOrientationLandscapeRight</string>
    </array>
    <key>UISupportedInterfaceOrientations~ipad</key>
    <array>
        <string>UIInterfaceOrientationPortrait</string>
        <string>UIInterfaceOrientationPortraitUpsideDown</string>
        <string>UIInterfaceOrientationLandscapeLeft</string>
        <string>UIInterfaceOrientationLandscapeRight</string>
    </array>
</dict>
</plist>
