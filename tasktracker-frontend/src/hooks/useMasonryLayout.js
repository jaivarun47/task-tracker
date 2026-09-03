import { useState, useRef, useEffect, useCallback, useMemo } from 'react';

/**
 * Custom hook for responsive, packed masonry layout.
 *
 * Requirements:
 * - Dynamic column count calculated from actual available container width.
 * - Dynamic column width that distributes available width across columns (no dead right-side space).
 * - Real-time reflow on container resize and sidebar toggle.
 * - Deterministic packing preserving logical ordering.
 *
 * @param {Array} items - The list items in logical order
 * @param {Object} options - Config options (targetColumnWidth, minColumnWidth, gap, addSlotHeight, hasAddSlot)
 * @returns {Object} { containerRef, positions, addSlotPos, totalHeight, columnWidth, columnsCount, registerItemRef, containerWidth }
 */
export function useMasonryLayout(items = [], options = {}) {
  const {
    targetColumnWidth = 300,
    minColumnWidth = 260,
    gap = 20,
    addSlotHeight = 140,
    hasAddSlot = true,
    collapsedListIds = null,
  } = options;

  const [containerNode, setContainerNode] = useState(null);
  const [containerWidth, setContainerWidth] = useState(0);
  const [measuredHeights, setMeasuredHeights] = useState({});
  const itemElementsRef = useRef(new Map());
  const itemResizeObserverRef = useRef(null);

  // Callback ref for container element to guarantee observation even across conditional renders
  const containerRef = useCallback((node) => {
    if (node) {
      setContainerNode(node);
      const rect = node.getBoundingClientRect();
      if (rect.width > 0) {
        setContainerWidth(Math.floor(rect.width));
      }
    } else {
      setContainerNode(null);
    }
  }, []);

  // ── Measure Container Width via ResizeObserver ───────────────────────────
  useEffect(() => {
    if (!containerNode) return;

    const observer = new ResizeObserver((entries) => {
      for (const entry of entries) {
        const width = Math.floor(entry.contentRect.width);
        if (width > 0) {
          setContainerWidth((prev) => (prev !== width ? width : prev));
        }
      }
    });

    observer.observe(containerNode);
    return () => observer.disconnect();
  }, [containerNode]);

  // ── Measure Item Heights via ResizeObserver ──────────────────────────────
  useEffect(() => {
    const observer = new ResizeObserver((entries) => {
      let changed = false;
      const updates = {};

      for (const entry of entries) {
        const target = entry.target;
        const itemId = target.getAttribute('data-masonry-id');
        if (itemId) {
          const height = Math.ceil(target.getBoundingClientRect().height);
          if (height > 0) {
            updates[itemId] = height;
            changed = true;
          }
        }
      }

      if (changed) {
        setMeasuredHeights((prev) => {
          let hasDiff = false;
          for (const [k, v] of Object.entries(updates)) {
            if (prev[k] !== v) {
              hasDiff = true;
              break;
            }
          }
          if (!hasDiff) return prev;
          return { ...prev, ...updates };
        });
      }
    });

    itemResizeObserverRef.current = observer;

    // Observe all currently registered item elements
    itemElementsRef.current.forEach((el) => {
      if (el) observer.observe(el);
    });

    return () => observer.disconnect();
  }, []);

  // Callback ref for registering each item's DOM element
  const registerItemRef = useCallback((id, el) => {
    const map = itemElementsRef.current;
    const observer = itemResizeObserverRef.current;

    if (el) {
      el.setAttribute('data-masonry-id', String(id));
      map.set(id, el);
      if (observer) observer.observe(el);
    } else {
      const prevEl = map.get(id);
      if (prevEl && observer) {
        observer.unobserve(prevEl);
      }
      map.delete(id);
    }
  }, []);

  // Helper: Fallback Height Estimation
  const getEstimatedHeight = useCallback((item) => {
    if (collapsedListIds && collapsedListIds.has(item.id)) {
      return 50;
    }
    const cardCount = item.cards ? item.cards.length : 0;
    const raw = 118 + cardCount * 68;
    return Math.min(820, raw);
  }, [collapsedListIds]);

  // ── Dynamic Responsive Column & Packing Algorithm ──────────────────────
  const layoutResult = useMemo(() => {
    // If containerWidth not yet measured, estimate reasonably from window innerWidth minus sidebar (~300px)
    const width =
      containerWidth ||
      (typeof window !== 'undefined' ? Math.max(300, window.innerWidth - 300) : 1000);

    // Maximum number of columns that can physically fit while respecting minColumnWidth:
    const maxPossibleColumns = Math.max(1, Math.floor((width + gap) / (minColumnWidth + gap)));

    // Target number of columns around targetColumnWidth (~300px):
    let columnsCount = Math.max(1, Math.round((width + gap) / (targetColumnWidth + gap)));
    if (columnsCount > maxPossibleColumns) {
      columnsCount = maxPossibleColumns;
    }

    // Responsive column width: distribute available width across columns evenly
    // width = columnsCount * columnWidth + (columnsCount - 1) * gap
    const availableWidth = width - (columnsCount - 1) * gap;
    const columnWidth = Math.max(minColumnWidth, Math.floor(availableWidth / columnsCount));

    const colHeights = new Array(columnsCount).fill(0);
    const positions = {};

    // Place each list item in logical order
    items.forEach((item, index) => {
      let colIndex;

      if (index < columnsCount) {
        // Guarantee that the top row fills from left-to-right (0, 1, ..., K-1)
        colIndex = index;
      } else {
        // Greedy shortest-column placement
        colIndex = 0;
        let minHeight = colHeights[0];
        for (let c = 1; c < columnsCount; c++) {
          if (colHeights[c] < minHeight) {
            minHeight = colHeights[c];
            colIndex = c;
          }
        }
      }

      const isCollapsed = collapsedListIds && collapsedListIds.has(item.id);
      let itemHeight;
      if (isCollapsed) {
        // If collapsed, use measured height if it reflects collapsed state (<= 80px), otherwise fallback to 50px
        const measured = measuredHeights[item.id];
        itemHeight = (measured && measured <= 80) ? measured : 50;
      } else {
        // If expanded, use measured height if it reflects expanded state (> 80px), otherwise fallback to estimated
        const measured = measuredHeights[item.id];
        itemHeight = (measured && measured > 80) ? measured : getEstimatedHeight(item);
      }

      const x = colIndex * (columnWidth + gap);
      const y = colHeights[colIndex];

      positions[item.id] = {
        x,
        y,
        width: columnWidth,
        colIndex,
      };

      colHeights[colIndex] += itemHeight + gap;
    });

    // Place "+ Add List" slot into the shortest column
    let addSlotPos = null;
    if (hasAddSlot) {
      let shortestCol = 0;
      let minColHeight = colHeights[0];
      for (let c = 1; c < columnsCount; c++) {
        if (colHeights[c] < minColHeight) {
          minColHeight = colHeights[c];
          shortestCol = c;
        }
      }

      const x = shortestCol * (columnWidth + gap);
      const y = colHeights[shortestCol];

      addSlotPos = {
        x,
        y,
        width: columnWidth,
        colIndex: shortestCol,
      };

      colHeights[shortestCol] += addSlotHeight + gap;
    }

    const totalHeight = Math.max(...colHeights, 200) + 40;

    return {
      columnsCount,
      columnWidth,
      positions,
      addSlotPos,
      totalHeight,
    };
  }, [
    containerWidth,
    items,
    measuredHeights,
    targetColumnWidth,
    minColumnWidth,
    gap,
    addSlotHeight,
    hasAddSlot,
    collapsedListIds,
    getEstimatedHeight,
  ]);

  return {
    containerRef,
    registerItemRef,
    containerWidth,
    ...layoutResult,
  };
}
