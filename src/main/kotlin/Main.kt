import kotlin.math.pow

fun main() {
	generateBreaks("abcde").forEach { println(it) }
}

fun generateBreaks(input: String): MutableList<String> {
	val breaks = MutableList(2.0.pow(input.length - 1).toInt()) { input }
	for (i in 0 until 2.0.pow(input.length - 1).toInt()) {
		val binaryRep = String.format("%0${input.length - 1}d}", Integer.toBinaryString(i).toInt())
		var spaceCount = 1
		for (index in binaryRep.indices) {
			if (binaryRep[index] == '1') {
				breaks[i] = breaks[i].substring(0, index + spaceCount) + " " + breaks[i].substring(index + spaceCount)
				spaceCount++
			}
		}
	}

	return breaks
}