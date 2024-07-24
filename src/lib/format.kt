package lib
import kotlinx.coroutines.flow.*
import java.net.InetAddress

fun toIPs(value: List<String>) =
	value.map{it.split('.').map{it.toUByte().toByte()}.toByteArray()}.map(InetAddress::getByAddress).asFlow()

fun sanitize(domains: Flow<String>) = domains.map{dom->dom
	.takeWhile{it!='/'}
	.takeLastWhile{it!='='&&it!='@'}
	.trimEnd('^')
}.distinct()
