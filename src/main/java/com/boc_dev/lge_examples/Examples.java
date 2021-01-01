package com.boc_dev.lge_examples;

import com.boc_dev.lge_core.GameLoop;
import com.boc_dev.lge_core.SceneLayer;
import com.boc_dev.lge_model.gcs.Registry;
import com.boc_dev.lge_model.generated.components.*;
import com.boc_dev.lge_model.generated.enums.*;
import com.boc_dev.lge_model.systems.GcsSystem;
import com.boc_dev.lge_systems.MaterialChangeSystem;
import com.boc_dev.lge_systems.MeshAddSystem;
import com.boc_dev.lge_systems.MeshRemoveSystem;
import com.boc_dev.lge_systems.boids.BoidSystem;
import com.boc_dev.lge_systems.control.SelectionSystem;
import com.boc_dev.lge_systems.control.TimerSystem;
import com.boc_dev.lge_systems.generation.*;
import com.boc_dev.graphics_library.WindowInitialisationParametersBuilder;
import com.boc_dev.graphics_library.objects.lighting.Fog;
import com.boc_dev.lge_systems.gui.ButtonSystem;
import com.boc_dev.lge_systems.gui.ListSystem;
import com.boc_dev.lge_systems.physics.ParticleSystem;
import com.boc_dev.lge_systems.physics.RigidBodyPhysicsSystem;
import com.boc_dev.lge_systems.gui.TextChangeSystem;
import com.boc_dev.maths.noise.Perlin2Df;
import com.boc_dev.maths.noise.Perlin3D;
import com.boc_dev.maths.objects.QuaternionF;
import com.boc_dev.maths.objects.matrix.Matrix4f;
import com.boc_dev.maths.objects.srt.Transform;
import com.boc_dev.maths.objects.srt.TransformBuilder;
import com.boc_dev.maths.objects.vector.Vec2i;
import com.boc_dev.maths.objects.vector.Vec3d;
import com.boc_dev.maths.objects.vector.Vec3f;

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
		//examples.orthographic();
		//examples.timerExample();
		//examples.boidsExample();
		//examples.meshTypeConversionExample();
		//examples.instancedRenderingExample();
		//examples.terrainGenerationExample();
		//examples.cubeWorldExample();
		//examples.mazeExample();
		//examples.pickingExample();
		//examples.twoBallsExample();
		//examples.twoLinesExample();
		//examples.bigBangExample();
		//examples.cubeSphereExample();
		//examples.materialChangeExample();
		//examples.rigidBodyCameraControlExample();
		//examples.rigidBodyWallExample();
		//examples.particleSimExample();
		//examples.particleSimGravityExample();
		//examples.springsExample();
		//examples.rigidBodyForcesExample();
		//examples.rigidBodyGravitationalPullExample();
		//examples.marchingCubesExample();
		examples.gui();

		// todo
		//examples.renderingToFBOs();
	}

	public void gui() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
		);

		ControllableObject controllableObject = new ControllableObject(
				mainSceneLayer.getRegistry(),
				"Camera controller",
				false,
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

		for (int i = 0; i < 5; i++) {

			for (int j = 0; j < 5; j++) {

				for (int k = 0; k < 5; k++) {


					Transform build = transformBuilder.reset().setPosition(new Vec3f(i * 4, j * 4, k * 4)).build();


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
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(false).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);



		// gui layer

		Vec3f guiAmbientLight = new Vec3f(1, 1, 1);

		SceneLayer guiSceneLayer = new SceneLayer(
				"GUI",
				guiAmbientLight,
				Fog.NOFOG
		);

		Transform guiCameraTransform = transformBuilder
				.setPosition(new Vec3f(-100, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation).build();

		CameraObject guiCameraObject = new CameraObject(
				guiSceneLayer.getRegistry(),
				"GuiCamera",
				CameraProjectionType.ORTHOGRAPHIC,
				CameraObjectType.PRIMARY,
				1000,
				0,
				100,
				-1000,
				100
		);

		TransformObject guiCameraTransformObject = new TransformObject(
				guiSceneLayer.getRegistry(),
				"CameraTransform",
				guiCameraTransform.getPosition(),
				guiCameraTransform.getRotation(),
				guiCameraTransform.getScale());

		guiCameraObject.getUpdater().setParent(guiCameraTransformObject).sendUpdate();

		Transform guiSceneTransform = transformBuilder.reset()
				.setPosition(new Vec3f(0, 4.4f, -2.5f))
				.setScale(new Vec3f(200, 200, 200))
				.build();

		TransformObject guiSceneTransformObject = new TransformObject(
				guiSceneLayer.getRegistry(),
				"TransformObject",
				guiSceneTransform.getPosition(),
				guiSceneTransform.getRotation(),
				guiSceneTransform.getScale());

		UUID selectedMaterial = createMaterial(guiSceneLayer, "material1", "/textures/black.png", "/normalMaps/waterNormalMap.jpg");

		ListObject listObject = new ListObject(
				guiSceneLayer.getRegistry(),
				"ListObject",
				1.1f
		);

		listObject.getUpdater().setParent(guiSceneTransformObject).sendUpdate();

		for (int i = 0; i < 6; i++) {

			Transform build = transformBuilder.reset()
					.build();

			TransformObject newTransformObject = new TransformObject(
					guiSceneLayer.getRegistry(),
					"TransformObject" + i,
					build.getPosition(),
					QuaternionF.RotationZ(Math.PI),
					build.getScale());

			GeometryObject newGeometryObject = new GeometryObject(
					guiSceneLayer.getRegistry(),
					"Geometry" + i,
					Matrix4f.Identity,
					basicMaterial,
					"DEFAULT_SQUARE"
			);

			SelectableObject selectableObject = new SelectableObject(
					guiSceneLayer.getRegistry(),
					"Selectable " + i,
					false,
					selectedMaterial,
					basicMaterial
			);

			PickableObject pickableObject = new PickableObject(
					guiSceneLayer.getRegistry(),
					"Pickable " + i,
					true
			);

			ButtonObject buttonObject = new ButtonObject(
					guiSceneLayer.getRegistry(),
					"buttonObj " + i,
					true,
					"Button Pressed " + i
			);

			Transform textTransform = transformBuilder.reset()
					.setPosition(new Vec3f(1, 0.5f, 0.8f))
					.build();

			TransformObject textTransformObject = new TransformObject(
					guiSceneLayer.getRegistry(),
					"TransformObject" + i,
					textTransform.getPosition(),
					QuaternionF.RotationZ(Math.PI),
					textTransform.getScale());

			TextObject guiTextObject = new TextObject(
					guiSceneLayer.getRegistry(),
					"ButtonText" + i,
					FontAlignment.CENTER,
					"montserrat_light",
					0.1f,
					"Button " + i
			);

			textTransformObject.getUpdater().setParent(newTransformObject).sendUpdate();
			guiTextObject.getUpdater().setParent(textTransformObject).sendUpdate();
			newGeometryObject.getUpdater().setParent(newTransformObject).sendUpdate();
			newTransformObject.getUpdater().setParent(listObject).sendUpdate();

			pickableObject.getUpdater().setParent(newGeometryObject).sendUpdate();
			selectableObject.getUpdater().setParent(newGeometryObject).sendUpdate();
			buttonObject.getUpdater().setParent(newGeometryObject).sendUpdate();

		}

		Transform textTransform = transformBuilder.reset()
				.setRotation(QuaternionF.Identity)
				.setPosition(new Vec3f(0, 970, -700))
				.build();

		TransformObject textTransformObject = new TransformObject(
				guiSceneLayer.getRegistry(),
				"TextTransformObject",
				textTransform.getPosition(),
				textTransform.getRotation(),
				textTransform.getScale());

		TextObject guiTextObject = new TextObject(
				guiSceneLayer.getRegistry(),
				"GameEngineTimeText",
				FontAlignment.BEGIN,
				"montserrat_light",
				30,
				"Hello, World!@!"
		);

		guiTextObject.getUpdater().setParent(textTransformObject).sendUpdate();

		Transform buttonResultTransform = transformBuilder.reset()
				.setRotation(QuaternionF.Identity)
				.setPosition(new Vec3f(0, 970, -790))
				.build();

		TransformObject buttonResultTransformObject = new TransformObject(
				guiSceneLayer.getRegistry(),
				"buttonResultTransformObject",
				buttonResultTransform.getPosition(),
				buttonResultTransform.getRotation(),
				buttonResultTransform.getScale());

		TextObject buttonResultTextObject = new TextObject(
				guiSceneLayer.getRegistry(),
				"buttonResult",
				FontAlignment.BEGIN,
				"montserrat_light",
				40,
				"Button Result"
		);

		buttonResultTextObject.getUpdater().setParent(buttonResultTransformObject).sendUpdate();

		guiSceneLayer.getGcsSystems().add((GcsSystem) new SelectionSystem());
		guiSceneLayer.getGcsSystems().add((GcsSystem) new ButtonSystem());
		guiSceneLayer.getGcsSystems().add((GcsSystem) new TextChangeSystem());
		guiSceneLayer.getGcsSystems().add((GcsSystem) new ListSystem());

		sceneLayers.add(guiSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void orthographic() {

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
				Vec3f.X.neg(),
				1,
				LightingType.DIRECTIONAL
		);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-100, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation).build();

//		CameraObject cameraObject = new CameraObject(
//				mainSceneLayer.getRegistry(),
//				"Camera",
//				CameraProjectionType.ORTHOGRAPHIC,
//				CameraObjectType.PRIMARY,
//				10000,
//				1.22f,
//				1080,
//				1,
//				1920
//		);

		CameraObject cameraObject = new CameraObject(
				mainSceneLayer.getRegistry(),
				"Camera",
				CameraProjectionType.ORTHOGRAPHIC,
				CameraObjectType.PRIMARY,
				1000,
				0,
				100,
				-1000,
				100
		);

		ControllableObject controllableObject = new ControllableObject(
				mainSceneLayer.getRegistry(),
				"Camera controller",
				true,
				true,
				0.01f,
				10);

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

		Transform sceneTransform = transformBuilder.reset()
				.setRotation(QuaternionF.RotationX(Math.PI / 4))
				.setScale(new Vec3f(100, 100, 100))
				.build();

		TransformObject sceneTransformObject = new TransformObject(
				mainSceneLayer.getRegistry(),
				"TransformObject",
				sceneTransform.getPosition(),
				sceneTransform.getRotation(),
				sceneTransform.getScale());

//		controllableObject.getUpdater().setParent(sceneTransformObject).sendUpdate();

//		TransformObject cubeTransform = new TransformObject(
//				mainSceneLayer.getRegistry(),
//				"CubeTransformObject",
//				new Vec3f(0, 0, 0),
//				quaternionY,
//				Vec3f.ONE.scale(200));
//
//		GeometryObject cube = new GeometryObject(
//				mainSceneLayer.getRegistry(),
//				"Cube",
//				Matrix4f.Identity,
//				basicMaterial,
//				"DEFAULT_SQUARE"
//		);
//		cube.getUpdater().setParent(cubeTransform).sendUpdate();

		for (int i = 0; i < 2; i++) {

			for (int j = 0; j < 1; j++) {

				Transform build = transformBuilder.reset()
						.setPosition(new Vec3f(0, i, j))
						.build();

				TransformObject newTransformObject = new TransformObject(
						mainSceneLayer.getRegistry(),
						"TransformObject" + i,
						build.getPosition(),
						QuaternionF.RotationZ(Math.PI),
						build.getScale());

				GeometryObject newGeometryObject = new GeometryObject(
						mainSceneLayer.getRegistry(),
						"Geometry" + i,
						//Matrix4f.Scale(new Vec3f(100, 100, 100)),
						Matrix4f.Identity,
						basicMaterial,
						"DEFAULT_SQUARE"
				);

//				SelectableObject selectableObject = new SelectableObject(
//						mainSceneLayer.getRegistry(),
//						"Selectable",
//						false,
//						selectedMaterial,
//						basicMaterial
//				);
//
//				PickableObject pickableObject = new PickableObject(
//						mainSceneLayer.getRegistry(),
//						"Pickable " + i + " " + j,
//						true
//				);


				newGeometryObject.getUpdater().setParent(newTransformObject).sendUpdate();
				newTransformObject.getUpdater().setParent(sceneTransformObject).sendUpdate();

//				if (i == 0 && j == 0) {
//					controllableObject.getUpdater().setParent(newTransformObject).sendUpdate();
//				}

//				pickableObject.getUpdater().setParent(newGeometryObject).sendUpdate();
//				selectableObject.getUpdater().setParent(newGeometryObject).sendUpdate();


			}
		}

		mainSceneLayer.getGcsSystems().add((GcsSystem) new SelectionSystem());

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(false).setWindowWidth(800).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void timerExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
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

		mainSceneLayer.getGcsSystems().add((GcsSystem) new TimerSystem());

		TimerObject timerObject = new TimerObject(
				mainSceneLayer.getRegistry(),
				"Timer",
				true,
				"Repeating function name",
				true,
				0,
				false,
				200

		);

		TimerObject setTimerObject = new TimerObject(
				mainSceneLayer.getRegistry(),
				"Timer",
				false,
				"Single set function name",
				false,
				500,
				false,
				300

		);

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void cubeWorldExample() {

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

		SkyBoxObject skyBoxObject = new SkyBoxObject(
				mainSceneLayer.getRegistry(),
				"SKY_BOX",
				500,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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
				1000,
				1.22f,
				1200,
				0.01f,
				1600
		);

		ControllableObject controllableObject = new ControllableObject(
				mainSceneLayer.getRegistry(),
				"Camera controller",
				true,
				true,
				0.01f,
				0.1f);
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

		int segmentSize = 10;
		int hillHeight = 20;
		Perlin3D perlin3D = new Perlin3D(500, segmentSize);
		Perlin2Df perlin2D = new Perlin2Df(500, segmentSize);
		int size = 50;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				for (int k = 0; k < size; k++) {
					double point = perlin3D.getPoint(Math.abs(i), Math.abs(j), Math.abs(k));

					double weight = (k - (size / 2.0)) / (size / 2.0) - 0.15;

					if (point < (weight * weight * weight * weight)) {

						createCube(transformBuilder,
								new Vec3f(i, j, k),
								mainSceneLayer,
								basicMaterial);

					}
				}

				double point = (int) (perlin2D.getPoint(i, j) * hillHeight);

				for (int k = 0; k < point; k++) {

					createCube(transformBuilder,
							new Vec3f(i, j, k + size),
							mainSceneLayer,
							basicMaterial);

//					if (k > 7) {
//						GeometryGameObject geometryGameObject = new GeometryGameObject(cubeSnow);
//						transformObject.getGameObjectData().attachGameObjectNode(geometryGameObject);
//					} else {
//						GeometryGameObject geometryGameObject = new GeometryGameObject(cubeGrass);
//						transformObject.getGameObjectData().attachGameObjectNode(geometryGameObject);
//					}

				}
			}
		}

		// make some lights
		Random random = new Random();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				for (int k = 0; k < 3; k++) {


					TransformObject pointLightTransformObject = new TransformObject(
							mainSceneLayer.getRegistry(),
							"CameraTransform",
							new Vec3f(i * 15 - 1, j * 15 - 1, k * 25 - 1),
							QuaternionF.Identity,
							Vec3f.ONE);

					LightObject pointLight = new LightObject(
							mainSceneLayer.getRegistry(),
							"PointLight",
							0.25f,
							0.5f,
							1f,
							new Vec3f(random.nextFloat(), random.nextFloat(), random.nextFloat()),
							0.1f,
							Vec3f.Z.neg(),
							50,
							LightingType.POINT
					);

					pointLight.getUpdater().setParent(pointLightTransformObject).sendUpdate();

				}
			}
		}

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

	public void marchingCubesExample() {

		TransformBuilder transformBuilder = new TransformBuilder();

		Vec3f ambientLight = new Vec3f(0.1f, 0.1f, 0.1f);
		Fog fog = new Fog(true, new Vec3f(0, 0, 0), 0.05f);
		//Fog fog = new Fog(true, new Vec3f(0, 0, 0), 0.00001f);

		SceneLayer mainSceneLayer = new SceneLayer(
				"MAIN",
				ambientLight,
				fog
		);

		LightObject lightObject = new LightObject(
				mainSceneLayer.getRegistry(),
				"MyFirstLight",
				1,
				0.25f,
				0.5f,
				Vec3f.ONE,
				0.2f,
				Vec3f.Z.neg(),
				100,
				LightingType.SPOT
		);

		SkyBoxObject skyBoxObject = new SkyBoxObject(
				mainSceneLayer.getRegistry(),
				"SKY_BOX",
				999,
				SkyboxType.SPHERE,
				"/textures/black.png"
		);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(0, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation).build();

		CameraObject cameraObject = new CameraObject(
				mainSceneLayer.getRegistry(),
				"Camera",
				CameraProjectionType.PERSPECTIVE,
				CameraObjectType.PRIMARY,
				1000,
				1.22f,
				1200,
				0.01f,
				1600
		);

		ControllableObject controllableObject = new ControllableObject(
				mainSceneLayer.getRegistry(),
				"Camera controller",
				true,
				true,
				0.01f,
				0.1f);
		TransformObject cameraTransformObject = new TransformObject(
				mainSceneLayer.getRegistry(),
				"CameraTransform",
				cameraTransform.getPosition(),
				cameraTransform.getRotation(),
				cameraTransform.getScale());

		LightObject pointLight = new LightObject(
				mainSceneLayer.getRegistry(),
				"DirectionLight",
				0.25f,
				0.5f,
				1f,
				new Vec3f(1, 1, 1),
				0.1f,
				Vec3f.X,
				1,
				LightingType.POINT
		);
		pointLight.getUpdater().setParent(cameraTransformObject).sendUpdate();

		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		UUID material = createMaterial(mainSceneLayer, "material", "/textures/small_wall_texture.png", "/normalMaps/small_wall_normal_map.png");

		MarchingCubeGenerationObject marchingCubeGenerationObject = new MarchingCubeGenerationObject(
				mainSceneLayer.getRegistry(),
				"MarchingGen",
				10,
				4,
				material
		);
		marchingCubeGenerationObject.getUpdater().setParent(cameraTransformObject).sendUpdate();


		mainSceneLayer.getGcsSystems().add((GcsSystem) new MarchingCubesGeneration());

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true).setWindowWidth(800).setWindowHeight(600).setDebug(true);
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void terrainGenerationExample() {

		TransformBuilder transformBuilder = new TransformBuilder();
		Vec3f ambientLight = new Vec3f(0.1f, 0.1f, 0.1f);
		Fog fog = new Fog(true, ambientLight, 0.0005f);

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
				0.05f,
				Vec3f.Z.neg(),
				10000000,
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
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
		);

		ControllableObject controllableObject = new ControllableObject(
				mainSceneLayer.getRegistry(),
				"Camera controller",
				true,
				true,
				0.01f,
				5);
		TransformObject cameraTransformObject = new TransformObject(
				mainSceneLayer.getRegistry(),
				"CameraTransform",
				cameraTransform.getPosition(),
				cameraTransform.getRotation(),
				cameraTransform.getScale());

		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		mainSceneLayer.getGcsSystems().add((GcsSystem) new TerrainGeneration());
		mainSceneLayer.getGcsSystems().add((GcsSystem) new WaterGeneration());

		MaterialObject materialObject = new MaterialObject(
				mainSceneLayer.getRegistry(),
				"Material",
				new Vec3f(1, 1, 1),
				1,
				1,
				new Vec3f(1, 1, 1)
		);

		TextureObject textureObjectVisual = new TextureObject(
				mainSceneLayer.getRegistry(),
				"VisualTextureOne",
				"/textures/grassTile.jpg"
		);

//		NormalMapObject normalMapObject = new NormalMapObject(
//				mainSceneLayer.getRegistry(),
//				"NormalTextureOne",
//				"/normalMaps/grassNormalTile.png"
//		);

		textureObjectVisual.getUpdater().setParent(materialObject).sendUpdate();
//		normalMapObject.getUpdater().setParent(materialObject).sendUpdate();

		TerrainGenerationObject terrainGenerationObject = new TerrainGenerationObject(
				mainSceneLayer.getRegistry(),
				"TerrainGenerationObject",
				100,
				5,
				31,
				12,
				1.7f,
				materialObject.getUuid(),
				5,
				6
		);

		terrainGenerationObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		WaterGenerationObject waterGenerationObject = new WaterGenerationObject(
				mainSceneLayer.getRegistry(),
				"water",
				1024,
				32,
				0
		);
		waterGenerationObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
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


					Transform build = transformBuilder.reset().setPosition(new Vec3f(i * 4, j * 4, k * 4)).build();


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
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void rigidBodyCameraControlExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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

		ImpulseControllableObject controllableObject = new ImpulseControllableObject(
				mainSceneLayer.getRegistry(),
				"Camera controller",
				0.01f,
				true,
				true,
				0.05f);

		TransformObject cameraTransformObject = new TransformObject(
				mainSceneLayer.getRegistry(),
				"CameraTransform",
				cameraTransform.getPosition(),
				cameraTransform.getRotation(),
				cameraTransform.getScale());

		RigidBodyObject rigidBodyObject = new RigidBodyObject(
				mainSceneLayer.getRegistry(),
				"RigidBodyObject",
				Vec3d.ZERO,
				Vec3d.ONE.scale(1.2f),
				Vec3d.ZERO,
				1,
				RigidBodyObjectType.SPHERE
		);

		ImpulseObject impulseObject = new ImpulseObject(
				mainSceneLayer.getRegistry(),
				"ImpulseObject",
				Vec3d.ZERO,
				Vec3d.ZERO
		);

		rigidBodyObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		impulseObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		UUID basicMaterial = createBasicMaterial(mainSceneLayer);

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

				createRigidBody(
						mainSceneLayer.getRegistry(),
						basicMaterial,
						new Vec3f(5, (float) (j * 3.0 - 2 * i / 3.0), (float) (i * 15)),
						mom,
						ang,
						RigidBodyObjectType.SPHERE
				);
			}
		}

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		//wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		mainSceneLayer.getGcsSystems().add((GcsSystem) new RigidBodyPhysicsSystem());

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void mazeExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
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

		int width = 20;
		int height = 20;

		RecursiveBackTracker recursiveBackTracker = new RecursiveBackTracker(width, height);
		ArrayList<Cell> visited = recursiveBackTracker.getVisited();

		// render diagonals
		for (int i = -1; i < width * 2 + 1; i += 2) {

			for (int j = -1; j < height * 2 + 1; j += 2) {

				createCube(transformBuilder, new Vec3f(i, j, 0), mainSceneLayer, basicMaterial);

			}

		}

		Vec2i north = new Vec2i(0, -1);
		Vec2i west = new Vec2i(-1, 0);
		Vec2i south = new Vec2i(0, 1);
		Vec2i east = new Vec2i(1, 0);

		// render walls
		for (Cell cell : visited) {

			if (!cell.getPathDirections().contains(north)) {

				createCube(transformBuilder, new Vec3f((cell.getPosition().getX() * 2), (cell.getPosition().getY() * 2) - 1, 0), mainSceneLayer, basicMaterial);

			}

			if (!cell.getPathDirections().contains(south)) {

				createCube(transformBuilder, new Vec3f((cell.getPosition().getX() * 2), (cell.getPosition().getY() * 2) + 1, 0), mainSceneLayer, basicMaterial);

			}

			if (!cell.getPathDirections().contains(west) && !cell.getPosition().equals(Vec2i.ZERO)) {

				createCube(transformBuilder, new Vec3f((cell.getPosition().getX() * 2) - 1, (cell.getPosition().getY() * 2), 0), mainSceneLayer, basicMaterial);

			}

			if (!cell.getPathDirections().contains(east) && !cell.getPosition().equals(new Vec2i(width - 1, height - 1))) {

				createCube(transformBuilder, new Vec3f((cell.getPosition().getX() * 2) + 1, (cell.getPosition().getY() * 2), 0), mainSceneLayer, basicMaterial);

			}

		}

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

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
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
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

		for (int i = 0; i < 15; i++) {

			for (int j = 0; j < 15; j++) {

				for (int k = 0; k < 15; k++) {

					Transform build = transformBuilder.reset().setPosition(new Vec3f(i * 4, j * 4, k * 4)).build();

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
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setInstanceArraySizeLimit(5000);

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
				"/textures/bw_gradient_skybox.png"
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

					Transform build = transformBuilder.reset().setPosition(new Vec3f(i * 4, j * 4, k * 4)).build();

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

	public void pickingExample() {

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
				500,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
		);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-50, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation).build();

		CameraObject cameraObject = new CameraObject(
				mainSceneLayer.getRegistry(),
				"Camera",
				CameraProjectionType.PERSPECTIVE,
				CameraObjectType.PRIMARY,
				1000,
				1.22f,
				800,
				0.1f,
				1000
		);

		ControllableObject controllableObject = new ControllableObject(
				mainSceneLayer.getRegistry(),
				"Camera controller",
				false,
				true,
				0.01f,
				0.5f);
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
		UUID selectedMaterial = createMaterial(mainSceneLayer, "material4", "/textures/grassTile.jpg", "/normalMaps/plastic-normal.jpg");

		Random random = new Random();

		for (int i = 0; i < 10; i++) {

			for (int j = 0; j < 10; j++) {

				for (int k = 0; k < 10; k++) {


					Transform build = transformBuilder.reset().setPosition(new Vec3f(i * 4, j * 4, k * 4)).build();


					TransformObject newTransformObject = new TransformObject(
							mainSceneLayer.getRegistry(),
							"TransformObject" + i,
							build.getPosition(),
							build.getRotation(),
							build.getScale());

					GeometryObject newGeometryObject = new GeometryObject(
							mainSceneLayer.getRegistry(),
							"Geometry " + i + " " + j + " " + k,
							Matrix4f.Identity,
							basicMaterial,
							"DEFAULT_SPHERE"
					);

					SelectableObject selectableObject = new SelectableObject(
							mainSceneLayer.getRegistry(),
							"Selectable",
							false,
							selectedMaterial,
							basicMaterial
					);

					PickableObject pickableObject = new PickableObject(
							mainSceneLayer.getRegistry(),
							"Pickable Boid number " + i + " " + j + " " + k,
							true
					);

					newGeometryObject.getUpdater().setParent(newTransformObject).sendUpdate();
					pickableObject.getUpdater().setParent(newGeometryObject).sendUpdate();
					selectableObject.getUpdater().setParent(newGeometryObject).sendUpdate();

				}
			}
		}

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(false).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(false).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		mainSceneLayer.getGcsSystems().add((GcsSystem) new SelectionSystem());

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();

	}

	public void twoBallsExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
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

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();


		createRigidBody(
				mainSceneLayer.getRegistry(),
				basicMaterial,
				new Vec3f(0, 0, -5),
				Vec3d.Z.scale(1),
				Vec3d.X.scale(0.1),
				RigidBodyObjectType.SPHERE
		);

		createRigidBody(
				mainSceneLayer.getRegistry(),
				basicMaterial,
				new Vec3f(0, 0, 5),
				Vec3d.Z.scale(-1),
				Vec3d.X.scale(0.1),
				RigidBodyObjectType.SPHERE
		);

		mainSceneLayer.getGcsSystems().add((GcsSystem) new RigidBodyPhysicsSystem());


		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void twoLinesExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
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

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();


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

				createRigidBody(
						mainSceneLayer.getRegistry(),
						basicMaterial,
						new Vec3f(5, (float) (j * 3.0 - 2 * i / 3.0), (float) (i * 8)),
						mom,
						ang,
						RigidBodyObjectType.SPHERE
				);
			}
		}

		mainSceneLayer.getGcsSystems().add((GcsSystem) new RigidBodyPhysicsSystem());


		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void bigBangExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
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

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();


		Random random = new Random();
		int cubeSideLength = 5;
		for (int k = -cubeSideLength; k < cubeSideLength; k++) {
			for (int j = -cubeSideLength; j < cubeSideLength; j++) {
				for (int i = -cubeSideLength; i < cubeSideLength; i++) {
					Vec3d mom = Vec3d.X.scale(-i).add(Vec3d.Y.scale(-j)).add(Vec3d.Z.scale(-k));
					Vec3d angMom = Vec3d.X.scale(random.nextInt(10) - 4).add(Vec3d.Y.scale(random.nextInt(10) - 4)).add(Vec3d.Z.scale(random.nextInt(10) - 4));
					UUID uuid = UUID.randomUUID();
					createRigidBody(
							mainSceneLayer.getRegistry(),
							basicMaterial,
							new Vec3f(i * 10, j * 10, k * 10),
							mom,
							angMom.scale(0.02),
							RigidBodyObjectType.SPHERE);
				}
			}
		}


		mainSceneLayer.getGcsSystems().add((GcsSystem) new RigidBodyPhysicsSystem());


		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void rigidBodyForcesExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
		);
