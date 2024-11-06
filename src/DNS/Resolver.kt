package DNS
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.*
import suspending.SuspendingSocket
import java.net.*
import java.nio.ByteBuffer

// DNS protocol implementation; the standard Java implementation is too slow
class Resolver: SuspendingSocket() {
	companion object { // pseudo-constructor (constructors can't suspend)
		suspend operator fun invoke(vararg ip: Byte) = Resolver().connect(InetAddress.getByAddress(ip))
		private const val HEAD = Record.HEAD + 2
		private val tail = byteArrayOf(0,0,1,0,1)
		private val Padding = HEAD + tail.size - 1
	}

	private suspend fun connect(addr: InetAddress) = connect(InetSocketAddress(addr, 53)).let{this}

	private val buf = ByteBuffer.allocate(4096)
		.put(4, 1) // recursive lookup flag
		.put(7, 1) // QDCOUNT (values other than 1 are not supported)

	private var ID: Short = 0
	private fun QName (domain: String) = domain.split(".").map(Charsets.UTF_8::encode)
	suspend fun lookup(domain: String) = write(buf.run{
		putShort(0, (domain.length + Padding).toShort())
		putShort(2, ID++)
		position(HEAD)
		QName(domain).forEach{ // null terminated sequence of pascal strings
			put(it.limit().toByte())
			put(it)
		}; put(tail)
		slice(0,position())
	})

	private val readLock = Mutex()
	private suspend fun readPacket() = readLock.withLock{read(read(2).getShort().toInt())}
	suspend fun readRecord() = runCatching{Record(readPacket())}
	suspend fun lookup(domains: Iterable<String>) = callbackFlow{
		domains.map{ lookup(it); launch{ send(readRecord()) } }.joinAll()
		close()
	}
}
