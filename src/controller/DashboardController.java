package controller;

import bll.AppointmentBLL;
import dao.impl.SqlAppointmentDAO;
import dao.impl.SqlReminderDAO;
import model.Appointment;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController
{

    // Khai báo kiểu chuẩn là Appointment
    @FXML
    private TableView<Appointment> tblAppointments;
    @FXML
    private TableColumn<Appointment, String> colId;
    @FXML
    private TableColumn<Appointment, String> colTitle;
    @FXML
    private TableColumn<Appointment, String> colLocation;
    @FXML
    private TableColumn<Appointment, String> colStart;
    @FXML
    private TableColumn<Appointment, String> colEnd;

    @FXML
    private Button btnRefresh;
    @FXML
    private Button btnAddApp;

    private AppointmentBLL appointmentBLL;
    private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();

    @FXML
    public void initialize()
    {
        System.out.println("UI: Đã load thành công giao diện Dashboard!");
        appointmentBLL = new AppointmentBLL(new SqlAppointmentDAO(), new SqlReminderDAO());

        setupColumns();
        loadDashboardData();
    }

    private void setupColumns()
    {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        colStart.setCellValueFactory(cellData -> {
            LocalDateTime startTime = cellData.getValue().getStartTime();
            return new SimpleStringProperty(startTime != null ? startTime.format(timeFormatter) : "");
        });

        colEnd.setCellValueFactory(cellData -> {
            LocalDateTime endTime = cellData.getValue().getEndTime();
            return new SimpleStringProperty(endTime != null ? endTime.format(timeFormatter) : "");
        });
    }

    private void loadDashboardData()
    {
        appointmentList.clear();
        try
        {
            List<Appointment> data = appointmentBLL.getAllAppointments();
            appointmentList.addAll(data);
            tblAppointments.setItems(appointmentList);
        } catch (Exception e)
        {
            System.err.println("Lỗi load dữ liệu Dashboard: " + e.getMessage());
        }
    }

    @FXML
    void handleRefresh(ActionEvent event)
    {
        System.out.println("UI: Đang tải lại dữ liệu Dashboard qua BLL...");
        loadDashboardData();
    }

    @FXML
    void showAddDialog(ActionEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chức năng mới");
        alert.setHeaderText(null);
        alert.setContentText("Tính năng gọi Form thêm Lịch từ Dashboard đang được phát triển!");
        alert.showAndWait();
    }
}