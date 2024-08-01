package lib
import java.io.*

inline fun <T: Closeable, R> T.close(crossinline fn: T.() -> R) = this.use(fn)

fun File.appendWriter() = FileOutputStream(this,true).writer()
