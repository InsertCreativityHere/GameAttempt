//any problems with assuming all 'invoke' calls are meant for just this window?
//should do any cleanup after destroying window?
package com.insertcreativity.zoogame;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.glfw.GLFWVidMode;

public class Window implements GLFWWindowSizeCallbackI
{
	/**The handle ID for the window.*/
	private final long handle;
	/**The width of the window.*/
	private int windowWidth;
	/**The height of the window.*/
	private int windowHeight;
	/**Flag for whether the window is fullscreen.*/
	private boolean isFullscreen;
	/**Flag for whether Vsync is enabled.*/
	private boolean isVsyncEnabled;
	/**Object that handles input for the window.*/
	public final InputManager inputManager;
	/**Object for rendering things to the window.*/
	public Renderer renderer;
	
	/**Creates a new window with the specified parameters.
	 * @param width The width to make the screen. (in pixels)
	 * @param height The height to make the screen. (in pixels)
	 * @param title The title to display for the window.
	 * @param monitor Handle ID for the monitor to display the window on.
	 * @param fullscreen Flag for whether the window should be fullscreen.
	 * @throws IllegalStateException If the window couldn't be created successfully.*/
	public Window(int width, int height, String title, long monitor, boolean fullscreen) throws IllegalStateException
	{
		GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);//load the monitor's video mode and properties
		
		handle = GLFW.glfwCreateWindow(width, height, title, (fullscreen? monitor : 0), 0);//create the window
		
		if(handle == 0){//check to make sure the window was successfully created
			throw new IllegalStateException("Failed to create window.");
		}
		
		windowWidth = width;//store the window's width
		windowHeight = height;//store the window's height
		isFullscreen = fullscreen;//store whether the window is in fullscreen mode
		GLFW.glfwSetWindowPos(handle, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);//set the window's position

		GLFW.glfwShowWindow(handle);//display the window
		GLFW.glfwMakeContextCurrent(handle);//create a context for the window
		
		GLFW.glfwSwapInterval(1);//enable Vsync
		isVsyncEnabled = true;//set that Vsync is enabled by default
		
