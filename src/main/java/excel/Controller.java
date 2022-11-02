package excel;

import excel.services.FileService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

//Класс получает запросы пользователя и обрабатывает их
@org.springframework.stereotype.Controller
public class Controller {

    FileService fileService = new FileService();


    //Загрузка файла
    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file){

        if(file.isEmpty()){
            return "error";
        }

        fileService.uploadFile(file);

        return "redirect:/";
    }

    //Главная страница со списком всех загруженных файлов
    @GetMapping("/")
    public String getList(Model model){
        model.addAttribute("files", fileService.getFiles());

        return "index";
    }

    //Страница с отображением данных файла с переданным id
    @GetMapping("/{id}")
    public String getInfo(@PathVariable int id, Model model){

         model.addAttribute("file", fileService.getFileById(id));
         model.addAttribute("openingbalance", fileService.getResultArray("openingbalance", id));
         model.addAttribute("turnover", fileService.getResultArray("turnover", id));
         model.addAttribute("closingbalance", fileService.getResultArray("closingbalance", id));
         model.addAttribute("openingbalanceSum", fileService.getBalance("openingbalance", id));
         model.addAttribute("turnoverSum", fileService.getBalance("turnover", id));
         model.addAttribute("closingbalanceSum", fileService.getBalance("closingbalance", id));

        return "info";
    }


}
