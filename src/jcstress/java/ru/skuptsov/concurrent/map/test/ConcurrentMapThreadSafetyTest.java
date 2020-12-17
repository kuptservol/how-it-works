package ru.skuptsov.concurrent.map.test;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;
import org.openjdk.jcstress.infra.results.JJ_Result;
import ru.skuptsov.concurrent.map.impl.LockFreeArrayConcurrentHashMap;

import java.util.Map;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

public class ConcurrentMapThreadSafetyTest {

    @State
    public static class MapState {
        final Map<String, Integer> map = new LockFreeArrayConcurrentHashMap<>(3);

    }

    @JCStressTest
    @Description("Test race map get and put")
    @Outcome(id = "0, 1", expect = ACCEPTABLE, desc = "return 0L and 1L")
    @Outcome(expect = FORBIDDEN, desc = "Case violating atomicity.")
    public static class MapPutGetTest {

        @Actor
        public void actor1(MapState state, JJ_Result result) {
            state.map.put("A", 0);
            Integer r = state.map.get("A");
            result.r1 = (r == null ? -1 : r);
        }

        @Actor
        public void actor2(MapState state, JJ_Result result) {
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
        public void arbiter(MapState state, I_Result result) {
            result.r1 = state.map.size();
        }
    }
}