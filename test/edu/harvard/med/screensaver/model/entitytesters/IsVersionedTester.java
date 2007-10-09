// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.entitytesters;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * Tests that the entity is versioned, that the name of the version property is "version",
 * and that the version property is not nullable.
 */
public class IsVersionedTester<E extends AbstractEntity>
extends AbstractEntityTester<E>
{
  private static Logger log = Logger.getLogger(IsVersionedTester.class);

  public IsVersionedTester(Class<E> entityClass, SessionFactory sessionFactory)
  {
    super(entityClass, sessionFactory);
  }

  @Override
  public void testEntity()
  {
    testIsVersioned();
  }

  /**
   * Test that the entity is versioned, that the name of the version property is "version",
   * and that the version property is not nullable.
   */
  private void testIsVersioned()
  {
    ClassMetadata classMetadata = _sessionFactory.getClassMetadata(_entityClass);
    String entityName = classMetadata.getEntityName();
    assertTrue(
      "hibernate class is versioned: " + entityName,
      classMetadata.isVersioned());
    
    int versionIndex = classMetadata.getVersionProperty();
    String versionName = classMetadata.getPropertyNames()[versionIndex];
    assertTrue(
      "name of version property is version: " + entityName,
      versionName.equals("version"));
    
    boolean versionNullability = classMetadata.getPropertyNullability()[versionIndex];
    assertFalse("version property is not nullable: " + entityName, versionNullability);
  }
}