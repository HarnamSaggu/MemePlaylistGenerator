import com.adamratzman.spotify.endpoints.pub.SearchApi
import com.adamratzman.spotify.models.Track
import com.adamratzman.spotify.spotifyAppApi
import com.adamratzman.spotify.utils.Language
import java.io.File
import java.io.InputStream
import kotlin.math.min
import kotlin.math.pow

suspend fun main() {
	println("Enter sentence:")
	val inputSentence = readLine() ?: return
	if (inputSentence.isEmpty()) return
	val combinations = generateBreaks(inputSentence)

	combinations.forEach { println(it) }

	val inputStream: InputStream = File("C:/Users/theto/OneDrive/Desktop/spotify api keys.txt").inputStream()
	val lineList = mutableListOf<String>()
	inputStream.bufferedReader().forEachLine { lineList.add(it) }
	val clientId = lineList[0]
	val clientSecret = lineList[1]
	val api = spotifyAppApi(clientId, clientSecret).build()

	val playlists = mutableListOf<MutableList<Track>>()
	var count = 0
	combinationLoop@ for (phrases in combinations) {
		val tracks = mutableListOf<Track>()
		for (phrase in phrases) {
			try {
				val found = SearchApi(api).searchTrack(phrase).first { phrase.equals(it?.name, ignoreCase = true) }
				if (found != null) tracks.add(found)
			} catch (e: NoSuchElementException) {
				try {
					val found = SearchApi(api).searchTrack(phrase).minByOrNull { if (it != null) levenshtein(it.name, phrase) else Int.MAX_VALUE }
					if (found != null) tracks.add(found)
				} catch (e: NoSuchElementException) {
					continue@combinationLoop
				}
			}
		}
		playlists.add(tracks)
		count++
		println("List #${count} ".padEnd(112, '='))
		tracks.forEach { x ->
			println("\t${String.format("%-30s", x.name)}${String.format("%-70s", x.externalUrls.spotify)}${x.artists.map { y -> y.name }}")
		}
		println("================================================================================================================\n")
	}

	val fullStringList = mutableListOf<String>()
	var bestSoFarScore = Int.MAX_VALUE
	var bestSoFarIndex = 0
	for (index in playlists.indices) {
		fullStringList.add(playlists[index].joinToString(" "))
		if (levenshtein(fullStringList.last(), inputSentence) <= bestSoFarScore) {
			bestSoFarScore = levenshtein(fullStringList.last(), inputSentence)
			bestSoFarIndex = index
		}
	}

	println("List BEST ".padEnd(112, '='))
	playlists[bestSoFarIndex].forEach { x ->
		println("\t${String.format("%-30s", x.name)}${String.format("%-70s", x.externalUrls.spotify)}${x.artists.map { y -> y.name }}")
	}
	println("================================================================================================================\n")

//	playlists.forEach {
//		println("List #${playlists.indexOf(it) + 1}")
//		it.forEach { x ->
//			println("\t${String.format("%-20s", x.name)}${String.format("%-70s", x.href)}${x.artists.map { y -> y.name }}")
//		}
//		println("===========\n")
//	}
}

fun generateBreaks(input: String): MutableList<MutableList<String>> {
	val inputList = input.split("[ \t]+".toRegex()).toMutableList()
	if (inputList.size <= 1) return mutableListOf(inputList)

	val breaks = MutableList(2.0.pow(inputList.size - 1).toInt()) { inputList.indices.toMutableList() }

	for (i in 0 until 2.0.pow(inputList.size - 1).toInt()) {
//		val binaryKey = String.format("%0${inputList.size - 1}d", Integer.toBinaryString(i).toInt())
		val binaryKey = Integer.toBinaryString(i).padStart(inputList.size - 1, '0')
		var offset = 1
		for (cutIndex in binaryKey.indices) {
			if (binaryKey[cutIndex] == '1') {
				breaks[i] = (breaks[i].subList(0, cutIndex + offset) + mutableListOf(-1) + breaks[i].subList(cutIndex + offset, breaks[i].size)).toMutableList()
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

fun levenshtein(lhs : CharSequence, rhs : CharSequence) : Int {
	val lhsLength = lhs.length + 1
	val rhsLength = rhs.length + 1

	var cost = Array(lhsLength) { it }
	var newCost = Array(lhsLength) { 0 }

	for (i in 1 until rhsLength) {
		newCost[0] = i

		for (j in 1 until lhsLength) {
			val match = if(lhs[j - 1] == rhs[i - 1]) 0 else 1

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