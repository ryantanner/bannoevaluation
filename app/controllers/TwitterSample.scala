package controllers

import play.api.Play.current
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.ClassTag

import plugins._
import models._
import models.protocol._

object TwitterSample extends Controller {

  lazy val actors = ActorPlugin.component

  implicit val timeout = Timeout(5 seconds)

  def total = Action.async {
    request[TotalTweets](actors.totalTweets)
  }

  def average = Action.async {
    request[AverageTweets](actors.averageTweets)
  }

  def emojis(count: Int) = Action.async {
    request[EmojiStats](actors.emojis, RequestEmojis(count))
  }

  def topHashtags = Action.async {
    request[TopHashtags](actors.hashtags)
  }

  def urlStats = Action.async {
    request[URLStats](actors.urls)
  }

  def topDomains = Action.async {
    request[TopDomains](actors.topDomains)
  }

  def stop = Action {
    actors.stop()
    Ok
  }

  private def request[T : ClassTag : Writes](ref: ActorRef, req: Any = RequestData): Future[Result] = {
    for {
      data <- (ref ? req).mapTo[T]
    } yield {
      Ok(Json.toJson(data))
    }
  }

}
