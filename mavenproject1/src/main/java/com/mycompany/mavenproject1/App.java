package com.mycompany.mavenproject1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;



public class App extends Application {

    private final Map<String, Student> students = new HashMap<>();
    private Scene loginScene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        if (!loadStudentData()) {
            System.err.println("Failed to load student data. Please check the students.txt file.");
            return;
        }

        // Login Page
        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Arial", 14));
        emailLabel.setTextFill(Color.WHITE);
        TextField emailField = new TextField();
        emailField.setPrefWidth(250);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Arial", 14));
        passwordLabel.setTextFill(Color.WHITE);
        PasswordField passwordField = new PasswordField();
        passwordField.setPrefWidth(250);

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);

        GridPane loginPane = new GridPane();
        loginPane.setPadding(new Insets(20));
        loginPane.setVgap(45);
        loginPane.setHgap(15);
        loginPane.add(emailLabel, 0, 0);
        loginPane.add(emailField, 1, 0);
        loginPane.add(passwordLabel, 0, 1);
        loginPane.add(passwordField, 1, 1);
        loginPane.add(loginButton, 1, 2);
        loginPane.add(messageLabel, 1, 3);

        loginPane.setAlignment(Pos.CENTER);
        loginPane.setStyle("-fx-background-color: #2E2E2E;");

        loginScene = new Scene(loginPane, 600, 400);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("School System - Login");
        primaryStage.show();

        loginButton.setOnAction(event -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            // First check if the user is admin
            if (isAdmin(email, password)) {
                showAdminPage(primaryStage);
            } else {
                // If not admin, check if the user is a student
                Student student = students.get(email);
                if (student != null && student.getPassword().equals(password)) {
                    showStudentPage(primaryStage, student);
                } else {
                    messageLabel.setText("Invalid email or password.");
                }
            }
        });
    }

    private boolean loadStudentData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("students.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String email = parts[2].trim();
                    String password = parts[3].trim();
                    double math = Double.parseDouble(parts[4].trim());
                    double science = Double.parseDouble(parts[5].trim());
                    double english = Double.parseDouble(parts[6].trim());
                    students.put(email, new Student(id, name, email, password, math, science, english));
                }
            }
            return true;
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isAdmin(String email, String password) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("admin.dat"))) {
            String storedEmail = (String) ois.readObject();
            String storedPassword = (String) ois.readObject();
            if(storedEmail.equals(email) && storedPassword.equals(password))
            {
                return true;
            }
            else
            {
                return false; 
            }
            
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAdminPage(Stage stage) {
        VBox adminPane = new VBox(20);
        adminPane.setPadding(new Insets(20));
        adminPane.setStyle("-fx-background-color: #2E2E2E;");

        Label adminLabel = new Label("Admin Dashboard");
        adminLabel.setFont(Font.font("Arial", 24));
        adminLabel.setTextFill(Color.WHITE);

        Button modifyStudentButton = new Button("Modify Student Data");
        modifyStudentButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        modifyStudentButton.setOnAction(event -> showModifyStudentPage(stage));

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white;");
        backButton.setOnAction(event -> stage.setScene(loginScene));

        adminPane.getChildren().addAll(adminLabel, modifyStudentButton, backButton);
        adminPane.setAlignment(Pos.CENTER);

        Scene adminScene = new Scene(adminPane, 800, 600);
        stage.setScene(adminScene);
        stage.setTitle("Admin Dashboard");
    }

    private void showModifyStudentPage(Stage stage) {
        VBox modifyPane = new VBox(20);
        modifyPane.setPadding(new Insets(20));
        modifyPane.setStyle("-fx-background-color: #2E2E2E;");

        Label header = new Label("Modify Student Data");
        header.setFont(Font.font("Arial", 24));
        header.setTextFill(Color.WHITE);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter Student Email");
        emailField.setPrefWidth(300);

        Button fetchButton = new Button("Fetch Student");
        fetchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);

        VBox studentDetails = new VBox(10);

        fetchButton.setOnAction(event -> {
            String email = emailField.getText().trim();
            Student student = students.get(email);
            if (student != null) {
                studentDetails.getChildren().clear();

                TextField nameField = new TextField(student.getName());
                TextField mathField = new TextField(String.valueOf(student.getMath()));
                TextField scienceField = new TextField(String.valueOf(student.getScience()));
                TextField englishField = new TextField(String.valueOf(student.getEnglish()));

                Button saveButton = new Button("Save Changes");
                saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

                saveButton.setOnAction(e -> {
                    student.setName(nameField.getText());
                    student.setMath(Double.parseDouble(mathField.getText()));
                    student.setScience(Double.parseDouble(scienceField.getText()));
                    student.setEnglish(Double.parseDouble(englishField.getText()));

                    saveStudentDataToFile();
                    
                    messageLabel.setText("Student data updated successfully.");
                });

                studentDetails.getChildren().addAll(
                        new Label("Name:"), nameField,
                        new Label("Math Grade:"), mathField,
                        new Label("Science Grade:"), scienceField,
                        new Label("English Grade:"), englishField,
                        saveButton
                );
            } else {
                messageLabel.setText("Student not found.");
            }
        });

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white;");
        backButton.setOnAction(event -> stage.setScene(loginScene));

        modifyPane.getChildren().addAll(header, emailField, fetchButton, messageLabel, studentDetails, backButton);

        Scene modifyScene = new Scene(modifyPane, 800, 600);
        stage.setScene(modifyScene);
        stage.setTitle("Modify Student Data");
    }

    private void saveStudentDataToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("students.txt"))) {
            for (Student student : students.values()) {
                writer.write(student.getId() + "," +
                        student.getName() + "," +
                        student.getEmail() + "," +
                        student.getPassword() + "," +
                        student.getMath() + "," +
                        student.getScience() + "," +
                        student.getEnglish());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showStudentPage(Stage stage, Student student) {
        Label nameLabel = new Label("Name: " + student.getName());
        nameLabel.setFont(Font.font("Arial", 24));
        nameLabel.setTextFill(Color.WHITE);
        Label idLabel = new Label("ID: " + student.getId());
        idLabel.setFont(Font.font("Arial", 24));
        idLabel.setTextFill(Color.WHITE);

        // Displaying Grades (Math, Science, and English)
        Label mathGradeLabel = new Label("Math Grade: " + student.getMath());
        mathGradeLabel.setFont(Font.font("Arial", 18));
        mathGradeLabel.setTextFill(Color.WHITE);

        Label scienceGradeLabel = new Label("Science Grade: " + student.getScience());
        scienceGradeLabel.setFont(Font.font("Arial", 18));
        scienceGradeLabel.setTextFill(Color.WHITE);

        Label englishGradeLabel = new Label("English Grade: " + student.getEnglish());
        englishGradeLabel.setFont(Font.font("Arial", 18));
        englishGradeLabel.setTextFill(Color.WHITE);

        Button backButton = new Button("Back to Login");
        backButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white;");
        backButton.setOnAction(event -> stage.setScene(loginScene)); 

        // Print Button
        Button printButton = new Button("Print");
        printButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        printButton.setOnAction(event -> printStudentPage(student));

        VBox studentInfo = new VBox(10, nameLabel, idLabel, mathGradeLabel, scienceGradeLabel, englishGradeLabel, printButton);
        studentInfo.setAlignment(Pos.CENTER);
        studentInfo.setPadding(new Insets(20));

        VBox layout = new VBox(20, studentInfo, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2E2E2E;");

        Scene studentScene = new Scene(layout, 800, 600);
        stage.setScene(studentScene);
        stage.setTitle("Student Information");
    }

    private void printStudentPage(Student student) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(null)) {
            job.printPage(createPrintContent(student));
            job.endJob();
        }
    }

    private VBox createPrintContent(Student student) {
        VBox printContent = new VBox(10);
        printContent.setPadding(new Insets(20));

        Label header = new Label("Student Report");
        header.setFont(Font.font("Arial", 24));

        HBox tableHeader = new HBox(30);
        tableHeader.getChildren().addAll(new Label("Subject"), new Label("Grade"));

        HBox mathRow = new HBox(30);
        mathRow.getChildren().addAll(new Label("Math"), new Label(String.valueOf(student.getMath())));

        HBox scienceRow = new HBox(30);
        scienceRow.getChildren().addAll(new Label("Science"), new Label(String.valueOf(student.getScience())));

        HBox englishRow = new HBox(30);
        englishRow.getChildren().addAll(new Label("English"), new Label(String.valueOf(student.getEnglish())));

            printContent.getChildren().addAll(header, tableHeader, mathRow, scienceRow, englishRow);
                return printContent;
        }

        public class Student {
        private String id;
        private String name;
        private String email;
        private String password;
        private double math;
        private double science;
        private double english;

    // Constructor
        public Student(String id, String name, String email, String password, double math, double science, double english) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
            this.math = math;
            this.science = science;
            this.english = english;
        }

    // Getters and Setters
        public String getName() 
        {
            return name;
        }

        public void setName(String name) 
        {
            this.name = name;
        }

        public String getId() 
        {
            return id;
        }

        public void setId(String id) 
        {
            this.id = id;
        }

        public String getEmail() 
        {
            return email;
        }

        public void setEmail(String email) 
        {
            this.email = email;
        }

        public String getPassword() 
        {
            return password;
        }

        public void setPassword(String password) 
        {
            this.password = password;
        }

        public double getMath() 
        {
            return math;
        }

        public void setMath(double math) 
        {
            this.math = math;
        }

        public double getScience() 
        {
            return science;
        }

        public void setScience(double science) 
        {
            this.science = science;
        }

        public double getEnglish() 
        {
            return english;
        }

        public void setEnglish(double english) 
        {
            this.english = english;
        }
    }
}
