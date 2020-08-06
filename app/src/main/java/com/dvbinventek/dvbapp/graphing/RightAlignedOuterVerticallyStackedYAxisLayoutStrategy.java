package com.dvbinventek.dvbapp.graphing;

import com.scichart.charting.layoutManagers.ChartLayoutState;
import com.scichart.charting.layoutManagers.VerticalAxisLayoutStrategy;
import com.scichart.charting.visuals.axes.AxisLayoutState;
import com.scichart.charting.visuals.axes.IAxis;

public class RightAlignedOuterVerticallyStackedYAxisLayoutStrategy extends VerticalAxisLayoutStrategy {
    @Override
    public void measureAxes(int availableWidth, int availableHeight, ChartLayoutState chartLayoutState) {
        for (int i = 0, size = axes.size(); i < size; i++) {
            final IAxis axis = axes.get(i);
            axis.updateAxisMeasurements();
            final AxisLayoutState axisLayoutState = axis.getAxisLayoutState();
            chartLayoutState.rightOuterAreaSize = Math.max(getRequiredAxisSize(axisLayoutState), chartLayoutState.rightOuterAreaSize);
        }
    }

    @Override
    public void layoutAxes(int left, int top, int right, int bottom) {
        final int size = axes.size();
        final int height = bottom - top;
        final int axisHeight = height / size;
        int topPlacement = top;
        for (int i = 0; i < size; i++) {
            final IAxis axis = axes.get(i);
            final AxisLayoutState axisLayoutState = axis.getAxisLayoutState();
            final int bottomPlacement = Math.round(topPlacement + axisHeight);
            axis.layoutArea(left, topPlacement, left + getRequiredAxisSize(axisLayoutState), bottomPlacement);
            topPlacement = bottomPlacement;
        }
    }
}