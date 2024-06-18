package DNS
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import java.nio.channels.CompletionHandler
import kotlin.coroutines.*

open class SuspendingSocket(val chan: AsynchronousSocketChannel = AsynchronousSocketChannel.open()): AsynchronousChannel, NetworkChannel by chan {
	suspend fun connect(addr: SocketAddress) =
		suspendCoroutine{ chan.connect(addr, it, ContinuationHandler()) }

	suspend fun read(bytes: Int) = ByteBuffer.allocate(bytes).also{buf->
		while(buf.hasRemaining()) suspendCoroutine{ chan.read(buf, it, IOHandler) }
	}.rewind()

	suspend fun write(buf: ByteBuffer){
		while(buf.hasRemaining()) suspendCoroutine{ chan.write(buf, it, IOHandler) }
	}

	private open class ContinuationHandler<T>: CompletionHandler<T, Continuation<T>> {
		override fun completed(res: T, cont: Continuation<T>) = cont.resume(res)
		override fun failed(e: Throwable, cont: Continuation<T>) = cont.resumeWithException(e)
	}; private object IOHandler: ContinuationHandler<Int>()
}
