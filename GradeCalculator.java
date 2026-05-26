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
        System.out.print("Do you want to calculate a project in a quarter, a midterm, or a final? ");
        String assessmentType = scanner.nextLine().trim().toLowerCase();
        while (!assessmentType.equals("project") && !assessmentType.equals("midterm") && !assessmentType.equals("final")) {
            System.out.print("Please enter project, midterm, or final: ");
            assessmentType = scanner.nextLine().trim().toLowerCase();
        }
        String assessmentLabel = assessmentType.substring(0, 1).toUpperCase() + assessmentType.substring(1);
        System.out.println("Using " + assessmentLabel + " grade mode.");
        
        // 1. Setup Categories and Weights
        System.out.print("How many grading categories are there in your syllabus? (e.g., Homework, Quizzes, Final): ");
        int numCategories = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        for (int i = 0; i < numCategories; i++) {
            System.out.print("\nEnter name for category #" + (i + 1) + ": ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter the weight for '" + name + "' (e.g., enter 20 for 20%): ");
            double weight = scanner.nextDouble();
            scanner.nextLine(); // Consume newline
            
            categories.put(name.toLowerCase(), new Category(name, weight));
        }

        // 2. Input Existing Grades
        System.out.print("\nHow many grades have been entered so far? ");
        int numGrades = scanner.nextInt();
        scanner.nextLine(); 

        for (int i = 0; i < numGrades; i++) {
            System.out.println("\n--- Grade #" + (i + 1) + " ---");
            System.out.print("Which category does this belong to? (Options: ");
            for (String catName : categories.keySet()) {
                System.out.print(categories.get(catName).name + " ");
            }
            System.out.print("): ");
            
            String catInput = scanner.nextLine().trim().toLowerCase();
            
            while (!categories.containsKey(catInput)) {
                System.out.print("Category not found. Please try again: ");
                catInput = scanner.nextLine().trim().toLowerCase();
            }

            System.out.print("Points earned: ");
            double earned = scanner.nextDouble();
            System.out.print("Maximum points possible: ");
            double max = scanner.nextDouble();
            scanner.nextLine(); 

            categories.get(catInput).addGrade(earned, max);
        }

        // 3. Calculate Current Grade
        double currentGrade = calculateOverallGrade(categories);
        System.out.printf("\n===================================\n");
        System.out.printf("Your CURRENT overall grade is: %.2f%% (%s)\n", currentGrade, getLetterGrade(currentGrade));
        System.out.printf("===================================\n");

        // 4. Predict Future Grade
        System.out.print("\nWould you like to predict your grade with an upcoming assignment? (yes/no): ");
        String predict = scanner.nextLine().trim().toLowerCase();

        if (predict.equals("yes") || predict.equals("y")) {
            System.out.print("Which category will this new " + assessmentType + " grade be in?: ");
            String predCat = scanner.nextLine().trim().toLowerCase();
            
            while (!categories.containsKey(predCat)) {
                System.out.print("Category not found. Please try again: ");
                predCat = scanner.nextLine().trim().toLowerCase();
            }

            System.out.print("Predicted points earned (e.g., 80): ");
            double predEarned = scanner.nextDouble();
            System.out.print("Predicted maximum points possible (e.g., 100): ");
            double predMax = scanner.nextDouble();
            
            // Add the hypothetical grade
            categories.get(predCat).addGrade(predEarned, predMax);
            
            // Recalculate
            double predictedGrade = calculateOverallGrade(categories);
            System.out.printf("\n===================================\n");
            System.out.printf("If you get a %.1f/%.1f on this assignment,\n", predEarned, predMax);
            System.out.printf("Your PREDICTED final grade will be: %.2f%% (%s)\n", predictedGrade, getLetterGrade(predictedGrade));
            System.out.printf("===================================\n");
        }

        System.out.println("\nGood luck with your classes!");
        scanner.close();
    }

    /**
     * Calculates the overall grade based on categories that have grades.
     * Scales weights dynamically if some categories are empty.
     */
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

    /**
     * Converts a percentage to your school's specific letter grade scale.
     */
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