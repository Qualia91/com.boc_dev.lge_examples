package com.nick.wood.game_engine.examples;

import com.nick.wood.game_engine.core.GameLoop;
import com.nick.wood.game_engine.core.SceneLayer;
import com.nick.wood.game_engine.event_bus.busses.GameBus;
import com.nick.wood.game_engine.gcs_model.gcs.Component;
import com.nick.wood.game_engine.gcs_model.gcs.Registry;
import com.nick.wood.game_engine.gcs_model.gcs.RegistryUpdater;
import com.nick.wood.game_engine.gcs_model.generated.components.*;
import com.nick.wood.game_engine.gcs_model.generated.enums.CameraObjectType;
import com.nick.wood.game_engine.gcs_model.generated.enums.CameraProjectionType;
import com.nick.wood.game_engine.gcs_model.generated.enums.LightingType;
import com.nick.wood.game_engine.gcs_model.generated.enums.SkyboxType;
import com.nick.wood.game_engine.gcs_model.systems.GcsSystem;
import com.nick.wood.game_engine.systems.MeshAddSystem;
import com.nick.wood.game_engine.systems.MeshRemoveSystem;
import com.nick.wood.game_engine.systems.boids.BoidSystem;
import com.nick.wood.game_engine.systems.generation.TerrainGeneration;
import com.nick.wood.graphics_library.Shader;
import com.nick.wood.graphics_library.WindowInitialisationParametersBuilder;
import com.nick.wood.graphics_library.objects.lighting.Fog;
import com.nick.wood.graphics_library.objects.render_scene.Scene;
import com.nick.wood.maths.objects.QuaternionF;
import com.nick.wood.maths.objects.matrix.Matrix4f;
import com.nick.wood.maths.objects.srt.Transform;
import com.nick.wood.maths.objects.srt.TransformBuilder;
import com.nick.wood.maths.objects.vector.Vec3f;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class Examples {

	// this is to get world in sensible coordinate system to start with
	private static final QuaternionF quaternionX = QuaternionF.RotationX((float) Math.toRadians(-90));
	private static final QuaternionF quaternionY = QuaternionF.RotationY((float) Math.toRadians(180));
	private static final QuaternionF quaternionZ = QuaternionF.RotationZ((float) Math.toRadians(90));
	private static final QuaternionF cameraRotation = quaternionZ.multiply(quaternionY).multiply(quaternionX);

	public static void main(String[] args) {
		Examples examples = new Examples();
		//examples.basicExample();
		//examples.boidsExample();
		//examples.meshTypeConversionExample();
		//examples.instancedRenderingExample();
		examples.terrainGenerationExample();
		//examples.renderingToFBOs();
		//examples.infiniteHeightMapTerrain();
		//examples.picking();
		//examples.cubeTerrain();
		//examples.maze();
	}

	private UUID createBasicMaterial(SceneLayer sceneLayer) {
		MaterialObject materialObject = new MaterialObject(
				sceneLayer.getRegistry(),
				"Material",
				new Vec3f(1, 1, 1),
				1,
				1,
				new Vec3f(1, 1, 1)
		);

		TextureObject textureObjectVisual = new TextureObject(
				sceneLayer.getRegistry(),
				"VisualTextureOne",
				"/textures/brickwall.jpg"
		);

		NormalMapObject normalMapObject = new NormalMapObject(
				sceneLayer.getRegistry(),
				"NormalTextureOne",
				"/normalMaps/brickwall_normal.jpg"
		);

		textureObjectVisual.getUpdater().setParent(materialObject).sendUpdate();
		normalMapObject.getUpdater().setParent(materialObject).sendUpdate();

		return materialObject.getUuid();
	}

	public void basicExample() {

		TransformBuilder transformBuilder = new TransformBuilder();

		Vec3f ambientLight = new Vec3f(0.1f, 0.1f, 0.1f);
		Fog fog = new Fog(true, ambientLight, 0.0001f);

		SceneLayer mainSceneLayer = new SceneLayer(
				"MAIN",
				ambientLight,
				fog
		);

		LightObject lightObject = new LightObject(
				mainSceneLayer.getRegistry(),
				"MyFirstLight",
				0.25f,
				0.5f,
				1f,
				Vec3f.X,
				0.1f,
				Vec3f.Z.neg(),
				1000,
				LightingType.SPOT
		);

		LightObject directionalObject = new LightObject(
				mainSceneLayer.getRegistry(),
				"MySecondLight",
				0.25f,
				0.5f,
				1f,
				new Vec3f(0.529f, 0.808f, 0.922f),
				0.2f,
				Vec3f.Z.neg().add(Vec3f.X),
				1,
				LightingType.DIRECTIONAL
		);

		SkyBoxObject skyBoxObject = new SkyBoxObject(
				mainSceneLayer.getRegistry(),
				"SKY_BOX",
				5000,
				SkyboxType.SPHERE,
				"textures/bw_gradient_skybox.png"
		);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation).build();

		CameraObject cameraObject = new CameraObject(
				mainSceneLayer.getRegistry(),
				"Camera",
				CameraProjectionType.PERSPECTIVE,
				CameraObjectType.PRIMARY,
				10000,
				1.22f,
				1080,
				1,
				1920
		);

		ControllableObject controllableObject = new ControllableObject(
				mainSceneLayer.getRegistry(),
				"Camera controller",
				true,
				true,
				0.01f,
				1);
		TransformObject cameraTransformObject = new TransformObject(
				mainSceneLayer.getRegistry(),
				"CameraTransform",
				cameraTransform.getPosition(),
				cameraTransform.getRotation(),
				cameraTransform.getScale());

		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		mainSceneLayer.getGcsSystems().add((GcsSystem) new BoidSystem());

		UUID basicMaterial = createBasicMaterial(mainSceneLayer);

		Random random = new Random();

		for (int i = 0; i < 10; i++) {

			for (int j = 0; j < 10; j++) {

				for (int k = 0; k < 10; k++) {


					Transform build = transformBuilder.reset().setPosition(new Vec3f(i*4, j*4, k*4)).build();


					TransformObject newTransformObject = new TransformObject(
							mainSceneLayer.getRegistry(),
							"TransformObject" + i,
							build.getPosition(),
							build.getRotation(),
							build.getScale());

					GeometryObject newGeometryObject = new GeometryObject(
							mainSceneLayer.getRegistry(),
							"Geometry" + i,
							Matrix4f.Identity,
							basicMaterial,
							"DEFAULT_SPHERE"
					);

					BoidObject boidObject = new BoidObject(
							mainSceneLayer.getRegistry(),
							"Boid" + i,
							0.001f,
							0.1f,
							new Vec3f(random.nextInt(10) - 5, random.nextInt(10) - 5, random.nextInt(10) - 5),
							400,
							10,
							8,
							0.001f,
							2,
							20,
							Vec3f.ZERO,
							0.001f
					);
					newGeometryObject.getUpdater().setParent(newTransformObject).sendUpdate();
					boidObject.getUpdater().setParent(newTransformObject).sendUpdate();

				}
			}
		}

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true).setWindowWidth(800).setWindowHeight(600).setDebug(true);


		////// gui layer ///////

		SceneLayer guiSceneLayer = new SceneLayer(
				"GUI",
				Vec3f.ONE,
				Fog.NOFOG
		);

		TransformObject newTransformObject = new TransformObject(
				guiSceneLayer.getRegistry(),
				"TransformObjectGui",
				new Vec3f(0, 0, 0),
				QuaternionF.RotationX(Math.PI/2),
				Vec3f.ONE.scale(100));

		GeometryObject newGeometryObject = new GeometryObject(
				guiSceneLayer.getRegistry(),
				"GeometryGui",
				Matrix4f.Identity,
				basicMaterial,
				"DEFAULT_SQUARE"
		);

		CameraObject hudCameraObject = new CameraObject(
				guiSceneLayer.getRegistry(),
				"Camera",
				CameraProjectionType.ORTHOGRAPHIC,
				CameraObjectType.PRIMARY,
				1000000,
				1,
				1,
				10,
				1
		);

		Transform hudTransform = transformBuilder
				.setPosition(new Vec3f(-1000, 0, 0))
				.setRotation(cameraRotation)
				.build();

		TransformObject hudCameraTransformObject = new TransformObject(
				guiSceneLayer.getRegistry(),
				"CameraTransform",
				hudTransform.getPosition(),
				hudTransform.getRotation(),
				hudTransform.getScale());


