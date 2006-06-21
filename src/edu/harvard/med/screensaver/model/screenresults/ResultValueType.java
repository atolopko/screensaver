// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;


/**
 * Provides the metadata for a subset of a
 * {@link edu.harvard.med.screensaver.model.screens.Screen Screen}'s
 * {@link ResultValue}s, all of which will have been produced with the "same
 * way". A <code>ResultValueType</code> can describe either how a subset of
 * raw data values were generated via automated machine reading of assay plates,
 * or how a subset of derived data values were calculated. For raw data values,
 * the metadata describes the parameters of a single, physical machine read of a
 * single replicate assay plate, using the same assay read technology, at the
 * same read time interval, etc.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class
 *   lazy="false"
 */
public class ResultValueType extends AbstractEntity implements Comparable
{

  // TODO: perhaps we should split ResultValueType into subclasses, one for raw
  // data value descriptions and one for derived data descriptions?
  
  private static final long serialVersionUID = -2325466055774432202L;

  
  // instance data

  private Integer                    _resultValueTypeId;
  private Integer                    _version;
  private ScreenResult               _screenResult;
  private SortedSet<ResultValue>     _resultValues = new TreeSet<ResultValue>();
  private String                     _name;
  private String                     _description;
  private Integer                    _ordinal;
  private Integer                    _replicateOrdinal;
  private AssayReadoutType           _assayReadoutType;
  private String                     _timePoint;
  private boolean                    _isDerived;
  private String                     _howDerived;
  private SortedSet<ResultValueType> _typesDerivedFrom = new TreeSet<ResultValueType>();
  private SortedSet<ResultValueType> _derivedTypes = new TreeSet<ResultValueType>();
  private boolean                    _isActivityIndicator;
  private ActivityIndicatorType      _activityIndicatorType;
  private IndicatorDirection         _indicatorDirection;
  private Double                     _indicatorCutoff;
  private boolean                    _isFollowUpData;
  private String                     _assayPhenotype;
  private boolean                    _isCherryPick;
  private String                     _comments;

  
  // public constructors and instance methods

  /**
   * Constructs an initialized ResultValueType object.
   * @param screenResult
   * @param name
   * @param replicateOrdinal
   * @param isDerived
   * @param isActivityIndicator
   * @param isFollowupData
   * @param assayPhenotype
   * @param isCherryPick
   */
  public ResultValueType(
    ScreenResult screenResult,
    String name,
    Integer replicateOrdinal,
    boolean isDerived,
    boolean isActivityIndicator,
    boolean isFollowupData,
    String assayPhenotype,
    boolean isCherryPick
    )
  {
    setScreenResult(screenResult);
    setName(name);
    setReplicateOrdinal(replicateOrdinal);
    setDerived(isDerived);
    setActivityIndicator(isActivityIndicator);
    setFollowUpData(isFollowupData);
    setAssayPhenotype(assayPhenotype);
    setCherryPick(isCherryPick);
  }
  
  // TODO: jps: I suggest this as a valid minimal constructor; we should discuss... --ant
  /**
   * Constructs an initialized ResultValueType object.
   * @param screenResult
   * @param name
   */
  public ResultValueType(
    ScreenResult screenResult,
    String name)
  {
    setScreenResult(screenResult);
    setName(name);
  }

  /**
   * Get a unique identifier for the <code>ResultValueType</code>.
   * 
   * @return an Integer representing a unique identifier for the
   *         <code>ResultValueType</code>
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="result_value_type_id_seq"
   */
  public Integer getResultValueTypeId() {
    return _resultValueTypeId;
  }

  /**
   * Set a unique identifier for the <code>ResultValueType</code>.
   * 
   * @param resultValueTypeId a unique identifier for the
   *          <code>ResultValueType</code>
   */
  public void setResultValueTypeId(Integer resultValueTypeId) {
    _resultValueTypeId = resultValueTypeId;
  }
  
  /**
   * Get the parent {@link ScreenResult}.
   * @return the parent {@link ScreenResult}
   */
  public ScreenResult getScreenResult() {
    return _screenResult;
  }
  
