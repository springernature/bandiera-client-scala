package com.springernature.bandieraclientscala.tests

//import utest.framework.Result

class CustomFramework extends utest.runner.Framework{
  override def showSummaryThreshold = 5
  override def formatWrapWidth = 140
//  override def formatSingle(path: Seq[String], r: Result)  = None

}
