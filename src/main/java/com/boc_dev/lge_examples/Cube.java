package com.boc_dev.lge_examples;

import com.boc_dev.maths.objects.vector.Vec3f;

public class Cube {
	private final int cubeIndex;
	private final Vec3f vertices[];

	public Cube(int cubeIndex, Vec3f[] vertices) {
		this.cubeIndex = cubeIndex;
		this.vertices = vertices;
	}

	public int getCubeIndex() {
		return cubeIndex;
	}

	public Vec3f[] getVertices() {
		return vertices;
	}
}
