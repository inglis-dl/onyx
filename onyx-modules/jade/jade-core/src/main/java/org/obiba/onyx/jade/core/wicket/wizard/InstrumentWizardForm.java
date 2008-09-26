package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantInteractionType;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class InstrumentWizardForm extends WizardForm {

  private static final Logger log = LoggerFactory.getLogger(InstrumentWizardForm.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private InstrumentService instrumentService;

  private WizardStepPanel instrumentSelectionStep;

  private WizardStepPanel observedContraIndicationStep;

  private WizardStepPanel askedContraIndicationStep;

  private WizardStepPanel inputParametersStep;

  private WizardStepPanel instructionsStep;

  private WizardStepPanel outputParametersStep;

  private WizardStepPanel validationStep;

  public InstrumentWizardForm(String id, IModel instrumentTypeModel) {
    super(id);

    activeInstrumentRunService.setInstrumentType((InstrumentType) instrumentTypeModel.getObject());

    WizardStepPanel startStep = null;

    instrumentSelectionStep = new InstrumentSelectionStep(getStepId());
    observedContraIndicationStep = new ObservedContraIndicationStep(getStepId());
    askedContraIndicationStep = new AskedContraIndicationStep(getStepId());
    inputParametersStep = new InputParametersStep(getStepId());
    instructionsStep = new InstructionsStep(getStepId());
    outputParametersStep = new OutputParametersStep(getStepId());
    validationStep = new ValidationStep(getStepId());

    // do we need to select the instrument ?
    Instrument template = new Instrument();
    template.setInstrumentType((InstrumentType) instrumentTypeModel.getObject());
    template.setStatus(InstrumentStatus.ACTIVE);
    if(queryService.count(template) > 1) {
      activeInstrumentRunService.reset();
      startStep = instrumentSelectionStep;
    } else {
      // pre selected instrument
      activeInstrumentRunService.start(activeInterviewService.getParticipant(), queryService.matchOne(template));
      startStep = setUpWizardFlow();
    }

    add(startStep);
    startStep.onStepIn(this, null);
    startStep.handleWizardState(this, null);

    // never show cancel link
    getCancelLink().setVisible(false);
  }

  public WizardStepPanel setUpWizardFlow() {
    WizardStepPanel startStep = null;
    WizardStepPanel lastStep = null;

    // instrument is not null at this point
    Instrument instrument = activeInstrumentRunService.getInstrument();
    if (instrument == null) throw new IllegalStateException("Instrument is not supposed to be null in current active instrument run.");
    
    // are there observed contra-indications to display ?
    ContraIndication ciTemplate = new ContraIndication();
    ciTemplate.setType(ParticipantInteractionType.OBSERVED);
    ciTemplate.setInstrument(instrument);
    log.info("observed.ci.count={}", queryService.count(ciTemplate));
    if(queryService.count(ciTemplate) > 0) {
      if(startStep == null) {
        startStep = observedContraIndicationStep;
        lastStep = startStep;
      } else {
        lastStep.setNextStep(observedContraIndicationStep);
        observedContraIndicationStep.setPreviousStep(lastStep);
        lastStep = observedContraIndicationStep;
      }
    }

    // are there asked contra-indications to display ?
    ciTemplate.setType(ParticipantInteractionType.ASKED);
    log.info("asked.ci.count={}", queryService.count(ciTemplate));
    if(queryService.count(ciTemplate) > 0) {
      if(startStep == null) {
        startStep = askedContraIndicationStep;
        lastStep = startStep;
      } else {
        lastStep.setNextStep(askedContraIndicationStep);
        askedContraIndicationStep.setPreviousStep(lastStep);
        lastStep = askedContraIndicationStep;
      }
    }

    // are there input parameters with input source that requires user provisionning ?
    // or interpretative questions
    InterpretativeParameter template = new InterpretativeParameter();
    template.setInstrument(instrument);
    log.info("instrumentInterpretativeParameters.count={}", queryService.count(template));
    log.info("instrumentInputParameters.count={}", instrumentService.countInstrumentInputParameter(instrument, false));
    if(queryService.count(template) > 0 || instrumentService.countInstrumentInputParameter(instrument, false) > 0) {
      if(startStep == null) {
        startStep = inputParametersStep;
        lastStep = startStep;
      } else {
        lastStep.setNextStep(inputParametersStep);
        inputParametersStep.setPreviousStep(lastStep);
        lastStep = inputParametersStep;
      }
    }

    // are there output parameters that are to be captured automatically from instrument (i.e. requires instrument
    // launch) ?
    log.info("instrument.isInteractive={}", instrumentService.isInteractiveInstrument(instrument));
    if(instrumentService.isInteractiveInstrument(instrument)) {
      if(startStep == null) {
        startStep = instructionsStep;
        lastStep = startStep;
      } else {
        lastStep.setNextStep(instructionsStep);
        instructionsStep.setPreviousStep(lastStep);
        lastStep = instructionsStep;
      }
    }

    // are there output parameters that are to be captured manually from instrument ?
    InstrumentOutputParameter opTemplate = new InstrumentOutputParameter();
    opTemplate.setInstrument(instrument);
    opTemplate.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);
    log.info("instrumentOutputParameters.MANUAL.count={}", queryService.count(opTemplate));
    if(queryService.count(opTemplate) > 0) {
      if(startStep == null) {
        startStep = outputParametersStep;
        lastStep = startStep;
      } else {
        lastStep.setNextStep(outputParametersStep);
        outputParametersStep.setPreviousStep(lastStep);
        lastStep = outputParametersStep;
      }
    }

    // validation: final step
    if(startStep == null) {
      startStep = validationStep;
      lastStep = startStep;
    } else {
      lastStep.setNextStep(validationStep);
      validationStep.setPreviousStep(lastStep);
      lastStep = validationStep;
    }

    return startStep;
  }

  public WizardStepPanel getInstrumentSelectionStep() {
    return instrumentSelectionStep;
  }

  public WizardStepPanel getInputParametersStep() {
    return inputParametersStep;
  }

  public WizardStepPanel getInstructionsStep() {
    return instructionsStep;
  }

  public WizardStepPanel getOutputParametersStep() {
    return outputParametersStep;
  }

  public WizardStepPanel getValidationStep() {
    return validationStep;
  }

}
