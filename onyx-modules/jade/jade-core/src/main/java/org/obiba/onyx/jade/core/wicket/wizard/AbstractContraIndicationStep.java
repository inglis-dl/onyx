/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantInteractionType;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Termination Step after contra-indication has been selected.
 * 
 */
public abstract class AbstractContraIndicationStep extends WizardStepPanel {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractContraIndicationStep.class);

  @SpringBean
  protected ActiveInstrumentRunService activeInstrumentRunService;

  public AbstractContraIndicationStep(String id) {
    super(id);
    // TODO Auto-generated constructor stub
  }
  
  protected abstract ParticipantInteractionType getParticipantInteractionType();

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    // exit if a ci is selected
    ContraIndication ci = activeInstrumentRunService.getContraIndication();
    if(ci != null && ci.getType().equals(getParticipantInteractionType())) {
      WizardStepPanel nextStep = new ContraIndicatedStep(WizardForm.getStepId());
      // no possibility to come back
      // nextStep.setPreviousStep(this);
      setNextStep(nextStep);
      nextStep.setPreviousStep(this);
      log.debug("Contra-indicated by {} ({})", ci, activeInstrumentRunService.getOtherContraIndication());
    }
    else {
      // do it in case of back and forth
      ((InstrumentWizardForm)form).setUpWizardFlow();
    }
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setEnabled(getPreviousStep() != null);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
      target.addComponent(form.getFinishLink());
    }
  }

}
