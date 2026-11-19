package io.github.eliasls.masterthesis.dataGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Each projects_N.csv links to the matching supervisors_N.csv: every supervisor gets as many
// projects as their capacity.
public class ProjectDataGenerator {

  private static final int N_FILES = 20;
  private static final Path SUPERVISORS_DIR = Path.of("instances/generated/supervisors");
  private static final Path PROJECTS_DIR = Path.of("instances/generated/projects");

  // 1->20%, 2->10%, 3->40%, 4->20%, 5->10%.
  private static final int[] SCORES = { 1, 2, 3, 4, 5 };
  private static final double[] CUMULATIVE_PROBABILITIES = { 0.20, 0.30, 0.70, 0.90, 1.00 };

  private final Random rng;

  public ProjectDataGenerator() {
    this(new Random());
  }

  ProjectDataGenerator(Random rng) {
    this.rng = rng;
  }

  int drawPopularityScore() {
    double draw = rng.nextDouble();
    for (int i = 0; i < SCORES.length; i++) {
      if (draw < CUMULATIVE_PROBABILITIES[i]) {
        return SCORES[i];
      }
    }
    return SCORES[SCORES.length - 1];
  }

  // supervisorCapacities rows: {supervisorId, capacity}. Returns rows: {projectId, supervisorId, popularityScore}.
  public List<int[]> generate(List<int[]> supervisorCapacities) {
    List<int[]> projects = new ArrayList<>();
    int projectId = 0;
    for (int[] supervisor : supervisorCapacities) {
      int supervisorId = supervisor[0];
      int capacity = supervisor[1];
      for (int i = 0; i < capacity; i++) {
        projects.add(new int[] { projectId++, supervisorId, drawPopularityScore() });
      }
    }
    return projects;
  }

  static List<int[]> readSupervisorCapacities(Path file) throws IOException {
    List<String> lines = Files.readAllLines(file);
    List<int[]> capacities = new ArrayList<>();
    for (String line : lines.subList(1, lines.size())) {
      String[] parts = line.split(",");
      capacities.add(new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) });
    }
    return capacities;
  }

  static void writeCsv(List<int[]> projects, Path file) throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("Project ID,Supervisor ID,Popularity Score");
    for (int[] project : projects) {
      lines.add(project[0] + "," + project[1] + "," + project[2]);
    }
    Files.write(file, lines);
  }

  public static void main(String[] args) throws IOException {
    Path dir = Files.createDirectories(PROJECTS_DIR);
    ProjectDataGenerator generator = new ProjectDataGenerator();

    for (int i = 1; i <= N_FILES; i++) {
      Path supervisorsFile = SUPERVISORS_DIR.resolve("supervisors_" + i + ".csv");
      List<int[]> supervisorCapacities = readSupervisorCapacities(supervisorsFile);
      writeCsv(generator.generate(supervisorCapacities), dir.resolve("projects_" + i + ".csv"));
    }
    System.out.println("Wrote " + N_FILES + " files to " + dir);
  }
}
