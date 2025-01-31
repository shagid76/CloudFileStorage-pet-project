const urlParts = window.location.pathname.split('/');
if(urlParts.length === 3) {
    const fileOwner = urlParts[urlParts.length - 1];

    document.getElementById("add-file").addEventListener("submit", (event) => {
        event.preventDefault();

        const formData = new FormData(event.target);

        fetch(`http://localhost:8080/add-file/${fileOwner}`, {
            method: "POST",
            body: formData
        })
            .then(response => {
                if (!response.ok) {
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
}else{
    const fileOwner = urlParts[urlParts.length - 2];
    const parentId = urlParts[urlParts.length - 1];

    document.getElementById("add-file").addEventListener("submit", (event) => {
        event.preventDefault();

        const formData = new FormData(event.target);

        fetch(`http://localhost:8080/add-file/${fileOwner}/${parentId}`, {
            method: "POST",
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Error: ${response.status}`);
                }
                return response.text();
            })
            .then(data => {
                window.location.href = `/folder/${fileOwner}/${parentId}`;
            })
            .catch(error => {
                console.error("Error:", error);
            });
    });
}