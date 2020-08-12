//package com.nick.wood.game_engine.examples;
//
//import com.nick.wood.event_bus.DebugSubscribable;
//import com.nick.wood.game_engine.core.GameLoop;
//import com.nick.wood.game_engine.model.object_builders.CameraBuilder;
//import com.nick.wood.game_engine.model.object_builders.GeometryBuilder;
//import com.nick.wood.game_engine.model.object_builders.LightingBuilder;
//import com.nick.wood.game_engine.model.types.GeometryType;
//import com.nick.wood.game_engine.model.types.LightingType;
//import com.nick.wood.game_engine.model.types.SkyboxType;
//import com.nick.wood.game_engine.model.utils.Creation;
//import com.nick.wood.game_engine.model.utils.GameObjectUtils;
//import com.nick.wood.game_engine.io.GameSer;
//import com.nick.wood.game_engine.io.IO;
//import com.nick.wood.game_engine.io.OI;
//import com.nick.wood.game_engine.io.ser.SceneGraphNodeSer;
//import com.nick.wood.game_engine.model.game_objects.*;
//import com.nick.wood.game_engine.systems.control.DirectTransformController;
//import com.nick.wood.graphics_library.Shader;
//import com.nick.wood.graphics_library.WindowInitialisationParametersBuilder;
//import com.nick.wood.graphics_library.lighting.Fog;
//import com.nick.wood.graphics_library.objects.render_scene.Scene;
//import com.nick.wood.maths.objects.QuaternionF;
//import com.nick.wood.maths.objects.srt.Transform;
//import com.nick.wood.maths.objects.srt.TransformBuilder;
//import com.nick.wood.maths.objects.vector.Vec3f;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.UUID;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class IOExample {
//
//	// this is to get world in sensible coordinate system to start with
//	private static final QuaternionF quaternionX = QuaternionF.RotationX((float) Math.toRadians(-90));
//	private static final QuaternionF quaternionY = QuaternionF.RotationY((float) Math.toRadians(180));
//	private static final QuaternionF quaternionZ = QuaternionF.RotationZ((float) Math.toRadians(90));
//	private static final QuaternionF cameraRotation = quaternionZ.multiply(quaternionY).multiply(quaternionX);
//
//	public static void main(String[] args) {
//		create();
//		test();
//	}
//
//	static public void test() {
//		// load in state
//		OI oi = new OI();
//		GameSer gameSer = oi.load("TestMin.json");
//
//		oi.link(gameSer);
//
//		// create map of tags to uuid to be used later with behaviours
//		HashMap<String, UUID> tagUUIDMap = new HashMap<>();
//
//		for (SceneGraphNodeSer sceneGraphNodeSer : gameSer.getSceneGraphNodeSers()) {
//
//			tagUUIDMap.put(sceneGraphNodeSer.getTag(), sceneGraphNodeSer.getUuid());
//
//			createMap(tagUUIDMap, sceneGraphNodeSer);
//
//		}
//
//		ArrayList<GameObject> gameObjects = oi.deserialize(gameSer);
//
//		TransformObject transformObject = (TransformObject) GameObjectUtils.FindGameObjectByID(gameObjects, tagUUIDMap.get("rotatethis"));
//
//		ExecutorService executorService = Executors.newFixedThreadPool(1);
//
//		executorService.submit(() -> {
//			int i = 0;
//			while (true) {
//				Thread.sleep(100);
//				transformObject.setRotation(QuaternionF.RotationX((i / 100.0) % (2 * Math.PI)));
//				i++;
//			}
//		});
//
//		play(gameObjects);
//
//	}
//
//	static public void createMap(HashMap<String, UUID> tagUUIDMap, SceneGraphNodeSer sceneGraphNodeSer) {
//
//		for (SceneGraphNodeSer child : sceneGraphNodeSer.getChildren()) {
//
//			tagUUIDMap.put(child.getTag(), child.getUuid());
//
//			createMap(tagUUIDMap, child);
//
//		}
//
//	}
//
//	static public void create() {
//		ArrayList<GameObject> gameObjects = new ArrayList<>();
//
//		GroupObject rootGameObject = new GroupObject();
//
//		TransformBuilder transformBuilder = new TransformBuilder();
//
//		Transform transform = transformBuilder
//				.setPosition(Vec3f.ZERO).build();
//
//		TransformObject wholeSceneTransform = new TransformObject(rootGameObject, transform);
//
//		Transform textTransform = transformBuilder
//				.setPosition(new Vec3f(0, 10, 0))
//				.setScale(Vec3f.ONE.scale(100)).build();
//
//		TransformObject textTransformObject = new TransformObject(rootGameObject, textTransform);
//
//		GeometryBuilder textItem = new GeometryBuilder("Text")
//				.setGeometryType(GeometryType.TEXT);
//
//		for (int i = 0; i < 1_000; i++) {
//
//			GeometryGameObject textGeometryGameObject = new GeometryGameObject(textTransformObject, textItem);
//
//		}
//
//		GeometryBuilder mesh = new GeometryBuilder("BrickCuboid")
//				.setGeometryType(GeometryType.CUBOID)
//				.setTexture("/textures/brickwall.jpg")
//				.setNormalTexture("/normalMaps/brickwall_normal.jpg")
//				.setTransform(transformBuilder
//						.reset().build());
//
//
//		GeometryBuilder meshGroupLight = new GeometryBuilder("MarsModel")
//				.setGeometryType(GeometryType.MODEL)
//				.setInvertedNormals(false)
//				.setTexture("/textures/mars.jpg")
//				.setTransform(transformBuilder
//						.setScale(Vec3f.ONE).build());
//
//
//		GeometryGameObject geometryGameObject = new GeometryGameObject(wholeSceneTransform, mesh);
//
//		LightingBuilder pointLight = new LightingBuilder("PointLight")
//				.setLightingType(LightingType.POINT)
//				.setColour(new Vec3f(0.0f, 1.0f, 0.0f))
//				.setIntensity(10f);
//
//		LightingBuilder directionalLight = new LightingBuilder("DirectionalLight")
//				.setLightingType(LightingType.DIRECTIONAL)
//				.setColour(new Vec3f(1.0f, 1.0f, 1.0f))
//				.setDirection(new Vec3f(0.0f, 0.0f, -1.0f))
//				.setIntensity(1);
//
//		LightingBuilder spotLight = new LightingBuilder("SpotLight")
//				.setLightingType(LightingType.SPOT)
//				.setColour(new Vec3f(1.0f, 0.0f, 0.0f))
//				.setIntensity(100f)
//				.setDirection(Vec3f.Y)
//				.setConeAngle(0.1f);
//
//		Transform build = new TransformBuilder()
//				.setScale(new Vec3f(1000, 1000, 1000))
//				.setRotation(QuaternionF.RotationY(Math.PI)).build();
//
//
//		SkyBoxObject skyBoxObject = new SkyBoxObject(rootGameObject, "/textures/altimeterSphere.png", SkyboxType.SPHERE, build);
//
//		Creation.CreateAxis(wholeSceneTransform);
//		Creation.CreateLight(pointLight, wholeSceneTransform, new Vec3f(0.0f, 0.0f, -10), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
//		Creation.CreateLight(spotLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0.0f), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
//		Creation.CreateLight(directionalLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
//
//		CameraBuilder cameraBuilder = new CameraBuilder("Camera")
//				.setFov(1.22173f)
//				.setNear(1)
//				.setFar(100000);
//
//		Transform cameraTransform = transformBuilder
//				.setPosition(new Vec3f(-10, 0, 0))
//				.setScale(Vec3f.ONE)
//				.setRotation(cameraRotation)
//				.build();
//
//		TransformObject cameraTransformGameObject = new TransformObject(wholeSceneTransform, cameraTransform);
//		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, cameraBuilder);
//		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 1);
//		gameObjects.add(rootGameObject);
//
//		IO io = new IO();
//		io.apply(gameObjects, "TestMin.json");
//	}
//
//	static private void play(ArrayList<GameObject> gameObjects) {
//
//		CameraObject cameraObject = null;
//		TransformObject cameraTransformObject = null;
//
//		for (GameObject gameObject : gameObjects) {
//			CameraObject camera = findMainCamera(gameObject);
//			if (camera != null) {
//				cameraObject = camera;
//			}
//		}
//
//		for (GameObject gameObject : gameObjects) {
//			TransformObject mainCameraTransform = findMainCameraTransform(gameObject, null);
//			if (mainCameraTransform != null) {
//				cameraTransformObject = mainCameraTransform;
//			}
//		}
//
//		DirectTransformController directTransformController = new DirectTransformController(cameraTransformObject, true, true, 0.01f, 10);
//
//		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder()
//				.setLockCursor(true);
//
//		Vec3f ambientLight = new Vec3f(0.0529f, 0.0808f, 0.0922f);
//		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
//		Fog fog = new Fog(true, ambientLight, 0.0001f);
//
//		Scene mainScene = new Scene(
//				"MAIN_SCENE",
//				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
//				null,
//				null,
//				new Shader("/shaders/pickingVertex.glsl", "/shaders/pickingFragment.glsl"),
//				null,
//				fog,
//				ambientLight,
//				skyboxAmbientLight
//		);
//
//		HashMap<String, ArrayList<GameObject>> layeredGameObjectsMap = new HashMap<>();
//
//		layeredGameObjectsMap.put("MAIN_SCENE", gameObjects);
//
//		ArrayList<Scene> sceneLayers = new ArrayList<>();
//		sceneLayers.add(mainScene);
//
//		GameLoop gameLoop = new GameLoop(
//				sceneLayers,
//				wip.build(),
//				directTransformController,
//				layeredGameObjectsMap);
//
//		DebugSubscribable debugSubscribable = new DebugSubscribable();
//		gameLoop.getGameBus().register(debugSubscribable);
//		gameLoop.getExecutorService().submit(debugSubscribable);
//
//		try {
//			gameLoop.run();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	static private CameraObject findMainCamera(GameObject gameObject) {
//		if (gameObject instanceof CameraObject) {
//			CameraObject cameraObject = (CameraObject) gameObject;
//			if (cameraObject.getBuilder().getCameraObjectType().toString().equals("PRIMARY")) {
//				return cameraObject;
//			}
//		} else {
//			for (GameObject child : gameObject.getGameObjectData().getChildren()) {
//				CameraObject mainCamera = findMainCamera(child);
//				if (mainCamera != null) {
//					return mainCamera;
//				}
//			}
//		}
//		return null;
//	}
//
//	static private TransformObject findMainCameraTransform(GameObject gameObject, GameObject parent) {
//		if (gameObject instanceof CameraObject) {
//			CameraObject cameraObject = (CameraObject) gameObject;
//			if (cameraObject.getBuilder().getCameraObjectType().toString().equals("PRIMARY")) {
//				return (TransformObject) parent;
//			}
//		} else {
//			for (GameObject child : gameObject.getGameObjectData().getChildren()) {
//				TransformObject mainCameraTransform = findMainCameraTransform(child, gameObject);
//				if (mainCameraTransform != null) {
//					return mainCameraTransform;
//				}
//			}
//		}
//		return null;
//	}
//
//}
