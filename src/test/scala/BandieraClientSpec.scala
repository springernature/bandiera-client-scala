package test.utest.examples

import com.softwaremill.sttp.Response
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import com.softwaremill.sttp.testing.SttpBackendStub
import com.springernature.bandieraclientscala.{BandieraClient, FeatureFlag, FeatureNotFound, GroupNotFound}
import utest._

import scala.concurrent.ExecutionContext.Implicits.global

object BandieraClientTests extends TestSuite {


  val tests = Tests {
    "get features for existing group" - {
      val mockedResp = """{"response": {"1":false, "2":true}} """
      implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
        .whenRequestMatches(_.uri.toString().endsWith("/api/v2/groups/g/features"))
        .thenRespond(Response(Right(mockedResp), 200, "", Nil, Nil))

      new BandieraClient(backend=testingBackend).getFeaturesForGroup("g").map(featureFlags => {
        assert(featureFlags.length == 2)
        assert(featureFlags.contains(FeatureFlag("1", false)))
        assert(featureFlags.contains(FeatureFlag("2", true)))
      })
    }
    "get features for non-existing group" - {
      val mockedResp = """{"response": "false", "warning": "This group does not exist in the Bandiera database."} """
      implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
        .whenRequestMatches(_.uri.toString().endsWith("/api/v2/groups/nope/features"))
        .thenRespond(Response(Left(mockedResp), 404, "", Nil, Nil))

      new BandieraClient(backend=testingBackend).getFeaturesForGroup("nope").map(response => {
        throw new Exception("failed")
      }).recover {
        case e: GroupNotFound => {/* success */}
      }
    }
    "get existing feature of group" - {
      val mockedResp = """{"response": false} """
      implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
        .whenRequestMatches(_.uri.toString().endsWith("/api/v2/groups/g/features/gg"))
        .thenRespond(Response(Right(mockedResp), 200, "", Nil, Nil))

      new BandieraClient(backend=testingBackend).getFeature(group = "g", feature = "gg").map(flag => {
        assert(flag.name == "gg")
        assert(flag.active == false)
      })
    }
    "get non-existing feature of group" - {
      val mockedResp = """{"response": "false", "warning": "This feature does not exist in the Bandiera database."} """
      implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
        .whenRequestMatches(_.uri.toString().endsWith("/api/v2/groups/g/features/nope"))
        .thenRespond(Response(Left(mockedResp), 404, "", Nil, Nil))

      new BandieraClient(backend=testingBackend).getFeature(group = "g", feature = "nope").map(response => {
        throw new Exception("failed")
      }).recover {
        case e: FeatureNotFound => {/* success */}
      }
    }
    "get all: when no groupds/features " - {
      val mockedResp = """{"response": {}} """
      implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
        .whenRequestMatches(_.uri.toString().endsWith("/api/v2/all"))
        .thenRespond(Response(Right(mockedResp), 200, "", Nil, Nil))

      new BandieraClient(backend=testingBackend).getAll().map(response => {
        assert(response.isEmpty)
      })
    }

    "get all: groups and features exist" - {
      val mockedResp = """{"response": {"g": {"foo":false, "bar":true}, "f": {"bar":false}}} """
      implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
        .whenRequestMatches(_.uri.toString().endsWith("/api/v2/all"))
        .thenRespond(Response(Right(mockedResp), 200, "", Nil, Nil))

      new BandieraClient(backend=testingBackend).getAll().map(response => {
        assert(response.size == 2)
        assert(response("g").size == 2)
        assert(response("g").contains(FeatureFlag("foo", false)))
        assert(response("g").contains(FeatureFlag("bar", true)))
        assert(response("f").size == 1)
        assert(response("f").contains(FeatureFlag("bar", false)))
      })
    }
  }
}
