package org.example;

import java.util.Map;

public class CreateCol {
//    create collumns with type&newvalue
//    public static void createCol(Object[] mappings){
//        if (mappings == null || mappings.length == 0) {
//            System.err.println("No mappings found!");
//        }
//        // Add columns from mappings
//        for (Object mappingObj : mappings) {
//            @SuppressWarnings("unchecked")
//            Map<String, String> mapping = (Map<String, String>) mappingObj;
//
//            String psqlCol = mapping.get("psqlCol").trim();
//            String type = mapping.get("type");
//
//            sql.append(", ");
//            sql.append(psqlCol).append(" ");
//            sql.append(getSQLType(type));
//        }
//    }
//   Adds a single column to an existing table
//    public static boolean addColumn(Connection conn, String tableName,
//                                    String columnName, String type) {
//        String sql = "ALTER TABLE " + tableName +
//                " ADD COLUMN IF NOT EXISTS " + columnName +
//                " " + getSQLType(type);
//
//        try (Statement stmt = conn.createStatement()) {
//            stmt.executeUpdate(sql);
//            System.out.println("✓ Column added: " + columnName);
//            return true;
//        } catch (SQLException e) {
//            System.err.println("✗ Failed to add column: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        }
//    }
}
