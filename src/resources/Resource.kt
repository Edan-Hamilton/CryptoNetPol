package resources
import java.io.File
import kotlin.reflect.KProperty

object Resource {
	fun getResource(name: String) = File(ClassLoader.getSystemResource(name).file)
	operator fun getValue(ref: Any, prop: KProperty<*>) =
		getResource("${ref.javaClass.simpleName}/${prop.name}.yml").reader()
}
