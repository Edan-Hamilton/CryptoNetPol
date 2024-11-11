package suspending

interface Seekable {
	fun size(): Long
	fun position(): Long
	fun position(newPosition: Long): Seekable
}