  /**
   * Add this <code>ResultValueType</code> to the specified
   * {@link ScreenResult}, removing from the existing {@link ScreenResult}
   * parent, if necessary.
   * @param newScreenResult the new parent {@link ScreenResult}
   */
  public void setScreenResult(ScreenResult newScreenResult) {
    if (_screenResult != null && newScreenResult != _screenResult) {
      _screenResult.getHbnResultValueTypes().remove(this);
    }
    _screenResult = newScreenResult;
    setOrdinal(_screenResult.getHbnResultValueTypes().size());
    _screenResult.getHbnResultValueTypes().add(this);
  }

  /**
   * Get a set of all {@link ResultValue}s for this
   * <code>ResultValueType</code>.
   * 
   * @return an unmodifiable {@link java.util.SortedSet} of the
   *         {@link ResultValue}s generated for this
   *         <code>ResultValueType</code>.
   */
  public SortedSet<ResultValue> getResultValues() {
    return Collections.unmodifiableSortedSet(_resultValues);
  }
  
  /**
   * Add the result value to the result value type.
   * 
   * @param resultValue the result value to add to the result value type
   * @return true iff the result value was not already in the result value type
   */
  public boolean addResultValue(ResultValue resultValue) {
    assert !(_resultValues.contains(resultValue) ^
      resultValue.getResultValueType().equals(this)) :
      "asymmetric result value type / result value association encountered";
    if (_resultValues.add(resultValue)) {
      resultValue.setHbnResultValueType(this);
      return true;
    }
    return false;
  }

  /**
   * Remove the result value from the result value type.
   * 
   * @param resultValue the result value to remove from the result value type
   * @return true iff the result value was previously in the result value type
   */
  public boolean removeResultValue(ResultValue resultValue) {
    assert !(_resultValues.contains(resultValue) ^
      resultValue.getResultValueType().equals(this)) :
      "asymmetric result value type / result value association encountered";
    if (_resultValues.remove(resultValue)) {
      resultValue.setHbnResultValueType(null);
      return true;
    }
    return false;
  }
  
  /**
   * Get the ordinal position of this <code>ResultValueType</code> within its
   * parent {@link ScreenResult}.
   * 
   * @return an <code>Integer</code>
   * @hibernate.property type="integer" column="ordinal"
   */  
  public Integer getOrdinal() {
    return _ordinal;
  }
  
  /**
   * Set the ordinal position of this <code>ResultValueType</code> within its
   * parent {@link ScreenResult}. To be called by Hibernate only, as this
   * property is set automatically when {@link #setScreenResult(ScreenResult)}
   * is called.
   * 
   * @param ordinal the ordinal position of this <code>ResultValueType</code>
   *          within its parent {@link ScreenResult}
   * @motivation for Hibernate
   */
  public void setOrdinal(Integer ordinal) {
    _ordinal = ordinal;
  }

  /**
   * Get the replicate ordinal, a zero-based index indicating the order of this
   * <code>ResultValueType</code> within its parent {@link ScreenResult}.
   * This ordering is really only significant from the standpoint of presenting
   * a {@link ScreenResult} to the user (historically speaking, it reflects the
   * ordering found during spreadsheet file import).
   * 
   * @return the replicate ordinal
   * @hibernate.property
   *   type="integer"
   */
  public Integer getReplicateOrdinal() {
    return _replicateOrdinal;
  }
  
  /**
   * Set the replicate ordinal, a 1-based index indicating the order of this
   * <code>ResultValueType</code> within its parent {@link ScreenResult}.  
   * 
   * @param replicateOrdinal the replicate ordinal
   */
  public void setReplicateOrdinal(Integer replicateOrdinal) {
    assert replicateOrdinal == null || replicateOrdinal > 0 : "replicate ordinal values must be positive (non-zero), unless null";
    _replicateOrdinal = replicateOrdinal;
  }
  
  /**
   * Get the Activity Indicator Type.
   * @return an {@link ActivityIndicatorType} enum
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType$UserType"
   */
  public ActivityIndicatorType getActivityIndicatorType() {
    return _activityIndicatorType;
  }

  /**
   * Set the Activity Indicator Type.
   * @param activityIndicatorType the Activity Indicator Type to set
   */
  public void setActivityIndicatorType(ActivityIndicatorType activityIndicatorType)
  {
    _activityIndicatorType = activityIndicatorType;
  }


  /**
   * Get the Assay Phenotype.
   * @return a <code>String</code> representing the Assay Phenotype
   * @hibernate.property
   *   type="text"
   */
  public String getAssayPhenotype() {
    return _assayPhenotype;
  }


