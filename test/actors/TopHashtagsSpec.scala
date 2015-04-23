package test.actors

import actors._
import models._

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.time._
import org.scalatest.concurrent.ScalaFutures
 
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.DefaultTimeout
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.testkit.TestActorRef

import play.api.libs.iteratee._
import play.api.libs.json._

import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.collection.immutable

import models.protocol._
import helpers.Samples._

class HashtagActorSpec
  extends TestKit(ActorSystem("HashtagActorSpec"))
  with DefaultTimeout with ImplicitSender with ScalaFutures
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val defaultPatience =
    PatienceConfig(timeout =  Span(2, Seconds), interval = Span(5, Millis))

  import system.dispatcher

  override def afterAll {
    shutdown()
  }
 
  "An HashtagActor" should {
    "start with 0 tweets" in {
      val hashtagRef = TestActorRef[HashtagActor]

      within(500 millis) {
        hashtagRef ! RequestData
        expectMsg(TopHashtags(Set.empty[String]))
      }
    }

    "return 1 hashtag after 1 tweet is received" in {
      val hashtagRef = TestActorRef[HashtagActor]

      within(500 millis) {
        hashtagRef ! sampleTweet
        hashtagRef ! RequestData
        expectMsg(TopHashtags(Set("akka")))
      }
    }

    "return top hashtags (roughly)" in {
      val hashtagRef = TestActorRef[HashtagActor]

      within(500 millis) {
        hashtagRef ! sampleTweet
        hashtagRef ! sampleTweet.copy(urls = Nil)
        hashtagRef ! sampleTweet.copy(hashtags = Seq("akka", "scala"))
        hashtagRef ! sampleTweet.copy(hashtags = Seq("banno", "scala"))
        hashtagRef ! sampleTweet.copy(hashtags = Seq("battlestargalactica", "akka"))
        hashtagRef ! sampleTweet.copy(hashtags = Seq("banno", "akka"))
        hashtagRef ! RequestData
        expectMsg(TopHashtags(Set("akka", "scala", "banno", "battlestargalactica")))
      }
    }

  }

}
 

