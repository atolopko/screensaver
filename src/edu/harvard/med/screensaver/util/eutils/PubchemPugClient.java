// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * An abstract class providing utility methods for querying PubChem via the
 * PUG (Power Users Gateway) interface.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public abstract class PubchemPugClient extends EutilsUtils
{
  // static members

  private static final Logger log = Logger.getLogger(PubchemPugClient.class);
  protected static final String PUG_URL = "http://pubchem.ncbi.nlm.nih.gov/pug/pug.cgi";
  protected static final int NUM_RETRIES = 5;
  protected static final int CONNECT_TIMEOUT = 5000;


  // protected and private instance methods
  
  abstract protected void reportError(String errorMessage);

  abstract protected List<String> getResultsFromOutputDocument(Document outputDocument) throws EutilsException;


  //TODO: this does the same thing as initializeDocumentBuilder on the base class
  protected PubchemPugClient()
  {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      _documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      reportError(e.getMessage());
    }
  }
  
  protected List<String> getResultsForSearchDocument(Document searchDocument) throws EutilsException
  {
    Document outputDocument = getXMLForPugQuery(searchDocument);
    String reqid;
    while ((reqid = getReqidFromOutputDocument(outputDocument)) != null) {
      sleep(1000);
      Document pollDocument = createPollDocumentForReqid(reqid);      
      outputDocument = getXMLForPugQuery(pollDocument);
    }
    if (! isJobSuccessfullyCompleted(outputDocument)) {
      return null;
    }
    if (! hasResults(outputDocument)) {
      return new ArrayList<String>();
    }
    return getResultsFromOutputDocument(outputDocument);
  }

  protected Document getXMLForPugQuery(Document query) throws EutilsException
  {
    URL url = getUrlForUrlString(PUG_URL);
    for (int i = 0; i < NUM_RETRIES; i ++) {
      try {
        return getXMLForPugQuery0(url, query);
      }
      catch (EutilsConnectionException e) {
      }
    }
    reportError("couldnt get XML for query after " + NUM_RETRIES + " tries.");
    return null;
  }

  private Document getXMLForPugQuery0(URL url, Document query)
  throws EutilsConnectionException
  {
    logDocument("query document", query);
    InputStream esearchContent = getResponseContent(url, query);
    assert(esearchContent != null);
    Document response = getDocumentFromInputStream(esearchContent);
    logDocument("response document", response);
    return response;
  }

  private InputStream getResponseContent(URL url, Document query)
  throws EutilsConnectionException
  {
    try {
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(CONNECT_TIMEOUT);
      connection.setReadTimeout(CONNECT_TIMEOUT);
      connection.setDoOutput(true);
      printDocumentToOutputStream(query, connection.getOutputStream());
      connection.connect();
      return connection.getInputStream();
    }
    catch (Exception e) {
      log.warn("failed to connect to PUG URL \"" + url + "\"", e);
      throw new EutilsConnectionException();
    }
  }

  protected Element createParentedElement(Document document, Node parent, String elementName) {
    Element element = (Element) document.createElement(elementName);
    parent.appendChild(element);
    return element;
  }

  protected Text createParentedTextNode(Document document, Node parent, String text) {
    Text textNode = (Text) document.createTextNode(text);
    parent.appendChild(textNode);
    return textNode;
  }

  protected boolean isJobCompleted(Document outputDocument) {
    if (outputDocument == null) {
      // this happens when there was a connection error in one of the handshakes, with the
      // specified TIMEOUT and NUM_RETRIES. the error has already been reported. the job should
      // be considered completed in this circumstance
      return true;
    }
    String statusValue = getStatusValueFromOutputDocument(outputDocument); 
    return ! (statusValue.equals("running") || statusValue.equals("queued"));
  }

  /**
   * Get the status value from the non-null output document.
   */
  private String getStatusValueFromOutputDocument(Document outputDocument) {
    NodeList nodes = outputDocument.getElementsByTagName("PCT-Status");
    if (nodes.getLength() != 1) {
      throw new RuntimeException("PCT-Status node count in response != 1: " + nodes.getLength());
    }
    Element element = (Element) nodes.item(0);
    return element.getAttribute("value");
  }

  /**
   * Check the output document to see if the job completed successfully. If it has not, and the
   * error has not previously been reported, then report the error. In any case return true
   * whenever the job completed successfully.
   * @param outputDocument the output document to check for successful job completion
   * @return true whenever the job completed successfully
   */
  protected boolean isJobSuccessfullyCompleted(Document outputDocument) {
    if (outputDocument == null) {
      // this happens when there was a connection error in one of the handshakes, with the
      // specified TIMEOUT and NUM_RETRIES. the error has already been reported.
      return false;
    }
    String statusValue = getStatusValueFromOutputDocument(outputDocument);
    if (statusValue.equals("success")) {
      return true;
    }
    String errorMessage = findErrorMessage(outputDocument);
    reportError(
      "PUG server reported non-success status '" + statusValue +
      "' with error message '" + errorMessage + "'");
    return false;
  }

  protected String findErrorMessage(Document outputDocument)
  {
    String errorMessage;
    NodeList nodes = outputDocument.getElementsByTagName("PCT-Status-Message_message");
    if (nodes.getLength() != 1) {
      nodes = outputDocument.getElementsByTagName("PCT-Status-Message_messages_E");
      if (nodes.getLength() != 1) {
        errorMessage = "unknown error message";
      }
    }
    errorMessage = getTextContent(nodes.item(0));
    return errorMessage;
  }

  protected void logDocument(String message, Document outputDocument)
  {
    if (log.isDebugEnabled()) {
      OutputStream out = new ByteArrayOutputStream();
      printDocumentToOutputStream(outputDocument, out);
      log.debug(message + ": " + out.toString());
    }
  }

  /**
   * Check the output document to see if this successfully completed job has any results. Return
   * true whenever there are results.
   * @param outputDocument the output document to check to see if there are any results
   * @return true whenever there are results
   */
  abstract protected boolean hasResults(Document outputDocument);
  
  protected String getWebenvFromDocument(Document document) {
    NodeList nodes = document.getElementsByTagName("PCT-Entrez_webenv");
    if (nodes.getLength() != 1) {
      throw new RuntimeException("no PCT-Entrez_webenv node in response");      
    }
    Element element = (Element) nodes.item(0);
    return getTextContent(element);
  }

  protected String getQueryKeyFromDocument(Document document) {
    NodeList nodes = document.getElementsByTagName("PCT-Entrez_query-key");
    if (nodes.getLength() != 1) {
      throw new RuntimeException("no PCT-Entrez_query-key node in response");      
    }
    Element element = (Element) nodes.item(0);
    return getTextContent(element);
  }

  protected void sleep(long numMillisecondsToSleep) {
    try {
      Thread.sleep(numMillisecondsToSleep);
    }
    catch (InterruptedException e) {
    }
  }

  protected String getReqidFromOutputDocument(Document outputDocument) {
    if (outputDocument == null) {
      log.warn("outputDocument == null, in getReqidFromOutputDocument.");
      return null;
    }
    NodeList nodes = outputDocument.getElementsByTagName("PCT-Waiting_reqid");
    if (nodes.getLength() == 0) {
      return null;
    }
    return getTextContent(nodes.item(0));
  }

  protected Document createPollDocumentForReqid(String reqid) {
  
    Document document = _documentBuilder.newDocument();
    
    // every elt has a single child, up to PCT-Request
    
    Element element = createParentedElement(document, document, "PCT-Data");
    element = createParentedElement(document, element, "PCT-Data_input");
    element = createParentedElement(document, element, "PCT-InputData");
    element = createParentedElement(document, element, "PCT-InputData_request");
    Element pctRequest = createParentedElement(document, element, "PCT-Request");
  
    // PCT-Request has two children
    
    element = createParentedElement(document, pctRequest, "PCT-Request_reqid");
    createParentedTextNode(document, element, reqid);
  
    element = createParentedElement(document, pctRequest, "PCT-Request_type");
    element.setAttribute("value", "status");
  
    return document; 
  }
}

