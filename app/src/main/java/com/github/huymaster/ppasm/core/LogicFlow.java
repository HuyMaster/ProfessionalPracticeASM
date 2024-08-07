package com.github.huymaster.ppasm.core;

import com.github.huymaster.ppasm.App;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Pattern;

enum Option {
    New_Student("1", "Insert new student", new InsertStudentAction()),
    Print_Student_List("2", "Student list", new PrintStudentListAction()),
    Delete_Student("3", "Delete student", new DeleteStudentAction()),
    Print_Student_List_ByGrade("4", "Student list (by grade from high to low)", new PrintStudentListAction(
            (Comparator<Student>) (o1, o2) -> Float.compare(o2.getGrade(), o1.getGrade())
    )),
    Find_Student("5", "Find students", new FindStudentByName()),
    Find_Student_ByGrade("6", "Find students (by grade)", new FindStudentByGrade()),
    Exit("0", "Exit", s -> {
        System.out.println("Exiting...");
        s.close();
        System.exit(0);
    });

    final String key;
    final String text;
    final Action action;

    private Option(String key, String text, Action action) {
        this.key = key;
        this.text = text;
        this.action = action;
    }
}

interface Action {
    void execute(Scanner scanner);
}

public class LogicFlow {
    public LogicFlow() {
        startLogicFlow();
    }

    private void startLogicFlow() {
        var options = Option.values();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n\nSelect an option:");
            for (Option option : options) {
                System.out.println(option.key + ": " + option.text);
            }
            System.out.print("\nYour choice: ");
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                var opt = Arrays.stream(options).filter(it -> it.key.equals(input)).findFirst();
                if (opt.isPresent()) {
                    opt.get().action.execute(scanner);
                } else
                    System.out.format("Invalid option: %s\n", input);
            }
        }
    }
}

class InsertStudentAction implements Action {

    @Override
    public void execute(Scanner scanner) {
        var emailPattern = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        var phonePattern = Pattern.compile("^0\\d{9}$");
        System.out.println("===Insert new student===");
        String id = null;
        do {
            System.out.print("ID: ");
            var tmp = scanner.nextLine();
            if (tmp.isBlank()) {
                System.out.println("Invalid ID.");
                continue;
            }
            id = tmp;
        } while (id == null);
        String name = null;
        do {
            System.out.print("Name: ");
            var tmp = scanner.nextLine();
            if (tmp.isBlank()) {
                System.out.println("Invalid name.");
                continue;
            }
            name = tmp;
        } while (name == null);
        int age = -1;
        do {
            try {
                System.out.print("Age: ");
                var str = scanner.nextLine().trim();
                age = Integer.parseInt(str);
                if (age < 0)
                    System.out.println("Invalid age.");
            } catch (Exception ignore) {
                System.out.println("Invalid age.");
            }
        } while (age < 0);
        String email = null;
        do {
            System.out.print("Email: ");
            var tmp = scanner.nextLine();
            if (tmp.isBlank() || !emailPattern.matcher(tmp).matches()) {
                System.out.println("Invalid email.");
                continue;
            }
            email = tmp;
        } while (email == null);
        String phone = null;
        do {
            System.out.print("Phone: ");
            var tmp = scanner.nextLine();
            if (tmp.isBlank() || !phonePattern.matcher(tmp).matches()) {
                System.out.println("Invalid phone.");
                continue;
            }
            phone = tmp;
        } while (phone == null);
        int gender = -2;
        do {
            System.out.print("Gender (0: Male, 1: Female): ");
            try {
                var tmp = Integer.parseInt(scanner.nextLine());
                gender = Gender.getGender(tmp).id;
            } catch (Exception ignore) {
                System.out.println("Invalid gender.");
            }
        } while (gender == -2);
        float grade = -1;
        do {
            try {
                System.out.print("Grade: ");
                var tmp = Float.parseFloat(scanner.nextLine());
                grade = tmp;
                if (grade < 0)
                    System.out.println("Invalid grade.");
            } catch (Exception ignore) {
                System.out.println("Invalid grade.");
            }
        } while (grade < 0);
        var conn = App.connection.get();
        try {
            var command =
                    "INSERT INTO students (id, name, age, email, phone, gender, grade) VALUES (?, ?, ?, ?, ?, ?, ?)";
            var stm = conn.prepareStatement(command);
            stm.setString(1, id);
            stm.setString(2, name);
            stm.setInt(3, age);
            stm.setString(4, email);
            stm.setString(5, phone);
            stm.setInt(6, gender);
            stm.setFloat(7, grade);
            stm.executeUpdate();
            stm.close();
            System.out.println("\nStudent inserted.");
        } catch (Exception e) {
            System.err.println("Cannot insert student: " + e.getMessage());
        }
    }
}

class PrintStudentListAction implements Action {
    private final Comparator<Student> comparator;

    public PrintStudentListAction() {
        this((o1, o2) -> 0);
    }

