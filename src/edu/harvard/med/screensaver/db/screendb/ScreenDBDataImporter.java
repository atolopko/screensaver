// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * Uploads a partial set of data from ScreenDB into Screensaver. For the
 * moment, only the tables with the most critical data are loaded:
 * {@link Library}, {@link Screen}, and {@link ScreeningRoomUser}.
 * 
 * <p>
 * In order to run this program, perform the following steps:
 * <ol>
 *   <li>
 *     create a database user <code>screendbweb</code> with password
 *     <code>screendbweb</code>
 *   </li>
 *   <li>
 *     create a database <code>screendb</code>
 *   </li>
 *   <li>
 *     load the contents of
 *     <code>screensaver/flotsam+jetsam/screendb/screendb-20060725.sql</code>
 *     into the <code>screendb</code> database (you can do this with the
 *     <code>psql</code> command <code>\i</code>)
 *   </li>
 *   <li>
 *     run {@link #main}.
 *   </li>
 * </ol>
 * </p>
 */
public class ScreenDBDataImporter
{

  private static Logger log = Logger.getLogger(ScreenDBDataImporter.class);
  
  public static void main(String [] args)
  {
    ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext(new String[] { 
      "spring-context-logging.xml",
      "spring-context-services.xml", 
      "spring-context-persistence.xml", 
    });
    ScreenDBDataImporter importer = (ScreenDBDataImporter) appCtx.getBean("screenDBDataImporter");
    importer.loadScreenDBData();
  }
  
  private DAO _dao;
  private SchemaUtil _schemaUtil;
  
  /**
   * Construct a new <code>ScreenDBDataImporter</code> object.
   * @param dao
   * @param schemaUtil
   */
  public ScreenDBDataImporter(DAO dao, SchemaUtil schemaUtil)
  {
    _dao = dao;
    _schemaUtil = schemaUtil;
    _schemaUtil.recreateSchema();
  }
  
  /**
   * Use a {@link ScreenDBProxy} to load the ScreenDB data into Screensaver.
   */
  public void loadScreenDBData()
  {
    ScreenDBProxy screenDBProxy = new ScreenDBProxy();
    for (Library library : screenDBProxy.getLibraries()) {
      _dao.persistEntity(library);
    }
    for (ScreeningRoomUser screeningRoomUser : screenDBProxy.getScreeningRoomUsers()) {
      log.info("saving user " + screeningRoomUser);
      log.info("with lab head " + screeningRoomUser.getLabHead());
      _dao.persistEntity(screeningRoomUser);
    }
    for (Screen screen : screenDBProxy.getScreens()) {
      _dao.persistEntity(screen);
    }
  }
}
