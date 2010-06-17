// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.searchresults.EntitySearchResults;


// TODO: this class should be removed if/when we refactor the subclasses to have
// "detail" viewers for the editable parts; since we never actually *edit*
// within a search result context (we only view), this class doesn't really make
// sense
public abstract class SearchResultContextEditableEntityViewerBackingBean<E extends AbstractEntity,R> extends EditableEntityViewerBackingBean<E> implements SearchResultContextEntityViewer<E,R>
{

  private EntitySearchResults<E,R,?> _entitySearchResults;
  private String _browserActionResult;

  public SearchResultContextEditableEntityViewerBackingBean()
  {
  }

  public SearchResultContextEditableEntityViewerBackingBean(SearchResultContextEditableEntityViewerBackingBean<E,R> thisProxy,
                                                            Class<E> entityClass,
                                                            String browserActionResult,
                                                            String viewerActionResult,
                                                            GenericEntityDAO dao,
                                                            EntitySearchResults<E,R,?> entitySearchResults)
  {
    super(thisProxy, entityClass, viewerActionResult, dao);
    _entitySearchResults = entitySearchResults;
    _browserActionResult = browserActionResult;
  }
  
  public EntitySearchResults<E,R,?> getContextualSearchResults()
  {
    return _entitySearchResults;
  }

  @Transactional
  @UICommand
  @Override
  public String viewEntity(E entity)
  {
    if (_entitySearchResults.findEntity(entity)) {
      return _browserActionResult;
    }
    log.debug("entity " + entity + " is not a member of the current search results; entity will be viewed independently");
    return super.viewEntity(entity);
  }
  
}
