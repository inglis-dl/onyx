/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.magma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.beans.NoSuchBeanException;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.magma.AbstractOnyxBeanResolver;
import org.obiba.onyx.util.StringUtil;
import org.obiba.onyx.util.data.Data;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ValueSetBeanResolver for InstrumentRun beans.
 */
public class InstrumentRunBeanResolver extends AbstractOnyxBeanResolver {
  //
  // Constants
  //

  // TODO: This constant is also defined in InstrumentRunVariableValueSourceFactory.
  // Should define it once somewhere.
  public static final String INSTRUMENT_RUN = "InstrumentRun";

  //
  // Instance Variables
  //

  @Autowired
  private InstrumentService instrumentService;

  @Autowired
  private InstrumentRunService instrumentRunService;

  //
  // AbstractOnyxBeanResolver Methods
  //

  public boolean resolves(Class<?> type) {
    return InstrumentRun.class.equals(type) || InstrumentRunValue.class.equals(type) || Measure.class.equals(type) || Data.class.equals(type) || Contraindication.class.equals(type) || Measure.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
    if(type.equals(InstrumentRun.class)) {
      return resolveInstrumentRun(valueSet, variable);
    } else if(type.equals(InstrumentRunValue.class)) {
      String secondToken = StringUtil.splitAndReturnTokenAt(variable.getName(), "\\.", 1);
      if(secondToken != null) {
        if(secondToken.equals("MEASURE")) {
          return resolveInstrumentRunValues(valueSet, variable);
        } else {
          return resolveInstrumentRunValue(valueSet, variable);
        }
      }
    } else if(type.equals(Measure.class)) {
      return resolveMeasure(valueSet, variable);
    } else if(type.equals(Data.class)) {
      String secondToken = StringUtil.splitAndReturnTokenAt(variable.getName(), "\\.", 1);
      if(secondToken != null) {
        if(secondToken.equals("MEASURE")) {
          return resolveDatas(valueSet, variable);
        } else {
          return resolveData(valueSet, variable);
        }
      }
    } else if(type.equals(Contraindication.class)) {
      return resolveContraindication(valueSet, variable);
    } else if(type.equals(Measure.class)) {
      return resolveMeasure(valueSet, variable);
    }

    return null;
  }

  //
  // Methods
  //

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  protected InstrumentService getInstrumentService() {
    return instrumentService;
  }

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  protected InstrumentRunService getInstrumentRunService() {
    return instrumentRunService;
  }

  protected InstrumentRun resolveInstrumentRun(ValueSet valueSet, Variable variable) {
    String instrumentTypeName = extractInstrumentTypeName(variable.getName());
    if(instrumentTypeName != null) {
      Participant participant = getParticipant(valueSet);
      if(participant != null) {
        return instrumentRunService.getInstrumentRun(participant, instrumentTypeName);
      }
    }
    return null;
  }

  protected InstrumentRunValue resolveInstrumentRunValue(ValueSet valueSet, Variable variable) {
    InstrumentRun instrumentRun = resolveInstrumentRun(valueSet, variable);
    if(instrumentRun != null) {
      String parameterCode = StringUtil.splitAndReturnTokenAt(variable.getName(), "\\.", 1);
      if(parameterCode != null) {
        for(InstrumentRunValue runValue : instrumentRun.getInstrumentRunValues()) {
          if(runValue.getInstrumentParameter().equals(parameterCode)) {
            return runValue;
          }
        }
      }
    }
    return null;
  }

  protected List<InstrumentRunValue> resolveInstrumentRunValues(ValueSet valueSet, Variable variable) {
    InstrumentRun instrumentRun = resolveInstrumentRun(valueSet, variable);
    if(instrumentRun != null) {
      String parameterCode = StringUtil.splitAndReturnTokenAt(variable.getName(), "\\.", 2);
      if(parameterCode != null) {
        List<InstrumentRunValue> values = new ArrayList<InstrumentRunValue>();
        for(Measure measure : instrumentRun.getMeasures()) {
          for(InstrumentRunValue runValue : measure.getInstrumentRunValues()) {
            if(runValue.getInstrumentParameter().equals(parameterCode)) {
              values.add(runValue);
            }
          }
        }
        return values;
      }
    }
    return Collections.emptyList();
  }

