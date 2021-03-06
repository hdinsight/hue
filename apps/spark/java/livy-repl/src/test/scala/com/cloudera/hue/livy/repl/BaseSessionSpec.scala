/*
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudera.hue.livy.repl

import java.util.concurrent.TimeUnit

import com.cloudera.hue.livy.sessions.{NotStarted, Idle, Starting}
import org.json4s.DefaultFormats
import org.scalatest.{FlatSpec, Matchers}

import _root_.scala.concurrent.duration.Duration

abstract class BaseSessionSpec extends FlatSpec with Matchers {

  implicit val formats = DefaultFormats

  def withSession(testCode: Session => Any) = {
    val session = Session(createInterpreter())
    session.waitForStateChange(NotStarted(), Duration(30, TimeUnit.SECONDS))
    try {
      testCode(session)
    } finally session.close()
  }

  def createInterpreter(): Interpreter

  it should "start in the starting or idle state" in withSession { session =>
    session.state should (equal (Starting()) or equal (Idle()))
  }

  it should "eventually become the idle state" in withSession { session =>
    session.waitForStateChange(Starting(), Duration(30, TimeUnit.SECONDS))
    session.state should equal (Idle())
  }
}
