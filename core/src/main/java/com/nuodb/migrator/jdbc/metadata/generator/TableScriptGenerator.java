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
package com.nuodb.migrator.jdbc.metadata.generator;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import com.nuodb.migrator.jdbc.dialect.Dialect;
import com.nuodb.migrator.jdbc.metadata.Check;
import com.nuodb.migrator.jdbc.metadata.Column;
import com.nuodb.migrator.jdbc.metadata.DatabaseInfo;
import com.nuodb.migrator.jdbc.metadata.ForeignKey;
import com.nuodb.migrator.jdbc.metadata.Identifiable;
import com.nuodb.migrator.jdbc.metadata.Index;
import com.nuodb.migrator.jdbc.metadata.MetaDataType;
import com.nuodb.migrator.jdbc.metadata.PrimaryKey;
import com.nuodb.migrator.jdbc.metadata.Sequence;
import com.nuodb.migrator.jdbc.metadata.Table;
import com.nuodb.migrator.jdbc.metadata.Trigger;
import com.nuodb.migrator.jdbc.type.JdbcType;

import java.util.Collection;
import java.util.Iterator;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.nuodb.migrator.jdbc.metadata.IndexUtils.getNonRepeatingIndexes;
import static com.nuodb.migrator.jdbc.metadata.MetaDataType.*;
import static com.nuodb.migrator.jdbc.metadata.generator.ScriptGeneratorManager.*;
import static com.nuodb.migrator.jdbc.metadata.generator.ScriptGeneratorUtils.getCreateMultipleIndexes;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * @author Sergey Bushik
 */
@SuppressWarnings({"all"})
public class TableScriptGenerator extends ScriptGeneratorBase<Table> {

    public TableScriptGenerator() {
        super(Table.class);
    }

    @Override
    public Collection<String> getCreateScripts(Table table, ScriptGeneratorManager scriptGeneratorManager) {
        Collection<String> scripts = newArrayList();
        if (addScripts(table, scriptGeneratorManager)) {
            addCreateSequencesScripts(table, scripts, scriptGeneratorManager);
            addCreateTableScript(table, scripts, scriptGeneratorManager);
            addCreatePrimaryKeyScript(table, scripts, scriptGeneratorManager);
            addCreateIndexesScripts(table, scripts, scriptGeneratorManager);
            addCreateTriggersScripts(table, scripts, scriptGeneratorManager);
            addCreateForeignKeysScripts(table, scripts, scriptGeneratorManager);
        }
        return scripts;
    }

    protected boolean addScripts(Table table, ScriptGeneratorManager scriptGeneratorManager) {
        Collection<String> tableTypes = (Collection<String>) scriptGeneratorManager.getAttributes().get(TABLE_TYPES);
        return tableTypes != null ? tableTypes.contains(table.getType()) : Table.TABLE.equals(table.getType());
    }

    protected boolean addCreateScripts(Table table, MetaDataType objectType,
                                       ScriptGeneratorManager scriptGeneratorManager) {
        Collection<MetaDataType> objectTypes = scriptGeneratorManager.getObjectTypes();
        return objectTypes.contains(objectType);
    }

    protected boolean addScriptsInCreateTable(Table table, MetaDataType objectType,
                                              ScriptGeneratorManager scriptGeneratorManager) {
        Object scriptsInCreateTable = scriptGeneratorManager.getAttribute(SCRIPTS_IN_CREATE_TABLE);
        return scriptsInCreateTable instanceof Boolean ? (Boolean) scriptsInCreateTable :
                SCRIPTS_IN_CREATE_TABLE_DEFAULT;
    }

    protected void addCreateSequencesScripts(Table table, Collection<String> scripts,
                                             ScriptGeneratorManager scriptGeneratorManager) {
        boolean createSequences = addCreateScripts(table, SEQUENCE, scriptGeneratorManager) &&
                scriptGeneratorManager.getTargetDialect().supportsSequence();
        if (!createSequences) {
            return;
        }
        for (Sequence sequence : table.getSequences()) {
            scripts.addAll(scriptGeneratorManager.getCreateScripts(sequence));
        }
    }

