package com.proximax.nem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class pgConnector {

    /**
     * AMS temporary directory and property file
     */
    private final String SERVER_URL = "jdbc:postgresql://localhost:5432/";
    private final String[] params;

    pgConnector(String[] params) {
        this.params = params;
    }

    public ResultSet suQuery(String sqlCmd, Connection sqlConnection) {
        ResultSet res = null;
        if (sqlConnection != null) {
            try {
                Statement statement = sqlConnection.createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                statement.setFetchSize(1024);
                res = statement.executeQuery(sqlCmd);
            } catch (SQLException ex) {
                System.out.println(sqlCmd.toUpperCase());
                Logger.getLogger(pgConnector.class.getName()).log(Level.INFO, null, ex);
            }

        }
        return res;
    }

    public Connection suConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(SERVER_URL + params[1], "postgres", "");
            return conn;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(pgConnector.class.getName()).log(Level.INFO, null, ex);
        }
        return null;
    }

    public void closeResult(ResultSet sqlResult) {
        try {
            sqlResult.close();
        } catch (SQLException ex) {
            Logger.getLogger(pgConnector.class.getName()).log(Level.INFO, null, ex);
        }
    }

    public ResultSet suQuery(String sqlCmd) {
        ResultSet sqlResult = null;
        Connection sqlConnection = suConnection();
        if (sqlConnection != null) {
            try {
                Statement sqlStatement = sqlConnection.createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                sqlStatement.setFetchSize(1024);
                sqlResult = sqlStatement.executeQuery(sqlCmd);
            } catch (SQLException ex) {
                System.out.println(sqlCmd);
                Logger.getLogger(pgConnector.class.getName()).log(Level.INFO, null, ex);
            }
            try {
                sqlConnection.close();
            } catch (SQLException ex) {
                Logger.getLogger(pgConnector.class.getName()).log(Level.INFO, null, ex);
            }
        }
        return sqlResult;
    }

}
