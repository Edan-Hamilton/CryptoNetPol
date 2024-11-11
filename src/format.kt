import kotlinx.coroutines.flow.*
import lib.Trie
import net.*
import resources.cryptnono_allows

suspend fun formatCryptnono() = algos().toMutableSet().apply{removeAll(cryptnono_allows)}
	.map(String::asIterable).toCollection(Trie()).asSequence()
	.map{it.joinToString("")}

private fun Trie<Char>.regex(): String = children.map{(k,v)->
	k + if(!v.isEmpty()) v.regex().let{ if(v.size==1) it else "($it)" } else ""
}.joinToString("|")

suspend fun DNSFilter(domains: List<String>): String {
	val etlds = getFiles("$gh/publicsuffix/list/master/public_suffix_list.dat")
		.filterNot{ it.startsWith("//") }
		.map{it.split('.').asReversed()}
		.toCollection(Trie())

	return domains.groupBy{it.split('.').run{
		takeLast(etlds.match(asReversed().iterator()){if(children.contains("*")) 2 else 1})
	}}.map{(tld,doms)->
		if(doms.size==1) doms.single()
		else tld.joinToString(".")
	}.map{it.asIterable()}.toCollection(Trie()).regex()
		.replace(".","\\.")
}