//		CameraObject cameraObject = new CameraObject(
//				mainSceneLayer.getRegistry(),
//				"Camera",
//				CameraProjectionType.PERSPECTIVE,
//				CameraObjectType.PRIMARY,
//				10000,
//				1.22f,
//				1080,
//				1,
//				1920
//		);

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

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();


		Random random = new Random();
		int cubeSideLength = 5;
		for (int k = -cubeSideLength; k < cubeSideLength; k++) {
			for (int j = -cubeSideLength; j < cubeSideLength; j++) {
				for (int i = -cubeSideLength; i < cubeSideLength; i++) {
					Vec3d mom = new Vec3d(-i * 2, -j * 2, -k + 100);
					Vec3d angMom = Vec3d.X.scale(random.nextInt(10) - 4).add(Vec3d.Y.scale(random.nextInt(10) - 4)).add(Vec3d.Z.scale(random.nextInt(10) - 4));
					RigidBodyObject rigidBody = createRigidBody(
							mainSceneLayer.getRegistry(),
							basicMaterial,
							new Vec3f(i * 10, j * 10, k * 10),
							mom,
							angMom.scale(0.02),
							RigidBodyObjectType.SPHERE);

					GravityObject gravity = new GravityObject(
							mainSceneLayer.getRegistry(),
							"Gravity",
							9.81f,
							true
					);

					gravity.getUpdater().setParent(rigidBody).sendUpdate();

					ViscousDragObject viscousDragObject = new ViscousDragObject(
							mainSceneLayer.getRegistry(),
							"ViscousDragObject",
							-0.1f
					);

					viscousDragObject.getUpdater().setParent(rigidBody).sendUpdate();

				}
			}
		}


		mainSceneLayer.getGcsSystems().add((GcsSystem) new RigidBodyPhysicsSystem());


		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void rigidBodyGravitationalPullExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
		);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-100, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation).build();

		CameraObject cameraObject = new CameraObject(
				mainSceneLayer.getRegistry(),
				"Camera",
				CameraProjectionType.PERSPECTIVE,
				CameraObjectType.PRIMARY,
				10000,
				1.22f,
				800,
				1,
				1000
		);
