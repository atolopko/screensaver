// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;

public class ScreenerCherryPickReagentEntityColumn<R extends Reagent,T> extends ReagentEntityColumn<ScreenerCherryPick,R,T> 
{
  
  public ScreenerCherryPickReagentEntityColumn(Class<R> reagentClass,
                                               TableColumn<R,T> delegateEntityColumn)
  {
    super(reagentClass, ScreenerCherryPick.screenedWell.to(Well.latestReleasedReagent), delegateEntityColumn);
  }

  @Override
  protected R getReagent(ScreenerCherryPick scp)
  {
    return scp.getScreenedWell().<R>getLatestReleasedReagent();
  }
}

