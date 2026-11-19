package io.github.eliasls.masterthesis.dataGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Each students_N.csv links to the matching projects_N.csv: student count is STUDENT_RATIO of
// the project count, and choices are drawn weighted by each project's popularity score.
public class StudentDataGenerator {

  private static final int N_FILES = 20;
  private static final int N_CHOICES = 5;
  private static final double STUDENT_RATIO = 0.8;
  private static final Path PROJECTS_DIR = Path.of("instances/generated/projects");
  private static final Path STUDENTS_DIR = Path.of("instances/generated/students");

  private final Random rng;

  public StudentDataGenerator() {
    this(new Random());
  }

  StudentDataGenerator(Random rng) {
    this.rng = rng;
  }

  int[] drawChoices(List<int[]> projects) {
    List<int[]> pool = new ArrayList<>(projects);
    int[] choices = new int[N_CHOICES];
    for (int pick = 0; pick < N_CHOICES; pick++) {
      int totalWeight = 0;
      for (int[] project : pool) {
        totalWeight += project[1];
      }
      double draw = rng.nextDouble() * totalWeight;
      int cumulative = 0;
      int index = 0;
      for (; index < pool.size() - 1; index++) {
        cumulative += pool.get(index)[1];
        if (draw < cumulative) {
          break;
        }
      }
      choices[pick] = pool.remove(index)[0];
    }
    return choices;
  }

  // projects rows: {projectId, popularityScore}. Returns rows: {studentId, choice1, ..., choiceN}.
  public List<int[]> generate(List<int[]> projects) {
    int nStudents = (int) Math.round(STUDENT_RATIO * projects.size());
    List<int[]> students = new ArrayList<>();
    for (int studentId = 0; studentId < nStudents; studentId++) {
      int[] choices = drawChoices(projects);
      int[] row = new int[1 + N_CHOICES];
      row[0] = studentId;
      System.arraycopy(choices, 0, row, 1, N_CHOICES);
      students.add(row);
    }
    return students;
  }

  static List<int[]> readProjects(Path file) throws IOException {
    List<String> lines = Files.readAllLines(file);
    List<int[]> projects = new ArrayList<>();
    for (String line : lines.subList(1, lines.size())) {
      String[] parts = line.split(",");
      projects.add(new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[2]) });
    }
    return projects;
  }

  static void writeCsv(List<int[]> students, Path file) throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("Student ID,Choice 1,Choice 2,Choice 3,Choice 4,Choice 5");
    for (int[] student : students) {
      StringBuilder line = new StringBuilder();
      for (int i = 0; i < student.length; i++) {
        if (i > 0) {
          line.append(',');
        }
        line.append(student[i]);
      }
      lines.add(line.toString());
    }
    Files.write(file, lines);
  }

  public static void main(String[] args) throws IOException {
    Path dir = Files.createDirectories(STUDENTS_DIR);
    StudentDataGenerator generator = new StudentDataGenerator();

    for (int i = 1; i <= N_FILES; i++) {
      Path projectsFile = PROJECTS_DIR.resolve("projects_" + i + ".csv");
      List<int[]> projects = readProjects(projectsFile);
      writeCsv(generator.generate(projects), dir.resolve("students_" + i + ".csv"));
    }
    System.out.println("Wrote " + N_FILES + " files to " + dir);
  }
}