  /**
   * Set the Assay Phenotype.
   * @param assayPhenotype the Assay Phenotype to set
   */
  public void setAssayPhenotype(String assayPhenotype) {
    _assayPhenotype = assayPhenotype;
  }


  /**
   * Get the Assay Readout Type.
   * @return an {@link AssayReadoutType} enum
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.screens.AssayReadoutType$UserType"
   */
  public AssayReadoutType getAssayReadoutType() {
    return _assayReadoutType;
  }

  /**
   * Set the Assay Readout Type.
   * @param assayReadoutType the Assay Readout Type to set
   */
  public void setAssayReadoutType(AssayReadoutType assayReadoutType) {
    // TODO: verify that assayReadoutType is in Screen's assayReadoutTypes set.
    _assayReadoutType = assayReadoutType;
  }

  /**
   * Get the comments. Comments should describe real-world issues relating to
   * how the data was generated, how it may be problematic, etc. Contrast with
   * {@link #getDescription()}.
   * 
   * @return a <code>String</code> containing the comments
   * @see #getDescription()
   * @hibernate.property type="text"
   */
  public String getComments() {
    return _comments;
  }


  /**
   * Set the comments.
   * @param comments The comments to set.
   */
  public void setComments(String comments) {
    _comments = comments;
  }

  /**
   * Get the set of {@link ResultValueType}s that this
   * <code>ResultValueType</code> was derived from. By "derived", we mean that
   * the calculated values of our {@link ResultValues} depend upon the the
   * {@link ResultValue}s of other {@link ResultValueType}s (of the same stock
   * plate well). The details of the derivation should be specified via
   * {@link #setHowDerived}.
   * 
   * @return the set of {@link ResultValueType}s that this
   *         <code>ResultValueType</code> was derived from
   */
  public SortedSet<ResultValueType> getTypesDerivedFrom() {
    return Collections.unmodifiableSortedSet(getHbnTypesDerivedFrom());
  }

  /**
   * Add the result value type to the types derived from.
   * @param typeDerivedFrom the result value type to add 
   * @return true iff the result value type was not already contained in the
   * set of types derived from this type
   */
  public boolean addTypeDerivedFrom(ResultValueType typeDerivedFrom) {
    assert !(typeDerivedFrom.getHbnDerivedTypes().contains(this) ^
      getHbnTypesDerivedFrom().contains(typeDerivedFrom)) : 
      "asymmetric types derived from / derived types association encountered";
    if (getHbnTypesDerivedFrom().add(typeDerivedFrom)) {
      setDerived(true);
      return typeDerivedFrom.getHbnDerivedTypes().add(this);
    }
    return false;
  }
  
  /**
   * Remove the result value type from the types derived from.
   * @param typeDerivedFrom the result value type to remove
   * @return true iff the result value type was previously contained in
   * the set of types derived from this type
   */
  public boolean removeTypeDerivedFrom(ResultValueType typeDerivedFrom) {
    assert !(typeDerivedFrom.getHbnDerivedTypes().contains(this) ^
      getHbnTypesDerivedFrom().contains(typeDerivedFrom)) : 
      "asymmetric types derived from / derived types association encountered";
    if (getHbnTypesDerivedFrom().remove(typeDerivedFrom)) {
      setDerived(! getHbnTypesDerivedFrom().isEmpty());
      return typeDerivedFrom.getHbnDerivedTypes().remove(this);
    }
    return false;
  }
  
  /**
   * Get the set of {@link ResultValueType}s that derive from this
   * <code>ResultValueType</code>.
   * 
   * @return the set of {@link ResultValueType}s that derive from this
   *         <code>ResultValueType</code>
   */
  public SortedSet<ResultValueType> getDerivedTypes() {
    return Collections.unmodifiableSortedSet(getHbnDerivedTypes());
  }

  /**
   * Add the result value type to the derived types.
   * @param derivedType the result value type to add 
   * @return true iff the result value type was not already contained in the
   * set of derived types
   */
  public boolean addDerivedType(ResultValueType derivedType) {
    assert !(derivedType.getHbnTypesDerivedFrom().contains(this) ^
      getHbnDerivedTypes().contains(derivedType)) : 
      "asymmetric derived types / types derived from association encountered";
    if (getHbnDerivedTypes().add(derivedType)) {
      derivedType.getHbnTypesDerivedFrom().add(this);
      derivedType.setDerived(true);
      return true;
    }
    return false;
  }
  
