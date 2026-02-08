package com.example.dashboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GridLayout model
 */
@DisplayName("GridLayout Tests")
class GridLayoutTest {
    
    @Test
    @DisplayName("Should create layout with default settings")
    void testDefaultConstructor() {
        GridLayout layout = new GridLayout();
        
        assertEquals(12, layout.getColumns());
        assertEquals(30, layout.getRowHeight());
        assertTrue(layout.isCompact());
        assertEquals("vertical", layout.getCompactType());
        assertEquals(0, layout.size());
    }
    
    @Test
    @DisplayName("Should create layout with custom settings")
    void testCustomConstructor() {
        GridLayout layout = new GridLayout(16, 50);
        
        assertEquals(16, layout.getColumns());
        assertEquals(50, layout.getRowHeight());
    }
    
    @Test
    @DisplayName("Should add and retrieve items")
    void testPutAndGetItem() {
        GridLayout layout = new GridLayout();
        GridItemConfig item = new GridItemConfig("test", 0, 0, 4, 3);
        
        layout.putItem(item);
        
        assertTrue(layout.hasItem("test"));
        assertEquals(1, layout.size());
        
        GridItemConfig retrieved = layout.getItem("test");
        assertNotNull(retrieved);
        assertEquals("test", retrieved.getId());
    }
    
    @Test
    @DisplayName("Should remove items")
    void testRemoveItem() {
        GridLayout layout = new GridLayout();
        GridItemConfig item = new GridItemConfig("test", 0, 0, 4, 3);
        
        layout.putItem(item);
        assertTrue(layout.hasItem("test"));
        
        GridItemConfig removed = layout.removeItem("test");
        
        assertNotNull(removed);
        assertEquals("test", removed.getId());
        assertFalse(layout.hasItem("test"));
        assertEquals(0, layout.size());
    }
    
    @Test
    @DisplayName("Should return null when removing non-existent item")
    void testRemoveNonExistent() {
        GridLayout layout = new GridLayout();
        GridItemConfig removed = layout.removeItem("non-existent");
        assertNull(removed);
    }
    
    @Test
    @DisplayName("Should track revision number")
    void testRevisionTracking() {
        GridLayout layout = new GridLayout();
        long initialRevision = layout.getRevision();
        
        layout.putItem(new GridItemConfig("item1", 0, 0, 4, 3));
        assertTrue(layout.getRevision() > initialRevision);
        
        long afterAdd = layout.getRevision();
        layout.removeItem("item1");
        assertTrue(layout.getRevision() > afterAdd);
    }
    
    @Test
    @DisplayName("Should not increment revision when removing non-existent item")
    void testRevisionNoChangeOnFailedRemove() {
        GridLayout layout = new GridLayout();
        long revision = layout.getRevision();
        
        layout.removeItem("non-existent");
        
        assertEquals(revision, layout.getRevision());
    }
    
    @Test
    @DisplayName("Should clear all items")
    void testClear() {
        GridLayout layout = new GridLayout();
        layout.putItem(new GridItemConfig("item1", 0, 0, 4, 3));
        layout.putItem(new GridItemConfig("item2", 4, 0, 4, 3));
        
        assertEquals(2, layout.size());
        
        layout.clear();
        
        assertEquals(0, layout.size());
        assertFalse(layout.hasItem("item1"));
        assertFalse(layout.hasItem("item2"));
    }
    
    @Test
    @DisplayName("Should not increment revision when clearing empty layout")
    void testClearEmptyLayout() {
        GridLayout layout = new GridLayout();
        long revision = layout.getRevision();
        
        layout.clear();
        
        assertEquals(revision, layout.getRevision());
    }
    
    @Test
    @DisplayName("Should get all item IDs")
    void testGetItemIds() {
        GridLayout layout = new GridLayout();
        layout.putItem(new GridItemConfig("item1", 0, 0, 4, 3));
        layout.putItem(new GridItemConfig("item2", 4, 0, 4, 3));
        layout.putItem(new GridItemConfig("item3", 8, 0, 4, 3));
        
        var itemIds = layout.getItemIds();
        
        assertEquals(3, itemIds.size());
        assertTrue(itemIds.contains("item1"));
        assertTrue(itemIds.contains("item2"));
        assertTrue(itemIds.contains("item3"));
    }
    