		inputManager = new InputManager();//create an input manager for the window
		GLFW.glfwSetKeyCallback(handle, inputManager);//set the key event callback
	}
	
	/**Returns the handle ID for this window.*/
	public long getWindowHandle()
	{
		return handle;
	}
	
	/**Sets the window's size.
	 * @param width The width to resize the window to.
	 * @param height The height to resize the window to.*/
	public void setSize(int width, int height)
	{
		GLFW.glfwSetWindowSize(handle, width, height);//set the window's new size
		invoke(handle, width, height);//update this window's size and it's renderer if it has one
	}
	
	/**Returns the width of the window.
	 * @return The current width of the window.*/
	public int getWindowWidth()
	{
		return windowWidth;
	}
	
	/**Returns the height of the window.
	 * @return The current height of the window.*/
	public int getWindowHeight()
	{
		return windowHeight;
	}
	
	/**Sets the fullscreen mode of the window.
	 * @param fullscreen Sets the window to fullscreen if true, or makes the window normal if false.*/
	public void setFullscreen(boolean fullscreen)
	{
		if(isFullscreen != fullscreen){//if the specified mode differs from the current mode
			//TODO
		}
	}
	
	/**Returns whether the window is currently in fullscreen mode.
	 * @return True if the window is fullscreen, false otherwise.*/
	public boolean isWindowFullscreen()
	{
		return isFullscreen;
	}
	
	/**Sets whether the window should enable Vsync while rendering
	 * @param enabled Flag for whether Vsync should be enabled. Note, some displays may not use Vsync, even if enabled.*/
	public void setVsyncEnabled(boolean enabled)
	{
		if(isVsyncEnabled != enabled){//if the Vsync state needs to be changed
			GLFW.glfwSwapInterval(enabled? 1 : 0);//set whether Vsync should be enabled
			isVsyncEnabled = enabled;//store whether Vsync is enabled
		}
	}
	
	/**Returns whether Vsync is currently enabled.
	 * @return True if Vsync is enabled, false otherwise.*/
	public boolean isVsyncEnabled()
	{
		return isVsyncEnabled;
	}
	
	/**Updates the window.
	 * @return False if the window was closed, true otherwise.*/
	public boolean update()
	{
		if(GLFW.glfwWindowShouldClose(handle)){//if the window should be closed
			GLFW.glfwDestroyWindow(handle);//destroy the window
			return false;//return that the window was closed
		}
		
		inputManager.update();//update the input manager
		GLFW.glfwPollEvents();//process any received events
		
		return true;//return that execution should continue normally
	}
	
	/**Renders the contents of this window.*/
	public void render()
	{
		GLFW.glfwSwapBuffers(handle);//swap the window's back buffer with the front
	}
	
	//TODO
	public void invoke(long window, int width, int height)
	{
		windowWidth = width;//store the window's new width
		windowHeight = height;//store the window's new height

		if(renderer != null){//if this window has a renderer
			renderer.resizeViewport(windowWidth, windowHeight);//update the renderer's viewport
		}
	}
	
	/**Called whenever a key is pressed inside this window.
	 * @param key The GLFW key-code for the key that was pressed.
	 * @param scancode The system's key-code for the key that was pressed.
	 * @param modifiers Bit flags indicating which modifier keys were also being pressed.*/
	protected void onKeyPress(int key, int scancode, int modifiers)
	{
		//TODO
	}
	
	/**Called whenever a key is released inside this window.
	 * @param key The GLFW key-code for the key that was released.
	 * @param scancode The system's key-code for the key that was released.
	 * @param modifiers Bit flags indicating which modifier keys were also being pressed.*/
	protected void onKeyRelease(int key, int scancode, int modifiers)
	{
		//TODO
	}
	
	/**Manages all the input for it's parent window.*/
	protected class InputManager implements GLFWKeyCallbackI
	{
		/**Stores the number of ticks that each key has been held for, or 0 for keys not currently being held.*/
		private final int[] keyPressTimes;
		
		/**Creates a new input manager for monitoring key strokes in the window.*/
		protected InputManager()
		{
			//create an array for storing the number of ticks each key has been held for
			keyPressTimes = new int[GLFW.GLFW_KEY_LAST];
		}

		/**Called whenever GLFW detects that an action has occurred on a key.
		 * @param window The handle ID of the window that the action occurred in.
		 * @param key The GLFW key-code for the key.
		 * @param scancode The system's key-code for the key.
		 * @param action The action that was performed on the key.
		 * @param modifiers Bit flags indicating which modifier keys were also being pressed during the action.*/
		public void invoke(long window, int key, int scancode, int action, int modifiers)
		{
			if(key == GLFW.GLFW_KEY_UNKNOWN){//if an unknown key was pressed
				return;//ignore it
			}
			if(action == GLFW.GLFW_PRESS){//if the key was pressed
				keyPressTimes[key] = 1;//set that the key has been held for 1 tick
				Window.this.onKeyPress(key, scancode, modifiers);//inform the window that the key was pressed
			} else
			if(action == GLFW.GLFW_RELEASE){
				keyPressTimes[key] = 0;//set that the key is no longer being held
				Window.this.onKeyRelease(key, scancode, modifiers);//inform the window that the key was released
			}
		}
		
		/**Updates the number of ticks each key has been held for.*/
		protected void update()
		{
			for(int keyPressTime : keyPressTimes){//iterate through all the keys
				if(keyPressTime != 0){//if the key is being pressed
					keyPressTime++;//increment how many ticks the key has been held for
				}
			}
		}
		
		/**Returns whether the specified key is currently held down.
		 * @param key The GLFW key-code of the key to check.
		 * @return Whether the specified key is being held down.*/
		protected boolean isKeyDown(int key)
		{
			return (keyPressTimes[key] != 0);//return whether the key has been held for more than 0 seconds
		}
	}
	
	/**Initializes the game's window system; this must be called before any windows can be created.*/
	public static void initialize()
	{
		if(!GLFW.glfwInit()){//initialize GLFW
			throw new IllegalStateException("Failed to initialize GLFW");
		}
		
		GLFW.glfwSetErrorCallback(new GLFWErrorCallback() {
			public void invoke(int errorCode, long descriptionPointer)//wrap the GLFW error in an exception
			{
				throw new IllegalStateException(errorCode + ": " + GLFWErrorCallback.getDescription(descriptionPointer));
			}
		});//set the error callback for GLFW
	}
}