  /**
   * Remove the result value type from the derived types.
   * @param derivedType the result value type to remove
   * @return true iff the result value type was previously contained in
   * the set of derived types
   */
  public boolean removeDerivedType(ResultValueType derivedType) {
    assert !(derivedType.getHbnTypesDerivedFrom().contains(this) ^
      getHbnDerivedTypes().contains(derivedType)) : 
      "asymmetric derived types / types derived from association encountered";
    if (getHbnDerivedTypes().remove(derivedType)) {
      derivedType.getHbnTypesDerivedFrom().remove(this);
      derivedType.setDerived(! derivedType.getHbnTypesDerivedFrom().isEmpty());
      return true;
    }
    return false;
  }                       

  /**
   * Get a description of this <code>ResultValueType</code>.
   * 
   * @return a <code>String</code> description of this
   *         <code>ResultValueType</code>
   * @hibernate.property type="text"
   */
  public String getDescription() {
    return _description;
  }

  /**
   * Set a description of this <code>ResultValueType</code>.
   * 
   * @param description a description of this <code>ResultValueType</code>
   */
  public void setDescription(String description) {
    _description = description;
  }


  /**
   * Get then description of how this <code>ResultValueType</code> was derived
   * from other <code>ResultValueType</code>s.
   * 
   * @return a <code>String</code> description of how this
   *         <code>ResultValueType</code> was derived from other
   *         <code>ResultValueType</code>s
   * @hibernate.property type="text"
   */
  public String getHowDerived() {
    return _howDerived;
  }

  /**
   * Set the description of how this <code>ResultValueType</code> was derived
   * from other <code>ResultValueType</code>s.
   * 
   * @param howDerived a description of how this <code>ResultValueType</code>
   *          was derived from other <code>ResultValueType</code>s
   */
  public void setHowDerived(String howDerived) {
    _howDerived = howDerived;
  }


  public Double getIndicatorCutoff() {
    return _indicatorCutoff;
  }


  public void setIndicatorCutoff(Double indicatorCutoff) {
    _indicatorCutoff = indicatorCutoff;
  }

  /**
   * Get the indicator direction, which indicates whether a "hit" exists based
   * upon whether a numeric result value is above or below the indicator cutoff.
   * 
   * @return an {@link IndicatorDirection} enum
   * @hibernate.property type="edu.harvard.med.screensaver.model.screenresults.IndicatorDirection$UserType"
   */
  public IndicatorDirection getIndicatorDirection() {
    return _indicatorDirection;
  }

  /**
   * Set the indicator direction, which indicates whether a "hit" exists based
   * upon whether a numeric result value is above or below the indicator cutoff.
   * 
   * @param indicatorDirection the indicator direction
   */
  public void setIndicatorDirection(IndicatorDirection indicatorDirection) {
    _indicatorDirection = indicatorDirection;
  }

  /**
   * Get whether this <code>ResultValueType</code> is an activity indicator.
   * TODO: explain what this is, exactly.
   * 
   * @return <code>true</code> iff this <code>ResultValueType</code> is an
   *         activity indicator
   * @hibernate.property type="boolean" not-null="true"
   */
  public boolean isActivityIndicator() {
    return _isActivityIndicator;
  }

  /**
   * Set whether this <code>ResultValueType</code> is an activity indicator.
   * 
   * @param isActivityIndicator set to <code>true</code> iff this
   *          <code>ResultValueType</code> is an activity indicator
   */
  public void setActivityIndicator(boolean isActivityIndicator) {
    _isActivityIndicator = isActivityIndicator;
  }

  /**
   * Get whether the {@link ResultValue}s of this <code>ResultValueType</code>
   * constitute the criteria for making cherry picks.
   * 
   * @return <code>true</code> iff the {@link ResultValue}s of this
   *         <code>ResultValueType</code> constitute the criteria for making
   *         cherry picks.
   * @hibernate.property type="boolean" not-null="true"
   */
  public boolean isCherryPick() {
    return _isCherryPick;
  }

  /**
   * Set whether the {@link ResultValue}s of this <code>ResultValueType</code>
   * constitute the criteria for making cherry picks.
   * 
   * @param isCherryPick set to <code>true</code> iff the {@link ResultValue}s
   *          of this <code>ResultValueType</code> constitute the criteria for
   *          making cherry picks.
   */
  public void setCherryPick(boolean isCherryPick) {
    _isCherryPick = isCherryPick;
   }

