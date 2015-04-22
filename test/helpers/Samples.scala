package helpers

import org.joda.time.DateTime

import java.net.URL

import com.vdurmont.emoji.{ Emoji, EmojiManager}
import collection.JavaConverters._

import models._

object Samples {

  val sampleTweet = Tweet(42l, DateTime.now, Seq("akka"), Seq(new URL("http://scala-lang.org")), "Banno Evaluation using #akka")

  val emojis = EmojiManager.getAll().asScala.take(10).toList

  def emojiTweet() = sampleTweet.copy(
    text = sampleTweet.text + " " + emojis.head.getUnicode
  )

  def emojiTweet(emoji: Emoji) = sampleTweet.copy(
    text = sampleTweet.text + " " + emoji.getUnicode
  )

}
 
