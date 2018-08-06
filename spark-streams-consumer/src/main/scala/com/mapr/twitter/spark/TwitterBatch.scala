package com.mapr.twitter.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._

object TwitterBatch {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("SparkStream").setMaster("local[*]") //.setMaster("yarn")
    val mysc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.SQLContext(mysc)
    import sqlContext.implicits._

    val df = sqlContext.read.json("file:///Users/chufe/Documents/twitter/*.json")
    df.registerTempTable("twitter")
    /*
    val result = df.groupBy("lang").count()
//    df.printSchema()
//    result.printSchema()
//    result.explain()
//    print(result.collect())
    result.orderBy(desc("count")).show()
    result.sort($"count".desc).show()
//    result.orderBy("count").toJSON.collect().foreach(println)
*/
    val topCountries = sqlContext.sql("SELECT lang, count(*) as num FROM twitter group by lang order by num desc")
    val out = topCountries.where($"num" > 100).coalesce(1)
    out.explain()
    out.write.json("file:///Users/chufe/Documents/workspaces/twitter-streams/abc")
    val res = mysc.wholeTextFiles("file:///Users/chufe/Documents/workspaces/twitter-streams/abc")
    res.collect().foreach(println)
  }


}