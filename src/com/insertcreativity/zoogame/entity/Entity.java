
package com.insertcreativity.zoogame.entity;

public abstract class Entity
{
	private float posX;
	private float posY;
	public final float BBx1;
	public final float BBx2;
	public final float BBy1;
	public final float BBy2;
	public final String model;
	private boolean isCollidable;
	
	public Entity(float x, float y, float topBB, float rightBB, float bottomBB, float leftBB, String modelName)
	{
		posX = x;
		posY = y;
		BBy2 = topBB;
		BBx2 = rightBB;
		BBy1 = bottomBB;
		BBx1 = leftBB;
		model = modelName;
		isCollidable = true;
	}
	
	public void setPosition(float x, float y)
	{
		posX = x;
		posY = y;
	}
	
	public float getX()
	{
		return posX;
	}
	
	public float getY()
	{
		return posY;
	}
	
	protected void setCollidable(boolean collidable)
	{
		isCollidable = collidable;
	}
	
	public boolean checkCollision(Entity entity)
	{
		if(entity.isCollidable && isCollidable){
			return checkCollision(entity.BBx1 + entity.posX, entity.BBy1 + posY, entity.BBx2 + posX, entity.BBy2 + posY);
		}
		return false;
	}
	
	public boolean checkCollision(float x1, float y1, float x2, float y2)
	{
		return !(((BBx2 + posX) < x1) || ((BBx1 + posX) > x2) || ((BBy2 + posY) < y1) || ((BBy1 + posY) > y2));
	}
}
