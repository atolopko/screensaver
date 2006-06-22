// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

/**
 * Used to report an unrecoverable parse error; indicates that further parsing
 * is not possible; causes parsing to abort immediately.
 * 
 * @author ant
 */
public class UnrecoverableScreenResultParseException extends Exception
{

  private Workbook _workbook;
  private Cell _cell;
  
  /**
   * 
   */
  private static final long serialVersionUID = 5285861320482270566L;
  
  public UnrecoverableScreenResultParseException(String message, Workbook workbook)
  {
    super(message + " in " + workbook);
    _workbook = workbook;
  }

  public UnrecoverableScreenResultParseException(String message, Cell cell)
  {
    super(message + " @ " + cell);
    _cell = cell;
  }
  
  public Workbook getWorkbook()
  {
    if (_workbook != null) {
      return _workbook;
    }
    if (_cell != null) {
      return _cell.getWorkbook();
    }
    return null;
  }

}
