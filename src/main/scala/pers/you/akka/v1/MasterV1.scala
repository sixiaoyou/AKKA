package pers.you.akka.v1

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by You on 2017/3/12.
  */
class MasterV1 extends Actor{


//  重写未实现的方法:Ctrl+I;重写已经实现的方法:Ctrl+O
  //接收消息的方法,Master等待Worker发送消息
  override def receive: Receive = {
    //
    case "hello" => {
      println("hello received")
      //一个感叹号代表异步消息
      sender ! "hi"
    }
  }
}

object MasterV1 {
  def main(args: Array[String]): Unit = {
    //Master 需要一些参数,以后可以写入配置文件中
    val host = "127.0.0.1"
    val port = "8888"

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
    val masterActorSystem: ActorSystem = ActorSystem("MasterSystem",conf)
    //通过ActorSystem创建Actor
    masterActorSystem.actorOf(Props(classOf[MasterV1]),"AMaster")
    //启动ActorSystem -> 创建 Actor
    //等待结束
    masterActorSystem.awaitTermination()

  }
}