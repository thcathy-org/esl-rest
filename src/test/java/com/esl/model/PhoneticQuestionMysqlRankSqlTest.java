package com.esl.model;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.sql.model.MutationOperation;
import org.hibernate.sql.model.PreparableMutationOperation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneticQuestionMysqlRankSqlTest {

    @Test
    void mysqlInsertAndUpdateQuoteRank() {
        var registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.JAKARTA_JDBC_DRIVER, "org.h2.Driver")
                .applySetting(AvailableSettings.JAKARTA_JDBC_URL, "jdbc:h2:mem:rankquote;MODE=MySQL")
                .applySetting(AvailableSettings.JAKARTA_JDBC_USER, "sa")
                .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.MySQLDialect")
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "none")
                .build();
        try (var factory = new MetadataSources(registry)
                .addAnnotatedClass(PhoneticQuestion.class)
                .addAnnotatedClass(Grade.class)
                .buildMetadata()
                .getSessionFactoryBuilder()
                .build()) {
            var persister = (AbstractEntityPersister) factory.unwrap(SessionFactoryImplementor.class)
                    .getMappingMetamodel()
                    .getEntityDescriptor(PhoneticQuestion.class);
            var insert = sql(persister.getInsertCoordinator().getStaticInsertGroup().getSingleOperation());
            var update = sql(persister.getUpdateCoordinator().getStaticUpdateGroup().getSingleOperation());

            assertQuotedRank(insert);
            assertQuotedRank(update);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static String sql(MutationOperation operation) {
        return ((PreparableMutationOperation) operation).getSqlString();
    }

    private static void assertQuotedRank(String sql) {
        assertTrue(sql.contains("`RANK`"), sql);
        assertFalse(sql.matches("(?s).*[(,]\\s*RANK\\s*[,)].*"), sql);
    }
}
