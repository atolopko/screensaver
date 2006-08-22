// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Exercise the entities as JavaBeans.
 */
abstract class EntityBeansExercizor extends EntityClassesExercisor
{
  
  private static Logger log = Logger.getLogger(EntityBeansExercizor.class);
  
  protected static interface PropertyDescriptorExercizor
  {
    public void exercizePropertyDescriptor(
      AbstractEntity bean,
      BeanInfo beanInfo,
      PropertyDescriptor propertyDescriptor);
  }
  
  protected void exercizePropertyDescriptors(final PropertyDescriptorExercizor exercizor)
  {
    exercizeJavaBeanEntities(new JavaBeanEntityExercizor()
      {
        public void exercizeJavaBeanEntity(AbstractEntity bean, BeanInfo beanInfo)
        {
          for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            if (
              propertyName.equals("class") ||
              propertyName.startsWith("hbn")) {
              continue;
            }
            exercizor.exercizePropertyDescriptor(bean, beanInfo, propertyDescriptor);
          }
        }
      });
  }
  
  protected static interface JavaBeanEntityExercizor
  {
    void exercizeJavaBeanEntity(AbstractEntity bean, BeanInfo beanInfo);
  }
  
  protected void exercizeJavaBeanEntities(JavaBeanEntityExercizor exercizor)
  {
    for (Class<AbstractEntity> entityClass : getEntityClasses()) {
      try {
        exercizor.exercizeJavaBeanEntity(newInstance(entityClass),
                                         Introspector.getBeanInfo(entityClass));
      }
      catch (IntrospectionException e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }
  }

  protected static Map<String, String> oddPluralToSingularPropertiesMap =
    new HashMap<String, String>();
  static {
    oddPluralToSingularPropertiesMap.put("children", "child");
    oddPluralToSingularPropertiesMap.put("copies", "copy");
    oddPluralToSingularPropertiesMap.put("typesDerivedFrom", "typeDerivedFrom");
    oddPluralToSingularPropertiesMap.put("lettersOfSupport", "letterOfSupport");
    oddPluralToSingularPropertiesMap.put("equipmentUsed", "equipmentUsed");
    oddPluralToSingularPropertiesMap.put("visitsPerformed", "visitPerformed");
    oddPluralToSingularPropertiesMap.put("screensLed", "screenLed");
    oddPluralToSingularPropertiesMap.put("screensHeaded", "screenHeaded");
    oddPluralToSingularPropertiesMap.put("screensCollaborated", "screenCollaborated");
    oddPluralToSingularPropertiesMap.put("platesUsed", "platesUsed");
  }
  protected static Map<String, String> oddSingularToPluralPropertiesMap =
    new HashMap<String, String>();
  static {
    oddSingularToPluralPropertiesMap.put("child", "children");
    oddSingularToPluralPropertiesMap.put("copy", "copies");
    oddSingularToPluralPropertiesMap.put("typeDerivedFrom", "typesDerivedFrom");
    oddSingularToPluralPropertiesMap.put("letterOfSupport", "lettersOfSupport");
    oddSingularToPluralPropertiesMap.put("equipmentUsed", "equipmentUsed");
    oddSingularToPluralPropertiesMap.put("visitPerformed", "visitPerformed");
    oddSingularToPluralPropertiesMap.put("screenLed", "screensLed");
    oddSingularToPluralPropertiesMap.put("screenHeaded", "screensHeaded");
    oddSingularToPluralPropertiesMap.put("screenCollaborated", "screensCollaborated");
    oddSingularToPluralPropertiesMap.put("platesUsed", "platesUsed");
  }
  
  protected static Map<String, String> oddPropertyToRelatedPropertyMap =
    new HashMap<String, String>();
  static {
    oddPropertyToRelatedPropertyMap.put("cherryPick", "RNAiKnockdownConfirmation");
    oddPropertyToRelatedPropertyMap.put("equipmentUsed", "visit");
    oddPropertyToRelatedPropertyMap.put("platesUsed", "visit");
    oddPropertyToRelatedPropertyMap.put("labMembers", "labHead");
    oddPropertyToRelatedPropertyMap.put("screensHeaded", "labHead");
    oddPropertyToRelatedPropertyMap.put("screensLed", "leadScreener");
    oddPropertyToRelatedPropertyMap.put("visitsPerformed", "performedBy");
  }
  protected static Map<String, String> oddPropertyToRelatedPluralPropertyMap =
    new HashMap<String, String>();
  static {
    oddPropertyToRelatedPluralPropertyMap.put("derivedTypes", "typesDerivedFrom");
    oddPropertyToRelatedPluralPropertyMap.put("typesDerivedFrom", "derivedTypes");
    oddPropertyToRelatedPluralPropertyMap.put("collaborators", "screensCollaborated");
    oddPropertyToRelatedPluralPropertyMap.put("screensCollaborated", "collaborators");
    oddPropertyToRelatedPluralPropertyMap.put("labHead", "screensHeaded");
    oddPropertyToRelatedPluralPropertyMap.put("leadScreener", "screensLed");
    oddPropertyToRelatedPluralPropertyMap.put("performedBy", "visitsPerformed");
  }
  

  /**
   * Find the method with the given name, and unspecified arguments. If no such
   * method exists, and the isRequiredMethod parameter is true, then fail. If no
   * such method exists, and isRequiredMethod is false, then return null. Fail if
   * the method does not return a boolen. Return the method.
   * @param beanClass the class to find the method in
   * @param methodName the name of the method to find
   * @param isRequiredMethod true iff the method is required to exist
   * @return the method. Return null if isRequiredMethod is false and no such
   * method exists.
   */
  protected Method findAndCheckMethod(
    Class<? extends AbstractEntity> beanClass,
    String methodName,
    boolean isRequiredMethod)
  {
    String fullMethodName = beanClass.getName() + "." + methodName;
    Method foundMethod = null;
    // note: we're calling getMethods() instead of getDeclaredMethods() to allow
    // inherited methods to satisfy our isRequiredMethod constraint (e.g. for
    // AdministratorUser.addRole())
    for (Method method : beanClass.getMethods()) {
      if (method.getName().equals(methodName)) {
        foundMethod = method;
        break;
      }
    }
    if (! isRequiredMethod && foundMethod == null) {
      return null;
    }
    assertNotNull(
      "collection property missing method: " + fullMethodName,
      foundMethod);
    assertEquals(
      "collection property method returns boolean: " + fullMethodName,
      foundMethod.getReturnType(),
      Boolean.TYPE);
    return foundMethod;
  }
  
  /**
   * Returns true iff the property corresponds to the entity's Hibernate ID.
   * 
   * @param beanInfo the bean the property belongs to
   * @param propertyDescriptor the property
   * @return true iff property is "entityId" or the property that is named the
   * same as the entity, but with an "Id" suffix; otherwise false
   */
  public boolean isHibernateIdProperty(
    BeanInfo beanInfo, 
    PropertyDescriptor propertyDescriptor)
  {
    if (propertyDescriptor.getName().equals("entityId")) {
      return true;
    }

    // Check whether property corresponds to the bean's Hibernate ID method, which is named similary to the bean.
    // We also check the parent classes, to handle the case where the property
    // has been inherited, as the property name will depend upon the class it
    // was declared in.
    String capitalizedPropertyName = propertyDescriptor.getName().substring(0, 1).toUpperCase() + propertyDescriptor.getName().substring(1);
    Class clazz = beanInfo.getBeanDescriptor().getBeanClass();
    while (!clazz.equals(AbstractEntity.class) && clazz != null) {
      if (capitalizedPropertyName.endsWith(clazz.getSimpleName() + "Id")) {
        return true;
      }
      clazz = clazz.getSuperclass();
    }
    return false;
  }
  
  
  // HACK: special case handling 
  protected int getExpectedInitialCollectionSize(
    String beanName,
    PropertyDescriptor propertyDescriptor)
  {
    if (beanName.equals("Gene") &&
      propertyDescriptor.getName().equals("genbankAccessionNumbers")) {
      return 1;
    }
    return 0;
  }
}
