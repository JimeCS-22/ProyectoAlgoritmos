package ucr.proyectoalgoritmos.Controller;

public interface LoginListener {
    void onLoginSuccess(String userRole);
    void onLoginCanceled();
}
