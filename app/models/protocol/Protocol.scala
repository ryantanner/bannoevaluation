package models.protocol

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._ 

case object RequestData

case class TotalTweets(total: Int)

object TotalTweets {

  implicit val fmt = Json.format[TotalTweets]

}

case class AverageTweets(perSecond: Double) {
  lazy val perMinute = perSecond * 60
  lazy val perHour   = perMinute * 60
}

object AverageTweets {

  implicit val writes: Writes[AverageTweets] = Writes {
    (averageTweets: AverageTweets) => Json.obj(
      "average_per_second" -> averageTweets.perSecond,
      "average_per_minute" -> averageTweets.perMinute,
      "average_per_hour"   -> averageTweets.perHour
    )
  }

}

case class RequestEmojis(count: Int)

case class EmojiStats(percentContaining: Double, topEmojis: Map[String, Int])

object EmojiStats {

  implicit val writes = (
    (__ \ "percent_containing_emojis").write[Double] ~
    (__ \ "top_emojis").write[Map[String, Int]]
  )(unlift(EmojiStats.unapply))

}

case class TopHashtags(heavyHitters: Set[String])

object TopHashtags {

  implicit val writes: Writes[TopHashtags] = 
    (__ \ "top_hashtags").write[Set[String]].contramap(_.heavyHitters)

}
