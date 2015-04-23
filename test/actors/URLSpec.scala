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

import java.net.URL

import scala.concurrent.duration._
import scala.collection.immutable

import models.protocol._
import helpers.Samples._

class URLActorSpec
  extends TestKit(ActorSystem("URLActorSpec"))
  with DefaultTimeout with ImplicitSender with ScalaFutures
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val defaultPatience =
    PatienceConfig(timeout =  Span(2, Seconds), interval = Span(5, Millis))

  import system.dispatcher

  override def afterAll {
    shutdown()
  }
 
  "An URLActor" should {
    "start with 0 tweets" in {
      val urlRef = TestActorRef[URLActor]

      within(500 millis) {
        urlRef ! RequestData
        expectMsg(URLStats(0.0, 0.0))
      }
    }

    "return 1.0/0.0 after 1 tweet with non-photo domain is received" in {
      val urlRef = TestActorRef[URLActor]

      within(500 millis) {
        urlRef ! sampleTweet
        urlRef ! RequestData
        expectMsg(URLStats(1.0, 0.0))
      }
    }

    "return 1.0/1.0 after 1 tweet with photo domain is received" in {
      val urlRef = TestActorRef[URLActor]

      within(500 millis) {
        urlRef ! sampleTweet.copy(urls = Seq(new URL("http://pic.twitter.com")))
        urlRef ! RequestData
        expectMsg(URLStats(1.0, 1.0))
      }
    }

    "return 1.0/0.5 after 2 tweets with 1 photo and 1 non-photo domain are received" in {
      val urlRef = TestActorRef[URLActor]

      within(500 millis) {
        urlRef ! sampleTweet.copy(urls = Seq(new URL("http://pic.twitter.com")))
        urlRef ! sampleTweet
        urlRef ! RequestData
        expectMsg(URLStats(1.0, 0.5))
      }
    }

    "return 1.0/1.0 after 1 tweets with 1 photo and 1 non-photo domain is received" in {
      val urlRef = TestActorRef[URLActor]

      within(500 millis) {
        urlRef ! sampleTweet.copy(urls = Seq(new URL("http://pic.twitter.com"), new URL("http://twitter.com")))
        urlRef ! RequestData
        expectMsg(URLStats(1.0, 1.0))
      }
    }

    "return 0.5/0.0 after 2 tweets with 1 photo domain are received" in {
      val urlRef = TestActorRef[URLActor]

      within(500 millis) {
        urlRef ! sampleTweet.copy(urls = Nil)
        urlRef ! sampleTweet.copy(urls = Seq(new URL("http://twitter.com")))
        urlRef ! RequestData
        expectMsg(URLStats(0.5, 0.0))
      }
    }

  }

}
 

