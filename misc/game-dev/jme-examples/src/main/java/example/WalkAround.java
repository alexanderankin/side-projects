package example;

import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.RectangleMesh;
import com.jme3.system.AppSettings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WalkAround extends SimpleApplication {
    public static void main(String[] args) {
        var game = new WalkAround();
        AppSettings settings = new AppSettings(true);
        game.setSettings(settings);
        game.setShowSettings(false);
        game.setDisplayFps(false);
        game.setDisplayStatView(false);
        game.start();
    }

    @Override
    public void simpleInitApp() {
        // stateManager.attach(new ResetStatsState());

        var square = new Quad(10, 10);

        Geometry walker = getGeoWalker(square);
        rootNode.attachChild(walker);

        // PointLight myLight = new PointLight();
        // rootNode.addLight(myLight);
        // LightControl lightControl = new LightControl(myLight);
        // geom.addControl(lightControl); // this spatial controls the position of this light.

        // cam.setLocation(new Vector3f(10, -5, 30));
        // cam.setLocation(new Vector3f(-30, 30, 60));
        cam.setLocation(new Vector3f(1, 30, 90));
        cam.setRotation(new Quaternion(0, .99f, -.1f, 0));
        flyCam.setMoveSpeed(30);
        flyCam.setEnabled(false);
        // flyCam.setRotationSpeed(0);
        // flyCam.setZoomSpeed(0);

        //
        // var chaseCam = new ChaseCamera(cam, walker, inputManager);
        // chaseCam.setRotationSpeed(0);

        rootNode.attachChild(getGeoGround());
        // cam.setRotation(new Quaternion(0, 1, 0, 0));
        // cam.setAxes(
        //         new Vector3f(0f, 0f, 0f),
        //         new Vector3f(0f, 0f, 0f),
        //         new Vector3f(0f, 0f, 0f)
        // );
        // System.out.println(cam.getLocation());
    }

    private Geometry getGeoGround() {
        Material mat_ground = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_ground.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
        Geometry ground = new Geometry("ground", new RectangleMesh(
                new Vector3f(-25, -1, 25),
                new Vector3f(25, -1, 25),
                new Vector3f(-25, -1, -25)));
        ground.setMaterial(mat_ground);
        return ground;
    }

    private Geometry getGeoWalker(Quad square) {
        var mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.fromRGBA255(180, 23, 18, 0));
        var geom = new Geometry("geometry", square, mat);
        geom.setMesh(square);
        return geom;
    }

    int counter = 0;

    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);

        if (log.isTraceEnabled() && counter % 100000 == 0) {
            log.info("camera location is {}, rotation is x/{} y/{} z/{} w/{}", cam.getLocation(),
                    Math.round(cam.getRotation().getX() * 1000),
                    Math.round(cam.getRotation().getY() * 1000),
                    Math.round(cam.getRotation().getZ() * 1000),
                    Math.round(cam.getRotation().getW() * 1000)
            );
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        super.simpleRender(rm);
    }
}
