// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

/**
 * A hibernate entity representing an RNAi cherry pick request.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="cherryPickRequestId")
@org.hibernate.annotations.ForeignKey(name="fk_rnai_cherry_pick_request_to_cherry_pick_request")
@org.hibernate.annotations.Proxy
public class RNAiCherryPickRequest extends CherryPickRequest
{
  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(RNAiCherryPickRequest.class);
  /* Currently (2007-04-20), all RNAi cherry pick assay plates use EPPENDORF plate types. */
  public static final PlateType RNAI_CHERRY_PICK_ASSAY_PLATE_TYPE = PlateType.EPPENDORF;
  private static final Volume DEFAULT_TRANSFER_VOLUME = null;
  
  
  /**
   * Construct an initialized <code>RNAiCherryPickRequest</code>. Intended only for use
   * by {@link Screen}.
   * @param screen the screen
   * @param requestedBy the user that made the request
   * @param dateRequested the date created
   * @param legacyCherryPickRequestNumber the legacy ID from ScreenDB
   * @motivation for creating CherryPickRequests from legacy ScreenDB cherry pick visits
   */
  public RNAiCherryPickRequest(AdministratorUser createdBy,
                               Screen screen,
                               ScreeningRoomUser requestedBy,
                               LocalDate dateRequested)
  {
    super(createdBy, screen, requestedBy, dateRequested);
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public PlateType getDefaultAssayPlateType()
  {
    return RNAI_CHERRY_PICK_ASSAY_PLATE_TYPE;
  }
  
  @Override
  @Transient
  public Volume getDefaultTransferVolume()
  {
    return DEFAULT_TRANSFER_VOLUME;
  }

  /**
   * Construct an uninitialized <code>RNAiCherryPickRequest</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected RNAiCherryPickRequest() {}
}