    protected void addCreatePrimaryKeyScript(Table table, Collection<String> scripts,
                                             ScriptGeneratorManager scriptGeneratorManager) {
        boolean createPrimaryKey = addCreateScripts(table, PRIMARY_KEY, scriptGeneratorManager) ?
                !addScriptsInCreateTable(table, PRIMARY_KEY, scriptGeneratorManager) : false;
        if (!createPrimaryKey) {
            return;
        }
        PrimaryKey primaryKey = table.getPrimaryKey();
        if (primaryKey != null) {
            scripts.addAll(scriptGeneratorManager.getCreateScripts(primaryKey));
        }
    }

    protected void addCreateIndexesScripts(Table table, Collection<String> scripts,
                                           ScriptGeneratorManager scriptGeneratorManager) {
        Collection<MetaDataType> objectTypes = scriptGeneratorManager.getObjectTypes();
        boolean createIndexes = addCreateScripts(table, INDEX, scriptGeneratorManager);
        if (!createIndexes) {
            return;
        }
        Dialect dialect = scriptGeneratorManager.getTargetDialect();
        Collection<String> indexes = newLinkedHashSet();
        Collection<Index> nonRepeatingIndexes = getNonRepeatingIndexes(table,
                new Predicate<Index>() {
                    @Override
                    public boolean apply(Index index) {
                        if (logger.isTraceEnabled()) {
                            String indexName = index.getName();
                            String tableName = index.getTable().getQualifiedName();
                            Iterable<String> columnsNames = transform(index.getTable().getColumns(),
                                    new Function<Identifiable, String>() {
                                        @Override
                                        public String apply(Identifiable column) {
                                            return column.getName();
                                        }
                                    });
                            logger.trace(format("Index %s on table %s skipped " +
                                    "as index with column(s) %s is added already",
                                    indexName, tableName, join(columnsNames, ", ")));
                        }
                        return true;
                    }
                });
        boolean addIndexesInCreateTable = addScriptsInCreateTable(table, INDEX, scriptGeneratorManager);
        Collection<Index> multipleIndexes = newArrayList();
        for (Index nonRepeatingIndex : nonRepeatingIndexes) {
            boolean uniqueInCreateTable =
                    nonRepeatingIndex.isUnique() && size(nonRepeatingIndex.getColumns()) == 1 &&
                            !get(nonRepeatingIndex.getColumns(), 0).isNullable() &&
                            dialect.supportsUniqueInCreateTable() && addIndexesInCreateTable;
            if (!nonRepeatingIndex.isPrimary() || !uniqueInCreateTable) {
                if (dialect.supportsCreateMultipleIndexes()) {
                    multipleIndexes.add(nonRepeatingIndex);
                } else {
                    indexes.addAll(scriptGeneratorManager.getCreateScripts(nonRepeatingIndex));
                }
            }
        }
        // join multiple indexes with comma and add this statement to scripts
        if (!multipleIndexes.isEmpty() && dialect.supportsCreateMultipleIndexes()) {
            indexes.addAll(getCreateMultipleIndexes(multipleIndexes, scriptGeneratorManager));
        }
        scripts.addAll(indexes);
    }

    protected void addCreateTriggersScripts(Table table, Collection<String> scripts,
                                            ScriptGeneratorManager scriptGeneratorManager) {
        boolean createTriggers = addCreateScripts(table, TRIGGER, scriptGeneratorManager);
        boolean createColumnTriggers = addCreateScripts(table, COLUMN_TRIGGER, scriptGeneratorManager);
        if (!createTriggers && !createColumnTriggers) {
            return;
        }
        for (Trigger trigger : table.getTriggers()) {
            if (trigger.getObjectType() == TRIGGER && createTriggers) {
                scripts.addAll(scriptGeneratorManager.getCreateScripts(trigger));
            } else if (trigger.getObjectType() == COLUMN_TRIGGER && createColumnTriggers) {
                scripts.addAll(scriptGeneratorManager.getCreateScripts(trigger));
            }
        }
    }

