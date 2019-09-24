package com.springernature.bandieraclientscala

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import upickle.default.{ReadWriter, macroRW}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


/* model case classes */

case class FeatureFlag(name: String, active: Boolean)

object FeatureFlag {
  implicit val rw: ReadWriter[FeaturesForGroupResponse] = macroRW
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

  import com.springernature.bandieraclientscala.AllFeaturesResponse._
  import com.springernature.bandieraclientscala.FeaturesForGroupResponse._
  import com.springernature.bandieraclientscala.SingleFeatureForGroupResponse._
  import upickle.default._

  def getFeature(group: String,
                 feature: String,
                 userGroup: Option[String] = None,
                 userId: Option[String] = None): Future[FeatureFlag] = {
    val path = s"$baseApiUri/v2/groups/$group/features/$feature"
    val params = Seq(
      userGroup.map(ug => ("user_group", ug)),
      userId.map(uid => ("user_id", uid))
    ).flatten
    sttp
      .get(uri"$path".params(params: _*))
      .readTimeout(5.seconds)
      .response(
        asString
          .map(s => read[SingleFeatureForGroupResponse](s))
          .map(featResp => FeatureFlag(feature, featResp.response))
      )
      .send()
      .flatMap(transformEither)
  }

  def getFeaturesForGroup(group: String,
                          userGroup: Option[String] = None,
                          userId: Option[String] = None): Future[Seq[FeatureFlag]] = {
    val path = s"$baseApiUri/v2/groups/$group/features"
    val params = Seq(
      userGroup.map(ug => ("user_group", ug)),
      userId.map(uid => ("user_id", uid))
    ).flatten
    sttp
      .get(uri"$path".params(params: _*))
      .readTimeout(5.seconds)
      .response(
        asString
          .map(s => read[FeaturesForGroupResponse](s))
          .map(resp => resp.response.toSeq.map{
            case (featName, bool) => FeatureFlag(featName, bool)
          })
      )
      .send()
      .flatMap(transformEither)
  }


  def getAll(userGroup: Option[String] = None,
             userId: Option[String] = None): Future[Map[String, Seq[FeatureFlag]]] = {
    val path = s"$baseApiUri/v2/all"
    val params = Seq(
      userGroup.map(ug => ("user_group", ug)),
      userId.map(uid => ("user_id", uid))
    ).flatten
    sttp
      .get(uri"$path".params(params: _*))
      .readTimeout(5.seconds)
      .response(
        asString
          .map(s => read[AllFeaturesResponse](s))
          .map(resp => {
            resp.response.map {
              case (group, groupFeatsMap) =>
                (group, groupFeatsMap.toSeq.map(tup => FeatureFlag(tup._1, tup._2)))
            }
          }))
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
    ujson.read(s)("warning").str
  }
}
