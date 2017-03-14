package pers.you.akka.v2

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import scala.concurrent.duration._
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by You on 2017/3/12.
  */
class WorkerV2(val masterHost: String,val masterPort: Int,val memory: Int,val cores: Int) extends Actor{
  val workerId = UUID.randomUUID().toString
  var masterUrl: String = _
  var masterRef: ActorSelection= _
  //在Worker的构造器执行之后,Receive方法执行之前,执行一次preStart
  override def preStart(): Unit = {
    //跟Master方法建立连接
      masterRef = context.actorSelection(s"akka.tcp://${MasterV2.MASTER_SYSTEM}@$masterHost:$masterPort/user/${MasterV2.MASTER_NAME}")
      //发送注册消息给Master
      masterRef ! RegisterWorker(workerId,memory,cores)
  }

  override def receive: Receive = {
    //Master返回给Worker的消息，告诉worker注册成功了
    case RegisteredWorker(masterUrl) => {
      this.masterUrl = masterUrl
      import context.dispatcher
      //启动一个定时器，定期向Master发送心跳
      context.system.scheduler.schedule(0 millis,10000 millis,self,SendHeartBeat)
    }

      //发给自己的消息
    case SendHeartBeat => {
      //给Maste发送心跳
      masterRef ! HeartBeat(workerId)
    }
  }
}

object WorkerV2{
  val WORKER_SYSTEM = "WorkerSystem"
  val WORKER_NAME = "Worker"

  def main(args: Array[String]): Unit = {
    val host=args(0)
    val masterHost = args(1)
    val masterPort = args(2).toInt
    val memory = args(3).toInt
    val cores = args(4).toInt
    //准备参数并解析参数
    val confStr =
    s"""
        |akka.actor.provider ="akka.remote.RemoteActorRefProvider"
        |akka.remote.netty.tcp.hostname = "$host"
      """.stripMargin
      val conf: Config = ConfigFactory.parseString(confStr)
      val workerActorSystem: ActorSystem = ActorSystem(WORKER_SYSTEM,conf)
    //创建ActorSystem
      val workerActor: ActorRef = workerActorSystem.actorOf(Props(new WorkerV2(masterHost,masterPort,memory,cores)),WORKER_NAME)


    //通过ActorSystem创建Actor
      workerActorSystem.awaitTermination()
    //等待结束

  }
}
