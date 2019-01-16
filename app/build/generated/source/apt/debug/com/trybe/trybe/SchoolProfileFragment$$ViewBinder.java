// Generated code from Butter Knife. Do not modify!
package com.trybe.trybe;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class SchoolProfileFragment$$ViewBinder<T extends com.trybe.trybe.SchoolProfileFragment> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296553, "field 'schoolCancel'");
    target.schoolCancel = finder.castView(view, 2131296553, "field 'schoolCancel'");
    view = finder.findRequiredView(source, 2131296559, "field 'schoolSave'");
    target.schoolSave = finder.castView(view, 2131296559, "field 'schoolSave'");
    view = finder.findRequiredView(source, 2131296555, "field 'schoolDelete'");
    target.schoolDelete = finder.castView(view, 2131296555, "field 'schoolDelete'");
    view = finder.findRequiredView(source, 2131296558, "field 'schoolName'");
    target.schoolName = finder.castView(view, 2131296558, "field 'schoolName'");
    view = finder.findRequiredView(source, 2131296554, "field 'schoolDegree'");
    target.schoolDegree = finder.castView(view, 2131296554, "field 'schoolDegree'");
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
    view = finder.findRequiredView(source, 2131296362, "field 'isCurrentSchool'");
    target.isCurrentSchool = finder.castView(view, 2131296362, "field 'isCurrentSchool'");
  }

  @Override public void unbind(T target) {
    target.schoolCancel = null;
    target.schoolSave = null;
    target.schoolDelete = null;
    target.schoolName = null;
    target.schoolDegree = null;
    target.fromYear = null;
    target.fromMonth = null;
    target.toYear = null;
    target.toMonth = null;
    target.toDateLayout = null;
    target.isCurrentSchool = null;
  }
}
