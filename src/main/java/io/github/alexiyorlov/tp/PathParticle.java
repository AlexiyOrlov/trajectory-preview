package io.github.alexiyorlov.tp;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created on 2/22/18.
 */
public class PathParticle extends Particle
{
    private int size;

    PathParticle(World worldIn, double posXIn, double posYIn, double posZIn, int pathIndex, int pathLength)
    {
        super(worldIn, posXIn, posYIn, posZIn);
        canCollide = false;
    }

    @Override
    public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks)
    {
        buffer.pos(posX, posY, posZ).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();

    }

    @Override
    public IParticleRenderType getRenderType()
    {
        return IParticleRenderType.CUSTOM;
    }

    public static class Factory implements IParticleFactory<BasicParticleType>
    {
        @Nullable
        @Override
        public Particle makeParticle(BasicParticleType typeIn, World worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            return new PathParticle(worldIn, x, y, z, 0, 0);
        }
    }

//    @Override
//    public void setAlphaF(float alpha)
//    {
//        super.setAlphaF(alpha);
//    }
}
