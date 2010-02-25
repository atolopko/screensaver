// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.security.Principal;
import java.util.Set;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;

import com.google.common.base.Function;
import com.google.common.collect.Sets;


/**
 * Defines the roles that can be assigned to a ScreensaverUser, controlling the
 * privileges granted to that user. Also acts as JAAS
 * {@link java.security.Principal}.
 * <p>
 * roles can form a hierarchy, in that granting a given role can imply the
 * granting of more fundamental roles. ScreensaverUserRole merely provides
 * information about this hierarchy via {@link #getImpliedRole()} and
 * {@link #getImpliedRole()}, but does not otherwise enforce that implied roles
 * have been explicitly granted to a user.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum ScreensaverUserRole implements VocabularyTerm, Principal
{

  // the vocabulary

  // note: having the root 'screensaverUser' role allows:
  // 1) accounts to be activated and deactivated
  // 2) guest accounts to be created (i.e., with no other roles) 
  SCREENSAVER_USER("screensaverUser", "Screensaver User Login", "Basic role for users, admins, and guests that have login privileges to Screensaver.  The person may or may not be a user of the screening facility."),

  READ_EVERYTHING_ADMIN("readEverythingAdmin", "Read Everything Administrator", "Administrators that can view and search over data of all categories, except screen billing information."),
  
  LIBRARIES_ADMIN("librariesAdmin", "Libraries Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify libraries."),
  USER_CHECKLIST_ITEMS_ADMIN("userChecklistItemsAdmin", "User Checklist Items Administrator", READ_EVERYTHING_ADMIN, "Administrators that can edit the checklist items for a user, even if they are not a Users Adminstrator."),
  USERS_ADMIN("usersAdmin", "Users Administrator", USER_CHECKLIST_ITEMS_ADMIN, "Administrators that can create and modify user accounts that are not lab heads."),
  LAB_HEADS_ADMIN("labHeadsAdmin", "Lab Heads Administrator", USERS_ADMIN, "Administrators that can create and modify user accounts that are lab heads."),
  USER_ROLES_ADMIN("userRolesAdmin", "User Roles Admin", USERS_ADMIN , "Administrators that can modify data access roles on user accounts."),
  SCREENS_ADMIN("screensAdmin", "Screens Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify screens."),
  SCREEN_RESULTS_ADMIN("screenResultsAdmin", "Screen Results Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify screen results."),
  CHERRY_PICK_REQUESTS_ADMIN("cherryPickRequestsAdmin", "Cherry Pick Requests Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify cherry pick requests, including the generation of cherry pick plate mapping files, and the recording of cherry pick liquid transfers."),
  BILLING_ADMIN("billingAdmin", "Billing Information Administrator", SCREENS_ADMIN, "Administrators that can view, create, and modify billing information for a screen."),

  MARCUS_ADMIN("marcusAdmin", "Marcus Screens Administrator", READ_EVERYTHING_ADMIN, "Administrators that have access to Marcus library-related screens (only)."),
  GRAY_ADMIN("grayAdmin", "Gray Screens Administrator", READ_EVERYTHING_ADMIN, "Administrators that have access to Gray library-related screens (only)."),

  SM_DSL_LEVEL3_SHARED_SCREENS("smDsl3SharedScreens", "Small Molecule Screens Level 3", "Small molecule screeners that can view shared small molecule screens."),
  SM_DSL_LEVEL2_MUTUAL_POSITIVES("smDsl2MutualPositives", "Small Molecule Screens Level 2", SM_DSL_LEVEL3_SHARED_SCREENS, "Small molecule screeners that can view each others' screen result \"positives\" data, with associated screen summary information."),
  SM_DSL_LEVEL1_MUTUAL_SCREENS("smDsl1MutualScreens", "Small Molecule Screens Level 1", SM_DSL_LEVEL2_MUTUAL_POSITIVES, "Small molecule screeners that can view each others' screen information and screen result data."),
  
  RNAI_SCREENS("rnaiScreens", "RNAi Screens", "RNAi screeners that view RNAi screens."),

  // note: developers do not automatically get admin roles (other than readEverythingAdmin), allowing developers to restrict themselves from mutating data in production environments
  DEVELOPER("developer", "Developer", READ_EVERYTHING_ADMIN, "Special users that have permission to invoke development-related functionality and view low-level system information.")
  ;

  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link LibraryScreeningStatus} vocabulary.
   */
  public static class UserType extends VocabularyUserType<ScreensaverUserRole>
  {
    public UserType()
    {
      super(ScreensaverUserRole.values());
    }
  }
  
  public static Function<ScreensaverUserRole,String> ToDisplayableRoleName = new Function<ScreensaverUserRole,String>() {
    public String apply(ScreensaverUserRole role)
    {
      return role.getDisplayableRoleName();
    }
  }; 
    


  // private instance field and constructor

  private String _roleName;
  private String _displayableRoleName;
  private String _comment;
  private ScreensaverUserRole _impliedRole;
  private Set<ScreensaverUserRole> _impliedRoles;


  private ScreensaverUserRole(String roleName,
                              String displayableRoleName,
                              ScreensaverUserRole impliedRole,
                              String comment)
  {
    _roleName = roleName;
    _displayableRoleName = displayableRoleName;
    _comment = comment;
    _impliedRole = impliedRole;
    _impliedRoles = calcImpliedRoles();
  }

  private ScreensaverUserRole(String roleName,
                              String displayableRoleName,
                              String comment)
  {
    this(roleName, displayableRoleName, null, comment);
  }

  private Set<ScreensaverUserRole> calcImpliedRoles()
  {
    Set<ScreensaverUserRole> impliedRoles = Sets.newHashSet();
    ScreensaverUserRole impliedRole = getImpliedRole();
    while (impliedRole != null && !impliedRoles.contains(impliedRole)) {
      impliedRoles.add(impliedRole);
      impliedRole = impliedRole.getImpliedRole();
    }
    return impliedRoles;
  }

  // public instance methods

  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue()
  {
    return _roleName;
  }

  public String getRoleName()
  {
    return _roleName;
  }

  public String getDisplayableRoleName()
  {
    return _displayableRoleName;
  }

  public String getComment()
  {
    return _comment;
  }

  public ScreensaverUserRole getImpliedRole()
  {
    return _impliedRole;
  }

  public Set<ScreensaverUserRole> getImpliedRoles()
  {
    return _impliedRoles;
  }

  @Override
  public String toString()
  {
    return getValue();
  }

  // Principal interface methods

  /**
   * Get the user Principal name.
   */
  public String getName()
  {
    return getValue();
  }

  public boolean isAdministrative()
  {
    return this == READ_EVERYTHING_ADMIN || _impliedRoles.contains(READ_EVERYTHING_ADMIN);
  }
}
