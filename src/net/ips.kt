package net
import DNS.Record.*
import DNS.Resolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import lib.*
import resources.ip_blacklist

object DNSErrors { val data: Array<Int> = Array(6){0}
	operator fun get(i: Int) = data[i]
	operator fun set(i: Int, e: Int){ data[i] = e }
	override fun toString() = RCode.entries.zip(data).toMap().toString()
}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun lookup() = domains.chunked(Short.MAX_VALUE.toInt())
	.flatMapMerge{ Resolver(1,1,1,1).lookup(it) }
	.handle{ if(it is DNSException) DNSErrors[it.code.ordinal]++ else throw it }
	.flatten()

suspend fun ips() = merge(lookup(), ips, ip_blacklist).distinct()
