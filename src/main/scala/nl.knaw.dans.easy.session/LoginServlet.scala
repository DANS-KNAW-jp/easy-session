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

import nl.knaw.dans.easy.session.components.{ AuthenticationProvider, AuthenticationSupport }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.scalatra._

class LoginServlet(app: EasySessionApp) extends ScalatraServlet
  with AuthenticationSupport
  with DebugEnhancedLogging {

  get("/new") {
    if (isAuthenticated) redirect("/")

    contentType = "text/html"
    <html>
      <body>
        <form action="/sessions" method="post">
          <p><label for="login">login id</label><input type="text" name="login" id="login"/></p>
          <p><label for="password">password</label><input type="password" name="password" id="password"/></p>
          <p><input type="submit"/></p>
        </form>
      </body>
    </html>
  }

  post("/") {
    scentry.authenticate()

    if (isAuthenticated) {
      redirect("/")
    }
    else {
      redirect("/sessions/new")
    }
  }

  override def getAuthenticationProvider: AuthenticationProvider = app.getAuthenticationProvider
}
