package application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {
	private final IntegerProperty userId;
	private final StringProperty name;

    public User() {
        this(null, null);
    }

    public User(Integer userId, String name) {
        this.userId = new SimpleIntegerProperty(userId);
        this.name = new SimpleStringProperty(name);
    }
    
    public Integer getUserId() {
		return userId.get();
	}

	public void setUserId(Integer userId) {
		this.userId.set(userId);
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}
	
	public StringProperty nameProperty() {
        return name;
    }
	
	public IntegerProperty userIdProperty() {
        return userId;
    }

}
