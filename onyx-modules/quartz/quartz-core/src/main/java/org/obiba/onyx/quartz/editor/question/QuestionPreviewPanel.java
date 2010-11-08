/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultQuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanelFactory;

@SuppressWarnings("serial")
public class QuestionPreviewPanel extends Panel {

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  public QuestionPreviewPanel(String id, IModel<Question> model, IModel<Questionnaire> questionnaireModel) {
    super(id, model);
    activeQuestionnaireAdministrationService.setQuestionnaire(questionnaireModel.getObject());
    activeQuestionnaireAdministrationService.setDefaultLanguage(questionnaireModel.getObject().getLocales().get(0));
    activeQuestionnaireAdministrationService.setQuestionnaireDevelopmentMode(true);
    try {
      if(model.getObject().getUIFactoryName().contains(DropDownQuestionPanelFactory.class.getSimpleName())) {
        add(new DropDownQuestionPanel("preview", model));
      } else {
        add(new DefaultQuestionPanel("preview", model));
      }
    } catch(Exception e) {
      // TODO: localize error message
      add(new Label("preview", "Error while generating the Question preview: " + e.getMessage()));
    }
  }
}
