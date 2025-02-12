/*
 *  Copyright 2009 Pavel Ponec
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
package benchmark;

import benchmark.bo.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;

/**
 * OrmUjo performance test
 * @author pavel
 */
public class BenchmarkHnP {

    public static final String INFO = "** HIBERNATE " + "3.3.1.GA + POJO";

    public static final int DEFAULT_ORDER_COUNT = 2000;
    public static final int DEFAULT_ITEM_COUNT = 7;
    public static final boolean DEFAULT_COMMIT_FLUSH_MODE = false;
    //
    private final int ORDER_COUNT;
    private final int ITEM_COUNT;
    private final boolean COMMIT_FLUSH_MODE;
    //
    SessionFactory sessionFactory;
    Session session;

    public BenchmarkHnP(int countOfOrder, int countOfItem, boolean commitFlushMode) {
        this.ORDER_COUNT = countOfOrder;
        this.ITEM_COUNT = countOfItem;
        this.COMMIT_FLUSH_MODE = commitFlushMode;
    }

    /** Before the first use you must load a meta-model. */
    public void loadMetaModel() {
        Logger.getLogger("").setLevel(Level.SEVERE);

        long time1 = System.currentTimeMillis();

        sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
        session = sessionFactory.openSession();
        if (this.COMMIT_FLUSH_MODE) {
            // Note: the default mode of the Hibernate is "AUTO" due to reduced risks
            session.setFlushMode(org.hibernate.FlushMode.COMMIT);
        }

        printTime("META-DATA", time1, System.currentTimeMillis());
    }

    /** Create database and using INSERT */
    public void useInsert() {

        long time1 = System.currentTimeMillis();

        Transaction tr = session.beginTransaction();

        HbmUser user1 = new HbmUser();
        user1.setForename("Lorem ipsum dolor");
        user1.setSurname("Sit amet consectetur");
        user1.setPersonalId("12345678");
        session.save(user1);

        HbmUser user2 = new HbmUser();
        user2.setForename("Lorem ipsum dolor");
        user2.setSurname("Sit amet consectetur");
        user2.setPersonalId("12345678");
        session.save(user2);

        for (int i = 1; i <= ORDER_COUNT; i++) {

            HbmOrder order = new HbmOrder();
            order.setDateOfOrder(new Date());
            order.setDeletionReason("NO");
            order.setDiscount(new BigDecimal(100));
            order.setLanguage("cs");
            order.setOrderType("BX");
            order.setPaid(true);
            order.setParent(null);
            order.setPaymentType("C");
            order.setPublicId("P" + String.valueOf(1001000 + i));
            order.setUser(user1);
            session.save(order);

            for (int j = 1; j <= ITEM_COUNT; j++) {
                HbmOrderItem item = new HbmOrderItem();
                item.setArrival(false);
                item.setCharge(new BigDecimal(1000 - j));
                item.setDescription("Ut diam ante, aliquam ut varius at, fermentum non odio. Aliquam sodales, diam eu faucibus mattis");
                item.setOrder(order);
                item.setPrice(new BigDecimal(1000 + j));
                item.setPublicId("xxss-" + j);
                item.setUser(user2);
                session.save(item);
            }
        }

        tr.commit();
        printTime("INSERT", time1, System.currentTimeMillis());

    }

    /** Create database and using SELECT */
    @SuppressWarnings("unchecked")
    public void useSingleSelect() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        String hql = "from HbmOrderItem where deleted = :deleted and order.deleted = :deleted";
        Query query = session.createQuery(hql);
        query.setParameter("deleted", false);
        List<HbmOrderItem> items = (List<HbmOrderItem>) query.list();

        int i = 0;
        for (HbmOrderItem item : items) {
            ++i;
            Long id = item.getId();
            BigDecimal price = item.getPrice();
            if (false) {
                System.out.println(">>> Item.id: " + id + " " + price);
            }
        }

