/**
 * Lit-based custom element that wraps the React grid layout component
 * and provides the bridge to Vaadin Flow
 */

import { LitElement, html, css, PropertyValues } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import React from 'react';
import ReactDOM from 'react-dom/client';
import ReactGridWrapper from './react-grid-wrapper';
import type { GridItemLayout, LayoutChangedDetail, ChangeReason } from './types';

/**
 * Custom element for the dashboard grid
 * Usage: <dashboard-grid></dashboard-grid>
 */
@customElement('dashboard-grid')
export class DashboardGrid extends LitElement {
  // Properties synced with server-side component
  
  @property({ type: Number })
  columns = 12;
  
  @property({ type: Number })
  rowHeight = 30;
  
  @property({ type: Boolean })
  compact = true;
  
  @property({ type: String })
  compactType: 'vertical' | 'horizontal' | null = 'vertical';
  
  @property({ type: String })
  layoutData = '[]';
  
  @property({ type: Number })
  revision = 0;
  
  // Internal state
  
  @state()
  private layout: GridItemLayout[] = [];
  
  @state()
  private clientRevision = 0;
  
  private reactRoot: ReactDOM.Root | null = null;
  private reactContainer: HTMLDivElement | null = null;
  private resizeObserver: ResizeObserver | null = null;
  
  // Suppress echo flag to prevent loops
  _suppressEcho = false;
  
  // Styles for the component
  static styles = css`
    :host {
      display: block;
      width: 100%;
      height: 100%;
      position: relative;
      overflow: auto;
    }
    
    #react-root {
      width: 100%;
      height: 100%;
      min-height: 400px;
    }
    
    /* Grid item styling */
    ::slotted(*) {
      width: 100%;
      height: 100%;
    }
    
    /* Base styles for grid layout */
    :host ::ng-deep .dashboard-grid-layout {
      background: var(--lumo-base-color, #fff);
      position: relative;
    }
    
    :host ::ng-deep .grid-item-wrapper {
      background: var(--lumo-contrast-5pct, #f5f5f5);
      border: 1px solid var(--lumo-contrast-10pct, #e0e0e0);
      border-radius: var(--lumo-border-radius-m, 4px);
      box-shadow: var(--lumo-box-shadow-xs, 0 1px 4px rgba(0, 0, 0, 0.1));
      transition: box-shadow 0.2s;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }
    
    :host ::ng-deep .grid-item-wrapper:hover {
      box-shadow: var(--lumo-box-shadow-s, 0 2px 6px rgba(0, 0, 0, 0.15));
    }
    
    :host ::ng-deep .react-grid-item.react-grid-placeholder {
      background: var(--lumo-primary-color-10pct, rgba(33, 150, 243, 0.1));
      border: 2px dashed var(--lumo-primary-color-50pct, rgba(33, 150, 243, 0.5));
      border-radius: var(--lumo-border-radius-m, 4px);
      opacity: 0.8;
      transition-duration: 100ms;
      z-index: 2;
    }
    
    /* Drag handle */
    :host ::ng-deep .drag-handle {
      cursor: grab;
      padding: 8px;
      background: var(--lumo-contrast-10pct, #e0e0e0);
      border-bottom: 1px solid var(--lumo-contrast-20pct, #ccc);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      color: var(--lumo-secondary-text-color, #666);
    }
    
    :host ::ng-deep .drag-handle:hover {
      background: var(--lumo-contrast-20pct, #ccc);
      color: var(--lumo-body-text-color, #000);
    }
    
    :host ::ng-deep .drag-handle:active {
      cursor: grabbing;
    }
    
    :host ::ng-deep .drag-handle:focus {
      outline: 2px solid var(--lumo-primary-color, #1976d2);
      outline-offset: -2px;
    }
    
    /* Content area */
    :host ::ng-deep .grid-item-content {
      flex: 1;
      overflow: auto;
      padding: 12px;
    }
    
    :host ::ng-deep .dashboard-grid-item {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
    }
    
    /* Resize handles */
    :host ::ng-deep .react-resizable-handle {
      background-color: var(--lumo-primary-color, #1976d2);
      opacity: 0.6;
    }
    
    :host ::ng-deep .react-resizable-handle:hover {
      opacity: 1;
    }
    
    /* Loading state */
    :host([loading]) #react-root {
      opacity: 0.6;
      pointer-events: none;
    }
    
    /* Accessibility: focus indicators */
    :host ::ng-deep .react-grid-item:focus-within {
      outline: 2px solid var(--lumo-primary-color, #1976d2);
      outline-offset: 2px;
      z-index: 100;
    }
  `;
  
