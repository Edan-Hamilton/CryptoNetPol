package resources
import java.io.File
import kotlin.reflect.KProperty

object Resource {
	fun get(name: String) = File(ClassLoader.getSystemResource(name).file)
	fun fromProp(ref: Any, prop: KProperty<*>) = "${ref.javaClass.simpleName}/${prop.name}"
	operator fun getValue(ref: Any, prop: KProperty<*>) = get(fromProp(ref,prop)+".yml")
}
