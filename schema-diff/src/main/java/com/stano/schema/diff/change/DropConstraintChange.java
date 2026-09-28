package com.stano.schema.diff.change;

import com.stano.schema.diff.SchemaChange;
import com.stano.schema.model.DatabaseType;

/**
 * Represents a constraint that exists on a table in the old schema but not in the new one, and so
 * must be dropped. Carries the owning table's name, the dropped constraint's name, and the dropped
 * constraint's own {@link DatabaseType} so a migration generator for a different dialect can skip
 * it instead of emitting drop SQL for something it never created.
 */
public final class DropConstraintChange implements SchemaChange {
  private final String tableName;
  private final String constraintName;
  private final DatabaseType databaseType;

  /**
   * Creates a change describing a constraint to drop.
   *
   * @param tableName the name of the table the constraint is dropped from
   * @param constraintName the name of the dropped constraint
   * @param databaseType the database type the dropped constraint applies to, or {@code null} if it
   *     applied to all database types
   */
  public DropConstraintChange(String tableName, String constraintName, DatabaseType databaseType) {
    this.tableName = tableName;
    this.constraintName = constraintName;
    this.databaseType = databaseType;
  }

  /** Returns the name of the table the constraint is dropped from. */
  public String getTableName() {
    return tableName;
  }

  /** Returns the name of the dropped constraint. */
  public String getConstraintName() {
    return constraintName;
  }

  /** Returns the database type the dropped constraint applies to, or {@code null} for all types. */
  public DatabaseType getDatabaseType() {
    return databaseType;
  }
}
