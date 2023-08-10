#!/bin/bash

if (( $EUID == 0 )); then
    echo "Don't run as root!"
    exit
fi

actDir=$(pwd)
deployingApp="regal-api"
toscienceDir="/opt/regal"
deployDir="$toscienceDir/$deployingApp"
tmpDir="/opt/regal/regal-tmp"
targetDir="/opt/regal"
linkDir="regal-server"
fileName="regal-api-0.8.0-SNAPSHOT.zip"
folderName="regal-api-0.8.0-SNAPSHOT"
newInstallDir="$linkDir.$(date +'%Y%m%d%H%M%S')"
confDir="/etc/to.science/api.conf"
resourcesDir="/etc/to.science/resources"
PLAYPORT=9000

cp $deployDir/target/universal/$fileName $tmpDir

cd $tmpDir
unzip $fileName

mv $tmpDir/$folderName $targetDir/$newInstallDir
rm $tmpDir/$fileName
mkdir $targetDir/$newInstallDir/logs

OLDDIR=`readlink $toscienceDir/$linkDir`
if [ -L $toscienceDir/$linkDir ]; then
       	rm $toscienceDir/$linkDir
fi
ln -sf $targetDir/$newInstallDir $toscienceDir/$linkDir
rm -r  $targetDir/$newInstallDir/conf
ln -sf $confDir $targetDir/$newInstallDir/conf
ln -sf $resourcesDir $targetDir/$newInstallDir/resources
cd $actDir

echo ""
echo "Neue Binärversion verfügbar unter $targetDir/$newInstallDir."
echo "Port ist fest eingestellet auf: $PLAYPORT"
echo "Zum Umschalten auf die neue Version:"
echo "sudo service regal-api stop"
echo "evtl. den noch laufenden Prozess töten:"
echo "ps -eaf | grep regal-server | more"
echo "kill `cat $OLDDIR/RUNNING_PID`"
echo "sudo service regal-api start"
echo "Das Log unter $targetDir/$newInstallDir/logs/application.log beobachten"
echo "Warten auf die Meldung \"Success! Regal-API started!\""
