package dev.buildtool.tp;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
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
    public void func_230430_a_(MatrixStack matrixStack,int p_render_1_, int p_render_2_, float p_render_3_)
    {
        func_230446_a_(matrixStack);
        func_238472_a_(matrixStack,field_230712_o_,new StringTextComponent("Trajectory preview configuration"),field_230708_k_/2,6,0xffffff);
        super.func_230430_a_(matrixStack,p_render_1_, p_render_2_, p_render_3_);
        for (Widget button : this.field_230710_m_)
        {
            if(button instanceof TextFieldWidget)
            {
                if (!button.func_230458_i_().getString().isEmpty())
                {
                    func_238475_b_(matrixStack,field_230712_o_, button.func_230458_i_(), button.field_230690_l_ - field_230712_o_.getStringWidth(button.func_230458_i_().getUnformattedComponentText())-3, button.field_230691_m_+6, 0xffffff);
                }
            }
        }
    }

    @Override
    protected void func_231160_c_()
    {
        super.func_231160_c_();
        int i=30;
        TextFieldWidget primaryDotColor=new TextFieldWidget(field_230712_o_,field_230708_k_/2,i,50,20,new StringTextComponent("Primary dot color"));
        primaryDotColor.setText(TrajectoryPreview.primaryDotColor.get());
        primaryDotColor.setValidator(s-> s.isEmpty() || StringUtils.isAlphanumeric(s));
        func_230480_a_(primaryDotColor);
        i+=20;
        TextFieldWidget secondaryDotCOlor=new TextFieldWidget(field_230712_o_,field_230708_k_/2,i,50,20,new StringTextComponent("Secondary dot color"));
        secondaryDotCOlor.setText(TrajectoryPreview.secondaryDotColor.get());
        func_230480_a_(secondaryDotCOlor);
        primaryDotColor.setValidator(s-> s.isEmpty() || StringUtils.isAlphanumeric(s));
        i+=20;
        TextFieldWidget startpathfrom=new TextFieldWidget(field_230712_o_,field_230708_k_/2,i,50,20,new StringTextComponent("Path starts after"));
        startpathfrom.setText(TrajectoryPreview.pathStart.get().intValue()+"");
        func_230480_a_(startpathfrom);
        startpathfrom.setValidator(s-> s.isEmpty() || StringUtils.isNumeric(s));
        i+=20;
        this.func_230480_a_(new ExtendedButton(field_230708_k_/2,field_230709_l_-30,100,20,new StringTextComponent("Save"), p_onPress_1_ -> {
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
            {
                e.printStackTrace();
            }
            func_231175_as__();
        }));
    }

    @Override
    public void func_231175_as__()
    {
        field_230706_i_.displayGuiScreen(previous);
    }
}
