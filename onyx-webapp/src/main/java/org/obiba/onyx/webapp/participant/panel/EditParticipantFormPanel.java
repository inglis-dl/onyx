/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.panel;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * 
 */
public class EditParticipantFormPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("serial")
  public EditParticipantFormPanel(String id, IModel participantModel, Page sourcePage) {
    super(id);
    Form editParticipantForm = new Form("editParticipantForm");
    editParticipantForm.add(new EditParticipantPanel("editParticipantPanel", participantModel, sourcePage, editParticipantForm));
    add(editParticipantForm);
  }

}
