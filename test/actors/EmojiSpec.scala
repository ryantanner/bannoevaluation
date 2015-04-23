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

class EmojiActorSpec
  extends TestKit(ActorSystem("EmojiActorSpec"))
  with DefaultTimeout with ImplicitSender with ScalaFutures
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val defaultPatience =
    PatienceConfig(timeout =  Span(2, Seconds), interval = Span(5, Millis))

  import system.dispatcher

  override def afterAll {
    shutdown()
  }
 
  "An EmojiActor" should {
    "start with 0 tweets" in {
      val emojiRef = TestActorRef[EmojiActor]

      within(500 millis) {
        emojiRef ! RequestEmojis(10)
        expectMsg(EmojiStats(0.0, Map.empty))
      }
    }

    "return 1 after 1 tweet is received" in {
      val emojiRef = TestActorRef[EmojiActor]

      within(500 millis) {
        emojiRef ! emojiTweet()
        emojiRef ! RequestEmojis(10)
        expectMsg(EmojiStats(1.0, Map(emojis.head.getUnicode -> 1)))
      }
    }

    "return 5 after 5 tweets received" in {
      val emojiRef = TestActorRef[EmojiActor]

      within(500 millis) {
        emojiRef ! emojiTweet(emojis(0))
        emojiRef ! emojiTweet(emojis(1))
        emojiRef ! emojiTweet(emojis(2))
        emojiRef ! emojiTweet(emojis(3))
        emojiRef ! emojiTweet(emojis(4))
        emojiRef ! RequestEmojis(10)
        expectMsg(EmojiStats(1.0, Map(
          emojis(0).getUnicode -> 1,
          emojis(1).getUnicode -> 1,
          emojis(2).getUnicode -> 1,
          emojis(3).getUnicode -> 1,
          emojis(4).getUnicode -> 1
        )))
      }
    }

    "return max of $count emojis" in {
      val emojiRef = TestActorRef[EmojiActor]

      within(500 millis) {
        emojiRef ! emojiTweet(emojis(0))
        emojiRef ! emojiTweet(emojis(0))
        emojiRef ! emojiTweet(emojis(2))
        emojiRef ! emojiTweet(emojis(3))
        emojiRef ! emojiTweet(emojis(4))
        emojiRef ! RequestEmojis(1)
        expectMsg(EmojiStats(1.0, Map(
          emojis(0).getUnicode -> 2
        )))
      }
    }

  }

}
 

