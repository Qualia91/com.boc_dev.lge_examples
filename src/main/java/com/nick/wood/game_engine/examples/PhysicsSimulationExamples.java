package com.nick.wood.game_engine.examples;

import com.nick.wood.game_engine.core.GameLoop;
import com.nick.wood.game_engine.model.game_objects.*;
import com.nick.wood.game_engine.model.object_builders.CameraBuilder;
import com.nick.wood.game_engine.model.object_builders.GeometryBuilder;
import com.nick.wood.game_engine.model.object_builders.LightingBuilder;
import com.nick.wood.game_engine.model.object_builders.RigidBodyBuilder;
import com.nick.wood.game_engine.model.types.GeometryType;
import com.nick.wood.game_engine.model.types.LightingType;
import com.nick.wood.game_engine.model.types.RigidBodyObjectType;
import com.nick.wood.game_engine.model.utils.Creation;
import com.nick.wood.game_engine.systems.control.DirectTransformController;
import com.nick.wood.game_engine.systems.physics.RigidBodyPhysicsSystem;
import com.nick.wood.graphics_library.Shader;
import com.nick.wood.graphics_library.WindowInitialisationParametersBuilder;
import com.nick.wood.graphics_library.lighting.Fog;
import com.nick.wood.graphics_library.objects.render_scene.Scene;
import com.nick.wood.maths.objects.QuaternionF;
import com.nick.wood.maths.objects.srt.Transform;
import com.nick.wood.maths.objects.srt.TransformBuilder;
import com.nick.wood.maths.objects.vector.Vec3d;
import com.nick.wood.maths.objects.vector.Vec3f;
import com.nick.wood.maths.points_on_a_sphere.SpiralAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

class PhysicsSimulationExamples {

	// this is to get world in sensible coordinate system to start with
	private static final QuaternionF quaternionX = QuaternionF.RotationX((float) Math.toRadians(-90));
	private static final QuaternionF quaternionY = QuaternionF.RotationY((float) Math.toRadians(180));
	private static final QuaternionF quaternionZ = QuaternionF.RotationZ((float) Math.toRadians(90));
	private static final QuaternionF cameraRotation = quaternionZ.multiply(quaternionY).multiply(quaternionX);

	public static void main(String[] args) {
		PhysicsSimulationExamples physicsSimulationExamples = new PhysicsSimulationExamples();
		physicsSimulationExamples.twoBalls();
		//physicsSimulationExamples.twoLinesInteracting();
		//physicsSimulationExamples.randomBox();
		//physicsSimulationExamples.bigBangBox();
	}

	public void twoBalls() {
		ArrayList<GameObject> gameObjects = new ArrayList<>();

		TransformBuilder transformBuilder = new TransformBuilder();

		RigidBodyBuilder rigidBodyBuilderOne = new RigidBodyBuilder(UUID.randomUUID())
				.setOrigin(new Vec3d(0.0, 0.0, 5.0))
				.setLinearMomentum(Vec3d.Z.scale(-1))
				.setAngularMomentum(Vec3d.X.scale(0.1));

		RigidBodyBuilder rigidBodyBuilderTwo = new RigidBodyBuilder(UUID.randomUUID())
				.setOrigin(new Vec3d(0.0, 0.0, -5.0))
				.setLinearMomentum(Vec3d.Z.scale(1))
				.setAngularMomentum(Vec3d.X.scale(0.1));

		RigidBodyObject rigidBodyOne = new RigidBodyObject(rigidBodyBuilderOne);
		RigidBodyObject rigidBodyTwo = new RigidBodyObject(rigidBodyBuilderTwo);

		gameObjects.add(rigidBodyOne);
		gameObjects.add(rigidBodyTwo);

		GeometryBuilder meshGroupLight = new GeometryBuilder("LIGHTMODEL")
				.setGeometryType(GeometryType.POINT);

		GeometryBuilder physModel = new GeometryBuilder("PhysModel")
				.setGeometryType(GeometryType.MODEL)
				.setInvertedNormals(false)
				.setTexture("/textures/mars.jpg")
				.setTransform(transformBuilder
						.setScale(0.4f).build());

		GeometryGameObject physicsObMeshOne = new GeometryGameObject(
				physModel
		);

		GeometryGameObject physicsObMeshTwo = new GeometryGameObject(
				physModel
		);

		rigidBodyOne.getTransformObject().getGameObjectData().attachGameObjectNode(physicsObMeshOne);
		rigidBodyTwo.getTransformObject().getGameObjectData().attachGameObjectNode(physicsObMeshTwo);


		LightingBuilder directionalLight = new LightingBuilder("DirectionalLight")
				.setLightingType(LightingType.DIRECTIONAL)
				.setColour(new Vec3f(1.0f, 1.0f, 1.0f))
				.setDirection(new Vec3f(-1.0f, -1.0f, -1.0f))
				.setIntensity(1);


		TransformObject axisTransform = new TransformObject(transformBuilder.reset().build());
		//Creation.CreateAxis(axisTransform);
		Creation.CreateLight(directionalLight, axisTransform, new Vec3f(0.0f, 0.0f, 10), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
		gameObjects.add(axisTransform);

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
		gameObjects.add(cameraTransformGameObject);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformGameObject.getGameObjectData().attachGameObjectNode(cameraObject);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 0.5f);

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true);

