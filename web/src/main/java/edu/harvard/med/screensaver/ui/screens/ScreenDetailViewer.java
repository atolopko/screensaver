// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.iccbl.screensaver.IccblScreensaverConstants;
import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.NoSuchEntityException;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;
import edu.harvard.med.screensaver.model.RequiredPropertyException;
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.activities.ServiceActivity;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.AssayType;
import edu.harvard.med.screensaver.model.screens.BillingItem;
import edu.harvard.med.screensaver.model.screens.CellLine;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenAttachedFileType;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenStatus;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.screens.Species;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.TransfectionAgent;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserComparator;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.service.screens.ScreenGenerator;
import edu.harvard.med.screensaver.service.screens.ScreeningDuplicator;
import edu.harvard.med.screensaver.ui.activities.ActivityViewer;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.util.AttachedFiles;
import edu.harvard.med.screensaver.ui.arch.util.UICompositeSelectorBean;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.ui.arch.view.EditResult;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.cells.CellSearchResults;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestDetailViewer;
import edu.harvard.med.screensaver.util.NullSafeUtils;
import edu.harvard.med.screensaver.util.StringUtils;
import edu.harvard.med.screensaver.util.eutils.EutilsException;
import edu.harvard.med.screensaver.util.eutils.PublicationInfoProvider;

public class ScreenDetailViewer extends AbstractStudyDetailViewer<Screen>
{
  private static final int CHILD_ENTITY_TABLE_MAX_ROWS = 10;

  public static final String UNKNOWN_SPECIES = "<unspecified>";

  private static Logger log = Logger.getLogger(ScreenDetailViewer.class);

  private ScreenDAO _screenDao;
  private EntityViewPolicy _entityViewPolicy;
  private ScreenViewer _screenViewer;
  private ActivityViewer _activityViewer;
  private CherryPickRequestDetailViewer _cherryPickRequestDetailViewer;
  private PublicationInfoProvider _publicationInfoProvider;
  private ScreeningDuplicator _screeningDuplicator;
  private AttachedFiles _attachedFiles;

  private boolean _isAdminViewMode = false;
  private boolean _isPublishableProtocolDetailsCollapsed = true;
  private boolean _isBillingInformationCollapsed = true;
  private UISelectOneBean<FundingSupport> _newFundingSupport;
  private UISelectOneBean<ScreenStatus> _newStatusItemValue;
  private LocalDate _newStatusItemDate;

  private Publication _newPublication;
  private UploadedFile _uploadedPublicationAttachedFileContents;
  private BillingItem _newBillingItem;

  private UISelectOneEntityBean<AdministratorUser> _pinTransferApprovedBy;
  private LocalDate _pinTransferApprovalDate;
  private String _pinTransferApprovalComments;

  private UISelectOneBean<ScreenDataSharingLevel> _dataSharingLevel;

  private CellLine _newCellLine;
  protected SortedSet<CellLine> _cellLines;
  private UISelectOneEntityBean<CellLine> _cellLineMenu;
  
  private UISelectOneEntityBean<TransfectionAgent> _transfectionAgentMenu;
  private TransfectionAgent _newTransfectionAgent;
  private UICompositeSelectorBean<BigDecimal, MolarUnit> _perturbagenMolarConcentrationSelector;
  
  private ScreenDataSharingLevel _lastDataSharingLevel;
  private LabHead _lastLabHead;
  private ScreeningRoomUser _lastLeadScreener;
  private LocalDate _lastMinAllowedDataPrivacyExpirationDate;
  private LocalDate _lastMaxAllowedDataPrivacyExpirationDate;

  private AttachedFileType _publicationAttachedFileType;

  private UISelectOneBean<Species> _species;
  private UISelectOneBean<AssayType> _assayType;
  
  private ScreenGenerator _screenGenerator;
	
   private CellSearchResults _cellSearchResults;

  /**
   * @motivation for CGLIB2
   */
  protected ScreenDetailViewer()
  {
  }

  public ScreenDetailViewer(ScreenDetailViewer thisProxy,
                            ScreenViewer screenViewer,
                            GenericEntityDAO dao,
                            ScreenDAO screenDao,
                            UsersDAO usersDao,
                            EntityViewPolicy entityViewPolicy,
                            ActivityViewer activityViewer,
                            CherryPickRequestDetailViewer cherryPickRequestDetailViewer,
                            PublicationInfoProvider publicationInfoProvider,
                            ScreeningDuplicator screeningDuplicator,
                            AttachedFiles attachedFiles,
                            ScreenGenerator screenGenerator,
                            CellSearchResults cellSearchResults)
  {
    super(thisProxy,
          dao,
          EDIT_SCREEN,
          usersDao);
    _screenDao = screenDao;
    _entityViewPolicy = entityViewPolicy;
    _screenViewer = screenViewer;
    _activityViewer = activityViewer;
    _cherryPickRequestDetailViewer = cherryPickRequestDetailViewer;
    _publicationInfoProvider = publicationInfoProvider;
    _screeningDuplicator = screeningDuplicator;
    _attachedFiles = attachedFiles;
    _screenGenerator = screenGenerator;
    _cellSearchResults = cellSearchResults;
    getIsPanelCollapsedMap().put("screenDetail", false);
    getIsPanelCollapsedMap().put("cellsForScreen", false); // LINCS proj
  }