  protected List<Measure> resolveMeasure(ValueSet valueSet, Variable variable) {
    InstrumentRun instrumentRun = (InstrumentRun) resolveInstrumentRun(valueSet, variable);
    if(instrumentRun != null) {
      return instrumentRun.getMeasures();
    }
    return null;
  }

  protected InstrumentRunValue getInstrumentRunValue(ValueSet valueSet, InstrumentType instrumentType, InstrumentParameter instrumentParameter) {
    Participant participant = getParticipant(valueSet);
    if(participant != null) {
      return instrumentRunService.getInstrumentRunValue(participant, instrumentType.getName(), instrumentParameter.getCode(), null);
    }
    return null;
  }

  protected Data resolveData(ValueSet valueSet, Variable variable) {
    String instrumentTypeName = StringUtil.splitAndReturnTokenAt(variable.getName(), "\\.", 0);
    String instrumentParameterCode = StringUtil.splitAndReturnTokenAt(variable.getName(), "\\.", 1);

    if(instrumentTypeName != null && instrumentParameterCode != null) {
      InstrumentRunValue instrumentRunValue = resolveInstrumentRunValue(valueSet, variable);

      if(instrumentRunValue != null) {
        InstrumentType instrumentType = instrumentService.getInstrumentType(instrumentTypeName);

        if(instrumentType != null) {
          InstrumentParameter instrumentParameter = instrumentType.getInstrumentParameter(instrumentParameterCode);

          if(instrumentParameter != null) {
            return instrumentRunValue.getData(instrumentParameter.getDataType());
          }
        }
      }
    }

    return null;
  }

  protected List<Data> resolveDatas(ValueSet valueSet, Variable variable) {
    String instrumentTypeName = StringUtil.splitAndReturnTokenAt(variable.getName(), "\\.", 0);
    String instrumentParameterCode = StringUtil.splitAndReturnTokenAt(variable.getName(), "\\.", 2);

    if(instrumentTypeName != null && instrumentParameterCode != null) {
      List<InstrumentRunValue> instrumentRunValues = resolveInstrumentRunValues(valueSet, variable);

      if(!instrumentRunValues.isEmpty()) {
        List<Data> datas = new ArrayList<Data>();

        for(InstrumentRunValue instrumentRunValue : instrumentRunValues) {
          InstrumentType instrumentType = instrumentService.getInstrumentType(instrumentTypeName);

          if(instrumentType != null) {
            InstrumentParameter instrumentParameter = instrumentType.getInstrumentParameter(instrumentParameterCode);

            if(instrumentParameter != null) {
              datas.add(instrumentRunValue.getData(instrumentParameter.getDataType()));
            }
          }
        }

        return datas;
      }
    }

    return Collections.emptyList();
  }

  protected Object resolveContraindication(ValueSet valueSet, Variable variable) {
    String instrumentTypeName = extractInstrumentTypeName(variable.getName());
    if(instrumentTypeName != null) {
      InstrumentType instrumentType = instrumentService.getInstrumentType(instrumentTypeName);
      if(instrumentType != null) {
        InstrumentRun instrumentRun = resolveInstrumentRun(valueSet, variable);
        if(instrumentRun != null) {
          return instrumentType.getContraindication(instrumentRun.getContraindication());
        }
      }
    }
    return null;
  }

  private String extractInstrumentTypeName(String variableName) {
    String[] elements = variableName.split("\\.");
    if(elements.length != 0) {
      return elements[0];
    }
    return null;
  }

  private String extractInstrumentParameterCode(String variableName, String instrumentTypeName) {
    String[] elements = variableName.split("\\.");
    for(int i = 0; i < elements.length - 1; i++) {
      if(elements[i].equals(instrumentTypeName)) {
        return elements[i + 1];
      }
    }

    return null;
  }
}