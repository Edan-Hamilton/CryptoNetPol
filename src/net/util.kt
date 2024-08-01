@file:OptIn(ExperimentalCoroutinesApi::class)
package net
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.util.zip.GZIPInputStream

const val gh = "https://raw.githubusercontent.com"

private val client = HttpClient{ install(ContentNegotiation){ json(Json{ ignoreUnknownKeys = true }) } }

private fun HttpResponse.lineFlow() = flow {
	val chan = bodyAsChannel()
	while(!chan.isClosedForRead) emit(chan.readUTF8Line()!!)
}

fun getFiles(vararg files: String) = files.asFlow()
	.flatMapMerge{client.get(it).lineFlow()}.filterNot(CharSequence::isEmpty)

suspend fun liveAPI() = coroutineScope{
	@Serializable data class Miner(val url: String)
	client.get("https://api.minerstat.com/v2/pools").body<List<Miner>>().map{async{
		Ksoup.parseGetRequest("https://minerstat.com/pools/${it.url}/addresses")
			.select(".td.flexAddress").eachText()
	}}.awaitAll().flatten()
}

suspend fun algos() = coroutineScope{
	@Serializable data class Coin(val algorithm: String)
	client.get("https://api.minerstat.com/v2/coins").body<List<Coin>>()
		.map{it.algorithm.takeWhile{c->c!=' '&&c!='('}.lowercase()}
}

fun getArchive(uri: String)= flow {
	GZIPInputStream(URI(uri).toURL().openStream())
		.toByteReadChannel().apply{discard(512)}
		.let{ while(true) emit(it.readUTF8Line()!!) }
}.flowOn(Dispatchers.IO).takeWhile{!it.startsWith('\u0000')}
