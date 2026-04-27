package controller;

import bll.UserBLL;
import dao.impl.SqlUserDAO;
import model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController
{

    @FXML
    private TextField txtFullName;
    @FXML
    private TextField txtEmail;

    private final UserBLL userBLL = new UserBLL(new SqlUserDAO());

    @FXML
    void onRegisterClick(ActionEvent event)
    {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();

        // Validate cơ bản
        if (fullName.isEmpty() || email.isEmpty())
        {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ Họ tên và Email!");
            return;
        }

        try
        {
            // Tạo đối tượng User (ID truyền null để DAO tự sinh UUID)
            User newUser = new User(null, fullName, email);

            // Gọi BLL xử lý nghiệp vụ đăng ký (Check trùng email, insert...)
            boolean isSuccess = userBLL.register(newUser);

            if (isSuccess)
            {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng ký thành công! Vui lòng đăng nhập.");
                openLoginView(); // Tự động quay về màn hình đăng nhập
            }
        } catch (Exception e)
        {
            showAlert(Alert.AlertType.WARNING, "Không thể đăng ký", e.getMessage());
        }
    }

    @FXML
    void onBackToLoginClick(ActionEvent event)
    {
        openLoginView();
    }

    private void openLoginView()
    {
        try
        {
            java.net.URL fxmlUrl = getClass().getResource("/view/LoginView.fxml");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlUrl);
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Đăng nhập Hệ thống");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.show();

            // Đóng màn hình đăng ký hiện tại
            Stage currentStage = (Stage) txtEmail.getScene().getWindow();
            currentStage.close();
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
}