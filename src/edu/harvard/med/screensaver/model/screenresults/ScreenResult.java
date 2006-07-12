// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Well;

/**
 * A <code>ScreenResult</code> represents the data produced by machine-reading
 * each of the assay plates associated with a
 * {@link edu.harvard.med.screensaver.model.screens.Screen}. Each stock plate
 * of the library being screened will be replicated across one or more assay
 * plates ("replicates"). Each replicate assay plate can have one or more
 * readouts performed on it, possibly over time intervals and/or with different
 * assay readout technologies. Every distinct readout type is identified by a
 * {@link ResultValueType}. A <code>ScreenResult</code> becomes the parent of
 * {@link ResultValue}s. For visualization purposes, one can imagine a
 * <code>ScreenResult</code> as representing a spreadsheet, where the column
 * headings are represented by {@link ResultValueType}s and the rows are
 * identified by stock plate {@link Well}s, and each row contains a
 * {@link ResultValue} for each {@link ResultValueType} "column".
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class lazy="false"
 */
public class ScreenResult extends AbstractEntity
{

  private static final long serialVersionUID = 41904893172411174L;
  

  // persistent instance data
  
  private Integer                    _screenResultId;
  private Integer                    _version;
  private Date                       _dateCreated;
  private boolean                    _isShareable;
  private Integer                    _replicateCount;
  private SortedSet<ResultValueType> _resultValueTypes = new TreeSet<ResultValueType>();
  

  // transient (derived) instance data
  
  transient private TreeSet<Integer> _plateNumbers;

  
  // public constructors and instance methods
  
