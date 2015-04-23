import org.scalatest._
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._

import play.api.libs.json._

import java.net.URL

import org.joda.time._

import controllers._
import models._
import plugins._

import helpers.Samples._

class TwitterSampleSpec extends PlaySpec with OneAppPerSuite {

  import ActorPlugin.component

  "TwitterSample" must {

    "return 0 top tweets when initialized" in {
      val result = TwitterSample.total().apply(FakeRequest())

      status(result) must be(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must be (Json.obj("total" -> 0))
    }

    "return tweet count" in {
      component.totalTweets ! sampleTweet
      component.totalTweets ! sampleTweet
      component.totalTweets ! sampleTweet

      val result = TwitterSample.total().apply(FakeRequest())

      status(result) must be(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must be (Json.obj("total" -> 3))
    }

    "return averages" in {
      component.averageTweets ! sampleTweet
      component.averageTweets ! sampleTweet.copy(timestamp = sampleTweet.timestamp - 1000)
      component.averageTweets ! sampleTweet.copy(timestamp = sampleTweet.timestamp - 1000)

      val result = TwitterSample.average().apply(FakeRequest())

      status(result) must be(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must be (Json.obj(
        "average_per_second" -> 1.5,
        "average_per_minute" -> 90.0,
        "average_per_hour" -> 5400.0
      ))
    }

    "return emoji stats" in {
      val multiEmojiTweet = sampleTweet.copy(text = s"Multiple: ${emojis(0).getUnicode} ${emojis(1).getUnicode}")

      val tweets: Seq[Tweet] = Seq(
        multiEmojiTweet,
        emojiTweet(emojis(0)), emojiTweet(emojis(0)), emojiTweet(emojis(0)),
        emojiTweet(emojis(1)), emojiTweet(emojis(1))
      ) ++ emojis.drop(2).map(emojiTweet) :+ sampleTweet :+ sampleTweet

      tweets.foreach { t => component.emojis ! t }

      val result = TwitterSample.emojis(2).apply(FakeRequest())

      status(result) must be(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must be (Json.obj(
        "percent_containing_emojis" -> 14/16.0,
        "top_emojis" -> Json.obj(
          emojis(0).getUnicode -> 4,
          emojis(1).getUnicode -> 3
        )
      ))

    }

    "return top hashtags" in {
      component.hashtags ! sampleTweet
      component.hashtags ! sampleTweet.copy(hashtags = Seq("akka", "scala"))
      component.hashtags ! sampleTweet.copy(hashtags = Seq("banno", "scala"))
      component.hashtags ! sampleTweet.copy(hashtags = Seq("scala", "akka"))
      component.hashtags ! sampleTweet.copy(hashtags = Seq("banno", "akka"))

      val result = TwitterSample.topHashtags().apply(FakeRequest())

      status(result) must be(OK)
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "top_hashtags").as[Set[String]] mustBe (
        Set("akka", "scala", "banno")
      )
    }

    "return top domains" in {
      component.topDomains ! sampleTweet
      component.topDomains ! sampleTweet.copy(urls = Nil)
      component.topDomains ! sampleTweet.copy(urls = Seq(new URL("http://google.com"), new URL("http://twitter.com")))
      component.topDomains ! sampleTweet.copy(urls = Seq(new URL("http://akka.io")))
      component.topDomains ! sampleTweet.copy(urls = Seq(new URL("http://scala-lang.org")))
      component.topDomains ! sampleTweet.copy(urls = Seq(new URL("http://playframework.com")))

      val result = TwitterSample.topDomains().apply(FakeRequest())

      status(result) must be(OK)
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "top_domains").as[Set[String]] mustBe (
        Set("google.com", "akka.io", "scala-lang.org", "playframework.com", "twitter.com")
      )
    }

    "return URL stats" in {
      component.urls ! sampleTweet.copy(urls = Nil)
      component.urls ! sampleTweet.copy(urls = Nil)
      component.urls ! sampleTweet.copy(urls = Seq(new URL("http://google.com"), new URL("http://pic.twitter.com")))
      component.urls ! sampleTweet.copy(urls = Seq(new URL("http://akka.io")))
      component.urls ! sampleTweet.copy(urls = Seq(new URL("http://scala-lang.org")))
      component.urls ! sampleTweet.copy(urls = Seq(new URL("http://playframework.com")))

      val result = TwitterSample.urlStats().apply(FakeRequest())

      status(result) must be(OK)
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "percent_containing_urls").as[Double] mustBe 4/6.0
      (contentAsJson(result) \ "percent_containing_photo_urls").as[Double] mustBe 1/6.0
    }

  }
}
