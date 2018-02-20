package nl.knaw.dans.easy.session

import org.apache.commons.configuration.PropertiesConfiguration
import org.eclipse.jetty.http.HttpStatus._
import org.scalamock.scalatest.MockFactory
import org.scalatra.test.scalatest.ScalatraSuite

class ProtectedServletSpec extends TestSupportFixture with ServletFixture with ScalatraSuite with MockFactory {

  private val configuration: PropertiesConfiguration = new PropertiesConfiguration() {
    addProperty("ldap.provider.url", "ldap://hostDoesNotExist")
  }

  def app: EasySessionApp = new EasySessionApp(new ApplicationWiring(new Configuration("", configuration)) {
    //override val authentication: AuthenticationProvider = mock[AuthenticationProvider]
  })

  addServlet(new ProtectedServlet(app), "/*")

  "get /" should "???" in {
    get("/") {
      status shouldBe MOVED_TEMPORARILY_302
      header("Location") should startWith("http://localhost:")
      header("Location") should include("/sessions/new;jsessionid=")
      header("Set-Cookie") should startWith("JSESSIONID=")
      header("Set-Cookie") should endWith(";Path=/")
    }
  }
}