    @Test
    @DisplayName("Should get all items as list")
    void testGetItems() {
        GridLayout layout = new GridLayout();
        layout.putItem(new GridItemConfig("item1", 0, 0, 4, 3));
        layout.putItem(new GridItemConfig("item2", 4, 0, 4, 3));
        
        var items = layout.getItems();
        
        assertEquals(2, items.size());
    }
    
    @Test
    @DisplayName("Should replace items with new set")
    void testSetItems() {
        GridLayout layout = new GridLayout();
        layout.putItem(new GridItemConfig("item1", 0, 0, 4, 3));
        layout.putItem(new GridItemConfig("item2", 4, 0, 4, 3));
        
        var newItems = java.util.List.of(
            new GridItemConfig("item3", 0, 0, 4, 3),
            new GridItemConfig("item4", 4, 0, 4, 3)
        );
        
        layout.setItems(newItems);
        
        assertEquals(2, layout.size());
        assertFalse(layout.hasItem("item1"));
        assertFalse(layout.hasItem("item2"));
        assertTrue(layout.hasItem("item3"));
        assertTrue(layout.hasItem("item4"));
    }
    
    @Test
    @DisplayName("Should find next available position")
    void testFindNextAvailablePosition() {
        GridLayout layout = new GridLayout();
        layout.putItem(new GridItemConfig("item1", 0, 0, 4, 3));
        
        GridItemConfig nextPos = layout.findNextAvailablePosition("item2", 4, 3);
        
        assertNotNull(nextPos);
        assertEquals("item2", nextPos.getId());
        assertEquals(4, nextPos.getW());
        assertEquals(3, nextPos.getH());
        // Should not overlap with item1 (which is at x:0-3, y:0-2)
        assertTrue(nextPos.getX() >= 4 || nextPos.getY() >= 3);
    }
    
    @Test
    @DisplayName("Should create deep copy")
    void testCopy() {
        GridLayout original = new GridLayout(12, 30);
        original.putItem(new GridItemConfig("item1", 0, 0, 4, 3));
        original.setCompactType("horizontal");
        
        GridLayout copy = original.copy();
        
        assertEquals(original.getColumns(), copy.getColumns());
        assertEquals(original.getRowHeight(), copy.getRowHeight());
        assertEquals(original.getCompactType(), copy.getCompactType());
        assertEquals(original.size(), copy.size());
        assertTrue(copy.hasItem("item1"));
        
        // Verify it's a deep copy by modifying the copy
        copy.putItem(new GridItemConfig("item2", 4, 0, 4, 3));
        
        assertFalse(original.hasItem("item2"));
        assertEquals(1, original.size());
        assertEquals(2, copy.size());
    }
    
    @Test
    @DisplayName("Should update properties and increment revision")
    void testPropertyUpdates() {
        GridLayout layout = new GridLayout();
        long initialRevision = layout.getRevision();
        
        layout.setColumns(16);
        assertTrue(layout.getRevision() > initialRevision);
        
        long afterColumns = layout.getRevision();
        layout.setRowHeight(50);
        assertTrue(layout.getRevision() > afterColumns);
        
        long afterRowHeight = layout.getRevision();
        layout.setCompact(false);
        assertTrue(layout.getRevision() > afterRowHeight);
        
        long afterCompact = layout.getRevision();
        layout.setCompactType("horizontal");
        assertTrue(layout.getRevision() > afterCompact);
    }
    
    @Test
    @DisplayName("Should throw exception for null item config")
    void testNullItemConfig() {
        GridLayout layout = new GridLayout();
        
        assertThrows(IllegalArgumentException.class, () -> {
            layout.putItem(null);
        });
    }
    
    @Test
    @DisplayName("Should throw exception for item with null ID")
    void testNullItemId() {
        GridLayout layout = new GridLayout();
        GridItemConfig item = new GridItemConfig();
        item.setId(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            layout.putItem(item);
        });
    }
}
