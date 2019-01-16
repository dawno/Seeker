// Generated code from Butter Knife. Do not modify!
package com.trybe.trybe;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class WorkProfileFragment$$ViewBinder<T extends com.trybe.trybe.WorkProfileFragment> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296674, "field 'workCancel'");
    target.workCancel = finder.castView(view, 2131296674, "field 'workCancel'");
    view = finder.findRequiredView(source, 2131296680, "field 'workSave'");
    target.workSave = finder.castView(view, 2131296680, "field 'workSave'");
    view = finder.findRequiredView(source, 2131296675, "field 'workDelete'");
    target.workDelete = finder.castView(view, 2131296675, "field 'workDelete'");
    view = finder.findRequiredView(source, 2131296678, "field 'workName'");
    target.workName = finder.castView(view, 2131296678, "field 'workName'");
    view = finder.findRequiredView(source, 2131296679, "field 'workPos'");
    target.workPos = finder.castView(view, 2131296679, "field 'workPos'");
    view = finder.findRequiredView(source, 2131296417, "field 'fromYear'");
    target.fromYear = view;
    view = finder.findRequiredView(source, 2131296416, "field 'fromMonth'");
    target.fromMonth = view;
    view = finder.findRequiredView(source, 2131296636, "field 'toYear'");
    target.toYear = view;
    view = finder.findRequiredView(source, 2131296635, "field 'toMonth'");
    target.toMonth = view;
    view = finder.findRequiredView(source, 2131296634, "field 'toDateLayout'");
    target.toDateLayout = finder.castView(view, 2131296634, "field 'toDateLayout'");
    view = finder.findRequiredView(source, 2131296363, "field 'isCurrentWork'");
    target.isCurrentWork = finder.castView(view, 2131296363, "field 'isCurrentWork'");
  }

  @Override public void unbind(T target) {
    target.workCancel = null;
    target.workSave = null;
    target.workDelete = null;
    target.workName = null;
    target.workPos = null;
    target.fromYear = null;
    target.fromMonth = null;
    target.toYear = null;
    target.toMonth = null;
    target.toDateLayout = null;
    target.isCurrentWork = null;
  }
}
