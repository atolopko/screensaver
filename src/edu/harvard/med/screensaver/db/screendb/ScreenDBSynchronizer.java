// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;

/**
 * Synchronizes the Screensaver database to the latest contents of ScreenDB. For every ScreenDB
 * entity parsed, the synchronizer tries to find a matching corresponding entity in Screensaver.
 * If it finds one, it will update the existing entity to match the contents in ScreenDB. If not,
 * it will create a new Screensaver entity to represent the ScreenDB entity.
 * <p>
 * This procedure works fine, unless a ScreenDB entity previously synchronized over to Screensaver
 * is modified in such a way that the synchronizer is unable to make the connection between the
 * new version of the ScreenDB entity, and the old version as translated into Screensaver terms.
 * In such a case, the original entity will be duplicated in Screensaver, having both an up-to-date
 * version, and an out-of-date version.
 * <p>
 * This could only happen when part of the "business key" for the entity changes. These aren't
 * necessarilly Screensaver entity business keys, and also should not really change that much.
 * A list of the different entity types, and the "business keys" used by the synchronizer, follows:
 * 
 * <ul>
 * <li>ScreeningRoomUsers are looked up by firstName, lastName
 * <li>Libraries are looked up by startPlate
 * <li>Screns are looked up by screenNumber
 * </ul>
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenDBSynchronizer
{
  // static members

  private static Logger log = Logger.getLogger(ScreenDBSynchronizer.class);
  static {
    try {
      Class.forName("org.postgresql.Driver");
    }
    catch (ClassNotFoundException e) {
      log.error("couldn't find postgresql driver");
    }    
  }
  
  public static void main(String [] args)
  {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      CommandLineApplication.DEFAULT_SPRING_CONFIG,
    });
    Options options = new Options();
    options.addOption("S", true, "server");
    options.addOption("D", true, "database");
    options.addOption("U", true, "username");
    options.addOption("P", true, "password");
    CommandLine commandLine;
    try {
      commandLine = new GnuParser().parse(options, args);
    }
    catch (ParseException e) {
      log.error("error parsing command line options", e);
      return;
    }
    ScreenDBSynchronizer synchronizer = new ScreenDBSynchronizer(
      commandLine.getOptionValue("S"),
      commandLine.getOptionValue("D"),
      commandLine.getOptionValue("U"),
      commandLine.getOptionValue("P"),
      (DAO) context.getBean("dao"));
    synchronizer.synchronize();
    log.info("successfully synchronized with ScreenDB.");
  }
  

  // instance data members
  
  private String _server;
  private String _database;
  private String _username;
  private String _password;
  private Connection _connection;
  private DAO _dao;
  private ScreenDBCompoundCherryPickSynchronizer compoundCherryPickSynchronizer;
  //private ScreenDBRnaiCherryPickSynchronizer rnRnaiCherryPickSynchronizer;
  

  // public constructors and methods

  public ScreenDBSynchronizer(String server, String database, String username, String password, DAO dao)
  {
    _server = server;
    _database = database;
    _username = username;
    _password = password;
    _dao = dao;
  }
  
  /**
   * Synchronize Screensaver with ScreenDB.
   * @throws ScreenDBSynchronizationException whenever there is a problem synchronizing
   */
  public void synchronize() throws ScreenDBSynchronizationException
  {
    initializeConnection();
    deleteOldCherryPickRequests();
    synchronizeLibraries();
    synchronizeNonLibraries();
    closeConnection();
  }

  private void initializeConnection() throws ScreenDBSynchronizationException
  {
    try {
      _connection = DriverManager.getConnection(
        "jdbc:postgresql://" + _server + "/" + _database,
        _username,
        _password);
    }
    catch (SQLException e) {
      throw new ScreenDBSynchronizationException("could not connect to ScreenDB database", e);
    }
  }

  /**
   * Delete the old compound cherry pick requests in a separate transaction.
   * 
   * @motivation This should really be part of
   *             {@link ScreenDBCompoundCherryPickSynchronizer} and
   *             {@link ScreenDBRnaiCherryPickSynchronizer}, but I need to run
   *             it in a separate transaction or I get hibernate exceptions
   *             about deleted entities that would be resaved by cascade. I
   *             probably should refactor things a bit so I can call
   *             "deleteOldCherryPickRequests" methods in the two
   *             above-mentioned synchronizers, in separate transactions.
   */
  private void deleteOldCherryPickRequests()
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        for (CherryPickRequest request : _dao.findAllEntitiesWithType(CherryPickRequest.class)) {
          _dao.deleteCherryPickRequest(request, true);
        }
      }
    });
  }

  private void synchronizeLibraries() throws ScreenDBSynchronizationException
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        ScreenDBLibrarySynchronizer librarySynchronizer =
          new ScreenDBLibrarySynchronizer(_connection, _dao);
        librarySynchronizer.synchronizeLibraries();
      }
    });
  }

  private void synchronizeNonLibraries() throws ScreenDBSynchronizationException
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        ScreenDBUserSynchronizer userSynchronizer = new ScreenDBUserSynchronizer(_connection, _dao);
        userSynchronizer.synchronizeUsers();
        ScreenDBScreenSynchronizer screenSynchronizer =
          new ScreenDBScreenSynchronizer(_connection, _dao, userSynchronizer);
        screenSynchronizer.synchronizeScreens();
        ScreenDBLibraryScreeningSynchronizer libraryScreeningSynchronizer =
          new ScreenDBLibraryScreeningSynchronizer(_connection, _dao, userSynchronizer, screenSynchronizer);
        libraryScreeningSynchronizer.synchronizeLibraryScreenings();
        ScreenDBCompoundCherryPickSynchronizer compoundCherryPickSynchronizer =
          new ScreenDBCompoundCherryPickSynchronizer(_connection, _dao, userSynchronizer, screenSynchronizer);
        compoundCherryPickSynchronizer.synchronizeCompoundCherryPicks();
        ScreenDBRNAiCherryPickSynchronizer rnaiCherryPickSynchronizer =
          new ScreenDBRNAiCherryPickSynchronizer(_connection, _dao, userSynchronizer, screenSynchronizer);
        rnaiCherryPickSynchronizer.synchronizeRNAiCherryPicks();
      }
    });
  }

  private void closeConnection()
  {
    try {
      _connection.close();
    }
    catch (SQLException e) {
    }
  }
}

