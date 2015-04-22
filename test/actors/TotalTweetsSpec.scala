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

class TotalTweetsActorSpec
  extends TestKit(ActorSystem("TotalTweetsActorSpec"))
  with DefaultTimeout with ImplicitSender with ScalaFutures
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val defaultPatience =
    PatienceConfig(timeout =  Span(2, Seconds), interval = Span(5, Millis))

  import system.dispatcher

  override def afterAll {
    shutdown()
  }
 
  "An TotalTweetsActor" should {
    "start with 0 tweets" in {
      val totalTweetsRef = TestActorRef[TotalTweetsActor]

      within(500 millis) {
        totalTweetsRef ! RequestData
        expectMsg(TotalTweets(0))
      }
    }

    "return 1 after 1 tweet is received" in {
      val totalTweetsRef = TestActorRef[TotalTweetsActor]

      within(500 millis) {
        totalTweetsRef ! sampleTweet
        totalTweetsRef ! RequestData
        expectMsg(TotalTweets(1))
      }
    }

    "return 5 after 5 tweets received" in {
      val totalTweetsRef = TestActorRef[TotalTweetsActor]

      within(500 millis) {
        totalTweetsRef ! sampleTweet
        totalTweetsRef ! sampleTweet
        totalTweetsRef ! sampleTweet
        totalTweetsRef ! sampleTweet
        totalTweetsRef ! sampleTweet
        totalTweetsRef ! RequestData
        expectMsg(TotalTweets(5))
      }
    }

  }

}
 

