// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.util.EditableViewer;

public abstract class AbstractEditableBackingBean 
       extends AbstractBackingBean implements EditableViewer
{
  // static members

  private static Logger log = Logger.getLogger(AbstractEditableBackingBean.class);

  private ScreensaverUserRole _editableAdminRole;

  public AbstractEditableBackingBean(ScreensaverUserRole editableAdminRole)
  {
    _editableAdminRole = editableAdminRole;
  }
  
  /**
   * @motivation this is for the spring library (CGLIB2)
   */
  protected AbstractEditableBackingBean()
  {
  }
  
  public ScreensaverUserRole getEditableAdminRole()
  {
    return _editableAdminRole;
  }

  public boolean isEditMode()
  {
    return false;
  }

  public boolean isReadOnly()
  {
    if (_editableAdminRole == null) {
      return true;
    }
    return !isUserInRole(_editableAdminRole);
  }

  public boolean isEditable()
  {
    return !isReadOnly();
  }
}