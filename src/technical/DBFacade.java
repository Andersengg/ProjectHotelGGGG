package technical;

import domain.Booking;
import domain.User;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import domain.LoginController;

/**
 * Created by suman on 4/18/2017.
 */
public class DBFacade {
    public List<Booking> customerList;

    private static String userName = "sa";
    private static String password = "lagkage123";

    private static String port = "1433";
    private static String databaseName = "DB_Hotel";

    Connection connection = null;

    public DBFacade() {
        getConnection();
    }

    @Override
    public void finalize() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // TODO LOG
            }
        }
    }

    public Connection getConnection() {

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
            connection = DriverManager.getConnection("jdbc:sqlserver://localhost:" + port + ";databaseName=" +
                    databaseName, userName, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    /**
     * closes an opened connection
     */
    public void closeConnect() {

        try {
            connection.close();
            System.out.println("Connection closed");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("xD");
        }

    }

    public List<Booking> allBookings() {
        customerList = new ArrayList<>();
        PreparedStatement pst = null;
        ResultSet rs;
        String sql = null;
        try {
            connection.setAutoCommit(false);
            sql = "EXECUTE AllBookings ?, ?, ?";
            pst = connection.prepareStatement(sql);
            pst.setString(1, "");
            pst.setString(2, "");
            pst.setString(3, "");
            rs = pst.executeQuery();
            while (rs.next()) {
                Booking b = new Booking(
                        rs.getInt("fld_bookingID"),
                        rs.getInt("fld_Roomnumber"),
                        rs.getDouble("fld_Price"),
                        rs.getString("fld_Email"),
                        rs.getString("fld_RoomSize"),
                        rs.getString("fld_FirstName"),
                        rs.getString("fld_LastName"),
                        rs.getString("fld_Description"),
                        rs.getString("fld_StartDate"),
                        rs.getString("fld_EndDate"));
                customerList.add(b);
            }
            //connection.commit();
            // TODO rs.close
        } catch (SQLException e) {
            tryRollback(connection);
            throw new RuntimeException("Exception executing: '" + sql + "'", e);
        } finally {
            tryClose(pst, sql);
            }
            return customerList;
        }

    private void tryRollback(Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not roll back", e);
        }
    }

    public void addBooking(int bookingID, String firstName, String lastName, int roomNumber, String email,
                           String roomSize, double price, String description, String startDate,
                           String endDate) {
        String sql = null;
        PreparedStatement pst = null;
        try {
            sql = "EXEC addBooking ?,?,?,?,?,?,?,?,?,?";
            pst = connection.prepareStatement(sql);
            pst.setInt(1, bookingID);
            pst.setString(2, firstName);
            pst.setString(3, lastName);
            pst.setInt(5, roomNumber);
            pst.setString(4, email);
            pst.setString(6, roomSize);
            pst.setDouble(7, price);
            pst.setString(8, description);
            pst.setDate(9, getSQLDate(startDate));
            pst.setDate(10, getSQLDate(endDate));
            pst.execute();
            connection.commit();
        } catch (SQLException e) {
            tryRollback(connection);
            throw new RuntimeException("Exception executing: '" + sql + "'", e);
        } catch (ParseException e) {
            tryRollback(connection);
            throw new RuntimeException("Exception executing: '" + sql + "'", e);
        } finally {
            tryClose(pst, sql);
        }
    }

    private void tryClose(PreparedStatement pst, String sql) {
        try {
            if (pst != null) {
                pst.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not close prepared statement: '" + sql + "'", e);
        }
    }

    public User findUserByUserName(String userName) {
        User result = null;
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("SELECT * FROM tbl_users " +
                    "WHERE fld_email = (?)");
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {   // only picking the first row
                int userId = rs.getInt(1);
                String name = rs.getString(2);
                String password = rs.getString(3);
                String role = rs.getString(4);
                result = new User(userId, name, password, role);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public void createNewArrangementAndRoomsOrder(int arrangementOrderID, String arrangementName, String arrangementType,
                                                  String description, int customerId, double price, int roomID, int roomNumber,
                                                  java.sql.Date startTime, java.sql.Date endTime) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("INSERT INTO tbl_ArrangementAndRoomsOrder VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, arrangementOrderID);
            ps.setString(2, arrangementName);
            ps.setString(3, arrangementType);
            ps.setString(4, description);
            ps.setInt(5, customerId);
            ps.setDouble(6, price);
            ps.setInt(7, roomID);
            ps.setInt(8, roomNumber);
            ps.setDate(9, startTime);
            ps.setDate(10, endTime);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public void deleteArrangement(int arrangementID) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("DELETE FROM tbl_ArrangementAndRoomsOrder WHERE fld_arrangementID = ?");
            ps.setInt(1, arrangementID);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public void updateArrangementAndRoomsOrder(int arrangementOrderID, String arrangementName, String arrangementType,
                                  String description, int customerId, double price, int roomID, int roomNumber,
                                  java.sql.Date startTime, java.sql.Date endTime) {
        // TODO update everything every time
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("UPDATE tbl_ArrangementAndRoomsOrder WHERE fld_arrangementOrderID = ?"
                                            + "SET fld_arrangementName = ?"
                                            + "SET fld_arrangementType = ?"
                                            + "SET fld_description = ?"
                                            + "SET fld_customerID = ?"
                                            + "SET fld_price = ?"
                                            + "SET fld_roomID = ?"
                                            + "SET fld_roomNumber = ?"
                                            + "SET fld_startDate = ?"
                                            + "SET fld_endDate= ?");
            ps.setInt(1, arrangementOrderID);
            ps.setString(2,arrangementName);
            ps.setString(3,arrangementType);
            ps.setString(4,description);
            ps.setInt(5,customerId);
            ps.setDouble(6,price);
            ps.setInt(7,roomID);
            ps.setInt(8,roomNumber);
            ps.setDate(9,startTime);
            ps.setDate(10,endTime);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public void createNewCateringOrder(int id, String description, int foodID, int customerID, int amount,
                                  double price, java.sql.Date timeOfArrival) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("INSERT INTO tbl_CateringOrder VALUES(?, ?, ?, ?, ?, ?, ?");
            ps.setInt(1, id);
            ps.setString(2, description);
            ps.setInt(3, foodID);
            ps.setInt(4,customerID);
            ps.setInt(5, amount);
            ps.setDouble(6, price);
            ps.setDate(7, timeOfArrival);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public void deleteCateringOrder(int id) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("DELETE FROM tbl_CateringOrder WHERE fld_cateringID = ?");
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public void updateCateringOrder(int id, String description, int foodID, int customerID, int amount,
                                    double price, java.sql.Date timeOfArrival) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("UPDATE tbl_CateringOrder WHERE fld_cateringID = ?"
                    + "SET fld_description = ?"
                    + "SET fld_foodID = ?"
                    + "SET fld_customerID = ?"
                    + "SET fld_ammount = ?"
                    + "SET fld_price = ?"
                    + "SET fld_timeOfArrival = ?");
            ps.setInt(1, id);
            ps.setString(2,description);
            ps.setInt(3,foodID);
            ps.setInt(4,customerID);
            ps.setInt(5,amount);
            ps.setDouble(6,price);
            ps.setDate(7,timeOfArrival);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public void deleteFromTable(String tableName, String currentID, int ID) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("DELETE FROM ? WHERE ? = ?");
            ps.setString(1, tableName);
            ps.setString(2, currentID);
            ps.setInt(3, ID);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public void createNewUser(int userID, String userName, String password, String role) {
        LoginController controller = new LoginController();
        PreparedStatement ps;
        String hashPassword = controller.createSHA(userName, password);
        try {
            ps = connection.prepareStatement("INSERT INTO tbl_Users VALUES(?, ?, ?, ?)");
            ps.setInt(1, userID);
            ps.setString(2, userName);
            ps.setString(3, hashPassword);
            ps.setString(4, role);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public void deleteUser(int userID) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("DELETE FROM tbl_User WHERE fld_userID = ?");
            ps.setInt(1, userID);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }

    }

    public void updateUser (int ID, String userName, String password, String role){
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("UPDATE tbl_user WHERE fld_userID = ? "
                    + "SET fld_userName = ?"
                    + "SET fld_password = ?"
                    + "SET fld_role = ?");
            ps.setInt(1, ID);
            ps.setString(2,userName);
            ps.setString(3,password);
            ps.setString(4,role);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }


    }

    public void createRoomServiceOrder(int orderId, String type, int customerId, double price, java.sql.Date timeOfArrival){

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("INSERT INTO tbl_RoomServiceOrder VALUES(?, ?, ?, ?, ?)");
            ps.setInt(1, orderId);
            ps.setString(2, type);
            ps.setInt(3, customerId);
            ps.setDouble(4, price);
            ps.setDate(5,timeOfArrival);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }

    }

    public void updateRoomServiceOrder(int orderId, String type, int customerId, double price, java.sql.Date timeOfArrival){

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("UPDATE tbl_RoomServiceOrder WHERE fld_roomServieOrderId = ?"
                    + "SET fld_roomServiceType = ?"
                    + "SET fld_customerID = ?"
                    + "SET fld_price = ?"
                    + "SET fld_timeOfArrival = ?");
            ps.setInt(1, orderId);
            ps.setString(2, type);
            ps.setInt(3, customerId);
            ps.setDouble(4, price);
            ps.setDate(5,timeOfArrival);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public void deleteRoomServiceOrder (int RoomServiceOrderId){
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("DELETE FROM tbl_RoomServiceOrder WHERE fld_roomServiceOrderId = ?");
            ps.setInt(1, RoomServiceOrderId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public static java.sql.Date getSQLDate(String date) throws java.text.ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        java.util.Date parsed = dateFormat.parse("" + date);
        java.sql.Date sqlDate = new java.sql.Date(parsed.getTime());
        return sqlDate;
    }
}

