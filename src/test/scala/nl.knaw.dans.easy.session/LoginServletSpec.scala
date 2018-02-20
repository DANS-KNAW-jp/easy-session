package nl.knaw.dans.easy.session

import org.apache.commons.configuration.PropertiesConfiguration
import org.eclipse.jetty.http.HttpStatus._
import org.scalamock.scalatest.MockFactory
import org.scalatra.test.scalatest.ScalatraSuite

class LoginServletSpec extends TestSupportFixture with ServletFixture with ScalatraSuite with MockFactory {

  private val configuration: PropertiesConfiguration = new PropertiesConfiguration() {
    addProperty("ldap.provider.url", "ldap://hostDoesNotExist")
  }

  def app: EasySessionApp = new EasySessionApp(new ApplicationWiring(new Configuration("", configuration)) {
    //override val authentication: AuthenticationProvider = mock[AuthenticationProvider]
  })

  addServlet(new LoginServlet(app), "/*")

  "get /new" should "present a login form" in {
    get("/new") {
      status shouldBe OK_200
      body should include("""<label for="login">""")
    }
  }

  "post /" should "redirect to /sessions/new" in {

    post(
      uri = "/",
      params = Seq()
    ) {
      status shouldBe MOVED_TEMPORARILY_302
      header("Location") should startWith("http://localhost:")
      header("Location") should endWith("/sessions/new")
    }
  }
}
