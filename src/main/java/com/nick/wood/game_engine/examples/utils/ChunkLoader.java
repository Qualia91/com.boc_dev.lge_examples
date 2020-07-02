package com.nick.wood.game_engine.examples.utils;

import com.nick.wood.game_engine.model.game_objects.GameObject;
import com.nick.wood.game_engine.model.game_objects.GroupObject;
import com.nick.wood.game_engine.model.game_objects.TerrainObject;
import com.nick.wood.graphics_library.objects.mesh_objects.*;
import com.nick.wood.maths.noise.Perlin2Df;
import com.nick.wood.maths.objects.srt.Transform;
import com.nick.wood.maths.objects.srt.TransformBuilder;
import com.nick.wood.maths.objects.vector.Vec3f;
import com.nick.wood.game_engine.model.game_objects.TransformObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkLoader {

	private final GroupObject groupObject;

	private final int chunkSize = 50;
	private final int segmentSize = 100;
	private final ArrayList<ChunkIndex> loadedChunkIndices = new ArrayList<>();
	private final ConcurrentHashMap<ChunkIndex, GameObject> chunkIndexSceneGraphHashMap = new ConcurrentHashMap<>();
	private final Perlin2Df[] perlin2Ds;
	private final int cellSpace = 1000;

	private final int loadingClippingDistance = 10;
	private final int loadingClippingDistance2 = 300;
	private final int visualClippingDistance2 = 200;
	private final ArrayList<TerrainTextureObject> terrainTextureObjects;

	public ChunkLoader(ArrayList<GameObject> gameObjects, int octaves, int lacunarity) {
		perlin2Ds = new Perlin2Df[octaves];
		for (int i = 0; i < octaves; i++) {
			double frequency = Math.pow(lacunarity, i);
			int currentSegmentSize = (int) (segmentSize / frequency);
			perlin2Ds[i] = new Perlin2Df(10000, currentSegmentSize);
		}

		this.groupObject = new GroupObject();
		gameObjects.add(groupObject);


		this.terrainTextureObjects = new ArrayList<>();

		terrainTextureObjects.add(new TerrainTextureObject(
				0,
				500,
				"/textures/sand.jpg",
				"/normalMaps/sandNormalMap.jpg"
		));

		terrainTextureObjects.add(new TerrainTextureObject(
				500,
				2500,
				"/textures/terrain2.jpg",
				"/normalMaps/grassNormal.jpg"
		));

		terrainTextureObjects.add(new TerrainTextureObject(
				7000,
				1000,
				"/textures/snow.jpg",
				"/normalMaps/large.jpg"
		));
	}

	public void loadChunk(Vec3f currentPlayerPosition) {

		// get the index of the player position
		int xIndex = (int) (currentPlayerPosition.getX() / (double) (chunkSize * cellSpace));
		int yIndex = (int) (currentPlayerPosition.getY() / (double) (chunkSize * cellSpace));

		ChunkIndex playerChunk = new ChunkIndex(xIndex, yIndex);

		// use this position to create the tiles all around the player
		// load all 16 chunks around it
		for (int x = xIndex - loadingClippingDistance; x <= xIndex + loadingClippingDistance; x++) {
			for (int y = yIndex - loadingClippingDistance; y <= yIndex + loadingClippingDistance; y++) {

				ChunkIndex chunkIndex = new ChunkIndex(x, y);

				// see if the chunk hasn't already been loaded
				if (!loadedChunkIndices.contains(chunkIndex)) {
					// add chunk to new list
					// and load it
					GameObject gameObject = createChunk(chunkIndex);
					chunkIndexSceneGraphHashMap.put(chunkIndex, gameObject);
					loadedChunkIndices.add(chunkIndex);

				}
			}
		}

		// see if the chunk hasn't already been loaded
		Iterator<ChunkIndex> iterator = loadedChunkIndices.iterator();
		while (iterator.hasNext()) {
			ChunkIndex next = iterator.next();
			int dist = next.distance2AwayFrom(playerChunk);
			// if chunk is within visual range, set render to true
			if (dist < visualClippingDistance2) {
				chunkIndexSceneGraphHashMap.get(next).getGameObjectData().setRenderChildren(true);
			}
			else if (dist < loadingClippingDistance2) {
				chunkIndexSceneGraphHashMap.get(next).getGameObjectData().setRenderChildren(false);
			}
			else {
				destroyChunk(next);
				iterator.remove();
			}
		}


	}

	private void destroyChunk(ChunkIndex chunkIndex) {
		chunkIndexSceneGraphHashMap.get(chunkIndex).getGameObjectData().markForDeletion();
		chunkIndexSceneGraphHashMap.remove(chunkIndex);
	}

	private GameObject createChunk(ChunkIndex chunkIndex) {

		ProceduralGeneration proceduralGeneration = new ProceduralGeneration();
		float[][] grid = proceduralGeneration.generateHeightMapChunk(
				chunkSize + 1,
				0.7,
				chunkIndex.getX() * chunkSize,
				chunkIndex.getY() * chunkSize,
				perlin2Ds,
				30,
				amp -> amp * amp * amp
		);


		Transform transform = new TransformBuilder()
				.setPosition(new Vec3f(chunkIndex.getX() * chunkSize * cellSpace, chunkIndex.getY() * chunkSize * cellSpace, 0)).build();

		TransformObject transformObject = new TransformObject(groupObject, transform);
		transformObject.getGameObjectData().setRenderChildren(false);


		TerrainObject terrainObject = new TerrainObject(
				transformObject,
				grid,
				terrainTextureObjects,
				cellSpace
		);

		return transformObject;
	}

}
