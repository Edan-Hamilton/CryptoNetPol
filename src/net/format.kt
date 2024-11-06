package net
import kotlinx.coroutines.flow.*
import lib.distinct
import java.net.InetAddress

fun toIPs(value: Sequence<String>) =
	value.map{it.split('.').map{it.toUByte().toByte()}.toByteArray()}.map(InetAddress::getByAddress)

fun sanitize(domains: Flow<String>) = domains.map{dom->dom
	.takeWhile{it!='/'}
	.takeLastWhile{it!='='&&it!='@'}
	.trimEnd('^')
}.distinct()
