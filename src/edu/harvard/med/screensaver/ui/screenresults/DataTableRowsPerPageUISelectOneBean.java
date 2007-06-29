// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.log4j.Logger;

public class DataTableRowsPerPageUISelectOneBean extends UISelectOneBean<Integer>
{
  // static members

  private static final Integer SHOW_ALL_VALUE = -1;
  private static Logger log = Logger.getLogger(DataTableRowsPerPageUISelectOneBean.class);
  private Integer _allRowsValue;

  // instance data members
  
  public DataTableRowsPerPageUISelectOneBean(List<Integer> values)
  {
    super(values);
  }

  // public constructors and methods

  @Override
  public String getLabel(Integer value) 
  { 
    if (SHOW_ALL_VALUE.equals(value)) {
      return "All";
    }
    return super.getLabel(value);
  }
    
  @Override
  public Integer getSelection()
  {
    if (SHOW_ALL_VALUE.equals(super.getSelection())) {
      return _allRowsValue;
    }
    return super.getSelection();
  }
  
  @Override
  public List<SelectItem> getSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList<SelectItem>(super.getSelectItems());
    if (_allRowsValue == null && 
      selectItems.size() > 0 && SHOW_ALL_VALUE.equals(selectItems.get(selectItems.size() - 1))) {
      selectItems.remove(selectItems.size() - 1);
    }
    return selectItems;
  }

  public void setAllRowsValue(Integer allRowsValue)
  {
    _allRowsValue = allRowsValue;
  }

  // private methods

}
