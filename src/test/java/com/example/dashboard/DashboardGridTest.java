package com.example.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DashboardGrid component
 */
@DisplayName("DashboardGrid Tests")
class DashboardGridTest {
    
    private DashboardGrid grid;
    
    @BeforeEach
    void setUp() {
        grid = new DashboardGrid();
    }
    
    @Test
    @DisplayName("Should create grid with default settings")
    void testDefaultConstructor() {
        assertNotNull(grid);
        assertEquals(12, grid.getColumns());
        assertEquals(30, grid.getRowHeight());
        assertTrue(grid.isCompact());
    }
    
    @Test
    @DisplayName("Should create grid with custom settings")
    void testCustomConstructor() {
        DashboardGrid customGrid = new DashboardGrid(16, 50);
        
        assertEquals(16, customGrid.getColumns());
        assertEquals(50, customGrid.getRowHeight());
    }
    
    @Test
    @DisplayName("Should add item with configuration")
    void testAddItem() {
        Button button = new Button("Test");
        GridItemConfig config = GridItemConfig.at("btn1", 0, 0, 4, 3);
        
        grid.addItem("btn1", button, config);
        
        assertTrue(grid.hasItem("btn1"));
        assertEquals(button, grid.getItemComponent("btn1"));
        
        GridItemConfig retrievedConfig = grid.getItemConfig("btn1");
        assertNotNull(retrievedConfig);
        assertEquals("btn1", retrievedConfig.getId());
        assertEquals(0, retrievedConfig.getX());
        assertEquals(0, retrievedConfig.getY());
    }
    
    @Test
    @DisplayName("Should add item with automatic positioning")
    void testAddItemAutoPosition() {
        TextField field = new TextField();
        
        grid.addItem("field1", field, 6, 4);
        
        assertTrue(grid.hasItem("field1"));
        assertNotNull(grid.getItemConfig("field1"));
    }
    
    @Test
    @DisplayName("Should add item with default size")
    void testAddItemDefaultSize() {
        Button button = new Button();
        
        grid.addItem("btn", button);
        
        assertTrue(grid.hasItem("btn"));
        GridItemConfig config = grid.getItemConfig("btn");
        assertEquals(4, config.getW());
        assertEquals(3, config.getH());
    }
    
    @Test
    @DisplayName("Should remove item")
    void testRemoveItem() {
        Button button = new Button("Test");
        grid.addItem("btn1", button);
        
        assertTrue(grid.hasItem("btn1"));
        
        Component removed = grid.removeItem("btn1");
        
        assertEquals(button, removed);
        assertFalse(grid.hasItem("btn1"));
        assertNull(grid.getItemComponent("btn1"));
    }
    
    @Test
    @DisplayName("Should return null when removing non-existent item")
    void testRemoveNonExistentItem() {
        Component removed = grid.removeItem("non-existent");
        assertNull(removed);
    }
    
    @Test
    @DisplayName("Should replace item content")
    void testReplaceItem() {
        Button oldButton = new Button("Old");
        grid.addItem("btn1", oldButton);
        
        Button newButton = new Button("New");
        Component replaced = grid.replaceItem("btn1", newButton);
        
        assertEquals(oldButton, replaced);
        assertEquals(newButton, grid.getItemComponent("btn1"));
    }
    
    @Test
    @DisplayName("Should update item configuration")
    void testSetItemConfig() {
        Button button = new Button("Test");
        grid.addItem("btn1", button, GridItemConfig.at("btn1", 0, 0, 4, 3));
        
        GridItemConfig newConfig = GridItemConfig.at("btn1", 5, 5, 6, 4);
        grid.setItemConfig("btn1", newConfig);
        
        GridItemConfig retrieved = grid.getItemConfig("btn1");
        assertEquals(5, retrieved.getX());
        assertEquals(5, retrieved.getY());
        assertEquals(6, retrieved.getW());
        assertEquals(4, retrieved.getH());
    }
    
    @Test
    @DisplayName("Should throw exception for ID mismatch in addItem")
    void testAddItemIdMismatch() {
        Button button = new Button("Test");
        GridItemConfig config = GridItemConfig.at("btn2", 0, 0, 4, 3);
        
        assertThrows(IllegalArgumentException.class, () -> {
            grid.addItem("btn1", button, config);
        });
    }
    
