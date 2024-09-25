#!/bin/bash

if (( $EUID == 0 )); then
    echo "Don't run as root!"
    exit
fi

actDir=$(pwd)
deployingApp="to.science.api"
toscienceDir="/opt/toscience"
deployDir="$toscienceDir/git/$deployingApp"
tmpDir="/opt/toscience/tmp"
targetDir="/opt/toscience/apps"
linkDir="toscience-api"
fileName="toscience-api-1.0.0-SNAPSHOT.zip"
folderName="toscience-api-1.0.0-SNAPSHOT"
newInstallDir="$linkDir.$(date +'%Y%m%d%H%M%S')"
confDir="/etc/toscience/api"
resourcesDir="/etc/toscience/resources"
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
cp $deployDir/conf.tmpl/routes $targetDir/$newInstallDir/conf
ln -sf $resourcesDir $targetDir/$newInstallDir/resources
cd $actDir

echo ""
echo "Neue Binärversion verfügbar unter $targetDir/$newInstallDir."
echo "Port ist fest eingestellet auf: $PLAYPORT"
echo "Zum Umschalten auf die neue Version:"
echo "systemctl stop $linkDir.service"
echo "evtl. den noch laufenden Prozess töten:"
echo "ps -eaf | grep $linkDir | more"
echo "kill `cat $OLDDIR/RUNNING_PID`"
echo "systemctl start $linkDir.service"
echo "Das Log unter $toscienceDir/$linkDir/logs/application.log beobachten"
echo "Warten auf die Meldung \"Success! Regal-API started!\""
