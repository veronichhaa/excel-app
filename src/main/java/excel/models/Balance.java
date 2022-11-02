package excel.models;

//объект данного класса представляет сущность таблицы openingbalance, turnover или closing balance БД, то есть одну строку
public class Balance {

    private int account;
    private String asset;
    private String liability;
    private String accountClass;
    private String accountSubClassl;

    public Balance() {
    }

    public Balance(int account, String asset, String liability) {
        this.account = account;
        this.asset = asset;
        this.liability = liability;
        //номер класса получаем, взяв первую цифру счета
        this.accountClass = String.valueOf(account).substring(0,1);
        //номер подкласса получаем, взяв первые 2 цифры счета
        this.accountSubClassl = String.valueOf(account).substring(0,2);
    }

    public String getAccountClass() {
        return accountClass;
    }

    public String getAccountSubClass() {
        return accountSubClassl;
    }

    public void setAccount(String account) {

        this.account = Integer.valueOf(account);
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public void setLiability(String liability) {
        this.liability = liability;
    }

    public int getAccount() {
        return account;
    }

    public String getAsset() {
        return asset;
    }

    public String  getLiability() {
        return liability;
    }
}
