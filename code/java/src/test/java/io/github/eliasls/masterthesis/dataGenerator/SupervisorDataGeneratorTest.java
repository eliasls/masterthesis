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

public class SupervisorDataGeneratorTest {

  @Test @DisplayName("Capacity stays between 1 and 10, and 3 is the most common value")
  public void testCapacityDistribution() {
    int[] capacities = new SupervisorDataGenerator(new Random(1)).generate(100_000);
    int[] counts = new int[11];
    for (int c : capacities) {
      assertTrue(c >= 1 && c <= 10, "capacity " + c);
      counts[c]++;
    }
    for (int c = 1; c <= 10; c++) {
      if (c != 3) {
        assertTrue(counts[c] < counts[3], "capacity " + c + " (" + counts[c] + ") vs 3 (" + counts[3] + ")");
      }
    }
  }

  @Test @DisplayName("CSV has the header and one line per supervisor")
  public void testCsv(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("out.csv");
    SupervisorDataGenerator.writeCsv(new int[] { 2, 5 }, file);
    assertEquals(List.of("Supervisor ID,Capacity", "0,2", "1,5"), Files.readAllLines(file));
  }
}
