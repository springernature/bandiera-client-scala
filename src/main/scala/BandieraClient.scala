package com.springernature.bandieraclientscala

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import play.api.libs.json._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


/* model case classes */

case class FeatureFlag(name: String, active: Boolean)

object FeatureFlag {
  implicit val fmt: Format[FeatureFlag] = Json.format[FeatureFlag]
}

/* exception case classes */

sealed abstract class BandieraApiException(message: String) extends Exception(message)

case class GroupNotFound() extends BandieraApiException("This group does not exist in the Bandiera database")
case class FeatureNotFound() extends BandieraApiException("This feature does not exist in the Bandiera database")
case class UserGroupMissing() extends BandieraApiException("This feature is configured for user groups - you must supply a `user_group` param")
case class UserIdMissing() extends BandieraApiException("This feature is configured for user percentages - you must supply a `user_id` param")
case class MultipleWarnings(s: String) extends BandieraApiException(s)

/* client code */

class BandieraClient(baseApiUri: String = "http://127.0.0.1:5000/api",
                     implicit val backend: SttpBackend[Future, Nothing] = AsyncHttpClientFutureBackend())
                    (implicit val ec: ExecutionContext) {

  def getFeaturesForGroup(group: String,
                          userGroup: Option[String] = None,
                          userId: Option[String] = None): Future[Seq[FeatureFlag]] = {
    val path = s"$baseApiUri/v2/groups/$group/features"
    sttp
      .get(uri"$path")
      .readTimeout(5.seconds)
      .response(asString.map(Json.parse).map(_.as[FeaturesForGroupResponse].flags))
      .send()
      .flatMap(transformEither)
  }

  def getFeature(group: String,
                 feature: String,
                 userGroup: Option[String] = None,
                 userId: Option[String] = None): Future[FeatureFlag] = {
    val path = s"$baseApiUri/v2/groups/$group/features/$feature"
    sttp
      .get(uri"$path")
      .readTimeout(5.seconds)
      .response(asString.map(Json.parse).map(jsVal => {
        val flagVal = jsVal.as[SingleFeatureForGroupResponse].flagValue
        FeatureFlag(feature, flagVal)
      }))
      .send()
      .flatMap(transformEither)
  }

  def getAll(userGroup: Option[String] = None,
             userId: Option[String] = None): Future[Map[String, Seq[FeatureFlag]]] = {
    val path = s"$baseApiUri/v2/all"
    sttp
      .get(uri"$path")
      .readTimeout(5.seconds)
      .response(asString.map(Json.parse).map(_.as[AllFeaturesResponse]).map(_.groupsToFlagsMap))
      .send()
      .flatMap(transformEither)
  }


  private def transformEither[T](sttpResponse: Response[T]) = {
    sttpResponse.body match {
      case Right(s) => Future.successful(s)
      case Left(s) if s.contains("""The following warnings were raised""") => Future.failed(MultipleWarnings(extractWarning(s)))
      case Left(s) if s.contains("""This group does not exist""") => Future.failed(GroupNotFound())
      case Left(s) if s.contains("""This feature does not exist""") => Future.failed(FeatureNotFound())
      case Left(s) if s.contains("""This feature is configured for user groups""") => Future.failed(UserGroupMissing())
      case Left(s) if s.contains("""This feature is configured for user percentage""") => Future.failed(UserIdMissing())
      case Left(s) => Future.failed(new Exception(s"unhandled error: $s"))
      case _ => Future.failed(new Exception("unhandled error"))
    }
  }

  // only used for multiple warning for now
  // TODO: extract and prettify all different warnings
  private def extractWarning(s: String): String = {
    (Json.parse(s) \ "warning").get.toString
  }
}
