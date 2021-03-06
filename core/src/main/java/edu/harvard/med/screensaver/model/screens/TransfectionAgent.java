// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Version;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;

/**
 * The  screen transfection agent vocabulary.
 */
@Entity
@org.hibernate.annotations.Proxy
public class TransfectionAgent extends AbstractEntity<Integer> implements Comparable<TransfectionAgent>
{
  private static final long serialVersionUID = 1L;

  private Integer _version;
  private String _value;

  /**
   * @motivation for hibernate
   */
  public TransfectionAgent()
  {
  }
  
  /**
   * Constructs an <code>TransfectionAgent</code> vocabulary term.
   * @param value The value of the term.
   */
  public TransfectionAgent(String value)
  {
    _value = value;
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="transfection_agent_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="transfection_agent_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="transfection_agent_id_seq")
  public Integer getTransfectionAgentId()
  {
    return getEntityId();
  }
  
  private void setTransfectionAgentId(Integer value)
  {
    setEntityId(value);
  }

  @Column(unique=true, nullable=false)
  @Type(type="text")
  public String getValue()
  {
    return _value;
  }

  public void setValue(String value)
  {
    _value = value;
  }

  @Override
  public String toString()
  {
    return getValue();
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  public int compareTo(TransfectionAgent other)
  {
    if (other == null) { 
      return 1;
    }
    return this.getValue().compareTo(other.getValue());
  }

  private void setVersion(Integer _version)
  {
    this._version = _version;
  }

  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }
}
