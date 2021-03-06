// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.cellhts2;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

// BII (Siew Cheng): Implement VocabularyTerm
// TODO: no need to implement VocabularyTerm, as this is not a persisted type
public enum SummarizeReplicatesMethod implements VocabularyTerm
{
  // the vocabulary
  //TODO determine the possible values 
	
  MEAN("mean");

  // BII (Siew Cheng) start: implement VocabularyTerm
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link SummarizeReplicatesMethod} vocabulary.
   */
  public static class UserType extends VocabularyUserType<SummarizeReplicatesMethod>
  {
    public UserType()
    {
      super(SummarizeReplicatesMethod.values());
    }
  }

  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>SummarizeReplicatesMethod</code> vocabulary term.
   * @param value The value of the term.
   */
  private SummarizeReplicatesMethod(String value)
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
  // BII end
}