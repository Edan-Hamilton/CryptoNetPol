package format
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.ips
import java.io.File

suspend fun writePolicy() = File("NetworkPolicy.yml").bufferedWriter()
	.use{file->policy.collect(file::appendLine)}


private var policy = flow{coroutineScope{
	emit("""
		- op: add
		  path: /spec/egress/-1/ports
		  value:
	""".trimIndent())
	emitAll(blockRanges().map{"  - port: ${it.first}\n    endPort: ${it.last}"})
	emit("""
		  - protocol: UDP
		    port: 1
		    endPort: 65535
		- op: add
		  path: /spec/egress/-1/to/0/ipBlock/except
		  value:
		  - 10.0.0.0/8
		  - 172.16.0.0/12
		  - 192.168.0.0/16
		  - 169.254.169.254/32
	""".trimIndent())
	emitAll(ips().map{"  - ${it.hostAddress}/32"})
}}
