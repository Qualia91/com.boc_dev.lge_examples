package com.nick.wood.game_engine.examples;

import com.nick.wood.game_engine.core.GameLoop;
import com.nick.wood.game_engine.model.game_objects.*;
import com.nick.wood.game_engine.model.object_builders.CameraBuilder;
import com.nick.wood.game_engine.model.object_builders.GeometryBuilder;
import com.nick.wood.game_engine.model.object_builders.LightingBuilder;
import com.nick.wood.game_engine.model.types.GeometryType;
import com.nick.wood.game_engine.model.types.LightingType;
import com.nick.wood.game_engine.model.types.SkyboxType;
import com.nick.wood.game_engine.model.utils.Creation;
import com.nick.wood.game_engine.systems.DirectTransformController;
import com.nick.wood.graphics_library.Shader;
import com.nick.wood.graphics_library.WindowInitialisationParametersBuilder;
import com.nick.wood.graphics_library.lighting.Fog;
import com.nick.wood.graphics_library.objects.render_scene.Scene;
import com.nick.wood.maths.objects.QuaternionF;
import com.nick.wood.maths.objects.srt.Transform;
import com.nick.wood.maths.objects.srt.TransformBuilder;
import com.nick.wood.maths.objects.vector.Vec3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Examples {

	// this is to get world in sensible coordinate system to start with
	private static final QuaternionF quaternionX = QuaternionF.RotationX((float) Math.toRadians(-90));
	private static final QuaternionF quaternionY = QuaternionF.RotationY((float) Math.toRadians(180));
	private static final QuaternionF quaternionZ = QuaternionF.RotationZ((float) Math.toRadians(90));
	private static final QuaternionF cameraRotation = quaternionZ.multiply(quaternionY).multiply(quaternionX);

	public static void main(String[] args) {
		Examples examples = new Examples();
		//examples.basicExample();
		examples.infiniteHeightMapTerrain();
	}

	public void basicExample() {
		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform transform = transformBuilder
				.setPosition(Vec3f.ZERO).build();

		TransformObject wholeSceneTransform = new TransformObject(transform);
		rootGameObject.getGameObjectData().attachGameObjectNode(wholeSceneTransform);

		Transform textTransform = transformBuilder
				.setPosition(new Vec3f(0, 10, 0))
				.setScale(Vec3f.ONE.scale(100)).build();

		TransformObject textTransformObject = new TransformObject(textTransform);
		rootGameObject.getGameObjectData().attachGameObjectNode(textTransformObject);

		GeometryBuilder textItem = new GeometryBuilder("Text")
				.setGeometryType(GeometryType.TEXT);

		GeometryBuilder mesh = new GeometryBuilder("BrickCuboid")
				.setGeometryType(GeometryType.CUBOID)
				.setTexture("/textures/brickwall.jpg")
				.setNormalTexture("/normalMaps/brickwall_normal.jpg")
				.setTransform(transformBuilder
						.reset().build());
				

		GeometryBuilder meshGroupLight = new GeometryBuilder("MarsModel")
				.setGeometryType(GeometryType.MODEL)
				.setInvertedNormals(false)
				.setTexture("/textures/mars.jpg")
				.setTransform(transformBuilder
						.setScale(Vec3f.ONE).build());
				

		GeometryGameObject geometryGameObject = new GeometryGameObject(mesh);
		wholeSceneTransform.getGameObjectData().attachGameObjectNode(geometryGameObject);

		LightingBuilder pointLight = new LightingBuilder("PointLight")
				.setLightingType(LightingType.POINT)
				.setColour(new Vec3f(0.0f, 1.0f, 0.0f))
				.setIntensity(10f);

		LightingBuilder directionalLight = new LightingBuilder("DirectionalLight")
				.setLightingType(LightingType.DIRECTIONAL)
				.setColour(new Vec3f(1.0f, 1.0f, 1.0f))
				.setDirection(new Vec3f(0.0f, 0.0f, -1.0f))
				.setIntensity(1);

		LightingBuilder spotLight = new LightingBuilder("SpotLight")
				.setLightingType(LightingType.SPOT)
				.setColour(new Vec3f(1.0f, 0.0f, 0.0f))
				.setIntensity(100f)
				.setDirection(Vec3f.Y)
				.setConeAngle(0.1f);

		Transform build = new TransformBuilder()
				.setScale(new Vec3f(1000, 1000, 1000))
				.setRotation(QuaternionF.RotationY(Math.PI)).build();
				

		SkyBoxObject skyBoxObject = new SkyBoxObject("/textures/altimeterSphere.png", SkyboxType.SPHERE, build);
		rootGameObject.getGameObjectData().attachGameObjectNode(skyBoxObject);

		Creation.CreateAxis(wholeSceneTransform);
		Creation.CreateLight(pointLight, wholeSceneTransform, new Vec3f(0.0f, 0.0f, -10), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
		Creation.CreateLight(spotLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0.0f), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
		Creation.CreateLight(directionalLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);

		CameraBuilder cameraBuilder = new CameraBuilder("Camera")
				.setFov(1.22173f)
				.setNear(1)
				.setFar(100000);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				.build();
				
		TransformObject cameraTransformGameObject = new TransformObject(cameraTransform);
		wholeSceneTransform.getGameObjectData().attachGameObjectNode(cameraTransformGameObject);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformGameObject.getGameObjectData().attachGameObjectNode(cameraObject);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 1);
		gameObjects.add(rootGameObject);

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true);

		Vec3f ambientLight = new Vec3f(0.0529f, 0.0808f, 0.0922f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, ambientLight, 0.0001f);

		Scene mainScene = new Scene(
				"MAIN_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				new Shader("/shaders/waterVertex.glsl", "/shaders/waterFragment.glsl"),
				new Shader("/shaders/skyboxVertex.glsl", "/shaders/skyboxFragment.glsl"),
				new Shader("/shaders/pickingVertex.glsl", "/shaders/pickingFragment.glsl"),
				new Shader("/shaders/terrainVertex.glsl", "/shaders/terrainFragment.glsl"),
				fog,
				ambientLight,
				skyboxAmbientLight
		);

		HashMap<String, ArrayList<GameObject>> layeredGameObjectsMap = new HashMap<>();

		layeredGameObjectsMap.put("MAIN_SCENE", gameObjects);

		ArrayList<Scene> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainScene);

		GameLoop gameLoop = new GameLoop(sceneLayers,
				wip.build(),
				directTransformController,
				layeredGameObjectsMap) {
		};

		gameLoop.getExecutorService().execute(gameLoop::update);

		gameLoop.getExecutorService().execute(gameLoop::render);
	}

	void infiniteHeightMapTerrain() {

		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		int size = 1000;

		LightingBuilder directionalLight = new LightingBuilder("DirectionalLight")
				.setLightingType(LightingType.DIRECTIONAL)
				.setColour(new Vec3f(1.0f, 1.0f, 1.0f))
				.setDirection(new Vec3f(0.0f, 1.0f, -1.0f))
				.setIntensity(1f);


		LightObject lightObject = new LightObject(directionalLight);
		rootGameObject.getGameObjectData().attachGameObjectNode(lightObject);


		Transform cameraTransform = new TransformBuilder()
				.setPosition(new Vec3f(0, 0, 5000))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				.build();
				
		TransformObject cameraTransformObj = new TransformObject(cameraTransform);
		rootGameObject.getGameObjectData().attachGameObjectNode(cameraTransformObj);

		CameraBuilder cameraBuilder = new CameraBuilder("Camera")
				.setFov(1.22173f)
				.setNear(500)
				.setFar(10_000_000);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformObj.getGameObjectData().attachGameObjectNode(cameraObject);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformObj, true, true, 0.01f, 1000);

		Transform transform = new TransformBuilder()
				.setScale(5_000_000)
				.setRotation(QuaternionF.RotationY(Math.PI))
				.build();
				

		SkyBoxObject skyBoxObject = new SkyBoxObject("/textures/skyBox.jpg", SkyboxType.MODEL, transform);
		rootGameObject.getGameObjectData().attachGameObjectNode(skyBoxObject);
		gameObjects.add(rootGameObject);

		Transform waterTransform = new TransformBuilder()
				.reset()
				.setPosition(new Vec3f(0, 0, 0)).build();
				
		TransformObject waterTransformObj = new TransformObject(waterTransform);
		rootGameObject.getGameObjectData().attachGameObjectNode(waterTransformObj);
		WaterObject water = new WaterObject("WATER_SQUARE", "/textures/waterDuDvMap.jpg", "/normalMaps/waterNormalMap.jpg", size, 0, 1000);
		waterTransformObj.getGameObjectData().attachGameObjectNode(water);

		ArrayList<TerrainTextureGameObject> terrainTextureGameObjects = new ArrayList<>();
		terrainTextureGameObjects.add(new TerrainTextureGameObject(
				0,
				500,
				"/textures/sand.jpg",
				"/normalMaps/sandNormalMap.jpg"
		));

		terrainTextureGameObjects.add(new TerrainTextureGameObject(
				500,
				2500,
				"/textures/terrain2.jpg",
				"/normalMaps/grassNormal.jpg"
		));

		terrainTextureGameObjects.add(new TerrainTextureGameObject(
				7000,
				1000,
				"/textures/snow.jpg",
				"/normalMaps/large.jpg"
		));

		TerrainGenerationObject terrainGenerationObject = new TerrainGenerationObject("AUTO_TERRAIN",
				3,
				2,
				10,
				terrainTextureGameObjects,
				1000,
				50,
				100);

		rootGameObject.getGameObjectData().attachGameObjectNode(terrainGenerationObject);

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true);

		Vec3f ambientLight = new Vec3f(0.529f, 0.808f, 0.922f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, new Vec3f(0, 0.282f, 0.4f), 0.000003f);

		Scene mainScene = new Scene(
				"MAIN_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				new Shader("/shaders/waterVertex.glsl", "/shaders/waterFragment.glsl"),
				new Shader("/shaders/skyboxVertex.glsl", "/shaders/skyboxFragment.glsl"),
				null,
				new Shader("/shaders/terrainVertex.glsl", "/shaders/terrainFragment.glsl"),
				fog,
				ambientLight,
				skyboxAmbientLight
		);

		HashMap<String, ArrayList<GameObject>> layeredGameObjectsMap = new HashMap<>();
		layeredGameObjectsMap.put("MAIN_SCENE", gameObjects);

		ArrayList<Scene> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainScene);

		GameLoop gameLoop = new GameLoop(sceneLayers,
				wip.build(),
				directTransformController,
				layeredGameObjectsMap);

		//gameLoop.getExecutorService().submit(() -> {
		//	while (true) {
		//		Thread.sleep(5000);
		//		waterTransform.setPosition(new Vec3f(cameraTransform.getPosition().getX() - (size * 1000.0f / 2.0f), cameraTransform.getPosition().getY() - (size * 1000.0f / 2.0f), 0));
		//	}
		//});

		gameLoop.getExecutorService().execute(gameLoop::render);
		gameLoop.getExecutorService().execute(gameLoop::update);


	}
