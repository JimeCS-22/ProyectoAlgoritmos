package ucr.proyectoalgoritmos.Domain;

public class User {

    private String Username;
    private String Password;
    private String Role;

    public User(String username, String password, String role) {
        this.Username = username;
        this. Password = password;
        this.Role = role;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }
}
