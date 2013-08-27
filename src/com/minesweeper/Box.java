package com.minesweeper;

public class Box {
	
    public float x,y, widthBox;
    public int content = 0;
    public boolean uncovered = false;

	/**
     * Set the position and the width of the box 
     * @param x
     * @param y
     * @param ancho
     */
    public void setXY(float xx, float yy, float widthBox) {
        x = xx;
        y = yy;
        this.widthBox = widthBox;
    }
    
    /**
     * Check if we have pulsed the box  
     * @param xx
     * @param yy
     * @return boolean
     */
    public boolean isInside(float xx, float yy) {
        return ((xx >= x) && (xx <= x + widthBox) && (yy >= y) && (yy <= y + widthBox));
    }
    
    /**
     * Get the box's content
     * @return integer
     */
    public int getContent() {
		return content;
	}

    /**
     * Set the box's content
     * @param content
     */
	public void setContent(int content) {
		this.content = content;
	}

	/**
	 * Check if the box was discovered
	 * @return boolean
	 */
	public boolean isUncovered() {
		return uncovered;
	}

	/**
	 * Set the box's state
	 * @param uncovered
	 */
	public void setUncovered(boolean uncovered) {
		this.uncovered = uncovered;
	}    
    
}