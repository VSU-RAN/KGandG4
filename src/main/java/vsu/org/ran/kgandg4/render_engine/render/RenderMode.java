package vsu.org.ran.kgandg4.render_engine.render;

public class RenderMode {
    private final boolean wireframe;
    private final boolean texture;
    private final boolean lighting;

    public RenderMode(boolean wireframe, boolean texture, boolean lighting) {
        this.wireframe = wireframe;
        this.texture = texture;
        this.lighting = lighting;
    }

    public boolean isWireframe() { return wireframe; }
    public boolean isTexture() { return texture; }
    public boolean isLighting() { return lighting; }
}