  /**
   * Constructs an initialized ScreenResult object.
   * @param dateCreated
   * @param isShareable
   * @param replicateCount
   */
  public ScreenResult(
    Date dateCreated,
    boolean isShareable,
    Integer replicateCount)
  {
    setDateCreated(dateCreated);
    setShareable(isShareable);
    setReplicateCount(replicateCount);
  }

  
  // TODO: jps: I suggest this as the minimal constructor.
  /**
   * Constructs an initialized ScreenResult object.
   * @param dateCreated
   */
  public ScreenResult(Date dateCreated)
  {
    setDateCreated(dateCreated);
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.model.AbstractEntity#getEntityId()
   */
  public Integer getEntityId()
  {
    return getScreenResultId();
  }
  
  /**
   * Get a unique identifier for the <code>ScreenResult</code>.
   * 
   * @return an Integer representing a unique identifier for the
   *         <code>ScreenResult</code>
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="screen_result_id_seq"
   */
  public Integer getScreenResultId()
  {
    return _screenResultId;
  }

  /**
   * Set the unique identifier for the <code>ScreenResult</code>.
   * 
   * @param screenResultId a unique identifier for the <code>ScreenResult</code>
   */
  public void setScreenResultId(Integer screenResultId)
  {
    _screenResultId = screenResultId;
  }

  /**
   * Get the date this <code>ScreenResult</code> was generated in the lab.
   * 
   * @return returns a {@link java.util.Date} representing the date this
   *         <code>ScreenResult</code> was created
   * @hibernate.property type="date" not-null="true"
   */
  public Date getDateCreated()
  {
    return _dateCreated;
  }
  
  /**
   * Set the date this <code>ScreenResult</code> was generated in the lab.
   * 
   * @param dateCreated the date this <code>ScreenResult</code> was generated
   *          in the lab
   */
  public void setDateCreated(Date dateCreated)
  {
    _dateCreated = truncateDate(dateCreated);
  }

  /**
   * Get whether this <code>ScreenResult</code> can be viewed by all users of
   * the system; that is,
   * {@link edu.harvard.med.screensaver.model.users.ScreeningRoomUser}s other
   * than those associated with the
   * {@link edu.harvard.med.screensaver.screens.Screen}.
   * 
   * @return <code>true</code> iff this <code>ScreenResult</code> is
   *         shareable among all users
   * @hibernate.property column="is_shareable" not-null="true"
   */
  public boolean isShareable()
  {
    return _isShareable;
  }

  /**
   * Set the shareability of this <code>ScreenResult</code>.
   * 
   * @param isShareable whether this <code>ScreenResult</code> can be viewed
   *          by all users of the system; that is,
   *          {@link edu.harvard.med.screensaver.model.users.ScreeningRoomUser}s
   *          other than those associated with the
   *          {@link edu.harvard.med.screensaver.screens.Screen}
   */
  public void setShareable(boolean isShareable)
  {
    _isShareable = isShareable;
  }

  /**
   * Get a ordered set of all {@link ResultValueType}s for this
   * <code>ScreenResult</code>.
   * 
   * @return an unmodifiable {@link java.util.SortedSet} of all
   *         {@link ResultValueType}s for this <code>ScreenResult</code>.
   */
  public SortedSet<ResultValueType> getResultValueTypes()
  {
    return Collections.unmodifiableSortedSet(_resultValueTypes);
  }
  
  /**
   * Add the result value type to the screen result.
   * @param resultValueType The result value type to add
   * @return true iff the result value type was not already in the screen result
   */
  public boolean addResultValueType(ResultValueType resultValueType)
  {
    assert !(_resultValueTypes.contains(resultValueType) ^ resultValueType.getScreenResult().equals(this)) :
      "assymetic screen result/result value type encountered";
    if (_resultValueTypes.add(resultValueType)) {
      resultValueType.setHbnScreenResult(this);
      return true;
    }
    return false;
  }
  
  /**
   * Get the number of replicates (assay plates) associated with this
   * <code>ScreenResult</code>.
   * 
   * @return the number of replicates (assay plates) associated with this
   *         <code>ScreenResult</code>
   * @hibernate.property type="integer" not-null="true"
   */
  public Integer getReplicateCount()
  {
    if (_replicateCount == null) {
      if (getResultValueTypes().size() == 0) {
        _replicateCount = 0;
      } 
      else {
        ResultValueType maxOrdinalRvt = 
          Collections.max(getResultValueTypes(),
            new Comparator<ResultValueType>()
            {
              public int compare(ResultValueType rvt1, ResultValueType rvt2)
              {
                if (rvt1.getReplicateOrdinal() == null && rvt2.getReplicateOrdinal() == null) {
                  return 0;
                }
                if (rvt1.getReplicateOrdinal() == null && rvt2.getReplicateOrdinal() != null) {
                  return -1;
                }
                if (rvt1.getReplicateOrdinal() != null && rvt2.getReplicateOrdinal() == null) {
                  return 1;
                }
                return rvt1.getReplicateOrdinal().compareTo(rvt2.getReplicateOrdinal());
              }
            } );
        _replicateCount = maxOrdinalRvt.getReplicateOrdinal();
      }
    }
    return _replicateCount;
  }
  
  /**
   * Set the number of replicates (assay plates) associated with this
   * <code>ScreenResult</code>.
   * 
   * @param replicateCount the number of replicates (assay plates) associated
   *          with this <code>ScreenResult</code>
   */
  public void setReplicateCount(Integer replicateCount)
  {
    _replicateCount = replicateCount;
  }

  public SortedSet<Integer> getDerivedPlateNumbers()
  {
    if (_plateNumbers == null) {
      _plateNumbers = new TreeSet<Integer>();
      if (getResultValueTypes().size() > 0) {
        for (ResultValue rv : getResultValueTypes().first().getResultValues()) {
          _plateNumbers.add(rv.getWell().getPlateNumber());
        }
      }
    }
    return _plateNumbers;
  }

  // protected getters and setters
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.model.AbstractEntity#getBusinessKey()
   */
  protected Object getBusinessKey()
  {
    // TODO: replace with "return getScreen();" when it is implemented
    return DateFormat.getDateInstance().format(getDateCreated());
  }
  
  
  // package instance methods
  
  /**
   * Get a sorted set of all {@link ResultValueType}s for this
   * <code>ScreenResult</code>.
   * 
   * @motivation for Hibernate
   * @return an {@link java.util.SortedSet} of all {@link ResultValueType}s for
   *         this <code>ScreenResult</code>
   * @hibernate.set cascade="save-update" inverse="true" sort="natural"
   * @hibernate.collection-one-to-many class="edu.harvard.med.screensaver.model.screenresults.ResultValueType"
   * @hibernate.collection-key column="screen_result_id"
   */
  SortedSet<ResultValueType> getHbnResultValueTypes() {
    return _resultValueTypes;
  }


  // private getters and setters
  
  /**
   * Constructs an uninitialized <code>ScreenResult</code> object.
   * @motivation for Hibernate loading
   */
  private ScreenResult() {}

  /**
   * Get the version number of the compound.
   * 
   * @return the version number of the <code>ScreenResult</code>
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version number of the <code>ScreenResult</code>
   * 
   * @param version the new version number for the <code>ScreenResult</code>
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }
  
  /**
   * Set the sorted set of {@link ResultValueType}s that comprise this
   * <code>ScreenResult</code>.
   * 
   * @param resultValueTypes the {@link java.util.SortedSet} of
   *          {@link ResultValueType}s that comprise this
   *          <code>ScreenResult</code>.
   * @motivation for hibernate
   */
  private void setHbnResultValueTypes(SortedSet<ResultValueType> resultValueTypes) {
    _resultValueTypes = resultValueTypes;
  }

}
