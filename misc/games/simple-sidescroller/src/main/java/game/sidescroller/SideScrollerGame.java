package game.sidescroller;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.*;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;

public class SideScrollerGame extends SimpleApplication implements ActionListener {

    private static final float BACKGROUND_SCROLL_RATIO = 1f;
    private static final float PLAYER_SCALE = 1.5f;
    private static final float GROUND_Y = 160f;
    private static final float SPEED = 240f;
    private static final float JUMP = 520f;
    private static final float GRAVITY = 1400f;
    private Screen screen = Screen.GAMEPLAY;
    private Geometry player;
    private Geometry bg1, bg2;
    private Node creditsNode;
    private float playerX = 300;
    private float playerY = 160;
    private float velocityY = 0;
    private boolean left, right;
    private boolean facingRight = true;
    private float animTime = 0;
    private int animFrame = 1;

    public static void main(String[] args) {
        SideScrollerGame app = new SideScrollerGame();

        AppSettings settings = new AppSettings(true);
        settings.setTitle("Simple Side Scroller");
        settings.setResolution(1280, 720);

        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);

        setupInput();
        setupBackground();
        setupPlayer();
        setupCredits();
    }

    private void setupInput() {
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);

        inputManager.addMapping("Left",
                new KeyTrigger(KeyInput.KEY_A),
                new KeyTrigger(KeyInput.KEY_LEFT));

        inputManager.addMapping("Right",
                new KeyTrigger(KeyInput.KEY_D),
                new KeyTrigger(KeyInput.KEY_RIGHT));

        inputManager.addMapping("Jump",
                new KeyTrigger(KeyInput.KEY_SPACE));

        inputManager.addMapping("Escape",
                new KeyTrigger(KeyInput.KEY_ESCAPE));

        inputManager.addMapping("Click",
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addListener(this, "Left", "Right", "Jump", "Escape", "Click");
    }

    private void setupBackground() {
        Texture bgTex = assetManager.loadTexture("simple-background.png");

        bg1 = quadHelper("bg1", bgTex, (float) cam.getWidth(), (float) cam.getHeight());
        bg2 = quadHelper("bg2", bgTex, (float) cam.getWidth(), (float) cam.getHeight());

        bg1.setLocalTranslation(0, 0, -10);
        bg2.setLocalTranslation(cam.getWidth(), 0, -10);

        guiNode.attachChild(bg1);
        guiNode.attachChild(bg2);
    }

    private void setupPlayer() {
        Texture spriteSheet = assetManager.loadTexture("protag.png");

        int sheetW = spriteSheet.getImage().getWidth();
        int sheetH = spriteSheet.getImage().getHeight();

        float frameW = sheetW / 3f;
        float frameH = sheetH / 4f;

        var geom = quadHelper("player", spriteSheet, frameW * PLAYER_SCALE, frameH * PLAYER_SCALE);
        setSpriteFrame(geom, 1, 2);
        player = geom;

        player.setLocalTranslation(playerX, playerY, 10);
        guiNode.attachChild(player);
    }

    private void setupCredits() {
        creditsNode = new Node(Screen.CREDITS.name());

        Geometry panel = new Geometry("creditsPanel", new Quad(cam.getWidth(), cam.getHeight()));

        Material panelMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        panelMat.setColor("Color", ColorRGBA.Black);
        panel.setMaterial(panelMat);
        panel.setLocalTranslation(0, 0, 15);

        var creditsText = new BitmapText(guiFont);
        creditsText.setText("Credits\n\nSimple Side Scroller\nBuilt with jMonkeyEngine 3");
        creditsText.setSize(42);
        creditsText.setColor(ColorRGBA.White);
        creditsText.setLocalTranslation(390, 500, 20);

        var backButton = new BitmapText(guiFont);
        backButton.setText("[ Back to Game ]");
        backButton.setSize(34);
        backButton.setColor(ColorRGBA.White);
        backButton.setLocalTranslation(480, 260, 20);

        creditsNode.attachChild(panel);
        creditsNode.attachChild(creditsText);
        creditsNode.attachChild(backButton);
    }

    private Geometry quadHelper(String name, Texture texture, float w, float h) {
        Quad quad = new Quad(w, h);
        Geometry geom = new Geometry(name, quad);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", texture);
        mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);

        geom.setMaterial(mat);
        return geom;
    }

    private void setSpriteFrame(Geometry geom, int col, int row) {
        Mesh mesh = geom.getMesh();

        float cols = 3f;
        float rows = 4f;

        float u0 = col / cols;
        float u1 = (col + 1) / cols;

        // jME texture origin is bottom-left, so invert row index.
        float v1 = 1f - row / rows;
        float v0 = 1f - (row + 1) / rows;

        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(
                u0, v0,
                u1, v0,
                u1, v1,
                u0, v1
        ));

        mesh.updateBound();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (screen == Screen.GAMEPLAY) {
            float dx = 0;

            if (left) {
                dx -= SPEED * tpf;
                facingRight = false;
            }

            if (right) {
                dx += SPEED * tpf;
                facingRight = true;
            }

            playerX += dx;

            velocityY -= GRAVITY * tpf;
            playerY += velocityY * tpf;

            if (playerY <= GROUND_Y) {
                playerY = GROUND_Y;
                velocityY = 0;
            }

            updateAnimation(tpf, dx != 0);
            updateBackground();

            player.setLocalTranslation(playerX, playerY, 10);
        } else {

        }
    }

    private void updateAnimation(float tpf, boolean walking) {
        int row = facingRight ? 2 : 1;

        if (walking) {
            animTime += tpf;

            if (animTime >= 0.12f) {
                animTime = 0;
                animFrame = (animFrame + 1) % 3;
            }
        } else {
            animFrame = 1;
        }

        setSpriteFrame(player, animFrame, row);
    }

    private void updateBackground() {
        float w = cam.getWidth();

        float offset = -(playerX * BACKGROUND_SCROLL_RATIO) % w;
        if (offset > 0) {
            offset -= w;
        }

        bg1.setLocalTranslation(offset, 0, -10);
        bg2.setLocalTranslation(offset + w, 0, -10);
    }

    private void toggleCredits() {
        if (screen == Screen.CREDITS) {
            creditsNode.removeFromParent();
            guiNode.attachChild(player);
            screen = Screen.GAMEPLAY;
        } else {
            guiNode.attachChild(creditsNode);
            player.removeFromParent();
            screen = Screen.CREDITS;
        }
    }

    @Override
    public void onAction(String name, boolean pressed, float tpf) {
        if ("Left".equals(name)) left = pressed;
        if ("Right".equals(name)) right = pressed;

        if ("Jump".equals(name) && pressed && screen == Screen.GAMEPLAY && playerY <= GROUND_Y) {
            velocityY = JUMP;
        }

        if ("Escape".equals(name) && pressed) {
            toggleCredits();
        }

        if ("Click".equals(name) && pressed && screen == Screen.CREDITS) {
            Vector2f mouse = inputManager.getCursorPosition();

            if (mouse.x >= 460 && mouse.x <= 800 && mouse.y >= 220 && mouse.y <= 280) {
                toggleCredits();
            }
        }
    }

    private enum Screen { GAMEPLAY, CREDITS }
}
