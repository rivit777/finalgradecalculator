import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Category {
    String name;
    double weight; // Percentage (e.g., 20 for 20%)
    double earnedPoints = 0;
    double maxPoints = 0;

    public Category(String name, double weight) {
        this.name = name;
        this.weight = weight;
    }

    public void addGrade(double earned, double max) {
        this.earnedPoints += earned;
        this.maxPoints += max;
    }
    
    public double getCategoryPercentage() {
        if (maxPoints == 0) return 0;
        return (earnedPoints / maxPoints) * 100.0;
    }
}

public class GradeCalculator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Map<String, Category> categories = new HashMap<>();

        System.out.println("=== School Grade Calculator & Predictor ===");
        String assessmentType = promptChoice(scanner,
                "Do you want to calculate a project in a quarter, a midterm, or a final? ",
                new String[]{"project", "midterm", "final"});
        String assessmentLabel = assessmentType.substring(0, 1).toUpperCase() + assessmentType.substring(1);
        System.out.println("Using " + assessmentLabel + " grade mode.");

        int markingPeriods = 0;
        double explicitCurrentGrade = -1.0;
        if (assessmentType.equals("midterm") || assessmentType.equals("final")) {
            markingPeriods = promptInt(scanner, "How many marking periods did you have? ", 1, Integer.MAX_VALUE);
            explicitCurrentGrade = promptDouble(scanner, "What is your current grade right now? ", 0.0, 150.0);
        }

        int numCategories = promptInt(scanner,
                "How many grading categories are there in your syllabus? (e.g., Homework, Quizzes, Final): ",
                1, Integer.MAX_VALUE);

        double totalWeight = 0.0;
        boolean restartCategories;
        do {
            restartCategories = false;
            categories.clear();
            totalWeight = 0.0;

            for (int i = 0; i < numCategories; i++) {
                System.out.print("\nEnter name for category #" + (i + 1) + ": ");
                String name = scanner.nextLine().trim();
                while (name.isEmpty()) {
                    System.out.print("Category name cannot be empty. Please enter a category name: ");
                    name = scanner.nextLine().trim();
                }

                double weight = promptDouble(scanner,
                        "Enter the weight for '" + name + "' (e.g., enter 20 for 20%): ",
                        0.0, 100.0);
                double projectedTotal = totalWeight + weight;
                if (projectedTotal > 100.0) {
                    System.out.println("I understand: total weights exceed 100%.");
                    System.out.println("Restarting category entry so you can correct weights.");
                    restartCategories = true;
                    break;
                }

                totalWeight += weight;
                categories.put(name.toLowerCase(), new Category(name, weight));
            }
        } while (restartCategories);

        int numGrades = promptInt(scanner, "\nHow many grades have been entered so far? ", 0, Integer.MAX_VALUE);

        for (int i = 0; i < numGrades; i++) {
            System.out.println("\n--- Grade #" + (i + 1) + " ---");
            System.out.print("Which category does this belong to? (Options: ");
            for (Category category : categories.values()) {
                System.out.print(category.name + " ");
            }
            System.out.print("): ");

            String catInput = scanner.nextLine().trim().toLowerCase();
            while (!categories.containsKey(catInput)) {
                System.out.print("Category not found. Please try again: ");
                catInput = scanner.nextLine().trim().toLowerCase();
            }

            double earned;
            double max;
            while (true) {
                earned = promptDouble(scanner, "Points earned: ", 0.0, Double.MAX_VALUE);
                max = promptDouble(scanner, "Maximum points possible: ", Double.MIN_VALUE, Double.MAX_VALUE);

                if (max <= 0) {
                    System.out.println("Maximum points must be greater than 0. Please re-enter the values.");
                    continue;
                }

                if (earned > max) {
                    System.out.printf("Warning: points earned (%.2f) exceed maximum (%.2f).\n", earned, max);
                    System.out.print("If this is a data entry mistake type 'r' to re-enter, or 'a' to accept anyway: ");
                    String resp = scanner.nextLine().trim().toLowerCase();
                    if (resp.equals("a") || resp.equals("accept") || resp.equals("yes") || resp.equals("y")) {
                        break;
                    } else {
                        System.out.println("Re-enter the points for this grade.");
                        continue;
                    }
                }
                break;
            }

            categories.get(catInput).addGrade(earned, max);
        }

        double categoryCurrentGrade = calculateOverallGrade(categories);
        System.out.printf("\n===================================\n");
        System.out.printf("Your CURRENT overall grade from categories is: %.2f%% (%s)\n", categoryCurrentGrade,
                getLetterGrade(categoryCurrentGrade));
        if (explicitCurrentGrade >= 0) {
            System.out.printf("Your CURRENT grade right now is: %.2f%% (%s)\n", explicitCurrentGrade,
                    getLetterGrade(explicitCurrentGrade));
            System.out.printf("Based on %d marking period(s) for your %s calculation.\n", markingPeriods,
                    assessmentType);
        }
        System.out.printf("===================================\n");

        double baselineCurrentGrade = explicitCurrentGrade >= 0 ? explicitCurrentGrade : categoryCurrentGrade;
        System.out.printf("Baseline grade used for predictions: %.2f%% (%s)\n", baselineCurrentGrade,
                getLetterGrade(baselineCurrentGrade));

        if (promptYesNo(scanner,
                "\nWould you like to see what you need to reach nearby letter grades with a future assignment? (yes/no): ")) {
            double futureWeight = promptDouble(scanner,
                    "What percentage of your overall grade is the future assignment/exam worth? ", 0.0, 100.0);
            printLetterProximity(baselineCurrentGrade, futureWeight);
        }

        if (promptYesNo(scanner, "\nWould you like to predict your grade with an upcoming assignment? (yes/no): ")) {
            double futureWeight = promptDouble(scanner,
                    "What percentage of your overall grade is the upcoming assignment/exam worth? ", 0.0, 100.0);
            double predictedEarned = promptDouble(scanner, "Predicted points earned (e.g., 80): ", 0.0,
                    Double.MAX_VALUE);
            double predictedMax = promptDouble(scanner, "Predicted maximum points possible (e.g., 100): ",
                    Double.MIN_VALUE, Double.MAX_VALUE);
            double predictedPercent = (predictedEarned / predictedMax) * 100.0;
            double projectedGrade = calculateProjectedGrade(baselineCurrentGrade, futureWeight, predictedPercent);

            String gradeLabel = assessmentType.equals("final") ? "final" : assessmentType.equals("midterm") ? "semester" : "overall";
            System.out.printf("\n===================================\n");
            System.out.printf("If you score %.1f/%.1f (%.2f%%) on this assignment,\n", predictedEarned,
                    predictedMax, predictedPercent);
            System.out.printf("Your PREDICTED %s grade is: %.2f%% (%s)\n", gradeLabel, projectedGrade,
                    getLetterGrade(projectedGrade));
            System.out.printf("===================================\n");

            printLetterProximity(baselineCurrentGrade, futureWeight);
        }

        System.out.println("\nGood luck with your classes!");
        scanner.close();
    }

    public static int promptInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    System.out.printf("Please enter a whole number between %d and %d.%n", min, max);
                    continue;
                }
                return value;
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    public static double promptDouble(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(line);
                if (value < min || value > max) {
                    System.out.printf("Please enter a number between %.2f and %.2f.%n", min, max);
                    continue;
                }
                return value;
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String response = scanner.nextLine().trim().toLowerCase();
        while (!response.equals("yes") && !response.equals("y") && !response.equals("no") && !response.equals("n")) {
            System.out.print("Please answer yes or no: ");
            response = scanner.nextLine().trim().toLowerCase();
        }
        return response.equals("yes") || response.equals("y");
    }

    public static String promptChoice(Scanner scanner, String prompt, String[] allowedValues) {
        System.out.print(prompt);
        String choice = scanner.nextLine().trim().toLowerCase();
        while (true) {
            for (String allowed : allowedValues) {
                if (choice.equals(allowed)) {
                    return choice;
                }
            }
            System.out.print("Please enter ");
            for (int i = 0; i < allowedValues.length; i++) {
                System.out.print(allowedValues[i]);
                if (i < allowedValues.length - 1) {
                    System.out.print(" or ");
                }
            }
            System.out.print(": ");
            choice = scanner.nextLine().trim().toLowerCase();
        }
    }

    public static double calculateOverallGrade(Map<String, Category> categories) {
        double totalWeightedScore = 0;
        double activeWeights = 0;

        for (Category cat : categories.values()) {
            if (cat.maxPoints > 0) {
                double categoryScore = cat.getCategoryPercentage();
                totalWeightedScore += (categoryScore * (cat.weight / 100.0));
                activeWeights += cat.weight;
            }
        }

        if (activeWeights == 0) return 0.0;

        return (totalWeightedScore / (activeWeights / 100.0));
    }

    public static double calculateProjectedGrade(double currentGrade, double futureWeight, double futurePercent) {
        double weightFraction = futureWeight / 100.0;
        return currentGrade * (1.0 - weightFraction) + futurePercent * weightFraction;
    }

    public static void printLetterProximity(double currentGrade, double futureWeight) {
        double weightFraction = futureWeight / 100.0;
        System.out.printf("\n--- Letter grade proximity with %.1f%% weight ---\n", futureWeight);
        if (weightFraction <= 0) {
            System.out.println("A future assignment weight must be greater than 0 to calculate letter proximity.");
            return;
        }

        String[] targets = {"B+", "A-", "A", "A+"};
        for (String target : targets) {
            double threshold = getLetterThreshold(target);
            if (currentGrade >= threshold) {
                System.out.printf("Already at or above %s (%.1f%%).\n", target, threshold);
            } else {
                double required = (threshold - currentGrade * (1.0 - weightFraction)) / weightFraction;
                if (required <= 0) {
                    System.out.printf("Already at or above %s (%.1f%%).\n", target, threshold);
                } else if (required > 150.0) {
                    System.out.printf("To reach %s (%.1f%%), you would need over 150%% on the future assignment (not realistic).\n",
                            target, threshold);
                } else {
                    System.out.printf("To reach %s (%.1f%%), you need %.2f%% on the future assignment.\n",
                            target, threshold, required);
                }
            }
        }
    }

    public static double getLetterThreshold(String letter) {
        switch (letter) {
            case "A+":
                return 96.5;
            case "A":
                return 92.5;
            case "A-":
                return 89.5;
            case "B+":
                return 86.5;
            case "B":
                return 82.5;
            case "B-":
                return 79.5;
            case "C+":
                return 76.5;
            case "C":
                return 72.5;
            case "C-":
                return 69.5;
            case "D+":
                return 66.5;
            case "D":
                return 62.5;
            case "D-":
                return 59.5;
            case "F+":
                return 49.5;
            default:
                return 0.0;
        }
    }

    public static String getLetterGrade(double percentage) {
        if (percentage >= 96.5) return "A+";
        if (percentage >= 92.5) return "A";
        if (percentage >= 89.5) return "A-";
        if (percentage >= 86.5) return "B+";
        if (percentage >= 82.5) return "B";
        if (percentage >= 79.5) return "B-";
        if (percentage >= 76.5) return "C+";
        if (percentage >= 72.5) return "C";
        if (percentage >= 69.5) return "C-";
        if (percentage >= 66.5) return "D+";
        if (percentage >= 62.5) return "D";
        if (percentage >= 59.5) return "D-";
        if (percentage >= 49.5) return "F+";
        return "F";
    }
}
