package nl.knaw.dans.easy.session.components

import javax.naming.NamingEnumeration
import javax.naming.directory.{ BasicAttributes, SearchControls, SearchResult }
import javax.naming.ldap.LdapContext

import org.scalamock.handlers.CallHandler3
import org.scalamock.scalatest.MockFactory

object LdapMocker extends MockFactory {

  // TODO why not needed in easy-dowload?
  class MockedSearchResult extends SearchResult("", "", new BasicAttributes())

  val mockedLdpContext: LdapContext = mock[LdapContext]
  private val mockedLdapSearchResults = mock[NamingEnumeration[SearchResult]]
  private val mockedLdapSearchResult = mock[MockedSearchResult]


  def expectLdapSearch: CallHandler3[String, String, SearchControls, NamingEnumeration[SearchResult]] = {
    (mockedLdpContext.search(_: String, _: String, _: SearchControls)) expects(*, *, *)
  }

  def expectLdapAttributes(attributes: BasicAttributes): Unit = {
    expectLdapSearch returning mockedLdapSearchResults
    mockedLdpContext.close _ expects()

    mockedLdapSearchResults.hasMoreElements _ expects() returning true
    mockedLdapSearchResults.hasMoreElements _ expects() returning false
    mockedLdapSearchResults.nextElement _ expects() returning mockedLdapSearchResult
    mockedLdapSearchResult.getAttributes _ expects() returning attributes
  }

}
