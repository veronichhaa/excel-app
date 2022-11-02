package excel.models;

import java.util.ArrayList;

//объект данного класса представляет собой класс данных из таблицы
public class AccountClass {

    //номер класса
    public String number;

    //сумма первого столбца класса
    public String assetsSum;

    //сумма второго столбца подкласса
    public String liabilitySum;

    //список подклассов, относящихся к классу
    public ArrayList<AccountSubClass> subClasses;

    public AccountClass() {
        subClasses = new ArrayList<>();
    }

}