const urlParts = window.location.pathname.split('/');
const fileOwner = urlParts[urlParts.length - 1];

fetch(`http://localhost:8080/files/${fileOwner}`)
    .then(response => {
        if (!response) {
            throw new Error(`Error: ${response.status}`);
        }
        return response.json();
    })
    .then(files => {
        const fileList = document.getElementById("fileList");
        const emptyMassage = document.getElementById("emptyMassage");

        if (files.length === 0) {
            emptyMassage.classList.remove("d-none");
            return;
        }
        files.forEach(file => {
            const uploadDate = new Date(file.uploadDate).toLocaleString("en-US", {
                year: "numeric",
                month: "2-digit",
                day: "2-digit",
                hour: "2-digit",
                minute: "2-digit"
            });
            const listItem = document.createElement("li");
            listItem.className = "list-group-item";

            listItem.innerHTML = `<a href="/download/${file.id}" target="_blank">${file.fileName}</a>
        <span class="float-end">${uploadDate}</span>`

            fileList.appendChild(listItem);
        })

    })
    .catch(error => {
            console.error(error);
        }
    )


document.getElementById("delete-directory").addEventListener("submit", (event) => {
    event.preventDefault();

    fetch(`http://localhost:8080/delete-directory/${fileOwner}`, {
        method: "DELETE",

    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }
            window.location.href = `/directory/${fileOwner}`;
        })
        .catch(error => {
            console.error(error);
        })
})

const addFile = document.getElementById("add-file");
addFile.href=`/add-file/${fileOwner}`;

