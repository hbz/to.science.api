#!/bin/bash

if (( $EUID == 0 )); then
    echo "Don't run as root!"
    exit
fi

appDeployDir=$(pwd)
deployDir="/opt/toscience/src"
targetDir="/opt/toscience"
linkDir="toscience-server"
fileName="toscience-api-1.0.0-SNAPSHOT.zip"
folderName="toscience-api-1.0.0-SNAPSHOT"
newInstallDir="$linkDir.$(date +'%Y%m%d%H%M%S')"
confDir="/etc/toscience/api.conf"

cp $appDeployDir/target/universal/$fileName $deployDir

cd $deployDir
unzip $fileName

mv $deployDir/$folderName $targetDir/$newInstallDir
mkdir $targetDir/$newInstallDir/logs

if [ -L $targetDir/$linkDir ]; then
       	rm $targetDir/$linkDir
fi
ln -sf $targetDir/$newInstallDir $targetDir/$linkDir
rm -r  $targetDir/$newInstallDir/conf
ln -sf $confDir $targetDir/$newInstallDir/conf
