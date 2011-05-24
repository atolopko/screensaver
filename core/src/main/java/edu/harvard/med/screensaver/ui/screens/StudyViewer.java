// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.screens.StudyImageProvider;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.view.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResults;


public class StudyViewer<E extends Study> extends SearchResultContextEntityViewerBackingBean<E,E>
{
  private static Logger log = Logger.getLogger(StudyViewer.class);

  private StudyDetailViewer _studyDetailViewer;
  private AnnotationTypesTable _annotationTypesTable;
  private WellSearchResults _wellsBrowser;
  private StudyImageProvider _studyImageProvider; // LINCS-only feature


  /**
   * @motivation for CGLIB2
   */
  protected StudyViewer()
  {
  }

  public StudyViewer(StudyViewer thisProxy,
                     StudyDetailViewer studyDetailViewer,
                     StudySearchResults studiesBrowser,
                     GenericEntityDAO dao,
                     AnnotationTypesTable annotationTypesTable,
                     WellSearchResults wellsBrowser,
                     StudyImageProvider studyImageProvider)
  {
    super(thisProxy,
          (Class<E>) Study.class,
          ScreensaverConstants.BROWSE_STUDIES,
          ScreensaverConstants.VIEW_STUDY,
          dao,
          (EntitySearchResults<E,E,?>) studiesBrowser);
    _studyDetailViewer = studyDetailViewer;
    _annotationTypesTable = annotationTypesTable;
    _wellsBrowser = wellsBrowser;
    _studyImageProvider = studyImageProvider;

    getIsPanelCollapsedMap().put("reagentsData", false);
  }

  protected StudyViewer(Class<E> entityClass,
                        StudyViewer thisProxy,
                        EntitySearchResults<E,E,?> studiesBrowser,
                        String browserActionResult,
                        String viewerActionResult,
                        GenericEntityDAO dao,
                        AnnotationTypesTable annotationTypesTable,
                        WellSearchResults wellSearchResults)
  {
    super(thisProxy,
          entityClass,
          browserActionResult,
          viewerActionResult,
          dao,
          (EntitySearchResults<E,E,?>) studiesBrowser);
    _annotationTypesTable = annotationTypesTable;
    _wellsBrowser = wellSearchResults;

    getIsPanelCollapsedMap().put("reagentsData", false);
  }

  public WellSearchResults getWellsBrowser()
  {
    return _wellsBrowser;
  }

  public AnnotationTypesTable getAnnotationTypesTable()
  {
    return _annotationTypesTable;
  }

  public String getStudyImageUrl()
  {
    if (_studyImageProvider == null) return null;

    URL url = _studyImageProvider.getImageUrl((Screen) getEntity());
    return url == null ? null : url.toString();
  }

  @Override
  protected void initializeEntity(E study)
  {
    getDao().needReadOnly((Screen) study, Screen.labHead.to(LabHead.labMembers));
    getDao().needReadOnly((Screen) study, Screen.leadScreener);
    getDao().needReadOnly((Screen) study, Screen.collaborators);
    getDao().needReadOnly((Screen) study, Screen.publications);
    getDao().needReadOnly((Screen) study, Screen.annotationTypes);
  }

  @Override
  protected void initializeViewer(E study)
  {
    if (study.isStudyOnly()) {
      _annotationTypesTable.initialize(new ArrayList<AnnotationType>(study.getAnnotationTypes()));
      _wellsBrowser.searchReagentsForStudy(study);
      _studyDetailViewer.setEntity(study);
    }
  }
}

