// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.io.PrintWriter;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

public class ScreenResultPrinter
{
  private ScreenResult _screenResult;

  public ScreenResultPrinter(ScreenResult screenResult)
  {
    _screenResult = screenResult;
  }

  public void print()
  {
    print(null);
  }

  public void print(Integer maxResultValuesToPrint)
  {
    PrintWriter printer = new PrintWriter(System.out);
    if (_screenResult == null) {
      printer.println("ScreenResult was not parsed (probably due to an invalid input file)");
      return;
    }

    printer.println("dateCreated=" + _screenResult.getDateCreated());
    printer.println("replicateCount=" + _screenResult.getReplicateCount());
    printer.println("dataHeaderCount=" + _screenResult.getResultValueTypes().size());

    for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
      printer.println("Result Value Type:");
      printer.println("\tordinal=" + rvt.getOrdinal());
      printer.println("\tname=" + rvt.getName());
      printer.println("\tdataType=" + rvt.getDataType());
      printer.println("\tdecimalPlaces=" + rvt.getDecimalPlaces());
      printer.println("\tdescription="+rvt.getDescription());
      printer.println("\tisNumeric=" + rvt.isNumeric());
      printer.println("\treplicateOrdinal=" + rvt.getReplicateOrdinal());
      printer.println("\ttimePoint=" + rvt.getTimePoint());
      printer.println("\tassayPhenotype=" + rvt.getAssayPhenotype());
      printer.println("\tassayReadoutType=" + rvt.getAssayReadoutType());
      printer.println("\tisDerived=" + rvt.isDerived());
      printer.println("\tderivedFrom="+rvt.getTypesDerivedFrom()); // TODO
      printer.println("\thowDerived=" + rvt.getHowDerived());
      printer.println("\tisActivityIndicator=" + rvt.isPositiveIndicator());
      printer.println("\tisFollowupData=" + rvt.isFollowUpData());
      printer.println("\tcomments="+rvt.getComments());

      int nResultValues = rvt.getResultValues().size();
      printer.println("\tResult Values: (" + nResultValues + ")");
      int n = 0;
      boolean ellipsesOnce = false;

      //TODO: reload the rvt's for the wells here because we are clearing them during parse now - sde4
      for (WellKey wellKey : new TreeSet<WellKey>(rvt.getWellKeyToResultValueMap().keySet())) {
        if (maxResultValuesToPrint != null) {
          if (n < maxResultValuesToPrint / 2 || n >= nResultValues - maxResultValuesToPrint / 2) {
            ResultValue resultValue = rvt.getWellKeyToResultValueMap().get(wellKey);
            printer.println("\t\t" + wellKey + "\t" + resultValue.getTypedValue());
          }
          else if (!ellipsesOnce) {
            printer.println("\t\t...");
            ellipsesOnce = true;
          }
        }
        ++n;
      }
    }
    //Note: don't do this as it closes the output stream for other services like log4j: printer.close();
  }
}
