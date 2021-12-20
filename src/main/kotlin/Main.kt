import kotlin.math.pow

fun main() {
	generateBreaks("abcde").forEach { println(it) }
}

fun generateBreaks(input: String): MutableList<String> {
	val breaks = MutableList(2.0.pow(input.length - 1).toInt()) { input }
	for (i in 0 until 2.0.pow(input.length - 1).toInt()) {
		val binaryRep = String.format("%03d", Integer.toBinaryString(i).toInt())
		println(binaryRep)
		var spaceCount = 1
		var newBreak = input
		for (index in binaryRep.indices) {
			if (binaryRep[index] == '1') {
				newBreak = newBreak.substring(0, index + spaceCount) + " " + newBreak.substring(index + spaceCount)
				spaceCount++
			}
		}
		breaks[i] = newBreak
	}

	return breaks
}