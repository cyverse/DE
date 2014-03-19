package org.iplantc.admin.belphegor.client.systemMessage.view;

import org.iplantc.de.client.models.systemMessages.SystemMessage;
import org.iplantc.de.client.models.systemMessages.SystemMessageFactory;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.ValueAwareEditor;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.autobean.shared.Splittable;
import com.google.web.bindery.autobean.shared.impl.StringQuoter;

import com.sencha.gxt.core.client.util.DateWrapper;
import com.sencha.gxt.data.shared.StringLabelProvider;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TimeField;

import java.util.Date;
import java.util.List;

class EditCreateSystemMessageDialog extends Composite implements ValueAwareEditor<SystemMessage>, TakesValue<SystemMessage> {

    private static CreateSystemMessageDialogUiBinder uiBinder = GWT.create(CreateSystemMessageDialogUiBinder.class);

    @UiTemplate("SystemMessageDialogPanel.ui.xml")
    interface CreateSystemMessageDialogUiBinder extends UiBinder<Widget, EditCreateSystemMessageDialog> {}
    
    interface EditorDriver extends SimpleBeanEditorDriver<SystemMessage, EditCreateSystemMessageDialog> {}
    private final EditorDriver editorDriver = GWT.create(EditorDriver.class);

    @UiField
    VerticalLayoutContainer vlc;

    @UiField
    @Path("type")
    SimpleComboBox<String> typeCombo;

    @UiField
    @Path("body")
    TextArea messageField;

    @UiField
    @Path("activationTime")
    DateField activationDateField;
    @UiField
    @Ignore
    TimeField activationTimeField;

    @UiField
    @Path("deactivationTime")
    DateField deActivationDateField;
    @UiField
    @Ignore
    TimeField deActivationTimeField;

    @UiField
    @Path("dismissible")
    CheckBox dismissibleField;

    @UiField
    @Path("loginsDisabled")
    CheckBox loginsDisabledField;

    @UiField(provided = true)
    @Ignore
    Date minTime = new DateWrapper().clearTime().asDate();

    @UiField(provided = true)
    @Ignore
    Date maxTime = new DateWrapper().clearTime().addHours(23).addSeconds(46).asDate();

    private final List<String> announcementTypes = Lists.newArrayList("warning", "announcement", "maintenance");

    static EditCreateSystemMessageDialog createSystemMessage(List<String> announcementTypes) {
        SystemMessageFactory factory = GWT.create(SystemMessageFactory.class);
        final Splittable createSplittable = StringQuoter.createSplittable();
        final Date date = new Date();
        final String currentDateAsEpochString = Long.toString(date.getTime());

        StringQuoter.create(currentDateAsEpochString).assign(createSplittable, "date_created");
        StringQuoter.create(currentDateAsEpochString).assign(createSplittable, "activation_date");

        final DateWrapper addDays = new DateWrapper().addDays(7);
        final String oneWeekFromNowAsEpochString = Long.toString(addDays.getTime());
        StringQuoter.create(oneWeekFromNowAsEpochString).assign(createSplittable, "deactivation_date");
        SystemMessage newSysMessage = factory.systemMessage().as();
        newSysMessage.setCreationTime(new Date());
        newSysMessage.setActivationTime(new Date());
        newSysMessage.setDeactivationTime(addDays.asDate());

        EditCreateSystemMessageDialog dlg = new EditCreateSystemMessageDialog(newSysMessage, announcementTypes);
        return dlg;
    }
    
    static EditCreateSystemMessageDialog editSystemMessage(SystemMessage message, List<String> announcementTypes) {
        EditCreateSystemMessageDialog dlg = new EditCreateSystemMessageDialog(message, announcementTypes);
        return dlg;
    }

    private EditCreateSystemMessageDialog(SystemMessage message, List<String> announcementTypes) {
        // If the announcement types are not empty, clear local defaults and add them.
        if (!announcementTypes.isEmpty()) {
            this.announcementTypes.clear();
            this.announcementTypes.addAll(announcementTypes);
        }
        initWidget(uiBinder.createAndBindUi(this));
        
        editorDriver.initialize(this);
        editorDriver.edit(message);
    }

    @UiFactory
    @Ignore
    SimpleComboBox<String> createTypeCombo() {
        SimpleComboBox<String> cb = new SimpleComboBox<String>(new StringLabelProvider<String>());
        cb.add(announcementTypes);
        return cb;
    }

    @Override
    public void setValue(SystemMessage value) {
        activationTimeField.setValue(value.getActivationTime(), false);
        deActivationTimeField.setValue(value.getDeactivationTime(), false);

    }

    @UiHandler("activationDateField")
    void onActivationDateValueChange(ValueChangeEvent<Date> event) {
        DateWrapper activationTime = new DateWrapper(activationTimeField.getValue());
        final DateWrapper newActivationDate = new DateWrapper(event.getValue())
            .addHours(activationTime.getHours())
            .addMinutes(activationTime.getMinutes())
            .addSeconds(activationTime.getSeconds());
        activationDateField.setValue(newActivationDate.asDate(), false);
        activationTimeField.setValue(newActivationDate.asDate(), false);
    }

    @SuppressWarnings("deprecation")
    @UiHandler("activationTimeField")
    void onActivationTimeValueChange(ValueChangeEvent<Date> event) {
        final DateWrapper newActivationTime = new DateWrapper(activationDateField.getValue())
                .clearTime()
                .addHours(event.getValue().getHours())
                .addMinutes(event.getValue().getMinutes())
                .addSeconds(event.getValue().getSeconds());
        activationDateField.setValue(newActivationTime.asDate(), false);
    }

    @UiHandler("deActivationDateField")
    void onDeActivationDateValueChange(ValueChangeEvent<Date> event) {
        DateWrapper deactivationTime = new DateWrapper(deActivationTimeField.getValue());
        final DateWrapper newDeActivationDate = new DateWrapper(event.getValue())
            .addHours(deactivationTime.getHours())
            .addMinutes(deactivationTime.getMinutes())
            .addSeconds(deactivationTime.getSeconds());
        deActivationDateField.setValue(newDeActivationDate.asDate(), false);
        deActivationTimeField.setValue(newDeActivationDate.asDate(), false);
    }

    @SuppressWarnings("deprecation")
    @UiHandler("deActivationTimeField")
    void onDeActivationTimeValueChange(ValueChangeEvent<Date> event) {
        final DateWrapper newDeActivationTime = new DateWrapper(deActivationDateField.getValue())
                .clearTime()
                .addHours(event.getValue().getHours())
                .addMinutes(event.getValue().getMinutes())
                .addSeconds(event.getValue().getSeconds());
        deActivationDateField.setValue(newDeActivationTime.asDate(), false);
    }

    @Override
    public SystemMessage getValue() {
        typeCombo.finishEditing();
        editorDriver.flush();
        return editorDriver.flush();
    }

    public boolean hasErrors() {
        return editorDriver.hasErrors();
    }

    @Override
    public void setDelegate(EditorDelegate<SystemMessage> delegate) {
        // TODO Auto-generated method stub

    }

    @Override
    public void flush() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPropertyChange(String... paths) {
        // TODO Auto-generated method stub

    }

}
