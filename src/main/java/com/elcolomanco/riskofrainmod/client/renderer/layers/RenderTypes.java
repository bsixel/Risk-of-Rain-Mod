package com.elcolomanco.riskofrainmod.client.renderer.layers;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RenderTypes extends RenderState {

	public RenderTypes(String nameIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, setupTaskIn, clearTaskIn);
	}

	public static RenderType getEmissiveEntity(ResourceLocation texture) {
		RenderType.State state = RenderType.State.builder().setTextureState(new RenderState.TextureState(texture, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(NO_DIFFUSE_LIGHTING)
				.setAlphaState(DEFAULT_ALPHA).setCullState(NO_CULL).setLightmapState(RenderState.LIGHTMAP).setOverlayState(RenderState.OVERLAY).createCompositeState(true);
		return RenderType.create("entity_emissive_cutout", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, false, state);
	}
}
