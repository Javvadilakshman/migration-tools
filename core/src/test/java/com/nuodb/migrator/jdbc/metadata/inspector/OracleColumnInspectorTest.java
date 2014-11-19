package com.nuodb.migrator.jdbc.metadata.inspector;
/**
 * Copyright (c) 2014, NuoDB, Inc.
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

import static com.google.common.collect.Iterables.get;
import static com.nuodb.migrator.jdbc.metadata.MetaDataType.COLUMN;
import static com.nuodb.migrator.jdbc.metadata.MetaDataUtils.createColumn;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.Collection;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.nuodb.migrator.jdbc.metadata.Column;

/**
 * @author Mahesha Godekere
 */

public class OracleColumnInspectorTest extends InspectorTestBase {

    String catalogName = null, schemaName = "schema", tableName = "table";

    public OracleColumnInspectorTest() {
        super(OracleColumnInspector.class);
    }

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
       
    }

    @DataProvider(name = "getColumnData")
    public Object[][] createGetTypeNameData() throws Exception{
        
        String columnName = "column";
        
        Column userTypeCol = createColumn(catalogName, schemaName, tableName, columnName);
        userTypeCol.setTypeName("usertype");
        userTypeCol.setTypeCode(Types.OTHER);
        userTypeCol.setUserDefinedType(true);        

        Column nonUserTypeCol = createColumn(catalogName, schemaName, tableName, columnName);
        nonUserTypeCol.setTypeName("CHAR");
        nonUserTypeCol.setTypeCode(Types.CHAR);
        nonUserTypeCol.setUserDefinedType(false);
        
        return new Object[][] {
                { userTypeCol },
                { nonUserTypeCol } 
        };
    }

    @Test(dataProvider = "getColumnData")
    public void testUserDefinedType(Column column) throws Exception {
        configureUserDefinedResultSet(column.isUserDefinedType() ? column.getTypeName() : null);
        configureMetaDataResultSet(column.getName(),column.getTypeCode(), column.getTypeName());
        
        TableInspectionScope inspectionScope = new TableInspectionScope(catalogName, schemaName, tableName);
        InspectionResults inspectionResults = getInspectionManager().inspect(getConnection(), inspectionScope, COLUMN);
        verifyInspectScope(getInspector(), inspectionScope);

        Collection<Column> columns = inspectionResults.getObjects(COLUMN);
        assertNotNull(columns);
        assertEquals(columns.size(), 1);
        
        assertEquals(get(columns, 0).isUserDefinedType(), column.isUserDefinedType());
    }

    private ResultSet configureUserDefinedResultSet(String typeNameValue) throws Exception{
        PreparedStatement query = mock(PreparedStatement.class);
        given(getConnection().prepareStatement(anyString(), anyInt(), anyInt())).willReturn(query);

        given(getConnection().prepareStatement(anyString())).willReturn(query);

        ResultSet udResultSet = mock(ResultSet.class);
        given(query.executeQuery()).willReturn(udResultSet);
        given(udResultSet.next()).willReturn(true, false);
        given(udResultSet.getString(1)).willReturn(typeNameValue);
        
        return udResultSet;
        
    }

    private ResultSet configureMetaDataResultSet(String columnName, int typeCode, String typeName) throws Exception{
        
        ResultSet resultSet = mock(ResultSet.class);
        DatabaseMetaData dbmd = mock(DatabaseMetaData.class);
        
        ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
        given(resultSet.getMetaData()).willReturn(rsmd);
        given(resultSet.getString("IS_AUTOINCREMENT")).willReturn(null);
        given(getConnection().getMetaData()).willReturn(dbmd);
        given(getConnection().getMetaData().getColumns(anyString(), anyString(), anyString(),anyString())).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getString("TABLE_CAT")).willReturn(catalogName);
        given(resultSet.getString("TABLE_SCHEM")).willReturn(schemaName);
        given(resultSet.getString("TABLE_NAME")).willReturn(tableName);
        given(resultSet.getString("COLUMN_NAME")).willReturn(columnName);
        given(resultSet.getString("COLUMN_DEF")).willReturn("");
        given(resultSet.getInt("DATA_TYPE")).willReturn(typeCode);
        given(resultSet.getString("TYPE_NAME")).willReturn(typeName);
        given(resultSet.getInt("DECIMAL_DIGITS")).willReturn(0);
        given(resultSet.getString("IS_AUTOINCREMENT")).willReturn(null);
        given(resultSet.getString("IS_NULLABLE")).willReturn("YES");
        given(resultSet.getInt("ORDINAL_POSITION")).willReturn(1);        
        
        return resultSet;
    }

}
