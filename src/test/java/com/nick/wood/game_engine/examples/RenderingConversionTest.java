//package com.nick.wood.game_engine.examples;
//
//import com.nick.wood.game_engine.model.game_objects.*;
//import com.nick.wood.game_engine.model.utils.Creation;
//import com.nick.wood.graphics_library.lighting.PointLight;
//import com.nick.wood.graphics_library.objects.Camera;
//import com.nick.wood.graphics_library.objects.mesh_objects.MeshBuilder;
//import com.nick.wood.graphics_library.objects.mesh_objects.MeshObject;
//import com.nick.wood.graphics_library.objects.mesh_objects.MeshType;
//import com.nick.wood.graphics_library.objects.mesh_objects.TextItem;
//import com.nick.wood.graphics_library.objects.render_scene.RenderGraph;
//import com.nick.wood.maths.objects.QuaternionF;
//import com.nick.wood.maths.objects.matrix.Matrix4f;
//import com.nick.wood.maths.objects.srt.Transform;
//import com.nick.wood.maths.objects.srt.TransformBuilder;
//import com.nick.wood.maths.objects.vector.Vec3f;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//class RenderingConversionTest {
//
//	private final QuaternionF quaternionX = QuaternionF.RotationX((float) Math.toRadians(-90));
//	private final QuaternionF quaternionY = QuaternionF.RotationY((float) Math.toRadians(180));
//	private final QuaternionF quaternionZ = QuaternionF.RotationZ((float) Math.toRadians(90));
//	private final QuaternionF cameraRotation = quaternionZ.multiply(quaternionY).multiply(quaternionX);
//
//	@BeforeEach
//	void setUp() {
//	}
//
//	@AfterEach
//	void tearDown() {
//	}
//
//	@Test
//	void convertToRenderGraphTest() {
//
//		ArrayList<GameObject> gameObjects = new ArrayList<>();
//
//		GroupObject rootGameObject = new GroupObject();
//
//		TransformBuilder transformBuilder = new TransformBuilder();
//
//		Transform rootTransform = transformBuilder.build();
//
//		TransformObject rootTransformObject = new TransformObject(rootGameObject, rootTransform);
//
//		Transform textTransform = transformBuilder.build();
//
//		TransformObject textTransformObject = new TransformObject(rootGameObject, textTransform);
//
//		TextItem textItem = (TextItem) new MeshBuilder()
//				.setMeshType(MeshType.TEXT)
//				.build();
//
//		GeometryGameObject textGeometryGameObject = new GeometryGameObject(textTransformObject, textItem);
//
//		MeshObject meshGroupLight = new MeshBuilder()
//				.setMeshType(MeshType.MODEL)
//				.setTransform(transformBuilder.build())
//				.build();
//
//		PointLight pointLight = new PointLight(
//				new Vec3f(0.0f, 1.0f, 0.0f),
//				10f);
//
//		Creation.CreateLight(pointLight, rootTransformObject, new Vec3f(0.0f, 0.0f, -10), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
//
//		Camera camera = new Camera("camera", 1.22173f, 1, 100000);
//		Transform cameraTransform = transformBuilder
//				.setPosition(new Vec3f(-10, 0, 0))
//				.setScale(Vec3f.ONE)
//				.setRotation(cameraRotation)
//				.build();
//		TransformObject cameraTransformGameObject = new TransformObject(rootTransformObject, cameraTransform);
//		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, camera);
//		gameObjects.add(rootGameObject);
//
//		HashMap<String, ArrayList<GameObject>> stringArrayListHashMap = new HashMap<>();
//
//		stringArrayListHashMap.put("MAIN_SCENE", gameObjects);
//
//		HashMap<String, RenderGraph> stringRenderGraphHashMap = new HashMap<>();
//		stringRenderGraphHashMap.put("MAIN_SCENE", new RenderGraph());
//
//		RenderingConversion renderingConversion = new RenderingConversion();
//
//		for (Map.Entry<String, ArrayList<GameObject>> stringArrayListEntry : stringArrayListHashMap.entrySet()) {
//			RenderGraph renderGraph = stringRenderGraphHashMap.get(stringArrayListEntry.getKey());
//
//			for (GameObject gameObject : stringArrayListEntry.getValue()) {
//
//				renderingConversion.createRenderLists(renderGraph, gameObject, Matrix4f.Identity);
//
//			}
//
//			Assertions.assertEquals(1, renderGraph.getCameras().size());
//			Assertions.assertEquals(2, renderGraph.getMeshes().size());
//			renderGraph.getMeshes().forEach((meshObject, instanceObjects) -> {
//				Assertions.assertEquals(1, instanceObjects.size());
//			});
//			Assertions.assertEquals(1, renderGraph.getLights().size());
//			Assertions.assertEquals(0, renderGraph.getWaterMeshes().size());
//			renderGraph.getWaterMeshes().forEach((meshObject, instanceObjects) -> {
//				Assertions.assertEquals(0, instanceObjects.size());
//			});
//			Assertions.assertNull(renderGraph.getSkybox());
//		}
//	}
//
//	@Test
//	void convertToRenderGraphSetInvisibleTest() {
//
//		ArrayList<GameObject> gameObjects = new ArrayList<>();
//
//		GroupObject rootGameObject = new GroupObject();
//
//		TransformBuilder transformBuilder = new TransformBuilder();
//
//		Transform rootTransform = transformBuilder.build();
//
//		TransformObject rootTransformObject = new TransformObject(rootGameObject, rootTransform);
//
//		Transform textTransform = transformBuilder.build();
//
//		TransformObject textTransformObject = new TransformObject(rootGameObject, textTransform);
//
//		TextItem textItem = (TextItem) new MeshBuilder()
//				.setMeshType(MeshType.TEXT)
//				.build();
//
//		GeometryGameObject textGeometryGameObject = new GeometryGameObject(textTransformObject, textItem);
//		textGeometryGameObject.getGameObjectData().setVisible(false);
//
//		MeshObject meshGroupLight = new MeshBuilder()
//				.setMeshType(MeshType.MODEL)
//				.setTransform(transformBuilder.build())
//				.build();
//
//		PointLight pointLight = new PointLight(
//				new Vec3f(0.0f, 1.0f, 0.0f),
//				10f);
//
//		Creation.CreateLight(pointLight, rootTransformObject, new Vec3f(0.0f, 0.0f, -10), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
//
//		Camera camera = new Camera("camera", 1.22173f, 1, 100000);
//		Transform cameraTransform = transformBuilder
//				.setPosition(new Vec3f(-10, 0, 0))
//				.setScale(Vec3f.ONE)
//				.setRotation(cameraRotation)
//				.build();
//		TransformObject cameraTransformGameObject = new TransformObject(rootTransformObject, cameraTransform);
//		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, camera);
//		gameObjects.add(rootGameObject);
//
//		HashMap<String, ArrayList<GameObject>> stringArrayListHashMap = new HashMap<>();
//
//		stringArrayListHashMap.put("MAIN_SCENE", gameObjects);
//
//		HashMap<String, RenderGraph> stringRenderGraphHashMap = new HashMap<>();
//		stringRenderGraphHashMap.put("MAIN_SCENE", new RenderGraph());
//
//		RenderingConversion renderingConversion = new RenderingConversion();
//
//		for (Map.Entry<String, ArrayList<GameObject>> stringArrayListEntry : stringArrayListHashMap.entrySet()) {
//			RenderGraph renderGraph = stringRenderGraphHashMap.get(stringArrayListEntry.getKey());
//
//			for (GameObject gameObject : stringArrayListEntry.getValue()) {
//
//				renderingConversion.createRenderLists(renderGraph, gameObject, Matrix4f.Identity);
//
//			}
//
//			Assertions.assertEquals(1, renderGraph.getCameras().size());
//			Assertions.assertEquals(1, renderGraph.getMeshes().size());
//			renderGraph.getMeshes().forEach((meshObject, instanceObjects) -> {
//				Assertions.assertEquals(1, instanceObjects.size());
//			});
//			Assertions.assertEquals(1, renderGraph.getLights().size());
//			Assertions.assertEquals(0, renderGraph.getWaterMeshes().size());
//			renderGraph.getWaterMeshes().forEach((meshObject, instanceObjects) -> {
//				Assertions.assertEquals(0, instanceObjects.size());
//			});
//			Assertions.assertNull(renderGraph.getSkybox());
//		}
//	}
//
//	@Test
//	void convertToRenderGraphSetChildrenInvisibleTest() {
//
//		ArrayList<GameObject> gameObjects = new ArrayList<>();
//
//		GroupObject rootGameObject = new GroupObject();
//
//		TransformBuilder transformBuilder = new TransformBuilder();
//
//		Transform rootTransform = transformBuilder.build();
//
//		TransformObject rootTransformObject = new TransformObject(rootGameObject, rootTransform);
//		rootTransformObject.getGameObjectData().setRenderChildren(false);
//
//		Transform textTransform = transformBuilder.build();
//
//		TransformObject textTransformObject = new TransformObject(rootGameObject, textTransform);
//
//		TextItem textItem = (TextItem) new MeshBuilder()
//				.setMeshType(MeshType.TEXT)
//				.build();
//
//		GeometryGameObject textGeometryGameObject = new GeometryGameObject(textTransformObject, textItem);
//
//		MeshObject meshGroupLight = new MeshBuilder()
//				.setMeshType(MeshType.MODEL)
//				.setTransform(transformBuilder.build())
//				.build();
//
//		PointLight pointLight = new PointLight(
//				new Vec3f(0.0f, 1.0f, 0.0f),
//				10f);
//
//		Creation.CreateLight(pointLight, rootTransformObject, new Vec3f(0.0f, 0.0f, -10), Vec3f.ONE.scale(0.5f), QuaternionF.Identity, meshGroupLight);
//
//		Camera camera = new Camera("camera", 1.22173f, 1, 100000);
//		Transform cameraTransform = transformBuilder
//				.setPosition(new Vec3f(-10, 0, 0))
//				.setScale(Vec3f.ONE)
//				.setRotation(cameraRotation)
//				.build();
//		TransformObject cameraTransformGameObject = new TransformObject(rootTransformObject, cameraTransform);
//		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, camera);
//		gameObjects.add(rootGameObject);
//
//		HashMap<String, ArrayList<GameObject>> stringArrayListHashMap = new HashMap<>();
//
//		stringArrayListHashMap.put("MAIN_SCENE", gameObjects);
//
//		HashMap<String, RenderGraph> stringRenderGraphHashMap = new HashMap<>();
//		stringRenderGraphHashMap.put("MAIN_SCENE", new RenderGraph());
//
//		RenderingConversion renderingConversion = new RenderingConversion();
//
//		for (Map.Entry<String, ArrayList<GameObject>> stringArrayListEntry : stringArrayListHashMap.entrySet()) {
//			RenderGraph renderGraph = stringRenderGraphHashMap.get(stringArrayListEntry.getKey());
//
//			for (GameObject gameObject : stringArrayListEntry.getValue()) {
//
//				renderingConversion.createRenderLists(renderGraph, gameObject, Matrix4f.Identity);
//
//			}
//
//			Assertions.assertEquals(0, renderGraph.getCameras().size());
//			Assertions.assertEquals(1, renderGraph.getMeshes().size());
//			renderGraph.getMeshes().forEach((meshObject, instanceObjects) -> {
//				Assertions.assertEquals(1, instanceObjects.size());
//			});
//			Assertions.assertEquals(0, renderGraph.getLights().size());
//			Assertions.assertEquals(0, renderGraph.getWaterMeshes().size());
//			renderGraph.getWaterMeshes().forEach((meshObject, instanceObjects) -> {
//				Assertions.assertEquals(0, instanceObjects.size());
//			});
//			Assertions.assertNull(renderGraph.getSkybox());
//		}
//	}
//
//	@Test
//	void convertToRenderGraphInstancingTest() {
//
//		ArrayList<GameObject> gameObjects = new ArrayList<>();
//
//		GroupObject rootGameObject = new GroupObject();
//
//		TransformBuilder transformBuilder = new TransformBuilder();
//
//		Transform rootTransform = transformBuilder.build();
//
//		TransformObject rootTransformObject = new TransformObject(rootGameObject, rootTransform);
//
//		Transform textTransform = transformBuilder.build();
//
//		TransformObject textTransformObject = new TransformObject(rootGameObject, textTransform);
//
//		TextItem textItem = (TextItem) new MeshBuilder()
//				.setMeshType(MeshType.TEXT)
//				.build();
//
//		for (int i = 0; i < 1000; i++) {
//			GeometryGameObject textGeometryGameObject = new GeometryGameObject(textTransformObject, textItem);
//		}
//
//		Camera camera = new Camera("camera", 1.22173f, 1, 100000);
//		Transform cameraTransform = transformBuilder
//				.setPosition(new Vec3f(-10, 0, 0))
//				.setScale(Vec3f.ONE)
//				.setRotation(cameraRotation)
//				.build();
//		TransformObject cameraTransformGameObject = new TransformObject(rootTransformObject, cameraTransform);
//		CameraObject cameraObject = new CameraObject(cameraTransformGameObject, camera);
//		gameObjects.add(rootGameObject);
//
//		HashMap<String, ArrayList<GameObject>> stringArrayListHashMap = new HashMap<>();
//
//		stringArrayListHashMap.put("MAIN_SCENE", gameObjects);
//
//		HashMap<String, RenderGraph> stringRenderGraphHashMap = new HashMap<>();
//		stringRenderGraphHashMap.put("MAIN_SCENE", new RenderGraph());
//
//		RenderingConversion renderingConversion = new RenderingConversion();
//
//		for (Map.Entry<String, ArrayList<GameObject>> stringArrayListEntry : stringArrayListHashMap.entrySet()) {
//			RenderGraph renderGraph = stringRenderGraphHashMap.get(stringArrayListEntry.getKey());
//
//			for (GameObject gameObject : stringArrayListEntry.getValue()) {
//
//				renderingConversion.createRenderLists(renderGraph, gameObject, Matrix4f.Identity);
//
//			}
//
//			Assertions.assertEquals(1, renderGraph.getCameras().size());
//			Assertions.assertEquals(1, renderGraph.getMeshes().size());
//			renderGraph.getMeshes().forEach((meshObject, instanceObjects) -> {
//				Assertions.assertEquals(1000, instanceObjects.size());
//			});
//			Assertions.assertEquals(0, renderGraph.getLights().size());
//			Assertions.assertEquals(0, renderGraph.getWaterMeshes().size());
//			renderGraph.getWaterMeshes().forEach((meshObject, instanceObjects) -> {
//				Assertions.assertEquals(0, instanceObjects.size());
//			});
//			Assertions.assertNull(renderGraph.getSkybox());
//		}
//	}
//}