package com.example.dashboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializes and deserializes GridLayout to/from JSON compatible with react-grid-layout.
 * 
 * JSON format:
 * {
 *   "revision": 123,
 *   "columns": 12,
 *   "rowHeight": 30,
 *   "compact": true,
 *   "compactType": "vertical",
 *   "items": [
 *     {
 *       "i": "item-id",
 *       "x": 0,
 *       "y": 0,
 *       "w": 4,
 *       "h": 3,
 *       "minW": 2,
 *       "minH": 2,
 *       "maxW": 8,
 *       "maxH": 6,
 *       "static": false,
 *       "isDraggable": true,
 *       "isResizable": true
 *     }
 *   ]
 * }
 */
public class LayoutSerializer {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    /**
     * Serialize a GridLayout to JSON string
     */
    public static String toJson(GridLayout layout) {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            
            // Layout properties
            root.put("revision", layout.getRevision());
            root.put("columns", layout.getColumns());
            root.put("rowHeight", layout.getRowHeight());
            root.put("compact", layout.isCompact());
            
            if (layout.getCompactType() != null) {
                root.put("compactType", layout.getCompactType());
            }
            
            // Items array
            ArrayNode itemsArray = root.putArray("items");
            for (GridItemConfig item : layout.getItems()) {
                ObjectNode itemNode = itemsArray.addObject();
                
                // Required fields (using 'i' to match react-grid-layout)
                itemNode.put("i", item.getId());
                itemNode.put("x", item.getX());
                itemNode.put("y", item.getY());
                itemNode.put("w", item.getW());
                itemNode.put("h", item.getH());
                
                // Optional fields
                if (item.getMinW() != null) {
                    itemNode.put("minW", item.getMinW());
                }
                if (item.getMinH() != null) {
                    itemNode.put("minH", item.getMinH());
                }
                if (item.getMaxW() != null) {
                    itemNode.put("maxW", item.getMaxW());
                }
                if (item.getMaxH() != null) {
                    itemNode.put("maxH", item.getMaxH());
                }
                
                // Boolean flags
                itemNode.put("static", item.isStatic());
                itemNode.put("isDraggable", item.isDraggable());
                itemNode.put("isResizable", item.isResizable());
            }
            
            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize layout", e);
        }
    }
    
    /**
     * Serialize just the items array (for client-side updates)
     */
    public static String itemsToJson(GridLayout layout) {
        try {
            ArrayNode itemsArray = MAPPER.createArrayNode();
            
            for (GridItemConfig item : layout.getItems()) {
                ObjectNode itemNode = itemsArray.addObject();
                itemNode.put("i", item.getId());
                itemNode.put("x", item.getX());
                itemNode.put("y", item.getY());
                itemNode.put("w", item.getW());
                itemNode.put("h", item.getH());
                
                if (item.getMinW() != null) itemNode.put("minW", item.getMinW());
                if (item.getMinH() != null) itemNode.put("minH", item.getMinH());
                if (item.getMaxW() != null) itemNode.put("maxW", item.getMaxW());
                if (item.getMaxH() != null) itemNode.put("maxH", item.getMaxH());
                
                itemNode.put("static", item.isStatic());
                itemNode.put("isDraggable", item.isDraggable());
                itemNode.put("isResizable", item.isResizable());
            }
            
            return MAPPER.writeValueAsString(itemsArray);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize items", e);
        }
    }
    
    /**
     * Deserialize a GridLayout from JSON string
     */
    public static GridLayout fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new GridLayout();
        }
        
        try {
            JsonNode root = MAPPER.readTree(json);
            GridLayout layout = new GridLayout();
            
            // Layout properties
            if (root.has("columns")) {
                layout.setColumns(root.get("columns").asInt(12));
            }
            if (root.has("rowHeight")) {
                layout.setRowHeight(root.get("rowHeight").asInt(30));
            }
            if (root.has("compact")) {
                layout.setCompact(root.get("compact").asBoolean(true));
            }
            if (root.has("compactType")) {
                layout.setCompactType(root.get("compactType").asText());
            }
            if (root.has("revision")) {
                layout.setRevision(root.get("revision").asLong(0));
            }
            
            // Items
            if (root.has("items") && root.get("items").isArray()) {
                ArrayNode itemsArray = (ArrayNode) root.get("items");
                List<GridItemConfig> items = parseItemsArray(itemsArray);
                items.forEach(layout::putItem);
            }
            
            return layout;
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize layout", e);
        }
    }
    
    /**
     * Update layout items from a JSON items array (from client)
     */
    public static void updateItemsFromJson(GridLayout layout, String itemsJson) {
        if (itemsJson == null || itemsJson.trim().isEmpty()) {
            return;
        }
        
        try {
            JsonNode node = MAPPER.readTree(itemsJson);
            if (!node.isArray()) {
                throw new IllegalArgumentException("Expected JSON array for items");
            }
            
            List<GridItemConfig> items = parseItemsArray((ArrayNode) node);
            
            // Update existing items while preserving ones not in the update
            for (GridItemConfig item : items) {
                if (layout.hasItem(item.getId())) {
                    layout.putItem(item);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update items from JSON", e);
        }
    }
    
    /**
     * Parse an array of item objects
     */
    private static List<GridItemConfig> parseItemsArray(ArrayNode itemsArray) {
        List<GridItemConfig> items = new ArrayList<>();
        
        for (JsonNode itemNode : itemsArray) {
            // Required fields (using 'i' to match react-grid-layout)
            String id = itemNode.get("i").asText();
            int x = itemNode.get("x").asInt(0);
            int y = itemNode.get("y").asInt(0);
            int w = itemNode.get("w").asInt(1);
            int h = itemNode.get("h").asInt(1);
            
            GridItemConfig item = new GridItemConfig(id, x, y, w, h);
            
            // Optional fields
            if (itemNode.has("minW") && !itemNode.get("minW").isNull()) {
                item.setMinW(itemNode.get("minW").asInt());
            }
            if (itemNode.has("minH") && !itemNode.get("minH").isNull()) {
                item.setMinH(itemNode.get("minH").asInt());
            }
            if (itemNode.has("maxW") && !itemNode.get("maxW").isNull()) {
                item.setMaxW(itemNode.get("maxW").asInt());
            }
            if (itemNode.has("maxH") && !itemNode.get("maxH").isNull()) {
                item.setMaxH(itemNode.get("maxH").asInt());
            }
            
            // Boolean flags
            if (itemNode.has("static")) {
                item.setStatic(itemNode.get("static").asBoolean(false));
            }
            if (itemNode.has("isDraggable")) {
                item.setDraggable(itemNode.get("isDraggable").asBoolean(true));
            }
            if (itemNode.has("isResizable")) {
                item.setResizable(itemNode.get("isResizable").asBoolean(true));
            }
            
            items.add(item);
        }
        
        return items;
    }
    
    /**
     * Convert layout to a pretty-printed JSON string (for debugging)
     */
    public static String toPrettyJson(GridLayout layout) {
        try {
            String json = toJson(layout);
            JsonNode node = MAPPER.readTree(json);
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create pretty JSON", e);
        }
    }
}
