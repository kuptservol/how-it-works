package ru.skuptsov.map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.skuptsov.concurrent.map.impl.LockFreeArrayConcurrentHashMap;
import ru.skuptsov.concurrent.map.impl.LockStripingArrayConcurrentHashMap;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Sergey Kuptsov
 * @since 04/04/2017
 */
public class MapTest {

    @DataProvider
    public Object[][] maps() {
        return new Object[][]{
                {new HashMap<>()},
                {new LockStripingArrayConcurrentHashMap<>(2)},
                {new LockFreeArrayConcurrentHashMap<>(2)}
        };
    }

    @Test(dataProvider = "maps")
    private void testMapSimpleGetPut(Map<TestObject, Integer> map) {
        TestObject key = new TestObject(1, 1);
        map.put(key, 1);

        assertEquals(map.get(key), new Integer(1));
        assertNull(map.get(new TestObject(0, 2)));

        assertEquals(map.size(), 1);
    }

    @Test(dataProvider = "maps")
    private void testMapAddMultiple(Map<TestObject, Integer> map) {

        for (int i = 0; i < 30; i++) {
            TestObject key = new TestObject(i, i);
            map.put(key, i);
        }

        assertEquals(map.size(), 30);

        for (int i = 0; i < 30; i++) {
            TestObject key = new TestObject(i, i);
            assertEquals(map.get(key), new Integer(i));
        }
    }

    @Test(dataProvider = "maps")
    private void testMapUpdate(Map<TestObject, Integer> map) {
        TestObject key = new TestObject(1, 1);
        map.put(key, 1);

        assertEquals(map.get(key), new Integer(1));

        map.put(key, 2);

        assertEquals(map.get(key), new Integer(2));

        assertEquals(map.size(), 1);
    }

    @Test(dataProvider = "maps")
    private void testMapSimpleGetPutSameBucket(Map<TestObject, Integer> map) {
        TestObject key1 = new TestObject(2, 2);
        TestObject key2 = new TestObject(1, 3);

        map.put(key1, 1);
        map.put(key2, 2);

        assertEquals(map.get(key1), new Integer(1));
        assertEquals(map.get(key2), new Integer(2));

        assertEquals(map.size(), 2);
    }

    @Test(dataProvider = "maps")
    private void testMapRemove(Map<TestObject, Integer> map) {
        TestObject key1 = new TestObject(2, 2);
        TestObject key2 = new TestObject(1, 3);

        map.put(key1, 1);
        map.put(key2, 2);

        assertEquals(map.get(key1), new Integer(1));
        assertEquals(map.get(key2), new Integer(2));

        assertEquals(map.size(), 2);

        map.remove(key1);

        assertEquals(map.get(key1), null);
        assertEquals(map.get(key2), new Integer(2));

        assertEquals(map.size(), 1);

        map.remove(key1);

        assertEquals(map.get(key1), null);
        assertEquals(map.get(key2), new Integer(2));

        assertEquals(map.size(), 1);

        map.remove(key2);

        assertEquals(map.get(key1), null);
        assertEquals(map.get(key2), null);

        assertEquals(map.size(), 0);
    }


    private static class TestObject {
        final int x;
        final int y;

        private TestObject(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            TestObject that = (TestObject) o;
            return x == that.x &&
                    y == that.y;
        }

        @Override
        public int hashCode() {
            return x + y;
        }
    }
}
