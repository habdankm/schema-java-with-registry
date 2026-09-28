package com.stano.schema.diff.change;

import com.stano.schema.diff.SchemaChange;
import com.stano.schema.model.DatabaseType;

/**
 * Represents a view that must be dropped because it no longer exists in the new schema, or because
 * its SQL definition changed (in which case an {@code AddViewChange} with the new definition
 * follows this change). Carries the name of the dropped view and its own {@link DatabaseType} so a
 * migration generator for a different dialect can skip it instead of emitting drop SQL for
 * something it never created.
 */
public final class DropViewChange implements SchemaChange {
  private final String viewName;
  private final DatabaseType databaseType;

  /**
   * Creates a change describing a view to drop.
   *
   * @param viewName the name of the dropped view
   * @param databaseType the database type the dropped view applies to, or {@code null} if it
   *     applied to all database types
   */
  public DropViewChange(String viewName, DatabaseType databaseType) {
    this.viewName = viewName;
    this.databaseType = databaseType;
  }

  /** Returns the name of the dropped view. */
  public String getViewName() {
    return viewName;
  }

  /** Returns the database type the dropped view applies to, or {@code null} for all types. */
  public DatabaseType getDatabaseType() {
    return databaseType;
  }
}
