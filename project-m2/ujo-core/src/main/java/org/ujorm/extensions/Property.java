/*
 *  Copyright 2007-2014 Pavel Ponec
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

package org.ujorm.extensions;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.annot.Immutable;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.criterion.ValueCriterion;
import org.ujorm.validator.ValidationException;
import static org.ujorm.extensions.PropertyModifier.*;

/**
 * The main implementation of the interface {@link Key}.
 * @see AbstractUjo
 * @author Pavel Ponec
 */
@Immutable
public class Property<U extends Ujo,VALUE> implements Key<U,VALUE> {

    /** Property Separator character */
    public static final char PROPERTY_SEPARATOR = '.';
    /** Undefined index value */
    public static final int UNDEFINED_INDEX = -1;

    /** Property name */
    private String name;
    /** Property index, there are exist three index ranges:
     * <ul>
     *     <li>index == UNDEFINED_INDEX
     *     : an undefine index or a signal for auto-index action</li>
     *     <li>index &lt; UNDEFINED_INDEX
     *      : the discontinuous and ascending series of numbers that is generated using a special method</li>
     *     <li>index &gt; UNDEFINED_INDEX
     *     : the continuous and ascending series of numbers usable as a pointer to an array. This is a final state</li>
     * </ul>
     */
    private int index;
    /** Property type (class) */
    private Class<VALUE> type;
    /** Domain type type (class) */
    private Class<U> domainType;
    /** Property default value */
    private VALUE defaultValue;
    /** Input Validator */
    private Validator<VALUE> validator;
    /** Lock all properties after initialization */
    private boolean lock;


    /** A key sequencer for an index attribute
     * @see #_nextSequence()
     */
    private static int _sequencer = Integer.MIN_VALUE;

    /** Returns a next key index by a synchronized method.
     * The UJO key indexed by this method may not be in continuous series
     * however numbers have the <strong>upward direction</strong> always.
     */
    protected static synchronized int _nextRawSequence() {
        return _sequencer++;
    }

    /** Protected constructor */
    protected Property(int index) {
        this.index = index != UNDEFINED_INDEX
                ? index
                : _nextRawSequence();
    }

    /**
     * Property initialization.
     * @param name Replace the Name of key if the one is NULL.
     * @param index Replace index always, the value -1 invoke a next number from the internal sequencer.
     * @param type Replace the Type of key if the one is NULL.
     * @param defaultValue Replace the Optional default value if the one is NULL.
     * @param lock Lock the key.
     */
    @SuppressWarnings("unchecked")
    protected final Property<U,VALUE> init(final int field, final Object value) {
        checkLock();
        switch (field) {
            case NAME:
                if (this.name == null) {
                    setName((String)value);
                }
                break;
            case INDEX:
                final int idxParam = ((Integer)value).intValue();
                if (this.index < 0 && idxParam >= 0) {
                    this.index = idxParam;
                }
                break;
            case TYPE:
                if (this.type == null) {
                    this.type = (Class<VALUE>) value;
                }
                break;
            case DOMAIN_TYPE:
                if (this.domainType == null) {
                    this.domainType = (Class<U>) value;
                }
                break;
            case DEFAULT_VALUE:
                if (this.defaultValue == null) {
                    this.defaultValue = (VALUE) value;
                }
                break;
            case VALIDATOR:
                if (this.validator == null) {
                    this.validator = (Validator<VALUE>) value;
                }
                break;
            case LOCK:
                if (Boolean.TRUE.equals(value)) {
                    lock();
                    checkValidity();
                }
                break;
            default:
                final String msg = String.format("Undefined field %s with value '%s'", field, value);
                throw new IllegalArgumentException(msg);
        }
        return this;
    }

    /** Lock the Property */
    protected void lock() {
        this.lock = true;
    }
    /** Check an internal log and throw an {@code IllegalStateException} if the object is locked. */
    protected final void checkLock() throws IllegalStateException {
        if (this.lock) {
            throw new IllegalArgumentException("The key is already initialized: " + this);
        }
    }

    /** The Name must not contain any dot character */
    private void setName(String name) throws IllegalArgumentException{
        if (name==null) {
            return;
        }
        if (name.isEmpty()) {
            final String msg = String.format("Property name '%s' must not be empty"
                    , name);
            throw new IllegalArgumentException(msg);
        }
        if (isPropertySeparatorDisabled()
        && name.indexOf(PROPERTY_SEPARATOR)>0) {
            final String msg = String.format("Property name '%s' must not contain a dot character '%c'."
                    , name
                    , PROPERTY_SEPARATOR);
            throw new IllegalArgumentException(msg);
        }
        this.name = name;
    }

