#! /bin/bash

cd /opt/regal/cronjobs

source variables.conf

INDEXNAME=frl


curl -s -XGET localhost:9200/$INDEXNAME/journal,monograph,webpage/_search -d'{"query":{"match_all":{}},"fields":["/@id"],"size":"50000"}'|egrep -o "$INDEXNAME:[^\"]*">$ARCHIVE_HOME/logs/titleObjects.txt
for i in `cat $ARCHIVE_HOME/logs/titleObjects.txt`;do ht=`curl -s localhost:9200/$INDEXNAME/_all/$i | egrep -o "hbzId\":[\[\"]{1,2}[^\"]*"|egrep  -o "[A-Z]{2}[0-9]{9}"`; if [ ${#ht} -eq 11 ] ; then echo $i , $ht; else echo $i , XXXXXXXXXXX; fi ;done |sort > $ARCHIVE_HOME/logs/pid-catalog-conc-`date +"%Y%m%d"`.csv


log="$ARCHIVE_HOME/logs/lobidify-`date +"%Y%m%d"`.log"
echo "lobidify & enrich"
echo "Find logfile in $log"

cat $ARCHIVE_HOME/logs/titleObjects.txt | parallel --jobs 5 ./updatePid.sh {} $BACKEND > $log 2>&1
cp $log /tmp/updateMetadata
echo >> $log
echo "Summary" >> $log
numOfUpdatePids=`grep  "Enrichment.*succeeded!" /tmp/updateMetadata | grep -v "Not updated"|grep -o "frl:[^\ ]*"|sort|uniq|wc -l`
echo "Updated Pids $numOfUpdatePids" >> $log
grep  "Enrichment.*succeeded!" /tmp/updateMetadata | grep -v "Not updated"|grep -o "frl:[^\ ]*"|sort|uniq >> $log
cd -