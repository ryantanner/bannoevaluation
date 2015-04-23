package actors

import akka.actor._

import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.oauth._
import play.api.libs.iteratee._
import play.api.libs.iteratee.Concurrent._

import scala.concurrent.ExecutionContext
import scala.util.{ Try, Success }

import models._

object StreamProducerActor {

  case class Stop(reason: String)
  case object Finalize
  case object Subscribe

  def props(subscribers: ActorRef*) = Props(classOf[StreamProducerActor], subscribers)

  /* Enumeratees */
  def parseByteArray(implicit ec: ExecutionContext) = Enumeratee.mapConcat { itemByteArray: Array[Byte] =>
    new String(itemByteArray, "UTF-8").toCharArray
  }

  // Twitter separates messages with carriage returns
  // This enumeratee takes characters until it reaches one and
  // concatenantes it into a string
  def takeMessages(implicit ec: ExecutionContext) = Enumeratee.grouped {
    for {
      line <- Enumeratee.takeWhile[Char](_ != '\r') &>> Iteratee.getChunks
      _    <- Enumeratee.take(1) &>> Iteratee.ignore[Char]
    } yield line.mkString
  }

  def filterBlankLines(implicit ec: ExecutionContext) = Enumeratee.filter((st: String) => st.trim.nonEmpty)

  // map *then* collect successfully parsed JsValues
  def parseJson(implicit ec: ExecutionContext) = 
    Enumeratee.map((st: String) => Try(Json.parse(st))) compose
    Enumeratee.collect[Try[JsValue]] {
      case Success(json) => json
    }

  //def validateJson(implicit ec: ExecutionContext) = Json.fromJson[TwitterStreamItem]
  def validateJson(implicit ec: ExecutionContext) = Enumeratee.map { json: JsValue =>
    json.validate[TwitterStreamItem]
  } compose Enumeratee.collect { 
    case JsSuccess(item, _) => item
  }

  def collectTweets(implicit ec: ExecutionContext) = Enumeratee.collect[TwitterStreamItem] {
    case t: Tweet => println(t); t
  }

  def collectDisconnects(implicit ec: ExecutionContext) = Enumeratee.collect[TwitterStreamItem] {
    case d: Disconnect => d
  }

  def collectStallWarnings(implicit ec: ExecutionContext) = Enumeratee.collect[TwitterStreamItem] {
    case s: StallWarning => s
  }

}

class StreamProducerActor(subscribers: Seq[ActorRef]) extends Actor with ActorLogging {

  import StreamProducerActor._
  import context.dispatcher

  def subscribe: Enumerator[TwitterStreamItem] = {
    val (key, token) = credentials

    val twitterStream =
      requestTwitterSample(key, token) through
      parseByteArray through
      takeMessages through
      filterBlankLines through
      parseJson through
      validateJson

    twitterStream(processMessage)

    twitterStream onDoneEnumerating {
      self ! Stop
    }
  }

  def credentials: (ConsumerKey, RequestToken) = {
    (for {
      consumerKey     <- current.configuration.getString("consumer_key")
      consumerSecret  <- current.configuration.getString("consumer_secret")
      accessToken     <- current.configuration.getString("access_token")
      accessSecret    <- current.configuration.getString("access_secret")
    } yield {
      (ConsumerKey(consumerKey, consumerSecret),
       RequestToken(accessToken, accessSecret))
    }).getOrElse {
      throw new IllegalStateException("Missing Twitter API credentials")
    }
  }

  def createRequest(key: ConsumerKey, requestToken: RequestToken): WSRequestHolder = {
    val request = WS.url("https://stream.twitter.com/1.1/statuses/sample.json")

    request.sign(OAuthCalculator(key, requestToken))
  }

  def requestTwitterSample(key: ConsumerKey, requestToken: RequestToken): Enumerator[Array[Byte]] = {
    val response = createRequest(key, requestToken).getStream

    response.onSuccess { case (headers, _) =>
      if (headers.status == 200)
        log.info("Successfully connected to Twitter stream")
      else 
        self ! Stop(s"Response ${headers.status}")
    }

    response.onFailure { case t =>
      self ! Stop(t.getMessage)
    }

    Enumerator.flatten(response.map { case (_, stream) => stream })
  }

  val processMessage = Iteratee.foreach[TwitterStreamItem] { item =>
    item match {
      case tweet: Tweet => publishTweet(tweet)
      case d @ Disconnect(code, name, reason) =>
        log.info(s"Received disconnect message from Twitter: $d")
        self ! Stop(reason)
      case stallWarning: StallWarning =>
        log.warning(s"Received stall warning: $stallWarning")
    }
  }

  def publishTweet(tweet: Tweet) { 
    subscribers.foreach { subscriber =>
      subscriber ! tweet
    }
  }

  def receive = {
    case Subscribe => subscribe
    case Stop(reason) =>
      log.info(s"Stopping Twitter stream: $reason")
      subscribers.foreach(s => s ! Finalize)
      context.become(stopped)
  }

  def stopped: Actor.Receive = {
    case _: Stop => // ignore
  }

}
