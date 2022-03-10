public class CustomObject {
    
    private String string;
    private boolean checked;
    private int id;

    CustomObject(String str, int id) {
        this.string = str;
        this.checked  = false;
        this.id = id;
    }

    public String getValue() {
        return this.string;
    }

    public boolean isChecked() {
        return checked;
    }

    public void toggleChecked() {
        this.checked = (!this.checked);
    }

    public int getID() {
        return this.id;
    }
}