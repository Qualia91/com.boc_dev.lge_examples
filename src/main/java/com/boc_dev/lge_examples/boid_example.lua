local VEC3F = luajava.bindClass("com.boc_dev.maths.objects.vector.Vec3f")
local QUATERNION_F = luajava.bindClass("com.boc_dev.maths.objects.QuaternionF")
local MATRIX_4F = luajava.bindClass("com.boc_dev.maths.objects.matrix.Matrix4f")
local LIGHTING_TYPE = luajava.bindClass("com.boc_dev.lge_model.generated.enums.LightingType")
local SKYBOX_TYPE = luajava.bindClass("com.boc_dev.lge_model.generated.enums.SkyboxType")
local CAMERA_PROJECTION_TYPE = luajava.bindClass("com.boc_dev.lge_model.generated.enums.CameraProjectionType")
local CAMERA_OBJECT_TYPE = luajava.bindClass("com.boc_dev.lge_model.generated.enums.CameraObjectType")
local MATH = luajava.bindClass("java.lang.Math")

local quaternionX = QUATERNION_F:RotationX(MATH:toRadians(-90));
local quaternionY = QUATERNION_F:RotationY(MATH:toRadians(180));
local quaternionZ = QUATERNION_F:RotationZ(MATH:toRadians(90));
local cameraRotation = quaternionZ:multiply(quaternionY):multiply(quaternionX);

local transformBuilder = luajava.newInstance( "com.boc_dev.maths.objects.srt.TransformBuilder")
local ambientLight = luajava.newInstance( "com.boc_dev.maths.objects.vector.Vec3f", 0.1, 0.1, 0.1)
local fog = luajava.newInstance( "com.boc_dev.graphics_library.objects.lighting.Fog", true, ambientLight, 0.0001)
local mainSceneLayer = luajava.newInstance( "com.boc_dev.lge_core.SceneLayer", "MAIN", ambientLight, fog)

local create_basic_material = function(sceneLayer)
    local materialObject = luajava.newInstance("com.boc_dev.lge_model.generated.components.MaterialObject",
            sceneLayer:getRegistry(),
            "Material",
            luajava.newInstance( "com.boc_dev.maths.objects.vector.Vec3f", 1, 1, 1),
            1,
            1,
            luajava.newInstance( "com.boc_dev.maths.objects.vector.Vec3f", 1, 1, 1)
    )

    local textureObjectVisual = luajava.newInstance("com.boc_dev.lge_model.generated.components.TextureObject",
            sceneLayer:getRegistry(),
            "VisualTextureOne",
            "/textures/brickwall.jpg"
    )

    local normalMapObject = luajava.newInstance("com.boc_dev.lge_model.generated.components.NormalMapObject",
            sceneLayer:getRegistry(),
            "NormalTextureOne",
            "/normalMaps/brickwall_normal.jpg"
    )

    textureObjectVisual:getUpdater():setParent(materialObject):sendUpdate()
    normalMapObject:getUpdater():setParent(materialObject):sendUpdate()

    return materialObject:getUuid()
end


local lightObject = luajava.newInstance("com.boc_dev.lge_model.generated.components.LightObject",
        mainSceneLayer:getRegistry(),
        "MyFirstLight",
        0.25,
        0.5,
        1,
        VEC3F.X,
        0.1,
        VEC3F.Z:neg(),
        1000,
        LIGHTING_TYPE.SPOT
)

local lightObject = luajava.newInstance("com.boc_dev.lge_model.generated.components.LightObject",
        mainSceneLayer:getRegistry(),
        "MySecondLight",
        0.25,
        0.5,
        1,
        luajava.newInstance( "com.boc_dev.maths.objects.vector.Vec3f", 0.529, 0.808, 0.922),
        0.2,
        VEC3F.Z:neg():add(VEC3F.X),
        1,
        LIGHTING_TYPE.DIRECTIONAL
)

local skyBoxObject = luajava.newInstance("com.boc_dev.lge_model.generated.components.SkyBoxObject",
        mainSceneLayer:getRegistry(),
        "SKY_BOX",
        1000,
        SKYBOX_TYPE.SPHERE,
        "/textures/bw_gradient_skybox.png"
)

