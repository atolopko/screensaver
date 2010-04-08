// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

/**
 * All the different types for the content in the right-hand column (the value column) of a
 * {@link NameValueTable}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum ValueType {
  TEXT,
  UNESCAPED_TEXT,
  COMMAND,
  LINK,
  IMAGE,
  TEXT_LIST,
  LINK_LIST,
}

