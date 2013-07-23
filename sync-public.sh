#!/bin/sh

# Because minecrift uses code from Minecraft as a base, primary development takes place
# on a private repository so that we do not distribute Mojang property.

# This script automates the private->public commit transfer process

function sync_rev() {
REV=$1
	AUTHOR=$(git log --pretty=format:"%an <%ae>" -1 $REV)
	DATE=$(git log --pretty=format:"%ad" -1 $REV)
	BODY=$(git log --pretty=format:"%B" -1 $REV | sed '/Merge/s/of http[^ \t\n\r]*/of private-repo/' )
	GIT_WORK_TREE=$WORK_TREE git checkout $REV
	./getchanges.sh
	GIT_DIR=.git git add -A src patches
	GIT_DIR=.git git commit --author="$AUTHOR" --date="$DATE" -m "$BODY"
	git tag -f public
}

WORK_TREE=mcp804/src/minecraft
export GIT_DIR=$WORK_TREE/.git
if [ "$#" -ne 1 ] ; then
	git rev-list --reverse public..master | while read REV; do
		sync_rev $REV
	done
else
	sync_rev $1
fi
GIT_WORK_TREE=$WORK_TREE git checkout master


