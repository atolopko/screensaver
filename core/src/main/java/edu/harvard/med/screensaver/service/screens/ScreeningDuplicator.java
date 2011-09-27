// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screens;

import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

/**
 * Adds a new {@link Screening} to a {@link Screen}, copying the following properties from a
 * the most recent {@link Screening} of the same type, if it exists:
 * <ul>
 * <li>assayProtocol</li>
 * <li>assayProtocolLastModifiedDate</li>
 * <li>assayProtocolType</li>
 * <li>numberOfReplicates</li>
 * <li>volumeTransferredPerWellFromAssayPlates</li>
 * <li>volumeTransferredPerWellToLibraryPlates</li>
 * <li>concentration</li>
 * <li>performedBy</li>
 * </ul>
 * 
 * @author atolopko
 */
public class ScreeningDuplicator
{
  private static Logger log = Logger.getLogger(ScreeningDuplicator.class);
  
  private GenericEntityDAO _dao;
  
  /**
   * @motivation for CGLIB2
   */
  protected ScreeningDuplicator()
  {
  }
  
  public ScreeningDuplicator(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  @Transactional
  public LibraryScreening addLibraryScreening(Screen screen, AdministratorUser recordedBy)
  {
    screen = _dao.reloadEntity(screen, false, Screen.leadScreener);
    SortedSet<LibraryScreening> activities = screen.getLabActivitiesOfType(LibraryScreening.class);
    Screening lastScreening = null;
    if (!activities.isEmpty()) {
      lastScreening = activities.last();
    }
    LibraryScreening newScreening = screen.createLibraryScreening(recordedBy, screen.getLeadScreener(), new LocalDate());
    if (lastScreening != null) { 
      copyActivityProperties(lastScreening, newScreening);
    }
    return newScreening;
  }
  
  @Transactional
  public CherryPickScreening addCherryPickScreening(Screen screen,
                                                    ScreeningRoomUser performedBy,
                                                    AdministratorUser recordedBy,
                                                    CherryPickRequest cpr)
  {
    recordedBy = _dao.reloadEntity(recordedBy);
    performedBy = _dao.reloadEntity(performedBy);
    screen = _dao.reloadEntity(screen);
    cpr = _dao.reloadEntity(cpr);
    SortedSet<CherryPickScreening> activities = screen.getLabActivitiesOfType(CherryPickScreening.class);
    CherryPickScreening lastScreening = null;
    if (!activities.isEmpty()) {
      lastScreening = activities.last();
    }
    CherryPickScreening newScreening = screen.createCherryPickScreening(recordedBy,
                                                                        performedBy,
                                                                        new LocalDate(),
                                                                        cpr);
    
    if (lastScreening != null) {
      copyActivityProperties(lastScreening, newScreening);
    }
    return newScreening;
  }
  
  @Transactional
  public CherryPickLiquidTransfer addCherryPickLiquidTransfer(Screen screen,
                                                              ScreensaverUser performedBy,
                                                              AdministratorUser recordedBy, 
                                                              CherryPickLiquidTransferStatus status)
  {
    recordedBy = _dao.reloadEntity(recordedBy);
    performedBy = _dao.reloadEntity(performedBy);
    screen = _dao.reloadEntity(screen);
    CherryPickLiquidTransfer newCplt = screen.createCherryPickLiquidTransfer(recordedBy,
                                                                             performedBy,
                                                                             new LocalDate(),
                                                                             status);
    CherryPickLiquidTransfer lastCplt = null;
    SortedSet<CherryPickLiquidTransfer> activities = screen.getLabActivitiesOfType(CherryPickLiquidTransfer.class);
    if (!activities.isEmpty()) {
      lastCplt = activities.last();
    }
    if (lastCplt != null) {
      copyActivityProperties(lastCplt, newCplt);
    }
    return newCplt;
  }
  
  private void copyActivityProperties(Screening fromScreening, Screening toScreening)
  {
    copyActivityProperties((LabActivity) fromScreening, (LabActivity) toScreening);
    toScreening.setAssayProtocol(fromScreening.getAssayProtocol());
    toScreening.setAssayProtocolLastModifiedDate(fromScreening.getAssayProtocolLastModifiedDate());
    toScreening.setAssayProtocolType(fromScreening.getAssayProtocolType());
    toScreening.setNumberOfReplicates(fromScreening.getNumberOfReplicates());
    toScreening.setVolumeTransferredPerWellToAssayPlates(fromScreening.getVolumeTransferredPerWellToAssayPlates());
    toScreening.setAssayWellVolume(fromScreening.getAssayWellVolume());
  }

  private void copyActivityProperties(LabActivity fromActivity, LabActivity toActivity)
  {
    toActivity.setVolumeTransferredPerWellFromLibraryPlates(fromActivity.getVolumeTransferredPerWellFromLibraryPlates());
    toActivity.setMolarConcentration(fromActivity.getMolarConcentration());
    toActivity.setPerformedBy(fromActivity.getPerformedBy());
  }

}
