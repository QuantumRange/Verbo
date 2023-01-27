window.addEventListener('load', function () {
    const table = document.getElementById("user-table");

    for (let i = 0; i < table.children.length; i++) {
        let tr = table.children[i];
        const ch = tr.children;
        const id = ch[0].innerText;
        const roleDiv = ch[3];

        const infoDiv = roleDiv.children[0];
        const formDiv = roleDiv.children[1];

        formDiv.style.display = 'none';

        infoDiv.addEventListener('click', function () {
            infoDiv.style.display = 'none';
            formDiv.style.display = '';
        });

        formDiv.addEventListener('change', function () {
            const xmlhttp = new XMLHttpRequest();

            xmlhttp.open("POST", "/users/" + id + "/role", true);
            xmlhttp.setRequestHeader("Content-Type", "application/json");

            xmlhttp.onreadystatechange = function () {
                if (xmlhttp.readyState === 4 &&
                    xmlhttp.status === 200) {
                    location.reload();
                } else {
                    infoDiv.style.display = '';
                    formDiv.style.display = 'none';
                }
            };

            let selected = 'error';

            let options = formDiv.children;

            for (let j = 0; j < options.length; j++) {
                if (options[j].selected === true) {
                    selected = options[j].value;
                    break;
                }
            }

            xmlhttp.send(selected);
        });
    }
});