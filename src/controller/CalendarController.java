package controller;

import bll.AppointmentBLL;
import dao.impl.SqlAppointmentDAO;
import dao.impl.SqlReminderDAO;
import model.Appointment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class CalendarController
{

    @FXML
    private GridPane calendarGrid;
    @FXML
    private ComboBox<Integer> cbMonth;
    @FXML
    private ComboBox<Integer> cbYear;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnClose;

    private boolean isInitializing = true;
    private LocalDate selectedDateFromGrid;

    // Khai báo BLL
    private AppointmentBLL appointmentBLL;

    @FXML
    public void initialize()
    {
        System.out.println("Đã load Giao diện Lịch!");

        // Khởi tạo BLL
        appointmentBLL = new AppointmentBLL(new SqlAppointmentDAO(), new SqlReminderDAO());

        for (int i = 1; i <= 12; i++) cbMonth.getItems().add(i);

        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 5; i <= currentYear + 10; i++) cbYear.getItems().add(i);

        cbMonth.setValue(LocalDate.now().getMonthValue());
        cbYear.setValue(currentYear);

        isInitializing = false;
        renderCalendarGrid();
    }

    @FXML
    void onDateSelectionChanged(ActionEvent event)
    {
        if (!isInitializing) renderCalendarGrid();
    }

    private void renderCalendarGrid()
    {
        calendarGrid.getChildren().clear();
        LocalDate today = LocalDate.now();
        YearMonth currentViewMonth = YearMonth.of(cbYear.getValue(), cbMonth.getValue());

        // 1. Lấy toàn bộ dữ liệu lịch hẹn từ DB thông qua BLL
        List<Appointment> allAppointments = appointmentBLL.getAllAppointments();

        // 2. Vẽ tiêu đề (Th 2 -> CN)
        String[] daysOfWeek = {"Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7", "CN"};
        int currentDowIndex = today.getDayOfWeek().getValue() - 1;

        for (int i = 0; i < 7; i++)
        {
            Label lblDay = new Label(daysOfWeek[i]);
            String headerStyle = "-fx-font-weight: bold; -fx-font-size: 14px; ";
            if (i == currentDowIndex && currentViewMonth.equals(YearMonth.from(today)))
            {
                headerStyle += "-fx-text-fill: red;";
            } else
            {
                headerStyle += "-fx-text-fill: #2c3e50;";
            }
            lblDay.setStyle(headerStyle);
            lblDay.setMaxWidth(Double.MAX_VALUE);
            lblDay.setAlignment(Pos.CENTER);
            calendarGrid.add(lblDay, i, 0);
        }

        // 3. Vẽ các ô ngày
        LocalDate firstDayOfMonth = currentViewMonth.atDay(1);
        int col = firstDayOfMonth.getDayOfWeek().getValue() - 1;
        int row = 1;
        int daysInMonth = currentViewMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++)
        {
            Button btnDay = new Button(String.valueOf(day));
            btnDay.setMaxWidth(Double.MAX_VALUE);
            btnDay.setMaxHeight(Double.MAX_VALUE);

            String style = "-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px;";

            // Tô ngày hiện tại tinh tế hơn (Chữ đỏ, viền đỏ, không đổ nền)
            if (day == today.getDayOfMonth() && currentViewMonth.equals(YearMonth.from(today)))
            {
                style += " -fx-text-fill: red; -fx-font-weight: bold; -fx-border-width: 1px; -fx-border-radius: 5px;";
            }

            // TÍNH NĂNG MỚI: Kiểm tra xem ngày này có sự kiện không
            LocalDate currentDate = LocalDate.of(cbYear.getValue(), cbMonth.getValue(), day);
            boolean hasEvent = allAppointments.stream()
                    .anyMatch(app -> app.getStartTime().toLocalDate().equals(currentDate));

            // Nếu có sự kiện, đóng khung màu xanh lá cây
            if (hasEvent)
            {
                style += " -fx-border-color: #2ecc71; -fx-border-width: 2px; -fx-border-radius: 20px;";
            }

            btnDay.setStyle(style);

            int selectedDay = day;
            btnDay.setOnAction(e -> {
                selectedDateFromGrid = LocalDate.of(cbYear.getValue(), cbMonth.getValue(), selectedDay);
                System.out.println("Đã chọn ngày: " + selectedDateFromGrid);
            });

            calendarGrid.add(btnDay, col, row);
            col++;
            if (col > 6)
            {
                col = 0;
                row++;
            }
        }
    }

    @FXML
    void onAddClick(ActionEvent event)
    {
        if (selectedDateFromGrid == null)
        {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một ngày trên lịch trước!");
            return;
        }

        try
        {
            java.net.URL fxmlUrl = getClass().getResource("/view/AppointmentView.fxml");
            if (fxmlUrl == null)
            {
                System.err.println("LỖI: Không tìm thấy file '/view/AppointmentView.fxml'");
                return;
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlUrl);
            javafx.scene.Parent root = loader.load();

            AppointmentController addController = loader.getController();
            addController.setSelectedDate(selectedDateFromGrid);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Chi tiết cuộc hẹn");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // Chờ người dùng đóng form Thêm mới
            stage.showAndWait();

            // Tự động vẽ lại lịch để cập nhật khung màu xanh cho sự kiện vừa thêm
            renderCalendarGrid();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    void onViewListClick(ActionEvent event)
    {
        try
        {
            java.net.URL fxmlUrl = getClass().getResource("/view/AppointmentListView.fxml");
            if (fxmlUrl == null) return;

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlUrl);
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Danh sách các buổi hẹn");
            stage.setScene(new javafx.scene.Scene(root));

            stage.showAndWait();

            // Vẽ lại lịch khi đóng bảng danh sách (lỡ người dùng có xóa sự kiện)
            renderCalendarGrid();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    void onCloseClick(ActionEvent event)
    {
        System.exit(0);
    }

    private void showAlert(Alert.AlertType type, String title, String content)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}