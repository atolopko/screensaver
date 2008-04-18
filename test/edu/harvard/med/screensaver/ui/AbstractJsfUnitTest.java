// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

import org.apache.log4j.Logger;
import org.jboss.jsfunit.facade.JSFClientSession;
import org.jboss.jsfunit.facade.JSFServerSession;
import org.jboss.jsfunit.framework.WebConversationFactory;

import com.meterware.httpunit.WebConversation;

/**
 * Base class for user interface unit tests. 
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class AbstractJsfUnitTest extends org.apache.cactus.ServletTestCase
{
  
  private static final Logger log = Logger.getLogger(AbstractJsfUnitTest.class);

  public static final String TEST_USER_NAME = "testuser";
  public static final String TEST_USER_PASSWORD = "testuser";

  // nested class to avoid deployment-time complexities (only *JsfUnitTest named classes are deployed for UI testing) 
  public static class JsfUnitException extends RuntimeException
  {
    private static final long serialVersionUID = 1L;

    public JsfUnitException(Throwable causedBy)
    {
      super(causedBy);
    }
  }

  
  protected SchemaUtil _schemaUtil;
  protected AdministratorUser _testUser;
  protected GenericEntityDAO _dao;
  protected WebConversation _webConv;
  
  /**
   * Sets up the database with a test user and authenticates the user via BASIC
   * authentication servlet mechanism. Will abort unless database is a test
   * database (see {@link SchemaUtil#verifyIsTestDatabase()}). Navigates to the
   * main page.
   */
  @Override
  protected void setUp() throws Exception
  {
    // we create a CommandLineApplication so that we can obtain the DAO object
    // that is needed to initialize the test database; we need the
    // CommandLineApplication to get around a Catch-22: we cannot use the web
    // app's DAO object, since obtaining it first requires making a request,
    // which in turn requires authenticating as user, although no users are
    // defined in the database until we create one, which requires the DAO
    // object
    CommandLineApplication app = new CommandLineApplication(new String[] {});
    app.setSpringConfigurationResource("spring-context-cmdline.xml");
    boolean processOptions = app.processOptions(true, false);
    assertTrue("initialize command line application", processOptions);
    _dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    _schemaUtil = (SchemaUtil) app.getSpringBean("schemaUtil");

    _schemaUtil.truncateTablesOrCreateSchema();
    log.debug("truncated database tables");

    _testUser = createTestUser("Test", "User");

    // We assume BASIC servlet authentication mechanism is in use, and so we
    // specify authentication credentials in this way; all unit tests here can
    // assume the test user is logged in
    _webConv = WebConversationFactory.makeWebConversation();
    _webConv.setAuthorization(TEST_USER_NAME, TEST_USER_PASSWORD);

    JSFClientSession client = new JSFClientSession(_webConv, "/main/main.jsf");
    JSFServerSession server = new JSFServerSession(client);
    _schemaUtil.verifyIsTestDatabase();
  }

  private AdministratorUser createTestUser(String first, String last)
  {
    AdministratorUser user = 
      new AdministratorUser(first,
                            last,
                            first.toLowerCase() + "_" + last.toLowerCase() + "@hms.harvard.edu",
                            "",
                            "",
                            "for jsfunit testing",
                            (first + last).toLowerCase(),
                            (first + last).toLowerCase());
    user.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
    user.addScreensaverUserRole(ScreensaverUserRole.DEVELOPER);
    user.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
    user.addScreensaverUserRole(ScreensaverUserRole.SCREENS_ADMIN);
    user.addScreensaverUserRole(ScreensaverUserRole.SCREEN_RESULTS_ADMIN);
    user.addScreensaverUserRole(ScreensaverUserRole.CHERRY_PICK_ADMIN);
    _dao.persistEntity(user);
    log.debug("created test user " + user);
    return user;
  }
  
  protected JSFClientSession getClient(String jsfPagePath)
  {
    try {
      return new JSFClientSession(_webConv, jsfPagePath);
    }
    catch (Exception e) {
      throw new JsfUnitException(e);
    }
  }
  
  protected JSFServerSession getServer(JSFClientSession client)
  {
    try {
      return new JSFServerSession(client);
    }
    catch (Exception e) {
      throw new JsfUnitException(e);
    }
  }
}