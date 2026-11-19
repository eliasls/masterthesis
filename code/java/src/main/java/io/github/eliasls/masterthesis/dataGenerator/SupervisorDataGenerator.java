package io.github.eliasls.masterthesis.dataGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Writes N_FILES CSVs with "Supervisor ID,Capacity" for 82 supervisors each.
// Capacity is drawn from a normal distribution around 3 (range 1-10), different on every run.
public class SupervisorDataGenerator {

  private static final int N_FILES = 20;
  private static final int N_SUPERVISORS = 82;
  private static final int MIN_CAPACITY = 1;
  private static final int MAX_CAPACITY = 10;
  private static final double MEAN = 3.0;
  private static final double STANDARD_DEVIATION = 1.5;

  private final Random rng;

  public SupervisorDataGenerator() {
    this(new Random());
  }

  SupervisorDataGenerator(Random rng) {
    this.rng = rng;
  }

  // Draw from the normal distribution and round; draw again if outside 1-10.
  int drawCapacity() {
    int capacity;
    do {
      capacity = (int) Math.round(MEAN + STANDARD_DEVIATION * rng.nextGaussian());
    } while (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY);
    return capacity;
  }

  // Index in the returned array is the supervisor ID.
  public int[] generate(int nSupervisors) {
    int[] capacities = new int[nSupervisors];
    for (int id = 0; id < nSupervisors; id++) {
      capacities[id] = drawCapacity();
    }
    return capacities;
  }

  static void writeCsv(int[] capacities, Path file) throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("Supervisor ID,Capacity");
    for (int id = 0; id < capacities.length; id++) {
      lines.add(id + "," + capacities[id]);
    }
    Files.write(file, lines);
  }

  public static void main(String[] args) throws IOException {
    Path dir = Files.createDirectories(Path.of("instances/generated/supervisors"));
    SupervisorDataGenerator generator = new SupervisorDataGenerator();

    for (int i = 1; i <= N_FILES; i++) {
      writeCsv(generator.generate(N_SUPERVISORS), dir.resolve("supervisors_" + i + ".csv"));
    }
    System.out.println("Wrote " + N_FILES + " files to " + dir);
  }
}