        tr.commit();
        printTime("SINGLE SELECT " + i, time1, System.currentTimeMillis());
    }

    /** Create database and using SELECT */
    @SuppressWarnings("unchecked")
    public void useEmptySelect() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();


        for (int i = -ORDER_COUNT; i < 0; i++) {
            String hql = "from HbmOrder where id = :id and deleted = :deleted";
            Query query = session.createQuery(hql);
            query.setParameter("id", new Long(i));
            query.setParameter("deleted", true);
            List<HbmOrder> items = (List<HbmOrder>) query.list();
        }

        tr.commit();
        printTime("EMPTY SELECT " + ORDER_COUNT, time1, System.currentTimeMillis());
        ;

    }

    /** Create database and using SELECT */
    @SuppressWarnings("unchecked")
    public void useMultiSelect() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        String hql = "from HbmOrder where deleted = :deleted";
        Query query = session.createQuery(hql);
        query.setParameter("deleted", false);
        List<HbmOrder> orders = (List<HbmOrder>) query.list();

        int i = 0;
        for (HbmOrder order : orders) {
            String surename = order.getUser().getSurname();
            if (false) { System.out.println("Usr.surename: " + surename); }

            hql = "from HbmOrderItem where deleted = :deleted and order = :order";
            query = session.createQuery(hql);
            query.setParameter("deleted", false);
            query.setParameter("order", order);
            List<HbmOrderItem> items = (List<HbmOrderItem>) query.list();

            for (HbmOrderItem item : items) {
                ++i;
                BigDecimal price = item.getPrice();
                BigDecimal charge = item.getCharge();
                if (true) {
                    String lang = item.getOrder().getLanguage();
                    String name = item.getUser().getForename();
                    if (false) { System.out.println(">>> Order.lang: " + lang + " User.lastname" + name); }
                }
            }
        }

        tr.commit();
        printTime("MULTI SELECT " + i, time1, System.currentTimeMillis());
    }

    /** Update a charge of the order items */
    @SuppressWarnings("unchecked")
    public void useUpdate() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        String hql = "from HbmOrderItem where deleted = :deleted and order.deleted = :deleted";
        Query query = session.createQuery(hql);
        query.setParameter("deleted", false);
        List<HbmOrderItem> items = (List<HbmOrderItem>) query.list();

        int i = 0;
        for (HbmOrderItem item : items) {
            ++i;
            item.setCharge(item.getCharge().add(BigDecimal.ONE));
            session.update(item);
        }

        tr.commit();
        printTime("UPDATE " + i, time1, System.currentTimeMillis());
    }

    /** Create database and using DELETE */
    @SuppressWarnings("unchecked")
    public void useDelete() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        String hql = "from HbmOrder";
        Query query = session.createQuery(hql);
        List<HbmOrder> orders = (List<HbmOrder>) query.list();

        for (HbmOrder order : orders) {

            hql = "delete from HbmOrderItem it where it.order = :order";
            query = session.createQuery(hql);
            query.setParameter("order", order);
            int rows = query.executeUpdate();

            session.delete(order);
        }

        session.flush();

        hql = "delete from HbmUser";
        query = session.createQuery(hql);
        int rows = query.executeUpdate();

        tr.commit();
        printTime("DELETE", time1, System.currentTimeMillis());
    }

    /** Close session */
    public void useClose() {
        session.close();
        sessionFactory.close();
    }

    /** Print time message. */
    protected void printTime(String msg, long time1, long time2) {
        long time = time2 - time1;
        double result = time / 1000d;
        System.out.println("TIME." + getClass().getSimpleName() + ": " + msg + ": " + result);
    }

    /** Test
     * <br/>Example:
     * {@codemain(new String[]{"2000"}) }
     */
    public static void main(String[] args) {
        try {
            BenchmarkHnP sample = newInstance(args);

            sample.loadMetaModel();
            sample.useInsert();
            sample.useSingleSelect();
            sample.useEmptySelect();
            sample.useMultiSelect();
            sample.useUpdate();
            sample.useDelete();
            sample.useClose();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**  Create new instance
     * <br/>Example:
     * {@code new Instance("2000". "yes") }
     */
    public static BenchmarkHnP newInstance(String... args) {
        int i = -1;
        try {
            int countOfOrder = args.length > ++i ? Integer.parseInt(args[i]) : DEFAULT_ORDER_COUNT;
            int countOfItem = /*args.length>++i ? Integer.parseInt(args[i]) :*/ DEFAULT_ITEM_COUNT;
            boolean commitFlushMode = args.length > ++i ? "tyTY".contains(args[i].substring(0, 1)) : DEFAULT_COMMIT_FLUSH_MODE;

            BenchmarkHnP result = new BenchmarkHnP(countOfOrder, countOfItem, commitFlushMode);
            printInputParameters(result, args);
            return result;
        } catch (Throwable e) {
            throw new RuntimeException("Usage: java -jar benchmark.jar [countOfOrder:int] [commitFlushMode:boolean]", e);
        }
    }

    /** Print Input Parameters */
    private static void printInputParameters(Object mainClas, Object[] params) {
        StringBuilder sb = new StringBuilder(256);
        sb.append(mainClas.getClass().getSimpleName());
        sb.append(".java");
        for (Object par : params) {
            sb.append(" ");
            sb.append(par);
        }
        System.out.println(INFO + " (" + sb + ")");
    }
}
