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
package com.nuodb.migrator.jdbc.type;

import com.nuodb.migrator.jdbc.connection.ConnectionProxy;
import com.nuodb.migrator.jdbc.model.Column;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Sergey Bushik
 */
@SuppressWarnings("unchecked")
public class SimpleJdbcValueSetter implements JdbcValueSetter {

    private final JdbcTypeRegistry jdbcTypeRegistry;
    private final JdbcType jdbcType;

    public SimpleJdbcValueSetter(JdbcTypeRegistry jdbcTypeRegistry, JdbcType jdbcType) {
        this.jdbcTypeRegistry = jdbcTypeRegistry;
        this.jdbcType = jdbcType;
    }

    @Override
    public <X> void setValue(PreparedStatement statement, int columnIndex, Column columnModel, X value,
                             Map<String, Object> options) throws SQLException {
        JdbcTypeAdapter<X> adapter = jdbcTypeRegistry.getJdbcTypeAdapter(value != null ? value.getClass() : null,
                jdbcType.getValueClass());
        if (adapter != null) {
            Connection connection = statement.getConnection();
            value = adapter.wrap(value, getConnection(connection));
        }
        jdbcType.setValue(statement, columnIndex, columnModel, value, options);
    }

    protected Connection getConnection(Connection connection) {
        if (connection instanceof ConnectionProxy) {
            return ((ConnectionProxy) connection).getConnection();
        } else {
            return connection;
        }
    }
}