// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.view.libraries;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;

public class WellSearchResultsViewer extends AbstractBackingBean
{
  private WellSearchResults _wellSearchResults;
  
  public WellSearchResults getWellSearchResults()
  {
    return _wellSearchResults;
  }

  public void setWellSearchResults(WellSearchResults wellSearchResults)
  {
    _wellSearchResults = wellSearchResults;
  }
}
