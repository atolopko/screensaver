// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import edu.harvard.med.iccbl.screensaver.policy.WebDataAccessPolicy;
import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.accesspolicy.DataAccessPolicy;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.TestDataFactory;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.NaturalProductReagent;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.Lab;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

/**
 * Tests WedDataAccessPolicy implementation, as well as Hibernate interceptor-based
 * mechanism for setting "restricted" flag on entities.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class WebDataAccessPolicyTest extends AbstractSpringTest
{
  // static members

  private static Logger log = Logger.getLogger(WebDataAccessPolicyTest.class);


  // instance data members
  
  protected GenericEntityDAO genericEntityDao;
  protected LibrariesDAO librariesDao;
  protected SchemaUtil schemaUtil;
  protected CurrentScreensaverUser currentScreensaverUser;
  protected DataAccessPolicy dataAccessPolicy;
  protected ScreenResultParser screenResultParser;
  
  // public constructors and methods
  
  @Override
  protected String[] getConfigLocations()
  {
    return new String[] { "spring-context-test-security.xml" };
  }
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    schemaUtil.truncateTablesOrCreateSchema();
  }
  
  @Override
  protected void onTearDown() throws Exception
  {
    // must clear the "current" user, so that subsequent tests are not affected
    // by data access permissions
    currentScreensaverUser.setScreensaverUser(null);
  }
  
  public void testScreensaverUserPermissions()
  {
    final ScreeningRoomUser[] users = new ScreeningRoomUser[4];
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        users[0] = makeUserWithRoles(true, ScreensaverUserRole.SMALL_MOLECULE_SCREENER);
        users[1] = makeUserWithRoles(false, ScreensaverUserRole.SMALL_MOLECULE_SCREENER);
        users[2] = makeUserWithRoles(false, ScreensaverUserRole.SMALL_MOLECULE_SCREENER);
        users[3] = makeUserWithRoles(false, ScreensaverUserRole.SMALL_MOLECULE_SCREENER);
        users[1].setLab(users[0].getLab());
        users[2].setLab(users[0].getLab());
      }
    });
    
    // wrap in a transaction, and use findEntityById, to avoid problems with comparing entities
    // from different sessions for identity, and problems with lazy init exceptions
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        List<ScreeningRoomUser> filteredUsers = genericEntityDao.findAllEntitiesOfType(
          ScreeningRoomUser.class);
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreeningRoomUser.class,
          users[1].getScreensaverUserId()));
        assertTrue(filteredUsers.contains(currentScreensaverUser.getScreensaverUser()));
        for (Iterator iter = filteredUsers.iterator(); iter.hasNext();) {
          ScreeningRoomUser user = (ScreeningRoomUser) iter.next();
          if (user.isRestricted()) {
            iter.remove();
          }
        }
        assertTrue("user can view own account ", filteredUsers.contains(genericEntityDao.findEntityById(
          ScreeningRoomUser.class,
          users[1].getScreensaverUserId())));
        assertTrue("user can view account of user in same lab", filteredUsers.contains(genericEntityDao.findEntityById(
          ScreeningRoomUser.class,
          users[2].getScreensaverUserId())));
        assertTrue("user can view account of lab head", filteredUsers.contains(genericEntityDao.findEntityById(
          ScreeningRoomUser.class,
          users[0].getScreensaverUserId())));
        assertFalse("user cannot view account of user not in same lab", filteredUsers.contains(genericEntityDao.findEntityById(
          ScreeningRoomUser.class,
          users[3].getScreensaverUserId())));
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        List<ScreeningRoomUser> filteredUsers = genericEntityDao.findAllEntitiesOfType(ScreeningRoomUser.class);
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreeningRoomUser.class,
          users[0].getScreensaverUserId()));
        for (Iterator iter = filteredUsers.iterator(); iter.hasNext();) {
          ScreeningRoomUser user = (ScreeningRoomUser) iter.next();
          if (user.isRestricted()) {
            iter.remove();
          }
        }
        assertTrue("lab head can view own account", filteredUsers.contains(genericEntityDao.findEntityById(
          ScreeningRoomUser.class,
          users[0].getScreensaverUserId())));
        assertTrue("lab head can view account of user in same lab", filteredUsers.contains(genericEntityDao.findEntityById(
          ScreeningRoomUser.class,
          users[1].getScreensaverUserId())));
        assertTrue("lab head can view account of user in same lab", filteredUsers.contains(genericEntityDao.findEntityById(
          ScreeningRoomUser.class,
          users[2].getScreensaverUserId())));
        assertFalse("lab head cannot view account of user not in same lab", filteredUsers.contains(genericEntityDao.findEntityById(
          ScreeningRoomUser.class,
          users[3].getScreensaverUserId())));
      }
    });
  }

  public void testScreenPermissions()
  {
    ScreeningRoomUser rnaiUser = makeUserWithRoles(false, ScreensaverUserRole.RNAI_SCREENER);
    ScreeningRoomUser smallMoleculeUser = makeUserWithRoles(false, ScreensaverUserRole.SMALL_MOLECULE_SCREENER);
    ScreeningRoomUser smallMoleculeRnaiUser = makeUserWithRoles(false, ScreensaverUserRole.SMALL_MOLECULE_SCREENER, ScreensaverUserRole.RNAI_SCREENER);

    Screen rnaiScreen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
    ScreenResult screenResult1 = rnaiScreen.createScreenResult();
    screenResult1.setShareable(true);
    Screen smallMoleculeScreen = MakeDummyEntities.makeDummyScreen(2, ScreenType.SMALL_MOLECULE);
    ScreenResult screenResult2 = smallMoleculeScreen.createScreenResult();
    screenResult2.setShareable(true);

    rnaiScreen.setLeadScreener(rnaiUser);
    smallMoleculeScreen.setLeadScreener(smallMoleculeUser);
    rnaiScreen.addCollaborator(smallMoleculeRnaiUser);
    smallMoleculeScreen.addCollaborator(smallMoleculeRnaiUser);
    
    genericEntityDao.saveOrUpdateEntity(rnaiUser);
    genericEntityDao.saveOrUpdateEntity(smallMoleculeUser);
    genericEntityDao.saveOrUpdateEntity(smallMoleculeRnaiUser);
    genericEntityDao.saveOrUpdateEntity(rnaiScreen.getLabHead());
    genericEntityDao.saveOrUpdateEntity(rnaiScreen);
    genericEntityDao.saveOrUpdateEntity(smallMoleculeScreen.getLabHead());
    genericEntityDao.saveOrUpdateEntity(smallMoleculeScreen);

    List<Screen> screens = genericEntityDao.findAllEntitiesOfType(Screen.class);
    assertEquals("screens count", 2, screens.size());
    for (Screen screen : screens) {
      if (screen.getScreenType().equals(ScreenType.RNAI)) {
        currentScreensaverUser.setScreensaverUser(rnaiUser);
        assertTrue("rnai user is not restricted from rnai screens", !screen.isRestricted());
        assertTrue("rnai user is not restricted from shared rnai screen result", !screen.getScreenResult().isRestricted());
        currentScreensaverUser.setScreensaverUser(smallMoleculeUser);
        assertTrue("small molecule user is restricted from rnai screens", screen.isRestricted());
        assertTrue("small molecule user is restricted from shared rnai screen result", screen.getScreenResult().isRestricted());
        currentScreensaverUser.setScreensaverUser(smallMoleculeRnaiUser);
        assertTrue("small molecule+rnai user is not restricted from rnai screens", !screen.isRestricted());
        assertTrue("small molecule+rnai user is not restricted from shared rnai screen result", !screen.getScreenResult().isRestricted());
      } 
      else if (screen.getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
        currentScreensaverUser.setScreensaverUser(rnaiUser);
        assertTrue("rnai user is restricted from small molecule screens", screen.isRestricted());
        assertTrue("rnai user is restricted from shared small molecule screen result", screen.getScreenResult().isRestricted());
        currentScreensaverUser.setScreensaverUser(smallMoleculeUser);
        assertTrue("small molecule user is not restricted from small molecule screens", !screen.isRestricted());
        assertTrue("small molecule user is not restricted from shared small molecule screen result", !screen.getScreenResult().isRestricted());
        currentScreensaverUser.setScreensaverUser(smallMoleculeRnaiUser);
        assertTrue("small molecule+rnai user is not restricted from small molecule screens", !screen.isRestricted());
        assertTrue("small molecule+rnai user is not restricted from shared small molecule screen result", !screen.getScreenResult().isRestricted());
      }
      else {
        fail("unknown screen type" + screen.getScreenType());
      }
    }
  }
  
  public void testRNAiScreenResultPermissions()
  {
    doTestScreenResultPermissionsForScreenType(ScreenType.RNAI);
  }

  public void testSmallMoleculeScreenResultPermissions()
  {
    doTestScreenResultPermissionsForScreenType(ScreenType.SMALL_MOLECULE);
  }
  
  public void doTestScreenResultPermissionsForScreenType(final ScreenType screenType) 
  {
    final ScreeningRoomUser[] users = new ScreeningRoomUser[7];
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        // define the library and wells needed to import ScreenResultTest115.xls
        Library library = new Library(
          "library 1",
          "lib1",
          screenType,
          LibraryType.COMMERCIAL,
          1,
          3);
        librariesDao.loadOrCreateWellsForLibrary(library);
        genericEntityDao.saveOrUpdateEntity(library);
        genericEntityDao.flush();

        ScreensaverUserRole role = screenType.equals(ScreenType.SMALL_MOLECULE) ? ScreensaverUserRole.SMALL_MOLECULE_SCREENER : ScreensaverUserRole.RNAI_SCREENER;
        ScreensaverUserRole otherRole = screenType.equals(ScreenType.SMALL_MOLECULE) ? ScreensaverUserRole.RNAI_SCREENER : ScreensaverUserRole.SMALL_MOLECULE_SCREENER;
        users[0] = makeUserWithRoles(false, role); // lead screener for screen 115
        users[1] = makeUserWithRoles(false, role); // collaborator for screen 115
        users[2] = makeUserWithRoles(false, role); // lab member with above users, but not associated with screen 115, and no deposited screen result data
        users[3] = makeUserWithRoles(true, role); // lab head of above users, not otherwise associated with screen 115
        users[4] = makeUserWithRoles(false, role); // unaffiliated with previous users, and has deposited screen result data
        users[5] = makeUserWithRoles(false, otherRole); // unaffiliated with previous users, missing appropriate screening type role
        users[6] = makeUserWithRoles(false, role, otherRole); // unaffiliated with previous users, dual screening type roles, has deposited screen result data
        
        users[0].setLab(users[3].getLab());
        users[1].setLab(users[3].getLab());
        users[2].setLab(users[3].getLab());

        Screen screen115 = MakeDummyEntities.makeDummyScreen(115, screenType);
        try {
          screenResultParser.parse(screen115, new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, 
                                                       ScreenResultParserTest.SCREEN_RESULT_115_TEST_WORKBOOK_FILE));
        }
        catch (FileNotFoundException e) {
          fail(e.getMessage());
        }

        if (screenResultParser.getHasErrors()) {
          log.error(screenResultParser.getErrors());
        }
        assertFalse("screenresult import successful", screenResultParser.getHasErrors());
       
        screen115.setLabHead((LabHead) users[3]);
        screen115.setLeadScreener(users[0]);
        screen115.addCollaborator(users[1]);
        
        Screen screen116 = MakeDummyEntities.makeDummyScreen(116, screenType);
        try {
          screenResultParser.parse(screen116, new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, 
                                                       ScreenResultParserTest.SCREEN_RESULT_116_TEST_WORKBOOK_FILE));
        }
        catch (FileNotFoundException e) {
          fail(e.getMessage());
        }
        screen116.getScreenResult().setShareable(true);
        assertEquals("screenresult import successful", 0, screenResultParser.getErrors().size());
        screen116.setLeadScreener(users[4]);
        screen116.addCollaborator(users[6]);
        
        Screen screen117 = MakeDummyEntities.makeDummyScreen(117, screenType);
        screen117.createScreenResult();
        screen117.setLeadScreener(users[6]);
        
        for (int i = 0; i < users.length; i ++) {
          genericEntityDao.saveOrUpdateEntity(users[i]);          
        }
        genericEntityDao.saveOrUpdateEntity(screen115);
        genericEntityDao.saveOrUpdateEntity(screen116.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen116);
        genericEntityDao.saveOrUpdateEntity(screen117.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen117);
        
      }
    } );
    
    // put in a transaction to avoid problems with equality tests and lazy inits. -s
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 115);

        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[0].getEntityId()));
        ScreenResult screenResult1 = genericEntityDao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
        assertTrue("lead screener can view own, private screen result", !screenResult1.isRestricted());

        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[1].getEntityId()));
        ScreenResult screenResult2 = genericEntityDao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
        assertTrue("screen collaborator can view own, private screen result", !screenResult2.isRestricted());

        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[3].getEntityId()));
        ScreenResult screenResult3 = genericEntityDao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
        assertTrue("lab head can view own private screen result", !screenResult3.isRestricted());

        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[2].getEntityId()));
        ScreenResult screenResult4 = genericEntityDao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
        assertTrue("lab member cannot view lab's private screen result, if not also lead screener, lab head, or collaborator", screenResult4.isRestricted());

        Screen screen116 = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 116);

        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[0].getEntityId()));
        ScreenResult screenResult5 = genericEntityDao.findEntityById(ScreenResult.class, screen116.getScreenResult().getEntityId());
        assertTrue("screener with deposited data can view shareable screen result", !screenResult5.isRestricted());

        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[2].getEntityId()));
        ScreenResult screenResult6 = genericEntityDao.findEntityById(ScreenResult.class, screen116.getScreenResult().getEntityId());
        assertTrue("screener without deposited data cannot view shareable screen result", screenResult6.isRestricted());

        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[4].getEntityId()));
        ScreenResult screenResult7 = genericEntityDao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
        assertTrue("screener with deposited data cannot view private screen result", screenResult7.isRestricted());

        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[5].getEntityId()));
        ScreenResult screenResult8 = genericEntityDao.findEntityById(ScreenResult.class, screen116.getScreenResult().getEntityId());
        assertTrue("screener missing appropriate screen type role cannot view shareable screen result", screenResult8.isRestricted());

        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[6].getEntityId()));
        ScreenResult screenResult9 = genericEntityDao.findEntityById(ScreenResult.class, screen116.getScreenResult().getEntityId());
        assertTrue("screener with dual screen type roles can view shareable screen result", !screenResult9.isRestricted());
      }
    });
  }
  
  public void testRNAiCherryPickRequestPermissions()
  {
    final ScreeningRoomUser[] users = new ScreeningRoomUser[6];
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        // define the library and wells needed to import ScreenResultTest115.xls
        Library library = new Library(
          "library 1",
          "lib1",
          ScreenType.RNAI,
          LibraryType.COMMERCIAL,
          1,
          3);
        librariesDao.loadOrCreateWellsForLibrary(library);
        genericEntityDao.saveOrUpdateEntity(library);
        genericEntityDao.flush();

        users[0] = makeUserWithRoles(false, ScreensaverUserRole.RNAI_SCREENER); // lead screener for screen 115
        users[1] = makeUserWithRoles(false, ScreensaverUserRole.RNAI_SCREENER); // collaborator for screen 115
        users[2] = makeUserWithRoles(false, ScreensaverUserRole.RNAI_SCREENER); // lab member with above users, but not associated with screen 115
        users[3] = makeUserWithRoles(true, ScreensaverUserRole.RNAI_SCREENER); // lab head of above users, not otherwise associated with screen 115
        users[4] = makeUserWithRoles(false, ScreensaverUserRole.RNAI_SCREENER); // unaffiliated with previous users
        users[5] = makeUserWithRoles(false, ScreensaverUserRole.SMALL_MOLECULE_SCREENER); // unaffiliated with previous users, small molecule screener role only
        
        users[0].setLab(users[3].getLab());
        users[1].setLab(users[3].getLab());
        users[2].setLab(users[3].getLab());

        Screen screen = MakeDummyEntities.makeDummyScreen(115, ScreenType.RNAI);
        screen.setLabHead((LabHead) users[3]);
        screen.setLeadScreener(users[0]);
        screen.addCollaborator(users[1]);
        screen.createCherryPickRequest();
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 115);
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[0].getEntityId()));
        RNAiCherryPickRequest rnaiCherryPickRequest = genericEntityDao.findEntityById(RNAiCherryPickRequest.class, screen.getCherryPickRequests().iterator().next().getEntityId());
        assertTrue("lead screener can view RNAi Cherry Pick Request", !rnaiCherryPickRequest.isRestricted());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[1].getEntityId()));
        assertTrue("collaborator can view RNAi Cherry Pick Request", !rnaiCherryPickRequest.isRestricted());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[3].getEntityId()));
        assertTrue("lab head can view RNAi Cherry Pick Request", !rnaiCherryPickRequest.isRestricted());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[2].getEntityId()));
        assertTrue("lab member not associated with screen cannot view RNAi Cherry Pick Request", rnaiCherryPickRequest.isRestricted());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[2].getEntityId()));
        assertTrue("lab member not associated with screen cannot view RNAi Cherry Pick Request", rnaiCherryPickRequest.isRestricted());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[4].getEntityId()));
        assertTrue("non-lab member cannot view RNAi Cherry Pick Request", rnaiCherryPickRequest.isRestricted());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[5].getEntityId()));
        assertTrue("non-rnai user cannot view RNAi Cherry Pick Request", rnaiCherryPickRequest.isRestricted());
      }
    });
  }
  
  public void testMarcusAdminRestrictions()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        AdministratorUser marcusAdmin = new AdministratorUser("Marcus", "Admin", "", "", "", "", null, "");
        marcusAdmin.addScreensaverUserRole(ScreensaverUserRole.MARCUS_ADMIN);
        AdministratorUser normalAdmin = new AdministratorUser("Normal", "Admin", "", "", "", "", null, "");
        normalAdmin.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
        Screen nonMarcusScreen = MakeDummyEntities.makeDummyScreen(2);
        nonMarcusScreen.createCherryPickRequest();
        nonMarcusScreen.createLibraryScreening(nonMarcusScreen.getLeadScreener(), new LocalDate());
        nonMarcusScreen.createScreenResult();
        Screen marcusScreen = MakeDummyEntities.makeDummyScreen(1);
        FundingSupport marcusFundingSupport = new FundingSupport(WebDataAccessPolicy.MARCUS_LIBRARY_SCREEN_FUNDING_SUPPORT_NAME);
        marcusScreen.addFundingSupport(marcusFundingSupport);
        marcusScreen.createCherryPickRequest();
        marcusScreen.createLibraryScreening(marcusScreen.getLeadScreener(), new LocalDate());
        marcusScreen.createScreenResult();
        genericEntityDao.persistEntity(marcusFundingSupport);
        genericEntityDao.persistEntity(marcusAdmin);
        genericEntityDao.persistEntity(normalAdmin);
        genericEntityDao.persistEntity(marcusScreen);
        genericEntityDao.persistEntity(nonMarcusScreen);
      }
    });
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen marcusScreen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        Screen nonMarcusScreen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 2);

        AdministratorUser marcusAdmin = genericEntityDao.findEntityByProperty(AdministratorUser.class, "firstName", "Marcus");
        currentScreensaverUser.setScreensaverUser(marcusAdmin);
        assertFalse(marcusScreen.isRestricted());
        assertFalse(marcusScreen.getScreenResult().isRestricted());
        assertFalse(marcusScreen.getLabActivities().iterator().next().isRestricted());
        assertFalse(marcusScreen.getCherryPickRequests().iterator().next().isRestricted());
        assertTrue(nonMarcusScreen.isRestricted());
        assertTrue(nonMarcusScreen.getScreenResult().isRestricted());
        assertTrue(nonMarcusScreen.getLabActivities().iterator().next().isRestricted());
        assertTrue(nonMarcusScreen.getCherryPickRequests().iterator().next().isRestricted());
        
        AdministratorUser normalAdmin = genericEntityDao.findEntityByProperty(AdministratorUser.class, "firstName", "Normal");
        currentScreensaverUser.setScreensaverUser(normalAdmin);
        assertFalse(marcusScreen.isRestricted());
        assertFalse(marcusScreen.getScreenResult().isRestricted());
        assertFalse(marcusScreen.getLabActivities().iterator().next().isRestricted());
        assertFalse(marcusScreen.getCherryPickRequests().iterator().next().isRestricted());
        assertFalse(nonMarcusScreen.isRestricted());
        assertFalse(nonMarcusScreen.getScreenResult().isRestricted());
        assertFalse(nonMarcusScreen.getLabActivities().iterator().next().isRestricted());
        assertFalse(nonMarcusScreen.getCherryPickRequests().iterator().next().isRestricted());
      }
    });
  }
  
  public void testSilencingReagentSequenceRestriction()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = new Library(
          "rnai library",
          "rnai lib",
          ScreenType.RNAI,
          LibraryType.COMMERCIAL,
          1,
          1);
        new TestDataFactory().newInstance(LibraryContentsVersion.class, library);
        library.createWell(new WellKey(1, 0, 0), LibraryWellType.EXPERIMENTAL).createSilencingReagent(new ReagentVendorIdentifier("vendor", "sirnai1"), SilencingReagentType.SIRNA, "ACTG");
        genericEntityDao.saveOrUpdateEntity(library);
        
        AdministratorUser admin = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
        admin.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
        admin.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
        genericEntityDao.saveOrUpdateEntity(admin);
        
        ScreeningRoomUser screener = new ScreeningRoomUser("Screener", "User", "");
        screener.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
        screener.addScreensaverUserRole(ScreensaverUserRole.RNAI_SCREENER);
        genericEntityDao.saveOrUpdateEntity(screener);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        SilencingReagent reagent = genericEntityDao.findAllEntitiesOfType(SilencingReagent.class, true, Reagent.well.to(Well.library).getPath()).get(0);
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityByProperty(AdministratorUser.class, "firstName", "Admin"));
        assertTrue("admin can access SilencingReagent.sequence", dataAccessPolicy.isAllowedAccessToSilencingReagentSequence(reagent));
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityByProperty(ScreeningRoomUser.class, "firstName", "Screener"));
        assertFalse("screener cannot access SilencingReagent.sequence", dataAccessPolicy.isAllowedAccessToSilencingReagentSequence(reagent));
      }
    });
  }
  
  public void testChemDiv6Restriction()
  {
    final TestDataFactory dataFactory = new TestDataFactory();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library chemDiv6Library = new Library(
          "ChemDiv6",
          "ChemDiv6",
          ScreenType.SMALL_MOLECULE,
          LibraryType.COMMERCIAL,
          1,
          1);
        dataFactory.newInstance(LibraryContentsVersion.class, chemDiv6Library);
        chemDiv6Library.createWell(new WellKey(1, 0, 0), LibraryWellType.EXPERIMENTAL).createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor", "Cdiv0001"), "molfile", "smiles", "inchi", new BigDecimal(1), new BigDecimal(1), new MolecularFormula("CCC")); 
        genericEntityDao.saveOrUpdateEntity(chemDiv6Library);
        
        Library notCDiv6Library = new Library(
                                              "NotChemDiv6",
                                              "NotChemDiv6",
                                              ScreenType.SMALL_MOLECULE,
                                              LibraryType.COMMERCIAL,
                                              2,
                                              2);
        dataFactory.newInstance(LibraryContentsVersion.class, notCDiv6Library);
        notCDiv6Library.createWell(new WellKey(2, 0, 0), LibraryWellType.EXPERIMENTAL).createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor", "notCdiv0001"), "molfile", "smiles", "inchi", new BigDecimal(1), new BigDecimal(1), new MolecularFormula("CCC")); 
        genericEntityDao.saveOrUpdateEntity(notCDiv6Library);
                                            
        AdministratorUser admin = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
        admin.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
        admin.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
        genericEntityDao.saveOrUpdateEntity(admin);
        
        ScreeningRoomUser screener = new ScreeningRoomUser("Screener", "User", "");
        screener.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
        screener.addScreensaverUserRole(ScreensaverUserRole.SMALL_MOLECULE_SCREENER);
        genericEntityDao.saveOrUpdateEntity(screener);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        SmallMoleculeReagent reagent = genericEntityDao.findEntityByProperty(SmallMoleculeReagent.class, SmallMoleculeReagent.vendorIdentifier.getPath(), "Cdiv0001", true, Reagent.well.to(Well.library).getPath());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityByProperty(AdministratorUser.class, "firstName", "Admin"));
        assertFalse("admin can access ChemDiv6 reagent", reagent.isRestricted());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityByProperty(ScreeningRoomUser.class, "firstName", "Screener"));
        assertTrue("screener cannot access ChemDiv6 reagent", reagent.isRestricted());

        reagent = genericEntityDao.findEntityByProperty(SmallMoleculeReagent.class, SmallMoleculeReagent.vendorIdentifier.getPath(), "notCdiv0001", true, Reagent.well.to(Well.library).getPath());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityByProperty(AdministratorUser.class, "firstName", "Admin"));
        assertFalse("admin can access non-ChemDiv6 reagent", reagent.isRestricted());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityByProperty(ScreeningRoomUser.class, "firstName", "Screener"));
        assertFalse("screener can access non-ChemDiv6 reagent", reagent.isRestricted());
      }
    });
  }
 
  public void testMarcusNaturalProductReagentRestriction()
  {
    final TestDataFactory dataFactory = new TestDataFactory();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library marcusLibrary = new Library(
          "Marcus Lib",
          "MarcusLib",
          ScreenType.SMALL_MOLECULE,
          LibraryType.NATURAL_PRODUCTS,
          1,
          1);
        dataFactory.newInstance(LibraryContentsVersion.class, marcusLibrary);
        marcusLibrary.createWell(new WellKey(1, 0, 0), LibraryWellType.EXPERIMENTAL).createNaturalProductReagent(new ReagentVendorIdentifier("vendor", "marcus0001"));
        genericEntityDao.saveOrUpdateEntity(marcusLibrary);
        
        Library notMarcusLibrary = new Library("Not Marcus Library",
                                              "NotMarcusLib",
                                              ScreenType.SMALL_MOLECULE,
                                              LibraryType.NATURAL_PRODUCTS,
                                              2,
                                              2);
        dataFactory.newInstance(LibraryContentsVersion.class, notMarcusLibrary);
        notMarcusLibrary.createWell(new WellKey(2, 0, 0), LibraryWellType.EXPERIMENTAL).createNaturalProductReagent(new ReagentVendorIdentifier("vendor", "notmarcus0001"));
        genericEntityDao.saveOrUpdateEntity(notMarcusLibrary);
                                            
        AdministratorUser admin = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
        admin.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
        admin.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
        genericEntityDao.saveOrUpdateEntity(admin);
        
        ScreeningRoomUser screener = new ScreeningRoomUser("Screener", "User", "");
        screener.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
        screener.addScreensaverUserRole(ScreensaverUserRole.SMALL_MOLECULE_SCREENER);
        genericEntityDao.saveOrUpdateEntity(screener);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        NaturalProductReagent reagent = genericEntityDao.findEntityByProperty(NaturalProductReagent.class, NaturalProductReagent.vendorIdentifier.getPath(), "marcus0001", true, Reagent.well.to(Well.library).getPath());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityByProperty(AdministratorUser.class, "firstName", "Admin"));
        assertFalse("admin can access Marcus nat prod reagent", reagent.isRestricted());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityByProperty(ScreeningRoomUser.class, "firstName", "Screener"));
        assertTrue("screener cannot access Marcus nat prod reagent", reagent.isRestricted());

        reagent = genericEntityDao.findEntityByProperty(NaturalProductReagent.class, NaturalProductReagent.vendorIdentifier.getPath(), "notmarcus0001", true, Reagent.well.to(Well.library).getPath());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityByProperty(AdministratorUser.class, "firstName", "Admin"));
        assertFalse("admin can access non-Marcus nat prod reagent", reagent.isRestricted());
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityByProperty(ScreeningRoomUser.class, "firstName", "Screener"));
        assertFalse("screener can access non-Marcus nat prod reagent", reagent.isRestricted());
      }
    });
  }

  private ScreeningRoomUser makeUserWithRoles(boolean isLabHead, ScreensaverUserRole... roles)
  {
    
    ScreeningRoomUser user;
    if (isLabHead) {
      user = new LabHead("first", 
                         "last" + new Object().hashCode(),
                         "email@hms.harvard.edu",
                         null);
    }
    else {
      user = new ScreeningRoomUser("first",
                                   "last" + new Object().hashCode(),
                                   "email@hms.harvard.edu");
    }
    for (ScreensaverUserRole role : roles) {
      user.addScreensaverUserRole(role);
    }
    genericEntityDao.saveOrUpdateEntity(user);
    return user;
  }
  
  public void testLibraryPermissions() 
  {
    final ScreeningRoomUser[] users = new ScreeningRoomUser[3];
    final AdministratorUser[] admUsers = new AdministratorUser[1];
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        users[0] = makeUserWithRoles(false, ScreensaverUserRole.RNAI_SCREENER); 

        users[1] = makeUserWithRoles(false, ScreensaverUserRole.SMALL_MOLECULE_SCREENER); 

        users[2] = makeUserWithRoles(true, ScreensaverUserRole.RNAI_SCREENER); 
        Lab lab = new Lab((LabHead) users[2]);
        users[0].setLab(lab);
        genericEntityDao.saveOrUpdateEntity(users[0]);
        
        //user with library_admin has to be an Administrator user otherwise error on validation. 
         admUsers[0] = new AdministratorUser("Joe", "Admin", "joe_admin@hms.harvard.edu", "", "", "", "", "");
         genericEntityDao.saveOrUpdateEntity(admUsers[0]);
         admUsers[0].addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
        
        Library library = new Library(
          "library 1",
          "lib1",
          ScreenType.RNAI,
          LibraryType.COMMERCIAL,
          100001,
          100002);
        library.setOwner(users[0]);
        librariesDao.loadOrCreateWellsForLibrary(library);
        genericEntityDao.saveOrUpdateEntity(library);
        genericEntityDao.flush();

      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {

        Library library = genericEntityDao.findEntityByProperty(Library.class, "shortName","lib1" );
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[0].getEntityId()));
        assertTrue("Owner can view (validation) library", !library.isRestricted());
        
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
          ScreensaverUser.class,
          users[1].getEntityId()));
        assertTrue("Not owner cannot view Validation Library", library.isRestricted());
        
/*  TODO ADD CHECK WHEN "NO SESSION" error is solved 
 *       currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
           ScreensaverUser.class,
           users[2].getEntityId()));        
        assertTrue("Library head of owner can view (validation) library", !library.isRestricted());*/
        
        currentScreensaverUser.setScreensaverUser(genericEntityDao.findEntityById(
           ScreensaverUser.class,
           admUsers[0].getEntityId()));         
        assertTrue("LibrarieAdmin can view (validation) library", !library.isRestricted());
        
        
        

      }
    });
  }  
}

