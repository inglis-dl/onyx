/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

/**
 * Empty page with no JS included (to be able to use newer JQuery version).
 */
public class QuestionnaireLayoutPage extends WebPage {

  public QuestionnaireLayoutPage(IModel<Questionnaire> model) {
    super(model);
    add(new QuestionnaireTreePanel("content", model));
  }

}