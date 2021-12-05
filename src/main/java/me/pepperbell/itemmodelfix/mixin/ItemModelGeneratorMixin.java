package me.pepperbell.itemmodelfix.mixin;

import me.pepperbell.itemmodelfix.util.ModelFixUtil;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemModelGenerator.class)
public class ItemModelGeneratorMixin {

    @Inject(method = "processFrames", at = @At(value = "HEAD"), cancellable = true)
    private void onHeadAddLayerElements(int layer, String key, TextureAtlasSprite sprite, CallbackInfoReturnable<List<BlockElement>> returnable) {
        returnable.setReturnValue(ModelFixUtil.createOutlineLayerElements(layer, key, sprite));
    }
}
