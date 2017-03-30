package ru.skuptsov.concurrent.map.test;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IntResult1;
import org.openjdk.jcstress.infra.results.LongResult2;

import java.util.HashMap;
import java.util.Map;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

/**
 * @author Sergey Kuptsov
 * @since 30/03/2017
 */
public class ConcurrentMapTest {

    @State
    public static class MapState {
        final Map<String, Integer> map = new HashMap<>(3);
//        final Map<String, Integer> map = new Hashtable<>(3);
//        final Map<String, Integer> map = Collections.synchronizedMap(new HashMap<>(3));
//        final Map<String, Integer> map = new ConcurrentHashMap<>(3)

    }

    @JCStressTest
    @Description("Test race map get and put")
    @Outcome(id = "0, 1", expect = ACCEPTABLE, desc = "return 0L and 1L")
    @Outcome(expect = FORBIDDEN, desc = "Case violating atomicity.")
    public static class MapPutGetTest {

        @Actor
        public void actor1(MapState state, LongResult2 result) {
            state.map.put("A", 0);
            Integer r = state.map.get("A");
            result.r1 = (r == null ? -1 : r);
        }

        @Actor
        public void actor2(MapState state, LongResult2 result) {
            state.map.put("B", 1);
            Integer r = state.map.get("B");
            result.r2 = (r == null ? -1 : r);
        }
    }

    @JCStressTest
    @Description("Test race map check size")
    @Outcome(id = "2", expect = ACCEPTABLE, desc = "size of map = 2 ")
    @Outcome(id = "1", expect = FORBIDDEN, desc = "size of map = 1 is race")
    @Outcome(expect = FORBIDDEN, desc = "Case violating atomicity.")
    public static class MapSizeTest {

        @Actor
        public void actor1(MapState state) {
            state.map.put("A", 0);
        }

        @Actor
        public void actor2(MapState state) {
            state.map.put("B", 0);
        }

        @Arbiter
        public void arbiter(MapState state, IntResult1 result) {
            result.r1 = state.map.size();
        }
    }

    @JCStressTest
    @Description("Test concurrent resize map check size")
    @Outcome(id = "4", expect = ACCEPTABLE, desc = "acceptable resize")
    @Outcome(expect = FORBIDDEN, desc = "Case violating atomicity.")
    public static class MapResizeTest {

        @Actor
        public void actor1(MapState state) {
            state.map.put("A", 0);
        }

        @Actor
        public void actor2(MapState state) {
            state.map.put("B", 0);
        }

        @Actor
        public void actor3(MapState state) {
            state.map.put("C", 0);
        }

        @Actor
        public void actor4(MapState state) {
            state.map.put("D", 0);
        }

        @Arbiter
        public void arbiter(MapState state, IntResult1 result) {
            result.r1 = state.map.size();
        }
    }
}
