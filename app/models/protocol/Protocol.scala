package models.protocol

import play.api.libs.json._

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
