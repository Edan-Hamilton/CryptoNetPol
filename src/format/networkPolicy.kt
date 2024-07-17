package format
import kotlinx.coroutines.flow.*
import net.ips
import resources.Templates
import java.io.*

suspend fun writePolicy() = File("NetworkPolicy.yml").bufferedWriter().use{file->
	Templates.ports.transferTo(file)
	blockRanges().map{"  - port: ${it.first}\n    endPort: ${it.last}"}.collect(file::appendLine)
	Templates.ips.transferTo(file)
	ips().map{"  - ${it.hostAddress}/32"}.collect(file::appendLine)
}
