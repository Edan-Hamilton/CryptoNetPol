package format
import kotlinx.coroutines.flow.*
import lib.close
import net.ips
import resources.Templates
import java.io.*

suspend fun writePolicy() = File("NetworkPolicy.yml").bufferedWriter().use{file->
	Templates.ports.close{transferTo(file)}
	blockRanges().map{"  - port: ${it.first}\n    endPort: ${it.last}"}.collect(file::appendLine)
	Templates.ips.close{transferTo(file)}
	ips().map{"  - ${it.hostAddress}/32"}.collect(file::appendLine)
}
