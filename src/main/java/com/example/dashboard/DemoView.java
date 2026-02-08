package com.example.dashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demo view showing DashboardGrid features:
 * - Dynamic add/remove items
 * - Layout persistence and restore
 * - Various Vaadin components as grid items
 * - Real-time layout updates
 */
@Route("")
public class DemoView extends VerticalLayout {
    
    private final DashboardGrid grid;
    private final AtomicInteger itemCounter = new AtomicInteger(1);
    
    // In-memory layout storage (in production, use a database)
    private static final Map<String, String> layoutStorage = new HashMap<>();
    private static final String LAYOUT_KEY = "dashboard-layout";
    
    public DemoView() {
        setSpacing(true);
        setPadding(true);
        setSizeFull();
        
        // Header
        H1 title = new H1("Vaadin Dashboard Grid Demo");
        title.getStyle().set("margin-top", "0");
        
        Paragraph description = new Paragraph(
            "A production-ready dashboard component using react-grid-layout. " +
            "Drag items by their handles, resize them, and see real-time updates."
        );
        
        // Create the dashboard grid
        grid = new DashboardGrid(12, 30);
        grid.setWidth("100%");
        grid.setHeight("600px");
        
        // Add layout change listener
        grid.addLayoutChangeListener(event -> {
            if (event.isFinal()) {
                // Only log final updates to avoid spam
                Notification notification = Notification.show(
                    String.format("Layout changed: %s on item %s",
                        event.getReason(),
                        event.getAffectedItemId() != null ? event.getAffectedItemId() : "multiple"
                    ),
                    3000,
                    Notification.Position.BOTTOM_END
                );
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                // Auto-save layout
                autoSaveLayout();
            }
        });
        
        // Control panel
        HorizontalLayout controls = createControlPanel();
        
        // Add some initial items
        addInitialItems();
        
        // Try to restore saved layout
        restoreLayout();
        
        // Layout the view
        add(title, description, controls, grid);
        expand(grid);
    }
    
    private HorizontalLayout createControlPanel() {
        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing(true);
        controls.setAlignItems(Alignment.CENTER);
        
        // Add item buttons
        Button addButtonItem = new Button("Add Button", VaadinIcon.PLUS.create());
        addButtonItem.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButtonItem.addClickListener(e -> addButtonItem());
        
        Button addFormItem = new Button("Add Form", VaadinIcon.FORM.create());
        addFormItem.addClickListener(e -> addFormItem());
        
        Button addChartItem = new Button("Add Chart", VaadinIcon.CHART.create());
        addChartItem.addClickListener(e -> addChartPlaceholder());
        
        Button addTextItem = new Button("Add Text", VaadinIcon.TEXT_LABEL.create());
        addTextItem.addClickListener(e -> addTextItem());
        
        // Layout controls
        Button saveLayout = new Button("Save Layout", VaadinIcon.DOWNLOAD.create());
        saveLayout.addClickListener(e -> saveLayout());
        
        Button restoreButton = new Button("Restore Layout", VaadinIcon.UPLOAD.create());
        restoreButton.addClickListener(e -> restoreLayout());
        
        Button clearButton = new Button("Clear All", VaadinIcon.TRASH.create());
        clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        clearButton.addClickListener(e -> clearGrid());
        
        Button exportButton = new Button("Export JSON", VaadinIcon.CODE.create());
        exportButton.addClickListener(e -> exportLayoutJson());
        
        controls.add(
            addButtonItem, addFormItem, addChartItem, addTextItem,
            new Div(), // Spacer
            saveLayout, restoreButton, clearButton, exportButton
        );
        
        return controls;
    }
    
    private void addInitialItems() {
        // Welcome card
        VerticalLayout welcomeCard = new VerticalLayout();
        welcomeCard.setPadding(true);
        H3 welcomeTitle = new H3("Welcome!");
        Paragraph welcomeText = new Paragraph(
            "This is a dashboard grid component. You can drag and resize these items. " +
            "Use the buttons above to add more items."
        );
        welcomeCard.add(welcomeTitle, welcomeText);
        grid.addItem("welcome", welcomeCard, GridItemConfig.at("welcome", 0, 0, 6, 4));
        
        // Statistics card with buttons
        VerticalLayout statsCard = new VerticalLayout();
        statsCard.setPadding(true);
        H3 statsTitle = new H3("Quick Actions");
        Button action1 = new Button("Action 1", e -> 
            Notification.show("Action 1 clicked"));
        Button action2 = new Button("Action 2", e -> 
            Notification.show("Action 2 clicked"));
        action1.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        statsCard.add(statsTitle, action1, action2);
        grid.addItem("stats", statsCard, GridItemConfig.at("stats", 6, 0, 6, 4));
        
        // Text field demo
        VerticalLayout inputCard = new VerticalLayout();
        inputCard.setPadding(true);
        TextField nameField = new TextField("Name");
        nameField.setWidth("100%");
        TextField emailField = new TextField("Email");
        emailField.setWidth("100%");
        inputCard.add(new H3("Input Form"), nameField, emailField);
        grid.addItem("input", inputCard, GridItemConfig.at("input", 0, 4, 4, 5));
    }
    
