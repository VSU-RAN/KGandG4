package vsu.org.ran.kgandg4.render_engine.render;

public class RenderMode {
    private final boolean wireframe;
    private final boolean texture;
    private final boolean lighting;
    private final boolean onlyWireframe;

    public RenderMode(boolean wireframe, boolean texture, boolean lighting, boolean onlyWireframe) {
        this.wireframe = wireframe;
        this.texture = texture;
        this.lighting = lighting;
        this.onlyWireframe = onlyWireframe;
    }

    public boolean isWireframe() { return wireframe; }
    public boolean isTexture() { return texture; }
    public boolean isLighting() { return lighting; }
    public boolean isOnlyWireframe() { return onlyWireframe; }

}