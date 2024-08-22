package format
import lib.*
import net.algos
import resources.*
import java.io.File

suspend fun writeCryptnono(){
	Templates.cryptnono.copyTo(File("cryptnono.yml"),true).appendWriter().use{file->
		algos().toMutableSet().apply{removeAll(cryptnono_allows)}
			.map(String::asIterable).toCollection(Trie()).asSequence()
			.map{it.joinToString("","        - ")}
			.forEach(file::appendLine)
	}
}
