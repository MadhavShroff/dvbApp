package com.dvbinventek.dvbapp.graphing;

import com.scichart.charting.visuals.renderableSeries.XyRenderableSeriesBase;
import com.scichart.charting.visuals.renderableSeries.paletteProviders.IStrokePaletteProvider;
import com.scichart.charting.visuals.renderableSeries.paletteProviders.PaletteProviderBase;
import com.scichart.core.model.IntegerValues;
import com.scichart.drawing.utility.ColorUtil;

public class DimTracePaletteProvider extends PaletteProviderBase<XyRenderableSeriesBase> implements IStrokePaletteProvider {
    private final IntegerValues colors = new IntegerValues();

    private final double startOpacity;
    private final double diffOpacity;

    public DimTracePaletteProvider() {
        super(XyRenderableSeriesBase.class);
        this.startOpacity = 0.2;
        this.diffOpacity = 1 - startOpacity;
    }

    @Override
    public IntegerValues getStrokeColors() {
        return colors;
    }

    @Override
    public void update() {
        final int defaultColor = renderableSeries.getStrokeStyle().getColor();
        final int size = renderableSeries.getCurrentRenderPassData().pointsCount();

        colors.setSize(size);

        final int[] colorsArray = colors.getItemsArray();

        for (int i = 0; i < size; i++) {
            final double faction = i / (double) size;

            final float opacity = (float) (startOpacity + faction * diffOpacity);

            colorsArray[i] = ColorUtil.argb(defaultColor, opacity);
        }
    }
}