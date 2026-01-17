package org.example;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TypeHandler {
    public static void setValue(PreparedStatement pstmt, int index, Object value, String typeName) throws SQLException {
        if (value == null) {
            pstmt.setNull(index, getSqlType(typeName));
            return;
        }


        switch (typeName) {
            case "String":
                pstmt.setString(index, value.toString());

                break;
            case "Integer":
            case "int":
                pstmt.setInt(index, ((Number) value).intValue());
                break;
            case "Long":
            case "long":
                pstmt.setLong(index, ((Number) value).longValue());
                break;
            case "Double":
            case "double":
                pstmt.setDouble(index, ((Number) value).doubleValue());
                break;
            case "Float":
            case "float":
                pstmt.setFloat(index, ((Number) value).floatValue());
                break;
            case "Boolean":
            case "boolean":
                pstmt.setBoolean(index, (Boolean) value);
                break;
            case "Date":
                if (value instanceof java.util.Date) {
                    pstmt.setTimestamp(index, new Timestamp(((java.util.Date) value).getTime()));
                } else if (value instanceof Long) {
                    pstmt.setTimestamp(index, new Timestamp((Long) value));
                }
                break;
            case "Timestamp":
                pstmt.setTimestamp(index, (Timestamp) value);
                break;
            case "BigDecimal":
                pstmt.setBigDecimal(index, new BigDecimal(value.toString()));
                break;
            case "byte[]":
            case "Binary":
                pstmt.setBytes(index, (byte[]) value);
                break;
            default:
                // Fallback: use setObject for any unknown type
                pstmt.setObject(index, value);
        }
    }

    private static int getSqlType(String typeName) {
        switch (typeName) {
            case "String": return java.sql.Types.VARCHAR;
            case "Integer": case "int": return java.sql.Types.INTEGER;
            case "Long": case "long": return java.sql.Types.BIGINT;
            case "Double": case "double": return java.sql.Types.DOUBLE;
            case "Float": case "float": return java.sql.Types.FLOAT;
            case "Boolean": case "boolean": return java.sql.Types.BOOLEAN;
            case "Date": case "Timestamp": return java.sql.Types.TIMESTAMP;
            case "BigDecimal": return java.sql.Types.DECIMAL;
            case "byte[]": case "Binary": return java.sql.Types.BINARY;
            default: return java.sql.Types.OTHER;
        }
//        switch (typeName) {
//        } else if (originalValue instanceof Integer) {
//            psmt.setInt(i + 1, (Integer) originalValue);
//        } else if (originalValue instanceof Long) {
//            psmt.setLong(i + 1, (Long) originalValue);
//        } else if (originalValue instanceof Double) {
//            psmt.setDouble(i + 1, (Double) originalValue);
//        } else if (originalValue instanceof Boolean) {
//            psmt.setBoolean(i + 1, (Boolean) originalValue);
//        } else if (originalValue instanceof Date) {
//            psmt.setTimestamp(i + 1, new java.sql.Timestamp(((Date) originalValue).getTime()));
//        } else {
//            psmt.setString(i + 1, originalValue.toString());
//        }
    }


}
