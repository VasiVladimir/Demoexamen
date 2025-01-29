package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginForm extends JFrame {
    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton loginButton;


    public LoginForm() {
        setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());

        setTitle("Авторизация");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);


        JPanel panel = new JPanel();
        panel.setBackground(AppStyle.BACKGROUND_COLOR);
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("Авторизация", SwingConstants.CENTER);
        titleLabel.setFont(AppStyle.HEADER_FONT);
        titleLabel.setBounds(100, 20, 200, 30);
        panel.add(titleLabel);

        JLabel loginLabel = new JLabel("Логин:");
        loginLabel.setBounds(50, 70, 80, 25);
        panel.add(loginLabel);

        loginField = new JTextField();
        loginField.setBounds(130, 70, 200, 30);
        panel.add(loginField);

        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setBounds(50, 110, 80, 25);
        panel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(130, 110, 200, 30);
        panel.add(passwordField);

        loginButton = new JButton("Войти");
        loginButton.setBounds(130, 160, 200, 35);
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(AppStyle.BUTTON_COLOR);
        loginButton.setFont(AppStyle.MAIN_FONT);
        panel.add(loginButton);

        add(panel);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticateUser();
            }
        });
    }

    private void authenticateUser() {
        String login = loginField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT ФИО, Должность FROM public.\"Сотрудники\" WHERE Логин = ? AND Пароль = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, login);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String fullName = rs.getString("ФИО");
                    String position = rs.getString("Должность");

                    // Проверка на должность и открытие соответствующего окна
                    if ("Поставщик".equals(position)) {
                        new SupplierWindow(fullName); // Открытие окна для Поставщика
                        dispose(); // Закрываем окно авторизации
                    } else if ("Менеджер".equals(position)) {
                        new MainWindow(fullName); // Открытие окна для Менеджера
                        dispose(); // Закрываем окно авторизации
                    } else {
                        JOptionPane.showMessageDialog(this, "Доступ запрещен! Неверная должность.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Неверный логин или пароль!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка подключения к базе данных!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }



}
