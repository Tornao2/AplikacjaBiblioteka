package com.project.crud.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

public class AdminReportsController {
    @FXML private Label activeLoansLabel, totalPenaltiesLabel, totalUsersLabel;
    @FXML private Label totalSalariesLabel;
    @FXML private Label avgTenureLabel;
    @FXML private PieChart genrePieChart;
    @FXML private BarChart<String, Number> loansBarChart;

    @FXML
    public void initialize() {
        setupPieChart();
        setupBarChart();
        activeLoansLabel.setText("158");
        totalPenaltiesLabel.setText("1,240.50");
        totalUsersLabel.setText("842");
        calculateStaffMetrics();
    }

    private void setupPieChart() {
        PieChart.Data slice1 = new PieChart.Data("Fantastyka", 45);
        PieChart.Data slice2 = new PieChart.Data("Kryminał", 30);
        PieChart.Data slice3 = new PieChart.Data("Nauka", 15);
        PieChart.Data slice4 = new PieChart.Data("Inne", 10);
        genrePieChart.getData().addAll(slice1, slice2, slice3, slice4);
    }

    private void setupBarChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Liczba wypożyczeń");
        series.getData().add(new XYChart.Data<>("Paź", 120));
        series.getData().add(new XYChart.Data<>("Lis", 150));
        series.getData().add(new XYChart.Data<>("Gru", 90));
        series.getData().add(new XYChart.Data<>("Sty", 200));
        series.getData().add(new XYChart.Data<>("Lut", 180));
        series.getData().add(new XYChart.Data<>("Mar", 220));
        loansBarChart.getData().add(series);
    }

    private void calculateStaffMetrics() {
        double totalMonthlySalaries = 45200.00;
        totalSalariesLabel.setText(String.format("%.2f", totalMonthlySalaries));
        double avgYears = 3.5;
        avgTenureLabel.setText(String.format("%.1f lat", avgYears));
    }
}