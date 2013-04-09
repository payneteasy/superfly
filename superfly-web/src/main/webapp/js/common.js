function rowClick(url, event){
    var tagName = event.target.tagName;
    if(tagName !== "A"){
        window.location = url;
    }
}
