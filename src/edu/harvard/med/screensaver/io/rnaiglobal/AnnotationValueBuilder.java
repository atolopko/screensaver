// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.rnaiglobal;

import java.math.BigDecimal;

import jxl.Cell;

import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;

class AnnotationValueBuilder
{
  private int _sourceColumnIndex;
  private AnnotationType _annotationType;

  public AnnotationValueBuilder(int sourceColumnIndex,
                               AnnotationType annotationType)
  {
    _sourceColumnIndex = sourceColumnIndex;
    _annotationType = annotationType;
  }

  public AnnotationValue buildAnnotationValue(Cell[] row)
  {
    String value = transformValue(row[_sourceColumnIndex].getContents());
    if (_annotationType.isNumeric()) {
      return _annotationType.addAnnotationValue(row[0].getContents(), null, new BigDecimal(value));
    }
    else {
      return _annotationType.addAnnotationValue(row[0].getContents(), value, null);
    }
  }

  public AnnotationType getAnnotationType()
  {
    return _annotationType;
  }

  public String transformValue(String value)
  {
    return value;
  }
}