  /**
   * Get whether this <code>ResultValueType</code> contains follow up data
   * [TODO: presumably generated during a subsequent visit?]
   * 
   * @return <code>true</code> iff this <code>ResultValueType</code>
   *         contains follow up data
   * @hibernate.property type="boolean" not-null="true"
   */
  public boolean isFollowUpData() {
    return _isFollowUpData;
  }

  /**
   * Set whether this <code>ResultValueType</code> contains follow up data
   * [TODO: presumably generated during a subsequent visit?]
   * 
   * @param isFollowUpData set to <code>true</code> iff this
   *          <code>ResultValueType</code> contains follow up data
   */
  public void setFollowUpData(boolean isFollowUpData) {
    _isFollowUpData = isFollowUpData;
  }

  /**
   * Get the name of this <code>ResultValueType</code>.
   * 
   * @return a <code>String</code> name
   * @hibernate.property type="string" not-null="true"
   */
  public String getName() {
    return _name;
  }

  /**
   * Set the name of this <code>ResultValueType</code>.
   * @param name the name of this <code>ResultValueType</code>
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * Get the time point, indicating the time interval, relative to the time the
   * assay plate was first read [TODO: prepared?], at which the
   * {@link ResultValue}s for this <code>ResultValueType</code> were read.
   * The format and units for the time point is arbitrary.
   * 
   * @return a <code>String</code> representing the time point
   * @hibernate.property type="text"
   */
  public String getTimePoint() {
    return _timePoint;
  }


  /**
   * Get the time point, indicating the time interval, relative to the time the
   * assay plate was first read [TODO: prepared?], at which the
   * {@link ResultValue}s for this <code>ResultValueType</code> were read.
   * The format and units for the time point is arbitrary.
   * 
   * @param timePoint the time point
   */
  public void setTimePoint(String timePoint) {
    _timePoint = timePoint;
  }


  // public Object methods
  
  /**
   * Get whether this <code>ResultValueType</code> is derived from other
   * <code>ResultValueType</code>s.
   * 
   * @return <code>true</code> iff this <code>ResultValueType</code> is
   *         derived from other <code>ResultValueType</code>s.
   * @see #setTypesDerivedFrom(SortedSet)
   * @hibernate.property type="boolean" not-null="true"
   */
  public boolean isDerived() {
    return _isDerived;
  }

  /**
   * Set whether this <code>ResultValueType</code> is derived from other
   * <code>ResultValueType</code>s.
   * 
   * @param isDerived <code>true</code> iff this <code>ResultValueType</code>
   *          is derived from other <code>ResultValueType</code>s.
   * @see #setTypesDerivedFrom(SortedSet)
   */
  public void setDerived(boolean isDerived) {
    _isDerived = isDerived;
  }

  
  // Comparable interface methods

  /**
   * Defines natural ordering of <code>ResultValueType</code> objects, based
   * upon their ordinal field value. Note that natural ordering is only defined
   * between <code>ResultValueType</code> objects that share the same parent
   * {@link ScreenResult}.
   */
  public int compareTo(Object that) {
    return getOrdinal().compareTo(((ResultValueType) that).getOrdinal());
  }


  // protected getters and setters
  
  /**
   * Set the parent {@link ScreenResult}.
   * @param newScreenResult the parent {@link ScreenResult}
   * @motivation for Hibernate
   */
  void setHbnScreenResult(ScreenResult screenResult) {
    _screenResult = screenResult;
  }

  /**
   * Get the set of {@link ResultValue}s that were generated for this
   * <code>ResultValueType</code>.
   * 
   * @motivation for Hibernate and bi-directional association management
   * @return the {@link java.util.SortedSet} of {@link ResultValue}s generated
   *         for this <code>ResultValueType</code>
   * @hibernate.set cascade="all-delete-orphan" inverse="true" sort="natural"
   * @hibernate.collection-one-to-many class="edu.harvard.med.screensaver.model.screenresults.ResultValue"
   * @hibernate.collection-key column="result_value_type_id"
   */
  SortedSet<ResultValue> getHbnResultValues() {
    return _resultValues;
  }