/*
	private void createFboGameObjects(ArrayList<GameObject> fboOneGameObjects) {

		GroupObject fboRootGameObject = new GroupObject();

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform transform = transformBuilder
				.setPosition(Vec3f.ZERO).build();

		TransformObject wholeSceneTransform = new TransformObject(fboRootGameObject, transform);

		Transform textTransform = transformBuilder
				.setPosition(new Vec3f(0, 10, 0))
				.setScale(Vec3f.ONE.scale(100)).build();

		transformBuilder.reset();

		GeometryBuilder meshGroupLight = new GeometryBuilder("BASIC_MODEL")
				.setGeometryType(GeometryType.MODEL)
				.setInvertedNormals(false)
				.setTransform(transformBuilder
						.setScale(Vec3f.ONE).build());



		LightingBuilder directionalLight = new LightingBuilder("DirectionalLight")
				.setLightingType(LightingType.DIRECTIONAL)
				.setColour(new Vec3f(1.0f, 1.0f, 1.0f))
				.setDirection(new Vec3f(1.0f, 0.0f, 0.0f))
				.setIntensity(1f);

		Creation.CreateAxis(wholeSceneTransform);
		Creation.CreateLight(directionalLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);


		CameraBuilder cameraBuilder = new CameraBuilder("fboCamera")
				.setCameraObjectType(CameraObjectType.FBO)
				.setWidth(1000)
				.setHeight(1000)
				.setFov(1.22173f)
				.setNear(1)
				.setFar(100000);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				.build();
				
		TransformObject cameraTransformGameObject = new TransformObject(wholeSceneTransform, cameraTransform);
		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, cameraBuilder);
		fboOneGameObjects.add(fboRootGameObject);


	}

	public void renderingToFBOs() {


		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder()
				.setLockCursor(true);

		ArrayList<GameObject> fboOneGameObjects = new ArrayList<>();

		createFboGameObjects(fboOneGameObjects);

		ArrayList<GameObject> mainGameObjects = new ArrayList<>();

		GroupObject rootObject = new GroupObject();

		mainGameObjects.add(rootObject);

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform mainTransform = 	transformBuilder
				.setPosition(Vec3f.X).build();

		TransformObject wholeSceneTransform = new TransformObject(rootObject, mainTransform);

		GeometryBuilder circle = new GeometryBuilder("FBO_RENDERING_PANEL")
				.setGeometryType(GeometryType.CIRCLE)
				.setTriangleNumber(100)
				.setTransform(transformBuilder
						.setScale(10).build())
				.setTextureFboCameraName("fboCamera");


		GeometryGameObject geometryGameObject = new GeometryGameObject(wholeSceneTransform, circle);


		CameraBuilder cameraBuilder = new CameraBuilder("CAMERA")
				.setFov(1.22173f)
				.setNear(1)
				.setFar(100000);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				.build();
				
		TransformObject cameraTransformGameObject = new TransformObject(wholeSceneTransform, cameraTransform);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 1);
		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, cameraBuilder);


		Vec3f ambientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, ambientLight, 0.0003f);

		Scene mainScene = new Scene(
				"MAIN_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				null,
				null,
				null,
				null,
				fog,
				ambientLight,
				skyboxAmbientLight
		);

		Scene fboOneScene = new Scene(
				"FBO_SCENE_ONE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				null,
				new Shader("/shaders/skyboxVertex.glsl", "/shaders/skyboxFragment.glsl"),
				null,
				null,
				NOFOG,
				ambientLight,
				skyboxAmbientLight
		);


		HashMap<String, ArrayList<GameObject>> layeredGameObjectsMap = new HashMap<>();

		layeredGameObjectsMap.put("MAIN_SCENE", mainGameObjects);
		layeredGameObjectsMap.put("FBO_SCENE_ONE", fboOneGameObjects);

		ArrayList<Scene> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainScene);
		sceneLayers.add(fboOneScene);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build(),
				directTransformController,
				layeredGameObjectsMap);

		try {
			gameLoop.run();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public void stress() {

		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform transform = transformBuilder.build();

		GeometryBuilder meshGroup = new GeometryBuilder("DRAGON")
				.setGeometryType(GeometryType.MODEL)
				.setModelFile("\\models\\dragon.obj")
				.setTexture("/textures/white.png")
				.setTransform(transformBuilder
						.setPosition(Vec3f.ZERO)
						.setRotation(QuaternionF.RotationX(90)).build());
				


		TransformObject wholeSceneTransform = new TransformObject(rootGameObject, transform);

		for (int i = 0; i < 1500; i++) {
			Creation.CreateObject(Vec3f.Y.scale(i), wholeSceneTransform, meshGroup);
		}

		GeometryBuilder meshGroupLight = new GeometryBuilder("LIGHT")
				.setInvertedNormals(true);

		LightingBuilder pointLight = new LightingBuilder("PointLight")
				.setLightingType(LightingType.POINT)
				.setColour(new Vec3f(0.0f, 1.0f, 0.0f))
				.setIntensity(10f);

		LightingBuilder directionalLight = new LightingBuilder("DirectionalLight")
				.setLightingType(LightingType.DIRECTIONAL)
				.setColour(new Vec3f(1.0f, 1.0f, 1.0f))
				.setDirection(new Vec3f(0.0f, 0.0f, -1.0f))
				.setIntensity(1);

		LightingBuilder spotLight = new LightingBuilder("SpotLight")
				.setLightingType(LightingType.SPOT)
				.setColour(new Vec3f(1.0f, 0.0f, 0.0f))
				.setIntensity(100f)
				.setDirection(Vec3f.Y)
				.setConeAngle(0.1f);

		Transform build = new TransformBuilder()
				.setScale(new Vec3f(1000, 1000, 1000))
				.setRotation(QuaternionF.RotationY(Math.PI)).build();


		SkyBoxObject skyBoxObject = new SkyBoxObject(rootGameObject, "/textures/altimeterSphere.png", SkyboxType.SPHERE, build);

		Creation.CreateAxis(wholeSceneTransform);
		Creation.CreateLight(pointLight, wholeSceneTransform, new Vec3f(0.0f, 0.0f, -10), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
		Creation.CreateLight(spotLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0.0f), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
		Creation.CreateLight(directionalLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);

		CameraBuilder cameraBuilder = new CameraBuilder("Camera")
				.setFov(1.22173f)
				.setNear(1)
				.setFar(10000);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				.build();
				
		TransformObject cameraTransformObj = new TransformObject(rootGameObject, cameraTransform);
		CameraObject cameraObject = new CameraObject(cameraTransformObj, cameraBuilder);
		DirectTransformController directCameraController = new DirectTransformController(cameraTransformObj, true, true, 0.01f, 1);

		gameObjects.add(rootGameObject);

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();

		Vec3f ambientLight = new Vec3f(0.1f, 0.1f, 0.1f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, ambientLight, 0.0003f);

		Scene mainScene = new Scene(
				"MAIN_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				null,
				null,
				null,
				null,
				fog,
				ambientLight,
				skyboxAmbientLight
		);

		HashMap<String, ArrayList<GameObject>> layeredGameObjectsMap = new HashMap<>();

		layeredGameObjectsMap.put("MAIN_SCENE", gameObjects);

		ArrayList<Scene> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainScene);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build(),
				directCameraController,
				layeredGameObjectsMap);

		try {
			gameLoop.run();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void picking() {

		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform transform = transformBuilder
				.setPosition(Vec3f.ZERO)
				.build();

		TransformObject wholeSceneTransform = new TransformObject(rootGameObject, transform);

		Transform textTransform = transformBuilder
				.setPosition(new Vec3f(0, 10, 0))
				.setScale(Vec3f.ONE.scale(100))
				.build();

		transformBuilder.setPosition(Vec3f.ZERO);

		TransformObject textTransformObject = new TransformObject(rootGameObject, textTransform);

		GeometryBuilder meshGroupLight = new GeometryBuilder("Mars")
				.setGeometryType(GeometryType.MODEL)
				.setInvertedNormals(false)
				.setTexture("/textures/mars.jpg")
				.setTransform(transformBuilder
						.setScale(Vec3f.ONE).build());

		GeometryBuilder mesh = new GeometryBuilder("BRICK_CUBE")
				.setGeometryType(GeometryType.CUBOID)
				.setTexture("/textures/brickwall.jpg")
				.setNormalTexture("/normalMaps/brickwall_normal.jpg")
				.setTransform(transformBuilder.build());

		LightingBuilder pointLight = new LightingBuilder("PointLight")
				.setLightingType(LightingType.POINT)
				.setColour(new Vec3f(0.0f, 1.0f, 0.0f))
				.setIntensity(10f);

		LightingBuilder directionalLight = new LightingBuilder("DirectionalLight")
				.setLightingType(LightingType.DIRECTIONAL)
				.setColour(new Vec3f(1.0f, 1.0f, 1.0f))
				.setDirection(new Vec3f(0.0f, 0.0f, -1.0f))
				.setIntensity(1);

		LightingBuilder spotLight = new LightingBuilder("SpotLight")
				.setLightingType(LightingType.SPOT)
				.setColour(new Vec3f(1.0f, 0.0f, 0.0f))
				.setIntensity(100f)
				.setDirection(Vec3f.Y)
				.setConeAngle(0.1f);

		Transform build = new TransformBuilder()
				.setScale(new Vec3f(1000, 1000, 1000))
				.setRotation(QuaternionF.RotationY(Math.PI))
				.build();

		//SkyBoxObject skyBoxObject = new SkyBoxObject(rootGameObject, "/textures/altimeterSphere.png", SkyboxType.SPHERE, build);

		Creation.CreateAxis(wholeSceneTransform);
		Creation.CreateLight(pointLight, wholeSceneTransform, new Vec3f(0.0f, 0.0f, -10), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
		Creation.CreateLight(spotLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0.0f), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
		Creation.CreateLight(directionalLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				.build();

		CameraBuilder cameraBuilder = new CameraBuilder("Camera")
				.setFov(1.22173f)
				.setNear(1)
				.setFar(10000);

		TransformObject cameraTransformGameObject = new TransformObject(wholeSceneTransform, cameraTransform);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, false, true, 0.01f, 1);
		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, cameraBuilder);
		gameObjects.add(rootGameObject);

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder()
				.setLockCursor(false);

		Vec3f ambientLight = new Vec3f(0.1f, 0.1f, 0.1f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, ambientLight, 0.0003f);

		Scene mainScene = new Scene(
				"MAIN_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				null,
				null,
				new Shader("/shaders/pickingVertex.glsl", "/shaders/pickingFragment.glsl"),
				null,
				fog,
				ambientLight,
				skyboxAmbientLight
		);

		HashMap<String, ArrayList<GameObject>> layeredGameObjectsMap = new HashMap<>();

		layeredGameObjectsMap.put("MAIN_SCENE", gameObjects);

		ArrayList<Scene> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainScene);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build(),
				directTransformController,
				layeredGameObjectsMap);

		PickingSubscribable pickingSubscribable = new PickingSubscribable(uuid -> {
			GameObject gameObject = GameObjectUtils.FindGameObjectByID(gameObjects, uuid);
			return gameObject.getGameObjectData().getUuid().toString();
		});
		gameLoop.getGameBus().register(pickingSubscribable);
		gameLoop.getExecutorService().submit(pickingSubscribable);

		try {
			gameLoop.run();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
*/
/*
	void terrain() {

		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		int size = 500;

		ProceduralGeneration proceduralGeneration = new ProceduralGeneration();
		float[][] grid = proceduralGeneration.generateHeightMapChunk(
				100000,
				size,
				5,
				2,
				0.7,
				100,
				0,
				0,
				10,
				(amp) -> amp * amp
		);

		SkyBoxObject skyBoxObject = new SkyBoxObject(rootGameObject, "/textures/2k_neptune.jpg", SkyboxType.SPHERE, 1_000_000);

		GeometryBuilder terrain = new GeometryBuilder()
				.setGeometryType(GeometryType.TERRAIN)
				.setTerrainHeightMap(grid)
				.setTexture("/textures/mars.jpg")
				.setCellSpace(2.0)
				

		MeshGameObject meshGameObject = new MeshGameObject(rootGameObject, terrain);

		WaterObject water = new WaterObject(rootGameObject, "/textures/waterDuDvMap.jpg", "/normalMaps/waterNormalMap.jpg", size, 0, 2);

		GeometryBuilder meshGroupLight = new GeometryBuilder()
				.setInvertedNormals(true)
				

		DirectionalLight sun = new DirectionalLight(
				new Vec3f(0.9f, 1.0f, 1.0f),
				Vec3f.Y.add(Vec3f.Z.neg()),
				0.5f);

		LightObject lightObject = new LightObject(rootGameObject, sun);

		Camera camera = new Camera(1.22173f, 10, 1_000_000);
		Transform cameraTransform = new TransformBuilder()
				.setPosition(new Vec3f(0, 0, 100))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				
		TransformObject cameraTransformGameObject = new TransformObject(rootGameObject, cameraTransform);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 1);
		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, camera);
		gameObjects.add(rootGameObject);

		WindowInitialisationParametersBuilder windowInitialisationParametersBuilder = new WindowInitialisationParametersBuilder();

		Vec3f ambientLight = new Vec3f(0.1f, 0.1f, 0.1f);
		Vec3f hudAmbientLight = new Vec3f(0.5f, 0.5f, 0.5f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, ambientLight, 0.0003f);

		Scene mainScene = new Scene(
				"MAIN_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				new Shader("/shaders/waterVertex.glsl", "/shaders/waterFragment.glsl"),
				new Shader("/shaders/skyboxVertex.glsl", "/shaders/skyboxFragment.glsl"),
				new Shader("/shaders/pickingVertex.glsl", "/shaders/pickingFragment.glsl"),
				fog,
				ambientLight,
				skyboxAmbientLight,
				true
		);

		Scene hudScene = new Scene(
				"HUD_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				null,
				null,
				null,
				NOFOG,
				hudAmbientLight,
				skyboxAmbientLight,
				false
		);

		HashMap<String, ArrayList<GameObject>> stringArrayListHashMap = new HashMap<>();

		stringArrayListHashMap.put("MAIN_SCENE", gameObjects);
		stringArrayListHashMap.put("HUD_SCENE", new ArrayList<>());

		ArrayList<Scene> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainScene);
		sceneLayers.add(hudScene);

		try (Window window = new Window(sceneLayers)) {

			window.init(windowInitialisationParametersBuilder.build());

			LWJGLGameControlManager LWJGLGameControlManager = new LWJGLGameControlManager(window.getGraphicsLibraryInput(), directTransformController);


			while (!window.shouldClose()) {

				window.loop(stringArrayListHashMap);

				LWJGLGameControlManager.checkInputs();

			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	void terrain3D() {

		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		int cubeSize = 2;

		TransformBuilder transformBuilder = new TransformBuilder();

		GeometryBuilder cubeSand = new GeometryBuilder()
				.setGeometryType(GeometryType.MODEL)
				.setModelFile("\\models\\cube.obj")
				.setTexture("/textures/brickwall.jpg")
				.setNormalTexture("/normalMaps/brickwall_normal.jpg")
				//.setTransform(Matrix4f.Scale(new Vec3f(cubeSize, cubeSize, cubeSize)))
				

		GeometryBuilder cubeGrass = new GeometryBuilder()
				.setGeometryType(GeometryType.MODEL)
				.setModelFile("\\models\\cube.obj")
				.setTexture("/textures/grass.png")
				.setNormalTexture("/normalMaps/sandNormalMap.jpg")
				//.setTransform(Matrix4f.Scale(new Vec3f(cubeSize, cubeSize, cubeSize)))
				

		GeometryBuilder cubeSnow = new GeometryBuilder()
				.setGeometryType(GeometryType.MODEL)
				.setModelFile("\\models\\cube.obj")
				.setTexture("/textures/white.png")
				.setTransform(transformBuilder.setScale(new Vec3f(cubeSize, cubeSize, cubeSize)).build())
				

		GeometryBuilder cubeFire = new GeometryBuilder()
				.setGeometryType(GeometryType.MODEL)
				.setModelFile("\\models\\cube.obj")
				.setTexture("/textures/8k_venus_surface.jpg")
				.setNormalTexture("/normalMaps/sandNormalMap.jpg")
				//.setTransform(Matrix4f.Scale(new Vec3f(cubeSize, cubeSize, cubeSize)))
				

		int segmentSize = 10;
		int hillHeight = 20;
		Perlin3D perlin3D = new Perlin3D(500, segmentSize);
		Perlin2Df perlin2D = new Perlin2Df(500, segmentSize);
		int size = 50;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				for (int k = 0; k < size; k++) {
					double point = perlin3D.getPoint(i, j, k);

					double weight = (k - (size / 2.0)) / (size / 2.0) - 0.15;

					if (point < (weight * weight * weight * weight)) {

						Transform transform = transformBuilder
								.setPosition(new Vec3f(i * cubeSize, j * cubeSize, k * cubeSize))
								.setScale(Vec3f.ONE)

						TransformObject transformObject = new TransformObject(rootGameObject, transform);

						if (k < 2) {
							MeshGameObject meshGameObject = new MeshGameObject(transformObject, cubeFire);
						}
						if (k < size - 2) {
							MeshGameObject meshGameObject = new MeshGameObject(transformObject, cubeSand);
						} else {
							MeshGameObject meshGameObject = new MeshGameObject(transformObject, cubeGrass);
						}

					}
				}

				double point = (int) (perlin2D.getPoint(i, j) * hillHeight);

				for (int k = 0; k < point; k++) {

					Transform transform = transformBuilder
							.setPosition(new Vec3f(i * cubeSize, j * cubeSize, (k + size) * cubeSize))

					TransformObject transformObject = new TransformObject(rootGameObject, transform);

					if (k > 15) {
						MeshGameObject meshGameObject = new MeshGameObject(transformObject, cubeSnow);
					} else {
						MeshGameObject meshGameObject = new MeshGameObject(transformObject, cubeGrass);
					}

				}
			}
		}


		Camera camera = new Camera(1.22173f, 1, 100000);
		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(0, 0, 100))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				
		TransformObject cameraTransformGameObject = new TransformObject(rootGameObject, cameraTransform);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 1);
		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, camera);
		gameObjects.add(rootGameObject);

		float width = (size * cubeSize);
		int space = 50;

		int counter = 0;
		for (int i = -space; i < width + space; i+= space) {
			for (int j = -space; j < width + space; j+= space) {
				for (int k = -space; k < width + space; k+= space) {
					Transform t = transformBuilder
							.setPosition(new Vec3f(i, j, k))

					PointLight pointLight = new PointLight(
							new Vec3f(0.5412f, 0.1f, 0.1f),
							50
					);
					TransformObject ct = new TransformObject(rootGameObject, t);
					LightObject pointLightSceneObj = new LightObject(ct, pointLight);
					counter++;
				}
			}
		}

		WindowInitialisationParametersBuilder windowInitialisationParametersBuilder = new WindowInitialisationParametersBuilder();

		Vec3f ambientLight = new Vec3f(0.1f, 0.1f, 0.1f);
		Vec3f hudAmbientLight = new Vec3f(0.5f, 0.5f, 0.5f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, ambientLight, 0.0003f);

		Scene mainScene = new Scene(
				"MAIN_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				null,
				null,
				null,
				fog,
				ambientLight,
				skyboxAmbientLight,
				true
		);

		Scene hudScene = new Scene(
				"HUD_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				null,
				null,
				null,
				NOFOG,
				hudAmbientLight,
				skyboxAmbientLight,
				false
		);

		HashMap<String, ArrayList<GameObject>> stringArrayListHashMap = new HashMap<>();

		stringArrayListHashMap.put("MAIN_SCENE", gameObjects);
		stringArrayListHashMap.put("HUD_SCENE", new ArrayList<>());

		ArrayList<Scene> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainScene);
		sceneLayers.add(hudScene);

		try (Window window = new Window(sceneLayers)) {

			window.init(windowInitialisationParametersBuilder.build());

			LWJGLGameControlManager LWJGLGameControlManager = new LWJGLGameControlManager(window.getGraphicsLibraryInput(), directTransformController);

			while (!window.shouldClose()) {

				window.loop(stringArrayListHashMap);

				LWJGLGameControlManager.checkInputs();

			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}

	HashMap<String, TransformObject> cubeMap = new HashMap<>();

	void infiniteTerrain3D() {

		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		int cubeSize = 1;

		TransformBuilder transformBuilder = new TransformBuilder();

		GeometryBuilder cubeSand = new GeometryBuilder()
				.setGeometryType(GeometryType.CUBOID)
				.setTexture("/textures/sand_blocky.jpg")
				.setTransform(transformBuilder
						.setScale(new Vec3f(cubeSize, cubeSize, cubeSize)).build())
				

		GeometryBuilder cubeGrass = new GeometryBuilder()
				.setGeometryType(GeometryType.CUBOID)
				.setTexture("/textures/grass.png")
				.setTransform(transformBuilder.build())
				

		GeometryBuilder cubeSnow = new GeometryBuilder()
				.setGeometryType(GeometryType.CUBOID)
				.setTexture("/textures/white.png")
				.setTransform(transformBuilder.build())
				

		GeometryBuilder cubeFire = new GeometryBuilder()
				.setGeometryType(GeometryType.CUBOID)
				.setTexture("/textures/8k_venus_surface.jpg")
				.setTransform(transformBuilder.build())
				

		int segmentSize = 10;
		int hillHeight = 30;
		Vec3f cullCube = new Vec3f(30, 30, 30);
		Perlin3D perlin3D = new Perlin3D(50000, segmentSize);
		Perlin2Df perlin2D = new Perlin2Df(50000, segmentSize);

		DirectionalLight pos = new DirectionalLight(
				new Vec3f(1f, 1f, 1f),
				Vec3f.Y.add(Vec3f.X).add(Vec3f.Z),
				1f);

		LightObject lightObject = new LightObject(rootGameObject, pos);




		SkyBoxObject skyBoxObject = new SkyBoxObject(rootGameObject, "/textures/2k_neptune.jpg", SkyboxType.SPHERE, 1000);

		Camera camera = new Camera(1.22173f, 1, 100000);
		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(0, 0, 100))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				
		TransformObject cameraTransformGameObject = new TransformObject(rootGameObject, cameraTransform);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 1);
		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, camera);
		gameObjects.add(rootGameObject);


		WindowInitialisationParametersBuilder windowInitialisationParametersBuilder = new WindowInitialisationParametersBuilder().setDebug(true);

		Vec3f ambientLight = new Vec3f(0.1f, 0.1f, 0.1f);
		Vec3f hudAmbientLight = new Vec3f(0.5f, 0.5f, 0.5f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, ambientLight, 0.0003f);

		Scene mainScene = new Scene(
				"MAIN_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				null,
				new Shader("/shaders/skyboxVertex.glsl", "/shaders/skyboxFragment.glsl"),
				null,
				fog,
				ambientLight,
				skyboxAmbientLight,
				true
		);

		Scene hudScene = new Scene(
				"HUD_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				null,
				null,
				null,
				NOFOG,
				hudAmbientLight,
				skyboxAmbientLight,
				false
		);

		HashMap<String, ArrayList<GameObject>> stringArrayListHashMap = new HashMap<>();

		stringArrayListHashMap.put("MAIN_SCENE", gameObjects);
		stringArrayListHashMap.put("HUD_SCENE", new ArrayList<>());

		ArrayList<Scene> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainScene);
		sceneLayers.add(hudScene);

		try (Window window = new Window(sceneLayers)) {

			window.init(windowInitialisationParametersBuilder.build());

			LWJGLGameControlManager LWJGLGameControlManager = new LWJGLGameControlManager(window.getGraphicsLibraryInput(), directTransformController);

			while (!window.shouldClose()) {

				window.loop(stringArrayListHashMap);

				LWJGLGameControlManager.checkInputs();

				createMap(cameraTransform.getPosition(), cullCube, perlin3D, perlin2D, cubeSize, gameObjects, hillHeight, cubeFire, cubeSand, cubeGrass, cubeSnow);

				if (cubeMap.size() > 1000) {
					cullCubes(rootGameObject, cameraTransform.getPosition());
				}

			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}

	private void cullCubes(GroupObject rootGameObject, Vec3f pos) {

		ArrayList<String> removeList = new ArrayList<>();

		for (Map.Entry<String, TransformObject> integerTransformSceneGraphEntry : cubeMap.entrySet()) {

			if (integerTransformSceneGraphEntry.getValue().getTransform().getPosition().subtract(pos).length2() > 900) {
				StringBuilder stringBuffer = new StringBuilder();
				stringBuffer.append(((int) integerTransformSceneGraphEntry.getValue().getTransform().getPosition().getX()))
						.append("_")
						.append(((int) (integerTransformSceneGraphEntry.getValue().getTransform().getPosition().getY())))
						.append("_")
						.append(((int) (integerTransformSceneGraphEntry.getValue().getTransform().getPosition().getZ())));
				removeList.add(stringBuffer.toString());

				rootGameObject.getGameObjectData().removeGameObjectNode(integerTransformSceneGraphEntry.getValue());

			} else {
				// check if box is behind camera
			}

		}


		for (String index : removeList) {
			cubeMap.remove(index);
		}

	}

	private void createMap(Vec3f center,
	                       Vec3f cullCube,
	                       Perlin3D perlin3D,
	                       Perlin2Df perlin2D,
	                       int cubeSize,
	                       ArrayList<GameObject> sceneGraphArrayList,
	                       int hillHeight,
	                       GeometryBuilder cubeFire,
	                       GeometryBuilder cubeSand,
	                       GeometryBuilder cubeGrass,
	                       GeometryBuilder cubeSnow) {


		Vec3f bottomCornerToLoad = center.add(cullCube.scale(-0.5f));
		Vec3f topCornerToLoad = bottomCornerToLoad.add(cullCube);

		TransformBuilder transformBuilder = new TransformBuilder();

		for (int i = (int) bottomCornerToLoad.getX(); i < topCornerToLoad.getX(); i++) {
			for (int j = (int) bottomCornerToLoad.getY(); j < topCornerToLoad.getY(); j++) {
				for (int k = (int) bottomCornerToLoad.getZ(); k < topCornerToLoad.getZ(); k++) {

					if (k >= 0) {

						String index = i + "_" + j + "_" + k;

						if (!cubeMap.containsKey(index)) {

							double point = perlin3D.getPoint(Math.abs(i), Math.abs(j), Math.abs(k));

							if (point < 0.05) {

								double weight = -1;

								if (k < 100) {
									weight = ((k - 50.0) / 50.0) - 0.4;
									weight *= weight * weight * weight;
								} else if (k < 110) {
									weight = ((103.0 - k) / 10.0);
								}

								if (point < weight) {

									GroupObject rootObject = new GroupObject();

									Transform transform = transformBuilder
											.setPosition(new Vec3f(i * cubeSize, j * cubeSize, k * cubeSize))

									TransformObject transformObject = new TransformObject(rootObject, transform);

									if (k < 2) {
										MeshGameObject meshGameObject = new MeshGameObject(transformObject, cubeFire);
									} else if (k < 100) {
										MeshGameObject meshGameObject = new MeshGameObject(transformObject, cubeSand);
									} else {
										MeshGameObject meshGameObject = new MeshGameObject(transformObject, cubeGrass);
									}
									cubeMap.put(index, transformObject);
									sceneGraphArrayList.add(rootObject);
								}
							}
						}
					}
				}
			}
		}
	}

	public void normal() {

		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform transform = transformBuilder
				.setPosition(Vec3f.ZERO)

		TransformObject wholeSceneTransform = new TransformObject(rootGameObject, transform);

		Transform textTransform = transformBuilder
				.setPosition(new Vec3f(0, 10, 0))
				.setScale(Vec3f.ONE.scale(100))

		transformBuilder.setPosition(Vec3f.ZERO);

		TransformObject textTransformObject = new TransformObject(rootGameObject, textTransform);

		TextItem textItem = (TextItem) new GeometryBuilder()
				.setGeometryType(GeometryType.TEXT)
				

		MeshGameObject textMeshGameObject = new MeshGameObject(textTransformObject, textItem);

		GeometryBuilder meshGroupLight = new GeometryBuilder()
				.setGeometryType(GeometryType.MODEL)
				.setInvertedNormals(false)
				.setTexture("/textures/mars.jpg")
				.setTransform(transformBuilder
						.setScale(Vec3f.ONE).build())
				

		GeometryBuilder mesh = new GeometryBuilder()
				.setGeometryType(GeometryType.CUBOID)
				.setTexture("/textures/brickwall.jpg")
				.setNormalTexture("/normalMaps/brickwall_normal.jpg")
				.setTransform(transformBuilder.build())
				


		MeshGameObject meshGameObject = new MeshGameObject(wholeSceneTransform, mesh);

		PointLight pointLight = new PointLight(
				new Vec3f(0.0f, 1.0f, 0.0f),
				10f);
		DirectionalLight directionalLight = new DirectionalLight(
				new Vec3f(1.0f, 1.0f, 1.0f),
				new Vec3f(0.0f, 0.0f, -1.0f),
				1f);
		SpotLight spotLight = new SpotLight(
				new PointLight(
						new Vec3f(1.0f, 0.0f, 0.0f),
						100f),
				Vec3f.Y,
				0.1f
		);


		SkyBoxObject skyBoxObject = new SkyBoxObject(rootGameObject, "/textures/altimeterSphere.png", SkyboxType.SPHERE, 1000);

		Creation.CreateAxis(wholeSceneTransform);
		Creation.CreateLight(pointLight, wholeSceneTransform, new Vec3f(0.0f, 0.0f, -10), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
		Creation.CreateLight(spotLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0.0f), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
		Creation.CreateLight(directionalLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);

		Camera camera = new Camera(1.22173f, 1, 100000);
		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				
		TransformObject cameraTransformGameObject = new TransformObject(wholeSceneTransform, cameraTransform);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 1);
		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, camera);
		gameObjects.add(rootGameObject);

		WindowInitialisationParametersBuilder windowInitialisationParametersBuilder = new WindowInitialisationParametersBuilder();
		windowInitialisationParametersBuilder.setLockCursor(true);

		Vec3f ambientLight = new Vec3f(0.0529f, 0.0808f, 0.0922f);
		Vec3f hudAmbientLight = new Vec3f(0.5f, 0.5f, 0.5f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, ambientLight, 0.0003f);

		Scene mainScene = new Scene(
				"MAIN_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				new Shader("/shaders/waterVertex.glsl", "/shaders/waterFragment.glsl"),
				new Shader("/shaders/skyboxVertex.glsl", "/shaders/skyboxFragment.glsl"),
				new Shader("/shaders/pickingVertex.glsl", "/shaders/pickingFragment.glsl"),
				fog,
				ambientLight,
				skyboxAmbientLight,
				true
		);

		Scene hudScene = new Scene(
				"HUD_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				null,
				null,
				null,
				NOFOG,
				hudAmbientLight,
				skyboxAmbientLight,
				false
		);

		HashMap<String, ArrayList<GameObject>> stringArrayListHashMap = new HashMap<>();

		stringArrayListHashMap.put("MAIN_SCENE", gameObjects);
		stringArrayListHashMap.put("HUD_SCENE", new ArrayList<>());

		ArrayList<Scene> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainScene);
		sceneLayers.add(hudScene);

		try (Window window = new Window(sceneLayers)) {

			window.init(windowInitialisationParametersBuilder.build());

			LWJGLGameControlManager LWJGLGameControlManager = new LWJGLGameControlManager(window.getGraphicsLibraryInput(), directTransformController);

			while (!window.shouldClose()) {

				window.loop(stringArrayListHashMap);

				LWJGLGameControlManager.checkInputs();


			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}

	public void vr() {
		System.err.println("VR_IsRuntimeInstalled() = " + VR_IsRuntimeInstalled());
		System.err.println("VR_RuntimePath() = " + VR_RuntimePath());
		System.err.println("VR_IsHmdPresent() = " + VR_IsHmdPresent());

		try (MemoryStack stack = stackPush()) {
			IntBuffer peError = stack.mallocInt(1);

			int token = VR_InitInternal(peError, 0);
			if (peError.get(0) == 0) {
				try {
					OpenVR.create(token);

					System.err.println("Model Number : " + VRSystem_GetStringTrackedDeviceProperty(
							k_unTrackedDeviceIndex_Hmd,
							ETrackedDeviceProperty_Prop_ModelNumber_String,
							peError
					));
					System.err.println("Serial Number: " + VRSystem_GetStringTrackedDeviceProperty(
							k_unTrackedDeviceIndex_Hmd,
							ETrackedDeviceProperty_Prop_SerialNumber_String,
							peError
					));

					IntBuffer w = stack.mallocInt(1);
					IntBuffer h = stack.mallocInt(1);
					VRSystem_GetRecommendedRenderTargetSize(w, h);
					System.err.println("Recommended width : " + w.get(0));
					System.err.println("Recommended height: " + h.get(0));
				} finally {
					VR_ShutdownInternal();
				}
			} else {
				System.out.println("INIT ERROR SYMBOL: " + VR_GetVRInitErrorAsSymbol(peError.get(0)));
				System.out.println("INIT ERROR  DESCR: " + VR_GetVRInitErrorAsEnglishDescription(peError.get(0)));
			}
		}
	}

	
	public void particleSystem() {
		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform hudTransform = transformBuilder
				.setPosition(Vec3f.X)
				

		Transform transform = transformBuilder
				.setPosition(Vec3f.ZERO)
				

		TransformObject wholeSceneTransform = new TransformObject(rootGameObject, transform);

		TransformObject hudTransformGameObject = new TransformObject(rootGameObject, hudTransform);

		GeometryBuilder point = new GeometryBuilder()
				.setGeometryType(GeometryType.POINT)
				

		MeshGameObject textMeshGameObject = new MeshGameObject(wholeSceneTransform, point);

		PointLight pointLight = new PointLight(
				new Vec3f(0.0f, 1.0f, 0.0f),
				10f);
		DirectionalLight directionalLight = new DirectionalLight(
				new Vec3f(1.0f, 1.0f, 1.0f),
				new Vec3f(0.0f, 0.0f, -1.0f),
				1f);
		SpotLight spotLight = new SpotLight(
				new PointLight(
						new Vec3f(1.0f, 0.0f, 0.0f),
						100f),
				Vec3f.Y,
				0.1f
		);

		Creation.CreateLight(pointLight, wholeSceneTransform, new Vec3f(0.0f, 0.0f, -1), Vec3f.ONE, QuaternionF.Identity, point);
		Creation.CreateLight(spotLight, wholeSceneTransform, new Vec3f(0.0f, -1.0f, 0.0f), Vec3f.ONE, QuaternionF.Identity, point);
		Creation.CreateLight(directionalLight, wholeSceneTransform, new Vec3f(0.0f, -1.0f, 0), Vec3f.ONE, QuaternionF.Identity, point);

		Camera camera = new Camera(1.22173f, 1, 100000);

		Transform cameraTransform = transformBuilder
				.setPosition(Vec3f.X)
				

		TransformObject cameraTransformGameObject = new TransformObject(wholeSceneTransform, cameraTransform);
		DirectTransformController directTransformController = new DirectTransformController(wholeSceneTransform, true, true);

		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, camera);
		DirectTransformController directCameraController = new DirectTransformController(cameraTransformGameObject, true, true);



		gameObjects.add(rootGameObject);

		WindowInitialisationParametersBuilder windowInitialisationParametersBuilder = new WindowInitialisationParametersBuilder();

		try (Window window = new Window()) {

			window.init(windowInitialisationParametersBuilder.build());

			LWJGLGameControlManager LWJGLGameControlManager = new LWJGLGameControlManager(window.getGraphicsLibraryInput(), directCameraController);

			while (!window.shouldClose()) {

				window.loop(gameObjects, new ArrayList<>());

				LWJGLGameControlManager.checkInputs();

			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public void mase() {
		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform transform = transformBuilder

		TransformObject wholeSceneTransform = new TransformObject(rootGameObject, transform);

		GeometryBuilder point = new GeometryBuilder()
				.setGeometryType(GeometryType.POINT)
				

		MeshGameObject textMeshGameObject = new MeshGameObject(wholeSceneTransform, point);

		PointLight pointLight = new PointLight(
				new Vec3f(0.0f, 1.0f, 0.0f),
				10f);
		DirectionalLight directionalLight = new DirectionalLight(
				new Vec3f(1.0f, 1.0f, 1.0f),
				new Vec3f(0.0f, 0.0f, -1.0f),
				1f);
		SpotLight spotLight = new SpotLight(
				new PointLight(
						new Vec3f(1.0f, 0.0f, 0.0f),
						100f),
				Vec3f.Y,
				0.1f
		);

		Creation.CreateLight(pointLight, wholeSceneTransform, new Vec3f(0.0f, 0.0f, -1), Vec3f.ONE, QuaternionF.Identity, point);
		Creation.CreateLight(spotLight, wholeSceneTransform, new Vec3f(0.0f, -1.0f, 0.0f), Vec3f.ONE, QuaternionF.Identity, point);
		Creation.CreateLight(directionalLight, wholeSceneTransform, new Vec3f(0.0f, -1.0f, 0), Vec3f.ONE, QuaternionF.Identity, point);
		Camera camera = new Camera(1.22173f, 1, 100000);
		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				
		TransformObject cameraTransformGameObject = new TransformObject(wholeSceneTransform, cameraTransform);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 1);
		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, camera);
		gameObjects.add(rootGameObject);

		int width = 100;
		int height = 100;

		RecursiveBackTracker recursiveBackTracker = new RecursiveBackTracker(width, height);
		ArrayList<Cell> visited = recursiveBackTracker.getVisited();

		// build mase
		GeometryBuilder cuboid = new GeometryBuilder().setGeometryType(GeometryType.CUBOID).setNormalTexture("/normalMaps/sandNormalMap.jpg")

		// render diagonals
		for (int i = -1; i < width * 2 + 1; i += 2) {

			for (int j = -1; j < height * 2 + 1; j += 2) {

				Transform cellTransformcell = transformBuilder
						.setPosition(new Vec3f(i, j, 0))

				TransformObject transformSceneGraphcell = new TransformObject(wholeSceneTransform, cellTransformcell);

				MeshGameObject meshSceneGraphcell = new MeshGameObject(transformSceneGraphcell, cuboid);

			}

		}

		Vec2i north = new Vec2i(0, -1);
		Vec2i west = new Vec2i(-1, 0);
		Vec2i south = new Vec2i(0, 1);
		Vec2i east = new Vec2i(1, 0);

		// render walls
		for (Cell cell : visited) {

			if (!cell.getPathDirections().contains(north)) {

				Transform cellTransformcell = transformBuilder
						.setPosition(new Vec3f((cell.getPosition().getX() * 2), (cell.getPosition().getY() * 2) - 1, 0))

				TransformObject transformSceneGraphcell = new TransformObject(wholeSceneTransform, cellTransformcell);

				MeshGameObject meshSceneGraphcell = new MeshGameObject(transformSceneGraphcell, cuboid);

			}

			if (!cell.getPathDirections().contains(south)) {

				Transform cellTransformcell = transformBuilder
						.setPosition(new Vec3f((cell.getPosition().getX() * 2), (cell.getPosition().getY() * 2) + 1, 0))

				TransformObject transformSceneGraphcell = new TransformObject(wholeSceneTransform, cellTransformcell);

				MeshGameObject meshSceneGraphcell = new MeshGameObject(transformSceneGraphcell, cuboid);

			}

			if (!cell.getPathDirections().contains(west) && !cell.getPosition().equals(Vec2i.ZERO)) {

				Transform cellTransformcell = transformBuilder
						.setPosition(new Vec3f((cell.getPosition().getX() * 2) - 1, (cell.getPosition().getY() * 2), 0))

				TransformObject transformSceneGraphcell = new TransformObject(wholeSceneTransform, cellTransformcell);

				MeshGameObject meshSceneGraphcell = new MeshGameObject(transformSceneGraphcell, cuboid);

			}

			if (!cell.getPathDirections().contains(east) && !cell.getPosition().equals(new Vec2i(width - 1, height - 1))) {

				Transform cellTransformcell = transformBuilder
						.setPosition(new Vec3f((cell.getPosition().getX() * 2) + 1, (cell.getPosition().getY() * 2), 0))

				TransformObject transformSceneGraphcell = new TransformObject(wholeSceneTransform, cellTransformcell);

				MeshGameObject meshSceneGraphcell = new MeshGameObject(transformSceneGraphcell, cuboid);

			}

		}

		WindowInitialisationParametersBuilder windowInitialisationParametersBuilder = new WindowInitialisationParametersBuilder();


		Vec3f ambientLight = new Vec3f(0.0529f, 0.0808f, 0.0922f);
		Vec3f hudAmbientLight = new Vec3f(0.5f, 0.5f, 0.5f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, ambientLight, 0.0003f);

		Scene mainScene = new Scene(
				"MAIN_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				new Shader("/shaders/waterVertex.glsl", "/shaders/waterFragment.glsl"),
				new Shader("/shaders/skyboxVertex.glsl", "/shaders/skyboxFragment.glsl"),
				new Shader("/shaders/pickingVertex.glsl", "/shaders/pickingFragment.glsl"),
				fog,
				ambientLight,
				skyboxAmbientLight,
				true
		);

		Scene hudScene = new Scene(
				"HUD_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				null,
				null,
				null,
				NOFOG,
				hudAmbientLight,
				skyboxAmbientLight,
				false
		);

		HashMap<String, ArrayList<GameObject>> stringArrayListHashMap = new HashMap<>();

		stringArrayListHashMap.put("MAIN_SCENE", gameObjects);
		stringArrayListHashMap.put("HUD_SCENE", new ArrayList<>());

		ArrayList<Scene> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainScene);
		sceneLayers.add(hudScene);

		try (Window window = new Window(sceneLayers)) {

			window.init(windowInitialisationParametersBuilder.build());

			LWJGLGameControlManager LWJGLGameControlManager = new LWJGLGameControlManager(window.getGraphicsLibraryInput(), directTransformController);

			while (!window.shouldClose()) {

				window.loop(stringArrayListHashMap);

				LWJGLGameControlManager.checkInputs();

			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}


*/

}