  @Override
  protected void initializeEntity(Screen screen)
  {
    super.initializeEntity(screen);
  }
  
  @Override
  protected void initializeViewer(final Screen screen)
  {
    super.initializeViewer(screen);
    //_isAdminViewMode = false; // maintain this setting when viewing a new screen
    //_isPublishableProtocolDetailsCollapsed = true; // maintain this setting when viewing a new screen
    //_isBillingInformationCollapsed = true; // maintain this setting when viewing a new screen
    _newFundingSupport = null;
    _newStatusItemValue = null;
    _newStatusItemDate = null;
    _newPublication = null;
    _cellLineMenu=null;
    _newCellLine=null;
    _transfectionAgentMenu=null;
    _newTransfectionAgent=null;
    _uploadedPublicationAttachedFileContents = null;
    _newBillingItem = null;
    _pinTransferApprovalDate = null;
    _pinTransferApprovedBy = null;
    _pinTransferApprovalComments = null;
    _dataSharingLevel = null;
    _species = null;
    _assayType = null;
    _lastDataSharingLevel = screen.getDataSharingLevel();
    _lastLabHead = screen.getLabHead();
    _lastLeadScreener = screen.getLeadScreener();
    initalizeAttachedFiles(screen);
    _lastMinAllowedDataPrivacyExpirationDate = screen.getMinAllowedDataPrivacyExpirationDate();
    _lastMaxAllowedDataPrivacyExpirationDate = screen.getMaxAllowedDataPrivacyExpirationDate();
    _perturbagenMolarConcentrationSelector = null;    
    
    
    _cellLines = Sets.newTreeSet(screen.getCellLines());
    
    //    
    //    
    //    if(isLINCS())
    //		_cellSearchResults.initialize(new InMemoryEntityDataModel<Cell, Integer, Cell>(
    //				new EntityDataFetcher<Cell, Integer>(Cell.class, getDao()) {
    //					@Override
    //					public void addDomainRestrictions(HqlBuilder hql) {
    //						DataFetcherUtil.addDomainRestrictions(hql, Cell.experimentalCellInformationSetPath.to("screen"), screen,
    //								getRootAlias());
    //					}
    //				}));

  }

  private void initalizeAttachedFiles(Screen screen)
  {
    SortedSet<AttachedFileType> attachedFileTypes = 
      Sets.<AttachedFileType>newTreeSet(Iterables.filter(getDao().findAllEntitiesOfType(ScreenAttachedFileType.class, true),
                                                         new Predicate<AttachedFileType>() { 
        public boolean apply(AttachedFileType aft) { return !!!aft.getValue().equals(Publication.PUBLICATION_ATTACHED_FILE_TYPE_VALUE); }
      }));
    _attachedFiles.initialize(screen, attachedFileTypes,
                              new Predicate<AttachedFile>() {
                                public boolean apply(AttachedFile af)
                                {
                                  return !!!af.getFileType().getValue().equals(Publication.PUBLICATION_ATTACHED_FILE_TYPE_VALUE);
                                }
                              });
    _publicationAttachedFileType = getDao().findEntityByProperty(ScreenAttachedFileType.class, "value", Publication.PUBLICATION_ATTACHED_FILE_TYPE_VALUE);
    if (_publicationAttachedFileType == null) {
      throw NoSuchEntityException.forProperty(ScreenAttachedFileType.class, "value", Publication.PUBLICATION_ATTACHED_FILE_TYPE_VALUE);
    }
  }
  
  public boolean isAdminViewMode()
  {
    return _isAdminViewMode;
  }

  public boolean isPublishableProtocolDetailsCollapsed()
  {
    return _isPublishableProtocolDetailsCollapsed;
  }

  public void setPublishableProtocolDetailsCollapsed(boolean isPublishableProtocolDetailsCollapsed)
  {
    _isPublishableProtocolDetailsCollapsed = isPublishableProtocolDetailsCollapsed;
  }

  public boolean isBillingInformationCollapsed()
  {
    return _isBillingInformationCollapsed;
  }

  public void setBillingInformationCollapsed(boolean isBillingInformationCollapsed)
  {
    _isBillingInformationCollapsed = isBillingInformationCollapsed;
  }

  /**
   * Determine if the current user can view the restricted screen fields.
   */
  public boolean isAllowedAccessToScreenDetails()
  {
    return _entityViewPolicy.isAllowedAccessToScreenDetails(getEntity())
        || _entityViewPolicy.isAllowedAccessToMutualScreenDetails(getEntity());
  }

