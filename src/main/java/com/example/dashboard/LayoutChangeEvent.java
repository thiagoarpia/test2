package com.example.dashboard;

import com.vaadin.flow.component.ComponentEvent;

import java.util.Objects;

/**
 * Event fired when the grid layout changes due to user interaction.
 * Contains the updated layout and metadata about the change.
 */
public class LayoutChangeEvent extends ComponentEvent<DashboardGrid> {
    
    /**
     * Reason for the layout change
     */
    public enum ChangeReason {
        /**
         * Item was dragged to a new position
         */
        DRAG,
        
        /**
         * Item was resized
         */
        RESIZE,
        
        /**
         * Programmatic update from server
         */
        SERVER_UPDATE,
        
        /**
         * Unknown or multiple reasons
         */
        UNKNOWN
    }
    
    private final GridLayout layout;
    private final String affectedItemId;
    private final ChangeReason reason;
    private final boolean isDragging;
    private final boolean isResizing;
    private final long clientRevision;
    
    /**
     * Create a layout change event
     * 
     * @param source The dashboard grid component
     * @param fromClient Whether this event originated from the client
     * @param layout The updated layout
     * @param affectedItemId The ID of the item that changed (may be null for bulk updates)
     * @param reason The reason for the change
     * @param isDragging Whether a drag operation is in progress
     * @param isResizing Whether a resize operation is in progress
     * @param clientRevision The client-side revision number
     */
    public LayoutChangeEvent(
            DashboardGrid source,
            boolean fromClient,
            GridLayout layout,
            String affectedItemId,
            ChangeReason reason,
            boolean isDragging,
            boolean isResizing,
            long clientRevision) {
        super(source, fromClient);
        this.layout = Objects.requireNonNull(layout, "Layout must not be null");
        this.affectedItemId = affectedItemId;
        this.reason = reason != null ? reason : ChangeReason.UNKNOWN;
        this.isDragging = isDragging;
        this.isResizing = isResizing;
        this.clientRevision = clientRevision;
    }
    
    /**
     * Get the updated layout
     */
    public GridLayout getLayout() {
        return layout;
    }
    
    /**
     * Get the ID of the item that changed (may be null for bulk updates)
     */
    public String getAffectedItemId() {
        return affectedItemId;
    }
    
    /**
     * Get the reason for the change
     */
    public ChangeReason getReason() {
        return reason;
    }
    
    /**
     * Check if a drag operation is currently in progress (intermediate update)
     */
    public boolean isDragging() {
        return isDragging;
    }
    
    /**
     * Check if a resize operation is currently in progress (intermediate update)
     */
    public boolean isResizing() {
        return isResizing;
    }
    
    /**
     * Check if this is an intermediate update (not final)
     */
    public boolean isIntermediate() {
        return isDragging || isResizing;
    }
    
    /**
     * Check if this is a final update (drag/resize stopped)
     */
    public boolean isFinal() {
        return !isIntermediate();
    }
    
    /**
     * Get the client-side revision number (for echo suppression)
     */
    public long getClientRevision() {
        return clientRevision;
    }
    
    @Override
    public String toString() {
        return "LayoutChangeEvent{" +
                "reason=" + reason +
                ", affectedItemId='" + affectedItemId + '\'' +
                ", isDragging=" + isDragging +
                ", isResizing=" + isResizing +
                ", fromClient=" + isFromClient() +
                ", clientRevision=" + clientRevision +
                '}';
    }
}
