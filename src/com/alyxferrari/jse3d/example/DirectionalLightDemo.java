package com.alyxferrari.jse3d.example;
import com.alyxferrari.jse3d.obj.*;
import com.alyxferrari.jse3d.enums.*;
import com.alyxferrari.jse3d.gfx.*;
import java.awt.*;
public class DirectionalLightDemo {
	public static final String OBJ_PATH = "/Users/alyxferrari/Downloads/cat.obj"; // change this if you want to use this example
	public static void main(String[] args) throws Exception {
		Object3D[] objects = {Object3D.createFromObj(OBJ_PATH, new Color(255, 10, 10, 255))};
		Scene scene = new Scene(objects, 550);
		scene.addDirectionalLight(new DirectionalLight(new Vector3(0, 0, -1), 0.6f));
		scene.addDirectionalLight(new DirectionalLight(new Vector3(0, 0, 1), 0.2f));
		Display display = new Display(scene, "jse3d obj model demo", Math.toRadians(60), objects[0].points.length, objects[0].points.length, 1);
		display.setPointSize(new Dimension(200, 200));
		display.disableFPSLimit();
		display.enableFaceRendering();
		display.enableCameraPositionPrinting();
		display.disableLineRendering();
		display.setScrollWheelMultiplier(1.2);
		display.enableFPSLogging();
		display.setRenderTarget(RenderTarget.CPU_SINGLETHREADED);
		display.setRenderQuality(RenderMode.PERFORMANCE);
		display.startRender();
	}
}