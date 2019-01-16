// Generated code from Butter Knife. Do not modify!
package com.trybe.trybe;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class ChangePassActivity$$ViewBinder<T extends com.trybe.trybe.ChangePassActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296505, "field 'oldPass'");
    target.oldPass = finder.castView(view, 2131296505, "field 'oldPass'");
    view = finder.findRequiredView(source, 2131296499, "field 'newPass'");
    target.newPass = finder.castView(view, 2131296499, "field 'newPass'");
    view = finder.findRequiredView(source, 2131296354, "field 'confirmPass'");
    target.confirmPass = finder.castView(view, 2131296354, "field 'confirmPass'");
    view = finder.findRequiredView(source, 2131296382, "field 'submit'");
    target.submit = finder.castView(view, 2131296382, "field 'submit'");
  }

  @Override public void unbind(T target) {
    target.oldPass = null;
    target.newPass = null;
    target.confirmPass = null;
    target.submit = null;
  }
}
