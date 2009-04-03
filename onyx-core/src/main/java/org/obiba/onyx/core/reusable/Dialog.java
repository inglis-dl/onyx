/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.reusable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.IClusterable;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * Reusable class extending ModalWindow to predefine dialog boxes Title with possibility of adding an icon, scrollable
 * content, predefined customizable buttons
 */
public class Dialog extends ModalWindow {

  private static final long serialVersionUID = 928252608995728804L;

  private Status status;

  private Type type;

  private Form form;

  private IDialogCloseButtonCallback dialogCloseButtonCallback = null;

  private IDialogClosedCallback dialogClosedCallback = null;

  public enum Option {
    YES_OPTION, NO_OPTION, OK_OPTION, CANCEL_OPTION, CLOSE_OPTION, YES_NO_OPTION, YES_NO_CANCEL_OPTION, OK_CANCEL_OPTION
  }

  public enum Type {
    WARNING, INFO, ERROR, PLAIN
  }

  public enum Status {
    SUCCESS, ERROR, YES, NO, CANCELLED, CLOSED
  }

  public Dialog(String id) {
    super(id);

    form = new Form("form");
    form.add(new WebMarkupContainer(getContentId()));

    AjaxButton okButton = new AjaxButton("ok", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        Dialog.this.setStatus(Status.SUCCESS);
        ModalWindow.closeCurrent(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        Dialog.this.setStatus(Status.ERROR);
        ModalWindow.closeCurrent(target);
      }

    };
    okButton.add(new AttributeModifier("value", true, new StringResourceModel("Dialog.Ok", this, null)));
    form.add(okButton);

    AjaxLink cancelButton = new AjaxLink("cancel") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        Dialog.this.setStatus(Status.CANCELLED);
        ModalWindow.closeCurrent(target);
      }

    };
    cancelButton.add(new AttributeModifier("value", true, new StringResourceModel("Dialog.Cancel", this, null)));
    form.add(cancelButton);

    AjaxLink yesButton = new AjaxLink("yes") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        Dialog.this.setStatus(Status.YES);
        ModalWindow.closeCurrent(target);
      }

    };
    yesButton.add(new AttributeModifier("value", true, new StringResourceModel("Dialog.Yes", this, null)));
    form.add(yesButton);

    AjaxLink noButton = new AjaxLink("no") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        Dialog.this.setStatus(Status.NO);
        ModalWindow.closeCurrent(target);
      }

    };
    noButton.add(new AttributeModifier("value", true, new StringResourceModel("Dialog.No", this, null)));
    form.add(noButton);

    AjaxLink closeButton = new AjaxLink("close") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        Dialog.this.setStatus(Status.CLOSED);
        ModalWindow.closeCurrent(target);
      }

    };
    closeButton.add(new AttributeModifier("value", true, new StringResourceModel("Dialog.Close", this, null)));
    form.add(closeButton);

    WebMarkupContainer modalContent = new WebMarkupContainer(getContentId());
    modalContent.setOutputMarkupId(true);
    modalContent.add(form);
    super.setContent(modalContent);
  }

  public void setOptions(Option option, String... labels) {
    if(option == null) {
      setEnabledOptions(true, false, false, false, false);
      return;
    }

    switch(option) {
    case YES_NO_CANCEL_OPTION:
      setEnabledOptions(false, true, true, true, false);
      if(labels != null) setOptionLabels(new String[] { "yes", "no", "cancel" }, labels);
      break;

    case YES_NO_OPTION:
      setEnabledOptions(false, false, true, true, false);
      if(labels != null) setOptionLabels(new String[] { "yes", "no" }, labels);
      break;

    case YES_OPTION:
      setEnabledOptions(false, false, true, false, false);
      if(labels != null) setOptionLabels(new String[] { "yes" }, labels);
      break;

    case NO_OPTION:
      setEnabledOptions(false, false, false, true, false);
      if(labels != null) setOptionLabels(new String[] { "no" }, labels);
      break;

    case OK_CANCEL_OPTION:
      setEnabledOptions(true, true, false, false, false);
      if(labels != null) setOptionLabels(new String[] { "ok", "cancel" }, labels);
      break;

    case OK_OPTION:
      setEnabledOptions(true, false, false, false, false);
      if(labels != null) setOptionLabels(new String[] { "ok" }, labels);
      break;

    case CANCEL_OPTION:
      setEnabledOptions(false, true, false, false, false);
      if(labels != null) setOptionLabels(new String[] { "cancel" }, labels);
      break;

    case CLOSE_OPTION:
      setEnabledOptions(false, false, false, false, true);
      if(labels != null) setOptionLabels(new String[] { "close" }, labels);
      break;

    default:
      setEnabledOptions(true, false, false, false, false);
    }
  }

  private void setEnabledOptions(boolean ok, boolean cancel, boolean yes, boolean no, boolean close) {
    form.get("ok").setVisible(ok);
    form.get("cancel").setVisible(cancel);
    form.get("yes").setVisible(yes);
    form.get("no").setVisible(no);
    form.get("close").setVisible(close);
  }

  private void setOptionLabels(String[] componentIds, String... labels) {
    int i = 0;
    for(String label : labels) {
      if(i >= componentIds.length) break;
      form.get(componentIds[i]).add(new AttributeModifier("value", true, new StringResourceModel("Dialog." + label, this, null)));
      i++;
    }
  }

  /**
   * Sets the content of the dialog box.
   * 
   * @param component
   */
  @Override
  public void setContent(Component component) {
    if(component.getId().equals(getContentId()) == false) {
      throw new WicketRuntimeException("Dialog box content id is wrong.");
    }
    component.setOutputMarkupPlaceholderTag(true);
    component.setVisible(true);
    form.replace(component);
  }

  @Override
  public void setTitle(String title) {
    if(type == null || type.equals(Type.PLAIN)) {
      super.setTitle(title);
    } else {
      super.setTitle("<IMG SRC=\"../icons/" + type.toString().toLowerCase() + ".png\" />" + title);
    }
  }

  @Override
  public void setTitle(final IModel title) {
    if(type == null || type.equals(Type.PLAIN)) {
      super.setTitle(title);
    } else {
      super.setTitle(new Model() {
        @Override
        public Object getObject() {
          return ("<IMG SRC=\"../icons/" + type.toString().toLowerCase() + ".png\" />" + title.getObject());
        }
      });
    }
  }

  public static interface IDialogCloseButtonCallback extends IClusterable {
    /**
     * Methods invoked after the button has been clicked. The invocation is done using an ajax call, so
     * <code>{@link AjaxRequestTarget}</code> instance is available.
     * 
     * @param target <code>{@link AjaxRequestTarget}</code> instance bound with the ajax request.
     * @param status of the Dialog when button pressed
     * @return True if the window can be closed (will close the window), false otherwise
     */
    public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status);
  }

  public static interface IDialogClosedCallback extends IClusterable {
    /**
     * Called after the window has been closed.
     * @param target <code>{@link AjaxRequestTarget}</code> instance bound with the ajax request.
     * @param status of the Dialog when button pressed
     */
    public void onClose(AjaxRequestTarget target, Status status);
  }

  /**
   * Returns the id of formContent component.
   * @return Id of formContent component.
   */
  public String getContentId() {
    return "content";
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public void setDialogCloseButtonCallback(IDialogCloseButtonCallback dialogCloseButtonCallback) {
    this.dialogCloseButtonCallback = dialogCloseButtonCallback;
  }

  public void setDialogClosedCallback(IDialogClosedCallback dialogClosedCallback) {
    this.dialogClosedCallback = dialogClosedCallback;
  }

  public IDialogCloseButtonCallback getDialogCloseButtonCallback() {
    return dialogCloseButtonCallback;
  }

  public IDialogClosedCallback getDialogClosedCallback() {
    return dialogClosedCallback;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Form getForm() {
    return form;
  }
}
