package com.stano.schema.reverseengineer.impl.common;

import com.stano.schema.model.Schema;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The engine-specific part of reverse engineering, deliberately as small as it can be.
 *
 * <p>The common reader walks a database with plain JDBC: tables, columns, primary keys, keys and
 * imported keys come out the same way on every engine. The steps that are genuinely engine-specific
 * are the default schema and constraints: PostgreSQL answers constraints with catalog objects of
 * its own (pg_constraint, pg_get_constraintdef, ::regclass) that no other engine has, H2 answers
 * from the information schema instead, and the schema an engine uses when the caller names none is
 * part of its identity (H2 PUBLIC, PostgreSQL public, SQL Server dbo).
 *
 * <p>It carries those two and nothing else: extracting differences that have not been hit yet would
 * be designing for a guess.
 *
 * <p>Implementations are chosen by {@link DialectFactory} from the product name the driver reports,
 * so nothing in the reader branches on which engine it is talking to.
 */
public interface ReverseEngineeringDialect {

  /**
   * The product name this dialect answers to, as DatabaseMetaData.getDatabaseProductName() reports
   * it. A method so it is checked against the driver rather than trusted.
   */
  String productName();

  /**
   * The schema this engine uses when the caller names none. It belongs here and not in a constant
   * on the reader, because it IS the engine: assuming one name for every engine made the reader
   * introspect nothing at all on the other one.
   */
  String defaultSchema();

  /**
   * Reads the constraints this engine holds for the named schema and adds them to the model, one
   * Constraint per constraint, on the table it belongs to.
   *
   * <p>Adding nothing is a legitimate implementation for an engine whose constraints cannot be
   * read: an empty list is honest, while an exception here would throw away a whole definition over
   * a step that carries extra information.
   *
   * @param schema the schema being built, already carrying its tables
   * @param connection the live connection, for the engine's own catalog queries
   * @param schemaName the schema to read, named by the caller rather than assumed
   */
  void populateConstraints(Schema schema, Connection connection, String schemaName)
      throws SQLException;
}
