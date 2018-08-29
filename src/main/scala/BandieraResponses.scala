package com.springernature.bandieraclientscala

import play.api.libs.json._


case class FeaturesForGroupResponse(flags: Seq[FeatureFlag])

case class SingleFeatureForGroupResponse(flagValue: Boolean)

case class AllFeaturesResponse(groupsToFlagsMap: Map[String, Seq[FeatureFlag]])


//  existing group: {"response":{"gg":false}}
//  non-existing: {"response":{}, warning: "This group does not exist in the Bandiera database." }
object FeaturesForGroupResponse {
  implicit val fmt: Format[FeaturesForGroupResponse] = Format[FeaturesForGroupResponse](
    Reads[FeaturesForGroupResponse](json => {
      val maybeWarning = (json \ "warning")
      if (maybeWarning.isDefined) {
        JsError(maybeWarning.as[String])
      }
      else {
        val flags: Seq[FeatureFlag] = (json \ "response").as[JsObject].fields.map {
          case (name, jsval) => FeatureFlag(name, jsval.as[Boolean])
        }
        JsSuccess(FeaturesForGroupResponse(flags))
      }
    }
    ),
    Writes[FeaturesForGroupResponse](
      feats => JsObject(feats.flags.map(f => (f.name, JsBoolean(f.active))))
    )
  )
}



//  when feature found { "response": true }
//  when feature not found:
// {
//    "response": false,
//    "warning": "This group does not exist in the Bandiera database."
//  }
object SingleFeatureForGroupResponse {
  implicit val fmt: Format[SingleFeatureForGroupResponse] = Format[SingleFeatureForGroupResponse](
    Reads[SingleFeatureForGroupResponse](json => {
      val maybeWarning = (json \ "warning")
      if (maybeWarning.isDefined) {
        JsError(maybeWarning.as[String])
      }
      else {
        (json \ "response").asOpt[JsBoolean]
          .map(jsbool => JsSuccess(SingleFeatureForGroupResponse(jsbool.value)))
          .getOrElse(JsError(s"failed to parse flag boolean value from: ${json.toString()}"))
      }
    }
    ),
    Writes[SingleFeatureForGroupResponse](fv => JsBoolean(fv.flagValue))
  )
}

// example success response:
//  "response": {
//      "f": {
//     "ff": false
//    },
//      "g": {
//      "gg": false
//    }
//  }
object AllFeaturesResponse {
  implicit val fmt: Format[AllFeaturesResponse] = Format[AllFeaturesResponse](
    Reads[AllFeaturesResponse](json => {
      val maybeWarning = (json \ "warning")
      if (maybeWarning.isDefined) {
        JsError(maybeWarning.as[String])
      }
      else {
        val groupsToFlagsMap: Map[String, Seq[FeatureFlag]] =
          (json \ "response").as[Map[String, JsObject]].mapValues(
            jsobj => jsobj.fields.map {
              case (name, jsval) => FeatureFlag(name, jsval.as[Boolean])
            }
          )
        JsSuccess(AllFeaturesResponse(groupsToFlagsMap))
      }
    }
    ),
    Writes[AllFeaturesResponse](allResp => Json.toJson(allResp.groupsToFlagsMap))
  )
}


