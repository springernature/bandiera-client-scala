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

case class GroupNotFound() extends BandieraApiException("This group does not exist in the Bandiera database.")

case class FeatureNotFound() extends BandieraApiException("This group does not exist in the Bandiera database.")

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
      .response(asString.map(Json.parse).map(_.as[AllFeaturesResponse].groupsToFlagsMap))
      .send()
      .flatMap(transformEither)
  }


  private def transformEither[T](sttpResponse: Response[T]) = {
    sttpResponse.body match {
      case Right(s) => Future.successful(s)
      case Left(s) if s.contains("""This group does not exist in the Bandiera database""") => Future.failed(GroupNotFound())
      case Left(s) if s.contains("""This feature does not exist in the Bandiera database""") => Future.failed(FeatureNotFound())
      case Left(s) => Future.failed(new Exception(s"unhandled error: $s"))
      case _ => Future.failed(new Exception("unhandled error"))
    }
  }
}
