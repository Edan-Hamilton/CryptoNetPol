package lib
import java.io.*

fun File.writeLines(lines: List<String>) = bufferedWriter().use{lines.forEach(it::appendLine)}

inline fun<T,R> Sequence<T>.transform(crossinline fn: suspend SequenceScope<R>.(T)->Unit)=
	flatMap{ sequence{ fn(it) } }
