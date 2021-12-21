import kotlin.math.pow

fun main() {
	generateBreaks("hello world i am harnam").forEach { println(it) }
}

fun generateBreaks(input: String): MutableList<MutableList<String>> {
	val inputList = input.split("[ \t]+".toRegex()).toMutableList()
	val breaks = MutableList(2.0.pow(inputList.size - 1).toInt()) { inputList }

	for (i in 0 until 2.0.pow(inputList.size - 1).toInt()) {
		val binaryKey = String.format("%0${inputList.size - 1}d", Integer.toBinaryString(i).toInt()) // BINARY COUNTER VAL
		var offset = 1
		for (cutIndex in binaryKey.indices) {
			if (binaryKey[cutIndex] == '1') { // CUT
				breaks[i] = (breaks[i].subList(0, cutIndex + offset) + mutableListOf(" ") + breaks[i].subList(cutIndex + offset, breaks[i].size)).toMutableList()
				offset++
			}
		}
	}


//	for (i in 0 until 2.0.pow(input.length - 1).toInt()) {
//		val binaryRep = String.format("%0${input.length - 1}d}", Integer.toBinaryString(i).toInt())
//		var spaceCount = 1
//		for (index in binaryRep.indices) {
//			if (binaryRep[index] == '1') {
//				breaks[i] = breaks[i].substring(0, index + spaceCount) + " " + breaks[i].substring(index + spaceCount)
//				spaceCount++
//			}
//		}
//	}

	return breaks
}