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
	val ipList = toIPs(File("ips").bufferedReader().lineSequence())

	val filter = async{DNSFilter(domains)}
	val args = async{formatCryptnono().joinToString(", ")}

	val portStr = blockRanges(ports)
		.map{"{port: ${it.first}${if(it.first < it.last) ",endPort: ${it.last}" else ""}},"}
		.joinToString()

	Formatter("NetworkPolicy.yml")
		.format(Templates.cryptnono.readText(),args.await())
		.format("dnsFilter: '.*(%s)'\n", filter.await())
		.format(Templates.netpol.readText(),portStr)
		.close()

	SuspendingFile("NetworkPolicy.yml").apply{position(size())}.use{file->
		file.write("[")
		merge(lookupIPs(domains.asFlow()), ipList.asFlow(), blacklist)
			.distinct().filterNot(whitelist::contains)
			.map{it.hostAddress+"/32,"}
			.collect(file::write)
		file.write("]")
	}
}
