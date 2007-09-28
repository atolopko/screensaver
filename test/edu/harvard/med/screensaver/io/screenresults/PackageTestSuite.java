// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/test/edu/harvard/med/screensaver/io/PackageTestSuite.java $
// $Id: PackageTestSuite.java 275 2006-06-28 15:32:40Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PackageTestSuite extends TestSuite
{

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    return new PackageTestSuite();
  }

  public PackageTestSuite() {
    addTestSuite(ScreenResultParserTest.class);
    addTestSuite(ScreenResultExporterTest.class);
    addTestSuite(ScreenResultPersistenceTest.class);
  }
  
}
