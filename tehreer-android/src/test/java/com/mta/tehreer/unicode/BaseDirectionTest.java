package com.mta.tehreer.unicode;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BaseDirectionTest {
    @Test
    public void testValues() {
        assertEquals(BaseDirection.LEFT_TO_RIGHT.value, 0);
        assertEquals(BaseDirection.RIGHT_TO_LEFT.value, 1);
        assertEquals(BaseDirection.DEFAULT_LEFT_TO_RIGHT.value, 0xFE);
        assertEquals(BaseDirection.DEFAULT_RIGHT_TO_LEFT.value, 0xFD);
    }
}
