let rows =  document.getElementsByTagName("tr")

for(let row of rows){
    row.addEventListener("mouseenter", ()=>{
       let cells = row.children;
       for(let cell of cells){
           if(cell.classList.contains("head")){}
           else{
               cell.style.backgroundColor="#e6f2ff";
           }
       }
    })
}

for(let row of rows){
    row.addEventListener("mouseleave", ()=>{
        let cells = row.children;
        for(let cell of cells){
            if(cell.classList.contains("head")){}
            else{
                cell.style.backgroundColor="#b7d2ed";
            }
        }
    })
}