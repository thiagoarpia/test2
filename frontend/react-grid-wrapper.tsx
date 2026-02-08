/**
 * React wrapper component for react-grid-layout
 * Handles the grid layout and integrates with slotted Vaadin components
 */

import React, { useCallback, useRef, useState, useEffect } from 'react';
import GridLayout, { WidthProvider, Layout } from 'react-grid-layout';
import type { GridItemLayout, ReactGridWrapperProps, ChangeReason } from './types';

// Import react-grid-layout styles
import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';

// Wrap GridLayout with WidthProvider for responsive width
const ResponsiveGridLayout = WidthProvider(GridLayout);

/**
 * Individual grid item component that renders a slot for Vaadin content
 */
const GridItem: React.FC<{ id: string }> = ({ id }) => {
  return (
    <div className="dashboard-grid-item" data-item-id={id}>
      {/* This slot will hold the Vaadin component */}
      <slot name={`item-${id}`}></slot>
    </div>
  );
};

/**
 * Main React grid wrapper component
 */
const ReactGridWrapper: React.FC<ReactGridWrapperProps> = ({
  layout,
  columns,
  rowHeight,
  compact,
  compactType,
  onLayoutChange,
}) => {
  const [localLayout, setLocalLayout] = useState<GridItemLayout[]>(layout);
  const isDraggingRef = useRef(false);
  const isResizingRef = useRef(false);
  const currentItemRef = useRef<string | null>(null);
  const throttleTimerRef = useRef<NodeJS.Timeout | null>(null);
  
  // Update local layout when props change (from server)
  useEffect(() => {
    setLocalLayout(layout);
  }, [layout]);
  
  /**
   * Throttled callback for intermediate updates
   */
  const throttledUpdate = useCallback(
    (
      newLayout: Layout[],
      itemId: string | null,
      reason: ChangeReason,
      isDragging: boolean,
      isResizing: boolean
    ) => {
      if (throttleTimerRef.current) {
        clearTimeout(throttleTimerRef.current);
      }
      
      throttleTimerRef.current = setTimeout(() => {
        const converted = convertLayout(newLayout);
        onLayoutChange(converted, itemId, reason, isDragging, isResizing);
        throttleTimerRef.current = null;
      }, 150); // 150ms throttle for intermediate updates
    },
    [onLayoutChange]
  );
  
  /**
   * Immediate callback for final updates
   */
  const immediateUpdate = useCallback(
    (
      newLayout: Layout[],
      itemId: string | null,
      reason: ChangeReason
    ) => {
      // Cancel any pending throttled update
      if (throttleTimerRef.current) {
        clearTimeout(throttleTimerRef.current);
        throttleTimerRef.current = null;
      }
      
      const converted = convertLayout(newLayout);
      onLayoutChange(converted, itemId, reason, false, false);
    },
    [onLayoutChange]
  );
  
  /**
   * Handle layout change (called on every drag/resize)
   */
  const handleLayoutChange = useCallback(
    (newLayout: Layout[]) => {
      setLocalLayout(convertLayout(newLayout));
      
      // Send intermediate update if dragging or resizing
      if (isDraggingRef.current || isResizingRef.current) {
        const reason: ChangeReason = isDraggingRef.current ? 'DRAG' : 'RESIZE';
        throttledUpdate(
          newLayout,
          currentItemRef.current,
          reason,
          isDraggingRef.current,
          isResizingRef.current
        );
      }
    },
    [throttledUpdate]
  );
  
  /**
   * Handle drag start
   */
  const handleDragStart = useCallback(
    (layout: Layout[], oldItem: Layout, newItem: Layout) => {
      isDraggingRef.current = true;
      currentItemRef.current = newItem.i;
    },
    []
  );
  
  /**
   * Handle drag stop (final position)
   */
  const handleDragStop = useCallback(
    (layout: Layout[], oldItem: Layout, newItem: Layout) => {
      isDraggingRef.current = false;
      const itemId = newItem.i;
      currentItemRef.current = null;
      
      // Send final update
      immediateUpdate(layout, itemId, 'DRAG');
    },
    [immediateUpdate]
  );
  
  /**
   * Handle resize start
   */
  const handleResizeStart = useCallback(
    (layout: Layout[], oldItem: Layout, newItem: Layout) => {
      isResizingRef.current = true;
      currentItemRef.current = newItem.i;
    },
    []
  );
  
  /**
   * Handle resize stop (final size)
   */
  const handleResizeStop = useCallback(
    (layout: Layout[], oldItem: Layout, newItem: Layout) => {
      isResizingRef.current = false;
      const itemId = newItem.i;
      currentItemRef.current = null;
      
      // Send final update
      immediateUpdate(layout, itemId, 'RESIZE');
    },
    [immediateUpdate]
  );
  
  /**
   * Convert react-grid-layout Layout[] to our GridItemLayout[]
   */
  const convertLayout = (layout: Layout[]): GridItemLayout[] => {
    return layout.map((item) => ({
      i: item.i,
      x: item.x,
      y: item.y,
      w: item.w,
      h: item.h,
      minW: item.minW,
      minH: item.minH,
      maxW: item.maxW,
      maxH: item.maxH,
      static: item.static,
      isDraggable: item.isDraggable,
      isResizable: item.isResizable,
    }));
  };
  
  return (
    <ResponsiveGridLayout
      className="dashboard-grid-layout"
      layout={localLayout as Layout[]}
      cols={columns}
      rowHeight={rowHeight}
      compactType={compactType || 'vertical'}
      preventCollision={!compact}
      onLayoutChange={handleLayoutChange}
      onDragStart={handleDragStart}
      onDragStop={handleDragStop}
      onResizeStart={handleResizeStart}
      onResizeStop={handleResizeStop}
      draggableHandle=".drag-handle"
      margin={[10, 10]}
      containerPadding={[10, 10]}
      useCSSTransforms={true}
      // Accessibility features
      transformScale={1}
      // Performance optimizations
      isBounded={false}
    >
      {localLayout.map((item) => (
        <div
          key={item.i}
          data-grid={{
            x: item.x,
            y: item.y,
            w: item.w,
            h: item.h,
            minW: item.minW,
            minH: item.minH,
            maxW: item.maxW,
            maxH: item.maxH,
            static: item.static,
            isDraggable: item.isDraggable !== false,
            isResizable: item.isResizable !== false,
          }}
          className="grid-item-wrapper"
        >
          {/* Drag handle for accessibility and better control */}
          <div className="drag-handle" role="button" tabIndex={0} aria-label={`Drag ${item.i}`}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
              <path d="M9 3h2v2H9V3zm0 4h2v2H9V7zm0 4h2v2H9v-2zm0 4h2v2H9v-2zm0 4h2v2H9v-2zm4-16h2v2h-2V3zm0 4h2v2h-2V7zm0 4h2v2h-2v-2zm0 4h2v2h-2v-2zm0 4h2v2h-2v-2z"/>
            </svg>
          </div>
          
          {/* Content area with slot for Vaadin component */}
          <div className="grid-item-content">
            <GridItem id={item.i} />
          </div>
        </div>
      ))}
    </ResponsiveGridLayout>
  );
};

export default ReactGridWrapper;
