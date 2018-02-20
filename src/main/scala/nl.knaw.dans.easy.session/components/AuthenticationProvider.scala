package nl.knaw.dans.easy.session.components

import scala.util.Try

trait AuthenticationProvider {
  def getUser(userName: String, password: String): Try[Option[User]]
}
