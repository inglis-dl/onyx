package org.obiba.onyx.jade.core.wicket.instrument.panel;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InputDataSourceVisitor;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentParameterPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  private static final Logger log = LoggerFactory.getLogger(InstrumentParameterPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private InputDataSourceVisitor inputDataSourceVisitor;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private InstrumentRun instrumentRun;

  public InstrumentParameterPanel(String id, IModel instrumentModel, final FeedbackPanel feedbackPanel) {
    super(id);
    setModel(new DetachableEntityModel(queryService, instrumentModel.getObject()));
    setOutputMarkupId(true);

    Instrument instrument = (Instrument) getModelObject();
    InstrumentInputParameter template = new InstrumentInputParameter();
    template.setInstrument(instrument);

    instrumentRun = activeInstrumentRunService.start(activeInterviewService.getParticipant(), instrument);

    KeyValueDataPanel inputs = new KeyValueDataPanel("inputs");
    for(InstrumentInputParameter param : queryService.match(template)) {
      Label label = new Label(KeyValueDataPanel.getRowKeyId(), param.getName());
      Component input = null;
      InstrumentRunValue runValue = new InstrumentRunValue();
      // runValue.setCaptureMethod(param.getCaptureMethod());
      runValue.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL); // for testing
      runValue.setInstrumentParameter(param);
      instrumentRun.addInstrumentRunValue(runValue);

      switch(runValue.getCaptureMethod()) {
      case MANUAL:
        input = new DataField(KeyValueDataPanel.getRowValueId(), new PropertyModel(runValue, "data"), runValue.getDataType()).add(new OnChangeAjaxBehavior() {

          @Override
          protected void onUpdate(AjaxRequestTarget target) {
            log.info("On field update");
            target.addComponent(feedbackPanel);
          }

        });
        break;
      case AUTOMATIC:
        Data data = inputDataSourceVisitor.getData(activeInterviewService.getParticipant(), param.getInputSource());
        IModel value = (data == null ? new Model("") : new Model((Serializable) data.getValue()));
        input = new Label(KeyValueDataPanel.getRowValueId(), value);
        break;
      }
      inputs.addRow(label, input);
    }
    add(inputs);

  }
}
