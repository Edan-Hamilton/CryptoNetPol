package format
import kotlinx.coroutines.flow.*
import lib.*
import net.ips
import resources.Templates
import java.io.*

suspend fun writePolicy() = Templates.ports.copyTo(File("NetworkPolicy.yml"),true).appendWriter().use{file->
	blockRanges().transform{
		emit("  - port: ${it.first}")
		if(it.first!=it.last) emit("    endPort: ${it.last}")
	}.collect(file::appendLine)
	Templates.ips.reader().close{transferTo(file)}
	ips().map{"  - ${it.hostAddress}/32"}.collect(file::appendLine)
}
