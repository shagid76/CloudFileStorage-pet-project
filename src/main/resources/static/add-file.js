const urlParts = window.location.pathname.split('/');
const fileOwner = urlParts[urlParts.length - 1];

document.getElementById("add-file").addEventListener("submit", (event) =>{
     event.preventDefault();

     const formData = new FormData(event.target);

     fetch(`http://localhost:8080/add-file/${fileOwner}`,{
         method: "POST",
         body: formData
     })
         .then(response => {
             if(!response.ok){
                 throw new Error(`Error: ${response.status}`);
             }
             return response.text();
         })
         .then(data => {
             window.location.href = `/directory/${fileOwner}`;
         })
         .catch(error => {
             console.error("Error:", error);
         });
});