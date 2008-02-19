// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumnManager;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

import org.apache.log4j.Logger;

/**
 * JSF backing bean for data tables. Provides the following functionality:
 * <ul>
 * <li>manages DataModel, UIData, and {@link TableColumnManager} objects
 * <li>handles (re)sorting, (re)filtering, and changes to column composition,
 * in response to notifications from its {@link TableColumnManager}</li>
 * <li>handles "rows per page" command (via JSF listener method)</li>
 * <li>handles "goto row" command (via JSF listener method)</li>
 * <li>reports whether the "current" column is numeric
 * {@link #isNumericColumn()}</li>
 * </ul>
 *
 * @param R the type of the data object associated with each row
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class DataTable<R> extends AbstractBackingBean implements Observer
{
  // static members

  private static Logger log = Logger.getLogger(DataTable.class);


  // instance data members

  private DataTableModelLazyUpdateDecorator<R> _dataTableModel;
  private UIData _dataTableUIComponent;
  private TableColumnManager<R> _columnManager;
  private UIInput _rowsPerPageUIComponent;
  private RowsPerPageSelector _rowsPerPageSelector = new RowsPerPageSelector(Collections.<Integer>emptyList());
  private boolean _isTableFilterMode;
  /**
   * @motivation for unit tests
   */
  private DataTableModel<R> _baseDataTableModel;



  // public constructors and methods

  /**
   * @motivation for CGLIB2
   */
  public DataTable()
  {}

  public void initialize(DataTableModel<R> dataTableModel,
                         List<? extends TableColumn<R,?>> columns,
                         RowsPerPageSelector rowsPerPageSelector)
  {
    _columnManager = new TableColumnManager<R>(columns);
    _columnManager.addObserver(this);
    _rowsPerPageSelector = rowsPerPageSelector;

    _baseDataTableModel = dataTableModel;
    _dataTableModel = new DataTableModelLazyUpdateDecorator<R>(_baseDataTableModel);
    refetch();
    refilter();
    resort();
  }

  public UIData getDataTableUIComponent()
  {
    return _dataTableUIComponent;
  }

  public void setDataTableUIComponent(UIData dataTableUIComponent)
  {
    _dataTableUIComponent = dataTableUIComponent;
  }

  /**
   * @motivation MyFaces dataScroller component's 'for' attribute needs the
   *             (absolute) clientId of the dataTable component, if the
   *             dataScroller is in a different (or nested) subView.
   */
  public String getClientId()
  {
    if (_dataTableUIComponent != null) {
      return _dataTableUIComponent.getClientId(getFacesContext());
    }
    return null;
  }

  /**
   * Get the DataTableModel. Lazy instantiate, re-sort, and re-filter, as
   * necessary.
   *
   * @return the table's DataTableModel, sorted and filtered according to the
   *         latest column settings
   */
  public DataTableModel<R> getDataTableModel()
  {
    verifyIsInitialized();
    return _dataTableModel;
  }

  /**
   * @motivation for unit tests
   */
  public DataTableModel<R> getBaseDataTableModel()
  {
    verifyIsInitialized();
    return _baseDataTableModel;
  }

  public TableColumnManager<R> getColumnManager()
  {
    verifyIsInitialized();
    return _columnManager;
  }

  public UIInput getRowsPerPageUIComponent()
  {
    return _rowsPerPageUIComponent;
  }

  public void setRowsPerPageUIComponent(UIInput rowsPerPageUIComponent)
  {
    _rowsPerPageUIComponent = rowsPerPageUIComponent;
  }

  public boolean isTableFilterMode()
  {
    return _isTableFilterMode;
  }

  public void setTableFilterMode(boolean isTableFilterMode)
  {
    _isTableFilterMode = isTableFilterMode;
  }

  /**
   * Convenience method that invokes the JSF action for the "current" row and
   * column (as set by JSF during the invoke application phase). Equivalent to
   * <code>getSortManager().getCurrentColumn().cellAction((E) getDataModel().getRowData())</code>.
   */
  @SuppressWarnings("unchecked")
  @UIControllerMethod
  public String cellAction()
  {
    return (String) getColumnManager().getCurrentColumn().cellAction(getRowData());
  }

  /**
   * Convenience method that returns the value of the "current" row and column
   * (as set by JSF during the render phase, when rendering each table cell).
   * Equivalent to
   * <code>getSortManager().getCurrentColumn().getCellValue(getDataModel().getRowData())</code>.
   */
  public Object getCellValue()
  {
    return getColumnManager().getCurrentColumn().getCellValue(getRowData());
  }

  /**
   * Get the data object associated with the "current" row (i.e., as set by JSF
   * at render time).
   */
  @SuppressWarnings("unchecked")
  final protected R getRowData()
  {
    return (R) getDataTableModel().getRowData();
  }

  public int getRowsPerPage()
  {
    return getRowsPerPageSelector().getSelection();
  }

  public RowsPerPageSelector getRowsPerPageSelector()
  {
    verifyIsInitialized();
    return _rowsPerPageSelector;
  }

  public void pageNumberListener(ValueChangeEvent event)
  {
    if (event.getNewValue() != null &&
      event.getNewValue().toString().trim().length() > 0) {
      log.debug("page number changed to " + event.getNewValue());
      gotoPageIndex(Integer.parseInt(event.getNewValue()
                                          .toString()) - 1);
// _rowsPerPageUIComponent.setSubmittedValue(null); // clear
// _rowsPerPageUIComponent.setValue(null); // clear
      getFacesContext().renderResponse();
    }
  }

  public void gotoPageIndex(int pageIndex)
  {
    gotoRowIndex(pageIndex * getRowsPerPage());
  }

  public void gotoRowIndex(int rowIndex)
  {
    if (_dataTableUIComponent != null) {
      log.debug("gotoRowIndex(): requested row: " + rowIndex);
      // ensure value is within valid range, and in particular that we never
      // show less than the table's configured row count (unless it's more than
      // the total number of rows)
      rowIndex = Math.max(0, Math.min(rowIndex,
                                      getRowCount() -
                                        getDataTableUIComponent().getRows()));
      _dataTableUIComponent.setFirst(rowIndex);
      log.debug("gotoRowIndex(): actual row: " + rowIndex);
    }
  }

  public boolean isNumericColumn()
  {
    return getColumnManager().getCurrentColumn().isNumeric();
  }

  public int getRowCount()
  {
    return getDataTableModel().getRowCount();
  }

  public void rowsPerPageListener(ValueChangeEvent event)
  {
    String rowsPerPageValue = (String) event.getNewValue();
    log.debug("rowsPerPage changed to " + rowsPerPageValue);
    getRowsPerPageSelector().setValue(rowsPerPageValue);
    // scroll to a page boundary, to ensure that first/next do not have problems moving to first/last page
    int rowIndex = _dataTableUIComponent.getFirst();
    int rowsPerPage = getRowsPerPage();
    if (rowIndex % rowsPerPage != 0) {
      int pageBoundaryRowIndex = rowsPerPage * (rowIndex / rowsPerPage);
      log.debug("scrolling to page boundary row: " + pageBoundaryRowIndex);
      gotoRowIndex(pageBoundaryRowIndex);
    }
    getFacesContext().renderResponse();
  }

  public boolean isMultiPaged()
  {
    return getRowCount() > getRowsPerPage();
  }

  /**
   * Resets each column's criteria to a single, non-restricting criterion. This
   * is useful for a user interface that wants to present a single criterion per
   * column, that can be edited by the user (without having to explicitly add a
   * criterion first).
   */
  @UIControllerMethod
  public String resetFilter()
  {
    for (TableColumn<R,?> column : getColumnManager().getAllColumns()) {
      column.resetCriteria();
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /**
   * Delete all criteria from each column.
   */
  @UIControllerMethod
  public String clearFilter()
  {
    for (TableColumn<R,?> column : getColumnManager().getAllColumns()) {
      column.clearCriteria();
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @SuppressWarnings("unchecked")
  public void update(Observable o, Object arg)
  {
    if (o == getColumnManager()) {
      if (arg instanceof SortChangedEvent) {
        log.debug("DataTable notified of sort column change: " + arg);
        resort();
      }
      else if (arg instanceof Criterion) {
        log.debug("DataTable notified of criterion change: " + arg);
        refilter();
      }
      else if (arg instanceof ColumnVisibilityChangedEvent) {
        log.debug("DataTable notified of column visibility change: " + arg);
        ColumnVisibilityChangedEvent event = (ColumnVisibilityChangedEvent) arg;
        if (event.getColumnsAdded().size() > 0) {
          // TODO: only refetch if the added columns add new RelationshipPaths! (we may already have fetched the data for this column)
          refetch();
          for (TableColumn<?,?> addedColumn : event.getColumnsAdded()) {
            if (addedColumn.hasCriteria()) {
              refilter();
              break;
            }
          }
        }
        if (event.getColumnsRemoved().size() > 0) {
          for (TableColumn<?,?> removedColumn : event.getColumnsRemoved()) {
            if (removedColumn.hasCriteria()) {
              refilter();
              break;
            }
          }
          // note: if removedColumn is also a sort column, TableColumnManager
          // will handle issuing the event that forces a resort(), as necessary
        }
      }
    }
  }

  public void refetch()
  {
    getDataTableModel().fetch(getColumnManager().getVisibleColumns());
  }

  public void refilter()
  {
    getDataTableModel().filter(getColumnManager().getVisibleColumns());
    // note: we cannot call gotoRowIndex(), as this will cause DTMLUD to trigger
    if (_dataTableUIComponent != null) {
      _dataTableUIComponent.setFirst(0);
    }
  }

  public void resort()
  {
    getDataTableModel().sort(getColumnManager().getSortColumns(),
                             getColumnManager().getSortDirection());
    // note: we cannot call gotoRowIndex(), as this will cause DTMLUD to trigger
    if (_dataTableUIComponent != null) {
      _dataTableUIComponent.setFirst(0);
    }
  }

  // private methods

  private void verifyIsInitialized()
  {
    if (_columnManager == null ||
      _dataTableModel == null ||
      _rowsPerPageSelector == null) {
      throw new IllegalStateException("DataTable not initialized");
    }
  }

}
