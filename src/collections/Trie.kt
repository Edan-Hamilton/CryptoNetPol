package collections

class Trie<T>: AbstractMutableSet<Iterable<T>>() {
	val children = mutableMapOf<T, Trie<T>>()
	private var end = false

	override fun add(element: Iterable<T>): Boolean = element
		.fold(this){node, x->node.children.getOrPut(x){Trie()} }
			.run{end=true}.let{true}

	fun match(element: Iterable<T>): Int = match(element.iterator())?:0
	private fun match(el: Iterator<T>): Int? =
		el.nextOrNull()?.let{children[it]}?.match(el)?.plus(1) ?: if(end) 0 else null

	override val size: Int get() = children.size
	override fun iterator() = throw NotImplementedError()
}
