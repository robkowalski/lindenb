DATE=`date`
WHOAMI=`whoami`

compilation:
	cat src/java/org/lindenb/util/Compilation.java |\
	 sed -e "s/__DATE__/${DATE}/"  -e "s/__USER__/${WHOAMI}/"

clean:
	rm -rf build
