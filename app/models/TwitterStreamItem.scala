package models

import org.joda.time.DateTime

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

case class Tweet(id: Long, createdAt: DateTime, hashtags: Seq[String], urls: Seq[String], text: String) extends TwitterStreamItem

object Tweet {

  implicit val rds: Reads[Tweet] = (
    (__ \ "id").read[Long] and
    (__ \ "created_at").read[DateTime] and
    (__ \ "entities" \ "hashtags").read[Seq[String]] and
    (__ \ "entities" \ "urls").read[Seq[String]] and
    (__ \ "text").read[String]
  )(Tweet.apply _)

}
