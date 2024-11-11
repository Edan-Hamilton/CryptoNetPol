package net
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import lib.writeLines
import java.io.File

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

const val gh = "https://raw.githubusercontent.com"
suspend fun getData() = coroutineScope{
	val zd1 = "https://gitlab.com/ZeroDot1/CoinBlockerLists/-/raw/master"
	val lists = getFiles(
		"https://gist.githubusercontent.com/jordan-wright/95af062378e9a7436b94f893d195bcd2/raw/",
		"$zd1/list.txt", "$zd1/list_browser.txt", "$zd1/list_optional.txt",
		"$gh/andoniaf/mining-pools-list/master/mining-pools.lst",
		"$gh/BinaryDefense/mining-pools/main/xmr-pools.txt",
		"$gh/codingo/Minesweeper/master/lib/sources.txt",
		"$gh/Marfjeh/coinhive-block/master/domains",
	)

	val hosts = getFiles(
		"$gh/hoshsadiq/adblock-nocoin-list/master/hosts.txt",
		"$gh/anudeepND/blacklist/master/CoinMiner.txt",
	).filterNot{it.startsWith('#')}.map{it.drop(8)}

	val archive = getArchive("https://dsi.ut-capitole.fr/blacklists/download/cryptojacking.tar.gz")

	val withPorts = liveAPI() + getFiles("https://go.catonetworks.com/rs/245-RJK-441/images/miningpools-feed-by-Cato-Networks.txt").toList()
	val (names, ports) = withPorts.map{it.split(':')}.map{(a,b)->a to b}.unzip()
	val (ips,domains) = names.partition{it.all{c->c.isDigit()||c=='.'}}

	File("ports").writeLines(ports)
	File("domains").bufferedWriter().use{file->
		sanitize(merge(archive, lists, hosts, domains.asFlow()))
			.collect(file::appendLine)
	}
	File("ips").writeLines(ips)
}
