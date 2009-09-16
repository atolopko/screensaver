// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.AllEntitiesOfTypeDataFetcher;
import edu.harvard.med.screensaver.db.hibernate.HqlBuilder;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.BooleanEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.ListEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;


/**
 * A {@link SearchResults} for {@link Library Libraries}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibrarySearchResults extends EntitySearchResults<Library,Integer>
{

  // private static final fields

  private static final Logger log = Logger.getLogger(LibrarySearchResults.class);
  public static final Set<LibraryType> LIBRARY_TYPES_TO_DISPLAY =
    new HashSet<LibraryType>(Arrays.asList(LibraryType.COMMERCIAL,
                                           LibraryType.KNOWN_BIOACTIVES,
                                           LibraryType.NATURAL_PRODUCTS,
                                           LibraryType.SIRNA,
                                           LibraryType.MIRNA_INHIBITOR,
                                           LibraryType.MIRNA_MIMIC,
                                           LibraryType.OTHER));


  // instance fields

  private GenericEntityDAO _dao;
  private LibraryViewer _libraryViewer;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected LibrarySearchResults()
  {
  }

  public LibrarySearchResults(GenericEntityDAO dao,
                              LibraryViewer libraryViewer)
  {
    _dao = dao;
    _libraryViewer = libraryViewer;
  }


  // implementations of the SearchResults abstract methods

  @SuppressWarnings("unchecked")
  public void searchLibraryScreenType(ScreenType screenType)
  {
    initialize(new AllEntitiesOfTypeDataFetcher<Library,Integer>(Library.class, _dao) {
      @Override
      protected void addDomainRestrictions(HqlBuilder hql,
                                           Map<RelationshipPath<Library>,String> path2Alias)
      {
        hql.whereIn(getRootAlias(), "libraryType", LIBRARY_TYPES_TO_DISPLAY);
      }
    });

    TableColumn<Library,ScreenType> column = (TableColumn<Library,ScreenType>) getColumnManager().getColumn("Screen Type");
    column.clearCriteria();
    column.addCriterion(new Criterion<ScreenType>(Operator.EQUAL, screenType));
  }

  @SuppressWarnings("unchecked")
  protected List<? extends TableColumn<Library,?>> buildColumns()
  {
    ArrayList<TableColumn<Library,?>> columns = Lists.newArrayList();
    columns.add(new TextEntityColumn<Library>(new PropertyPath(Library.class, "shortName"),
      "Short Name", "The abbreviated name for the library", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Library library) 
      {
        return library==null? "null" : library.getShortName(); 
      }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(Library library) { return viewSelectedEntity(); }
    });
    columns.add(new TextEntityColumn<Library>(new PropertyPath(Library.class, "libraryName"),
      "Library Name", "The full name of the library", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Library library) { return library==null? "null" : library.getLibraryName() ; }
    });
    columns.add(new EnumEntityColumn<Library,ScreenType>(new PropertyPath(Library.class, "screenType"),
      "Screen Type", "'RNAi' or 'Small Molecule'",
      TableColumn.UNGROUPED, ScreenType.values()) {
      @Override
      public ScreenType getCellValue(Library library) { return library==null? null : library.getScreenType(); }
    });
    columns.add(new EnumEntityColumn<Library,LibraryType>(new PropertyPath(Library.class, "libraryType"),
      "Library Type", "The type of library, e.g., 'Commercial', 'Known Bioactives', 'siRNA', etc.",
      TableColumn.UNGROUPED, LibraryType.values()) {
      @Override
      public LibraryType getCellValue(Library library) { return library==null? null : library.getLibraryType(); }
    });
    columns.add(new BooleanEntityColumn<Library>(new PropertyPath(Library.class, "pool"),
      "Is Pool", 
      "Whether wells contains pools of reagents or single reagents",
      TableColumn.UNGROUPED) {
      @Override
      public Boolean getCellValue(Library library) { return library==null? null : library.isPool(); }
    });
    columns.add(new IntegerEntityColumn<Library>(
      Library.startPlate,
      "Start Plate", 
      "The plate number for the first plate in the library", 
      TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Library library) { return library==null? null : library.getStartPlate(); }
    });
    columns.add(new IntegerEntityColumn<Library>(
      Library.endPlate,
      "End Plate", "The plate number for the last plate in the library", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Library library) { return library==null? null : library.getEndPlate(); }
    });
    if (getScreensaverUser().isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN) ||
      getScreensaverUser().isUserInRole(ScreensaverUserRole.LIBRARIES_ADMIN)) {
      columns.add(new TextEntityColumn<Library>(new PropertyPath(Library.class, "vendor"),
        "Vendor/Source", "The vendor or source that produced the library",
        TableColumn.ADMIN_COLUMN_GROUP) {
        @Override
        public String getCellValue(Library library) { return library==null? null : library.getVendor(); }
      });
      columns.add(new ListEntityColumn<Library>(
        Library.copies.toProperty("name"),
        "Copies",
        "The copies that have been made of this library",
        TableColumn.ADMIN_COLUMN_GROUP) {
        @Override
        public List<String> getCellValue(Library library)
        {
          return library==null? null : new ArrayList<String>(
            CollectionUtils.collect(library.getCopies(), new Transformer() {
              public Object transform(Object e) { return ((Copy) e).getName(); }
            }));
        }
      });
      columns.add(new EnumEntityColumn<Library,LibraryScreeningStatus>(new PropertyPath(Library.class, "screeningStatus"),
        "Screening Status", "Screening status for the library, e.g., 'Allowed','Not Allowed', 'Not Yet Plated', etc.",
        TableColumn.ADMIN_COLUMN_GROUP
        , LibraryScreeningStatus.values()) {
        @Override
        public LibraryScreeningStatus getCellValue(Library library) 
        { return library==null? null : library.getScreeningStatus(); }
      });
    }

//    TableColumnManager<Library> columnManager = getColumnManager();
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Screen Type"),
//                                         columnManager.getColumn("Library Type"),
//                                         columnManager.getColumn("Short Name"));
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Library Type"),
//                                         columnManager.getColumn("Short HName"));

    return columns;
  }

  @Override
  protected void setEntityToView(Library library)
  {
    _libraryViewer.viewLibrary(library);
  }
}
