package io.github.eliasls.masterthesis.dataGenerator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class StudentDataGeneratorTest {

  // 20 projects per interest score (1-5), 100 projects total.
  private static List<int[]> buildProjectPool() {
    List<int[]> projects = new ArrayList<>();
    int projectId = 0;
    for (int score = 1; score <= 5; score++) {
      for (int i = 0; i < 20; i++) {
        projects.add(new int[] { projectId++, score });
      }
    }
    return projects;
  }

  @Test @DisplayName("Choices are 5 distinct projects, and higher interest scores are picked more often")
  public void testDrawChoicesDistribution() {
    List<int[]> projects = buildProjectPool();
    StudentDataGenerator generator = new StudentDataGenerator(new Random(1));
    int[] scoreByProject = new int[projects.size()];
    for (int[] project : projects) {
      scoreByProject[project[0]] = project[1];
    }

    int[] pickCountByScore = new int[6];
    int n = 20_000;
    for (int i = 0; i < n; i++) {
      int[] choices = generator.drawChoices(projects);
      Set<Integer> distinct = new HashSet<>();
      for (int projectId : choices) {
        assertTrue(distinct.add(projectId), "choices should be distinct within a student");
        pickCountByScore[scoreByProject[projectId]]++;
      }
    }

    // Weight ratio is 1:2:3:4:5, so pick counts should follow the same strict order.
    for (int score = 1; score < 5; score++) {
      assertTrue(pickCountByScore[score] < pickCountByScore[score + 1],
          "score " + score + " (" + pickCountByScore[score] + ") should be picked less than score "
              + (score + 1) + " (" + pickCountByScore[score + 1] + ")");
    }
  }

  @Test @DisplayName("Student count is 80% of the number of projects, rounded to the nearest integer")
  public void testGenerateStudentCount() {
    List<int[]> projects = buildProjectPool(); // 100 projects -> 80 students
    List<int[]> students = new StudentDataGenerator(new Random(1)).generate(projects);
    assertEquals(80, students.size());

    for (int i = 0; i < students.size(); i++) {
      int[] student = students.get(i);
      assertEquals(i, student[0]);
      assertEquals(6, student.length); // studentId + 5 choices
      Set<Integer> distinct = new HashSet<>();
      for (int c = 1; c < student.length; c++) {
        assertTrue(distinct.add(student[c]), "a student's choices should be distinct");
      }
    }
  }

  @Test @DisplayName("CSV has the header and one line per student")
  public void testCsv(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("out.csv");
    List<int[]> students = List.of(new int[] { 0, 4, 2, 9, 1, 7 });
    StudentDataGenerator.writeCsv(students, file);
    assertEquals(
        List.of("Student ID,Choice 1,Choice 2,Choice 3,Choice 4,Choice 5", "0,4,2,9,1,7"),
        Files.readAllLines(file));
  }

  @Test @DisplayName("Projects are read back correctly from a projects CSV")
  public void testReadProjects(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("projects.csv");
    List<int[]> projects = List.of(new int[] { 0, 7, 5 }, new int[] { 1, 7, 1 }, new int[] { 2, 8, 3 });
    ProjectDataGenerator.writeCsv(projects, file);

    List<int[]> read = StudentDataGenerator.readProjects(file);

    assertEquals(3, read.size());
    assertEquals(0, read.get(0)[0]);
    assertEquals(5, read.get(0)[1]);
    assertEquals(1, read.get(1)[0]);
    assertEquals(1, read.get(1)[1]);
    assertEquals(2, read.get(2)[0]);
    assertEquals(3, read.get(2)[1]);
  }
}
