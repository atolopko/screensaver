// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

public enum AssayPlateStatus {
  PLANNED, // expecting to create this assay plate for a screen, but does not physically exist
  PLATED, // physically exists
  SCREENED,
  DATA_LOADED,
  DATA_ANALYZED
}
