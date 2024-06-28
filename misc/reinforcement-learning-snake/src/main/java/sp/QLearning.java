package sp;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

class QLearning {
    static Q initializeQTable() {
        return new Q();
    }

    static class Q {
        private final Map<Integer, Double> data = new HashMap<>();

        public double getQValue(SnakeGame.State state, int a) {
            return data.getOrDefault(Objects.hash(state, a), 0.0);
        }

        public double updateQValue(Config config, SnakeGame.State state, int action, SnakeGame.Step step) {
            double oldValue = getQValue(state, action);
            var nextMax = IntStream.range(0, 4)
                    .mapToDouble(a -> getQValue(state, a))
                    .max().orElseThrow();
            var newValue = oldValue + config.getAlpha() * (step.reward() + config.getGamma() * nextMax - oldValue);

            data.put(Objects.hash(state, action), newValue);
            return newValue;
        }
    }
}
