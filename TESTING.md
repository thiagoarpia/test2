# Testing Strategy for Vaadin Dashboard Grid

This document outlines comprehensive testing approaches for the DashboardGrid component in a Quarkus + Vaadin environment.

## Table of Contents

1. [Unit Tests](#unit-tests)
2. [Integration Tests](#integration-tests)
3. [UI/E2E Tests](#uie2e-tests)
4. [Manual Testing Checklist](#manual-testing-checklist)
5. [Performance Testing](#performance-testing)
6. [Accessibility Testing](#accessibility-testing)

---

## Unit Tests

### Java Unit Tests (JUnit 5)

All unit tests are located in `src/test/java/com/example/dashboard/` and can be run with:

```bash
mvn test
```

#### Test Coverage

**LayoutSerializerTest** - Tests JSON serialization/deserialization
- ✅ Serialize layout to JSON
- ✅ Deserialize layout from JSON
- ✅ Preserve item properties during round-trip
- ✅ Serialize items array only
- ✅ Update items from JSON
- ✅ Handle empty layouts
- ✅ Handle null/empty JSON strings
- ✅ Pretty-print JSON
- ✅ Handle optional fields
- ✅ Full round-trip without data loss

**DashboardGridTest** - Tests component API
- ✅ Create with default/custom settings
- ✅ Add items with various configurations
- ✅ Remove items
- ✅ Replace item content
- ✅ Update item configuration
- ✅ Handle ID mismatches (error cases)
- ✅ Get item IDs and components
- ✅ Clear all items
- ✅ Layout serialization/restoration
- ✅ Grid property updates
- ✅ Event listener registration

**GridLayoutTest** - Tests layout model
- ✅ Create with default/custom settings
- ✅ Add/retrieve items
- ✅ Remove items
- ✅ Revision tracking
- ✅ Clear operations
- ✅ Get all item IDs/items
- ✅ Replace items with new set
- ✅ Find next available position
- ✅ Deep copy functionality
- ✅ Property updates and revision increments
- ✅ Error handling (null checks)

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=LayoutSerializerTest

# Run with coverage report
mvn test jacoco:report

# Run tests in debug mode
mvn test -Dmaven.surefire.debug
```

### Writing New Unit Tests

```java
@Test
@DisplayName("Should do something specific")
void testSomething() {
    // Arrange
    DashboardGrid grid = new DashboardGrid();
    Button button = new Button("Test");
    
    // Act
    grid.addItem("btn1", button, 4, 3);
    
    // Assert
    assertTrue(grid.hasItem("btn1"));
    assertEquals(button, grid.getItemComponent("btn1"));
}
```

---

## Integration Tests

### Quarkus Integration Tests

Create integration tests that start the full Quarkus application:

```java
@QuarkusTest
public class DashboardIntegrationTest {
    
    @Test
    public void testDashboardViewLoads() {
        // Test that the view is accessible
        given()
            .when().get("/")
            .then()
            .statusCode(200)
            .body(containsString("dashboard-grid"));
    }
}
```

### Testing with Mock UI

```java
@ExtendWith(MockitoExtension.class)
class DashboardWithContextTest {
    
    @Test
    void testLayoutChangeEvent() {
        // Set up Vaadin UI context
        UI ui = new UI();
        UI.setCurrent(ui);
        
        DashboardGrid grid = new DashboardGrid();
        AtomicInteger eventCount = new AtomicInteger(0);
        
        grid.addLayoutChangeListener(event -> {
            eventCount.incrementAndGet();
            assertTrue(event.isFromClient());
        });
        
        // Simulate client event
        grid.getElement().fireEvent(new DomEvent(
            grid.getElement(),
            "layout-changed",
            createEventData()
        ));
        
        assertEquals(1, eventCount.get());
    }
}
```

---

## UI/E2E Tests

### Vaadin TestBench (Recommended for Vaadin)

**Setup:**

Add TestBench dependency:
```xml
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-testbench</artifactId>
    <scope>test</scope>
</dependency>
```

**Example Test:**

```java
@RunWith(Parameterized.class)
public class DashboardGridIT extends TestBenchTestCase {
    
    @Before
    public void setup() throws Exception {
        setDriver(new ChromeDriver());
        getDriver().get("http://localhost:8080");
    }
    
    @Test
    public void testDragItem() {
        // Find the grid
        DashboardGridElement grid = $(DashboardGridElement.class).first();
        
        // Get initial position
        GridItemElement item = grid.getItemById("btn1");
        Point initialPos = item.getLocation();
        
        // Drag item
        Actions actions = new Actions(getDriver());
        actions.dragAndDropBy(item.getDragHandle(), 200, 100).perform();
        
        // Wait for animation
        waitForAnimationsToFinish();
        
        // Verify position changed
        Point newPos = item.getLocation();
        assertNotEquals(initialPos, newPos);
    }
    
    @Test
    public void testResizeItem() {
        DashboardGridElement grid = $(DashboardGridElement.class).first();
        GridItemElement item = grid.getItemById("btn1");
        
        Dimension initialSize = item.getSize();
        
        // Find resize handle and drag
        WebElement resizeHandle = item.getResizeHandle();
        Actions actions = new Actions(getDriver());
        actions.dragAndDropBy(resizeHandle, 100, 50).perform();
        
        waitForAnimationsToFinish();
        
        Dimension newSize = item.getSize();
        assertTrue(newSize.width > initialSize.width);
        assertTrue(newSize.height > initialSize.height);
    }
    
    @Test
    public void testAddAndRemoveItems() {
        // Click "Add Button" button
        $(ButtonElement.class).caption("Add Button").first().click();
        
        // Wait for item to appear
        waitUntil(driver -> $(DashboardGridElement.class)
            .first()
            .getItemIds()
            .size() > 0);
        
        int itemCount = $(DashboardGridElement.class).first().getItemIds().size();
        
        // Click remove on first item
        $(ButtonElement.class).caption("Remove").first().click();
        
        // Verify item removed
        waitUntil(driver -> $(DashboardGridElement.class)
            .first()
            .getItemIds()
            .size() == itemCount - 1);
    }
    
    @After
    public void tearDown() {
        getDriver().quit();
    }
    
    private void waitForAnimationsToFinish() {
        try {
            Thread.sleep(500); // Wait for CSS transitions
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

### Custom Element Helpers

```java
@Element("dashboard-grid")
public class DashboardGridElement extends TestBenchElement {
    
    public List<String> getItemIds() {
        return (List<String>) executeScript(
            "return Array.from(this.querySelectorAll('[data-item-id]'))" +
            ".map(el => el.getAttribute('data-item-id'));"
        );
    }
    
    public GridItemElement getItemById(String id) {
        return $(GridItemElement.class)
            .attribute("data-item-id", id)
            .first();
    }
}

public class GridItemElement extends TestBenchElement {
    
    public WebElement getDragHandle() {
        return findElement(By.className("drag-handle"));
    }
    
    public WebElement getResizeHandle() {
        return findElement(By.className("react-resizable-handle"));
    }
    
    public Point getPosition() {
        return getLocation();
    }
    
    public Dimension getSize() {
        return getSize();
    }
}
```

### Playwright/Selenium Alternative

For non-Vaadin-specific tests:

```javascript
// tests/dashboard.spec.js
const { test, expect } = require('@playwright/test');

test('drag and drop grid item', async ({ page }) => {
  await page.goto('http://localhost:8080');
  
  // Wait for grid to load
  await page.waitForSelector('dashboard-grid');
  
  // Get item handle
  const handle = await page.locator('.drag-handle').first();
  const boundingBox = await handle.boundingBox();
  
  // Drag
  await handle.hover();
  await page.mouse.down();
  await page.mouse.move(
    boundingBox.x + 200, 
    boundingBox.y + 100
  );
  await page.mouse.up();
  
  // Wait for animation
  await page.waitForTimeout(500);
  
  // Verify position changed (check via API or visual regression)
  const newBox = await handle.boundingBox();
  expect(newBox.x).not.toBe(boundingBox.x);
});
```

---

## Manual Testing Checklist

### Functional Tests

**Basic Operations:**
- [ ] Add item to grid
- [ ] Remove item from grid
- [ ] Replace item content
- [ ] Clear all items
- [ ] Update item configuration

**Drag & Drop:**
- [ ] Drag item to new position
- [ ] Drag multiple items sequentially
- [ ] Drag item to edge of grid
- [ ] Verify grid reflows correctly
- [ ] Check placeholder appearance during drag
- [ ] Verify snap-to-grid behavior

**Resize:**
- [ ] Resize item using handle
- [ ] Resize to minimum size
- [ ] Resize to maximum size
- [ ] Verify constraints are enforced
- [ ] Check aspect ratio (if configured)

**Layout Persistence:**
- [ ] Save layout JSON
- [ ] Restore layout from JSON
- [ ] Export/import layout
- [ ] Verify revision tracking
- [ ] Test with malformed JSON

**Events:**
- [ ] Verify intermediate events during drag
- [ ] Verify final event on drag stop
- [ ] Verify intermediate events during resize
- [ ] Verify final event on resize stop
- [ ] Check event data accuracy
- [ ] Verify no echo loops

### Browser Compatibility

Test on:
- [ ] Chrome (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Edge (latest)
- [ ] Mobile Safari (iOS)
- [ ] Chrome Mobile (Android)

### Responsive Behavior

- [ ] Grid adapts to container width
- [ ] Items reflow on window resize
- [ ] Mobile viewport (< 768px)
- [ ] Tablet viewport (768-1024px)
- [ ] Desktop viewport (> 1024px)

### Performance

- [ ] Load with 10 items
- [ ] Load with 50 items
- [ ] Load with 100 items
- [ ] Load with 200 items
- [ ] Measure drag/resize smoothness
- [ ] Check memory usage over time

### Edge Cases

- [ ] Empty grid
- [ ] Grid with 1 item
- [ ] Grid with items at boundaries
- [ ] Overlapping items (if allowed)
- [ ] Very large items (12 columns wide)
- [ ] Very small items (1x1)
- [ ] Items with complex content (forms, charts)
- [ ] Items with scrollable content

---

## Performance Testing

### Metrics to Track

1. **Initial Load Time**
   - Time to first render
   - Time to interactive

2. **Drag Performance**
   - Frame rate during drag (target: 60fps)
   - Render time per frame

3. **Resize Performance**
   - Frame rate during resize
   - Layout recalculation time

4. **Memory Usage**
   - Initial memory footprint
   - Memory after 100 drag operations
   - Memory leak detection

### Performance Testing Tools

**Browser DevTools:**
```javascript
// In console, measure drag performance
const grid = document.querySelector('dashboard-grid');
const item = grid.querySelector('[data-item-id="item1"]');

console.time('drag');
// Perform drag operation
console.timeEnd('drag');

// Memory profiling
performance.memory.usedJSHeapSize
```

**Lighthouse:**
```bash
lighthouse http://localhost:8080 --view
```

**Custom Performance Test:**
```java
@Test
public void testPerformanceWith100Items() {
    DashboardGrid grid = new DashboardGrid();
    
    long startTime = System.currentTimeMillis();
    
    // Add 100 items
    for (int i = 0; i < 100; i++) {
        grid.addItem("item" + i, new Button("Button " + i));
    }
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // Should complete within reasonable time
    assertTrue(duration < 5000, "Adding 100 items took " + duration + "ms");
}
```

---

## Accessibility Testing

### Automated Tools

**axe-core:**
```javascript
// In browser console
import axe from 'axe-core';

axe.run(document.querySelector('dashboard-grid'))
  .then(results => {
    console.log(results.violations);
  });
```

**WAVE:**
- Install WAVE browser extension
- Navigate to dashboard page
- Run WAVE analysis

### Manual Accessibility Checklist

**Keyboard Navigation:**
- [ ] Tab through all grid items
- [ ] Tab through all controls within items
- [ ] Focus visible on all interactive elements
- [ ] Drag handles are keyboard accessible
- [ ] Can trigger drag with keyboard (optional feature)
- [ ] No keyboard traps

**Screen Reader:**
- [ ] Test with NVDA (Windows)
- [ ] Test with JAWS (Windows)
- [ ] Test with VoiceOver (macOS/iOS)
- [ ] Test with TalkBack (Android)
- [ ] Verify ARIA labels are announced
- [ ] Verify layout structure is logical
- [ ] Verify drag operations are announced

**Visual:**
- [ ] Sufficient color contrast (4.5:1 minimum)
- [ ] Focus indicators visible
- [ ] Content readable at 200% zoom
- [ ] No information conveyed by color alone

**Motor:**
- [ ] Drag handles are large enough (44x44px minimum)
- [ ] Resize handles are easily targetable
- [ ] Touch targets don't overlap
- [ ] Works with voice control

### Accessibility Test Script

```java
@Test
public void testAccessibilityFeatures() {
    DashboardGrid grid = new DashboardGrid();
    Button button = new Button("Test");
    grid.addItem("btn1", button);
    
    // Verify drag handle has proper attributes
    Element element = grid.getElement();
    List<Element> handles = element.getElementsByClassName("drag-handle");
    
    assertFalse(handles.isEmpty(), "Drag handle should exist");
    Element handle = handles.get(0);
    
    assertEquals("button", handle.getAttribute("role"));
    assertNotNull(handle.getAttribute("aria-label"));
    assertEquals("0", handle.getAttribute("tabindex"));
}
```

---

## Continuous Integration

### GitHub Actions Example

```yaml
name: Dashboard Grid Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Run unit tests
        run: mvn test
        
      - name: Start application
        run: mvn quarkus:dev &
        
      - name: Wait for application
        run: |
          while ! curl -s http://localhost:8080 > /dev/null; do
            sleep 1
          done
          
      - name: Run E2E tests
        run: mvn verify -Pe2e
        
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: target/surefire-reports/
```

---

## Troubleshooting Failed Tests

### Common Issues

**1. Timing Issues:**
```java
// Instead of Thread.sleep()
waitUntil(driver -> {
    return $(DashboardGridElement.class)
        .first()
        .getItemIds()
        .contains("item1");
}, 5);
```

**2. Stale Element References:**
```java
// Re-query element if needed
GridItemElement item = () -> $(GridItemElement.class)
    .attribute("data-item-id", "item1")
    .first();
```

**3. Event Timing:**
```java
// Wait for event propagation
CountDownLatch latch = new CountDownLatch(1);
grid.addLayoutChangeListener(event -> latch.countDown());
assertTrue(latch.await(5, TimeUnit.SECONDS));
```

---

## Test Reporting

### Generate Coverage Report

```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### Generate Test Report

```bash
mvn surefire-report:report
open target/site/surefire-report.html
```

---

## Best Practices

1. **Isolate Tests** - Each test should be independent
2. **Use Descriptive Names** - `testAddItemWithAutoPosition()` not `test1()`
3. **Test One Thing** - Each test should verify one behavior
4. **Use Assertions** - Make expectations explicit
5. **Clean Up** - Reset state in `@AfterEach`
6. **Mock External Dependencies** - Don't rely on databases in unit tests
7. **Fast Tests** - Unit tests should run in milliseconds
8. **Deterministic** - Tests should always produce same result

---

## Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Vaadin TestBench Documentation](https://vaadin.com/docs/latest/testing/end-to-end)
- [Quarkus Testing Guide](https://quarkus.io/guides/getting-started-testing)
- [Web Accessibility Testing](https://www.w3.org/WAI/test-evaluate/)
- [Playwright Documentation](https://playwright.dev/)
