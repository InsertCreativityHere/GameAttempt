//should do any cleanup after destroying window?
package com.insertcreativity.zoogame;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.glfw.GLFWVidMode;

public class Window implements GLFWWindowSizeCallbackI
{
	/**The handle ID for the window.*/
	public final long handle;
	/**The width of the window.*/
	private int windowWidth;
	/**The height of the window.*/
	private int windowHeight;
	/**Flag for whether the window is fullscreen.*/
	private boolean isFullscreen;
	/**Flag for whether Vsync is enabled.*/
	private boolean isVsyncEnabled;
	/**Object that handles keyboard input for the window.*/
	private final KeyManager keyManager;
	/**Object that handles mouse input for the window.*/
	private final MouseManager mouseManager;
	/**Object that handles scroll wheel input for the window.*/
	private final ScrollManager scrollManager;
	/**Object that tracks the mouse cursor for the window.*/
	private final CursorManager cursorManager;
	/**Reference to the instance of the game.*/
	protected final Main game;
	
	/**Creates a new window with the specified parameters.
	 * @param main Reference to the game running in this window.
	 * @param width The width to make the screen. (in pixels)
	 * @param height The height to make the screen. (in pixels)
	 * @param title The title to display for the window.
	 * @param monitor Handle ID for the monitor to display the window on.
	 * @param fullscreen Flag for whether the window should be fullscreen.
	 * @throws IllegalStateException If the window couldn't be created successfully.*/
	public Window(Main main, int width, int height, String title, long monitor, boolean fullscreen) throws IllegalStateException
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
		
		keyManager = new KeyManager();//create a key manager for the window
		GLFW.glfwSetKeyCallback(handle, keyManager);//set the key event callback
		mouseManager = new MouseManager();//create a mouse manager for the window
		GLFW.glfwSetMouseButtonCallback(handle, mouseManager);//set the mouse manager callback
		scrollManager = new ScrollManager();//create a new scroll manager for the window
		GLFW.glfwSetScrollCallback(handle, scrollManager);//set the scroll callback
		cursorManager = new CursorManager();//create a cursor manager for the window
		GLFW.glfwSetCursorPosCallback(handle, cursorManager);//set the cursor position callback
		
		game = main;//store a reference to the game instance using this window
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
	protected boolean update()
	{
		if(GLFW.glfwWindowShouldClose(handle)){//if the window should be closed
			GLFW.glfwDestroyWindow(handle);//destroy the window
			return false;//return that the window was closed
		}
		
		keyManager.update();//update the key manager
		mouseManager.update();//update the mouse manager
		GLFW.glfwPollEvents();//process any received events
		
		return true;//return that execution should continue normally
	}
	
	/**Renders the contents of this window.*/
	protected void render()
	{
		GLFW.glfwSwapBuffers(handle);//swap the window's back buffer with the front
	}

	/**Called whenever GLFW detects that the window's size has changed.
	 * @param window The handle ID of the window that was resized.
	 * @param width The new width of the window.
	 * @param height The new height of the window.*/
	public void invoke(long window, int width, int height)
	{
		windowWidth = width;//store the window's new width
		windowHeight = height;//store the window's new height

		game.onWindowResize(windowWidth, windowHeight);//inform the game that the window was resized
	}
	
	/**Returns whether the specified key is currently held down.
	 * @param key The GLFW key-code of the key to check.
	 * @return True if the key is currently being pressed, false otherwise.*/
	public boolean isKeyDown(int key)
	{
		return (keyManager.keyPressTimes[key] != 0);//return whether the key has been held for more than 0 seconds
	}
	
	/**Returns how long the specified key has been held down for.
	 * @param key The GLFW key-code of the key to check.
	 * @return The number of ticks that the specified key has been pressed for.*/
	public int getKeyPressTime(int key)
	{
		return keyManager.keyPressTimes[key];
	}
	
	/**Returns whether the specified button is currently held down.
	 * @param button The GLFW button-code of the button to check.
	 * @return True if the button is currently being pressed, false otherwise.*/
	public boolean isButtonDown(int button)
	{
		return (mouseManager.mousePressTimes[button] != 0);//return whether the button has been held for more than 0 seconds
	}
	
	/**Returns how long the specified button has been held down for.
	 * @param button The GLFW button-code of the button to check.
	 * @return The number of ticks that the specified button has been pressed for.*/
	public int getButtonPressTime(int button)
	{
		return mouseManager.mousePressTimes[button];
	}
	
	/**Returns the cursor's current x position in the window.
	 * @return The x coordinate of the cursor.*/
	public double getMouseX()
	{
		return cursorManager.posX;
	}
	
	/**Returns the cursor's current y position in the window.
	 * @return The y coordinate of the cursor.*/
	public double getMouseY()
	{
		return cursorManager.posY;
	}
	
