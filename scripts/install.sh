#!/bin/bash

if (( $EUID == 0 )); then
    echo "Don't run as root!"
    exit
fi

appDeployDir=$(pwd)
toscienceDir="/opt/toscience"
deployDir="/opt/toscience/git"
targetDir="/opt/toscience/apps"
linkDir="toscience-api"
fileName="toscience-api-1.0.0-SNAPSHOT.zip"
folderName="toscience-api-1.0.0-SNAPSHOT"
newInstallDir="$linkDir.$(date +'%Y%m%d%H%M%S')"
confDir="/etc/toscience/api"

cp $appDeployDir/target/universal/$fileName $deployDir

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
cd $appDeployDir

echo ""
echo "Neue Binärversion verfügbar unter $targetDir/$newInstallDir."
echo "Port ist fest eingestellt auf $PLAYPORT"
echo "Zum Umschalten auf die neue Version:"
echo "sudo systemctl stop toscience-api.service"
echo "sudo systemctl start toscience-api.service"
echo "./loadCache.sh"
