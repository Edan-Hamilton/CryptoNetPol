package lib

fun<T> Iterator<T>.nextOrNull() = if(hasNext()) next() else null

inline fun<T,R> Sequence<T>.transform(crossinline fn: suspend SequenceScope<R>.(T)->Unit)=
	flatMap{ sequence{ fn(it) } }