	/**Manages keyboard input for it's parent window.*/
	private class KeyManager implements GLFWKeyCallbackI
	{
		/**Stores the number of ticks that each key has been held for, or 0 for keys not currently being held.*/
		private final int[] keyPressTimes;
		
		/**Creates a new manager for monitoring key strokes in the window.*/
		private KeyManager()
		{
			keyPressTimes = new int[GLFW.GLFW_KEY_LAST];//create an array for storing the number of ticks each key has been held for
		}
		
		/**Called whenever GLFW detects that an action has occurred on a key.
		 * @param window The handle ID of the window that the action occurred in.
		 * @param key The GLFW key-code for the key.
		 * @param scancode The system's key-code for the key.
		 * @param action The action that was performed on the key.
		 * @param modifiers Bit flags indicating which modifier keys were being pressed during the action.*/
		public void invoke(long window, int key, int scancode, int action, int modifiers)
		{
			if(key == GLFW.GLFW_KEY_UNKNOWN){//if an unknown key was pressed
				return;//ignore it
			}
			if(action == GLFW.GLFW_PRESS){//if the key was pressed
				keyPressTimes[key] = 1;//set that the key has been held for 1 tick
				Window.this.game.onKeyPress(key, scancode, modifiers);//inform the game that the key was pressed
			} else
			if(action == GLFW.GLFW_RELEASE){//if the key was released
				keyPressTimes[key] = 0;//set that the key is no longer being held
				Window.this.game.onKeyRelease(key, scancode, modifiers);//inform the game that the key was released
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
	}
	
	/**Manages mouse input for it's parent window.*/
	private class MouseManager implements GLFWMouseButtonCallbackI
	{
		/**Stores the number of ticks that each mouse button has been held for, or 0 for buttons not currently being held.*/
		private final int[] mousePressTimes;
		
		/**Creates a new manager for monitoring mouse button presses in the window.*/
		private MouseManager()
		{
			mousePressTimes = new int[GLFW.GLFW_MOUSE_BUTTON_LAST];//create an array for storing the number of ticks each button has been held for
		}
		
		/**Called whenever GLFW detects that an action has occurred on a mouse button.
		 * @param window The handle ID of the window that the action occurred in.
		 * @param button The GLFW button-code for the button.
		 * @param scancode The system's button-code for the button.
		 * @param action The action that was performed on the button.
		 * @param modifiers Bit flags indicating which modifier keys were being pressed during the action.*/
		public void invoke(long window, int button, int action, int mods)
		{
			if(action == GLFW.GLFW_PRESS){//if the button was pressed
				mousePressTimes[button] = 1;//set that the button has been held for 1 tick
				Window.this.game.onButtonPress(button, mods);//inform the game that the button was pressed
			} else
			if(action == GLFW.GLFW_RELEASE){//if the button was released
				mousePressTimes[button] = 0;//set that the button is no longer being held
				Window.this.game.onButtonRelease(button, mods);//inform the game that the button was released
			}
		}
		
		/**Updates the number of ticks each button has been held for*/
		protected void update()
		{
			for(int mousePressTime : mousePressTimes){//iterate through all the buttons
				if(mousePressTime != 0){//if the button is being pressed
					mousePressTime++;//increment how many ticks the button has been held for
				}
			}
		}
	}
	
	/**Manages scroll wheel input for it's parent window*/
	private class ScrollManager implements GLFWScrollCallbackI
	{
		/**Creates a new scroll manager for monitoring scrolling in the window.*/
		private ScrollManager(){}

		/**Called whenever GLFW detects that the scroll wheel has been moved.
		 * @param window The handle ID of the window that the wheel was scrolled in.
		 * @param xoffset The amount that the wheel was scrolled in the x direction.
		 * @param yoffset The amount that the wheel was scrolled in the y direction.*/
		public void invoke(long window, double xoffset, double yoffset)
		{
			Window.this.game.onMouseScrolled(xoffset, yoffset);//inform the game that the scroll wheel was scrolled
		}
	}
	
	/**Tracks and stores the position of the mouse cursor through the window.*/
	private class CursorManager implements GLFWCursorPosCallbackI
	{
		/**The current x coordinate of the cursor in the window.*/
		private double posX;
		/**The current y coordinate of the cursor in the window.*/
		private double posY;
		
		/**Creates a new manager for tracking the cursor's position in the window.*/
		private CursorManager(){}

		/**Called whenever GLFW detects that the cursor has been moved.
		 * @param window The handle ID of the window that the cursor was moved in.
		 * @param xpos The x coordinate of the cursor's new position.
		 * @param ypos The y coordinate of the cursor's new position.*/
		public void invoke(long window, double xpos, double ypos)
		{
			posX = xpos;//update the x position
			posY = ypos;//update the y position
			Window.this.game.onCursorMove(posX, posY);//inform the game that the cursor was moved
		}
	}
	
	/**Initializes the game's window system; this must be called before any windows can be created.*/
	protected static void initialize()
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
