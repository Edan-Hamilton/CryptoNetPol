package format
import kotlinx.coroutines.flow.*
import lib.close
import net.ips
import resources.Templates
import java.io.*

suspend fun writePolicy() = Templates.ports.copyTo(File("NetworkPolicy.yml"),true).bufferedWriter().use{file->
	blockRanges().map{"  - port: ${it.first}\n    endPort: ${it.last}"}.collect(file::appendLine)
	Templates.ips.reader().close{transferTo(file)}
	ips().map{"  - ${it.hostAddress}/32"}.collect(file::appendLine)
}