//		ControllableObject controllableObject = new ControllableObject(
//				guiSceneLayer.getRegistry(),
//				"Camera controller",
//				true,
//				true,
//				0.01f,
//				1);
//
//		controllableObject.getUpdater().setParent(hudCameraTransformObject).sendUpdate();

		hudCameraObject.getUpdater().setParent(hudCameraTransformObject).sendUpdate();

		newGeometryObject.getUpdater().setParent(newTransformObject).sendUpdate();

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);
		//sceneLayers.add(guiSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void terrainGenerationExample() {

		TransformBuilder transformBuilder = new TransformBuilder();
		Vec3f ambientLight = new Vec3f(0.1f, 0.1f, 0.1f);
		Fog fog = new Fog(true, ambientLight, 0.0001f);

		SceneLayer mainSceneLayer = new SceneLayer(
				"MAIN",
				ambientLight,
				fog
		);

		LightObject lightObject = new LightObject(
				mainSceneLayer.getRegistry(),
				"MyFirstLight",
				0.25f,
				0.5f,
				1f,
				Vec3f.X,
				0.1f,
				Vec3f.Z.neg(),
				1000,
				LightingType.SPOT
		);

		LightObject directionalObject = new LightObject(
				mainSceneLayer.getRegistry(),
				"MySecondLight",
				0.25f,
				0.5f,
				1f,
				new Vec3f(0.529f, 0.808f, 0.922f),
				0.2f,
				Vec3f.Z.neg().add(Vec3f.X),
				1,
				LightingType.DIRECTIONAL
		);

		SkyBoxObject skyBoxObject = new SkyBoxObject(
				mainSceneLayer.getRegistry(),
				"SKY_BOX",
				5000,
				SkyboxType.SPHERE,
				"textures/bw_gradient_skybox.png"
		);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation).build();

		CameraObject cameraObject = new CameraObject(
				mainSceneLayer.getRegistry(),
				"Camera",
				CameraProjectionType.PERSPECTIVE,
				CameraObjectType.PRIMARY,
				10000,
				1.22f,
				1080,
				1,
				1920
		);

		ControllableObject controllableObject = new ControllableObject(
				mainSceneLayer.getRegistry(),
				"Camera controller",
				true,
				true,
				0.01f,
				1);
		TransformObject cameraTransformObject = new TransformObject(
				mainSceneLayer.getRegistry(),
				"CameraTransform",
				cameraTransform.getPosition(),
				cameraTransform.getRotation(),
				cameraTransform.getScale());

		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

