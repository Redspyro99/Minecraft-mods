package net.minecraft.src;

import java.io.PrintStream;
import java.util.*;
import net.minecraft.src.nbxlite.oldbiomes.*;
import net.minecraft.src.nbxlite.spawners.*;
import net.minecraft.src.nbxlite.indev.*;
import net.minecraft.src.nbxlite.chunkproviders.*;

public class WorldSSP2 extends WorldSSP
{
    /**
     * TreeSet of scheduled ticks which is used as a priority queue for the ticks
     */
    private TreeSet scheduledTickTreeSet;

    /** Set of scheduled ticks (used for checking if a tick already exists) */
    private Set scheduledTickSet;

    /** Entities marked for removal. */
    private List entityRemoval;
    private long cloudColour;

    /**
     * Contains a timestamp from when the World object was created. Is used in the session.lock file
     */
    public long lockTimestamp;
    protected int autosavePeriod;

    /**
     * Used to differentiate between a newly generated world and an already existing world.
     */
    public boolean isNewWorld;

    /**
     * A flag indicating whether or not all players in the world are sleeping.
     */
    private boolean allPlayersSleeping;
    private ArrayList collidingBoundingBoxes;
    private boolean scanningTileEntities;

    /** number of ticks until the next random ambients play */
    private int ambientTickCountdown;

    /**
     * entities within AxisAlignedBB excluding one, set and returned in getEntitiesWithinAABBExcludingEntity(Entity
     * var1, AxisAlignedBB var2)
     */
    private List entitiesWithinAABBExcludingEntity;
    private static final WeightedRandomChestContent field_73069_S[];

    public boolean snowCovered;
    public int mapGen;
    public int mapGenExtra;
    public int mapTypeIndev;
    private OldSpawnerAnimals animalSpawner;
    private OldSpawnerMonsters monsterSpawner;
    private OldSpawnerAnimals waterMobSpawner;

    /**
     * Gets the biome for a given set of x/z coordinates
     */
    public BiomeGenBase getBiomeGenForCoords(int par1, int par2)
    {
        return worldProvider.worldChunkMgr.getBiomeGenAt(par1, par2);
    }

    protected void func_73047_i()
    {
        if (ODNBXlite.Generator == ODNBXlite.GEN_BIOMELESS && ODNBXlite.MapFeatures == ODNBXlite.FEATURES_INDEV){
            int j = worldInfo.getSpawnX();
            int k = worldInfo.getSpawnZ();
            int l = worldInfo.getSpawnY() + 2;
            int dir = rand.nextInt(3);
            if (dir == 0){
                j -= 2;
            }else if (dir == 1){
                j += 2;
            }else if (dir == 2){
                k += 2;
            }
            setBlockWithNotify(j, l, k, Block.chest.blockID);
            TileEntityChest tileentitychest = (TileEntityChest)getBlockTileEntity(j, l, k);
            if (tileentitychest != null && tileentitychest != null){
                WeightedRandomChestContent.func_76293_a(rand, field_73069_S, tileentitychest, 10);
            }
            return;
        }
        WorldGeneratorBonusChest worldgeneratorbonuschest = new WorldGeneratorBonusChest(field_73069_S, 10);
        int i = 0;
        do
        {
            if (i >= 10)
            {
                break;
            }
            int j = (worldInfo.getSpawnX() + rand.nextInt(6)) - rand.nextInt(6);
            int k = (worldInfo.getSpawnZ() + rand.nextInt(6)) - rand.nextInt(6);
            int l = getTopSolidOrLiquidBlock(j, k) + 1;

            if (worldgeneratorbonuschest.generate(this, rand, j, l, k))
            {
                break;
            }
            i++;
        }
        while (true);
    }

    public WorldSSP2(ISaveHandler par1ISaveHandler, String par2Str, WorldProvider par3WorldProvider, WorldSettings par4WorldSettings, Profiler p)
    {
        super(par1ISaveHandler, par2Str, par3WorldProvider, par4WorldSettings, p);
        scheduledTickTreeSet = new TreeSet();
        scheduledTickSet = new HashSet();
        entityRemoval = new ArrayList();
        cloudColour = 0xffffffL;
        lockTimestamp = System.currentTimeMillis();
        autosavePeriod = 40;
        isNewWorld = false;
        collidingBoundingBoxes = new ArrayList();
        ambientTickCountdown = rand.nextInt(12000);
        entitiesWithinAABBExcludingEntity = new ArrayList();
        worldInfo = new WorldInfo(par4WorldSettings, par2Str);
        par3WorldProvider.registerWorld(this);
        ODNBXlite.SetGenerator(this, ODNBXlite.GEN_NEWBIOMES, ODNBXlite.FEATURES_12, ODNBXlite.THEME_NORMAL, ODNBXlite.TYPE_INLAND, false, false);
        ODNBXlite.setSkyBrightness(ODNBXlite.MapTheme);
        ODNBXlite.setSkyColor(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, 0);
        ODNBXlite.setSkyColor(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, 1);
        ODNBXlite.setSkyColor(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, 2);
        ODNBXlite.setCloudHeight(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, ODNBXlite.IndevMapType);
        ODNBXlite.setIndevBounds(ODNBXlite.IndevMapType, ODNBXlite.MapTheme);
        ODNBXlite.refreshProperties();
//         ODNBXlite NBX = new ODNBXlite();
//         NBX.RequestGeneratorInfo();
        turnOnOldSpawners();
        calculateInitialSkylight();
        calculateInitialWeather();
    }

    public WorldSSP2(WorldSSP par1World, WorldProvider par2WorldProvider, Profiler p)
    {
        super(par1World.saveHandler, par1World.getWorldInfo().getWorldName(), par2WorldProvider, new WorldSettings(par1World.getWorldInfo()), p);
        scheduledTickTreeSet = new TreeSet();
        scheduledTickSet = new HashSet();
        entityRemoval = new ArrayList();
        cloudColour = 0xffffffL;
        lockTimestamp = System.currentTimeMillis();
        autosavePeriod = 40;
        isNewWorld = false;
        collidingBoundingBoxes = new ArrayList();
        ambientTickCountdown = rand.nextInt(12000);
        entitiesWithinAABBExcludingEntity = new ArrayList();
        lockTimestamp = par1World.lockTimestamp;
        worldInfo = new WorldInfo(par1World.worldInfo);
        mapGen = worldInfo.mapGen;
        mapGenExtra = worldInfo.mapGenExtra;
        snowCovered = worldInfo.snowCovered;
        ODNBXlite.SetGenerator(this, mapGen, mapGenExtra, worldInfo.mapTheme, worldInfo.mapType, snowCovered, worldInfo.newOres);
        ODNBXlite.refreshProperties();
        turnOnOldSpawners();
        par2WorldProvider.registerWorld(this);
        calculateInitialSkylight();
        calculateInitialWeather();
    }

    public WorldSSP2(ISaveHandler par1ISaveHandler, String par2Str, WorldSettings par3WorldSettings, Profiler p)
    {
        this(par1ISaveHandler, par2Str, par3WorldSettings, ((WorldProvider)(null)), p);
    }

