package plugins

import akka.actor.{ ActorRef, Actor }

import play.api.{ Play, Plugin, Application}
import play.api.libs.concurrent.Akka 

import scala.concurrent.duration._

import actors._
import models.protocol._

class ActorPlugin(implicit app: Application) extends Plugin {

  lazy val averageTweets: ActorRef = Akka.system.actorOf(AverageTweetsActor.props)
  lazy val emojis: ActorRef = Akka.system.actorOf(EmojiActor.props)
  lazy val hashtags: ActorRef = Akka.system.actorOf(HashtagActor.props)
  lazy val totalTweets: ActorRef = Akka.system.actorOf(TotalTweetsActor.props)
  lazy val topDomains: ActorRef = Akka.system.actorOf(TopDomainsActor.props)
  lazy val urls: ActorRef = Akka.system.actorOf(URLActor.props)

  lazy val subscribers = Seq(
    averageTweets, emojis, hashtags, totalTweets, topDomains, urls)

  lazy val streamProducer: ActorRef = 
    Akka.system.actorOf(StreamProducerActor.props(
      totalTweets,
      averageTweets,
      emojis,
      hashtags,
      topDomains,
      urls
    ))

  override def onStart() = {
    streamProducer ! StreamProducerActor.Subscribe

    Akka.system.scheduler.schedule(30 seconds, 30 seconds)(tellSubscribersToLogStatus)(Akka.system.dispatcher)
  }

  override def onStop() = {
    tellSubscribersToLogStatus
  }

  def tellSubscribersToLogStatus = 
    subscribers.foreach { s => s ! Log }

  override def enabled = true

}

object ActorPlugin {

  def component: ActorPlugin = Play.current.plugin[ActorPlugin]
    .getOrElse(throw new RuntimeException("Actors plugin not loaded"))

}
