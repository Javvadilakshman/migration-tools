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
package com.nuodb.migrator.backup;

import com.nuodb.migrator.utils.xml.XmlReadContext;
import com.nuodb.migrator.utils.xml.XmlReadWriteHandlerBase;
import com.nuodb.migrator.utils.xml.XmlWriteContext;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import static com.nuodb.migrator.utils.xml.XmlAliasTypeMapper.TYPE_ATTRIBUTE;

/**
 * @author Sergey Bushik
 */
public abstract class XmlRowSetHandler<T extends RowSet> extends XmlReadWriteHandlerBase<T> implements XmlConstants {

    private static final String NAME_ATTRIBUTE = "name";
    private static final String ROW_COUNT_ATTRIBUTE = "row-count";
    private static final String COLUMN_ELEMENT = "column";
    private static final String CHUNK_ELEMENT = "chunk";

    private final String typeAttribute;

    protected XmlRowSetHandler(Class<? extends T> type, String typeAttribute) {
        super(type);
        this.typeAttribute = typeAttribute;
    }

    @Override
    protected void readAttributes(InputNode input, T target, XmlReadContext context) throws Exception {
        target.setType(context.readAttribute(input, TYPE_ATTRIBUTE, String.class));
        target.setName(context.readAttribute(input, NAME_ATTRIBUTE, String.class));
        Long rowCount = context.readAttribute(input, ROW_COUNT_ATTRIBUTE, Long.class);
        target.setRowCount(rowCount != null ? rowCount : 0);
    }

    @Override
    protected void writeAttributes(T rowSet, OutputNode output, XmlWriteContext context) throws Exception {
        context.writeAttribute(output, TYPE_ATTRIBUTE, getTypeAttribute());
        if (rowSet.getName() != null) {
            context.writeAttribute(output, NAME_ATTRIBUTE, rowSet.getName());
        }
        context.writeAttribute(output, ROW_COUNT_ATTRIBUTE, rowSet.getRowCount());
    }

    @Override
    protected void readElement(InputNode input, T rowSet, XmlReadContext context) throws Exception {
        if (COLUMN_ELEMENT.equals(input.getName())) {
            rowSet.addColumn(context.read(input, Column.class));
        } else if (CHUNK_ELEMENT.equals(input.getName())) {
            rowSet.addChunk(context.read(input, Chunk.class));
        }
    }

    @Override
    protected void writeElements(T rowSet, OutputNode output, XmlWriteContext context) throws Exception {
        for (Column column : rowSet.getColumns()) {
            context.writeElement(output, COLUMN_ELEMENT, column);
        }
        for (Chunk chunk : rowSet.getChunks()) {
            context.writeElement(output, CHUNK_ELEMENT, chunk);
        }
    }

    protected String getTypeAttribute() {
        return typeAttribute;
    }
}
