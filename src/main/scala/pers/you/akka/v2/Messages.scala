package pers.you.akka.v2

/**
  * Created by You on 2017/3/13.
  */
trait RemoteMessages extends Serializable

//Worker发送给Master的注册消息
case class RegisterWorker(id: String,memory: Int,cores: Int) extends RemoteMessages

//Master发送给Worker的注册成功反馈
case class RegisteredWorker(masterUrl: String)

//Worker向Master发送的消息
case class HeartBeat(workerId: String)

//Worker发送给自己的内部消息
case object SendHeartBeat

//Master发送给自己的内部消息
case object CheckTimeOutWorker