  /**
   * Get a business key that uniquely represents this object and that is based
   * upon some subset of its domain-model data fields.
   * 
   * @motivation for Hibernate (as hashCode()-based set membership cannot rely
   *             upon database sequence ID)
   * @motivation used by Comparable methods
   * @return a <code>String</code> representing the business key
   */
  protected String getBusinessKey() {
    assert _screenResult != null && _ordinal != null :
      "business key fields have not been defined";
    return getScreenResult().getBusinessKey().toString() + ":" + getOrdinal();
  }


  // private constructors and instance methods

  /**
   * Constructs an uninitialized <code>ResultValueType</code> object.
   * @motivation for Hibernate loading
   */
  private ResultValueType() {}
  
  /**
   * Get the version number of the compound.
   * 
   * @return the version number of the <code>ResultValueType</code>
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version number of the <code>ResultValueType</code>
   * 
   * @param version the new version number for the <code>ResultValueType</code>
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the parent {@link ScreenResult}.
   * 
   * @hibernate.many-to-one class="edu.harvard.med.screensaver.model.screenresults.ScreenResult"
   *                        column="screen_result_id" not-null="true"
   */
  private ScreenResult getHbnScreenResult() {
    return _screenResult;
  }
  
  /**
   * Set the set of {@link ResultValue}s that comprise this
   * <code>ResultValueType</code>.
   * 
   * @param resultValue the {@link java.util.SortedSet} of {@link ResultValue}s
   *          generated for this <code>ResultValueType</code>.
   * @motivation for Hibernate
   */
  private void setHbnResultValues(SortedSet<ResultValue> resultValues) {
    _resultValues = resultValues;
  }

  /**
   * Get the set of {@link ResultValueType}s that this
   * <code>ResultValueType</code> was derived from.
   * The caller of this method must ensure bi-directionality is perserved.
   * 
   * @return the set of {@link ResultValueType}s that this
   *         <code>ResultValueType</code> was derived from
   * @hibernate.set
   *   table="result_value_type_derived_from_link"
   *   sort="natural"
   * @hibernate.collection-key
   *   column="derived_result_value_type_id"
   * @hibernate.collection-many-to-many
   *   class="edu.harvard.med.screensaver.model.screenresults.ResultValueType"
   *   column="derived_from_result_value_type_id"
   *   foreign-key="fk_result_value_type_derived_result_value_type"
   */
  private SortedSet<ResultValueType> getHbnTypesDerivedFrom() {
    return _typesDerivedFrom;
  }

  /**
   * Set the set of {@link ResultValueType}s that this
   * <code>ResultValueType</code> was derived from. The caller of this method
   * must ensure bi-directionality is perserved.
   * 
   * @param derivedFrom the set of {@link ResultValueType}s that this
   *          <code>ResultValueType</code> was derived from
   * @motivation for hibernate
   */
  private void setHbnTypesDerivedFrom(SortedSet<ResultValueType> derivedFrom) {
    if (derivedFrom != null && derivedFrom.size() > 0) {
      setDerived(true);
    }
    _typesDerivedFrom = derivedFrom;
  }
  
  /**
   * Get the set of {@link ResultValueType}s that derive from this
   * <code>ResultValueType</code>.
   * The caller of this method must ensure bi-directionality is perserved.
   * 
   * @return the set of {@link ResultValueType}s that derive from this
   *         <code>ResultValueType</code>
   * @hibernate.set
   *   table="result_value_type_derived_from_link"
   *   sort="natural"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="derived_from_result_value_type_id"
   * @hibernate.collection-many-to-many
   *   class="edu.harvard.med.screensaver.model.screenresults.ResultValueType"
   *   column="derived_result_value_type_id"
   *   foreign-key="fk_result_value_type_derived_result_value_type"
   */
  private SortedSet<ResultValueType> getHbnDerivedTypes() {
    return _derivedTypes;
  }

  /**
   * Set the set of {@link ResultValueType}s that derive from this
   * <code>ResultValueType</code>. The caller of this method
   * must ensure bi-directionality is perserved.
   * 
   * @param derivedTypes the set of {@link ResultValueType}s that derive from
   * this <code>ResultValueType</code>
   * @motivation for hibernate
   */
  private void setHbnDerivedTypes(SortedSet<ResultValueType> derivedTypes) {
    _derivedTypes = derivedTypes;
  }
}
