/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.engine.state;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.marble.core.wicket.MarblePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class MarbleInProgressState extends AbstractMarbleStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(MarbleInProgressState.class);

  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinitionBuilder.CANCEL_ACTION);
    addSystemAction(ActionDefinitionBuilder.COMPLETE_ACTION);
  }

  public Component getWidget(String id) {
    return new MarblePanel(id, getStage());
  }

  @Override
  public void complete(Action action) {
    log.info("Marble Stage {} is completing", super.getStage().getName());
    castEvent(TransitionEvent.COMPLETE);
  }

  @Override
  public void stop(Action action) {
    log.info("Marble Stage {} is canceling", super.getStage().getName());
    castEvent(TransitionEvent.CANCEL);
  }

  @Override
  public boolean isInteractive() {
    return true;
  }

  public String getName() {
    return "InProgress";
  }

}
