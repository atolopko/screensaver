// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook2;

/**
 * Exception for reporting attempt to read a cell that is outside of the range
 * of valid cells in an <code>HSSFSheet</code> worksheet.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CellOutOfRangeException extends Exception
{
  private static final long serialVersionUID = 1;

  /**
   * Constructs a CellOutOfRangeException exception object.
   * 
   * @param cell the cell that was undefined
   */
  public CellOutOfRangeException(Cell cell)
  {
    super("cell " + cell + " undefined");
  }
}
