class ClassicMApplet extends com.mojang.minecraft.MinecraftApplet {
    public String getParameter(String p) {
        return Main.para(p);
    }
}