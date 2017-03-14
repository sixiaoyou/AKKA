package pers.you.akka.v2

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * Created by You on 2017/3/12.
  */
class MasterV2(val host: String,val port: Int) extends Actor{

  //存放work的ID和WorkerInfo的一个HashMap
  val idToWorker = new mutable.HashMap[String,WorkerInfo]()
  //存放workerInfo的一个HashSet
  val workers = new mutable.HashSet[WorkerInfo]()

  val CHECK_INTERVAL = 15000

  override def preStart(): Unit = {
    //启动定时器：定期检测超时的Worker
    //导入隐式转换
    import context.dispatcher
    context.system.scheduler.schedule(0 millis,CHECK_INTERVAL millis,self,CheckTimeOutWorker)
  }

  //  重写未实现的方法:Ctrl+I;重写已经实现的方法:Ctrl+O
  //接收消息的方法,Master等待Worker发送消息
  override def receive: Receive = {
    //Worker发送给Maste的注册消息
    case RegisterWorker(id,memory,cores) => {
      //封装消息
      val workerInfo = new WorkerInfo(id,memory,cores)
      //保存workerInfo
        idToWorker(id) = workerInfo
        workers += workerInfo
      //Master向Worker反馈消息，告诉Worker注册成功了
        sender ! RegisteredWorker(s"akka.tcp://${MasterV2.MASTER_SYSTEM}@$host:$port")
    }
    //Woker发送给Master的心跳信息，只是为了报活
    case HeartBeat(workerId) => {
      //更新WorkerInfo的上一次心跳时间
      val currentTime = System.currentTimeMillis()
      //查找对应Worker的信息
      val workerInfo = idToWorker(workerId)
      //更新WokerInfo的上一次心跳时间
      workerInfo.lastHeartBeatTime = currentTime
    }
      //检测超时的Worker
    case CheckTimeOutWorker => {
      val currentTime = System.currentTimeMillis()
      //过滤出超时的Worker
      val deadWorkers  = workers.filter(w => currentTime - w.lastHeartBeatTime > CHECK_INTERVAL)
//      for(deadWorker <- deadWorkers){
//        idToWorker -= deadWorker.id
//        workers -= deadWorker
//      }
      deadWorkers.foreach(dw => {
      idToWorker -= dw.id
        workers -= dw
      })
      println(s"alive woker numbers: ${workers.size}")
    }
  }
}

object MasterV2 {

  val MASTER_SYSTEM = "MasterSystem"
  val MASTER_NAME = "Master"

  def main(args: Array[String]): Unit = {
    //Master 需要一些参数,以后可以写入配置文件中
    val host = args(0)
    val port = args(1).toInt

    //准备配置字符串
    val confStr=
      s"""
        |   akka.actor.provider = "akka.remote.RemoteActorRefProvider"
        |   akka.remote.netty.tcp.hostname = "$host"
        |   akka.remote.netty.tcp.port = "$port"
      """.stripMargin

    //解析配置字符串
    val conf: Config = ConfigFactory.parseString(confStr)
    //创建ActorSystem,ActorSystem是单例的，只要有一个即可
    val masterActorSystem: ActorSystem = ActorSystem(MASTER_SYSTEM,conf)
    //通过ActorSystem创建Actor
    masterActorSystem.actorOf(Props(new MasterV2(host,port)),MASTER_NAME)
    //启动ActorSystem -> 创建 Actor
    //等待结束
    masterActorSystem.awaitTermination()

  }
}