package me.paulschwarz.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class ArgsTest {

  @Test
  public void testGetString() {
    Args args = new Args(Arrays.asList("--output=save", "--jobs=100"));

    assertEquals("save", args.get("output", "save"));
    assertEquals("default", args.get("missing", "default"));
  }

  @Test
  public void testGetInteger() {
    Args args = new Args(Arrays.asList("--output=save", "--jobs=100", "--invalid=x"));

    assertEquals(100, args.get("jobs", 100));
    assertEquals(500, args.get("missing", 500));
  }

  @Test(expected = RuntimeException.class)
  public void testGetIntegerNumberFormatException() {
    Args args = new Args(Arrays.asList("--output=save", "--jobs=100", "--invalid=x"));

    assertEquals(0, args.get("invalid", 500));
  }
}
