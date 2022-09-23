/*
   Copyright (c) 2004-2020, Jean-Baptiste BRIAUD. All Rights Reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License
 */
package ivar.fwk.rpc;

import ivar.common.AbstractObject;
import ivar.fwk.jpa.EntityManagerFactory;
import ivar.helper.CollectionFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.openjpa.meta.FetchGroup;
import org.apache.openjpa.persistence.*;

/**
 * Created by IntelliJ IDEA. User: jbb Date: Aug 5, 2009 Time: 3:27:37 PM To change this template use File | Settings | File Templates.
 */
public final class ControllerContext extends AbstractObject {

    private static EntityManagerFactory entityManagerFactory = null;

    private OpenJPAEntityManager entityManager;
    private Map<Class, List<String>> addedFields = CollectionFactory.newMap();
    private Map<Class, List<String>> removedFields = CollectionFactory.newMap();
    private Map<Class, List<String>> transientfields = CollectionFactory.newMap();
    private boolean fetchPlanComputed = false;

    public ControllerContext() {
        entityManagerFactory = EntityManagerFactory.getInstance();
        internalNewEntityManager();
    }

    private void internalNewEntityManager() {
        this.entityManager = OpenJPAPersistence.cast(entityManagerFactory.getNewEntityManager());
    }

    public OpenJPAQuery createQuery(String s) {
        return entityManager.createQuery(s);
    }

    public void newEntityManager() throws TransactionStillActiveException {
        if (entityManager != null && entityManager.getTransaction().isActive()) {
            throw new TransactionStillActiveException("Can't give you a new EntityManager, there is still an active transaction. Nothing had been done and tx need you do something before calling newEntityManager() again.");
        }
        internalNewEntityManager();
    }

    public void beginTransaction() {
        entityManager.getTransaction().begin();
    }

    public boolean isTransactionActive() {
        return entityManager.getTransaction().isActive();
    }

    public void commitTransaction() {
        entityManager.getTransaction().commit();
    }

