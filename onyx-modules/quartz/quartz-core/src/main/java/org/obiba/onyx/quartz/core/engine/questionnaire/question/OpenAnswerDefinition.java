/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.AnswerSource;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

public class OpenAnswerDefinition implements Serializable, ILocalizable {

  private static final long serialVersionUID = -7756577128502621726L;

  private String name;

  private DataType dataType;

  private String unit;

  private String format;

  private Data absoluteMinValue;

  private Data absoluteMaxValue;

  private Data usualMinValue;

  private Data usualMaxValue;

  private List<Data> defaultValues;

  private AnswerSource answerSource;

  public OpenAnswerDefinition(String name, DataType dataType) {
    this.name = name;
    this.dataType = dataType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public Data getAbsoluteMinValue() {
    return absoluteMinValue;
  }

  public void setAbsoluteMinValue(Data absoluteMinValue) {
    if(absoluteMinValue != null && !absoluteMinValue.getType().equals(getDataType())) {
      throw new IllegalArgumentException("Wrong data type for absolute min value: " + getDataType() + " expected, " + absoluteMinValue.getType() + " found.");
    }
    this.absoluteMinValue = absoluteMinValue;
  }

  public Data getAbsoluteMaxValue() {
    return absoluteMaxValue;
  }

  public void setAbsoluteMaxValue(Data absoluteMaxValue) {
    if(absoluteMaxValue != null && !absoluteMaxValue.getType().equals(getDataType())) {
      throw new IllegalArgumentException("Wrong data type for absolute max value: " + getDataType() + " expected, " + absoluteMaxValue.getType() + " found.");
    }
    this.absoluteMaxValue = absoluteMaxValue;
  }

  public Data getUsualMinValue() {
    return usualMinValue;
  }

  public void setUsualMinValue(Data usualMinValue) {
    if(usualMinValue != null && !usualMinValue.getType().equals(getDataType())) {
      throw new IllegalArgumentException("Wrong data type for usual min value: " + getDataType() + " expected, " + usualMinValue.getType() + " found.");
    }
    this.usualMinValue = usualMinValue;
  }

  public Data getUsualMaxValue() {
    return usualMaxValue;
  }

  public void setUsualMaxValue(Data usualMaxValue) {
    if(usualMaxValue != null && !usualMaxValue.getType().equals(getDataType())) {
      throw new IllegalArgumentException("Wrong data type for usual max value: " + getDataType() + " expected, " + usualMaxValue.getType() + " found.");
    }
    this.usualMaxValue = usualMaxValue;
  }

  public List<Data> getDefaultValues() {
    return defaultValues != null ? defaultValues : (defaultValues = new ArrayList<Data>());
  }

  public void addDefaultValue(String value) {
    if(value != null && value.length() > 0) {
      getDefaultValues().add(DataBuilder.build(dataType, value));
    }
  }

  public void addDefaultValue(Data data) {
    if(data != null && data.getValue() != null) {
      if(!data.getType().equals(getDataType())) {
        throw new IllegalArgumentException("Wrong data type for default value: " + getDataType() + " expected, " + data.getType() + " found.");
      }
      getDefaultValues().add(data);
    }
  }

  public void accept(IVisitor visitor) {
    visitor.visit(this);
  }

  public AnswerSource getAnswerSource() {
    return answerSource;
  }

  public void setAnswerSource(AnswerSource answerSource) {
    this.answerSource = answerSource;
  }
}
