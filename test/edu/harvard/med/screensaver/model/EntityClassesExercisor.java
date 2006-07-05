// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import edu.harvard.med.screensaver.AbstractSpringTest;

/**
 * Exercise the entity classes.
 */
abstract class EntityClassesExercisor extends AbstractSpringTest
{
  
  private static final String [] entityPackages = {
    "edu.harvard.med.screensaver.model",
    "edu.harvard.med.screensaver.model.derivatives",
    "edu.harvard.med.screensaver.model.libraries",
    "edu.harvard.med.screensaver.model.screenresults",
    "edu.harvard.med.screensaver.model.screens",
    "edu.harvard.med.screensaver.model.users"
  };
  
  private Integer _integerTestValue = 77;
  private double  _doubleTestValue = 77.1;
  private boolean _booleanTestValue = true;
  private String  _stringTestValue = "test:a";
  private long    _dateMilliseconds = 0;
  private int     _vocabularyTermCounter = 0;
  
  @SuppressWarnings("unchecked")
  protected Object getTestValueForType(Class type)
  {
    if (type.equals(Integer.class)) {
      _integerTestValue += 1;
      return _integerTestValue;
    }
    if (type.equals(Double.class)) {
      _doubleTestValue *= 1.32;
      return new Double(new Double(_doubleTestValue * 1000).intValue() / 1000);
    }
    if (type.equals(Boolean.TYPE)) {
      _booleanTestValue = ! _booleanTestValue;
      return _booleanTestValue;
    }
    if (type.equals(String.class)) {
      char lastChar = _stringTestValue.charAt(_stringTestValue.length() - 1);
      if (lastChar == 'z') {
        lastChar = 'a';
      }
      else {
        lastChar++;
      }
      _stringTestValue += lastChar;
      return _stringTestValue;
    }
    if (type.equals(Date.class)) {
      _dateMilliseconds += 1000 * 60 * 60 * 24 * 1.32;
      return DateUtils.round(new Date(_dateMilliseconds), Calendar.DATE);
    }
    if (AbstractEntity.class.isAssignableFrom(type)) {
      return newInstance((Class<AbstractEntity>) type);
    }
    if (VocabularyTerm.class.isAssignableFrom(type)) {
      try {
        Method valuesMethod = type.getMethod("values");
        Object values = (Object) valuesMethod.invoke(null);
        int numValues = Array.getLength(values);
        int valuesIndex = ++ _vocabularyTermCounter % numValues;
        return Array.get(values, valuesIndex);
      }
      catch (Exception e) {
        e.printStackTrace();
        fail("vocabular term test value code threw an exception");
      }
    }    
    throw new IllegalArgumentException(
      "can't create test values for type: " + type.getName());
  }
  
  protected static interface EntityClassExercizor
  {
    void exercizeEntityClass(Class<AbstractEntity> entityClass);
  }
  
  protected void exercizeEntityClasses(EntityClassExercizor exercizor)
  {
    for (Class<AbstractEntity> entityClass : getEntityClasses()) {
      exercizor.exercizeEntityClass(entityClass);
    }
  }

  @SuppressWarnings("unchecked")
  protected List<Class<AbstractEntity>> getEntityClasses()
  {
    List<Class<AbstractEntity>> entityClasses = new ArrayList<Class<AbstractEntity>>();
    for (String entityPackage : entityPackages) {
      String packagePath = "/" + entityPackage.replace('.', '/');
      URL packageURL = getClass().getResource(packagePath);
      File directory = new File(packageURL.getFile());
      if (! directory.exists()) {
        throw new RuntimeException("directory " + directory + "doesn't exist");
      }
      for (String file : directory.list()) {
        if (! file.endsWith(".class")) {
          continue;
        }
        
        // skip inner classes of AbstractEntityTest
        if (entityPackage.equals("edu.harvard.med.screensaver.model") &&
            file.startsWith("AbstractEntityTest")) {
          continue;
        }
        
        String classname = file.substring(0, file.length() - 6); // remove the .class extension
        Class entityClass;
        try {
          entityClass = Class.forName(entityPackage + "." + classname);
        }
        catch (ClassNotFoundException e) {
          continue;
        }
        
        // skip abstract classes
        if (Modifier.isAbstract(entityClass.getModifiers())) {
          continue;
        }
        //if (AbstractEntity.class.equals(entityClass)) {
        //  continue;
        //}
        
        if (AbstractEntity.class.isAssignableFrom(entityClass)) {
          entityClasses.add((Class<AbstractEntity>) entityClass);
        }
      }
    }
    return entityClasses;
  }

  protected AbstractEntity newInstance(Class<AbstractEntity> entityClass) {
    Constructor constructor = getMaxArgConstructor(entityClass);
    Class [] parameterTypes = constructor.getParameterTypes();
    Object[] arguments = getArgumentsForParameterTypes(parameterTypes);
    try {
      return (AbstractEntity) constructor.newInstance(arguments);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("newInstance for " + entityClass + " threw an Exception: " + e);
    }
    return null;
  }

  private Object[] getArgumentsForParameterTypes(Class[] parameterTypes) {
    Object [] arguments = new Object[parameterTypes.length];
    for (int i = 0; i < arguments.length; i++) {
      arguments[i] = getTestValueForType(parameterTypes[i]);
    }
    return arguments;
  }

  private Constructor getMaxArgConstructor(Class<AbstractEntity> entityClass)
  {
    int maxArgs = 0;
    Constructor maxArgConstructor = null;
    for (Constructor constructor : entityClass.getConstructors()) {
      if (Modifier.isPublic(constructor.getModifiers())) {
        int numArgs = constructor.getParameterTypes().length;
        if (numArgs > maxArgs) {
          maxArgs = numArgs;
          maxArgConstructor = constructor;
        }
      }
    }
    return maxArgConstructor;
  }
}
