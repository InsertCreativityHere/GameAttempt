
package com.insertcreativity.zoogame;

public interface Screen
{
	/**This method is called to update the current state of the screen.
	 * @param window Reference to the window that the screen is being displayed in*/
	public void update(Window window);
	
	/**This method is called to render the screen to the window it's being displayed in.
	 * @param renderer Reference to the object this screen should render with.*/
	public void render(Renderer renderer);
	
	/**This method is called whenever a key is pressed while this screen has focus.
	 * @param key The GLFW key-code for the key that was pressed.
	 * @param scancode The system's key-code for the key that was pressed.
	 * @param modifiers Bit flags indicating which modifier keys were also being pressed.*/
	public void keyPresssed(int key, int scancode, int modifiers);
	
	/**This method is called whenever a key is released while this screen has focus.
	 * @param key The GLFW key-code for the key that was released.
	 * @param scancode The system's key-code for the key that was released.
	 * @param modifiers Bit flags indicating which modifier keys were also being pressed.*/
	public void keyReleased(int key, int scancode, int modifiers);
	
	/**This method is called whenever a button is pressed while this screen has focus.
	 * @param button The GLFW button-code for the button that was pressed.
	 * @param modifiers Bit flags indicating which modifier keys were also being pressed.*/
	public void buttonPressed(int button, int modifiers);
	
	/**This method is called whenever a button is released while this screen has focus.
	 * @param button The GLFW button-code for the button that was released.
	 * @param modifiers Bit flags indicating which modifier keys were also being pressed.*/
	public void buttonReleased(int button, int modifiers);
	
	/**This method is called whenever the scroll wheel is scrolled while this screen has focus.
	 * @param x The amount that the scroll wheel was moved in the x direction.
	 * @param y The amount that the scroll wheel was moved in the y direction.*/
	public void mouseScrolled(double x, double y);
	
	/**This method is called whenever the cursor moves while this screen has focus.
	 * @param x The new x coordinate of the cursor.
	 * @param y The new y coordinate of the cursor.*/
	public void cursorMoved(double x, double y);
}
