import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Incomplete on 11/13/17.
 */
public class Constants {
    // Constants for SQL
    static final String SELECT = "select ";
    static final String FROM = " from ";
    static final String WHERE = " where ";
    static final String ALL = "*";
    static final String SINGLE_QUOTE = "'";
    static final String EQUAL = "=";
    static final String TABLE_SIGN_WITH_SPACES = " \t ";
    static final String AND = " and ";
    static final String OR = " or ";

    // Constants for Connection
    static final String DRIVER = "com.mysql.jdbc.Driver";
    static final String URL = "jdbc:mysql://127.0.0.1:3306/project3-nudb";
    static final String USER = "root";
    static final String PASSWORD = "wzt12629";

    // Constants for consistency
    static final String NAME = "Name";
    static final String ID = "Id";
    static final Set<String> CHOICES_SET = new HashSet<>(Arrays.asList(
            "transcript", "enroll", "withdraw", "personal details", "logout"));
}
