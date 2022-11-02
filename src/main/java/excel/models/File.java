package excel.models;

//объект данного класса представляет объект таблицы Files БД
public class File {

    private String name;
    private int id;

    public File() {
    }

    public void setName(String name) {
        this.name = name.replace(".xls", "");
    }

    public void setId(int id) {
        this.id = id;
    }

    public File(int id, String name) {
        this.name = name.replace(".xls", "");
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
