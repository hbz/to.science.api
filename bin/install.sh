#!/bin/bash

if (( $EUID == 0 )); then
    echo "Don't run as root!"
    exit
fi

actDir=$(pwd)
deployingApp="to.science.api"
toscienceDir="/opt/toscience"
deployDir="/opt/toscience/git"
targetDir="/opt/toscience/apps"
linkDir="toscience-api"
fileName="regal-api-0.8.0-SNAPSHOT.zip"
folderName="regal-api-0.8.0-SNAPSHOT"
newInstallDir="$linkDir.$(date +'%Y%m%d%H%M%S')"
confDir="/etc/toscience/api"
resourcesDir="/etc/toscience/resources"
PLAYPORT=9000

cp $deployDir/$deployingApp/target/universal/$fileName $deployDir

cd $deployDir
unzip $fileName

mv $deployDir/$folderName $targetDir/$newInstallDir
mkdir $targetDir/$newInstallDir/logs

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
echo "sudo service toscience-api stop"
echo "sudo service toscience-api start"
echo "Das Log unter /opt/toscience/toscience-api/logs/application.log beobachten"
#echo "./loadCache.sh"
