package format
import collections.sorted
import kotlinx.coroutines.flow.*
import net.ports
import java.io.File

private fun Flow<Int>.toRanges()= flow {
	var x: Int?=null; var y: Int?=null
	collect{ if(y!=it-1){ x?.let{x->emit(x..y!!)}; x=it }; y=it }
	emit(x!!..y!!)
}

private fun Flow<IntRange>.cut(min: Int, max: Int) = flow {
	var x = min
	collect{ emit(x..<it.first); x = it.last + 1 }
	emit(x..max)
}

private fun portFile(name: String) = File(ClassLoader.getSystemResource(name).file).readLines().map(String::toInt)

val white = portFile("port_whitelist").toSet()
val black = portFile("ports").asFlow()

fun blockRanges() = merge(ports.sorted().distinctUntilChanged().filterNot(white::contains), black)
	.toRanges().cut(1,UShort.MAX_VALUE.toInt())
