package lib
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.collections.HashSet

inline fun<T> Flow<Result<T>>.handle(crossinline fn: suspend FlowCollector<T>.(Throwable)->Unit) =
	transform{ it.onSuccess{emit(it)}.onFailure{fn(it)} }

fun<T> Flow<T>.distinct() = HashSet<T>().let{set->filter{ set.add(it) }}
fun<T> Flow<Iterable<T>>.flatten() = transform{emitAll(it.asFlow())}
fun<T> Flow<T>.sorted() =
	flow{ toCollection(PriorityQueue()).run{ emitAll(generateSequence{ poll() }.asFlow()) } }

suspend fun Flow<CharSequence>.joinToString() = buildString{collect(::append)}
