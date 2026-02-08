# Vaadin Dashboard Grid Component

A production-ready Vaadin Flow 24 component that integrates [react-grid-layout](https://github.com/react-grid-layout/react-grid-layout) to provide a draggable, resizable dashboard grid for Quarkus applications.

## Features

✅ **Vaadin Component Integration** - Embed any Vaadin Flow components (Button, TextField, FormLayout, etc.) as grid items using slots  
✅ **Drag & Drop** - Intuitive drag handles with visual feedback  
✅ **Resizable Items** - Per-item resize constraints (min/max dimensions)  
✅ **Layout Persistence** - Save/restore layouts as JSON  
✅ **Two-Way Sync** - Server ↔ Client communication with echo suppression  
✅ **Real-Time Events** - Throttled intermediate updates + final updates on drag/resize stop  
✅ **Responsive** - Auto-adjusts to container width  
✅ **Accessibility** - Keyboard navigation, ARIA labels, focus management  
✅ **Performance** - Stable React keys, minimal DOM churn, throttled updates  
✅ **Type-Safe** - Full TypeScript definitions  

## Architecture

```
┌─────────────────────────────────────────┐
│   Vaadin Flow Application (Java)       │
│   ┌─────────────────────────┐          │
│   │  DashboardGrid.java     │          │
│   │  (Server Component)     │          │
│   └──────────┬──────────────┘          │
└──────────────┼──────────────────────────┘
               │ Properties & Events
               ↓
┌──────────────────────────────────────────┐
│   Custom Element (Lit)                   │
│   ┌──────────────────────────┐          │
│   │  dashboard-grid.ts       │          │
│   │  (Web Component Bridge)  │          │
│   └──────────┬───────────────┘          │
└──────────────┼──────────────────────────┘
               │ React Props
               ↓
┌──────────────────────────────────────────┐
│   React Component                        │
│   ┌──────────────────────────┐          │
│   │  react-grid-wrapper.tsx  │          │
│   │  (react-grid-layout)     │          │
│   └──────────────────────────┘          │
└──────────────────────────────────────────┘
               │ Slots
               ↓
      ┌────────────────┐
      │ Vaadin Content │
      │   (in slots)   │
      └────────────────┘
```

## Quick Start

### 1. Add to your Quarkus + Vaadin project

Ensure your `pom.xml` includes the Vaadin and Quarkus dependencies (see [pom.xml](pom.xml) for full configuration).

### 2. Use in your Views

```java
@Route("dashboard")
public class MyDashboardView extends VerticalLayout {
    
    public MyDashboardView() {
        // Create the grid
        DashboardGrid grid = new DashboardGrid(12, 30); // 12 columns, 30px rows
        grid.setWidth("100%");
        grid.setHeight("600px");
        
        // Add items with Vaadin components
        Button button = new Button("Click me!");
        button.addClickListener(e -> Notification.show("Clicked!"));
        grid.addItem("btn1", button, GridItemConfig.at("btn1", 0, 0, 4, 3));
        
        TextField textField = new TextField("Name");
        grid.addItem("field1", textField, GridItemConfig.at("field1", 4, 0, 4, 3));
        
        // Listen for layout changes
        grid.addLayoutChangeListener(event -> {
            if (event.isFinal()) {
                // Save layout when user finishes dragging/resizing
                saveLayoutToDatabase(event.getLayout());
            }
        });
        
        add(grid);
    }
}
```

### 3. Layout Configuration API

```java
// Create item config with builder pattern
GridItemConfig config = GridItemConfig.at("item1", 0, 0, 6, 4)
    .withMinSize(4, 3)           // Min 4x3
    .withMaxSize(12, 8)          // Max 12x8
    .asStatic();                 // Can't be moved/resized

// Or use mutable setters
GridItemConfig config2 = new GridItemConfig("item2", 0, 0, 4, 3);
config2.setMinW(2);
config2.setDraggable(false);    // Can resize but not drag

// Add with automatic positioning
grid.addItem("item3", myComponent, 6, 4); // Auto-finds position for 6x4 item
```

### 4. Persistence & Restore

```java
// Save layout as JSON
String layoutJson = grid.getLayoutJson();
saveToDatabase(layoutJson);

// Restore layout (updates positions of existing items)
String savedLayout = loadFromDatabase();
grid.restoreLayout(savedLayout);

// Or work with GridLayout objects directly
GridLayout layout = grid.getLayout();
layout.getItems().forEach(item -> {
    System.out.println(item.getId() + " at (" + item.getX() + "," + item.getY() + ")");
});
```

## Component API Reference

### DashboardGrid (Java)

**Constructors:**
```java
DashboardGrid()                          // Default: 12 columns, 30px rows
DashboardGrid(int columns, int rowHeight)
DashboardGrid(GridLayout layout)
```

**Item Management:**
```java
void addItem(String id, Component content, GridItemConfig config)
void addItem(String id, Component content, int width, int height)
void addItem(String id, Component content)  // Default 4x3
Component removeItem(String id)
Component replaceItem(String id, Component newContent)
void clear()
```

**Configuration:**
```java
void setItemConfig(String id, GridItemConfig config)
GridItemConfig getItemConfig(String id)
Component getItemComponent(String id)
Set<String> getItemIds()
boolean hasItem(String id)
```

**Layout:**
```java
GridLayout getLayout()              // Get copy
void setLayout(GridLayout layout)   // Replace all items
String getLayoutJson()              // Serialize
void restoreLayout(String json)     // Restore positions
```

**Grid Properties:**
```java
void setColumns(int columns)
void setRowHeight(int rowHeight)
void setCompact(boolean compact)
void setCompactType(String type)    // "vertical", "horizontal", or null
```

**Events:**
```java
Registration addLayoutChangeListener(ComponentEventListener<LayoutChangeEvent> listener)
```

### LayoutChangeEvent

```java
GridLayout getLayout()              // Updated layout
String getAffectedItemId()          // May be null for bulk updates
ChangeReason getReason()            // DRAG, RESIZE, SERVER_UPDATE, UNKNOWN
boolean isDragging()                // True during drag
boolean isResizing()                // True during resize
boolean isIntermediate()            // True if drag/resize in progress
boolean isFinal()                   // True when operation completes
long getClientRevision()            // For echo suppression
```

## Frontend Structure

```
frontend/
├── dashboard-grid.ts          # Lit custom element
├── react-grid-wrapper.tsx     # React integration
└── types.ts                   # TypeScript definitions
```

The component automatically manages npm dependencies via Vaadin's frontend build pipeline:
- `react` & `react-dom` (18.2.0)
- `react-grid-layout` (1.4.4)
- `lit` (3.1.0)

## Styling & Customization

The component uses CSS custom properties (Lumo theme tokens) for styling:

```css
/* In your styles */
dashboard-grid::part(grid-item) {
    --item-background: var(--lumo-base-color);
    --item-border: 1px solid var(--lumo-contrast-10pct);
}

/* Override drag handle */
dashboard-grid .drag-handle {
    background: var(--lumo-primary-color);
}

/* Placeholder appearance during drag */
dashboard-grid .react-grid-placeholder {
    background: rgba(33, 150, 243, 0.1);
}
```

## Performance Characteristics

### Tested Capacity
- **50-100 items**: Excellent performance
- **100-200 items**: Good performance with throttling
- **200+ items**: Consider virtual scrolling or pagination

### Optimization Techniques
1. **Stable React keys** - Items use ID as key, preventing remounting
2. **Throttled updates** - Intermediate drag/resize events throttled to 150ms
3. **Echo suppression** - Revision numbers prevent server→client→server loops
4. **Minimal DOM churn** - React only updates changed properties
5. **CSS transforms** - Hardware-accelerated positioning

## Accessibility

### Keyboard Navigation
- **Tab**: Move focus between grid items
- **Drag handle**: Focusable with `role="button"` and `tabindex="0"`
- **Arrow keys**: (Optional) Implement via custom key handlers

### Screen Reader Support
- ARIA labels on drag handles
- Semantic HTML structure
- Focus management during drag/resize

### Accessibility Enhancements
```java
// Add keyboard move/resize handlers (example)
grid.getElement().addEventListener("keydown", e -> {
    String key = e.getString("event.key");
    String itemId = getCurrentFocusedItem();
    GridItemConfig config = grid.getItemConfig(itemId);
    
    if (key.equals("ArrowRight") && e.getBoolean("event.ctrlKey")) {
        config.setX(config.getX() + 1);
        grid.setItemConfig(itemId, config);
    }
    // ... more key handlers
}).addEventData("event.key").addEventData("event.ctrlKey");
```

## Known Limitations & Edge Cases

### 1. Touch Devices
- ✅ Touch drag/resize works
- ⚠️ Precision may be lower than mouse
- 📝 Consider larger hit areas on mobile

### 2. Nested Scrolling
- ⚠️ Grid items with `overflow: auto` may conflict with drag
- 💡 Use drag handles to avoid conflict
- 📝 Test scrollable content inside items

### 3. Server Push
- ✅ Compatible with Vaadin Push
- ⚠️ High-frequency updates may cause jitter
- 💡 Use `event.isFinal()` to filter events

### 4. Shadow DOM
- ✅ Slots work correctly with Shadow DOM
- ⚠️ Global styles won't penetrate
- 💡 Use CSS custom properties for theming

### 5. Browser Compatibility
- ✅ Modern browsers (Chrome, Firefox, Safari, Edge)
- ⚠️ Requires ES2018+ support
- 📝 IE11 not supported

### 6. Layout Serialization
- ✅ JSON format compatible with react-grid-layout
- ⚠️ Only positions stored, not component state
- 💡 Store component configs separately if needed

## Testing Strategy

See [TESTING.md](TESTING.md) for comprehensive testing guidelines.

### Unit Tests (Included)
```bash
mvn test
```

Tests cover:
- ✅ Layout serialization/deserialization
- ✅ Grid item configuration
- ✅ Layout model operations
- ✅ Event handling
- ✅ Edge cases & error conditions

### Integration Testing Approach
```java
// Example Vaadin TestBench test
@Test
public void testDragAndDrop() {
    DashboardGridElement grid = $(DashboardGridElement.class).first();
    GridItemElement item = grid.getItem("btn1");
    
    // Drag item
    item.dragAndDrop(200, 100);
    
    // Verify position changed
    assertNotEquals(initialPosition, item.getPosition());
}
```

## Build & Deploy

### Development Mode
```bash
mvn quarkus:dev
```

Visit: http://localhost:8080

### Production Build
```bash
mvn clean package -Pproduction
```

This creates an optimized build with:
- Minified frontend bundle
- Compressed assets
- Production Vaadin configuration

### Docker Deployment
```dockerfile
FROM registry.access.redhat.com/ubi8/openjdk-17:1.15

COPY target/quarkus-app/ /deployments/

EXPOSE 8080
CMD ["java", "-jar", "/deployments/quarkus-run.jar"]
```

## Migration Guide

### From Plain HTML/JS Grid
```java
// Before: Manual HTML
Div gridContainer = new Div();
gridContainer.getElement().executeJs("initGrid()");

// After: Type-safe component
DashboardGrid grid = new DashboardGrid();
grid.addItem("item1", new Button("Click"), 4, 3);
```

### From Vaadin Board
```java
// Before: Board
Board board = new Board();
board.add(component1, component2);

// After: DashboardGrid (drag/resize support)
DashboardGrid grid = new DashboardGrid();
grid.addItem("c1", component1);
grid.addItem("c2", component2);
```

## Troubleshooting

### Items not appearing
- ✅ Check that item IDs are unique
- ✅ Verify slot names match (`item-{id}`)
- ✅ Ensure grid has explicit height

### Layout not saving
- ✅ Listen for `event.isFinal()` not intermediate events
- ✅ Check echo suppression isn't blocking updates
- ✅ Verify JSON serialization works

### Performance issues
- ✅ Reduce item count (consider pagination)
- ✅ Increase throttle delay (edit `react-grid-wrapper.tsx`)
- ✅ Disable animations for large grids

### Drag/resize not working
- ✅ Check that items aren't marked as `static`
- ✅ Verify drag handles are present
- ✅ Test with browser DevTools (check for JS errors)

## Contributing

This is a self-contained example project. To customize:

1. **Modify layout engine**: Edit [react-grid-wrapper.tsx](frontend/react-grid-wrapper.tsx)
2. **Change styling**: Update CSS in [dashboard-grid.ts](frontend/dashboard-grid.ts)
3. **Add features**: Extend [DashboardGrid.java](src/main/java/com/example/dashboard/DashboardGrid.java)

## License

This example code is provided as-is for educational purposes. 

Dependencies:
- Vaadin Flow: Apache License 2.0
- Quarkus: Apache License 2.0
- React: MIT License
- react-grid-layout: MIT License
- Lit: BSD-3-Clause License

## Resources

- [Vaadin Documentation](https://vaadin.com/docs)
- [Quarkus + Vaadin Guide](https://github.com/vaadin/quarkus)
- [react-grid-layout](https://github.com/react-grid-layout/react-grid-layout)
- [Lit Documentation](https://lit.dev/)

## Support

For issues or questions:
1. Check [TESTING.md](TESTING.md) for testing guidelines
2. Review [Known Limitations](#known-limitations--edge-cases)
3. Consult Vaadin community forums
4. Open an issue with minimal reproduction case