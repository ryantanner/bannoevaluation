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

class AverageTweetsActorSpec
  extends TestKit(ActorSystem("AverageTweetsActorSpec"))
  with DefaultTimeout with ImplicitSender with ScalaFutures
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val defaultPatience =
    PatienceConfig(timeout =  Span(2, Seconds), interval = Span(5, Millis))

  import system.dispatcher

  override def afterAll {
    shutdown()
  }
 
  "An AverageTweetsActor" should {
    "start with 0 tweets" in {
      val averageTweetsRef = TestActorRef[AverageTweetsActor]

      within(500 millis) {
        averageTweetsRef ! RequestData
        expectMsg(AverageTweets(0.0))
      }
    }

    "return 1.0 after 1 tweet is received" in {
      val averageTweetsRef = TestActorRef[AverageTweetsActor]

      within(500 millis) {
        averageTweetsRef ! sampleTweet
        averageTweetsRef ! RequestData
        expectMsg(AverageTweets(1.0))
      }
    }

    "return 1.5 after 3 tweets received over 2 seconds" in {
      val averageTweetsRef = TestActorRef[AverageTweetsActor]

      within(500 millis) {
        averageTweetsRef ! sampleTweet
        averageTweetsRef ! sampleTweet.copy(createdAt = sampleTweet.createdAt.minusSeconds(1))
        averageTweetsRef ! sampleTweet.copy(createdAt = sampleTweet.createdAt.minusSeconds(1))
        averageTweetsRef ! RequestData
        expectMsg(AverageTweets(1.5))
      }
    }

  }

}
 

