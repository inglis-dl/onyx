/**
 * State skipped for Quartz module
 * 
 * @author acarey
 * 
 * Coming from states: ready
 * Possible forward states/actions/transitions: cancel, notApplicable
 */
package org.obiba.onyx.quartz.engine.state;

import java.util.Locale;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class QuartzSkippedState extends AbstractStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(QuartzSkippedState.class);

  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinitionBuilder.CANCEL_SKIPPED_ACTION);
  }

  public String getName() {
    return "Quartz.Skipped";
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.info("Quartz Stage {} is cancelling", super.getStage().getName());
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void onTransition(IStageExecution execution, TransitionEvent event) {
    // case not applicable transition
    Boolean var = areDependenciesCompleted();
    if(var != null && var == false) castEvent(TransitionEvent.NOTAPPLICABLE);
  }

  @Override
  public String getMessage() {
    Locale locale = userSessionService.getLocale();

    String state = getName();
    String reason = (getReason() != null) ? getReason().getEventReason() : null;

    String message = context.getMessage(state, null, locale);

    if(reason != null) {
      message += " (" + context.getMessage(reason, null, locale) + ")";
    }

    return message;
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.SKIP;
  }
}
