# Database-Project
EECS495 Database Project: Caesar Information Management System

## Details:
Develop a small database client that implements something along the lines of Northwestern Caesar’s academic system. It should implement the following subset of the functionality provided by Caesar:

### a) login
The system should first ask the students to login using their username and password. Those two input arguments must be matched against the database. When successfully logged in, students shall be directed to the STUDENT MENU screen. If username or password is incorrect, an appropriate error message shall be given.

### b) show menu
The STUDENT MENU shall list the student’s current courses of this quarter and year (use clock to programmatically determine the current year and quarter instead of hardcoding it. The courses currently being taken are also in the transcript but the grades are NULL), plus the following options: Transcript, Enroll, Withdraw, Personal Details and Logout. Logout shall log out the student and go back to the state where it waits for a student to login.

### c) show transcript
The TRANSCRIPT screen shall list logged-in students their full transcript with all courses and grades. The student should be provided with an option to see details of any of the courses listed in the transcript or go back to the STUDENT MENU. If the student wants to see details of any course, then he/she should be asked to enter the course number and the details of the corresponding course should be shown. The course details should include: the course number and title, the year and quarter when the student took the course, the number of enrolled students, the maximum enrollment and the lecturer (name), the grade scored by the student.

### d) enroll courses
The ENROLL screen shall allow logged-in students to enroll in a new course. Students shall be able to select a specific course offering in this screen, but only subject offerings of the current year and quarter or the following quarter shall be presented (again: don’t hard-code these years but determine year and quarter programmatically from the current date using the clock). Students can only enroll in courses whose direct pre-requisites they have passed (not failed or incomplete) and whose maximum enrollment number has not been reached (i.e. MaxEnrollment > Enrollment). On successful enrollment, a new entry in the Transcript table shall be created with a NULL grade, plus the Enrollment attribute of the corresponding course shall be increased by one. In case the student cannot enroll because he/she has not cleared the prerequisites, print those prerequisites on the screen. Implement this part using stored procedures and call it from your database client program.

### e) withdraw courses
The WITHDRAW screen shall allow logged-in students to withdraw from a course that they are currently enrolled in. Students can only withdraw from a unit that they have not finished so far (i.e. grade is NULL). If successful, the corresponding Transcript entry shall be removed and the current Enrollment number of the corresponding course shall be decreased by one. Implement this part using stored procedures and call it from your database client program. If the Enrollment number goes below 50% of the MaxEnrollment, then a warning message should be shown on the screen. Implement this using Triggers.

### f) show personal details
The PERSONAL DETAILS screen shall show the current personal record of the student and allow him/her to change his/her password and address. On submission, the corresponding Student record shall be updated accordingly. Students cannot change their student id or name.
