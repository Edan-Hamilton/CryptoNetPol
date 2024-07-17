package format
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import collections.Trie
import net.*
import resources.Templates
import java.io.File

private fun Trie<Char>.regex(): String = children.map{(k,v)->
	k + if(!v.isEmpty()) v.regex().let{ if(v.size==1) it else "($it)" } else ""
}.joinToString("|")

private val etlds = runBlocking{
	getFiles("https://raw.githubusercontent.com/publicsuffix/list/master/public_suffix_list.dat")
		.filterNot{ it.startsWith("//") }.map{it.split('.').asReversed()}.toCollection(Trie())
}

private fun etldp1(domain: String) = domain.split('.')
	.run{takeLast(etlds.match(asReversed()) + 1)}

//	Scanner(File("blocklist.bac")).asSequence().drop(1)
private val regex = cache.groupBy{etldp1(it)}
	.map{(tld,doms)->
		if(doms.size==1) doms.single()
		else tld.joinToString("\\.",".*")
	}.map{it.asIterable()}.toCollection(Trie()).regex()

fun writeRegEx() = File("DNSBlocking.yml")
	.writeText(Templates.DNS.readText().replace("$", regex))
