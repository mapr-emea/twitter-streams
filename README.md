## Project structure

* `flume` contains the flume script
* `twitter-streams-producer-agent` gets data from twitter and writes it into MapR streams (runs as agent)
* `spark-streams-consumer` is a spark streaming app which aggretates data and writes it into OpenTSDB

Generate IDE files

```
gradle idea

 or

gradle eclipse
```

Build with

```
gradle clean build distZip
```

Run `twitter-streams-producer-agent <config file>` for Twitter agent
Run `spark-submit myjarfile.jar` for Spark streaming app

Start Flume:
 /opt/mapr/flume/flume-1.6.0/bin/flume-ng agent -f conf/twitter_disk.conf -n TwitterDisk