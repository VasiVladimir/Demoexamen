package org.example;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SupplierWindow extends JFrame {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> typeComboBox;
    private JTextField searchField;
    private JTextField priceFromField;
    private JTextField priceToField;
    private JComboBox<String> productComboBox;
    private JTextField quantityField;
    private DefaultTableModel orderTableModel;
    private JTable orderTable;
    private DefaultTableModel requestTableModel;  // Модель для таблицы заявок

    public SupplierWindow(String fullName) {
        setTitle("Поставщик - " + fullName);
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Применяем стиль через AppStyle
        AppStyle.applyStyle(this);

        // Создание вкладок
        JTabbedPane tabbedPane = new JTabbedPane();

        // Вкладка 1: "Просмотр каталога продукции"
        JPanel productCatalogPanel = new JPanel();
        productCatalogPanel.setLayout(new BorderLayout(10, 10));
        productCatalogPanel.setBackground(AppStyle.BACKGROUND_COLOR);

        // Панель с фильтрацией и поиском
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2, 2, 10, 10));
        topPanel.setBackground(AppStyle.BACKGROUND_COLOR);

        // Фильтрация по типу
        JLabel typeLabel = new JLabel("Фильтрация по типу");
        typeComboBox = new JComboBox<>(new String[]{"Все типы"});
        topPanel.add(typeLabel);
        topPanel.add(typeComboBox);

        // Поиск по названию
        JLabel searchLabel = new JLabel("Поиск по названию");
        searchField = new JTextField();
        topPanel.add(searchLabel);
        topPanel.add(searchField);

        // Фильтрация по цене (от и до)
        JLabel priceLabel = new JLabel("Фильтрация по цене");
        JPanel pricePanel = new JPanel();
        pricePanel.setLayout(new GridLayout(1, 2, 10, 10));
        priceFromField = new JTextField();
        priceToField = new JTextField();
        pricePanel.add(new JLabel("От"));
        pricePanel.add(priceFromField);
        pricePanel.add(new JLabel("До"));
        pricePanel.add(priceToField);
        topPanel.add(priceLabel);
        topPanel.add(pricePanel);

        productCatalogPanel.add(topPanel, BorderLayout.NORTH);

        // Создаем таблицу
        String[] columns = {"Артикул", "Название", "Тип", "Цена"};
        tableModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(tableModel);
        // Применяем стиль таблицы
        AppStyle.applyTableStyle(productTable);

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBackground(Color.WHITE);
        productCatalogPanel.add(scrollPane, BorderLayout.CENTER);

        // Добавляем вкладку в JTabbedPane
        tabbedPane.addTab("Просмотр каталога продукции", productCatalogPanel);

        // Слушатели событий для фильтрации
        typeComboBox.addActionListener(e -> filterProducts());
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterProducts();
            }
        });
        priceFromField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterProducts();
            }
        });
        priceToField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterProducts();
            }
        });

        // Вкладка 2: "Оставить заявку"
        JPanel leaveRequestPanel = new JPanel();
        leaveRequestPanel.setLayout(new BorderLayout());

        // Панель для добавления товара
        JPanel addProductPanel = new JPanel();
        addProductPanel.setLayout(new GridLayout(2, 2, 10, 10));
        addProductPanel.setBackground(AppStyle.BACKGROUND_COLOR);

        // Выпадающий список для выбора товара
        JLabel productLabel = new JLabel("Товар");
        productComboBox = new JComboBox<>();
        loadProducts(); // Загрузка продуктов из базы
        addProductPanel.add(productLabel);
        addProductPanel.add(productComboBox);

        // Поле для ввода количества
        JLabel quantityLabel = new JLabel("Количество");
        quantityField = new JTextField();
        addProductPanel.add(quantityLabel);
        addProductPanel.add(quantityField);

        // Кнопка для добавления товара в таблицу
        JButton addButton = new JButton("Добавить");
        AppStyle.applyButtonStyle(addButton);
        addButton.addActionListener(e -> addProductToTable());
        addProductPanel.add(addButton);

        leaveRequestPanel.add(addProductPanel, BorderLayout.NORTH);

        // Таблица для отображения добавленных товаров
        String[] orderColumns = {"Артикул", "Название", "Тип", "Количество", "Цена", "Скидка", "Цена со скидкой"};
        orderTableModel = new DefaultTableModel(orderColumns, 0);
        orderTable = new JTable(orderTableModel);
        AppStyle.applyTableStyle(orderTable);

        JScrollPane orderScrollPane = new JScrollPane(orderTable);
        orderScrollPane.setBackground(Color.WHITE);
        leaveRequestPanel.add(orderScrollPane, BorderLayout.CENTER);

        // Кнопка для отправки заявки
        JButton submitButton = new JButton("Оставить заявку");
        AppStyle.applyButtonStyle(submitButton);
        submitButton.addActionListener(e -> createOrder());
        leaveRequestPanel.add(submitButton, BorderLayout.SOUTH);

        // Добавляем вкладку в JTabbedPane
        tabbedPane.addTab("Оставить заявку", leaveRequestPanel);

        // Вкладка 3: "Заявки"
        JPanel requestsPanel = new JPanel();
        requestsPanel.setLayout(new BorderLayout());

        // Таблица для отображения заявок
        String[] requestColumns = {"Номер", "Статус", "Дата", "Оплата"};
        requestTableModel = new DefaultTableModel(requestColumns, 0);  // Модель для таблицы заявок
        JTable requestTable = new JTable(requestTableModel);
        AppStyle.applyTableStyle(requestTable);

        JScrollPane requestScrollPane = new JScrollPane(requestTable);
        requestScrollPane.setBackground(Color.WHITE);
        requestsPanel.add(requestScrollPane, BorderLayout.CENTER);

        // Кнопки управления заявками
        JPanel requestButtonsPanel = new JPanel();
        requestButtonsPanel.setLayout(new GridLayout(1, 2, 10, 10));
        JButton deleteButton = new JButton("Удалить заявку");
        AppStyle.applyButtonStyle(deleteButton);
        requestButtonsPanel.add(deleteButton);

        JButton payButton = new JButton("Произвести оплату");
        AppStyle.applyButtonStyle(payButton);
        requestButtonsPanel.add(payButton);

        requestsPanel.add(requestButtonsPanel, BorderLayout.SOUTH);

        // Добавляем вкладку в JTabbedPane
        tabbedPane.addTab("Заявки", requestsPanel);

        // Основное окно
        add(tabbedPane);
        setVisible(true);

        // Загрузка данных из базы данных
        loadTypes();
        loadProducts();
        loadRequests();  // Загрузка заявок

        deleteButton.addActionListener(e -> deleteSelectedRequest(requestTable));
        payButton.addActionListener(e -> markAsPaid(requestTable));
    }

    // Метод для загрузки уникальных типов продукции из базы данных
    private void loadTypes() {
        Set<String> types = new HashSet<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT DISTINCT Тип FROM Продукция";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Добавляем типы в набор (для удаления дубликатов)
            types.add("Все типы");
            while (rs.next()) {
                types.add(rs.getString("Тип"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Добавляем типы в JComboBox
        for (String type : types) {
            typeComboBox.addItem(type);
        }
    }

    // Метод для загрузки данных из базы данных с использованием DatabaseConnection
    private void loadProducts() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT Артикул, Наименование, Тип, Минимальная_стоимость_для_партнер FROM Продукция";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String article = rs.getString("Артикул");
                String name = rs.getString("Наименование");
                String type = rs.getString("Тип");
                double price = rs.getDouble("Минимальная_стоимость_для_партнер");
                productComboBox.addItem(article + " - " + name);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Метод для фильтрации данных
    private void filterProducts() {
        String typeFilter = (String) typeComboBox.getSelectedItem();
        String nameFilter = searchField.getText().toLowerCase();
        String priceFromText = priceFromField.getText();
        String priceToText = priceToField.getText();

        try (Connection conn = DatabaseConnection.getConnection()) {
            StringBuilder query = new StringBuilder("SELECT Артикул, Наименование, Тип, Минимальная_стоимость_для_партнер FROM Продукция WHERE 1=1");

            if (!typeFilter.equals("Все типы")) {
                query.append(" AND Тип = '").append(typeFilter).append("'"); // Фильтрация по типу
            }
            if (!nameFilter.isEmpty()) {
                query.append(" AND LOWER(Наименование) LIKE '%").append(nameFilter).append("%'"); // Фильтрация по названию
            }
            if (!priceFromText.isEmpty()) {
                query.append(" AND Минимальная_стоимость_для_партнер >= ").append(Double.parseDouble(priceFromText)); // Фильтрация по цене
            }
            if (!priceToText.isEmpty()) {
                query.append(" AND Минимальная_стоимость_для_партнер <= ").append(Double.parseDouble(priceToText)); // Фильтрация по цене
            }
            query.append(" ORDER BY Минимальная_стоимость_для_партнер ASC");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query.toString());

            // Очистка таблицы перед обновлением
            tableModel.setRowCount(0);

            while (rs.next()) {
                String article = rs.getString("Артикул");
                String name = rs.getString("Наименование");
                String type = rs.getString("Тип");
                double price = rs.getDouble("Минимальная_стоимость_для_партнер");
                tableModel.addRow(new Object[]{article, name, type, price});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Метод для добавления товара в таблицу заявки с случайной скидкой
    private void addProductToTable() {
        String selectedProduct = (String) productComboBox.getSelectedItem();
        String quantityText = quantityField.getText();

        if (selectedProduct != null && !quantityText.isEmpty()) {
            // Извлекаем артикул и название товара из выбранного пункта
            String article = selectedProduct.split(" - ")[0];
            String name = selectedProduct.split(" - ")[1];
            int quantity = Integer.parseInt(quantityText);

            // Получаем цену из базы данных
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "SELECT Минимальная_стоимость_для_партнер FROM Продукция WHERE Артикул = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, article);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    double price = rs.getDouble("Минимальная_стоимость_для_партнер");

                    // Генерация случайной скидки от 1% до 10%
                    double discount = Math.random() * 10 + 1; // Скидка от 1 до 10

                    // Расчет цены со скидкой
                    double finalPrice = price - (price * discount / 100);

                    // Добавляем строку в таблицу
                    orderTableModel.addRow(new Object[]{article, name, "Тип", quantity, price, discount, finalPrice});
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Метод для создания заявки в базе данных
    private void createOrder() {
        int rowCount = orderTable.getRowCount();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO Заявки (Дата_создания, Оплата, Статус, Продукция, id_Продукции, ФИО_менеджера) VALUES (?, ?, ?, ?, ?, ?)";

            for (int i = 0; i < rowCount; i++) {
                // Получаем данные из таблицы
                String article = (String) orderTable.getValueAt(i, 0);
                String name = (String) orderTable.getValueAt(i, 1);
                String type = (String) orderTable.getValueAt(i, 2);
                int quantity = (Integer) orderTable.getValueAt(i, 3);
                double price = (Double) orderTable.getValueAt(i, 4);
                double discount = (Double) orderTable.getValueAt(i, 5);
                double finalPrice = (Double) orderTable.getValueAt(i, 6);

                // Получаем id продукции из базы данных
                String productIdQuery = "SELECT id_Продукции FROM Продукция WHERE Артикул = ?";
                PreparedStatement productStmt = conn.prepareStatement(productIdQuery);
                productStmt.setString(1, article);
                ResultSet rs = productStmt.executeQuery();

                if (rs.next()) {
                    int productId = rs.getInt("id_Продукции");

                    // Добавляем заявку в базу данных с оплатой = цене со скидкой
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setDate(1, new java.sql.Date(new Date().getTime()));  // Текущая дата
                    pstmt.setDouble(2, finalPrice);  // Сохраняем цену со скидкой в поле "Оплата"
                    pstmt.setString(3, "Не оплачено");  // Статус
                    pstmt.setString(4, name);  // Продукция
                    pstmt.setInt(5, productId);  // id_Продукции
                    pstmt.setString(6, "");  // ФИО_менеджера
                    pstmt.executeUpdate();
                }
            }
            JOptionPane.showMessageDialog(this, "Заявка успешно создана!");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Метод для загрузки заявок из базы данных
    private void loadRequests() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id_Заявки, Статус, Дата_создания, Оплата FROM Заявки";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("id_Заявки");
                String status = rs.getString("Статус");
                Date creationDate = rs.getDate("Дата_создания");
                double payment = rs.getDouble("Оплата");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = sdf.format(creationDate);
                requestTableModel.addRow(new Object[]{id, status, formattedDate, payment});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Метод для удаления заявки
    private void deleteSelectedRequest(JTable requestTable) {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow != -1) {
            int requestId = (int) requestTable.getValueAt(selectedRow, 0);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM Заявки WHERE id_Заявки = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, requestId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Удаление строки из таблицы
                    requestTableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Заявка успешно удалена!");
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка при удалении заявки.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите заявку для удаления.");
        }
    }

    // Метод для изменения статуса заявки на "Оплачено"
    private void markAsPaid(JTable requestTable) {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow != -1) {
            int requestId = (int) requestTable.getValueAt(selectedRow, 0);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE Заявки SET Статус = 'Оплачено' WHERE id_Заявки = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, requestId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Обновление статуса в таблице
                    requestTable.setValueAt("Оплачено", selectedRow, 1);
                    JOptionPane.showMessageDialog(this, "Статус заявки обновлен на 'Оплачено'");
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка при обновлении статуса.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите заявку для оплаты.");
        }
    }
}