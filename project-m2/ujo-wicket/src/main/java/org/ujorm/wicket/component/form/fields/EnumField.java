/*
 *  Copyright 2013 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.wicket.component.form.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.wicket.component.tools.ChoiceRendererNullable;

/**
 * CheckBox field with a Label including a feedback message.
 * @author Pavel Ponec
 */
public class EnumField<T extends Enum<T>> extends Field<T> {
    private static final long serialVersionUID = 20130621L;
    /** This component does not support the {@code null} item now. */
    private static final boolean NULL_SUPPORT = false;
    /** Available items */
    private List<T> items;

    public <U extends Ujo> EnumField(Key<U, T> key) {
        this(key, null);
    }

    public <U extends Ujo> EnumField(Key<U, T> key, String cssClass) {
        this(key.getName(), key, cssClass);
    }

    public <U extends Ujo> EnumField(String componentId, Key<U, T> key, String cssClass) {
        super(componentId, key, null);
        this.items = Arrays.asList(key.getType().getEnumConstants());
        if (NULL_SUPPORT && !isRequired()) {
            ArrayList list = new ArrayList(items.size() + 1);
            list.add(null);
            list.addAll(items);
            items = Collections.unmodifiableList(list);
        }
    }

    /** Create Form inputComponent */
    @Override
    protected FormComponent createInput(String componentId, IModel<T> model) {
        DropDownChoice<T> result = new DropDownChoice<T>(componentId, new Model(), getItems());
        result.setChoiceRenderer(new ChoiceRendererNullable<T>(this));
        result.setEnabled(isEnabled());
        result.setLabel(createLabelModel());
        return result;
    }

    /**
     * Get the value for displaying to an end user.
     * @param object the actual object
     * @return the value meant for displaying to an end user
     */
    protected Object getComboDisplayValue(T object) {
        return object.name();
    }

    /**
     * This method is called to get the id value of an object (used as the value attribute of a
     * choice element) The id can be extracted from the object like a primary key,
     * or if the list is stable you could just return a toString of the index.
     * <p>
     * Note that the given index can be {@code -1} if the object in question is not contained in the
     * available choices.
     *
     * @param object The object for which the id should be generated
     * @param index The index of the object in the choices list.
     * @return String
     */
    protected String getComboIdValue(T object, int index) {
        return object.name();
    }

    /** Get component items */
    public List<T> getItems() {
        return items;
    }


}