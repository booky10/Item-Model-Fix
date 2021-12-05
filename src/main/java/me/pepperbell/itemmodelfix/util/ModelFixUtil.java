package me.pepperbell.itemmodelfix.util;

import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;

public class ModelFixUtil {

    public static List<BlockElement> createOutlineLayerElements(int layer, String key, TextureAtlasSprite sprite) {
        List<BlockElement> elements = new ArrayList<>();

        int width = sprite.getWidth();
        int height = sprite.getHeight();

        float animationFrameDelta = sprite.uvShrinkRatio();
        float xFactor = width / 16.0F;
        float yFactor = height / 16.0F;

        Map<Direction, BlockElementFace> map = new HashMap<>();
        map.put(Direction.SOUTH, new BlockElementFace(null, layer, key, createUnlerpedTexture(new float[]{0f, 0f, 16f, 16f}, 0, animationFrameDelta)));
        map.put(Direction.NORTH, new BlockElementFace(null, layer, key, createUnlerpedTexture(new float[]{16f, 0f, 0f, 16f}, 0, animationFrameDelta)));
        elements.add(new BlockElement(new Vector3f(0f, 0f, 7.5f), new Vector3f(16f, 16f, 8.5f), map, null, true));

        int first1 = -1, first2 = -1, last1 = -1, last2 = -1;
        PrimitiveIterator.OfInt frames = sprite.getUniqueFrames().iterator();

        while (frames.hasNext()) {
            int frame = frames.nextInt();
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    if (!isPixelTransparent(sprite, frame, x, y)) {
                        if (isPixelTransparent(sprite, frame, x, y + 1)) { // DOWN
                            if (first1 == -1) {
                                first1 = x;
                            }
                            last1 = x;
                        }
                        if (isPixelTransparent(sprite, frame, x, y - 1)) { // UP
                            if (first2 == -1) {
                                first2 = x;
                            }
                            last2 = x;
                        }
                    } else {
                        if (first1 != -1) {
                            elements.add(createHorizontalOutlineElement(Direction.DOWN, layer, key, first1, last1, y, height, animationFrameDelta, xFactor, yFactor));
                            first1 = -1;
                        }
                        if (first2 != -1) {
                            elements.add(createHorizontalOutlineElement(Direction.UP, layer, key, first2, last2, y, height, animationFrameDelta, xFactor, yFactor));
                            first2 = -1;
                        }
                    }
                }

                if (first1 != -1) {
                    elements.add(createHorizontalOutlineElement(Direction.DOWN, layer, key, first1, last1, y, height, animationFrameDelta, xFactor, yFactor));
                    first1 = -1;
                }
                if (first2 != -1) {
                    elements.add(createHorizontalOutlineElement(Direction.UP, layer, key, first2, last2, y, height, animationFrameDelta, xFactor, yFactor));
                    first2 = -1;
                }
            }

            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    if (!isPixelTransparent(sprite, frame, x, y)) {
                        if (isPixelTransparent(sprite, frame, x + 1, y)) { // EAST
                            if (first1 == -1) {
                                first1 = y;
                            }
                            last1 = y;
                        }

                        if (isPixelTransparent(sprite, frame, x - 1, y)) { // WEST
                            if (first2 == -1) {
                                first2 = y;
                            }
                            last2 = y;
                        }
                    } else {
                        if (first1 != -1) {
                            elements.add(createVerticalOutlineElement(Direction.EAST, layer, key, first1, last1, x, height, animationFrameDelta, xFactor, yFactor));
                            first1 = -1;
                        }

                        if (first2 != -1) {
                            elements.add(createVerticalOutlineElement(Direction.WEST, layer, key, first2, last2, x, height, animationFrameDelta, xFactor, yFactor));
                            first2 = -1;
                        }
                    }
                }

                if (first1 != -1) {
                    elements.add(createVerticalOutlineElement(Direction.EAST, layer, key, first1, last1, x, height, animationFrameDelta, xFactor, yFactor));
                    first1 = -1;
                }

                if (first2 != -1) {
                    elements.add(createVerticalOutlineElement(Direction.WEST, layer, key, first2, last2, x, height, animationFrameDelta, xFactor, yFactor));
                    first2 = -1;
                }
            }
        }

        return elements;
    }

    public static BlockElement createHorizontalOutlineElement(Direction direction, int layer, String key, int start, int end, int y, int height, float animationFrameDelta, float xFactor, float yFactor) {
        Map<Direction, BlockElementFace> faces = new HashMap<>();
        faces.put(direction, new BlockElementFace(null, layer, key, createUnlerpedTexture(new float[]{start / xFactor, y / yFactor, (end + 1) / xFactor, (y + 1) / yFactor}, 0, animationFrameDelta)));
        return new BlockElement(new Vector3f(start / xFactor, (height - (y + 1)) / yFactor, 7.5F), new Vector3f((end + 1) / xFactor, (height - y) / yFactor, 8.5F), faces, null, true);
    }

    public static BlockElement createVerticalOutlineElement(Direction direction, int layer, String key, int start, int end, int x, int height, float animationFrameDelta, float xFactor, float yFactor) {
        Map<Direction, BlockElementFace> faces = new HashMap<>();
        faces.put(direction, new BlockElementFace(null, layer, key, createUnlerpedTexture(new float[]{(x + 1) / xFactor, start / yFactor, x / xFactor, (end + 1) / yFactor}, 0, animationFrameDelta)));
        return new BlockElement(new Vector3f(x / xFactor, (height - (end + 1)) / yFactor, 7.5F), new Vector3f((x + 1) / xFactor, (height - start) / yFactor, 8.5F), faces, null, true);
    }

    public static BlockFaceUV createUnlerpedTexture(float[] uvs, int rotation, float delta) {
        float centerU = (uvs[0] + uvs[2]) / 2.0F;
        float centerV = (uvs[1] + uvs[3]) / 2.0F;

        uvs[0] = (uvs[0] - delta * centerU) / (1 - delta);
        uvs[2] = (uvs[2] - delta * centerU) / (1 - delta);
        uvs[1] = (uvs[1] - delta * centerV) / (1 - delta);
        uvs[3] = (uvs[3] - delta * centerV) / (1 - delta);

        return new BlockFaceUV(uvs, rotation);
    }

    public static boolean isPixelTransparent(TextureAtlasSprite sprite, int frame, int x, int y) {
        return x < 0 || y < 0 || x >= sprite.getWidth() || y >= sprite.getHeight() || sprite.isTransparent(frame, x, y);
    }
}
