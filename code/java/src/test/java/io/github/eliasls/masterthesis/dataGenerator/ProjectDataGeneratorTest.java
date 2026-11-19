package io.github.eliasls.masterthesis.dataGenerator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ProjectDataGeneratorTest {

  @Test @DisplayName("Popularity score stays between 1 and 5, and follows the configured distribution")
  public void testPopularityScoreDistribution() {
    ProjectDataGenerator generator = new ProjectDataGenerator(new Random(1));
    int[] counts = new int[6];
    int n = 100_000;
    for (int i = 0; i < n; i++) {
      int score = generator.drawPopularityScore();
      assertTrue(score >= 1 && score <= 5, "score " + score);
      counts[score]++;
    }
    // Expected shares: 1->20%, 2->10%, 3->40%, 4->20%, 5->10%.
    assertTrue(counts[3] > counts[1] && counts[3] > counts[4], "score 3 should be the most common");
    assertTrue(counts[1] > counts[2], "score 1 (20%) should beat score 2 (10%)");
    assertTrue(counts[4] > counts[5], "score 4 (20%) should beat score 5 (10%)");
    assertEquals(counts[1], counts[4], n * 0.02, "score 1 and score 4 both have 20% probability");
    assertEquals(counts[2], counts[5], n * 0.02, "score 2 and score 5 both have 10% probability");
  }

  @Test @DisplayName("Number of projects matches the sum of supervisor capacities, with project IDs running globally")
  public void testGenerate() {
    List<int[]> supervisorCapacities = List.of(new int[] { 0, 2 }, new int[] { 1, 0 }, new int[] { 2, 3 });
    List<int[]> projects = new ProjectDataGenerator(new Random(1)).generate(supervisorCapacities);

    assertEquals(5, projects.size());
    for (int i = 0; i < projects.size(); i++) {
      assertEquals(i, projects.get(i)[0], "project IDs should run globally across the file");
    }
    assertEquals(0, projects.get(0)[1]);
    assertEquals(0, projects.get(1)[1]);
    assertEquals(2, projects.get(2)[1]);
    assertEquals(2, projects.get(3)[1]);
    assertEquals(2, projects.get(4)[1]);
    for (int[] project : projects) {
      int score = project[2];
      assertTrue(score >= 1 && score <= 5, "score " + score);
    }
  }

  @Test @DisplayName("CSV has the header and one line per project")
  public void testCsv(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("out.csv");
    List<int[]> projects = List.of(new int[] { 0, 3, 5 }, new int[] { 1, 3, 1 });
    ProjectDataGenerator.writeCsv(projects, file);
    assertEquals(
        List.of("Project ID,Supervisor ID,Popularity Score", "0,3,5", "1,3,1"),
        Files.readAllLines(file));
  }

  @Test @DisplayName("Supervisor capacities are read back correctly from a supervisors CSV")
  public void testReadSupervisorCapacities(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("supervisors.csv");
    SupervisorDataGenerator.writeCsv(new int[] { 2, 0, 5 }, file);

    List<int[]> capacities = ProjectDataGenerator.readSupervisorCapacities(file);

    assertEquals(3, capacities.size());
    assertEquals(0, capacities.get(0)[0]);
    assertEquals(2, capacities.get(0)[1]);
    assertEquals(1, capacities.get(1)[0]);
    assertEquals(0, capacities.get(1)[1]);
    assertEquals(2, capacities.get(2)[0]);
    assertEquals(5, capacities.get(2)[1]);
  }
}
