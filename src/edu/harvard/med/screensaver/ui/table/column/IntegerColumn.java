// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;


public abstract class IntegerColumn<R> extends TableColumn<R,Integer>
{
  public IntegerColumn(String name, String description, String group)
  {
    super(name, description, ColumnType.INTEGER, group);
  }
}
