package test

import _root_.actors._
import models._

import org.scalatest._
import org.scalatest.time._
import org.scalatest.concurrent.ScalaFutures
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._

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
import play.api.libs.concurrent.Akka

import org.joda.time.DateTime

import scala.concurrent._
import scala.concurrent.duration._
import scala.collection.immutable

import helpers.Samples._

class StreamProducerIntegrationSpec extends PlaySpec with OneAppPerSuite with ScalaFutures {

  implicit val system = Akka.system

  val streamProducerRef = TestActorRef(StreamProducerActor.props())
  val streamProducer = streamProducerRef.underlyingActor.asInstanceOf[StreamProducerActor]

  implicit val defaultPatience =
    PatienceConfig(timeout =  Span(5, Seconds), interval = Span(5, Millis))

  import StreamProducerActor._
  import system.dispatcher

  "A StreamProducerActor" should {

    def take10[T] = Enumeratee.take[T](10)

    "stream Twitter sample API" in {

      val (e, p) = streamProducer.stopWhenDone(streamProducer.subscribe)

      val list: Future[List[TwitterStreamItem]] = e.through(take10).run(Iteratee.getChunks).andThen { case _ => p.success(()) }
      list.futureValue must have length(10)

    }

  }

}
 
