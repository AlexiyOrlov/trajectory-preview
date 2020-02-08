package io.github.alexiyorlov.tp;

/**
 * Created on 12/17/19.
 */
public class Options
{
    //    @Config.RangeInt(min = 0,max = 255)
    public static int previewStartPoint = 3;

    //    @Config.Comment("Hexadecimal")
    public static String pointColor1 = "75aaff";

    //    @Config.Comment("Hexadecimal")
    public static String pointColor2 = "e7ed49";

    //    @Config.RangeInt(min = 5,max = 255)
    public static short pointsEnlargeAfterThisDistance = 6;

    public static float pointScalingStep = 0.4f;
}