//		gcsSystems.add((GcsSystem) new BoidSystem());
//		gcsSystems.add((GcsSystem) new TestGcsSystem());
		mainSceneLayer.getGcsSystems().add((GcsSystem) new TerrainGeneration());

		UUID basicMaterial = createBasicMaterial(mainSceneLayer);

		TerrainGenerationObject terrainGenerationObject = new TerrainGenerationObject(
				mainSceneLayer.getRegistry(),
				"TerrainGenerationObject",
				50,
				5,
				31,
				10,
				1.7f,
				basicMaterial,
				5,
				10
		);

		terrainGenerationObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true).setWindowWidth(800).setWindowHeight(600).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void boidsExample() {

		TransformBuilder transformBuilder = new TransformBuilder();
		Vec3f ambientLight = new Vec3f(0.1f, 0.1f, 0.1f);
		Fog fog = new Fog(true, ambientLight, 0.0001f);

		SceneLayer mainSceneLayer = new SceneLayer(
				"MAIN",
				ambientLight,
				fog
		);

		LightObject lightObject = new LightObject(
				mainSceneLayer.getRegistry(),
				"MyFirstLight",
				0.25f,
				0.5f,
				1f,
				Vec3f.X,
				0.1f,
				Vec3f.Z.neg(),
				1000,
				LightingType.SPOT
		);

		LightObject directionalObject = new LightObject(
				mainSceneLayer.getRegistry(),
				"MySecondLight",
				0.25f,
				0.5f,
				1f,
				new Vec3f(0.529f, 0.808f, 0.922f),
				0.2f,
				Vec3f.Z.neg().add(Vec3f.X),
				1,
				LightingType.DIRECTIONAL
		);

		SkyBoxObject skyBoxObject = new SkyBoxObject(
				mainSceneLayer.getRegistry(),
				"SKY_BOX",
				5000,
				SkyboxType.SPHERE,
				"textures/bw_gradient_skybox.png"
		);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation).build();

		CameraObject cameraObject = new CameraObject(
				mainSceneLayer.getRegistry(),
				"Camera",
				CameraProjectionType.PERSPECTIVE,
				CameraObjectType.PRIMARY,
				10000,
				1.22f,
				1080,
				1,
				1920
		);

		ControllableObject controllableObject = new ControllableObject(
				mainSceneLayer.getRegistry(),
				"Camera controller",
				true,
				true,
				0.01f,
				1);
		TransformObject cameraTransformObject = new TransformObject(
				mainSceneLayer.getRegistry(),
				"CameraTransform",
				cameraTransform.getPosition(),
				cameraTransform.getRotation(),
				cameraTransform.getScale());

		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		UUID basicMaterial = createBasicMaterial(mainSceneLayer);

		mainSceneLayer.getGcsSystems().add((GcsSystem) new BoidSystem());

		Random random = new Random();

		for (int i = 0; i < 10; i++) {

			for (int j = 0; j < 10; j++) {

				for (int k = 0; k < 10; k++) {


					Transform build = transformBuilder.reset().setPosition(new Vec3f(i*4, j*4, k*4)).build();


					TransformObject newTransformObject = new TransformObject(
							mainSceneLayer.getRegistry(),
							"TransformObject" + i,
							build.getPosition(),
							build.getRotation(),
							build.getScale());

					GeometryObject newGeometryObject = new GeometryObject(
							mainSceneLayer.getRegistry(),
							"Geometry" + i,
							Matrix4f.Identity,
							basicMaterial,
							"DEFAULT_SPHERE"
					);

					BoidObject boidObject = new BoidObject(
							mainSceneLayer.getRegistry(),
							"Boid" + i,
							0.001f,
							0.1f,
							new Vec3f(random.nextInt(10) - 5, random.nextInt(10) - 5, random.nextInt(10) - 5),
							400,
							10,
							10,
							0.001f,
							2,
							50,
							Vec3f.ZERO,
							0.001f
					);
					newGeometryObject.getUpdater().setParent(newTransformObject).sendUpdate();
					boidObject.getUpdater().setParent(newTransformObject).sendUpdate();

				}
			}
		}

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setDebug(true);
		wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void meshTypeConversionExample() {

		TransformBuilder transformBuilder = new TransformBuilder();
		Vec3f ambientLight = new Vec3f(0.1f, 0.1f, 0.1f);
		Fog fog = new Fog(true, ambientLight, 0.0001f);

		SceneLayer mainSceneLayer = new SceneLayer(
				"MAIN",
				ambientLight,
				fog
		);

		LightObject lightObject = new LightObject(
				mainSceneLayer.getRegistry(),
				"MyFirstLight",
				0.25f,
				0.5f,
				1f,
				Vec3f.X,
				0.1f,
				Vec3f.Z.neg(),
				1000,
				LightingType.SPOT
		);

		LightObject directionalObject = new LightObject(
				mainSceneLayer.getRegistry(),
				"MySecondLight",
				0.25f,
				0.5f,
				1f,
				new Vec3f(0.529f, 0.808f, 0.922f),
				0.2f,
				Vec3f.Z.neg().add(Vec3f.X),
				1,
				LightingType.DIRECTIONAL
		);

		SkyBoxObject skyBoxObject = new SkyBoxObject(
				mainSceneLayer.getRegistry(),
				"SKY_BOX",
				5000,
				SkyboxType.SPHERE,
				"textures/bw_gradient_skybox.png"
		);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation).build();

		CameraObject cameraObject = new CameraObject(
				mainSceneLayer.getRegistry(),
				"Camera",
				CameraProjectionType.PERSPECTIVE,
				CameraObjectType.PRIMARY,
				10000,
				1.22f,
				1080,
				1,
				1920
		);

		ControllableObject controllableObject = new ControllableObject(
				mainSceneLayer.getRegistry(),
				"Camera controller",
				true,
				true,
				0.01f,
				1);
		TransformObject cameraTransformObject = new TransformObject(
				mainSceneLayer.getRegistry(),
				"CameraTransform",
				cameraTransform.getPosition(),
				cameraTransform.getRotation(),
				cameraTransform.getScale());

		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		UUID basicMaterial = createBasicMaterial(mainSceneLayer);

		for (int i = 0; i < 20; i++) {

			for (int j = 0; j < 20; j++) {

				for (int k = 0; k < 20; k++) {

					Transform build = transformBuilder.reset().setPosition(new Vec3f(i*4, j*4, k*4)).build();

					TransformObject newTransformObject = new TransformObject(
							mainSceneLayer.getRegistry(),
							"TransformObject" + i,
							build.getPosition(),
							build.getRotation(),
							build.getScale());

					GeometryObject newGeometryObject = new GeometryObject(
							mainSceneLayer.getRegistry(),
							"Geometry" + i,
							Matrix4f.Identity,
							basicMaterial,
							"DEFAULT_CUBE"
					);

					newGeometryObject.getUpdater().setParent(newTransformObject).sendUpdate();

				}
			}
		}

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setDebug(true);
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1200).setWindowHeight(1100).setDebug(true);

		mainSceneLayer.getGcsSystems().add((GcsSystem) new MeshAddSystem());
		mainSceneLayer.getGcsSystems().add((GcsSystem) new MeshRemoveSystem());

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void instancedRenderingExample() {

		TransformBuilder transformBuilder = new TransformBuilder();
		Vec3f ambientLight = new Vec3f(0.1f, 0.1f, 0.1f);
		Fog fog = new Fog(true, ambientLight, 0.0001f);

		SceneLayer mainSceneLayer = new SceneLayer(
				"MAIN",
				ambientLight,
				fog
		);

		LightObject lightObject = new LightObject(
				mainSceneLayer.getRegistry(),
				"MyFirstLight",
				0.25f,
				0.5f,
				1f,
				Vec3f.X,
				0.1f,
				Vec3f.Z.neg(),
				1000,
				LightingType.SPOT
		);

		LightObject directionalObject = new LightObject(
				mainSceneLayer.getRegistry(),
				"MySecondLight",
				0.25f,
				0.5f,
				1f,
				new Vec3f(0.529f, 0.808f, 0.922f),
				0.2f,
				Vec3f.Z.neg().add(Vec3f.X),
				1,
				LightingType.DIRECTIONAL
		);

		SkyBoxObject skyBoxObject = new SkyBoxObject(
				mainSceneLayer.getRegistry(),
				"SKY_BOX",
				5000,
				SkyboxType.SPHERE,
				"textures/bw_gradient_skybox.png"
		);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation).build();

		CameraObject cameraObject = new CameraObject(
				mainSceneLayer.getRegistry(),
				"Camera",
				CameraProjectionType.PERSPECTIVE,
				CameraObjectType.PRIMARY,
				10000,
				1.22f,
				1080,
				1,
				1920
		);

		ControllableObject controllableObject = new ControllableObject(
				mainSceneLayer.getRegistry(),
				"Camera controller",
				true,
				true,
				0.01f,
				1);
		TransformObject cameraTransformObject = new TransformObject(
				mainSceneLayer.getRegistry(),
				"CameraTransform",
				cameraTransform.getPosition(),
				cameraTransform.getRotation(),
				cameraTransform.getScale());

		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		UUID basicMaterial = createBasicMaterial(mainSceneLayer);

		for (int i = 0; i < 60; i++) {

			for (int j = 0; j < 60; j++) {

				for (int k = 0; k < 60; k++) {

					Transform build = transformBuilder.reset().setPosition(new Vec3f(i*4, j*4, k*4)).build();

					TransformObject newTransformObject = new TransformObject(
							mainSceneLayer.getRegistry(),
							"TransformObject" + i,
							build.getPosition(),
							build.getRotation(),
							build.getScale());

					GeometryObject newGeometryObject = new GeometryObject(
							mainSceneLayer.getRegistry(),
							"Geometry" + i,
							Matrix4f.Identity,
							basicMaterial,
							"DEFAULT_CUBE"
					);

					newGeometryObject.getUpdater().setParent(newTransformObject).sendUpdate();

				}
			}
		}

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setDebug(true);
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(800).setWindowHeight(600).setDebug(true);


		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

