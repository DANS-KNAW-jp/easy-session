package nl.knaw.dans.easy.session.components.authstrategies

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import nl.knaw.dans.easy.session.components.{ AuthenticationProvider, User }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy

import scala.util.{ Failure, Success }

class UserPasswordStrategy(protected val app: ScalatraBase, authenticationProvider: AuthenticationProvider)
                          (implicit request: HttpServletRequest, response: HttpServletResponse)
  extends ScentryStrategy[User]
    with DebugEnhancedLogging {

  override def name: String = "UserPassword"

  private def login: String = app.params.getOrElse("login", "")

  private def password: String = app.params.getOrElse("password", "")

  /**
   * Determine whether the strategy should be run for the current request.
   */
  override def isValid(implicit request: HttpServletRequest): Boolean = {
    logger.info("UserPasswordStrategy: determining isValid: " + (login != "" && password != "").toString)
    login != "" && password != ""
  }

  def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[User] = {
    logger.info("UserPasswordStrategy: attempting authentication")

    authenticationProvider.getUser(login, password) match {
      case Success(Some(user)) =>
        logger.info("UserPasswordStrategy: login succeeded")
        Some(user)
      case Success(None) =>
        logger.info("login failed")
        None
      case Failure(t) =>
        logger.error(s"login exception: $t", t) // TODO or throw?
        None
    }
  }

  /**
   * What should happen if the user is currently not authenticated?
   */
  override def unauthenticated()(implicit request: HttpServletRequest, response: HttpServletResponse) {
    app.redirect("/sessions/new")
  }
}

