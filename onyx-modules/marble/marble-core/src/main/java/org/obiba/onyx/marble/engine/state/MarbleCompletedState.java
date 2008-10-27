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

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class MarbleCompletedState extends AbstractMarbleStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(MarbleCompletedState.class);

  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinitionBuilder.CANCEL_ACTION);
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.info("Marble Stage {} is cancelling", super.getStage().getName());
    getActiveConsentService().deletePreviousConsent();
    castEvent(TransitionEvent.CANCEL);
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  public String getName() {
    return "Marble.Completed";
  }

}
