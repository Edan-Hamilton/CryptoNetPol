package net
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import collections.*
import java.net.InetAddress

private val lists = getFiles(
	"https://gist.githubusercontent.com/jordan-wright/95af062378e9a7436b94f893d195bcd2/raw/",
	"https://raw.githubusercontent.com/andoniaf/mining-pools-list/master/mining-pools.lst",
	"https://raw.githubusercontent.com/BinaryDefense/mining-pools/main/xmr-pools.txt",
	"https://raw.githubusercontent.com/codingo/Minesweeper/master/lib/sources.txt",
	"https://raw.githubusercontent.com/Marfjeh/coinhive-block/master/domains",
	"https://gitlab.com/ZeroDot1/CoinBlockerLists/-/raw/master/list.txt",
	"https://gitlab.com/ZeroDot1/CoinBlockerLists/-/raw/master/list_browser.txt",
	"https://gitlab.com/ZeroDot1/CoinBlockerLists/-/raw/master/list_optional.txt",
)

private val hosts = getFiles(
	"https://raw.githubusercontent.com/hoshsadiq/adblock-nocoin-list/master/hosts.txt",
	"https://raw.githubusercontent.com/anudeepND/blacklist/master/CoinMiner.txt",
).filterNot{it.startsWith('#')}.map{it.drop(8)}

private val archive = getArchive("https://dsi.ut-capitole.fr/blacklists/download/cryptojacking.tar.gz")

private fun toIPs(value: List<String>) =
	value.map{it.split('.').map{it.toUByte().toByte()}.toByteArray()}.map(InetAddress::getByAddress).asFlow()

private fun sanitize(domains: Flow<String>) = domains.map{dom->dom
	.takeWhile{it!='/'}
	.takeLastWhile{it!='='&&it!='@'}
	.trimEnd('^')
}.distinct()

private const val PORT_LIST = "https://go.catonetworks.com/rs/245-RJK-441/images/miningpools-feed-by-Cato-Networks.txt"
private val withPorts = runBlocking{(liveAPI() + getFiles(PORT_LIST).toList())}
	.map{it.split(':')}.map{(a,b)->a to b}.unzip()
	.let{(domains,portList)->
		ports = portList.map{it.toInt()}.asFlow()
		domains.partition{it.all{c->c.isDigit()||c=='.'}}.let{(ipList,domains)->
			ips = toIPs(ipList); domains.asFlow()
		}
	}

lateinit var ips: Flow<InetAddress>
lateinit var ports: Flow<Int>
val cache = mutableListOf<String>()
val domains = sanitize(merge(archive, lists, hosts, withPorts)).onEach{ cache.add(it) }
