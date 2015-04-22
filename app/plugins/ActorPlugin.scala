package plugins

import akka.actor.{ ActorRef, Actor }

import play.api.{ Play, Plugin, Application}
import play.api.libs.concurrent.Akka 

import actors._
import models.protocol._

class ActorPlugin(implicit app: Application) extends Plugin {

  lazy val averageTweets: ActorRef = Akka.system.actorOf(AverageTweetsActor.props)

  lazy val emojis: ActorRef = Akka.system.actorOf(EmojiActor.props)
  lazy val hashtags: ActorRef = Akka.system.actorOf(HashtagActor.props)
  lazy val totalTweets: ActorRef = Akka.system.actorOf(TotalTweetsActor.props)
  //val url: ActorRef = Actor.noSender

  lazy val streamProducer: ActorRef = 
    Akka.system.actorOf(StreamProducerActor.props(
      totalTweets,
      averageTweets,
      emojis,
      hashtags
    ))

  override def onStart() = {
  }

  override def onStop() = {
  }

  override def enabled = true

}

object ActorPlugin {

  def component: ActorPlugin = Play.current.plugin[ActorPlugin]
    .getOrElse(throw new RuntimeException("Actors plugin not loaded"))

}
