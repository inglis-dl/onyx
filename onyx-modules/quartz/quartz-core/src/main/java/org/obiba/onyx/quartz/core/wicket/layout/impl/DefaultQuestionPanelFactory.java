/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;

public class DefaultQuestionPanelFactory implements IQuestionPanelFactory {

  public QuestionPanel createPanel(String id, Question question) {
    if(question.isMultiple()) {
      return new MultipleChoiceQuestionPanel(id, new Model(question));
    }
    else {
      return new SingleChoiceQuestionPanel(id, new Model(question));
    }
  }

  public String getName() {
    return "quartz." + getClass().getSimpleName();
  }

}
