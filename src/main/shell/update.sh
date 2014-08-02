#!/bin/sh
#
# utility to download remote host alias dictionaries.
# this will retrieve lists containing addresses of known
# adservers and malware/spyware distributors.
#

BIN_D=`dirname "$0"`
HOSTSMERGE_D=`readlink -f "$BIN_D/../${assembly.share}"`

YOYO="http://pgl.yoyo.org/adservers/serverlist.php?hostformat=hosts&showintro=1&mimetype=plaintext"
MVPS="http://winhelp2002.mvps.org/hosts.txt"
HPAD="http://hosts-file.net/ad_servers.asp"

# TODO: implement sources.list. read upstream URLs from separate file to
#       allow users to maintain their own hosts sources.
#       the file is a plain key/value storage (ads=nfs://hosts/block.txt)
#       either 'awk' it in this shell script or rewrite using a different
#       language.

hosts_merge() {
    wget --no-verbose \
        --output-document="$HOSTSMERGE_D/$2" "$1"
}

hosts_merge "$YOYO" "yoyo"
hosts_merge "$MVPS" "mvps"
hosts_merge "$HPAD" "hphosts-ads"
