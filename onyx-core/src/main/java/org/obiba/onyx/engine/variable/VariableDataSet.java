/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * A set of {@link VariableData}, usually associated to one {@link Participant}.
 */
@XStreamAlias("variableDataSet")
public class VariableDataSet implements Serializable {

  private static final long serialVersionUID = 1L;

  @XStreamAsAttribute
  private Date exportDate;

  @XStreamAsAttribute
  private Date captureDate;

  @XStreamImplicit
  private List<VariableData> variableDatas;

  public List<VariableData> getVariableDatas() {
    return variableDatas != null ? variableDatas : (variableDatas = new ArrayList<VariableData>());
  }

  public VariableData addVariableData(VariableData variableData) {
    if(variableData != null) {
      getVariableDatas().add(variableData);
    }
    return variableData;
  }

  public Date getExportDate() {
    return exportDate;
  }

  public void setExportDate(Date exportDate) {
    this.exportDate = exportDate;
  }

  public Date getCaptureDate() {
    return captureDate;
  }

  public void setCaptureDate(Date captureDate) {
    this.captureDate = captureDate;
  }

}