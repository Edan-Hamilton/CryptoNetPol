package suspending

import java.nio.ByteBuffer
import java.nio.channels.*
import kotlin.coroutines.*

abstract class SuspendingChannel(open val chan: AsynchronousByteChannel): AsynchronousChannel by chan {
	suspend fun read(bytes: Int) = ByteBuffer.allocate(bytes).also{buf->
		while(buf.hasRemaining()) suspendCoroutine {chan.read(buf, it, IOHandler)}
	}.rewind()

	suspend fun write(buf: ByteBuffer){
		while(buf.hasRemaining()) suspendCoroutine {chan.write(buf, it, IOHandler)}
	}

	protected open class ContinuationHandler<T>: CompletionHandler<T, Continuation<T>> {
		override fun completed(res: T, cont: Continuation<T>) = cont.resume(res)
		override fun failed(e: Throwable, cont: Continuation<T>) = cont.resumeWithException(e)
	}; private object IOHandler: ContinuationHandler<Int>()
}
