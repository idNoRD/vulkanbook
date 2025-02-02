package org.vulkanb;

import org.apache.logging.log4j.*;
import org.joml.*;
import org.vulkanb.eng.*;
import org.vulkanb.eng.graph.Render;
import org.vulkanb.eng.scene.*;

import java.lang.Math;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 10.0f / 1E9f;

    private float angleInc;
    private Entity bobEntity;
    private Light directionalLight;
    private float lightAngle = 90.1f;
    private int maxFrames = 0;

    public static void main(String[] args) {
        LOGGER.info("Starting application");
        Engine engine = new Engine("Vulkan Book", new Main());
        engine.start();
    }

    @Override
    public void cleanup() {
        // To be implemented
    }

    @Override
    public void handleInput(Window window, Scene scene, long diffTimeMills, boolean inputConsumed) {
        float move = diffTimeMills * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        if (window.isKeyPressed(GLFW_KEY_W)) {
            camera.moveForward(move);
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            camera.moveBackwards(move);
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            camera.moveLeft(move);
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            camera.moveRight(move);
        }
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            camera.moveUp(move);
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            camera.moveDown(move);
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            angleInc -= 0.05f;
            scene.setLightChanged(true);
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            angleInc += 0.05f;
            scene.setLightChanged(true);
        } else {
            angleInc = 0;
            scene.setLightChanged(false);
        }

        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            bobEntity.getEntityAnimation().setStarted(!bobEntity.getEntityAnimation().isStarted());
        }

        MouseInput mouseInput = window.getMouseInput();
        if (mouseInput.isRightButtonPressed()) {
            Vector2f displVec = mouseInput.getDisplVec();
            camera.addRotation((float) Math.toRadians(-displVec.x * MOUSE_SENSITIVITY),
                    (float) Math.toRadians(-displVec.y * MOUSE_SENSITIVITY));
        }

        lightAngle += angleInc;
        if (lightAngle < 0) {
            lightAngle = 0;
        } else if (lightAngle > 180) {
            lightAngle = 180;
        }
        updateDirectionalLight();

        Entity.EntityAnimation entityAnimation = bobEntity.getEntityAnimation();
        if (entityAnimation != null && entityAnimation.isStarted()) {
            int currentFrame = Math.floorMod(entityAnimation.getCurrentFrame() + 1, maxFrames);
            entityAnimation.setCurrentFrame(currentFrame);
        }
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        List<ModelData> modelDataList = new ArrayList<>();

        String sponzaModelId = "sponza-model";
        ModelData sponzaModelData = ModelLoader.loadModel(sponzaModelId, "resources/models/sponza/Sponza.gltf",
                "resources/models/sponza", false);
        modelDataList.add(sponzaModelData);
        Entity sponzaEntity = new Entity("SponzaEntity", sponzaModelId, new Vector3f(0.0f, 0.0f, 0.0f));
        scene.addEntity(sponzaEntity);

        String bobModelId = "bob-model";
        ModelData bobModelData = ModelLoader.loadModel(bobModelId, "resources/models/bob/boblamp.md5mesh",
                "resources/models/bob", true);
        maxFrames = bobModelData.getAnimationsList().get(0).frames().size();
        modelDataList.add(bobModelData);
        bobEntity = new Entity("BobEntity", bobModelId, new Vector3f(0.0f, 0.0f, 0.0f));
        bobEntity.setScale(0.04f);
        bobEntity.getRotation().rotateY((float) Math.toRadians(-90.0f));
        bobEntity.updateModelMatrix();
        bobEntity.setEntityAnimation(new Entity.EntityAnimation(true, 0, 0));
        scene.addEntity(bobEntity);

        render.loadModels(modelDataList);

        Camera camera = scene.getCamera();
        camera.setPosition(-6.0f, 2.0f, 0.0f);
        camera.setRotation((float) Math.toRadians(20.0f), (float) Math.toRadians(90.f));

        scene.getAmbientLight().set(0.2f, 0.2f, 0.2f, 1.0f);
        List<Light> lights = new ArrayList<>();
        directionalLight = new Light();
        directionalLight.getColor().set(1.0f, 1.0f, 1.0f, 1.0f);
        lights.add(directionalLight);
        updateDirectionalLight();

        Light[] lightArr = new Light[lights.size()];
        lightArr = lights.toArray(lightArr);
        scene.setLights(lightArr);
    }

    private void updateDirectionalLight() {
        float zValue = (float) Math.cos(Math.toRadians(lightAngle));
        float yValue = (float) Math.sin(Math.toRadians(lightAngle));
        Vector4f lightDirection = directionalLight.getPosition();
        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();
        lightDirection.w = 0.0f;
    }
}
