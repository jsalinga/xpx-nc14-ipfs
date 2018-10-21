/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.proximax.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JunS
 */
public class pgWritter {

    private final String db_name;

    public pgWritter(String db_name) {
        this.db_name = db_name;
    }

    public boolean suWrite(String sqlCmd) {
        boolean retVal = false;
        Connection SQLCONNECTION = (new pgConnector(db_name)).suConnection();
        if (SQLCONNECTION != null) {
            try {
                Statement statement = SQLCONNECTION.createStatement();
                statement.executeUpdate(sqlCmd);
                statement.close();
                SQLCONNECTION.close();
                retVal = true;
            } catch (SQLException ex) {
                Logger.getLogger(pgWritter.class.getName()).log(Level.INFO, null, ex);
            }
        }
        return retVal;
    }

    public boolean suWrite(String sqlCmd, File binFile) {

        boolean retVal = false;
        // make temporary connection
        Connection SQLCONNECTION = (new pgConnector(db_name)).suConnection();
        if (SQLCONNECTION != null) {
            try {
                PreparedStatement pstatement = SQLCONNECTION.prepareStatement(sqlCmd);
                FileInputStream fis1 = new FileInputStream(binFile);
                pstatement.setBinaryStream(1, fis1, (int) binFile.length());
                pstatement.executeUpdate();
                pstatement.close();
                fis1.close();
                SQLCONNECTION.close();
                retVal = true;
            } catch (IOException ex) {
                System.out.println(sqlCmd.toUpperCase());
                Logger.getLogger(pgWritter.class.getName()).log(Level.INFO, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(pgWritter.class.getName()).log(Level.INFO, null, ex);
            }
        }
        return retVal;
    }

}
