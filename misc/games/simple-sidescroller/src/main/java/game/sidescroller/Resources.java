package game.sidescroller;

import lombok.SneakyThrows;

public class Resources {
    @SneakyThrows
    static void main(String[] args) {
        System.out.println(Resources.class.getResourceAsStream("/protag.png").readAllBytes().length);
        System.out.println(Resources.class.getResourceAsStream("/simple-background.png").readAllBytes().length);
    }
}
