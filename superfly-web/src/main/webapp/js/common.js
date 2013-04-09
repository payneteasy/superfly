function rowClick(url, event){
    var tagName = event.target.tagName;
    if(tagName !== "A" && tagName != "INPUT"){
        window.location = url;
    }
}
