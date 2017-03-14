package pers.you.akka.v2

/**
  * Created by You on 2017/3/13.
  */
class WorkerInfo(val id: String,val memory: Int,val cores: Int) {
  var lastHeartBeatTime: Long = _

}
