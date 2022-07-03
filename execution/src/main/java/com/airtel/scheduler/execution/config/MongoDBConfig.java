package com.airtel.scheduler.execution.config;

import com.airtel.scheduler.execution.constants.CommonConstants;
import com.airtel.scheduler.execution.properties.MongoProperties;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.SocketSettings;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
public class MongoDBConfig extends AbstractMongoClientConfiguration {

    @Autowired
    private MongoProperties mongoProperties;

    @Override
    @Bean(name = "primaryMongoClient")
    public MongoClient mongoClient() {
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applicationName(CommonConstants.APPLICATION_NAME)
                .applyConnectionString(this.buildConnectionString())
                .applyToSocketSettings(socketBuilder -> socketBuilder.applySettings(this.socketSettingsBuilder()))
                .applyToConnectionPoolSettings(poolBuilder -> poolBuilder.applySettings(this.connectionPoolBuilder()))
                .readConcern(ReadConcern.AVAILABLE)
                .retryWrites(Boolean.TRUE)
                .readPreference(mongoProperties.isPrimaryPreference() ? ReadPreference.primary() : ReadPreference.secondaryPreferred())
                .writeConcern(WriteConcern.ACKNOWLEDGED)
                .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Override
    public Collection<String> getMappingBasePackages() {
        return Collections.singleton("com.airtel.scheduler.*");
    }

    @Override
    protected String getDatabaseName() {
        return mongoProperties.getPrimaryDatabase();
    }

    @Primary
    @Bean("primaryMongoFactory")
    public SimpleMongoClientDatabaseFactory mongoDbFactory(@Qualifier("primaryMongoClient") MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, this.getDatabaseName());
    }

    @Bean("secondaryMongoTemplate")
    public MongoTemplate secondaryMongoTemplate(@Qualifier("primaryMongoFactory") SimpleMongoClientDatabaseFactory mongoDbFactory) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory);
        mongoTemplate.setReadPreference(ReadPreference.secondaryPreferred());
        mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED);
        return mongoTemplate;
    }

    @Bean("primaryMongoTemplate")
    public MongoTemplate primaryMongoTemplate(@Qualifier("primaryMongoFactory") SimpleMongoClientDatabaseFactory mongoDbFactory) {
        return new MongoTemplate(mongoDbFactory);
    }

    private ConnectionString buildConnectionString() {
        String connectionUri = CommonConstants.MONGO_URI_PREFIX  + mongoProperties.getUri() + "/" + getDatabaseName();
        return new ConnectionString(connectionUri);
    }

    private ConnectionPoolSettings connectionPoolBuilder() {
        ConnectionPoolSettings.Builder connectionPoolBuilder = ConnectionPoolSettings.builder();
        connectionPoolBuilder.minSize(mongoProperties.getMinPoolSize());
        connectionPoolBuilder.maxConnectionLifeTime(mongoProperties.getConnectLifeTimeout(), TimeUnit.MILLISECONDS);
        connectionPoolBuilder.maxConnectionIdleTime(mongoProperties.getConnectIdleTimeout(), TimeUnit.MILLISECONDS);
        connectionPoolBuilder.maxWaitTime(mongoProperties.getMaxWaitTime(), TimeUnit.MILLISECONDS);
        return connectionPoolBuilder.build();
    }

    private SocketSettings socketSettingsBuilder() {
        SocketSettings.Builder socketSettingsBuilder = SocketSettings.builder();
        socketSettingsBuilder.connectTimeout(mongoProperties.getConnectTimeout(), TimeUnit.MILLISECONDS);
        socketSettingsBuilder.readTimeout(mongoProperties.getReadTimeOut(), TimeUnit.MILLISECONDS);
        return socketSettingsBuilder.build();
    }
}