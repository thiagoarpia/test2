package com.example.dashboard;

import java.io.Serializable;
import java.util.Objects;

/**
 * Configuration for a single grid item in the dashboard.
 * Maps to react-grid-layout's layout item structure.
 * 
 * All fields are mutable to allow dynamic updates.
 */
public class GridItemConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Unique identifier for this grid item. Must be stable across updates.
     */
    private String id;
    
    /**
     * X position in grid units (columns)
     */
    private int x;
    
    /**
     * Y position in grid units (rows)
     */
    private int y;
    
    /**
     * Width in grid units
     */
    private int w;
    
    /**
     * Height in grid units
     */
    private int h;
    
    /**
     * Minimum width in grid units (optional)
     */
    private Integer minW;
    
    /**
     * Minimum height in grid units (optional)
     */
    private Integer minH;
    
    /**
     * Maximum width in grid units (optional)
     */
    private Integer maxW;
    
    /**
     * Maximum height in grid units (optional)
     */
    private Integer maxH;
    
    /**
     * If true, item cannot be dragged or resized
     */
    private boolean isStatic = false;
    
    /**
     * If true, item cannot be dragged (but can be resized)
     */
    private boolean isDraggable = true;
    
    /**
     * If true, item can be resized
     */
    private boolean isResizable = true;
    
    public GridItemConfig() {
        // Default constructor for deserialization
    }
    
    public GridItemConfig(String id, int x, int y, int w, int h) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
    
    /**
     * Creates a new config with default size and position
     */
    public static GridItemConfig create(String id) {
        return new GridItemConfig(id, 0, 0, 4, 3);
    }
    
    /**
     * Creates a config at a specific position
     */
    public static GridItemConfig at(String id, int x, int y, int w, int h) {
        return new GridItemConfig(id, x, y, w, h);
    }
    
    // Fluent setters for builder-style usage
    
    public GridItemConfig withMinSize(int minW, int minH) {
        this.minW = minW;
        this.minH = minH;
        return this;
    }
    
    public GridItemConfig withMaxSize(int maxW, int maxH) {
        this.maxW = maxW;
        this.maxH = maxH;
        return this;
    }
    
    public GridItemConfig asStatic() {
        this.isStatic = true;
        return this;
    }
    
    public GridItemConfig notDraggable() {
        this.isDraggable = false;
        return this;
    }
    
    public GridItemConfig notResizable() {
        this.isResizable = false;
        return this;
    }
    
    // Standard getters and setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getW() {
        return w;
    }
    
    public void setW(int w) {
        this.w = w;
    }
    
    public int getH() {
        return h;
    }
    
    public void setH(int h) {
        this.h = h;
    }
    
    public Integer getMinW() {
        return minW;
    }
    
    public void setMinW(Integer minW) {
        this.minW = minW;
    }
    
    public Integer getMinH() {
        return minH;
    }
    
    public void setMinH(Integer minH) {
        this.minH = minH;
    }
    
    public Integer getMaxW() {
        return maxW;
    }
    
    public void setMaxW(Integer maxW) {
        this.maxW = maxW;
    }
    
    public Integer getMaxH() {
        return maxH;
    }
    
    public void setMaxH(Integer maxH) {
        this.maxH = maxH;
    }
    
    public boolean isStatic() {
        return isStatic;
    }
    
    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }
    
    public boolean isDraggable() {
        return isDraggable;
    }
    
    public void setDraggable(boolean isDraggable) {
        this.isDraggable = isDraggable;
    }
    
    public boolean isResizable() {
        return isResizable;
    }
    
    public void setResizable(boolean isResizable) {
        this.isResizable = isResizable;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridItemConfig that = (GridItemConfig) o;
        return x == that.x && y == that.y && w == that.w && h == that.h 
                && isStatic == that.isStatic && isDraggable == that.isDraggable 
                && isResizable == that.isResizable && Objects.equals(id, that.id) 
                && Objects.equals(minW, that.minW) && Objects.equals(minH, that.minH) 
                && Objects.equals(maxW, that.maxW) && Objects.equals(maxH, that.maxH);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, x, y, w, h, minW, minH, maxW, maxH, isStatic, isDraggable, isResizable);
    }
    
    @Override
    public String toString() {
        return "GridItemConfig{" +
                "id='" + id + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                ", minW=" + minW +
                ", minH=" + minH +
                ", maxW=" + maxW +
                ", maxH=" + maxH +
                ", isStatic=" + isStatic +
                ", isDraggable=" + isDraggable +
                ", isResizable=" + isResizable +
                '}';
    }
}