//		CameraObject cameraObject = new CameraObject(
//				mainSceneLayer.getRegistry(),
//				"Camera",
//				CameraProjectionType.PERSPECTIVE,
//				CameraObjectType.PRIMARY,
//				10000,
//				1.22f,
//				1080,
//				1,
//				1920
//		);

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

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();


		Random random = new Random();
		int cubeSideLength = 4;
		for (int k = -cubeSideLength; k < cubeSideLength; k++) {
			for (int j = -cubeSideLength; j < cubeSideLength; j++) {
				for (int i = -cubeSideLength; i < cubeSideLength; i++) {
					Vec3d mom = new Vec3d(random.nextInt(20) - 10, random.nextInt(20) - 10, random.nextInt(20) - 10);
					Vec3d angMom = Vec3d.X.scale(random.nextInt(10) - 4).add(Vec3d.Y.scale(random.nextInt(10) - 4)).add(Vec3d.Z.scale(random.nextInt(10) - 4));
					RigidBodyObject rigidBody = createRigidBody(
							mainSceneLayer.getRegistry(),
							basicMaterial,
							new Vec3f(i * 10, j * 10, k * 10),
							mom,
							angMom.scale(0.02),
							RigidBodyObjectType.SPHERE);

					GravityObject gravity = new GravityObject(
							mainSceneLayer.getRegistry(),
							"Gravity",
							5,
							false
					);

					gravity.getUpdater().setParent(rigidBody).sendUpdate();

				}
			}
		}


		mainSceneLayer.getGcsSystems().add((GcsSystem) new RigidBodyPhysicsSystem());


		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void cubeSphereExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
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

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();


		createRigidBody(
				mainSceneLayer.getRegistry(),
				basicMaterial,
				new Vec3f(0, 1, 5),
				Vec3d.Z.neg(),
				Vec3d.ZERO,
				RigidBodyObjectType.SPHERE);

		createRigidBody(
				mainSceneLayer.getRegistry(),
				basicMaterial,
				new Vec3f(0, 0, -5),
				Vec3d.ZERO,
				Vec3d.ZERO,
				RigidBodyObjectType.CUBOID);


		mainSceneLayer.getGcsSystems().add((GcsSystem) new RigidBodyPhysicsSystem());


		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void materialChangeExample() {

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
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
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
		createMaterial(mainSceneLayer, "material1", "/textures/black.png", "/normalMaps/waterNormalMap.jpg");
		createMaterial(mainSceneLayer, "material2", "/textures/brickwall.jpg", "/normalMaps/brickwall_normal.jpg");
		createMaterial(mainSceneLayer, "material3", "/textures/grassTile.jpg", "/normalMaps/waterNormalMap.jpg");
		createMaterial(mainSceneLayer, "material4", "/textures/small_wall_texture.png", "/normalMaps/plastic-normal.jpg");

		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {

				Transform build = transformBuilder.reset().setPosition(new Vec3f(0, i * 4, j * 4)).build();

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

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setDebug(true);
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		mainSceneLayer.getGcsSystems().add((GcsSystem) new MaterialChangeSystem());

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void rigidBodyWallExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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
				0.5f);

		TransformObject cameraTransformObject = new TransformObject(
				mainSceneLayer.getRegistry(),
				"CameraTransform",
				cameraTransform.getPosition(),
				cameraTransform.getRotation(),
				cameraTransform.getScale());

		RigidBodyObject rigidBodyObject = new RigidBodyObject(
				mainSceneLayer.getRegistry(),
				"RigidBodyObject",
				Vec3d.ZERO,
				Vec3d.ONE.scale(10),
				Vec3d.ZERO,
				1000,
				RigidBodyObjectType.SPHERE
		);

		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		rigidBodyObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		UUID basicMaterial = createBasicMaterial(mainSceneLayer);

		int width = 10;
		int height = 10;
		int depth = 10;

		// render diagonals
		for (int i = 0; i < width; i++) {

			for (int j = 0; j < height; j++) {

				for (int k = 0; k < depth; k++) {

					createRigidBody(
							mainSceneLayer.getRegistry(),
							basicMaterial,
							new Vec3f(k * 1.2f, j * 1.2f, i * 1.2f),
							Vec3d.ZERO,
							Vec3d.ZERO,
							RigidBodyObjectType.SPHERE
					);

				}
			}

		}

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		//wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		mainSceneLayer.getGcsSystems().add((GcsSystem) new RigidBodyPhysicsSystem());

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void particleSimExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
		);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-10, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation).build();

