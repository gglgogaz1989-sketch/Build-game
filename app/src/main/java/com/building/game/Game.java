// BuildingGame/app/src/main/java/com/building/game/Game.java
package com.building.game;

import android.content.res.AssetManager;
import com.jme3.app.AndroidHarness;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

public class Game extends SimpleApplication {
    
    private ToolType currentTool = ToolType.BRICK;
    private Spatial wrenchModel;
    private Spatial trowelModel;
    private Spatial brickModel;
    private BitmapText toolText;
    private Spatial selectedForAttach = null;
    private AssetManager androidAssetManager;
    
    // Переменные для управления от MainActivity
    private float moveX = 0;
    private float moveY = 0;
    
    public Game(AssetManager assetManager) {
        this.androidAssetManager = assetManager;
    }
    
    @Override
    public void simpleInitApp() {
        // Настройка камеры
        flyCam.setEnabled(false);
        cam.setLocation(new Vector3f(0, 5, 20));
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
        
        // Создание платформы
        createPlatform();
        
        // Загрузка моделей
        loadModels();
        
        // Создание текста
        toolText = new BitmapText(guiFont);
        toolText.setLocalTranslation(10, settings.getHeight() - 50, 0);
        guiNode.attachChild(toolText);
        
        updateToolText();
    }
    
    private void createPlatform() {
        Box floorBox = new Box(50f, 1f, 50f);
        Geometry floor = new Geometry("Platform", floorBox);
        Material floorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floorMat.setColor("Color", ColorRGBA.Green);
        floor.setMaterial(floorMat);
        floor.setLocalTranslation(0, -1, 0);
        rootNode.attachChild(floor);
    }
    
    private void loadModels() {
        try {
            wrenchModel = assetManager.loadModel("Models/wrench.obj");
            wrenchModel.setLocalScale(0.2f);
            wrenchModel.setLocalTranslation(-2, 1, 0);
            rootNode.attachChild(wrenchModel);
            
            trowelModel = assetManager.loadModel("Models/trowel.obj");
            trowelModel.setLocalScale(0.5f);
            trowelModel.setLocalTranslation(0, 1, 0);
            rootNode.attachChild(trowelModel);
            
            brickModel = assetManager.loadModel("Models/brick.obj");
            brickModel.setLocalScale(0.5f);
            brickModel.setLocalTranslation(2, 1, 0);
            rootNode.attachChild(brickModel);
            
            // Добавим несколько кирпичей для теста
            for (int i = 0; i < 5; i++) {
                Spatial brick = assetManager.loadModel("Models/brick.obj");
                brick.setLocalScale(0.5f);
                brick.setLocalTranslation(i * 3 + 5, 1, 0);
                rootNode.attachChild(brick);
            }
        } catch (Exception e) {
            System.out.println("Ошибка загрузки моделей: " + e.getMessage());
        }
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        // Движение камеры от джойстика
        if (moveX != 0 || moveY != 0) {
            Vector3f camDir = cam.getDirection().clone();
            Vector3f camLeft = cam.getLeft().clone();
            camDir.y = 0;
            camLeft.y = 0;
            camDir.normalizeLocal();
            camLeft.normalizeLocal();
            
            Vector3f walkDirection = camDir.mult(moveY).add(camLeft.mult(moveX));
            walkDirection.multLocal(10 * tpf);
            
            cam.setLocation(cam.getLocation().add(walkDirection));
        }
    }
    
    public void handleScreenClick(float x, float y) {
        // Конвертация координат экрана в мир
        Vector2f screenPos = new Vector2f(x, y);
        Vector3f origin = cam.getWorldCoordinates(screenPos, 0.0f);
        Vector3f direction = cam.getWorldCoordinates(screenPos, 1.0f).subtractLocal(origin).normalizeLocal();
        
        Ray ray = new Ray(origin, direction);
        CollisionResults results = new CollisionResults();
        rootNode.collideWith(ray, results);
        
        if (results.size() > 0) {
            Geometry target = results.getClosestCollision().getGeometry();
            Vector3f contactPoint = results.getClosestCollision().getContactPoint();
            
            switch (currentTool) {
                case WRENCH:
                    useWrench(target);
                    break;
                case TROWEL:
                    useTrowel(target);
                    break;
                case BRICK:
                    spawnBrick(contactPoint);
                    break;
            }
        }
    }
    
    private void useWrench(Geometry target) {
        System.out.println("Гаечный ключ на: " + target.getName());
        // Здесь будет меню
    }
    
    private void useTrowel(Geometry target) {
        if (selectedForAttach == null) {
            selectedForAttach = target;
            target.getMaterial().setColor("Color", ColorRGBA.Yellow);
        } else {
            System.out.println("Прикрепляем " + selectedForAttach.getName() + " к " + target.getName());
            selectedForAttach.getMaterial().setColor("Color", ColorRGBA.White);
            selectedForAttach = null;
        }
    }
    
    private void spawnBrick(Vector3f position) {
        try {
            Spatial brick = assetManager.loadModel("Models/brick.obj");
            brick.setLocalScale(0.5f);
            brick.setLocalTranslation(position.add(0, 1, 0));
            rootNode.attachChild(brick);
        } catch (Exception e) {
            System.out.println("Ошибка спавна");
        }
    }
    
    public void setCurrentTool(ToolType tool) {
        this.currentTool = tool;
        updateToolText();
    }
    
    private void updateToolText() {
        String toolName = "";
        switch (currentTool) {
            case WRENCH: toolName = "Гаечный ключ"; break;
            case TROWEL: toolName = "Шпатель"; break;
            case BRICK: toolName = "Кирпич"; break;
        }
        if (toolText != null) {
            toolText.setText("Инструмент: " + toolName);
        }
    }
    
    public void setJoystickInput(float x, float y) {
        this.moveX = x;
        this.moveY = y;
    }
  }
