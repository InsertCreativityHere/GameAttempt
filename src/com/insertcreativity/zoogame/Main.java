//should game pause when dragged and such?
package com.insertcreativity.zoogame;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public class Main implements Runnable
{
	/**The window that the game is running in.*/
	private Window window;
	/**Object for rendering the game and it's world to the window.*/
	private Renderer renderer;
	/**The preferred FPS to run the game at.*/
	private int FPS;
	
	/**Creates a new instance of the game.
	 * @param windowWidth The initial width of the game window.
	 * @param windowHeight The initial height of the game window.
	 * @param preferredFPS The preferred FPS to run the game at, note that this is only a hint to the display which is free to ignore this.
	 * @throws IllegalStateException If the game fails to initialize properly.*/
	public Main(int windowWidth, int windowHeight, int preferredFPS)
	{
		Window.initialize();//initialize the window system
		window = new Window(windowWidth, windowHeight, "Zoo Game!", GLFW.glfwGetPrimaryMonitor(), false);//create the game's window
		
		FPS = preferredFPS;//set the FPS that the game should run at
		
		renderer = new Renderer(windowWidth, windowHeight, 0, 0, 0, 64f);//create a new renderer for the game
	}
	
	/**Runs the game's logic and main loop.*/
	public void run()
	{
		System.gc();//run the garbage collector to cleanup leftover resources from initialization
		
		long tickLength = 1000 / FPS;//set the game to tick at 60 FPS
		long startTime;//variable for storing the start time of each loop iteration
		long sleepTime;//variable for storing how long each loop iteration should sleep for
		
		while(window.update()){//run the game loop so long as the window is open
			startTime = System.nanoTime();//store the time that the loop started at
			
			window.render();//render the game in it's window
			
			sleepTime = tickLength - ((System.nanoTime() - startTime) / 1000000);//calculate the amount of time the game loop should sleep for
			if(sleepTime > 0){//if the game loop should sleep this cycle
				try{
					Thread.sleep(sleepTime);//sleep for the calculated amount of time
				} catch(InterruptedException interruptedException){}
			}
		}
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