    protected void addCreateForeignKeysScripts(Table table, Collection<String> scripts,
                                               ScriptGeneratorManager scriptGeneratorManager) {
        boolean createForeignKeys = addCreateScripts(table, FOREIGN_KEY, scriptGeneratorManager) ?
                !addScriptsInCreateTable(table, FOREIGN_KEY, scriptGeneratorManager) : false;
        if (!createForeignKeys) {
            return;
        }
        Collection<Table> tables = (Collection<Table>) scriptGeneratorManager.getAttribute(TABLES);
        Multimap<Table, ForeignKey> foreignKeys =
                (Multimap<Table, ForeignKey>) scriptGeneratorManager.getAttribute(FOREIGN_KEYS);
        for (ForeignKey foreignKey : table.getForeignKeys()) {
            Table primaryTable = foreignKey.getPrimaryTable();
            Table foreignTable = foreignKey.getForeignTable();
            if (!addScripts(primaryTable, scriptGeneratorManager) ||
                    !addScripts(foreignTable, scriptGeneratorManager)) {
                continue;
            }
            if (tables != null && !tables.contains(primaryTable)) {
                foreignKeys.put(primaryTable, foreignKey);
            } else {
                foreignKeys.remove(primaryTable, foreignKey);
                scripts.addAll(scriptGeneratorManager.getCreateScripts(foreignKey));
            }
        }
    }

