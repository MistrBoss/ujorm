package org.version2;

import java.util.Date;
import org.version2.bo.*;
import org.version2.bo.ujo.*;

/**
 * DEMO Sample
 * @author Pavel Ponec
 */
public class Sample2 {

    public void run_01() {
        Address address = new $Address();
        address.setId(10);
        address.setCity("Brno");
        address.setCountry("Czech Republic");

        User user = new $User();
        user.setId(100);
        user.setForename("Jan");
        user.setSurname("Novák");
        user.setBirthday(new Date());
        user.setAddress(address);

        System.out.println("User: " + user);
    }

    public void run_02() {
        Address address = new Address();
        address.setId(10);
        address.setCity("Brno");
        address.setCountry("Czech Republic");

        $Address ujoAddress = new $Address(address);
        System.out.println("ujoAddress: " + ujoAddress);
    }

    public void run_03() {
        $User user = new $User();
        String city1 = "Kroměříž";
        String city2 = null;
        //
        user.set($User.ADDRESS.add($Address.CITY), city1);
        city2 = user.get($User.ADDRESS.add($Address.CITY));

        assert city1 == city2;
    }

}
