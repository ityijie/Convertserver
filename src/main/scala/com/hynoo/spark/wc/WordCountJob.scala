package com.hynoo.spark.wc

import org.apache.spark.Logging
import org.apache.spark.sql.SQLContext

/**
 * WordCount业务线Job
 */
object WordCountJob extends Logging{

  val input = "hdfs://namenode1:8020/user/flume/Test/wc.txt"
  val output = "hdfs://namenode1:8020/user/flume/Test/wcout.txt"

  def doJob(parentContext: SQLContext):String = {
    var response = "success"

    var sqlContext = parentContext.newSession()
    try {
      sqlContext.sparkContext.textFile(input)
        .flatMap(x=>x.split(",")).map((_,1)).reduceByKey(_+_).coalesce(1)
        .saveAsTextFile(output)
    } catch {
      case e:Exception => {
        logError("Execute Failure", e)
        response = "Execute Failure:" + e.getMessage
      }
    } finally {
      if (sqlContext != null) {
        sqlContext.clearCache()
        sqlContext = null
      }
    }
    response
  }
}
