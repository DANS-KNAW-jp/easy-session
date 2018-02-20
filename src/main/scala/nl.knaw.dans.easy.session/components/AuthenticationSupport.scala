package nl.knaw.dans.easy.session.components

import nl.knaw.dans.easy.session.components.authstrategies.UserPasswordStrategy
import org.scalatra.ScalatraBase
import org.scalatra.auth.{ ScentryConfig, ScentrySupport }


trait AuthenticationSupport extends ScalatraBase
  with ScentrySupport[User] {
  self: ScalatraBase =>

  // TODO more than id? see also https://gist.github.com/casualjim/4400115#file-session_token_strategy-scala-L49-L50
  protected def fromSession: PartialFunction[String, User] = { case id: String => User(Map("uid" -> Seq(id))) }

  protected def toSession: PartialFunction[User, String] = { case usr: User => usr.id }

  def getAuthenticationProvider: AuthenticationProvider

  protected val scentryConfig: ScentryConfiguration = new ScentryConfig {
    override val login = "/sessions/new"
  }.asInstanceOf[ScentryConfiguration]

  protected def requireLogin(): Unit = {
    if (!isAuthenticated) {
      redirect(scentryConfig.login)
    }
  }

  /**
   * If an unauthenticated user attempts to access a route which is protected by Scentry,
   * run the unauthenticated() method on the UserPasswordStrategy.
   */
  override protected def configureScentry: Unit = {
    scentry.unauthenticated {
      scentry.strategies("UserPassword").unauthenticated()
    }
  }

  /**
   * Register auth strategies with Scentry. Any controller with this trait mixed in will attempt to
   * progressively use all registered strategies to log the user in, falling back if necessary.
   */
  override protected def registerAuthStrategies: Unit = {
    scentry.register("UserPassword", app => new UserPasswordStrategy(app, getAuthenticationProvider) {})
  }

}
