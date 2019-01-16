// Generated code from Butter Knife. Do not modify!
package com.trybe.trybe;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class JobPostActivity$$ViewBinder<T extends com.trybe.trybe.JobPostActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296401, "field 'etCompany'");
    target.etCompany = finder.castView(view, 2131296401, "field 'etCompany'");
    view = finder.findRequiredView(source, 2131296402, "field 'etPosition'");
    target.etPosition = finder.castView(view, 2131296402, "field 'etPosition'");
    view = finder.findRequiredView(source, 2131296647, "field 'location'");
    target.location = finder.castView(view, 2131296647, "field 'location'");
    view = finder.findRequiredView(source, 2131296644, "field 'jobCategory'");
    target.jobCategory = finder.castView(view, 2131296644, "field 'jobCategory'");
    view = finder.findRequiredView(source, 2131296545, "field 'salary_picker_min'");
    target.salary_picker_min = finder.castView(view, 2131296545, "field 'salary_picker_min'");
    view = finder.findRequiredView(source, 2131296546, "field 'salary_picker_max'");
    target.salary_picker_max = finder.castView(view, 2131296546, "field 'salary_picker_max'");
    view = finder.findRequiredView(source, 2131296403, "field 'etRemarks'");
    target.etRemarks = finder.castView(view, 2131296403, "field 'etRemarks'");
    view = finder.findRequiredView(source, 2131296382, "field 'done'");
    target.done = finder.castView(view, 2131296382, "field 'done'");
    view = finder.findRequiredView(source, 2131296486, "field 'menuBtn'");
    target.menuBtn = finder.castView(view, 2131296486, "field 'menuBtn'");
    view = finder.findRequiredView(source, 2131296328, "field 'chatBtn'");
    target.chatBtn = finder.castView(view, 2131296328, "field 'chatBtn'");
    view = finder.findRequiredView(source, 2131296383, "field 'mDrawer'");
    target.mDrawer = finder.castView(view, 2131296383, "field 'mDrawer'");
    view = finder.findRequiredView(source, 2131296494, "field 'mNavView'");
    target.mNavView = finder.castView(view, 2131296494, "field 'mNavView'");
  }

  @Override public void unbind(T target) {
    target.etCompany = null;
    target.etPosition = null;
    target.location = null;
    target.jobCategory = null;
    target.salary_picker_min = null;
    target.salary_picker_max = null;
    target.etRemarks = null;
    target.done = null;
    target.menuBtn = null;
    target.chatBtn = null;
    target.mDrawer = null;
    target.mNavView = null;
  }
}
