package com.stano.schema.gensql.impl.sqlserver;

import com.stano.schema.gensql.impl.common.ColumnTypeMapper;
import com.stano.schema.model.BooleanMode;
import com.stano.schema.model.Column;
import com.stano.schema.model.EnumType;
import com.stano.schema.model.EnumValue;
import com.stano.schema.model.Schema;

public class SQLServerColumnTypeMapper extends ColumnTypeMapper {
  private final int targetSqlServerVersion;

  public SQLServerColumnTypeMapper(BooleanMode booleanMode, Schema schema) {
    this(booleanMode, schema, 0);
  }

  /**
   * Creates a mapper that gates version-specific column types (currently just the native {@code
   * json} type, introduced in SQL Server 2025) on a target SQL Server product year.
   *
   * @param booleanMode how boolean columns should be expressed in the generated SQL
   * @param schema the schema being generated, used to resolve enum value lengths, or {@code null}
   * @param targetSqlServerVersion the target SQL Server product year (e.g. 2022, 2025), or {@code
   *     0} to target the widest range of servers
   */
  public SQLServerColumnTypeMapper(
      BooleanMode booleanMode, Schema schema, int targetSqlServerVersion) {
    super(booleanMode, schema);
    this.targetSqlServerVersion = targetSqlServerVersion;
  }

  @Override
  protected String getSequenceSql() {
    return "integer identity(1,1)";
  }

  @Override
  protected String getLongSequenceSql() {
    return "bigint identity(1,1)";
  }

  @Override
  protected String getNativeBooleanSql() {
    return "bit";
  }

  @Override
  protected String getDateSql() {
    return "date";
  }

  @Override
  protected String getDateTimeSql() {
    return "datetime";
  }

  @Override
  protected String getTimestampSql() {
    return "datetime2";
  }

  @Override
  protected String getTimeSql() {
    return "time";
  }

  @Override
  protected String getTimestampTZSql() {
    return "datetimeoffset";
  }

  @Override
  protected String getCharSql(Column column) {
    return "char(" + column.getLength() + ")";
  }

  @Override
  protected String getVarcharSql(Column column) {
    return "nvarchar(" + (column.getLength() == -1 ? "max" : column.getLength()) + ")";
  }

  @Override
  protected String getTextSql(Column column) {
    return column != null && column.getLength() > 0
        ? "nvarchar(" + column.getLength() + ")"
        : "nvarchar(max)";
  }

  @Override
  protected String getCitextSql() {
    return "nvarchar(max)";
  }

  @Override
  protected String getCstextSql() {
    return "nvarchar(max)";
  }

  @Override
  protected String getBinarySql() {
    return "varbinary(max)";
  }

  @Override
  protected String getBooleanSql() {
    if (booleanMode == BooleanMode.YES_NO) return "nvarchar(3)";
    if (booleanMode == BooleanMode.YN) return "nchar(1)";
    return getNativeBooleanSql();
  }

  @Override
  protected String getEnumSql(Column column) {
    if (schema == null) {
      return "nvarchar(255)";
    }
    EnumType enumType = schema.getEnumType(column.getEnumType());
    int minLength = Integer.MAX_VALUE;
    int maxLength = 0;
    for (EnumValue enumValue : enumType.getValues()) {
      String code = enumValue.getCode();
      minLength = Math.min(minLength, code.length());
      maxLength = Math.max(maxLength, code.length());
    }
    if (minLength != maxLength) return "nvarchar(" + maxLength + ")";
    return "nchar(" + maxLength + ")";
  }

  @Override
  protected String getUUIDSql(Column column) {
    return "uniqueidentifier";
  }

  @Override
  protected String getArraySql(Column column) {
    throw new UnsupportedOperationException("SQL Server does not support arrays");
  }

  @Override
  protected String getJsonSql() {
    // SQL Server's native json type doesn't exist before SQL Server 2025; use it only when
    // explicitly targeting 2025+, otherwise fall back to nvarchar(max), which works on every
    // supported version.
    if (targetSqlServerVersion >= 2025) {
      return "json";
    }
    return super.getJsonSql();
  }
}
