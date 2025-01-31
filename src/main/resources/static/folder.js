const urlParts = window.location.pathname.split('/');
const parentId = urlParts[urlParts.length - 1];
const owner = urlParts[urlParts.length - 2];

fetch(`/files/folder/${parentId}`)
    .then(response => {
        if (!response.ok) {
            throw new Error(`Error: ${response.status}`);
        }
        return response.json();
    })
    .then(files => {
        const fileList = document.getElementById("fileList");
        const emptyMessage = document.getElementById("emptyMessage");

        if (files.length === 0) {
            emptyMessage.classList.remove("d-none");
            fileList.classList.add("d-none");
            return;
        }

        emptyMessage.classList.add("d-none");
        fileList.classList.remove("d-none");

        files.forEach(file => {
            const uploadDate = new Date(file.uploadDate).toLocaleString("en-US", {
                year: "numeric",
                month: "2-digit",
                day: "2-digit",
                hour: "2-digit",
                minute: "2-digit"
            });

            const sizeInMB = (file.fileSize / 1000000).toPrecision(3);
            const listItem = document.createElement("li");
            listItem.className = "list-group-item d-flex align-items-center justify-content-between";
            listItem.innerHTML = `
                <div>
                    <span>${file.fileName}</span>
                    <span class="ms-3">${uploadDate}</span>
                    <span class="ms-3">${sizeInMB} MB</span>
                </div>
                <div class="dropdown">
                    <a class="btn btn-sm p-1" href="#" role="button" id="dropdownMenuLink" data-bs-toggle="dropdown" aria-expanded="false">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-three-dots-vertical" viewBox="0 0 16 16">
                            <path d="M9.5 13a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0m0-5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0m0-5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0"/>
                        </svg>
                    </a>
                    <ul class="dropdown-menu" aria-labelledby="dropdownMenuLink">
                        <li><a class="dropdown-item delete-file" href="#" data-file-id="${file.id}">Delete file</a></li>
                        <li><a class="dropdown-item rename-file" href="#" data-file-id="${file.id}">Rename file</a></li>
                        <li><a class="dropdown-item copy-file" href="#" data-file-id="${file.id}">Copy file</a></li>
                        <li><a class="dropdown-item" href="#">Put to folder</a></li>
                        <li><a class="dropdown-item download-file" href="#" data-file-id="${file.id}">Download</a></li>
                    </ul>
                </div>
            `;
            fileList.appendChild(listItem);
        });
    });


fileList.addEventListener('click', event => {
    event.preventDefault();
    const target = event.target;
    const fileId = target.getAttribute('data-file-id');

    if (!fileId) return;

    if (target.classList.contains('delete-file')) {
        fetch(`/delete/${fileId}`, {method: "DELETE"})
            .then(response => {
                if (!response.ok) throw new Error(`Error: ${response.status}`);
                return response.text();
            })
            .then(() => location.reload())
            .catch(console.error);
    }
});

document.getElementById("back-to-directory").addEventListener("click", event => {
    const target = event.target;
    if (target.classList.contains("back-to-directory")) {
        event.preventDefault();
        window.location.href = `/directory/${owner}`;
    }
});
const addFileLink = document.getElementById("upload-file");
if (addFileLink) {
    addFileLink.href = `/add-file/${owner}/${parentId}`;
}
