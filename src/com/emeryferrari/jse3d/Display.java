package com.emeryferrari.jse3d;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
public class Display {
	private DisplayRenderer renderer;
	private Scene scene;
	private JFrame frame;
	private boolean renderPoints;
	private boolean rendering;
	private int pointWidth;
	private int pointHeight;
	private boolean rendererStarted;
	private boolean fpsLimit;
	private boolean fpsLogging;
	private Color lineColor;
	private boolean lineRender;
	private boolean faceRender;
	private int targetFps;
	private long optimalTime;
	private boolean invertColors;
	private Color backgroundColor;
	private Point lastMousePos;	
	private boolean mouseClicked;
	private Point mouseDiff;
	private boolean scrollWheel;
	private int physicsTimestep = 60;
	Point3D camPos;
	private CameraMode mode;
	private ArrayList<ArrayList<Distance>> distance;
	private double camPosX = 0;
	private double camPosY = 0;
	private double camPosZ = 0;
	private ArrayList<ArrayList<Double>> camScale;
	private double scale = 125;
	private double sensitivity = 125;
	private double xTransform = 0;
	private double yTransform = 0;
	private double viewAngleX = 0;
	private double viewAngleY = 0;
	private boolean camPosPrint = false;
	private int fps = 0;
	public Display(Scene scene, String frameTitle) {
		this(scene, frameTitle, true);
	}
	public Display(Scene scene, String frameTitle, boolean frameVisible) {
		this (scene, frameTitle, frameVisible, false);
	}
	public Display(Scene scene, String frameTitle, boolean frameVisible, boolean renderPoints) {
		this(scene, frameTitle, frameVisible, renderPoints, 5, 5);
	}
	public Display(Scene scene, String frameTitle, boolean frameVisible, boolean renderPoints, int frameWidth, int frameHeight) {
		this(scene, frameTitle, frameVisible, renderPoints, 5, 5, frameWidth, frameHeight);
	}
	public Display(Scene scene, String frameTitle, boolean frameVisible, boolean renderPoints, int pointWidth, int pointHeight ,int frameWidth, int frameHeight) {
		this(scene, frameTitle, frameVisible, renderPoints, pointWidth, pointHeight, frameWidth, frameHeight, 60);
	}
	public Display(Scene scene, String frameTitle, boolean frameVisible, boolean renderPoints, int pointWidth, int pointHeight, int frameWidth, int frameHeight, int fps) {
		renderer = new DisplayRenderer();
		this.scene = scene;
		if (frameTitle.equals("")) {
			frame = new JFrame(JSE3DConst.FULL_NAME);
		} else {
			frame = new JFrame(frameTitle + " // " + JSE3DConst.FULL_NAME);
		}
		frame.setSize(frameWidth, frameHeight);
		frame.setVisible(frameVisible);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		rendering = false;
		frame.getContentPane().add(BorderLayout.CENTER, renderer);
		distance = new ArrayList<ArrayList<Distance>>(scene.object.length);
		camScale = new ArrayList<ArrayList<Double>>(scene.object.length);
		for (int x = 0; x < scene.object.length; x++) {
			ArrayList<Distance> distTemp = new ArrayList<Distance>(scene.object[x].points.length);
			ArrayList<Double> camScaleTemp = new ArrayList<Double>(scene.object[x].points.length);
			for (int y = 0; y < scene.object[x].points.length; y++) {
				distTemp.add(new Distance(0, -1));
				camScaleTemp.add(0.0);
			}
			distance.add(distTemp);
			camScale.add(camScaleTemp);
		}
		this.renderPoints = renderPoints;
		this.pointWidth = pointWidth;
		this.pointHeight = pointHeight;
		rendererStarted = false;
		fpsLimit = true;
		fpsLogging = false;
		lineRender = true;
		faceRender = false;
		targetFps = fps;
		optimalTime = 1000000000/targetFps;
		invertColors = false;
		lineColor = Color.BLACK;
		backgroundColor = Color.WHITE;
		renderer.addMouseListener(new ClickListener());
		renderer.addMouseWheelListener(new ScrollListener());
		mouseClicked = false;
		scrollWheel = true;
		mode = CameraMode.DRAG;
		camPos = new Point3D(0, 0, 0);
		mouseDiff = new Point(0, 0);
	}
	public void startRender() {
		if (!rendererStarted) {
			lastMousePos = new Point(MouseInfo.getPointerInfo().getLocation().x-frame.getLocationOnScreen().x, MouseInfo.getPointerInfo().getLocation().y-frame.getLocationOnScreen().y);
			rendering = true;
			Thread renderer = new Thread(new Renderer());
			renderer.start();
		}
	}
	public void pauseRender() {
		rendering = false;
	}
	public void resumeRender() {
		rendering = true;
	}
	public void setVisible(boolean visible) {
		frame.setVisible(visible);
	}
	public JFrame getFrame() {
		return frame;
	}
	private class DisplayRenderer extends JComponent {
		private static final long serialVersionUID = 1L;
		@Override
		public void paintComponent(Graphics graphics) {
			ArrayList<Point[]> pointArrays = new ArrayList<Point[]>();
			if (invertColors) {
				graphics.setColor(Display.invertColor(backgroundColor));
			} else {
				graphics.setColor(backgroundColor);
			}
			graphics.fillRect(0, 0, this.getSize().width, this.getSize().height);
			Point mouse;
			if (mode == CameraMode.DRAG) {
				if (mouseClicked) {
					Point temp = new Point(MouseInfo.getPointerInfo().getLocation().x-frame.getLocationOnScreen().x, MouseInfo.getPointerInfo().getLocation().y-frame.getLocationOnScreen().y);
					mouse = new Point(temp.x-mouseDiff.x, temp.y-mouseDiff.y);
				} else {
					mouse = lastMousePos;
				}
			} else {
				mouse = new Point(MouseInfo.getPointerInfo().getLocation().x-frame.getLocationOnScreen().x, MouseInfo.getPointerInfo().getLocation().y-frame.getLocationOnScreen().y);
			}
			for (int a = 0; a < scene.object.length; a++) {
				Point[] points = new Point[scene.object[a].points.length];
				// WRITTEN BY SAM KRUG START
				for (int i = 0; i < scene.object[a].points.length; i++) {
					double zAngle = Math.atan((scene.object[a].points[i].z)/(scene.object[a].points[i].x));
					if (scene.object[a].points[i].x == 0 && scene.object[a].points[i].z == 0) {
						zAngle = 0;
					}
					double mag = Math.sqrt(Math.pow(scene.object[a].points[i].x, 2) + Math.pow(scene.object[a].points[i].z, 2));
					viewAngleY = -(mouse.y-this.getSize().height/2)/sensitivity;
					if (Math.abs(mouse.y-this.getSize().height/2)>Math.PI/2*sensitivity) {
						if (viewAngleY < 0) {
							viewAngleY = -Math.PI/2*sensitivity;
						} else {
							viewAngleY = Math.PI/2*sensitivity;
						}
					}
					viewAngleX = -(mouse.x-this.getSize().width/2)/sensitivity;
					if (scene.object[a].points[i].x < 0) {
						xTransform = -mag*scale*Math.cos(viewAngleX+zAngle);
						yTransform = -mag*scale*Math.sin(viewAngleX+zAngle)*Math.sin(viewAngleY)+(scene.object[a].points[i].y)*scale*Math.cos(viewAngleY);
					} else {
						xTransform = mag*scale*Math.cos(viewAngleX+zAngle);
						yTransform = mag*scale*Math.sin(viewAngleX+zAngle)*Math.sin(viewAngleY)+(scene.object[a].points[i].y)*scale*Math.cos(viewAngleY);
					}
					camPosX = scene.camDist*Math.sin(viewAngleX)*Math.cos(viewAngleY);
					camPosY = -scene.camDist*Math.sin(viewAngleY);
					camPosZ = scene.camDist*Math.cos(viewAngleX)*Math.cos(viewAngleY);
					if (!(scene.object[a].points[i].z*Math.cos(viewAngleX)*Math.cos(viewAngleY) + scene.object[a].points[i].x*Math.sin(viewAngleX)*Math.cos(viewAngleY) - scene.object[a].points[i].y*Math.sin(viewAngleY) > scene.camDist)) {
						distance.get(a).set(i, new Distance(Math.sqrt(Math.pow(camPosX-(scene.object[a].points[i].x), 2)+Math.pow(camPosY-scene.object[a].points[i].y, 2)+Math.pow(camPosZ-scene.object[a].points[i].z, 2)), i));
						double theta = Math.asin((Math.sqrt(Math.pow(xTransform, 2)+Math.pow(yTransform, 2))/scale)/distance.get(a).get(i).distance);
						camScale.get(a).set(i, distance.get(a).get(i).distance*Math.cos(theta)*Math.sin(scene.viewAngle/2));
						points[i] = new Point((int)(this.getSize().width/2+xTransform/camScale.get(a).get(i)), (int)(this.getSize().height/2-yTransform/camScale.get(a).get(i)));
					}
					// WRITTEN BY SAM KRUG END
					if (renderPoints) {
						if (invertColors) {
							graphics.setColor(Color.WHITE);
						} else {
							graphics.setColor(Color.BLACK);
						}
						graphics.fillOval(points[i].x, points[i].y, pointWidth, pointHeight);
					}
				}
				if (faceRender) {
					double objDist = 0.0;
					for (int x = 0; x < distance.get(a).size(); x++) {
						objDist += distance.get(a).get(x).distance;
					}
					objDist /= (double) distance.get(a).size();
					scene.object[a].camDist = objDist;
					for (int x = 0; x < scene.object[a].faces.length; x++) {
						int[] pointIDs = scene.object[a].faces[x].getPointIDs();
						double[] distances = new double[pointIDs.length];
						for (int y = 0; y < pointIDs.length; y++) {
							for (int z = 0; z < distance.get(a).size(); z++) {
								if (distance.get(a).get(z).pointID == pointIDs[y]) {
									distances[y] = distance.get(a).get(z).distance;
								}
							}
						}
						double average = 0.0;
						for (int i = 0; i < distances.length; i++) {
							average += distances[i];
						}
						average /= (double) distances.length;
						scene.object[a].faces[x].camDist = average;
					}
					for (int x = 0; x < scene.object[a].faces.length; x++) {
						for (int y = x+1; y < scene.object[a].faces.length; y++) {
							if (scene.object[a].faces[x].camDist < scene.object[a].faces[y].camDist) {
								Face temp = scene.object[a].faces[x];
								scene.object[a].faces[x] = scene.object[a].faces[y];
								scene.object[a].faces[y] = temp;
							}
						}
					}
					pointArrays.add(points);
				}
			}
			if (camPosPrint) {
				Point3D cameraPos = getCameraPositionActual();
				graphics.setColor(invertColor(backgroundColor));
				graphics.drawString("x: " + cameraPos.x + " // y: " + cameraPos.y + " // z: " + cameraPos.z, 0, 11);
			}
			if (faceRender) {
				for (int a = 0; a < scene.object.length; a++) {
					for (int x = a+1; x < scene.object.length; x++) {
						if (scene.object[a].camDist < scene.object[x].camDist) {
							Point[] temp = pointArrays.get(a);
							pointArrays.set(a, pointArrays.get(x));
							pointArrays.set(x, temp);
						}
					}
					for (int x = 0; x < scene.object[a].faces.length; x++) {
						for (int y = 0; y < scene.object[a].faces[x].triangles.length; y++) {
							int[] xs = {0, 0, 0};
							int[] ys = {0, 0, 0};
							try {
								int[] xs2 = {pointArrays.get(a)[scene.object[a].faces[x].triangles[y].pointID1].x, pointArrays.get(a)[scene.object[a].faces[x].triangles[y].pointID2].x, pointArrays.get(a)[scene.object[a].faces[x].triangles[y].pointID3].x};
								int[] ys2 = {pointArrays.get(a)[scene.object[a].faces[x].triangles[y].pointID1].y, pointArrays.get(a)[scene.object[a].faces[x].triangles[y].pointID2].y, pointArrays.get(a)[scene.object[a].faces[x].triangles[y].pointID3].y};
								xs = xs2;
								ys = ys2;
							} catch (NullPointerException ex) {}
							if (invertColors) {
								graphics.setColor(Display.invertColor(scene.object[a].faces[x].triangles[y].color));
							} else {
								graphics.setColor(scene.object[a].faces[x].triangles[y].color);
							}
							graphics.fillPolygon(xs, ys, 3);
						}
					}
				}
			}
			if (lineRender) {
				for (int a = 0; a < scene.object.length; a++) {
					if (lineRender) {
						if (invertColors) {
							graphics.setColor(Display.invertColor(lineColor));
						} else {
							graphics.setColor(lineColor);
						}
						for (int i = 0; i < scene.object[a].edges.length; i++) {
							int point1 = scene.object[a].edges[i].pointID1;
							int point2 = scene.object[a].edges[i].pointID2;
							try {graphics.drawLine(pointArrays.get(a)[point1].x, pointArrays.get(a)[point1].y, pointArrays.get(a)[point2].x, pointArrays.get(a)[point2].y);} catch (NullPointerException ex) {}
						}
					}
				}
			}
			fps++;
			this.revalidate();
		}
	}
	private class Renderer implements Runnable {
		public void run() {
			while (true) {
				long lastFpsTime = 0L;
				long lastLoopTime = System.nanoTime();
				while (rendering) {
				    long now = System.nanoTime();
				    long updateLength = now - lastLoopTime;
				    lastLoopTime = now;
				    lastFpsTime += updateLength;
				    if (lastFpsTime >= 1000000000) {
				    	if (fpsLogging) {
				    		System.out.println("FPS: " + fps);
				    	}
				        lastFpsTime = 0;
				        fps = 0;
				    }
				    renderFrame();
				    if (fpsLimit) {
				    	long tmp = (lastLoopTime-System.nanoTime()+optimalTime)/1000000;
				    	if (tmp > 0) {
				    		try {Thread.sleep(tmp);} catch (InterruptedException ex) {ex.printStackTrace();}
				    	}
				    }
				}
			}
		}
		private void renderFrame() {
			getFrame().repaint();
		}
	}
	private class ClickListener implements MouseListener {
		public void mouseEntered(MouseEvent ev) {}
		public void mousePressed(MouseEvent ev) {
			mouseClicked = true;
			Point temp = new Point(MouseInfo.getPointerInfo().getLocation().x-frame.getLocationOnScreen().x, MouseInfo.getPointerInfo().getLocation().y-frame.getLocationOnScreen().y);
			mouseDiff = new Point(temp.x-lastMousePos.x, temp.y-lastMousePos.y);
		}
		public void mouseClicked(MouseEvent ev) {}
		public void mouseReleased(MouseEvent ev) {
			mouseClicked = false;
			Point temp = new Point(MouseInfo.getPointerInfo().getLocation().x-frame.getLocationOnScreen().x, MouseInfo.getPointerInfo().getLocation().y-frame.getLocationOnScreen().y);
			lastMousePos = new Point(temp.x-mouseDiff.x, temp.y-mouseDiff.y);
		}
		public void mouseExited(MouseEvent ev) {}
	}
	private class ScrollListener implements MouseWheelListener {
		public void mouseWheelMoved(MouseWheelEvent ev) {
			if (scrollWheel) {
				if (ev.getPreciseWheelRotation() > 0) {
					scene.camDist *= 1.2;
				} else {
					scene.camDist /= 1.2;
				}
			}
		}
	}
	public void setTargetFPS(int fps) {
		targetFps = fps;
		optimalTime = 1000000000 / targetFps;
	}
	public void enableFPSLimit() {
		fpsLimit = true;
	}
	public void disableFPSLimit() {
		fpsLimit = false;
	}
	public void enableFPSLogging() {
		fpsLogging = true;
	}
	public void disableFPSLogging() {
		fpsLogging = false;
	}
	public void enableLineRendering() {
		lineRender = true;
	}
	public void disableLineRendering() {
		lineRender = false;
	}
	public void enableFaceRendering() {
		faceRender = true;
	}
	public void disableFaceRendering() {
		faceRender = false;
	}
	public void setLineColor(Color color) {
		lineColor = color;
	}
	public void enableInvertColors() {
		invertColors = true;
	}
	public void disableInvertColors() {
		invertColors = false;
	}
	public void setBackgroundColor(Color color) {
		backgroundColor = color;
	}
	public Scene getScene() {
		return scene;
	}
	public void setScene(Scene scene) {
		this.scene = scene;
	}
	public void setPhysicsTimestep(int timestep) { // not an actual timestep, the precision of physics movements is proportional to the timestep
		physicsTimestep = timestep;
	}
	public int getPhysicsTimestep() {
		return physicsTimestep;
	}
	public void setCameraPositionRel(Point3D point) {
		Thread cameraPos = new CameraPos(point, this);
		cameraPos.start();
	}
	public void transitionCameraPositionRel(Point3D point, int millis) {
		Thread transition = new Transition(point, millis, this);
		transition.start();
	}
	public Point3D getCameraPosition() {
		return camPos;
	}
	private class CameraPos extends Thread {
		private double xt;
		private double yt;
		private double zt;
		private Display display;
		private CameraPos(Point3D point, Display display) {
			this.xt = point.x;
			this.yt = point.y;
			this.zt = point.z;
			this.display = display;
		}
		@Override
		public void run() {
			for (int i = 0; i < scene.object.length; i++) {
				scene.object[i].movePosRel(xt, yt, zt, display);
			}
			camPos.x += xt;
			camPos.y += yt;
			camPos.z += zt;
		}
	}
	private class Transition extends Thread {
		private double xt;
		private double yt;
		private double zt;
		private int millis;
		private Display display;
		private Transition(Point3D point, int millis, Display display) {
			this.xt = point.x;
			this.yt = point.y;
			this.zt = point.z;
			this.millis = millis;
			this.display = display;
		}
		@Override
		public void run() {
			double xIteration = xt/(double)(physicsTimestep*((double)millis/1000.0));
			double yIteration = yt/(double)(physicsTimestep*((double)millis/1000.0));
			double zIteration = zt/(double)(physicsTimestep*((double)millis/1000.0));
			long lastFpsTime = 0L;
			long lastLoopTime = System.nanoTime();
			final long OPTIMAL_TIME = 1000000000 / physicsTimestep;
			for (int x = 0; x < (int)(physicsTimestep*((double)millis/1000.0)); x++) {
				long now = System.nanoTime();
			    long updateLength = now - lastLoopTime;
			    lastLoopTime = now;
			    lastFpsTime += updateLength;
			    if (lastFpsTime >= 1000000000) {
			        lastFpsTime = 0;
			    }
			    for (int y = 0; y < scene.object.length; y++) {
			    	scene.object[y].movePosRel(xIteration, yIteration, zIteration, display);
			    }
			    try {Thread.sleep((lastLoopTime-System.nanoTime()+OPTIMAL_TIME)/1000000);} catch (InterruptedException ex) {ex.printStackTrace();}
			}
			camPos.x += xt;
			camPos.y += yt;
			camPos.z += zt;
		}
	}
	public void enableScrollWheel() {
		scrollWheel = true;
	}
	public void disableScrollWheel() {
		scrollWheel = false;
	}
	public void setCameraDistance(double distance) {
		scene.camDist = distance;
	}
	public void setCameraMode(CameraMode mode) {
		this.mode = mode;
	}
	public CameraMode getCameraMode() {
		return mode;
	}
	private static Color invertColor(Color color) {
		return new Color(255-color.getRed(), 255-color.getGreen(), 255-color.getBlue(), color.getAlpha());
	}
	public Point3D getCameraPositionActual() {
		double x = (Math.sin(viewAngleX)*Math.cos(viewAngleY)*scene.camDist) + camPos.x;
		double y = -((Math.sin(viewAngleY)*scene.camDist) + camPos.y);
		double z = (Math.cos(viewAngleX)*Math.cos(viewAngleY)*scene.camDist) + camPos.z;
		return new Point3D(x, y, z);
	}
	public void enableCameraPositionPrinting() {
		camPosPrint = true;
	}
	public void disableCameraPositionPrinting() {
		camPosPrint = false;
	}
}