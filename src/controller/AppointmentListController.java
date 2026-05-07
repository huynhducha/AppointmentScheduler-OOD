package controller;

import bll.AppointmentBLL;
import dao.impl.SqlAppointmentDAO;
import dao.impl.SqlReminderDAO;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import model.Appointment;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AppointmentListController
{

    @FXML
    private TableView<Appointment> tblAppointments;
    @FXML
    private TableColumn<Appointment, String> colId;
    @FXML
    private TableColumn<Appointment, String> colTitle;
    @FXML
    private TableColumn<Appointment, String> colLocation;
    @FXML
    private TableColumn<Appointment, String> colDate;
    @FXML
    private TableColumn<Appointment, String> colStart;
    @FXML
    private TableColumn<Appointment, String> colEnd;
    @FXML
    private TableColumn<Appointment, String> colType;

    // Sử dụng BLL thay vì DAO
    private AppointmentBLL appointmentBLL;
    private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();

    @FXML
    public void initialize()
    {
        appointmentBLL = new AppointmentBLL(new SqlAppointmentDAO(), new SqlReminderDAO());
        setupTableColumns();
        loadDataFromDatabase();

        // --- CODE MỚI: Bắt sự kiện Double-click trên bảng ---
        tblAppointments.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Nếu nhấn đúp (2 lần)
                showAppointmentDetails();
            }
        });
    }

    private void setupTableColumns()
    {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        colDate.setCellValueFactory(cellData -> {
            LocalDateTime startTime = cellData.getValue().getStartTime();
            return new SimpleStringProperty(startTime != null ? startTime.format(dateFormatter) : "");
        });

        colStart.setCellValueFactory(cellData -> {
            LocalDateTime startTime = cellData.getValue().getStartTime();
            return new SimpleStringProperty(startTime != null ? startTime.format(timeFormatter) : "");
        });

        colEnd.setCellValueFactory(cellData -> {
            LocalDateTime endTime = cellData.getValue().getEndTime();
            return new SimpleStringProperty(endTime != null ? endTime.format(timeFormatter) : "");
        });

        // --- XỬ LÝ ĐỔ DỮ LIỆU CHO CỘT LOẠI (CÁ NHÂN / NHÓM) ---
        colType.setCellValueFactory(cellData -> {
            String appointmentId = cellData.getValue().getId();

            // Gọi BLL để lấy danh sách người tham gia của cái ID này
            List<User> participants = appointmentBLL.getParticipants(appointmentId);

            // Nếu có người tham gia -> Là họp nhóm -> In ra số lượng luôn!
            if (participants != null && !participants.isEmpty()) {
                return new SimpleStringProperty("Nhóm (" + participants.size() + ")");
            }
            // Nếu không có ai -> Là cá nhân
            return new SimpleStringProperty("Cá nhân");
        });
    }

    private void loadDataFromDatabase()
    {
        appointmentList.clear();
        try
        {
            // Lấy dữ liệu qua BLL
            String currentUserId = utils.SessionManager.getCurrentUser().getId();
            List<Appointment> allAppointments = appointmentBLL.getUserAppointments(currentUserId);
            appointmentList.addAll(allAppointments);
            tblAppointments.setItems(appointmentList);
        } catch (Exception e)
        {
            System.err.println("Lỗi khi load dữ liệu: " + e.getMessage());
        }
    }

    @FXML
    void onRefresh(ActionEvent event)
    {
        loadDataFromDatabase();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã tải lại dữ liệu mới nhất từ hệ thống!");
    }

    @FXML
    void onDelete(ActionEvent event)
    {
        Appointment selectedApp = tblAppointments.getSelectionModel().getSelectedItem();

        if (selectedApp == null)
        {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một lịch hẹn trong bảng để xóa!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa buổi hẹn '" + selectedApp.getTitle() + "' không?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            try
            {
                // Xóa qua BLL (BLL sẽ tự dọn dẹp Reminder liên quan)
                appointmentBLL.deleteAppointment(selectedApp.getId());
                loadDataFromDatabase();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa buổi hẹn thành công!");
            } catch (Exception e)
            {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa: " + e.getMessage());
            }
        }
    }

    @FXML
    void onClose(ActionEvent event)
    {
        Stage stage = (Stage) tblAppointments.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void onEdit(ActionEvent event)
    {
        // Lấy lịch hẹn đang được chọn trên bảng
        Appointment selectedApp = tblAppointments.getSelectionModel().getSelectedItem();

        if (selectedApp == null)
        {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một cuộc hẹn trong bảng để chỉnh sửa!");
            return;
        }

        try
        {
            // Mở lại Form Thêm Cuộc Hẹn
            java.net.URL fxmlUrl = getClass().getResource("/view/AppointmentView.fxml");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlUrl);
            javafx.scene.Parent root = loader.load();

            // Truyền dữ liệu cũ sang Controller
            AppointmentController controller = loader.getController();
            controller.setEditData(selectedApp);

            Stage stage = new Stage();
            stage.setTitle("Chỉnh sửa cuộc hẹn");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Tự động làm mới lại bảng sau khi Sửa xong và đóng cửa sổ
            loadDataFromDatabase();

        } catch (Exception e)
        {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở giao diện chỉnh sửa!");
        }
    }

    // Hàm xử lý hiển thị chi tiết khi Double-click
    private void showAppointmentDetails() {
        Appointment selectedApp = tblAppointments.getSelectionModel().getSelectedItem();

        // Nếu click ra vùng trống không có dữ liệu thì bỏ qua
        if (selectedApp == null) return;

        // Gọi BLL lấy danh sách người tham gia
        List<User> participants = appointmentBLL.getParticipants(selectedApp.getId());

        if (participants == null || participants.isEmpty()) {
            // Lịch cá nhân
            showAlert(Alert.AlertType.INFORMATION, "Chi tiết sự kiện",
                    "Sự kiện: " + selectedApp.getTitle() + "\n" +
                            "Địa điểm: " + selectedApp.getLocation() + "\n" +
                            "Loại: Lịch hẹn cá nhân (Chỉ mình bạn)");
        } else {
            // Lịch họp nhóm -> Dùng StringBuilder để nối chuỗi danh sách cho mượt
            StringBuilder sb = new StringBuilder();
            sb.append("Sự kiện: ").append(selectedApp.getTitle()).append("\n");
            sb.append("Địa điểm: ").append(selectedApp.getLocation()).append("\n");
            sb.append("Loại: Họp Nhóm (").append(participants.size()).append(" thành viên)\n\n");

            sb.append("--- Danh sách tham gia ---\n");
            for (int i = 0; i < participants.size(); i++) {
                User u = participants.get(i);
                sb.append(i + 1).append(". ").append(u.getFullName())
                        .append(" (").append(u.getEmail()).append(")\n");
            }

            showAlert(Alert.AlertType.INFORMATION, "Chi tiết Họp nhóm", sb.toString());
        }
    }
}