package net.minecraft.src.nbxlite.gui;

import net.minecraft.src.*;

public class PageBeta extends Page{
    private GuiButtonNBXlite[] featuresButtons;
    private GuiButtonNBXlite newOresButton;
    private GuiButtonNBXlite jungleButton;
    private GuiButtonNBXlite iceDesertButton;
    private GuiButtonNBXlite fixBeachesButton;
    private GuiButtonNBXlite weatherButton;
    private boolean newores;
    private boolean jungle;
    private boolean iceDesert;
    private boolean fixbeaches;
    private boolean weather;
    private int features;

    public PageBeta(GuiNBXlite parent){
        super(parent);
        featuresButtons = new GuiButtonNBXlite[GeneratorList.feat1length + 1];
        jungle = ODNBXlite.getDefaultFlag("jungle");
        newores = ODNBXlite.getDefaultFlag("newores");
        iceDesert = ODNBXlite.getDefaultFlag("icedesert");
        fixbeaches = ODNBXlite.getDefaultFlag("fixbeaches");
        weather = getDefaultWeather();
        features = ODNBXlite.DefaultFeaturesBeta;
    }

    @Override
    public void initButtons(){
        int l = featuresButtons.length;
        for (int i = 0; i < l; i++){
            featuresButtons[i] = new GuiButtonNBXlite(i, (width / 2 - 115) + leftmargin, 210);
            String name = mod_OldDays.lang.get("nbxlite.betafeatures" + (i + 1));
            if (i != ODNBXlite.FEATURES_SKY){
                name += " (" + mod_OldDays.lang.get("nbxlite.betafeatures" + (i + 1) + ".desc") + ")";
            }
            featuresButtons[i].displayString = name;
            addButton(featuresButtons[i]);
        }
        addButton(newOresButton = new GuiButtonNBXlite(l, width / 2 - 85 + leftmargin, 150));
        addButton(jungleButton = new GuiButtonNBXlite(l + 1, width / 2 - 85 + leftmargin, 150));
        addButton(iceDesertButton = new GuiButtonNBXlite(l + 2, width / 2 - 85 + leftmargin, 150));
        addButton(fixBeachesButton = new GuiButtonNBXlite(l + 3, width / 2 - 85 + leftmargin, 150));
        addButton(weatherButton = new GuiButtonNBXlite(l + 4, width / 2 - 85 + leftmargin, 150));
        featuresButtons[features].enabled=false;
    }

    @Override
    public void scrolled(){
        super.scrolled();
        for (int i = 0; i < featuresButtons.length; i++){
            setY(featuresButtons[i], (i - 1) * 21);
        }
        setY(newOresButton, 127);
        setY(jungleButton, 149);
        setY(iceDesertButton, 171);
        setY(fixBeachesButton, jungleButton.drawButton ? 193 : 149);
        setY(weatherButton, jungleButton.drawButton ? 215 : 171);
        updateButtonPosition();
    }

    @Override
    public void updateButtonText(){
        newOresButton.displayString = mod_OldDays.lang.get("flag.newores") + ": " + mod_OldDays.lang.get("gui." + (newores ? "on" : "off"));
        jungleButton.displayString = mod_OldDays.lang.get("flag.jungle") + ": " + mod_OldDays.lang.get("gui." + (jungle ? "on" : "off"));
        iceDesertButton.displayString = mod_OldDays.lang.get("flag.icedesert") + ": " + mod_OldDays.lang.get("gui." + (iceDesert ? "on" : "off"));
        fixBeachesButton.displayString = mod_OldDays.lang.get("flag.fixbeaches") + ": " + mod_OldDays.lang.get("gui." + (fixbeaches ? "on" : "off"));
        weatherButton.displayString = mod_OldDays.lang.get("flag.weather") + ": " + mod_OldDays.lang.get("gui." + (weather ? "on" : "off"));
    }

    @Override
    public void updateButtonVisibility(){
        jungleButton.drawButton = features == ODNBXlite.FEATURES_BETA173;
        iceDesertButton.drawButton = features == ODNBXlite.FEATURES_BETA173;
        fixBeachesButton.drawButton = features <= ODNBXlite.FEATURES_BETA173;
        weatherButton.drawButton = features <= ODNBXlite.FEATURES_BETA173;
    }

    @Override
    public void actionPerformedScrolling(GuiButton guibutton){
        if (guibutton == newOresButton){
            newores = !newores;
        }else if (guibutton == jungleButton){
            jungle = !jungle;
        }else if (guibutton == iceDesertButton){
            iceDesert = !iceDesert;
        }else if (guibutton == fixBeachesButton){
            fixbeaches = !fixbeaches;
        }else if (guibutton == weatherButton){
            weather = !weather;
        }else if (guibutton.id < featuresButtons.length){
            featuresButtons[features].enabled = true;
            features = guibutton.id;
            guibutton.enabled = false;
        }
        super.actionPerformedScrolling(guibutton);
    }

    @Override
    public void applySettings(){
        ODNBXlite.Generator = ODNBXlite.GEN_OLDBIOMES;
        ODNBXlite.MapFeatures = features;
        ODNBXlite.setFlag("jungle", jungle);
        ODNBXlite.setFlag("icedesert", iceDesert);
        ODNBXlite.setFlag("newores", newores);
        ODNBXlite.setFlag("fixbeaches", fixbeaches);
        ODNBXlite.setFlag("weather", weather && features != ODNBXlite.FEATURES_SKY);
//         ODNBXlite.setFlag("weather", features == ODNBXlite.FEATURES_BETA15 || features == ODNBXlite.FEATURES_BETA173);
    }

    @Override
    public void setDefaultSettings(){
        features = ODNBXlite.DefaultFeaturesBeta;
        newores = ODNBXlite.getDefaultFlag("newores");
        jungle = ODNBXlite.getDefaultFlag("jungle");
        iceDesert = ODNBXlite.getDefaultFlag("icedesert");
        fixbeaches = ODNBXlite.getDefaultFlag("fixbeaches");
        weather = getDefaultWeather();
    }

    @Override
    public void loadFromWorldInfo(WorldInfo w){
        features = w.mapGenExtra;
        newores = ODNBXlite.getFlagFromString(w.flags, "newores");
        jungle = ODNBXlite.getFlagFromString(w.flags, "jungle");
        iceDesert = ODNBXlite.getFlagFromString(w.flags, "icedesert");
        fixbeaches = ODNBXlite.getFlagFromString(w.flags, "fixbeaches");
        weather = ODNBXlite.getFlagFromString(w.flags, "weather");
    }

    @Override
    public String getString(){
        String str = mod_OldDays.lang.get("nbxlite.betafeatures" + (features + 1));
        if (jungle){
            str += " (" + mod_OldDays.lang.get("betaJungle") + ")";
        }
        return str;
    }

    private boolean getDefaultWeather(){
        return ODNBXlite.getDefaultFlag("weather") || features == ODNBXlite.FEATURES_BETA15 || features == ODNBXlite.FEATURES_BETA173;
    }
}