package com.chumi.gymapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumi.gymapp.data.formatKcal
import com.chumi.gymapp.data.formatKg
import kotlin.math.roundToInt

/** Colores fijos para cada macro (siempre los mismos en toda la app). */
data class MacroColors(val protein: Color, val carbs: Color, val fat: Color, val fiber: Color)

@Composable
fun macroColors(): MacroColors {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (dark) MacroColors(Color(0xFF3987E5), Color(0xFFC98500), Color(0xFF199E70), Color(0xFF9085E9))
    else MacroColors(Color(0xFF2A78D6), Color(0xFFEDA100), Color(0xFF1BAF7A), Color(0xFF6250D6))
}

fun g(v: Double): String = "${v.roundToInt()} g"

/** Anillo de calorías: consumidas / objetivo. */
@Composable
fun CalorieRing(consumed: Double, target: Double?, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val over = MaterialTheme.colorScheme.error
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(modifier.size(150.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(150.dp)) {
            val stroke = 14.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(track, 135f, 270f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            if (target != null && target > 0) {
                val frac = (consumed / target).toFloat()
                drawArc(
                    if (frac > 1.05f) over else color, 135f, 270f * frac.coerceIn(0f, 1f), false,
                    Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val remaining = target?.let { it - consumed }
            Text(
                if (remaining == null) formatKcal(consumed) else formatKcal(kotlin.math.abs(remaining)),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                when {
                    remaining == null -> "kcal"
                    remaining >= 0 -> "kcal restantes"
                    else -> "kcal de más"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Barra de progreso de un macro con etiqueta y valores. */
@Composable
fun MacroBar(label: String, value: Double, target: Double?, color: Color, modifier: Modifier = Modifier, unit: String = "g") {
    Column(modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text(
                "${value.roundToInt()}" + (target?.let { " / ${it.roundToInt()}" } ?: "") + " $unit",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(4.dp))
        val track = MaterialTheme.colorScheme.surfaceContainerHighest
        Canvas(Modifier.fillMaxWidth().height(8.dp)) {
            val r = CornerRadius(4.dp.toPx())
            drawRoundRect(track, cornerRadius = r)
            if (target != null && target > 0) {
                val frac = (value / target).toFloat().coerceIn(0f, 1f)
                if (frac > 0) drawRoundRect(color, size = Size(size.width * frac, size.height), cornerRadius = r)
            }
        }
    }
}

/** Escala de IMC de colores con un marcador en tu valor. */
@Composable
fun BmiGauge(bmi: Double, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val marker = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surfaceContainer
    val min = 15.0
    val max = 40.0
    // tramos: bajo peso, normal, sobrepeso, obesidad
    val segments = listOf(
        15.0 to 18.5 to Color(0xFF4FC3F7),
        18.5 to 25.0 to Color(0xFF66BB6A),
        25.0 to 30.0 to Color(0xFFFFCA28),
        30.0 to 40.0 to Color(0xFFEF5350),
    )
    Canvas(modifier.fillMaxWidth().height(56.dp)) {
        val barTop = 14.dp.toPx()
        val barH = 12.dp.toPx()
        val gap = 2.dp.toPx()
        fun xOf(v: Double) = (size.width * ((v - min) / (max - min))).toFloat()
        segments.forEach { (range, c) ->
            val x0 = xOf(range.first) + if (range.first > min) gap / 2 else 0f
            val x1 = xOf(range.second) - if (range.second < max) gap / 2 else 0f
            drawRoundRect(c, Offset(x0, barTop), Size(x1 - x0, barH), CornerRadius(4.dp.toPx()))
        }
        listOf(18.5, 25.0, 30.0).forEach { v ->
            val t = measurer.measure("%.1f".format(v).replace(".0", "").replace('.', ','), TextStyle(fontSize = 10.sp, color = labelColor))
            drawText(t, topLeft = Offset(xOf(v) - t.size.width / 2, barTop + barH + 4.dp.toPx()))
        }
        val mx = xOf(bmi.coerceIn(min, max))
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(mx - 7.dp.toPx(), 0f); lineTo(mx + 7.dp.toPx(), 0f); lineTo(mx, barTop - 1.dp.toPx()); close()
        }
        drawPath(path, marker)
        drawLine(surface, Offset(mx, barTop), Offset(mx, barTop + barH), strokeWidth = 4.dp.toPx())
        drawLine(marker, Offset(mx, barTop), Offset(mx, barTop + barH), strokeWidth = 2.dp.toPx())
    }
}

/** Fila resultado: etiqueta a la izquierda, valor grande a la derecha, explicación debajo. */
@Composable
fun ResultRow(label: String, value: String, note: String? = null) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        }
        if (note != null) Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
    }
}