    private void addButtonItem() {
        String id = "button-" + itemCounter.getAndIncrement();
        
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        
        H3 title = new H3("Button Item");
        Button button = new Button("Click Me!");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(e -> 
            Notification.show("Button " + id + " clicked!"));
        
        Button removeBtn = new Button("Remove", VaadinIcon.CLOSE.create());
        removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeBtn.addClickListener(e -> {
            grid.removeItem(id);
            Notification.show("Item removed");
        });
        
        content.add(title, button, removeBtn);
        
        grid.addItem(id, content, 4, 3);
    }
    
    private void addFormItem() {
        String id = "form-" + itemCounter.getAndIncrement();
        
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(false);
        
        H3 title = new H3("Form Item");
        
        FormLayout form = new FormLayout();
        TextField firstName = new TextField("First Name");
        TextField lastName = new TextField("Last Name");
        TextField email = new TextField("Email");
        NumberField age = new NumberField("Age");
        
        form.add(firstName, lastName, email, age);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("300px", 2)
        );
        
        Button submitBtn = new Button("Submit", e -> 
            Notification.show("Form submitted!"));
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button removeBtn = new Button("Remove", VaadinIcon.CLOSE.create());
        removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeBtn.addClickListener(e -> {
            grid.removeItem(id);
            Notification.show("Item removed");
        });
        
        HorizontalLayout buttons = new HorizontalLayout(submitBtn, removeBtn);
        
        content.add(title, form, buttons);
        
        grid.addItem(id, content, 6, 6);
    }
    
    private void addChartPlaceholder() {
        String id = "chart-" + itemCounter.getAndIncrement();
        
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        
        H3 title = new H3("Chart Placeholder");
        Paragraph info = new Paragraph(
            "In a real application, this would contain a chart component. " +
            "You can integrate any Vaadin component here."
        );
        
        Button removeBtn = new Button("Remove", VaadinIcon.CLOSE.create());
        removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeBtn.addClickListener(e -> {
            grid.removeItem(id);
            Notification.show("Item removed");
        });
        
        content.add(title, info, removeBtn);
        
        grid.addItem(id, content, 6, 4);
    }
    
    private void addTextItem() {
        String id = "text-" + itemCounter.getAndIncrement();
        
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        
        H3 title = new H3("Text Area");
        TextArea textArea = new TextArea();
        textArea.setWidth("100%");
        textArea.setPlaceholder("Enter some text...");
        textArea.setValue("This is a text area component. You can edit this text.");
        
        Button removeBtn = new Button("Remove", VaadinIcon.CLOSE.create());
        removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeBtn.addClickListener(e -> {
            grid.removeItem(id);
            Notification.show("Item removed");
        });
        
        content.add(title, textArea, removeBtn);
        
        grid.addItem(id, content, 4, 5);
    }
    
    private void saveLayout() {
        String layoutJson = grid.getLayoutJson();
        layoutStorage.put(LAYOUT_KEY, layoutJson);
        
        Notification notification = Notification.show(
            "Layout saved successfully!",
            3000,
            Notification.Position.MIDDLE
        );
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private void autoSaveLayout() {
        String layoutJson = grid.getLayoutJson();
        layoutStorage.put(LAYOUT_KEY, layoutJson);
    }
    
    private void restoreLayout() {
        String savedLayout = layoutStorage.get(LAYOUT_KEY);
        if (savedLayout != null) {
            grid.restoreLayout(savedLayout);
            
            Notification notification = Notification.show(
                "Layout restored!",
                3000,
                Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show("No saved layout found", 3000, Notification.Position.MIDDLE);
        }
    }
    
    private void clearGrid() {
        grid.clear();
        Notification.show("All items cleared", 3000, Notification.Position.MIDDLE);
    }
    
    private void exportLayoutJson() {
        String layoutJson = LayoutSerializer.toPrettyJson(grid.getLayout());
        
        // Create a dialog showing the JSON
        com.vaadin.flow.component.dialog.Dialog dialog = 
            new com.vaadin.flow.component.dialog.Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("600px");
        
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSizeFull();
        
        H3 dialogTitle = new H3("Layout JSON");
        
        TextArea jsonArea = new TextArea();
        jsonArea.setValue(layoutJson);
        jsonArea.setWidth("100%");
        jsonArea.setHeight("100%");
        jsonArea.setReadOnly(true);
        
        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        dialogLayout.add(dialogTitle, jsonArea, closeBtn);
        dialogLayout.expand(jsonArea);
        
        dialog.add(dialogLayout);
        dialog.open();
    }
}
