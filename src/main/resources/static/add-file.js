const parentId = document.getElementById("data-container-parentId").getAttribute("parentId");
const fileOwner = document.getElementById("data-container-email").getAttribute("email");

if (!parentId || parentId.trim() === "") {
    document.getElementById("add-file").addEventListener("submit", (event) => {
        event.preventDefault();

        const formData = new FormData(event.target);

        fetch(`http://localhost:8080/files/${fileOwner}/upload`, {
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
                window.location.href = `/directory`;
            })
            .catch(error => {
                console.error("Error:", error);
            });
    });
} else {
    document.getElementById("add-file").addEventListener("submit", (event) => {
        event.preventDefault();

        const formData = new FormData(event.target);

        fetch(`http://localhost:8080/folders/${parentId}/files/${fileOwner}/upload`, {
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
                window.location.href = `/folder/${parentId}`;
            })
            .catch(error => {
                console.error("Error:", error);
            });
    });
}