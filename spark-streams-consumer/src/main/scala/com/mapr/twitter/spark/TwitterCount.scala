package com.mapr.twitter.spark

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.v09.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.opentsdb.client.builder.MetricBuilder
import org.opentsdb.client.{ExpectResponse, HttpClientImpl}


object TwitterCount {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("SparkStream") //.setMaster("yarn")
    val mysc = new SparkContext(conf)
    val kafkaParams = Map[String, String](
      ConsumerConfig.GROUP_ID_CONFIG -> "sparkgroup",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "false",
      "spark.kafka.poll.time" -> "1000")
    // KafkaUtils.createStream(...)

    val topics = Set("/user/mapr/twitter:samples")
    val ssc = new StreamingContext(mysc, Seconds(5))
    val stream: InputDStream[(String, String)] = KafkaUtils.createDirectStream(ssc, kafkaParams, topics)
    val cc = stream.count()

    cc.foreachRDD(_.foreach(data => {
      println("Num " + data)

      val builder = MetricBuilder.getInstance()
      builder.addMetric("twitter.count.5sec").setDataPoint(System.currentTimeMillis(), data).addTag("unit", "count.5sec")

      try {
        val client = new HttpClientImpl("http://10.0.0.195:4242")
        val result = client.pushMetrics(builder, ExpectResponse.SUMMARY)
        println(result)
      } catch {
        case e: Exception => println(e)
      }
    }))

    val sqlContext = new org.apache.spark.sql.SQLContext(mysc)

    // https://spark.apache.org/docs/1.6.1/sql-programming-guide.html#json-datasets
    stream.foreachRDD(rdd => {
      val df = sqlContext.read.json(rdd.map(_._2))
      val result = df.groupBy("lang").count()
      result.foreach(f => {
        val country: String = f.getAs("lang")
        val count: Long = f.getAs("count")
        val builder = MetricBuilder.getInstance()
        builder.addMetric("twitter.countryall.count.5sec").setDataPoint(System.currentTimeMillis(), count).addTag("country", country.toUpperCase())

        try {
          val client = new HttpClientImpl("http://10.0.0.195:4242")
          val result = client.pushMetrics(builder, ExpectResponse.SUMMARY)
          println(country.toUpperCase() + " => " + count)
        } catch {
          case e: Exception => println(e)
        }
      })
      // df.show()

    })

    ssc.start()
    ssc.awaitTermination()
    ssc.stop(stopSparkContext = true, stopGracefully = true)
  }



}