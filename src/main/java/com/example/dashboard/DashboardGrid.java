package com.example.dashboard;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A production-ready Vaadin Flow component that wraps react-grid-layout
 * to provide a draggable, resizable dashboard grid.
 * 
 * Each grid item can contain arbitrary Vaadin components using slots.
 * 
 * Usage example:
 * <pre>
 * DashboardGrid grid = new DashboardGrid();
 * 
 * Button button = new Button("Click me");
 * grid.addItem("btn1", button, GridItemConfig.at("btn1", 0, 0, 4, 3));
 * 
 * TextField field = new TextField("Name");
 * grid.addItem("field1", field, GridItemConfig.at("field1", 4, 0, 4, 3));
 * 
 * grid.addLayoutChangeListener(event -> {
 *     if (event.isFinal()) {
 *         // Save layout to database
 *         saveLayout(event.getLayout());
 *     }
 * });
 * </pre>
 */
@Tag("dashboard-grid")
@NpmPackage(value = "react", version = "18.2.0")
@NpmPackage(value = "react-dom", version = "18.2.0")
@NpmPackage(value = "react-grid-layout", version = "1.4.4")
@NpmPackage(value = "lit", version = "3.1.0")
@JsModule("./dashboard-grid.ts")
public class DashboardGrid extends Component implements HasSize, HasStyle {
    
    private final GridLayout layout;
    private final Map<String, Component> itemComponents = new ConcurrentHashMap<>();
    private long lastClientRevision = 0;
    private boolean suppressEcho = false;
    
    /**
     * Create a new dashboard grid with default settings (12 columns, 30px row height)
     */
    public DashboardGrid() {
        this(new GridLayout());
    }
    
    /**
     * Create a dashboard grid with specific column and row height settings
     */
    public DashboardGrid(int columns, int rowHeight) {
        this(new GridLayout(columns, rowHeight));
    }
    
    /**
     * Create a dashboard grid with an existing layout
     */
    public DashboardGrid(GridLayout layout) {
        this.layout = Objects.requireNonNull(layout, "Layout must not be null");
        
        // Set default size
        setWidth("100%");
        setHeight("600px");
        
        // Initialize the element
        initializeElement();
        
        // Setup client-to-server communication
        setupClientListeners();
        
        // Sync initial layout to client
        syncLayoutToClient();
    }
    
    private void initializeElement() {
        Element element = getElement();
        
        // Set grid configuration properties
        element.setProperty("columns", layout.getColumns());
        element.setProperty("rowHeight", layout.getRowHeight());
        element.setProperty("compact", layout.isCompact());
        
        if (layout.getCompactType() != null) {
            element.setProperty("compactType", layout.getCompactType());
        }
    }
    
    private void setupClientListeners() {
        // Listen for layout changes from the client
        getElement().addEventListener("layout-changed", event -> {
            if (suppressEcho) {
                return; // Ignore echo from our own update
            }
            
            String itemsJson = event.getEventData().getString("event.detail.items");
            String itemId = event.getEventData().getString("event.detail.itemId");
            String reason = event.getEventData().getString("event.detail.reason");
            boolean isDragging = event.getEventData().getBoolean("event.detail.isDragging");
            boolean isResizing = event.getEventData().getBoolean("event.detail.isResizing");
            long clientRevision = (long) event.getEventData().getNumber("event.detail.revision");
            
            // Avoid processing old events
            if (clientRevision <= lastClientRevision) {
                return;
            }
            lastClientRevision = clientRevision;
            
            // Update layout from client data
            LayoutSerializer.updateItemsFromJson(layout, itemsJson);
            
            // Determine change reason
            LayoutChangeEvent.ChangeReason changeReason = parseChangeReason(reason);
            
            // Fire event to listeners
            fireEvent(new LayoutChangeEvent(
                this,
                true, // from client
                layout,
                itemId,
                changeReason,
                isDragging,
                isResizing,
                clientRevision
            ));
        }).addEventData("event.detail.items")
          .addEventData("event.detail.itemId")
          .addEventData("event.detail.reason")
          .addEventData("event.detail.isDragging")
          .addEventData("event.detail.isResizing")
          .addEventData("event.detail.revision");
    }
    
