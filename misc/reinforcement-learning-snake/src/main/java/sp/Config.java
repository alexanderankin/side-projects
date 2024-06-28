package sp;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Config {
    //<editor-fold desc="Screen dimensions">
    /**
     * Width of the screen
     */
    private int width = 400;

    /**
     * Height of the screen
     */
    private int height = 400;

    /**
     * Size of each cell
     */
    private int cellSize = 10;
    //</editor-fold>

    //<editor-fold desc="Colors">
    /**
     * Black color RGB
     */
    private int[] black = {0, 0, 0};

    /**
     * White color RGB
     */
    private int[] white = {255, 255, 255};

    /**
     * Red color RGB
     */
    private int[] red = {255, 0, 0};

    /**
     * Green color RGB
     */
    private int[] green = {0, 255, 0};
    //</editor-fold>

    //<editor-fold desc="Q-learning parameters">
    /**
     * Learning rate for Q-learning
     */
    private double alpha = 0.1;

    /**
     * Discount factor for Q-learning
     */
    private double gamma = 0.9;

    /**
     * Exploration rate in Q-learning
     */
    private double epsilon = 0.001;

    /**
     * Number of episodes for training
     */
    private int episodes = 1_000_000;
    //</editor-fold>

    /**
     * Length of snake in learning mode
     */
    private int snakeLearningLen = 10;

    /**
     * Frequency of rendering frames
     */
    private int renderEvery = 5_000;
}