    public PrintStudentListAction(Comparator<Student> comparator) {
        this.comparator = comparator;
    }


    @Override
    public void execute(Scanner scanner) {
        System.out.println("===Student list===");
        var list = new ArrayList<Student>();
        try {
            var conn = App.connection.get();
            var stm = conn.createStatement();
            var rs = stm.executeQuery("SELECT * FROM students");
            while (rs.next()) {
                var id = rs.getString("id");
                var name = rs.getString("name");
                var age = rs.getInt("age");
                var email = rs.getString("email");
                var phone = rs.getString("phone");
                var gender = Gender.getGender(rs.getInt("gender"));
                var grade = rs.getFloat("grade");
                list.add(new Student(id, name, age, email, phone, gender, grade));
            }
        } catch (Exception e) {
            System.err.println("Cannot get student list: " + e.getMessage());
        }
        list.sort(comparator);
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("#%d: %s%n", i + 1, list.get(i));
        }
        System.out.print("\n\nPress enter to continue...");
        scanner.nextLine();
    }
}

class DeleteStudentAction implements Action {
    @Override
    public void execute(Scanner scanner) {
        System.out.println("===Delete student===");
        String id = null;
        do {
            System.out.print("ID (exit to cancel): ");
            var tmp = scanner.nextLine();
            if (tmp.isBlank()) {
                System.out.println("Invalid ID.");
                continue;
            }
            if (tmp.equals("exit")) {
                return;
            }
            if (checkId(tmp))
                id = tmp;
            else
                System.out.println("Student ID not found.");
        } while (id == null);
        var conn = App.connection.get();
        try {
            var stm = conn.createStatement();
            var command = String.format("DELETE FROM students WHERE id='%s'", id);
            stm.executeUpdate(command);
            stm.close();
            System.out.println("\nStudent deleted.");
        } catch (Exception e) {
            System.err.println("Cannot delete student: " + e.getMessage());
        }
    }

    private boolean checkId(String id) {
        try {
            var conn = App.connection.get();
            var stm = conn.createStatement();
            var rs = stm.executeQuery(String.format("SELECT id FROM students WHERE id='%s'", id));
            return rs.next();
        } catch (Exception e) {
            System.err.println("Cannot check ID: " + e.getMessage());
        }
        return false;
    }
}

class FindStudentByName implements Action {
    @Override
    public void execute(Scanner scanner) {
        System.out.println("===Student finder===");
        String filter;
        do {
            System.out.print("Filter (ID or Name): ");
            filter = scanner.nextLine();
        } while (filter == null);
        var list = new ArrayList<Student>();
        try {
            var conn = App.connection.get();
            var stm = conn.createStatement();
            var rs = stm.executeQuery("SELECT * FROM students");
            while (rs.next()) {
                var id = rs.getString("id");
                var name = rs.getString("name");
                var age = rs.getInt("age");
                var email = rs.getString("email");
                var phone = rs.getString("phone");
                var gender = Gender.getGender(rs.getInt("gender"));
                var grade = rs.getFloat("grade");
                list.add(new Student(id, name, age, email, phone, gender, grade));
            }
        } catch (Exception e) {
            System.err.println("Cannot get student list: " + e.getMessage());
        }
        String finalFilter = filter;
        var filtered = list.stream().filter(it -> it.getName().startsWith(finalFilter) || it.getId().startsWith(finalFilter)).toList();
        for (int i = 0; i < filtered.size(); i++) {
            System.out.printf("#%d: %s%n", i + 1, filtered.get(i));
        }
        System.out.print("\n\nPress enter to continue...");
        scanner.nextLine();
    }
}

class FindStudentByGrade implements Action {
    @Override
    public void execute(Scanner scanner) {
        System.out.println("===Student finder===");
        float filter = -1f;
        do {
            System.out.print("Student have grade >= ");
            try {
                filter = Float.parseFloat(scanner.nextLine());
            } catch (Exception ignore) {
                System.out.println("Invalid grade.");
            }
        } while (filter < 0);
        var list = new ArrayList<Student>();
        try {
            var conn = App.connection.get();
            var stm = conn.prepareStatement("SELECT * FROM students WHERE grade >= ?");
            stm.setFloat(1, filter);
            var rs = stm.executeQuery();
            while (rs.next()) {
                var id = rs.getString("id");
                var name = rs.getString("name");
                var age = rs.getInt("age");
                var email = rs.getString("email");
                var phone = rs.getString("phone");
                var gender = Gender.getGender(rs.getInt("gender"));
                var grade = rs.getFloat("grade");
                list.add(new Student(id, name, age, email, phone, gender, grade));
            }
        } catch (Exception e) {
            System.err.println("Cannot get student list: " + e.getMessage());
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("#%d: %s%n", i + 1, list.get(i));
        }
        System.out.print("\n\nPress enter to continue...");
        scanner.nextLine();
    }
}