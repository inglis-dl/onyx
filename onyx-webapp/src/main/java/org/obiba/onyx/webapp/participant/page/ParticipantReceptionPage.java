/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.page;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.panel.AssignCodeToParticipantPanel;
import org.obiba.onyx.webapp.participant.panel.EditParticipantPanel;
import org.obiba.onyx.webapp.participant.panel.ParticipantPanel;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class ParticipantReceptionPage extends BasePage {

  @SuppressWarnings("serial")
  public ParticipantReceptionPage(IModel participantModel, Page sourcePage) {
    super();

    final ParticipantPanel participantPanel = new ParticipantPanel("participantPanel", participantModel);
    add(participantPanel);
    add(new AssignCodeToParticipantPanel("assignCodeToParticipantPanel", participantModel, sourcePage));

    //
    // Add the Edit Participant pop-up.
    //
    final ModalWindow editParticipantModalWindow = new ModalWindow("editParticipantModalWindow");
    editParticipantModalWindow.setTitle(new StringResourceModel("Participant", this, null));
    editParticipantModalWindow.setInitialHeight(435);
    editParticipantModalWindow.setInitialWidth(400);
    editParticipantModalWindow.setContent(new EditParticipantPanel("content", participantModel, editParticipantModalWindow));
    editParticipantModalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {

      public void onClose(AjaxRequestTarget target) {
        target.addComponent(participantPanel);
      }

    });
    add(editParticipantModalWindow);

    @SuppressWarnings("serial")
    AjaxLink link = new AjaxLink("editParticipantAction") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        editParticipantModalWindow.show(target);
      }
    };

    add(link);
  }
}
