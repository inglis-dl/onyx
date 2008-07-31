package org.obiba.onyx.engine;

public enum ActionType {
  EXECUTE("Start"), CANCEL("Cancel"), PAUSE("Pause"), SKIP("Skip");

  private String name;
  
  private ActionType(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
