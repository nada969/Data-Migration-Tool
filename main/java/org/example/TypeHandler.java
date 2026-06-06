package org.example;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TypeHandler {
    public static void setValue(PreparedStatement pstmt, int index, Object value, String typeName) throws SQLException {
//        if (value == null) {
//            pstmt.setNull(index, toSqlType(typeName));
//            return;
//        }


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




}
