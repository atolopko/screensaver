// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column.entity;

import java.util.Comparator;
import java.util.Date;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.ui.table.column.ColumnType;
import edu.harvard.med.screensaver.util.NullSafeComparator;

public abstract class DateEntityColumn<E extends AbstractEntity> extends EntityColumn<E,Date>
{
  abstract protected Date getDate(E o);

  public DateEntityColumn(RelationshipPath<E> relationshipPath, String name, String description, String group)
  {
    super(relationshipPath, name, description, ColumnType.DATE, group);
  }

  public DateEntityColumn(PropertyPath<E> propertyPath, String name, String description, String group)
  {
    super(propertyPath, name, description, ColumnType.DATE, group);
  }

  @Override
  public Date getCellValue(E o)
  {
    return getDate(o);
  }
  @Override
  protected Comparator<E> getAscendingComparator()
  {
    return new NullSafeComparator<E>() {
      @Override
      protected int doCompare(E o1, E o2) { return getDate(o1).compareTo(getDate(o2)); }
    };
  }

}