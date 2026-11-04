package com.chumi.gymapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumi.gymapp.data.Stats
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

data class ChartPoint(val x: Long, val y: Double)

private val es = Locale("es", "ES")
val shortDate: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", es)
val longDate: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", es)
val mediumDate: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", es)

/** Escala "bonita" para los ejes (1, 2, 2.5, 5, 10...). */
private fun niceStep(range: Double, ticks: Int): Double {
    if (range <= 0) return 1.0
    val raw = range / ticks
    val mag = 10.0.pow(floor(log10(raw)))
    val norm = raw / mag
    val nice = when {
        norm <= 1 -> 1.0
        norm <= 2 -> 2.0
        norm <= 2.5 -> 2.5
        norm <= 5 -> 5.0
        else -> 10.0
    }
    return nice * mag
}

private fun axisLabel(v: Double): String = when {
    abs(v) >= 1_000_000 -> "%.1fM".format(v / 1_000_000).replace(".0M", "M")
    abs(v) >= 10_000 -> "%.0fk".format(v / 1000)
    abs(v) >= 1000 -> "%.1fk".format(v / 1000).replace(".0k", "k")
    v % 1.0 == 0.0 -> v.toLong().toString()
    else -> "%.1f".format(v)
}

/**
 * Gráfico de línea con selección táctil: al tocar o arrastrar se muestra el valor
 * del punto más cercano en la cabecera (sin tapar el gráfico con el dedo).
 */
