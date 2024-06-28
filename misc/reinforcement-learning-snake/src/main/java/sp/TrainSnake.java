package sp;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import picocli.CommandLine;

import java.util.*;
import java.util.stream.IntStream;

@CommandLine.Command(mixinStandardHelpOptions = true)
public class TrainSnake implements Runnable {
    @CommandLine.Parameters(defaultValue = "", description = "one of 'play', 'train'")
    String command = "";

    public static void main(String[] args) {
        // args = new String[]{"play"};
        System.exit(new CommandLine(new TrainSnake()).execute(args));
    }

    @Override
    public void run() {
        switch (command) {
            case "play" -> play();
            case "train" -> train();
            case "" -> throw new RuntimeException("need a command - but it was empty");
            default -> throw new RuntimeException("unknown command: " + command);
        }
    }

    public void play() {
        var settings = new AppSettings(true);
        settings.setTitle("Snake Game - Player");


    }

    public void train() {
        var settings = new AppSettings(true);
        settings.setTitle("Snake Q-learning");

        var app = new SimpleApplication() {
            @Override
            public void simpleInitApp() {
            }

            @SuppressWarnings("unused")
            public void simpleInitApp_demoFromWebsite() {
                Box b = new Box(1, 1, 1);
                Geometry geom = new Geometry("Box", b);
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.setColor("Color", ColorRGBA.Blue);
                geom.setMaterial(mat);

                rootNode.attachChild(geom);
            }
        };

        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();

        Config config = new Config();

        var q = QLearning.initializeQTable();

        var maxLength = 0;
        var movingAverage = new ArrayDeque<Integer>();

        for (int episode = 0; episode < config.getEpisodes(); episode++) {
            var game = new SnakeGame(app, config);
            var state = game.reset();
            var done = false;

            var verbose = episode % config.getRenderEvery() == 0;

            while (!done) {
                int action;
                if (SnakeGame.RANDOM.nextDouble(0, 1) < config.getEpsilon()) {
                    // explore
                    action = game.randomChoice(List.of(1, 2, 3, 4));
                } else {
                    // exploit
                    SnakeGame.State finalState = state;
                    action = IntStream.range(0, 4)
                            .mapToObj(a -> Map.entry(a, q.getQValue(finalState, a)))
                            .max(Map.Entry.comparingByValue()).orElseThrow()
                            .getKey();
                }

                SnakeGame.Step step = game.step(action);
                done = step.done();
                if (verbose) {
                    System.out.printf("\tReward: %.4f%n", step.reward());
                }

                q.updateQValue(config, state, action, step);

                state = step.nextState();

                if (verbose) {
                    game.render(null);
                    game.waitFor(25);
                }
            }

            var snakeLength = game.snake.length;
            movingAverage.addFirst(snakeLength);
            while (movingAverage.size() > 200)
                movingAverage.removeLast();
            var avg = movingAverage.stream().mapToInt(Integer::intValue).average().orElse(0.0);

            if (snakeLength > maxLength)
                maxLength = snakeLength;

            System.out.printf("Episode %s/%s completed, length=%s,avg=%.2f max=%s%n",
                    episode + 1,
                    config.getEpisodes(),
                    snakeLength,
                    avg,
                    maxLength);
        }
    }

}