//		CameraObject cameraObject = new CameraObject(
//				mainSceneLayer.getRegistry(),
//				"Camera",
//				CameraProjectionType.PERSPECTIVE,
//				CameraObjectType.PRIMARY,
//				10000,
//				1.22f,
//				800,
//				1,
//				1000
//		);
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

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		Random random = new Random();
		int cubeSideLength = 5;
		for (int k = -cubeSideLength; k < cubeSideLength; k++) {
			for (int j = -cubeSideLength; j < cubeSideLength; j++) {
				for (int i = -cubeSideLength; i < cubeSideLength; i++) {
					Vec3d velocity = new Vec3d(-i * 5, -j * 5, -j);
					//Vec3d velocity = Vec3d.ZERO;

					ParticleBodyObject particle = createParticle(
							mainSceneLayer.getRegistry(),
							basicMaterial,
							new Vec3f(i * 10, j * 10, k * 10),
							velocity,
							100);

					if (random.nextBoolean()) {

						// gravity
						GravityObject particleSimpleGravityObject = new GravityObject(
								mainSceneLayer.getRegistry(),
								"Gravity",
								9.81f,
								true
						);
						particleSimpleGravityObject.getUpdater().setParent(particle).sendUpdate();

						// viscous drag
						ViscousDragObject particleViscousDragObject = new ViscousDragObject(
								mainSceneLayer.getRegistry(),
								"particleViscousDragObject",
								0.5f
						);
						particleViscousDragObject.getUpdater().setParent(particle).sendUpdate();

					}

				}
			}
		}

		mainSceneLayer.getGcsSystems().add((GcsSystem) new ParticleSystem());


		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		//wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void particleSimGravityExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
		);

		Transform cameraTransform = transformBuilder
				.setPosition(new Vec3f(-100, 0, 0))
				.setScale(Vec3f.ONE)
				.setRotation(cameraRotation).build();

