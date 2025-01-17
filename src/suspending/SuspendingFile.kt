package suspending
import java.nio.ByteBuffer
import java.nio.channels.*
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.WRITE
import java.util.concurrent.atomic.AtomicLong

class SuspendingFile private constructor(private val file: AsyncFileBytes): SuspendingChannel(file), Seekable by file {
	constructor(path: String): this(AsyncFileBytes(path))

	suspend fun write(str: String) = write(ByteBuffer.wrap(str.toByteArray()))

	private class AsyncFileBytes(
		path: String, val file: AsynchronousFileChannel = AsynchronousFileChannel.open(Paths.get(path), WRITE),
	): AsynchronousByteChannel, Seekable  {
		override fun size() = file.size()
		override fun position() = pos.get()
		override fun position(newPosition: Long) = pos.set(newPosition).let{this}

		override fun close() = file.close()
		override fun isOpen() = file.isOpen

		private var pos = AtomicLong(0)
		private fun pos(inc: Int) = pos.getAndAdd(inc.toLong())

		override fun <A: Any?> read(dst: ByteBuffer, attachment: A, handler: CompletionHandler<Int, in A>?) =
			file.read(dst, pos(dst.limit()), attachment, handler)

		override fun <A: Any?> write(src: ByteBuffer, attachment: A, handler: CompletionHandler<Int, in A>?) =
			file.write(src, pos(src.limit()), attachment, handler)

		override fun write(src: ByteBuffer) = file.write(src, pos(src.limit()))
		override fun  read(dst: ByteBuffer) = file. read(dst, pos(dst.limit()))
	}
}