  constructor() {
    super();
    this.handleLayoutChange = this.handleLayoutChange.bind(this);
  }
  
  connectedCallback() {
    super.connectedCallback();
    
    // Set up resize observer for responsive behavior
    this.resizeObserver = new ResizeObserver(() => {
      this.forceReactUpdate();
    });
    
    this.resizeObserver.observe(this);
  }
  
  disconnectedCallback() {
    super.disconnectedCallback();
    
    // Clean up React
    if (this.reactRoot) {
      this.reactRoot.unmount();
      this.reactRoot = null;
    }
    
    // Clean up resize observer
    if (this.resizeObserver) {
      this.resizeObserver.disconnect();
      this.resizeObserver = null;
    }
  }
  
  protected firstUpdated(_changedProperties: PropertyValues): void {
    super.firstUpdated(_changedProperties);
    
    // Parse initial layout
    this.parseLayoutData();
    
    // Create React root
    this.reactContainer = this.shadowRoot!.getElementById('react-root') as HTMLDivElement;
    if (this.reactContainer) {
      this.reactRoot = ReactDOM.createRoot(this.reactContainer);
      this.renderReact();
    }
  }
  
  protected updated(changedProperties: PropertyValues): void {
    super.updated(changedProperties);
    
    // Re-parse layout if layoutData changed
    if (changedProperties.has('layoutData')) {
      this.parseLayoutData();
    }
    
    // Re-render React on any property change
    if (
      changedProperties.has('columns') ||
      changedProperties.has('rowHeight') ||
      changedProperties.has('compact') ||
      changedProperties.has('compactType') ||
      changedProperties.has('layout')
    ) {
      this.renderReact();
    }
  }
  
  /**
   * Parse layout data from JSON string
   */
  private parseLayoutData(): void {
    try {
      const parsed = JSON.parse(this.layoutData || '[]');
      this.layout = Array.isArray(parsed) ? parsed : [];
    } catch (error) {
      console.error('Failed to parse layout data:', error);
      this.layout = [];
    }
  }
  
  /**
   * Render the React component
   */
  private renderReact(): void {
    if (!this.reactRoot || !this.reactContainer) {
      return;
    }
    
    const reactElement = React.createElement(ReactGridWrapper, {
      layout: this.layout,
      columns: this.columns,
      rowHeight: this.rowHeight,
      compact: this.compact,
      compactType: this.compactType,
      onLayoutChange: this.handleLayoutChange,
    });
    
    this.reactRoot.render(reactElement);
  }
  
  /**
   * Force React to re-render (used for resize events)
   */
  private forceReactUpdate(): void {
    this.renderReact();
  }
  
  /**
   * Handle layout changes from React component
   */
  private handleLayoutChange(
    newLayout: GridItemLayout[],
    itemId: string | null,
    reason: ChangeReason,
    isDragging: boolean,
    isResizing: boolean
  ): void {
    // Skip if this is an echo from server update
    if (this._suppressEcho) {
      return;
    }
    
    // Update client revision
    this.clientRevision++;
    
    // Update local layout state
    this.layout = newLayout;
    
    // Dispatch custom event to notify Vaadin
    const detail: LayoutChangedDetail = {
      items: newLayout,
      itemId,
      reason,
      isDragging,
      isResizing,
      revision: this.clientRevision,
    };
    
    this.dispatchEvent(
      new CustomEvent('layout-changed', {
        detail,
        bubbles: true,
        composed: true,
      })
    );
  }
  
  /**
   * Render the Lit template
   */
  render() {
    return html`
      <div id="react-root"></div>
      <slot></slot>
    `;
  }
}

declare global {
  interface HTMLElementTagNameMap {
    'dashboard-grid': DashboardGrid;
  }
}
