@file:OptIn(ExperimentalCoroutinesApi::class)
package net
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.net.URI
import java.util.zip.GZIPInputStream

val client = HttpClient{ install(ContentNegotiation){ json(Json{ ignoreUnknownKeys = true }) } }

private fun HttpResponse.lineFlow() = flow {
	val chan = bodyAsChannel()
	while(!chan.isClosedForRead) emit(chan.readUTF8Line()!!)
}

fun getFiles(vararg files: String) = files.asFlow()
	.flatMapMerge{client.get(it).lineFlow()}.filterNot(CharSequence::isEmpty)

fun getArchive(uri: String)= flow {
	GZIPInputStream(URI(uri).toURL().openStream())
		.toByteReadChannel().apply{discard(512)}
		.let{ while(true) emit(it.readUTF8Line()!!) }
}.flowOn(Dispatchers.IO).takeWhile{!it.startsWith('\u0000')}
