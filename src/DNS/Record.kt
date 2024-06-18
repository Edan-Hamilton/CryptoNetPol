package DNS
import java.net.*
import java.nio.ByteBuffer

// response to a DNS request
class Record(buf: ByteBuffer): AbstractList<InetAddress>() {
	private val flags = buf.getShort(2)
	override val size = buf.getShort(6).toInt()

	val domain by lazy {
		buf.position(HEAD)
		generateSequence{
			buf.get().takeIf{it!=0.toByte()}?.let{
				ByteArray(it.toInt())
					.also{buf[it]}
					.decodeToString()
			}
		}.joinToString(".")
	}

	companion object{
		private const val SUCCESS = 0x8180.toShort()
		const val HEAD = 12
	}
	init { if(flags!= SUCCESS) throw DNSException() }

	private val data = buf.array().sliceArray(buf.limit()-size*16+12..<buf.limit())
	override fun get(index: Int) =
		InetAddress.getByAddress(data.sliceArray(index*16..<index*16+4))

	inner class DNSException(val code: RCode = RCode.entries[flags.toInt() and 0xf], val name: String = domain): Exception() {
		override val message get() = "${name}: ${code.name}"
	}; enum class RCode{ None, Format, Server, Name, NotImp, Refused; }
}
