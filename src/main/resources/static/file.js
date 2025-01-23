fetch("http://localhost:8080/updated-file-name",{
    method: "Post"
})
    .then(response =>{
       if (!response){
           throw new Error(`Error: ${response.status}`);
       }
       return response.json();
   })
   .then()