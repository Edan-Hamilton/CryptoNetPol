import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import lib.*
import net.*
import resources.IPs.blacklist
import resources.IPs.whitelist
import resources.Templates
import java.io.File
import java.util.Formatter

suspend fun main(): Unit = runBlocking{
	getData()
	val ports = File("ports").bufferedReader().lineSequence().map(String::toUShort).asFlow()
	val domains = File("domains").bufferedReader().readLines()
	val ipList = toIPs(File("ips").bufferedReader().lineSequence())

	val filter = async{DNSFilter(domains)}
	val args = async{formatCryptnono()}

	val ips = async{
		merge(lookupIPs(domains.asFlow()), ipList.asFlow(), blacklist)
		.distinct().filterNot(whitelist::contains)
		.map{"${it.hostAddress}/32,"}
		.joinToString()
	}

	val portStr = blockRanges(ports)
		.map{"{port: ${it.first}${if(it.first < it.last) ",endPort: ${it.last}" else ""}},"}
		.joinToString()

	Formatter("NetworkPolicy.yml")
		.format("dnsFilter: '%s'\n", filter.await())
		.format(Templates.cryptnono.readText(),args.await())
		.format(Templates.netpol.readText(),portStr,ips)
		.close()
}
