package com.springernature.bandieraclientscala.tests

import com.softwaremill.sttp.Response
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import com.softwaremill.sttp.testing.SttpBackendStub
import com.springernature.bandieraclientscala._
import utest._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object BandieraClientTests extends TestSuite {

  val tests = Tests {
    'getFeature - {
      "existing feature of group" - {
        val mockedResp = """{"response": false} """
        implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
          .whenRequestMatches(_.uri.toString().contains("/api/v2/groups/g/features/gg"))
          .thenRespond(Response(Right(mockedResp), 200, "", Nil, Nil))

        new BandieraClient(backend = testingBackend).getFeature(group = "g", feature = "gg").map(flag => {
          assert(flag.name == "gg")
          assert(flag.active == false)
        })
      }

      "non-existing feature of group" - {
        val mockedResp = """{"response": false, "warning": "This feature does not exist in the Bandiera database."} """
        implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
          .whenRequestMatches(_.uri.toString().endsWith("/api/v2/groups/g/features/nope"))
          .thenRespond(Response(Left(mockedResp), 404, ""))

        val e = intercept[FeatureNotFound] {
          Await.result(new BandieraClient(backend = testingBackend).getFeature(group = "g", feature = "nope"), Duration("5 seconds"))
        }
        assert(e.getMessage().equals("This feature does not exist in the Bandiera database"))
      }

      "percentage based" - {
        "and user_id is passed in " - {
          val mockedResp = """{"response": true}"""
          implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
            .whenRequestMatches(_.uri.toString().contains("/api/v2/groups/g/features/gg"))
            .thenRespond(Response(Right(mockedResp), 200, "", Nil, Nil))

          new BandieraClient(backend = testingBackend).getFeature(group = "g", feature = "gg", userId = Some("123")).map(flag => {
            assert(flag.name == "gg")
            assert(flag.active == true)
          })
        }

        "but user_id not passed" - {
          val mockedResp = """{"response": false, "warning": "This feature is configured for user percentages - you must supply a `user_id` param"} """
          implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
            .whenRequestMatches(_.uri.toString().endsWith("/api/v2/groups/g/features/gg"))
            .thenRespond(Response(Left(mockedResp), 200, ""))

          val e = intercept[UserIdMissing] {
            Await.result(new BandieraClient(backend = testingBackend).getFeature(group = "g", feature = "gg", userId = None), Duration("5 seconds"))
          }
          assert(e.getMessage().equals("This feature is configured for user percentages - you must supply a `user_id` param"))
        }
      }
    }

    'getFeaturesForGroup - {
      "for existing group" - {
        val mockedResp = """{"response": {"1":false, "2":true}} """
        implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
          .whenRequestMatches(_.uri.toString().contains("/api/v2/groups/g/features"))
          .thenRespond(Response(Right(mockedResp), 200, "", Nil, Nil))

        new BandieraClient(backend = testingBackend).getFeaturesForGroup("g").map(featureFlags => {
          assert(featureFlags.length == 2)
          assert(featureFlags.contains(FeatureFlag("1", false)))
          assert(featureFlags.contains(FeatureFlag("2", true)))
        })
      }
      "for non-existing group" - {
        val mockedResp = """{"response": false, "warning": "This group does not exist in the Bandiera database."} """
        implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
          .whenRequestMatches(_.uri.toString().endsWith("/api/v2/groups/nope/features"))
          .thenRespond(Response(Left(mockedResp), 404, ""))

        val e = intercept[GroupNotFound] {
          Await.result(new BandieraClient(backend = testingBackend).getFeaturesForGroup("nope"), Duration("5 seconds"))
        }
        assert(e.getMessage().equals("This group does not exist in the Bandiera database"))
      }
    }


    'getAll - {
      "when no groups/features: empty response" - {
        val mockedResp = """{"response": {}} """
        implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
          .whenRequestMatches(_.uri.toString().contains("/api/v2/all"))
          .thenRespond(Response(Right(mockedResp), 200, "", Nil, Nil))

        new BandieraClient(backend = testingBackend).getAll().map(response => {
          assert(response.isEmpty)
        })
      }

      "groups and features exist: response with flags per group" - {
        val mockedResp = """{"response": {"g": {"foo":false, "bar":true}, "f": {"bar":false}}} """
        implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
          .whenRequestMatches(_.uri.toString().contains("/api/v2/all"))
          .thenRespond(Response(Right(mockedResp), 200, "", Nil, Nil))

        new BandieraClient(backend = testingBackend).getAll().map(response => {
          assert(response.size == 2)
          assert(response("g").size == 2)
          assert(response("g").contains(FeatureFlag("foo", false)))
          assert(response("g").contains(FeatureFlag("bar", true)))
          assert(response("f").size == 1)
          assert(response("f").contains(FeatureFlag("bar", false)))
        })
      }

      //      "when one of the features is percentage based but no user id passed: return the rest of the features" - {
      //        val mockedResp = """{"response": {"g": {"foo":false, "bar":true}, "f": {"foo":true, "bar":false}}, "warning":"The following warnings were raised on this request:\n  * these features have user groups configured and require a `user_group` param:\n    - f: bar\n"} """
      //        implicit val testingBackend = SttpBackendStub(AsyncHttpClientFutureBackend())
      //          .whenRequestMatches(_.uri.toString().endsWith("/api/v2/all"))
      //          .thenRespond(Response(Right(mockedResp), 200, "", Nil, Nil))
      //
      ////        val e = intercept[MultipleWarnings]{
      ////          Await.result(new BandieraClient(backend = testingBackend).getAll(), Duration("5 seconds"))
      ////        }
      ////        assert(e.getMessage.contains("""configured and require a `user_group` param:\n    - f: bar\n"""))
      //
      //        new BandieraClient(backend = testingBackend).getAll().map(response => {
      //          assert(response.size == 2)
      //          assert(response("g").size == 2)
      //          assert(response("g").contains(FeatureFlag("foo", false)))
      //          assert(response("g").contains(FeatureFlag("bar", true)))
      //          assert(response("f").size == 1)
      //          assert(response("f").contains(FeatureFlag("foo", true)))
      //        })
      //      }
    }
  }
}
