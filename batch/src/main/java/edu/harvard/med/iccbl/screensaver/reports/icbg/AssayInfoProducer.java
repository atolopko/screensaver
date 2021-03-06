// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.reports.icbg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.google.common.collect.Maps;

import edu.harvard.med.screensaver.io.workbook2.Row;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.util.StringUtils;


/**
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class AssayInfoProducer
{
  private static Logger log = Logger.getLogger(AssayInfoProducer.class);
	private String _categoryFilename;
	private Map<String,String> assayCategories;
  
  public AssayInfoProducer(String filename) throws FileNotFoundException {
  	if(!StringUtils.isEmpty(filename)) _categoryFilename = filename;

    if(_categoryFilename != null)
    {
    	assayCategories = Maps.newHashMap();
    	Workbook book = new Workbook(new File(_categoryFilename));
    	for(Row row:book.getWorksheet(0)) {
    		String temp = row.getCell(0).getString();
    		temp = temp.replace("ICCBL", "");
    		assayCategories.put(temp,row.getCell(1).getString());
    		
    	}
    }
  }

  public AssayInfo getAssayInfoForScreen(Screen screen) throws FileNotFoundException
  {
    AssayInfo assayInfo = new AssayInfo();
    assayInfo.setAssayName("ICCBL" + screen.getFacilityId());
    assayInfo.setProtocolDescription(screen.getTitle());
    assayInfo.setPNote(screen.getSummary());
    assayInfo.setInvestigator(screen.getLabHead() == null ? "" : screen.getLabHead().getLastName().toUpperCase());

    String assayCategoryText = "";
    for (String keyword : screen.getKeywords()) {
      assayCategoryText += keyword.toUpperCase() + " ";
    }
    if(assayCategories != null) {
    	assayInfo.setAssayCategory(assayCategories.get(screen.getFacilityId()));
    }else {
	    assayCategoryText += screen.getSummary().toUpperCase();
	    setAssayCategory(assayInfo, assayCategoryText);
    }
    LocalDate assayDate = screen.getDateCreated().toLocalDate();
    for (StatusItem statusItem: screen.getStatusItems()) {
      LocalDate statusItemDate = statusItem.getStatusDate();
      if (assayDate == null || assayDate.compareTo(statusItemDate) < 0) {
        assayDate = statusItemDate;
      }
    }

    assayInfo.setAssayDate(
      assayDate.getMonthOfYear() + "/" +
      assayDate.getDayOfMonth() + "/" +
      (assayDate.getYear()));
    assert assayDate.getYear() >= 1900 : "year should include century";
    


    return assayInfo;
  }

  private void setAssayCategory(AssayInfo assayInfo, String assayCategoryText)
  {
    // TODO: the just HAS to be a better way to do this. July!

    // NOTE: assayCategory is a CHAR(30) in NAPIS. some existing values are getting truncated in the
    // ICBG report.
    if (
      assayCategoryText.contains("MALARIA")) {
      assayInfo.setAssayCategory("ANTI-MALARIA");
    }
    else if (
      assayCategoryText.contains("DIABETES") ||
      assayCategoryText.contains("OBESITY")) {
      assayInfo.setAssayCategory("OBESITY_DIABETES");
    }
    else if (
      assayCategoryText.contains("TRYPANOSOMIASIS")) {
      assayInfo.setAssayCategory("TRYPANOSOMIASIS");
    }
    else if (
      assayCategoryText.contains("LEISHMANIASIS")) {
      assayInfo.setAssayCategory("LEISHMANIASIS");
    }
    else if (
      assayCategoryText.contains("INFLAM")) {
      assayInfo.setAssayCategory("INFLAMMATION");
    }
    else if (
      assayCategoryText.contains("APOPTOSIS") ||
      assayCategoryText.contains("CYTOTOXIC") ||
      assayCategoryText.contains("CELL DEATH")) {
      assayInfo.setAssayCategory("CYTOTOXICITY");
    }
    else if (
      assayCategoryText.contains("CONTRACEPTION")) {
      assayInfo.setAssayCategory("CONTRACEPTION");
    }
    else if (
      assayCategoryText.contains("CHAGAS")) {
      assayInfo.setAssayCategory("CHAGAS_DISEASE");
    }
    else if (
      assayCategoryText.contains("CARDIOVASC")) {
      assayInfo.setAssayCategory("CARDIOVASCULAR");
    }
    else if (
      assayCategoryText.contains("CANCER") ||
      assayCategoryText.contains("FANCONI ANEMIA/BRCA PATHWAY") ||
      assayCategoryText.contains("E6-DEPENDENT DEGRADATION OF P53") ||
      assayCategoryText.contains("NEUROBLASTOMA") ||
      assayCategoryText.contains("FANCD2") ||
      assayCategoryText.contains("WNT PATHWAY")) {
      assayInfo.setAssayCategory("ANTI-CANCER");
    }
    else if (
      assayCategoryText.contains("TUBERCULOSIS")) {
      assayInfo.setAssayCategory("ANTI-TUBERCULOSIS");
    }
    else if (
      assayCategoryText.contains("FUNG")) {
      assayInfo.setAssayCategory("ANTI-FUNGAL");
    }
    else if (
      assayCategoryText.contains("ANTHRAX") ||
      assayCategoryText.contains("BOTULINUM") ||
      assayCategoryText.contains("TULARENSIS") ||
      assayCategoryText.contains("TULARENSIS") ||
      assayCategoryText.contains("CHOLER") ||
      assayCategoryText.contains("AERUGINOSA") ||
      assayCategoryText.contains("YERSINIA PESTIS") ||
      assayCategoryText.contains("AMINOARABINOSE") ||
      assayCategoryText.contains("LEGIONELLA") ||
      assayCategoryText.contains("SALMONELLA") ||
      assayCategoryText.contains("A SCREEN FOR COMPOUNDS THAT INHIBIT GROWTH OF E. COLI") ||
      assayCategoryText.contains("BACTERI")) {
      assayInfo.setAssayCategory("ANTI-BACTERIAL");
    }
    else if (
      assayCategoryText.contains("ANTIVIRAL") ||
      assayCategoryText.contains("INFLUENZA") ||
      assayCategoryText.contains("VACCINIA") ||
      assayCategoryText.contains("WEST NILE") ||
      assayCategoryText.contains("VIRUS")) {
      assayInfo.setAssayCategory("ANTI-VIRAL_NO_HIV");
    }
    else if (
      assayCategoryText.contains("AIDS") ||
      assayCategoryText.contains("HIV")) {
      assayInfo.setAssayCategory("ANTI-HIV_AIDS");
    }
    else if (
      assayCategoryText.contains("INHIBIT THE ATPASE ACTIVITY")) {
      assayInfo.setAssayCategory("OTHER: INHIBIT ATPASE ACTIVITY");
    }
    else if (
      assayCategoryText.contains("INHIBIT THE RANGTPASE SYSTEM")) {
      assayInfo.setAssayCategory("OTHER: INHIBIT RANGTPASE SYSTEM");
    }
    else if (
      assayCategoryText.contains("CRYPTOSPORIDIUM PARVUM")) {
      assayInfo.setAssayCategory("OTHER: ANTI-PROTOZOA");
    }
    else if (
      assayCategoryText.contains("INHIBIT TNF-ALPHA-MEDIATED NECROSIS IN JURKAT CELLS")) {
      assayInfo.setAssayCategory("OTHER: NEURODEGENERATIVE DISEASE");
    }
    else if (
      assayCategoryText.contains("SHWACHMAN DIAMOND SYNDROME")) {
      assayInfo.setAssayCategory("OTHER: SHWACHMAN DIAMOND SYNDROME");
    }
    else if (
      assayCategoryText.contains("DOWN SYNDROME")) {
      assayInfo.setAssayCategory("OTHER: DOWN SYNDROME");
    }
    else if (
      assayCategoryText.contains("MULLERIAN INHIBITING SUBSTANCE")) {
      assayInfo.setAssayCategory("OTHER: HUMAN INFERTILITY");
    }
    else if (
      assayCategoryText.contains("ADULT ONSET DEAFNESS SYNDROME")) {
      assayInfo.setAssayCategory("OTHER: ADULT ONSET DEAFNESS");
    }
    else if (
      assayCategoryText.contains("S. CEREVISIAE")) {
      assayInfo.setAssayCategory("OTHER: S. CEREVISIAE");
    }
    else if (
      assayCategoryText.contains("SUPPRESS S6 PHOSPHORYLATION") ||
      assayCategoryText.contains("LUCIFERASE")) {
      assayInfo.setAssayCategory("OTHER: BIOLUMINESCENCE");
    }
    else if (
      assayCategoryText.contains("BINUCLEATE CELLS IN DROSOPHILA CELLS")) {
      assayInfo.setAssayCategory("OTHER: CELL DIVISION");
    }
    else if (
      assayCategoryText.contains("MITOCHONDRIAL PROLIFERATION")) {
      assayInfo.setAssayCategory("OTHER: MITOCHONDRIAL PROLIFERATION");
    }
    else if (
      assayCategoryText.contains("INHIBIT DNA REPLICATION")) {
      assayInfo.setAssayCategory("OTHER: INHIBIT DNA REPLICATION");
    }
    else if (
      assayCategoryText.contains("EMBRYONIC STEM (ES) CELL")) {
      assayInfo.setAssayCategory("OTHER: EMBRYONIC STEM CELLS");
    }
    else if (
      assayCategoryText.contains("IRON UPTAKE")) {
      assayInfo.setAssayCategory("OTHER: ANEMIA");
    }
    else if (
      assayCategoryText.contains("LILY POLLEN TUBES")) {
      assayInfo.setAssayCategory("OTHER: LILY POLLEN TUBES");
    }
    else if (
      assayCategoryText.contains("SIRT1")) {
      assayInfo.setAssayCategory("OTHER: ANTI-AGING");
    }
    else {
      log.info("assigning assay category OTHER for assay category text: " + assayCategoryText);
      assayInfo.setAssayCategory("OTHER");
    }
  }
}
