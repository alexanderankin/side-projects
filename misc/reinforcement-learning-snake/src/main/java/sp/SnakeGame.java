package sp;

import com.jme3.app.SimpleApplication;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class SnakeGame {
    static final List<int[]> DIRECTION_CHOICES = Arrays.asList(new int[][]{{0, -1}, {0, 1}, {-1, 0}, {1, 0}});
    static final Random RANDOM = new Random();

    final SimpleApplication app;
    final Config config;
    int[][] snake;
    int[] direction;
    int[] food;
    boolean done;
    int previousDistance;

    State reset() {
        snake = new int[][]{{config.getWidth() / 2, config.getHeight() / 2}};
        direction = randomChoice(DIRECTION_CHOICES);
        food = placeFood();
        previousDistance = getDistanceToFood();
        return getState();
    }

    @SuppressWarnings("SameParameterValue")
    <T> T randomChoice(List<T> directionChoices) {
        return directionChoices.get(RANDOM.nextInt(directionChoices.size()));
    }

    private int[] placeFood() {
        while (true) {
            var x = RANDOM.nextInt((config.getWidth() / config.getCellSize()) - 1) * config.getCellSize();
            var y = RANDOM.nextInt((config.getHeight() / config.getCellSize()) - 1) * config.getCellSize();

            if (xyNotInSnake(x, y))
                return new int[]{x, y};
        }
    }

    private boolean xyNotInSnake(int x, int y) {
        for (int[] i : snake) {
            if (i[0] == x && i[1] == y) {
                return true;
            }
        }

        return false;
    }

    int getDistanceToFood() {
        var head = snake[0];
        return Math.abs(head[0] - food[0]) + Math.abs(head[1] - food[1]);
    }

    State getState() {
        var head = snake[0];

        // # Distance to walls
        var distance_to_left = head[0] / config.getCellSize();
        var distance_to_right = (config.getWidth() - head[0]) / config.getCellSize();
        var distance_to_top = head[1] / config.getCellSize();
        var distance_to_bottom = (config.getHeight() - head[1]) / config.getCellSize();

        // # Relative position of the food to the head
        var food_rel_x = (food[0] - head[0]) / config.getCellSize();
        var food_rel_y = (food[1] - head[1]) / config.getCellSize();

        // # Check for tail in adjacent cells
        var adjacent_cells = new int[][]{
                {head[0], head[1] - config.getCellSize()},  // # Up
                {head[0], head[1] + config.getCellSize()},  // # Down
                {head[0] - config.getCellSize(), head[1]},  // # Left
                {head[0] + config.getCellSize(), head[1]}   // # Right
        };

        var tail_state = 0;
        for (int i = 0; i < adjacent_cells.length; i++) {
            var cellInSnake = false;

            for (int i1 = 1; i1 < snake.length; i1++) {
                if (snake[i1][0] == adjacent_cells[i][0] &&
                    snake[i1][1] == adjacent_cells[i][1]) {
                    cellInSnake = true;
                    break;
                }
            }

            if (cellInSnake) {
                tail_state |= (1 << (3 - i));
            }
        }

        return new State(
                abslogint(distance_to_left),
                abslogint(distance_to_right),
                abslogint(distance_to_top),
                abslogint(distance_to_bottom),
                abslogint(food_rel_x),
                abslogint(food_rel_y),
                tail_state
        );
    }

    double abslogint(int input) {
        if (input == 0) return 0;
        else return Math.copySign(1, input) * Math.abs(1 + 2 * Math.log(Math.abs(input)));
    }

    Step step(int action) {
        var newDirection = direction;
        if (action == 0) {  // up
            newDirection = new int[]{0, -1};
        } else if (action == 1) {  // down
            newDirection = new int[]{0, 1};
        } else if (action == 2) {  // left
            newDirection = new int[]{-1, 0};
        } else if (action == 3) {  // right
            newDirection = new int[]{1, 0};
        }

        // Check if the new direction would cause the snake to move backwards
        if (snake.length > 1) {
            var head = snake[0];
            var neck = snake[1];

            int x = head[0] + newDirection[0] * config.getCellSize();
            int y = head[1] + newDirection[1] * config.getCellSize();

            if (x == neck[0] && y == neck[1])
                // # If moving backwards, keep the current direction
                newDirection = direction;
        }

        direction = newDirection;

        var head = snake[0];
        var newHead = new int[]{
                head[0] + direction[0] * config.getCellSize(),
                head[1] + direction[1] * config.getCellSize()
        };

        // Check for collision with walls or self
        if (!xyNotInSnake(newHead[0], newHead[1]) ||
            newHead[0] < 0 || newHead[0] >= config.getWidth() ||
            newHead[1] < 0 || newHead[1] >= config.getHeight()) {
            done = true;
            return new Step(
                    getState(),
                    -100,
                    done
            );
        }

        // self.snake.insert(0, new_head)
        {
            var tmp = new int[snake.length + 1][];
            System.arraycopy(snake, 0, tmp, 1, snake.length);
            snake = tmp;
        }
        snake[0] = newHead;

        int reward;
        if (newHead[0] == food[0] && newHead[1] == food[1]) {
            reward = 10;
            food = placeFood();
            previousDistance = getDistanceToFood();
        } else {
            // self.snake.pop()
            snake = Arrays.copyOfRange(snake, 0, snake.length - 1);
            var newDistance = getDistanceToFood();
            if (newDistance < previousDistance) {
                reward = 1; // Small positive reward for moving towards food
            } else {
                reward = -2; // Small negative reward for moving away from food
            }
            previousDistance = newDistance;
        }

        return new Step(
                getState(),
                reward,
                done
        );
    }

    public void render(Graphics g) {
        if (g == null) return;

        Color color = g.getColor();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, config.getWidth(), config.getHeight());

        g.setColor(Color.GREEN);
        for (int[] segment : snake) {
            g.fillRect(segment[0], segment[1], config.getCellSize(), config.getCellSize());
        }
        g.setColor(Color.RED);
        g.fillRect(food[0], food[1], config.getCellSize(), config.getCellSize());

        g.setColor(color);
    }

    public void waitFor(long i) {
    }

    record State(
            Object distance_to_left,
            Object distance_to_right,
            Object distance_to_top,
            Object distance_to_bottom,
            Object food_rel_x,
            Object food_rel_y,
            Object tail_state
    ) {
    }

    record Step(
            State nextState,
            double reward,
            boolean done
    ) {
    }
}
