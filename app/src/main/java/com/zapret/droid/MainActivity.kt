package com.zapret.droid

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.zapret.droid.strategies.ByedpiCandidate
import com.zapret.droid.strategies.ByedpiConfigGenerator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GeneratorApp() }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GeneratorApp() {
    val context = LocalContext.current
    val candidates = remember { mutableStateListOf<ByedpiCandidate>() }
    var seed by remember { mutableStateOf(0L) }
    var batchCount by remember { mutableStateOf(1) }
    var serial by remember { mutableStateOf(0) }
    var notice by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(ByedpiConfigGenerator.export(candidates, seed).toByteArray(Charsets.UTF_8))
                notice = "Конфиг сохранён."
            } ?: run { notice = "Не удалось открыть файл." }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("ByeDPI Strategy Generator") }) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Новый набор с нуля", style = MaterialTheme.typography.titleLarge)
                        Text("Seed: $seed · в партии: $batchCount · всего: ${candidates.size}")
                        if (notice.isNotBlank()) Text(notice)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { batchCount = (batchCount - 1).coerceAtLeast(1) },
                                modifier = Modifier.weight(1f)
                            ) { Text("-") }
                            OutlinedButton(
                                onClick = { batchCount = (batchCount + 1).coerceAtMost(Int.MAX_VALUE) },
                                modifier = Modifier.weight(1f)
                            ) { Text("+1") }
                            OutlinedButton(
                                onClick = { batchCount = (batchCount + 10).coerceAtMost(Int.MAX_VALUE) },
                                modifier = Modifier.weight(1f)
                            ) { Text("+10") }
                            OutlinedButton(
                                onClick = { batchCount = (batchCount + 100).coerceAtMost(Int.MAX_VALUE) },
                                modifier = Modifier.weight(1f)
                            ) { Text("+100") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    seed += 1
                                    serial = 0
                                    notice = "Новый seed: $seed"
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Новый seed") }
                            Button(
                                onClick = {
                                    val generated = ByedpiConfigGenerator.generate(seed, serial, batchCount)
                                    candidates.addAll(generated)
                                    serial += generated.size
                                    notice = "Создано: ${generated.size}"
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Генерировать") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    candidates.clear()
                                    serial = 0
                                    notice = "Список очищен."
                                },
                                enabled = candidates.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            ) { Text("Очистить") }
                            OutlinedButton(
                                onClick = { exportLauncher.launch("byedpi-$seed.txt") },
                                enabled = candidates.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            ) { Text("Экспорт .txt") }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Кандидаты", style = MaterialTheme.typography.titleLarge)
                        if (candidates.isEmpty()) {
                            Text("Нажми «Генерировать», чтобы создать конфиг с нуля.")
                        } else {
                            candidates.takeLast(100).asReversed().forEach { candidate ->
                                Text(candidate.arguments, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}
