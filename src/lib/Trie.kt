package lib

class Trie<T>: AbstractMutableSet<Iterable<T>>() {
	val children = mutableMapOf<T, Trie<T>>()

	override fun add(element: Iterable<T>): Boolean =
		element.fold(this){node, x->node.children.getOrPut(x){Trie()} }.let{true}

	fun match(el: Iterator<T>, init: Trie<T>.()->Int): Int =
		try { children[el.next()]!!.match(el,init) + 1 }
		catch(_: Exception){ init() }

	override val size by children::size

	override fun iterator() = asSequence().toMutableList().iterator()
	fun asSequence() = sequence{rec(mutableListOf(),this@Trie)}
	private suspend fun SequenceScope<List<T>>.rec(prev: MutableList<T>, next: Trie<T>){
		if(next.children.isNotEmpty()) for((k,v) in next.children) {
			prev.add(k)
			rec(prev,v)
			prev.removeLast()
		} else yield(prev)
	}
}
