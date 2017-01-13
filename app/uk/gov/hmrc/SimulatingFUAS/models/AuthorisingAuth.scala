/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.SimulatingFUAS.models

import com.github.t3hnar.bcrypt._
import org.apache.commons.codec.binary.Base64
import play.api.http.HeaderNames
import play.api.mvc.RequestHeader


class AuthorisingAuth {

  val authCode = "admin:$2a$10$LYNcgBSUFCUcRfKZCyfSNueAXCdxZB1sRjTnVY.nshDNEKiyPoxF2;"
  val authorisedUsers: List[User] = getAuthorisedUsers(authCode)

  def checkName(name: String, password: String): Boolean = {
    authorisedUsers.exists(user => user.name == name && password.isBcrypted(user.password))
  }

  def getAuthorisedUsers(authorisedUsers: String): List[User] = {
      authorisedUsers.split(";").flatMap(
        user => {
          user.split(":") match {
            case Array(username, password) => Some(User(username, password))
            case _ => None
          }
        }
      ).toList
  }

  def checkToken(implicit request: RequestHeader): Boolean = {
    val maybeCredentials = request.headers.get(HeaderNames.AUTHORIZATION)
    maybeCredentials match {
      case Some(auth) =>
        auth.split(" ").toList match {
          case "Basic" :: usernameAndPassword :: Nil =>
            val mayUserInBase64 = new String(new Base64(true).decode(usernameAndPassword))
            val mayUser: Option[User] = mayUserInBase64.split(":").toList match {
              case username :: password :: Nil => Some(User(username, password))
              case _ => None
            }
            mayUser.exists { _ =>
              authorisedUsers.exists(user => user.name == mayUser.get.name && mayUser.get.password.isBcrypted(user.password))
            }
          case _ => false
        }
      case _ => false
    }
  }
}

