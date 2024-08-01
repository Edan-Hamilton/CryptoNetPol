package lib

class Trie<T>: AbstractMutableSet<Iterable<T>>() {
	val children = mutableMapOf<T, Trie<T>>()
	private var end = false

	override fun add(element: Iterable<T>): Boolean = element
		.fold(this){node, x->node.children.getOrPut(x){Trie()} }
			.run{end=true}.let{true}

	fun match(element: Iterable<T>): Int = match(element.iterator())?:0
	private fun match(el: Iterator<T>): Int? =
		el.nextOrNull()?.let{children[it]}?.match(el)?.plus(1) ?: if(end) 0 else null

	override val size by children::size

	override fun iterator() = asSequence().toMutableList().iterator()
	fun asSequence() = sequence{rec(mutableListOf(),this@Trie)}
	private suspend fun SequenceScope<List<T>>.rec(prev: MutableList<T>, next: Trie<T>){
		if(!next.end) for((k,v) in next.children) {
			prev.add(k)
			rec(prev,v)
			prev.removeLast()
		} else yield(prev)
	}
}
