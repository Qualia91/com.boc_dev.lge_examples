package com.boc_dev.lge_examples;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;

public class LuaExample {

	public LuaExample() {
	}

	public static void main(String[] args) {

		LuaExample luaExample = new LuaExample();
		luaExample.runScenario("boid_example");
	}

	public void runScenario(String exampleName) {
		File file = new File("../com.boc_dev.lge_examples/src/main/java/com/boc_dev/lge_examples/" + exampleName + ".lua");
		if (file.exists() && file.isFile()) {
			Globals globals = JsePlatform.standardGlobals();
			LuaValue chunk = globals.loadfile(file.getAbsolutePath());
			chunk.call();
		} else {
			System.out.println("Scenario not found");
		}
	}

}
