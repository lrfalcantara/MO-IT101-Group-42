/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cp1ms2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 *
 * @author ASUS
 */
public class CP1MS2 {
      //====== MAX SIZES (adjust if needed) ======
    static final int MAX_EMP = 2000;
    static final int MAX_REC = 200000;

    //====== EMPLOYEE "DATABASE" (parallel arrays; no OOP) ======
    static int empCount = 0;
    static int[] empNo = new int[MAX_EMP];
    static String[] empName = new String[MAX_EMP];
    static String[] empBday = new String[MAX_EMP];
    static double[] empRate = new double[MAX_EMP];

    // ====== ATTENDANCE RECORDS (parallel arrays) ======
    static int recCount = 0;
    static int[] recEmpNo = new int[MAX_REC];
    static LocalDate[] recDate = new LocalDate[MAX_REC];
    static LocalTime[] recIn = new LocalTime[MAX_REC];
    static LocalTime[] recOut = new LocalTime[MAX_REC];

    // ====== LOGIN VALID CREDENTIALS ======
    static final String USER_EMPLOYEE = "employee";
    static final String USER_PAYROLL = "payroll_staff";
    static final String PASSWORD = "12345";

    // ====== TIME RULES ======
    static final LocalTime SHIFT_START = LocalTime.of(8, 0);
    static final LocalTime SHIFT_END = LocalTime.of(17, 0);
    static final LocalTime GRACE_TIME = LocalTime.of(8, 5); // 08:05 treated as 08:00

    // ====== DEDUCTION RATES (SAMPLE - adjust if your instructor gives specific) ======
    // IMPORTANT: Deductions computed from MONTHLY GROSS (cutoff1 + cutoff2)
    static final double SSS_RATE = 0.045;
    static final double PHILHEALTH_RATE = 0.03;
    static final double PAGIBIG_FIXED = 100.00;
    static final double TAX_RATE = 0.10;
    
