package io.github.alexiyorlov.tp.api;

/**
 * Created on 7/6/19 by alexiy.
 * RGB-255 format
 */
public class IntColor
{
    private int red;
    private int green;
    private int blue;
    private int alpha;
    private int packedColor;

    public IntColor(int color, int alpha)
    {
        red = color >> 16 & 255;
        green = color >> 8 & 255;
        blue = color & 255;
        packedColor = color;
        this.alpha = alpha;
    }

    public IntColor(int intColor)
    {
        this(intColor, 255);
    }

    /**
     * @param alpha 0 - transparent, 255 - opaque
     */
    public IntColor(int red, int green, int blue, int alpha)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        packedColor = 256 * 256 * red + 256 * green + blue;
    }

    public int getAlpha()
    {
        return alpha;
    }

    public int getBlue()
    {
        return blue;
    }

    public int getGreen()
    {
        return green;
    }

    public int getRed()
    {
        return red;
    }

    public int getPackedColor()
    {
        return packedColor;
    }
}
