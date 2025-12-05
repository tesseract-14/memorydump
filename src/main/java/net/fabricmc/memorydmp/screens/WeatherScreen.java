//package net.fabricmc.memorydmp.screens;
//
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.client.gui.widget.ButtonWidget;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.server.world.ServerWorld;
//import net.minecraft.world.World;
//
//import static net.fabricmc.memorydmp.MemoryDmpMod.txt;
//
//public class WeatherScreen extends Screen {
//    private World world;
//    private String selectedWeather = "";
//
//    public WeatherScreen(World world) {
//        super(txt("Weather Controller"));
//        this.world = world;
//    }
//
//    @Override
//    protected void init() {
//        int centerX = this.width / 2;
//        int centerY = this.height / 2;
//
//        // Weather buttons - 1.18.2 syntax
//        this.addDrawableChild(new ButtonWidget(centerX - 100, centerY - 30, 200, 20,
//                txt("☀ Clear"), button -> {
//            setWeather("clear");
//        }));
//
//        this.addDrawableChild(new ButtonWidget(centerX - 100, centerY, 200, 20,
//                txt("🌧️ Rainy"), button -> {
//            setWeather("rain");
//        }));
//
//        this.addDrawableChild(new ButtonWidget(centerX - 100, centerY + 30, 200, 20,
//                txt("⛈️ Stormy"), button -> {
//            setWeather("thunder");
//        }));
//
//        // Close button with more padding
//        this.addDrawableChild(new ButtonWidget(centerX - 50, centerY + 80, 100, 20,
//                txt("Close"), button -> {
//            this.close();
//        }));
//    }
//
//    private void setWeather(String weather) {
//        this.selectedWeather = weather;
//
//        if (!this.world.isClient()) {
//            ServerWorld serverWorld = (ServerWorld) this.world;
//
//
//            switch (weather) {
//                case "clear":
//                    serverWorld.setWeather();
//                    break;
//                case "rain":
//                    this.world.setWeather(0, 6000, true, false); // Rain for 5 minutes
//                    break;
//                case "thunder":
//                    this.world.setWeather(0, 6000, true, true); // Thunderstorm for 5 minutes
//                    break;
//            }
//        }
//
//        this.close();
//    }
//
//    public String getSelectedWeather() {
//        return this.selectedWeather;
//    }
//
//    @Override
//    public void close() {
//        this.client.setScreen(null);
//    }
//
//    @Override
//    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
//        this.renderBackground(matrices);
//        drawCenteredText(matrices, this.textRenderer, this.title,
//                this.width / 2, 30, 0xFFFFFF);
//        super.render(matrices, mouseX, mouseY, delta);
//    }
//}