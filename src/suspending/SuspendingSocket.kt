package suspending
import java.net.SocketAddress
import java.nio.channels.AsynchronousSocketChannel
import kotlin.coroutines.suspendCoroutine

open class SuspendingSocket(
	override val chan: AsynchronousSocketChannel = AsynchronousSocketChannel.open()
): SuspendingChannel(chan) {
	suspend fun connect(addr: SocketAddress) = suspendCoroutine{ chan.connect(addr, it, ContinuationHandler()) }
}