		Vec3f ambientLight = new Vec3f(0.529f, 0.808f, 0.922f);
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

		gameLoop.getGESystems().add(new RigidBodyPhysicsSystem(5));

		gameLoop.getExecutorService().execute(gameLoop::update);
		gameLoop.getExecutorService().execute(gameLoop::render);
	}

	public void twoLinesInteracting() {
		ArrayList<GameObject> gameObjects = new ArrayList<>();

		TransformBuilder transformBuilder = new TransformBuilder();

		GeometryBuilder physModel = new GeometryBuilder("PhysModel")
				.setGeometryType(GeometryType.MODEL)
				.setInvertedNormals(false)
				.setTexture("/textures/mars.jpg")
				.setTransform(transformBuilder
						.setScale(0.35f).build());

		GeometryGameObject physicsObMeshOne = new GeometryGameObject(
				physModel
		);

		// demo 1: 2 lines interacting
		for (int j = 0; j < 10; j++) {
			for (int i = 0; i < 3; i++) {
				Vec3d mom = Vec3d.Z.scale(i + j / 10.0);// * (j/10.0));
				Vec3d ang = Vec3d.X.scale(0.001).scale(j);
				//Vec3d ang = Vec3d.ZERO;
				if (i > 0) {
					mom = mom.neg();
					//ang = ang.neg();
					//ang = Vec3d.X.scale(0.01).scale(j);
					ang = Vec3d.ZERO;
				}

				UUID uuid = UUID.randomUUID();
				RigidBodyBuilder rigidBodyBuilder = new RigidBodyBuilder(uuid)
						.setOrigin(new Vec3d(5.0, j * 3.0 - 2 * i / 3.0, i * 8))
						.setLinearMomentum(mom)
						.setAngularMomentum(ang);

				RigidBodyObject rigidBody = new RigidBodyObject(rigidBodyBuilder);

				gameObjects.add(rigidBody);

				rigidBody.getTransformObject().getGameObjectData().attachGameObjectNode(physicsObMeshOne);
			}
		}

		GeometryBuilder meshGroupLight = new GeometryBuilder("MarsModel")
				.setGeometryType(GeometryType.MODEL)
				.setInvertedNormals(false)
				.setTexture("/textures/mars.jpg")
				.setTransform(transformBuilder
						.setScale(0.3f).build());

		LightingBuilder directionalLightOne = new LightingBuilder("DirectionalLightOne")
				.setLightingType(LightingType.DIRECTIONAL)
				.setColour(new Vec3f(1.0f, 1.0f, 1.0f))
				.setDirection(new Vec3f(-1.0f, -1.0f, -1.0f))
				.setIntensity(1);

		TransformObject axisTransform = new TransformObject(transformBuilder.reset().build());
		Creation.CreateAxis(axisTransform);
		Creation.CreateLight(directionalLightOne, axisTransform, new Vec3f(0.0f, 0.0f, 10), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
		gameObjects.add(axisTransform);

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
		gameObjects.add(cameraTransformGameObject);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformGameObject.getGameObjectData().attachGameObjectNode(cameraObject);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 1);

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true);

		Vec3f ambientLight = new Vec3f(0.5f, 0.5f, 0.5f);
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

		gameLoop.getGESystems().add(new RigidBodyPhysicsSystem(5));

		gameLoop.getExecutorService().execute(gameLoop::update);
		gameLoop.getExecutorService().execute(gameLoop::render);
	}

	public void randomBox() {
		ArrayList<GameObject> gameObjects = new ArrayList<>();

		TransformBuilder transformBuilder = new TransformBuilder();

		GeometryBuilder physModel = new GeometryBuilder("PhysModel")
				.setGeometryType(GeometryType.MODEL)
				.setInvertedNormals(false)
				.setTexture("/textures/mars.jpg")
				.setTransform(transformBuilder
						.setScale(0.35f).build());

		GeometryGameObject physicsObMeshOne = new GeometryGameObject(
				physModel
		);

		Random random = new Random();
		for (int k = -3; k < 3; k++) {
			for (int j = -3; j < 3; j++) {
				for (int i = -3; i < 3; i++) {
					if (!(i == 0 && j == 0 && k == 0)) {
						UUID uuid = UUID.randomUUID();
						Vec3d mom = Vec3d.X.scale(random.nextInt(20) - 10).add(Vec3d.Y.scale(random.nextInt(20) - 10)).add(Vec3d.Z.scale(random.nextInt(20) - 10));
						Vec3d angMom = Vec3d.X.scale(random.nextInt(20) - 10).add(Vec3d.Y.scale(random.nextInt(20) - 10)).add(Vec3d.Z.scale(random.nextInt(20) - 10));
						RigidBodyBuilder rigidBodyBuilder = new RigidBodyBuilder(uuid)
								.setOrigin(new Vec3d(5.0, j * 3.0 - 2 * i / 3.0, i * 8))
								.setLinearMomentum(mom)
								.setAngularMomentum(angMom);

						RigidBodyObject rigidBody = new RigidBodyObject(rigidBodyBuilder);

						gameObjects.add(rigidBody);

						rigidBody.getTransformObject().getGameObjectData().attachGameObjectNode(physicsObMeshOne);
					}
				}
			}
		}

		createArena(gameObjects, 100);

		TransformObject axisTransform = new TransformObject(transformBuilder.reset().build());
		Creation.CreateAxis(axisTransform);
		gameObjects.add(axisTransform);

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
		gameObjects.add(cameraTransformGameObject);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformGameObject.getGameObjectData().attachGameObjectNode(cameraObject);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 1);

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true);

		Vec3f ambientLight = new Vec3f(0.5f, 0.5f, 0.5f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, ambientLight, 0.0001f);

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
				layeredGameObjectsMap) {
		};

		gameLoop.getGESystems().add(new RigidBodyPhysicsSystem(5));

		gameLoop.getExecutorService().execute(gameLoop::update);
		gameLoop.getExecutorService().execute(gameLoop::render);
	}

	public void bigBangBox() {
		ArrayList<GameObject> gameObjects = new ArrayList<>();

		TransformBuilder transformBuilder = new TransformBuilder();

		GeometryBuilder physModel = new GeometryBuilder("PhysModel")
				.setGeometryType(GeometryType.MODEL)
				.setInvertedNormals(false)
				.setTexture("/textures/mars.jpg")
				.setTransform(transformBuilder
						.setScale(0.35f).build());

		GeometryGameObject physicsObMeshOne = new GeometryGameObject(
				physModel
		);

		Random random = new Random();
		for (int k = -2; k < 2; k++) {
			for (int j = -2; j < 2; j++) {
				for (int i = -2; i < 2; i++) {
					Vec3d mom = Vec3d.X.scale(-i * 15).add(Vec3d.Y.scale(-j * 15)).add(Vec3d.Z.scale(-k * 15));
					Vec3d angMom = Vec3d.X.scale(random.nextInt(10) - 4).add(Vec3d.Y.scale(random.nextInt(10) - 4)).add(Vec3d.Z.scale(random.nextInt(10) - 4));
					UUID uuid = UUID.randomUUID();

					RigidBodyBuilder rigidBodyBuilder = new RigidBodyBuilder(uuid)
							.setOrigin(new Vec3d(5.0, j * 3.0 - 2 * i / 3.0, i * 8))
							.setLinearMomentum(mom)
							.setAngularMomentum(angMom);

					RigidBodyObject rigidBody = new RigidBodyObject(rigidBodyBuilder);

					gameObjects.add(rigidBody);

					rigidBody.getTransformObject().getGameObjectData().attachGameObjectNode(physicsObMeshOne);
				}
			}
		}

		createArena(gameObjects, 100);
		RigidBodyObject ball = createBall();
		gameObjects.add(ball);

		TransformObject axisTransform = new TransformObject(transformBuilder.reset().build());
		Creation.CreateAxis(axisTransform);
		gameObjects.add(axisTransform);

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
		gameObjects.add(cameraTransformGameObject);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformGameObject.getGameObjectData().attachGameObjectNode(cameraObject);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 1);

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true);

		Vec3f ambientLight = new Vec3f(0.5f, 0.5f, 0.5f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, ambientLight, 0.0001f);

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
				layeredGameObjectsMap) {
		};

		gameLoop.getGESystems().add(new RigidBodyPhysicsSystem(5));

		gameLoop.getExecutorService().execute(gameLoop::render);
		gameLoop.getExecutorService().execute(gameLoop::update);
	}

	/*

	void bigBangWithPlayer() throws ExecutionException, InterruptedException {

		ArrayList<RigidBody> rigidBodies = new ArrayList<>();
		ArrayList<UUID> toRender = new ArrayList<>();

		ArrayList<Force> forces = new ArrayList<>();
		ArrayList<Force> forces2 = new ArrayList<>();
		forces.add(new Drag(-0.1));

		// demo 3: big bang
		int cubeSideLength = 5;
		Random random = new Random();
		for (int k = -cubeSideLength; k < cubeSideLength; k++) {
			for (int j = -cubeSideLength; j < cubeSideLength; j++) {
				for (int i = -cubeSideLength; i < cubeSideLength; i++) {
					Vec3d mom = Vec3d.X.scale(-i).add(Vec3d.Y.scale(-j)).add(Vec3d.Z.scale(-k));
					Vec3d angMom = Vec3d.X.scale(random.nextInt(10) - 4).add(Vec3d.Y.scale(random.nextInt(10) - 4)).add(Vec3d.Z.scale(random.nextInt(10) - 4));
					UUID uuid = UUID.randomUUID();
					toRender.add(uuid);
					RigidBody rigidBody = new RigidBody(uuid, 1, new Vec3d(1.0, 1.0, 1.0), new Vec3d(i * 10, j * 10, k * 10), new Quaternion(1.0, 0.0, 0.0, 0.0), mom, angMom.scale(0.02), RigidBodyType.SPHERE, forces2);
					rigidBodies.add(rigidBody);
				}
			}
		}

		UUID playerUUID = UUID.randomUUID();
		RigidBody rigidBody = new RigidBody(playerUUID, 1, new Vec3d(1.0, 1.0, 1.0), new Vec3d(-50, 0, 0), new Quaternion(1.0, 0.0, 0.0, 0.0), Vec3d.ZERO, Vec3d.ZERO, RigidBodyType.SPHERE, forces);
		SceneGraph rootGameObject = convertToGameObject(rigidBody, "/textures/white.png");
		TransformSceneGraph transformGameObjectLaser = (TransformSceneGraph) rootGameObject.getSceneGraphNodeData().getChildren().get(0);
		createLaserUnderTransform(new Vec3f(1.0f, 1.0f, 1.0f), Vec3f.ZERO, transformGameObjectLaser);
		rigidBodies.add(rigidBody);
		Control control = new RigidBodyControl(100 * rigidBody.getMass(), 1 * Math.sqrt(rigidBody.getMass()), rigidBody.getUuid());


		Camera camera = new Camera(new Vec3f(-5.0f, 0.0f, 1.0f), new Vec3f(0.0f, 0.0f, 0.0f), 0.5f, 0.1f);
		CameraSceneGraph cameraGameObject = new CameraSceneGraph(transformGameObjectLaser, camera, CameraType.PRIMARY);


		SceneGraph lightRootObject = new SceneGraph();

		HashMap<UUID, SceneGraph> rootGameObjectHashMap = new HashMap<>();

		for (RigidBody rigidBodyInLoop : rigidBodies) {
			rootGameObjectHashMap.put(rigidBodyInLoop.getUuid(), convertToGameObject(rigidBodyInLoop, "/textures/white.png"));
		}

		createArena(rootGameObjectHashMap, rigidBodies, 1000, forces);

		//HudController hudController = createHud(cameraGameObject, playerUUID, toRender);

		rootGameObjectHashMap.put(playerUUID, rootGameObject);

		rootGameObjectHashMap.put(UUID.randomUUID(), lightRootObject);

		SimulationInterface simulation = new com.nick.wood.physics.rigid_body_dynamics_verbose.Simulation(rigidBodies);

		Game game = new Game(1400, 900, simulation, rootGameObjectHashMap);

		LWJGLGameControlManager lwjglGameControlManagerCameraView = new LWJGLGameControlManager(game.getWindow().getGraphicsLibraryInput(), control);
		game.addController(lwjglGameControlManagerCameraView);

		//game.addHudController(hudController);

		ExecutorService executor = Executors.newFixedThreadPool(4);

		Future<?> submit = executor.submit(game);

		// waits for game to finish
		submit.get();

		// closes executor service
		executor.shutdown();
	}

	void cubeSphereInteraction() throws ExecutionException, InterruptedException {

		ArrayList<RigidBody> rigidBodies = new ArrayList<>();

		ArrayList<Force> forces = new ArrayList<>();
		ArrayList<Force> forces2 = new ArrayList<>();
		forces.add(new GravityBasic());


		UUID uuid2 = UUID.randomUUID();
		RigidBody rigidBody2 = new RigidBody(uuid2, 1, new Vec3d(1.0, 1.0, 1.0), new Vec3d(0.0, -10.0, 10), new Quaternion(1.0, 0.0, 0.0, 0.0), Vec3d.Z.neg(), Vec3d.ZERO, RigidBodyType.SPHERE, forces);
		rigidBodies.add(rigidBody2);

		UUID uuid3 = UUID.randomUUID();
		RigidBody rigidBody3 = new RigidBody(uuid3, 1, new Vec3d(1.0, 1.0, 1.0), new Vec3d(0.0, -12.0, 15), new Quaternion(1.0, 0.0, 0.0, 0.0), Vec3d.Z.neg(), Vec3d.ZERO, RigidBodyType.SPHERE, forces);
		rigidBodies.add(rigidBody3);

		UUID floorUUID = UUID.randomUUID();
		RigidBody floorRigidBody = new RigidBody(floorUUID, 100, new Vec3d(50.0, 50.0, 1.0), new Vec3d(0.0, 0.0, -1.0), Quaternion.RotationY(0.0), Vec3d.ZERO, Vec3d.ZERO, RigidBodyType.CUBOID, forces2);
		rigidBodies.add(floorRigidBody);

		UUID floorUUID1 = UUID.randomUUID();
		RigidBody floorRigidBody1 = new RigidBody(floorUUID1, 100, new Vec3d(50.0, 50.0, 1.0), new Vec3d(0.0, -20.0, -10.0), Quaternion.RotationY(0.0), Vec3d.ZERO, Vec3d.ZERO, RigidBodyType.CUBOID, forces2);
		rigidBodies.add(floorRigidBody1);

		SceneGraph cameraRootObject = new SceneGraph();
		Camera camera = new Camera(new Vec3f(-10.0f, 0.0f, 10.0f), new Vec3f(0.0f, 0.0f, 0.0f), 0.5f, 0.1f);
		CameraSceneGraph cameraGameObject = new CameraSceneGraph(cameraRootObject, camera, CameraType.PRIMARY);

		SceneGraph lightRootObject = new SceneGraph();

		HashMap<UUID, SceneGraph> rootGameObjectHashMap = new HashMap<>();

		for (RigidBody rigidBody : rigidBodies) {
			rootGameObjectHashMap.put(rigidBody.getUuid(), convertToGameObject(rigidBody, "/textures/white.png"));
		}

		rootGameObjectHashMap.put(UUID.randomUUID(), cameraRootObject);
		rootGameObjectHashMap.put(UUID.randomUUID(), lightRootObject);

		createArena(rootGameObjectHashMap, rigidBodies, 100, forces);

		SimulationInterface simulation = new com.nick.wood.physics.rigid_body_dynamics_verbose.Simulation(rigidBodies);

		Game game = new Game(1000, 800, simulation, rootGameObjectHashMap);

		Control cameraViewControl = new DirectCameraController(camera, true, false);
		LWJGLGameControlManager lwjglGameControlManagerCameraView = new LWJGLGameControlManager(game.getWindow().getGraphicsLibraryInput(), cameraViewControl);
		game.addController(lwjglGameControlManagerCameraView);

		ExecutorService executor = Executors.newFixedThreadPool(4);

		Future<?> submit = executor.submit(game);

		// waits for game to finish
		submit.get();

		// closes executor service
		executor.shutdown();
	}

	void game() throws ExecutionException, InterruptedException {

		ArrayList<RigidBody> rigidBodies = new ArrayList<>();
		ArrayList<UUID> toRender = new ArrayList<>();
		HashMap<UUID, SceneGraph> rootGameObjectHashMap = new HashMap<>();
		ArrayList<Force> forces = new ArrayList<>();
		forces.add(new Drag(-0.1));

		// Arena
		createArena(rootGameObjectHashMap, rigidBodies, 100, forces);

		UUID ballUUID = createBall(rootGameObjectHashMap, rigidBodies, forces, toRender);

		// player
		UUID playerUUID = UUID.randomUUID();
		double playerMass = 10;
		RigidBody playerRigidBody = new RigidBody(playerUUID, playerMass, new Vec3d(4.0, 2.0, 1.0), new Vec3d(-40, 0, 0), new Quaternion(1.0, 0.0, 0.0, 0.0), Vec3d.ZERO, Vec3d.ZERO, RigidBodyType.CUBOID, forces);
		SceneGraph rootGameObject = convertToGameObjectCuboid(playerRigidBody, "/textures/spaceShipTexture.jpg");
		TransformSceneGraph transformGameObjectLaser = (TransformSceneGraph) rootGameObject.getSceneGraphNodeData().getChildren().get(0);
		createLaserUnderTransform(new Vec3f(1.0f, 1.0f, 1.0f), Vec3f.ZERO, transformGameObjectLaser);
		rigidBodies.add(playerRigidBody);
		Camera camera = new Camera(new Vec3f(-5.0f, 0.0f, 1.0f), new Vec3f(0.0f, 0.0f, 0.0f), 0.5f, 0.1f);
		CameraSceneGraph cameraGameObject = new CameraSceneGraph(transformGameObjectLaser, camera, CameraType.PRIMARY);

		Hud hud = new Hud();
		HudController hudController = new HudController(hud, playerUUID, toRender);

		rootGameObjectHashMap.put(playerUUID, rootGameObject);

		// controls
		Control rigidBodyControl = new RigidBodyControl(100 * playerMass, 50, playerUUID);
		Control ballControl = new RigidBodyControl(1, 50, ballUUID);
		Control cameraViewControl = new DirectCameraController(camera, true, false);

		// sim
		SimulationInterface simulation = new com.nick.wood.physics.rigid_body_dynamics_verbose.Simulation(rigidBodies);

		Game game = new Game(1400, 1200, simulation, rootGameObjectHashMap);

		LWJGLGameControlManager lwjglGameControlManagerRigidBody = new LWJGLGameControlManager(game.getWindow().getGraphicsLibraryInput(), rigidBodyControl);
		LWJGLGameControlManager lwjglGameControlManagerCameraView = new LWJGLGameControlManager(game.getWindow().getGraphicsLibraryInput(), cameraViewControl);

		game.addController(lwjglGameControlManagerRigidBody);
		game.addController(lwjglGameControlManagerCameraView);
		game.addHudController(hudController);

		ExecutorService executor = Executors.newFixedThreadPool(4);

		Future<?> submit = executor.submit(game);

		// waits for game to finish
		submit.get();

		// closes executor service
		executor.shutdown();
	}
*/
	private RigidBodyObject createBall() {

		// ball
		TransformBuilder transformBuilder = new TransformBuilder();

		Random random = new Random();
		Vec3d angMom = Vec3d.X.scale(random.nextInt(10) - 4).add(Vec3d.Y.scale(random.nextInt(10) - 4)).add(Vec3d.Z.scale(random.nextInt(10) - 4));
		UUID uuid = UUID.randomUUID();

		RigidBodyBuilder rigidBodyBuilder = new RigidBodyBuilder(uuid)
				.setAngularMomentum(angMom);

		RigidBodyObject rigidBody = new RigidBodyObject(rigidBodyBuilder);

		GeometryBuilder physModel = new GeometryBuilder("PhysModel")
				.setGeometryType(GeometryType.MODEL)
				.setInvertedNormals(false)
				.setTexture("/textures/mars.jpg")
				.setTransform(transformBuilder
						.setScale(0.35f).build());

		GeometryGameObject physicsObMeshOne = new GeometryGameObject(
				physModel
		);

		rigidBody.getTransformObject().getGameObjectData().attachGameObjectNode(physicsObMeshOne);

		GeometryBuilder geometryBuilder = new GeometryBuilder("LightMesh")
				.setTriangleNumber(6)
				.setInvertedNormals(true)
				.setTransform(transformBuilder.setScale(0.1f).build());

		LightingBuilder pointLight = new LightingBuilder("PointLight")
				.setLightingType(LightingType.POINT)
				.setColour(new Vec3f(0.0f, 1.0f, 0.0f))
				.setIntensity(10f);

		SpiralAlgorithms spiralAlgorithms = new SpiralAlgorithms();
		Vec3f[] vec3fs = spiralAlgorithms.fibonacciSphereF(50);
		for (Vec3f vec3f : vec3fs) {
			Creation.CreateLight(pointLight, rigidBody.getTransformObject(), vec3f.scale(3f), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, geometryBuilder);
		}


		return rigidBody;

	}

	private void createArena(ArrayList<GameObject> gameObjects, int width) {
		//// Arena
		UUID uuid = UUID.randomUUID();
		RigidBodyBuilder rigidBodyBuilder = new RigidBodyBuilder(uuid)
				.setMass(1000)
				.setDimensions(new Vec3d(width, width, width))
				.setRigidBodyType(RigidBodyObjectType.SPHERE_INNER);

		RigidBodyObject rigidBody = new RigidBodyObject(rigidBodyBuilder);

		GeometryBuilder arenaMesh = new GeometryBuilder("ARENA")
				.setGeometryType(GeometryType.SPHERE)
				.setInvertedNormals(true)
				.setTexture("/textures/2k_neptune.jpg");

		GeometryGameObject geometryGameObject = new GeometryGameObject(arenaMesh);
		rigidBody.getTransformObject().getGameObjectData().attachGameObjectNode(geometryGameObject);

		gameObjects.add(rigidBody);

	}
}