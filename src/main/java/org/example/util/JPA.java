package org.example.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

    public class JPA{

        private static final String PERSISTENCE_UNIT_NAME = "h2-persistence-unit";
        private static EntityManagerFactory entityManagerFactory;

        static {
            try {
                entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            } catch (Exception e) {
                System.err.println("Error initializing EntityManagerFactory: " + e.getMessage());
                e.printStackTrace();
                throw new ExceptionInInitializerError(e);
            }
        }

        public static EntityManager getEntityManager() {
            return entityManagerFactory.createEntityManager();
        }


        public static void close() {
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
        }
    }