    protected void addCreateTableScript(Table table, Collection<String> scripts, ScriptGeneratorManager
            scriptGeneratorManager) {
        boolean createTable = addCreateScripts(table, TABLE, scriptGeneratorManager);
        if (!createTable) {
            return;
        }
        Dialect dialect = scriptGeneratorManager.getTargetDialect();
        StringBuilder buffer = new StringBuilder("CREATE TABLE");
        buffer.append(' ').append(scriptGeneratorManager.getName(table)).append(" (");
        Collection<Column> columns = table.getColumns();
        Collection<Index> indexes = getNonRepeatingIndexes(table);
        Collection<Table> tables = (Collection<Table>) scriptGeneratorManager.getAttributes().get(TABLES);
        if (tables != null) {
            tables.add(table);
        }
        boolean addPrimaryKey = addScriptsInCreateTable(table, PRIMARY_KEY, scriptGeneratorManager);
        boolean addIndexes = addScriptsInCreateTable(table, INDEX, scriptGeneratorManager);
        boolean addForeignKeys = addScriptsInCreateTable(table, FOREIGN_KEY, scriptGeneratorManager);
        boolean addChecks = addCreateScripts(table, CHECK, scriptGeneratorManager);
        boolean addSequences = addCreateScripts(table, SEQUENCE, scriptGeneratorManager);
        for (Iterator<Column> iterator = columns.iterator(); iterator.hasNext(); ) {
            final Column column = iterator.next();
            buffer.append(scriptGeneratorManager.getName(column));
            buffer.append(' ');
            buffer.append(getTypeName(column, scriptGeneratorManager));
            if (column.isIdentity() && addSequences) {
                buffer.append(' ');
                buffer.append(dialect.getIdentityColumn(
                        column.getSequence() != null ?
                                scriptGeneratorManager.getName(column.getSequence()) : null));
            }
            if (column.isNullable()) {
                buffer.append(dialect.getNullColumnString());
            } else {
                buffer.append(' ');
                buffer.append("NOT NULL");
            }
            String defaultValue = dialect.getDefaultValue(column, scriptGeneratorManager.getSourceSession());
            if (defaultValue != null) {
                buffer.append(" DEFAULT ").append(defaultValue);
            }
            if (addIndexes && !dialect.supportsCreateMultipleIndexes()) {
                Optional<Index> index = tryFind(indexes, new Predicate<Index>() {
                    @Override
                    public boolean apply(Index index) {
                        Collection<Column> columns = index.getColumns();
                        return columns.size() == 1 && columns.contains(column) &&
                                !index.isPrimary() && index.isUnique();
                    }
                });
                boolean unique = index.isPresent() && (!column.isNullable() || dialect.supportsNotNullUnique());
                if (unique) {
                    if (dialect.supportsUniqueInCreateTable()) {
                        buffer.append(' ');
                        buffer.append("UNIQUE");
                        indexes.remove(index.get());
                    }
                }
            }
            if (addChecks && dialect.supportsColumnCheck()) {
                for (Check check : column.getChecks()) {
                    buffer.append(", CHECK ");
                    buffer.append(dialect.getCheckClause(check.getText()));
                }
            }
            String comment = column.getComment();
            if (!isEmpty(comment)) {
                buffer.append(dialect.getColumnComment(comment));
            }
            if (iterator.hasNext()) {
                buffer.append(", ");
            }
        }
        if (addPrimaryKey) {
            PrimaryKey primaryKey = table.getPrimaryKey();
            if (primaryKey != null) {
                ConstraintScriptGenerator<PrimaryKey> generator = (ConstraintScriptGenerator<PrimaryKey>)
                        scriptGeneratorManager.getScriptGenerator(primaryKey);
                buffer.append(", ").append(generator.getConstraintScript(primaryKey, scriptGeneratorManager));
            }
        }
        if (addIndexes &&
                (dialect.supportsIndexInCreateTable() && !dialect.supportsCreateMultipleIndexes())) {
            boolean primary = false;
            for (Index index : indexes) {
                if (!primary && index.isPrimary()) {
                    primary = true;
                    continue;
                }
                ConstraintScriptGenerator<Index> generator = (ConstraintScriptGenerator<Index>)
                        scriptGeneratorManager.getScriptGenerator(index);
                String constraint = generator.getConstraintScript(index, scriptGeneratorManager);
                if (constraint != null) {
                    buffer.append(", ").append(constraint);
                }

            }
        }
        if (addForeignKeys) {
            for (ForeignKey foreignKey : table.getForeignKeys()) {
                ConstraintScriptGenerator<ForeignKey> generator = (ConstraintScriptGenerator<ForeignKey>)
                        scriptGeneratorManager.getScriptGenerator(foreignKey);
                String constraint = generator.getConstraintScript(foreignKey, scriptGeneratorManager);
                if (constraint != null) {
                    buffer.append(", ").append(constraint);
                }
            }
        }
        if (addChecks && dialect.supportsTableCheck()) {
            for (Check check : table.getChecks()) {
                buffer.append(", CHECK ");
                buffer.append(dialect.getCheckClause(check.getText()));
            }
        }
        buffer.append(')');
        String comment = table.getComment();
        if (!isEmpty(comment)) {
            buffer.append(dialect.getTableComment(comment));
        }
        scripts.add(buffer.toString());
    }

    protected String getTypeName(Column column, ScriptGeneratorManager scriptGeneratorManager) {
        Dialect dialect = scriptGeneratorManager.getTargetDialect();
        int scale = column.getScale();
        JdbcType jdbcType = column.getJdbcType();
        if (scale < 0 && !dialect.supportsNegativeScale()) {
            jdbcType = jdbcType.withScale(0);
        }
        Dialect sourceDialect = scriptGeneratorManager.getSourceSession().getDialect();
        DatabaseInfo databaseInfo = sourceDialect.getDatabaseInfo();
        String typeName = dialect.getTypeName(databaseInfo, jdbcType);
        if (typeName == null) {
            String tableName = scriptGeneratorManager.getQualifiedName(column.getTable(),
                    column.getTable().getSchema().getName(), column.getTable().getCatalog().getName(), false);
            String columnName = scriptGeneratorManager.getName(column, false);
            Collection<String> typeInfo = newArrayList();
            typeInfo.add(format("type name %s", column.getTypeName()));
            typeInfo.add(format("type code %s", column.getTypeCode()));
            typeInfo.add(format("length %d", column.getSize()));
            if (column.getPrecision() != null) {
                typeInfo.add(format("precision %d", column.getPrecision()));
            }
            if (column.getScale() != null) {
                typeInfo.add(format("scale %d", column.getScale()));
            }
            if(column.isUserDefinedType() &&
                    sourceDialect.getUserDefinedTypeName() != null){
                logger.warn(format("Unsupported type on table %s column %s: %s",
                            tableName, columnName, join(typeInfo, ", ")));
                return sourceDialect.getUserDefinedTypeName();
            }
            throw new GeneratorException(
                    format("Unsupported type on table %s column %s: %s",
                            tableName, columnName, join(typeInfo, ", ")));
        }
        return typeName;
    }

