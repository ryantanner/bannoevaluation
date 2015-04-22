package controllers

import play.api.Play.current
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._

import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

import plugins._
import models._
import models.protocol._

object TwitterSample extends Controller {

  lazy val actors = ActorPlugin.component

  implicit val timeout = Timeout(5 seconds)

  def total = Action.async {
    for {
      total <- (actors.totalTweets ? RequestData).mapTo[TotalTweets]
    } yield {
      Ok(Json.toJson(total))
    }
  }

  def average = Action.async {
    for {
      average <- (actors.averageTweets ? RequestData).mapTo[AverageTweets]
    } yield {
      Ok(Json.toJson(average))
    }
  }

  def topEmojis = Action {
    NotImplemented
  }

  def percentContainingEmojis = Action {
    NotImplemented
  }

  def topHashtags = Action {
    NotImplemented
  }

  def percentContainingUrl = Action {
    NotImplemented
  }

  def percentContainingPhotoUrl = Action {
    NotImplemented
  }

  def topDomains = Action {
    NotImplemented
  }

}
