// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id: ComplexDAOTest.java 2359 2008-05-09 21:16:57Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.apache.log4j.Logger;


/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenDAOTest extends AbstractSpringPersistenceTest
{

  private static final Logger log = Logger.getLogger(ScreenDAOTest.class);


  protected ScreenDAO screenDao;


  public void testFindNextScreenNumber()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Integer nextScreenNumber = screenDao.findNextScreenNumber();
        assertEquals(new Integer(1), nextScreenNumber);
        Screen screen = MakeDummyEntities.makeDummyScreen(nextScreenNumber);
        genericEntityDao.persistEntity(screen.getLabHead());
        genericEntityDao.persistEntity(screen.getLeadScreener());
        genericEntityDao.persistEntity(screen);

        nextScreenNumber = screenDao.findNextScreenNumber();
        assertEquals(new Integer(2), nextScreenNumber);
        screen = MakeDummyEntities.makeDummyScreen(nextScreenNumber);
        genericEntityDao.persistEntity(screen.getLabHead());
        genericEntityDao.persistEntity(screen.getLeadScreener());
        genericEntityDao.persistEntity(screen);
      }
    });
    Integer nextScreenNumber = screenDao.findNextScreenNumber();
    assertEquals(new Integer(3), nextScreenNumber);
    nextScreenNumber = screenDao.findNextScreenNumber();
    assertEquals(new Integer(3), nextScreenNumber);

    screenDao.deleteStudy(genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 2));
    nextScreenNumber = screenDao.findNextScreenNumber();
    assertEquals(new Integer(2), nextScreenNumber);
  }
}