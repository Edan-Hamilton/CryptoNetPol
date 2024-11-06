package resources
import kotlinx.coroutines.flow.asFlow
import net.toIPs
import kotlin.reflect.KProperty

object ListFile {
	operator fun getValue(ref: Any, prop: KProperty<*>) =
		Resource.get(Resource.fromProp(ref,prop)).bufferedReader().lineSequence()
}

private object Blacklist {
	val ports by ListFile
	val ips by ListFile
}

private object Whitelist {
	val common by ListFile
	val ports by ListFile
	val hash by ListFile
	val ips by ListFile
}

object Ports {
	val whitelist = Whitelist.ports.map(String::toUShort).toSet()
	val blacklist = Blacklist.ports.map(String::toUShort).asFlow()
}

object IPs {
	val whitelist = toIPs(Whitelist.ips).toSet()
	val blacklist = toIPs(Blacklist.ips).asFlow()
}

val cryptnono_allows = (Whitelist.common + Whitelist.hash).toSet()
