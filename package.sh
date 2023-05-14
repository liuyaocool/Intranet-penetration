abPath=$(cd `dirname $0`;pwd)
my_mvn_home=
cd ${abPath}
${my_mvn_home}mvn clean && \
    ${my_mvn_home}mvn install && \
#    cd ${abPath}/commons && ${my_mvn_home}mvn install && \
    cd ${abPath}/server && ${my_mvn_home}mvn assembly:assembly && \
    cd ${abPath}/client && ${my_mvn_home}mvn assembly:assembly