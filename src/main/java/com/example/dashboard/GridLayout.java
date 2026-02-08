package com.example.dashboard;

import java.io.Serializable;
import java.util.*;

/**
 * Represents the complete layout of all items in the dashboard grid.
 * Contains a collection of GridItemConfig objects and provides methods for layout manipulation.
 */
public class GridLayout implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Map of item ID to configuration. Uses LinkedHashMap to preserve insertion order.
     */
    private final Map<String, GridItemConfig> items = new LinkedHashMap<>();
    
    /**
     * Revision number incremented on each change. Used for echo suppression.
     */
    private long revision = 0;
    
    /**
     * Number of columns in the grid (default: 12, compatible with react-grid-layout)
     */
    private int columns = 12;
    
    /**
     * Row height in pixels (default: 30)
     */
    private int rowHeight = 30;
    
    /**
     * Whether the grid should be compacted automatically
     */
    private boolean compact = true;
    
    /**
     * Compaction type: "vertical" | "horizontal" | null
     */
    private String compactType = "vertical";
    
    public GridLayout() {
        // Default constructor
    }
    
    public GridLayout(int columns, int rowHeight) {
        this.columns = columns;
        this.rowHeight = rowHeight;
    }
    
    /**
     * Add or update an item configuration
     */
    public void putItem(GridItemConfig config) {
        if (config == null || config.getId() == null) {
            throw new IllegalArgumentException("Config and ID must not be null");
        }
        items.put(config.getId(), config);
        revision++;
    }
    
    /**
     * Remove an item by ID
     */
    public GridItemConfig removeItem(String id) {
        GridItemConfig removed = items.remove(id);
        if (removed != null) {
            revision++;
        }
        return removed;
    }
    
    /**
     * Get item configuration by ID
     */
    public GridItemConfig getItem(String id) {
        return items.get(id);
    }
    
    /**
     * Check if an item exists
     */
    public boolean hasItem(String id) {
        return items.containsKey(id);
    }
    
    /**
     * Get all item configurations as a list
     */
    public List<GridItemConfig> getItems() {
        return new ArrayList<>(items.values());
    }
    
    /**
     * Get all item IDs
     */
    public Set<String> getItemIds() {
        return new LinkedHashSet<>(items.keySet());
    }
    
    /**
     * Get the number of items
     */
    public int size() {
        return items.size();
    }
    
    /**
     * Clear all items
     */
    public void clear() {
        if (!items.isEmpty()) {
            items.clear();
            revision++;
        }
    }
    
    /**
     * Replace all items with a new set of configurations
     */
    public void setItems(List<GridItemConfig> newItems) {
        items.clear();
        newItems.forEach(this::putItem);
        revision++;
    }
    
    /**
     * Find the next available position for a new item
     */
    public GridItemConfig findNextAvailablePosition(String id, int width, int height) {
        // Simple algorithm: find the first available spot
        // In production, you might want a more sophisticated algorithm
        
        Set<String> occupied = new HashSet<>();
        for (GridItemConfig item : items.values()) {
            for (int x = item.getX(); x < item.getX() + item.getW(); x++) {
                for (int y = item.getY(); y < item.getY() + item.getH(); y++) {
                    occupied.add(x + "," + y);
                }
            }
        }
        
        // Try to find a spot
        for (int y = 0; y < 1000; y++) { // arbitrary max
            for (int x = 0; x <= columns - width; x++) {
                boolean fits = true;
                for (int dx = 0; dx < width && fits; dx++) {
                    for (int dy = 0; dy < height && fits; dy++) {
                        if (occupied.contains((x + dx) + "," + (y + dy))) {
                            fits = false;
                        }
                    }
                }
                if (fits) {
                    return new GridItemConfig(id, x, y, width, height);
                }
            }
        }
        
        // Fallback: place at the end
        int maxY = items.values().stream()
                .mapToInt(item -> item.getY() + item.getH())
                .max()
                .orElse(0);
        return new GridItemConfig(id, 0, maxY, width, height);
    }
    
    // Getters and setters
    
    public long getRevision() {
        return revision;
    }
    
    public void setRevision(long revision) {
        this.revision = revision;
    }
    
    public int getColumns() {
        return columns;
    }
    
    public void setColumns(int columns) {
        this.columns = columns;
        revision++;
    }
    
    public int getRowHeight() {
        return rowHeight;
    }
    
    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
        revision++;
    }
    
    public boolean isCompact() {
        return compact;
    }
    
    public void setCompact(boolean compact) {
        this.compact = compact;
        revision++;
    }
    
    public String getCompactType() {
        return compactType;
    }
    
    public void setCompactType(String compactType) {
        this.compactType = compactType;
        revision++;
    }
    
    /**
     * Create a deep copy of this layout
     */
    public GridLayout copy() {
        GridLayout copy = new GridLayout(columns, rowHeight);
        copy.compact = this.compact;
        copy.compactType = this.compactType;
        copy.revision = this.revision;
        
        this.items.values().forEach(item -> {
            GridItemConfig itemCopy = new GridItemConfig(
                item.getId(), item.getX(), item.getY(), item.getW(), item.getH()
            );
            itemCopy.setMinW(item.getMinW());
            itemCopy.setMinH(item.getMinH());
            itemCopy.setMaxW(item.getMaxW());
            itemCopy.setMaxH(item.getMaxH());
            itemCopy.setStatic(item.isStatic());
            itemCopy.setDraggable(item.isDraggable());
            itemCopy.setResizable(item.isResizable());
            copy.items.put(itemCopy.getId(), itemCopy);
        });
        
        return copy;
    }
    
    @Override
    public String toString() {
        return "GridLayout{" +
                "items=" + items.size() +
                ", revision=" + revision +
                ", columns=" + columns +
                ", rowHeight=" + rowHeight +
                ", compact=" + compact +
                ", compactType='" + compactType + '\'' +
                '}';
    }
}
