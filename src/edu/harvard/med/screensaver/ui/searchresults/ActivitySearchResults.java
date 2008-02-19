// $HeadURL$
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.AllEntitiesOfTypeDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntitySetDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.ParentedEntityDataFetcher;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.VocabularyEntityColumn;
import edu.harvard.med.screensaver.ui.util.VocabularlyConverter;
import edu.harvard.med.screensaver.util.CollectionUtils;

import org.apache.log4j.Logger;


/**
 * A {@link SearchResults} for {@link Activity Activities}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class ActivitySearchResults<A extends Activity> extends EntitySearchResults<A,Integer>
{

  // private static final fields

  private static final Logger log = Logger.getLogger(ActivitySearchResults.class);

  // instance fields

  private GenericEntityDAO _dao;
  private Class<A> _type;


  // public constructor

  /**
   * @motivation for CGLIB2
   */
  protected ActivitySearchResults()
  {
  }

  public ActivitySearchResults(//ActivityViewer activityViewer,
                               Class<A> type,
                               GenericEntityDAO dao)
  {
    //_activityViewer = activityViewer;
    _type = type;
    _dao = dao;
  }

  public void searchAllActivities()
  {
    EntityDataFetcher<A,Integer> dataFetcher =
      (EntityDataFetcher<A,Integer>) new AllEntitiesOfTypeDataFetcher<A,Integer>(
        _type,
        _dao);
    initialize(dataFetcher);
  }

  public void searchActivitiesForUser(ScreensaverUser user)
  {
    initialize((EntityDataFetcher<A,Integer>) new ParentedEntityDataFetcher<A,Integer>(
      _type,
      new RelationshipPath<A>(_type, "performedBy"),
      user,
      _dao));
  }

  public void searchActivities(Set<Activity> activities)
  {
    initialize((EntityDataFetcher<A,Integer>) new EntitySetDataFetcher<A,Integer>(
      _type,
      CollectionUtils.<Integer>entityIds(activities),
      _dao));
  }


  // implementations of the SearchResults abstract methods

  @Override
  protected List<? extends TableColumn<A,?>> buildColumns()
  {
    ArrayList<EntityColumn<A,?>> columns = new ArrayList<EntityColumn<A,?>>();
    columns.add(new VocabularyEntityColumn<A,String>(
      new PropertyPath<A>(_type, "activityType"),
      "Activity Type",
      "The type of the activity",
      TableColumn.UNGROUPED,
      new VocabularlyConverter<String>(getActivityTypes()), getActivityTypes()) {
      @Override
      public String getCellValue(A activity)
      {
        return activity.getActivityTypeName();
      }
    });
    columns.add(new DateEntityColumn<A>(
      new PropertyPath<A>(_type, "datePerformed"),
      "Date Performed", "The date of the activity", TableColumn.UNGROUPED) {
      @Override
      protected Date getDate(Activity activity) {
        return activity.getDateOfActivity();
      }
    });
    columns.add(new DateEntityColumn<A>(
      new PropertyPath<A>(_type, "dateRecorded"),
      "Date Recorded", "The date the activity was recorded", TableColumn.UNGROUPED) {
      @Override
      protected Date getDate(A activity) {
        return activity.getDateCreated();
      }
    });
    columns.get(columns.size() - 1).setVisible(showAdminStatusFields());
    columns.add(new UserNameColumn<A>(
      new RelationshipPath<A>(_type, "performedBy"),
      "Performed By", "The person that performed the activity", TableColumn.UNGROUPED) {
      @Override
      public ScreensaverUser getUser(A activity) { return activity.getPerformedBy(); }
    });
    return columns;
  }

  protected abstract Set<String> getActivityTypes();

  @Override
  protected void setEntityToView(A activity)
  {
    //_activityViewer.viewActivity(activity);
  }

  private boolean showAdminStatusFields()
  {
    return isUserInRole(ScreensaverUserRole.SCREENS_ADMIN) ||
      isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }
}
