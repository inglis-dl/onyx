/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;

/**
 * Question categories are presented in a dropdown.
 */
public class DropDownQuestionPanel extends DefaultQuestionPanel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public DropDownQuestionPanel(String id, IModel<Question> questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);
  }

  @Override
  protected void setContent(String id) {
    add(new DropDownQuestionCategoriesPanel(id, getDefaultModel()));
  }

}
