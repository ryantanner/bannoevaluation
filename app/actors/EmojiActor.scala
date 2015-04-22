package actors

import akka.actor._

import play.api.Logger

import com.vdurmont.emoji._

import scala.collection.mutable.{ Map => MutableMap }

import models._
import protocol._

object EmojiActor {

  def props = Props[EmojiActor]

}

case class Emoji(codepoint: String)

class EmojiActor extends Actor {

  var totalTweets = 0
  var tweetsWithEmojis = 0

  val emojiCounts = MutableMap.empty[Emoji, Int].withDefaultValue(0)

  def emojisFor(tweet: Tweet): Seq[Emoji] = {
    tweet.text.split("\\s+").filter(EmojiManager.isEmoji).map(Emoji.apply _)
  }

  def update(tweet: Tweet) = { 
    val emojis = emojisFor(tweet)

    totalTweets += 1
    if (emojis.nonEmpty)
      tweetsWithEmojis += 1

    emojis.foreach { emoji =>
      emojiCounts.update(emoji, emojiCounts(emoji) + 1)
    }
  }

  def percentContainingEmojis = 
    if (totalTweets == 0)
      0.0
    else
      tweetsWithEmojis / (totalTweets * 1.0)

  def receive = {
    case tweet: Tweet => update(tweet)
    case RequestEmojis(count) => 
      sender ! EmojiStats(
        percentContainingEmojis,
        emojiCounts.toList.sortBy(_._2).reverse.take(count).map { case (emoji, count) => emoji.codepoint -> count }.toMap
      )
  }

}

