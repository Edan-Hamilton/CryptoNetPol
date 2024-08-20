package net
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import lib.*
import java.net.InetAddress

private const val zd1 = "https://gitlab.com/ZeroDot1/CoinBlockerLists/-/raw/master"
private val lists = getFiles(
	"https://gist.githubusercontent.com/jordan-wright/95af062378e9a7436b94f893d195bcd2/raw/",
	"$zd1/list.txt", "$zd1/list_browser.txt", "$zd1/list_optional.txt",
	"$gh/andoniaf/mining-pools-list/master/mining-pools.lst",
	"$gh/BinaryDefense/mining-pools/main/xmr-pools.txt",
	"$gh/codingo/Minesweeper/master/lib/sources.txt",
	"$gh/Marfjeh/coinhive-block/master/domains",
)

private val hosts = getFiles(
	"$gh/hoshsadiq/adblock-nocoin-list/master/hosts.txt",
	"$gh/anudeepND/blacklist/master/CoinMiner.txt",
).filterNot{it.startsWith('#')}.map{it.drop(8)}

private val archive = getArchive("https://dsi.ut-capitole.fr/blacklists/download/cryptojacking.tar.gz")

private const val PORT_LIST = "https://go.catonetworks.com/rs/245-RJK-441/images/miningpools-feed-by-Cato-Networks.txt"
private val withPorts = runBlocking{(liveAPI() + getFiles(PORT_LIST).toList())}
	.map{it.split(':')}.map{(a,b)->a to b}.unzip()
	.let{(domains,portList)->
		ports = portList.map(String::toUShort).asFlow()
		domains.partition{it.all{c->c.isDigit()||c=='.'}}.let{(ipList,domains)->
			ips = toIPs(ipList).asFlow(); domains.asFlow()
		}
	}

var ips: Flow<InetAddress>
var ports: Flow<UShort>
val cache = mutableListOf<String>()
val domains = sanitize(merge(archive, lists, hosts, withPorts)).onEach{ cache.add(it) }
