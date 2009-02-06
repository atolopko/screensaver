// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.harvard.med.screensaver.io.libraries.compound.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.libraries.CompoundViewer;
import edu.harvard.med.screensaver.ui.libraries.GeneViewer;
import edu.harvard.med.screensaver.ui.libraries.ReagentViewer;

import org.apache.log4j.Logger;

/**
 * A NameValueTable for the Reagent Viewer.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ReagentNameValueTable extends ComboNameValueTable
{
  private static Logger log = Logger.getLogger(ReagentNameValueTable.class);

  public ReagentNameValueTable(Reagent reagent,
                               ReagentViewer reagentViewer,
                               GeneViewer geneViewer,
                               CompoundViewer compoundViewer,
                               StructureImageProvider structureImageProvider)
  {
    initialize(reagent, reagentViewer, geneViewer, compoundViewer, structureImageProvider, new ReagentDetailsNameValueTable(reagent));
  }

  protected ReagentNameValueTable(Reagent reagent,
                                  ReagentViewer reagentViewer,
                                  GeneViewer geneViewer,
                                  CompoundViewer compoundViewer,
                                  StructureImageProvider structureImageProvider,
                                  NameValueTable detailsNameValueTable)
  {
    initialize(reagent, reagentViewer, geneViewer, compoundViewer, structureImageProvider, detailsNameValueTable);
  }

  private void initialize(Reagent reagent,
                          ReagentViewer reagentViewer,
                          GeneViewer geneViewer,
                          CompoundViewer compoundViewer,
                          StructureImageProvider structureImageProvider,
                          NameValueTable detailsNameValueTable)
  {
    List<NameValueTable> comboNameValueTables = new ArrayList<NameValueTable>();
    comboNameValueTables.add(detailsNameValueTable);
    // TODO: once Well.{compounds,silencingReagents} are moved to Reagent, we won't need to go through well anymore
    if (reagent != null) {
      Iterator<Well> iterator = reagent.getWells().iterator();
      if (iterator.hasNext()) {
        Well well = iterator.next();
        for (Gene gene : well.getGenes()) {
          comboNameValueTables.add(new GeneNameValueTable(gene, geneViewer, reagentViewer));
        }
        for (Compound compound : well.getOrderedCompounds()) {
          comboNameValueTables.add(new CompoundNameValueTable(compound, 
                                                              compoundViewer,
                                                              structureImageProvider,
                                                              reagentViewer));
        }
      }
    }
    initializeComboNameValueTable((NameValueTable [])
      comboNameValueTables.toArray(new NameValueTable [0]));
  }

}
