package org.example;


import javax.swing.*;
import java.awt.*;

public class AppStyle {
    // Основные стили
    public static final Color BACKGROUND_COLOR = new Color(244, 231, 205); // Бежевый фон
    public static final Color BUTTON_COLOR = new Color(101, 183, 114); // Зеленая кнопка
    public static final Font MAIN_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 22);

    // Метод для применения стилей ко всем компонентам
    public static void applyStyle(JFrame frame) {
        // Устанавливаем основной фон окна
        frame.getContentPane().setBackground(BACKGROUND_COLOR);
    }

    // Метод для применения стилей ко всем компонентам таблицы
    public static void applyTableStyle(JTable table) {
        table.setFont(MAIN_FONT);  // Применяем шрифт
        table.setRowHeight(25);     // Высота строк
        table.setBackground(Color.WHITE); // Белый фон таблицы
    }

    // Метод для применения стилей ко всем кнопкам
    public static void applyButtonStyle(JButton button) {
        button.setFont(MAIN_FONT);  // Шрифт для кнопок
        button.setBackground(BUTTON_COLOR);  // Цвет кнопки
        button.setForeground(Color.WHITE);  // Белый цвет текста на кнопке
    }
}