  /**
   * Determine whether the current user can see the Status Items, Lab
   * Activities, and Cherry Pick Requests tables. These are considered more
   * private than the screen details (see
   * {@link #isAllowedAccessToScreenDetails()}).
   */
  public boolean isAllowedAccessToScreenActivity()
  {
    return _entityViewPolicy.isAllowedAccessToScreenActivity(getEntity());
  }

  public UISelectOneBean<FundingSupport> getNewFundingSupport()
  {
    if (_newFundingSupport == null) {
      Set<FundingSupport> candidateFundingSupports = Sets.newTreeSet(getDao().findAllEntitiesOfType(FundingSupport.class));
      candidateFundingSupports.removeAll(getEntity().getFundingSupports());
      List<FundingSupport> newCandidates = Lists.newArrayList();
      for(FundingSupport fs:candidateFundingSupports){
        if(!fs.isRetired()){
          newCandidates.add(fs);
        }
      }
      Collections.sort(newCandidates);
      _newFundingSupport = new UISelectOneEntityBean<FundingSupport>(newCandidates, null, true, getDao()) {
          @Override
        protected String getEmptyLabel()
        {
          return ScreensaverConstants.REQUIRED_VOCAB_FIELD_PROMPT;
        }
      };
    }
    return _newFundingSupport;
  }

  public UISelectOneBean<ScreenStatus> getNewStatusItemValue()
  {
    if (_newStatusItemValue == null) {
      _newStatusItemValue = new UISelectOneBean<ScreenStatus>(getEntity().getCandidateStatuses());
    }
    return _newStatusItemValue;
  }

  public LocalDate getNewStatusItemDate()
  {
    return _newStatusItemDate;
  }

  public void setNewStatusItemDate(LocalDate newStatusItemDate)
  {
    _newStatusItemDate = newStatusItemDate;
  }

  public DataModel getStatusItemsDataModel()
  {
    return new ListDataModel(new ArrayList<StatusItem>(getEntity().getStatusItems()));
  }
  
  public int getActivitiesCount()
  {
    return getEntity().getLabActivities().size() + 
        getEntity().getServiceActivities().size();
  }

  public Activity getLastActivity()
  {
    Activity lastActivity = null;
    if (!getEntity().getLabActivities().isEmpty()){
      lastActivity = getEntity().getLabActivities().last();
    }
    if (!getEntity().getServiceActivities().isEmpty()){
        Activity temp = getEntity().getServiceActivities().last();
        if (lastActivity == null ){
          lastActivity = temp;
        }
        else{
            if(lastActivity.getDateOfActivity().compareTo(temp.getDateOfActivity()) < 0){
                lastActivity = temp;
            }
        }
    }
    
    return lastActivity;
  }	

  public DataModel getCherryPickRequestsDataModel()
  {
    ArrayList<CherryPickRequest> cherryPickRequests = new ArrayList<CherryPickRequest>(getEntity().getCherryPickRequests());
    Collections.sort(cherryPickRequests,
                     new Comparator<CherryPickRequest>() {
                       public int compare(CherryPickRequest cpr1, CherryPickRequest cpr2)
      {
        return -1 * cpr1.getCherryPickRequestNumber().compareTo(cpr2.getCherryPickRequestNumber());
      }
                     });
    return new ListDataModel(new ArrayList<CherryPickRequest>(cherryPickRequests.subList(0, Math.min(CHILD_ENTITY_TABLE_MAX_ROWS, cherryPickRequests.size()))));
  }

  public DataModel getPublicationsDataModel()
  {
    ArrayList<Publication> publications = new ArrayList<Publication>(getEntity().getPublications());
    Collections.sort(publications,
                     new Comparator<Publication>() {
      public int compare(Publication p1, Publication p2)
      {
        return p1.getAuthors().compareTo(p2.getAuthors());
      }
    });
    return new ListDataModel(publications);
  }
  
  public CellSearchResults getCellSearchResults()
  {
  	return _cellSearchResults;
  }

  public Publication getNewPublication()
  {
    if (_newPublication == null) {
      _newPublication = new Publication();
    }
    return _newPublication;
  }

  public void setUploadedPublicationAttachedFileContents(UploadedFile uploadedFile)
  {
    _uploadedPublicationAttachedFileContents = uploadedFile;
  }

  public UploadedFile getUploadedPublicationAttachedFileContents()
  {
    return _uploadedPublicationAttachedFileContents;
  }

  public AttachedFiles getAttachedFiles()
  {
    return _attachedFiles;
  }

  public DataModel getFundingSupportsDataModel()
  {
    return new ListDataModel(new ArrayList<FundingSupport>(getEntity().getFundingSupports()));
  }

