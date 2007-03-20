// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.io.screenresults.MockDaoForScreenResultImporter;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;

import org.apache.log4j.Logger;

public class RNAiCherryPickRequestTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(RNAiCherryPickRequestTest.class);



  public void testFindCherryPickSourceWells()
  {
    // TODO: consider testing same gene in 2 libs, but with different silencing reagents (shared gene, above, also sahres same silencing reagents between the 2 libraries)
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library poolLibrary1 = new Library("Pools Library1", "pools lib1", ScreenType.RNAI, LibraryType.COMMERCIAL, 1, 1);
        Library duplexLibrary1 = new Library("Duplexes Library1", "duplexes lib1", ScreenType.RNAI, LibraryType.COMMERCIAL, 2, 5);
        Library poolLibrary2 = new Library("Pools Library2", "pools lib2", ScreenType.RNAI, LibraryType.COMMERCIAL, 6, 61);
        Library duplexLibrary2 = new Library("Duplexes Library2", "duplexes lib2", ScreenType.RNAI, LibraryType.COMMERCIAL, 7, 10);

        Gene gene1 = new Gene("ANT1", 1, "ant1", "Human");
        Gene gene2 = new Gene("ANT2", 2, "ant2", "Human");
        Gene gene3 = new Gene("ANT3", 3, "ant3", "Human");

        // shared by lib1 and lib2
        SilencingReagent silencingReagent1_1 = new SilencingReagent(gene1, SilencingReagentType.SIRNA, "GATTACA");
        SilencingReagent silencingReagent1_2 = new SilencingReagent(gene1, SilencingReagentType.SIRNA, "GATTACC");
        SilencingReagent silencingReagent1_3 = new SilencingReagent(gene1, SilencingReagentType.SIRNA, "GATTACT");
        SilencingReagent silencingReagent1_4 = new SilencingReagent(gene1, SilencingReagentType.SIRNA, "GATTACG");

        // lib1 only
        SilencingReagent silencingReagent2_1 = new SilencingReagent(gene2, SilencingReagentType.SIRNA, "GATTAAA");
        SilencingReagent silencingReagent2_2 = new SilencingReagent(gene2, SilencingReagentType.SIRNA, "GATTAAC");
        SilencingReagent silencingReagent2_3 = new SilencingReagent(gene2, SilencingReagentType.SIRNA, "GATTAAT");
        SilencingReagent silencingReagent2_4 = new SilencingReagent(gene2, SilencingReagentType.SIRNA, "GATTAAG");

        // lib2 only
        SilencingReagent silencingReagent3_1 = new SilencingReagent(gene3, SilencingReagentType.SIRNA, "GATTAGA");
        SilencingReagent silencingReagent3_2 = new SilencingReagent(gene3, SilencingReagentType.SIRNA, "GATTAGC");
        SilencingReagent silencingReagent3_3 = new SilencingReagent(gene3, SilencingReagentType.SIRNA, "GATTAGT");
        SilencingReagent silencingReagent3_4 = new SilencingReagent(gene3, SilencingReagentType.SIRNA, "GATTAGG");

        // gene 1
        Well poolWell1_1 = new Well(poolLibrary1, 1, "A01");
        poolWell1_1.addSilencingReagent(silencingReagent1_1);
        poolWell1_1.addSilencingReagent(silencingReagent1_2);
        poolWell1_1.addSilencingReagent(silencingReagent1_3);
        poolWell1_1.addSilencingReagent(silencingReagent1_4);

        Well duplexWell1_1_1 = new Well(duplexLibrary1, 2, "A01");
        Well duplexWell1_1_2 = new Well(duplexLibrary1, 3, "B02");
        Well duplexWell1_1_3 = new Well(duplexLibrary1, 4, "C03");
        Well duplexWell1_1_4 = new Well(duplexLibrary1, 5, "D04");
        duplexWell1_1_1.addSilencingReagent(silencingReagent1_1);
        duplexWell1_1_2.addSilencingReagent(silencingReagent1_2);
        duplexWell1_1_3.addSilencingReagent(silencingReagent1_3);
        duplexWell1_1_4.addSilencingReagent(silencingReagent1_4);

        Well poolWell2_1 = new Well(poolLibrary2, 6, "A01");
        poolWell2_1.addSilencingReagent(silencingReagent1_1);
        poolWell2_1.addSilencingReagent(silencingReagent1_2);
        poolWell2_1.addSilencingReagent(silencingReagent1_3);
        poolWell2_1.addSilencingReagent(silencingReagent1_4);

        Well duplexWell2_1_1 = new Well(duplexLibrary2, 7, "A01");
        Well duplexWell2_1_2 = new Well(duplexLibrary2, 8, "B02");
        Well duplexWell2_1_3 = new Well(duplexLibrary2, 9, "C03");
        Well duplexWell2_1_4 = new Well(duplexLibrary2, 10, "D04");
        duplexWell2_1_1.addSilencingReagent(silencingReagent1_1);
        duplexWell2_1_2.addSilencingReagent(silencingReagent1_2);
        duplexWell2_1_3.addSilencingReagent(silencingReagent1_3);
        duplexWell2_1_4.addSilencingReagent(silencingReagent1_4);

        // gene 2
        Well poolWell1_2 = new Well(poolLibrary1, 1, "B02");
        poolWell1_2.addSilencingReagent(silencingReagent2_1);
        poolWell1_2.addSilencingReagent(silencingReagent2_2);
        poolWell1_2.addSilencingReagent(silencingReagent2_3);
        poolWell1_2.addSilencingReagent(silencingReagent2_4);

        Well duplexWell1_2_1 = new Well(duplexLibrary1, 2, "A02");
        Well duplexWell1_2_2 = new Well(duplexLibrary1, 3, "B03");
        Well duplexWell1_2_3 = new Well(duplexLibrary1, 4, "C04");
        Well duplexWell1_2_4 = new Well(duplexLibrary1, 5, "D05");
        duplexWell1_2_1.addSilencingReagent(silencingReagent2_1);
        duplexWell1_2_2.addSilencingReagent(silencingReagent2_2);
        duplexWell1_2_3.addSilencingReagent(silencingReagent2_3);
        duplexWell1_2_4.addSilencingReagent(silencingReagent2_4);

        // gene 3
        Well poolWell2_2 = new Well(poolLibrary2, 6, "B02");
        poolWell2_2.addSilencingReagent(silencingReagent3_1);
        poolWell2_2.addSilencingReagent(silencingReagent3_2);
        poolWell2_2.addSilencingReagent(silencingReagent3_3);
        poolWell2_2.addSilencingReagent(silencingReagent3_4);

        Well duplexWell2_2_1 = new Well(duplexLibrary2, 7, "A02");
        Well duplexWell2_2_2 = new Well(duplexLibrary2, 8, "B03");
        Well duplexWell2_2_3 = new Well(duplexLibrary2, 9, "C04");
        Well duplexWell2_2_4 = new Well(duplexLibrary2, 10, "D05");
        duplexWell2_2_1.addSilencingReagent(silencingReagent3_1);
        duplexWell2_2_2.addSilencingReagent(silencingReagent3_2);
        duplexWell2_2_3.addSilencingReagent(silencingReagent3_3);
        duplexWell2_2_4.addSilencingReagent(silencingReagent3_4);
        
        dao.persistEntity(poolLibrary1);
        dao.persistEntity(duplexLibrary1);
        dao.persistEntity(poolLibrary2);
        dao.persistEntity(duplexLibrary2);
      }
    });
    
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(1);
        RNAiCherryPickRequest rnaiCherryPickRequest = new RNAiCherryPickRequest(screen, screen.getLeadScreener(), new Date());

        // chery pick pool whose gene is both library 1 and 2, but only cherry picked from library 1
        {
          Set<Well> poolCherryPickWells = new HashSet<Well>();
          poolCherryPickWells.add(dao.findWell(new WellKey(1, "A01")));
          Set<Well> actualDuplexCherryPickWells = rnaiCherryPickRequest.findCherryPickSourceWells(poolCherryPickWells);
          Set<WellKey> actualDuplexCherryPickWellKeys = new TreeSet<WellKey>();
          for (Well well : actualDuplexCherryPickWells) {
            actualDuplexCherryPickWellKeys.add(well.getWellKey());
          }
          TreeSet<WellKey> expectedDuplexCherryPickWellKeys = new TreeSet<WellKey>();
          expectedDuplexCherryPickWellKeys.add(new WellKey(2, "A01"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(3, "B02"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(4, "C03"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(5, "D04"));

          assertEquals(expectedDuplexCherryPickWellKeys, actualDuplexCherryPickWellKeys);
        }
        
        // chery pick pool whose gene is both library 1 and 2, and cherry picked from both libraries
        {
          Set<Well> poolCherryPickWells = new HashSet<Well>();
          poolCherryPickWells.add(dao.findWell(new WellKey(1, "A01")));
          poolCherryPickWells.add(dao.findWell(new WellKey(6, "A01")));
          Set<Well> actualDuplexCherryPickWells = rnaiCherryPickRequest.findCherryPickSourceWells(poolCherryPickWells);
          Set<WellKey> actualDuplexCherryPickWellKeys = new TreeSet<WellKey>();
          for (Well well : actualDuplexCherryPickWells) {
            actualDuplexCherryPickWellKeys.add(well.getWellKey());
          }
          TreeSet<WellKey> expectedDuplexCherryPickWellKeys = new TreeSet<WellKey>();
          expectedDuplexCherryPickWellKeys.add(new WellKey(2, "A01"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(3, "B02"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(4, "C03"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(5, "D04"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(7, "A01"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(8, "B02"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(9, "C03"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(10, "D04"));

          assertEquals(expectedDuplexCherryPickWellKeys, actualDuplexCherryPickWellKeys);
        }
        
        // chery pick pool whose gene is only library1
        {
          Set<Well> poolCherryPickWells = new HashSet<Well>();
          poolCherryPickWells.add(dao.findWell(new WellKey(1, "B02")));
          Set<Well> actualDuplexCherryPickWells = rnaiCherryPickRequest.findCherryPickSourceWells(poolCherryPickWells);
          Set<WellKey> actualDuplexCherryPickWellKeys = new TreeSet<WellKey>();
          for (Well well : actualDuplexCherryPickWells) {
            actualDuplexCherryPickWellKeys.add(well.getWellKey());
          }
          TreeSet<WellKey> expectedDuplexCherryPickWellKeys = new TreeSet<WellKey>();
          expectedDuplexCherryPickWellKeys.add(new WellKey(2, "A02"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(3, "B03"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(4, "C04"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(5, "D05"));

          assertEquals(expectedDuplexCherryPickWellKeys, actualDuplexCherryPickWellKeys);
        }
        
        // chery pick pool whose gene is only in library2
        {
          Set<Well> poolCherryPickWells = new HashSet<Well>();
          poolCherryPickWells.add(dao.findWell(new WellKey(6, "B02")));
          Set<Well> actualDuplexCherryPickWells = rnaiCherryPickRequest.findCherryPickSourceWells(poolCherryPickWells);
          Set<WellKey> actualDuplexCherryPickWellKeys = new TreeSet<WellKey>();
          for (Well well : actualDuplexCherryPickWells) {
            actualDuplexCherryPickWellKeys.add(well.getWellKey());
          }
          TreeSet<WellKey> expectedDuplexCherryPickWellKeys = new TreeSet<WellKey>();
          expectedDuplexCherryPickWellKeys.add(new WellKey(7, "A02"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(8, "B03"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(9, "C04"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(10, "D05"));

          assertEquals(expectedDuplexCherryPickWellKeys, actualDuplexCherryPickWellKeys);
        }
      }
    });
  }
}
