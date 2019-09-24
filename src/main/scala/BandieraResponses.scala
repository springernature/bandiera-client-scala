package com.springernature.bandieraclientscala

import upickle.default.{ReadWriter, Reader, Writer, macroRW, macroW}

//  when feature found { "response": true }
//  when feature not found:
// {
//    "response": false,
//    "warning": "This group does not exist in the Bandiera database."
//  }
case class SingleFeatureForGroupResponse(response: Boolean, warning: Option[String] = None)

//  existing group: {"response":{"gg":false}}
//  non-existing: {"response":{}, warning: "This group does not exist in the Bandiera database." }
case class FeaturesForGroupResponse(response: Map[String, Boolean], warning: Option[String] = None)

// example success response:
//  "response": {
//      "f": {
//     "ff": false
//    },
//      "g": {
//      "gg": false
//    }
//  }
case class AllFeaturesResponse(response: Map[String, Map[String, Boolean]], warning: Option[String] = None)


object FeaturesForGroupResponse {
  implicit val rw: ReadWriter[FeaturesForGroupResponse] = macroRW
}

object SingleFeatureForGroupResponse {
  implicit val rw: ReadWriter[SingleFeatureForGroupResponse] = macroRW
}

object AllFeaturesResponse {
  implicit val rw: ReadWriter[AllFeaturesResponse] = macroRW
}


