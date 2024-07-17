package resources
import kotlinx.coroutines.flow.*
import resources.Resource.getResource

object Ports {
	fun portFile(name: String) = getResource(name).readLines().map(String::toInt)
	val whitelist = portFile("Ports/whitelist").toSet()
	val blacklist = portFile("Ports/blacklist").asFlow()
}
