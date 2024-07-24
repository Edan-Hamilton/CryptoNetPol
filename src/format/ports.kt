package format
import lib.sorted
import kotlinx.coroutines.flow.*
import net.ports
import resources.Ports.blacklist
import resources.Ports.whitelist

private fun Flow<UShort>.toRanges()= flow {
	var x: UShort?=null; var y: UShort?=null
	collect{ if(y!=it.dec()){ x?.let{x->emit(x..y!!)}; x=it }; y=it }
	emit(x!!..y!!)
}

private fun Flow<UIntRange>.cut(min: UInt, max: UInt) = flow {
	var x = min
	collect{ emit(x..<it.first); x = it.last.inc() }
	emit(x..max)
}

fun blockRanges() = merge(ports,blacklist).sorted().distinctUntilChanged().filterNot(whitelist::contains)
	.toRanges().cut(1u,UShort.MAX_VALUE.toUInt())