//		CameraObject cameraObject = new CameraObject(
//				mainSceneLayer.getRegistry(),
//				"Camera",
//				CameraProjectionType.PERSPECTIVE,
//				CameraObjectType.PRIMARY,
//				10000,
//				1.22f,
//				800,
//				1,
//				1000
//		);
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

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();

		Random random = new Random();
		int cubeSideLength = 5;
		for (int k = -cubeSideLength; k < cubeSideLength; k++) {
			for (int j = -cubeSideLength; j < cubeSideLength; j++) {
				for (int i = -cubeSideLength; i < cubeSideLength; i++) {
					Vec3d velocity = new Vec3d(random.nextInt(10) - 5, random.nextInt(10) - 5, random.nextInt(10) - 5);
					//Vec3d velocity = Vec3d.ZERO;

					ParticleBodyObject particle = createParticle(
							mainSceneLayer.getRegistry(),
							basicMaterial,
							new Vec3f(i * 10, j * 10, k * 10),
							velocity,
							100);

					// gravity
					GravityObject particleSimpleGravityObject = new GravityObject(
							mainSceneLayer.getRegistry(),
							"Gravity",
							1,
							false
					);
					particleSimpleGravityObject.getUpdater().setParent(particle).sendUpdate();

				}
			}
		}

		mainSceneLayer.getGcsSystems().add((GcsSystem) new ParticleSystem());


		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	public void springsExample() {

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
				1000,
				SkyboxType.SPHERE,
				"/textures/bw_gradient_skybox.png"
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
				800,
				1,
				1000
		);
