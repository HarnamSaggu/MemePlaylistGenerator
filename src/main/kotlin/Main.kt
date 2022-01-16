import com.adamratzman.spotify.endpoints.pub.SearchApi
import com.adamratzman.spotify.models.Track
import com.adamratzman.spotify.spotifyAppApi
import java.io.File
import java.io.InputStream
import kotlin.math.pow

suspend fun main() {
	println("Enter sentence:")
	val inputSentence = readLine() ?: return
	if (inputSentence.isEmpty()) return
	val combinations = generateBreaks(inputSentence)

	val inputStream: InputStream = File("C:/Users/theto/OneDrive/Desktop/spotify api keys.txt").inputStream()
	val lineList = mutableListOf<String>()
	inputStream.bufferedReader().forEachLine { lineList.add(it) }
	val clientId = lineList[0]
	val clientSecret = lineList[1]
	val api = spotifyAppApi(clientId, clientSecret).build()

	var count = 1
	var bestPlaylistScore = Int.MAX_VALUE
	combinationLoop@ for (phrases in combinations) {
		val tracks = mutableListOf<Track>()
		for (phrase in phrases) {
			try {
				val found = SearchApi(api).searchTrack(phrase).first { phrase.equals(it?.name, ignoreCase = true) }
				if (found != null) tracks.add(found)
			} catch (e: NoSuchElementException) {
				try {
					val found = SearchApi(api).searchTrack(phrase)
						.minByOrNull { if (it != null) levenshtein(it.name, phrase) else Int.MAX_VALUE }
					if (found != null) tracks.add(found)
				} catch (e: NoSuchElementException) {
					continue@combinationLoop
				}
			}
		}

		val phraseScore = levenshtein(tracks.joinToString(" ") { it.name }, inputSentence)
		if (phraseScore <= bestPlaylistScore) {
			bestPlaylistScore = phraseScore
			println("List #${count++}")
			tracks.forEach { x ->
				println(
					"\t${String.format("%-20s", x.name)}\t${
						String.format(
							"%-60s",
							x.externalUrls.spotify
						)
					}\t${x.artists.map { y -> y.name }}"
				)
			}
			println("\n\n")
		}
	}
}

fun generateBreaks(input: String): MutableList<MutableList<String>> {
	val inputList = input.split("[ \t]+".toRegex()).toMutableList()
	if (inputList.size <= 1) return mutableListOf(inputList)

	val breaks = MutableList(2.0.pow(inputList.size - 1).toInt()) { inputList.indices.toMutableList() }

	for (i in 0 until 2.0.pow(inputList.size - 1).toInt()) {
		val binaryKey = Integer.toBinaryString(i).padStart(inputList.size - 1, '0')
		var offset = 1
		for (cutIndex in binaryKey.indices) {
			if (binaryKey[cutIndex] == '1') {
				breaks[i] = (breaks[i].subList(0, cutIndex + offset) + mutableListOf(-1) + breaks[i].subList(
					cutIndex + offset,
					breaks[i].size
				)).toMutableList()
				offset++
			}
		}
	}

	val stringBreaks = MutableList(breaks.size) { mutableListOf("") }
	for (i in breaks.indices) {
		var nextIndex = 0
		for (index in breaks[i]) {
			if (index == -1) {
				stringBreaks[i][nextIndex] = stringBreaks[i][nextIndex].substring(0, stringBreaks[i][nextIndex].length - 1)
				nextIndex++
				stringBreaks[i].add("")
			} else {
				stringBreaks[i][nextIndex] += inputList[index] + " "
			}
		}
		stringBreaks[i][nextIndex] = stringBreaks[i][nextIndex].substring(0, stringBreaks[i][nextIndex].length - 1)
	}

	stringBreaks.reverse()
	return stringBreaks
}

// https://gist.github.com/ademar111190/34d3de41308389a0d0d8
// CREDIT GOES TO ademar111190
// THANKS
fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
	val lhsLength = lhs.length + 1
	val rhsLength = rhs.length + 1

	var cost = Array(lhsLength) { it }
	var newCost = Array(lhsLength) { 0 }

	for (i in 1 until rhsLength) {
		newCost[0] = i

		for (j in 1 until lhsLength) {
			val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

			val costReplace = cost[j - 1] + match
			val costInsert = cost[j] + 1
			val costDelete = newCost[j - 1] + 1

			newCost[j] = costInsert.coerceAtMost(costDelete).coerceAtMost(costReplace)
		}

		val swap = cost
		cost = newCost
		newCost = swap
	}

	return cost[lhsLength - 1]
}