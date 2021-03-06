package com.hynoo.spark

import java.io.OutputStream
import java.net.InetSocketAddress
import java.util.concurrent.Executors

import com.sun.net.httpserver.{Headers, HttpExchange, HttpHandler, HttpServer}
import org.apache.hadoop.fs.FileSystem
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Administrator on 2018/1/29.
  */
object ConvertServer {

  def main(args: Array[String]) {

    val sparkConf = new SparkConf().setAppName("ConvertServer")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    val fileSystem = FileSystem.get(sc.hadoopConfiguration)

    createServer(9999, sqlContext, fileSystem)
  }

  def createServer(port: Int, sqlContext: SQLContext, fileSystem: FileSystem): Unit = {

    val httpServer = HttpServer.create(new InetSocketAddress(port), 30)
    httpServer.setExecutor(Executors.newCachedThreadPool())
    httpServer.createContext("/convert", new HttpHandler() {
      override def handle(httpExchange: HttpExchange): Unit = {
        System.out.println("处理新请求:" + httpExchange.getRequestMethod + " , " + httpExchange.getRequestURI)

        var response = "成功"
        var httpCode = 200

        val responseHeaders: Headers = httpExchange.getResponseHeaders
        responseHeaders.set("Content-Type", "text/html;charset=utf-8")

        // 调用SparkSQL的方法进行测试
        try {
          //处理逻辑
          //sqlContext.read.format("json").load("file:///Users/rocky/data/people.json").show
          //sqlContext.read.format("json").load("file:///D:\\sparktest\\people.json").show
          sqlContext.read.format("json").load("hdfs://namenode1:8020/user/flume/Test/people.json").show
          //WordCountJob.doJob(sqlContext)
        } catch {
          case e: Exception =>
            e.printStackTrace()
            response = "失败"
            httpCode = 500
        }

        httpExchange.sendResponseHeaders(httpCode, response.getBytes.length)
        val responseBody: OutputStream = httpExchange.getResponseBody
        responseBody.write(response.getBytes)
        responseBody.flush
        responseBody.close
      }

      httpServer.createContext("/ping", new HttpHandler() {
        override def handle(httpExchange: HttpExchange): Unit = {
          var response = "存活"
          var httpCode = 200

          try {
            if (sqlContext.sparkContext.isStopped) {
              httpCode = 400
              response = "spark终止"
            }
          } catch {
            case e: Exception =>
              httpCode = 500
              response = "服务异常"
          } finally {
            httpExchange.sendResponseHeaders(httpCode, response.getBytes().length)
            val out = httpExchange.getResponseBody //获得输出流
            out.write(response.getBytes())
            out.flush()
            httpExchange.close()
          }
        }
      })


      /**
        * 停止sc测试
        */
      httpServer.createContext("/stop_sc", new HttpHandler() {
        override def handle(httpExchange: HttpExchange): Unit = {
          var response = "成功"
          var httpCode = 200

          try {
            sqlContext.sparkContext.stop()
          } catch {
            case e: Exception =>
              httpCode = 500
              response = "服务异常"
          } finally {
            httpExchange.sendResponseHeaders(httpCode, response.getBytes().length)
            val out = httpExchange.getResponseBody //获得输出流
            out.write(response.getBytes())
            out.flush()
            httpExchange.close()
          }
        }
      })

      httpServer.start()
      println("ConvertServer started " + port + " ......")
    })
  }


}
