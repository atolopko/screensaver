// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

public interface ScreensaverConstants
{
  public String APPLICATION_TITLE = "Screensaver 1.0";

  
  // JSF Action Results 
  // these values are returned by backing beans' action methods and are used to
  // define navigation rules in faces-config.xml
  
  public static final String REDISPLAY_PAGE_ACTION_RESULT = null;
  public static final String SUCCESS_ACTION_RESULT = "success";
  public static final String ERROR_ACTION_RESULT = "error";
  public static final String DONE_ACTION_RESULT = "done";
  public static final String GO_IMPORT_SCREEN_RESULT_ACTION_RESULT = "goImportScreenResult";
  
}
