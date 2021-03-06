// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.beans.IntrospectionException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import junit.framework.TestSuite;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

public class AnnotationTypeTest extends AbstractEntityInstanceTest<AnnotationType>
{
  public static TestSuite suite()
  {
    return buildTestSuite(AnnotationTypeTest.class, AnnotationType.class);
  }

  @Autowired
  protected ScreenResultParser screenResultParser;
  @Autowired
  protected LibrariesDAO librariesDao;

  public AnnotationTypeTest()
  {
    super(AnnotationType.class);
  }

  public void testAnnotationValues()
  {
    schemaUtil.truncateTables();

    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Library library = dataFactory.newInstance(Library.class, getName());
        library.setStartPlate(1);
        library.setEndPlate(1);
        library.setScreenType(ScreenType.RNAI);
        library.createContentsVersion(dataFactory.newInstance(AdministratorUser.class));
        Well well1 = library.createWell(new WellKey(1, 0, 0), LibraryWellType.EXPERIMENTAL);
        Well well2 = library.createWell(new WellKey(1, 0, 1), LibraryWellType.EXPERIMENTAL);
        Reagent reagent1 = well1.createSilencingReagent(new ReagentVendorIdentifier("vendor", "1"), SilencingReagentType.SIRNA, "ATCG");
        Reagent reagent2 = well2.createSilencingReagent(new ReagentVendorIdentifier("vendor", "2"), SilencingReagentType.SIRNA, "GCTA");
        Screen study = dataFactory.newInstance(Screen.class);
        study.setFacilityId("S");
        study.setProjectPhase(ProjectPhase.ANNOTATION);
        AnnotationType at = study.createAnnotationType("annotation", "", false);
        assertNotNull(at.createAnnotationValue(reagent1, "a"));
        assertNotNull(at.createAnnotationValue(reagent2, "b"));
        assertNull("one annotation value per reagent/annotation type pair (transient test)", 
                   at.createAnnotationValue(reagent1, "c"));;
                   genericEntityDao.persistEntity(library);
                   genericEntityDao.persistEntity(study);
                   //new EntityNetworkPersister(genericEntityDao, study).persistEntityNetwork();
      }
    });

    Screen study = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "S", false, Screen.annotationTypes.to(AnnotationType.annotationValues).to(AnnotationValue.reagent));
    AnnotationType at = study.getAnnotationTypes().last();
    assertEquals(at.getName(), "annotation");
    Reagent reagent1 = genericEntityDao.findEntityByProperty(Reagent.class, "vendorId.vendorIdentifier", "1");
    Reagent reagent2 = genericEntityDao.findEntityByProperty(Reagent.class, "vendorId.vendorIdentifier", "2");
    assertNotNull(reagent1);
    assertNotNull(reagent2);
    assertEquals(Sets.newHashSet(reagent1, reagent2), 
                 Sets.newHashSet(at.getAnnotationValues().keySet())); 
    assertEquals(Sets.newHashSet("a", "b"), 
                 Sets.newHashSet(Iterables.transform(at.getAnnotationValues().values(), 
                                                     new Function<AnnotationValue,String>() { public String apply(AnnotationValue av) { return av.getValue(); } }))); 
    assertNull("one annotation value per reagent/annotation type pair (persisted test)", 
               at.createAnnotationValue(reagent1, "d"));
  }
}

