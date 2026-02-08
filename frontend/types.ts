/**
 * Type definitions for the dashboard grid component
 * Compatible with react-grid-layout types
 */

/**
 * Layout item configuration (matches react-grid-layout's Layout type)
 */
export interface GridItemLayout {
  /** Unique identifier for this item */
  i: string;
  
  /** X position in grid units */
  x: number;
  
  /** Y position in grid units */
  y: number;
  
  /** Width in grid units */
  w: number;
  
  /** Height in grid units */
  h: number;
  
  /** Minimum width in grid units */
  minW?: number;
  
  /** Minimum height in grid units */
  minH?: number;
  
  /** Maximum width in grid units */
  maxW?: number;
  
  /** Maximum height in grid units */
  maxH?: number;
  
  /** If true, item is not draggable or resizable */
  static?: boolean;
  
  /** If false, item cannot be dragged */
  isDraggable?: boolean;
  
  /** If false, item cannot be resized */
  isResizable?: boolean;
  
  /** If true, item is currently being dragged */
  moved?: boolean;
}

/**
 * Complete layout configuration
 */
export interface DashboardLayout {
  /** Layout revision number (for echo suppression) */
  revision: number;
  
  /** Number of columns in the grid */
  columns: number;
  
  /** Row height in pixels */
  rowHeight: number;
  
  /** Enable automatic compaction */
  compact: boolean;
  
  /** Compaction type */
  compactType?: 'vertical' | 'horizontal' | null;
  
  /** Array of item layouts */
  items: GridItemLayout[];
}

/**
 * Reason for layout change
 */
export type ChangeReason = 'DRAG' | 'RESIZE' | 'SERVER_UPDATE' | 'UNKNOWN';

/**
 * Event detail for layout-changed event
 */
export interface LayoutChangedDetail {
  /** Updated items array */
  items: GridItemLayout[];
  
  /** ID of the item that changed (may be null for bulk updates) */
  itemId: string | null;
  
  /** Reason for the change */
  reason: ChangeReason;
  
  /** True if currently dragging */
  isDragging: boolean;
  
  /** True if currently resizing */
  isResizing: boolean;
  
  /** Client-side revision number */
  revision: number;
}

/**
 * Props for the React grid wrapper component
 */
export interface ReactGridWrapperProps {
  /** Layout data */
  layout: GridItemLayout[];
  
  /** Number of columns */
  columns: number;
  
  /** Row height in pixels */
  rowHeight: number;
  
  /** Enable compaction */
  compact: boolean;
  
  /** Compaction type */
  compactType?: 'vertical' | 'horizontal' | null;
  
  /** Callback when layout changes */
  onLayoutChange: (
    layout: GridItemLayout[],
    itemId: string | null,
    reason: ChangeReason,
    isDragging: boolean,
    isResizing: boolean
  ) => void;
  
  /** Container width (provided by WidthProvider) */
  width?: number;
}

/**
 * Props for individual grid items
 */
export interface GridItemProps {
  /** Item ID */
  id: string;
  
  /** Slot name for the content */
  slotName: string;
}

/**
 * Throttle options
 */
export interface ThrottleOptions {
  /** Throttle delay in milliseconds */
  delay: number;
  
  /** Whether to call on the leading edge */
  leading?: boolean;
  
  /** Whether to call on the trailing edge */
  trailing?: boolean;
}

/**
 * Resize observer entry data
 */
export interface ResizeData {
  /** Container width */
  width: number;
  
  /** Container height */
  height: number;
}

export default GridItemLayout;
