// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.faces.component.UIColumn;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * JSF backing bean for Screen Result Viewer web page (screenresultviewer.jsp).
 * <p>
 * The <code>screenResult</code> property should be set to the
 * {@link ScreenResult} that is to be viewed.<br>
 * The <code>itemsPerPage</code> property controls how many rows to show per
 * "page"; defaults to {@link #DEFAULT_ITEMS_PER_PAGE}.<br>

 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreenResultViewerController
{

  // static data members
  
  private static Logger log = Logger.getLogger(MainController.class);
  
  private static final int DEFAULT_ITEMS_PER_PAGE = 10;
  

  // instance data members

  private DAO _dao;
  private ScreenResult _screenResult;
  private int itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
  private int _pageIndex;
  private boolean _showMetadataTable = true;
  private boolean _showRawDataTable = true;
  private UIData _dataTable;
  private UIData _metadataTable;
  

  // public methods
  
  public ScreenResultViewerController()
  {
  }
  

  // bean property methods

  public DAO getDAO()
  {
    return _dao;
  }

  public void setDAO(DAO dao)
  {
    _dao = dao;
  }

  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }
  
  public List<ResultValueType> getDataHeaders()
  {
    return new ArrayList<ResultValueType>(_screenResult.getResultValueTypes());
  }

  public void setScreenResult(ScreenResult screenResult)
  {
    if (_screenResult != screenResult) {
      reset();
    }
    _screenResult = screenResult;
  }
  
  public boolean isShowMetadataTable()
  {
    return _showMetadataTable;
  }

  public void setShowMetadataTable(boolean showMetadataTable)
  {
    _showMetadataTable = showMetadataTable;
  }

  public boolean isShowRawDataTable()
  {
    return _showRawDataTable;
  }

  public void setShowRawDataTable(boolean showRawDataTable)
  {
    _showRawDataTable = showRawDataTable;
  }

  public UIData getDataTable()
  {
    return _dataTable;
  }

  public void setDataTable(UIData dataUIComponent)
  {
    _dataTable = dataUIComponent;
    addDataHeaderColumns();
  }
  
  public UIData getMetadataTable()
  {
    return _metadataTable;
  }

  public void setMetadataTable(UIData dataUIComponent)
  {
    _metadataTable = dataUIComponent;
    addDataHeaderColumns();
  }
  
  public List<MetadataRow> getMetadata()
  {
    String[] properties = null;
    try {
      properties = new String[] {
        "name",
        "description",
        "activityIndicator",
        "derived",
        "howDerived"
      };
    } catch (Exception e) {
      e.printStackTrace();
      log.error("property not valid for ResultValueType");
    }

    List<MetadataRow> tableData = new ArrayList<MetadataRow>();
    for (String property : properties) {
      try {
        tableData.add(new MetadataRow(_screenResult.getResultValueTypes(),
                                      property));
      }
      catch (Exception e) {
        e.printStackTrace();
        log.error("could not obtain property value for ResultValueType");
      }
    }
    return tableData;
  }

  /**
   * @return a List of {@link DataRow} objects
   */
  public List<DataRow> getRawData()
  {
    // to build our table data structure, we will iterate the ResultValueTypes'
    // ResultValues in parallel (kind of messy!)
    Map<ResultValueType,Iterator> rvtIterators = new HashMap<ResultValueType,Iterator>();
    for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
      rvtIterators.put(rvt,
                       rvt.getResultValues().iterator());
    }
    List<DataRow> tableData = new ArrayList<DataRow>();
    for (ResultValue majorResultValue : _screenResult.getResultValueTypes().first().getResultValues()) {
      DataRow dataRow = new DataRow(_screenResult.getResultValueTypes(), majorResultValue.getWell());
      for (Map.Entry<ResultValueType,Iterator> entry : rvtIterators.entrySet()) {
        Iterator rvtIterator = entry.getValue();
        dataRow.addResultValue(entry.getKey(), 
                               (ResultValue) rvtIterator.next());
      }
      tableData.add(dataRow);
    }
    return tableData;
  }
  
  public int getItemsPerPage()
  {
    return itemsPerPage;
  }

  public void setItemsPerPage(int itemsPerPage)
  {
    this.itemsPerPage = itemsPerPage;
  }


  // JSF application methods
  
  public String gotoPage(int pageIndex)
  {
    try {
      UIData dataTable = getDataTable();
      int firstRow = pageIndex * dataTable.getRows();
      if (firstRow >= 0 &&
        firstRow < _screenResult.getResultValueTypes().first().getResultValues().size()) {
        dataTable.setFirst(firstRow);
        _pageIndex = pageIndex;
      }
      return null;
    } 
    catch (Exception e) {
      return "error";
    }
  }
  
  public String nextPage()
  {
    return gotoPage(_pageIndex + 1); 
  }
  
  public String prevPage()
  {
    return gotoPage(_pageIndex - 1); 
  }
  
  public String done()
  {
    reset();
    return "done";
  }

  
  // JSF event listener methods
  
  /**
   * Causes the page to be re-rendered. For example, in response to a checkbox
   * that controls which components are visible.
   */
  public void refresh(ValueChangeEvent event)
  {
    FacesContext.getCurrentInstance().renderResponse();
  }
  

  // private methods
  
  /**
   * Generates a standard name for dynamically-added table columns.
   */
  private String makeResultValueTypeColumnName(ResultValueType rvt)
  {
    return rvt.getName() + "Column";
  }

  /**
   * Removes any dynamically-added columns, allowing the table's dynamic columns
   * to be added anew for a new ScreenResult.
   */
  @SuppressWarnings("unchecked")
  private void reset()
  {
    
    if (getDataTable() == null) {
      return;
    }
    log.debug("dynamically removing old columns from table");
    Collection dynamicColumns = getDataTable().getChildren().subList(2, getDataTable().getChildCount());
    // set parent of columns to remove to null, just to be safe (JSF spec says you must do this)
    for (Iterator iter = dynamicColumns.iterator(); iter.hasNext();) {
      UIColumn column = (UIColumn) iter.next();
      column.setParent(null);
    }
    getDataTable().getChildren().removeAll(dynamicColumns);
    
    // reset the 1st row to be displayed
    getDataTable().setFirst(0);
    // reset the "current" row (in terms of selection/editing)
    // "-1" also causes the state of child components to be reset by UIData (according to JSF spec)
    getDataTable().setRowIndex(-1);
  }

  /**
   * Dynamically add table columns for Data Headers (aka ResultValueTypes) to
   * both raw data and metadata tables, iff they have not already been added
   */
  private void addDataHeaderColumns()
  {
    assert _screenResult != null : "screenResult property must be set";
    if (_dataTable == null || _metadataTable == null) {
      // both tables' UI components must be known before we can do our thing
      return;
    }
    FacesContext facesCtx = FacesContext.getCurrentInstance();
    // TODO: is there a better way to determine whether the table has already
    // been rendered?
    if (facesCtx.getViewRoot()
                .findComponent(":rawDataForm:rawDataTable:" + makeResultValueTypeColumnName(_screenResult.getResultValueTypes()
                                                                                                         .first())) == null) {
      log.debug("dynamically adding columns to table");
      String iteratedRowBeanVariableName = "row";
      _dataTable.setVar(iteratedRowBeanVariableName);
      _metadataTable.setVar(iteratedRowBeanVariableName);
      for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
        JSFUtils.addTableColumn(FacesContext.getCurrentInstance(),
                                _metadataTable,
                                makeResultValueTypeColumnName(rvt),
                                rvt.getName(),
                                MetadataRow.getBindingExpression(iteratedRowBeanVariableName, rvt));
        JSFUtils.addTableColumn(FacesContext.getCurrentInstance(),
                                _dataTable,
                                makeResultValueTypeColumnName(rvt),
                                rvt.getName(),
                                DataRow.getBindingExpression(iteratedRowBeanVariableName, rvt));
      }
    }
  }
  

  // inner classes
  
  /**
   * MetadataRow bean, used to provide ScreenResult metadata to JSF components via EL
   * @author ant
   */
  public static class MetadataRow
  {
    private String _rowLabel;
    /**
     * Array containing the value of the same property for each ResultValueType
     */
    private String[] _rvtPropertyValues;    

    /**
     * Constructs a MetadataRow object.
     * @param rvts The {@link ResultValueType}s that contain the data for this row
     * @param property a bean property of the {@link ResultValueType}, which defines the type of metadata to be displayed by this row
     * @throws Exception if the specified property cannot be determined for a ResultValueType
     */
    public MetadataRow(Collection<ResultValueType> rvts, String propertyName) throws Exception
    {
      _rowLabel = propertyName; // TODO: need better display name
      _rvtPropertyValues = new String[rvts.size()];
      int i = 0;
      for (ResultValueType rvt : rvts) {
        _rvtPropertyValues[i++] = BeanUtils.getProperty(rvt, propertyName);
      }
    }

    /**
     * Returns a JSF EL expression that can be used to specify the bean property to
     * bind to a UIData cell. Since the DataRow is in fact the bean providing the data
     * to a UIData table, it is the best place for storing the knowledge of how
     * to access its data via a JSF EL expression.
     * 
     * @param dataTable
     * @param rvt
     * @return a JSF EL expression
     */
    public static String getBindingExpression(String rowBeanVariableName, ResultValueType rvt)
    {
      return "#{" + rowBeanVariableName + ".dataHeaderSinglePropertyValues[" + rvt.getOrdinal() + "]}";
    }
    
    public String getRowLabel()
    {
      return _rowLabel;
    }
    
    public String[] getDataHeaderSinglePropertyValues()
    {
      return _rvtPropertyValues;
    }
  }

  
  /**
   * DataRow bean, used to provide ScreenResult data to JSF components via EL
   * @author ant
   */
  public static class DataRow
  {
    private ScreenResult _screenResult;
    private Well _well;
    private ResultValue[] _resultValues;
    
    public DataRow(SortedSet<ResultValueType> rvts, Well well)
    {
      _resultValues = new ResultValue[rvts.size()];
      _well = well;
    }

    /**
     * Returns a JSF EL expression that can be used to specify the bean property to
     * bind to a UIData cell. Since the DataRow is in fact the bean providing the data
     * to a UIData table, it is the best place for storing the knowledge of how
     * to access its data via a JSF EL expression.
     * 
     * @param dataTable
     * @param rvt
     * @return a JSF EL expression
     */
    public static String getBindingExpression(String rowBeanVariableName, ResultValueType rvt)
    {
      return "#{" + rowBeanVariableName + ".resultValues[" + rvt.getOrdinal() + "].value}";
    }

    public Well getWell()
    {
      return _well;
    }

    public ResultValue[] getResultValues()
    {
      return _resultValues;
    }

    public void addResultValue(ResultValueType rvt, ResultValue rv)
    {
      _resultValues[rvt.getOrdinal()] = rv;
    }
  }

}
