package actors

import akka.actor._

import models._
import protocol._

object TotalTweetsActor {

  def props = Props[TotalTweetsActor]

}

class TotalTweetsActor extends TweetProcessor {

  val name = "TotalTweetsActor"

  var totalTweets = 0

  def process(tweet: Tweet) = {
    totalTweets += 1
  }

  val receiveRequests: Actor.Receive = {
    case RequestData => sender ! TotalTweets(totalTweets)
  }

  def logStats {
    log.info(s"\n      Total tweets: $totalTweets")
  }

}
