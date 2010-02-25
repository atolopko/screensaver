// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.policy.EntityEditPolicy;
import edu.harvard.med.screensaver.ui.searchresults.EntityUpdateSearchResults;
import edu.harvard.med.screensaver.ui.util.EditableEntityViewer;
import edu.harvard.med.screensaver.util.NullSafeUtils;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public abstract class EditableEntityViewerBackingBean<E extends Entity> extends EntityViewerBackingBean<E> implements EditableEntityViewer<E>
{
  protected static Logger log = Logger.getLogger(EditableEntityViewerBackingBean.class);
  
  private EntityEditPolicy _entityEditPolicy;
  private boolean _isEditMode;
  private String _updateComments;


  
  public EditableEntityViewerBackingBean(EditableEntityViewerBackingBean<E> thisProxy,
                                         Class<E> entityClass,
                                         String viewerActionResult,
                                         GenericEntityDAO dao)
  {
    super(thisProxy, entityClass, viewerActionResult, dao);
    getIsPanelCollapsedMap().put("updateHistory", true);
  }
  
  /**
   * @motivation this is for the spring library (CGLIB2)
   */
  protected EditableEntityViewerBackingBean()
  {
  }
  
  abstract protected String postEditAction(EditResult editResult);

  public EditableEntityViewerBackingBean<E> getThisProxy()
  {
    return (EditableEntityViewerBackingBean<E>) super.getThisProxy();
  }
  
  public EntityEditPolicy getEntityEditPolicy()
  {
    return _entityEditPolicy;
  }

  public void setEntityEditPolicy(EntityEditPolicy entityEditPolicy)
  {
    _entityEditPolicy = entityEditPolicy;
  }

  @Transactional
  public void setEntity(E entity)
  {
    _isEditMode = false;
    super.setEntity(entity);
  }

  protected void recordUpdateActivity(String updateMessage)
  {
    E entity = getEntity();
    if (entity instanceof AuditedAbstractEntity) {
      AdministratorUser admin = (AdministratorUser) getCurrentScreensaverUser().getScreensaverUser();
      admin = getDao().reloadEntity(admin, false, ScreensaverUser.activitiesPerformed.getPath());
      ((AuditedAbstractEntity) entity).createUpdateActivity(admin, updateMessage);
    }
  }
  
  protected void recordUpdateActivity()
  {
    recordUpdateActivity("saved" + (getUpdateComments() == null ? "" : (": " + getUpdateComments())));
    _updateComments = null;
  }
  
  public String getUpdateComments()
  {
    return _updateComments;
  }

  public void setUpdateComments(String updateComments)
  {
    _updateComments = updateComments;
  }
  
  public EntityUpdateSearchResults getEntityUpdateSearchResults()
  {
    return null;
  }

  public boolean isEditMode()
  {
    return _isEditMode;
  }
  
  protected void setEditMode(boolean isEditMode)
  {
    _isEditMode = isEditMode;
  }

  public boolean isReadOnly()
  {
    if (_entityEditPolicy == null || getEntity() == null) {
      return true;
    }
    return !!!((Boolean) getEntity().acceptVisitor(_entityEditPolicy)).booleanValue();
  }

  public boolean isEditable()
  {
    return !isReadOnly();
  }
  
  public boolean isDeleteSupported()
  {
    return false;
  }
  
  @UICommand
  /*final*/ public String cancel()
  {
    setEditMode(false);
    if (getEntity() == null || getEntity().isTransient()) {
      String editAction = postEditAction(EditResult.CANCEL_NEW);
      _setEntity(null); // no longer a valid entity, since new edit was canceled (note that we do this after calling postEditAction() in case the method implementation needs to inspect the current entity.
      return editAction;

    }
    return postEditAction(EditResult.CANCEL_EDIT);
  }

  @UICommand
  /*final*/ public String edit()
  {
    setEditMode(true);
    return view();
  }
  
  @UICommand
  
  @Transactional(/*TODO: this appears to be causing problems on entity reattachment in initializeNewEntity() method implementations: propagation=Propagation.REQUIRES_NEW,*/ readOnly=true) // be defensive: prevent saving entity before user invokes "save" command
  /*final*/ public String editNewEntity(E newEntity)
  {
    if (_entityEditPolicy == null || newEntity == null ||
      !!!((Boolean) newEntity.acceptVisitor(_entityEditPolicy)).booleanValue()) {
      showMessage("restrictedOperation", "add new " + newEntity.getClass().getSimpleName());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    
    _setEntity(null); // prevent initializeViewer() implementations from erroneously calling getEntity()
    initializeNewEntity(newEntity);
    initializeViewer(newEntity);
    _setEntity(newEntity);
    setEditMode(true);
    return view();
  }

  /**
   * Subclasses should override this method if property values need to be set to
   * defaults.
   */
  protected void initializeNewEntity(E entity) 
  {
  }

  @UICommand
  @Transactional
  /*final*/ public String save()
  {
    EditResult editResult = getEntity().isTransient() ? EditResult.SAVE_NEW : EditResult.SAVE_EDIT;
    if (!validateEntity(getEntity())) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    setEditMode(false); // leave edit mode only after validation occurs, in order to remain in edit mode if validation failed 
    getDao().saveOrUpdateEntity(getEntity());
    updateEntityProperties(getEntity());
    recordUpdateActivity();
    getDao().flush();
    getDao().clear(); // forces entity to be reloaded from db in view mode; this ensures that newly created entities have dataAccessPolicy injected; also, any problems with persistence will be caught immediately after save during UI testing
    return postEditAction(editResult);
  }

  /**
   * Subclasses should override this method to perform validation of the entity
   * prior to saving it. The entity may be transient (when creating new
   * entities) or detached (when updating existing entities).
   * 
   * @param entity
   * @return true if entity state is valid, otherwise false
   */
  protected boolean validateEntity(E entity)
  {
    return true;
  }

  /**
   * Subclasses should override this method if there are entity properties that
   * need to be updated from UI components that were not directly bound to the
   * entity properties. This method will be called within a transaction. The
   * specified entity will be managed by the Hibernate session and will reflect
   * the new value of all properties bound to the UI view. The method cannot
   * perform validations on data at this point (it is too late to do so!); use
   * {@link #validateEntity(Entity)}.
   * 
   * @param entity
   */
  protected void updateEntityProperties(E entity)
  {
  }

  @UICommand
  @Transactional
  /**
   * @motivation provide a convenient default implementation as delete support is off by default
   */
  public String delete()
  {
    return null;
  }
  
  public void showFieldInputError(String fieldName, String message)
  {
    showMessage("invalidUserInput", fieldName, NullSafeUtils.toString(message, ""));
    // TODO: fix
//    showMessageForLocalComponentId(getFacesContext(),
//                                   fieldName,
//                                   "invalidUserInput",
//                                   NullSafeUtils.toString(message, ""));
  }
}