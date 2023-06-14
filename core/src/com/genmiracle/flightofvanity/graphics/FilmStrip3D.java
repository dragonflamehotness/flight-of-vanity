package com.genmiracle.flightofvanity.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.genmiracle.flightofvanity.assets.FilmStripParser;
import com.genmiracle.flightofvanity.util.FilmStrip;

public class FilmStrip3D extends FilmStrip {

    /**
     * Creates a new 3D filmstrip from the given texture.
     * <p>
     * The filmstrip will use the entire texture.
     *
     * @param texture The texture image to use
     * @param rows    The number of rows in the filmstrip
     * @param cols    The number of columns in the filmstrip
     */
    public FilmStrip3D(Texture texture, int rows, int cols) {
        super(texture, rows, cols);
    }
}
