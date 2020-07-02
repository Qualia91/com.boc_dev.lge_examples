package com.nick.wood.game_engine.examples;

import com.nick.wood.event_bus.busses.GameBus;
import com.nick.wood.event_bus.interfaces.Bus;
import com.nick.wood.game_engine.model.GameObjectManager;
import com.nick.wood.game_engine.model.game_objects.GameObject;
import com.nick.wood.game_engine.model.input.ControllerState;
import com.nick.wood.game_engine.model.input.DirectTransformController;
import com.nick.wood.game_engine.model.input.GameManagementInputController;
import com.nick.wood.graphics_library.Picking;
import com.nick.wood.graphics_library.Window;
import com.nick.wood.graphics_library.WindowInitialisationParameters;
import com.nick.wood.graphics_library.objects.render_scene.RenderGraph;
import com.nick.wood.graphics_library.objects.render_scene.Scene;
import com.nick.wood.maths.objects.matrix.Matrix4f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameLoop {

	private final WindowInitialisationParameters wip;
	private final DirectTransformController directTransformController;
	private final HashMap<String, ArrayList<GameObject>> layeredGameObjectsMap;
	private final HashMap<String, RenderGraph> renderGraphLayerMap;
	private final GameObjectManager gameObjectManager;
	private final GameBus gameBus;
	private final ControllerState controllerState;
	private final ExecutorService executorService;
	private final GameManagementInputController gameManagementInputController;
	private final Window window;

	public GameLoop(ArrayList<Scene> sceneLayers,
	                WindowInitialisationParameters wip,
	                DirectTransformController directTransformController,
	                HashMap<String, ArrayList<GameObject>> layeredGameObjectsMap) {

		this.wip = wip;
		this.layeredGameObjectsMap = layeredGameObjectsMap;
		this.gameBus = new GameBus();

		this.executorService = Executors.newFixedThreadPool(4);

		this.directTransformController = directTransformController;

		this.renderGraphLayerMap = new HashMap<>();
		for (String layerName : layeredGameObjectsMap.keySet()) {
			renderGraphLayerMap.put(layerName, new RenderGraph());
		}

		this.gameObjectManager = new GameObjectManager();

		this.controllerState = new ControllerState();

		this.gameManagementInputController = new GameManagementInputController(gameBus);

		this.executorService.submit(controllerState);

		this.gameBus.register(controllerState);

		this.window = new Window(sceneLayers, gameBus);
		this.gameBus.register(window);

		for (Scene sceneLayer : sceneLayers) {
			if (sceneLayer.getPickingShader() != null) {
				this.gameBus.register(new Picking(gameBus, sceneLayer, renderGraphLayerMap.get(sceneLayer.getName())));
			}
		}

	}

	public void run(Runnable ... runnables) throws IOException {

		window.init(wip);

		long oldTime = System.currentTimeMillis();

		while (!window.shouldClose()) {

			directTransformController.updateUserInput(controllerState);
			gameManagementInputController.updateUserInput(controllerState);

			for (Runnable runnable : runnables) {
				runnable.run();
			}

			for (Map.Entry<String, ArrayList<GameObject>> stringArrayListEntry : layeredGameObjectsMap.entrySet()) {

				RenderGraph renderGraph = renderGraphLayerMap.get(stringArrayListEntry.getKey());

				renderGraph.empty();

				for (GameObject gameObject : stringArrayListEntry.getValue()) {
					gameObjectManager.createRenderLists(renderGraph,
							gameObject,
							Matrix4f.Identity);
				}
			}

			window.loop(renderGraphLayerMap);

			long currentTime = System.currentTimeMillis();

			long frameRate = currentTime - oldTime;

			window.setTitle("Diff Time: " + frameRate);

			oldTime = currentTime;

		}

		window.close();

		executorService.shutdown();
	}

	public Bus getGameBus() {
		return gameBus;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}
}
