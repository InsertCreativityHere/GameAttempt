
package com.insertcreativity.zoogame;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class Model
{
	/**Handle ID for the vertex coordinates of this model.*/
	private final int vertexArrayHandle;
	/**Handle ID for the texture coordinates of this model.*/
	private final int textureArrayHandle;
	/**Handle ID for the indexes of this model.*/
	private final int indexArrayHandle;
	/**The number of indexes the model has on it.*/
	private final int indexCount;
	/**The offset in the x direction to render the model at.*/
	private final float offsetX;
	/**The offset in the y direction to render the model at.*/
	private final float offsetY;
	/**The offset in the z direction to render the model at.*/
	private final float offsetZ;
	/**Map containing the name and model object for every model currently in use by the renderer.*/
	private static final HashMap<String, Model> models = new HashMap<String, Model>();
	
	public static Model createModel(String name, float[] vertexArray, float[] textureArray, int[] indexArray, float xOff, float yOff, float zOff)
	{
		if(!models.containsKey(name)){//if this model doesn't already exist
			Model model = new Model(name, vertexArray, textureArray, indexArray, xOff, yOff, zOff);//create a new model
			models.put(name, model);//add the model to the model list
		}
		
		return getModel(name);//return the model with the specified name
	}
	
	/**Creates a new model.
	 * @param name The name of this model.
	 * @param vertexArray Array of all the vertex coordinates for the model formatted as {x1, y1, x2, y2, ...}.
	 * @param textureArray Array of all the texture coordinates for the model formatted as {x1, y1, x2, y2, ...}.
	 * @param indexArray Array of the vertexes specifying the order in which to render them.
	 * @param xOff The offset to render the model at in the x direction.
	 * @param yOff The offset to render the model at in the y direction.
	 * @param zOff The offset to render the model at in the z direction.*/
	private Model(String name, float[] vertexArray, float[] textureArray, int[] indexArray, float xOff, float yOff, float zOff)
	{
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);//allocate a buffer for the vertex array
		vertexBuffer.put(vertexArray);//load the vertex array into the vertex coordinate buffer
		vertexBuffer.flip();//reset the buffer position for reading
		vertexArrayHandle = GL15.glGenBuffers();//generate a handle for the vertex coordinate buffer
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexArrayHandle);//bind the vertex coordinate buffer
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);//set the vertex coordinate buffer
		
		FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(textureArray.length);//allocate a buffer for the texture array
		textureBuffer.put(textureArray);//load the texture into the texture coordinate buffer
		textureBuffer.flip();//reset the buffer position for reading
		textureArrayHandle = GL15.glGenBuffers();//generate a handle for the texture coordinate buffer
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureArrayHandle);//bind the texture coordinate buffer
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureBuffer, GL15.GL_STATIC_DRAW);//set the texture coordinate buffer
		
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(indexArray.length);//allocate a buffer for the index array
		indexBuffer.put(indexArray);//load the index array into the index buffer
		indexBuffer.flip();//reset the buffer position for reading
		indexArrayHandle = GL15.glGenBuffers();//generate a handle for the index buffer
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexArrayHandle);//bind the index buffer
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);//set the index buffer
		
		indexCount = indexArray.length;//store the number of indexes the model has
		offsetX = xOff;//store the x offset of this model
		offsetY = yOff;//store the y offset of this model
		offsetZ = zOff;//store the z offset of this model
	}
	
	/**Returns the model object corresponding to the provided name
	 * @param name The name of the model to get.
	 * @return The model whose name matches the one specified.*/
	public static Model getModel(String name)
	{
		return models.get(name);
	}
	
	/**Renders the model with the provided texture at the specified coordinates.
	 * @param renderer The Renderer to render the entity with.
	 * @param currentShader The name of the shader currently in use.
	 * @param texture The name of the texture to render this model with.
	 * @param x The x position to render this model at.
	 * @param y The y position to render this model at.*/
	public void render(Renderer renderer, String currentShader, String texture, float x, float y)
	{
		FloatBuffer projection = renderer.getProjection(x + offsetX, y + offsetY, offsetZ);//create a projection for the model
		renderer.setUniform(currentShader, "projection", projection);//set the projection matrix
		
		//TODO more efficient handling of sample indexes
		renderer.bindTexture(texture, 0);//bind this model's texture
		renderer.setUniform(currentShader, "sampler", 0);//set the sample of this model's texture

		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);//enable vertex coordinate arrays
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);//enable texture coordinate arrays
		
		//TODO learn how to glVertexAttribPointer better
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexArrayHandle);//bind the vertex coordinate array
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);//set the vertex coordinates
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureArrayHandle);//bind the texture coordinate array
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);//set the texture coordinate
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexArrayHandle);//bind the index array
		GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);//render the model
		
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);//disable vertex coordinate arrays
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);//disable vertex coordinate arrays
	}
}
