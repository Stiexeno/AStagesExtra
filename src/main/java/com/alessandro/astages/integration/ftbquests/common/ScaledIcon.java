package com.alessandro.astages.integration.ftbquests.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconWithParent;
import net.minecraft.client.gui.GuiGraphics;

public class ScaledIcon extends IconWithParent
{
    public int scale;
    
    public ScaledIcon(Icon p, int b)
    {
        super(p);
        scale = b;
    }
    
    @Override
    public void draw(GuiGraphics graphics, int x, int y, int w, int h)
    {
        int renderX = x - scale;
        int renderY = y - scale;
        
        int renderW = w + scale * 2;
        int renderH = h + scale * 2;
        
        parent.draw(graphics, renderX, renderY, renderW, renderH);
    }
    
    @Override
    public JsonElement getJson()
    {
        if (scale == 0)
        {
            return parent.getJson();
        }
        
        var json = new JsonObject();
        json.addProperty("id", "scale");
        json.addProperty("scale", scale);
        json.add("parent", parent.getJson());
        return json;
    }
    
    @Override
    public ScaledIcon copy()
    {
        return new ScaledIcon(parent.copy(), scale);
    }
    
    @Override
    public ScaledIcon withTint(Color4I color)
    {
        return new ScaledIcon(parent.withTint(color), scale);
    }
    
    @Override
    public ScaledIcon withColor(Color4I color)
    {
        return new ScaledIcon(parent.withColor(color), scale);
    }
}

