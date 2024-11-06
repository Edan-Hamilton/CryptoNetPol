import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import lib.*
import net.*
import resources.IPs.blacklist
import resources.IPs.whitelist
import resources.Templates
import suspending.SuspendingFile
import java.io.File
import java.util.Formatter

suspend fun main(): Unit = runBlocking{
	getData()
	val ports = File("ports").bufferedReader().lineSequence().map(String::toUShort).asFlow()
	val domains = File("domains").bufferedReader().readLines()
	val ips = toIPs(File("ips").bufferedReader().lineSequence())

	launch { Formatter("DNSFilter.yml").format(Templates.DNS.readText(),DNSFilter(domains)).close() }

	SuspendingFile("NetworkPolicy.yml").use{file->
		file.append(Templates.cryptnono.readText().format(formatCryptnono()))

		val (head, middle, tail) = Templates.netpol.readText().split("@")

		flow {
			emit(head)
			emitAll(
				blockRanges(ports).map{"{port: ${it.first}${if(it.first < it.last) ",endPort: ${it.last}" else ""}},"}
			)
			emit(middle)
			emitAll(
				merge(lookupIPs(domains.asFlow()), ips.asFlow(), blacklist)
					.distinct().filterNot(whitelist::contains)
					.map{"${it.hostAddress}/32,"}
			)
			emit(tail)
		}.collect(file::append)
	}
}
