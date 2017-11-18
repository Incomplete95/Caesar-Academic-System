/**
 * Created by Incomplete on 11/12/17.
 */

import com.sun.org.apache.regexp.internal.RE;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SQLDriver {

    public static void main(String [] args) {
        Connection con;
        try
        {
            Class.forName(Constants.DRIVER);
            con = DriverManager.getConnection(Constants.URL,Constants.USER,Constants.PASSWORD);
            if(!con.isClosed())
                System.out.println("Succeeded connecting to the Database!");
            User user = new User(tryLogin(con), con);
            showQuarterAndYear(user);
            while(true) {
                System.out.println( "\nPlease choose your operation: " );
                printChoices();
                Scanner scanner = new Scanner( System.in );
                String choice = scanner.nextLine().toLowerCase();
                for (String item : Constants.CHOICES_SET) {
                    if (item.startsWith(choice)) {
                        choice = item;
                    }
                }
                doOperation(con, choice.trim().toLowerCase(), user);
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            // TODO: handle exception
            e.printStackTrace();
        }
        finally
        {
            System.out.println("Exit System Succeed!");
        }
    }

    /**
     * Try to login into the system
     *
     * @param con
     *              The database connection
     * @return
     *              The map storing user's name and id
     */
    private static Map<String, String> tryLogin(Connection con) {
        Statement statement;
        ResultSet resTrySearchLoginName;
        Map<String, String> map = new HashMap<>();
        String correctPassword = null;
        try {
            statement = con.createStatement();
            while (map.get(Constants.NAME) == null) {
                Scanner scanner = new Scanner( System.in );
                System.out.print( "Please input your name: " );
                String trySearchLoginName = Constants.SELECT +
                        Constants.ALL +
                        Constants.FROM +
                        "student" +
                        Constants.WHERE +
                        "Name='" +
                        scanner.nextLine() +
                        Constants.SINGLE_QUOTE;
                resTrySearchLoginName = statement.executeQuery(trySearchLoginName);

                while(resTrySearchLoginName.next()) {
                    map.put(Constants.NAME, resTrySearchLoginName.getString(Constants.NAME));
                    map.put(Constants.ID, resTrySearchLoginName.getString(Constants.ID));
                    correctPassword = resTrySearchLoginName.getString("Password");
                }
                if (map.get(Constants.NAME) == null) {
                    System.out.println("Student name doesn't exist in system.");
                    System.out.println("Please check the spelling of both first name and last name.");
                    System.out.println("If spelling is correct, please contact system administrator immediately.");
                }
                resTrySearchLoginName.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int failCount = 0, totalChances = 5;
        while(failCount < totalChances) {
            Scanner scanner = new Scanner( System.in );
            System.out.print("Please input your password: ");
            String password = scanner.nextLine();
            if (password.equals(correctPassword)) {
                System.out.println("\nLogin success!");
                System.out.println("Welcome, " + map.get(Constants.NAME) + "!");
                return map;
            } else {
                failCount ++;
                System.out.println("Incorrect password, you have " +
                        (totalChances - failCount) +
                        " more times to try login");
            }
        }
        System.exit(0);
        return null;
    }

    /**
     * print current year and quarter
     *
     * @param user
     *              The instance of user
     */
    private static void showQuarterAndYear(User user) {
        user.refreshTime();
        System.out.println("\nYear: " + user.year);
        System.out.println("Quarter: " + user.quarter);
    }

    /**
     * Print all the choices
     */
    private static void printChoices() {
        for (String choice : Constants.CHOICES_SET) {
            System.out.println("* " + choice + "(" + choice.substring(0, 1) +")");
        }
    }

    /**
     * Execute the core operations
     *
     * @param con
     *              The database connection
     * @param choice
     *              User's choice
     * @param user
     *              The instance of user
     */
    private static void doOperation(Connection con, String choice, User user) {
        if (Constants.CHOICES_SET.contains(choice)) {
            try {
                switch (choice) {
                    case "personal details":
                        printPersonalInfoOp(user);
                        break;
                    case "transcript":
                        printTransciptsOp(user);
                        break;
                    case "enroll":
                        enrollOp(user, con);
                        break;
                    case "withdraw":
                        withdrawOp(user, con);
                        break;
                    default:
                        con.close();
                        System.out.println("Exit System Succeed!");
                        System.exit(0);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Cannot recognize your choice, please re-enter.");
        }
    }

    /**
     * The operations for printing personal information
     *
     * @param user
     *              The instance of user
     */
    private static void printPersonalInfoOp(User user) {
        user.printInfo();
        System.out.println("\nDo you want to change password or address?");
        System.out.println("* Password(p)");
        System.out.println("* Address(a)");
        System.out.println("* No(n)");
        Scanner scanner = new Scanner( System.in );
        String infoToChange = scanner.nextLine().trim().toLowerCase();
        infoToChange = infoToChange.substring(0, 1).toUpperCase() + infoToChange.substring(1);
        if (!(infoToChange.equals("No") || infoToChange.equals("N"))) {
            if (infoToChange.startsWith("P")) {
                infoToChange = "Password";
            } else {
                infoToChange = "Address";
            }
            scanner = new Scanner( System.in );
            System.out.print("Please input the new " + infoToChange + " you want to change:");
            String newInfo = scanner.nextLine();
            user.changeInfo(newInfo, infoToChange);
        }
    }

    /**
     * The operations for printing transcripts
     *
     * @param user
     *              The instance of user
     */
    private static void printTransciptsOp(User user) {
        try {
            Statement statement = user.createStatement();
            String Id = user.getId();
            String genTranscriptsQuery = Constants.SELECT +
                    "UoSCode, Grade" +
                    Constants.FROM +
                    "transcript" +
                    Constants.WHERE +
                    "StudId" +
                    Constants.EQUAL +
                    Constants.SINGLE_QUOTE +
                    user.getId() +
                    Constants.SINGLE_QUOTE;
            ResultSet resultSet = statement.executeQuery(genTranscriptsQuery);
            System.out.println("\nFollowing are your transcripts:");
            String format = "%-20s %s\n";
            System.out.format(format, "Course ID", "Grades");
            //System.out.println("\nCourse ID \t Grades");
            while(resultSet.next()) {
                System.out.format(format, resultSet.getString("UoSCode"), resultSet.getString("Grade"));
            }
            while(true) {
                System.out.println("\nPlease choose your operation:");
                System.out.println("* View details of course(v)");
                System.out.println("* Back to previous page(b)");
                Scanner scanner = new Scanner( System.in );
                String choice = scanner.nextLine().trim().toLowerCase();
                if (choice.equals("view") || choice.equals("v")) {
                    viewCourseDetails(user);
                } else {
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print the details of one course
     *
     * @param user
     *              The instance of user
     */
    private static void viewCourseDetails(User user) {
        System.out.print("\nPlease input the course ID you want to view(Caution: Case sensitive):");
        Scanner scanner = new Scanner( System.in );
        String courseId = scanner.nextLine().trim();
        String checkValidQuery = Constants.SELECT +
                Constants.ALL +
                Constants.FROM +
                "transcript" +
                Constants.WHERE +
                "StudId" +
                Constants.EQUAL +
                Constants.SINGLE_QUOTE +
                user.getId() +
                Constants.SINGLE_QUOTE +
                Constants.AND +
                "UoSCode" +
                Constants.EQUAL +
                Constants.SINGLE_QUOTE +
                courseId +
                Constants.SINGLE_QUOTE;
        Statement statement = null;
        while (true) {
            try {
                statement = user.createStatement();
                ResultSet resultSet = statement.executeQuery(checkValidQuery);
                if (!resultSet.next()) {
                    System.out.println("Cannot find records for your course ID, \n" +
                            "please check if the ID is valid and type the correct one again");
                } else {
                    break;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        String transcriptInfoQuery = Constants.SELECT +
                "Semester, Year, Grade" +
                Constants.FROM +
                "transcript" +
                Constants.WHERE +
                "studId" +
                Constants.EQUAL +
                Constants.SINGLE_QUOTE +
                user.getId() +
                Constants.SINGLE_QUOTE +
                Constants.AND +
                "UoSCode" +
                Constants.EQUAL +
                Constants.SINGLE_QUOTE +
                courseId +
                Constants.SINGLE_QUOTE;
        String semester = null, year = null, grade = null;
        try {
            statement = user.createStatement();
            ResultSet resultSet = statement.executeQuery(transcriptInfoQuery);
            while(resultSet.next()) {
                semester = resultSet.getString("Semester");
                year = resultSet.getString("Year");
                grade = resultSet.getString("Grade");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String courseInfoQuery = Constants.SELECT +
                "Enrollment, MaxEnrollment, InstructorId" +
                Constants.FROM +
                "uosoffering" +
                Constants.WHERE +
                "UoSCode" +
                Constants.EQUAL +
                Constants.SINGLE_QUOTE +
                courseId +
                Constants.SINGLE_QUOTE +
                Constants.AND +
                "Semester" +
                Constants.EQUAL +
                Constants.SINGLE_QUOTE +
                semester +
                Constants.SINGLE_QUOTE +
                Constants.AND +
                "Year" +
                Constants.EQUAL +
                Constants.SINGLE_QUOTE +
                year +
                Constants.SINGLE_QUOTE;
        String maxEnroll = null, enroll = null, instructorId = null;
        try {
            statement = user.createStatement();
            ResultSet resultSet = statement.executeQuery(courseInfoQuery);
            while(resultSet.next()) {
                maxEnroll = resultSet.getString("MaxEnrollment");
                enroll = resultSet.getString("Enrollment");
                instructorId = resultSet.getString("InstructorId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String courseNameQuery = Constants.SELECT +
                "UoSName" +
                Constants.FROM +
                "unitofstudy" +
                Constants.WHERE +
                "UoSCode" +
                Constants.EQUAL +
                Constants.SINGLE_QUOTE +
                courseId +
                Constants.SINGLE_QUOTE;
        String courseName = null;
        try {
            statement = user.createStatement();
            ResultSet resultSet = statement.executeQuery(courseNameQuery);
            while(resultSet.next()) {
                courseName = resultSet.getString("UoSName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String instructorNameQuery = Constants.SELECT +
                "Name" +
                Constants.FROM +
                "faculty" +
                Constants.WHERE +
                "Id" +
                Constants.EQUAL +
                Constants.SINGLE_QUOTE +
                instructorId +
                Constants.SINGLE_QUOTE;
        String instructorName = null;
        try {
            statement = user.createStatement();
            ResultSet resultSet = statement.executeQuery(instructorNameQuery);
            while(resultSet.next()) {
                instructorName = resultSet.getString("Name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("The course information:");
        String format = "%-10s %-45s %-10s %-10s %-20s %-20s %-20s %s\n";
        System.out.format(format,
                "Course Id",
                "Course Name",
                "Year",
                "Quarter",
                "Students Enrolled",
                "Max Enroll",
                "Lecturer Name",
                "Grade");
        System.out.format(format,
                courseId,
                courseName,
                year,
                semester,
                enroll,
                maxEnroll,
                instructorName,
                grade);
    }

    /**
     * The operations for enrolling
     *
     * @param user
     *              The instance of user
     */
    private static void enrollOp(User user, Connection con) throws SQLException {
        user.refreshTime();
        String curQuarter = user.quarter, curYear = user.year;
        String[] nextTime = nextTime(curQuarter, curYear);
        String nextQuarter = nextTime[0], nextYear = nextTime[1];
        CallableStatement cstmt = null;
        String course = null, year = null, quarter = null;
        while(true) {
            String listCourseQuery = "{call listCourses (?, ?, ?, ?, ?)}";
            cstmt = con.prepareCall (listCourseQuery);
            cstmt.setString(1, user.getId());
            cstmt.setString(2, curYear);
            cstmt.setString(3, curQuarter);
            cstmt.setString(4, nextYear);
            cstmt.setString(5, nextQuarter);
            ResultSet listSet = cstmt.executeQuery();
            System.out.println("\nFollowing are the courses you can enroll:");
            String format = "%-12s %-10s %s\n";
            System.out.format(format, "Course ID", "Quarter", "Year");
            while (listSet.next()) {
                System.out.format(format, listSet.getString("UoSCode"), listSet.getString("Semester"), listSet.getString("Year"));
            }
            Scanner scanner = new Scanner(System.in);
            System.out.print("\nPlease input the course ID you want to enroll: ");
            course = scanner.nextLine();
            System.out.print("Please input the year of the course you want to enroll: ");
            year = scanner.nextLine();
            System.out.print("Please input the quarter of the course you want to enroll: ");
            quarter = scanner.nextLine();
            try {
                String validQuery = "{call isvalid (?, ?, ?, ?, ?, ?)}";
                cstmt = con.prepareCall(validQuery);
                cstmt.setString(1, user.getId());
                cstmt.setString(2, course);
                cstmt.setString(3, curYear);
                cstmt.setString(4, curQuarter);
                cstmt.setString(5, nextYear);
                cstmt.setString(6, nextQuarter);
                ResultSet resultSet = cstmt.executeQuery();
                if (resultSet.next()) {
                    String enrollClass = "{call enrollclass (?, ?, ?, ?)}";
                    cstmt = con.prepareCall (enrollClass);
                    cstmt.setString(1, user.getId());
                    cstmt.setString(2, course);
                    cstmt.setString(3, year);
                    cstmt.setString(4, quarter);
                    cstmt.executeQuery();
                    System.out.println("Enroll course successfully!");
                } else {
                    String checkPreQuery = "{call needprereq (?, ?, ?, ?, ?, ?)}";
                    CallableStatement checkPre = con.prepareCall(checkPreQuery);
                    checkPre.setString(1, user.getId());
                    checkPre.setString(2, course);
                    checkPre.setString(3, curYear);
                    checkPre.setString(4, curQuarter);
                    checkPre.setString(5, nextYear);
                    checkPre.setString(6, nextQuarter);
                    ResultSet resultSet1 = checkPre.executeQuery();
                    if (resultSet1.next()) {
                        String listPreqQuery = "{call listprereq (?)}";
                        CallableStatement listPreq = con.prepareCall(listPreqQuery);
                        listPreq.setString(1, course);
                        ResultSet resultSet2 = listPreq.executeQuery();
                        System.out.println("The prerequisite classes you need to finish:");
                        System.out.println("Course ID");
                        while(resultSet2.next()) {
                            System.out.println(resultSet2.getString("PrereqUoSCode"));
                        }
                    } else {
                        System.out.println("The course you select is not valid, please reselect a new course.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            finally {
                cstmt.close();
            }
            System.out.println("\nDo you want to continue withdrawing courses? (Y/N)");
            if (scanner.nextLine().trim().toLowerCase().equals("n")) {
                break;
            }
        }
    }

    /**
     * Get the next quarter and year
     *
     * @param curQuarter
     *              The current quarter
     * @param curYear
     *              The current year
     * @return
     *              The String storing the next quarter and year
     */
    private static String[] nextTime(String curQuarter, String curYear) {
        String[] res = new String[2];
        if (Integer.parseInt(curQuarter.substring(1)) == 4) {
            res[0] = "Q" + 1;
            res[1] = Integer.toString(Integer.parseInt(curYear));
        } else if (Integer.parseInt(curQuarter.substring(1)) == 1) {
            res[0] = "Q" + 2;
            res[1] = Integer.toString(Integer.parseInt(curYear) + 1);
        } else {
            res[0] = "Q" + (Integer.parseInt(curQuarter.substring(1)) + 1);
            res[1] = curYear;
        }
        return res;
    }

    /**
     * The operations for withdrawing
     *
     * @param user
     *              The instance of user
     */
    private static void withdrawOp(User user, Connection con) {
        user.refreshTime();
        String curQuarter = user.quarter, curYear = user.year;
        String[] nextTime = nextTime(curQuarter, curYear);
        String nextQuarter = nextTime[0], nextYear = nextTime[1];
        CallableStatement cstmt = null;
        try {
            String course = null, year = null, quarter = null;
            while (true) {
                createWarning(con);
                String listWithdrawCourses = "{call listWithdrawCourses (?, ?, ?, ?, ?)}";
                cstmt = con.prepareCall(listWithdrawCourses);
                cstmt.setString(1, user.getId());
                cstmt.setString(2, curYear);
                cstmt.setString(3, curQuarter);
                cstmt.setString(4, nextYear);
                cstmt.setString(5, nextQuarter);
                ResultSet resultSet = cstmt.executeQuery();
                System.out.println("\nFollowing are the courses you can withdraw:");
                String format = "%-12s %-10s %s\n";
                System.out.format(format, "Course ID", "Quarter", "Year");
                while (resultSet.next()) {
                    System.out.format(format, resultSet.getString("UoSCode"), resultSet.getString("Semester"), resultSet.getString("Year"));
                }
                Scanner scanner = new Scanner(System.in);
                System.out.print("\nPlease input the course ID you want to withdraw: ");
                course = scanner.nextLine();
                System.out.print("Please input the year of the course you want to withdraw: ");
                year = scanner.nextLine();
                System.out.print("Please input the quarter of the course you want to withdraw: ");
                quarter = scanner.nextLine();
                try {
                    String withdrawCourse = "{call withdrawclass (?, ?, ?, ?)}";
                    cstmt = con.prepareCall(withdrawCourse);
                    cstmt.setString(1, user.getId());
                    cstmt.setString(2, course);
                    cstmt.setString(3, year);
                    cstmt.setString(4, quarter);
                    cstmt.executeQuery();
                    System.out.println("\nWithdraw course successfully!\n");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String selectWarning = "SELECT * FROM Warning";
                try {
                    Statement statement = user.createStatement();
                    ResultSet resultSet1 = statement.executeQuery(selectWarning);
                    while (resultSet1.next()) {
                        System.out.println("\nWarning: the enrolled students in course " +
                                resultSet1.getString("courseId") +
                                " is below the 50% of max enrollment\n");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                System.out.println("\nDo you want to continue withdrawing courses? (Y/N)");
                if (scanner.nextLine().trim().toLowerCase().equals("n")) {
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Execute CREATE TABLE Warning
     *
     * @param con
     *              The database connection
     */
    private static void createWarning(Connection con) {
        CallableStatement cstmt = null;
        try {
            String createSQL = "{call warning ()}";
            cstmt = con.prepareCall(createSQL);
            cstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
