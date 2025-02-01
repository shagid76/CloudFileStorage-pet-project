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

            const listItem = document.createElement("li");
            listItem.className = "list-group-item";
            if(file.folder === false){

                const sizeInMB = (file.fileSize / 1000000).toPrecision(3);
                listItem.innerHTML = `
    <li class="list-group-item d-flex align-items-center justify-content-between">
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
                <li><a class="dropdown-item put-to-folder" href="#" data-file-id="${file.id}">Put to folder</a></li>
                <li><a class="dropdown-item download-file" href="#" data-file-id="${file.id}">Download</a></li>
            </ul>
        </div>
    </li>`;}else {
                listItem.innerHTML = `
    <li class="list-group-item d-flex align-items-center justify-content-between">
        <div>
            <a class="folder-page-link" href="#">${file.fileName}</a>
            <span class="ms-3">${uploadDate}</span>
        </div>
        <div class="dropdown">
            <a class="btn btn-sm p-1" href="#" role="button" id="dropdownMenuLink" data-bs-toggle="dropdown" aria-expanded="false">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-three-dots-vertical" viewBox="0 0 16 16">
                    <path d="M9.5 13a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0m0-5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0m0-5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0"/>
                </svg>
            </a>
            <ul class="dropdown-menu" aria-labelledby="dropdownMenuLink">
                <li><a class="dropdown-item delete-file" href="#" data-file-id="${file.id}">Delete folder</a></li>
                <li><a class="dropdown-item rename-file" href="#" data-file-id="${file.id}">Rename folder</a></li>
                <li><a class="dropdown-item copy-file" href="#" data-file-id="${file.id}">Copy folder</a></li>
                <li><a class="dropdown-item put-to-folder" href="#" data-file-id="${file.id}">Put to folder</a></li>
                <li><a class="dropdown-item download-file" href="#" data-file-id="${file.id}">Download</a></li>
            </ul>
        </div>
    </li>`
                fileList.addEventListener("click", event => {
                    const target = event.target;

                    if (target.classList.contains("folder-page-link")) {
                        event.preventDefault();
                        const folderName = target.textContent.trim();
                        window.location.href = `/folder/${owner}/${folderName}`;
                        return;
                    }
                });
            }
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
document.getElementById("create-folder").addEventListener("click", async () =>{
    const modal = document.getElementById("modal-window-create-folder");
    const closeModal = document.getElementById("close-modal-create-folder");
    const createForm = document.getElementById("create-folder-post");
    const folderName = document.getElementById("folderName");

    modal.style.display = "block";

    closeModal.onclick = () => modal.style.display = "none";
    window.onclick = event => { if (event.target === modal) modal.style.display = "none"; };

    createForm.onsubmit = async event => {
        event.preventDefault();
        const folderNameValue = folderName.value.trim();

        try {
            const response = await fetch(`/check-folder-name/${encodeURIComponent(folderNameValue)}?owner=${encodeURIComponent(owner)}`);
            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }
            const { isNameUnique } = await response.json();

            if (!isNameUnique) {
                alert("Folder with this name already exists!");
                return;
            }

            fetch(`/create-folder`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    folderName: folderNameValue,
                    parentId: parentId,
                    owner: owner
                })
            })
                .then(response => {
                    if (!response.ok) throw new Error(`Error: ${response.status}`);
                    return response.json();
                })
                .then(() => {
                    modal.style.display = "none";
                    location.reload();
                })
                .catch(error => {
                    console.error(error);
                });

        } catch (error) {
            console.error(error);
        }
    };
})