/*
	public void basicExample() {
		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform transform = transformBuilder
				.setPosition(Vec3f.ZERO).build();

		TransformObject wholeSceneTransform = new TransformObject(transform);
		rootGameObject.getGameObjectData().attachGameObjectNode(wholeSceneTransform);

		Transform textTransform = transformBuilder
				.setPosition(new Vec3f(0, 0, 0))
				.setScale(Vec3f.ONE.scale(10)).build();

		TransformObject textTransformObject = new TransformObject(textTransform);
		rootGameObject.getGameObjectData().attachGameObjectNode(textTransformObject);

		GeometryBuilder textItem = new GeometryBuilder("Text")
				.setGeometryType(GeometryType.TEXT);

		GeometryGameObject textGameObject = new GeometryGameObject(textItem);
		wholeSceneTransform.getGameObjectData().attachGameObjectNode(textGameObject);

		GeometryBuilder mesh = new GeometryBuilder("BrickCuboid")
				.setGeometryType(GeometryType.CUBOID)
				.setTexture("/textures/brickwall.jpg")
				.setNormalTexture("/normalMaps/brickwall_normal.jpg")
				.setTransform(transformBuilder
						.reset().build());

		GeometryGameObject geometryGameObject = new GeometryGameObject(mesh);
		wholeSceneTransform.getGameObjectData().attachGameObjectNode(geometryGameObject);

		GeometryBuilder meshGroupLight = new GeometryBuilder("MarsModel")
				.setGeometryType(GeometryType.MODEL)
				.setInvertedNormals(false)
				.setTransform(transformBuilder
						.setScale(0.1f).build());

		LightingBuilder pointLight = new LightingBuilder("PointLight")
				.setLightingType(LightingType.POINT)
				.setColour(new Vec3f(0.0f, 1.0f, 0.0f))
				.setIntensity(100f);

		LightingBuilder directionalLight = new LightingBuilder("DirectionalLight")
				.setLightingType(LightingType.DIRECTIONAL)
				.setColour(new Vec3f(1.0f, 1.0f, 1.0f))
				.setDirection(new Vec3f(0.0f, 0.0f, -1.0f))
				.setIntensity(0.1f);

		LightingBuilder spotLight = new LightingBuilder("SpotLight")
				.setLightingType(LightingType.SPOT)
				.setColour(new Vec3f(1.0f, 0.0f, 0.0f))
				.setIntensity(100f)
				.setDirection(Vec3f.Y)
				.setConeAngle(0.05f);

		Transform build = new TransformBuilder()
				.setScale(new Vec3f(1000, 1000, 1000))
				.setRotation(QuaternionF.RotationY(Math.PI)).build();


//		SkyBoxObject skyBoxObject = new SkyBoxObject("/textures/8k_venus_surface.jpg", SkyboxType.MODEL, build);
//		rootGameObject.getGameObjectData().attachGameObjectNode(skyBoxObject);

//		Creation.CreateAxis(wholeSceneTransform);
		Creation.CreateLight(pointLight, wholeSceneTransform, new Vec3f(0.0f, 0.0f, -10), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
		Creation.CreateLight(spotLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0.0f), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
		Creation.CreateLight(directionalLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);

		CameraBuilder cameraBuilder = new CameraBuilder("Camera")
				.setFov(1.22173f)
				.setNear(0.01f)
				.setFar(1000);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				.build();

		TransformObject cameraTransformGameObject = new TransformObject(cameraTransform);
		wholeSceneTransform.getGameObjectData().attachGameObjectNode(cameraTransformGameObject);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformGameObject.getGameObjectData().attachGameObjectNode(cameraObject);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 0.1f);
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

		GameBus gameBus = new GameBus();

		Registry registry = new Registry(gameBus);

		GameLoop gameLoop = new GameLoop(sceneLayers,
				wip.build(),
				directTransformController,
				registry) {
		};

		gameLoop.getExecutorService().execute(gameLoop::update);

		gameLoop.getExecutorService().execute(gameLoop::render);

//		PickingSubscribable pickingSubscribable = new PickingSubscribable(gameObjects);
//		gameLoop.getGameBus().register(pickingSubscribable);
//		gameLoop.getExecutorService().execute(pickingSubscribable);
	}

	void infiniteHeightMapTerrain() {

		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		int size = 50;

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
				.setRotation(QuaternionF.RotationZ(Math.PI/4).multiply(cameraRotation))
				.build();
				
		TransformObject cameraTransformObj = new TransformObject(cameraTransform);
		rootGameObject.getGameObjectData().attachGameObjectNode(cameraTransformObj);

		CameraBuilder cameraBuilder = new CameraBuilder("Camera")
				.setFov(1.22173f)
				.setNear(500)
				.setFar(10_000_000);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformObj.getGameObjectData().attachGameObjectNode(cameraObject);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformObj, true, true, 0.001f, 50);

		Transform transform = new TransformBuilder()
				.setScale(5_000_000)
				.setRotation(QuaternionF.RotationY(Math.PI))
				.build();
				

		SkyBoxObject skyBoxObject = new SkyBoxObject("/textures/skyBox.jpg", SkyboxType.MODEL, transform);
		rootGameObject.getGameObjectData().attachGameObjectNode(skyBoxObject);
		gameObjects.add(rootGameObject);

		WaterGenerationObject water = new WaterGenerationObject("WATER_GENERATION", "/textures/waterDuDvMap.jpg", "/normalMaps/waterNormalMap.jpg", size, 0, 1000);
		rootGameObject.getGameObjectData().attachGameObjectNode(water);

		ArrayList<TerrainTextureGameObject> terrainTextureGameObjects = new ArrayList<>();

		terrainTextureGameObjects.add(new TerrainTextureGameObject(
				200,
				5000,
				"/textures/terrain2.jpg",
				"/normalMaps/grassNormal.jpg"
		));

		terrainTextureGameObjects.add(new TerrainTextureGameObject(
				700,
				5000,
				"/textures/rock.jpg",
				"/normalMaps/rockNormal.jpg"
		));

		terrainTextureGameObjects.add(new TerrainTextureGameObject(
				2000,
				1000,
				"/textures/snow.jpg",
				"/normalMaps/large.jpg"
		));

		TerrainGenerationObject terrainGenerationObject = new TerrainGenerationObject("AUTO_TERRAIN",
				5,
				1.7f,
				10,
				terrainTextureGameObjects,
				100,
				50,
				250,
				5000);

		rootGameObject.getGameObjectData().attachGameObjectNode(terrainGenerationObject);

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true);

		Vec3f ambientLight = new Vec3f(0.21f, 0.4f, 0.45f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, new Vec3f(0, 0.282f, 0.4f), 0.00003f);

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

		ArrayList<GESystem> geSystems = gameLoop.getGESystems();
		geSystems.add(new TerrainGeneration(10));
		geSystems.add(new WaterGeneration(10));

		gameLoop.getExecutorService().execute(gameLoop::render);
		gameLoop.getExecutorService().execute(gameLoop::update);

	}

	private TransformObject createFboGameObjects(ArrayList<GameObject> fboOneGameObjects) {

		GroupObject fboRootGameObject = new GroupObject();

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform transform = transformBuilder
				.setPosition(Vec3f.ZERO).build();

		TransformObject wholeSceneTransform = new TransformObject(transform);
		fboRootGameObject.getGameObjectData().attachGameObjectNode(wholeSceneTransform);

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


		SkyBoxObject skyBoxObject = new SkyBoxObject("/textures/skyBox.jpg", SkyboxType.MODEL, transformBuilder.setScale(100).build());
		fboRootGameObject.getGameObjectData().attachGameObjectNode(skyBoxObject);

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
				
		TransformObject cameraTransformGameObject = new TransformObject(cameraTransform);
		wholeSceneTransform.getGameObjectData().attachGameObjectNode(cameraTransformGameObject);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformGameObject.getGameObjectData().attachGameObjectNode(cameraObject);
		fboOneGameObjects.add(fboRootGameObject);


		return cameraTransformGameObject;
	}

	public void renderingToFBOs() {

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder()
				.setLockCursor(true);

		ArrayList<GameObject> fboOneGameObjects = new ArrayList<>();

		TransformObject fboCameraTransformObject = createFboGameObjects(fboOneGameObjects);

		ArrayList<GameObject> mainGameObjects = new ArrayList<>();

		GroupObject rootObject = new GroupObject();

		mainGameObjects.add(rootObject);

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform mainTransform = 	transformBuilder
				.setPosition(Vec3f.X).build();

		TransformObject wholeSceneTransform = new TransformObject(mainTransform);
		rootObject.getGameObjectData().attachGameObjectNode(wholeSceneTransform);

		GeometryBuilder circle = new GeometryBuilder("FBO_RENDERING_PANEL")
				.setGeometryType(GeometryType.CIRCLE)
				.setTriangleNumber(100)
				.setTransform(transformBuilder
						.setScale(10).build())
				.setTextureFboCameraName("fboCamera");


		GeometryGameObject geometryGameObject = new GeometryGameObject(circle);
		wholeSceneTransform.getGameObjectData().attachGameObjectNode(geometryGameObject);


		CameraBuilder cameraBuilder = new CameraBuilder("CAMERA")
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
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.005f, 0.1f);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformGameObject.getGameObjectData().attachGameObjectNode(cameraObject);


		DirectTransformController fboDirectTransformController = new DirectTransformController(fboCameraTransformObject, true, true, 0.005f, 0.1f);
		fboDirectTransformController.changeDefaultKeyMapping(new KeyMapping(
				265,
				264,
				263,
				262,
				266,
				267
		));

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
				Fog.NOFOG,
				ambientLight,
				skyboxAmbientLight
		);


		HashMap<String, ArrayList<GameObject>> layeredGameObjectsMap = new HashMap<>();

		layeredGameObjectsMap.put("MAIN_SCENE", mainGameObjects);
		layeredGameObjectsMap.put("FBO_SCENE_ONE", fboOneGameObjects);

		ArrayList<Scene> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainScene);
		sceneLayers.add(fboOneScene);

		GameLoop gameLoop = new GameLoop(sceneLayers,
				wip.build(),
				directTransformController,
				layeredGameObjectsMap);

		gameLoop.getInputSystem().addControl(fboDirectTransformController);

		gameLoop.getExecutorService().execute(gameLoop::render);
		gameLoop.getExecutorService().execute(gameLoop::update);


	}
/*
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
*/
/*
	public void picking() {

		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform transform = transformBuilder
				.setPosition(Vec3f.ZERO)
				.build();

		TransformObject wholeSceneTransform = new TransformObject(transform);
		rootGameObject.getGameObjectData().attachGameObjectNode(wholeSceneTransform);

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

		TransformObject cameraTransformGameObject = new TransformObject(cameraTransform);
		wholeSceneTransform.getGameObjectData().attachGameObjectNode(cameraTransformGameObject);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, false, true, 0.01f, 1);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformGameObject.getGameObjectData().attachGameObjectNode(cameraObject);
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

		GameLoop gameLoop = new GameLoop(sceneLayers,
				wip.build(),
				directTransformController,
				layeredGameObjectsMap);

		gameLoop.getExecutorService().execute(gameLoop::render);
		gameLoop.getExecutorService().execute(gameLoop::update);

		PickingSubscribable pickingSubscribable = new PickingSubscribable(gameObjects);
		gameLoop.getGameBus().register(pickingSubscribable);
		gameLoop.getExecutorService().execute(pickingSubscribable);

	}

	void cubeTerrain() {

		ArrayList<GameObject> gameObjects = new ArrayList<>();

		GroupObject rootGameObject = new GroupObject();

		int cubeSize = 2;

		TransformBuilder transformBuilder = new TransformBuilder();

		GeometryBuilder cubeSand = new GeometryBuilder("CUbE_SAND")
				.setGeometryType(GeometryType.MODEL)
				.setModelFile("\\models\\cube.obj")
				.setTexture("/textures/brickwall.jpg")
				.setNormalTexture("/normalMaps/brickwall_normal.jpg")
				.setTransform(transformBuilder.build());
				

		GeometryBuilder cubeGrass = new GeometryBuilder("CUBE_GRASS")
				.setGeometryType(GeometryType.MODEL)
				.setModelFile("\\models\\cube.obj")
				.setTexture("/textures/grass.png")
				.setNormalTexture("/normalMaps/sandNormalMap.jpg")
				.setTransform(transformBuilder.build());
				

		GeometryBuilder cubeSnow = new GeometryBuilder("CUBE_SNOW")
				.setGeometryType(GeometryType.MODEL)
				.setModelFile("\\models\\cube.obj")
				.setTexture("/textures/white.png")
				.setTransform(transformBuilder.build());
				

		GeometryBuilder cubeFire = new GeometryBuilder("CUBE_FIRE")
				.setGeometryType(GeometryType.MODEL)
				.setModelFile("\\models\\cube.obj")
				.setTexture("/textures/8k_venus_surface.jpg")
				.setNormalTexture("/normalMaps/sandNormalMap.jpg")
				.setTransform(transformBuilder.build());
				

		int segmentSize = 10;
		int hillHeight = 20;
		Perlin3D perlin3D = new Perlin3D(500, segmentSize);
		Perlin2Df perlin2D = new Perlin2Df(500, segmentSize);
		int size = 30;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				for (int k = 0; k < size; k++) {
					double point = perlin3D.getPoint(i, j, k);

					double weight = (k - (size / 2.0)) / (size / 2.0) - 0.15;

					if (point < (weight * weight * weight * weight)) {

						Transform transform = transformBuilder
								.setPosition(new Vec3f(i * cubeSize, j * cubeSize, k * cubeSize))
								.setScale(Vec3f.ONE).build();

						TransformObject transformObject = new TransformObject(transform);
						gameObjects.add(transformObject);

						if (k < 2) {
							GeometryGameObject geometryGameObject = new GeometryGameObject(cubeFire);
							transformObject.getGameObjectData().attachGameObjectNode(geometryGameObject);
						}
						if (k < size - 2) {
							GeometryGameObject geometryGameObject = new GeometryGameObject(cubeSand);
							transformObject.getGameObjectData().attachGameObjectNode(geometryGameObject);
						} else {
							GeometryGameObject geometryGameObject = new GeometryGameObject(cubeGrass);
							transformObject.getGameObjectData().attachGameObjectNode(geometryGameObject);
						}

					}
				}

				double point = (int) (perlin2D.getPoint(i, j) * hillHeight);

				for (int k = 0; k < point; k++) {

					Transform transform = transformBuilder
							.setPosition(new Vec3f(i * cubeSize, j * cubeSize, (k + size) * cubeSize))
							.build();

					TransformObject transformObject = new TransformObject(transform);
					rootGameObject.getGameObjectData().attachGameObjectNode(transformObject);

					if (k > 7) {
						GeometryGameObject geometryGameObject = new GeometryGameObject(cubeSnow);
						transformObject.getGameObjectData().attachGameObjectNode(geometryGameObject);
					} else {
						GeometryGameObject geometryGameObject = new GeometryGameObject(cubeGrass);
						transformObject.getGameObjectData().attachGameObjectNode(geometryGameObject);
					}

				}
			}
		}


		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				.build();

		CameraBuilder cameraBuilder = new CameraBuilder("Camera")
				.setFov(1.22173f)
				.setNear(0.01f)
				.setFar(100);

		TransformObject cameraTransformGameObject = new TransformObject(cameraTransform);
		rootGameObject.getGameObjectData().attachGameObjectNode(cameraTransformGameObject);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.005f, 0.5f);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformGameObject.getGameObjectData().attachGameObjectNode(cameraObject);
		gameObjects.add(rootGameObject);

		float width = (size * cubeSize);
		int space = 20;

		int counter = 0;
		for (int i = -1; i < width + 1; i+= space) {
			for (int j = -1; j < width + 1; j+= space) {
				for (int k = -1; k < width + 1; k+= space) {
					Transform t = transformBuilder
							.setPosition(new Vec3f(i, j, k))
							.build();

					LightingBuilder lightingBuilder = new LightingBuilder("Light" + counter++)
							.setLightingType(LightingType.POINT)
							.setColour(new Vec3f(i, j, k).scale(0.01f))
							.setIntensity(50);
					TransformObject ct = new TransformObject(t);
					gameObjects.add(ct);
					LightObject pointLightSceneObj = new LightObject(lightingBuilder);
					ct.getGameObjectData().attachGameObjectNode(pointLightSceneObj);
					counter++;
				}
			}
		}

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

		GameLoop gameLoop = new GameLoop(sceneLayers,
				wip.build(),
				directTransformController,
				layeredGameObjectsMap);

		gameLoop.getExecutorService().execute(gameLoop::render);
		gameLoop.getExecutorService().execute(gameLoop::update);

	}

	/*
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
*/
/*
	public void maze() {
		ArrayList<GameObject> gameObjects = new ArrayList<>();

		TransformBuilder transformBuilder = new TransformBuilder();

		Transform transform = transformBuilder.build();

		TransformObject wholeSceneTransform = new TransformObject(transform);

		gameObjects.add(wholeSceneTransform);

		GeometryBuilder meshGroupLight = new GeometryBuilder("MarsModel")
				.setGeometryType(GeometryType.MODEL)
				.setInvertedNormals(false)
				.setTexture("/textures/mars.jpg")
				.setTransform(transformBuilder
						.setScale(Vec3f.ONE).build());

		LightingBuilder directionalLight = new LightingBuilder("DirectionalLight")
				.setLightingType(LightingType.DIRECTIONAL)
				.setColour(new Vec3f(1.0f, 1.0f, 1.0f))
				.setDirection(new Vec3f(0.0f, 0.0f, -1.0f))
				.setIntensity(1);

		Transform build = new TransformBuilder()
				.setScale(new Vec3f(1000, 1000, 1000))
				.setRotation(QuaternionF.RotationY(Math.PI)).build();


		SkyBoxObject skyBoxObject = new SkyBoxObject("/textures/altimeterSphere.png", SkyboxType.SPHERE, build);
		gameObjects.add(skyBoxObject);

		Creation.CreateLight(directionalLight, wholeSceneTransform, new Vec3f(0.0f, -10.0f, 0), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation)
				.build();

		CameraBuilder cameraBuilder = new CameraBuilder("Camera")
				.setFov(1.22173f)
				.setNear(0.01f)
				.setFar(10000);

		TransformObject cameraTransformGameObject = new TransformObject(cameraTransform);
		gameObjects.add(cameraTransformGameObject);
		DirectTransformController directTransformController = new DirectTransformController(cameraTransformGameObject, true, true, 0.01f, 1);
		CameraObject cameraObject = new CameraObject(cameraBuilder);
		cameraTransformGameObject.getGameObjectData().attachGameObjectNode(cameraObject);

		int width = 50;
		int height = 50;

		RecursiveBackTracker recursiveBackTracker = new RecursiveBackTracker(width, height);
		ArrayList<Cell> visited = recursiveBackTracker.getVisited();

		// build mase
		GeometryBuilder cuboid = new GeometryBuilder("MAZE_WALL").setGeometryType(GeometryType.CUBOID).setNormalTexture("/normalMaps/sandNormalMap.jpg");

		// render diagonals
		for (int i = -1; i < width * 2 + 1; i += 2) {

			for (int j = -1; j < height * 2 + 1; j += 2) {

				Transform cellTransformcell = transformBuilder
						.setPosition(new Vec3f(i, j, 0)).build();

				TransformObject transformSceneGraphcell = new TransformObject(cellTransformcell);
				gameObjects.add(transformSceneGraphcell);

				GeometryGameObject meshSceneGraphcell = new GeometryGameObject(cuboid);
				transformSceneGraphcell.getGameObjectData().attachGameObjectNode(meshSceneGraphcell);

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
						.build();

				TransformObject transformSceneGraphcell = new TransformObject(cellTransformcell);
				gameObjects.add(transformSceneGraphcell);

				GeometryGameObject meshSceneGraphcell = new GeometryGameObject(cuboid);
				transformSceneGraphcell.getGameObjectData().attachGameObjectNode(meshSceneGraphcell);

			}

			if (!cell.getPathDirections().contains(south)) {

				Transform cellTransformcell = transformBuilder
						.setPosition(new Vec3f((cell.getPosition().getX() * 2), (cell.getPosition().getY() * 2) + 1, 0))
						.build();

				TransformObject transformSceneGraphcell = new TransformObject(cellTransformcell);
				gameObjects.add(transformSceneGraphcell);

				GeometryGameObject meshSceneGraphcell = new GeometryGameObject(cuboid);
				transformSceneGraphcell.getGameObjectData().attachGameObjectNode(meshSceneGraphcell);

			}

			if (!cell.getPathDirections().contains(west) && !cell.getPosition().equals(Vec2i.ZERO)) {

				Transform cellTransformcell = transformBuilder
						.setPosition(new Vec3f((cell.getPosition().getX() * 2) - 1, (cell.getPosition().getY() * 2), 0))
						.build();

				TransformObject transformSceneGraphcell = new TransformObject(cellTransformcell);
				gameObjects.add(transformSceneGraphcell);

				GeometryGameObject meshSceneGraphcell = new GeometryGameObject(cuboid);
				transformSceneGraphcell.getGameObjectData().attachGameObjectNode(meshSceneGraphcell);

			}

			if (!cell.getPathDirections().contains(east) && !cell.getPosition().equals(new Vec2i(width - 1, height - 1))) {

				Transform cellTransformcell = transformBuilder
						.setPosition(new Vec3f((cell.getPosition().getX() * 2) + 1, (cell.getPosition().getY() * 2), 0))
						.build();

				TransformObject transformSceneGraphcell = new TransformObject(cellTransformcell);
				gameObjects.add(transformSceneGraphcell);

				GeometryGameObject meshSceneGraphcell = new GeometryGameObject(cuboid);
				transformSceneGraphcell.getGameObjectData().attachGameObjectNode(meshSceneGraphcell);

			}

		}

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();


		Vec3f ambientLight = new Vec3f(0.0529f, 0.0808f, 0.0922f);
		Vec3f skyboxAmbientLight = new Vec3f(0.9f, 0.9f, 0.9f);
		Fog fog = new Fog(true, ambientLight, 0.0003f);

		Scene mainScene = new Scene(
				"MAIN_SCENE",
				new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl"),
				new Shader("/shaders/waterVertex.glsl", "/shaders/waterFragment.glsl"),
				new Shader("/shaders/skyboxVertex.glsl", "/shaders/skyboxFragment.glsl"),
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

		GameLoop gameLoop = new GameLoop(sceneLayers,
				wip.build(),
				directTransformController,
				layeredGameObjectsMap);

		gameLoop.getExecutorService().execute(gameLoop::render);
		gameLoop.getExecutorService().execute(gameLoop::update);
	}
*/
}
