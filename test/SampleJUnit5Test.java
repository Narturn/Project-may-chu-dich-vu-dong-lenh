package test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SampleJUnit5Test {

    @Test
    void testSanityCalculation() {
        int expected = 4;
        int actual = 2 + 2;
        assertEquals(expected, actual, "Phep tinh 2 + 2 phai bang 4");
    }
}

