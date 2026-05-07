package controller;

import bll.AppointmentBLL;
import dao.impl.SqlAppointmentDAO;
import dao.impl.SqlReminderDAO;
import model.Appointment;
import model.GroupMeeting;
import model.Reminder;
import model.ReminderType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentController
{

    @FXML
    private TextField txtTitle;
    @FXML
    private TextField txtLocation;

    // --- THÊM BIẾN COMBOBOX LOẠI LỊCH ---
    @FXML
    private ComboBox<String> cbAppointmentType;

    @FXML
    private ComboBox<Integer> cbStartHour;
    @FXML
    private ComboBox<Integer> cbEndHour;

    // --- BIẾN CHO REMINDER ---
    @FXML
    private CheckBox chkReminder;
    @FXML
    private ComboBox<Integer> cbReminderMinutes;
    @FXML
    private ComboBox<String> cbReminderType;

    @FXML
    private DatePicker dpDate;

    private LocalDate selectedDate;
    private AppointmentBLL appointmentBLL;

    private boolean isEditMode = false;
    private String editingId;

    @FXML
    public void initialize()
    {
        appointmentBLL = new AppointmentBLL(new SqlAppointmentDAO(), new SqlReminderDAO());

        // Cài đặt giờ cho ComboBox
        for (int i = 0; i <= 23; i++)
        {
            cbStartHour.getItems().add(i);
            cbEndHour.getItems().add(i);
        }

        // --- CÀI ĐẶT COMBOBOX CHỌN LOẠI CÁ NHÂN / NHÓM ---
        cbAppointmentType.getItems().addAll("Cá nhân", "Nhóm");
        cbAppointmentType.setValue("Cá nhân"); // Mặc định là lịch cá nhân

        // --- CÀI ĐẶT DỮ LIỆU MẶC ĐỊNH CHO REMINDER ---
        cbReminderMinutes.getItems().addAll(5, 10, 15, 30, 60, 120, 1440);
        cbReminderMinutes.setValue(15);

        cbReminderType.getItems().addAll("POPUP", "EMAIL");
        cbReminderType.setValue("POPUP");

        cbReminderMinutes.disableProperty().bind(chkReminder.selectedProperty().not());
        cbReminderType.disableProperty().bind(chkReminder.selectedProperty().not());
    }

    public void setSelectedDate(LocalDate date)
    {
        this.selectedDate = date;
        // Hiện ngày lên DatePicker
        if (dpDate != null) {
            dpDate.setValue(date);
        }
    }

    @FXML
    void onSave(ActionEvent event)
    {
        String title = txtTitle.getText().trim();
        String location = txtLocation.getText().trim();
        Integer startHour = cbStartHour.getValue();
        Integer endHour = cbEndHour.getValue();

        // Lấy ngày trực tiếp từ giao diện (người dùng có thể đã đổi ngày khác)
        LocalDate finalDate = dpDate.getValue();

        if (title.isEmpty() || location.isEmpty() || startHour == null || endHour == null || finalDate == null)
        {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đủ thông tin!");
            return;
        }

        // --- DÙNG finalDate THAY VÌ selectedDate NHƯ CŨ ---
        LocalDateTime startTime = finalDate.atTime(startHour, 0);
        LocalDateTime endTime = finalDate.atTime(endHour, 0);

        // --- LOGIC MỚI: TẠO ĐÚNG LOẠI ĐỐI TƯỢNG (ĐA HÌNH) ---
        Appointment newApp;
        if ("Nhóm".equals(cbAppointmentType.getValue())) {
            newApp = new GroupMeeting(null, title, location, startTime, endTime);
        } else {
            newApp = new Appointment(null, title, location, startTime, endTime);
        }

        List<Reminder> currentReminders = new ArrayList<>();
        if (chkReminder.isSelected())
        {
            ReminderType type = ReminderType.valueOf(cbReminderType.getValue());
            int minutes = cbReminderMinutes.getValue();
            String message = "Sắp đến giờ hẹn: " + title + " tại " + location;
            currentReminders.add(new Reminder(type, minutes, message));
        }

        try
        {
            String currentUserId = utils.SessionManager.getCurrentUser().getId();

            if (isEditMode)
            {
                // Sửa dòng này
                Appointment conflictApp = appointmentBLL.checkConflict(newApp, currentUserId);

                if (conflictApp != null && !conflictApp.getId().equals(editingId))
                {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Khung giờ này cấn với sự kiện: [" + conflictApp.getTitle() + "]\nBạn có muốn THAY THẾ không?",
                            ButtonType.YES, ButtonType.NO);

                    if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES)
                    {
                        appointmentBLL.processAppointmentReplacement(conflictApp.getId(), newApp, currentReminders);
                        appointmentBLL.deleteAppointment(editingId);
                        showSuccessAndClose("Đã ghi đè lịch hẹn thành công!");
                    }
                } else
                {
                    appointmentBLL.updateAppointment(editingId, newApp, currentReminders);
                    showSuccessAndClose("Cập nhật cuộc hẹn thành công!");
                }
            }
            // ==========================================
            // LUỒNG 2: ĐANG Ở CHẾ ĐỘ THÊM MỚI
            // ==========================================
            else
            {
                GroupMeeting matchingGroup = appointmentBLL.findMatchingGroupMeeting(
                        newApp.getTitle(),
                        newApp.getStartTime(),
                        newApp.getEndTime()
                );

                if (matchingGroup != null)
                {
                    Alert confirmGroup = new Alert(Alert.AlertType.CONFIRMATION,
                            "Đã có Group Meeting: " + matchingGroup.getTitle() + " vào giờ này.\nBạn có muốn tham gia nhóm thay vì tạo mới không?",
                            ButtonType.YES, ButtonType.NO);

                    if (confirmGroup.showAndWait().orElse(ButtonType.NO) == ButtonType.YES)
                    {
                        // --- ĐOẠN CODE ĐƯỢC CẬP NHẬT ĐỂ KHỚP SƠ ĐỒ (CÁCH B) ---
                        // Kiểm tra xem việc tham gia nhóm có đè lịch cá nhân nào không
                        Appointment conflictApp = appointmentBLL.checkConflict(newApp, currentUserId);

                        if (conflictApp != null) {
                            Alert confirmReplace = new Alert(Alert.AlertType.CONFIRMATION,
                                    "Tham gia nhóm này sẽ đè mất lịch cá nhân: [" + conflictApp.getTitle() + "]. Bạn vẫn muốn tiếp tục chứ?",
                                    ButtonType.YES, ButtonType.NO);

                            if (confirmReplace.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                                // Ghi đè: Gọi processGroupJoin với cờ isReplace = true
                                appointmentBLL.processGroupJoin(matchingGroup.getId(), currentUserId, true, conflictApp.getId());
                                showSuccessAndClose("Ghi đè lịch cũ và tham gia nhóm thành công!");
                            }
                        } else {
                            // Không cấn lịch: Join nhóm bình thường (isReplace = false)
                            appointmentBLL.processGroupJoin(matchingGroup.getId(), currentUserId, false, null);
                            showSuccessAndClose("Đã tham gia Group Meeting thành công!");
                        }
                        return; // Kết thúc luồng lưu
                    }
                }

                // --- ĐÃ SỬA LẠI ĐÚNG CHUẨN Ở ĐÂY ---
                // Kiểm tra xem lịch MỚI CỦA MÌNH (newApp) có cấn với LỊCH CỦA MÌNH (currentUserId) không
                Appointment conflictApp = appointmentBLL.checkConflict(newApp, currentUserId);

                if (conflictApp != null)
                {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Khung giờ này cấn với sự kiện: [" + conflictApp.getTitle() + "]\nBạn có muốn THAY THẾ không?",
                            ButtonType.YES, ButtonType.NO);

                    if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES)
                    {
                        appointmentBLL.processAppointmentReplacement(conflictApp.getId(), newApp, currentReminders);
                        showSuccessAndClose("Đã ghi đè lịch hẹn thành công!");
                    }
                } else
                {
                    // Lưu mới bình thường
                    appointmentBLL.processNewAppointment(newApp, currentReminders, currentUserId);
                    showSuccessAndClose("Đã thêm lịch hẹn thành công!");
                }
            }
        } catch (IllegalArgumentException ex)
        {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", ex.getMessage());
        } catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onCancel(ActionEvent event)
    {
        closeWindow();
    }

    private void showSuccessAndClose(String msg)
    {
        showAlert(Alert.AlertType.INFORMATION, "Thành công", msg);
        closeWindow();
    }

    private void closeWindow()
    {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
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

    public void setEditData(Appointment app)
    {
        this.isEditMode = true;
        this.editingId = app.getId();

        txtTitle.setText(app.getTitle());
        txtLocation.setText(app.getLocation());
        cbStartHour.setValue(app.getStartTime().getHour());
        cbEndHour.setValue(app.getEndTime().getHour());
        this.selectedDate = app.getStartTime().toLocalDate();

        dpDate.setValue(this.selectedDate);

        // Khóa không cho đổi loại (Cá nhân/Nhóm) khi đang Edit để tránh lỗi dữ liệu
        cbAppointmentType.setDisable(true);
    }
}