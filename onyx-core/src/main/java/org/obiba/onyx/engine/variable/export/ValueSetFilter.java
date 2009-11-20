/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.filter.CollectionFilterChain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("valueset")
class ValueSetFilter {

  @XStreamAsAttribute
  private final String entityTypeName;

  @XStreamAlias("entities")
  private CollectionFilterChain<ValueSet> entityFilterChain;

  @XStreamAlias("variables")
  private CollectionFilterChain<VariableValueSource> variableFilterChain;

  ValueSetFilter(String entityTypeName) {
    this.entityTypeName = entityTypeName;
  }

  CollectionFilterChain<ValueSet> getEntityFilterChain() {
    if(entityFilterChain == null) entityFilterChain = new CollectionFilterChain<ValueSet>();
    return entityFilterChain;
  }

  void setEntityFilterChain(CollectionFilterChain<ValueSet> entityFilterChain) {
    this.entityFilterChain = entityFilterChain;
  }

  CollectionFilterChain<VariableValueSource> getVariableFilterChain() {
    if(variableFilterChain == null) variableFilterChain = new CollectionFilterChain<VariableValueSource>();
    return variableFilterChain;
  }

  void setVariableFilterChain(CollectionFilterChain<VariableValueSource> variableFilterChain) {
    this.variableFilterChain = variableFilterChain;
  }

  public String getEntityTypeName() {
    return entityTypeName;
  }

}
