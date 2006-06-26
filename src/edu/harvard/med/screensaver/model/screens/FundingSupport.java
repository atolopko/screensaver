// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The funding support vocabulary.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum FundingSupport implements VocabularyTerm
{

  // the vocabulary
  
  CLARDY_GRANTS("Clardy Grants"),
  D_ANDREA_CMCR("D'Andrea CMCR"),
  ICCBL_HMS_INTERNAL("ICCB-L HMS Internal"),
  ICCBL_EXTERNAL("ICCB-L External"),
  MITCHISON_P01("Mitchison P01"),
  ICG("ICG"),
  NERCE_NSRB("NERCE/NSRB"),
  SANOFI_AVENTIS("Sanofi-Aventis"),
  YUAN_NIH_06_07("Yuan NIH 06-07"),
  OTHER("Other")
  ;

 
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link FundingSupport} vocabulary.
   */
  public static class UserType extends VocabularyUserType<FundingSupport>
  {
    public UserType()
    {
      super(FundingSupport.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>FundingSupport</code> vocabulary term.
   * @param value The value of the term.
   */
  private FundingSupport(String value)
  {
    _value = value;
  }


  // public instance methods

  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue()
  {
    return _value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return getValue();
  }
}
