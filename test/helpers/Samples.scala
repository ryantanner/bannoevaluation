package helpers

import play.api.libs.json._

import org.joda.time.DateTime

import java.net.URL

import com.vdurmont.emoji.{ Emoji, EmojiManager}
import collection.JavaConverters._

import models._

object Samples {

  val sampleTweet = Tweet(42l, DateTime.now.getMillis, Seq("akka"), Seq(new URL("http://scala-lang.org")), "Banno Evaluation using #akka")

  val sampleJson = Json.parse("""{"created_at":"Thu Apr 23 00:07:26 +0000 2015","id":591030583855341568,"id_str":"591030583855341568","text":"RT @edaheryerde: Oha 8000 takip\u00e7i g\u00f6nderdi te\u015fekk\u00fcrler http:\/\/t.co\/CDNPemy07A","source":"\u003ca href=\"http:\/\/www.mercadolibre.com\" rel=\"nofollow\"\u003eMercadoLibre Mobile\u003c\/a\u003e","truncated":false,"in_reply_to_status_id":null,"in_reply_to_status_id_str":null,"in_reply_to_user_id":null,"in_reply_to_user_id_str":null,"in_reply_to_screen_name":null,"user":{"id":2243634487,"id_str":"2243634487","name":"Ferdi YILDIRIM","screen_name":"frdiyldrm","location":"","url":null,"description":null,"protected":false,"verified":false,"followers_count":290,"friends_count":751,"listed_count":0,"favourites_count":112,"statuses_count":16,"created_at":"Fri Dec 13 09:12:59 +0000 2013","utc_offset":10800,"time_zone":"Bucharest","geo_enabled":true,"lang":"tr","contributors_enabled":false,"is_translator":false,"profile_background_color":"C0DEED","profile_background_image_url":"http:\/\/abs.twimg.com\/images\/themes\/theme1\/bg.png","profile_background_image_url_https":"https:\/\/abs.twimg.com\/images\/themes\/theme1\/bg.png","profile_background_tile":false,"profile_link_color":"0084B4","profile_sidebar_border_color":"C0DEED","profile_sidebar_fill_color":"DDEEF6","profile_text_color":"333333","profile_use_background_image":true,"profile_image_url":"http:\/\/pbs.twimg.com\/profile_images\/589522894683709440\/1ddhNOOu_normal.jpg","profile_image_url_https":"https:\/\/pbs.twimg.com\/profile_images\/589522894683709440\/1ddhNOOu_normal.jpg","profile_banner_url":"https:\/\/pbs.twimg.com\/profile_banners\/2243634487\/1429388305","default_profile":true,"default_profile_image":false,"following":null,"follow_request_sent":null,"notifications":null},"geo":null,"coordinates":null,"place":null,"contributors":null,"retweeted_status":{"created_at":"Thu Apr 23 00:05:33 +0000 2015","id":591030107843747840,"id_str":"591030107843747840","text":"Oha 8000 takip\u00e7i g\u00f6nderdi te\u015fekk\u00fcrler http:\/\/t.co\/CDNPemy07A","source":"\u003ca href=\"http:\/\/tapin.tv\" rel=\"nofollow\"\u003eTapIn.tv\u003c\/a\u003e","truncated":false,"in_reply_to_status_id":null,"in_reply_to_status_id_str":null,"in_reply_to_user_id":null,"in_reply_to_user_id_str":null,"in_reply_to_screen_name":null,"user":{"id":3188025742,"id_str":"3188025742","name":"Eda Serin","screen_name":"edaheryerde","location":"\u0130stanbul","url":null,"description":null,"protected":false,"verified":false,"followers_count":8945,"friends_count":460,"listed_count":1,"favourites_count":0,"statuses_count":5,"created_at":"Mon Apr 20 19:02:53 +0000 2015","utc_offset":null,"time_zone":null,"geo_enabled":false,"lang":"tr","contributors_enabled":false,"is_translator":false,"profile_background_color":"C0DEED","profile_background_image_url":"http:\/\/abs.twimg.com\/images\/themes\/theme1\/bg.png","profile_background_image_url_https":"https:\/\/abs.twimg.com\/images\/themes\/theme1\/bg.png","profile_background_tile":false,"profile_link_color":"0084B4","profile_sidebar_border_color":"C0DEED","profile_sidebar_fill_color":"DDEEF6","profile_text_color":"333333","profile_use_background_image":true,"profile_image_url":"http:\/\/pbs.twimg.com\/profile_images\/590229670076919808\/ndOyVSmJ_normal.jpg","profile_image_url_https":"https:\/\/pbs.twimg.com\/profile_images\/590229670076919808\/ndOyVSmJ_normal.jpg","profile_banner_url":"https:\/\/pbs.twimg.com\/profile_banners\/3188025742\/1429556752","default_profile":true,"default_profile_image":false,"following":null,"follow_request_sent":null,"notifications":null},"geo":null,"coordinates":null,"place":null,"contributors":null,"retweet_count":2667,"favorite_count":3,"entities":{"hashtags":[],"trends":[],"urls":[{"url":"http:\/\/t.co\/CDNPemy07A","expanded_url":"http:\/\/takipseli.zz.mu","display_url":"takipseli.zz.mu","indices":[38,60]}],"user_mentions":[],"symbols":[]},"favorited":false,"retweeted":false,"possibly_sensitive":false,"filter_level":"low","lang":"tr"},"retweet_count":0,"favorite_count":0,"entities":{"hashtags":[],"trends":[],"urls":[{"url":"http:\/\/t.co\/CDNPemy07A","expanded_url":"http:\/\/takipseli.zz.mu","display_url":"takipseli.zz.mu","indices":[55,77]}],"user_mentions":[{"screen_name":"edaheryerde","name":"Eda Serin","id":3188025742,"id_str":"3188025742","indices":[3,15]}],"symbols":[]},"favorited":false,"retweeted":false,"possibly_sensitive":false,"filter_level":"low","lang":"tr","timestamp_ms":"1429747646665"}""")

  val emojis = EmojiManager.getAll().asScala.take(10).toList

  def emojiTweet() = sampleTweet.copy(
    text = sampleTweet.text + " " + emojis.head.getUnicode
  )

  def emojiTweet(emoji: Emoji) = sampleTweet.copy(
    text = sampleTweet.text + " " + emoji.getUnicode
  )

}
 
