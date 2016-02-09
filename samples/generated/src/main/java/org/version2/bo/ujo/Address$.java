/* Generated objedt, do not modify it */
package org.version2.bo.ujo;

import java.util.List;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.UjoMiddle;
import org.version2.bo.*;

/**
 * Address$
 * @author Pavel Ponec
 */
public class Address$ extends Address implements UjoMiddle<Address$> {

    /** Key factory */
    private static final KeyFactory<Address$> f = new KeyFactory<Address$>(Address$.class, false);
    //
    public static final Key<Address$, Integer> ID = f.newKey();
    public static final Key<Address$, String> STREET = f.newKey();
    public static final Key<Address$, String> CITY = f.newKey();
    public static final Key<Address$, String> COUNTRY = f.newKey();

    static {
        f.lock();
    }

    private final Address data;

    public Address$(Address data) {
        this.data = data;
    }

    public Address$() {
        this(new Address());
    }

    public Address original() {
        return data;
    }

    @Override
    public <VALUE> VALUE get(Key<? super Address$, VALUE> key) {
        return key.of(this);
    }

    @Override
    public <VALUE> Address$ set(Key<? super Address$, VALUE> key, VALUE value) {
        key.setValue(this, value);
        return this;
    }

    @Override
    public <VALUE> List<VALUE> getList(ListKey<? super Address$, VALUE> key) {
        return key.getList(this);
    }

    @Override
    public String getText(Key key) {
        return UjoManager.getInstance().getText(this, key, null);
    }

    @Override
    public void setText(Key key, String value) {
        UjoManager.getInstance().setText(this, key, value, null, null);
    }

    @Override
    public KeyList<Address$> readKeyList() {
        return f.getKeys();
    }

    @Override
    public <U extends Ujo> KeyList<U> readKeys() {
        return (KeyList<U>) readKeyList();
    }

    @Override
    public Object readValue(Key<?, ?> key) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void writeValue(Key<?, ?> key, Object value) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean readAuthorization(UjoAction action, Key<?, ?> key, Object value) {
        return true;
    }

    // ---------- GETTERS AND SETTERS ----------

    @Override
    public Integer getId() {
        return ID.of(this);
    }

    @Override
    public void setId(Integer id) {
        ID.setValue(this, id);
    }

    @Override
    public String getStreet() {
        return STREET.of(this);
    }

    @Override
    public void setStreet(String street) {
        STREET.setValue(this, street);
    }

    @Override
    public String getCity() {
        return CITY.of(this);
    }

    @Override
    public void setCity(String city) {
        CITY.setValue(this, city);
    }

    @Override
    public String getCountry() {
        return COUNTRY.of(this);
    }

    @Override
    public void setCountry(String country) {
        COUNTRY.setValue(this, country);
    }

}
