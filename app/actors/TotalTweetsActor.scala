package actors

import akka.actor._

import models._
import protocol._

object TotalTweetsActor {

  def props = Props[TotalTweetsActor]

}

class TotalTweetsActor extends Actor with ActorLogging {

  var totalTweets = 0

  def receive = {
    case _: Tweet => totalTweets += 1
    case RequestData => sender ! TotalTweets(totalTweets)
    case Log => log.info(s"\n\tTotal tweets: $totalTweets")
  }

}