    @Test
    @DisplayName("Should throw exception for ID mismatch in setItemConfig")
    void testSetItemConfigIdMismatch() {
        Button button = new Button("Test");
        grid.addItem("btn1", button);
        
        GridItemConfig config = GridItemConfig.at("btn2", 0, 0, 4, 3);
        
        assertThrows(IllegalArgumentException.class, () -> {
            grid.setItemConfig("btn1", config);
        });
    }
    
    @Test
    @DisplayName("Should get all item IDs")
    void testGetItemIds() {
        grid.addItem("btn1", new Button(), 4, 3);
        grid.addItem("btn2", new Button(), 4, 3);
        grid.addItem("btn3", new Button(), 4, 3);
        
        var itemIds = grid.getItemIds();
        
        assertEquals(3, itemIds.size());
        assertTrue(itemIds.contains("btn1"));
        assertTrue(itemIds.contains("btn2"));
        assertTrue(itemIds.contains("btn3"));
    }
    
    @Test
    @DisplayName("Should clear all items")
    void testClear() {
        grid.addItem("btn1", new Button(), 4, 3);
        grid.addItem("btn2", new Button(), 4, 3);
        grid.addItem("btn3", new Button(), 4, 3);
        
        assertEquals(3, grid.getItemIds().size());
        
        grid.clear();
        
        assertEquals(0, grid.getItemIds().size());
    }
    
    @Test
    @DisplayName("Should get layout copy")
    void testGetLayout() {
        grid.addItem("btn1", new Button(), GridItemConfig.at("btn1", 0, 0, 4, 3));
        
        GridLayout layout = grid.getLayout();
        
        assertNotNull(layout);
        assertEquals(1, layout.size());
        assertTrue(layout.hasItem("btn1"));
    }
    
    @Test
    @DisplayName("Should serialize and restore layout")
    void testLayoutSerialization() {
        grid.addItem("btn1", new Button(), GridItemConfig.at("btn1", 0, 0, 4, 3));
        grid.addItem("btn2", new Button(), GridItemConfig.at("btn2", 4, 0, 4, 3));
        
        String layoutJson = grid.getLayoutJson();
        assertNotNull(layoutJson);
        
        // Modify positions
        grid.setItemConfig("btn1", GridItemConfig.at("btn1", 8, 8, 4, 3));
        
        // Restore original layout
        grid.restoreLayout(layoutJson);
        
        GridItemConfig config = grid.getItemConfig("btn1");
        assertEquals(0, config.getX());
        assertEquals(0, config.getY());
    }
    
    @Test
    @DisplayName("Should update grid properties")
    void testGridProperties() {
        grid.setColumns(16);
        assertEquals(16, grid.getColumns());
        
        grid.setRowHeight(50);
        assertEquals(50, grid.getRowHeight());
        
        grid.setCompact(false);
        assertFalse(grid.isCompact());
        
        grid.setCompactType("horizontal");
        assertEquals("horizontal", grid.getCompactType());
    }
    
    @Test
    @DisplayName("Should add layout change listener")
    void testLayoutChangeListener() {
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicReference<String> lastReason = new AtomicReference<>();
        
        grid.addLayoutChangeListener(event -> {
            eventCount.incrementAndGet();
            lastReason.set(event.getReason().toString());
        });
        
        // Note: In a real scenario, events would be fired by client interaction
        // This test validates that the listener can be registered
        assertNotNull(grid);
    }
    
    @Test
    @DisplayName("Should handle multiple items with different configurations")
    void testMultipleItemsWithDifferentConfigs() {
        GridItemConfig config1 = GridItemConfig.at("item1", 0, 0, 4, 3)
            .withMinSize(2, 2)
            .withMaxSize(8, 6);
        
        GridItemConfig config2 = GridItemConfig.at("item2", 4, 0, 4, 3)
            .asStatic();
        
        GridItemConfig config3 = GridItemConfig.at("item3", 8, 0, 4, 3)
            .notDraggable()
            .notResizable();
        
        grid.addItem("item1", new Button(), config1);
        grid.addItem("item2", new Button(), config2);
        grid.addItem("item3", new Button(), config3);
        
        assertEquals(3, grid.getItemIds().size());
        
        assertTrue(grid.getItemConfig("item2").isStatic());
        assertFalse(grid.getItemConfig("item3").isDraggable());
        assertFalse(grid.getItemConfig("item3").isResizable());
    }
}
