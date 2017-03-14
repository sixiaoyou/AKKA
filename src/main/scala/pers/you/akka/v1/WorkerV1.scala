package pers.you.akka.v1

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by You on 2017/3/12.
  */
class WorkerV1 extends Actor{

  //在Worker的构造器执行之后,Receive方法执行之前,执行一次preStart
  override def preStart(): Unit = {
    //跟Master方法建立连接
      val masterRef: ActorSelection = context.actorSelection("akka.tcp://MasterSystem@127.0.0.1:8888/user/AMaster")
      masterRef ! "hello"
  }

  override def receive: Receive = {
    case "hi" => {
      println("hi,received")
    }
  }
}

object WorkerV1 {
  def main(args: Array[String]): Unit = {
    val host="127.0.0.1"
    val port=6666
    //准备参数并解析参数
    val confStr =
    s"""
        |akka.actor.provider ="akka.remote.RemoteActorRefProvider"
        |akka.remote.netty.tcp.hostname = "$host"
        |akka.remote.netty.tcp.port = "$port"
      """.stripMargin
      val conf: Config = ConfigFactory.parseString(confStr)
      val workerActorSystem: ActorSystem = ActorSystem("WorkerSystem",conf)
    //创建ActorSystem
      val workerActor: ActorRef = workerActorSystem.actorOf(Props[WorkerV1])


    //通过ActorSystem创建Actor
      workerActorSystem.awaitTermination()
    //等待结束

  }
}
