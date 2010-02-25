// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/data-sharing-levels/src/edu/harvard/med/screensaver/model/users/ScreeningRoomUserClassification.java $
// $Id: ScreeningRoomUserClassification.java 2536 2008-07-02 22:11:39Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

import com.google.common.base.Function;

/**
 * Vocabulary for tracking how facility users are using the facility.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum FacilityUsageRole implements VocabularyTerm
{
  SMALL_MOLECULE_SCREENER("smallMoleculeScreener", "Small Molecule Screener", "Small molecule screeners that are conducting small molecule screens at the facility."),
  RNAI_SCREENER("rnaiScreener", "RNAi Screener", "Users that are conducting RNAi screens at the facility."),
  // note: nonScreeningUser is *not* mutually exclusive with screener roles; user may have been a nonScreeningUser initially, then became screener later on
  NON_SCREENER("nonScreeningUser", "Non-screening User", "Users that are using the facility for purposes other than conducting a screen."),
  MEDICINAL_CHEMIST_USER("medicinalChemistUser", "Medicinal Chemist User", "Users that are medicinal chemists."),
  QPCR_USER("qpcrUser", "QPRC User", "Users that are performing Quantitative PCR analyses at the facility.");

  public static Function<FacilityUsageRole,String> ToDisplayableName = new Function<FacilityUsageRole,String>() {
    public String apply(FacilityUsageRole role)
    {
      return role.getDisplayableName();
    }
  }; 
  
  /**
   * A Hibernate <code>UserType</code> to map the {@link FacilityUsageRole} vocabulary.
   */
  public static class UserType extends VocabularyUserType<FacilityUsageRole>
  {
    public UserType()
    {
      super(FacilityUsageRole.values());
    }
  }

  private String _value;
  private String _displayableName;
  private String _description;
  
  /**
   * Constructs a <code>ScreeningRoomUserClassification</code> vocabulary term.
   * @param value The value of the term.
   */
  private FacilityUsageRole(String value, String displayableName, String description)
  {
    _value = value;
    _displayableName = displayableName;
    _description = description;
  }

  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue()
  {
    return _value;
  }

  public String getDisplayableName()
  {
    return _displayableName;
  }


  public String getDescription()
  {
    return _description;
  }

  @Override
  public String toString()
  {
    return getValue();
  }
}
