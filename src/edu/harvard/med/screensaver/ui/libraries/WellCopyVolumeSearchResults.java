// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.searchresults.FixedDecimalColumn;
import edu.harvard.med.screensaver.ui.searchresults.IntegerColumn;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.searchresults.SearchResultsWithRowDetail;
import edu.harvard.med.screensaver.ui.searchresults.TextColumn;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;

public class WellCopyVolumeSearchResults extends SearchResultsWithRowDetail<WellCopyVolume,SearchResults<WellVolumeAdjustment>>
{
  // static members

  private static final List<Integer[]> COMPOUND_SORTS = new ArrayList<Integer[]>();
  static {
    COMPOUND_SORTS.add(new Integer[] {0, 1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {2, 1, 3});
    COMPOUND_SORTS.add(new Integer[] {4, 1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {5, 1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {6, 1, 2, 3});
    COMPOUND_SORTS.add(new Integer[] {7, 1, 2, 3});
  }
  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.LIBRARIES_ADMIN;

  private static Logger log = Logger.getLogger(WellCopyVolumeSearchResults.class);


  // instance data members

  private GenericEntityDAO _dao;
  private LibraryViewer _libraryViewer;
  private WellViewer _wellViewer;
  private WellVolumeSearchResults _wellVolumeSearchResults;

  private ArrayList<TableColumn<WellCopyVolume,?>> _columns;
  private Map<WellCopyVolume,BigDecimal> _newRemainingVolumes;
  private String _wellVolumeAdjustmentActivityComments;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected WellCopyVolumeSearchResults()
  {
  }

  public WellCopyVolumeSearchResults(GenericEntityDAO dao,
                                     LibraryViewer libraryViewer,
                                     WellViewer wellViewer,
                                     WellVolumeSearchResults wellVolumeSearchResults,
                                     WellVolumeAdjustmentSearchResults rowDetail)
  {
    _dao = dao;
    _wellVolumeSearchResults = wellVolumeSearchResults;
    _libraryViewer = libraryViewer;
    _wellViewer = wellViewer;
    setRowDetail(rowDetail);
  }

  // public methods

  @SuppressWarnings("unchecked")
  @Override
  public void setContents(Collection<? extends WellCopyVolume> wellCopyVolumes)
  {
    super.setContents(wellCopyVolumes);

    MultiMap wellKey2WellCopyVolumes = new MultiValueMap();
    for (WellCopyVolume wellCopyVolume : wellCopyVolumes) {
      wellKey2WellCopyVolumes.put(wellCopyVolume.getWell().getWellKey(),
                                  wellCopyVolume);
    }

    List<WellVolume> wellVolumes = new ArrayList<WellVolume>();
    for (Iterator iter = wellKey2WellCopyVolumes.keySet().iterator(); iter.hasNext(); ) {
      List<WellCopyVolume> wellCopyVolumesForWellKey = (List<WellCopyVolume>) wellKey2WellCopyVolumes.get(iter.next());
      wellVolumes.add(new WellVolume(wellCopyVolumesForWellKey.get(0).getWell(),
                                     wellCopyVolumesForWellKey));
    }
    _wellVolumeSearchResults.setContents(wellVolumes);
  }

  public WellVolumeSearchResults getWellVolumeSearchResults()
  {
    return _wellVolumeSearchResults;
  }

  @Override
  protected ScreensaverUserRole getEditableAdminRole()
  {
    return EDITING_ROLE;
  }

  @Override
  protected List<TableColumn<WellCopyVolume,?>> getColumns()
  {
    if (_columns == null) {
      _columns = new ArrayList<TableColumn<WellCopyVolume,?>>();
      _columns.add(new TextColumn<WellCopyVolume>("Library", "The library containing the well") {
        @Override
        public String getCellValue(WellCopyVolume wellVolume) { return wellVolume.getWell().getLibrary().getLibraryName(); }

        @Override
        public boolean isCommandLink() { return true; }

        @Override
        public Object cellAction(WellCopyVolume wellVolume) { return _libraryViewer.viewLibrary(wellVolume.getWell().getLibrary()); }
      });
      _columns.add(new IntegerColumn<WellCopyVolume>("Plate", "The number of the plate the well is located on") {
        @Override
        public Integer getCellValue(WellCopyVolume wellVolume) { return wellVolume.getWell().getPlateNumber(); }
      });
      _columns.add(new TextColumn<WellCopyVolume>("Well", "The plate coordinates of the well") {
        @Override
        public String getCellValue(WellCopyVolume wellVolume) { return wellVolume.getWell().getWellName(); }

        @Override
        public boolean isCommandLink() { return true; }

        @Override
        public Object cellAction(WellCopyVolume wellVolume) { return _wellViewer.viewWell(wellVolume.getWell()); }
      });
      _columns.add(new TextColumn<WellCopyVolume>("Copy", "The name of the library plate copy") {
        @Override
        public String getCellValue(WellCopyVolume wellVolume) { return wellVolume.getCopy().getName(); }

        // TODO
//        @Override
//        public boolean isCommandLink() { return true; }
//
//        @Override
//        public Object cellAction(WellCopyVolume wellVolume) { return _libraryViewer.viewLibraryCopyVolumes(wellVolume.getWell(), WellCopyVolumeSearchResults.this); }
      });
      _columns.add(new FixedDecimalColumn<WellCopyVolume>("Initial Volume", "The initial volume of this well copy") {
        @Override
        public BigDecimal getCellValue(WellCopyVolume wellVolume) { return wellVolume.getInitialMicroliterVolume(); }
      });
      _columns.add(new FixedDecimalColumn<WellCopyVolume>("Consumed Volume", "The volume already used from this well copy") {
        @Override
        public BigDecimal getCellValue(WellCopyVolume wellVolume) { return wellVolume.getConsumedMicroliterVolume(); }
      });
      _columns.add(new FixedDecimalColumn<WellCopyVolume>("Remaining Volume", "The remaining volume of this well copy") {
        @Override
        public BigDecimal getCellValue(WellCopyVolume wellVolume) { return wellVolume.getRemainingMicroliterVolume(); }
      });
      _columns.add(new IntegerColumn<WellCopyVolume>("Withdrawals/Adjustments", "The number of withdrawals and administrative adjustment smade from this well copy") {
        @Override
        public Integer getCellValue(WellCopyVolume wellVolume) { return wellVolume.getWellVolumeAdjustments().size(); }

        @Override
        public boolean isVisible() { return !isEditMode(); }

        @Override
        public boolean isCommandLink() { return getRowData().getWellVolumeAdjustments().size() > 0; }

        @Override
        public Object cellAction(WellCopyVolume entity)
        {
          return showRowDetail();
        }
      });
      _columns.add(new FixedDecimalColumn<WellCopyVolume>("New Remaining Volume", "Enter new remaining volume") {

        @Override
        public BigDecimal getCellValue(WellCopyVolume wellVolume) { return _newRemainingVolumes.get(wellVolume); }

        @Override
        public void setCellValue(WellCopyVolume wellVolume, Object value)
        {
          _newRemainingVolumes.put(wellVolume, (BigDecimal) value);
        }

        @Override
        public boolean isEditable() { return true; }

        @Override
        public boolean isVisible() { return isEditMode(); }
      });
    }
    return _columns;
  }

  @Override
  protected List<Integer[]> getCompoundSorts()
  {
    return COMPOUND_SORTS;
  }

  @Override
  protected void makeRowDetail(WellCopyVolume wcv)
  {
    List<WellVolumeAdjustment> wvas = new ArrayList<WellVolumeAdjustment>(wcv.getWellVolumeAdjustments().size());
    for (WellVolumeAdjustment wva : wcv.getWellVolumeAdjustments()) {
      WellVolumeAdjustment wva2 = _dao.reloadEntity(wva,
                                                    true,
                                                    "well",
                                                    "copy",
                                                    "labCherryPick.wellVolumeAdjustments",
                                                    "labCherryPick.cherryPickRequest",
                                                    "labCherryPick.assayPlate.cherryPickLiquidTransfer",
                                                    "wellVolumeCorrectionActivity.performedBy");
      wvas.add(wva2);
    }
    getRowDetail().setContents(wvas);
  }

  public String getWellVolumeAdjustmentActivityComments()
  {
    return _wellVolumeAdjustmentActivityComments;
  }

  public void setWellVolumeAdjustmentActivityComments(String wellVolumeAdjustmentActivityComments)
  {
    _wellVolumeAdjustmentActivityComments = wellVolumeAdjustmentActivityComments;
  }

  @Override
  public void doEdit()
  {
    _newRemainingVolumes = new HashMap<WellCopyVolume,BigDecimal>();
    _wellVolumeAdjustmentActivityComments = null;
  }

  @Override
  public void doSave()
  {
    final ScreensaverUser screensaverUser = getCurrentScreensaverUser().getScreensaverUser();
    if (!(screensaverUser instanceof AdministratorUser) || !((AdministratorUser) screensaverUser).isUserInRole(ScreensaverUserRole.LIBRARIES_ADMIN)) {
      throw new BusinessRuleViolationException("only libraries administrators can edit well volumes");
    }
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        if (_newRemainingVolumes.size() > 0) {
          AdministratorUser administratorUser = (AdministratorUser) _dao.reloadEntity(screensaverUser);
          WellVolumeCorrectionActivity wellVolumeCorrectionActivity =
            new WellVolumeCorrectionActivity(administratorUser, new Date());
          wellVolumeCorrectionActivity.setComments(getWellVolumeAdjustmentActivityComments());
          // TODO
          //wellVolumeCorrectionActivity.setApprovedBy();
          for (Map.Entry<WellCopyVolume,BigDecimal> entry : _newRemainingVolumes.entrySet()) {
            WellCopyVolume wellCopyVolume = entry.getKey();
            BigDecimal newRemainingVolume = entry.getValue();
            WellVolumeAdjustment wellVolumeAdjustment =
              wellVolumeCorrectionActivity.createWellVolumeAdjustment(
                  wellCopyVolume.getCopy(),
                                       wellCopyVolume.getWell(),
                  newRemainingVolume.subtract(wellCopyVolume.getRemainingMicroliterVolume()));
            wellCopyVolume.addWellVolumeAdjustment(wellVolumeAdjustment);
          }
          _dao.saveOrUpdateEntity(wellVolumeCorrectionActivity);
        }
      }
    });
    if (_newRemainingVolumes.size() > 0) {
      showMessage("libraries.updatedWellVolumes", new Integer(_newRemainingVolumes.size()));
    }
    else {
      showMessage("libraries.updatedNoWellVolumes");
    }
  }


  // private methods

}

