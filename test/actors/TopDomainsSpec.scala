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

class TopDomainsActorSpec
  extends TestKit(ActorSystem("TopDomainsActorSpec"))
  with DefaultTimeout with ImplicitSender with ScalaFutures
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val defaultPatience =
    PatienceConfig(timeout =  Span(2, Seconds), interval = Span(5, Millis))

  import system.dispatcher

  override def afterAll {
    shutdown()
  }
 
  "An TopDomainsActor" should {
    "start with 0 tweets" in {
      val domainsRef = TestActorRef[TopDomainsActor]

      within(500 millis) {
        domainsRef ! RequestData
        expectMsg(TopDomains(Set.empty[String]))
      }
    }

    "return 1 domain after 1 tweet is received" in {
      val domainsRef = TestActorRef[TopDomainsActor]

      within(500 millis) {
        domainsRef ! sampleTweet
        domainsRef ! RequestData
        expectMsg(TopDomains(Set("scala-lang.org")))
      }
    }

    "return top domains (roughly)" in {
      val domainsRef = TestActorRef[TopDomainsActor]

      within(500 millis) {
        domainsRef ! sampleTweet
        domainsRef ! sampleTweet.copy(urls = Seq(new URL("http://google.com")))
        domainsRef ! sampleTweet.copy(urls = Seq(new URL("http://akka.io")))
        domainsRef ! sampleTweet.copy(urls = Seq(new URL("http://scala-lang.org")))
        domainsRef ! sampleTweet.copy(urls = Seq(new URL("http://playframework.com")))
        domainsRef ! RequestData
        expectMsg(TopDomains(Set("akka.io", "scala-lang.org", "google.com", "playframework.com")))
      }
    }

  }

}
 