  public DataModel getBillingItemsDataModel()
  {
    ArrayList<BillingItem> billingItems = new ArrayList<BillingItem>();
    billingItems.addAll(getEntity().getBillingItems());
    Collections.sort(billingItems,
                     new Comparator<BillingItem>() {
      public int compare(BillingItem bi1, BillingItem bi2)
      {
        LocalDate one = bi1.getDateSentForBilling();
        LocalDate two = bi2.getDateSentForBilling();
        if(one != null && two != null )
        {
          return one.compareTo(two);
        }
        else if(one != null)
        {
          return -1;
        }
        else if(two != null)
        {
          return 1;
        }
        else 
        { // amount is guaranteed non-null, but still
          return bi1.getAmount().compareTo(bi2.getAmount());
        }
      }
    });
    return new ListDataModel(billingItems);
  }

  public BillingItem getNewBillingItem()
  {
    if (_newBillingItem == null) {
      _newBillingItem = new BillingItem();
    }
    return _newBillingItem;
  }

  public UISelectOneEntityBean<AdministratorUser> getPinTransferApprovedBy()
  {
    if (_pinTransferApprovedBy == null) {
      Set<AdministratorUser> candidateApprovers = 
        Sets.filter(ImmutableSortedSet.orderedBy(ScreensaverUserComparator.<AdministratorUser>getInstance()).addAll(getDao().findAllEntitiesOfType(AdministratorUser.class, true, ScreensaverUser.roles.castToSubtype(AdministratorUser.class))).build(),
                    new Predicate<AdministratorUser>() { public boolean apply(AdministratorUser u) { return u.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENS_ADMIN); } } );
      // note: we must reload 'performedBy', since it can otherwise be a proxy, which will not allow us to cast it to AdministratorUser
      AdministratorUser defaultSelection = getEntity().getPinTransferApprovalActivity() == null ? null : (AdministratorUser) getDao().reloadEntity(getEntity().getPinTransferApprovalActivity().getPerformedBy());
      _pinTransferApprovedBy = new UISelectOneEntityBean<AdministratorUser>(candidateApprovers,
        defaultSelection,
        true,
        getDao()) { @Override public String makeLabel(AdministratorUser a) { return a.getFullNameLastFirst(); }
      };
    }
    return _pinTransferApprovedBy;
  }

  public void setPinTransferApprovedBy(UISelectOneEntityBean<AdministratorUser> pinTransferApprovedBy)
  {
    _pinTransferApprovedBy = pinTransferApprovedBy;
  }

  public LocalDate getPinTransferApprovalDate()
  {
    if (_pinTransferApprovalDate == null && getEntity().getPinTransferApprovalActivity() != null) {
      _pinTransferApprovalDate = getEntity().getPinTransferApprovalActivity().getDateOfActivity();
    }
    return _pinTransferApprovalDate;
  }

  public void setPinTransferApprovalDate(LocalDate pinTransferApprovalDate)
  {
    _pinTransferApprovalDate = pinTransferApprovalDate;
  }

  /* JSF Application methods */

  public String getPinTransferApprovalComments()
  {
    if (_pinTransferApprovalComments == null && getEntity().getPinTransferApprovalActivity() != null) {
      _pinTransferApprovalComments = getEntity().getPinTransferApprovalActivity().getComments();
    }
    return _pinTransferApprovalComments;
  }

  public void setPinTransferApprovalComments(String pinTransferApprovalComments)
  {
    _pinTransferApprovalComments = pinTransferApprovalComments;
  }

  public UISelectOneBean<ScreenDataSharingLevel> getDataSharingLevel()
  {
    if (_dataSharingLevel == null) {
      _dataSharingLevel = new UISelectOneBean<ScreenDataSharingLevel>(Lists.newArrayList(ScreenDataSharingLevel.values()), getEntity().getDataSharingLevel(), false);
      _dataSharingLevel.addObserver(new Observer() {
        public void update(Observable arg0, Object dataSharingLevel)
        {
          getEntity().setDataSharingLevel((ScreenDataSharingLevel) dataSharingLevel);
        }
      });
    }
    return _dataSharingLevel;
  }
  
  public UISelectOneEntityBean<CellLine> getCellLineMenu()
  {
    if (_cellLineMenu == null) {
      SortedSet<CellLine> cellLines= new TreeSet<CellLine>();
      cellLines.addAll(getDao().findAllEntitiesOfType(CellLine.class));
      _cellLineMenu= new UISelectOneEntityBean<CellLine>(cellLines, /** getEntity().getCellLine(), true,**/ getDao()) ;
//      _cellLineMenu.addObserver(new Observer() {
//        public void update(Observable arg0, Object value)
//        {
//          getEntity().setCellLine((CellLine) value);
//        }
//      });

    }
    return _cellLineMenu;
  }
  
  @UICommand
  @Transactional
  public String addNewCellLine()
  {
    if (StringUtils.isEmpty(_newCellLine.getValue())) {
      showMessage("requiredValue", "cell line name");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (getDao().findEntityByProperty(CellLine.class,
                                      "value",
                                      _newCellLine.getValue()) != null) {
      showMessage("duplicateEntity", "cell line");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    getDao().persistEntity(_newCellLine);
    getDao().flush();

    _cellLineMenu = null;

    getCellLineMenu().setSelection(_newCellLine);

    _newCellLine = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }  

  public CellLine getNewCellLine()
  {
    if(_newCellLine == null)
    {
      _newCellLine = new CellLine();
    }
    return _newCellLine;
  }
  
  public UISelectOneEntityBean<CellLine> getNewCellLineToAdd()
  {
    if (_cellLineMenu == null) {
      SortedSet<CellLine> cellLines= new TreeSet<CellLine>();
      cellLines.addAll(getDao().findAllEntitiesOfType(CellLine.class));
      cellLines.removeAll(this._cellLines);
      
      _cellLineMenu= new UISelectOneEntityBean<CellLine>(cellLines, /** getEntity().getCellLine(), true,**/ getDao()) ;
    }
    return _cellLineMenu;
  }  
  
  @UICommand
  public String addCellLineToScreen()
  {
    if (getCellLineMenu().getSelection() != null) {
      CellLine cellLine = getCellLineMenu().getSelection();
      _cellLines.add(cellLine);
      _newCellLine = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deleteCellLineFromScreen()
  {
    CellLine cellLine = (CellLine) getRequestMap().get("element");
    _cellLines.remove(cellLine);
    _newCellLine = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public SortedSet<CellLine> getCellLines()
  {
    return _cellLines;
  }
  
  public DataModel getCellLinesDataModel()
  {
    return new ListDataModel(new ArrayList<CellLine>(_cellLines));
  }

  
  
  
  
  
  public UISelectOneEntityBean<TransfectionAgent> getTransfectionAgentMenu()
  {
    if (_transfectionAgentMenu == null) {
      SortedSet<TransfectionAgent> transfectionAgents= new TreeSet<TransfectionAgent>();
      transfectionAgents.addAll(getDao().findAllEntitiesOfType(TransfectionAgent.class));
      _transfectionAgentMenu= new UISelectOneEntityBean<TransfectionAgent>(transfectionAgents, getEntity().getTransfectionAgent(), true, getDao()) ;
            _transfectionAgentMenu.addObserver(new Observer() {
        public void update(Observable arg0, Object value)
        {
          getEntity().setTransfectionAgent((TransfectionAgent) value);
        }
      });

    }
    return _transfectionAgentMenu;
  }
  
  @UICommand
  @Transactional
  public String addNewTransfectionAgent()
  {
    if (StringUtils.isEmpty(_newTransfectionAgent.getValue())) {
      showMessage("requiredValue", "new transfection agent name");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (getDao().findEntityByProperty(TransfectionAgent.class,
                                      "value",
                                      _newTransfectionAgent.getValue()) != null) {
      showMessage("duplicateEntity", "transfection agent");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    getDao().persistEntity(_newTransfectionAgent);
    getDao().flush();

    // force reload of lab affiliation selections
    _transfectionAgentMenu = null;

    // set user's lab affiliation to new affiliation
    getTransfectionAgentMenu().setSelection(_newTransfectionAgent);

    _newTransfectionAgent = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public TransfectionAgent getNewTransfectionAgent()
  {
    if(_newTransfectionAgent == null)
    {
      _newTransfectionAgent = new TransfectionAgent();
    }
    return _newTransfectionAgent;
  }  

  public UISelectOneBean<Species> getSpecies()
  {
    if (_species == null) {
      Species[] speciesList = Species.values();
      if(getEntity().getScreenType() == ScreenType.RNAI ) 
      {
        speciesList = Species.getRNAiSpecies();
      }
      _species = new UISelectOneBean<Species>(Lists.newArrayList(speciesList), getEntity().getSpecies(), true)
      {
        @Override
        protected String getEmptyLabel()
        {
          return UNKNOWN_SPECIES;
        }
      };
     _species.addObserver(new Observer() {
        public void update(Observable arg0, Object species)
        {
          getEntity().setSpecies((Species) species);
        }
      });
    }
    return _species;
  }
  
  public UISelectOneBean<AssayType> getAssayType()
  {
    if (_assayType == null) {
      AssayType[] atList = AssayType.values();
      _assayType = new UISelectOneBean<AssayType>(Lists.newArrayList(atList), getEntity().getAssayType(), true)
      {
        @Override
        protected String getEmptyLabel()
        {
          return UNKNOWN_SPECIES;
        }
      };
      _assayType.addObserver(new Observer() {
        public void update(Observable arg0, Object at)
        {
          getEntity().setAssayType((AssayType) at);
        }
      });
    }
    return _assayType;
  }
  
  @Override
  public List<SelectItem> getProjectPhaseSelectItems()
  {
    List<SelectItem> selectItems = super.getProjectPhaseSelectItems();
    for (Iterator<SelectItem> iter = selectItems.iterator(); iter.hasNext();) {
      if (iter.next().getValue() == ProjectPhase.ANNOTATION) {
        iter.remove();
      }
    }
    return selectItems;
  }

  @UICommand
  public String toggleAdminViewMode()
  {
    setEditMode(false);
    _isAdminViewMode ^= true;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @Override
  protected void recordUpdateActivity()
  {
    if (_lastDataSharingLevel != getDataSharingLevel().getSelection()) {
      recordUpdateActivity("updated screen data sharing level from '" + NullSafeUtils.toString(_lastDataSharingLevel) + 
                           "' to '" + NullSafeUtils.toString(getDataSharingLevel().getSelection()) + "'");
    }
    if (!!!NullSafeUtils.nullSafeEquals(_lastLabHead, getEntity().getLabHead())) {
      String lastLabHead = NullSafeUtils.toString(_lastLabHead, ScreensaverUser.ToDisplayStringFunction);
      String newLabHead = NullSafeUtils.toString(getEntity().getLabHead(), ScreensaverUser.ToDisplayStringFunction);
      recordUpdateActivity("changed lab from '" + lastLabHead + "' to '" + newLabHead + "'");
    }
    if (!!!NullSafeUtils.nullSafeEquals(_lastLeadScreener, getEntity().getLeadScreener())) {
      String lastLeadScreener = NullSafeUtils.toString(_lastLeadScreener, ScreensaverUser.ToDisplayStringFunction);
      String newLeadScreener = NullSafeUtils.toString(getEntity().getLeadScreener(), ScreensaverUser.ToDisplayStringFunction);
      recordUpdateActivity("changed lead screener from '" + lastLeadScreener + "' to '" + newLeadScreener + "'");
    }
    
    if (!!!NullSafeUtils.nullSafeEquals(_lastMinAllowedDataPrivacyExpirationDate, getEntity().getMinAllowedDataPrivacyExpirationDate())) {
      String lastDate = NullSafeUtils.toString(_lastMinAllowedDataPrivacyExpirationDate);
      String newDate = NullSafeUtils.toString(getEntity().getMinAllowedDataPrivacyExpirationDate());
      recordUpdateActivity("changed Earliest Allowed Data Privacy Expiration Date'" + lastDate + "' to '" + newDate + "'");
    }
    
    if (!!!NullSafeUtils.nullSafeEquals(_lastMaxAllowedDataPrivacyExpirationDate, getEntity().getMaxAllowedDataPrivacyExpirationDate())) {
      String lastDate = NullSafeUtils.toString(_lastMaxAllowedDataPrivacyExpirationDate);
      String newDate = NullSafeUtils.toString(getEntity().getMaxAllowedDataPrivacyExpirationDate());
      recordUpdateActivity("changed Latest Allowed Data Privacy Expiration Date'" + lastDate + "' to '" + newDate + "'");
    }
    
    super.recordUpdateActivity();
  }

  @Override
  protected void updateEntityProperties(Screen screen)
  {
    super.updateEntityProperties(screen);
    screen.setLabHead(getLabName().getSelection());
    screen.setLeadScreener(getLeadScreener().getSelection());
    Set<ScreeningRoomUser> extantCollaborators = Sets.newHashSet(screen.getCollaborators());
    for (ScreeningRoomUser collaborator : Sets.difference(_collaborators, extantCollaborators)) {
      screen.addCollaborator(getDao().reloadEntity(collaborator));
    }
    for (ScreeningRoomUser collaborator : Sets.difference(extantCollaborators, _collaborators)) {
      screen.removeCollaborator(getDao().reloadEntity(collaborator));
    }

    Set<CellLine> extantCellLines= Sets.newHashSet(screen.getCellLines());
    for (CellLine cellLine : Sets.difference(_cellLines, extantCellLines )) {
      screen.addCellLine(cellLine);
    }
    for (CellLine cellLine : Sets.difference(extantCellLines, _cellLines)) {
      screen.removeCellLine(cellLine);
    }    
    
    if (getPinTransferApprovedBy().getSelection() != null && screen.getPinTransferApprovalActivity() == null) {
      screen.setPinTransferApproved((AdministratorUser) getDao().reloadEntity(getScreensaverUser(), false, ScreensaverUser.activitiesPerformed),
                                    getPinTransferApprovedBy().getSelection(),
                                    getPinTransferApprovalDate(),
                                    getPinTransferApprovalComments());
    }
    else if (!StringUtils.isEmpty(getPinTransferApprovalComments()) && screen.getPinTransferApprovalActivity() != null) {
      screen.getPinTransferApprovalActivity().setComments(getPinTransferApprovalComments());
    }

    if(getPerturbagenMolarConcentrationSelector().isEmpty())
    {
      screen.setPerturbagenMolarConcentration(null);
    } else {
        screen.setPerturbagenMolarConcentration(MolarConcentration.makeConcentration(getPerturbagenMolarConcentrationSelector().getValue().toString(),
                                             getPerturbagenMolarConcentrationSelector().getSelectorBean().getSelection()));
    }

  }
  
  @Override
  protected boolean validateEntity(Screen screen)
  {
    if (!_screenDao.isScreenFacilityIdUnique(screen)) {
      showMessage("duplicateEntity", "Screen ID");
      return false;
    }

    LocalDate max = screen.getMaxAllowedDataPrivacyExpirationDate();
    LocalDate min = screen.getMinAllowedDataPrivacyExpirationDate();
    
    if (max != null && min != null) {
      if(max.compareTo(min) < 0) {
        showMessage("screens.dataPrivacyExpirationDateOrderError", min, max);
        return false;
      }
    }

    boolean valid = true;
    if(!getPerturbagenMolarConcentrationSelector().isEmpty()) {
      try {
       MolarConcentration.makeConcentration(getPerturbagenMolarConcentrationSelector().getValue().toString(),
                                             getPerturbagenMolarConcentrationSelector().getSelectorBean().getSelection());
      }
      catch (ArithmeticException e) {
        showFieldInputError("Molar Concentration: number format error: allowed range (>= 1.0 pM, < 10.0 M), in 1 pM increments",  e.getLocalizedMessage());
        valid = false;
      }
      catch (Exception e) {
        showFieldInputError("Molar Concentration", StringUtils.isEmpty(e.getMessage())? e.getClass().getName() : e.getMessage() ); 
        valid = false;
      }
    }

    // At ICCB-L, the screen DSL 2 is not an option, so we hide it at the UI level; we maintain it in our model for consistency with the SM screen DSLs
    if (getApplicationProperties().isFacility(IccblScreensaverConstants.FACILITY_KEY)) {
      if (screen.getScreenType() == ScreenType.RNAI) {
        if (screen.getDataSharingLevel() == ScreenDataSharingLevel.MUTUAL_POSITIVES) {
          showMessage("screens.illegalDataSharingLevel", screen.getDataSharingLevel(), "for RNAi screens");
          valid = false;
        }
      }
    }

    return valid && super.validateEntity(screen);
  }

  @UICommand
  public String addRelatedScreen()
  {
    if (getEntity().getProjectId() == null) {
      showMessage("screens.projectIdRequiredForRelatedScreens");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    Screen relatedScreen = _screenGenerator.createRelatedScreen((AdministratorUser) getScreensaverUser(),
                                                                getEntity(),
                                                                null);

    return getThisProxy().editNewEntity(relatedScreen);
  }

  @UICommand
  public String addStatusItem()
  {
    if (getNewStatusItemValue().getSelection() == null) {
      showMessage("requiredValue", "Status Item Value");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (getNewStatusItemDate() == null) {
      showMessage("requiredValue", "Status Item Date");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    try {
      getEntity().createStatusItem(getNewStatusItemDate(),
                                   getNewStatusItemValue().getSelection());
    }
    catch (BusinessRuleViolationException e) {
      showMessage("businessError", e.getMessage());
    }
    _newStatusItemValue = null; // reset
    _newStatusItemDate = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deleteStatusItem()
  {
    getEntity().getStatusItems().remove(getRequestMap().get("element"));
    _newStatusItemValue = null; // reset
    _newStatusItemDate = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String addPublication()
  {
    try {
      Publication publication = getEntity().addCopyOfPublication(_newPublication);

      if (_uploadedPublicationAttachedFileContents != null) {
        String filename;
        InputStream contentsInputStream;
        filename = _uploadedPublicationAttachedFileContents.getName();
        try {
          contentsInputStream = _uploadedPublicationAttachedFileContents.getInputStream();
        }
        catch (IOException e) {
          reportApplicationError(e.getMessage());
          return REDISPLAY_PAGE_ACTION_RESULT;
        }
        _uploadedPublicationAttachedFileContents = null;
        ScreenAttachedFileType publicationAttachedFileType = getDao().findEntityByProperty(ScreenAttachedFileType.class, "value", Publication.PUBLICATION_ATTACHED_FILE_TYPE_VALUE);
        if (publicationAttachedFileType == null) {
          reportApplicationError("'publication' attached file type does not exist");
          return REDISPLAY_PAGE_ACTION_RESULT;
        }
        publication.setAttachedFile(getEntity().createAttachedFile(filename, publicationAttachedFileType, null, contentsInputStream));
      }
    }
    catch (IOException e) {
      reportApplicationError("could not attach the file contents");
    }

    _newPublication = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String lookupPublicationByPubMedId() throws EutilsException
  {
    Integer pubmedId = _newPublication.getPubmedId();
    _newPublication =
      _publicationInfoProvider.getPublicationForPubmedId(pubmedId);
    if (_newPublication == null) {
      reportApplicationError("Publication for PubMed ID " + pubmedId + " was not found");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deletePublication()
  {
    Publication publication = (Publication) getRequestMap().get("element");
    if (publication != null) {
      getEntity().getPublications().remove(publication);
      _newPublication = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  @Transactional
  public String downloadPublicationAttachedFile() throws IOException, SQLException
  {
    Publication publication = (Publication) getRequestMap().get("element");
    if (publication != null) {
      return _attachedFiles.doDownloadAttachedFile(publication.getAttachedFile(), getFacesContext(), getDao());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String addFundingSupport()
  {
    if (_newFundingSupport != null && _newFundingSupport.getSelection() != null) {
      getEntity().addFundingSupport(_newFundingSupport.getSelection());
      _newFundingSupport = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deleteFundingSupport()
  {
    getEntity().getFundingSupports().remove(getRequestMap().get("element"));
    _newFundingSupport = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  @Transactional
  public String addLibraryScreening()
  {
    if (!!!getScreensaverUser().isUserInRole(ScreensaverUserRole.SCREENS_ADMIN)) {
      throw new OperationRestrictedException("add Library Screening");
    }
    getDao().need(getEntity(), Screen.assayPlates);
    Screening screening = _screeningDuplicator.addLibraryScreening(getEntity(),
                                                                   (AdministratorUser) getScreensaverUser());
    getDao().clear(); // detach new Activity, as it should only be persisted if user invokes "save" command 
    return _activityViewer.editNewEntity(screening);
  }

  @UICommand
  public String copyLabActivity()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String copyCherryPickRequest()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  @Transactional
  public String addCherryPickRequest()
  {
    if (!!!getScreensaverUser().isUserInRole(ScreensaverUserRole.CHERRY_PICK_REQUESTS_ADMIN)) {
      throw new OperationRestrictedException("add Cherry Pick Request");
    }
    Screen screen = getDao().reloadEntity(getEntity(), true, Screen.cherryPickRequests);
    getDao().needReadOnly(screen, Screen.labActivities);
    getDao().needReadOnly(screen, Screen.labHead);
    getDao().needReadOnly(screen, Screen.leadScreener);
    getDao().needReadOnly(screen, Screen.collaborators);
    CherryPickRequest cpr = screen.createCherryPickRequest((AdministratorUser) getScreensaverUser());
    getDao().clear(); // detach new entity, as it should only be persisted if user invokes "save" command 
    return _cherryPickRequestDetailViewer.editNewEntity(cpr);
  }

  @UICommand
  public String addBillingItem()
  {
    if (_newBillingItem != null) {
      try {
        getEntity().addCopyOfBillingItem(_newBillingItem);
      }
      catch (RequiredPropertyException e) {
        showFieldInputError("", e.getMessage());
      }
      _newBillingItem = null; // reset
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deleteBillingItem()
  {
    getEntity().getBillingItems().remove(getRequestMap().get("element"));
    _newBillingItem = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @Override
  public boolean isDeleteSupported()
  {
    return getApplicationProperties().isFeatureEnabled("delete_screen");
  }
  
  @UICommand
  @Transactional
  public String delete()
  {
    if (getEntity().isDataLoaded()) {
      showMessage("cannotDeleteEntityInUse", "Screen " + getEntity().getFacilityId());
    }
    else {
      _screenDao.deleteStudy(getEntity());
      showMessage("deletedEntity", "Screen " + getEntity().getFacilityId());
    }
    return VIEW_MAIN;
  }

  @Override
  protected String postEditAction(EditResult editResult)
  {
    switch (editResult) {
      case CANCEL_EDIT:
      case SAVE_EDIT:
        return _screenViewer.reload();
      case CANCEL_NEW:
        return VIEW_MAIN;
      case SAVE_NEW:
        return _screenViewer.viewEntity(getEntity()); // note: can't call reload() since parent viewer is not yet configured with our new screen
      default:
        return null;
    }
  }
  
  public DataModel getAssociatedScreensDataModel()
  {
    List<Screen> screens = _screenDao.findRelatedScreens(getEntity());
    screens.remove(getEntity());
    return new ListDataModel(screens);
  }
  
  public UICompositeSelectorBean<BigDecimal, MolarUnit> getPerturbagenMolarConcentrationSelector()
  {
    if( _perturbagenMolarConcentrationSelector == null )
    {
      MolarUnit defaultUnit = MolarUnit.MICROMOLAR;      
      if(getEntity().getScreenType() == ScreenType.RNAI) {
        defaultUnit = MolarUnit.NANOMOLAR;
      }
      MolarConcentration c = getEntity().getPerturbagenMolarConcentration();
       _perturbagenMolarConcentrationSelector = new UICompositeSelectorBean<BigDecimal, MolarUnit>(
         c == null ? null  : c.getDisplayValue(), c == null ? defaultUnit : c.getUnits(), MolarUnit.DISPLAY_VALUES);
    }
    return _perturbagenMolarConcentrationSelector;
  }  

}
