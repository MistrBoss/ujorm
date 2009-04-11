/*
 *  Copyright 2007 Paul Ponec
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

package org.ujoframework.swing;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoActionImpl;
import org.ujoframework.core.UjoManager;
import org.ujoframework.extensions.UjoAction;
import org.ujoframework.extensions.UjoTextable;
import org.ujoframework.implementation.map.MapProperty;
import org.ujoframework.implementation.map.MapUjoExt;

/**
 * An implementation of TableModel for List of Ujo objects.
 * <br>An typical usage is an preview of UjoProperty list of the one Ujo object include values.
 * 
 * @author Pavel Ponec
 */
@SuppressWarnings("unchecked")
public class UjoPropertyRow extends MapUjoExt {
    
    /** Index of property */
    public static final MapProperty<UjoPropertyRow,Integer> P_INDEX    = new MapProperty("Index" , Integer.class);
    /** Name of property */
    public static final MapProperty<UjoPropertyRow,String> P_NAME      = new MapProperty("Name" , String.class);
    /** Type of property */
    public static final MapProperty<UjoPropertyRow,Class>  P_TYPE      = new MapProperty("Class", Class.class );
    /** Class name without packages */
    public static final MapProperty<UjoPropertyRow,String> P_TYPENAME  = new MapProperty("Type" , String.class);
    /** Value */
    public static final MapProperty<UjoPropertyRow,Object> P_VALUE     = new MapProperty("Value", Object.class);
    /** Text Value */
    public static final MapProperty<UjoPropertyRow,String> P_TEXT      = new MapProperty("Text" , String.class);
    /** Default Value */
    public static final MapProperty<UjoPropertyRow,Object> P_DEFAULT   = new MapProperty("Default", Object.class);
    /** A user column can be used in table renderer for any purpose */
    public static final MapProperty<UjoPropertyRow,Object> P_USER1     = new MapProperty("User1", Object.class);
    /** A user column can be used in table renderer for any purpose */
    public static final MapProperty<UjoPropertyRow,Object> P_USER2     = new MapProperty("User2", Object.class);
    
    final protected Ujo content;
    final protected UjoProperty property;
    
    public UjoPropertyRow(Ujo content, UjoProperty property) {
        this.content = content;
        this.property = property;
    }
    
    /** Write value */
    @Override
    public void writeValue(UjoProperty aProperty, Object value) {
        if (aProperty==P_VALUE) {
            content.writeValue(property, value);
        } else if (aProperty==P_TEXT) {
            UjoManager.getInstance().setText(content, property, (String) value, null, new UjoActionImpl(this));
        } else {
            throw new UnsupportedOperationException("Can't write property " + property);
        }
    }
    
    /** Write a text value. */
    @Override
    public void writeValueString(UjoProperty aProperty, String value, Class subtype, UjoAction action) {
        if (aProperty==P_VALUE) {
            if (content instanceof UjoTextable) {
                ((UjoTextable) content).writeValueString(property, value, subtype, action);
            } else {
                final Object objValue = readUjoManager().decodeValue(property, value, subtype);
                content.writeValue(property, objValue);
            }
        } else {
            throw new UnsupportedOperationException("Can't write property " + property);
        }
    }
    
    /** Read Value */
    @Override
    public Object readValue(final UjoProperty aProperty) {
        if (aProperty==P_INDEX)   { return property.getIndex(); }
        if (aProperty==P_NAME)    { return property.getName(); }
        if (aProperty==P_TYPE)    { return property.getType(); }
        if (aProperty==P_DEFAULT) { return property.getDefault(); }
        if (aProperty==P_VALUE)   { return property.getValue(content); }
        if (aProperty==P_TEXT)    { return UjoManager.getInstance().getText(content, property, new UjoActionImpl(this)); }
        if (aProperty==P_TYPENAME){
            final String result = property.getType().getName();
            final int i = 1 + result.lastIndexOf('.');
            return result.substring(i);
        }
        throw new UnsupportedOperationException("Can't read property " + property);
    }
    
    /** Returns an assigned property (a parameter e.g.) */
    public final UjoProperty getProperty() {
        return property;
    }
    
    /** Property name + value */
    @Override
    public String toString() {
       final String result = property.getName() + ":" + content.readValue(property);
       return result;
    }

}

