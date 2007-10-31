// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.ui.table.TableColumn;

public class AnnotationTypeColumn extends TableColumn<Reagent>
{
  private AnnotationType _annotationType;

  public AnnotationTypeColumn(AnnotationType annotationType)
  {
    super(annotationType.getName(),
          annotationType.getDescription(),
          annotationType.isNumeric());
    _annotationType = annotationType;
  }

  public AnnotationType getAnnotationType()
  {
    return _annotationType;
  }

  @Override
  public Object getCellValue(Reagent reagent)
  {
    AnnotationValue annotationValue = reagent.getAnnotationValue(_annotationType);

    return annotationValue == null ? null : annotationValue.getValue();
  }
}