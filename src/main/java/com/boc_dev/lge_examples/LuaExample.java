package com.boc_dev.lge_examples;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;

public class LuaExample {

	public static void main(String[] args) {
		if (args.length > 0 && args[0] != null) {
			new LuaExample().runScenario(args[0]);
		} else {
			System.out.println("No lua front end file found, stopping");
		}
	}

	public void runScenario(String example) {
		File file = new File(example);
		if (file.exists() && file.isFile()) {
			Globals globals = JsePlatform.standardGlobals();
			globals.load("package.path = '" + file.getParentFile().getPath().replace("\\", "/") + "/?.lua;'").call();
			LuaValue chunk = globals.loadfile(file.getAbsolutePath());
			chunk.call();
		} else {
			System.out.println(example + " not found");
		}
	}

}
