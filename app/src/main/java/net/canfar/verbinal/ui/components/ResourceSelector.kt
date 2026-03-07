package net.canfar.verbinal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ResourceSelector(
    coreOptions: List<Int>,
    cores: Int,
    onCoresChange: (Int) -> Unit,
    ramOptions: List<Int>,
    ram: Int,
    onRamChange: (Int) -> Unit,
    gpuOptions: List<Int>,
    gpus: Int,
    onGpusChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // CPU Cores
        if (coreOptions.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("CPU Cores", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "$cores",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(40.dp),
                    )
                    Slider(
                        value = cores.toFloat(),
                        onValueChange = { onCoresChange(it.roundToInt()) },
                        valueRange = coreOptions.min().toFloat()..coreOptions.max().toFloat(),
                        steps = coreOptions.max() - coreOptions.min() - 1,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // RAM (GB) - power of 2 snapping
        if (ramOptions.isNotEmpty()) {
            val ramMin = ramOptions.min()
            val ramMax = ramOptions.max()
            val pow2Values = remember(ramMin, ramMax) {
                val values = mutableListOf<Int>()
                var v = 1
                while (v <= ramMax) {
                    if (v >= ramMin) values.add(v)
                    v *= 2
                }
                if (values.isEmpty()) ramOptions.toMutableList()
                values
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("RAM (GB)", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "$ram",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(40.dp),
                    )
                    var sliderPos by remember(ram) {
                        val idx = pow2Values.indexOfFirst { abs(it - ram) == pow2Values.minOf { v -> abs(v - ram) } }
                        mutableFloatStateOf(idx.coerceAtLeast(0).toFloat())
                    }
                    Slider(
                        value = sliderPos,
                        onValueChange = { newVal ->
                            sliderPos = newVal
                            val idx = newVal.roundToInt().coerceIn(0, pow2Values.size - 1)
                            onRamChange(pow2Values[idx])
                        },
                        valueRange = 0f..(pow2Values.size - 1).toFloat(),
                        steps = (pow2Values.size - 2).coerceAtLeast(0),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // GPUs
        if (gpuOptions.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("GPUs", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "$gpus",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(40.dp),
                    )
                    if (gpuOptions.max() > 0) {
                        Slider(
                            value = gpus.toFloat(),
                            onValueChange = { onGpusChange(it.roundToInt()) },
                            valueRange = 0f..gpuOptions.max().toFloat(),
                            steps = gpuOptions.max() - 1,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
