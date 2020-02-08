package io.github.alexiyorlov.tp;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created on 12/24/19.
 */
public class ConfigScreen extends Screen
{
    Screen previous;
    public ConfigScreen(ITextComponent titleIn, Screen screen)
    {
        super(titleIn);
        previous=screen;
    }

    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_)
    {
        renderBackground();
        drawCenteredString(font,"Trajectory preview configuration",width/2,6,0xffffff);
        super.render(p_render_1_, p_render_2_, p_render_3_);
        for (Widget button : this.buttons)
        {
            if(button instanceof TextFieldWidget)
            {
                if (!button.getMessage().isEmpty())
                {
                    drawString(font, button.getMessage(), button.x - font.getStringWidth(button.getMessage())-3, button.y+6, 0xffffff);
                }
            }
        }
    }

    @Override
    protected void init()
    {
        super.init();
        int i=30;
        TextFieldWidget primaryDotColor=new TextFieldWidget(font,width/2,i,50,20,"Primary dot color");
        primaryDotColor.setText(TrajectoryPreview.primaryDotColor.get());
        primaryDotColor.setValidator(s-> s.isEmpty() || StringUtils.isAlphanumeric(s));
        addButton(primaryDotColor);
        i+=20;
        TextFieldWidget secondaryDotCOlor=new TextFieldWidget(font,width/2,i,50,20,"Secondary dot color");
        secondaryDotCOlor.setText(TrajectoryPreview.secondaryDotColor.get());
        addButton(secondaryDotCOlor);
        primaryDotColor.setValidator(s-> s.isEmpty() || StringUtils.isAlphanumeric(s));
        i+=20;
        TextFieldWidget startpathfrom=new TextFieldWidget(font,width/2,i,50,20,"Path starts after");
        startpathfrom.setText(TrajectoryPreview.pathStart.get().intValue()+"");
        addButton(startpathfrom);
        startpathfrom.setValidator(s-> s.isEmpty() || StringUtils.isNumeric(s));
        i+=20;
        this.addButton(new GuiButtonExt(width/2,height-30,100,20,"Save", p_onPress_1_ -> {
            try
            {
                TrajectoryPreview.primaryDotColor.set(primaryDotColor.getText());
                TrajectoryPreview.secondaryDotColor.set(secondaryDotCOlor.getText());
                TrajectoryPreview.pathStart.set(Double.valueOf(startpathfrom.getText()));

                TrajectoryPreview.primaryDotColor.save();
                TrajectoryPreview.secondaryDotColor.save();
                ;
                TrajectoryPreview.pathStart.save();
            }
            catch (Exception e)
            {e.printStackTrace();}
            onClose();
        }));
    }

    @Override
    public void onClose()
    {
        super.onClose();
        minecraft.displayGuiScreen(previous);
    }
}
