package lib
import java.io.Closeable

inline fun <T: Closeable, R> T.close(crossinline fn: T.() -> R) = this.use(fn)
