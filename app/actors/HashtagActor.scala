package actors

import akka.actor._

import com.twitter.algebird._

import models._
import protocol._

object HashtagActor {

  def props = Props[HashtagActor]

  // I have no idea why Algebird hasn't put this in their library
  implicit val hash: CMSHasher[String] = new CMSHasher[String] {
    def hash(a: Int, b: Int, width: Int)(x: String) = {
      val h = MurmurHash128(a.toLong << 32 | b.toLong)(x.getBytes)._1
      (h & Int.MaxValue).toInt % width
    }
  }

}

class HashtagActor extends Actor {

  import HashtagActor._

  val approxTopK = TopPctCMS.aggregator[String](eps = 0.01, delta = 0.01, seed = 314, heavyHittersPct = 0.003)

  var topHashtags: TopCMS[String] = approxTopK(Nil) // Initially returns TopPctCMSMonoid.zero[String]

  def receive = {
    case tweet: Tweet if tweet.hashtags.nonEmpty => 
      topHashtags = topHashtags ++ approxTopK(tweet.hashtags)
    case RequestData  => sender ! TopHashtags(topHashtags.heavyHitters)
  }

}

