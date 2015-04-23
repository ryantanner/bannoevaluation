package actors

import akka.actor._

import com.vdurmont.emoji._

import scala.collection.mutable.{ Map => MutableMap }

import models._
import protocol._

object EmojiActor {

  def props = Props[EmojiActor]

}

case class Emoji(codepoint: String)

class EmojiActor extends TweetProcessor {

  val name = "EmojiActor"

  var totalTweets = 0
  var tweetsWithEmojis = 0

  // I chose not to use a count-min-sketch here because the maximum number of keys is bounded
  // with a relatively small value (~800), so there isn't much concern about memory usage
  val emojiCounts = MutableMap.empty[Emoji, Int].withDefaultValue(0)

  def emojisFor(tweet: Tweet): Seq[Emoji] = {
    tweet.text.split("\\s+").filter(EmojiManager.isEmoji).map(Emoji.apply _)
  }

  def process(tweet: Tweet) = { 
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

  val receiveRequests: Actor.Receive = {
    case RequestEmojis(count) => 
      sender ! EmojiStats(
        percentContainingEmojis,
        emojiCounts.toList.sortBy(_._2).reverse.take(count).map { case (emoji, count) => emoji.codepoint -> count }.toMap
      )
  }

  def logStats {
    log.info(s"""
      Percent of tweets containing emojis: $percentContainingEmojis
      Top 10 emojis:
        ${emojiCounts.toList.sortBy(_._2).reverse.take(10).map(_._1.codepoint).mkString(", ")}
    """)
  }

}

