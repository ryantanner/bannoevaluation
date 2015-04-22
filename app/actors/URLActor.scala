package actors

import akka.actor._

import play.api.Logger

import com.vdurmont.emoji._

import scala.collection.mutable.{ Map => MutableMap }

import models._
import protocol._

object URLActor {

  def props = Props[URLActor]

}

class URLActor extends Actor {

  var totalTweets = 0
  var tweetsWithURLs = 0
  var tweetsWithPhotoURLs = 0

  def update(tweet: Tweet) = { 
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

  def receive = {
    case tweet: Tweet => update(tweet)
    case RequestData => 
      sender ! URLStats(percentContainingURLs, percentContainingPhotoURLs)
  }

}


