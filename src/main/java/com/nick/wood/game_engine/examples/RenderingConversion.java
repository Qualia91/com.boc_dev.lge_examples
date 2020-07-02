package com.nick.wood.game_engine.examples;

import com.nick.wood.game_engine.model.game_objects.*;
import com.nick.wood.graphics_library.objects.mesh_objects.MeshObject;
import com.nick.wood.graphics_library.objects.render_scene.InstanceObject;
import com.nick.wood.graphics_library.objects.render_scene.RenderGraph;
import com.nick.wood.maths.objects.matrix.Matrix4f;

import java.util.*;

public class RenderingConversion {

	public void createRenderLists(RenderGraph renderGraph, GameObject gameObject, Matrix4f transformationSoFar) {

		Iterator<GameObject> iterator = gameObject.getGameObjectData().getChildren().iterator();

		while (iterator.hasNext()) {

			GameObject child = iterator.next();

			if (child.getGameObjectData().isRenderChildren()) {

				switch (child.getGameObjectData().getType()) {

					case TRANSFORM:
						if (child.getGameObjectData().isDelete()) {
							child.getGameObjectData().delete();
							iterator.remove();
						} else {
							TransformObject transformGameObject = (TransformObject) child;
							createRenderLists(renderGraph, transformGameObject, transformGameObject.getTransformForRender().multiply(transformationSoFar));
						}
						break;
					case LIGHT:
						if (child.getGameObjectData().isDelete()) {
							renderGraph.removeLight(child.getGameObjectData().getUuid());
							child.getGameObjectData().delete();
							iterator.remove();
						} else {
							LightObject lightObject = (LightObject) child;
							if (child.getGameObjectData().isVisible()) {
								if (renderGraph.getLights().containsKey(lightObject.getLight())) {
									renderGraph.getLights().get(lightObject.getLight()).setTransformation(transformationSoFar);
								} else {
									InstanceObject lightInstance = new InstanceObject(child.getGameObjectData().getUuid(), transformationSoFar);
									renderGraph.getLights().put(lightObject.getLight(), lightInstance);
								}
							}
							createRenderLists(renderGraph, lightObject, transformationSoFar);
						}
						break;
					case MESH:
						if (child.getGameObjectData().isVisible()) {
							if (child.getGameObjectData().isDelete()) {
								renderGraph.removeMesh(child.getGameObjectData().getUuid());
								child.getGameObjectData().delete();
								iterator.remove();
							} else {
								MeshGameObject meshGameObject = (MeshGameObject) child;

								if (child.getGameObjectData().isVisible()) {
									boolean found = false;
									for (Map.Entry<MeshObject, ArrayList<InstanceObject>> meshObjectArrayListEntry : renderGraph.getMeshes().entrySet()) {
										if (meshObjectArrayListEntry.getKey().getStringToCompare().equals(meshGameObject.getMeshObject().getStringToCompare())) {
											InstanceObject meshInstance = new InstanceObject(child.getGameObjectData().getUuid(), transformationSoFar);
											meshObjectArrayListEntry.getValue().add(meshInstance);
											found = true;
											break;
										}
									}
									if (!found) {
										ArrayList<InstanceObject> meshObjects = new ArrayList<>();
										InstanceObject meshInstance = new InstanceObject(child.getGameObjectData().getUuid(), transformationSoFar);
										meshObjects.add(meshInstance);
										renderGraph.getMeshes().put(meshGameObject.getMeshObject(), meshObjects);

										// also tell the render graph that a new mesh needs to be created when it gets to the renderer
										renderGraph.getMeshesToBuild().add(meshGameObject.getMeshObject().getMesh());
									}
								}
								createRenderLists(renderGraph, meshGameObject, transformationSoFar);
							}
						}
						break;
					case TERRAIN:
						if (child.getGameObjectData().isDelete()) {
							renderGraph.removeTerrain(child.getGameObjectData().getUuid());
							child.getGameObjectData().delete();
							iterator.remove();
						} else {
							TerrainObject meshGameObject = (TerrainObject) child;

							if (child.getGameObjectData().isVisible()) {
								if (renderGraph.getTerrainMeshes().containsKey(meshGameObject.getTerrain())) {
									InstanceObject meshInstance = new InstanceObject(child.getGameObjectData().getUuid(), transformationSoFar);
									renderGraph.getTerrainMeshes().get(meshGameObject.getTerrain()).add(meshInstance);
								} else {
									ArrayList<InstanceObject> meshObjects = new ArrayList<>();
									InstanceObject meshInstance = new InstanceObject(child.getGameObjectData().getUuid(), transformationSoFar);
									meshObjects.add(meshInstance);
									renderGraph.getTerrainMeshes().put(meshGameObject.getTerrain(), meshObjects);

									// also tell the render graph that a new mesh needs to be created when it gets to the renderer
									renderGraph.getMeshesToBuild().add(meshGameObject.getTerrain().getMesh());
								}
							}
							createRenderLists(renderGraph, meshGameObject, transformationSoFar);
						}
						break;
					case WATER:
						if (child.getGameObjectData().isDelete()) {
							renderGraph.removeWater(child.getGameObjectData().getUuid());
							child.getGameObjectData().delete();
							iterator.remove();
						} else {
							WaterObject meshGameObject = (WaterObject) child;

							if (child.getGameObjectData().isVisible()) {
								if (renderGraph.getWaterMeshes().containsKey(meshGameObject.getWater())) {
									InstanceObject meshInstance = new InstanceObject(child.getGameObjectData().getUuid(), transformationSoFar);
									renderGraph.getWaterMeshes().get(meshGameObject.getWater()).add(meshInstance);
								} else {
									ArrayList<InstanceObject> meshObjects = new ArrayList<>();
									InstanceObject meshInstance = new InstanceObject(child.getGameObjectData().getUuid(), transformationSoFar);
									meshObjects.add(meshInstance);
									renderGraph.getWaterMeshes().put(meshGameObject.getWater(), meshObjects);

									// also tell the render graph that a new mesh needs to be created when it gets to the renderer
									renderGraph.getMeshesToBuild().add(meshGameObject.getWater().getMesh());
								}
							}
							createRenderLists(renderGraph, meshGameObject, transformationSoFar);
						}
						break;
					case SKYBOX:
						if (child.getGameObjectData().isDelete()) {
							renderGraph.removeSkybox();
							child.getGameObjectData().delete();
							iterator.remove();
						} else {
							SkyBoxObject skyBoxObject = (SkyBoxObject) child;
							if (child.getGameObjectData().isVisible()) {
								renderGraph.setSkybox(skyBoxObject.getSkybox());
							}
							createRenderLists(renderGraph, skyBoxObject, transformationSoFar);
						}
						break;
					case CAMERA:
						if (child.getGameObjectData().isDelete()) {
							renderGraph.removeCamera(child.getGameObjectData().getUuid());
							child.getGameObjectData().delete();
							iterator.remove();
						} else {
							CameraObject cameraObject = (CameraObject) child;
							if (child.getGameObjectData().isVisible()) {
								if (renderGraph.getCameras().containsKey(cameraObject.getCamera())) {
									renderGraph.getCameras().get(cameraObject.getCamera()).setTransformation(transformationSoFar);
								} else {
									InstanceObject cameraInstance = new InstanceObject(child.getGameObjectData().getUuid(), transformationSoFar);
									renderGraph.getCameras().put(cameraObject.getCamera(), cameraInstance);
								}
							}
							createRenderLists(renderGraph, cameraObject, transformationSoFar);
						}
						break;
					default:
						if (child.getGameObjectData().isDelete()) {
							child.getGameObjectData().delete();
							iterator.remove();
						} else {
							createRenderLists(renderGraph, child, transformationSoFar);
						}
						break;

				}

			}
		}

	}

}
