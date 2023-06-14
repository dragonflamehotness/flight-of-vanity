package com.genmiracle.flightofvanity.graphics.cube;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import org.w3c.dom.Text;

public class CubeSide {
    private MeshPartBuilder mpb;
    private TextureRegion base;
    private Material mat;
    private ModelBuilder mb;

    private Texture texture;

    private Vector3 v0;
    private Vector3 v1;
    private Vector3 v2;
    private Vector3 v3;
    private Vector3 norm;
    private float length;
    private int pixelLength;

    public CubeSide(ModelBuilder mb, TextureRegion base, int pixelLength) {
        this.mb = mb;
        this.base = base;
        this.pixelLength = pixelLength;

        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        mat = new Material(TextureAttribute.createDiffuse(base));

        texture = new Texture(pixelLength, pixelLength, Pixmap.Format.RGBA8888);

        mpb = mb.part("box", GL20.GL_TRIANGLES, attr, mat);
    }

    public void create(Vector3 v0, Vector3 v1, Vector3 v2, Vector3 v3, Vector3 norm, float length) {
        this.v0 = v0; this.v1 = v1; this.v2 = v2; this.v3 = v3; this.norm = norm;
        this.length = length;

        mpb.rect(v0.x * length, v0.y * length, v0.z * length,
                v1.x * length, v1.y * length, v1.z * length,
                v2.x * length, v2.y * length, v2.z * length,
                v3.x * length, v3.y * length, v3.z * length,
                norm.x, norm.y, norm.z);
    }

    public void render() {
        mpb.rect(v0.x * length, v0.y * length, v0.z * length,
                v1.x * length, v1.y * length, v1.z * length,
                v2.x * length, v2.y * length, v2.z * length,
                v3.x * length, v3.y * length, v3.z * length,
                norm.x, norm.y, norm.z);
    }

    public void setTexture(Texture t) {
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

        mat.set(TextureAttribute.createDiffuse(t));
        mpb = mb.part("box", GL20.GL_TRIANGLES, attr, mat);
    }

    public void setPixmap(Pixmap pm) {
        texture.draw(pm, 0, 0);
        setTexture(texture);
    }
//    public void drawBlank() {
//        texture.draw(Material, 0, 0);
//        setTexture(texture);
//    }
}
