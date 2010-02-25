// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.util.Iterator;
import java.util.Set;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.EntityNetworkPersister;
import edu.harvard.med.screensaver.model.TestDataFactory;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.InvalidCherryPickWellException;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class CherryPickRequestCherryPicksAdderTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(CherryPickRequestCherryPicksAdderTest.class);

  private static Function<ScreenerCherryPick,WellKey> scpToWellKey = new Function<ScreenerCherryPick,WellKey>()
  {
    public WellKey apply(ScreenerCherryPick scp) { return scp.getScreenedWell().getWellKey(); }
  };

  private static Function<LabCherryPick,WellKey> lcpToWellKey = new Function<LabCherryPick,WellKey>()
  {
    public WellKey apply(LabCherryPick lcp) { return lcp.getSourceWell().getWellKey(); }
  };

  protected CherryPickRequestCherryPicksAdder cherryPickRequestCherryPicksAdder;

  private RNAiCherryPickRequest cpr;
  private Library poolLibrary;
  private Library duplexLibrary;
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    
    TestDataFactory dataFactory = new TestDataFactory();

    duplexLibrary = dataFactory.newInstance(Library.class);
    duplexLibrary.setScreenType(ScreenType.RNAI);
    dataFactory.newInstance(LibraryContentsVersion.class, duplexLibrary);
    for (int i = 0; i < 8; ++i ) {
      Well well = duplexLibrary.createWell(new WellKey(duplexLibrary.getStartPlate(), 0, i), LibraryWellType.EXPERIMENTAL);
      well.createSilencingReagent(new ReagentVendorIdentifier("v", "d" + i), 
                                  SilencingReagentType.SIRNA,
                                  "ATCGGCTA");
    }
    duplexLibrary.getLatestContentsVersion().release(new AdministrativeActivity((AdministratorUser) duplexLibrary.getLatestContentsVersion().getLoadingActivity().getPerformedBy(), new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
    genericEntityDao.persistEntity(duplexLibrary);
    duplexLibrary = genericEntityDao.reloadEntity(duplexLibrary, true, 
                                                  Library.wells.to(Well.reagents).getPath(), 
                                                  Library.wells.to(Well.latestReleasedReagent).getPath());

    poolLibrary = dataFactory.newInstance(Library.class);
    poolLibrary.setScreenType(ScreenType.RNAI);
    dataFactory.newInstance(LibraryContentsVersion.class, poolLibrary);
    Well emptyWell = poolLibrary.createWell(new WellKey(poolLibrary.getStartPlate(), "A01"), LibraryWellType.EMPTY);
    Well poolWell1 = poolLibrary.createWell(new WellKey(poolLibrary.getStartPlate(), "A02"), LibraryWellType.EXPERIMENTAL);
    Well poolWell2 = poolLibrary.createWell(new WellKey(poolLibrary.getStartPlate(), "A03"), LibraryWellType.EXPERIMENTAL);
    Well redundantDuplexesPoolWell = poolLibrary.createWell(new WellKey(poolLibrary.getStartPlate(), "A04"), LibraryWellType.EXPERIMENTAL);
    Iterator<Well> duplexWellIter = duplexLibrary.getWells().iterator();
    emptyWell.createSilencingReagent(new ReagentVendorIdentifier("v", "1"), SilencingReagentType.SIRNA, null);
    poolWell1.createSilencingReagent(new ReagentVendorIdentifier("v", "2"), SilencingReagentType.SIRNA, null)
    .withDuplexWell(duplexWellIter.next()).withDuplexWell(duplexWellIter.next()).withDuplexWell(duplexWellIter.next()).withDuplexWell(duplexWellIter.next());
    SilencingReagent poolWell2Reagent = poolWell2.createSilencingReagent(new ReagentVendorIdentifier("v", "3"), SilencingReagentType.SIRNA, null)
    .withDuplexWell(duplexWellIter.next()).withDuplexWell(duplexWellIter.next()).withDuplexWell(duplexWellIter.next()).withDuplexWell(duplexWellIter.next());
    duplexWellIter = poolWell2Reagent.getDuplexWells().iterator();
    redundantDuplexesPoolWell.createSilencingReagent(new ReagentVendorIdentifier("v", "4"), SilencingReagentType.SIRNA, null)
    .withDuplexWell(duplexWellIter.next()).withDuplexWell(duplexWellIter.next()).withDuplexWell(duplexWellIter.next()).withDuplexWell(duplexWellIter.next());
    poolLibrary.getLatestContentsVersion().release(new AdministrativeActivity((AdministratorUser) poolLibrary.getLatestContentsVersion().getLoadingActivity().getPerformedBy(), new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
    genericEntityDao.persistEntity(poolLibrary);
    poolLibrary = genericEntityDao.reloadEntity(poolLibrary, true, Library.wells.to(Well.reagents).to(SilencingReagent.duplexWells).getPath());

    cpr = dataFactory.newInstance(RNAiCherryPickRequest.class);
    new EntityNetworkPersister(genericEntityDao, cpr).persistEntityNetwork();
  }

  public void testAddCherryPicks()
  {
    Set<WellKey> cpWells = Sets.newHashSet(new WellKey(poolLibrary.getStartPlate(), "A02"),
                                           new WellKey(poolLibrary.getStartPlate(), "A03"));
    CherryPickRequest cpr2 = cherryPickRequestCherryPicksAdder.addCherryPicksForWells(cpr, cpWells, false);
    assertEquals(cpWells,
                 Sets.newHashSet(Iterables.transform(cpr2.getScreenerCherryPicks(), scpToWellKey)));
    assertEquals(cpWells,
                 Sets.newHashSet(Iterables.transform(cpr2.getLabCherryPicks(), lcpToWellKey)));
  }

  public void testAddCherryPicksWithDeconvolution()
  {
    Set<WellKey> cpWells = Sets.newHashSet(new WellKey(poolLibrary.getStartPlate(), "A02"),
                                           new WellKey(poolLibrary.getStartPlate(), "A03"));
    CherryPickRequest cpr2 = cherryPickRequestCherryPicksAdder.addCherryPicksForWells(cpr, cpWells, true);
    assertEquals(cpWells,
                 Sets.newHashSet(Iterables.transform(cpr2.getScreenerCherryPicks(), scpToWellKey)));
    assertEquals(Sets.newHashSet(new WellKey(duplexLibrary.getStartPlate(), "A01"),
                                 new WellKey(duplexLibrary.getStartPlate(), "A02"),
                                 new WellKey(duplexLibrary.getStartPlate(), "A03"),
                                 new WellKey(duplexLibrary.getStartPlate(), "A04"),
                                 new WellKey(duplexLibrary.getStartPlate(), "A05"),
                                 new WellKey(duplexLibrary.getStartPlate(), "A06"),
                                 new WellKey(duplexLibrary.getStartPlate(), "A07"),
                                 new WellKey(duplexLibrary.getStartPlate(), "A08")),
                 Sets.newHashSet(Iterables.transform(cpr2.getLabCherryPicks(), lcpToWellKey)));
  }
  
  public void testAddCherryPicksIncrementally()
  {
    Set<WellKey> cpWells1 = Sets.newHashSet(new WellKey(poolLibrary.getStartPlate(), "A02"));
    /*CherryPickRequest cpr2 = */cherryPickRequestCherryPicksAdder.addCherryPicksForWells(cpr, cpWells1, false);
    Set<WellKey> cpWells2 = Sets.newHashSet(new WellKey(poolLibrary.getStartPlate(), "A03"));
    CherryPickRequest cpr3 = cherryPickRequestCherryPicksAdder.addCherryPicksForWells(cpr, cpWells2, false);
    Set<WellKey> cpWells = Sets.union(cpWells1, cpWells2);
    assertEquals(cpWells,
                 Sets.newHashSet(Iterables.transform(cpr3.getScreenerCherryPicks(), scpToWellKey)));
    assertEquals(cpWells,
                 Sets.newHashSet(Iterables.transform(cpr3.getLabCherryPicks(), lcpToWellKey)));
  }

  // TODO: this is a pain to test since allocation step requires a lot more data
  // setup (library copies, etc.), and it isn't really testing much at all
//  public void testAddCherryAfterAllocated()
//  {
//  }
  
  public void testAddCherryPicksTwice()
  {
    testAddCherryPicks();
    try {
      testAddCherryPicks();
      fail("expected DuplicateEntityException");
    }
    catch (DuplicateEntityException e) {
    }
  }
  
  public void testAddCherryPickInvalidWell()
  {
    Set<WellKey> cpWells = Sets.newHashSet(new WellKey(poolLibrary.getEndPlate() + duplexLibrary.getEndPlate(), "B01"));
    try {
      cherryPickRequestCherryPicksAdder.addCherryPicksForWells(cpr, cpWells, false);
      fail("expected InvalidCherryPickWellException");
    }
    catch (InvalidCherryPickWellException e) {
      assertTrue(e.getMessage().contains("no such well"));
    }
  }
  
  public void testAddCherryPicksWithDeconvolutionAndMissingDuplexes()
  {
    Set<WellKey> cpWells = Sets.newHashSet(new WellKey(poolLibrary.getStartPlate(), "A01"));
    cherryPickRequestCherryPicksAdder.addCherryPicksForWells(cpr, cpWells, true);
    assertEquals(1, cpr.getScreenerCherryPicks().size());
    assertEquals(0, cpr.getLabCherryPicks().size());
  }

  public void testAddDuplicateLabCherryPicks()
  {
    Set<WellKey> cpWells = Sets.newHashSet(new WellKey(poolLibrary.getStartPlate(), "A03"),
                                           new WellKey(poolLibrary.getStartPlate(), "A04"));
    cherryPickRequestCherryPicksAdder.addCherryPicksForWells(cpr, cpWells, true);
    assertEquals(2, cpr.getScreenerCherryPicks().size());
    assertEquals(2 * 4, cpr.getLabCherryPicks().size());
  }
}
