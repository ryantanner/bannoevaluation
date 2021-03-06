package models

import org.joda.time.DateTime

import java.net.URL

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._ 

trait TwitterStreamItem

object TwitterStreamItem {

  // Because Reads isn't covariant, we have to map
  // each Reads[_] to the base type of TwitterStreamItem
  implicit val reads: Reads[TwitterStreamItem] = 
    of[Tweet].map(identity[TwitterStreamItem]) orElse 
    of[StallWarning].map(identity[TwitterStreamItem]) orElse 
    of[Disconnect].map(identity[TwitterStreamItem])

}

case class Disconnect(code: Int, streamName: String, reason: String) extends TwitterStreamItem

object Disconnect {

  implicit val rds: Reads[Disconnect] = (
    (__ \ "code").read[Int] and
    (__ \ "stream_name").read[String] and
    (__ \ "reason").read[String]
  )(Disconnect.apply _)

}

case class StallWarning(code: String, message: String, percentFull: Int) extends TwitterStreamItem

object StallWarning {

  implicit val rds: Reads[StallWarning] = (
    (__ \ "code").read[String] and
    (__ \ "message").read[String] and
    (__ \ "percent_full").read[Int]
  )(StallWarning.apply _)

}

case class Hashtag(text: String)

object Hashtag {

  implicit val rds = Json.reads[Hashtag]

}

case class Tweet(id: Long, timestamp: Long, hashtags: Seq[String], urls: Seq[URL], text: String) extends TwitterStreamItem {
  lazy val domains = urls.map(_.getHost)
}

object Tweet {

  implicit val urlReads: Reads[URL] = 
    (__ \ "expanded_url").read[String].map(new URL(_))

  val hashtagText = (__ \ "text").read[String]

  implicit val rds: Reads[Tweet] = (
    (__ \ "id").read[Long] and
    (__ \ "timestamp_ms").read[String].map(_.toLong) and
    (__ \ "entities" \ "hashtags").read[Seq[String]](seq(hashtagText)) and
    (__ \ "entities" \ "urls").read[Seq[URL]] and
    (__ \ "text").read[String]
  )(Tweet.apply _)

}
