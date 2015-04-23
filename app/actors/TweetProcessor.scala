package actors

import akka.actor._

import models._
import protocol._

trait TweetProcessor extends Actor with ActorLogging {

  val name: String

  def logStats: Unit

  def process(tweet: Tweet): Unit
  
  val receiveRequests: Receive
  
  def receive: Receive = {
    case tweet: Tweet => process(tweet)
    case Log => logStats
    case Finalize => log.info(s"Stopping $name, final stats: "); logStats
    case x => if (receiveRequests.isDefinedAt(x)) receiveRequests(x) else unhandled(x)
  }

}

