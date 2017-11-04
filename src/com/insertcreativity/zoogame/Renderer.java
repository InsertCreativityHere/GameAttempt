//implement delete methods for textures and shaders
package com.insertcreativity.zoogame;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public class Renderer
{
	/**Map containing the names and handle IDs for all the shaders in use by this renderer.*/
	private final HashMap<String, Integer> shaders;
	/**The x coordinate of the camera.*/
	private float cameraX;
	/**The y coordinate of the camera.*/
	private float cameraY;
	/**The z coordinate of the camera.*/
	private float cameraZ;
	/**The zoom factor of the camera.*/
	private float cameraZoom;
	/**The width of the viewport.*/
	private int viewportWidth;
	/**The height of the viewport.*/
	private int viewportHeight;
	/**A 4x4 matrix for the camera's viewport projection*/
	private FloatBuffer viewportProjection;
	/**Map containing the names and objects for all the textures loaded by this renderer.*/
	private final HashMap<String, Texture> textures;
	
	/**Creates a new Renderer object used for rendering the game and it's content to the screen.
	 * @param viewWidth The width of the viewport the game is being rendered in.
	 * @param viewHeight The height of the viewport the game is being rendered in.
	 * @param posX The initial x coordinate of the camera.
	 * @param posY The initial y coordinate of the camera.
	 * @param posZ The initial z coordinate of the camera.
	 * @param zoom The initial zoom level of the camera.*/
	public Renderer(int viewWidth, int viewHeight, float posX, float posY, float posZ, float zoom) throws IllegalStateException
	{
		GL.createCapabilities();//create the context's capabilities
		GL11.glClearColor(0f, 0f, 0f, 1f);//set the buffer clear color
		GL11.glEnable(GL11.GL_TEXTURE_2D);//enable 2D texture rendering
		GL11.glEnable(GL11.GL_BLEND);//enable texture transparency
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);//set the transparency blending function
		
		shaders = new HashMap<String, Integer>();//create a new map for storing shaders in
		
		try{
			shaders.put("default", createShader("default.glsl"));//create and store the default shader
		} catch(IOException ioException){//if the shader source file couldn't be loaded
			throw new IllegalStateException("GLSL shader file is missing or damaged");
		}
		
		GL20.glUseProgram(shaders.get("default"));//load the default shader into the renderer
		
		cameraX = posX;//set the camera's initial x coordinate
		cameraY = posY;//set the camera's initial y coordinate
		cameraZ = posZ;//set the camera's initial z coordinate
		cameraZoom = zoom;//set the camera's initial zoom
		viewportWidth = viewWidth;//set the width of the viewport
		viewportHeight = viewHeight;//set the height of the viewport
		
		viewportProjection = BufferUtils.createFloatBuffer(16);//create a new float buffer for the projection
		viewportProjection.put(0, 2 * zoom / viewWidth);//set the x component of the projection
		viewportProjection.put(1, 0);
		viewportProjection.put(2, 0);
		viewportProjection.put(3, 0);
		viewportProjection.put(4, 0);
		viewportProjection.put(5, 2 * zoom / viewHeight);//set the y component of the projection
		viewportProjection.put(6, 0);
		viewportProjection.put(7, 0);
		viewportProjection.put(8, 0);
		viewportProjection.put(9, 0);
		viewportProjection.put(10, -zoom);//set the z component of the projection
		viewportProjection.put(11, 0);
		viewportProjection.put(15, 1);
		
		textures = new HashMap<String, Texture>();//create a new map for storing textures in
	}
	
	/**Creates a new shader by compiling the provided GLSL file.
	 * @param shaderSourceFile File containing the GLSL source code to compile into the shader.
	 * @throws IllegalStateException If the shader couldn't be compiled or loaded correctly.
	 * @throws IOException If the shader source code couldn't be loaded successfully.*/
	public int createShader(String shaderSource) throws IllegalStateException, IOException
	{
		StringBuilder vertexShaderSource = new StringBuilder();//create a string-builder for storing the vertex shader source code
		StringBuilder fragmentShaderSource = new StringBuilder();//create a string-builder for storing the fragment shader source code

		try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(shaderSource), "UTF-8"))){//open the GLSL source file
			String line = bufferedReader.readLine();//read the first line of the shader source code
			while(line != null){//continues reading from the file until EOF is reached
				if(line.equals("//<vertex>")){//if the following lines are vertex source code
					line = bufferedReader.readLine();//skip the vertex source section marker and read the next line
					if(line == null){//if EOF is reached while still inside the vertex source section
						throw new IOException("Unexpected EOF while parsing: <vertex> tag");
					}
					while(!line.equals("//</vertex>")){//continue reading until the end of the vertex source section
						vertexShaderSource.append(line).append('\n');//add the newline stripped by the reader
						line = bufferedReader.readLine();//read the next line of vertex source code
						if(line == null){//if EOF is reached while still inside the vertex source section
							throw new IOException("Unexpected EOF while parsing: <vertex> tag");
						}
					}
				} else
				if(line.equals("//<fragment>")){//if the following lines are fragment source code
					line = bufferedReader.readLine();//skip the fragment source section marker and read the next line
					if(line == null){//if EOF is reached while still inside the fragment source section
						throw new IOException("Unexpected EOF while parsing: <fragment> tag");
					}
					while(!line.equals("//</fragment>")){//continue reading until the end of the fragment source section
						fragmentShaderSource.append(line).append('\n');//add the newline stripped by the reader
						line = bufferedReader.readLine();//read the next line of fragment source code
						if(line == null){//if EOF is reached while still inside the fragment source section
							throw new IOException("Unexpected EOF while parsing: <fragment> tag");
						}
					}
				}
				line = bufferedReader.readLine();//read the next line of the shader source
			}
		}
		
		return createShader(vertexShaderSource.toString(), fragmentShaderSource.toString());//load and compile the shader from the loaded source
	}
		
	/**Creates a new shader by compiling the provided strings as GLSL source code
	 * @param vertexShaderSource String containing GLSL source code to compile into the vertex shader.
	 * @param fragmentShaderSource String containing GLSL source code to compile into the fragment shader.
	 * @param The handle ID for the created shader
	 * @throws IllegalStateException If the shader couldn't be compiled or loaded correctly.*/
	public int createShader(String vertexShaderSource, String fragmentShaderSource) throws IllegalStateException
	{
		int handle = GL20.glCreateProgram();//create a new shader program
		
		int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);//create a vertex shader
		GL20.glShaderSource(vs, vertexShaderSource);//attach the GLSL vertex shader source code
		GL20.glCompileShader(vs);//compile the vertex shader
		if(GL20.glGetShaderi(vs, GL20.GL_COMPILE_STATUS) != 1){//if the vertex shader failed to compile
			throw new IllegalStateException("Failed to compile shader\n" + GL20.glGetShaderInfoLog(vs));
		}
		
		int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);//create a fragment shader
		GL20.glShaderSource(fs, fragmentShaderSource);//attach the GLSL fragment shader source code
		GL20.glCompileShader(fs);//compile the fragment shader
		if(GL20.glGetShaderi(fs, GL20.GL_COMPILE_STATUS) != 1){//if the fragment shader failed to compile
			throw new IllegalStateException("Failed to compile shader\n" + GL20.glGetShaderInfoLog(fs));
		}
		
		GL20.glAttachShader(handle, vs);//attach the vertex shader to the shader program
		GL20.glAttachShader(handle, fs);//attach the fragment shader to the shader program
		
		GL20.glBindAttribLocation(handle, 0, "vertices");//create an attribute for the vertices
		GL20.glBindAttribLocation(handle, 1, "textureCoords");//create an attribute for the texture coordinates
		
		GL20.glLinkProgram(handle);//link the shader program
		if(GL20.glGetProgrami(handle, GL20.GL_LINK_STATUS) != 1){//if the shader program couldn't be linked
			throw new IllegalStateException("Failed to link shader\n" + GL20.glGetProgramInfoLog(handle));
		}
		GL20.glValidateProgram(handle);//validate the shader program
		if(GL20.glGetProgrami(handle, GL20.GL_VALIDATE_STATUS) != 1){//if the shader program was invalid
			throw new IllegalStateException("Failed to validate shader\n" + GL20.glGetProgramInfoLog(handle));
		}
		
		GL20.glDetachShader(handle, vs);//detach the vertex shader from the program
		GL20.glDetachShader(handle, fs);//detach the fragment shader from the program
		GL20.glDeleteShader(vs);//delete the vertex shader
		GL20.glDeleteShader(fs);//delete the fragment shader
		
		return handle;//return the shader's handle ID
	}
	
	/**Binds the specified shader for use.
	 * @param shaderName The name of the shader to bind.*/
	public void bindShader(String shaderName)
	{
		GL20.glUseProgram(shaders.get(shaderName));//load the specified shader
	}
	
	/**Sets the value of a uniform variable for the specified shader.
	 * @param shaderName The name of the shader to set the variable for.
	 * @param uniform The name of the uniform variable that is being set.
	 * @param i The value to set the uniform to.*/
	public void setUniform(String shaderName, String uniform, int i)
	{
		int location = GL20.glGetUniformLocation(shaders.get(shaderName), uniform);//get the location of the specified uniform
		if(location != -1){//if the uniform exists
			GL20.glUniform1i(location, i);//set the value of the uniform
		}
	}
	
	//TODO is this needed?
	/**Sets the value of a uniform variable for the specified shader.
	 * @param shaderName The name of the shader to set the variable for.
	 * @param uniform The name of the uniform variable that is being set.
	 * @param f The value to set the uniform to.*/
	public void setUniform(String shaderName, String uniform, float f)
	{
		int location = GL20.glGetUniformLocation(shaders.get(shaderName), uniform);//get the location of the specified uniform
		if(location != -1){//if the uniform exists
			GL20.glUniform1f(location, f);//set the value of the uniform
		}
	}
	
	/**Sets the value of a uniform variable for the specified shader.
	 * @param shaderName The name of the shader to set the variable for.
	 * @param uniform The name of the uniform variable that is being set.
	 * @param fb The value to set the uniform to.*/
	public void setUniform(String shaderName, String uniform, FloatBuffer fb)
	{
		int location = GL20.glGetUniformLocation(shaders.get(shaderName), uniform);//get the location of the specified uniform
		if(location != -1){//if the uniform exists
			GL20.glUniformMatrix4fv(location, false, fb);//set the value of the uniform
		}
	}
	
	/**Updates the size of the viewport the game is rendering in.
	 * @param width The new width of the viewport.
	 * @param height The new height of the viewport.*/
	public void resizeViewport(int width, int height)
	{
		viewportWidth = width;
		viewportHeight = height;
		
		viewportProjection.put(0, 2 * cameraZoom / viewportWidth);//update the x component of the projection
		viewportProjection.put(5, 2 * cameraZoom / viewportHeight);//update the y component of the projection
	}
	
	/**Translates the camera through the world by the specified amounts.
	 * @param deltaX The amount to move the camera in the x-direction.
	 * @param deltaY The amount to move the camera in the y-direction.
	 * @param deltaZ The amount to move the camera in the z-direction.*/
	public void moveCamera(float deltaX, float deltaY, float deltaZ)
	{
		cameraX += deltaX;
		cameraY += deltaY;
	}
	
	/**Sets the position of the camera in the world.
	 * @param x The x coordinate to move the camera to.
	 * @param y The y coordinate to move the camera to.
	 * @param z The z coordinate to move the camera to.*/
	public void setCameraPosition(float x, float y, float z)
	{
		cameraX = x;
		cameraY = y;
		cameraZ = z;
	}
	
	/**Sets the zoom factor of the camera in the world.
	 * @param zoom The new zoom factor for the camera.*/
	public void setCameraZoom(float zoom)
	{
		cameraZoom = zoom;
		
		viewportProjection.put(0, 2 * zoom / viewportWidth);//update the x component of the projection
		viewportProjection.put(5, 2 * zoom / viewportHeight);//update the y component of the projection
		viewportProjection.put(10, -zoom);//update the z component of the projection
	}
	
	/**Creates and returns the projection matrix for an object as the specified position. Note that subsequent calls will change the viewport projection.
	 * @param posX The x coordinate of the object.
	 * @param posY The y coordinate of the object.
	 * @return A projection matrix for the object.*/
	public FloatBuffer getProjection(float posX, float posY, float posZ)
	{
		viewportProjection.put(11, 2 * ((cameraZoom * cameraX) + posZ) / viewportWidth);//update the x position of the object
		viewportProjection.put(12, 2 * ((cameraZoom * cameraY) + posY) / viewportHeight);//update the y position of the object
		viewportProjection.put(13, -((cameraZoom * cameraZ) + posZ));//update the z position of the object
		
		return viewportProjection;//return the viewport projection
	}
	
	/**Loads a texture into the game.
	 * @param textureName The name of the texture to load.*/
	public void loadTexture(String textureName)
	{
		try{
			textures.put(textureName, new Texture(textureName));//load and store the texture
		} catch(IOException ioException){//if the texture couldn't be loaded successfully
			//TODO
		}
	}
	
	/**Binds a texture to the specified sample for use.
	 * @param textureName The name of the texture to bind.
	 * @param sampleIndex The index of the sample to bind the texture to (must be between 0 and 31).*/
	public void bindTexture(String textureName, int sampleIndex)
	{
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + sampleIndex);//switch to the specified sample index
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(textureName).handle);//bind the texture to the sample
	}
	
	/**Renders an entity.
	 * @param entity The entity to render in the world. If the entity's bounding box is entirely
	 *               outside the viewport, the entity isn't rendered. */
	public void renderEntity(Entity entity)
	{
		
	}
	
	private class Texture
	{
		/**The handle ID for this texture.*/
		final int handle;
		/**The width of this texture.*/
		final int width;
		/**The height of this texture.*/
		final int height;
		
		/**Loads a texture from {textureName}.png into the game. Note the file is resolved from the classpath.
		 * @param textureName The name of the texture and the file to load it from
		 * @throws IOException If the texture couldn't be loaded properly.*/
		Texture(String textureName) throws IOException
		{
			BufferedImage bufferedImage = ImageIO.read(getClass().getResourceAsStream(textureName + ".png"));//read the texture's file into a buffered-image

			width = bufferedImage.getWidth();//store the width of this texture
			height = bufferedImage.getHeight();//store the height of this texture

			ByteBuffer pixelData = BufferUtils.createByteBuffer(width * height * 4);//allocate a byte buffer for storing the pixel data
			for(int pixel : bufferedImage.getRGB(0, 0, width, height, null, 0, height)){//iterate through all the pixels in the texture
				pixelData.put((byte)((pixel >> 16) & 0xff));//store the pixel's R component
				pixelData.put((byte)((pixel >> 8) & 0xff));//store the pixel's G component
				pixelData.put((byte)(pixel & 0xff));//store the pixel's B component
				pixelData.put((byte)((pixel >> 24) & 0xff));//store the pixel's A component
			}
			pixelData.flip();//reset the buffer position for reading
			
			handle = GL11.glGenTextures();//generate a handle for this texture and store it
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);//bind the texture
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);//set the texture to be scaled down using 'nearest-neighbor'
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);//set the texture to be scaled up using 'nearest-neighbor'
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelData);//create the texture
		}
	}
}
