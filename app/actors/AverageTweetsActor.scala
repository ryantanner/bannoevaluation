package actors

import akka.actor._

import scala.collection.mutable.{ Map => MutableMap }

import models._
import protocol._

object AverageTweetsActor {

  def props = Props[AverageTweetsActor]

}

class AverageTweetsActor extends Actor with ActorLogging {

  type Second = Long
  type Count  = Int

  /*
  * Storing an unbounded count of all tweets per second
  * forever is a tradeoff based on two facts:
  *   1. Twitter's API does not guarantee order, so keeping a 
  *      running average per period would be inaccurate
  *   2. This map takes rougly 1MB memory per day (12 bytes per pair,
  *      60 pairs per minute, 60 pairs per hour, 24 hours per day), so
  *      even after a week, this would take less than 7.5MB.
  *
  * In the real world, there are a number of other options that would
  * decrease memory usage here.  A running average could be used with the
  * caveat that we would be tracking average tweets *received* per period 
  * rather than average tweets *tweeted*.  There are also probabilistic
  * data structures such as loglog counters which could be used to estimate
  * cardinality per period while using exponentially less memory.  This approach
  * was chosen for simplicity.
  */
  val tweetsPerSeconds = MutableMap.empty[Second, Count].withDefaultValue(0)

  def update(tweet: Tweet) = {
    val secondsSinceGenesis = (tweet.timestamp / 1000) 

    tweetsPerSeconds.update(secondsSinceGenesis,
      tweetsPerSeconds(secondsSinceGenesis) + 1)
  }

  def average = {
    if (tweetsPerSeconds.isEmpty)
      0.0
    else 
      tweetsPerSeconds.map {
        case (_, count) => count
      }.sum / (tweetsPerSeconds.size * 1.0)
  }

  def receive = {
    case tweet: Tweet => update(tweet)
    case RequestData  => sender ! AverageTweets(average)
    case Log => log.info(s"""
      Average tweets per second: $average
      Average tweets per minute: ${average * 60}
      Average tweets per hour:   ${average * 60 * 60}
    """)
  }

}

