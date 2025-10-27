package com.example.userservice;

import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import com.example.userservice.util.HibernateUtil;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class App {
    private final UserService userService;
    private final Scanner scanner;

    public App() {
        this.userService = new UserService();
        this.scanner = new Scanner(System.in);
    }

    public App(UserService userService) {
        this.userService = userService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Starting User Service application");

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutdown hook triggered");
                HibernateUtil.shutdown();
                scanner.close();
            }));

            System.out.println("=== User Service ===");
            boolean running = true;

            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> createUser();
                    case "2" -> listUsers();
                    case "3" -> getUserById();
                    case "4" -> getUserByEmail();
                    case "5" -> updateUser();
                    case "6" -> deleteUser();
                    case "7" -> showStatistics();
                    case "0" -> {
                        running = false;
                        System.out.println("Exiting...");
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            }

        } catch (Exception e) {
            System.out.println("Critical error occurred: " + e.getMessage());
        } finally {
            HibernateUtil.shutdown();
            scanner.close();
            System.out.println("User Service application stopped");
        }
    }

    public static void main(String[] args) {
        new App().start();
    }

    void printMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("            USER SERVICE MENU");
        System.out.println("=".repeat(40));
        System.out.println("1. Create user");
        System.out.println("2. List all users");
        System.out.println("3. Get user by ID");
        System.out.println("4. Get user by Email");
        System.out.println("5. Update user");
        System.out.println("6. Delete user");
        System.out.println("7. Statistics");
        System.out.println("0. Exit");
        System.out.println("-".repeat(40));
        System.out.print("Your choice: ");
    }

    void createUser() {
        try {
            System.out.println("\n--- Create New User ---");

            String name = getNonEmptyInput("Name: ");
            String email = getNonEmptyInput("Email: ");
            Integer age = getAgeInput();

            User user = userService.createUser(name, email, age);
            System.out.println("User created successfully!");
            System.out.println("Created: " + user);

        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    void listUsers() {
        try {
            System.out.println("\n--- All Users ---");
            List<User> users = userService.getAllUsers();

            if (users.isEmpty()) {
                System.out.println("No users found.");
            } else {
                System.out.printf("Found %d user(s):\n", users.size());
                users.forEach(System.out::println);
            }

        } catch (Exception e) {
            System.out.println("Error retrieving users: " + e.getMessage());
        }
    }

    void getUserById() {
        try {
            System.out.println("\n--- Find User by ID ---");
            Long id = getLongInput("Enter user ID: ");

            User user = userService.getUserById(id);
            System.out.println("User found:");
            System.out.println("  " + user);

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error finding user: " + e.getMessage());
        }
    }

    void getUserByEmail() {
        try {
            System.out.println("\n--- Find User by Email ---");
            String email = getNonEmptyInput("Enter email: ");

            Optional<User> userOpt = userService.findUserByEmail(email);
            if (userOpt.isPresent()) {
                System.out.println("User found:");
                System.out.println("  " + userOpt.get());
            } else {
                System.out.println("User not found with email: " + email);
            }

        } catch (Exception e) {
            System.out.println("Error finding user: " + e.getMessage());
        }
    }

    void updateUser() {
        try {
            System.out.println("\n--- Update User ---");
            Long id = getLongInput("Enter user ID to update: ");

            User existingUser = userService.getUserById(id);
            System.out.println("Current user data: " + existingUser);

            System.out.println("\nEnter new values (press Enter to keep current):");
            System.out.print("New name (" + existingUser.getName() + "): ");
            String name = scanner.nextLine().trim();

            System.out.print("New email (" + existingUser.getEmail() + "): ");
            String email = scanner.nextLine().trim();

            System.out.print("New age (" + existingUser.getAge() + "): ");
            String ageStr = scanner.nextLine().trim();
            Integer age = ageStr.isEmpty() ? null : Integer.parseInt(ageStr);

            User updatedUser = userService.updateUser(id,
                    name.isEmpty() ? null : name,
                    email.isEmpty() ? null : email,
                    age);

            System.out.println("User updated successfully!");
            System.out.println("Updated: " + updatedUser);

        } catch (NumberFormatException e) {
            System.out.println("Invalid age format. Please enter a number.");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    void deleteUser() {
        try {
            System.out.println("\n--- Delete User ---");
            Long id = getLongInput("Enter user ID to delete: ");

            User user = userService.getUserById(id);
            System.out.println("You are about to delete:");
            System.out.println("  " + user);

            System.out.print("Are you sure? Type 'DELETE' to confirm: ");
            String confirmation = scanner.nextLine().trim();

            if ("DELETE".equalsIgnoreCase(confirmation)) {
                boolean deleted = userService.deleteUser(id);
                if (deleted) {
                    System.out.println("User deleted successfully!");
                } else {
                    System.out.println("Failed to delete user.");
                }
            } else {
                System.out.println("Deletion cancelled.");
            }

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }

    void showStatistics() {
        try {
            System.out.println("\n--- Statistics ---");
            long userCount = userService.getUserCount();
            System.out.printf("Total users in database: %d\n", userCount);

            if (userCount > 0) {
                List<User> recentUsers = userService.getUsersWithPagination(1, 5);
                System.out.println("Recent users:");
                recentUsers.forEach(user -> System.out.println("  - " + user));
            }

        } catch (Exception e) {
            System.out.println("Error retrieving statistics: " + e.getMessage());
        }
    }

    String getNonEmptyInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("This field cannot be empty. Please try again.");
        }
    }

    Integer getAgeInput() {
        while (true) {
            System.out.print("Age (optional, press Enter to skip): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                int age = Integer.parseInt(input);
                if (age < 0 || age > 150) {
                    System.out.println("Age must be between 0 and 150. Please try again.");
                    continue;
                }
                return age;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number for age.");
            }
        }
    }

    Long getLongInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}