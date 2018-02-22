/**
 * Copyright (C) 2017 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.session

import nl.knaw.dans.easy.session.components.User
import org.apache.commons.configuration.PropertiesConfiguration
import org.eclipse.jetty.http.HttpStatus._
import org.scalamock.scalatest.MockFactory
import org.scalatra.test.scalatest.ScalatraSuite

import scala.util.Success

class ServletsSpec extends TestSupportFixture with ServletFixture with ScalatraSuite with MockFactory {

  private val configuration: PropertiesConfiguration = new PropertiesConfiguration() {
    addProperty("ldap.provider.url", "ldap://hostDoesNotExist")
  }
  private val wiring: ApplicationWiring = new ApplicationWiring(new Configuration("", configuration)) {
    override val authentication: Authentication = mock[Authentication]
  }
  val app: EasySessionApp = new EasySessionApp(wiring)

  addServlet(new ProtectedServlet(app), "/*")
  addServlet(new LoginServlet(app), "/sessions/*")

  "get /" should "redirect to /sessions/new with a JSESSIONID cookie" in {
    get("/") {
      status shouldBe MOVED_TEMPORARILY_302
      body shouldBe ""
      header("Location") should startWith(s"$baseUrl/sessions/new;jsessionid=")
      header("Content-Type") shouldBe "text/html;charset=UTF-8"
      header("Set-Cookie") should include("JSESSIONID=")
      header("Set-Cookie") should include("Path=/")
      header("Set-Cookie") should include(";")
      header("Expires") shouldBe "Thu, 01 Jan 1970 00:00:00 GMT"
    }
  }

  "get /sessions/new" should "present a login form" in {
    get("/sessions/new") {
      status shouldBe OK_200
      body should include("""<label for="login">""")
      header("Set-Cookie") should include("JSESSIONID=")
      header("Set-Cookie") should include("Path=/")
      header("Set-Cookie") should include(";")
      header("Content-Type") shouldBe "text/html;charset=UTF-8"
      header("Expires") shouldBe "Thu, 01 Jan 1970 00:00:00 GMT"
    }
  }

  "post /sessions with valid credentials" should "login" in {

    (wiring.authentication.getUser(_: String, _: String)
      ) expects("foo", "bar") returning Success(Some(User(Map.empty)))
    post(
      uri = "/sessions",
      params = Seq(("login", "foo"), ("password", "bar"))
    ) {
      status shouldBe MOVED_TEMPORARILY_302
      body shouldBe ""
      header("Location") should startWith (s"$baseUrl/;jsessionid=")
      header("Set-Cookie") should include("JSESSIONID=")
      header("Set-Cookie") should include("Path=/")
      header("Set-Cookie") should include(";")
      header("Content-Type") shouldBe "text/html;charset=UTF-8"
      header("Expires") shouldBe "Thu, 01 Jan 1970 00:00:00 GMT"

      // TODO expecting "You executed a protected action" with a manul test
    }
  }

  "post /sessions with invalid credentials" should "redirect to /sessions/new" in {

    (wiring.authentication.getUser(_: String, _: String)
      ) expects("someone", "invalid") returning Success(None)
    post(
      uri = "/sessions",
      params = Seq(("login", "someone"), ("password", "invalid"))
    ) {
      status shouldBe MOVED_TEMPORARILY_302
      body shouldBe ""
      header("Location") shouldBe s"$baseUrl/sessions/new"
      header("Content-Type") shouldBe "text/html;charset=UTF-8"
      header("Set-Cookie") shouldBe null
      header("Expires") shouldBe null
    }
  }
}