    public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);

        // ====== STEP 1: Load CSV Data ======
        System.out.println("=== MotorPH Basic Payroll System ===");
        

        if (!loadCSV()) {
            System.out.println("Program terminated due to CSV read error.");
            return;
        }

        // ====== STEP 2: LOGIN ======
        if (!login(sc)) {
            System.out.println("Incorrect username and/or password.");
            System.out.println("Terminating program.");
            return;
        }
        
        // ====== STEP 3: ROLE-BASED MENU ======
        // (We store the role from login in a static variable for procedural simplicity)
        if (lastLoginRole.equals(USER_EMPLOYEE)) {
            employeeMenu(sc);
        } else {
            payrollStaffMenu(sc);
        }
    }

    // ============================================================
    // LOGIN
    // ============================================================
    static String lastLoginRole = "";

    static boolean login(Scanner sc) {
        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        boolean validUser = username.equals(USER_EMPLOYEE) || username.equals(USER_PAYROLL);
        boolean validPass = password.equals(PASSWORD);

        if (validUser && validPass) {
            lastLoginRole = username;
            return true;
        }
        return false;
    }

    // ============================================================
    // MENUS
    // ============================================================
    static void employeeMenu(Scanner sc) {
        while (true) {
            System.out.println("If username is: employee");
            System.out.println("Display options:");
            System.out.println("1. Enter your employee number");
            System.out.println("2. Exit the program");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Enter employee number: ");
                String empStr = sc.nextLine().trim();

                int eno;
                try {
                    eno = Integer.parseInt(empStr);
                } catch (NumberFormatException e) {
                    System.out.println("Employee number does not exist.");
                    continue;
                }

                int idx = findEmployeeIndex(eno);
                if (idx == -1) {
                    System.out.println("Employee number does not exist.");
                } else {
                    // Display employee details + payroll records June-Dec
                    displayEmployeeHeader(idx);
                    displayPayrollRecordsForEmployee(eno);
                }
            } else if (choice.equals("2")) {
                System.out.println("Terminate the program.");
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    static void payrollStaffMenu(Scanner sc) {
        while (true) {
            System.out.println("\nIf username is: payroll_staff");
            System.out.println("Display options:");
            System.out.println("1. Process Payroll");
            System.out.println("2. Exit the program");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                processPayrollMenu(sc);
            } else if (choice.equals("2")) {
                System.out.println("Terminate the program.");
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    static void processPayrollMenu(Scanner sc) {
        while (true) {
            System.out.println("\nProcess Payroll (Do not include allowances)");
            System.out.println("Display sub-options:");
            System.out.println("1. One employee");
            System.out.println("2. All employees");
            System.out.println("3. Exit the program");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Enter employee number: ");
                String empStr = sc.nextLine().trim();

                int eno;
                try {
                    eno = Integer.parseInt(empStr);
                } catch (NumberFormatException e) {
                    System.out.println("Employee number does not exist.");
                    continue;
                }

                int idx = findEmployeeIndex(eno);
                if (idx == -1) {
                    System.out.println("Employee number does not exist.");
                } else {
                    displayEmployeeHeader(idx);
                    displayPayrollRecordsForEmployee(eno);
                }

            } else if (choice.equals("2")) {
                // Display all employees June-Dec
                for (int i = 0; i < empCount; i++) {
                    displayEmployeeHeader(i);
                    displayPayrollRecordsForEmployee(empNo[i]);
                    System.out.println("==================================================");
                }
            } else if (choice.equals("3")) {
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    // ============================================================
    // DISPLAY HELPERS
    // ============================================================
    static void displayEmployeeHeader(int idx) {
        System.out.println("\nEmployee #: " + empNo[idx]);
        System.out.println("Employee Name: " + empName[idx]);
        System.out.println("Birthday: " + empBday[idx]);
    }

    /*
     * Displays payroll records from June to December.
     * For each month:
     * - Cutoff 1: June 1–15 (NO deductions displayed/applied)
     * - Cutoff 2: June 16–end (SHOW deductions computed from monthly gross)
     */
    static void displayPayrollRecordsForEmployee(int employeeNumber) {
        int idx = findEmployeeIndex(employeeNumber);
        if (idx == -1) return;

        double rate = empRate[idx];

        // June (6) to December (12)
        for (int month = 6; month <= 12; month++) {
            // Pick a year based on your attendance data; if mixed years, you can extend logic.
            // Here we try to infer year from records (first matching record).
            Integer year = inferYearForEmployeeMonth(employeeNumber, month);
            if (year == null) {
                // If no records found for that month, still show month heading (optional)
                // Comment out if your instructor only wants months with data.
                continue;
            }

            YearMonth ym = YearMonth.of(year, month);
            LocalDate cutoff1Start = LocalDate.of(year, month, 1);
            LocalDate cutoff1End = LocalDate.of(year, month, 15);

            LocalDate cutoff2Start = LocalDate.of(year, month, 16);
            LocalDate cutoff2End = LocalDate.of(year, month, ym.lengthOfMonth());

            // Compute total hours per cutoff (based on 08:00-17:00 rule)
            double hours1 = computeTotalHours(employeeNumber, cutoff1Start, cutoff1End);
            double hours2 = computeTotalHours(employeeNumber, cutoff2Start, cutoff2End);

            // Gross per cutoff
            double gross1 = hours1 * rate;
            double gross2 = hours2 * rate;

            // IMPORTANT RULE: compute deductions from monthly gross = gross1 + gross2
            double monthlyGross = gross1 + gross2;

            // Deductions computed from monthly gross
            double sss = computeSSS(monthlyGross);
            double philHealth = computePhilHealth(monthlyGross);
            double pagIbig = computePagIbig(monthlyGross);
            double tax = computeIncomeTax(monthlyGross);
            double totalDeductions = sss + philHealth + pagIbig + tax;

            // Net salary rule:
            // - 1st cutoff: NO deductions (shows gross = net)
            // - 2nd cutoff: apply ALL monthly deductions (subtract totalDeductions from gross2)
            double net1 = gross1;
            double net2 = gross2 - totalDeductions;

            // ===== DISPLAY CUTOFF 1 =====
            System.out.println("\nCutoff Date: " + monthName(month) + " 1 to " + monthName(month) + " 15");
            System.out.println("Total Hours Worked: " + hours1);
            System.out.println("Gross Salary: " + gross1);
            System.out.println("Net Salary: " + net1);
            System.out.println("==============================");

            // ===== DISPLAY CUTOFF 2 =====
            System.out.println("\nCutoff Date: " + monthName(month) + " 16 to " + monthName(month) + " " + ym.lengthOfMonth() + " (Second payout includes all deductions)");
            System.out.println("Total Hours Worked: " + hours2);
            System.out.println("Gross Salary: " + gross2);

            System.out.println("Each Deduction:");
            System.out.println("SSS: " + sss);
            System.out.println("PhilHealth: " + philHealth);
            System.out.println("Pag-IBIG: " + pagIbig);
            System.out.println("Tax: " + tax);
            System.out.println("==============================");
            
            
            System.out.println("Total Deductions: " + totalDeductions);
            System.out.println("Net Salary: " + net2);
        }
    }

    // ============================================================
    // HOURS WORKED CALCULATION (STRICT 08:00–17:00 + 08:05 grace)
    // ============================================================

    /*
     * Computes total hours worked for an employee between startDate and endDate inclusive.
     * This respects the milestone rule: ONLY count time from 08:00 to 17:00.
    */
    static double computeTotalHours(int employeeNumber, LocalDate startDate, LocalDate endDate) {
        double total = 0.0;

        for (int i = 0; i < recCount; i++) {
            if (recEmpNo[i] != employeeNumber) continue;

            LocalDate d = recDate[i];
            if (d == null) continue;

            if (d.isBefore(startDate) || d.isAfter(endDate)) continue;

            total += computeDailyHours(recIn[i], recOut[i]);
        }

        return total;
    }

    /*
     * Computes daily hours based on your rule examples:
     * - Cap to [08:00, 17:00]
     * - If timeIn <= 08:05, treat as 08:00 (not late)
     * - Do not include extra hours beyond 17:00
     */
    static double computeDailyHours(LocalTime timeIn, LocalTime timeOut) {
        if (timeIn == null || timeOut == null) return 0.0;

        // Adjust start time
        LocalTime start;
        if (!timeIn.isAfter(GRACE_TIME)) {
            // 08:00–08:05 becomes 08:00 (example c & d)
            start = SHIFT_START;
        } else if (timeIn.isBefore(SHIFT_START)) {
            // Earlier than 08:00 becomes 08:00
            start = SHIFT_START;
        } else {
            start = timeIn;
        }

        // Adjust end time (cap to 17:00)
        LocalTime end = timeOut.isAfter(SHIFT_END) ? SHIFT_END : timeOut;

        // Also ensure end is not before SHIFT_START (if weird data)
        if (end.isBefore(SHIFT_START)) return 0.0;

        // If end is before start, no hours
        if (end.isBefore(start)) return 0.0;

        // Compute minutes difference, convert to hours (no rounding)
        long minutes = java.time.Duration.between(start, end).toMinutes();
        return minutes / 60.0;
    }

    // ============================================================
    // DEDUCTION METHODS (computed from monthly gross)
    // ============================================================

    static double computeSSS(double monthlyGross) {
        // Validation: salary must be positive
        if (monthlyGross <= 0) return 0.0;
        return monthlyGross * SSS_RATE;
    }

    static double computePhilHealth(double monthlyGross) {
        if (monthlyGross <= 0) return 0.0;
        return monthlyGross * PHILHEALTH_RATE;
    }

    static double computePagIbig(double monthlyGross) {
        if (monthlyGross <= 0) return 0.0;
        return PAGIBIG_FIXED;
    }

    static double computeIncomeTax(double monthlyGross) {
        if (monthlyGross <= 0) return 0.0;
        return monthlyGross * TAX_RATE;
    }

    // ============================================================
    // CSV LOADING (procedural)
    // ============================================================

    static boolean loadCSV() { 
        String filePath = "motorph_data.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line = br.readLine();
            if (line == null) {
                System.out.println("CSV is empty.");
                return false;
            }

            // Check header
            boolean hasHeader = line.toLowerCase().contains("employeeno")
                    || line.toLowerCase().contains("employee")
                    || line.toLowerCase().contains("hourlyrate");

            if (!hasHeader) {
                // If no header, process first line as data
                processCSVRow(line);
            }

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                processCSVRow(line);
            }

            System.out.println("Loaded employees: " + empCount);
            System.out.println("Loaded attendance records: " + recCount);
            return true;

        } catch (IOException e) {
            System.out.println("Error reading CSV: " + e.getMessage());
            return false;
        }
    }

    /*
     * Simple CSV split: assumes no commas inside fields.
     * If your names can contain commas, you'd need a proper CSV parser.
     */
    static void processCSVRow(String line) {

    String[] parts = line.split(",", -1);

    if (parts.length < 6) return;

    int eno = parseIntSafe(parts[0]);

    String lastName = parts[1].trim();
    String firstName = parts[2].trim();
    String fullName = firstName + " " + lastName;

    String birthday = parts[3].trim();     // birthday
    double rate = parseDoubleSafe(parts[7].trim());   // HourlyRate

    LocalDate date = parseDate(parts[4].trim());
    LocalTime tin = parseTime(parts[5].trim());
    LocalTime tout = parseTime(parts[6].trim());

    int idx = findEmployeeIndex(eno);

    if (idx == -1) {
        empNo[empCount] = eno;
        empName[empCount] = fullName;
        empBday[empCount] = birthday;
        empRate[empCount] = rate;
        empCount++;
    }

    recEmpNo[recCount] = eno;
    recDate[recCount] = date;
    recIn[recCount] = tin;
    recOut[recCount] = tout;

    recCount++;

        // Store attendance record
        if (recCount < MAX_REC && date != null) {
            recEmpNo[recCount] = eno;
            recDate[recCount] = date;
            recIn[recCount] = tin;
            recOut[recCount] = tout;
            recCount++;
        }
    }

    // ============================================================
    // SEARCH / PARSE HELPERS
    // ============================================================

    static int findEmployeeIndex(int employeeNumber) {
        for (int i = 0; i < empCount; i++) {
            if (empNo[i] == employeeNumber) return i;
        }
        return -1;
    }

    static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    static double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    static LocalDate parseDate(String s) {
    // Try ISO first: YYYY-MM-DD
    try {
        return LocalDate.parse(s);
    } catch (DateTimeParseException ignored) {
        // Try M/d/yyyy: 6/3/2024
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("M/d/yyyy");
            return LocalDate.parse(s, f);
        } catch (DateTimeParseException ignored2) {
            return null;
        }
    }
}

    static LocalTime parseTime(String s) {

    try {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("H:mm");
        return LocalTime.parse(s, f);
    } catch (DateTimeParseException e) {
        return null;
    }

}

    static Integer inferYearForEmployeeMonth(int employeeNumber, int month) {
        for (int i = 0; i < recCount; i++) {
            if (recEmpNo[i] != employeeNumber) continue;
            if (recDate[i] == null) continue;
            if (recDate[i].getMonthValue() == month) {
                return recDate[i].getYear();
            }
        }
        return null;
    }

    static String monthName(int month) {
        switch (month) {
            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
            default: return "Month" + month;
        }
    } 
}
   