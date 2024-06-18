package collections
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.collections.HashSet

fun<T> Iterator<T>.nextOrNull() = if(hasNext()) next() else null

// helper methods for dealing with flows

inline fun<T> Flow<Result<T>>.handle(crossinline fn: suspend FlowCollector<T>.(Throwable)->Unit) =
	transform{ it.onSuccess{emit(it)}.onFailure{fn(it)} }

fun<T> Flow<T>.distinct() = HashSet<T>().let{set->filter{ set.add(it) }}
fun<T> Flow<Iterable<T>>.flatten() = transform{emitAll(it.asFlow())}
fun<T> Flow<T>.sorted() =
	flow{ toCollection(PriorityQueue()).run{ emitAll(generateSequence{ poll() }.asFlow()) } }