@Composable
fun LineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    valueText: (Double) -> String = { axisLabel(it) },
    height: Int = 200,
) {
    if (points.isEmpty()) {
        EmptyChart(modifier, height); return
    }
    val sorted = remember(points) { points.sortedBy { it.x } }
    var selected by remember(points) { mutableStateOf(sorted.lastIndex) }
    val measurer = rememberTextMeasurer()
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val surface = MaterialTheme.colorScheme.surfaceContainer
    val labelStyle = TextStyle(fontSize = 10.sp, color = axisColor)

    val sel = sorted[selected.coerceIn(0, sorted.lastIndex)]
    Column(modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(valueText(sel.y), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(8.dp))
            Text(
                Stats.toDate(sel.x).format(mediumDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
        Spacer(Modifier.height(8.dp))

        val minY0 = sorted.minOf { it.y }
        val maxY0 = sorted.maxOf { it.y }
        val pad = if (maxY0 == minY0) (if (maxY0 == 0.0) 1.0 else abs(maxY0) * 0.1) else (maxY0 - minY0) * 0.1
        val step = niceStep((maxY0 + pad) - (minY0 - pad), 4)
        val minY = floor((minY0 - pad).coerceAtLeast(if (minY0 >= 0) 0.0 else Double.NEGATIVE_INFINITY) / step) * step
        val maxY = ceil((maxY0 + pad) / step) * step
        val minX = sorted.first().x
        val maxX = sorted.last().x

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(height.dp)
                .pointerInput(sorted) {
                    val leftPad = 40.dp.toPx()
                    fun pick(px: Float) {
                        val w = size.width - leftPad - 8.dp.toPx()
                        val idx = sorted.indices.minByOrNull { i ->
                            val x = if (maxX == minX) leftPad + w / 2 else leftPad + w * (sorted[i].x - minX).toFloat() / (maxX - minX)
                            abs(x - px)
                        } ?: 0
                        selected = idx
                    }
                    detectTapGestures { pick(it.x) }
                }
                .pointerInput(sorted) {
                    val leftPad = 40.dp.toPx()
                    detectDragGestures { change, _ ->
                        val w = size.width - leftPad - 8.dp.toPx()
                        val px = change.position.x
                        selected = sorted.indices.minByOrNull { i ->
                            val x = if (maxX == minX) leftPad + w / 2 else leftPad + w * (sorted[i].x - minX).toFloat() / (maxX - minX)
                            abs(x - px)
                        } ?: 0
                    }
                },
        ) {
            val leftPad = 40.dp.toPx()
            val rightPad = 8.dp.toPx()
            val bottomPad = 20.dp.toPx()
            val topPad = 6.dp.toPx()
            val w = size.width - leftPad - rightPad
            val h = size.height - bottomPad - topPad
            fun xOf(x: Long) = if (maxX == minX) leftPad + w / 2 else leftPad + w * (x - minX).toFloat() / (maxX - minX)
            fun yOf(y: Double) = topPad + h * (1 - ((y - minY) / (maxY - minY)).toFloat())

            // Rejilla y etiquetas del eje Y
            var t = minY
            while (t <= maxY + step / 2) {
                val y = yOf(t)
                drawLine(gridColor, Offset(leftPad, y), Offset(size.width - rightPad, y), strokeWidth = 1.dp.toPx())
                val lbl = measurer.measure(axisLabel(t), labelStyle)
                drawText(lbl, topLeft = Offset(leftPad - lbl.size.width - 6.dp.toPx(), y - lbl.size.height / 2))
                t += step
            }
            // Etiquetas del eje X (inicio / fin)
            val first = measurer.measure(Stats.toDate(minX).format(shortDate), labelStyle)
            drawText(first, topLeft = Offset(leftPad, size.height - first.size.height))
            if (maxX != minX) {
                val last = measurer.measure(Stats.toDate(maxX).format(shortDate), labelStyle)
                drawText(last, topLeft = Offset(size.width - rightPad - last.size.width, size.height - last.size.height))
            }

            // Área y línea
            val line = Path()
            sorted.forEachIndexed { i, p -> if (i == 0) line.moveTo(xOf(p.x), yOf(p.y)) else line.lineTo(xOf(p.x), yOf(p.y)) }
            if (sorted.size > 1) {
                val area = Path().apply {
                    addPath(line)
                    lineTo(xOf(sorted.last().x), topPad + h)
                    lineTo(xOf(sorted.first().x), topPad + h)
                    close()
                }
                drawPath(area, Brush.verticalGradient(listOf(color.copy(alpha = 0.25f), Color.Transparent), startY = topPad, endY = topPad + h))
                drawPath(line, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }

            // Cruz de selección
            val sx = xOf(sel.x)
            drawLine(axisColor.copy(alpha = 0.5f), Offset(sx, topPad), Offset(sx, topPad + h), strokeWidth = 1.dp.toPx())

            // Marcadores
            if (sorted.size <= 40) sorted.forEach { p ->
                drawCircle(surface, radius = 5.dp.toPx(), center = Offset(xOf(p.x), yOf(p.y)))
                drawCircle(color, radius = 3.5.dp.toPx(), center = Offset(xOf(p.x), yOf(p.y)))
            }
            drawCircle(surface, radius = 8.dp.toPx(), center = Offset(sx, yOf(sel.y)))
            drawCircle(color, radius = 6.dp.toPx(), center = Offset(sx, yOf(sel.y)))
        }
    }
}

data class BarItem(val label: String, val value: Double, val detail: String = label)

/** Gráfico de barras verticales con selección táctil. */
@Composable
fun BarChart(
    bars: List<BarItem>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    valueText: (Double) -> String = { axisLabel(it) },
    height: Int = 180,
    labelEvery: Int = 1,
    target: Double? = null,
    targetLabel: String = "Objetivo",
) {
    if (bars.isEmpty()) {
        EmptyChart(modifier, height); return
    }
    var selected by remember(bars) { mutableStateOf(bars.lastIndex) }
    val measurer = rememberTextMeasurer()
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val labelStyle = TextStyle(fontSize = 10.sp, color = axisColor)
    val sel = bars[selected.coerceIn(0, bars.lastIndex)]

    Column(modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(valueText(sel.value), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(8.dp))
            Text(
                sel.detail, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        val maxV = maxOf(bars.maxOf { it.value }, target ?: 0.0).coerceAtLeast(1.0)
        val step = niceStep(maxV, 4)
        val top = ceil(maxV / step) * step

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(height.dp)
                .pointerInput(bars) {
                    val leftPad = 40.dp.toPx()
                    fun pick(px: Float) {
                        val slot = (size.width - leftPad) / bars.size
                        selected = ((px - leftPad) / slot).toInt().coerceIn(0, bars.lastIndex)
                    }
                    detectTapGestures { pick(it.x) }
                }
                .pointerInput(bars) {
                    val leftPad = 40.dp.toPx()
                    detectDragGestures { change, _ ->
                        val slot = (size.width - leftPad) / bars.size
                        selected = ((change.position.x - leftPad) / slot).toInt().coerceIn(0, bars.lastIndex)
                    }
                },
        ) {
            val leftPad = 40.dp.toPx()
            val bottomPad = 20.dp.toPx()
            val topPad = 6.dp.toPx()
            val h = size.height - bottomPad - topPad
            val w = size.width - leftPad
            fun yOf(v: Double) = topPad + h * (1 - (v / top).toFloat())

            var t = 0.0
            while (t <= top + step / 2) {
                val y = yOf(t)
                drawLine(gridColor, Offset(leftPad, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                val lbl = measurer.measure(axisLabel(t), labelStyle)
                drawText(lbl, topLeft = Offset(leftPad - lbl.size.width - 6.dp.toPx(), y - lbl.size.height / 2))
                t += step
            }
            val slot = w / bars.size
            val gap = 2.dp.toPx().coerceAtLeast(slot * 0.2f)
            val targetY = target?.let { yOf(it) }
            val barW = slot - gap
            val r = 4.dp.toPx().coerceAtMost(barW / 2)
            bars.forEachIndexed { i, b ->
                val x = leftPad + i * slot + gap / 2
                val y = yOf(b.value)
                val c = if (i == selected) color else color.copy(alpha = 0.55f)
                if (b.value > 0) {
                    val barH = topPad + h - y
                    // barra con esquinas redondeadas arriba, base recta
                    drawRoundRect(c, Offset(x, y), Size(barW, barH), CornerRadius(r, r))
                    if (barH > r) drawRect(c, Offset(x, y + barH - r), Size(barW, r))
                }
                if (i % labelEvery == 0 || i == bars.lastIndex) {
                    val lbl = measurer.measure(b.label, labelStyle)
                    if (i % labelEvery == 0) drawText(lbl, topLeft = Offset(x + barW / 2 - lbl.size.width / 2, size.height - lbl.size.height))
                }
            }
            if (targetY != null) {
                drawLine(
                    axisColor, Offset(leftPad, targetY), Offset(size.width, targetY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 5.dp.toPx())),
                )
                val tl = measurer.measure(targetLabel, labelStyle)
                drawText(tl, topLeft = Offset(size.width - tl.size.width, targetY - tl.size.height - 2.dp.toPx()))
            }
        }
    }
}

/** Lista de barras horizontales (p. ej. series por grupo muscular). */
@Composable
fun HorizontalBars(items: List<Pair<String, Double>>, valueText: (Double) -> String, modifier: Modifier = Modifier) {
    val maxV = items.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0
    val color = MaterialTheme.colorScheme.primary
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (label, v) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(110.dp), maxLines = 1)
                Box(Modifier.weight(1f).height(14.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth((v / maxV).toFloat().coerceIn(0.02f, 1f))
                            .height(14.dp)
                            .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                            .background(color),
                    )
                }
                Text(
                    valueText(v), style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(56.dp).padding(start = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Calendario tipo "mapa de calor" con los días entrenados de las últimas semanas. */
@Composable
fun TrainingHeatmap(
    volumeByDay: Map<LocalDate, Double>,
    weeks: Int = 17,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.surfaceContainerHighest
    val today = LocalDate.now()
    val start = Stats.weekStart(today).minusWeeks((weeks - 1).toLong())
    val maxV = volumeByDay.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    var selected by remember { mutableStateOf<LocalDate?>(null) }
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    val dayNames = listOf("L", "M", "X", "J", "V", "S", "D")

    Column(modifier) {
        val s = selected
        Text(
            if (s == null) "Toca un día para ver el detalle"
            else {
                val v = volumeByDay[s]
                s.format(mediumDate) + if (v == null) " · descanso" else " · ${axisLabel(v)} kg"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(130.dp)
                .pointerInput(weeks) {
                    detectTapGestures { pos ->
                        val left = 16.dp.toPx()
                        val cell = ((size.width - left) / weeks).coerceAtMost(size.height / 7f)
                        val col = ((pos.x - left) / cell).toInt()
                        val row = (pos.y / cell).toInt()
                        if (col in 0 until weeks && row in 0..6) {
                            val d = start.plusWeeks(col.toLong()).plusDays(row.toLong())
                            selected = if (d.isAfter(today)) null else d
                        }
                    }
                },
        ) {
            val left = 16.dp.toPx()
            val cell = ((size.width - left) / weeks).coerceAtMost(size.height / 7f)
            val gap = 2.dp.toPx()
            for (row in 0..6) {
                if (row % 2 == 0) {
                    val l = measurer.measure(dayNames[row], labelStyle)
                    drawText(l, topLeft = Offset(0f, row * cell + cell / 2 - l.size.height / 2))
                }
            }
            for (col in 0 until weeks) for (row in 0..6) {
                val d = start.plusWeeks(col.toLong()).plusDays(row.toLong())
                if (d.isAfter(today)) continue
                val v = volumeByDay[d]
                val c = if (v == null) empty else color.copy(alpha = (0.35f + 0.65f * (v / maxV).toFloat()).coerceIn(0.35f, 1f))
                drawRoundRect(
                    c, Offset(left + col * cell + gap / 2, row * cell + gap / 2),
                    Size(cell - gap, cell - gap), CornerRadius(3.dp.toPx()),
                )
                if (d == selected) drawRoundRect(
                    Color.White.copy(alpha = 0.9f), Offset(left + col * cell + gap / 2, row * cell + gap / 2),
                    Size(cell - gap, cell - gap), CornerRadius(3.dp.toPx()), style = Stroke(1.5.dp.toPx()),
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Text("Menos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            listOf(0f, 0.35f, 0.6f, 0.8f, 1f).forEach { a ->
                Box(
                    Modifier.padding(horizontal = 2.dp).size(10.dp).clip(RoundedCornerShape(2.dp))
                        .background(if (a == 0f) empty else color.copy(alpha = a)),
                )
            }
            Text("Más volumen", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyChart(modifier: Modifier, height: Int) {
    Box(modifier.fillMaxWidth().height(height.dp), contentAlignment = Alignment.Center) {
        Text(
            "Aún no hay datos. ¡Entrena y aquí verás tu progreso!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
