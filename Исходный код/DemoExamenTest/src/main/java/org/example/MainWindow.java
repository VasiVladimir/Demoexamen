package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class MainWindow extends JFrame {
    private JPanel partnersPanel;
    private JTable requestsTable;
    private DefaultTableModel requestsTableModel;
    private JTable partnersTable;
    private DefaultTableModel partnersTableModel;
    private String managerFullName;

    public MainWindow(String fullName) {
        setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        this.managerFullName = fullName; // Сохраняем ФИО менеджера
        setTitle("Главное окно - " + fullName);
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Применяем стиль через AppStyle
        AppStyle.applyStyle(this);

        // Создание вкладок
        JTabbedPane tabbedPane = new JTabbedPane();

        // Вкладка "Заявки"
        JPanel requestsPanel = new JPanel();
        requestsPanel.setLayout(new BorderLayout());
        requestsPanel.setBackground(AppStyle.BACKGROUND_COLOR);

        // Создаем таблицу для заявок
        String[] requestsColumns = {"Продукция", "Оплата", "Статус", "Дата создания"};
        requestsTableModel = new DefaultTableModel(requestsColumns, 0);
        requestsTable = new JTable(requestsTableModel);
        AppStyle.applyTableStyle(requestsTable);

        JScrollPane requestsScrollPane = new JScrollPane(requestsTable);
        requestsPanel.add(requestsScrollPane, BorderLayout.CENTER);

        // Кнопка "Подтвердить оплату"
        JButton confirmPaymentButton = new JButton("Подтвердить оплату");
        AppStyle.applyButtonStyle(confirmPaymentButton);
        confirmPaymentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmPayment();
            }
        });

        JPanel requestsButtonPanel = new JPanel();
        requestsButtonPanel.setBackground(AppStyle.BACKGROUND_COLOR);
        requestsButtonPanel.add(confirmPaymentButton);
        requestsPanel.add(requestsButtonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Заявки", requestsPanel);

        // Вкладка "Партнеры"
        partnersPanel = new JPanel();
        partnersPanel.setLayout(new BoxLayout(partnersPanel, BoxLayout.Y_AXIS));
        partnersPanel.setBackground(AppStyle.BACKGROUND_COLOR);

        JScrollPane scrollPane = new JScrollPane(partnersPanel);
        scrollPane.setBorder(null);

        JPanel partnersContainer = new JPanel();
        partnersContainer.setLayout(new BorderLayout());
        partnersContainer.add(scrollPane, BorderLayout.CENTER);

        // Кнопка "Добавить нового партнера"
        JButton addPartnerButton = new JButton("Добавить нового партнера");
        AppStyle.applyButtonStyle(addPartnerButton);
        addPartnerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddPartnerDialog();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(AppStyle.BACKGROUND_COLOR);
        buttonPanel.add(addPartnerButton);
        partnersContainer.add(buttonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Партнеры", partnersContainer);

        // Добавляем вкладки в основное окно
        add(tabbedPane);

        // Загружаем данные из базы данных
        loadRequests();
        loadPartners();

        setVisible(true);

        // Внутри конструктора MainWindow добавьте новую вкладку:
        JPanel salesPanel = new JPanel();
        salesPanel.setLayout(new BorderLayout());
        salesPanel.setBackground(AppStyle.BACKGROUND_COLOR);

// Создаем таблицу для отображения данных о продажах
        String[] salesColumns = {"Наименование партнера", "Количество проданной продукции", "Дата продажи", "Продукция"};
        DefaultTableModel salesTableModel = new DefaultTableModel(salesColumns, 0);
        JTable salesTable = new JTable(salesTableModel);
        AppStyle.applyTableStyle(salesTable);

        JScrollPane salesScrollPane = new JScrollPane(salesTable);
        salesPanel.add(salesScrollPane, BorderLayout.CENTER);

// Загружаем данные о продажах из базы данных
        loadSalesData(salesTableModel);

// Добавляем вкладку "Продажи" в основной таб
        tabbedPane.addTab("Продажи", salesPanel);

        // Вкладка "Поставка материала"
        JPanel supplyMaterialPanel = new JPanel();
        supplyMaterialPanel.setLayout(new BorderLayout());
        supplyMaterialPanel.setBackground(AppStyle.BACKGROUND_COLOR);

// Таблица для отображения данных склада
        String[] supplyColumns = {"Номер склада", "Количество на складе", "Тип", "Наименование"};
        DefaultTableModel supplyTableModel = new DefaultTableModel(supplyColumns, 0);
        JTable supplyTable = new JTable(supplyTableModel);
        AppStyle.applyTableStyle(supplyTable);

        JScrollPane supplyScrollPane = new JScrollPane(supplyTable);
        supplyScrollPane.setBackground(Color.WHITE);
        supplyMaterialPanel.add(supplyScrollPane, BorderLayout.CENTER);

// Кнопка "Пополнить материалы"
        JButton replenishButton = new JButton("Пополнить материалы");
        AppStyle.applyButtonStyle(replenishButton);
        replenishButton.addActionListener(e -> openReplenishMaterialDialog(supplyTableModel));

        supplyMaterialPanel.add(replenishButton, BorderLayout.SOUTH);

// Добавляем вкладку "Поставка материала" в основной таб
        tabbedPane.addTab("Поставка материала", supplyMaterialPanel);

// Загрузка данных из таблицы "Склад"
        loadSupplyData(supplyTableModel);


    }

    private void loadSupplyData(DefaultTableModel tableModel) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Запрос с сортировкой по количеству на складе в порядке убывания
            String query = "SELECT Номер_склада, Количество_на_складе, Тип, Наименование FROM Склад ORDER BY Количество_на_складе DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Очищаем таблицу перед загрузкой новых данных
            tableModel.setRowCount(0);

            while (rs.next()) {
                int warehouseNumber = rs.getInt("Номер_склада");
                int quantity = rs.getInt("Количество_на_складе");
                String type = rs.getString("Тип");
                String name = rs.getString("Наименование");

                // Добавляем строку в таблицу
                tableModel.addRow(new Object[]{warehouseNumber, quantity, type, name});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки данных со склада!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void openReplenishMaterialDialog(DefaultTableModel supplyTableModel) {
        JDialog dialog = new JDialog(this, "Пополнить материалы", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBackground(AppStyle.BACKGROUND_COLOR);

        JLabel supplierLabel = new JLabel("Поставщик:");
        JComboBox<String> supplierComboBox = new JComboBox<>();
        loadSuppliers(supplierComboBox);

        JLabel materialTypeLabel = new JLabel("Тип материала:");
        JComboBox<String> materialTypeComboBox = new JComboBox<>();

        JLabel quantityLabel = new JLabel("Количество:");
        JTextField quantityField = new JTextField();

        // Обновляем типы материала при выборе поставщика
        supplierComboBox.addActionListener(e -> loadMaterialTypesForSupplier((String) supplierComboBox.getSelectedItem(), materialTypeComboBox));

        JButton orderButton = new JButton("Заказать материал");
        AppStyle.applyButtonStyle(orderButton);
        orderButton.addActionListener(e -> {
            String selectedSupplier = (String) supplierComboBox.getSelectedItem();
            String selectedMaterialType = (String) materialTypeComboBox.getSelectedItem();
            String quantityText = quantityField.getText();

            if (selectedSupplier != null && selectedMaterialType != null && !quantityText.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityText);
                    addMaterialToWarehouse(selectedSupplier, selectedMaterialType, quantity);
                    loadSupplyData(supplyTableModel); // Обновляем таблицу склада
                    dialog.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Количество должно быть числом!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Заполните все поля!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(supplierLabel);
        panel.add(supplierComboBox);
        panel.add(materialTypeLabel);
        panel.add(materialTypeComboBox);
        panel.add(quantityLabel);
        panel.add(quantityField);
        panel.add(new JLabel());
        panel.add(orderButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }


    private void loadSuppliers(JComboBox<String> comboBox) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT Наименование FROM Поставщики";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            comboBox.removeAllItems();
            while (rs.next()) {
                comboBox.addItem(rs.getString("Наименование"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки поставщиков!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMaterialTypesForSupplier(String supplierName, JComboBox<String> comboBox) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT DISTINCT Тип FROM Поставщики WHERE Наименование = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, supplierName);
            ResultSet rs = stmt.executeQuery();

            comboBox.removeAllItems();
            while (rs.next()) {
                comboBox.addItem(rs.getString("Тип"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки типов материалов!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addMaterialToWarehouse(String supplierName, String materialType, int quantity) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Получаем id_Поставщика по имени
            String supplierQuery = "SELECT id_Поставщика FROM Поставщики WHERE Наименование = ?";
            PreparedStatement supplierStmt = conn.prepareStatement(supplierQuery);
            supplierStmt.setString(1, supplierName);
            ResultSet supplierRs = supplierStmt.executeQuery();

            if (supplierRs.next()) {
                int supplierId = supplierRs.getInt("id_Поставщика");

                // Обновляем количество на складе для конкретного типа и поставщика
                String updateQuery = "UPDATE Склад SET Количество_на_складе = Количество_на_складе + ? " +
                        "WHERE Тип = ? AND id_Поставщика = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, quantity);
                updateStmt.setString(2, materialType);
                updateStmt.setInt(3, supplierId);
                int rowsUpdated = updateStmt.executeUpdate();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "Материалы успешно пополнены!");
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка: соответствующий склад не найден!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка: поставщик не найден!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при пополнении склада!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadSalesData(DefaultTableModel tableModel) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT Наименование_партнера, Количество_проданной_продукции, Дата_продажи, Продукция FROM Продажи";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Очищаем таблицу перед добавлением новых данных
            tableModel.setRowCount(0);

            while (rs.next()) {
                String partnerName = rs.getString("Наименование_партнера");
                int soldQuantity = rs.getInt("Количество_проданной_продукции");
                Date saleDate = rs.getDate("Дата_продажи");
                String product = rs.getString("Продукция");

                // Добавляем строку в таблицу
                tableModel.addRow(new Object[]{partnerName, soldQuantity, saleDate, product});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки данных о продажах!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Метод для загрузки заявок из базы данных
    private void loadRequests() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT Продукция, Оплата, Статус, Дата_создания FROM Заявки";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Очищаем таблицу перед загрузкой новых данных
            requestsTableModel.setRowCount(0);

            while (rs.next()) {
                String product = rs.getString("Продукция");
                double payment = rs.getDouble("Оплата");
                String status = rs.getString("Статус");
                Date creationDate = rs.getDate("Дата_создания");

                // Добавляем строку в таблицу
                requestsTableModel.addRow(new Object[]{product, payment, status, creationDate});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки данных заявок!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для подтверждения оплаты
    private void confirmPayment() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow != -1) {
            String status = (String) requestsTable.getValueAt(selectedRow, 2); // Получаем статус заявки

            if ("Оплачено".equals(status)) {
                // Обновляем поле "ФИО_менеджера" в базе данных
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String query = "UPDATE Заявки SET ФИО_менеджера = ? WHERE Продукция = ? AND Статус = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, managerFullName);
                    stmt.setString(2, (String) requestsTable.getValueAt(selectedRow, 0)); // Продукция
                    stmt.setString(3, status); // Статус
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Вы подтвердили заявку!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Ошибка при обновлении заявки!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка: партнер ещё не оплатил заявку!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите заявку для подтверждения оплаты!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для загрузки партнеров из базы данных и отображения их в виде карточек
    private void loadPartners() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT Тип, Наименование_компании, ФИО_директора, Телефон, Рейтинг FROM Партнеры";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Очищаем панель перед загрузкой новых данных
            partnersPanel.removeAll();

            while (rs.next()) {
                String type = rs.getString("Тип");
                String companyName = rs.getString("Наименование_компании");
                String director = rs.getString("ФИО_директора");
                String phone = rs.getString("Телефон");
                int rating = rs.getInt("Рейтинг");

                // Рассчитываем скидку на основе рейтинга
                int discount = calculateDiscount(rating);

                // Создаем карточку партнера
                JPanel partnerCard = createPartnerCard(type, companyName, director, phone, rating, discount);
                partnersPanel.add(partnerCard);
                partnersPanel.add(Box.createVerticalStrut(10)); // Отступ между карточками
            }

            // Обновляем панель
            partnersPanel.revalidate();
            partnersPanel.repaint();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки данных партнеров!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для расчета скидки на основе рейтинга
    private int calculateDiscount(int rating) {
        if (rating < 5) return 0;
        if (rating < 8) return 5;
        if (rating < 10) return 10;
        return 15;
    }

    // Метод для создания карточки партнера
    private JPanel createPartnerCard(String type, String companyName, String director, String phone, int rating, int discount) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(750, 80));

        // Верхняя часть (Тип | Название | Скидка)
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel typeNameLabel = new JLabel(type + " | " + companyName + "    " + discount + "%");
        typeNameLabel.setFont(AppStyle.MAIN_FONT);

        topPanel.add(typeNameLabel, BorderLayout.WEST);

        // Нижняя часть (Директор, Телефон, Рейтинг)
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        JLabel directorLabel = new JLabel("Директор: " + director);
        JLabel phoneLabel = new JLabel("Телефон: " + phone);
        JLabel ratingLabel = new JLabel("Рейтинг: " + rating);

        bottomPanel.add(directorLabel);
        bottomPanel.add(phoneLabel);
        bottomPanel.add(ratingLabel);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(bottomPanel, BorderLayout.CENTER);

        return card;
    }

    // Метод для отображения диалогового окна добавления нового партнера
// Метод для отображения диалогового окна добавления нового партнера
    private void showAddPartnerDialog() {
        JDialog addPartnerDialog = new JDialog(this, "Добавить нового партнера", true);
        addPartnerDialog.setSize(400, 500);
        addPartnerDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(AppStyle.BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Заголовок
        JLabel titleLabel = new JLabel("Добавление нового партнера", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        // Поля ввода
        gbc.gridwidth = 1;
        gbc.gridx = 0;

        gbc.gridy++;
        panel.add(new JLabel("Тип:"), gbc);
        JTextField typeField = new JTextField();
        gbc.gridx = 1;
        panel.add(typeField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Наименование компании:"), gbc);
        JTextField companyNameField = new JTextField();
        gbc.gridx = 1;
        panel.add(companyNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Юридический адрес:"), gbc);
        JTextField legalAddressField = new JTextField();
        gbc.gridx = 1;
        panel.add(legalAddressField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("ИНН:"), gbc);
        JTextField innField = new JTextField();
        gbc.gridx = 1;
        panel.add(innField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("ФИО директора:"), gbc);
        JTextField directorField = new JTextField();
        gbc.gridx = 1;
        panel.add(directorField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Телефон:"), gbc);
        JTextField phoneField = new JTextField();
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Email:"), gbc);
        JTextField emailField = new JTextField();
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Рейтинг:"), gbc);
        JTextField ratingField = new JTextField();
        gbc.gridx = 1;
        panel.add(ratingField, gbc);

        // Кнопка "Добавить партнера"
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton addButton = new JButton("Добавить партнера");
        addButton.setFont(AppStyle.MAIN_FONT);
        addButton.setBackground(AppStyle.BUTTON_COLOR);
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPartner(
                        typeField.getText(),
                        companyNameField.getText(),
                        legalAddressField.getText(),
                        innField.getText(),
                        directorField.getText(),
                        phoneField.getText(),
                        emailField.getText(),
                        Integer.parseInt(ratingField.getText()),
                        null
                );
                addPartnerDialog.dispose();
            }
        });
        panel.add(addButton, gbc);

        addPartnerDialog.add(panel);
        addPartnerDialog.setVisible(true);
    }


    // Метод для добавления нового партнера в базу данных и обновления отображения
    private void addPartner(String type, String companyName, String legalAddress, String inn,
                            String director, String phone, String email, int rating, String legalAddressRepeat) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO Партнеры (Тип, Наименование_компании, Юридический_адрес, ИНН, ФИО_директора, Телефон, email, Рейтинг, Юридический_адрес_повторный) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, type);
            stmt.setString(2, companyName);
            stmt.setString(3, legalAddress);
            stmt.setString(4, inn);
            stmt.setString(5, director);
            stmt.setString(6, phone);
            stmt.setString(7, email);
            stmt.setInt(8, rating);
            stmt.setString(9, legalAddressRepeat);
            stmt.executeUpdate();

            // Обновляем отображение партнеров
            loadPartners();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка добавления партнера!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}