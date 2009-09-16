// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.accesspolicy.DataAccessPolicy;
import edu.harvard.med.screensaver.io.libraries.WellsSdfDataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.EntityViewer;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.namevaluetable.NameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.WellNameValueTable;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;
import edu.harvard.med.screensaver.ui.searchresults.AnnotationHeaderColumn;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.table.SimpleCell;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class WellViewer extends AbstractBackingBean implements EntityViewer
{
  
  private static final Logger log = Logger.getLogger(WellViewer.class);

  // HACK: for special case message
  private static final String SPECIAL_CHEMDIV6_LIBRARY_NAME = "ChemDiv6";

  private WellViewer _thisProxy;
  private LibraryViewer _libraryViewer;
  private Well _well;
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private DataAccessPolicy _dataAccessPolicy;
  private StructureImageProvider _structureImageProvider;
  private NameValueTable _nameValueTable;
  private NameValueTable _annotationNameValueTable;
  private StudyViewer _studyViewer;
  private WellSearchResults _wellSearchResults;
  private WellsSdfDataExporter _wellsSdfDataExporter;

  private DataModel _otherWellsDataModel;
  private DataModel _duplexWellsDataModel;
  private Map<String,Boolean> _isPanelCollapsedMap;
  private Reagent _versionedReagent;


  
  /**
   * @motivation for CGLIB2
   */
  protected WellViewer() {}

  public WellViewer(WellViewer thisProxy,
                    GenericEntityDAO dao,
                    LibrariesDAO librariesDAO,
                    DataAccessPolicy dataAccessPolicy,
                    LibraryViewer libraryViewer,
                    StructureImageProvider structureImageProvider,
                    StudyViewer studyViewer,
                    WellSearchResults wellSearchResult,
                    WellsSdfDataExporter wellsSdfDataExporter)
  {
    _thisProxy = thisProxy;
    _dao = dao;
    _librariesDao = librariesDAO;
    _dataAccessPolicy = dataAccessPolicy;
    _libraryViewer = libraryViewer;
    _structureImageProvider = structureImageProvider;
    _studyViewer = studyViewer;
    _wellSearchResults = wellSearchResult;
    _wellsSdfDataExporter = wellsSdfDataExporter;
    _isPanelCollapsedMap = Maps.newHashMap();
    _isPanelCollapsedMap.put("otherWells", Boolean.TRUE);
    _isPanelCollapsedMap.put("duplexWells", Boolean.TRUE);
    _isPanelCollapsedMap.put("annotations", Boolean.TRUE);
  }

  public AbstractEntity getEntity()
  {
    return getWell();
  }

  public Well getWell()
  {
    return _well;
  }

  public Reagent getVersionedReagent()
  {
    return _versionedReagent;
  }

  public Map getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
  }

  /**
   * Compounds in certain libraries are to be treated specially - we need to display a special message to give some
   * idea to the user why there are no structures for these compounds. Returns a non-null, non-empty message
   * explaining why there is no structure, when such a message is applicable to the library that contains this well.
   */
  public String getSpecialMessage()
  {
    if (_well == null) {
      return null;
    }
    if (! _well.getLibraryWellType().equals(LibraryWellType.EXPERIMENTAL)) {
      return null;
    }
    Library library = _well.getLibrary();
    // HACK: special case messages
    if (library.getLibraryType().equals(LibraryType.NATURAL_PRODUCTS)) {
      return "Structure information is unavailable for compounds in natural products libraries.";
    }
    if (library.getLibraryName().equals(SPECIAL_CHEMDIV6_LIBRARY_NAME)) {
      return "Structure information for compounds in the " + SPECIAL_CHEMDIV6_LIBRARY_NAME +
        " library are available via ICCB-L staff.";
    }
    return null;
  }

  @UIControllerMethod
  public String viewWell()
  {
    WellKey wellKey = new WellKey((String) getRequestParameter("entityId"));
    return _thisProxy.viewWell(wellKey);
  }

  @UIControllerMethod
  public String viewWell(Well well)
  {
    if (well == null) {
      reportApplicationError("attempted to view an unknown well (not in database)");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _thisProxy.viewWell(well.getWellKey(), null );
  }

  @UIControllerMethod
  public String viewWell(WellKey wellKey)
  {
    return _thisProxy.viewWell(wellKey, null);
  }

  @UIControllerMethod
  @Transactional
  public String viewWell(final WellKey wellKey, LibraryContentsVersion lcv)
  {
    _well = _dao.findEntityById(Well.class,
                                wellKey.toString(),
                                true,
                                Well.library.getPath());
    if (_well == null) {
      showMessage("libraries.noSuchWell", wellKey.getPlateNumber(), wellKey.getWellName());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    _versionedReagent = lcv == null ? _well.getLatestReleasedReagent() : _well.getReagents().get(lcv);
    if (_well.getLibrary().getScreenType() == ScreenType.RNAI) {
      _dao.needReadOnly(_versionedReagent, SilencingReagent.vendorGene.to(Gene.genbankAccessionNumbers).getPath());
      _dao.needReadOnly(_versionedReagent, SilencingReagent.vendorGene.to(Gene.entrezgeneSymbols).getPath());
      _dao.needReadOnly(_versionedReagent, SilencingReagent.facilityGene.to(Gene.genbankAccessionNumbers).getPath());
      _dao.needReadOnly(_versionedReagent, SilencingReagent.facilityGene.to(Gene.entrezgeneSymbols).getPath());
      _dao.needReadOnly(_versionedReagent, SilencingReagent.duplexWells.to(Well.library).getPath());
    }
    else {
      _dao.needReadOnly(_versionedReagent, SmallMoleculeReagent.compoundNames.getPath());
      _dao.needReadOnly(_versionedReagent, SmallMoleculeReagent.pubchemCids.getPath());
      _dao.needReadOnly(_versionedReagent, SmallMoleculeReagent.chembankIds.getPath());
      _dao.needReadOnly(_versionedReagent, SmallMoleculeReagent.molfileList.getPath());
    }
    initializeAnnotationValuesTable(_well);
    setNameValueTable(new WellNameValueTable(_well,
                                             _versionedReagent,
                                             _thisProxy,
                                             _libraryViewer,
                                             _structureImageProvider));
    _otherWellsDataModel = null;
    _duplexWellsDataModel = null;
    return VIEW_WELL;
  }
  
  public String viewLibrary()
  {
    return _libraryViewer.viewLibrary(_well.getLibrary());
  }

  @UIControllerMethod
  public String downloadSDFile()
  {
    try {
      _wellsSdfDataExporter.setLibraryContentsVersion(_versionedReagent == null ? null : _versionedReagent.getLibraryContentsVersion());
      InputStream inputStream = _wellsSdfDataExporter.export(Sets.newHashSet(_well.getWellKey().toString()));
      JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                         inputStream,
                                         _wellsSdfDataExporter.getFileName(),
                                         _wellsSdfDataExporter.getMimeType());
    }
    catch (IOException e) {
      reportApplicationError(e.toString());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public DataModel getOtherWellsDataModel()
  {
    if (_otherWellsDataModel == null) {
      if (_versionedReagent != null) {
        Set<Reagent> reagents = _librariesDao.findReagents(_versionedReagent.getVendorId(), true /*Reagent.well.to(Well.library).getPath()*/);
        reagents.remove(_versionedReagent);
        _otherWellsDataModel = new ListDataModel(Lists.newArrayList(reagents));
      }
      else {
        _otherWellsDataModel = new ListDataModel(Lists.newArrayList());
      }
    }
    return _otherWellsDataModel;
  }
  
  @UIControllerMethod
  public String browseOtherWells()
  {
    Iterable<ReagentVendorIdentifier> rvis = 
      Iterables.transform((List<Reagent>) getOtherWellsDataModel().getWrappedData(),
                          new Function<Reagent,ReagentVendorIdentifier>() { public ReagentVendorIdentifier apply(Reagent r) { return r.getVendorId(); } });
    _wellSearchResults.searchReagents(Sets.newHashSet(rvis));
    return VIEW_WELL_SEARCH_RESULTS;
  }

  public DataModel getDuplexWellsDataModel()
  {
    if (_duplexWellsDataModel == null) {
      if (_versionedReagent != null && _versionedReagent instanceof SilencingReagent) {
        Set<Well> well = ((SilencingReagent) _versionedReagent).getDuplexWells();
        _duplexWellsDataModel = new ListDataModel(Lists.newArrayList(well));
      }
      else {
        _duplexWellsDataModel = new ListDataModel(Lists.newArrayList());
      }
    }
    return _duplexWellsDataModel;
  }
  
  @UIControllerMethod
  public String browseDuplexWells()
  {
    Iterable<WellKey> wellKeys =
      Iterables.transform((List<Well>) getDuplexWellsDataModel().getWrappedData(),
                          new Function<Well,WellKey>() { public WellKey apply(Well w) { return w.getWellKey(); } });
    _wellSearchResults.searchWells(Sets.newHashSet(wellKeys));
    return VIEW_WELL_SEARCH_RESULTS;
  }

  public NameValueTable getNameValueTable()
  {
    return _nameValueTable;
  }

  public void setNameValueTable(NameValueTable nameValueTable)
  {
    _nameValueTable = nameValueTable;
  }

  public NameValueTable getAnnotationNameValueTable()
  {
    return _annotationNameValueTable;
  }

  private void setAnnotationNameValueTable(NameValueTable annotationNameValueTable)
  {
    _annotationNameValueTable = annotationNameValueTable;
  }

  private void initializeAnnotationValuesTable(Well well)
  {
    List<AnnotationValue> annotationValues = new ArrayList<AnnotationValue>();
    Map<Integer,List<SimpleCell>> studyNumberToStudyInfoMap = Maps.newHashMap();
    if (_versionedReagent != null) {
      _dao.needReadOnly(_versionedReagent, Reagent.annotationValues.getPath());
      annotationValues.addAll(_versionedReagent.getAnnotationValues().values());
      for (Iterator iterator = annotationValues.iterator(); iterator.hasNext(); ) {
        AnnotationValue annotationValue = (AnnotationValue) iterator.next();
        if (annotationValue.isRestricted()) {
          iterator.remove();
        }
      }
      //TODO: remove annotations that the user has not selected to view, 
      // also use user settings to see which annotations to view

      // Optional header information
      // Note, rather than Lazy load the table, (i.e. extend DataTableModelLazyUpdateDecorator)
      // Just fill the whole table now, since if this is being created, then
      // we will need the data anyway.
      // group by study
      studyNumberToStudyInfoMap = Maps.newHashMapWithExpectedSize(annotationValues.size());

      for (AnnotationValue value: annotationValues)
      {
        final AnnotationType type = value.getAnnotationType();
        // once per study
        Integer studyNumber = value.getAnnotationType().getStudy().getStudyNumber();
        if(! studyNumberToStudyInfoMap.containsKey(studyNumber))
        {
          // create empty list either way
          List<SimpleCell> headerInfo = new ArrayList<SimpleCell>();

          // Now build a 2xN array of header values mapped to the study number
          for(AnnotationHeaderColumn headerColumn : EnumSet.allOf(AnnotationHeaderColumn.class))
          {
            String headerValue = headerColumn.getValue(_versionedReagent, type);
            if (!StringUtils.isEmpty(headerValue)) {
              final Study study = type.getStudy();
              if (headerColumn == AnnotationHeaderColumn.STUDY_NAME) {
                headerInfo.add(
                               new SimpleCell(headerColumn.getColName(), headerValue, headerColumn.getDescription()) 
                               {
                                 @Override
                                 public boolean isCommandLink() { return true; }

                                 @Override
                                 public Object cellAction() 
                                 { 
                                   return _studyViewer.viewStudy(study); 
                                 }
                               });
              } 
              else {
                headerInfo.add(new SimpleCell(headerColumn.getColName(), headerValue, headerColumn.getDescription()));
              }
            }
          }
          // add even if empty - will just show the number
          studyNumberToStudyInfoMap.put(studyNumber, headerInfo);
        }
      }
    }
    
    setAnnotationNameValueTable(new AnnotationNameValueTable(annotationValues, studyNumberToStudyInfoMap, null));
  }

  public boolean isAllowedAccessToSilencingReagentSequence() 
  {
    if (_well.getLatestReleasedReagent() != null && _well.<Reagent>getLatestReleasedReagent() instanceof SilencingReagent) {
      return _dataAccessPolicy.isAllowedAccessToSilencingReagentSequence((SilencingReagent) _well.getLatestReleasedReagent());
    }
    return false;
  }
}
