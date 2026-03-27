package ru.gentleman.order.config;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;

import java.sql.Types;

@SuppressWarnings("unused")
public class AxonPostgreSQLDialect extends PostgreSQLDialect {

    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        // Принудительно маппим BLOB (который хочет Axon) на Binary (который в Postgres станет bytea)
        typeContributions.getTypeConfiguration()
                .getJdbcTypeRegistry()
                .addDescriptor(Types.BLOB, BinaryJdbcType.INSTANCE);
    }
}
