/**
 * Created by Zhaoting on 11/13/17.
 */
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

public class User {
    private final String Name;
    private final String Id;
    private final Connection con;
    private String Address;
    private String password;
    String year;
    String quarter;

    /**
     * Constructor
     * @param nameAndId
     *              The map storing user's name and id
     * @param con
     *              The database connection
     */
    public User(Map<String, String> nameAndId, Connection con) {
        this.Name = nameAndId.get(Constants.NAME);
        this.Id = nameAndId.get(Constants.ID);
        this.con = con;
        completeInit();
    }

    /**
     * Print all the items(except time)
     */
    public void printInfo() {
        if (this.password == null) {
            completeInit();
        } else {
            System.out.println("Name: " + this.Name);
            System.out.println("ID: " + this.Id);
            System.out.println("Address: " + this.Address);
            System.out.println("Password: " + this.password);
        }
    }

    /**
     * Complete the initialization of remaining items
     */
    private void completeInit() {
        try {
            Statement statement = this.con.createStatement();
            String query = Constants.SELECT +
                    Constants.ALL +
                    Constants.FROM +
                    "student" +
                    Constants.WHERE +
                    "Name='" +
                    this.Name +
                    Constants.SINGLE_QUOTE;
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()) {
                this.password = resultSet.getString("Password");
                this.Address = resultSet.getString("Address");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh time stored in instance to current time
     */
    public void refreshTime() {
        LocalDate now = LocalDate.now();
        Month curMonth = now.getMonth();
        this.year = String.valueOf(now.getYear());
        if (curMonth.getValue() >= Month.SEPTEMBER.getValue() && curMonth.getValue() <= Month.NOVEMBER.getValue()) {
            this.quarter = "Q1";
        } else if (curMonth.getValue() == Month.DECEMBER.getValue() ||
                (curMonth.getValue() >= Month.JANUARY.getValue() && curMonth.getValue() <= Month.FEBRUARY.getValue())) {
            this.quarter = "Q2";
        } else if (curMonth.getValue() >= Month.MARCH.getValue() && curMonth.getValue() <= Month.MAY.getValue()) {
            this.quarter = "Q3";
        } else {
            this.quarter = "Q4";
        }
    }

    /**
     * Allow change user's password or address
     *
     * @param newInfo
     *              The new info
     * @param infoToChange
     *              The item to be changed
     *
     */
    public void changeInfo(String newInfo, String infoToChange) {

    }

    /**
     * Factory method for producing Statement objects
     *
     * @return
     *              The Statement instance
     */
    public Statement createStatement() throws SQLException {
        return this.con.createStatement();
    }

    /**
     * Getter method for name
     *
     * @return
     *              User's name
     */
    public String getName() {
        return this.Name;
    }

    /**
     * Getter method for ID
     *
     * @return
     *              User's ID
     */
    public String getId() {
        return this.Id;
    }
}
