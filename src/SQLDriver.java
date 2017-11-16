/**
 * Created by Incomplete on 11/12/17.
 */

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
                System.out.println( "Please choose your operation: " );
                printChoices();
                Scanner scanner = new Scanner( System.in );
                String choice = scanner.nextLine();
                doOperation(con, choice.trim().toLowerCase(), user);
            }
        }
        catch(ClassNotFoundException e)
        {
            //数据库驱动类异常处理
            System.out.println("Sorry,can`t find the Driver!");
            e.printStackTrace();
        }
        catch(SQLException e)
        {
            //数据库连接失败异常处理
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
                //studentName = scanner.nextLine();
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
                    System.out.println("Welcome, " + map.get(Constants.NAME) + "!");
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
                System.out.println("Login success!");
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
        System.out.println("Year: " + user.year);
        System.out.println("Quarter: " + user.quarter);
    }

    /**
     * Print all the choices
     */
    private static void printChoices() {
        for (String choice : Constants.CHOICES_SET) {
            System.out.println("* " + choice);
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
                       enrollOp(user);
                       break;
                   case "withdraw":
                       withdrawOp(user);
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
        System.out.println("Do you want to change password or address?");
        System.out.println("* Password(p)");
        System.out.println("* Address(a)");
        System.out.println("* No(n)");
        Scanner scanner = new Scanner( System.in );
        String infoToChange = scanner.nextLine().trim().toLowerCase();
        infoToChange = infoToChange.substring(0, 1).toUpperCase() + infoToChange.substring(1);
        if (!(infoToChange.equals("No") || infoToChange.equals("n"))) {
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
            System.out.println("Course ID \t Grades");
            while(resultSet.next()) {
                System.out.println(resultSet.getString("UoSCode") +
                    " \t " +
                    resultSet.getString("Grade"));
            }
            while(true) {
                System.out.println("Please choose your operation:");
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
        System.out.print("Please input the course ID you want to view(Caution: Case sensitive):");
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
        System.out.println("Course Id" +
                Constants.TABLE_SIGN_WITH_SPACES +
                "Course Name" +
                Constants.TABLE_SIGN_WITH_SPACES +
                "Year" +
                Constants.TABLE_SIGN_WITH_SPACES +
                "Quarter" +
                Constants.TABLE_SIGN_WITH_SPACES +
                "Students Enrolled" +
                Constants.TABLE_SIGN_WITH_SPACES +
                "Max Enroll" +
                Constants.TABLE_SIGN_WITH_SPACES +
                "Lecturer Name" +
                Constants.TABLE_SIGN_WITH_SPACES +
                "Grade");
        System.out.println(courseId +
                Constants.TABLE_SIGN_WITH_SPACES +
                courseName +
                Constants.TABLE_SIGN_WITH_SPACES +
                year +
                Constants.TABLE_SIGN_WITH_SPACES +
                semester +
                Constants.TABLE_SIGN_WITH_SPACES +
                enroll +
                Constants.TABLE_SIGN_WITH_SPACES +
                maxEnroll +
                Constants.TABLE_SIGN_WITH_SPACES +
                instructorName +
                Constants.TABLE_SIGN_WITH_SPACES +
                grade);
    }

    /**
     * The operations for enrolling
     *
     * @param user
     *              The instance of user
     */
    private static void enrollOp(User user) {

    }

    /**
     * The operations for withdrawing
     *
     * @param user
     *              The instance of user
     */
    private static void withdrawOp(User user) {

    }
}
