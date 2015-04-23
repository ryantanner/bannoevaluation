package actors

import akka.actor._

import com.twitter.algebird._

import java.net.URL

import models._
import protocol._

abstract class HeavyHitterActor[R](val name: String) extends TweetProcessor {

  // I have no idea why Algebird hasn't put this in their library
  implicit val hash: CMSHasher[String] = new CMSHasher[String] {
    def hash(a: Int, b: Int, width: Int)(x: String) = {
      val h = MurmurHash128(a.toLong << 32 | b.toLong)(x.getBytes)._1
      (h & Int.MaxValue).toInt % width
    }
  }

  def extractK(tweet: Tweet): Seq[String]

  def response(heavyHitters: Set[String]): R

  val approxTopK = TopPctCMS.aggregator[String](eps = 0.01, delta = 0.01, seed = 314, heavyHittersPct = 0.003)

  var topK: TopCMS[String] = approxTopK(Nil) // Initially returns TopPctCMSMonoid.zero[String]

  def process(tweet: Tweet) = {
    topK = topK ++ approxTopK(extractK(tweet))
  }

  val receiveRequests: Actor.Receive = {
    case RequestData => sender ! response(topK.heavyHitters)
  }

  def logStats {
    log.info(s"""
      Top $name heavy hitters:
        ${topK.heavyHitters.mkString(", ")}
    """)
  }

}

object HashtagActor {

  def props = Props[HashtagActor]

}

class HashtagActor extends HeavyHitterActor[TopHashtags]("Hashtag") {

  def extractK(tweet: Tweet) = tweet.hashtags

  def response(heavyHitters: Set[String]) = TopHashtags(heavyHitters)

}

object TopDomainsActor {

  def props = Props[TopDomainsActor]

}

class TopDomainsActor extends HeavyHitterActor[TopDomains]("Domains") {

  def extractK(tweet: Tweet) = tweet.domains

  def response(heavyHitters: Set[String]) = TopDomains(heavyHitters)

}

