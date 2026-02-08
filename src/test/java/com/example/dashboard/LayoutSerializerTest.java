package com.example.dashboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LayoutSerializer
 */
@DisplayName("LayoutSerializer Tests")
class LayoutSerializerTest {
    
    private GridLayout layout;
    
    @BeforeEach
    void setUp() {
        layout = new GridLayout(12, 30);
        
        // Add some test items
        GridItemConfig item1 = new GridItemConfig("item1", 0, 0, 4, 3);
        item1.setMinW(2);
        item1.setMinH(2);
        
        GridItemConfig item2 = new GridItemConfig("item2", 4, 0, 4, 3);
        item2.setStatic(true);
        
        GridItemConfig item3 = new GridItemConfig("item3", 8, 0, 4, 3);
        item3.setDraggable(false);
        item3.setResizable(false);
        
        layout.putItem(item1);
        layout.putItem(item2);
        layout.putItem(item3);
    }
    
    @Test
    @DisplayName("Should serialize layout to JSON")
    void testToJson() {
        String json = LayoutSerializer.toJson(layout);
        
        assertNotNull(json);
        assertTrue(json.contains("\"items\""));
        assertTrue(json.contains("\"columns\":12"));
        assertTrue(json.contains("\"rowHeight\":30"));
        assertTrue(json.contains("\"item1\""));
        assertTrue(json.contains("\"item2\""));
        assertTrue(json.contains("\"item3\""));
    }
    
    @Test
    @DisplayName("Should deserialize layout from JSON")
    void testFromJson() {
        String json = LayoutSerializer.toJson(layout);
        GridLayout deserialized = LayoutSerializer.fromJson(json);
        
        assertNotNull(deserialized);
        assertEquals(12, deserialized.getColumns());
        assertEquals(30, deserialized.getRowHeight());
        assertEquals(3, deserialized.size());
        
        assertTrue(deserialized.hasItem("item1"));
        assertTrue(deserialized.hasItem("item2"));
        assertTrue(deserialized.hasItem("item3"));
    }
    
    @Test
    @DisplayName("Should preserve item properties during serialization")
    void testPreserveItemProperties() {
        String json = LayoutSerializer.toJson(layout);
        GridLayout deserialized = LayoutSerializer.fromJson(json);
        
        GridItemConfig item1 = deserialized.getItem("item1");
        assertNotNull(item1);
        assertEquals(0, item1.getX());
        assertEquals(0, item1.getY());
        assertEquals(4, item1.getW());
        assertEquals(3, item1.getH());
        assertEquals(2, item1.getMinW());
        assertEquals(2, item1.getMinH());
        assertFalse(item1.isStatic());
        
        GridItemConfig item2 = deserialized.getItem("item2");
        assertNotNull(item2);
        assertTrue(item2.isStatic());
        
        GridItemConfig item3 = deserialized.getItem("item3");
        assertNotNull(item3);
        assertFalse(item3.isDraggable());
        assertFalse(item3.isResizable());
    }
    
    @Test
    @DisplayName("Should serialize just items array")
    void testItemsToJson() {
        String json = LayoutSerializer.itemsToJson(layout);
        
        assertNotNull(json);
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
        assertTrue(json.contains("\"i\":\"item1\""));
    }
    
    @Test
    @DisplayName("Should update items from JSON array")
    void testUpdateItemsFromJson() {
        // Create a modified items JSON
        String itemsJson = "[{\"i\":\"item1\",\"x\":5,\"y\":5,\"w\":6,\"h\":4}]";
        
        LayoutSerializer.updateItemsFromJson(layout, itemsJson);
        
        GridItemConfig updated = layout.getItem("item1");
        assertNotNull(updated);
        assertEquals(5, updated.getX());
        assertEquals(5, updated.getY());
        assertEquals(6, updated.getW());
        assertEquals(4, updated.getH());
        
        // Other items should still exist
        assertTrue(layout.hasItem("item2"));
        assertTrue(layout.hasItem("item3"));
    }
    
    @Test
    @DisplayName("Should handle empty layout")
    void testEmptyLayout() {
        GridLayout emptyLayout = new GridLayout();
        String json = LayoutSerializer.toJson(emptyLayout);
        
        assertNotNull(json);
        assertTrue(json.contains("\"items\":[]"));
        
        GridLayout deserialized = LayoutSerializer.fromJson(json);
        assertEquals(0, deserialized.size());
    }
    
    @Test
    @DisplayName("Should handle null or empty JSON string")
    void testNullOrEmptyJson() {
        GridLayout fromNull = LayoutSerializer.fromJson(null);
        assertNotNull(fromNull);
        assertEquals(0, fromNull.size());
        
        GridLayout fromEmpty = LayoutSerializer.fromJson("");
        assertNotNull(fromEmpty);
        assertEquals(0, fromEmpty.size());
    }
    
    @Test
    @DisplayName("Should create pretty-printed JSON")
    void testPrettyJson() {
        String prettyJson = LayoutSerializer.toPrettyJson(layout);
        
        assertNotNull(prettyJson);
        assertTrue(prettyJson.contains("\n")); // Should have newlines
        assertTrue(prettyJson.contains("  ")); // Should have indentation
    }
    
    @Test
    @DisplayName("Should handle optional fields correctly")
    void testOptionalFields() {
        GridLayout testLayout = new GridLayout();
        GridItemConfig item = new GridItemConfig("test", 0, 0, 4, 3);
        // Don't set optional fields
        testLayout.putItem(item);
        
        String json = LayoutSerializer.toJson(testLayout);
        GridLayout deserialized = LayoutSerializer.fromJson(json);
        
        GridItemConfig deserializedItem = deserialized.getItem("test");
        assertNotNull(deserializedItem);
        assertNull(deserializedItem.getMinW());
        assertNull(deserializedItem.getMinH());
        assertNull(deserializedItem.getMaxW());
        assertNull(deserializedItem.getMaxH());
    }
    
    @Test
    @DisplayName("Should roundtrip serialization without data loss")
    void testRoundTrip() {
        String json1 = LayoutSerializer.toJson(layout);
        GridLayout deserialized1 = LayoutSerializer.fromJson(json1);
        String json2 = LayoutSerializer.toJson(deserialized1);
        GridLayout deserialized2 = LayoutSerializer.fromJson(json2);
        
        assertEquals(layout.size(), deserialized2.size());
        assertEquals(layout.getColumns(), deserialized2.getColumns());
        assertEquals(layout.getRowHeight(), deserialized2.getRowHeight());
        
        for (String itemId : layout.getItemIds()) {
            GridItemConfig original = layout.getItem(itemId);
            GridItemConfig roundtripped = deserialized2.getItem(itemId);
            
            assertNotNull(roundtripped);
            assertEquals(original.getX(), roundtripped.getX());
            assertEquals(original.getY(), roundtripped.getY());
            assertEquals(original.getW(), roundtripped.getW());
            assertEquals(original.getH(), roundtripped.getH());
        }
    }
}
