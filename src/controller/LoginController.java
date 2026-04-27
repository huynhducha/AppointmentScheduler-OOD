package controller;

import bll.UserBLL;
import dao.impl.SqlUserDAO;
import model.User;
import utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController
{

    @FXML
    private TextField txtEmail;

    // Khởi tạo BLL
    private final UserBLL userBLL = new UserBLL(new SqlUserDAO());

    @FXML
    void onLoginClick(ActionEvent event)
    {
        String email = txtEmail.getText().trim();

        try
        {
            // 1. Gọi BLL xác thực
            User loggedInUser = userBLL.loginByEmail(email);

            // 2. Lưu vào Session
            SessionManager.setCurrentUser(loggedInUser);

            // 3. Thông báo và chuyển màn hình
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Chào mừng " + loggedInUser.getFullName() + "!");
            openDashboard();

        } catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR, "Lỗi đăng nhập", e.getMessage());
        }
    }

    private void openDashboard()
    {
        try
        {
            // Chuyển sang màn hình Lịch hoặc Dashboard của bạn
            java.net.URL fxmlUrl = getClass().getResource("/view/CalendarView.fxml");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlUrl);
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Hệ thống Quản lý Lịch hẹn");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();

            // Đóng màn hình đăng nhập
            Stage loginStage = (Stage) txtEmail.getScene().getWindow();
            loginStage.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
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
    void onGoToRegisterClick(ActionEvent event)
    {
        try
        {
            java.net.URL fxmlUrl = getClass().getResource("/view/RegisterView.fxml");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlUrl);
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Đăng ký Tài khoản");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.show();

            // Đóng màn hình đăng nhập
            Stage loginStage = (Stage) txtEmail.getScene().getWindow();
            loginStage.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}