package excel.models;

import java.util.ArrayList;

//объект данного класса представляет собой подкласс данных из таблицы
public class AccountSubClass {

    //номер самого подкласса
    public String number;

    //сумма первого столбца подкласса
    public String assetsSum;

    //сумма второго столбца данного подкласса
    public String liabilitySum;

    //список строк, относящихся к подклассу
    public ArrayList<Balance> balance;

    public AccountSubClass() {
        this.balance = new ArrayList<>();
    }

    public String getClassNumber() {
        return String.valueOf(this.number).substring(0,1);
    }

}