local cameraTransform = transformBuilder
        :setPosition(luajava.newInstance( "com.boc_dev.maths.objects.vector.Vec3f", -10, 0, 0))
        :setScale(VEC3F.ONE)
        :setRotation(cameraRotation):build()

local cameraObject = luajava.newInstance("com.boc_dev.lge_model.generated.components.CameraObject",
        mainSceneLayer:getRegistry(),
        "Camera",
        CAMERA_PROJECTION_TYPE.PERSPECTIVE,
        CAMERA_OBJECT_TYPE.PRIMARY,
        10000,
        1.22,
        800,
        1,
        1000
)

local controllableObject = luajava.newInstance("com.boc_dev.lge_model.generated.components.ControllableObject",
        mainSceneLayer:getRegistry(),
        "Camera controller",
        true,
        true,
        0.01,
        1)

local cameraTransformObject = luajava.newInstance("com.boc_dev.lge_model.generated.components.TransformObject",
        mainSceneLayer:getRegistry(),
        "CameraTransform",
        cameraTransform:getPosition(),
        cameraTransform:getRotation(),
        cameraTransform:getScale());

controllableObject:getUpdater():setParent(cameraTransformObject):sendUpdate()

lightObject:getUpdater():setParent(cameraTransformObject):sendUpdate()
cameraObject:getUpdater():setParent(cameraTransformObject):sendUpdate()

local basicMaterial = create_basic_material(mainSceneLayer)

mainSceneLayer:getGcsSystems():add(luajava.newInstance("com.boc_dev.lge_systems.boids.BoidSystem"))

local random = luajava.newInstance("java.util.Random")

for i = 1, 10 do

    for j = 1, 10 do

        for k = 1, 10 do


            local build = transformBuilder:reset():setPosition(luajava.newInstance( "com.boc_dev.maths.objects.vector.Vec3f", i * 4, j * 4, k * 4)):build();


            local newTransformObject = luajava.newInstance("com.boc_dev.lge_model.generated.components.TransformObject",
                    mainSceneLayer:getRegistry(),
                    "TransformObject" .. i,
                    build:getPosition(),
                    build:getRotation(),
                    build:getScale());

            local newGeometryObject = luajava.newInstance("com.boc_dev.lge_model.generated.components.GeometryObject",
                    mainSceneLayer:getRegistry(),
                    "Geometry" .. i,
                    MATRIX_4F.Identity,
                    basicMaterial,
                    "DEFAULT_SPHERE"
            );

            local boidObject = luajava.newInstance("com.boc_dev.lge_model.generated.components.BoidObject",
                    mainSceneLayer:getRegistry(),
                    "Boid" .. i,
                    0.001,
                    0.1,
                    luajava.newInstance( "com.boc_dev.maths.objects.vector.Vec3f", random:nextInt(10) - 5, random:nextInt(10) - 5, random:nextInt(10) - 5),
                    400,
                    10,
                    10,
                    0.001,
                    2,
                    50,
                    VEC3F.ZERO,
                    0.001
            );
            newGeometryObject:getUpdater():setParent(newTransformObject):sendUpdate();
            boidObject:getUpdater():setParent(newTransformObject):sendUpdate();

        end
    end
end

lightObject:getUpdater():setParent(cameraTransformObject):sendUpdate();
cameraObject:getUpdater():setParent(cameraTransformObject):sendUpdate();
controllableObject:getUpdater():setParent(cameraTransformObject):sendUpdate();

local wip = luajava.newInstance("com.boc_dev.graphics_library.WindowInitialisationParametersBuilder")
wip:setLockCursor(true):setWindowWidth(1000):setWindowHeight(800):setDebug(true);

local sceneLayers = luajava.newInstance("java.util.ArrayList")
sceneLayers:add(mainSceneLayer);

local gameLoop = luajava.newInstance("com.boc_dev.lge_core.GameLoop",
        sceneLayers,
        wip:build()
)

gameLoop:start();