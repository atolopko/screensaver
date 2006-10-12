
package edu.harvard.med.screensaver.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class StringUtils {


	/**
   * Makes a delimited list of items from a Collection, just like Perl's join()
   * function.
   * 
   * @param items a <code>Collection</code> of <code>Object</code>s with
   *          appropriate <code>toString</code> methods
   * @param delimiter a <code>String</code>
   * @return a <code>String</code> containing the string representation of the
   *         Collection elements, delimited by <code>delimiter</code>
   */
  /* TODO: provide escaping of specified characters */
  /* TODO: overload method signature for default parameters */
  public static String makeListString(Collection items, String delimiter)
  {
    boolean isFirst = true;
    StringBuffer buf = new StringBuffer();
    for (Iterator iter = items.iterator(); iter.hasNext();) {
      String item = iter.next()
                        .toString();
      if (isFirst) {
        isFirst = false;
      }
      else {
        buf.append(delimiter);
      }
      buf.append(item);
    }
    return buf.toString();
  }


	public static String makeRepeatedString(String segment, int count)
  {
    int targetLength = segment.length() * count;
    StringBuffer buf = new StringBuffer(targetLength);
    for (int i = 0; buf.length() < targetLength; ++i) {
      // for efficiency, we'll double the size of our repeated string, if
      // possible
      if (buf.length() > 0 && buf.length() * 2 <= targetLength) {
        buf.append(buf);
      }
      else {
        buf.append(segment);
      }
    }
    return buf.toString();
  }


	@SuppressWarnings("unchecked")
  public static List wrapStrings(List elements, String left, String right)
  {
    StringBuffer buf = new StringBuffer();
    List result = new ArrayList(elements.size());
    for (Iterator iter = elements.iterator(); iter.hasNext();) {
      buf.append(left)
         .append(iter.next())
         .append(right);
      result.add(buf.toString());
      buf.setLength(0);
    }
    return result;
  }
  
  public static String capitalize(String s)
  {
    if (s != null && s.length() > 0) {
      return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    return s;
  }

  public static String uncapitalize(String s)
  {
    if (s != null && s.length() > 0) {
      return s.substring(0, 1).toLowerCase() + s.substring(1);
    }
    return s;
  }
}
