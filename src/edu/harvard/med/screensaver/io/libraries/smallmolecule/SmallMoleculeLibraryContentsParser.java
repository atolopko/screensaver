// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/libraries/compound/SmallMoleculeLibraryContentsLoader.java
// $
// $Id: SmallMoleculeLibraryContentsLoader.java 1990 2007-10-24 02:12:17Z ant4
// $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.smallmolecule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.io.libraries.InvalidWellException;
import edu.harvard.med.screensaver.io.libraries.LibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

/**
 * Parses the contents of a small molecule Library from an SDFile into the
 * domain model.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class SmallMoleculeLibraryContentsParser extends LibraryContentsParser<SmallMoleculeReagent>
{
  private static final Logger log = Logger.getLogger(SmallMoleculeLibraryContentsParser.class);
  private SDRecordParser _sdRecordParser;

  public SmallMoleculeLibraryContentsParser(GenericEntityDAO dao, InputStream stream, Library library) throws IOException
  {
    super(dao, stream, library);
    _sdRecordParser = new SDRecordParser(new BufferedReader(new InputStreamReader(getStream())));
  }

  public Pair<Well,SmallMoleculeReagent> parseNext() throws ParseException, IOException
  {
    SDRecord record = _sdRecordParser.next();
    if (record == null) {
      return null;
    }
    // validate required fields
    if (record.getPlateNumber() == null) {
      throw new ParseException(new ParseError("plate_number is required", _sdRecordParser.getLineNumber()));
    }
    if (record.getWellName() == null) {
      throw new ParseException(new ParseError("well is required", _sdRecordParser.getLineNumber()));
    }
    if (record.getSmiles() == null && record.getLibraryWellType() == LibraryWellType.EXPERIMENTAL) {
      log.warn(new ParseError("smiles is required for 'experimental' well", _sdRecordParser.getLineNumber()).toString());
      //[#2037] throw new ParseException(new ParseError("smiles is required for 'experimental' well", _sdRecordParser.getLineNumber()));
    }
    if (record.getInChi() == null && record.getLibraryWellType() == LibraryWellType.EXPERIMENTAL) {
      log.warn(new ParseError("inchi is required for 'experimental' well", _sdRecordParser.getLineNumber()).toString());
      //[#2037] throw new ParseException(new ParseError("inchi is required for 'experimental' well", _sdRecordParser.getLineNumber()));
    }
    try {
      return updateWell(record, getDao(), getLibrary());
    }
    catch (Exception e) {
      throw new ParseException(new ParseError(e.getMessage(), _sdRecordParser.getLineNumber()));
    }
  }

  private Pair<Well,SmallMoleculeReagent> updateWell(SDRecord sdRecordData, GenericEntityDAO dao, Library library) 
    throws IOException
  {
    String molfile = sdRecordData.getMolfile();
    if (molfile == null) {
      log.warn("encountered an SD record with an empty MDL molfile specification");
    }
    
    //TODO: enforce "required" fields?
    // "Plate" "Well" Well_type", and in some cases "Chemical_Name" ([#1420])
    
    WellKey key = new WellKey(sdRecordData.getPlateNumber(),
                              sdRecordData.getWellName());
    Well well = dao.findEntityById(Well.class, key.getKey(), false, Well.library.getPath());
    if (well == null) {
      throw new InvalidWellException("no such well " + key);
    }
    if (!well.getLibrary().equals(library)) {
      throw new InvalidWellException("well " + key + " not in library " + library.getLibraryName());
    }
    if (sdRecordData.getLibraryWellType() != LibraryWellType.UNDEFINED &&
      well.getLibraryWellType() != sdRecordData.getLibraryWellType()) {
      log.warn("well type for well " + key + " changed from " + well.getLibraryWellType() + " to " + sdRecordData.getLibraryWellType());
      well.setLibraryWellType(sdRecordData.getLibraryWellType());
    }
    if (sdRecordData.getFacilityId() != null &&
        ! sdRecordData.getFacilityId().equals(well.getFacilityId())) {
      log.warn("facility ID for well " + key + " changed from " + well.getFacilityId() + " to " + sdRecordData.getFacilityId());
      well.setFacilityId(sdRecordData.getFacilityId());
    }
    SmallMoleculeReagent reagent = null;
    if (sdRecordData.getLibraryWellType() == LibraryWellType.EXPERIMENTAL) {
      reagent = addSmallMoleculeReagentForWell(well, sdRecordData);
    }
    return new Pair<Well,SmallMoleculeReagent>(well, reagent);
  }

  /**
   * Retrieve or create a small molecule reagent for the smiles.
   */
  private SmallMoleculeReagent addSmallMoleculeReagentForWell(Well well, SDRecord sdRecordData)
  {
    ReagentVendorIdentifier rvi = new ReagentVendorIdentifier(sdRecordData.getVendor(),
                                                              sdRecordData.getVendorIdentifier());
    SmallMoleculeReagent smallMoleculeReagent =
      well.createSmallMoleculeReagent(rvi,
                                      sdRecordData.getMolfile(),
                                      sdRecordData.getSmiles(),
                                      sdRecordData.getInChi(),
                                      sdRecordData.getMolecularMass(),
                                      sdRecordData.getMolecularWeight(),
                                      sdRecordData.getMolecularFormula(),
                                      false);
    smallMoleculeReagent.getCompoundNames().addAll(sdRecordData.getCompoundNames());
    smallMoleculeReagent.getPubchemCids().addAll(sdRecordData.getPubchemCids());
    smallMoleculeReagent.getChembankIds().addAll(sdRecordData.getChembankIds());
    return smallMoleculeReagent;
  }
}
