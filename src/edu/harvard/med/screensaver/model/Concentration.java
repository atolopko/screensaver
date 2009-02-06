// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.math.BigDecimal;

/**
 * A Concentration, in either millimolar, micromolar or nanomolar units, with
 * 1-nanoliter resolution (maximum).
 */
public class Concentration extends Quantity<Concentration,ConcentrationUnit>
    {
  public Concentration(Integer value)
  {
    super(value, ConcentrationUnit.MILLIMOLAR);
  }

  public Concentration(Integer value, ConcentrationUnit units)
  {
    super(value.toString(), units);
  }

  public Concentration(Long value)
  {
    this(value, ConcentrationUnit.MILLIMOLAR);
  }

  public Concentration(Long value, ConcentrationUnit units)
  {
    super(value.toString(), units);
  }

  /**
   * Create a Concentration using a String representation of a decimal number, provided
   * in the specified units. If value has a fractional value, converts to units
   * that are small enough to hold the provided value as a whole number.
   * 
   * @param value
   * @param units
   */
  public Concentration(String value, ConcentrationUnit units)
  {
    super(new BigDecimal(value), units);
  }

  public Concentration(BigDecimal value, ConcentrationUnit units)
  {
    super(value, units);
  }

  @Override
  protected Concentration newQuantity(BigDecimal value,
                                      ConcentrationUnit unit)
  {
    return new Concentration(value, unit);
  }

}
