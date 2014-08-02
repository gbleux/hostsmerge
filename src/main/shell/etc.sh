#!/bin/sh
#
# example script to update /etc/hosts from a common directory
#

BIN_D=`dirname "$0"`
HOSTSMERGE_D=`readlink -f "$BIN_D/../${assembly.share}"`
TEMP_HOSTS=/tmp/hostsmerge
SYSTEM_HOSTS=/etc/hosts

echo "Merging $HOSTSMERGE_D/* into $SYSTEM_HOSTS"
sh "$BIN_D/hostsmerge" --loopback "$HOSTSMERGE_D" "$TEMP_HOSTS"
sudo mv "$TEMP_HOSTS" "$SYSTEM_HOSTS"

if test 0 -eq $?;then
    echo "Done."
    exit 0
else
    echo "Failure."
    exit 1
fi
