package com.genmiracle.flightofvanity.util;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class TextureCombiner {
    public static Texture combineTextures(Texture[] textures) {
        Texture acc = textures[0];
        for(int i = 1; i<textures.length; i++){
            acc.getTextureData().prepare();
            Pixmap pixmap1 = acc.getTextureData().consumePixmap();
            Texture texture2 = textures[i];
            texture2.getTextureData().prepare();
            Pixmap pixmap2 = texture2.getTextureData().consumePixmap();

            pixmap1.drawPixmap(pixmap2, 0, 0);
            acc = new Texture(pixmap1);

            pixmap1.dispose();
            pixmap2.dispose();

        }


        return acc;
    }
}