    @Override
    public Collection<String> getDropScripts(Table table, ScriptGeneratorManager scriptGeneratorManager) {
        Collection<String> scripts = newArrayList();
        if (addScripts(table, scriptGeneratorManager)) {
            addDropTriggersScripts(table, scripts, scriptGeneratorManager);
            addDropTableScript(table, scripts, scriptGeneratorManager);
            addDropSequencesScripts(table, scripts, scriptGeneratorManager);
        }
        return scripts;
    }

    protected void addDropSequencesScripts(Table table, Collection<String> scripts,
                                           ScriptGeneratorManager scriptGeneratorManager) {
        Collection<MetaDataType> objectTypes = scriptGeneratorManager.getObjectTypes();
        boolean dropSequences = objectTypes.contains(SEQUENCE) &&
                scriptGeneratorManager.getTargetDialect().supportsSequence();
        if (!dropSequences) {
            return;
        }
        for (Sequence sequence : table.getSequences()) {
            scripts.addAll(scriptGeneratorManager.getDropScripts(sequence));
        }
    }

    protected void addDropTriggersScripts(Table table, Collection<String> scripts,
                                          ScriptGeneratorManager scriptGeneratorManager) {
        Collection<MetaDataType> objectTypes = scriptGeneratorManager.getObjectTypes();
        boolean dropTriggers = objectTypes.contains(TRIGGER);
        boolean dropColumnTriggers = objectTypes.contains(COLUMN_TRIGGER);
        if (!dropTriggers && !dropColumnTriggers) {
            return;
        }
        for (Trigger trigger : table.getTriggers()) {
            if (trigger.getObjectType() == TRIGGER && dropTriggers) {
                scripts.addAll(scriptGeneratorManager.getDropScripts(trigger));
            } else if (trigger.getObjectType() == COLUMN_TRIGGER && dropColumnTriggers) {
                scripts.addAll(scriptGeneratorManager.getDropScripts(trigger));
            }
        }
    }

    protected void addDropTableScript(Table table, Collection<String> scripts,
                                      ScriptGeneratorManager scriptGeneratorManager) {
        Collection<MetaDataType> objectTypes = scriptGeneratorManager.getObjectTypes();
        boolean dropTable = objectTypes.contains(TABLE);
        if (!dropTable) {
            return;
        }
        Dialect dialect = scriptGeneratorManager.getTargetDialect();
        StringBuilder buffer = new StringBuilder("DROP TABLE");
        buffer.append(' ');
        boolean ifExistsBeforeTable;
        if (ifExistsBeforeTable = dialect.supportsIfExistsBeforeDropTable()) {
            buffer.append("IF EXISTS");
            buffer.append(' ');
        }
        buffer.append(scriptGeneratorManager.getName(table));
        String cascadeConstraints = dialect.getCascadeConstraints();
        if (cascadeConstraints != null) {
            buffer.append(' ');
            buffer.append(cascadeConstraints);
        }
        if (!ifExistsBeforeTable && dialect.supportsIfExistsAfterDropTable()) {
            buffer.append(' ');
            buffer.append("IF EXISTS");
        }
        scripts.add(buffer.toString());
    }
}
