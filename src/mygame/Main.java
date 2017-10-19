package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.effect.ParticleEmitter;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication implements ActionListener, AnimEventListener {

    private BulletAppState bulletAppState;
    Spatial te;
    Node pacman;
    CharacterControl character;
    RigidBodyControl rbc;
    RigidBodyControl rbcO;
    boolean left = false, right = false, up = false, down = false, CamRight = false, CamLeft = false;
    Vector3f walkDirection = new Vector3f();
    float airTime = 0;
    ChaseCamera chaseCam;
    //bullet
    Sphere bullet;
    SphereCollisionShape bulletCollisionShape;
    //explosion
    ParticleEmitter effect;
    //brick wall
    Box brick;
    float bLength = 2f;
    float bWidth = 2f;
    float bHeight = 2f;
    FilterPostProcessor fpp;
    AnimChannel animationChannel;
    AnimChannel shootingChannel;
    AnimControl animationControl;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);

        setupKeys();
        createCharacter();
        setupChaseCamera();

        te = assetManager.loadModel("Materials/Maze/Maze.j3o");
        te.setLocalScale(3f);
        te.setLocalScale(1f, 2f, 1f);
        /**
         * oto = (Node) assetManager.loadModel("Models/Pac-Man/PacmanHigh.obj");
         * d * CollisionShape sceneShape =
         * CollisionShapeFactory.createDynamicMeshShape(te); rbc = new
         * RigidBodyControl(sceneShape, 0); te.addControl(rbc);
         *
         * /**CollisionShape sceneShapeO =
         * CollisionShapeFactory.createDynamicMeshShape(oto); rbcO = new
         * RigidBodyControl(sceneShapeO, 0.5f); oto.addControl(rbcO);
         */
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);



        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);

        getPhysicsSpace().add(te);

        flyCam.setMoveSpeed(100f);
        
        rootNode.addLight(sun);

        rootNode.attachChild(te);
    }

    private void setupKeys() {

        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("CharUp", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CharDown", new KeyTrigger(KeyInput.KEY_S));

        inputManager.addListener(this, "CharLeft");
        inputManager.addListener(this, "CharRight");
        inputManager.addListener(this, "CharUp");
        inputManager.addListener(this, "CharDown");

    }

    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }

    private void setupChaseCamera() {
        flyCam.setEnabled(false);
        chaseCam = new ChaseCamera(cam, pacman, inputManager);
        
        
    }

    private void setupAnimationController() {
        animationControl = pacman.getControl(AnimControl.class);
        animationControl.addListener(this);
        animationChannel = animationControl.createChannel();
        shootingChannel = animationControl.createChannel();

    }

    private void createCharacter() {
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(3f, 4f);
        character = new CharacterControl(capsule, 0.01f);
        pacman = (Node) assetManager.loadModel("Models/Pac-Man/PacmanHigh.obj");
        pacman.setLocalScale(7f);
        pacman.addControl(character);
        pacman.setLocalRotation(new Quaternion(0, 90, 0, 1));
        pacman.setLocalTranslation(-0.28f, 4, 0);
        
        //character.setPhysicsLocation(new Vector3f(-140, 40, -10));
        character.setPhysicsLocation(new Vector3f(-0.28f, 4, 0));
        rootNode.attachChild(pacman);
        getPhysicsSpace().add(character);

    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(0.1f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.1f);
        camDir.y = 0;
        camLeft.y = 0;
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        if (!character.onGround()) {
            airTime = airTime + tpf;
        } else {
            airTime = 0;
        }
//        if (walkDirection.length() == 0) {
//            if (!"stand".equals(animationChannel.getAnimationName())) {
//                animationChannel.setAnim("stand", 1f);
//            }
//        } else {
            character.setViewDirection(walkDirection);
//            if (airTime > .3f) {
//                if (!"stand".equals(animationChannel.getAnimationName())) {
//                    animationChannel.setAnim("stand");
//                }
//            } else if (!"Walk".equals(animationChannel.getAnimationName())) {
//                animationChannel.setAnim("Walk", 0.7f);
//            }
//        }
        character.setWalkDirection(walkDirection);
        
    }

    public void onAction(String name, boolean value, float tpf) {

        if (name.equals("CharLeft")) {
            if (value) {
                left = true;
            } else {
                left = false;
            }
        } else if (name.equals("CharRight")) {
            if (value) {
                right = true;
            } else {
                right = false;
            }
        } else if (name.equals("CharUp")) {
            if (value) {
                up = true;
            } else {
                up = false;
            }
        } else if (name.equals("CharDown")) {
            if (value) {
                down = true;
            } else {
                down = false;
            }
        }
    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
