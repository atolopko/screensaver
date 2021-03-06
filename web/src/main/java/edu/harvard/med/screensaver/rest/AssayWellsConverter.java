
package edu.harvard.med.screensaver.rest;

import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

public class AssayWellsConverter extends RestConverter
{
	
  public static class AssayWellsRequest
  {
    public ScreenResult sr;
  
    public AssayWellsRequest(ScreenResult sr)
    {
      this.sr = sr;
    }
  }	
  private static final Logger log = Logger.getLogger(AssayWellsConverter.class);
  
  @Autowired
  private AssayWellConverter assayWellConverter;

  public boolean canConvert(Class clazz)
  {
    return AssayWellsConverter.AssayWellsRequest.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer,
                        MarshallingContext context)
  {

    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
      	ScreenResult sr = getDao().reloadEntity(((AssayWellsConverter.AssayWellsRequest)value).sr);
        for(AssayWell aw:sr.getAssayWells())
        {
          writer.startNode(getNodeName(AssayWell.class));
          //aw = getDao().reloadEntity(aw);
          assayWellConverter.write(writer, aw);
          writer.endNode();
        }
      }
    });
    
  }


  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}