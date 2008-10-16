/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.instrument;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantInteractionType;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AskedContraIndicationPanel extends Panel {

  private static final long serialVersionUID = 1839206247478532673L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AskedContraIndicationPanel.class);

  private static final String YES = "Yes";
  private static final String NO = "No";
  private static final String DOESNOT_KNOW = "DoesNotKnow";
  
  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;
  
  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private List<RadioGroup> radioGroups;

  @SuppressWarnings("serial")
  public AskedContraIndicationPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    radioGroups = new ArrayList<RadioGroup>();

    RepeatingView repeat = new RepeatingView("repeat");
    add(repeat);

    final ContraIndication defaultCi = activeInstrumentRunService.getContraIndication();
    ContraIndication template = new ContraIndication();
    template.setType(ParticipantInteractionType.ASKED);
    template.setInstrument(activeInstrumentRunService.getInstrument());
    for(final ContraIndication ci : queryService.match(template)) {
      WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
      repeat.add(item);
      
      ci.setApplicationContext(((SpringWebApplication) getApplication()).getSpringContextLocator().getSpringContext());
      ci.setUserSessionService(userSessionService);

      item.add(new Label("ciLabel", new PropertyModel(ci, "description")));
      
      // radio group without default selection
      final RadioGroup radioGroup = new RadioGroup("radioGroup", new Model());
      radioGroups.add(radioGroup);
      radioGroup.setLabel(new PropertyModel(ci, "description"));
      item.add(radioGroup);
      ListView radioList = new ListView("radioItem", Arrays.asList(new String[] { YES, NO, DOESNOT_KNOW })) {

        @Override
        protected void populateItem(ListItem listItem) {
          final String key = listItem.getModelObjectAsString();
          final ContraIndicationSelection selection = new ContraIndicationSelection();
          selection.setContraIndication(ci);
          selection.setSelectionKey(key);
          
          Model selectModel = new Model(selection);
          
          Radio radio = new Radio("radio", selectModel);
          radio.setLabel(new StringResourceModel(key, AskedContraIndicationPanel.this, null));
          
          // set default selection
          // cannot decide if yes/no/dontknow was selected, so only deal with case the default ci is not null
          // and it was because yes was selected
          if (key.equals(YES) && defaultCi != null && (defaultCi.getType().equals(ci.getType()) & defaultCi.getName().equals(ci.getName()))) {
            radioGroup.setModel(selectModel);
          }
          
          FormComponentLabel radioLabel = new FormComponentLabel("radioLabel", radio);
          listItem.add(radioLabel);
          radioLabel.add(radio);
          radioLabel.add(new Label("label", radio.getLabel()).setRenderBodyOnly(true));
        }

      }.setReuseItems(true);
      radioGroup.add(radioList);
      radioGroup.setRequired(true);
    }

  }
  
  public void saveContraIndicationSelection() {
    activeInstrumentRunService.setContraIndication(null);
    for (RadioGroup rg : radioGroups) {
      ContraIndicationSelection ciSelection = (ContraIndicationSelection)rg.getModelObject();
      if (ciSelection.isSelected()) {
        activeInstrumentRunService.setContraIndication(ciSelection.getContraIndication());
        // just interested in the first one
        break;
      }
    }
  }

  @SuppressWarnings("serial")
  private class ContraIndicationSelection implements Serializable {

    private String selectionKey;

    private ContraIndication contraIndication;

    public String getSelectionKey() {
      return selectionKey;
    }

    public void setSelectionKey(String selectionKey) {
      this.selectionKey = selectionKey;
    }

    public boolean isSelected() {
      return selectionKey.equals(YES) || selectionKey.equals(DOESNOT_KNOW);
    }

    public ContraIndication getContraIndication() {
      return contraIndication;
    }

    public void setContraIndication(ContraIndication contraIndication) {
      this.contraIndication = contraIndication;
    }

  }

}
