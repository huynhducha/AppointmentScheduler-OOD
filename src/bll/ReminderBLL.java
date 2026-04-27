package bll;

import dao.IAppointmentDAO;
import dao.IReminderDAO;
import model.Appointment;
import model.Reminder;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderBLL
{
    private final IReminderDAO reminderDAO;
    private final IAppointmentDAO appointmentDAO;

    // Đối tượng quản lý luồng chạy ngầm
    private ScheduledExecutorService scheduler;

    // Cần cả AppointmentDAO để lấy ra thời gian bắt đầu của sự kiện
    public ReminderBLL(IReminderDAO reminderDAO, IAppointmentDAO appointmentDAO)
    {
        this.reminderDAO = reminderDAO;
        this.appointmentDAO = appointmentDAO;
    }

    // 1. Kích hoạt con robot chạy ngầm
    public void startBackgroundChecker()
    {
        // Tạo một luồng (thread) độc lập, không làm đơ giao diện chính
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Cài đặt cho nó cứ 1 PHÚT lại thức dậy quét Database 1 lần
        scheduler.scheduleAtFixedRate(() -> {
            checkAndTriggerReminders();
        }, 0, 1, TimeUnit.MINUTES);

        System.out.println("⚙️ Hệ thống Reminder đã khởi động và đang chạy ngầm...");
    }

    // 2. Tắt robot khi đóng ứng dụng để giải phóng RAM
    public void stopBackgroundChecker()
    {
        if (scheduler != null && !scheduler.isShutdown())
        {
            scheduler.shutdown();
            System.out.println("🛑 Hệ thống Reminder đã tắt.");
        }
    }

    // 3. Thuật toán kiểm tra giờ nhắc hẹn
    private void checkAndTriggerReminders()
    {
        try
        {
            // Lấy toàn bộ lịch hẹn trong DB
            List<Appointment> apps = appointmentDAO.findAll();

            // Lấy thời gian hiện tại của máy tính, bỏ qua phần giây (chỉ lấy đến phút)
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

            for (Appointment app : apps)
            {
                // Với mỗi lịch hẹn, lấy danh sách các lời nhắc tương ứng
                List<Reminder> reminders = reminderDAO.getRemindersByAppId(app.getId());

                for (Reminder rem : reminders)
                {
                    // CÔNG THỨC: Giờ cần nhắc = Giờ bắt đầu sự kiện - Số phút nhắc trước
                    LocalDateTime remindTime = app.getStartTime()
                            .minusMinutes(rem.getMinutesBefore())
                            .truncatedTo(ChronoUnit.MINUTES);

                    // Nếu Giờ hiện tại TRÙNG KHỚP với Giờ cần nhắc
                    if (now.equals(remindTime))
                    {
                        triggerNotification(rem, app.getTitle());
                    }
                }
            }
        } catch (Exception e)
        {
            System.err.println("Lỗi trong quá trình quét Reminder: " + e.getMessage());
        }
    }

    // 4. Thực thi việc thông báo (Bắn Popup hoặc Gửi Email)
    private void triggerNotification(Reminder reminder, String appTitle)
    {
        // Ép kiểu toString() an toàn vì enum
        if (reminder.getType().name().equals("POPUP"))
        {

            // CỰC KỲ QUAN TRỌNG: Phải dùng Platform.runLater để luồng ngầm đẩy Popup lên luồng Giao diện (JavaFX UI Thread), nếu không app sẽ bị crash!
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("⏰ NHẮC HẸN SỰ KIỆN");
                alert.setHeaderText("Sắp diễn ra: " + appTitle);
                alert.setContentText(reminder.getMessage() + "\n(Diễn ra trong " + reminder.getMinutesBefore() + " phút nữa)");
                alert.show();
            });

        } else if (reminder.getType().name().equals("EMAIL"))
        {
            // Gửi email thực tế sẽ cần tích hợp JavaMail API. Ở đây chúng ta in ra Console.
            System.out.println("📧 [SYSTEM EMAIL ĐÃ GỬI TỰ ĐỘNG]: " + reminder.getMessage() + " -> Sự kiện: " + appTitle);
        }
    }
}