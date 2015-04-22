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

import helpers.Samples._

class StreamProcessorActorSpec
  extends TestKit(ActorSystem("StreamProcessorActorSpec"))
  with DefaultTimeout with ImplicitSender with ScalaFutures
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val streamProcessorRef = TestActorRef(StreamProcessorActor.props(testActor))
  val streamProcessor = streamProcessorRef.underlyingActor.asInstanceOf[StreamProcessorActor]

  implicit val defaultPatience =
    PatienceConfig(timeout =  Span(2, Seconds), interval = Span(5, Millis))

  import StreamProcessorActor._
  import system.dispatcher

  override def afterAll {
    shutdown()
  }
 
  "A StreamProcessorActor" should {
    "forward tweets to subscribers" in {
      within(500 millis) {
        Enumerator(sampleTweet)(streamProcessor.publishTweets)
        expectMsg(sampleTweet)
      }
    }

    "filter blank lines" in {
      val e = Enumerator("\r\n", "asdf")
      val list = e through filterBlankLines run Iteratee.getChunks
      list.futureValue should have length(1)
      list.futureValue should contain("asdf")
    }

    "parse JSON, ignoring malformed input" in {
      val e = Enumerator("asdf", "{}")
      val list = e through parseJson run Iteratee.getChunks
      list.futureValue should have length(1)
      list.futureValue should contain(Json.obj())
    }

  }

}
 