//		CameraObject cameraObject = new CameraObject(
//				mainSceneLayer.getRegistry(),
//				"Camera",
//				CameraProjectionType.PERSPECTIVE,
//				CameraObjectType.PRIMARY,
//				10000,
//				1.22f,
//				1080,
//				1,
//				1920
//		);

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

		lightObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		cameraObject.getUpdater().setParent(cameraTransformObject).sendUpdate();
		controllableObject.getUpdater().setParent(cameraTransformObject).sendUpdate();


		for (int i = 0; i < 2; i++) {
			ParticleBodyObject particle = createParticle(
					mainSceneLayer.getRegistry(),
					basicMaterial,
					new Vec3f(0, i * 10, 0),
					Vec3d.ZERO,
					1);

			// viscous drag
			ParticleSpringObject particleSpringObject = new ParticleSpringObject(
					mainSceneLayer.getRegistry(),
					"particleViscousDragObject",
					-0.05f,
					10,
					-1
			);
			particleSpringObject.getUpdater().setParent(particle).sendUpdate();
		}

		for (int i = 0; i < 2; i++) {
			ParticleBodyObject particle = createParticle(
					mainSceneLayer.getRegistry(),
					basicMaterial,
					new Vec3f(0, i * 10, 10),
					Vec3d.ZERO,
					1);

			// viscous drag
			ParticleSpringObject particleSpringObject = new ParticleSpringObject(
					mainSceneLayer.getRegistry(),
					"particleViscousDragObject",
					-0.05f,
					10,
					-1
			);
			particleSpringObject.getUpdater().setParent(particle).sendUpdate();
		}

		ParticleBodyObject particle = createParticle(
				mainSceneLayer.getRegistry(),
				basicMaterial,
				new Vec3f(0, 5, 5),
				Vec3d.ZERO,
				1000);

		// viscous drag
		ParticleSpringObject particleSpringObject = new ParticleSpringObject(
				mainSceneLayer.getRegistry(),
				"particleViscousDragObject",
				-0.05f,
				10,
				-1
		);
		particleSpringObject.getUpdater().setParent(particle).sendUpdate();


		mainSceneLayer.getGcsSystems().add((GcsSystem) new ParticleSystem());


		WindowInitialisationParametersBuilder wip = new WindowInitialisationParametersBuilder();
		//wip.setLockCursor(true).setWindowWidth(1920).setWindowHeight(1080).setFullScreen(true);
		wip.setLockCursor(true).setWindowWidth(1000).setWindowHeight(800).setDebug(true);

		ArrayList<SceneLayer> sceneLayers = new ArrayList<>();
		sceneLayers.add(mainSceneLayer);

		GameLoop gameLoop = new GameLoop(
				sceneLayers,
				wip.build()
		);

		gameLoop.start();


	}

	/*
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


	}*/



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
	}*/

	private UUID createMaterial(SceneLayer sceneLayer, String name, String texture, String normalTexture) {
		MaterialObject materialObject = new MaterialObject(
				sceneLayer.getRegistry(),
				name,
				new Vec3f(1, 1, 1),
				1,
				1,
				new Vec3f(1, 1, 1)
		);

		TextureObject textureObjectVisual = new TextureObject(
				sceneLayer.getRegistry(),
				texture,
				texture
		);

		NormalMapObject normalMapObject = new NormalMapObject(
				sceneLayer.getRegistry(),
				normalTexture,
				normalTexture
		);

		textureObjectVisual.getUpdater().setParent(materialObject).sendUpdate();
		normalMapObject.getUpdater().setParent(materialObject).sendUpdate();

		return materialObject.getUuid();
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

	public void createCube(TransformBuilder transformBuilder, Vec3f position, SceneLayer mainSceneLayer, UUID materialUUID) {
		//new Vec3f(i*4, j*4, k*4)
		Transform build = transformBuilder.reset().setPosition(position).build();


		TransformObject newTransformObject = new TransformObject(
				mainSceneLayer.getRegistry(),
				"TransformObject",
				build.getPosition(),
				build.getRotation(),
				build.getScale());

		GeometryObject newGeometryObject = new GeometryObject(
				mainSceneLayer.getRegistry(),
				"Geometry",
				Matrix4f.Identity,
				materialUUID,
				"DEFAULT_CUBE"
		);

		newGeometryObject.getUpdater().setParent(newTransformObject).sendUpdate();

	}

	private ParticleBodyObject createParticle(Registry registry,
	                                          UUID basicMaterial,
	                                          Vec3f position,
	                                          Vec3d velocity,
	                                          float mass) {
		TransformObject transformObject1 = new TransformObject(
				registry,
				"TransformObject",
				position,
				QuaternionF.Identity,
				Vec3f.ONE);

		String modelFile = "DEFAULT_SPHERE";

		GeometryObject geometryObject1 = new GeometryObject(
				registry,
				"Geometry",
				Matrix4f.Identity,
				basicMaterial,
				modelFile
		);

		ParticleBodyObject particleBodyObject = new ParticleBodyObject(
				registry,
				"ParticleBody",
				mass,
				velocity
		);
		geometryObject1.getUpdater().setParent(transformObject1).sendUpdate();
		particleBodyObject.getUpdater().setParent(transformObject1).sendUpdate();

		return particleBodyObject;
	}

	private RigidBodyObject createRigidBody(Registry registry,
	                                        UUID basicMaterial,
	                                        Vec3f position,
	                                        Vec3d linearMomentum,
	                                        Vec3d angularMomentum,
	                                        RigidBodyObjectType rigidBodyObjectType) {
		TransformObject transformObject1 = new TransformObject(
				registry,
				"TransformObject",
				position,
				QuaternionF.Identity,
				Vec3f.ONE);

		String modelFile = "DEFAULT_SPHERE";
		if (rigidBodyObjectType.equals(RigidBodyObjectType.CUBOID)) {
			modelFile = "DEFAULT_CUBE";
		}

		GeometryObject geometryObject1 = new GeometryObject(
				registry,
				"Geometry",
				Matrix4f.Identity,
				basicMaterial,
				modelFile
		);

		RigidBodyObject rigidBodyObject1 = new RigidBodyObject(
				registry,
				"RigidBodyObject",
				angularMomentum,
				// todo why is this just off correct?
				Vec3d.ONE.scale(1.2),
				linearMomentum,
				1,
				rigidBodyObjectType
		);
		geometryObject1.getUpdater().setParent(transformObject1).sendUpdate();
		rigidBodyObject1.getUpdater().setParent(transformObject1).sendUpdate();

		return rigidBodyObject1;
	}
}