    public void rollbackTransaction() {
        beginBlock("Rolling back transaction ...");
        try {
            final OpenJPAEntityTransaction transaction = entityManager.getTransaction();
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            } else {
                error("Trying to rollback while transaction is not active !");
            }
        } finally {
            endBlock("Rolling back transaction ...");
        }
    }

    public void detach(final Object o) {
        entityManager.detach(o);
    }

    public <T> T find(Class<T> tClass, Object o) {
        return entityManager.find(tClass, o);
    }

    public <T> T merge(T t) {
        return entityManager.merge(t);
    }

    public void persist(Object o) {
        entityManager.persist(o);
    }

    public void remove(Object o) {
        entityManager.remove(o);
    }

    public void addField(final Class clazz, final String field) {
        List<String> classFields = addedFields.get(clazz);
        if (classFields == null) {
            classFields = CollectionFactory.newList();
            addedFields.put(clazz, classFields);
        }
        classFields.add(field);

        final List<String> removedFields = this.removedFields.get(clazz);
        if (removedFields != null) {
            removedFields.remove(field);
        }

    }

    public void removeField(final Class clazz, final String field) {
        List<String> classFields = removedFields.get(clazz);
        if (classFields == null) {
            classFields = CollectionFactory.newList();
            removedFields.put(clazz, classFields);
        }
        classFields.add(field);
        final List<String> addedFields = this.addedFields.get(clazz);
        if (addedFields != null) {
            addedFields.remove(field);
        }
    }

    public void addTransientField(final Class clazz, final String field) {
        List<String> classFields = transientfields.get(clazz);
        if (classFields == null) {
            classFields = CollectionFactory.newList();
            transientfields.put(clazz, classFields);
        }
        classFields.add(field);
    }

    public boolean hasField(final Class clazz, final String field) {
        boolean result = false;
        if (!this.fetchPlanComputed) {
            result = true;
        } else {
            final List<String> fieldsToKeep = addedFields.get(clazz);
            final List<String> fieldsNotToKeep = transientfields.get(clazz);
            if (removedFields.get(clazz) != null) {
                fieldsNotToKeep.addAll(removedFields.get(clazz));
            }

            if ((fieldsToKeep != null && fieldsToKeep.contains(field)) || (fieldsNotToKeep != null && fieldsNotToKeep.contains(field))) {
                result = true;
            } else {
                final Class parent = clazz.getSuperclass();
                if (parent != null && parent != Object.class) {
                    result = hasField(parent, field);
                }
            }
        }
        return result;
    }

    public Map<Class, List<String>> getAllFields() {
        if (!this.fetchPlanComputed) {
            return null;
        } else {
            final Map<Class, List<String>> res = CollectionFactory.newMap();

            for (Class clazz : addedFields.keySet()) {
                List<String> classFields = CollectionFactory.newList(addedFields.get(clazz));
                res.put(clazz, classFields);
            }

            for (Class clazz : transientfields.keySet()) {
                List<String> fields = res.get(clazz);
                if (fields == null) {
                    fields = CollectionFactory.newList(transientfields.get(clazz).size());
                    res.put(clazz, fields);
                }
                fields.addAll(transientfields.get(clazz));
            }

            for (Class clazz : removedFields.keySet()) {
                List<String> fields = res.get(clazz);
                if (fields == null) {
                    fields = CollectionFactory.newList(removedFields.get(clazz).size());
                    res.put(clazz, fields);
                }
                fields.addAll(removedFields.get(clazz));
            }

            return res;
        }
    }

    public void pushFetchPlan() {
        entityManager.pushFetchPlan();
    }

    public void popFetchPlan() {
        entityManager.popFetchPlan();
    }

    public void computeFetchPlan() {
        final FetchPlan fetchPlan = entityManager.getFetchPlan();
        fetchPlan.clearFetchGroups();
        fetchPlan.clearFields();
        fetchPlan.removeFetchGroup(FetchGroup.NAME_DEFAULT);

        final Set<Class> addedKeys = addedFields.keySet();
        for (final Class key : addedKeys) {
            final List<String> classFields = addedFields.get(key);
            for (final String field : classFields) {
                fetchPlan.addField(key, field);
            }
        }

        final Set<Class> removedKeys = removedFields.keySet();
        for (final Class key : removedKeys) {
            final List<String> classFields = removedFields.get(key);
            for (final String field : classFields) {
                fetchPlan.removeField(key, field);
            }
        }

//            // Remove from FetchPlan all attributes that are not added to the fetch plan
//            final Field[] allClassFields = key.getDeclaredFields();
//            for (final Field field : allClassFields) {
//                final String fieldName = field.getName();
//                if (key == field.getDeclaringClass() && !classFields.contains(fieldName)) {
//                    addTransientField(key, fieldName);
//                    fetchPlan.removeField(key, fieldName);
////                    System.out.println("-------> FIELD REMOVE FROM FETCH PLAN ... " + key + "." + field.getName());
//                }
//            }
//        debug("     **************************************************************");
//        debug("     ************* Begin FETCH PLAN DEBUGGING ******************");
//        debug("     **************************************************************");
//        for (final String field : fetchPlan.getFields()) {
//            debug("     *** " + field);
//        }
//        debug("     **************************************************************");
//        debug("     ************* End FETCH PLAN DEBUGGING ******************");
//        debug("     **************************************************************");
        fetchPlanComputed = true;
    }

    public void close() {
        /*
        To put back the connection to the pool :
        Connection conn = (Connection) kem.getConnection();
        conn.close();
         */

        try {
            if (addedFields != null) {
                final Set<Class> keys = addedFields.keySet();
                for (final Class key : keys) {
                    final List<String> classFields = addedFields.get(key);
                    classFields.clear();
                }
                addedFields.clear();
            }
            if (removedFields != null) {
                final Set<Class> keys = removedFields.keySet();
                for (final Class key : keys) {
                    final List<String> classFields = removedFields.get(key);
                    classFields.clear();
                }
                removedFields.clear();
            }
            if (transientfields != null) {
                final Set<Class> keys = transientfields.keySet();
                for (final Class key : keys) {
                    final List<String> classFields = transientfields.get(key);
                    classFields.clear();
                }
                transientfields.clear();
            }
        } finally {
            this.fetchPlanComputed = false;
            if (entityManager != null) {
                //entityManager.clear();
                debug("OpenJPA entityManager closing ...");
                try {
                    entityManager.close();
                } catch (Throwable t) {
                    error("Fatal error during Hibernate entityManager flush & close", t, false);
                }
                debug("OpenJPA entityManager closed");
            }
        }
    }
}
