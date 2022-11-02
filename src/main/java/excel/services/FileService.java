package excel.services;


import excel.models.AccountClass;
import excel.models.AccountSubClass;
import excel.models.Balance;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Scanner;

public class FileService {

    DataBaseHandler dataBaseHandler;
    DecimalFormat formattedDouble;

    public FileService() {
        try{
            this.dataBaseHandler = new DataBaseHandler();

            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
            otherSymbols.setDecimalSeparator('.');
            this.formattedDouble = new DecimalFormat("0.##", otherSymbols);
        }
        catch (SQLException | ClassNotFoundException e){
            System.out.println("Error: " + e);
        }
    }

    //метод возвращает список загруженных файлов
    public ArrayList getFiles(){
        return dataBaseHandler.getFiles();
    }

    //Загрузка файла в БД и считывание его данных
    public void uploadFile(MultipartFile file){

        File uploadedFile = new File("D:\\прога\\excel-app\\src\\main\\resources\\"+file.getOriginalFilename());

        //создается эксемпляр загруженного файла в папке resources
        try{
            byte[] bytes = file.getBytes();
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(uploadedFile));
            stream.write(bytes);
            stream.flush();
            stream.close();
        }catch (IOException e){
            System.out.println("Error:" + e);
        }

        //имя файла добавляется в БД
        dataBaseHandler.addFileToDB(uploadedFile.getName());
        //данные из файла считываются и добавляются в соответствующие таблицы БД
        readExcel(uploadedFile.getName());
    }

    public excel.models.File getFileById(int id){
        return dataBaseHandler.getFileByID(id);
    }

    //Метод возвращает итоговую сумму по переданной в параметре таблице
    public String[]getBalance(String tablename, int fileId){
        return dataBaseHandler.getSumClass("", tablename, fileId);
    }

    //метод возвращает все данные, относящиеся к таблице tablename и к файлу с fileId
    //в виде списка объектов AccountClass
    public ArrayList<AccountClass> getResultArray(String tablename, int fileId){

        ArrayList<AccountClass> classesArray = new ArrayList<>();
        ArrayList<AccountSubClass> subClassesArray = new ArrayList<>();
        ArrayList<Balance> arrayOfAll = getTable(tablename, fileId);

        AccountSubClass subClass = new AccountSubClass();

        subClass.number=arrayOfAll.get(0).getAccountSubClass();
        //проходимся по всем записям таблицы
        for(Balance item : arrayOfAll){

            //если подкласс заканчивается, то заполняем объект подкласса данными
            if(!subClass.number.equals(item.getAccountSubClass())){
                subClass.assetsSum=dataBaseHandler.getSumClass(subClass.number, tablename, fileId)[0];
                subClass.liabilitySum=dataBaseHandler.getSumClass(subClass.number, tablename, fileId)[1];
                //и добавялем его в список подклассов
                subClassesArray.add(subClass);
                //обновляем объект
                subClass = new AccountSubClass();
                //если запись в таблице последняя, то новый объект также заполняем данными
                if(item==arrayOfAll.get(arrayOfAll.size()-1)){
                    subClass.assetsSum=dataBaseHandler.getSumClass(subClass.number, tablename, fileId)[0];
                    subClass.liabilitySum=dataBaseHandler.getSumClass(subClass.number, tablename, fileId)[1];
                    subClassesArray.add(subClass);
                }
                subClass.balance.add(item);

            }
            else{
                subClass.balance.add(item);
            }

            subClass.number = item.getAccountSubClass();

        }

        AccountClass accountClass = new AccountClass();
        accountClass.number=arrayOfAll.get(0).getAccountClass();

        //теперь проходимся по всем подклассам
        for(AccountSubClass item : subClassesArray ){
            //если подкласс заканчивается, то заполняем объект класса данными
            if(!accountClass.number.equals(item.getClassNumber())){
                accountClass.assetsSum=dataBaseHandler.getSumClass(accountClass.number, tablename, fileId)[0];
                accountClass.liabilitySum=dataBaseHandler.getSumClass(accountClass.number, tablename, fileId)[1];
                classesArray.add(accountClass);
                //обновляем объект класса
                accountClass = new AccountClass();

               accountClass.subClasses.add(item);

            }
            else{
                accountClass.subClasses.add(item);
            }

            //если подкласс последний, то заполняем новый объект
            if(item==subClassesArray.get(subClassesArray.size()-1)){
                accountClass.assetsSum=dataBaseHandler.getSumClass(accountClass.number, tablename, fileId)[0];
                accountClass.liabilitySum=dataBaseHandler.getSumClass(accountClass.number, tablename, fileId)[1];
                classesArray.add(accountClass);
            }

            accountClass.number = item.getClassNumber();
        }


        return classesArray;
    }

    //метод возвращает все строчки данных переданной в параметре таблицы, относящейся к определенному файлу
    private ArrayList<Balance> getTable(String tableName, int fileId){
        return dataBaseHandler.getTable(tableName, fileId);
    }

    //получает список строк определенной таблицы в виде объектов
    //каждый объект поочередно заносится в бд
    private void addToDataBase(String tableName, ArrayList<Balance> balance, int fileId){

        balance.forEach(item -> dataBaseHandler.addToDB(item, tableName, fileId));
        System.out.println("Таблица: `"+ tableName + "` добавлена в БД");
    }

    //метод проверяет, стоит ли считывать строку из excel файла
    // 1 - да, 0 - нет
    private int isRowValid(Row row){

        org.apache.poi.ss.usermodel.Cell check = row.getCell(0);
        int result=1;
        //если ячейка с номером счета содержит не 4 цифры или не является числом, то result=0
        switch (check.getCellType()) {
            case NUMERIC: {
                if (String.valueOf((int) check.getNumericCellValue()).length() != 4) {
                    result = 0;
                }
                break;
            }
            case STRING: {
                if (!(new Scanner(check.getStringCellValue()).hasNextInt())) {
                    result = 0;
                }
                break;
            }
        }
        return result;
    }

    //считывает данные из sheet с данными из файла
    private ArrayList readBalance(HSSFSheet sheet){

        Iterator<Row> rowIterator = sheet.rowIterator();
        int checkResult = 1;
        //все строки, сгруппированные по таблицам
        ArrayList resultArray = new ArrayList();
        //строки, относящиеся соответственно к таблицам: openingbalance, turnover и closingbalance
        ArrayList<Balance> openingArray = new ArrayList();
        ArrayList<Balance> turnoverArray = new ArrayList();
        ArrayList<Balance> closingArray = new ArrayList();
        ArrayList result = new ArrayList();
        result.add(openingArray);
        result.add(turnoverArray);
        result.add(closingArray);

        //считываем данные из файла построчно
        while (rowIterator.hasNext()){
            Row row = rowIterator.next();
            checkResult=1;
            Balance opening = new Balance();
            Balance turnover = new Balance();
            Balance closing = new Balance();

            //переменная, которой будут присваиваться значения ячейки
            String data="";

            //перебираем ячейки
            for(int i=0; i<7; i++) {

                //проверяем, стоит ли считывать данную строку
                checkResult = isRowValid(row);

                if (checkResult == 1) {

                    Cell cell = row.getCell(i);
                    switch (cell.getCellType()) {
                        //в зависимости от типа данных в ячейке, форматируем их
                        case NUMERIC: {
                            data = this.formattedDouble.format(cell.getNumericCellValue());
                            break;
                        }
                        case STRING: {
                            if((new Scanner(row.getCell(0).getStringCellValue()).hasNextInt())){
                                data = cell.getStringCellValue();
                            }
                            break;
                        }
                    }
                    if(data!=""){

                        //присваиваем значение ячейки объекту, относящемуся к опр. таблице
                        switch (i){
                            case 0: {
                                opening.setAccount(data);
                                turnover.setAccount(data);
                                closing.setAccount(data);
                                break;
                            }
                            case 1: {opening.setAsset(data); break;}
                            case 2: {opening.setLiability(data); break;}
                            case 3:{turnover.setAsset(data); break;}
                            case 4:{turnover.setLiability(data); break;}
                            case 5:{closing.setAsset(data); break;}
                            case 6:{closing.setLiability(data); break;}

                        }
                    }
                }
            }
            if(opening.getAccount()!=0)
            {
                openingArray.add(opening);
                closingArray.add(closing);
                turnoverArray.add(turnover);
            }
        };
        resultArray.add(openingArray);
        resultArray.add(turnoverArray);
        resultArray.add(closingArray);
        return resultArray;
    }

    //считывание данных из xls файла
    private void readExcel(String filename){
        try{
            int fileId = dataBaseHandler.getFileByName(filename).getId();
            String path = "D:\\прога\\excel-app\\src\\main\\resources\\";
            FileInputStream file = new FileInputStream(path+filename);
            HSSFWorkbook workbook = new HSSFWorkbook(file);
            //создаем sheet с данными
            HSSFSheet sheet = workbook.getSheetAt(0);
            System.out.println("Лист прочитан");
            //методом readBalance считываем данные с листа и заносим их в соответствующую таблицу
            addToDataBase("openingbalance", (ArrayList<Balance>) readBalance(sheet).get(0), fileId);
            addToDataBase("turnover", (ArrayList<Balance>) readBalance(sheet).get(1), fileId);
            addToDataBase("closingbalance", (ArrayList<Balance>) readBalance(sheet).get(2), fileId);
            file.close();
            } catch (IOException e) {
                e.printStackTrace();
        }

    }


}
