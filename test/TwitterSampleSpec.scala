import org.scalatest._
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._

import play.api.libs.json._

import controllers._
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
      component.averageTweets ! sampleTweet.copy(createdAt = sampleTweet.createdAt.minusSeconds(1))
      component.averageTweets ! sampleTweet.copy(createdAt = sampleTweet.createdAt.minusSeconds(1))

      val result = TwitterSample.average().apply(FakeRequest())

      status(result) must be(OK)
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) must be (Json.obj(
        "average_per_second" -> 1.5,
        "average_per_minute" -> 90.0,
        "average_per_hour" -> 5400.0
      ))
    }

  }
}
