package actors

import akka.actor._

import com.vdurmont.emoji._

import scala.collection.mutable.{ Map => MutableMap }

import models._
import protocol._

object URLActor {

  def props = Props[URLActor]

}

class URLActor extends TweetProcessor {

  val name = "URLActor"

  var totalTweets = 0
  var tweetsWithURLs = 0
  var tweetsWithPhotoURLs = 0

  def process(tweet: Tweet) = { 
    totalTweets += 1

    if (tweet.urls.nonEmpty)
      tweetsWithURLs += 1

    val photoDomains = tweet.domains.filter(d => d == "pic.twitter.com" || d == "instagram.com")

    if (photoDomains.nonEmpty)
      tweetsWithPhotoURLs += 1
  }

  def percentContainingURLs = 
    if (totalTweets == 0)
      0.0
    else
      tweetsWithURLs / (totalTweets * 1.0)

  def percentContainingPhotoURLs = 
    if (totalTweets == 0)
      0.0
    else
      tweetsWithPhotoURLs / (totalTweets * 1.0)

  def logStats {
    log.info(f"""
      Percent of tweets containing URLs: $percentContainingURLs%2.5f%%
      Percent of tweets containing photo URLs: $percentContainingPhotoURLs%2.5f%%
    """)
  }

  val receiveRequests: Actor.Receive = {
    case RequestData => 
      sender ! URLStats(percentContainingURLs, percentContainingPhotoURLs)
  }

}


