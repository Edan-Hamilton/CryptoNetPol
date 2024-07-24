package resources
import kotlinx.coroutines.flow.*
import resources.getResource

object Ports {
	private fun portFile(name: String) = getResource(name).readLines().map(String::toUShort)
	val whitelist = portFile("Ports/whitelist").toSet()
	val blacklist = portFile("Ports/blacklist").asFlow()
}
