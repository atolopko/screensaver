// $HeadURL:
// http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.3.2-dev/core/src/main/java/edu/harvard/med/iccbl/screensaver/policy/IccblEntityViewPolicy.java
// $
// $Id: IccblEntityViewPolicy.java 5551 2011-03-24 19:39:31Z seanderickson1 $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.lincs.screensaver.policy;

import org.apache.log4j.Logger;

import edu.harvard.med.iccbl.screensaver.policy.SequenceRestrictedSilencingReagent;
import edu.harvard.med.iccbl.screensaver.policy.StructureRestrictedSmallMoleculeReagent;
import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.DownloadRestrictedAttachedFile;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.policy.DefaultEntityViewPolicy;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;
import edu.harvard.med.screensaver.ui.WebCurrentScreensaverUser;

/**
 * An EntityViewPolicy implementation for LINCS that is used by the production web application.
 */
public class LincsEntityViewPolicy extends DefaultEntityViewPolicy
{
  private static Logger log = Logger.getLogger(LincsEntityViewPolicy.class);

  private CurrentScreensaverUser _currentScreensaverUser;
  private ScreensaverUser _screensaverUser;

  protected LincsEntityViewPolicy()
  {}

  public LincsEntityViewPolicy(CurrentScreensaverUser user)
  {
    _currentScreensaverUser = user;
  }

  /**
   * @motivation for unit tests
   */
  public LincsEntityViewPolicy(ScreensaverUser user)
  {
    _screensaverUser = user;
  }

  public ScreensaverUser getScreensaverUser()
  {
    if (_screensaverUser == null) {
      _screensaverUser = _currentScreensaverUser.getScreensaverUser();
    }
    return _screensaverUser;
  }

  public SilencingReagent visit(SilencingReagent entity)
  {
    if (isGuestUser() && entity.isRestrictedSequence()) {
      SequenceRestrictedSilencingReagent sequenceRestrictedSilencingReagent = new SequenceRestrictedSilencingReagent(entity);
      if (log.isDebugEnabled()) {
        log.debug("returning sequence-restricted silencing reagent: " + sequenceRestrictedSilencingReagent);
      }
      return sequenceRestrictedSilencingReagent;
    }
    return entity;
  }

  public SmallMoleculeReagent visit(SmallMoleculeReagent entity)
  {
    if (isGuestUser() && entity.isRestrictedStructure()) {
      StructureRestrictedSmallMoleculeReagent structureRestrictedSmallMoleculeReagent = new StructureRestrictedSmallMoleculeReagent(entity);
      if (log.isDebugEnabled()) {
        log.debug("returning structure-restricted small molecule reagent: " + structureRestrictedSmallMoleculeReagent);
      }
      return structureRestrictedSmallMoleculeReagent;
    }
    return entity;
  }

  private boolean isGuestUser()
  {
    return _currentScreensaverUser.getScreensaverUser() instanceof WebCurrentScreensaverUser.GuestUser;
  }

  @Override
  public AttachedFile visit(AttachedFile entity)
  {
    if (isGuestUser() && !!! entity.getFileType().getValue().equals(ScreensaverConstants.STUDY_FILE_TYPE)) {
      if (log.isDebugEnabled()) {
        log.info("returning download restricted attached file: " + entity);
      }
      return new DownloadRestrictedAttachedFile(entity);
    }
    return entity;
  }

}