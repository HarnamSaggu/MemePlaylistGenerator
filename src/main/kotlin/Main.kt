import com.adamratzman.spotify.endpoints.pub.SearchApi
import com.adamratzman.spotify.endpoints.pub.TrackApi
import com.adamratzman.spotify.spotifyAppApi
import java.io.File
import java.io.InputStream
import kotlin.math.pow


suspend fun main() {
//	generateBreaks("a b c d e f").forEach { println(it) }

	val inputStream: InputStream = File("C:/Users/theto/OneDrive/Desktop/spotify api keys.txt").inputStream()
	val lineList = mutableListOf<String>()

	inputStream.bufferedReader().forEachLine { lineList.add(it) }
	val clientId = lineList[0]
	val clientSecret = lineList[1]
	val query = "karma police"

	val api = spotifyAppApi(clientId, clientSecret).build()
	SearchApi(api).searchTrack(query).forEach {
		if (it != null) {
			println("ITEM: ${it.name}\n\t${it.artists[0].name}")
		}
	}
}

fun generateBreaks(input: String): MutableList<MutableList<String>> {
	val inputList = input.split("[ \t]+".toRegex()).toMutableList()
	if (inputList.size <= 1) return mutableListOf(inputList)

	val breaks = MutableList(2.0.pow(inputList.size - 1).toInt()) { inputList.indices.toMutableList() }

	for (i in 0 until 2.0.pow(inputList.size - 1).toInt()) {
		val binaryKey = String.format("%0${inputList.size - 1}d", Integer.toBinaryString(i).toInt())
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