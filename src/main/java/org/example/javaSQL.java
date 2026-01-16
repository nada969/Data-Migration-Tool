package org.example;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface javaSQL {
    void type(String sql,int i, Object value) throws SQLException;
}
