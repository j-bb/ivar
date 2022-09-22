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
package ivar.fwk.jpa;

import ivar.common.AbstractObject;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.apache.openjpa.conf.OpenJPAVersion;

public class EntityManagerFactory extends AbstractObject {

    private javax.persistence.EntityManagerFactory entityManagerFactory = null;
    private static EntityManagerFactory instance = null;

    public synchronized static EntityManagerFactory getInstance() {
        return EntityManagerFactory.getInstance("openjpa");
    }

    public synchronized static EntityManagerFactory getInstance(final String persistenceUnit) {
        if (instance == null) {
            instance = new EntityManagerFactory();
            instance.connection(persistenceUnit);
        }
        return instance;
    }

    public EntityManager getNewEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    private void connection(final String persistenceUnit) {
        if (entityManagerFactory != null) {
            throw new RuntimeException("DBParser.connection() : entityManagerFactory is not null, connection() had been called twice, verboten !");
        }

        info("OpenJPA version\n" + new OpenJPAVersion().toString());

        beginBlock("Connecting to the database ...");
        info("Using persistance unit : " + persistenceUnit);
        try {
//            Properties props = new Properties();
//            if (!fortheweb) {
//                info("... This config is NOT FOR THE WEB, I repeat, NOT FOR THE WEB ...");
//                props.setProperty("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
//                props.setProperty("javax.persistence.jdbc.user", "user");
//                props.setProperty("javax.persistence.jdbc.password", "password");
//                props.setProperty("javax.persistence.jdbc.url", "jdbc:mysql://localhost:3306/composer");

//            props.setProperty("ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
//            props.setProperty("openjpa.ConnectionProperties", "DriverClassName=com.mysql.jdbc.Driver, Url=jdbc:mysql://localhost:3306/composer, Username=user, Password=password");
//            } else {
//                info("This is a web config");
//            }
//            props.setProperty("openjpa.ConnectionRetainMode", "always");
//            props.setProperty("openjpa.jdbc.DBDictionary", "batchLimit=100");
            // create the factory defined by the "openjpa" entity-manager entry
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit, null);
        } catch (Throwable t) {
            error("Error during " + this.getClass().getName() + ".connection()", t, true);
        }
        endBlock("Connecting to the database. Done.");
    }
}
