package format
import collections.sorted
import kotlinx.coroutines.flow.*
import net.ports
import resources.Ports.blacklist
import resources.Ports.whitelist

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

fun blockRanges() = merge(ports,blacklist).sorted().distinctUntilChanged().filterNot(whitelist::contains)
	.toRanges().cut(1,UShort.MAX_VALUE.toInt())
