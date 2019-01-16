// Generated code from Butter Knife. Do not modify!
package com.trybe.trybe;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class JobPref1Activity$$ViewBinder<T extends com.trybe.trybe.JobPref1Activity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296507, "field 'outerJcContainer'");
    target.outerJcContainer = finder.castView(view, 2131296507, "field 'outerJcContainer'");
  }

  @Override public void unbind(T target) {
    target.outerJcContainer = null;
  }
}
