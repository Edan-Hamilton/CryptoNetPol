package resources
import lib.toIPs

val ip_blacklist = toIPs(getResource("ip_blacklist").readLines())
