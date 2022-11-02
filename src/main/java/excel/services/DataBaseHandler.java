package excel.services;

import excel.models.Balance;
import excel.models.File;

import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

//класс отвечает за взаимодействие с БД
public class DataBaseHandler {

    Connection dbConnection;
    DecimalFormat formattedDouble;

    public DataBaseHandler() throws SQLException, ClassNotFoundException {
        this.dbConnection = getDbConnection();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        this.formattedDouble = new DecimalFormat("####,###.##", otherSymbols);
    }

    //подключаемся к БД
    private Connection getDbConnection() throws ClassNotFoundException, SQLException {
        String connectionString = "jdbc:mysql://localhost:3306/task2";
        Class.forName("com.mysql.cj.jdbc.Driver");
        dbConnection = DriverManager.getConnection(connectionString, "root", "root");
        return dbConnection;
    }

    //получаем объект File по id
    public File getFileByID(int id){
        File file = new File();
        try {
            String insert="SELECT * FROM files WHERE id='"+id+"'";

            PreparedStatement prSt = dbConnection.prepareStatement(insert);
            ResultSet result = prSt.executeQuery();

            while (result.next()){
                file.setId(result.getInt(1));
                file.setName(result.getString(2));
            }
        }
        catch (SQLException e){
            System.out.println("Error: " + e);
        }
        return file;
    }

    //получаем объект File по имени
    public File getFileByName(String filename){
        File file = new File();
        try {
            String insert="SELECT * FROM files WHERE name='"+filename+"'";

            PreparedStatement prSt = dbConnection.prepareStatement(insert);
            ResultSet result = prSt.executeQuery();

            while (result.next()){
                file.setId(result.getInt(1));
                file.setName(result.getString(2));
            }
        }
        catch (SQLException e){
            System.out.println("Error: " + e);
        }
        return file;
    }

    // метод возвращает сумму строк определенной таблицы
    // параметр classNumber отвечает за то, какие именно строки будут суммированы
    // если classNumber="", то метод вернет сумму строк всей таблицы
    // если classNumber равен номеру класса или подкласса - сумму строк класса или подкласса.
    public String[] getSumClass(String classNumber, String tableName, int fileId){
        String[] sum = new String[2];
        try {
            String insert="";
            if(tableName=="turnover"){
                insert="select sum(debit), sum(credit)\n" +
                        "from " + tableName + "\n" +
                        "where account like '" + classNumber + "%' and file_id="+fileId;
            }
            else{
               insert="select sum(asset), sum(liability)\n" +
                        "from " + tableName + "\n" +
                        "where account like '" + classNumber + "%' and file_id="+fileId;
            }

            PreparedStatement prSt = dbConnection.prepareStatement(insert);
            ResultSet result = prSt.executeQuery();

            while (result.next())
            {
                sum[0] = formattedDouble.format(result.getDouble(1));
                sum[1] = formattedDouble.format(result.getDouble(2));
            }

        }
        catch (SQLException | ClassCastException e){
            System.out.println("Error: " + e);
        }
        return sum;
    }

    //метод возвращает все строки указанной таблицы определенного файла в виде списка объектов Balance
    public ArrayList<Balance> getTable(String tableName, int fileId){

        ArrayList<Balance> arrayList = new ArrayList<>();

        try {
            String insert="SELECT * FROM " + tableName + " WHERE file_id=" + fileId;
            PreparedStatement prSt = dbConnection.prepareStatement(insert);
            ResultSet result = prSt.executeQuery();

            while (result.next())
            {
                Balance balance = new Balance(result.getInt(1), formattedDouble.format(result.getDouble(2)), formattedDouble.format(result.getDouble(3)));
                arrayList.add(balance);
            }

        }
        catch (SQLException | ClassCastException e){
            System.out.println("Error: " + e);
        }
        return arrayList;
    }

    //метод возвращает список файлов, загруженных в БД
    public ArrayList getFiles(){

        ArrayList<File> files = new ArrayList<>();

        try {
            String insert="SELECT * FROM files";

            PreparedStatement prSt = dbConnection.prepareStatement(insert);
            ResultSet result = prSt.executeQuery();
            int i=0;
            while (result.next()){
                files.add(new File(result.getInt(1), result.getString(2) ));
            }
        }
        catch (SQLException e){
            System.out.println("Error: " + e);
        }
        return files;
    }

    //добавляет файл в БД
    public void addFileToDB(String filename){

        try {
            String insert="INSERT INTO files(name) VALUES(?)";

            PreparedStatement prSt = dbConnection.prepareStatement(insert);
            prSt.setString(1, filename);

            prSt.executeUpdate();
        }
        catch (SQLException e){
            System.out.println("Error: " + e);
        }

    }

    //добавляет строку в таблицу
    public void addToDB(Balance balance, String tableName, int fileId){

        try {
                String insert="";
                switch (tableName){
                    case "openingbalance":{
                        insert = "INSERT INTO openingbalance(account, asset, liability, file_id)VALUES(?,?,?,?)";
                        break;
                    }
                    case "turnover":{
                        insert = "INSERT INTO turnover(account, debit, credit, file_id)VALUES(?,?,?,?)";
                        break;
                    }
                    case "closingbalance":{
                        insert = "INSERT INTO closingbalance(account, asset, liability,  file_id)VALUES(?,?,?,?)";
                        break;
                    }
                }

            PreparedStatement prSt = dbConnection.prepareStatement(insert);
            prSt.setInt(1, balance.getAccount());
            prSt.setString(2, balance.getAsset());
            prSt.setString(3, balance.getLiability());
            prSt.setInt(4, fileId);

            prSt.executeUpdate();
        }
        catch (SQLException | ClassCastException e){
            System.out.println("Error: " + e);
        }

    }
}