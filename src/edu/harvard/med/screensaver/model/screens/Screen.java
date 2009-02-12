// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.RequiredPropertyException;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.CompoundCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;


/**
 * A Hibernate entity bean representing a screen.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
public class Screen extends Study
{

  // private static data

  private static final Logger log = Logger.getLogger(Screen.class);
  private static final long serialVersionUID = 0L;


  // private instance data

  // study (provides annotation of library contents)

  //private Integer _studyId;
  private Integer _screenId;
  private Integer _version;
  private String _title;
  private ScreeningRoomUser _leadScreener; // should rename
  private LabHead _labHead;
  private SortedSet<ScreeningRoomUser> _collaborators = new TreeSet<ScreeningRoomUser>();
  private Set<Publication> _publications = new HashSet<Publication>();
  private String _url;
  private String _summary;
  private String _comments;
  private SortedSet<AnnotationType> _annotationTypes = new TreeSet<AnnotationType>();
  private Set<Reagent> _reagents = new HashSet<Reagent>();
  private StudyType _studyType;
  private boolean _isShareable = true;
  private boolean _isDownloadable = true;

  // generic screen

  private Integer _screenNumber;
  private ScreenType _screenType;
  private Set<AttachedFile> _attachedFiles = new HashSet<AttachedFile>();
  private SortedSet<String> _keywords = new TreeSet<String>();
  private String _publishableProtocol;
  private ScreenResult _screenResult;

  // iccb screen

  private SortedSet<StatusItem> _statusItems = new TreeSet<StatusItem>();
  private SortedSet<LabActivity> _labActivities = new TreeSet<LabActivity>();
  private LocalDate _dataMeetingScheduled;
  private LocalDate _dataMeetingComplete;
  private BillingInformation _billingInformation = new BillingInformation(this, false);
  private List<BillingItem> _billingItems = new ArrayList<BillingItem>();
  private Set<FundingSupport> _fundingSupports = new HashSet<FundingSupport>();
  private LocalDate _dateOfApplication;
  private Set<AbaseTestset> _abaseTestsets = new HashSet<AbaseTestset>();
  private String _abaseStudyId;
  private String _abaseProtocolId;
  private String _publishableProtocolComments;
  private LocalDate _publishableProtocolDateEntered;
  private String _publishableProtocolEnteredBy;
  private Set<CherryPickRequest> _cherryPickRequests = new HashSet<CherryPickRequest>();


  // public constructors


  /**
   * Construct an uninitialized <code>Screen</code>.
   *
   * @motivation for new Screen creation via user interface, where even required
   *             fields are allowed to be uninitialized, initially
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  public Screen() {}

  /**
   * Construct an initialized <code>Screen</code>.
   * @param leadScreener the lead screener
   * @param labHead the lab head
   * @param screenNumber the screen number
   * @param screenType the screen type
   * @param title the title
   */
  public Screen(
    ScreeningRoomUser leadScreener,
    LabHead labHead,
    Integer screenNumber,
    ScreenType screenType,
    String title)
  {
    this(
      leadScreener,
      labHead,
      screenNumber,
      screenType,
      StudyType.IN_VITRO,
      title,
      null,
      null,
      null,
      null,
      null,
      null);
  }

  /**
   * Construct an initialized <code>Screen</code>.
   * @param leadScreener the lead screener
   * @param labHead the lab head
   * @param screenNumber the screen number
   * @param screenType the screen type
   * @param studyType the study type
   * @param title the title
   */
  public Screen(
    ScreeningRoomUser leadScreener,
    LabHead labHead,
    Integer screenNumber,
    ScreenType screenType,
    StudyType studyType,
    String title)
  {
    this(
      leadScreener,
      labHead,
      screenNumber,
      screenType,
      studyType,
      title,
      null,
      null,
      null,
      null,
      null,
      null);
  }

  /**
   * Construct an initialized <code>Screen</code>.
   * @param leadScreener the lead screener
   * @param labHead the lab head
   * @param screenNumber the screen number
   * @param screenType the screen type
   * @param studyType the study type
   * @param title the title
   * @param dataMeetingScheduled the date the data meeting was scheduled for
   * @param dataMeetingComplete the date the data meeting took place
   * @param summary the summary
   * @param comments the comments
   * @param abaseStudyId the abase study id
   * @param abaseProtocolId the abase protocol id
   */
  public Screen(
    ScreeningRoomUser leadScreener,
    LabHead labHead,
    Integer screenNumber,
    ScreenType screenType,
    StudyType studyType,
    String title,
    LocalDate dataMeetingScheduled,
    LocalDate dataMeetingComplete,
    String summary,
    String comments,
    String abaseStudyId,
    String abaseProtocolId)
  {
    if (leadScreener == null) {
      throw new NullPointerException();
    }
    _leadScreener = leadScreener;
    _labHead = labHead;
    _screenNumber = screenNumber;
    _screenType = screenType;
    _title = title;
    _leadScreener.getScreensLed().add(this);
    if (_labHead != null) {
      _labHead.getScreensHeaded().add(this);
    }
    _studyType = studyType;
    _dataMeetingScheduled = dataMeetingScheduled;
    _dataMeetingComplete = dataMeetingComplete;
    _summary = summary;
    _comments = comments;
    _abaseStudyId = abaseStudyId;
    _abaseProtocolId = abaseProtocolId;
  }


  // public instance methods

  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    // TODO: HACK, until we have the real Study->Screen->IccbScreen hierarchy
    if (isStudyOnly()) {
      return visitor.visit((Study) this);
    }
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getScreenId();
  }

  /**
   * Get the id for the screen.
   * @return the id for the screen
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="screen_id_seq",
    strategy="sequence",
    parameters = { @org.hibernate.annotations.Parameter(name="sequence", value="screen_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="screen_id_seq")
  public Integer getScreenId()
  {
    return _screenId;
  }

  /**
   * Get the lead screener.
   * @return the lead screener
   */
  @ManyToOne(fetch=FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="leadScreenerId", nullable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_screen_to_lead_screener")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE
  })
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(inverseProperty="screensLed")
  public ScreeningRoomUser getLeadScreener()
  {
    return _leadScreener;
  }

  /**
   * Set the lead screener.
   * @param leadScreener the new lead screener
   */
  public void setLeadScreener(ScreeningRoomUser leadScreener)
  {
    if (isHibernateCaller()) {
      _leadScreener = leadScreener;
      return;
    }
    if (leadScreener == null) {
      throw new NullPointerException();
    }
    if (_leadScreener != null) {
      _leadScreener.getScreensLed().remove(this);
    }
    _leadScreener = leadScreener;
    _leadScreener.getScreensLed().add(this);
  }

  /**
   * Get the lab head.
   * @return the lab head
   */
  @ManyToOne(fetch=FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="labHeadId")
  @org.hibernate.annotations.ForeignKey(name="fk_screen_to_lab_head")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(inverseProperty="screensHeaded")
  public LabHead getLabHead()
  {
    return _labHead;
  }

  /**
   * Set the lab head.
   * @param labHead the new lab head
   */
  public void setLabHead(LabHead labHead)
  {
    if (isHibernateCaller()) {
      _labHead = labHead;
      return;
    }
    if ((labHead == null && _labHead == null) ||
      (labHead != null && labHead.equals(_labHead))) {
      return;
    }
    if (_labHead != null) {
      _labHead.getScreensHeaded().remove(this);
    }
    _labHead = labHead;
    if (_labHead != null) {
      _labHead.getScreensHeaded().add(this);
    }
  }

  /**
   * Get the set of collaborators.
   * @return the set of collaborators
   */
  @ManyToMany(fetch=FetchType.LAZY)
  @JoinTable(
    name="collaboratorLink",
    joinColumns=@JoinColumn(name="screenId"),
    inverseJoinColumns=@JoinColumn(name="collaboratorId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_collaborator_link_to_screen")
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @edu.harvard.med.screensaver.model.annotations.ManyToMany(inverseProperty="screensCollaborated")
  @Sort(type=SortType.NATURAL)
  public SortedSet<ScreeningRoomUser> getCollaborators()
  {
    return _collaborators;
  }

  /**
   * Get a sorted list of the collaborators.
   * @return a sorted list of the collaborators
   * @motivation JSF EL binding
   */
  @Transient
  public List<ScreeningRoomUser> getCollaboratorsList()
  {
    List<ScreeningRoomUser> collaboratorsList = new ArrayList<ScreeningRoomUser>(_collaborators);
    Collections.<ScreeningRoomUser>sort(collaboratorsList, ScreensaverUserComparator.getInstance());
    return collaboratorsList;
  }

  /**
   * Get a comma-delimited list of the full names of the Screen's collaborators, first name
   * first.
   * @return a comma-delimited list of the full names of the Screen's collaborators
   */
  @Transient
  public String getCollaboratorsString()
  {
    StringBuilder collaborators = new StringBuilder();
    boolean first = true;
    for (ScreeningRoomUser collaborator : getCollaborators()) {
      if (first) {
        first = false;
      }
      else {
        collaborators.append(", ");
      }
      collaborators.append(collaborator.getFullNameFirstLast());
    }
    return collaborators.toString();
  }

  /**
   * Replace the current set of collaborators with those screening room users in the provided
   * list of collaborators
   * @param collaborators a list of the new set of collaborators
   * @motivation JSF EL binding
   */
  public void setCollaboratorsList(List<ScreeningRoomUser> collaborators)
  {
    List<ScreeningRoomUser> collaboratorsToRemove = new ArrayList<ScreeningRoomUser>(_collaborators);
    for (ScreeningRoomUser collaborator : collaboratorsToRemove) {
      removeCollaborator(collaborator);
    }
    for (ScreeningRoomUser collaborator : collaborators) {
      addCollaborator(collaborator);
    }
  }

  /**
   * Add the collaborator.
   * @param collaborator the collaborator to add
   * @return true iff the screen did not already have the collaborator
   */
  public boolean addCollaborator(ScreeningRoomUser collaborator)
  {
    if (_collaborators.add(collaborator)) {
      return collaborator.getScreensCollaborated().add(this);
    }
    return false;
  }

  /**
   * Remove the collaborator.
   * @param collaborator the collaborator to remove
   * @return true iff the screen previously had the collaborator
   */
  public boolean removeCollaborator(ScreeningRoomUser collaborator)
  {
    if (_collaborators.remove(collaborator)) {
      return collaborator.getScreensCollaborated().remove(this);
    }
    return false;
  }

  @Transient
  public Set<ScreeningRoomUser> getAssociatedScreeningRoomUsers()
  {
    Set<ScreeningRoomUser> users = new HashSet<ScreeningRoomUser>();
    if (getLabHead() != null) {
      users.add(getLabHead());
    }
    users.add(getLeadScreener());
    users.addAll(getCollaborators());
    return users;
  }

  /**
   * Get the screen result.
   * @return the screen result
   */
  @OneToOne(
    mappedBy="screen",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }

  /**
   * Create and return a new screen result for the screen.
   * @return the new screen result
   */
  public ScreenResult createScreenResult()
  {
    _screenResult = new ScreenResult(this, false, null);
    return _screenResult;
  }

  /**
   * Set the screen result to null.
   */
  public void clearScreenResult()
  {
    _screenResult = null;
  }

  /**
   * Get the status items. A Screen may only contain one status with a given
   * {@link StatusValue#getRank() rank} value (StatusItems with the same rank are mutually
   * exclusive). Ordering of StatusItems must be equivalent whether by
   * {@link StatusValue#getRank() rank} or {@link StatusItem#getStatusDate() date}.
   *
   * @return the status items
   */
  @OneToMany(
    mappedBy="screen",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @Sort(type=SortType.NATURAL)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  public SortedSet<StatusItem> getStatusItems()
  {
    return _statusItems;
  }

  @Transient
  public StatusItem getCurrentStatusItem()
  {
    if (_statusItems.isEmpty()) {
      return null;
    }
    return _statusItems.last();
  }

  /**
   * Create and return a new <code>StatusItem</code> for this screen.
   * @param statusDate the status date
   * @param statusValue the status value
   * @return the new status item
   */
  public StatusItem createStatusItem(LocalDate statusDate, StatusValue statusValue)
  {
    StatusItem newStatusItem = new StatusItem(this, statusDate, statusValue);
    SortedSet<StatusItem> headSet = _statusItems.headSet(newStatusItem);
    SortedSet<StatusItem> tailSet = _statusItems.tailSet(newStatusItem);
    if (!headSet.isEmpty() &&
      headSet.last().getStatusDate().compareTo(newStatusItem.getStatusDate()) > 0) {
      throw new BusinessRuleViolationException("date of new status item must not be before the date of the previous status item");
    }
    if (!tailSet.isEmpty()) {
      if (tailSet.first().compareTo(newStatusItem) == 0) {
        throw new BusinessRuleViolationException("status value " + statusValue + " is mutually exclusive with existing status item value " + tailSet.first().getStatusValue());
      }
      if (tailSet.first().getStatusDate().compareTo(newStatusItem.getStatusDate()) < 0) {
        throw new BusinessRuleViolationException("date of new status item must not be after date of subsequent status item");
      }
    }
    _statusItems.add(newStatusItem);
    return newStatusItem;
  }

  /**
   * Get the lab activities.
   * @return the lab activities
   */
  @OneToMany(
    mappedBy="screen",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  @Sort(type=SortType.NATURAL)
  @edu.harvard.med.screensaver.model.annotations.OneToMany(singularPropertyName="labActivity")
  public SortedSet<LabActivity> getLabActivities()
  {
    return _labActivities;
  }

  /**
   * Get all the lab activities for this screen of a particular type.
   * @param <E> the type of the lab activities to get
   * @param clazz the type of the lab activities to get
   * @return all the lab activities for this screen of a particular type.
   */
  @SuppressWarnings("unchecked")
  @Transient
  public <E extends LabActivity> SortedSet<E> getLabActivitiesOfType(Class<E> clazz)
  {
    SortedSet<E> result = new TreeSet<E>();
    for (LabActivity labActivity : _labActivities) {
      if (clazz.isAssignableFrom(labActivity.getClass())) {
        result.add((E) labActivity);
      }
    }
    return result;
  }

  /**
   * Create and return a new library screening for the screen.
   * @param performedBy the user that performed the screening
   * @param dateOfActivity the date the lab activity took place
   * @return the new library screening
   */
  public LibraryScreening createLibraryScreening(
    ScreeningRoomUser performedBy,
    LocalDate dateOfActivity)
  {
    LibraryScreening libraryScreening =
      new LibraryScreening(this, performedBy, dateOfActivity);
    _labActivities.add(libraryScreening);
    return libraryScreening;
  }

  /**
   * Create and return a new cherry pick liquid transfer for the screen.
   * @param performedBy the user that performed the activity
   * @param dateOfActivity the date the lab activity took place
   * @return the new cherry pick liquid transfer
   */
  public CherryPickLiquidTransfer createCherryPickLiquidTransfer(
    ScreensaverUser performedBy,
    LocalDate dateOfActivity)
  {
    return createCherryPickLiquidTransfer(
      performedBy,
      dateOfActivity,
      CherryPickLiquidTransferStatus.SUCCESSFUL);
  }

  /**
   * Create and return a new cherry pick liquid transfer for the screen.
   * @param performedBy the user that performed the activity
   * @param dateOfActivity the date the lab activity took place
   * @param status the status of the cherry pick liquid transfer
   * @return the new cherry pick liquid transfer
   */
  public CherryPickLiquidTransfer createCherryPickLiquidTransfer(
    ScreensaverUser performedBy,
    LocalDate dateOfActivity,
    CherryPickLiquidTransferStatus status)
  {
    CherryPickLiquidTransfer cherryPickLiquidTransfer = new CherryPickLiquidTransfer(
      this,
      performedBy,
      dateOfActivity,
      status);
    _labActivities.add(cherryPickLiquidTransfer);
    return cherryPickLiquidTransfer;
  }

  /**
   * Create and return a new rnai cherry pick screening for the screen.
   * @param performedBy the user that performed the screening
   * @param dateOfActivity the date the screening took place
   * @param rnaiCherryPickRequest the RNAi cherry pick request
   * @return the newly created rnai cherry pick screening
   */
  public RNAiCherryPickScreening createRNAiCherryPickScreening(
    ScreeningRoomUser performedBy,
    LocalDate dateOfActivity,
    RNAiCherryPickRequest rnaiCherryPickRequest)
  {
    RNAiCherryPickScreening screening = new RNAiCherryPickScreening(
      this,
      performedBy,
      dateOfActivity,
      rnaiCherryPickRequest);
    _labActivities.add(screening);
    return screening;
  }

  /**
   * Get the cherry pick requests.
   * @return the cherry pick requests
   */
  @OneToMany(
    mappedBy="screen",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @OrderBy("dateRequested")
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  public Set<CherryPickRequest> getCherryPickRequests()
  {
    return _cherryPickRequests;
  }

  /**
   * Create and return a new cherry pick request for the screen of the appropriate type ({@link
   * CompoundCherryPickRequest} or {@link RNAiCherryPickRequest}. The cherry pick request will
   * have the {@link #getLeadScreener() lead screener} as the {@link
   * CherryPickRequest#getRequestedBy() requestor}, and the current date as the {@link
   * CherryPickRequest#getDateRequested() date requested}. It will not be a legacy ScreenDB
   * cherry pick.
   * @return the new cherry pick request
   */
  public CherryPickRequest createCherryPickRequest()
  {
    return createCherryPickRequest(getLeadScreener(), new LocalDate(), null);
  }

  /**
   * Create and return a new cherry pick request for the screen of the appropriate type ({@link
   * CompoundCherryPickRequest} or {@link RNAiCherryPickRequest}. The cherry pick request will
   * not be a legacy ScreenDB cherry pick.
   * @param requestedBy the requestor
   * @param dateRequested the date requested
   * @return the new cherry pick request
   */
  public CherryPickRequest createCherryPickRequest(
    ScreeningRoomUser requestedBy,
    LocalDate dateRequested)
  {
    return createCherryPickRequest(requestedBy, dateRequested, null);
  }

  /**
   * Create and return a new cherry pick request for the screen of the appropriate type ({@link
   * CompoundCherryPickRequest} or {@link RNAiCherryPickRequest}.
   * @param requestedBy the requestor
   * @param dateRequested the date requested
   * @param legacyId the ScreenDB legacy id
   * @return the new cherry pick request
   */
  public CherryPickRequest createCherryPickRequest(
    ScreeningRoomUser requestedBy,
    LocalDate dateRequested,
    Integer legacyId)
  {
    CherryPickRequest cherryPickRequest;
    if (getScreenType().equals(ScreenType.RNAI)) {
      cherryPickRequest = new RNAiCherryPickRequest(this, requestedBy, dateRequested, legacyId);
    }
    else if(getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
      cherryPickRequest = new CompoundCherryPickRequest(this, requestedBy, dateRequested, legacyId);
    }
    else {
      throw new UnsupportedOperationException(
        "screen of type " + getScreenType() + " does not support cherry pick requests");
    }
    _cherryPickRequests.add(cherryPickRequest);
    return cherryPickRequest;
  }

  /**
   * Get the set of abase testsets.
   * @return the abase testsets
   */
  @OneToMany(
    mappedBy="screen",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  public Set<AbaseTestset> getAbaseTestsets()
  {
    return _abaseTestsets;
  }

  /**
   * Create and return an <code>AbaseTestset</code> for the screen.
   * @param testsetDate the testset date
   * @param testsetName the testset name
   * @param comments the comments
   * @return the new abase testset
   */
  public AbaseTestset createAbaseTestset(LocalDate testsetDate, String testsetName, String comments)
  {
    AbaseTestset abaseTestset = new AbaseTestset(this, testsetDate, testsetName, comments);
    _abaseTestsets.add(abaseTestset);
    return abaseTestset;
  }

  /**
   * Get the publications.
   * @return the publications
   */
  @OneToMany(
    mappedBy="screen",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  public Set<Publication> getPublications()
  {
    return _publications;
  }

  /**
   * Create a new publication for this screen.
   *
   * @return the new publication
   */
  public Publication createPublication()
  {
    Publication publication = new Publication(this);
    _publications.add(publication);
    return publication;
  }

  public Publication createPublication(Publication publicationDTO)
  {
    Publication publication = createPublication();
    publication.setTitle(publicationDTO.getTitle());
    publication.setPubmedId(publicationDTO.getPubmedId());
    publication.setYearPublished(publicationDTO.getYearPublished());
    publication.setAuthors(publicationDTO.getAuthors());
    publication.setJournal(publicationDTO.getJournal());
    publication.setVolume(publicationDTO.getVolume());
    publication.setPages(publicationDTO.getPages());
    return publication;
  }

  /**
   * Get the attached files.
   * @return the attached files
   */
  @OneToMany(
    mappedBy="screen",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  public Set<AttachedFile> getAttachedFiles()
  {
    return _attachedFiles;
  }

  /**
   * Create and return a new attached file for the screen.
   * @param filename the filename
   * @param fileType the file type
   * @param fileContents the file contents
   * @throws IOException
   */
  public AttachedFile createAttachedFile(String filename, AttachedFileType fileType, String fileContents) throws IOException
  {
    return createAttachedFile(filename, fileType, new ByteArrayInputStream(fileContents.getBytes()));
  }

  /**
   * Create and return a new attached file for the screen. Use
   * {@link Publication#createAttachedFile(String, InputStream)} to create an
   * attached file that is associated with a Publication.
   * 
   * @param filename the filename
   * @param fileType the file type
   * @param fileContents the file contents
   * @throws IOException
   */
  public AttachedFile createAttachedFile(String filename, AttachedFileType fileType, InputStream fileContents) throws IOException
  {
    AttachedFile attachedFile = new AttachedFile(this, filename, fileType, fileContents);
    _attachedFiles.add(attachedFile);
    return attachedFile;
  }

  /**
   * Get the billing information.
   * @return the billing information
   */
  public BillingInformation getBillingInformation()
  {
    return _billingInformation;
  }
  
  /**
   * Get the set of billing items.
   * @return the billing items
   */
  @org.hibernate.annotations.CollectionOfElements
  @JoinTable(name = "screen_billing_item",
             joinColumns = @JoinColumn(name = "screen_id"))
  @org.hibernate.annotations.IndexColumn(name="ordinal")
  public List<BillingItem> getBillingItems()
  {
    return _billingItems;
  }
  
  /**
   * Set the set of billing items.
   * @param billingItems the new set of billing items
   * @motivation for hibernate
   */
  private void setBillingItems(List<BillingItem> billingItems)
  {
    _billingItems = billingItems;
  }

  /**
   * Create and return a new billing item for this billing information.
   * @param itemToBeCharged the item to be charged
   * @param amount the amount
   * @param dateSentForBilling the date sent for billing
   * @return the new billing item for this billing information
   */
  public BillingItem createBillingItem(String itemToBeCharged, BigDecimal amount, LocalDate dateSentForBilling)
  {
    if (itemToBeCharged == null) {
      throw new RequiredPropertyException(this, "billing item name");
    }
    if (amount == null) {
      throw new RequiredPropertyException(this, "billing item amount");
    }
    // allowed, as per [#1607]
    //if (dateSentForBilling == null) {
    //  throw new RequiredPropertyException(this, "billing item date faxed");
    //}
    BillingItem billingItem = new BillingItem(itemToBeCharged, amount, dateSentForBilling);
    _billingItems.add(billingItem);
    return billingItem;
  }

  public BillingItem createBillingItem(BillingItem dtoBillingItem)
  {
    return createBillingItem(dtoBillingItem.getItemToBeCharged(),
                             dtoBillingItem.getAmount(),
                             dtoBillingItem.getDateSentForBilling());
  }

  /**
   * Get the screen number.
   * @return the screen number
   */
  @Column(unique=true, nullable=false)
  public Integer getScreenNumber()
  {
    return _screenNumber;
  }

  /**
   * Set the screen number.
   * @param screenNumber the new screen number
   */
  public void setScreenNumber(Integer screenNumber)
  {
    _screenNumber = screenNumber;
  }

  @Transient
  @Override
  public Integer getStudyNumber()
  {
    return getScreenNumber();
  }

  /**
   * Get whether this <code>Screen</code> can be viewed by all users of
   * the system; that is,
   * {@link edu.harvard.med.screensaver.model.users.ScreeningRoomUser}s other
   * than those associated with the Screen.
   *
   * @return <code>true</code> iff this <code>ScreenResult</code> is
   *         shareable among all users
   */
  @Column(nullable=false, name="isShareable")
  public boolean isShareable()
  {
    return _isShareable;
  }

  /**
   * Set whether this <code>Screen</code> can be viewed by all users of
   * the system.
   * @param isShareable the new value of whether this screen can be viewed by all users of
   * the system
   * @see #isShareable()
   */
  public void setShareable(boolean isShareable)
  {
    _isShareable = isShareable;
  }

  /**
   * Get the study type.
   * @return the study type
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screens.StudyType$UserType")
  public StudyType getStudyType()
  {
    return _studyType;
  }

  /**
   * Set the study type.
   *
   * @param studyType the new studyType
   */
  public void setStudyType(StudyType studyType)
  {
    // commenting this until comprehensive testing is performed, since it requires many relationships to be eager fetched, which may cause problems
//  if (isDataLoaded()) {
//    throw new BusinessRuleViolationException("screen type is immutable after screen contains data");
//  }
    _studyType = studyType;
  }

  /**
   * Get the screen type.
   * @return the screen type
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screens.ScreenType$UserType")
  public ScreenType getScreenType()
  {
    return _screenType;
  }

  /**
   * Set the screen type.
   * @param screenType the new screen type
   * @motivation for hibernate
   */
  public void setScreenType(ScreenType screenType)
  {
    // commenting this until comprehensive testing is performed, since it requires many relationships to be eager fetched, which may cause problems
//    if (isDataLoaded()) {
//      throw new BusinessRuleViolationException("screen type is immutable after screen contains data");
//    }
    _screenType = screenType;
  }

  @Transient
  public boolean isDataLoaded()
  {
    return getScreenResult() != null || !getCherryPickRequests().isEmpty() || !getAnnotationTypes().isEmpty() || !getLabActivities().isEmpty();
  }

  /**
   * Get the title.
   * @return the title
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getTitle()
  {
    return _title;
  }

  /**
   * Set the title.
   * @param title the new title
   */
  public void setTitle(String title)
  {
    _title = title;
  }

  /**
   * Get the study url.
   * @return the study url
   */
  @Column(nullable=true)
  @org.hibernate.annotations.Type(type="text")
  public String getUrl()
  {
    return _url;
  }

  /**
   * Set the study url.
   * @param url the new study url
   */
  public void setUrl(String url)
  {
    _url = url;
  }

  /**
   * Get the data meeting scheduled.
   * @return the data meeting scheduled
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
  public LocalDate getDataMeetingScheduled()
  {
    return _dataMeetingScheduled;
  }

  /**
   * Set the data meeting scheduled date.
   * @param dataMeetingScheduled the new data meeting scheduled date
   */
  public void setDataMeetingScheduled(LocalDate dataMeetingScheduled)
  {
    _dataMeetingScheduled = dataMeetingScheduled;
  }

  /**
   * Get the data meeting completed date.
   * @return the data meeting completed date
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
  public LocalDate getDataMeetingComplete()
  {
    return _dataMeetingComplete;
  }

  /**
   * Set the data meeting complete.
   * @param dataMeetingComplete the new data meeting complete
   */
  public void setDataMeetingComplete(LocalDate dataMeetingComplete)
  {
    _dataMeetingComplete = dataMeetingComplete;
  }

  /**
   * Get the keywords.
   * @return the keywords
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="keyword", nullable=false)
  @JoinTable(
    name="screenKeyword",
    joinColumns=@JoinColumn(name="screenId")
  )
  @Sort(type=SortType.NATURAL)
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_screen_keyword_to_screen")
  public SortedSet<String> getKeywords()
  {
    return _keywords;
  }

  /**
   * Set the keywords.
   * @param keywords the new keywords
   */
  public void setKeywords(SortedSet<String> keywords)
  {
    _keywords = keywords;
  }

  /**
   * Add the keyword.
   * @param keyword the keyword to add
   * @return true iff the screen did not already have the keyword
   */
  public boolean addKeyword(String keyword)
  {
    return _keywords.add(keyword);
  }

  /**
   * Remove the keyword.
   * @param keyword the keyword to remove
   * @return true iff the screen previously had the keyword
   */
  public boolean removeKeyword(String keyword)
  {
    return _keywords.remove(keyword);
  }

  /**
   * Get the set of funding supports.
   * @return the set of funding supports
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="fundingSupport", nullable=false)
  @JoinTable(
    name="screenFundingSupport",
    joinColumns=@JoinColumn(name="screenId")
  )
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screens.FundingSupport$UserType")
  @org.hibernate.annotations.ForeignKey(name="fk_screen_funding_support_to_screen")
  public Set<FundingSupport> getFundingSupports()
  {
    return _fundingSupports;
  }

  /**
   * Add the funding support.
   * @param fundingSupport the funding support to add
   * @return true iff the screen did not already have the funding support
   */
  public boolean addFundingSupport(FundingSupport fundingSupport)
  {
    return _fundingSupports.add(fundingSupport);
  }

  /**
   * Remove the funding support.
   * @param fundingSupport the funding support to remove
   * @return true iff the screen previously had the funding support
   */
  public boolean removeFundingSupport(FundingSupport fundingSupport)
  {
    return _fundingSupports.remove(fundingSupport);
  }

  /**
   * Get the summary.
   * @return the summary
   */
  @org.hibernate.annotations.Type(type="text")
  public String getSummary()
  {
    return _summary;
  }

  /**
   * Set the summary.
   * @param summary the new summary
   */
  public void setSummary(String summary)
  {
    _summary = summary;
  }

  /**
   * Get the comments.
   * @return the comments
   */
  @org.hibernate.annotations.Type(type="text")
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }

  /**
   * Get the abase study id.
   * @return the abase study id
   */
  @org.hibernate.annotations.Type(type="text")
  public String getAbaseStudyId()
  {
    return _abaseStudyId;
  }

  /**
   * Set the abase study id.
   * @param abaseStudyId the new abase study id
   */
  public void setAbaseStudyId(String abaseStudyId)
  {
    _abaseStudyId = abaseStudyId;
  }

  /**
   * Get the abase protocol id.
   * @return the abase protocol id
   */
  @org.hibernate.annotations.Type(type="text")
  public String getAbaseProtocolId()
  {
    return _abaseProtocolId;
  }

  /**
   * Set the abase protocol id.
   * @param abaseProtocolId the new abase protocol id
   */
  public void setAbaseProtocolId(String abaseProtocolId)
  {
    _abaseProtocolId = abaseProtocolId;
  }

  /**
   * Get the assay readout types.
   * @return the assay readout types
   */
  @Transient
  public Set<AssayReadoutType> getAssayReadoutTypes()
  {
    Set<AssayReadoutType> assayReadoutTypes = new HashSet<AssayReadoutType>();
    if (getScreenResult() != null) {
      for (ResultValueType rvt : getScreenResult().getResultValueTypes()) {
        if (rvt.getAssayReadoutType() != null) {
          assayReadoutTypes.add(rvt.getAssayReadoutType());
        }
      }
    }
    return assayReadoutTypes;
  }

  /**
   * Get the assay readout types, as a formatted, comma-delimited string.
   * @return the assay readout types, as a formatted, comma-delimited string
   */
  @Transient
  public String getAssayReadoutTypesString()
  {
    if (getScreenResult() != null) {
      return StringUtils.makeListString(getAssayReadoutTypes(), ", ");
    }
    return "";
  }

  // TODO: extract PublishableProtocol value-typed collection

  /**
   * Get the date the publishable protocol was entered.
   * @return the date the publishable protocol was entered
   */
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
  public LocalDate getPublishableProtocolDateEntered()
  {
    return _publishableProtocolDateEntered;
  }

  /**
   * Set the date the publishable protocol was entered.
   * @param publishableProtocolDateEntered the new date the publishable protocol was entered
   */
  public void setPublishableProtocolDateEntered(LocalDate publishableProtocolDateEntered)
  {
    _publishableProtocolDateEntered = publishableProtocolDateEntered;
  }

  /**
   * Get the initials of the administrator who entered the publishable protocol.
   * @return the initials of the administrator who entered the publishable protocol
   */
  @org.hibernate.annotations.Type(type="text")
  public String getPublishableProtocolEnteredBy()
  {
    return _publishableProtocolEnteredBy;
  }

  /**
   * Set the initials of the administrator who entered the publishable protocol.
   * @param publishableProtocolEnteredBy the new initials of the administrator who
   * entered the publishable protocol
   */
  public void setPublishableProtocolEnteredBy(String publishableProtocolEnteredBy)
  {
    _publishableProtocolEnteredBy = publishableProtocolEnteredBy;
  }

  /**
   * Get the publishable protocol.
   * @return the publishable protocol
   */
  @org.hibernate.annotations.Type(type="text")
  public String getPublishableProtocol()
  {
    return _publishableProtocol;
  }

  /**
   * Set the publishable protocol.
   * @param publishableProtocol the new publishable protocol
   */
  public void setPublishableProtocol(String publishableProtocol)
  {
    _publishableProtocol = publishableProtocol;
  }

  /**
   * Get the publishable protocol comments.
   * @return the publishable protocol comments
   */
  @org.hibernate.annotations.Type(type="text")
  public String getPublishableProtocolComments()
  {
    return _publishableProtocolComments;
  }

  /**
   * Set the publishable protocol comments.
   * @param publishableProtocolComments the new publishable protocol comments
   */
  public void setPublishableProtocolComments(String publishableProtocolComments)
  {
    _publishableProtocolComments = publishableProtocolComments;
  }

  /**
   * Get the date of application.
   * @return the date of application
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
  public LocalDate getDateOfApplication()
  {
    return _dateOfApplication;
  }

  /**
   * Set the date of application.
   * @param dateOfApplication the new date of application
   */
  public void setDateOfApplication(LocalDate dateOfApplication)
  {
    _dateOfApplication = dateOfApplication;
  }

  /**
   * Get the annotation types provided by this study.
   * @return the annotation types
   */
  @OneToMany(
    mappedBy="study",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @OrderBy("ordinal")
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  public SortedSet<AnnotationType> getAnnotationTypes()
  {
    return _annotationTypes;
  }

  /**
   * Create and return a new annotation type for the study.
   * @param name the name of the annotation type
   * @param description the description for the annotation type
   * @param isNumeric true iff this annotation type contains numeric result values
   * @return the new annotation type
   */
  public AnnotationType createAnnotationType(
      String name,
      String description,
      boolean isNumeric)
  {
    verifyNameIsUnique(name);
    AnnotationType annotationType = new AnnotationType(
      this,
      name,
      description,
      _annotationTypes.size(),
      isNumeric);
    _annotationTypes.add(annotationType);
    return annotationType;
  }

  /**
   * Get the set of reagents associated with this screen result. <i>Do not modify
   * the returned collection.</i> To add a reagent, call {@link #addReagent}.
   * @motivation efficiently find all reagent-related data for a study (w/o reading annotationTypes.annotationValues.reagents)
   * @return the set of reagents associated with this screen result
   */
  @ManyToMany(fetch=FetchType.LAZY)
  @JoinTable(
    name="studyReagentLink",
    joinColumns=@JoinColumn(name="studyId"),
    inverseJoinColumns=@JoinColumn(name="reagentId", nullable=true, updatable=true)

  )
  @org.hibernate.annotations.ForeignKey(name="fk_reagent_link_to_study")
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @edu.harvard.med.screensaver.model.annotations.ManyToMany(inverseProperty="studies")
  public Set<Reagent> getReagents()
  {
    return _reagents;
  }

  /**
   * Add the reagent.
   * @param reagent the reagent to add
   * @return true iff the screen did not already have the reagent
   */
  public boolean addReagent(Reagent reagent)
  {
    if (_reagents.add(reagent)) {
      reagent.addStudy(this);
      return true;
    }
    return false;
  }

  /**
   * Remove the reagent.
   * @param reagent the reagent to remove
   * @return true iff the screen previously had the reagent
   */
  public boolean removeReagent(Reagent reagent)
  {
    if (_reagents.remove(reagent)) {
      reagent.removeStudy(this);
      return true;
    }
    return false;
  }

  @Transient
  public List<StatusValue> getCandidateStatusValues()
  {
    List<StatusValue> candidateStatusValues = new ArrayList<StatusValue>(Arrays.asList(StatusValue.values()));
    Set<Integer> illegalStatusValueRanks = new HashSet<Integer>();
    for (StatusItem statusItem : getStatusItems()) {
      illegalStatusValueRanks.add(statusItem.getStatusValue().getRank());
    }
    Iterator<StatusValue> iter = candidateStatusValues.iterator();
    while (iter.hasNext()) {
      if (illegalStatusValueRanks.contains(iter.next().getRank())) {
        iter.remove();
      }
    }
    return candidateStatusValues;
  }


  // private instance methods

  /**
   * Set the id for the screen.
   * @param screenId the new id for the screen
   * @motivation for hibernate
   */
  private void setScreenId(Integer screenId)
  {
    _screenId = screenId;
  }

  /**
   * Get the version for the screen.
   * @return the version for the screen
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the screen.
   * @param version the new version for the screen
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the set of collaborators.
   * @param collaborators the new set of collaborators
   * @motivation for hibernate
   */
  private void setCollaborators(SortedSet<ScreeningRoomUser> collaborators)
  {
    _collaborators = collaborators;
  }

  /**
   * Set the screen result.
   * @param screenResult the new screen result
   * @motivation for hibernate
   */
  private void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
  }

  /**
   * Set the status items.
   * @param statusItems the new status items
   * @motivation for hibernate
   */
  private void setStatusItems(SortedSet<StatusItem> statusItems)
  {
    _statusItems = statusItems;
  }

  /**
   * @motivation for hibernate
   */
  private void setLabActivities(SortedSet<LabActivity> labActivities)
  {
    _labActivities = labActivities;
  }

  /**
   * Set the cherry pick requests.
   * @param cherryPickRequests the new cherry pick requests
   * @motivation for hibernate
   */
  private void setCherryPickRequests(Set<CherryPickRequest> cherryPickRequests)
  {
    _cherryPickRequests = cherryPickRequests;
  }

  /**
   * Set the abase testsets.
   * @param abaseTestsets the new abase testsets
   * @motivation for hibernate
   */
  private void setAbaseTestsets(Set<AbaseTestset> abaseTestsets)
  {
    _abaseTestsets = abaseTestsets;
  }

  /**
   * Set the publications.
   * @param publications the new publications
   * @motivation for hibernate
   */
  private void setPublications(Set<Publication> publications)
  {
    _publications = publications;
  }

  /**
   * Set the attached files.
   * @param attachedFiles the new attached files
   * @motivation for hibernate
   */
  private void setAttachedFiles(Set<AttachedFile> attachedFiles)
  {
    _attachedFiles = attachedFiles;
  }

  /**
   * Set the billing information.
   * @param billingInformation the new billing information
   * @motivation for hibernate
   */
  private void setBillingInformation(BillingInformation billingInformation)
  {
    _billingInformation = billingInformation;
  }

  /**
   * Set the funding supports.
   * @param fundingSupports the new funding supports
   * @motivation for hibernate
   */
  private void setFundingSupports(Set<FundingSupport> fundingSupports)
  {
    _fundingSupports = fundingSupports;
  }

  /**
   * Set the annotation types.
   * @param annotationTypes the new annotation types
   * @motivation for hibernate
   */
  private void setAnnotationTypes(SortedSet<AnnotationType> annotationTypes)
  {
    _annotationTypes = annotationTypes;
  }

  /**
   * Set the reagents.
   * @param reagents the new reagents
   * @motivation for hibernate
   */
  private void setReagents(Set<Reagent> reagents)
  {
    _reagents = reagents;
  }

  private void verifyNameIsUnique(String name)
  {
    for (AnnotationType at : getAnnotationTypes()) {
      if (at.getName().equals(name)) {
        throw new DuplicateEntityException(this, at);
      }
    }
  }

  /**
   * Get the set of libraries that this screen has been authorized to screen,
   * even if the library's screening status would otherwise not permit it.
   */
  @Transient
  public Set<Library> getLibrariesPermitted()
  {
    return Collections.emptySet();
  }
}