    public WorldSSP2(ISaveHandler par1ISaveHandler, String par2Str, WorldSettings par3WorldSettings, WorldProvider par4WorldProvider, Profiler p)
    {
        super(par1ISaveHandler, par2Str, par3WorldSettings, par4WorldProvider, p);
        scheduledTickTreeSet = new TreeSet();
        scheduledTickSet = new HashSet();
        entityRemoval = new ArrayList();
        cloudColour = 0xffffffL;
        lockTimestamp = System.currentTimeMillis();
        autosavePeriod = 40;
        isNewWorld = false;
        collidingBoundingBoxes = new ArrayList();
        ambientTickCountdown = rand.nextInt(12000);
        entitiesWithinAABBExcludingEntity = new ArrayList();
        worldInfo = par1ISaveHandler.loadWorldInfo();
        isNewWorld = worldInfo == null;

        boolean flag = false;

        if (worldInfo == null)
        {
            worldInfo = new WorldInfo(par3WorldSettings, par2Str);
            flag = true;
        }
        else
        {
            worldInfo.setWorldName(par2Str);
        }

        worldProvider.registerWorld(this);
        chunkProvider = createChunkProvider();

        if (flag)
         {
            worldInfo.nbxlite = true;
            worldInfo.mapGen = ODNBXlite.Generator;
            worldInfo.mapGenExtra = ODNBXlite.MapFeatures;
            worldInfo.mapTheme = ODNBXlite.MapTheme;
            worldInfo.newOres = ODNBXlite.GenerateNewOres;
            mapGen=ODNBXlite.Generator;
            mapGenExtra=ODNBXlite.MapFeatures;
            if(ODNBXlite.Generator==ODNBXlite.GEN_BIOMELESS && (ODNBXlite.MapTheme==ODNBXlite.THEME_NORMAL || ODNBXlite.MapTheme==ODNBXlite.THEME_WOODS) && ODNBXlite.MapFeatures==ODNBXlite.FEATURES_ALPHA11201)
            {
                if (!ODNBXlite.Import){
                    if(rand.nextInt(ODNBXlite.MapTheme==ODNBXlite.THEME_WOODS ? 2 : 4) == 0)
                    {
                        worldInfo.snowCovered = true;
                        snowCovered = true;
                        ODNBXlite.SnowCovered = true;
                    }else{
                        ODNBXlite.SnowCovered=false;
                    }
                }else{
                    snowCovered = worldInfo.snowCovered;
                    ODNBXlite.SnowCovered=worldInfo.snowCovered;
                }
            }else{
                ODNBXlite.SnowCovered=false;
            }
            ODNBXlite.SetGenerator(this, ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, ODNBXlite.IndevMapType, ODNBXlite.SnowCovered, ODNBXlite.GenerateNewOres);
            if (!(ODNBXlite.Generator==ODNBXlite.GEN_BIOMELESS && ODNBXlite.MapFeatures==ODNBXlite.FEATURES_INDEV && ODNBXlite.Import)){
                worldInfo.cloudheight = ODNBXlite.setCloudHeight(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, ODNBXlite.IndevMapType);
                worldInfo.skybrightness = ODNBXlite.setSkyBrightness(ODNBXlite.MapTheme);
                worldInfo.skycolor = ODNBXlite.setSkyColor(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, 0);
                worldInfo.fogcolor = ODNBXlite.setSkyColor(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, 1);
                worldInfo.cloudcolor = ODNBXlite.setSkyColor(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, 2);
            }
            if (ODNBXlite.Generator==ODNBXlite.GEN_BIOMELESS && ODNBXlite.MapFeatures==ODNBXlite.FEATURES_INDEV){
                if (!ODNBXlite.Import){
                    ODNBXlite.generateIndevLevel(getSeed());
                    for (int x=-2; x<(ODNBXlite.IndevWidthX/16)+2; x++){
                        for (int z=-2; z<(ODNBXlite.IndevWidthZ/16)+2; z++){
                            chunkProvider.provideChunk(x,z);
                        }
                    }
                    ODNBXlite.IndevWorld = null;
                    ODNBXlite.setIndevBounds(ODNBXlite.IndevMapType, ODNBXlite.MapTheme);
                }else{
                    mod_OldDays.getMinecraftInstance().loadingScreen.printText("Importing Indev level");
                    mod_OldDays.getMinecraftInstance().loadingScreen.displayLoadingString("Loading blocks..");
                    for (int x=-2; x<(ODNBXlite.IndevWidthX/16)+2; x++){
                        mod_OldDays.getMinecraftInstance().loadingScreen.setLoadingProgress((x / ((ODNBXlite.IndevWidthX/16)+2)) * 100);
                        for (int z=-2; z<(ODNBXlite.IndevWidthZ/16)+2; z++){
                            chunkProvider.provideChunk(x,z);
                        }
                    }
                    worldInfo.setWorldTime(ODNBXlite.mclevelimporter.timeofday);
                    List tentlist = ODNBXlite.mclevelimporter.tileentities;
                    mod_OldDays.getMinecraftInstance().loadingScreen.displayLoadingString("Fixing blocks..");
                    for (int x = 0; x < ODNBXlite.IndevWidthX; x++){
                        mod_OldDays.getMinecraftInstance().loadingScreen.setLoadingProgress((int)(((float)x / (float)ODNBXlite.IndevWidthX) * 100F));
                        for (int y = 0; y < ODNBXlite.IndevHeight; y++){
                            for (int z = 0; z < ODNBXlite.IndevWidthZ; z++){
                                int id = getBlockId(x, y, z);
                                int meta = ODNBXlite.mclevelimporter.data[IndexFinite(x, y, z)] >> 4;
                                if (ODNBXlite.mclevelimporter.needsFixing(id)){
                                    setBlockAndMetadata(x, y, z, ODNBXlite.mclevelimporter.getRightId(id), ODNBXlite.mclevelimporter.getRightMetadata(id));
                                }else if (id != 0 && meta != 0){
                                    setBlockMetadata(x, y, z, meta);
                                }
                                if (Block.lightValue[id]>0){
                                    updateAllLightTypes(x, y, z);
                                }
                                if (id > 0 && Block.blocksList[id].hasTileEntity()){
                                    for (int i=0; i < tentlist.size(); i++){
                                        TileEntity tent = ((TileEntity)tentlist.get(i));
                                        if (tent.xCoord == x && tent.yCoord == y && tent.zCoord == z){
                                            setBlockTileEntity(x, y, z, tent);
                                            tentlist.remove(i);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    mod_OldDays.getMinecraftInstance().loadingScreen.displayLoadingString("Loading entities..");
                    List entlist = ODNBXlite.mclevelimporter.entities;
                    for (int i = 0; i < entlist.size(); i++){
                        Entity entity = EntityList.createEntityFromNBT(((NBTTagCompound)entlist.get(i)), this);
                        spawnEntityInWorld(entity);
                    }
                    worldInfo.cloudheight = ODNBXlite.CloudHeight;
                    worldInfo.skybrightness = ODNBXlite.SkyBrightness;
                    worldInfo.skycolor = ODNBXlite.SkyColor;
                    worldInfo.fogcolor = ODNBXlite.FogColor;
                    worldInfo.cloudcolor = ODNBXlite.CloudColor;
                }
                mapTypeIndev=ODNBXlite.IndevMapType;
                worldInfo.mapType = ODNBXlite.IndevMapType;
                worldInfo.indevX = ODNBXlite.IndevWidthX;
                worldInfo.indevZ = ODNBXlite.IndevWidthZ;
                worldInfo.indevY = ODNBXlite.IndevHeight;
            }else if (ODNBXlite.Generator==ODNBXlite.GEN_BIOMELESS && ODNBXlite.MapFeatures==ODNBXlite.FEATURES_CLASSIC){
                ODNBXlite.generateClassicLevel(getSeed());
                for (int x=-2; x<(ODNBXlite.IndevWidthX/16)+2; x++){
                    for (int z=-2; z<(ODNBXlite.IndevWidthZ/16)+2; z++){
                        chunkProvider.provideChunk(x,z);
                    }
                }
                ODNBXlite.IndevWorld = null;
                mapTypeIndev=0;
                worldInfo.mapType = 0;
                worldInfo.indevX = ODNBXlite.IndevWidthX;
                worldInfo.indevZ = ODNBXlite.IndevWidthZ;
                worldInfo.indevY = ODNBXlite.IndevHeight;
                ODNBXlite.setIndevBounds(5, ODNBXlite.MapTheme);
            }else{
                mapTypeIndev=0;
                worldInfo.mapType = 0;
            }
            generateSpawnPoint();
            if (par3WorldSettings.func_77167_c()){
                func_73047_i();
            }
        } else
        {
            if (worldInfo.nbxlite){
                mapGen = worldInfo.mapGen;
                mapGenExtra = worldInfo.mapGenExtra;
                snowCovered = worldInfo.snowCovered;
                mapTypeIndev = worldInfo.mapType;
                ODNBXlite.IndevWidthX = worldInfo.indevX;
                ODNBXlite.IndevWidthZ = worldInfo.indevZ;
                ODNBXlite.IndevHeight = worldInfo.indevY;
                ODNBXlite.SurrWaterType = worldInfo.surrwatertype;
                ODNBXlite.SurrWaterHeight = worldInfo.surrwaterheight;
                ODNBXlite.SurrGroundType = worldInfo.surrgroundtype;
                ODNBXlite.SurrGroundHeight = worldInfo.surrgroundheight;
                ODNBXlite.CloudHeight = worldInfo.cloudheight;
                ODNBXlite.SkyBrightness = worldInfo.skybrightness;
                ODNBXlite.SkyColor = worldInfo.skycolor;
                ODNBXlite.FogColor = worldInfo.fogcolor;
                ODNBXlite.CloudColor = worldInfo.cloudcolor;
                ODNBXlite.SetGenerator(this, mapGen, mapGenExtra, worldInfo.mapTheme, mapTypeIndev, snowCovered, worldInfo.newOres);
            }else{
                ODNBXlite.SetGenerator(this, ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, ODNBXlite.IndevMapType, ODNBXlite.SnowCovered, ODNBXlite.GenerateNewOres);
                worldInfo.nbxlite = true;
                worldInfo.mapGen = ODNBXlite.Generator;
                worldInfo.mapGenExtra = ODNBXlite.MapFeatures;
                worldInfo.mapTheme = ODNBXlite.MapTheme;
                worldInfo.newOres = ODNBXlite.GenerateNewOres;
                worldInfo.mapType = ODNBXlite.IndevMapType;
                worldInfo.indevX = ODNBXlite.IndevWidthX;
                worldInfo.indevZ = ODNBXlite.IndevWidthZ;
                worldInfo.indevY = ODNBXlite.IndevHeight;
                ODNBXlite.setIndevBounds(ODNBXlite.IndevMapType, ODNBXlite.MapTheme);
                worldInfo.surrwatertype = ODNBXlite.SurrWaterType;
                worldInfo.surrwaterheight = ODNBXlite.SurrWaterHeight;
                worldInfo.surrgroundtype = ODNBXlite.SurrGroundType;
                worldInfo.surrgroundheight = ODNBXlite.SurrGroundHeight;
                worldInfo.cloudheight = ODNBXlite.setCloudHeight(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, ODNBXlite.IndevMapType);
                worldInfo.skybrightness = ODNBXlite.setSkyBrightness(ODNBXlite.MapTheme);
                worldInfo.skycolor = ODNBXlite.setSkyColor(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, 0);
                worldInfo.fogcolor = ODNBXlite.setSkyColor(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, 1);
                worldInfo.cloudcolor = ODNBXlite.setSkyColor(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, 2);
            }
            worldProvider.registerWorld(this);
        }
        ODNBXlite.refreshProperties();
        ODNBXlite.setTextureFX();
        turnOnOldSpawners();
        calculateInitialSkylight();
        calculateInitialWeather();
    }

    private int IndexFinite(int x, int y, int z){
        return x+(y*ODNBXlite.IndevWidthZ+z)*ODNBXlite.IndevWidthX;
    }

    public void turnOnOldSpawners()
    {
        animalSpawner = new OldSpawnerAnimals(15, net.minecraft.src.EntityAnimal.class, new Class[] {
            net.minecraft.src.EntitySheep.class, net.minecraft.src.EntityPig.class, net.minecraft.src.EntityCow.class, net.minecraft.src.EntityChicken.class,
            net.minecraft.src.EntityWolf.class, net.minecraft.src.EntityOcelot.class
        });
        monsterSpawner = new OldSpawnerMonsters(200, net.minecraft.src.IMob.class, new Class[] {
            net.minecraft.src.EntityZombie.class, net.minecraft.src.EntitySkeleton.class, net.minecraft.src.EntityCreeper.class, net.minecraft.src.EntitySpider.class,
            net.minecraft.src.EntitySlime.class, net.minecraft.src.EntityEnderman.class
        });
        waterMobSpawner = new OldSpawnerAnimals(5, net.minecraft.src.EntityWaterMob.class, new Class[] {
            net.minecraft.src.EntitySquid.class
        });
    }

    /**
     * Finds an initial spawn location upon creating a new world
     */
    protected void generateSpawnPoint()
    {
        if (ODNBXlite.Generator==ODNBXlite.GEN_NEWBIOMES){
            if (ODNBXlite.MapFeatures<ODNBXlite.FEATURES_11){
                findingSpawnPoint = true;
                WorldChunkManager worldchunkmanager = getWorldChunkManager();
                List list = worldchunkmanager.getBiomesToSpawnIn();
                Random random = new Random(getSeed());
                ChunkPosition chunkposition = worldchunkmanager.findBiomePosition(0, 0, 256, list, random);
                int i = 0;
                int j = 64;
                int k = 0;
                if(chunkposition != null)
                {
                    i = chunkposition.x;
                    k = chunkposition.z;
                } else
                {
                    System.out.println("Unable to find spawn biome");
                }
                int l = 0;
                do
                {
                    if(worldProvider.canCoordinateBeSpawn(i, k))
                    {
                        break;
                    }
                    i += random.nextInt(64) - random.nextInt(64);
                    k += random.nextInt(64) - random.nextInt(64);
                } while(++l != 1000);
                worldInfo.setSpawnPosition(i, j, k);
                findingSpawnPoint = false;
            }else{
                if (!worldProvider.canRespawnHere())
                {
                    worldInfo.setSpawnPosition(0, worldProvider.getAverageGroundLevel(), 0);
                    return;
                }
                findingSpawnPoint = true;
                WorldChunkManager worldchunkmanager = getWorldChunkManager();
                List list = worldchunkmanager.getBiomesToSpawnIn();
                Random random = new Random(getSeed());
                ChunkPosition chunkposition = worldchunkmanager.findBiomePosition(0, 0, 256, list, random);
                int i = 0;
                int j = worldProvider.getAverageGroundLevel();
                int k = 0;
                if (chunkposition != null)
                {
                    i = chunkposition.x;
                    k = chunkposition.z;
                }
                else
                {
                    System.out.println("Unable to find spawn biome");
                }
                int l = 0;
                do
                {
                    if (worldProvider.canCoordinateBeSpawn(i, k))
                    {
                        break;
                    }
                    i += random.nextInt(64) - random.nextInt(64);
                    k += random.nextInt(64) - random.nextInt(64);
                }
                while (++l != 1000);
                worldInfo.setSpawnPosition(i, j, k);
                findingSpawnPoint = false;
            }
        }else if (ODNBXlite.Generator==ODNBXlite.GEN_BIOMELESS && ODNBXlite.MapFeatures==ODNBXlite.FEATURES_INDEV){
            findingSpawnPoint = true;
            worldInfo.setSpawnPosition(ODNBXlite.IndevSpawnX, ODNBXlite.IndevSpawnY, ODNBXlite.IndevSpawnZ);
            if (!ODNBXlite.Import){
                setBlockWithNotify(ODNBXlite.IndevSpawnX-2, ODNBXlite.IndevSpawnY+3, ODNBXlite.IndevSpawnZ, Block.torchWood.blockID);
                setBlockWithNotify(ODNBXlite.IndevSpawnX+2, ODNBXlite.IndevSpawnY+3, ODNBXlite.IndevSpawnZ, Block.torchWood.blockID);
            }
            findingSpawnPoint = false;
        }else if (ODNBXlite.Generator==ODNBXlite.GEN_BIOMELESS && ODNBXlite.MapFeatures==ODNBXlite.FEATURES_CLASSIC){
            findingSpawnPoint = true;
            worldInfo.setSpawnPosition(ODNBXlite.IndevSpawnX, ODNBXlite.IndevSpawnY, ODNBXlite.IndevSpawnZ);
            findingSpawnPoint = false;
        }else{
            findingSpawnPoint = true;
            int i = 0;
            byte byte0 = 64;
            int j;
            for(j = 0; !worldProvider.canCoordinateBeSpawn(i, j); j += rand.nextInt(64) - rand.nextInt(64)){
                i += rand.nextInt(64) - rand.nextInt(64);
            }
            worldInfo.setSpawnPosition(i, byte0, j);
            findingSpawnPoint = false;
        }
    }

    /**
     * Gets the hard-coded portal location to use when entering this dimension
     */
    public ChunkCoordinates getEntrancePortalLocation()
    {
        return worldProvider.getEntrancePortalLocation();
    }

    /**
     * Sets a new spawn location by finding an uncovered block at a random (x,z) location in the chunk.
     */
    public void setSpawnLocation()
    {
        if (ODNBXlite.isFinite()){
            findingSpawnPoint = true;
            worldInfo.setSpawnX(worldInfo.getSpawnX());
            worldInfo.setSpawnY(worldInfo.getSpawnY());
            worldInfo.setSpawnZ(worldInfo.getSpawnZ());
            findingSpawnPoint = false;
        }else if (ODNBXlite.Generator!=ODNBXlite.GEN_NEWBIOMES){
            if(worldInfo.getSpawnY() <= 0)
            {
                worldInfo.setSpawnY(64);
            }
            int i = worldInfo.getSpawnX();
            int j;
            for(j = worldInfo.getSpawnZ(); getFirstUncoveredBlock(i, j) == 0; j += rand.nextInt(8) - rand.nextInt(8))
            {
                i += rand.nextInt(8) - rand.nextInt(8);
            }
            worldInfo.setSpawnX(i);
            worldInfo.setSpawnZ(j);
        }else{
            if(worldInfo.getSpawnY() <= 0)
            {
                worldInfo.setSpawnY(64);
            }
            int i = worldInfo.getSpawnX();
            int j = worldInfo.getSpawnZ();
            int k = 0;
            do
            {
                if(getFirstUncoveredBlock(i, j) != 0)
                {
                    break;
                }
                i += rand.nextInt(8) - rand.nextInt(8);
                j += rand.nextInt(8) - rand.nextInt(8);
            } while(++k != 10000);
            if (ODNBXlite.MapFeatures<ODNBXlite.FEATURES_11){
                for(j = worldInfo.getSpawnZ(); getFirstUncoveredBlock(i, j) == 0; j += rand.nextInt(8) - rand.nextInt(8))
                {
                    i += rand.nextInt(8) - rand.nextInt(8);
                }
            }
            worldInfo.setSpawnX(i);
            worldInfo.setSpawnZ(j);
        }
    }

    public void func_6464_c()
    {
    }

    /**
     * spawns a player, load data from level.dat if needed and loads surrounding chunks
     */
    public void spawnPlayerWithLoadedChunks(EntityPlayer par1EntityPlayer)
    {
        try
        {
            NBTTagCompound nbttagcompound = worldInfo.getPlayerNBTTagCompound();
            if (ODNBXlite.isFinite() && ODNBXlite.Import){
                par1EntityPlayer.readFromNBT(ODNBXlite.mclevelimporter.localplayer);
                ODNBXlite.mclevelimporter = null;
            }
            ODNBXlite.Import = false;

            if (nbttagcompound != null)
            {
                par1EntityPlayer.readFromNBT(nbttagcompound);
                worldInfo.setPlayerNBTTagCompound(null);
            }

            if (chunkProvider instanceof ChunkProviderLoadOrGenerate)
            {
                ChunkProviderLoadOrGenerate chunkproviderloadorgenerate = (ChunkProviderLoadOrGenerate)chunkProvider;
                int i = MathHelper.floor_float((int)par1EntityPlayer.posX) >> 4;
                int j = MathHelper.floor_float((int)par1EntityPlayer.posZ) >> 4;
                chunkproviderloadorgenerate.setCurrentChunkOver(i, j);
            }

            spawnEntityInWorld(par1EntityPlayer);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Saves the data for this World. If passed true, then only save up to 2 chunks, otherwise, save all chunks.
     */
    public void saveWorld(boolean par1, IProgressUpdate par2IProgressUpdate)
    {
        if (!chunkProvider.canSave())
        {
            return;
        }

        if (par2IProgressUpdate != null)
        {
            par2IProgressUpdate.displaySavingString("Saving level");
        }

        saveLevel();

        if (par2IProgressUpdate != null)
        {
            par2IProgressUpdate.displayLoadingString("Saving chunks");
        }

        chunkProvider.saveChunks(par1, par2IProgressUpdate);
    }

    /**
     * Saves the global data associated with this World
     */
    private void saveLevel()
    {
        checkSessionLock();
        saveHandler.saveWorldInfoAndPlayer(worldInfo, playerEntities);
        worldInfo.setSaveVersion(19133);
        mapStorage.saveAllData();
    }

    /**
     * Saves the world and all chunk data without displaying any progress message. If passed 0, then save player info
     * and metadata as well.
     */
    public boolean quickSaveWorld(int par1)
    {
        if (!chunkProvider.canSave())
        {
            return true;
        }

        if (par1 == 0)
        {
            saveLevel();
        }

        return chunkProvider.saveChunks(false, null);
    }

    private boolean isBounds(int x, int y, int z){
        if (ODNBXlite.Generator==ODNBXlite.GEN_BIOMELESS && (worldProvider==null || worldProvider.worldType==0)){
            if (ODNBXlite.MapFeatures==ODNBXlite.FEATURES_INDEV){
                if(x<=0 || x>=ODNBXlite.IndevWidthX-1 || z<=0 || z>=ODNBXlite.IndevWidthZ-1 || y<0){
                    return true;
                }
            }
            if (ODNBXlite.MapFeatures==ODNBXlite.FEATURES_CLASSIC){
                if(x<0 || x>=ODNBXlite.IndevWidthX || z<0 || z>=ODNBXlite.IndevWidthZ || y<0){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBounds2(int x, int y, int z){
        if (ODNBXlite.Generator==ODNBXlite.GEN_BIOMELESS && (worldProvider==null || worldProvider.worldType==0)){
            if (ODNBXlite.MapFeatures==ODNBXlite.FEATURES_CLASSIC){
                if(x==0 || x==ODNBXlite.IndevWidthX-1 || z==0 || z==ODNBXlite.IndevWidthZ-1){
                    if(y<ODNBXlite.SurrWaterHeight && y>=ODNBXlite.SurrGroundHeight){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void commandSetTime(long par1)
    {
        long l = par1 - worldInfo.getWorldTime();

        for (Iterator iterator = scheduledTickSet.iterator(); iterator.hasNext();)
        {
            NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();
            nextticklistentry.scheduledTime += l;
        }

        setWorldTime(par1);
    }

    /**
     * Returns the block ID at coords x,y,z
     */
    public int getBlockId(int par1, int par2, int par3)
    {
        if (isBounds(par1, par2, par3)){
            return ODNBXlite.getBlockIdInBounds(par2);
        }
        if (par1 < 0xfe363c80 || par3 < 0xfe363c80 || par1 >= 0x1c9c380 || par3 >= 0x1c9c380)
        {
            return 0;
        }

        if (par2 < 0)
        {
            return 0;
        }

        if (par2 >= 256)
        {
            return 0;
        }
        else
        {
            return getChunkFromChunkCoords(par1 >> 4, par3 >> 4).getBlockID(par1 & 0xf, par2, par3 & 0xf);
        }
    }

    public int func_48462_d(int par1, int par2, int par3)
    {
        if (par1 < 0xfe363c80 || par3 < 0xfe363c80 || par1 >= 0x1c9c380 || par3 >= 0x1c9c380)
        {
            return 0;
        }

        if (par2 < 0)
        {
            return 0;
        }

        if (par2 >= 256)
        {
            return 0;
        }
        else
        {
            return getChunkFromChunkCoords(par1 >> 4, par3 >> 4).getBlockLightOpacity(par1 & 0xf, par2, par3 & 0xf);
        }
    }

    /**
     * Returns true if the block at the specified coordinates is empty
     */
    public boolean isAirBlock(int par1, int par2, int par3)
    {
        return getBlockId(par1, par2, par3) == 0;
    }

    /**
     * Returns whether a block exists at world coordinates x, y, z
     */
    public boolean blockExists(int par1, int par2, int par3)
    {
        if (par2 < 0 || par2 >= 256)
        {
            return false;
        }
        else
        {
            return chunkExists(par1 >> 4, par3 >> 4);
        }
    }

    /**
     * Checks if any of the chunks within distance (argument 4) blocks of the given block exist
     */
    public boolean doChunksNearChunkExist(int par1, int par2, int par3, int par4)
    {
        return checkChunksExist(par1 - par4, par2 - par4, par3 - par4, par1 + par4, par2 + par4, par3 + par4);
    }

    /**
     * Checks between a min and max all the chunks inbetween actually exist. Args: minX, minY, minZ, maxX, maxY, maxZ
     */
    public boolean checkChunksExist(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        if (par5 < 0 || par2 >= 256)
        {
            return false;
        }

        par1 >>= 4;
        par3 >>= 4;
        par4 >>= 4;
        par6 >>= 4;

        for (int i = par1; i <= par4; i++)
        {
            for (int j = par3; j <= par6; j++)
            {
                if (!chunkExists(i, j))
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns whether a chunk exists at chunk coordinates x, y
     */
    protected boolean chunkExists(int par1, int par2)
    {
        return chunkProvider.chunkExists(par1, par2);
    }

    /**
     * Returns a chunk looked up by block coordinates. Args: x, z
     */
    public Chunk getChunkFromBlockCoords(int par1, int par2)
    {
        return getChunkFromChunkCoords(par1 >> 4, par2 >> 4);
    }

    /**
     * Returns back a chunk looked up by chunk coordinates Args: x, y
     */
    public Chunk getChunkFromChunkCoords(int par1, int par2)
    {
        return chunkProvider.provideChunk(par1, par2);
    }

    public boolean func_72930_a(int par1, int par2, int par3, int par4, int par5, boolean par6)
    {
        if (isBounds(par1, par2, par3)){
            return false;
        }
        return super.func_72930_a(par1, par2, par3, par4, par5, par6);
    }

    /**
     * Sets the block to the specified blockID at the block coordinates Args x, y, z, blockID
     */
    public boolean setBlock(int par1, int par2, int par3, int par4)
    {
        if (isBounds(par1, par2, par3)){
            return false;
        }
        if (isBounds2(par1, par2, par3) && par4==0){
            par4 = ODNBXlite.SurrWaterType;
        }
        if (par1 < 0xfe363c80 || par3 < 0xfe363c80 || par1 >= 0x1c9c380 || par3 >= 0x1c9c380)
        {
            return false;
        }

        if (par2 < 0)
        {
            return false;
        }

        if (par2 >= 256)
        {
            return false;
        }
        else
        {
            Chunk chunk = getChunkFromChunkCoords(par1 >> 4, par3 >> 4);
            boolean flag = chunk.setBlockID(par1 & 0xf, par2, par3 & 0xf, par4);
            field_72984_F.startSection("checkLight");
            updateAllLightTypes(par1, par2, par3);
            field_72984_F.endSection();
            return flag;
        }
    }

    /**
     * Returns the block's material.
     */
    public Material getBlockMaterial(int par1, int par2, int par3)
    {
        int i = getBlockId(par1, par2, par3);

        if (i == 0)
        {
            return Material.air;
        }
        else
        {
            return Block.blocksList[i].blockMaterial;
        }
    }

    /**
     * Returns the block metadata at coords x,y,z
     */
    public int getBlockMetadata(int par1, int par2, int par3)
    {
        if (par1 < 0xfe363c80 || par3 < 0xfe363c80 || par1 >= 0x1c9c380 || par3 >= 0x1c9c380)
        {
            return 0;
        }

        if (par2 < 0)
        {
            return 0;
        }

        if (par2 >= 256)
        {
            return 0;
        }
        else
        {
            Chunk chunk = getChunkFromChunkCoords(par1 >> 4, par3 >> 4);
            par1 &= 0xf;
            par3 &= 0xf;
            return chunk.getBlockMetadata(par1, par2, par3);
        }
    }

    /**
     * Sets the blocks metadata and if set will then notify blocks that this block changed. Args: x, y, z, metadata
     */
    public void setBlockMetadataWithNotify(int par1, int par2, int par3, int par4)
    {
        if (setBlockMetadata(par1, par2, par3, par4))
        {
            int i = getBlockId(par1, par2, par3);

            if (Block.requiresSelfNotify[i & 0xfff])
            {
                notifyBlockChange(par1, par2, par3, i);
            }
            else
            {
                notifyBlocksOfNeighborChange(par1, par2, par3, i);
            }
        }
    }

    /**
     * Set the metadata of a block in global coordinates
     */
    public boolean setBlockMetadata(int par1, int par2, int par3, int par4)
    {
        if (par1 < 0xfe363c80 || par3 < 0xfe363c80 || par1 >= 0x1c9c380 || par3 >= 0x1c9c380)
        {
            return false;
        }

        if (par2 < 0)
        {
            return false;
        }

        if (par2 >= 256)
        {
            return false;
        }
        else
        {
            Chunk chunk = getChunkFromChunkCoords(par1 >> 4, par3 >> 4);
            par1 &= 0xf;
            par3 &= 0xf;
            return chunk.setBlockMetadata(par1, par2, par3, par4);
        }
    }

    /**
     * Sets a block and notifies relevant systems with the block change  Args: x, y, z, blockID
     */
    public boolean setBlockWithNotify(int par1, int par2, int par3, int par4)
    {
        if (setBlock(par1, par2, par3, par4))
        {
            notifyBlockChange(par1, par2, par3, par4);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Sets the block ID and metadata, then notifies neighboring blocks of the change Params: x, y, z, BlockID, Metadata
     */
    public boolean setBlockAndMetadataWithNotify(int par1, int par2, int par3, int par4, int par5)
    {
        if (setBlockAndMetadata(par1, par2, par3, par4, par5))
        {
            notifyBlockChange(par1, par2, par3, par4);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Marks the block as needing an update with the renderer. Args: x, y, z
     */
    public void markBlockNeedsUpdate(int par1, int par2, int par3)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).markBlockNeedsUpdate(par1, par2, par3);
        }
    }

    /**
     * The block type change and need to notify other systems  Args: x, y, z, blockID
     */
    public void notifyBlockChange(int par1, int par2, int par3, int par4)
    {
        markBlockNeedsUpdate(par1, par2, par3);
        notifyBlocksOfNeighborChange(par1, par2, par3, par4);
    }

    /**
     * marks a vertical line of blocks as dirty
     */
    public void markBlocksDirtyVertical(int par1, int par2, int par3, int par4)
    {
        if (par3 > par4)
        {
            int i = par4;
            par4 = par3;
            par3 = i;
        }

        if (!worldProvider.hasNoSky)
        {
            for (int j = par3; j <= par4; j++)
            {
                updateLightByType(EnumSkyBlock.Sky, par1, j, par2);
            }
        }

        markBlocksDirty(par1, par3, par2, par1, par4, par2);
    }

    /**
     * calls the 'MarkBlockAsNeedsUpdate' in all block accesses in this world
     */
    public void markBlockAsNeedsUpdate(int par1, int par2, int par3)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).markBlockRangeNeedsUpdate(par1, par2, par3, par1, par2, par3);
        }
    }

    public void markBlocksDirty(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).markBlockRangeNeedsUpdate(par1, par2, par3, par4, par5, par6);
        }
    }

    /**
     * Notifies neighboring blocks that this specified block changed  Args: x, y, z, blockID
     */
    public void notifyBlocksOfNeighborChange(int par1, int par2, int par3, int par4)
    {
        notifyBlockOfNeighborChange(par1 - 1, par2, par3, par4);
        notifyBlockOfNeighborChange(par1 + 1, par2, par3, par4);
        notifyBlockOfNeighborChange(par1, par2 - 1, par3, par4);
        notifyBlockOfNeighborChange(par1, par2 + 1, par3, par4);
        notifyBlockOfNeighborChange(par1, par2, par3 - 1, par4);
        notifyBlockOfNeighborChange(par1, par2, par3 + 1, par4);
    }

    /**
     * Notifies a block that one of its neighbor change to the specified type Args: x, y, z, blockID
     */
    private void notifyBlockOfNeighborChange(int par1, int par2, int par3, int par4)
    {
        if (editingBlocks || isRemote)
        {
            return;
        }

        Block block = Block.blocksList[getBlockId(par1, par2, par3)];

        if (block != null)
        {
            block.onNeighborBlockChange(this, par1, par2, par3, par4);
        }
    }

    /**
     * Checks if the specified block is able to see the sky
     */
    public boolean canBlockSeeTheSky(int par1, int par2, int par3)
    {
        return getChunkFromChunkCoords(par1 >> 4, par3 >> 4).canBlockSeeTheSky(par1 & 0xf, par2, par3 & 0xf);
    }

    /**
     * Does the same as getBlockLightValue_do but without checking if its not a normal block
     */
    public int getFullBlockLightValue(int par1, int par2, int par3)
    {
        if (par2 < 0)
        {
            return 0;
        }

        if (par2 >= 256)
        {
            par2 = 255;
        }

        return getChunkFromChunkCoords(par1 >> 4, par3 >> 4).getBlockLightValue(par1 & 0xf, par2, par3 & 0xf, 0);
    }

    /**
     * Gets the light value of a block location
     */
    public int getBlockLightValue(int par1, int par2, int par3)
    {
        return getBlockLightValue_do(par1, par2, par3, true);
    }

    /**
     * Gets the light value of a block location. This is the actual function that gets the value and has a bool flag
     * that indicates if its a half step block to get the maximum light value of a direct neighboring block (left,
     * right, forward, back, and up)
     */
    public int getBlockLightValue_do(int par1, int par2, int par3, boolean par4)
    {
        if (par1 < 0xfe363c80 || par3 < 0xfe363c80 || par1 >= 0x1c9c380 || par3 >= 0x1c9c380)
        {
            return 15;
        }

        if (par4)
        {
            int i = getBlockId(par1, par2, par3);

            if (i == Block.field_72079_ak.blockID || i == Block.field_72092_bO.blockID || i == Block.tilledField.blockID || i == Block.stairCompactCobblestone.blockID || i == Block.stairCompactPlanks.blockID)
            {
                int j = getBlockLightValue_do(par1, par2 + 1, par3, false);
                int k = getBlockLightValue_do(par1 + 1, par2, par3, false);
                int l = getBlockLightValue_do(par1 - 1, par2, par3, false);
                int i1 = getBlockLightValue_do(par1, par2, par3 + 1, false);
                int j1 = getBlockLightValue_do(par1, par2, par3 - 1, false);

                if (k > j)
                {
                    j = k;
                }

                if (l > j)
                {
                    j = l;
                }

                if (i1 > j)
                {
                    j = i1;
                }

                if (j1 > j)
                {
                    j = j1;
                }

                return j;
            }
        }

        if (par2 < 0)
        {
            return 0;
        }

        if (par2 >= 256)
        {
            par2 = 255;
        }

        Chunk chunk = getChunkFromChunkCoords(par1 >> 4, par3 >> 4);
        par1 &= 0xf;
        par3 &= 0xf;
        return chunk.getBlockLightValue(par1, par2, par3, skylightSubtracted);
    }

    /**
     * Returns the y coordinate with a block in it at this x, z coordinate
     */
    public int getHeightValue(int par1, int par2)
    {
        if (par1 < 0xfe363c80 || par2 < 0xfe363c80 || par1 >= 0x1c9c380 || par2 >= 0x1c9c380)
        {
            return 0;
        }

        if (!chunkExists(par1 >> 4, par2 >> 4))
        {
            return 0;
        }
        else
        {
            Chunk chunk = getChunkFromChunkCoords(par1 >> 4, par2 >> 4);
            return chunk.getHeightValue(par1 & 0xf, par2 & 0xf);
        }
    }

    /**
     * Brightness for SkyBlock.Sky is clear white and (through color computing it is assumed) DEPENDENT ON DAYTIME.
     * Brightness for SkyBlock.Block is yellowish and independent.
     */
    public int getSkyBlockTypeBrightness(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4)
    {
        if (worldProvider.hasNoSky && par1EnumSkyBlock == EnumSkyBlock.Sky)
        {
            return 0;
        }

        if (par3 < 0)
        {
            par3 = 0;
        }

        if (par3 >= 256)
        {
            return par1EnumSkyBlock.defaultLightValue;
        }

        if (par2 < 0xfe363c80 || par4 < 0xfe363c80 || par2 >= 0x1c9c380 || par4 >= 0x1c9c380)
        {
            return par1EnumSkyBlock.defaultLightValue;
        }

        int i = par2 >> 4;
        int j = par4 >> 4;

        if (!chunkExists(i, j))
        {
            return par1EnumSkyBlock.defaultLightValue;
        }

        if (Block.useNeighborBrightness[getBlockId(par2, par3, par4)])
        {
            int k = getSavedLightValue(par1EnumSkyBlock, par2, par3 + 1, par4);
            int l = getSavedLightValue(par1EnumSkyBlock, par2 + 1, par3, par4);
            int i1 = getSavedLightValue(par1EnumSkyBlock, par2 - 1, par3, par4);
            int j1 = getSavedLightValue(par1EnumSkyBlock, par2, par3, par4 + 1);
            int k1 = getSavedLightValue(par1EnumSkyBlock, par2, par3, par4 - 1);

            if (l > k)
            {
                k = l;
            }

            if (i1 > k)
            {
                k = i1;
            }

            if (j1 > k)
            {
                k = j1;
            }

            if (k1 > k)
            {
                k = k1;
            }

            return k;
        }
        else
        {
            Chunk chunk = getChunkFromChunkCoords(i, j);
            return chunk.getSavedLightValue(par1EnumSkyBlock, par2 & 0xf, par3, par4 & 0xf);
        }
    }

    /**
     * Returns saved light value without taking into account the time of day.  Either looks in the sky light map or
     * block light map based on the enumSkyBlock arg.
     */
    public int getSavedLightValue(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4)
    {
        if (par3 < 0)
        {
            par3 = 0;
        }

        if (par3 >= 256)
        {
            par3 = 255;
        }

        if (par2 < 0xfe363c80 || par4 < 0xfe363c80 || par2 >= 0x1c9c380 || par4 >= 0x1c9c380)
        {
            return par1EnumSkyBlock.defaultLightValue;
        }

        int i = par2 >> 4;
        int j = par4 >> 4;

        if (!chunkExists(i, j))
        {
            return par1EnumSkyBlock.defaultLightValue;
        }
        else
        {
            Chunk chunk = getChunkFromChunkCoords(i, j);
            return chunk.getSavedLightValue(par1EnumSkyBlock, par2 & 0xf, par3, par4 & 0xf);
        }
    }

    /**
     * Sets the light value either into the sky map or block map depending on if enumSkyBlock is set to sky or block.
     * Args: enumSkyBlock, x, y, z, lightValue
     */
    public void setLightValue(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4, int par5)
    {
        if (par2 < 0xfe363c80 || par4 < 0xfe363c80 || par2 >= 0x1c9c380 || par4 >= 0x1c9c380)
        {
            return;
        }

        if (par3 < 0)
        {
            return;
        }

        if (par3 >= 256)
        {
            return;
        }

        if (!chunkExists(par2 >> 4, par4 >> 4))
        {
            return;
        }

        Chunk chunk = getChunkFromChunkCoords(par2 >> 4, par4 >> 4);
        chunk.setLightValue(par1EnumSkyBlock, par2 & 0xf, par3, par4 & 0xf, par5);

        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).markBlockNeedsUpdate2(par2, par3, par4);
        }
    }

    public void func_48464_p(int par1, int par2, int par3)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).markBlockNeedsUpdate2(par1, par2, par3);
        }
    }

    /**
     * Any Light rendered on a 1.8 Block goes through here
     */
    public int getLightBrightnessForSkyBlocks(int par1, int par2, int par3, int par4)
    {
        if (isBounds(par1, par2, par3)){
            return ODNBXlite.getLightInBounds(par2);
        }
        int i = getSkyBlockTypeBrightness(EnumSkyBlock.Sky, par1, par2, par3);
        int j = getSkyBlockTypeBrightness(EnumSkyBlock.Block, par1, par2, par3);

        if (j < par4)
        {
            j = par4;
        }

        return i << 20 | j << 4;
    }

    public float getBrightness(int par1, int par2, int par3, int par4)
    {
        int i = getBlockLightValue(par1, par2, par3);

        if (i < par4)
        {
            i = par4;
        }

        return worldProvider.lightBrightnessTable[i];
    }

    /**
     * Returns how bright the block is shown as which is the block's light value looked up in a lookup table (light
     * values aren't linear for brightness). Args: x, y, z
     */
    public float getLightBrightness(int par1, int par2, int par3)
    {
        return worldProvider.lightBrightnessTable[getBlockLightValue(par1, par2, par3)];
    }

    /**
     * Checks whether its daytime by seeing if the light subtracted from the skylight is less than 4
     */
    public boolean isDaytime()
    {
        return skylightSubtracted < 4;
    }

    /**
     * ray traces all blocks, including non-collideable ones
     */
    public MovingObjectPosition rayTraceBlocks(Vec3 par1Vec3, Vec3 par2Vec3)
    {
        return rayTraceBlocks_do_do(par1Vec3, par2Vec3, false, false);
    }

    public MovingObjectPosition rayTraceBlocks_do(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3)
    {
        return rayTraceBlocks_do_do(par1Vec3, par2Vec3, par3, false);
    }

    public MovingObjectPosition rayTraceBlocks_do_do(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3, boolean par4)
    {
        if (Double.isNaN(par1Vec3.xCoord) || Double.isNaN(par1Vec3.yCoord) || Double.isNaN(par1Vec3.zCoord))
        {
            return null;
        }

        if (Double.isNaN(par2Vec3.xCoord) || Double.isNaN(par2Vec3.yCoord) || Double.isNaN(par2Vec3.zCoord))
        {
            return null;
        }

        int i = MathHelper.floor_double(par2Vec3.xCoord);
        int j = MathHelper.floor_double(par2Vec3.yCoord);
        int k = MathHelper.floor_double(par2Vec3.zCoord);
        int l = MathHelper.floor_double(par1Vec3.xCoord);
        int i1 = MathHelper.floor_double(par1Vec3.yCoord);
        int j1 = MathHelper.floor_double(par1Vec3.zCoord);
        int k1 = getBlockId(l, i1, j1);
        int i2 = getBlockMetadata(l, i1, j1);
        Block block = Block.blocksList[k1];

        if ((!par4 || block == null || block.getCollisionBoundingBoxFromPool(this, l, i1, j1) != null) && k1 > 0 && block.canCollideCheck(i2, par3))
        {
            MovingObjectPosition movingobjectposition = block.collisionRayTrace(this, l, i1, j1, par1Vec3, par2Vec3);

            if (movingobjectposition != null)
            {
                return movingobjectposition;
            }
        }

        for (int l1 = 200; l1-- >= 0;)
        {
            if (Double.isNaN(par1Vec3.xCoord) || Double.isNaN(par1Vec3.yCoord) || Double.isNaN(par1Vec3.zCoord))
            {
                return null;
            }

            if (l == i && i1 == j && j1 == k)
            {
                return null;
            }

            boolean flag = true;
            boolean flag1 = true;
            boolean flag2 = true;
            double d = 999D;
            double d1 = 999D;
            double d2 = 999D;

            if (i > l)
            {
                d = (double)l + 1.0D;
            }
            else if (i < l)
            {
                d = (double)l + 0.0D;
            }
            else
            {
                flag = false;
            }

            if (j > i1)
            {
                d1 = (double)i1 + 1.0D;
            }
            else if (j < i1)
            {
                d1 = (double)i1 + 0.0D;
            }
            else
            {
                flag1 = false;
            }

            if (k > j1)
            {
                d2 = (double)j1 + 1.0D;
            }
            else if (k < j1)
            {
                d2 = (double)j1 + 0.0D;
            }
            else
            {
                flag2 = false;
            }

            double d3 = 999D;
            double d4 = 999D;
            double d5 = 999D;
            double d6 = par2Vec3.xCoord - par1Vec3.xCoord;
            double d7 = par2Vec3.yCoord - par1Vec3.yCoord;
            double d8 = par2Vec3.zCoord - par1Vec3.zCoord;

            if (flag)
            {
                d3 = (d - par1Vec3.xCoord) / d6;
            }

            if (flag1)
            {
                d4 = (d1 - par1Vec3.yCoord) / d7;
            }

            if (flag2)
            {
                d5 = (d2 - par1Vec3.zCoord) / d8;
            }

            byte byte0 = 0;

            if (d3 < d4 && d3 < d5)
            {
                if (i > l)
                {
                    byte0 = 4;
                }
                else
                {
                    byte0 = 5;
                }

                par1Vec3.xCoord = d;
                par1Vec3.yCoord += d7 * d3;
                par1Vec3.zCoord += d8 * d3;
            }
            else if (d4 < d5)
            {
                if (j > i1)
                {
                    byte0 = 0;
                }
                else
                {
                    byte0 = 1;
                }

                par1Vec3.xCoord += d6 * d4;
                par1Vec3.yCoord = d1;
                par1Vec3.zCoord += d8 * d4;
            }
            else
            {
                if (k > j1)
                {
                    byte0 = 2;
                }
                else
                {
                    byte0 = 3;
                }

                par1Vec3.xCoord += d6 * d5;
                par1Vec3.yCoord += d7 * d5;
                par1Vec3.zCoord = d2;
            }

            Vec3 vec3d = Vec3.createVectorHelper(par1Vec3.xCoord, par1Vec3.yCoord, par1Vec3.zCoord);
            l = (int)(vec3d.xCoord = MathHelper.floor_double(par1Vec3.xCoord));

            if (byte0 == 5)
            {
                l--;
                vec3d.xCoord++;
            }

            i1 = (int)(vec3d.yCoord = MathHelper.floor_double(par1Vec3.yCoord));

            if (byte0 == 1)
            {
                i1--;
                vec3d.yCoord++;
            }

            j1 = (int)(vec3d.zCoord = MathHelper.floor_double(par1Vec3.zCoord));

            if (byte0 == 3)
            {
                j1--;
                vec3d.zCoord++;
            }

            int j2 = getBlockId(l, i1, j1);
            int k2 = getBlockMetadata(l, i1, j1);
            Block block1 = Block.blocksList[j2];

            if ((!par4 || block1 == null || block1.getCollisionBoundingBoxFromPool(this, l, i1, j1) != null) && j2 > 0 && block1.canCollideCheck(k2, par3))
            {
                MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(this, l, i1, j1, par1Vec3, par2Vec3);

                if (movingobjectposition1 != null)
                {
                    return movingobjectposition1;
                }
            }
        }

        return null;
    }

    /**
     * Plays a sound at the entity's position. Args: entity, sound, unknown1, volume (relative to 1.0)
     */
    public void playSoundAtEntity(Entity par1Entity, String par2Str, float par3, float par4)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).playSound(par2Str, par1Entity.posX, par1Entity.posY - (double)par1Entity.yOffset, par1Entity.posZ, par3, par4);
        }
    }

    /**
     * Play a sound effect. Many many parameters for this function. Not sure what they do, but a classic call is :
     * (double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D, 'random.door_open', 1.0F, world.rand.nextFloat() * 0.1F +
     * 0.9F with i,j,k position of the block.
     */
    public void playSoundEffect(double par1, double par3, double par5, String par7Str, float par8, float par9)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).playSound(par7Str, par1, par3, par5, par8, par9);
        }
    }

    /**
     * Plays a record at the specified coordinates of the specified name. Args: recordName, x, y, z
     */
    public void playRecord(String par1Str, int par2, int par3, int par4)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).playRecord(par1Str, par2, par3, par4);
        }
    }

    /**
     * Spawns a particle.  Args particleName, x, y, z, velX, velY, velZ
     */
    public void spawnParticle(String par1Str, double par2, double par4, double par6, double par8, double par10, double par12)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).spawnParticle(par1Str, par2, par4, par6, par8, par10, par12);
        }
    }

    /**
     * adds a lightning bolt to the list of lightning bolts in this world.
     */
    public boolean addWeatherEffect(Entity par1Entity)
    {
        weatherEffects.add(par1Entity);
        return true;
    }

    /**
     * Called to place all entities as part of a world
     */
    public boolean spawnEntityInWorld(Entity par1Entity)
    {
        int i = MathHelper.floor_double(par1Entity.posX / 16D);
        int j = MathHelper.floor_double(par1Entity.posZ / 16D);
        boolean flag = false;

        if (par1Entity instanceof EntityPlayer)
        {
            flag = true;
        }

        if (flag || chunkExists(i, j))
        {
            if (par1Entity instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)par1Entity;
                playerEntities.add(entityplayer);
                updateAllPlayersSleepingFlag();
            }

            getChunkFromChunkCoords(i, j).addEntity(par1Entity);
            loadedEntityList.add(par1Entity);
            obtainEntitySkin(par1Entity);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Start the skin for this entity downloading, if necessary, and increment its reference counter
     */
    protected void obtainEntitySkin(Entity par1Entity)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).obtainEntitySkin(par1Entity);
        }
    }

    /**
     * Decrement the reference counter for this entity's skin image data
     */
    protected void releaseEntitySkin(Entity par1Entity)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).releaseEntitySkin(par1Entity);
        }
    }

    /**
     * Dismounts the entity (and anything riding the entity), sets the dead flag, and removes the player entity from the
     * player entity list. Called by the playerLoggedOut function.
     */
    public void setEntityDead(Entity par1Entity)
    {
        if (par1Entity.riddenByEntity != null)
        {
            par1Entity.riddenByEntity.mountEntity(null);
        }

        if (par1Entity.ridingEntity != null)
        {
            par1Entity.mountEntity(null);
        }

        par1Entity.setDead();

        if (par1Entity instanceof EntityPlayer)
        {
            playerEntities.remove((EntityPlayer)par1Entity);
            updateAllPlayersSleepingFlag();
        }
    }

    /**
     * Adds a IWorldAccess to the list of worldAccesses
     */
    public void addWorldAccess(IWorldAccess par1IWorldAccess)
    {
        worldAccesses.add(par1IWorldAccess);
    }

    /**
     * Removes a worldAccess from the worldAccesses object
     */
    public void removeWorldAccess(IWorldAccess par1IWorldAccess)
    {
        worldAccesses.remove(par1IWorldAccess);
    }

    /**
     * Returns a list of bounding boxes that collide with aabb excluding the passed in entity's collision. Args: entity,
     * aabb
     */
    public List getCollidingBoundingBoxes(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
        collidingBoundingBoxes.clear();
        int i = MathHelper.floor_double(par2AxisAlignedBB.minX);
        int j = MathHelper.floor_double(par2AxisAlignedBB.maxX + 1.0D);
        int k = MathHelper.floor_double(par2AxisAlignedBB.minY);
        int l = MathHelper.floor_double(par2AxisAlignedBB.maxY + 1.0D);
        int i1 = MathHelper.floor_double(par2AxisAlignedBB.minZ);
        int j1 = MathHelper.floor_double(par2AxisAlignedBB.maxZ + 1.0D);

        for (int k1 = i; k1 < j; k1++)
        {
            for (int l1 = i1; l1 < j1; l1++)
            {
                if (!blockExists(k1, 64, l1))
                {
                    continue;
                }

                for (int i2 = k - 1; i2 < l; i2++)
                {
                    Block block = Block.blocksList[getBlockId(k1, i2, l1)];

                    if (block != null)
                    {
                        block.func_71871_a(this, k1, i2, l1, par2AxisAlignedBB, collidingBoundingBoxes, par1Entity);
                    }
                }
            }
        }

        double d = 0.25D;
        List list = getEntitiesWithinAABBExcludingEntity(par1Entity, par2AxisAlignedBB.expand(d, d, d));

        for (int j2 = 0; j2 < list.size(); j2++)
        {
            AxisAlignedBB axisalignedbb = ((Entity)list.get(j2)).getBoundingBox();

            if (axisalignedbb != null && axisalignedbb.intersectsWith(par2AxisAlignedBB))
            {
                collidingBoundingBoxes.add(axisalignedbb);
            }

            axisalignedbb = par1Entity.getCollisionBox((Entity)list.get(j2));

            if (axisalignedbb != null && axisalignedbb.intersectsWith(par2AxisAlignedBB))
            {
                collidingBoundingBoxes.add(axisalignedbb);
            }
        }

        return collidingBoundingBoxes;
    }

    /**
     * Returns the amount of skylight subtracted for the current time
     */
    public int calculateSkylightSubtracted(float f)
    {
        int brightness = 0;
        if (ODNBXlite.SkyBrightness == -1){
            brightness = ODNBXlite.getSkyBrightness(ODNBXlite.MapTheme);
        }else{
            brightness = ODNBXlite.SkyBrightness;
        }
        float f1 = (float)brightness;
        if(f1 == 16 || (worldProvider instanceof WorldProviderEnd) || ODNBXlite.DayNight==0)
        {
            f1 = 15F;
        }
        float f2 = getCelestialAngle(f);
        float f3 = 1.0F - (MathHelper.cos(f2 * 3.141593F * 2.0F) * 2.0F + 0.5F);
        if(f3 < 0.0F)
        {
            f3 = 0.0F;
        }
        if(f3 > 1.0F)
        {
            f3 = 1.0F;
        }
        f3 = 1.0F - f3;
        f3 = (float)((double)f3 * (1.0D - (double)(getRainStrength(f) * 5F) / 16D));
        f3 = (float)((double)f3 * (1.0D - (double)(getWeightedThunderStrength(f) * 5F) / 16D));
        f3 = 1.0F - f3;
        return (int)(f3 * (f1 - 4F) + (15F - f1));
    }

    public float func_72971_b(float par1)
    {
        float f = getCelestialAngle(par1);
        int brightness = 0;
        if (ODNBXlite.SkyBrightness == -1){
            brightness = ODNBXlite.getSkyBrightness(ODNBXlite.MapTheme);
        }else{
            brightness = ODNBXlite.SkyBrightness;
        }
        float f1 = (Math.min(brightness+1, 16F) / 16F) - (MathHelper.cos(f * (float)Math.PI * 2.0F) * 2.0F + 0.2F);

        if (f1 < 0.0F)
        {
            f1 = 0.0F;
        }

        if (f1 > 1.0F)
        {
            f1 = 1.0F;
        }

        f1 = (Math.min(brightness+1, 16F) / 16F) - f1;
        f1 = (float)((double)f1 * (((double)((Math.min(brightness+1, 16F) / 16F))) - (double)(getRainStrength(par1) * 5F) / 16D));
        f1 = (float)((double)f1 * (((double)((Math.min(brightness+1, 16F) / 16F))) - (double)(getWeightedThunderStrength(par1) * 5F) / 16D));
        return f1 * 0.8F + 0.2F;
    }

    /**
     * Calculates the color for the skybox
     */
    public Vec3 getSkyColor(Entity entity, float f)
    {
        if(true)
        {
            float f1 = getCelestialAngle(f);
            float f3 = MathHelper.cos(f1 * 3.141593F * 2.0F) * 2.0F + 0.5F;
            if(f3 < 0.0F)
            {
                f3 = 0.0F;
            }
            if(f3 > 1.0F)
            {
                f3 = 1.0F;
            }
            int i = MathHelper.floor_double(entity.posX);
            int j = MathHelper.floor_double(entity.posZ);
            float f7;
            int k;
            if (ODNBXlite.SkyColor==0){
                if (ODNBXlite.Generator == ODNBXlite.GEN_NEWBIOMES || worldProvider.worldType != 0){
                    BiomeGenBase biomegenbase = getBiomeGenForCoords(i, j);
                    if (worldProvider.worldType==0 && ODNBXlite.MapFeatures<ODNBXlite.FEATURES_12){
                        f7 = 0.2146759F;
                    }else{
                        f7 = biomegenbase.getFloatTemperature();
                    }
                    k = biomegenbase.getSkyColorByTemp(f7);
                }else if (ODNBXlite.Generator == ODNBXlite.GEN_OLDBIOMES && ODNBXlite.MapFeatures!=ODNBXlite.FEATURES_SKY){
                    f7 = (float)getWorldChunkManager().getTemperature_old(i, j);
                    k = getWorldChunkManager().oldGetBiomeGenAt(i, j).getSkyColorByTemp(f7);
                }else{
                    k = ODNBXlite.getSkyColor(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, 0);
                }
            }else{
                k = ODNBXlite.SkyColor;
            }
            float f9 = (float)(k >> 16 & 0xff) / 255F;
            float f10 = (float)(k >> 8 & 0xff) / 255F;
            float f11 = (float)(k & 0xff) / 255F;
            f9 *= f3;
            f10 *= f3;
            f11 *= f3;
            float f12 = getRainStrength(f);
            if(f12 > 0.0F)
            {
                float f13 = (f9 * 0.3F + f10 * 0.59F + f11 * 0.11F) * 0.6F;
                float f15 = 1.0F - f12 * 0.75F;
                f9 = f9 * f15 + f13 * (1.0F - f15);
                f10 = f10 * f15 + f13 * (1.0F - f15);
                f11 = f11 * f15 + f13 * (1.0F - f15);
            }
            float f14 = getWeightedThunderStrength(f);
            if(f14 > 0.0F)
            {
                float f16 = (f9 * 0.3F + f10 * 0.59F + f11 * 0.11F) * 0.2F;
                float f18 = 1.0F - f14 * 0.75F;
                f9 = f9 * f18 + f16 * (1.0F - f18);
                f10 = f10 * f18 + f16 * (1.0F - f18);
                f11 = f11 * f18 + f16 * (1.0F - f18);
            }
            if(lightningFlash > 0)
            {
                float f17 = (float)lightningFlash - f;
                if(f17 > 1.0F)
                {
                    f17 = 1.0F;
                }
                f17 *= 0.45F;
                f9 = f9 * (1.0F - f17) + 0.8F * f17;
                f10 = f10 * (1.0F - f17) + 0.8F * f17;
                f11 = f11 * (1.0F - f17) + 1.0F * f17;
            }
            return Vec3.func_72437_a().func_72345_a(f9, f10, f11);
        }
        float f2 = getCelestialAngle(f);
        float f4 = MathHelper.cos(f2 * 3.141593F * 2.0F) * 2.0F + 0.5F;
        if(f4 < 0.0F)
        {
            f4 = 0.0F;
        }
        if(f4 > 1.0F)
        {
            f4 = 1.0F;
        }
        float f5 = (float)(ODNBXlite.SkyColor >> 16 & 255L) / 255F;
        float f6 = (float)(ODNBXlite.SkyColor >> 8 & 255L) / 255F;
        float f8 = (float)(ODNBXlite.SkyColor & 255L) / 255F;
        f5 *= f4;
        f6 *= f4;
        f8 *= f4;
        return Vec3.func_72437_a().func_72345_a(f5, f6, f8);
    }

    /**
     * calls calculateCelestialAngle
     */
    public float getCelestialAngle(float par1)
    {
        if(ODNBXlite.Generator==ODNBXlite.GEN_OLDBIOMES && ODNBXlite.MapFeatures==ODNBXlite.FEATURES_SKY && worldProvider.worldType==0){
            return 0.0F;
        }
        if(ODNBXlite.Generator==ODNBXlite.GEN_BIOMELESS && ODNBXlite.MapFeatures==ODNBXlite.FEATURES_INFDEV0227){
            return 1.0F;
        }
        if(ODNBXlite.SkyBrightness == 16 || (ODNBXlite.SkyBrightness == -1 && ODNBXlite.getSkyBrightness(ODNBXlite.MapTheme) == 16)){
            return 1.0F;
        }
        return worldProvider.calculateCelestialAngle(worldInfo.getWorldTime(), par1);
    }

    public int getMoonPhase(float par1)
    {
        return worldProvider.getMoonPhase(worldInfo.getWorldTime(), par1);
    }

    /**
     * Return getCelestialAngle()*2*PI
     */
    public float getCelestialAngleRadians(float par1)
    {
        float f = getCelestialAngle(par1);
        return f * (float)Math.PI * 2.0F;
    }

    public Vec3 drawClouds(float par1)
    {
        int clouds = 0;
        if (ODNBXlite.CloudColor == 0){
            clouds = ODNBXlite.getSkyColor(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, 2);
        }else{
            clouds = ODNBXlite.CloudColor;
        }
        float f = getCelestialAngle(par1);
        float f1 = MathHelper.cos(f * (float)Math.PI * 2.0F) * 2.0F + 0.5F;

        if (f1 < 0.0F)
        {
            f1 = 0.0F;
        }

        if (f1 > 1.0F)
        {
            f1 = 1.0F;
        }

        float f2 = (float)(clouds >> 16 & 255L) / 255F;
        float f3 = (float)(clouds >> 8 & 255L) / 255F;
        float f4 = (float)(clouds & 255L) / 255F;
        float f5 = getRainStrength(par1);

        if (f5 > 0.0F)
        {
            float f6 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.6F;
            float f8 = 1.0F - f5 * 0.95F;
            f2 = f2 * f8 + f6 * (1.0F - f8);
            f3 = f3 * f8 + f6 * (1.0F - f8);
            f4 = f4 * f8 + f6 * (1.0F - f8);
        }

        f2 *= f1 * 0.9F + 0.1F;
        f3 *= f1 * 0.9F + 0.1F;
        f4 *= f1 * 0.85F + 0.15F;
        float f7 = getWeightedThunderStrength(par1);

        if (f7 > 0.0F)
        {
            float f9 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.2F;
            float f10 = 1.0F - f7 * 0.95F;
            f2 = f2 * f10 + f9 * (1.0F - f10);
            f3 = f3 * f10 + f9 * (1.0F - f10);
            f4 = f4 * f10 + f9 * (1.0F - f10);
        }

        return Vec3.func_72437_a().func_72345_a(f2, f3, f4);
    }

    /**
     * Returns vector(ish) with R/G/B for fog
     */
    public Vec3 getFogColor(float par1)
    {
        int fog = 0;
        if (ODNBXlite.FogColor == 0 && worldProvider.worldType == 0){
            fog = ODNBXlite.getSkyColor(ODNBXlite.Generator, ODNBXlite.MapFeatures, ODNBXlite.MapTheme, 1);
        }else{
            fog = ODNBXlite.FogColor;
        }
        if(fog != 0L && worldProvider.worldType == 0){
            float f1 = getCelestialAngle(par1);
            float f2 = MathHelper.cos(f1 * 3.141593F * 2.0F) * 2.0F + 0.5F;
            if(f2 < 0.0F)
            {
                f2 = 0.0F;
            }
            if(f2 > 1.0F)
            {
                f2 = 1.0F;
            }
            float f3 = (float)(fog >> 16 & 255L) / 255F;
            float f4 = (float)(fog >> 8 & 255L) / 255F;
            float f5 = (float)(fog & 255L) / 255F;
            f3 *= f2 * 0.94F + 0.06F;
            f4 *= f2 * 0.94F + 0.06F;
            f5 *= f2 * 0.91F + 0.09F;
            return Vec3.func_72437_a().func_72345_a(f3, f4, f5);
        }
        float f = getCelestialAngle(par1);
        return worldProvider.getFogColor(f, par1);
    }

    /**
     * Gets the height to which rain/snow will fall. Calculates it if not already stored.
     */
    public int getPrecipitationHeight(int par1, int par2)
    {
        return getChunkFromBlockCoords(par1, par2).getPrecipitationHeight(par1 & 0xf, par2 & 0xf);
    }

    /**
     * Finds the highest block on the x, z coordinate that is solid and returns its y coord. Args x, z
     */
    public int getTopSolidOrLiquidBlock(int par1, int par2)
    {
        Chunk chunk = getChunkFromBlockCoords(par1, par2);
        int i = chunk.getTopFilledSegment() + 16;
        par1 &= 0xf;
        par2 &= 0xf;

        while (i > 0)
        {
            int j = chunk.getBlockID(par1, i, par2);

            if (j == 0 || !Block.blocksList[j].blockMaterial.blocksMovement() || Block.blocksList[j].blockMaterial == Material.leaves)
            {
                i--;
            }
            else
            {
                return i + 1;
            }
        }

        return -1;
    }

    /**
     * Schedules a tick to a block with a delay (Most commonly the tick rate)
     */
    public void scheduleBlockUpdate(int par1, int par2, int par3, int par4, int par5)
    {
        NextTickListEntry nextticklistentry = new NextTickListEntry(par1, par2, par3, par4);
        byte byte0 = 8;

        if (scheduledUpdatesAreImmediate)
        {
            if (checkChunksExist(nextticklistentry.xCoord - byte0, nextticklistentry.yCoord - byte0, nextticklistentry.zCoord - byte0, nextticklistentry.xCoord + byte0, nextticklistentry.yCoord + byte0, nextticklistentry.zCoord + byte0))
            {
                int i = getBlockId(nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord);

                if (i == nextticklistentry.blockID && i > 0)
                {
                    Block.blocksList[i].updateTick(this, nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord, rand);
                }
            }

            return;
        }

        if (checkChunksExist(par1 - byte0, par2 - byte0, par3 - byte0, par1 + byte0, par2 + byte0, par3 + byte0))
        {
            if (par4 > 0)
            {
                nextticklistentry.setScheduledTime((long)par5 + worldInfo.getWorldTime());
            }

            if (!scheduledTickSet.contains(nextticklistentry))
            {
                scheduledTickSet.add(nextticklistentry);
                scheduledTickTreeSet.add(nextticklistentry);
            }
        }
    }

    /**
     * Schedules a block update from the saved information in a chunk. Called when the chunk is loaded.
     */
    public void scheduleBlockUpdateFromLoad(int par1, int par2, int par3, int par4, int par5)
    {
        NextTickListEntry nextticklistentry = new NextTickListEntry(par1, par2, par3, par4);

        if (par4 > 0)
        {
            nextticklistentry.setScheduledTime((long)par5 + worldInfo.getWorldTime());
        }

        if (!scheduledTickSet.contains(nextticklistentry))
        {
            scheduledTickSet.add(nextticklistentry);
            scheduledTickTreeSet.add(nextticklistentry);
        }
    }

    /**
     * Will update the entity in the world if the chunk the entity is in is currently loaded or its forced to update.
     * Args: entity, forceUpdate
     */
    public void updateEntityWithOptionalForce(Entity par1Entity, boolean par2)
    {
        int i = MathHelper.floor_double(par1Entity.posX);
        int j = MathHelper.floor_double(par1Entity.posZ);
        byte byte0 = 32;

        if (par2 && !checkChunksExist(i - byte0, 0, j - byte0, i + byte0, 0, j + byte0))
        {
            return;
        }

        par1Entity.lastTickPosX = par1Entity.posX;
        par1Entity.lastTickPosY = par1Entity.posY;
        par1Entity.lastTickPosZ = par1Entity.posZ;
        par1Entity.prevRotationYaw = par1Entity.rotationYaw;
        par1Entity.prevRotationPitch = par1Entity.rotationPitch;

        if (par2 && par1Entity.addedToChunk)
        {
            if (par1Entity.ridingEntity != null)
            {
                par1Entity.updateRidden();
            }
            else
            {
                par1Entity.onUpdate();
            }
        }

        field_72984_F.startSection("chunkCheck");

        if (Double.isNaN(par1Entity.posX) || Double.isInfinite(par1Entity.posX))
        {
            par1Entity.posX = par1Entity.lastTickPosX;
        }

        if (Double.isNaN(par1Entity.posY) || Double.isInfinite(par1Entity.posY))
        {
            par1Entity.posY = par1Entity.lastTickPosY;
        }

        if (Double.isNaN(par1Entity.posZ) || Double.isInfinite(par1Entity.posZ))
        {
            par1Entity.posZ = par1Entity.lastTickPosZ;
        }

        if (Double.isNaN(par1Entity.rotationPitch) || Double.isInfinite(par1Entity.rotationPitch))
        {
            par1Entity.rotationPitch = par1Entity.prevRotationPitch;
        }

        if (Double.isNaN(par1Entity.rotationYaw) || Double.isInfinite(par1Entity.rotationYaw))
        {
            par1Entity.rotationYaw = par1Entity.prevRotationYaw;
        }

        int k = MathHelper.floor_double(par1Entity.posX / 16D);
        int l = MathHelper.floor_double(par1Entity.posY / 16D);
        int i1 = MathHelper.floor_double(par1Entity.posZ / 16D);

        if (!par1Entity.addedToChunk || par1Entity.chunkCoordX != k || par1Entity.chunkCoordY != l || par1Entity.chunkCoordZ != i1)
        {
            if (par1Entity.addedToChunk && chunkExists(par1Entity.chunkCoordX, par1Entity.chunkCoordZ))
            {
                getChunkFromChunkCoords(par1Entity.chunkCoordX, par1Entity.chunkCoordZ).removeEntityAtIndex(par1Entity, par1Entity.chunkCoordY);
            }

            if (chunkExists(k, i1))
            {
                par1Entity.addedToChunk = true;
                getChunkFromChunkCoords(k, i1).addEntity(par1Entity);
            }
            else
            {
                par1Entity.addedToChunk = false;
            }
        }

        field_72984_F.endSection();

        if (par2 && par1Entity.addedToChunk && par1Entity.riddenByEntity != null)
        {
            if (par1Entity.riddenByEntity.isDead || par1Entity.riddenByEntity.ridingEntity != par1Entity)
            {
                par1Entity.riddenByEntity.ridingEntity = null;
                par1Entity.riddenByEntity = null;
            }
            else
            {
                updateEntity(par1Entity.riddenByEntity);
            }
        }
    }

    /**
     * Returns true if there are no solid, live entities in the specified AxisAlignedBB
     */
    public boolean checkIfAABBIsClear(AxisAlignedBB par1AxisAlignedBB)
    {
        List list = getEntitiesWithinAABBExcludingEntity(null, par1AxisAlignedBB);

        for (int i = 0; i < list.size(); i++)
        {
            Entity entity = (Entity)list.get(i);

            if (!entity.isDead && entity.preventEntitySpawning)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns if any of the blocks within the aabb are liquids. Args: aabb
     */
    public boolean isAnyLiquid(AxisAlignedBB par1AxisAlignedBB)
    {
        int i = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int j = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int k = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int l = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int i1 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int j1 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        if (par1AxisAlignedBB.minX < 0.0D)
        {
            i--;
        }

        if (par1AxisAlignedBB.minY < 0.0D)
        {
            k--;
        }

        if (par1AxisAlignedBB.minZ < 0.0D)
        {
            i1--;
        }

        for (int k1 = i; k1 < j; k1++)
        {
            for (int l1 = k; l1 < l; l1++)
            {
                for (int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.blocksList[getBlockId(k1, l1, i2)];

                    if (block != null && block.blockMaterial.isLiquid())
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns whether or not the given bounding box is on fire or not
     */
    public boolean isBoundingBoxBurning(AxisAlignedBB par1AxisAlignedBB)
    {
        int i = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int j = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int k = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int l = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int i1 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int j1 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        if (checkChunksExist(i, k, i1, j, l, j1))
        {
            for (int k1 = i; k1 < j; k1++)
            {
                for (int l1 = k; l1 < l; l1++)
                {
                    for (int i2 = i1; i2 < j1; i2++)
                    {
                        int j2 = getBlockId(k1, l1, i2);

                        if (j2 == Block.fire.blockID || j2 == Block.lavaMoving.blockID || j2 == Block.lavaStill.blockID)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * handles the acceleration of an object whilst in water. Not sure if it is used elsewhere.
     */
    public boolean handleMaterialAcceleration(AxisAlignedBB par1AxisAlignedBB, Material par2Material, Entity par3Entity)
    {
        int i = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int j = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int k = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int l = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int i1 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int j1 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        if (!checkChunksExist(i, k, i1, j, l, j1))
        {
            return false;
        }

        boolean flag = false;
        Vec3 vec3d = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);

        for (int k1 = i; k1 < j; k1++)
        {
            for (int l1 = k; l1 < l; l1++)
            {
                for (int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.blocksList[getBlockId(k1, l1, i2)];

                    if (block == null || block.blockMaterial != par2Material)
                    {
                        continue;
                    }

                    double d1 = (float)(l1 + 1) - BlockFluid.getFluidHeightPercent(getBlockMetadata(k1, l1, i2));

                    if ((double)l >= d1)
                    {
                        flag = true;
                        block.velocityToAddToEntity(this, k1, l1, i2, par3Entity, vec3d);
                    }
                }
            }
        }

        if (vec3d.lengthVector() > 0.0D)
        {
            vec3d = vec3d.normalize();
            double d = 0.014D;
            par3Entity.motionX += vec3d.xCoord * d;
            par3Entity.motionY += vec3d.yCoord * d;
            par3Entity.motionZ += vec3d.zCoord * d;
        }

        return flag;
    }

    /**
     * Returns true if the given bounding box contains the given material
     */
    public boolean isMaterialInBB(AxisAlignedBB par1AxisAlignedBB, Material par2Material)
    {
        int i = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int j = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int k = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int l = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int i1 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int j1 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        for (int k1 = i; k1 < j; k1++)
        {
            for (int l1 = k; l1 < l; l1++)
            {
                for (int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.blocksList[getBlockId(k1, l1, i2)];

                    if (block != null && block.blockMaterial == par2Material)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * checks if the given AABB is in the material given. Used while swimming.
     */
    public boolean isAABBInMaterial(AxisAlignedBB par1AxisAlignedBB, Material par2Material)
    {
        int i = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int j = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int k = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int l = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int i1 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int j1 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        for (int k1 = i; k1 < j; k1++)
        {
            for (int l1 = k; l1 < l; l1++)
            {
                for (int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.blocksList[getBlockId(k1, l1, i2)];

                    if (block == null || block.blockMaterial != par2Material)
                    {
                        continue;
                    }

                    int j2 = getBlockMetadata(k1, l1, i2);
                    double d = l1 + 1;

                    if (j2 < 8)
                    {
                        d = (double)(l1 + 1) - (double)j2 / 8D;
                    }

                    if (d >= par1AxisAlignedBB.minY)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Creates an explosion. Args: entity, x, y, z, strength
     */
    public Explosion createExplosion(Entity par1Entity, double par2, double par4, double par6, float par8)
    {
        return newExplosion(par1Entity, par2, par4, par6, par8, false);
    }

    /**
     * returns a new explosion. Does initiation (at time of writing Explosion is not finished)
     */
    public Explosion newExplosion(Entity par1Entity, double par2, double par4, double par6, float par8, boolean par9)
    {
        Explosion explosion = new Explosion(this, par1Entity, par2, par4, par6, par8);
        explosion.isFlaming = par9;
        explosion.doExplosionA();
        explosion.doExplosionB(true);
        return explosion;
    }

    /**
     * Gets the percentage of real blocks within within a bounding box, along a specified vector.
     */
    public float getBlockDensity(Vec3 par1Vec3, AxisAlignedBB par2AxisAlignedBB)
    {
        double d = 1.0D / ((par2AxisAlignedBB.maxX - par2AxisAlignedBB.minX) * 2D + 1.0D);
        double d1 = 1.0D / ((par2AxisAlignedBB.maxY - par2AxisAlignedBB.minY) * 2D + 1.0D);
        double d2 = 1.0D / ((par2AxisAlignedBB.maxZ - par2AxisAlignedBB.minZ) * 2D + 1.0D);
        int i = 0;
        int j = 0;

        for (float f = 0.0F; f <= 1.0F; f = (float)((double)f + d))
        {
            for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float)((double)f1 + d1))
            {
                for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float)((double)f2 + d2))
                {
                    double d3 = par2AxisAlignedBB.minX + (par2AxisAlignedBB.maxX - par2AxisAlignedBB.minX) * (double)f;
                    double d4 = par2AxisAlignedBB.minY + (par2AxisAlignedBB.maxY - par2AxisAlignedBB.minY) * (double)f1;
                    double d5 = par2AxisAlignedBB.minZ + (par2AxisAlignedBB.maxZ - par2AxisAlignedBB.minZ) * (double)f2;

                    if (rayTraceBlocks(Vec3.createVectorHelper(d3, d4, d5), par1Vec3) == null)
                    {
                        i++;
                    }

                    j++;
                }
            }
        }

        return (float)i / (float)j;
    }

    public boolean func_48457_a(EntityPlayer par1EntityPlayer, int par2, int par3, int par4, int par5)
    {
        if (par5 == 0)
        {
            par3--;
        }

        if (par5 == 1)
        {
            par3++;
        }

        if (par5 == 2)
        {
            par4--;
        }

        if (par5 == 3)
        {
            par4++;
        }

        if (par5 == 4)
        {
            par2--;
        }

        if (par5 == 5)
        {
            par2++;
        }

        if (getBlockId(par2, par3, par4) == Block.fire.blockID)
        {
            playAuxSFXAtEntity(par1EntityPlayer, 1004, par2, par3, par4, 0);
            setBlockWithNotify(par2, par3, par4, 0);
            return true;
        }
        else
        {
            return false;
        }
    }

    public Entity func_4085_a(Class par1Class)
    {
        return null;
    }

    /**
     * This string is 'All: (number of loaded entities)' Viewable by press ing F3
     */
    public String getDebugLoadedEntities()
    {
        return (new StringBuilder()).append("All: ").append(loadedEntityList.size()).toString();
    }

    /**
     * Returns the name of the current chunk provider, by calling chunkprovider.makeString()
     */
    public String getProviderName()
    {
        return chunkProvider.makeString();
    }

    /**
     * adds tile entity to despawn list (renamed from markEntityForDespawn)
     */
    public void markTileEntityForDespawn(TileEntity par1TileEntity)
    {
        entityRemoval.add(par1TileEntity);
    }

    /**
     * Returns true if the block at the specified coordinates is an opaque cube. Args: x, y, z
     */
    public boolean isBlockOpaqueCube(int par1, int par2, int par3)
    {
        Block block = Block.blocksList[getBlockId(par1, par2, par3)];

        if (block == null)
        {
            return false;
        }
        if(block == Block.glowStone){
            return true;
        }
        else
        {
            return block.isOpaqueCube();
        }
    }

    /**
     * Indicate if a material is a normal solid opaque cube.
     */
    public boolean isBlockNormalCube(int par1, int par2, int par3)
    {
        return Block.isNormalCube(getBlockId(par1, par2, par3));
    }

    /**
     * Checks if the block is a solid, normal cube. If the chunk does not exist, or is not loaded, it returns the
     * boolean parameter.
     */
    public boolean isBlockNormalCubeDefault(int par1, int par2, int par3, boolean par4)
    {
        if (par1 < 0xfe363c80 || par3 < 0xfe363c80 || par1 >= 0x1c9c380 || par3 >= 0x1c9c380)
        {
            return par4;
        }

        Chunk chunk = chunkProvider.provideChunk(par1 >> 4, par3 >> 4);

        if (chunk == null || chunk.isEmpty())
        {
            return par4;
        }

        Block block = Block.blocksList[getBlockId(par1, par2, par3)];

        if (block == null)
        {
            return false;
        }
        else
        {
            return block.blockMaterial.isOpaque() && block.renderAsNormalBlock();
        }
    }

    public void saveWorldIndirectly(IProgressUpdate par1IProgressUpdate)
    {
        saveWorld(true, par1IProgressUpdate);

        try
        {
            ThreadedFileIOBase.threadedIOInstance.waitForFinish();
        }
        catch (InterruptedException interruptedexception)
        {
            interruptedexception.printStackTrace();
        }
    }

    /**
     * Called on construction of the World class to setup the initial skylight values
     */
    public void calculateInitialSkylight()
    {
        int i = calculateSkylightSubtracted(1.0F);

        if (i != skylightSubtracted)
        {
            skylightSubtracted = i;
        }
    }

    /**
     * Set which types of mobs are allowed to spawn (peaceful vs hostile).
     */
    public void setAllowedSpawnTypes(boolean par1, boolean par2)
    {
        spawnHostileMobs = par1;
        spawnPeacefulMobs = par2;
    }

    /**
     * Runs a single tick for the world
     */
    public void tick()
    {
        if (getWorldInfo().isHardcoreModeEnabled() && difficultySetting < 3)
        {
            difficultySetting = 3;
        }

        worldProvider.worldChunkMgr.cleanupCache();
        if (ODNBXlite.Generator==ODNBXlite.GEN_NEWBIOMES ||
           (ODNBXlite.Generator==ODNBXlite.GEN_OLDBIOMES &&
           (ODNBXlite.MapFeatures==ODNBXlite.FEATURES_BETA15 ||
            ODNBXlite.MapFeatures==ODNBXlite.FEATURES_BETA173 ||
            ODNBXlite.MapFeatures==ODNBXlite.FEATURES_JUNGLE))){
            updateWeather();
        }

        if (isAllPlayersFullyAsleep())
        {
            boolean flag = false;

            if (spawnHostileMobs)
            {
                if (difficultySetting < 1);
            }

            if (!flag)
            {
                long l = worldInfo.getWorldTime() + 24000L;
                worldInfo.setWorldTime(l - l % 24000L);
                wakeUpAllPlayers();
            }
        }

        field_72984_F.startSection("mobSpawner");
        if (worldProvider.worldType!=1){
            if (ODNBXlite.Generator==ODNBXlite.GEN_NEWBIOMES || !ODNBXlite.OldSpawning){
                SpawnerAnimals.performSpawningSP(this, spawnHostileMobs, spawnPeacefulMobs && worldInfo.getWorldTime() % 400L == 0L);
            } else if (ODNBXlite.Generator==ODNBXlite.GEN_OLDBIOMES || worldProvider.worldType!=0){
                SpawnerAnimalsBeta.performSpawning(this, spawnHostileMobs, spawnPeacefulMobs);
            } else if (ODNBXlite.Generator==ODNBXlite.GEN_BIOMELESS){
                animalSpawner.func_1150_a(this);
                monsterSpawner.func_1150_a(this);
                waterMobSpawner.func_1150_a(this);
            }
        }else{
            SpawnerAnimals.performSpawningSP(this, spawnHostileMobs, spawnPeacefulMobs);
        }
        field_72984_F.endStartSection("chunkSource");
        chunkProvider.unload100OldestChunks();
        int i = calculateSkylightSubtracted(1.0F);

        if (i != skylightSubtracted)
        {
            skylightSubtracted = i;
        }

        long l1 = worldInfo.getWorldTime() + 1L;

        if (l1 % (long)autosavePeriod == 0L)
        {
            field_72984_F.endStartSection("save");
            saveWorld(false, null);
        }

        worldInfo.setWorldTime(l1);
        field_72984_F.endStartSection("tickPending");
        tickUpdates(false);
        field_72984_F.endStartSection("tickTiles");
        tickBlocksAndAmbiance();
        field_72984_F.endStartSection("village");
        villageCollectionObj.tick();
        villageSiegeObj.tick();
        field_72984_F.endSection();
    }

    /**
     * Called from World constructor to set rainingStrength and thunderingStrength
     */
    private void calculateInitialWeather()
    {
        if (worldInfo.isRaining())
        {
            rainingStrength = 1.0F;

            if (worldInfo.isThundering())
            {
                thunderingStrength = 1.0F;
            }
        }
    }

    /**
     * Updates all weather states.
     */
    protected void updateWeather()
    {
        if (worldProvider.hasNoSky)
        {
            return;
        }

        if (lastLightningBolt > 0)
        {
            lastLightningBolt--;
        }

        int i = worldInfo.getThunderTime();

        if (i <= 0)
        {
            if (worldInfo.isThundering())
            {
                worldInfo.setThunderTime(rand.nextInt(12000) + 3600);
            }
            else
            {
                worldInfo.setThunderTime(rand.nextInt(0x29040) + 12000);
            }
        }
        else
        {
            i--;
            worldInfo.setThunderTime(i);

            if (i <= 0)
            {
                worldInfo.setThundering(!worldInfo.isThundering());
            }
        }

        int j = worldInfo.getRainTime();

        if (j <= 0)
        {
            if (worldInfo.isRaining())
            {
                worldInfo.setRainTime(rand.nextInt(12000) + 12000);
            }
            else
            {
                worldInfo.setRainTime(rand.nextInt(0x29040) + 12000);
            }
        }
        else
        {
            j--;
            worldInfo.setRainTime(j);

            if (j <= 0)
            {
                worldInfo.setRaining(!worldInfo.isRaining());
            }
        }

        prevRainingStrength = rainingStrength;

        if (worldInfo.isRaining())
        {
            rainingStrength += 0.01D;
        }
        else
        {
            rainingStrength -= 0.01D;
        }

        if (rainingStrength < 0.0F)
        {
            rainingStrength = 0.0F;
        }

        if (rainingStrength > 1.0F)
        {
            rainingStrength = 1.0F;
        }

        prevThunderingStrength = thunderingStrength;

        if (worldInfo.isThundering())
        {
            thunderingStrength += 0.01D;
        }
        else
        {
            thunderingStrength -= 0.01D;
        }

        if (thunderingStrength < 0.0F)
        {
            thunderingStrength = 0.0F;
        }

        if (thunderingStrength > 1.0F)
        {
            thunderingStrength = 1.0F;
        }
    }

    /**
     * Stops all weather effects.
     */
    private void clearWeather()
    {
        worldInfo.setRainTime(0);
        worldInfo.setRaining(false);
        worldInfo.setThunderTime(0);
        worldInfo.setThundering(false);
    }

    protected void func_48461_r()
    {
        activeChunkSet.clear();
        field_72984_F.startSection("buildList");

        for (int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayer entityplayer = (EntityPlayer)playerEntities.get(i);
            int k = MathHelper.floor_double(entityplayer.posX / 16D);
            int i1 = MathHelper.floor_double(entityplayer.posZ / 16D);
            byte byte0 = 7;

            for (int l1 = -byte0; l1 <= byte0; l1++)
            {
                for (int i2 = -byte0; i2 <= byte0; i2++)
                {
                    activeChunkSet.add(new ChunkCoordIntPair(l1 + k, i2 + i1));
                }
            }
        }

        field_72984_F.endSection();

        if (ambientTickCountdown > 0)
        {
            ambientTickCountdown--;
        }

        field_72984_F.startSection("playerCheckLight");

        if (!playerEntities.isEmpty())
        {
            int j = rand.nextInt(playerEntities.size());
            EntityPlayer entityplayer1 = (EntityPlayer)playerEntities.get(j);
            int l = (MathHelper.floor_double(entityplayer1.posX) + rand.nextInt(11)) - 5;
            int j1 = (MathHelper.floor_double(entityplayer1.posY) + rand.nextInt(11)) - 5;
            int k1 = (MathHelper.floor_double(entityplayer1.posZ) + rand.nextInt(11)) - 5;
            updateAllLightTypes(l, j1, k1);
        }

        field_72984_F.endSection();
    }

    protected void func_48458_a(int par1, int par2, Chunk par3Chunk)
    {
        field_72984_F.endStartSection("tickChunk");
        par3Chunk.updateSkylight();
        field_72984_F.endStartSection("moodSound");

        if (ambientTickCountdown == 0)
        {
            updateLCG = updateLCG * 3 + 0x3c6ef35f;
            int i = updateLCG >> 2;
            int j = i & 0xf;
            int k = i >> 8 & 0xf;
            int l = i >> 16 & 0x7f;
            int i1 = par3Chunk.getBlockID(j, l, k);
            j += par1;
            k += par2;

            if (i1 == 0 && getFullBlockLightValue(j, l, k) <= rand.nextInt(8) && getSavedLightValue(EnumSkyBlock.Sky, j, l, k) <= 0)
            {
                EntityPlayer entityplayer = getClosestPlayer((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D, 8D);

                if (entityplayer != null && entityplayer.getDistanceSq((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D) > 4D)
                {
                    playSoundEffect((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D, "ambient.cave.cave", 0.7F, 0.8F + rand.nextFloat() * 0.2F);
                    ambientTickCountdown = rand.nextInt(12000) + 6000;
                }
            }
        }

        field_72984_F.endStartSection("checkLight");
        par3Chunk.enqueueRelightChecks();
    }

    /**
     * plays random cave ambient sounds and runs updateTick on random blocks within each chunk in the vacinity of a
     * player
     */
    protected void tickBlocksAndAmbiance()
    {
        func_48461_r();
        int i = 0;
        int j = 0;

        for (Iterator iterator = activeChunkSet.iterator(); iterator.hasNext(); field_72984_F.endSection())
        {
            ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair)iterator.next();
            int k = chunkcoordintpair.chunkXPos * 16;
            int l = chunkcoordintpair.chunkZPos * 16;
            field_72984_F.startSection("getChunk");
            Chunk chunk = getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);
            func_48458_a(k, l, chunk);
            field_72984_F.endStartSection("thunder");

            if (rand.nextInt(0x186a0) == 0 && isRaining() && isThundering())
            {
                updateLCG = updateLCG * 3 + 0x3c6ef35f;
                int i1 = updateLCG >> 2;
                int k1 = k + (i1 & 0xf);
                int j2 = l + (i1 >> 8 & 0xf);
                int i3 = getPrecipitationHeight(k1, j2);

                if (canLightningStrikeAt(k1, i3, j2))
                {
                    addWeatherEffect(new EntityLightningBolt(this, k1, i3, j2));
                    lastLightningBolt = 2;
                }
            }

            field_72984_F.endStartSection("iceandsnow");
            if(rand.nextInt(4) == 0 && ODNBXlite.Generator==ODNBXlite.GEN_BIOMELESS && snowCovered && ODNBXlite.SnowCovered && worldProvider.worldType==0)
            {
                updateLCG = updateLCG * 3 + 0x3c6ef35f;
                int l2 = updateLCG >> 2;
                int l3 = l2 & 0xf;
                int l4 = l2 >> 8 & 0xf;
                int l5 = getPrecipitationHeight(l3 + k, l4 + l);
                if(l5 >= 0 && l5 < 128 && chunk.getSavedLightValue(EnumSkyBlock.Block, l3, l5, l4) < 10)
                {
                    int k6 = chunk.getBlockID(l3, l5 - 1, l4);
                    int i7 = chunk.getBlockID(l3, l5, l4);
                    if(i7 == 0 && Block.snow.canPlaceBlockAt(this, l3 + k, l5, l4 + l) && k6 != 0 && k6 != Block.ice.blockID && Block.blocksList[k6].blockMaterial.isSolid())
                    {
                        setBlockWithNotify(l3 + k, l5, l4 + l, Block.snow.blockID);
                    }
                    if((k6 == Block.waterMoving.blockID || k6 == Block.waterStill.blockID) && chunk.getBlockMetadata(l3, l5 - 1, l4) == 0)
                    {
                        setBlockWithNotify(l3 + k, l5 - 1, l4 + l, Block.ice.blockID);
                    }
                }
            }else if (ODNBXlite.Generator==ODNBXlite.GEN_NEWBIOMES){
                updateLCG = updateLCG * 3 + 0x3c6ef35f;
                int l7 = updateLCG >> 2;
                int l8 = l7 & 0xf;
                int l9 = l7 >> 8 & 0xf;
                int l10 = getPrecipitationHeight(l8 + k, l9 + l);
                if(isRaining() && isBlockFreezable(l8 + k, l10 - 1, l9 + l))
                {
                    setBlockWithNotify(l8 + k, l10 - 1, l9 + l, Block.ice.blockID);
                }
                if(isRaining() && canSnowAt(l8 + k, l10, l9 + l))
                {
                    setBlockWithNotify(l8 + k, l10, l9 + l, Block.snow.blockID);
                }
            }else{
                if(rand.nextInt(16) == 0 && (ODNBXlite.MapFeatures==ODNBXlite.FEATURES_BETA15 || ODNBXlite.MapFeatures==ODNBXlite.FEATURES_BETA173))
                {
                    updateLCG = updateLCG * 3 + 0x3c6ef35f;
                    int l7 = updateLCG >> 2;
                    int l8 = l7 & 0xf;
                    int l9 = l7 >> 8 & 0xf;
                    int l10 = getPrecipitationHeight(l8 + k, l9 + l);
                    if(getWorldChunkManager().oldGetBiomeGenAt(l8 + k, l9 + l).getEnableSnow() && l10 >= 0 && l10 < 128 && chunk.getSavedLightValue(EnumSkyBlock.Block, l8, l10, l9) < 10)
                    {
                        int i66 = chunk.getBlockID(l8, l10 - 1, l9);
                        int k66 = chunk.getBlockID(l8, l10, l9);
                        if(isRaining() && k66 == 0 && Block.snow.canPlaceBlockAt(this, l8 + k, l10, l9 + l) && i66 != 0 && i66 != Block.ice.blockID && Block.blocksList[i66].blockMaterial.isSolid())
                        {
                            setBlockWithNotify(l8 + k, l10, l9 + l, Block.snow.blockID);
                        }
                        if(i66 == Block.waterStill.blockID && chunk.getBlockMetadata(l8, l10 - 1, l9) == 0)
                        {
                            setBlockWithNotify(l8 + k, l10 - 1, l9 + l, Block.ice.blockID);
                        }
                    }
                }
            }
            field_72984_F.endStartSection("tickTiles");
            ExtendedBlockStorage aextendedblockstorage[] = chunk.getBlockStorageArray();
            int i2 = aextendedblockstorage.length;

            for (int l2 = 0; l2 < i2; l2++)
            {
                ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[l2];

                if (extendedblockstorage == null || !extendedblockstorage.getNeedsRandomTick())
                {
                    continue;
                }

                for (int k3 = 0; k3 < 3; k3++)
                {
                    updateLCG = updateLCG * 3 + 0x3c6ef35f;
                    int l3 = updateLCG >> 2;
                    int i4 = l3 & 0xf;
                    int j4 = l3 >> 8 & 0xf;
                    int k4 = l3 >> 16 & 0xf;
                    int l4 = extendedblockstorage.getExtBlockID(i4, k4, j4);
                    j++;
                    Block block = Block.blocksList[l4];

                    if (block != null && block.getTickRandomly())
                    {
                        i++;
                        block.updateTick(this, i4 + k, k4 + extendedblockstorage.getYLocation(), j4 + l, rand);
                    }
                }
            }
        }
    }

    /**
     * checks to see if a given block is both water and is cold enough to freeze
     */
    public boolean isBlockFreezable(int par1, int par2, int par3)
    {
        return canBlockFreeze(par1, par2, par3, false);
    }

    /**
     * checks to see if a given block is both water and has at least one immediately adjacent non-water block
     */
    public boolean isBlockFreezableNaturally(int par1, int par2, int par3)
    {
        return canBlockFreeze(par1, par2, par3, true);
    }

    /**
     * checks to see if a given block is both water, and cold enough to freeze - if the par4 boolean is set, this will
     * only return true if there is a non-water block immediately adjacent to the specified block
     */
    public boolean canBlockFreeze(int par1, int par2, int par3, boolean par4)
    {
        BiomeGenBase biomegenbase = getBiomeGenForCoords(par1, par3);
        float f = biomegenbase.getFloatTemperature();

        if (f > 0.15F)
        {
            return false;
        }

        if (par2 >= 0 && par2 < 256 && getSavedLightValue(EnumSkyBlock.Block, par1, par2, par3) < 10)
        {
            int i = getBlockId(par1, par2, par3);

            if ((i == Block.waterStill.blockID || i == Block.waterMoving.blockID) && getBlockMetadata(par1, par2, par3) == 0)
            {
                if (!par4)
                {
                    return true;
                }

                boolean flag = true;

                if (flag && getBlockMaterial(par1 - 1, par2, par3) != Material.water)
                {
                    flag = false;
                }

                if (flag && getBlockMaterial(par1 + 1, par2, par3) != Material.water)
                {
                    flag = false;
                }

                if (flag && getBlockMaterial(par1, par2, par3 - 1) != Material.water)
                {
                    flag = false;
                }

                if (flag && getBlockMaterial(par1, par2, par3 + 1) != Material.water)
                {
                    flag = false;
                }

                if (!flag)
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Tests whether or not snow can be placed at a given location
     */
    public boolean canSnowAt(int par1, int par2, int par3)
    {
        BiomeGenBase biomegenbase = getBiomeGenForCoords(par1, par3);
        float f = biomegenbase.getFloatTemperature();

        if (f > 0.15F)
        {
            return false;
        }

        if (par2 >= 0 && par2 < 256 && getSavedLightValue(EnumSkyBlock.Block, par1, par2, par3) < 10)
        {
            int i = getBlockId(par1, par2 - 1, par3);
            int j = getBlockId(par1, par2, par3);

            if (j == 0 && Block.snow.canPlaceBlockAt(this, par1, par2, par3) && i != 0 && i != Block.ice.blockID && Block.blocksList[i].blockMaterial.blocksMovement())
            {
                return true;
            }
        }

        return false;
    }

    public void updateAllLightTypes(int par1, int par2, int par3)
    {
        if (!worldProvider.hasNoSky)
        {
            updateLightByType(EnumSkyBlock.Sky, par1, par2, par3);
        }

        updateLightByType(EnumSkyBlock.Block, par1, par2, par3);
    }

    private int computeSkyLightValue(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        int i = 0;

        if (canBlockSeeTheSky(par2, par3, par4))
        {
            i = 15;
        }
        else
        {
            if (par6 == 0)
            {
                par6 = 1;
            }

            int j = getSavedLightValue(EnumSkyBlock.Sky, par2 - 1, par3, par4) - par6;
            int k = getSavedLightValue(EnumSkyBlock.Sky, par2 + 1, par3, par4) - par6;
            int l = getSavedLightValue(EnumSkyBlock.Sky, par2, par3 - 1, par4) - par6;
            int i1 = getSavedLightValue(EnumSkyBlock.Sky, par2, par3 + 1, par4) - par6;
            int j1 = getSavedLightValue(EnumSkyBlock.Sky, par2, par3, par4 - 1) - par6;
            int k1 = getSavedLightValue(EnumSkyBlock.Sky, par2, par3, par4 + 1) - par6;
            if ((ODNBXlite.SurrWaterType==Block.waterStill.blockID||ODNBXlite.SurrWaterType==Block.waterMoving.blockID) && ODNBXlite.MapFeatures==ODNBXlite.FEATURES_INDEV){
                if (isBounds(par2-1, par3, par4)){
                    j = ODNBXlite.getSkyLightInBounds(par3);
                }
                if (isBounds(par2+1, par3, par4)){
                    k = ODNBXlite.getSkyLightInBounds(par3);
                }
                if (isBounds(par2, par3-1, par4)){
                    l = ODNBXlite.getSkyLightInBounds(par3-1);
                }
                if (isBounds(par2, par3+1, par4)){
                    i1 = ODNBXlite.getSkyLightInBounds(par3+1);
                }
                if (isBounds(par2, par3, par4-1)){
                    j1 = ODNBXlite.getSkyLightInBounds(par3);
                }
                if (isBounds(par2, par3, par4+1)){
                    k1 = ODNBXlite.getSkyLightInBounds(par3);
                }
            }

            if (j > i)
            {
                i = j;
            }

            if (k > i)
            {
                i = k;
            }

            if (l > i)
            {
                i = l;
            }

            if (i1 > i)
            {
                i = i1;
            }

            if (j1 > i)
            {
                i = j1;
            }

            if (k1 > i)
            {
                i = k1;
            }
        }

        return i;
    }

    private int computeBlockLightValue(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        int i = Block.lightValue[par5];
        int j = getSavedLightValue(EnumSkyBlock.Block, par2 - 1, par3, par4) - par6;
        int k = getSavedLightValue(EnumSkyBlock.Block, par2 + 1, par3, par4) - par6;
        int l = getSavedLightValue(EnumSkyBlock.Block, par2, par3 - 1, par4) - par6;
        int i1 = getSavedLightValue(EnumSkyBlock.Block, par2, par3 + 1, par4) - par6;
        int j1 = getSavedLightValue(EnumSkyBlock.Block, par2, par3, par4 - 1) - par6;
        int k1 = getSavedLightValue(EnumSkyBlock.Block, par2, par3, par4 + 1) - par6;

        if (j > i)
        {
            i = j;
        }

        if (k > i)
        {
            i = k;
        }

        if (l > i)
        {
            i = l;
        }

        if (i1 > i)
        {
            i = i1;
        }

        if (j1 > i)
        {
            i = j1;
        }

        if (k1 > i)
        {
            i = k1;
        }

        return i;
    }

    public void updateLightByType(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4)
    {
        if (!doChunksNearChunkExist(par2, par3, par4, 17))
        {
            return;
        }

        int i = 0;
        int j = 0;
        field_72984_F.startSection("getBrightness");
        int k = getSavedLightValue(par1EnumSkyBlock, par2, par3, par4);
        int i1 = 0;
        int k1 = k;
        int j2 = getBlockId(par2, par3, par4);
        int i3 = func_48462_d(par2, par3, par4);

        if (i3 == 0)
        {
            i3 = 1;
        }

        int l3 = 0;

        if (par1EnumSkyBlock == EnumSkyBlock.Sky)
        {
            l3 = computeSkyLightValue(k1, par2, par3, par4, j2, i3);
        }
        else
        {
            l3 = computeBlockLightValue(k1, par2, par3, par4, j2, i3);
        }

        i1 = l3;

        if (i1 > k)
        {
            lightUpdateBlockList[j++] = 0x20820;
        }
        else if (i1 < k)
        {
            if (par1EnumSkyBlock == EnumSkyBlock.Block);

            lightUpdateBlockList[j++] = 0x20820 + (k << 18);

            do
            {
                if (i >= j)
                {
                    break;
                }

                int l1 = lightUpdateBlockList[i++];
                int k2 = ((l1 & 0x3f) - 32) + par2;
                int j3 = ((l1 >> 6 & 0x3f) - 32) + par3;
                int i4 = ((l1 >> 12 & 0x3f) - 32) + par4;
                int k4 = l1 >> 18 & 0xf;
                int i5 = getSavedLightValue(par1EnumSkyBlock, k2, j3, i4);

                if (i5 == k4)
                {
                    setLightValue(par1EnumSkyBlock, k2, j3, i4, 0);

                    if (k4 > 0)
                    {
                        int l5 = k2 - par2;
                        int j6 = j3 - par3;
                        int l6 = i4 - par4;

                        if (l5 < 0)
                        {
                            l5 = -l5;
                        }

                        if (j6 < 0)
                        {
                            j6 = -j6;
                        }

                        if (l6 < 0)
                        {
                            l6 = -l6;
                        }

                        if (l5 + j6 + l6 < 17)
                        {
                            int j7 = 0;

                            while (j7 < 6)
                            {
                                int k7 = (j7 % 2) * 2 - 1;
                                int l7 = k2 + (((j7 / 2) % 3) / 2) * k7;
                                int i8 = j3 + (((j7 / 2 + 1) % 3) / 2) * k7;
                                int j8 = i4 + (((j7 / 2 + 2) % 3) / 2) * k7;
                                int j5 = getSavedLightValue(par1EnumSkyBlock, l7, i8, j8);
                                int k8 = Block.lightOpacity[getBlockId(l7, i8, j8)];

                                if (k8 == 0)
                                {
                                    k8 = 1;
                                }

                                if (j5 == k4 - k8 && j < lightUpdateBlockList.length)
                                {
                                    lightUpdateBlockList[j++] = (l7 - par2) + 32 + ((i8 - par3) + 32 << 6) + ((j8 - par4) + 32 << 12) + (k4 - k8 << 18);
                                }

                                j7++;
                            }
                        }
                    }
                }
            }
            while (true);

            i = 0;
        }

        field_72984_F.endSection();
        field_72984_F.startSection("tcp < tcc");

        do
        {
            if (i >= j)
            {
                break;
            }

            int l = lightUpdateBlockList[i++];
            int j1 = ((l & 0x3f) - 32) + par2;
            int i2 = ((l >> 6 & 0x3f) - 32) + par3;
            int l2 = ((l >> 12 & 0x3f) - 32) + par4;
            int k3 = getSavedLightValue(par1EnumSkyBlock, j1, i2, l2);
            int j4 = getBlockId(j1, i2, l2);
            int l4 = Block.lightOpacity[j4];

            if (l4 == 0)
            {
                l4 = 1;
            }

            int k5 = 0;

            if (par1EnumSkyBlock == EnumSkyBlock.Sky)
            {
                k5 = computeSkyLightValue(k3, j1, i2, l2, j4, l4);
            }
            else
            {
                k5 = computeBlockLightValue(k3, j1, i2, l2, j4, l4);
            }

            if (k5 != k3)
            {
                setLightValue(par1EnumSkyBlock, j1, i2, l2, k5);

                if (k5 > k3)
                {
                    int i6 = j1 - par2;
                    int k6 = i2 - par3;
                    int i7 = l2 - par4;

                    if (i6 < 0)
                    {
                        i6 = -i6;
                    }

                    if (k6 < 0)
                    {
                        k6 = -k6;
                    }

                    if (i7 < 0)
                    {
                        i7 = -i7;
                    }

                    if (i6 + k6 + i7 < 17 && j < lightUpdateBlockList.length - 6)
                    {
                        if (getSavedLightValue(par1EnumSkyBlock, j1 - 1, i2, l2) < k5)
                        {
                            lightUpdateBlockList[j++] = (j1 - 1 - par2) + 32 + ((i2 - par3) + 32 << 6) + ((l2 - par4) + 32 << 12);
                        }

                        if (getSavedLightValue(par1EnumSkyBlock, j1 + 1, i2, l2) < k5)
                        {
                            lightUpdateBlockList[j++] = ((j1 + 1) - par2) + 32 + ((i2 - par3) + 32 << 6) + ((l2 - par4) + 32 << 12);
                        }

                        if (getSavedLightValue(par1EnumSkyBlock, j1, i2 - 1, l2) < k5)
                        {
                            lightUpdateBlockList[j++] = (j1 - par2) + 32 + ((i2 - 1 - par3) + 32 << 6) + ((l2 - par4) + 32 << 12);
                        }

                        if (getSavedLightValue(par1EnumSkyBlock, j1, i2 + 1, l2) < k5)
                        {
                            lightUpdateBlockList[j++] = (j1 - par2) + 32 + (((i2 + 1) - par3) + 32 << 6) + ((l2 - par4) + 32 << 12);
                        }

                        if (getSavedLightValue(par1EnumSkyBlock, j1, i2, l2 - 1) < k5)
                        {
                            lightUpdateBlockList[j++] = (j1 - par2) + 32 + ((i2 - par3) + 32 << 6) + ((l2 - 1 - par4) + 32 << 12);
                        }

                        if (getSavedLightValue(par1EnumSkyBlock, j1, i2, l2 + 1) < k5)
                        {
                            lightUpdateBlockList[j++] = (j1 - par2) + 32 + ((i2 - par3) + 32 << 6) + (((l2 + 1) - par4) + 32 << 12);
                        }
                    }
                }
            }
        }
        while (true);

        field_72984_F.endSection();
    }

    /**
     * Runs through the list of updates to run and ticks them
     */
    public boolean tickUpdates(boolean par1)
    {
        int i = scheduledTickTreeSet.size();

        if (i != scheduledTickSet.size())
        {
            throw new IllegalStateException("TickNextTick list out of synch");
        }

        if (i > 1000)
        {
            i = 1000;
        }

        for (int j = 0; j < i; j++)
        {
            NextTickListEntry nextticklistentry = (NextTickListEntry)scheduledTickTreeSet.first();

            if (!par1 && nextticklistentry.scheduledTime > worldInfo.getWorldTime())
            {
                break;
            }

            scheduledTickTreeSet.remove(nextticklistentry);
            scheduledTickSet.remove(nextticklistentry);
            byte byte0 = 8;

            if (!checkChunksExist(nextticklistentry.xCoord - byte0, nextticklistentry.yCoord - byte0, nextticklistentry.zCoord - byte0, nextticklistentry.xCoord + byte0, nextticklistentry.yCoord + byte0, nextticklistentry.zCoord + byte0))
            {
                continue;
            }

            int k = getBlockId(nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord);

            if (k == nextticklistentry.blockID && k > 0)
            {
                Block.blocksList[k].updateTick(this, nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord, rand);
            }
        }

        return scheduledTickTreeSet.size() != 0;
    }

    public List getPendingBlockUpdates(Chunk par1Chunk, boolean par2)
    {
        ArrayList arraylist = null;
        ChunkCoordIntPair chunkcoordintpair = par1Chunk.getChunkCoordIntPair();
        int i = chunkcoordintpair.chunkXPos << 4;
        int j = i + 16;
        int k = chunkcoordintpair.chunkZPos << 4;
        int l = k + 16;
        Iterator iterator = scheduledTickSet.iterator();

        do
        {
            if (!iterator.hasNext())
            {
                break;
            }

            NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();

            if (nextticklistentry.xCoord >= i && nextticklistentry.xCoord < j && nextticklistentry.zCoord >= k && nextticklistentry.zCoord < l)
            {
                if (par2)
                {
                    scheduledTickTreeSet.remove(nextticklistentry);
                    iterator.remove();
                }

                if (arraylist == null)
                {
                    arraylist = new ArrayList();
                }

                arraylist.add(nextticklistentry);
            }
        }
        while (true);

        return arraylist;
    }

    /**
     * Randomly will call the random display update on a 1000 blocks within 16 units of the specified position. Args: x,
     * y, z
     */
    public void func_73029_E(int par1, int par2, int par3)
    {
        byte byte0 = 16;
        Random random = new Random();

        for (int i = 0; i < 1000; i++)
        {
            int j = (par1 + rand.nextInt(byte0)) - rand.nextInt(byte0);
            int k = (par2 + rand.nextInt(byte0)) - rand.nextInt(byte0);
            int l = (par3 + rand.nextInt(byte0)) - rand.nextInt(byte0);
            int i1 = getBlockId(j, k, l);

            if (i1 == 0 && rand.nextInt(8) > k && worldProvider.hasNoSky && ODNBXlite.VoidFog==0)
            {
                spawnParticle("depthsuspend", (float)j + rand.nextFloat(), (float)k + rand.nextFloat(), (float)l + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
                continue;
            }

            if (i1 > 0)
            {
                Block.blocksList[i1].randomDisplayTick(this, j, k, l, random);
            }
        }
    }

    /**
     * Will get all entities within the specified AABB excluding the one passed into it. Args: entityToExclude, aabb
     */
    public List getEntitiesWithinAABBExcludingEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
        entitiesWithinAABBExcludingEntity.clear();
        int i = MathHelper.floor_double((par2AxisAlignedBB.minX - 2D) / 16D);
        int j = MathHelper.floor_double((par2AxisAlignedBB.maxX + 2D) / 16D);
        int k = MathHelper.floor_double((par2AxisAlignedBB.minZ - 2D) / 16D);
        int l = MathHelper.floor_double((par2AxisAlignedBB.maxZ + 2D) / 16D);

        for (int i1 = i; i1 <= j; i1++)
        {
            for (int j1 = k; j1 <= l; j1++)
            {
                if (chunkExists(i1, j1))
                {
                    getChunkFromChunkCoords(i1, j1).getEntitiesWithinAABBForEntity(par1Entity, par2AxisAlignedBB, entitiesWithinAABBExcludingEntity);
                }
            }
        }

        return entitiesWithinAABBExcludingEntity;
    }

    /**
     * Returns all entities of the specified class type which intersect with the AABB. Args: entityClass, aabb
     */
    public List getEntitiesWithinAABB(Class par1Class, AxisAlignedBB par2AxisAlignedBB)
    {
        int i = MathHelper.floor_double((par2AxisAlignedBB.minX - 2D) / 16D);
        int j = MathHelper.floor_double((par2AxisAlignedBB.maxX + 2D) / 16D);
        int k = MathHelper.floor_double((par2AxisAlignedBB.minZ - 2D) / 16D);
        int l = MathHelper.floor_double((par2AxisAlignedBB.maxZ + 2D) / 16D);
        ArrayList arraylist = new ArrayList();

        for (int i1 = i; i1 <= j; i1++)
        {
            for (int j1 = k; j1 <= l; j1++)
            {
                if (chunkExists(i1, j1))
                {
                    getChunkFromChunkCoords(i1, j1).getEntitiesOfTypeWithinAAAB(par1Class, par2AxisAlignedBB, arraylist);
                }
            }
        }

        return arraylist;
    }

    public Entity findNearestEntityWithinAABB(Class par1Class, AxisAlignedBB par2AxisAlignedBB, Entity par3Entity)
    {
        List list = getEntitiesWithinAABB(par1Class, par2AxisAlignedBB);
        Entity entity = null;
        double d = Double.MAX_VALUE;
        Iterator iterator = list.iterator();

        do
        {
            if (!iterator.hasNext())
            {
                break;
            }

            Entity entity1 = (Entity)iterator.next();

            if (entity1 != par3Entity)
            {
                double d1 = par3Entity.getDistanceSqToEntity(entity1);

                if (d1 <= d)
                {
                    entity = entity1;
                    d = d1;
                }
            }
        }
        while (true);

        return entity;
    }

    /**
     * Accessor for world Loaded Entity List
     */
    public List getLoadedEntityList()
    {
        return loadedEntityList;
    }

    /**
     * marks the chunk that contains this tilentity as modified and then calls worldAccesses.doNothingWithTileEntity
     */
    public void updateTileEntityChunkAndDoNothing(int par1, int par2, int par3, TileEntity par4TileEntity)
    {
        if (blockExists(par1, par2, par3))
        {
            getChunkFromBlockCoords(par1, par3).setChunkModified();
        }
/*
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).doNothingWithTileEntity(par1, par2, par3, par4TileEntity);
        }*/
    }

    /**
     * Counts how many entities of an entity class exist in the world. Args: entityClass
     */
    public int countEntities(Class par1Class)
    {
        int i = 0;

        for (int j = 0; j < loadedEntityList.size(); j++)
        {
            Entity entity = (Entity)loadedEntityList.get(j);

            if (par1Class.isAssignableFrom(entity.getClass()))
            {
                i++;
            }
        }

        return i;
    }

    public int countEntities2(Class class1)
    {
        int i = 0;
        for (int j = 0; j < loadedEntityList.size(); j++)
        {
            Entity entity = (Entity)loadedEntityList.get(j);
            if (class1.isAssignableFrom(entity.getClass())){
                if (entity instanceof EntityAnimal){
                    EntityAnimal entityanimal = (EntityAnimal)entity;
                    if (!entityanimal.breeded)
                    {
                        i++;
                    }
                }else{
                    i++;
                }
            }
        }
        return i;
    }

    /**
     * adds entities to the loaded entities list, and loads thier skins.
     */
    public void addLoadedEntities(List par1List)
    {
        loadedEntityList.addAll(par1List);

        for (int i = 0; i < par1List.size(); i++)
        {
            obtainEntitySkin((Entity)par1List.get(i));
        }
    }

    /**
     * Adds a list of entities to be unloaded on the next pass of World.updateEntities()
     */
    public void unloadEntities(List par1List)
    {
        unloadedEntityList.addAll(par1List);
    }

    /**
     * Does nothing while unloading 100 oldest chunks
     */
    public void dropOldChunks()
    {
        while (chunkProvider.unload100OldestChunks()) ;
    }

    /**
     * Returns true if the specified block can be placed at the given coordinates, optionally making sure there are no
     * entities in the way. Args: blockID, x, y, z, ignoreEntities
     */
    public boolean canBlockBePlacedAt(int par1, int par2, int par3, int par4, boolean par5, int par6)
    {
        int i = getBlockId(par2, par3, par4);
        Block block = Block.blocksList[i];
        Block block1 = Block.blocksList[par1];
        AxisAlignedBB axisalignedbb = block1.getCollisionBoundingBoxFromPool(this, par2, par3, par4);

        if (par5)
        {
            axisalignedbb = null;
        }

        if (axisalignedbb != null && !checkIfAABBIsClear(axisalignedbb))
        {
            return false;
        }

        if (block != null && (block == Block.waterMoving || block == Block.waterStill || block == Block.lavaMoving || block == Block.lavaStill || block == Block.fire || block.blockMaterial.isGroundCover()))
        {
            block = null;
        }

        return par1 > 0 && block == null && block1.canPlaceBlockOnSide(this, par2, par3, par4, par6);
    }

    public boolean func_72931_a(int par1, int par2, int par3, int par4, boolean par5, int par6, Entity par7Entity)
    {
        return canBlockBePlacedAt(par1, par2, par3, par4, par5, par6);
    }

    public PathEntity getPathEntityToEntity(Entity par1Entity, Entity par2Entity, float par3, boolean par4, boolean par5, boolean par6, boolean par7)
    {
        field_72984_F.startSection("pathfind");
        int i = MathHelper.floor_double(par1Entity.posX);
        int j = MathHelper.floor_double(par1Entity.posY + 1.0D);
        int k = MathHelper.floor_double(par1Entity.posZ);
        int l = (int)(par3 + 16F);
        int i1 = i - l;
        int j1 = j - l;
        int k1 = k - l;
        int l1 = i + l;
        int i2 = j + l;
        int j2 = k + l;
        ChunkCache chunkcache = new ChunkCache(this, i1, j1, k1, l1, i2, j2);
        PathEntity pathentity = (new PathFinder(chunkcache, par4, par5, par6, par7)).createEntityPathTo(par1Entity, par2Entity, par3);
        field_72984_F.endSection();
        return pathentity;
    }

    public PathEntity getEntityPathToXYZ(Entity par1Entity, int par2, int par3, int par4, float par5, boolean par6, boolean par7, boolean par8, boolean par9)
    {
        field_72984_F.startSection("pathfind");
        int i = MathHelper.floor_double(par1Entity.posX);
        int j = MathHelper.floor_double(par1Entity.posY);
        int k = MathHelper.floor_double(par1Entity.posZ);
        int l = (int)(par5 + 8F);
        int i1 = i - l;
        int j1 = j - l;
        int k1 = k - l;
        int l1 = i + l;
        int i2 = j + l;
        int j2 = k + l;
        ChunkCache chunkcache = new ChunkCache(this, i1, j1, k1, l1, i2, j2);
        PathEntity pathentity = (new PathFinder(chunkcache, par6, par7, par8, par9)).createEntityPathTo(par1Entity, par2, par3, par4, par5);
        field_72984_F.endSection();
        return pathentity;
    }

    /**
     * Is this block powering in the specified direction Args: x, y, z, direction
     */
    public boolean isBlockProvidingPowerTo(int par1, int par2, int par3, int par4)
    {
        int i = getBlockId(par1, par2, par3);

        if (i == 0)
        {
            return false;
        }
        else
        {
            return Block.blocksList[i].isIndirectlyPoweringTo(this, par1, par2, par3, par4);
        }
    }

    /**
     * Whether one of the neighboring blocks is putting power into this block. Args: x, y, z
     */
    public boolean isBlockGettingPowered(int par1, int par2, int par3)
    {
        if (isBlockProvidingPowerTo(par1, par2 - 1, par3, 0))
        {
            return true;
        }

        if (isBlockProvidingPowerTo(par1, par2 + 1, par3, 1))
        {
            return true;
        }

        if (isBlockProvidingPowerTo(par1, par2, par3 - 1, 2))
        {
            return true;
        }

        if (isBlockProvidingPowerTo(par1, par2, par3 + 1, 3))
        {
            return true;
        }

        if (isBlockProvidingPowerTo(par1 - 1, par2, par3, 4))
        {
            return true;
        }

        return isBlockProvidingPowerTo(par1 + 1, par2, par3, 5);
    }

    /**
     * Is a block next to you getting powered (if its an attachable block) or is it providing power directly to you.
     * Args: x, y, z, direction
     */
    public boolean isBlockIndirectlyProvidingPowerTo(int par1, int par2, int par3, int par4)
    {
        if (isBlockNormalCube(par1, par2, par3))
        {
            return isBlockGettingPowered(par1, par2, par3);
        }

        int i = getBlockId(par1, par2, par3);

        if (i == 0)
        {
            return false;
        }
        else
        {
            return Block.blocksList[i].isPoweringTo(this, par1, par2, par3, par4);
        }
    }

    /**
     * Used to see if one of the blocks next to you or your block is getting power from a neighboring block. Used by
     * items like TNT or Doors so they don't have redstone going straight into them.  Args: x, y, z
     */
    public boolean isBlockIndirectlyGettingPowered(int par1, int par2, int par3)
    {
        if (isBlockIndirectlyProvidingPowerTo(par1, par2 - 1, par3, 0))
        {
            return true;
        }

        if (isBlockIndirectlyProvidingPowerTo(par1, par2 + 1, par3, 1))
        {
            return true;
        }

        if (isBlockIndirectlyProvidingPowerTo(par1, par2, par3 - 1, 2))
        {
            return true;
        }

        if (isBlockIndirectlyProvidingPowerTo(par1, par2, par3 + 1, 3))
        {
            return true;
        }

        if (isBlockIndirectlyProvidingPowerTo(par1 - 1, par2, par3, 4))
        {
            return true;
        }

        return isBlockIndirectlyProvidingPowerTo(par1 + 1, par2, par3, 5);
    }

    /**
     * Gets the closest player to the entity within the specified distance (if distance is less than 0 then ignored).
     * Args: entity, dist
     */
    public EntityPlayer getClosestPlayerToEntity(Entity par1Entity, double par2)
    {
        return getClosestPlayer(par1Entity.posX, par1Entity.posY, par1Entity.posZ, par2);
    }

    /**
     * Gets the closest player to the point within the specified distance (distance can be set to less than 0 to not
     * limit the distance). Args: x, y, z, dist
     */
    public EntityPlayer getClosestPlayer(double par1, double par3, double par5, double par7)
    {
        double d = -1D;
        EntityPlayer entityplayer = null;

        for (int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayer entityplayer1 = (EntityPlayer)playerEntities.get(i);
            double d1 = entityplayer1.getDistanceSq(par1, par3, par5);

            if ((par7 < 0.0D || d1 < par7 * par7) && (d == -1D || d1 < d))
            {
                d = d1;
                entityplayer = entityplayer1;
            }
        }

        return entityplayer;
    }

    public EntityPlayer func_48456_a(double par1, double par3, double par5)
    {
        double d = -1D;
        EntityPlayer entityplayer = null;

        for (int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayer entityplayer1 = (EntityPlayer)playerEntities.get(i);
            double d1 = entityplayer1.getDistanceSq(par1, entityplayer1.posY, par3);

            if ((par5 < 0.0D || d1 < par5 * par5) && (d == -1D || d1 < d))
            {
                d = d1;
                entityplayer = entityplayer1;
            }
        }

        return entityplayer;
    }

    /**
     * Returns the closest vulnerable player to this entity within the given radius, or null if none is found
     */
    public EntityPlayer getClosestVulnerablePlayerToEntity(Entity par1Entity, double par2)
    {
        return getClosestVulnerablePlayer(par1Entity.posX, par1Entity.posY, par1Entity.posZ, par2);
    }

    /**
     * Returns the closest vulnerable player within the given radius, or null if none is found.
     */
    public EntityPlayer getClosestVulnerablePlayer(double par1, double par3, double par5, double par7)
    {
        double d = -1D;
        EntityPlayer entityplayer = null;

        for (int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayer entityplayer1 = (EntityPlayer)playerEntities.get(i);

            if (entityplayer1.capabilities.disableDamage)
            {
                continue;
            }

            double d1 = entityplayer1.getDistanceSq(par1, par3, par5);

            if ((par7 < 0.0D || d1 < par7 * par7) && (d == -1D || d1 < d))
            {
                d = d1;
                entityplayer = entityplayer1;
            }
        }

        return entityplayer;
    }

    /**
     * Find a player by name in this world.
     */
    public EntityPlayer getPlayerEntityByName(String par1Str)
    {
        for (int i = 0; i < playerEntities.size(); i++)
        {
            if (par1Str.equals(((EntityPlayer)playerEntities.get(i)).username))
            {
                return (EntityPlayer)playerEntities.get(i);
            }
        }

        return null;
    }

    /**
     * If on MP, sends a quitting packet.
     */
    public void sendQuittingDisconnectingPacket()
    {
    }

    /**
     * Checks whether the session lock file was modified by another process
     */
    public void checkSessionLock()
    {
        try{
            saveHandler.checkSessionLock();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * Sets the world time.
     */
    public void setWorldTime(long par1)
    {
        worldInfo.setWorldTime(par1);
    }

    /**
     * Retrieve the world seed from level.dat
     */
    public long getSeed()
    {
        return worldInfo.getSeed();
    }

    public long getWorldTime()
    {
        return worldInfo.getWorldTime();
    }

    /**
     * Returns the coordinates of the spawn point
     */
    public ChunkCoordinates getSpawnPoint()
    {
        return new ChunkCoordinates(worldInfo.getSpawnX(), worldInfo.getSpawnY(), worldInfo.getSpawnZ());
    }

    public void setSpawnPoint(ChunkCoordinates par1ChunkCoordinates)
    {
        worldInfo.setSpawnPosition(par1ChunkCoordinates.posX, par1ChunkCoordinates.posY, par1ChunkCoordinates.posZ);
    }

    /**
     * spwans an entity and loads surrounding chunks
     */
    public void joinEntityInSurroundings(Entity par1Entity)
    {
        int i = MathHelper.floor_double(par1Entity.posX / 16D);
        int j = MathHelper.floor_double(par1Entity.posZ / 16D);
        byte byte0 = 2;

        for (int k = i - byte0; k <= i + byte0; k++)
        {
            for (int l = j - byte0; l <= j + byte0; l++)
            {
                getChunkFromChunkCoords(k, l);
            }
        }

        if (!loadedEntityList.contains(par1Entity))
        {
            loadedEntityList.add(par1Entity);
        }
    }

    /**
     * Called when checking if a certain block can be mined or not. The 'spawn safe zone' check is located here.
     */
    public boolean canMineBlock(EntityPlayer par1EntityPlayer, int par2, int par3, int i)
    {
        return true;
    }

    /**
     * sends a Packet 38 (Entity Status) to all tracked players of that entity
     */
    public void setEntityState(Entity entity, byte byte0)
    {
    }

    public void updateEntityList()
    {
        loadedEntityList.removeAll(unloadedEntityList);

        for (int i = 0; i < unloadedEntityList.size(); i++)
        {
            Entity entity = (Entity)unloadedEntityList.get(i);
            int l = entity.chunkCoordX;
            int j1 = entity.chunkCoordZ;

            if (entity.addedToChunk && chunkExists(l, j1))
            {
                getChunkFromChunkCoords(l, j1).removeEntity(entity);
            }
        }

        for (int j = 0; j < unloadedEntityList.size(); j++)
        {
            releaseEntitySkin((Entity)unloadedEntityList.get(j));
        }

        unloadedEntityList.clear();

        for (int k = 0; k < loadedEntityList.size(); k++)
        {
            Entity entity1 = (Entity)loadedEntityList.get(k);

            if (entity1.ridingEntity != null)
            {
                if (!entity1.ridingEntity.isDead && entity1.ridingEntity.riddenByEntity == entity1)
                {
                    continue;
                }

                entity1.ridingEntity.riddenByEntity = null;
                entity1.ridingEntity = null;
            }

            if (!entity1.isDead)
            {
                continue;
            }

            int i1 = entity1.chunkCoordX;
            int k1 = entity1.chunkCoordZ;

            if (entity1.addedToChunk && chunkExists(i1, k1))
            {
                getChunkFromChunkCoords(i1, k1).removeEntity(entity1);
            }

            loadedEntityList.remove(k--);
            releaseEntitySkin(entity1);
        }
    }

    /**
     * gets the IChunkProvider this world uses.
     */
    public IChunkProvider getChunkProvider()
    {
        return chunkProvider;
    }

    /**
     * Returns this world's current save handler
     */
    public ISaveHandler getSaveHandler()
    {
        return saveHandler;
    }

    /**
     * Gets the World's WorldInfo instance
     */
    public WorldInfo getWorldInfo()
    {
        return worldInfo;
    }

    /**
     * Updates the flag that indicates whether or not all players in the world are sleeping.
     */
    public void updateAllPlayersSleepingFlag()
    {
        allPlayersSleeping = !playerEntities.isEmpty();
        Iterator iterator = playerEntities.iterator();

        do
        {
            if (!iterator.hasNext())
            {
                break;
            }

            EntityPlayer entityplayer = (EntityPlayer)iterator.next();

            if (entityplayer.isPlayerSleeping())
            {
                continue;
            }

            allPlayersSleeping = false;
            break;
        }
        while (true);
    }

    /**
     * Wakes up all players in the world.
     */
    protected void wakeUpAllPlayers()
    {
        allPlayersSleeping = false;
        Iterator iterator = playerEntities.iterator();

        do
        {
            if (!iterator.hasNext())
            {
                break;
            }

            EntityPlayer entityplayer = (EntityPlayer)iterator.next();

            if (entityplayer.isPlayerSleeping())
            {
                entityplayer.wakeUpPlayer(false, false, true);
            }
        }
        while (true);

        clearWeather();
    }

    /**
     * Returns whether or not all players in the world are fully asleep.
     */
    public boolean isAllPlayersFullyAsleep()
    {
        if (allPlayersSleeping && !isRemote)
        {
            for (Iterator iterator = playerEntities.iterator(); iterator.hasNext();)
            {
                EntityPlayer entityplayer = (EntityPlayer)iterator.next();

                if (!entityplayer.isPlayerFullyAsleep())
                {
                    return false;
                }
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public float getWeightedThunderStrength(float par1)
    {
        return (prevThunderingStrength + (thunderingStrength - prevThunderingStrength) * par1) * getRainStrength(par1);
    }

    /**
     * Not sure about this actually. Reverting this one myself.
     */
    public float getRainStrength(float par1)
    {
        return prevRainingStrength + (rainingStrength - prevRainingStrength) * par1;
    }

    public void setRainStrength(float par1)
    {
        prevRainingStrength = par1;
        rainingStrength = par1;
    }

    /**
     * Returns true if the current thunder strength (weighted with the rain strength) is greater than 0.9
     */
    public boolean isThundering()
    {
        return (double)getWeightedThunderStrength(1.0F) > 0.90000000000000002D;
    }

    /**
     * Returns true if the current rain strength is greater than 0.2
     */
    public boolean isRaining()
    {
        return (double)getRainStrength(1.0F) > 0.20000000000000001D;
    }

    public boolean canLightningStrikeAt(int par1, int par2, int par3)
    {
        if (!isRaining())
        {
            return false;
        }

        if (!canBlockSeeTheSky(par1, par2, par3))
        {
            return false;
        }

        if (getPrecipitationHeight(par1, par3) > par2)
        {
            return false;
        }

        if (ODNBXlite.Generator==ODNBXlite.GEN_NEWBIOMES){
            BiomeGenBase biomegenbase = getBiomeGenForCoords(par1, par3);
            if (biomegenbase.getEnableSnow())
            {
                return false;
            }
            else
            {
                return biomegenbase.canSpawnLightningBolt();
            }
        }else{
            OldBiomeGenBase oldbiomegenbase = getWorldChunkManager().oldGetBiomeGenAt(par1, par3);
            if (oldbiomegenbase.getEnableSnow())
            {
                return false;
            }
            else
            {
                return oldbiomegenbase.canSpawnLightningBolt();
            }
        }
    }

    /**
     * Checks to see if the biome rainfall values for a given x,y,z coordinate set are extremely high
     */
    public boolean isBlockHighHumidity(int par1, int par2, int par3)
    {
        BiomeGenBase biomegenbase = getBiomeGenForCoords(par1, par3);
        return biomegenbase.isHighHumidity();
    }

    /**
     * Assigns the given String id to the given MapDataBase using the MapStorage, removing any existing ones of the same
     * id.
     */
    public void setItemData(String par1Str, WorldSavedData par2WorldSavedData)
    {
        mapStorage.setData(par1Str, par2WorldSavedData);
    }

    /**
     * Loads an existing MapDataBase corresponding to the given String id from disk using the MapStorage, instantiating
     * the given Class, or returns null if none such file exists. args: Class to instantiate, String dataid
     */
    public WorldSavedData loadItemData(Class par1Class, String par2Str)
    {
        return mapStorage.loadData(par1Class, par2Str);
    }

    /**
     * Returns an unique new data id from the MapStorage for the given prefix and saves the idCounts map to the
     * 'idcounts' file.
     */
    public int getUniqueDataId(String par1Str)
    {
        return mapStorage.getUniqueDataId(par1Str);
    }

    /**
     * See description for playAuxSFX.
     */
    public void playAuxSFX(int par1, int par2, int par3, int par4, int par5)
    {
        playAuxSFXAtEntity(null, par1, par2, par3, par4, par5);
    }

    /**
     * See description for playAuxSFX.
     */
    public void playAuxSFXAtEntity(EntityPlayer par1EntityPlayer, int par2, int par3, int par4, int par5, int par6)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).playAuxSFX(par1EntityPlayer, par2, par3, par4, par5, par6);
        }
    }

    public void func_72980_b(double par1, double par3, double par5, String par7Str, float par8, float par9)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).playSound(par7Str, par1, par3, par5, par8, par9);
        }
    }

    /**
     * Returns current world height.
     */
    public int getHeight()
    {
        return 256;
    }

    /**
     * puts the World Random seed to a specific state dependant on the inputs
     */
    public Random setRandomSeed(int par1, int par2, int par3)
    {
        long l = (long)par1 * 0x4f9939f508L + (long)par2 * 0x1ef1565bd5L + getWorldInfo().getSeed() + (long)par3;
        rand.setSeed(l);
        return rand;
    }

    /**
     * Updates lighting. Returns true if there are more lighting updates to update
     */
    public boolean updatingLighting()
    {
        return false;
    }

    /**
     * Gets a random mob for spawning in this world.
     */
    public SpawnListEntry getRandomMob(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        List list = getChunkProvider().getPossibleCreatures(par1EnumCreatureType, par2, par3, par4);

        if (list == null || list.isEmpty())
        {
            return null;
        }
        else
        {
            return (SpawnListEntry)WeightedRandom.getRandomItem(rand, list);
        }
    }

    /**
     * Returns the location of the closest structure of the specified type. If not found returns null.
     */
    public ChunkPosition findClosestStructure(String par1Str, int par2, int par3, int par4)
    {
        return getChunkProvider().findClosestStructure(this, par1Str, par2, par3, par4);
    }

    public boolean func_48452_a()
    {
        return false;
    }

    /**
     * Gets sea level for use in rendering the horizen.
     */
    public double getHorizon()
    {
        if (ODNBXlite.isFinite()){
            return ODNBXlite.SurrWaterHeight;
        }
        if (ODNBXlite.VoidFog>1){
            return -9999D;
        }
        return worldInfo.getTerrainType() != WorldType.FLAT ? 63D : 0.0D;
    }

    static
    {
        field_73069_S = (new WeightedRandomChestContent[]
                {
                    new WeightedRandomChestContent(Item.stick.shiftedIndex, 0, 1, 3, 10), new WeightedRandomChestContent(Block.planks.blockID, 0, 1, 3, 10), new WeightedRandomChestContent(Block.wood.blockID, 0, 1, 3, 10), new WeightedRandomChestContent(Item.axeStone.shiftedIndex, 0, 1, 1, 3), new WeightedRandomChestContent(Item.axeWood.shiftedIndex, 0, 1, 1, 5), new WeightedRandomChestContent(Item.pickaxeStone.shiftedIndex, 0, 1, 1, 3), new WeightedRandomChestContent(Item.pickaxeWood.shiftedIndex, 0, 1, 1, 5), new WeightedRandomChestContent(Item.appleRed.shiftedIndex, 0, 2, 3, 5), new WeightedRandomChestContent(Item.bread.shiftedIndex, 0, 2, 3, 3)
                });
    }
}