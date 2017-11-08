
package com.insertcreativity.zoogame.menu;

import com.insertcreativity.zoogame.Renderer;
import com.insertcreativity.zoogame.Screen;
import com.insertcreativity.zoogame.Window;

public class MainMenu implements Screen
{
	public void update(Window window)
	{
	}

	public void render(Renderer renderer)
	{
	}

	public void keyPresssed(int key, int scancode, int modifiers)
	{
		System.out.println("key pressed:" + key);
	}

	public void keyReleased(int key, int scancode, int modifiers)
	{
		System.out.println("key released:" + key);
	}

	public void buttonPressed(int button, int modifiers)
	{
		System.out.println("button pressed:" + button);
	}

	public void buttonReleased(int button, int modifiers)
	{
		System.out.println("button released:" + button);
	}

	public void mouseScrolled(double x, double y)
	{
		System.out.println("scrolled:" + x + "," + y);
	}

	public void cursorMoved(double x, double y)
	{
		System.out.println("moved:" + x + "," + y);
	}
}
