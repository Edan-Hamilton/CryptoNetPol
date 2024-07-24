package format
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import lib.Trie
import net.*
import resources.Templates
import java.io.File

private fun Trie<Char>.regex(): String = children.map{(k,v)->
	k + if(!v.isEmpty()) v.regex().let{ if(v.size==1) it else "($it)" } else ""
}.joinToString("|")

private val etlds = runBlocking{
	getFiles("$gh/publicsuffix/list/master/public_suffix_list.dat")
		.filterNot{ it.startsWith("//") }.map{it.split('.').asReversed()}.toCollection(Trie())
}

private val regex = cache
	.groupBy{it.split('.').run{takeLast(etlds.match(asReversed())+1)}}
	.map{(tld,doms)->
		if(doms.size==1) doms.single()
		else tld.joinToString("\\.",".*")
	}.map{it.asIterable()}.toCollection(Trie()).regex()

fun writeRegEx() = File("DNSBlocking.yml")
	.writeText(Templates.DNS.readText().replace("$", regex))