    private LayoutChangeEvent.ChangeReason parseChangeReason(String reason) {
        if (reason == null) {
            return LayoutChangeEvent.ChangeReason.UNKNOWN;
        }
        try {
            return LayoutChangeEvent.ChangeReason.valueOf(reason.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LayoutChangeEvent.ChangeReason.UNKNOWN;
        }
    }
    
    /**
     * Add an item to the grid with a specific configuration
     * 
     * @param id Unique identifier for the item
     * @param content The Vaadin component to display
     * @param config Layout configuration
     */
    public void addItem(String id, Component content, GridItemConfig config) {
        Objects.requireNonNull(id, "ID must not be null");
        Objects.requireNonNull(content, "Content must not be null");
        Objects.requireNonNull(config, "Config must not be null");
        
        if (!id.equals(config.getId())) {
            throw new IllegalArgumentException("ID mismatch: " + id + " != " + config.getId());
        }
        
        // Add to layout
        layout.putItem(config);
        
        // Store component reference
        itemComponents.put(id, content);
        
        // Create wrapper element with slot
        Element wrapper = new Element("div");
        wrapper.setAttribute("slot", "item-" + id);
        wrapper.getClassList().add("dashboard-item-content");
        
        // Attach component to wrapper
        wrapper.appendChild(content.getElement());
        getElement().appendChild(wrapper);
        
        // Sync to client
        syncLayoutToClient();
    }
    
    /**
     * Add an item with automatic position calculation
     */
    public void addItem(String id, Component content, int width, int height) {
        GridItemConfig config = layout.findNextAvailablePosition(id, width, height);
        addItem(id, content, config);
    }
    
    /**
     * Add an item with default size (4x3)
     */
    public void addItem(String id, Component content) {
        addItem(id, content, 4, 3);
    }
    
    /**
     * Remove an item from the grid
     * 
     * @param id The item ID to remove
     * @return The removed component, or null if not found
     */
    public Component removeItem(String id) {
        Objects.requireNonNull(id, "ID must not be null");
        
        // Remove from layout
        layout.removeItem(id);
        
        // Remove component
        Component removed = itemComponents.remove(id);
        
        if (removed != null) {
            // Find and remove the wrapper element
            String slotName = "item-" + id;
            getElement().getChildren()
                .filter(el -> slotName.equals(el.getAttribute("slot")))
                .findFirst()
                .ifPresent(Element::removeFromParent);
            
            // Sync to client
            syncLayoutToClient();
        }
        
        return removed;
    }
    
    /**
     * Replace an item's content while keeping its position
     * 
     * @param id The item ID
     * @param newContent The new component
     * @return The old component, or null if not found
     */
    public Component replaceItem(String id, Component newContent) {
        Objects.requireNonNull(id, "ID must not be null");
        Objects.requireNonNull(newContent, "New content must not be null");
        
        Component oldContent = itemComponents.get(id);
        if (oldContent == null) {
            return null;
        }
        
        // Find the wrapper element
        String slotName = "item-" + id;
        Optional<Element> wrapperOpt = getElement().getChildren()
            .filter(el -> slotName.equals(el.getAttribute("slot")))
            .findFirst();
        
        if (wrapperOpt.isPresent()) {
            Element wrapper = wrapperOpt.get();
            
            // Remove old content
            oldContent.getElement().removeFromParent();
            
            // Add new content
            wrapper.appendChild(newContent.getElement());
            itemComponents.put(id, newContent);
        }
        
        return oldContent;
    }
    
    /**
     * Update an item's configuration (position, size, constraints)
     * 
     * @param id The item ID
     * @param config The new configuration
     */
    public void setItemConfig(String id, GridItemConfig config) {
        Objects.requireNonNull(id, "ID must not be null");
        Objects.requireNonNull(config, "Config must not be null");
        
        if (!id.equals(config.getId())) {
            throw new IllegalArgumentException("ID mismatch");
        }
        
        if (!itemComponents.containsKey(id)) {
            throw new IllegalArgumentException("Item not found: " + id);
        }
        
        layout.putItem(config);
        syncLayoutToClient();
    }
    
    /**
     * Get an item's current configuration
     * 
     * @param id The item ID
     * @return The configuration, or null if not found
     */
    public GridItemConfig getItemConfig(String id) {
        return layout.getItem(id);
    }
    
    /**
     * Get the component for an item
     * 
     * @param id The item ID
     * @return The component, or null if not found
     */
    public Component getItemComponent(String id) {
        return itemComponents.get(id);
    }
    
    /**
     * Get all item IDs
     */
    public Set<String> getItemIds() {
        return new LinkedHashSet<>(itemComponents.keySet());
    }
    
    /**
     * Check if an item exists
     */
    public boolean hasItem(String id) {
        return itemComponents.containsKey(id);
    }
    
    /**
     * Remove all items
     */
    public void clear() {
        new ArrayList<>(itemComponents.keySet()).forEach(this::removeItem);
    }
    
    /**
     * Get the current layout (read-only copy)
     */
    public GridLayout getLayout() {
        return layout.copy();
    }
    
    /**
     * Set the entire layout (replaces all items)
     * WARNING: This removes all existing components
     */
    public void setLayout(GridLayout newLayout) {
        Objects.requireNonNull(newLayout, "Layout must not be null");
        
        // Clear existing items
        clear();
        
        // Copy new layout data
        layout.setColumns(newLayout.getColumns());
        layout.setRowHeight(newLayout.getRowHeight());
        layout.setCompact(newLayout.isCompact());
        layout.setCompactType(newLayout.getCompactType());
        
        newLayout.getItems().forEach(layout::putItem);
        
        // Update element properties
        initializeElement();
        syncLayoutToClient();
    }
    
    /**
     * Restore layout from JSON (useful for persistence)
     * Note: Only updates positions, doesn't create/remove components
     */
    public void restoreLayout(String layoutJson) {
        GridLayout restored = LayoutSerializer.fromJson(layoutJson);
        
        // Update only existing items
        for (GridItemConfig restoredItem : restored.getItems()) {
            if (hasItem(restoredItem.getId())) {
                layout.putItem(restoredItem);
            }
        }
        
        syncLayoutToClient();
    }
    
    /**
     * Get layout as JSON string (for persistence)
     */
    public String getLayoutJson() {
        return LayoutSerializer.toJson(layout);
    }
    
    /**
     * Sync the current layout to the client
     */
    private void syncLayoutToClient() {
        suppressEcho = true;
        
        String itemsJson = LayoutSerializer.itemsToJson(layout);
        getElement().setProperty("layoutData", itemsJson);
        getElement().setProperty("revision", layout.getRevision());
        
        // Reset echo suppression after a short delay
        getElement().executeJs(
            "setTimeout(() => { this._suppressEcho = false; }, 100);"
        );
        
        suppressEcho = false;
    }
    
    /**
     * Set the number of columns
     */
    public void setColumns(int columns) {
        layout.setColumns(columns);
        getElement().setProperty("columns", columns);
        syncLayoutToClient();
    }
    
    /**
     * Get the number of columns
     */
    public int getColumns() {
        return layout.getColumns();
    }
    
    /**
     * Set the row height in pixels
     */
    public void setRowHeight(int rowHeight) {
        layout.setRowHeight(rowHeight);
        getElement().setProperty("rowHeight", rowHeight);
        syncLayoutToClient();
    }
    
    /**
     * Get the row height
     */
    public int getRowHeight() {
        return layout.getRowHeight();
    }
    
    /**
     * Enable/disable automatic compaction
     */
    public void setCompact(boolean compact) {
        layout.setCompact(compact);
        getElement().setProperty("compact", compact);
    }
    
    /**
     * Check if compaction is enabled
     */
    public boolean isCompact() {
        return layout.isCompact();
    }
    
    /**
     * Set compaction type ("vertical", "horizontal", or null)
     */
    public void setCompactType(String compactType) {
        layout.setCompactType(compactType);
        if (compactType != null) {
            getElement().setProperty("compactType", compactType);
        }
    }
    
    /**
     * Get compaction type
     */
    public String getCompactType() {
        return layout.getCompactType();
    }
    
    /**
     * Add a listener for layout change events
     * 
     * @param listener The listener
     * @return A registration for removing the listener
     */
    public Registration addLayoutChangeListener(ComponentEventListener<LayoutChangeEvent> listener) {
        return addListener(LayoutChangeEvent.class, listener);
    }
}