    /** Method returns the {@code true} in case the {@link #PROPERTY_SEPARATOR}
     * character is disabled in a key name.
     * The method can be overriden.
     * The {@code true} is a default value.
     */
    protected boolean isPropertySeparatorDisabled() {
        return true;
    }

    /** Is the key Locked? */
    @PackagePrivate final boolean isLock() {
        return lock;
    }

    /** Check validity of keys */
    protected void checkValidity() throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null for key index: #" + index);
        }
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null in the " + this);
        }
        if (defaultValue != null && !type.isInstance(defaultValue)) {
            throw new IllegalArgumentException("Default value have not properly type in the " + this);
        }
        if (this.domainType==null) {
            throw new IllegalArgumentException("Domain type is missing for the key: " + name);
        }
    }

    /** Name of Property */
    @Override
    final public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    final public String getFullName() {
        return domainType != null
             ? domainType.getSimpleName() + '.' + name
             : name ;
    }

    /** Type of Property */
    @Override
    final public Class<VALUE> getType() {
        return type;
    }

    /** Type of Property */
    @Override
    final public Class<U> getDomainType() {
        return domainType;
    }

    /** Index of Property */
    @Override
    final public int getIndex() {
        return index;
    }

    /**
     * It is a basic method for setting an appropriate type safe value to an MapUjo object.
     * <br>For the setting value is used internally a method
     *     {@link AbstractUjo#writeValue(org.ujorm.Key, java.lang.Object) }
     * @see AbstractUjo#writeValue(org.ujorm.Key, java.lang.Object)
     */
    @Override
    public void setValue(final U ujo, final VALUE value) throws ValidationException{
        if (validator != null) {
            validator.checkValue(value, this, ujo);
        }
        ujo.writeValue(this, value);
    }

    /**
     * It is a basic method for setting an appropriate type safe value to an MapUjo object.
     * <br>For the setting value is used internally a method
     *     {@link AbstractUjo#writeValue(org.ujorm.Key, java.lang.Object) }
     * @param ujo Related Ujo object
     * @param value A value to assign.
     * @param createRelations create related UJO objects in case of the composite key
     * @throws ValidationException can be throwed from an assigned input validator{@Link Validator};
     * @see AbstractUjo#writeValue(org.ujorm.Key, java.lang.Object)
     */
    public final void setValue(final U ujo, final VALUE value, boolean createRelations) throws ValidationException{
        setValue(ujo, value);
    }

    /**
     * A shortcut for the method {@link #of(org.ujorm.Ujo)}.
     * @see #of(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final VALUE getValue(final U ujo) {
        return of(ujo);
    }

    /**
     * It is a basic method for getting an appropriate type safe value from an Ujo object.
     * <br>For the getting value is used internally a method
     *     {@link AbstractUjo#readValue(org.ujorm.Key)}
     * </a>.
     * <br>Note: this method replaces the value of <strong>null</strong> for default
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the ujo object.
     * @see AbstractUjo#readValue(Key)
     */
    @SuppressWarnings("unchecked")
    @Override
    public VALUE of(final U ujo) {
        final Object result = ujo.readValue(this);
        return result!= null ? (VALUE) result : defaultValue;
    }

    /** Returns a Default key value. The value replace the <code>null<code> value in the method Ujo.readValue(...).
     * If the default value is not modified, returns the <code>null<code>.
     */
    @Override
    public VALUE getDefault() {
        return defaultValue;
    }

    /** Assign a Default value. The default value may be modified after locking - at your own risk.
     * <br />WARNING: the change of the default value modifies all values in all instances with the null value of the current key!
     */
    @SuppressWarnings("unchecked")
    public <PROPERTY extends Property> PROPERTY writeDefault(VALUE value) {
        defaultValue = value;
        if (lock) checkValidity();
        return (PROPERTY) this;
    }

    /** Assign a value from the default value. */
    public void setValueFromDefault(U ujo) {
        setValue(ujo, defaultValue);
    }

    /** Indicates whether a parameter value of the ujo "equal to" this default value. */
    @Override
    public boolean isDefault(U ujo) {
        VALUE value = of(ujo);
        final boolean result
        =  value==defaultValue
        || (defaultValue!=null && defaultValue.equals(value))
        ;
        return result;
    }

    /**
     * If the key is the direct key of the related UJO class then method returns the TRUE value.
     * The return value false means, that key is type of {@link CompositeKey}.
     * <br />
     * Note: The composite keys are excluded from from function Ujo.readProperties() by default
     * and these keys should not be sent to methods Ujo.writeValue() and Ujo.readValue().
     * @see CompositeKey
     * @since 0.81
     * @deprecated use rather a negation of the method {@link #isComposite() }
     */
    @Deprecated
    public final boolean isDirect() {
        return ! isComposite();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isComposite() {
        return false;
    }

    /** A flag for a direction of sorting. This method returns true always.
     * @since 0.85
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public boolean isAscending() {
        return true;
    }

    /** Create a new instance of the <strong>indirect</strong> key with a descending direction of order.
     * @since 0.85
     * @see #isAscending()
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public Key<U, VALUE> descending() {
        return descending(true);
    }

    /** Create a new instance of the <strong>indirect</strong> key with a descending direction of order.
     * @since 1.21
     * @see #isAscending()
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public Key<U, VALUE> descending(boolean descending) {
        return PathProperty.sort(this, !descending);
    }

    /** Get the ujorm key validator or return the {@code null} value if no validator was assigned */
    public Validator<VALUE> getValidator() {
        return validator;
    }

    /** Create new composite (indirect) instance.
     * @since 0.92
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> CompositeKey<U, T> add(final Key<? super VALUE, T> key) {
        return PathProperty.of((Key) this, key);
    }

    /** Create new composite (indirect) instance for an object type of ListKey.
     * @since 0.92
     */
    public <T> ListKey<U, T> add(ListKey<? super VALUE, T> key) {
        return new PathListProperty<U, T>(PathProperty.DEFAULT_ALIAS, (Key)this, key);
    }

    @SuppressWarnings("unchecked")
    public <T> CompositeKey<U, T> add(Key<? super VALUE, T> key, String alias) {
        return new PathProperty(alias, (Key)this, key);
    }

    /** Create new composite (indirect) instance with a required alias name
     * @since 1.43
     */
    @Override
    public CompositeKey<U, VALUE> alias(String alias) {
        return new PathProperty<U, VALUE>(alias, this);
    }

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    @Override
    public void copy(final U from, final U to) {
        to.writeValue(this, from.readValue(this));
    }

    /** Returns true if the key type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isTypeOf(final Class type) {
        return type.isAssignableFrom(this.type);
    }

    /** Returns true if the domain type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isDomainOf(final Class type) {
        return type.isAssignableFrom(this.domainType);
    }

    /**
     * Returns true, if the key value equals to a parameter value. The key value can be null.
     *
     * @param ujo A basic Ujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    @Override
    public boolean equals(final U ujo, final VALUE value) {
        final Object myValue = of(ujo);
        if (myValue==value) { return true; }

        final boolean result
        =  myValue!=null
        && value  !=null
        && myValue.equals(value)
        ;
        return result;
    }

    /**
     * Returns true, if the key name equals to the parameter value.
     */
    @Override
    public boolean equalsName(final CharSequence name) {
        return name!=null && name.toString().equals(this.name);
    }

    /** Compare to another Key object by the index and name of the key.
     * @since 1.20
     */
    public int compareTo(final Key p) {
        return index<p.getIndex() ? -1
             : index>p.getIndex() ?  1
             : name.compareTo(p.getName())
             ;
    }

    /** A char from Name */
    @Override
    public char charAt(int index) {
        return name.charAt(index);
    }

    /** Length of the Name */
    @Override
    public int length() {
        return name.length();
    }

    /** Sub sequence from the Name */
    @Override
    public CharSequence subSequence(int start, int end) {
        return name.subSequence(start, end);
    }

    /** Returns a name of Property */
    @Override
    public final String toString() {
        return name;
    }

    /** Returns the full name of the Key including a simple domain class.
     * <br />Example: Person.id
     * @deprecated Use the method {@link #getFullName()} rather.
     */
    @Deprecated
    @Override
    public final String toStringFull() {
        return getFullName();
    }

    /**
     * Returns the full name of the Key including all attributes.
     * <br />Example: Person.id {index=0, ascending=false, ...}
     * @param extended arguments false calls the method {@link #getFullName()} only.
     * @return the full name of the Key including all attributes.
     */
    @Override
    public String toStringFull(boolean extended) {
        return  extended
                ? getFullName() + Property.printAttributes(this)
                : getFullName() ;
    }

    /** Print  */
    @PackagePrivate static String printAttributes(Key key) {
        return " {index=" + key.getIndex()
            + ", ascending=" + key.isAscending()
            + ", composite=" + key.isComposite()
            + ", default=" + key.getDefault()
            + ", validator=" + (key.getValidator()!=null ? key.getValidator().getClass().getSimpleName() : null)
            + ", type=" + key.getType()
            + ", domainType=" + key.getDomainType()
            + ", class=" + key.getClass().getName()
            + "}" ;
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> where(Operator operator, VALUE value) {
        return Criterion.where(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> where(Operator operator, Key<?, VALUE> value) {
        return Criterion.where(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereEq(VALUE value) {
        return Criterion.where(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereIn(Collection<VALUE> list) {
        return Criterion.whereIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereNotIn(Collection<VALUE> list) {
        return Criterion.whereNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereIn(VALUE... list) {
        return Criterion.whereIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereNotIn(VALUE... list) {
        return Criterion.whereNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereEq(Key<U, VALUE> value) {
        return Criterion.where(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereNull() {
        return Criterion.whereNull(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereNotNull() {
        return Criterion.whereNotNull(this);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Criterion<U> whereFilled() {
        final Criterion<U> result = whereNotNull()
            .and(Criterion.where(this, Operator.NOT_EQ, (VALUE) getEmptyValue()))
                ;
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Criterion<U> whereNotFilled(){
        final Criterion<U> result = whereNull()
            .or(new ValueCriterion(this, Operator.EQ, getEmptyValue()))
                ;
        return result;
    }

    /** Returns an empty value */
    private Object getEmptyValue() {
        if (CharSequence.class.isAssignableFrom(type)) {
            return "";
        }
        if (type.isArray()) {
            return Array.newInstance(type, 0);
        }
        if (List.class.isAssignableFrom(type)) {
            return Collections.EMPTY_LIST;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereNeq(VALUE value) {
        return Criterion.where(this, Operator.NOT_EQ, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereGt(VALUE value) {
        return Criterion.where(this, Operator.GT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereGe(VALUE value) {
        return Criterion.where(this, Operator.GE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereLt(VALUE value) {
        return Criterion.where(this, Operator.LT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> whereLe(VALUE value) {
        return Criterion.where(this, Operator.LE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forSql(String sqlCondition) {
        return Criterion.forSql(this, sqlCondition);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forSql(String sqlCondition, VALUE value) {
        return Criterion.forSql(this, sqlCondition, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forSqlUnchecked(String sqlCondition, Object value) {
        return Criterion.forSqlUnchecked(this, sqlCondition, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forAll() {
        return Criterion.forAll(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forNone() {
        return Criterion.forNone(this);
    }

    // --------- STATIC METHODS -------------------

    /** Returns a new instance of key where the default value is null.
     * The method assigns a next key index.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> of(String name, Class<VALUE> type, VALUE value, Integer index, boolean lock) {
        return of(name, type, value, index, (Validator) null, lock);
    }

    /** Returns a new instance of key where the default value is null.
     * The method assigns a next key index.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> of(String name, Class<VALUE> type, VALUE value, Integer index, Validator validator, boolean lock) {
        return new Property<UJO,VALUE>(index)
                .init(NAME, name)
                .init(TYPE, type)
                .init(DEFAULT_VALUE, value)
                .init(VALIDATOR, validator)
                .init(LOCK, lock);
    }


    /** Returns a new instance of key where the default value is null.
     * The method assigns a next key index.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> of(String name, Class<VALUE> type, Class<UJO> domainType, int index) {
        final boolean lock = type!=null
                    && domainType!=null;
        return new Property<UJO,VALUE>(index)
                .init(NAME, name)
                .init(TYPE, type)
                .init(DOMAIN_TYPE, domainType)
                .init(LOCK, lock);
    }

    /** Returns a new instance of key where the default value is null.
     * The method assigns a next key index.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> of(String name, Class<VALUE> type) {
        final Class<UJO> domainType = null;
        return of(name, type, domainType, Property.UNDEFINED_INDEX);
    }

    /** Returns a new instance of key where the default value is null.
     * The method assigns a next key index.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> of(String name, Class<VALUE> type, Class<UJO> domainType) {
        return of(name, type, domainType, Property.UNDEFINED_INDEX);
    }

    /** A Property Factory where a key type is related from from default value.
     * Method assigns a next key index.
     * @hidden
     */
    public static <UJO extends Ujo, VALUE> Property<UJO, VALUE> of(String name, VALUE value, int index) {
        @SuppressWarnings("unchecked")
        Class<VALUE> type = (Class) value.getClass();
        return new Property<UJO,VALUE>(index)
                .init(NAME, name)
                .init(TYPE, type)
                .init(DEFAULT_VALUE, value);
    }

    /** A Property Factory where a key type is related from from default value.
     * Method assigns a next key index.
     * @hidden
     */
    public static <UJO extends Ujo, VALUE> Property<UJO, VALUE> of(String name, VALUE value) {
         return of(name, value, UNDEFINED_INDEX);
    }


    /** A Property Factory where a key type is related from from default value.
     * Method assigns a next key index.
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, VALUE> Property<UJO, VALUE> of(Key p, int index) {
         return of(p.getName(), p.getType(), p.getDefault(), index, true);
    }


    /** A Property Factory where a key type is related from from default value.
     * <br />Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, VALUE> Key<UJO, VALUE> of(Key p) {
         return of(p.getName(), p.getType(), p.getDefault(), UNDEFINED_INDEX, false);
    }

    // --------- DEPRECATED STATIC METHODS -------------------

    /** Returns a new instance of key where the default value is null.
     * The method assigns a next key index.
     * @deprecated Use the of(...) operator
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newInstance(String name, Class<VALUE> type, VALUE value, Integer index, boolean lock) {
        return newInstance(name, type, value, index, (Validator) null, lock);
    }

    /** Returns a new instance of key where the default value is null.
     * The method assigns a next key index.
     * @deprecated Use the of(...) operator
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newInstance(String name, Class<VALUE> type, VALUE value, Integer index, Validator validator, boolean lock) {
        return new Property<UJO,VALUE>(index)
                .init(NAME, name)
                .init(TYPE, type)
                .init(DEFAULT_VALUE, value)
                .init(VALIDATOR, validator)
                .init(LOCK, lock);
    }


    /** Returns a new instance of key where the default value is null.
     * The method assigns a next key index.
     * @deprecated Use the of(...) operator
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newInstance(String name, Class<VALUE> type, Class<UJO> domainType, int index) {
        final boolean lock = type!=null
                    && domainType!=null;
        return new Property<UJO,VALUE>(index)
                .init(NAME, name)
                .init(TYPE, type)
                .init(DOMAIN_TYPE, domainType)
                .init(LOCK, lock);
    }

    /** Returns a new instance of key where the default value is null.
     * The method assigns a next key index.
     * @deprecated Use the of(...) operator
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newInstance(String name, Class<VALUE> type) {
        final Class<UJO> domainType = null;
        return newInstance(name, type, domainType, Property.UNDEFINED_INDEX);
    }

    /** Returns a new instance of key where the default value is null.
     * The method assigns a next key index.
     * @deprecated Use the of(...) operator
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newInstance(String name, Class<VALUE> type, Class<UJO> domainType) {
        return newInstance(name, type, domainType, Property.UNDEFINED_INDEX);
    }

    /** A Property Factory where a key type is related from from default value.
     * Method assigns a next key index.
     * @deprecated Use the of(...) operator
     * @hidden
     */
    public static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newInstance(String name, VALUE value, int index) {
        @SuppressWarnings("unchecked")
        Class<VALUE> type = (Class) value.getClass();
        return new Property<UJO,VALUE>(index)
                .init(NAME, name)
                .init(TYPE, type)
                .init(DEFAULT_VALUE, value);
    }

    /** A Property Factory where a key type is related from from default value.
     * Method assigns a next key index.
     * @deprecated Use the of(...) operator
     * @hidden
     */
    public static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newInstance(String name, VALUE value) {
         return newInstance(name, value, UNDEFINED_INDEX);
    }


    /** A Property Factory where a key type is related from from default value.
     * Method assigns a next key index.
     * @deprecated Use the of(...) operator
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newInstance(Key p, int index) {
         return newInstance(p.getName(), p.getType(), p.getDefault(), index, true);
    }


    /** A Property Factory where a key type is related from from default value.
     * <br />Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use the of(...) operator
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, VALUE> Key<UJO, VALUE> newInstance(Key p) {
         return newInstance(p.getName(), p.getType(), p.getDefault(), UNDEFINED_INDEX, false);
    }


}
