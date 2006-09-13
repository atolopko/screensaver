// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/ParseErrorManager.java $
// $Id: ParseErrorManager.java 275 2006-06-28 15:32:40Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Maintains a list of error messages.
 * @author ant
 */
public class SDFileParseErrorManager
{
  private static Logger log = Logger.getLogger(SDFileParseErrorManager.class);
  
  private List<SDFileParseError> _errors = new ArrayList<SDFileParseError>();
  
  /**
   * Add a simple error.
   * 
   * @param error the error
   */
  public void addError(String errorMessage)
  {
    SDFileParseError error = new SDFileParseError(errorMessage);
    _errors.add(error);
  }
  
  /**
   * Add an error, noting the file and record number associated with it.
   * 
   * @param error the error
   * @param sdFile the SDFile associated with the error
   * @param sdRecordNumber the SDFile record number associated with the error
   */
  public void addError(String errorMessage, File sdFile, int sdRecordNumber)
  {
    SDFileParseError error =
      new SDFileParseError(errorMessage, sdFile, sdRecordNumber);
    _errors.add(error);
  }
  
  /**
   * Get the list of <code>SDFileParseError</code> objects.
   * 
   * @return a list of <code>SDFileParseError</code> objects
   */
  public List<SDFileParseError> getErrors()
  {
    return _errors;
  }
  
  /**
   * @motivation jsp inspection
   * @return
   */
  public boolean getHasErrors()
  {
    return _errors.size() > 0;
  }
}
