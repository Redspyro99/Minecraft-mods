package net.minecraft.src;
import java.util.*;
import java.io.*;
import net.minecraft.client.Minecraft;

public class mod_WTFGameplay extends mod_WTF{
    public void load(){
        addProperty(this, 1, "Disable XP",            false, "DisableXP");
        addProperty(this, 2, "Disable hunger",        false, "DisableHunger");
        addProperty(this, 3, "Instant food",          false, "InstantFood");
        addProperty(this, 4, "Disable food stacking", false, "DisableFoodStacking");
        addProperty(this, 5, "Old loot",              false, "OldDrops");
        addProperty(this, 6, "Disable rare loot",     false, "DisableRareLoot");
        addProperty(this, 7, "Machine bow",           false, "InstantBow");
        addProperty(this, 8, "No bow durability",     false, "InfiniteBow");
        addProperty(this, 9, "Old combat system",     false, "OldCombatSystem");
        addProperty(this, 10,"Old armor",             false, "OldArmor");
        addProperty(this, 11,"Allow debug screen",    true,  "AllowDebug");
        loadModuleProperties(2);
        setSwordDamage(OldCombatSystem);
        setArmorDamage(OldArmor);
        ModLoader.setInGameHook(this, true, true);
    }

    public static boolean DisableXP;
    public static boolean DisableHunger;
    public static boolean InstantFood;
    public static boolean DisableFoodStacking;
    public static boolean OldDrops;
    public static boolean DisableRareLoot;
    public static boolean InstantBow;
    public static boolean InfiniteBow;
    public static boolean OldCombatSystem;
    public static boolean OldArmor;
    public static boolean AllowDebug = true;

    private void setSwordDamage(boolean b){
        try{
            if (b){
                ModLoader.setPrivateValue(net.minecraft.src.ItemSword.class, Item.swordDiamond, 0, 10);
                ModLoader.setPrivateValue(net.minecraft.src.ItemSword.class, Item.swordSteel, 0, 8);
                ModLoader.setPrivateValue(net.minecraft.src.ItemSword.class, Item.swordStone, 0, 6);
            }else{
                ModLoader.setPrivateValue(net.minecraft.src.ItemSword.class, Item.swordDiamond, 0, 7);
                ModLoader.setPrivateValue(net.minecraft.src.ItemSword.class, Item.swordSteel, 0, 6);
                ModLoader.setPrivateValue(net.minecraft.src.ItemSword.class, Item.swordStone, 0, 5);
            }
            ModLoader.setPrivateValue(net.minecraft.src.ItemSword.class, Item.swordWood, 0, 4);
            ModLoader.setPrivateValue(net.minecraft.src.ItemSword.class, Item.swordGold, 0, 4);
        }catch(Exception exception){
            System.out.println(new StringBuilder().append("WTF? '").append(exception).append("' OMG EXCEPTION LOL"));
        }
    }

    private void setArmorDamage(boolean b){
        try{
            if (b){
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.helmetLeather, 2, 3);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.helmetChain, 2, 3);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.helmetSteel, 2, 3);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.helmetGold, 2, 3);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.plateLeather, 2, 8);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.plateChain, 2, 8);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.plateSteel, 2, 8);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.plateGold, 2, 8);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.legsLeather, 2, 6);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.legsChain, 2, 6);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.legsSteel, 2, 6);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.legsGold, 2, 6);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.bootsLeather, 2, 3);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.bootsChain, 2, 3);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.bootsSteel, 2, 3);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.bootsGold, 2, 3);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.helmetLeather, 145, 33 << 0);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.helmetChain, 145, 33 << 1);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.helmetSteel, 145, 33 << 2);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.helmetGold, 145, 33 << 4);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.helmetDiamond, 145, 33 << 3);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.plateLeather, 145, 48 << 0);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.plateChain, 145, 48 << 1);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.plateSteel, 145, 48 << 2);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.plateGold, 145, 48 << 4);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.plateDiamond, 145, 48 << 3);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.legsLeather, 145, 45 << 0);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.legsChain, 145, 45 << 1);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.legsSteel, 145, 45 << 2);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.legsGold, 145, 45 << 4);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.legsDiamond, 145, 45 << 3);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.bootsLeather, 145, 39 << 0);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.bootsChain, 145, 39 << 1);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.bootsSteel, 145, 39 << 2);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.bootsGold, 145, 39 << 4);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.bootsDiamond, 145, 39 << 3);
            }else{
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.helmetLeather, 2, 1);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.helmetChain, 2, 2);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.helmetSteel, 2, 2);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.helmetGold, 2, 2);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.plateLeather, 2, 3);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.plateChain, 2, 5);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.plateSteel, 2, 6);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.plateGold, 2, 5);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.legsLeather, 2, 2);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.legsChain, 2, 4);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.legsSteel, 2, 5);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.legsGold, 2, 3);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.bootsLeather, 2, 1);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.bootsChain, 2, 1);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.bootsSteel, 2, 2);
                ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.bootsGold, 2, 1);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.helmetLeather, 145, 55);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.helmetChain, 145, 165);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.helmetSteel, 145, 165);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.helmetGold, 145, 77);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.helmetDiamond, 145, 363);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.plateLeather, 145, 80);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.plateChain, 145, 240);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.plateSteel, 145, 240);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.plateGold, 145, 112);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.plateDiamond, 145, 528);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.legsLeather, 145, 75);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.legsChain, 145, 225);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.legsSteel, 145, 225);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.legsGold, 145, 105);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.legsDiamond, 145, 495);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.bootsLeather, 145, 65);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.bootsChain, 145, 195);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.bootsSteel, 145, 195);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.bootsGold, 145, 91);
                ModLoader.setPrivateValue(net.minecraft.src.Item.class, Item.bootsDiamond, 145, 429);
            }
            ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.helmetDiamond, 2, 3);
            ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.plateDiamond, 2, 8);
            ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.legsDiamond, 2, 6);
            ModLoader.setPrivateValue(net.minecraft.src.ItemArmor.class, Item.bootsDiamond, 2, 3);
        }catch(Exception exception){
            System.out.println(new StringBuilder().append("WTF? '").append(exception).append("' OMG EXCEPTION LOL"));
        }
    }

    public boolean onTickInGame(float f, Minecraft minecraft){
        tickDebug(minecraft);
        return true;
    }

    public void tickDebug(Minecraft minecraft){
        if (AllowDebug){
            return;
        }
        minecraft.gameSettings.showDebugInfo = false;
    }
}