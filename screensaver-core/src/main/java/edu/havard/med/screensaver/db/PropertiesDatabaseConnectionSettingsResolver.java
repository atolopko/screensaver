// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.havard.med.screensaver.db;

import java.util.Properties;

import edu.harvard.med.screensaver.DatabaseConnectionSettings;
import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolutionException;

public class PropertiesDatabaseConnectionSettingsResolver extends NamedVariablesDatabaseConnectionSettingsResolver
{
  private Properties _properties;

  public PropertiesDatabaseConnectionSettingsResolver()
  {
    super("database.host",
          "database.port",
          "database.name",
          "database.user",
          "database.password");
  }

  protected void setProperties(Properties properties)
  {
    _properties = properties;
  }

  @Override
  public DatabaseConnectionSettings resolve() throws DatabaseConnectionSettingsResolutionException
  {
    if (_properties == null) {
      throw new DatabaseConnectionSettingsResolutionException("resolver not initialized with properties");
    }
    if (_properties.get(databaseVariableName) == null) {
      return null;
    }
    String port = _properties.getProperty(portVariableName);
    Integer portNumber = null;
    try {
      if (port != null) {
        portNumber = Integer.parseInt(port);
      }
    }
    catch (NumberFormatException e) {
      throw new DatabaseConnectionSettingsResolutionException("invalid port number " + port);
    }
    return new DatabaseConnectionSettings(_properties.get(hostVariableName).toString(),
                                          portNumber,
                                          _properties.get(databaseVariableName).toString(),
                                          _properties.get(userVariableName).toString(),
                                          _properties.get(passwordVariableName).toString());
  }
}
