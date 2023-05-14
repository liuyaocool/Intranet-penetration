#!/bin/bash
jdkPath=
javaPath=java
abPath=$(cd `dirname $0`;pwd)
jarName=intranet-server.jar
jarId=(`${jdkPath}/bin/jps | grep ${jarName}`)
jarId=${jarId[0]}

cd ${abPath}

start() {
	echo ${javaPath} -jar ${abPath}/${jarName}

	${javaPath} -jar ${abPath}/${jarName} \
	  -Xms512M -Xmx512M \
	  -XX:+HeapDumpOnOutOfMemoryError \
	  -Xlogg${abPath}/logs/gc-%t.log \
	  -XX:+UseGCLogFileRotation \
	  -XX:NumberOfGCLogFiles=10 \
	  -XX:GCLogFileSize=20M \
	  -XX:+PrintGCDetails \
	  -XX:+PrintGCDateStamps \
	  -XX:+PrintGCCause \
	  2 >& 1 >> console.log &
}

stop() {
	echo kill ${jarId}
	kill ${jarId}
}

restart() {
	stop
	sleep 1
	start
}

help() {
	echo -----method---------
	echo start
	echo stop
	echo restart
}

${1}
