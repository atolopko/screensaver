// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

public class ScreenDBRNAiCherryPickSynchronizer
{

  // static members

  private static Logger log = Logger.getLogger(ScreenDBRNAiCherryPickSynchronizer.class);
  

  // instance data members
  
  private Connection _connection;
  private DAO _dao;
  private ScreenDBUserSynchronizer _userSynchronizer;
  private ScreenDBScreenSynchronizer _screenSynchronizer;
  private ScreenDBSynchronizationException _synchronizationException = null;

  
  // public constructors and methods

  public ScreenDBRNAiCherryPickSynchronizer(
    Connection connection,
    DAO dao,
    ScreenDBUserSynchronizer userSynchronizer,
    ScreenDBScreenSynchronizer screenSynchronizer)
  {
    _connection = connection;
    _dao = dao;
    _userSynchronizer = userSynchronizer;
    _screenSynchronizer = screenSynchronizer;
  }

  public void synchronizeRNAiCherryPicks() throws ScreenDBSynchronizationException
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        try {
          deleteOldRNAiCherryPickRequests();
          synchronizeRNAiCherryPicksProper();
        }
        catch (SQLException e) {
          _synchronizationException = new ScreenDBSynchronizationException(
            "Encountered an SQL exception while synchonrizing library screenings: " + e.getMessage(),
            e);
        }
        catch (ScreenDBSynchronizationException e) {
          _synchronizationException = e;
        }
      }
    });
    if (_synchronizationException != null) {
      throw _synchronizationException;
    }
  }


  // private instance methods
  
  private void deleteOldRNAiCherryPickRequests()
  {
    // NOTE: this is now done in ScreenDBSynchronizer.deleteOldCherryPickRequests. see comment
    // there for details
//    for (CherryPickRequest request : _dao.findAllEntitiesWithType(RNAiCherryPickRequest.class)) {
//      _dao.deleteCherryPickRequest(request, true);
//    }
  }

  private void synchronizeRNAiCherryPicksProper() throws SQLException, ScreenDBSynchronizationException
  {
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery(
      "SELECT * FROM visits v WHERE visit_type = 'RNAi Cherry Pick'");
    while (resultSet.next()) {
      BigDecimal volumeTransferred = new BigDecimal(resultSet.getFloat("cherry_pick_volume_per_well"));
      RNAiCherryPickRequest request = createRNAiCherryPickRequest(resultSet, volumeTransferred);
      CherryPickLiquidTransfer liquidTransfer = null;
      if (liquidTransferHappened(resultSet)) {
        request.setMicroliterTransferVolumePerWellApproved(volumeTransferred);
        liquidTransfer = createCherryPickLiquidTransfer(resultSet, volumeTransferred, request);
      }
      createCherryPickAssayPlates(resultSet, request, liquidTransfer);
      if (cherryPickScreeningHappened(resultSet)) {
        createCherryPickScreening(resultSet, volumeTransferred, request);
      }
      _dao.persistEntity(request);
    }
    statement.close();
  }

  private RNAiCherryPickRequest createRNAiCherryPickRequest(ResultSet resultSet, BigDecimal volumeTransferred)
  throws SQLException
  {
    Screen screen = _screenSynchronizer.getScreenForScreenNumber(resultSet.getInt("screen_id"));
    ScreeningRoomUser requestedBy =
      _userSynchronizer.getScreeningRoomUserForScreenDBUserId(resultSet.getInt("performed_by"));
    Date dateRequested = resultSet.getDate("cherry_pick_request_date");
    Integer visitId = resultSet.getInt("id");
    RNAiCherryPickRequest request =
      new RNAiCherryPickRequest(screen, requestedBy, dateRequested, visitId);
    request.setMicroliterTransferVolumePerWellRequested(volumeTransferred);
    request.setComments(resultSet.getString("comments"));
    return request;
  }

  private boolean liquidTransferHappened(ResultSet resultSet) throws SQLException
  {
    // date_of_visit => CPLiquidTransfer.dateOfActivity
    return resultSet.getDate("date_of_visit") != null;
  }

  // NOTE: this method has not yet been tested, as there are not yet any RNAi Cherry Pick Visits
  // in ScreenDB with non-null date_of_visit
  private CherryPickLiquidTransfer createCherryPickLiquidTransfer(
    ResultSet resultSet,
    BigDecimal volumeTransferred,
    RNAiCherryPickRequest request)
  throws SQLException
  {
    CherryPickLiquidTransfer liquidTransfer = new CherryPickLiquidTransfer(
      _userSynchronizer.getScreeningRoomUserForScreenDBUserId(resultSet.getInt("performed_by")),
      resultSet.getDate("date_created"),
      resultSet.getDate("date_of_visit"), // date_of_visit => CPLiquidTransfer.dateOfActivity
      request,
      true);
    liquidTransfer.setMicroliterVolumeTransferedPerWell(volumeTransferred);
    return liquidTransfer;
  }

  private void createCherryPickAssayPlates(
    ResultSet resultSet,
    RNAiCherryPickRequest request,
    CherryPickLiquidTransfer liquidTransfer)
  throws SQLException
  {
    StringTokenizer tokenizer =
      new StringTokenizer(resultSet.getString("cherry_pick_filenames"), "\n");
    int plateOrdinal = 0;
    while (tokenizer.hasMoreTokens()) {
      String filename = tokenizer.nextToken();
      if (filename.endsWith("\r")) {
        filename = filename.substring(0, filename.length() - 1);
      }
      // TODO: is EPPENDORF the correct plate type here?
      CherryPickAssayPlate assayPlate =
        new CherryPickAssayPlate(request, plateOrdinal, 0, PlateType.EPPENDORF);
      assayPlate.setComments("ScreenDB filename for this assay plate is \"" + filename + "\".");
      assayPlate.setCherryPickLiquidTransfer(liquidTransfer);
      plateOrdinal ++;
    }
  }
  
  private boolean cherryPickScreeningHappened(ResultSet resultSet) throws SQLException
  {
    return resultSet.getDate("cherry_pick_assay_date") != null;
  }

  // NOTE: this method has not yet been tested, as there are not yet any RNAi Cherry Pick Visits
  // in ScreenDB with non-null cherry_pick_assay_date
  private void createCherryPickScreening(ResultSet resultSet, BigDecimal volumeTransferred, RNAiCherryPickRequest request) throws SQLException {
    Screen screen = _screenSynchronizer.getScreenForScreenNumber(resultSet.getInt("screen_id"));
    ScreeningRoomUser performedBy =
      _userSynchronizer.getScreeningRoomUserForScreenDBUserId(resultSet.getInt("performed_by"));
    Date dateCreated = resultSet.getDate("date_created");
    Date dateOfActivity = resultSet.getDate("cherry_pick_assay_date");
    RNAiCherryPickScreening screening = new RNAiCherryPickScreening(
      screen,
      performedBy,
      dateCreated,          
      dateOfActivity,
      request);
    screening.setAssayProtocol(resultSet.getString("assay_protocol"));
    screening.setMicroliterVolumeTransferedPerWell(volumeTransferred);
  }
}
