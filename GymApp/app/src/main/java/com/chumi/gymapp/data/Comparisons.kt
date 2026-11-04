package com.chumi.gymapp.data

import kotlin.math.abs
import kotlin.math.roundToInt

/** Algo que pesa lo mismo que lo que has levantado. Pesos aproximados. */
data class WeightThing(
    val singular: String,
    val plural: String,
    val article: String, // "un" / "una"
    val kg: Double,
    val emoji: String,
)

data class Comparison(
    val thing: WeightThing,
    val count: Double,
) {
    /** "un bebé tigre", "2 caballos", "3,5 elefantes africanos" */
    val phrase: String
        get() {
            val rounded = roundCount(count)
            return if (rounded == 1.0) "${thing.article} ${thing.singular}"
            else "${formatCount(rounded)} ${thing.plural}"
        }

    val isExact: Boolean get() = abs(count - roundCount(count)) < 0.08 * count.coerceAtLeast(1.0)

    companion object {
        fun roundCount(c: Double): Double = when {
            c < 10 -> (c * 2).roundToInt() / 2.0 // medios: 1, 1,5, 2...
            else -> c.roundToInt().toDouble()
        }

        fun formatCount(c: Double): String =
            if (c % 1.0 == 0.0) "%,d".format(c.toLong()).replace(',', '.')
            else "%.1f".format(c).replace('.', ',')
    }
}

object Comparisons {

    val things: List<WeightThing> = listOf(
        WeightThing("móvil Pixel 9a", "móviles Pixel 9a", "un", 0.186, "📱"),
        WeightThing("piña", "piñas", "una", 1.0, "🍍"),
        WeightThing("pollo", "pollos", "un", 2.5, "🐔"),
        WeightThing("gato", "gatos", "un", 4.5, "🐈"),
        WeightThing("bola de bolos", "bolas de bolos", "una", 7.0, "🎳"),
        WeightThing("sandía gigante", "sandías gigantes", "una", 10.0, "🍉"),
        WeightThing("perro beagle", "perros beagle", "un", 13.0, "🐶"),
        WeightThing("niño de 5 años", "niños de 5 años", "un", 18.0, "🧒"),
        WeightThing("saco de cemento", "sacos de cemento", "un", 25.0, "🧱"),
        WeightThing("labrador", "labradores", "un", 32.0, "🐕"),
        WeightThing("lobo", "lobos", "un", 45.0, "🐺"),
        WeightThing("canguro rojo", "canguros rojos", "un", 66.0, "🦘"),
        WeightThing("persona adulta", "personas adultas", "una", 75.0, "🧍"),
        WeightThing("panda gigante", "pandas gigantes", "un", 100.0, "🐼"),
        WeightThing("bebé tigre", "bebés tigre", "un", 130.0, "🐯"),
        WeightThing("león adulto", "leones adultos", "un", 190.0, "🦁"),
        WeightThing("moto deportiva", "motos deportivas", "una", 200.0, "🏍️"),
        WeightThing("oso pardo", "osos pardos", "un", 300.0, "🐻"),
        WeightThing("piano de cola", "pianos de cola", "un", 450.0, "🎹"),
        WeightThing("caballo", "caballos", "un", 550.0, "🐎"),
        WeightThing("vaca", "vacas", "una", 700.0, "🐄"),
        WeightThing("coche pequeño", "coches pequeños", "un", 950.0, "🚗"),
        WeightThing("hipopótamo", "hipopótamos", "un", 1500.0, "🦛"),
        WeightThing("todoterreno", "todoterrenos", "un", 2200.0, "🚙"),
        WeightThing("rinoceronte blanco", "rinocerontes blancos", "un", 2300.0, "🦏"),
        WeightThing("orca", "orcas", "una", 4000.0, "🐋"),
        WeightThing("elefante africano", "elefantes africanos", "un", 6000.0, "🐘"),
        WeightThing("Tyrannosaurus rex", "Tyrannosaurus rex", "un", 8000.0, "🦖"),
        WeightThing("autobús", "autobuses", "un", 12000.0, "🚌"),
        WeightThing("camión de bomberos", "camiones de bomberos", "un", 19000.0, "🚒"),
        WeightThing("ballena jorobada", "ballenas jorobadas", "una", 30000.0, "🐳"),
        WeightThing("avión Boeing 737", "aviones Boeing 737", "un", 41000.0, "✈️"),
        WeightThing("tanque", "tanques", "un", 60000.0, "🪖"),
        WeightThing("ballena azul", "ballenas azules", "una", 150000.0, "🐳"),
        WeightThing("Estatua de la Libertad", "Estatuas de la Libertad", "una", 204000.0, "🗽"),
        WeightThing("Boeing 747 cargado", "Boeing 747 cargados", "un", 400000.0, "🛫"),
        WeightThing("transbordador espacial", "transbordadores espaciales", "un", 2000000.0, "🚀"),
        WeightThing("Torre Eiffel", "Torres Eiffel", "una", 7300000.0, "🗼"),
    )

    /**
     * Elige el objeto con el que la comparación queda más "redonda":
     * se prefieren cantidades pequeñas (1–5) y cercanas a un número entero o medio.
     */
    fun compare(kg: Double): Comparison? {
        if (kg <= 0.0) return null
        var best: Comparison? = null
        var bestScore = Double.MAX_VALUE
        for (t in things) {
            val count = kg / t.kg
            if (count < 0.9) continue
            val rounded = Comparison.roundCount(count)
            val error = abs(count - rounded) / count // error relativo
            // penaliza cantidades grandes: "1 bebé tigre" mejor que "13 labradores"
            val sizePenalty = when {
                count <= 1.15 -> 0.0
                count <= 3.0 -> 0.03
                count <= 6.0 -> 0.07
                count <= 12.0 -> 0.15
                else -> 0.3 + count / 1000.0
            }
            val score = error + sizePenalty
            if (score < bestScore) {
                bestScore = score
                best = Comparison(t, count)
            }
        }
        return best
    }

    /** "Hoy has levantado 130 kg en total, lo mismo que un bebé tigre 🐯" */
    fun sentence(kg: Double, prefix: String = "Has levantado"): String {
        val c = compare(kg) ?: return "$prefix ${formatKg(kg)} kg en total. ¡A por más!"
        return "$prefix ${formatKg(kg)} kg en total, ${c.verb()} ${c.phrase} ${c.thing.emoji}"
    }

    private fun Comparison.verb(): String {
        val plural = Comparison.roundCount(count) != 1.0
        val pesa = if (plural) "pesan" else "pesa"
        return if (isExact) "lo mismo que $pesa" else "más o menos lo que $pesa"
    }
}

fun formatKg(kg: Double): String {
    return if (kg >= 1000 || kg % 1.0 == 0.0) "%,d".format(kg.toLong()).replace(',', '.')
    else "%.1f".format(kg).replace('.', ',')
}

fun formatWeight(kg: Double): String =
    if (kg % 1.0 == 0.0) kg.toLong().toString() else "%.2f".format(kg).trimEnd('0').trimEnd('.', ',').replace('.', ',')

/** Calorías siempre sin decimales: "1.250". */
fun formatKcal(kcal: Double): String = formatKg(Math.round(kcal).toDouble())
