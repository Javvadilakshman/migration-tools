/**
 * Copyright (c) 2012, NuoDB, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of NuoDB, Inc. nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NUODB, INC. BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.nuodb.migration.jdbc.connection;

import com.nuodb.migration.spec.ConnectionSpec;
import com.nuodb.migration.spec.DriverManagerConnectionSpec;
import com.nuodb.migration.utils.ClassUtils;
import com.nuodb.migration.utils.ReflectionException;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DriverManagerConnectionProvider implements ConnectionProvider {

    public static final String USER_PROPERTY = "user";
    public static final String PASSWORD_PROPERTY = "password";

    private transient final Log log = LogFactory.getLog(this.getClass());

    private DriverManagerConnectionSpec connectionSpec;
    private Boolean autoCommit;
    private Integer transactionIsolation;
    private DataSource dataSource;

    public DriverManagerConnectionProvider() {
    }

    public DriverManagerConnectionProvider(DriverManagerConnectionSpec connectionSpec) {
        this.connectionSpec = connectionSpec;
    }

    public DriverManagerConnectionProvider(DriverManagerConnectionSpec connectionSpec,
                                           boolean autoCommit) {
        this.connectionSpec = connectionSpec;
        this.autoCommit = autoCommit;
    }

    public DriverManagerConnectionProvider(DriverManagerConnectionSpec connectionSpec,
                                           Boolean autoCommit, Integer transactionIsolation) {
        this.connectionSpec = connectionSpec;
        this.autoCommit = autoCommit;
        this.transactionIsolation = transactionIsolation;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            dataSource = createDataSource();
        }
        return createConnection();
    }

    protected DataSource createDataSource() throws SQLException {
        try {
            Driver driver = connectionSpec.getDriver();
            if (driver == null) {
                String driverClassName = connectionSpec.getDriverClassName();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Loading driver %s", driverClassName));
                }
                driver = ClassUtils.newInstance(driverClassName);
            }
            DriverManager.registerDriver(driver);
        } catch (ReflectionException exception) {
            if (log.isWarnEnabled()) {
                log.warn("Driver can't be loaded", exception);
            }
        }

        String url = connectionSpec.getUrl();
        Properties properties = new Properties();
        if (connectionSpec.getProperties() != null) {
            properties.putAll(connectionSpec.getProperties());
        }
        String username = connectionSpec.getUsername();
        String password = connectionSpec.getPassword();
        if (username != null) {
            properties.setProperty(USER_PROPERTY, username);
        }
        if (password != null) {
            properties.setProperty(PASSWORD_PROPERTY, password);
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Creating connection pool at %s", url));
        }
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
                new DriverManagerConnectionFactory(url, properties),
                new GenericObjectPool(null), null, null, false, true);
        return new PoolingDataSource(poolableConnectionFactory.getPool());
    }

    protected Connection createConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        if (autoCommit != null) {
            connection.setAutoCommit(autoCommit);
        }
        if (transactionIsolation != null) {
            connection.setTransactionIsolation(transactionIsolation);
        }
        return connection;
    }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("Closing connection");
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public ConnectionSpec getConnectionSpec() {
        return connectionSpec;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public int getTransactionIsolation() {
        return transactionIsolation;
    }

    public void setTransactionIsolation(int transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }
}
