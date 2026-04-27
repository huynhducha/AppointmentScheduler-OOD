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
    @FXML
    private ComboBox<Integer> cbStartHour;
    @FXML
    private ComboBox<Integer> cbEndHour;

    // --- 3 BIẾN MỚI CHO REMINDER ---
    @FXML
    private CheckBox chkReminder;
    @FXML
    private ComboBox<Integer> cbReminderMinutes;
    @FXML
    private ComboBox<String> cbReminderType;

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

        // --- CÀI ĐẶT DỮ LIỆU MẶC ĐỊNH CHO REMINDER ---
        cbReminderMinutes.getItems().addAll(5, 10, 15, 30, 60, 120, 1440); // 1440 phút = 1 ngày
        cbReminderMinutes.setValue(15); // Mặc định nhắc trước 15 phút

        cbReminderType.getItems().addAll("POPUP", "EMAIL");
        cbReminderType.setValue("POPUP"); // Mặc định là bật Popup màn hình

        // Mặc định tắt các ô chọn nếu Checkbox chưa được tích
        cbReminderMinutes.disableProperty().bind(chkReminder.selectedProperty().not());
        cbReminderType.disableProperty().bind(chkReminder.selectedProperty().not());
    }

    public void setSelectedDate(LocalDate date)
    {
        this.selectedDate = date;
    }

    @FXML
    void onSave(ActionEvent event)
    {
        String title = txtTitle.getText().trim();
        String location = txtLocation.getText().trim();
        Integer startHour = cbStartHour.getValue();
        Integer endHour = cbEndHour.getValue();

        if (title.isEmpty() || location.isEmpty() || startHour == null || endHour == null)
        {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đủ thông tin!");
            return;
        }

        LocalDateTime startTime = selectedDate.atTime(startHour, 0);
        LocalDateTime endTime = selectedDate.atTime(endHour, 0);
        Appointment newApp = new Appointment(null, title, location, startTime, endTime);

        // THU THẬP DỮ LIỆU REMINDER
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

            // ==========================================
            // LUỒNG 1: ĐANG Ở CHẾ ĐỘ SỬA (EDIT MODE)
            // ==========================================
            if (isEditMode)
            {
                Appointment conflictApp = appointmentBLL.checkConflict(newApp);

                // Nếu có trùng lịch VÀ cái lịch bị trùng KHÔNG PHẢI là chính lịch đang sửa
                if (conflictApp != null && !conflictApp.getId().equals(editingId))
                {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Khung giờ này cấn với sự kiện: [" + conflictApp.getTitle() + "]\nBạn có muốn THAY THẾ không?",
                            ButtonType.YES, ButtonType.NO);

                    if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES)
                    {
                        appointmentBLL.processAppointmentReplacement(conflictApp.getId(), newApp, currentReminders);
                        // Xóa lịch cũ đi (vì ta đã ghi đè sang một ID khác)
                        appointmentBLL.deleteAppointment(editingId);
                        showSuccessAndClose("Đã ghi đè lịch hẹn thành công!");
                    }
                } else
                {
                    // Cập nhật bình thường
                    appointmentBLL.updateAppointment(editingId, newApp, currentReminders);
                    showSuccessAndClose("Cập nhật cuộc hẹn thành công!");
                }
            }
            // ==========================================
            // LUỒNG 2: ĐANG Ở CHẾ ĐỘ THÊM MỚI
            // ==========================================
            else
            {
                long duration = newApp.getDurationInMinutes();
                GroupMeeting matchingGroup = appointmentBLL.findMatchingGroupMeeting(title, duration);

                if (matchingGroup != null)
                {
                    Alert confirmGroup = new Alert(Alert.AlertType.CONFIRMATION,
                            "Lịch hẹn trùng với Group Meeting: " + matchingGroup.getTitle() + "\nBạn có muốn tham gia không?",
                            ButtonType.YES, ButtonType.NO);

                    if (confirmGroup.showAndWait().orElse(ButtonType.NO) == ButtonType.YES)
                    {
                        Appointment conflictForGroup = appointmentBLL.checkConflict(matchingGroup);
                        boolean isReplace = conflictForGroup != null;
                        String oldId = isReplace ? conflictForGroup.getId() : null;

                        appointmentBLL.processGroupJoin(matchingGroup.getId(), currentUserId, isReplace, oldId);
                        showSuccessAndClose("Đã tham gia Group Meeting thành công!");
                        return;
                    }
                }

                Appointment conflictApp = appointmentBLL.checkConflict(newApp);

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
    }
}