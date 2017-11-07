//should game pause when dragged and such?
//add delete and clear methods for renderer/shader/model?
package com.insertcreativity.zoogame;

import org.lwjgl.glfw.GLFW;
import com.insertcreativity.zoogame.menu.MainMenu;

public class Main implements Runnable
{
	/**The window that the game is running in.*/
	private Window window;
	/**Object responsible for rendering the game.*/
	private Renderer renderer;
	/**The current screen that the game is on.*/
	private Screen screen;
	/**The preferred FPS to run the game at.*/
	private int FPS;
	
	/**Creates a new instance of the game.
	 * @param windowWidth The initial width of the game window.
	 * @param windowHeight The initial height of the game window.
	 * @param preferredFPS The preferred FPS to run the game at, note that this is only a hint to the display which is free to ignore this.
	 * @throws IllegalStateException If the game fails to initialize properly.*/
	public Main(int windowWidth, int windowHeight, int preferredFPS) throws IllegalStateException
	{
		Window.initialize();//initialize the window system
		window = new Window(this, windowWidth, windowHeight, "Zoo Game!", GLFW.glfwGetPrimaryMonitor(), false);//create the game's window
		renderer = new Renderer(windowWidth, windowHeight, 0, 0, 0, 64f);//create a new renderer for the game
		FPS = preferredFPS;//set the FPS that the game should run at
		
		screen = new MainMenu();//create the main menu and set it as the game's current screen
		
		System.gc();//run the garbage collector to cleanup leftover resources from initialization
	}
	
	/**Runs the game's logic and main loop.*/
	public void run()
	{
		long tickLength = 1000 / FPS;//set the proper length each tick should be
		long startTime;//variable for storing the start time of each loop iteration
		long sleepTime;//variable for storing how long each loop iteration should sleep for
		
		while(window.update()){//run the game loop so long as the window is open
			startTime = System.nanoTime();//store the time that the loop started at
			
			screen.update(window);//update the screen
			screen.render(renderer);//render the screen
			window.render();//update the window to display the game's current screen
			
			sleepTime = tickLength - ((System.nanoTime() - startTime) / 1000000);//calculate the amount of time the game loop should sleep for
			if(sleepTime > 0){//if the game loop should sleep this cycle
				try{
					Thread.sleep(sleepTime);//sleep for the calculated amount of time
				} catch(InterruptedException interruptedException){}
			}
		}
	}
	
	/**Called whenever the game's window is resized.
	 * @param width The new width of the window.
	 * @param height The new height of the window.*/
	public void onWindowResize(int width, int height)
	{
		renderer.resizeViewport(width, height);//update the renderer's viewport
	}
	
	/**Called whenever a key is pressed inside the game's window.
	 * @param key The GLFW key-code for the key that was pressed.
	 * @param scancode The system's key-code for the key that was pressed.
	 * @param modifiers Bit flags indicating which modifier keys were also being pressed.*/
	protected void onKeyPress(int key, int scancode, int modifiers)
	{
		screen.keyPresssed(key, scancode, modifiers);//notify the current screen that a key was pressed
	}
	
	/**Called whenever a key is released inside the game's window.
	 * @param key The GLFW key-code for the key that was released.
	 * @param scancode The system's key-code for the key that was released.
	 * @param modifiers Bit flags indicating which modifier keys were also being pressed.*/
	protected void onKeyRelease(int key, int scancode, int modifiers)
	{
		screen.keyReleased(key, scancode, modifiers);//notify the current screen that a key was released
	}
	
	public static void main(String[] args)
	{
		Runtime.getRuntime().addShutdownHook(new Thread("Game Shutdown Hook"){//add a shutdown hook to cleanup
			public void run()//ensure the cleanup code is run on shutdown
			{
				GLFW.glfwTerminate();//terminate GLFW
			}
		});
		Thread.currentThread().setName("Main Game Thread");//set the name of the thread the game will run in
		(new Main(800, 600, 60)).run();//create a new main game instance and run it
	}
}
