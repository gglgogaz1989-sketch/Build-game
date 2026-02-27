// BuildingGame/app/src/main/java/com/building/game/MainActivity.java
package com.building.game;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.jme3.app.AndroidHarness;

public class MainActivity extends AppCompatActivity {
    
    private Game game;
    private AndroidHarness harness;
    private TextView txtCurrentTool;
    private TextView txtVersion; // НОВЫЙ
    private Button btnWrench, btnTrowel, btnBrick;
    private View joystickView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Инициализация UI
        txtCurrentTool = findViewById(R.id.txtCurrentTool);
        txtVersion = new TextView(this); // СОЗДАЁМ НОВЫЙ ТЕКСТ
        btnWrench = findViewById(R.id.btnWrench);
        btnTrowel = findViewById(R.id.btnTrowel);
        btnBrick = findViewById(R.id.btnBrick);
        joystickView = findViewById(R.id.joystickContainer);
        
        // Добавляем версию на экран
        txtVersion.setText("Версия: build10testing");
        txtVersion.setTextColor(0xFFFFFFFF);
        txtVersion.setTextSize(16f);
        FrameLayout glSurfaceFrame = findViewById(R.id.glSurfaceView);
        glSurfaceFrame.addView(txtVersion);
        
        // Создание и запуск игры
        game = new Game(getAssets());
        harness = new AndroidHarness() {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                app = game;
                appClass = Game.class.getCanonicalName();
                screenOrientation = ScreenOrientation.Landscape;
                super.onCreate(savedInstanceState);
            }
        };
        
        harness.onCreate(savedInstanceState);
        glSurfaceFrame.addView(harness.getGLSurfaceView(), 0); // Добавляем ПОД версию
        
        // Настройка кнопок
        setupButtons();
        
        // Настройка джойстика
        setupJoystick();
    }
    
    private void setupButtons() {
        btnWrench.setOnClickListener(v -> {
            game.setCurrentTool(ToolType.WRENCH);
            txtCurrentTool.setText("Гаечный ключ");
        });
        
        btnTrowel.setOnClickListener(v -> {
            game.setCurrentTool(ToolType.TROWEL);
            txtCurrentTool.setText("Шпатель");
        });
        
        btnBrick.setOnClickListener(v -> {
            game.setCurrentTool(ToolType.BRICK);
            txtCurrentTool.setText("Кирпич");
        });
    }
    
    private void setupJoystick() {
        joystickView.setOnTouchListener((v, event) -> {
            float x = event.getX() / v.getWidth() * 2 - 1;
            float y = -(event.getY() / v.getHeight() * 2 - 1);
            
            if (event.getAction() == MotionEvent.ACTION_MOVE || 
                event.getAction() == MotionEvent.ACTION_DOWN) {
                float length = (float) Math.sqrt(x * x + y * y);
                if (length > 1) {
                    x /= length;
                    y /= length;
                }
                game.setJoystickInput(x, y);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                game.setJoystickInput(0, 0);
            }
            return true;
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        harness.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        harness.onPause();
    }
}
