#!/bin/sh

# Because minecrift uses code from Minecraft as a base, primary development takes place
# on a private repository so that we do not distribute Mojang property.

# This script automates the private->public commit transfer process

WORK_TREE=mcp147/src/minecraft
export GIT_DIR=$WORK_TREE/.git
git rev-list public1.4.7..1.4.7 | tac | while read REV; do
	AUTHOR=$(git log --pretty=format:"%an <%ae>" -1 $REV)
	DATE=$(git log --pretty=format:"%ad" -1 $REV)
	BODY=$(git log --pretty=format:"%B" -1 $REV | sed '/Merge/s/of http[^ \t\n\r]*/of private-repo/' )
	GIT_WORK_TREE=$WORK_TREE git checkout $REV
	./getchanges.sh
	GIT_DIR=.git git commit --author="$AUTHOR" --date="$DATE" -m "$BODY" patches src
	git tag -f public1.4.7
done


GIT_WORK_TREE=$WORK_TREE git checkout 1.4.7
