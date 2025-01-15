import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import lib.*
import net.*
import resources.IPs.blacklist
import resources.IPs.whitelist
import suspending.SuspendingFile
import java.io.File

fun main(): Unit = runBlocking{
	getData()
	val ports = File("ports").bufferedReader().lineSequence().map(String::toUShort).asFlow()
	val domains = File("domains").bufferedReader().readLines()
	val ipList = toIPs(File("ips").bufferedReader().lineSequence())

	val filter = async{DNSFilter(domains)}
	val args = async{formatCryptnono().joinToString(", ")}
	val portStr = blockRanges(ports)
		.map{"{port: ${it.first}${if(it.first < it.last) ",endPort: ${it.last}" else ""}},"}
		.joinToString()

	val file = SuspendingFile("NetworkPolicy.yml")
	file.write("""
		block:
		  args: [ ${args.await()} ]
		  ports: [${portStr}]
		  ips: [
	""".trimIndent())

	merge(lookupIPs(domains.asFlow()), ipList.asFlow(), blacklist)
		.distinct().filterNot(whitelist::contains)
		.map{it.hostAddress+"/32,"}
		.collect(file::write)

	file.write("]\n  dnsFilter: '.*(${filter.await()})'")
}
