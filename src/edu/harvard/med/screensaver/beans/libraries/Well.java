// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.beans.libraries;

import java.util.HashSet;
import java.util.Set;


/**
 * A Hibernate entity bean representing a well.
 * 
 * @author john sullivan
 * @hibernate.class
 */
public class Well {

  
  // instance fields
  
  private Integer       _wellId;
  private Integer       _version;
  private Library       _library;
  private Set<Compound> _compounds = new HashSet<Compound>();
  private Integer       _plateNumber;
  private String        _wellName;
  private String        _iccbNumber;
  private String        _vendorIdentifier;
  

  // constructors
  
  /**
   * Constructs an uninitialized <code>Well</code> object.
   * @motivation for Hibernate loading
   */
  protected Well() {}
  
  /**
   * Constructs an initialized Well object.
   */
  public Well(String wellName,
              Library parentLibrary,
              int plateNumber) {
    _wellName = wellName;
    _plateNumber = new Integer(plateNumber);
    // this call must occur after assignments of wellName and plateNumber (to
    // ensure hashCode() works)
    addToLibrary(parentLibrary);
  }
  

  // public getters and setters
  
  /**
   * Get the well id for the well.
   * @return the well id for the well
   *
   * @hibernate.id
   *   generator-class="sequence"
   * @hibernate.generator-param
   *   name="sequence"
   *   value="well_id_seq"
   */
  public Integer getWellId() {
    return _wellId;
  }

  /**
   * Get the library the well is in.
   * @return the library the well is in
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.beans.libraries.Library"
   *   column="library_id"
   *   not-null="true"
   *   foreign-key="fk_well_to_library"
   */
  public Library getLibrary() {
    return _library;
  }

  /**
   * Set the library the well is in.
   * @param library the new library for the well
   */
  public void addToLibrary(Library library) {
    assert _wellName != null && _plateNumber != null : "properties forming business key have not been defined";
    library.getModifiableWellSet().add(this);
    if (getLibrary() != null) {
      getLibrary().getModifiableWellSet().remove(this);
    }
    setLibrary(library);
  }
  
  /**
   * Set the library the well is in.
   * @param library the new library for the well
   * @motivation for Hibernate (exclusively)
   */
  protected void setLibrary(Library library) {
    _library = library;
  }

  /**
   * Get the set of compounds contained in the well.
   * @return the set of compounds contained in the well
   *
   * @hibernate.set
   *   table="well_compound_link"
   *   cascade="all"
   * @hibernate.collection-key
   *   column="well_id"
   * @hibernate.collection-many-to-many
   *   column="compound_id"
   *   class="edu.harvard.med.screensaver.beans.libraries.Compound"
   *   foreign-key="fk_well_compound_link_to_well"
   */
  public Set<Compound> getCompounds() {
    // TODO: unmodifiableSet causing problems w/Hibernate, figure out whether to reinstate
    return /*Collections.unmodifiableSet*/(_compounds);
  }

  /**
   * Add the compound to the well.
   * @param compound the compound to add to the well
   * @return         true iff the compound was not already in the well
   */
  public boolean addCompound(Compound compound) {
    assert !(getCompounds().contains(compound) ^ compound.getWells().contains(this)) :
      "asymmetric compound/well association encountered";
    if (getModifiableCompoundSet().add(compound)) {
      return compound.getModifiableWellSet().add(this);
    }
    return false;
  }

  /**
   * Remove the compound from the well.
   * @param compound the compound to remove from the well
   * @return         true iff the compound was previously in the well
   */
  public boolean removeCompound(Compound compound) {
    assert !(getCompounds().contains(compound) ^ compound.getWells().contains(this)) :
      "asymmetric compound/well association encountered";
    if (getModifiableCompoundSet().remove(compound)) {
      return compound.getModifiableWellSet().remove(this);
    }
    return false;
  }
  
  /**
   * Get the plate number for the well.
   * @return the plate number for the well
   *
   * @hibernate.property
   *   not-null="true"
   */
  public Integer getPlateNumber() {
    return _plateNumber;
  }

  /**
   * Set the plate number for the well.
   * @param plateNumber the new plate number for the well
   */
  public void setPlateNumber(Integer plateNumber) {
    _plateNumber = plateNumber;
  }

  /**
   * Get the well name for the well.
   * @return the well name for the well
   *
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getWellName() {
    return _wellName;
  }

  /**
   * Set the well name for the well.
   * @param wellName the new well name for the well
   */
  public void setWellName(String wellName) {
    _wellName = wellName;
  }

  /**
   * Get the ICCB number for the well.
   * @return the ICCB number for the well
   *
   * @hibernate.property
   *   type="text"
   */
  public String getIccbNumber() {
    return _iccbNumber;
  }
  
  /**
   * Set the ICCB number for the well.
   * @param iccbNumber The new ICCB number for the well
   */
  public void setIccbNumber(String iccbNumber) {
    _iccbNumber = iccbNumber;
  }
  
  /**
   * Get the vendor identifier for the well.
   * @return the vendor identifier for the well
   *
   * @hibernate.property
   *   type="text"
   */
  public String getVendorIdentifier() {
    return _vendorIdentifier;
  }
  
  /**
   * Set the vendor identifier for the well.
   * @param vendorIdentifier the new vendor identifier for the well
   */
  public void setVendorIdentifier(String vendorIdentifier) {
    _vendorIdentifier = vendorIdentifier;
  }

  
  // identity methods
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object object) {
    if (! (object instanceof Well)) {
      return false;
    }
    Well that = (Well) object;
    return
      this.getPlateNumber().equals(that.getPlateNumber()) &&
      this.getWellName().equals(that.getWellName());
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getPlateNumber().hashCode() + getWellName().hashCode();
  }

  
  // protected getters and setters
  
  /**
   * Get the modifiable set of compounds.
   * @return the modifiable set of compounds
   * @motivation allow efficient maintenance of the bi-directional relationship
   *             between {@link Compound} and {@link Well}.
   */
  protected Set<Compound> getModifiableCompoundSet() {
    return _compounds;
  }

  
  // private getters and setters
  
  /**
   * Set the well id for the well.
   * @param wellId the new well id for the well
   * @motivation   for hibernate
   */
  private void setWellId(Integer wellId) {
    _wellId = wellId;
  }

  /**
   * Get the version of the well.
   * @return     the version of the well
   * @motivation for hibernate
   *
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version of the well.
   * @param version the new version of the well
   * @motivation    for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Set the set of compounds contained in the well.
   * @param compounds the new set of compounds contained in the well
   * @motivation      for hibernate
   */
  private void setCompounds(Set<Compound> compounds) {
    _compounds = compounds;
  }  
}
